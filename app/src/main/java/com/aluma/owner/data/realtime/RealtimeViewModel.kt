package com.aluma.owner.data.realtime

import android.util.Log
import com.aluma.owner.data.datastore.StorePreferences
import com.aluma.owner.data.realtime.model.RealtimeEvent
import com.aluma.owner.data.realtime.model.RealtimeSse
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class RealtimeViewModel(
    private val storePreferences: StorePreferences,
    private val realtimeRepository: RealtimeRepository,
    private val client: PocketbaseClient
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    private val _sseConnected = MutableStateFlow(false)
    val sseConnected: StateFlow<Boolean> = _sseConnected

    private val _sseId = MutableStateFlow<String?>(null)
    private var sseJob: Job? = null

    /**
     * SharedFlow that emits the collection name whenever an SSE event
     * with action create/update/delete is received for that collection.
     * ViewModels collect this and re-fetch their data when their collection is emitted.
     */
    private val _realtimeEvent = MutableSharedFlow<String>()
    val realtimeEvent: SharedFlow<String> = _realtimeEvent.asSharedFlow()

    // All collections to subscribe
    private val ALL_COLLECTIONS = listOf(
        "LaundryStore",
        "LaundryService",
        "LaundryOrder",
        "LaundryMachine",
        "LaundryLogMachine",
        "LaundryIncome",
        "LaundryEmployee",
        "LaundryAttendance"
    )

    init {
        viewModelScope.launch {
            storePreferences.userToken.collectLatest { token ->
                if (!token.isNullOrEmpty()) {
                    client.login(token)
                    startRealtime()
                }
            }
        }
    }

    fun startRealtime() {
        sseJob?.cancel()
        sseJob = viewModelScope.launch(Dispatchers.IO) {
            while (this.isActive) {
                try {
                    _sseConnected.value = false
                    Log.d("SSE", "Connecting to Realtime SSE...")

                    realtimeRepository.withRealtimeEvents { evt: RealtimeSse ->
                        evt.id?.let { id ->
                            if (_sseId.value != id) {
                                _sseId.value = id
                                viewModelScope.launch {
                                    try {
                                        realtimeRepository.subscribeRealtime(id, ALL_COLLECTIONS)
                                        Log.d("SSE", "Subscribed to all collections with clientId: $id")
                                    } catch (e: Exception) {
                                        Log.e("SSE", "Subscribe failed: ${e.message}")
                                    }
                                }
                            }
                            _sseConnected.value = true
                        }
                        handleEventPayload(evt.data)
                    }
                } catch (e: Exception) {
                    _sseConnected.value = false
                    Log.e("SSE", "Realtime connection error: ${e.message}. Reconnecting in 5s...")
                    delay(5000)
                }
            }
        }
    }

    private suspend fun handleEventPayload(jsonString: String?) {
        if (jsonString.isNullOrBlank()) return
        try {
            val element = json.parseToJsonElement(jsonString)
            if (element.jsonObject.containsKey("record")) {
                val event = json.decodeFromJsonElement(RealtimeEvent.serializer(), element)
                when (event.action) {
                    "create", "update", "delete" -> {
                        // Try to extract collection name from the event
                        val collectionName = element.jsonObject["record"]
                            ?.jsonObject?.get("collectionName")
                            ?.let { it.toString().trim('"') }

                        if (collectionName != null) {
                            Log.d("SSE", "Event: ${event.action} on $collectionName")
                            _realtimeEvent.emit(collectionName)
                        } else {
                            // If we can't determine which collection, emit all
                            ALL_COLLECTIONS.forEach { _realtimeEvent.emit(it) }
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }
}
