package com.aluma.laundry.data.machine.remote

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.machine.local.MachineLocalViewModel
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.data.machine.model.MachineRemote
import com.aluma.laundry.data.order.utils.RealtimeEvent
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.toJsonPrimitive
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.EOFException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.coroutines.cancellation.CancellationException

class MachineViewModel (
    private val storePreferences: StorePreferences,
    private val machineLocalViewModel: MachineLocalViewModel
): ViewModel() {
    private val client = PocketbaseClient(
        baseUrl = {
            protocol = URLProtocol.Companion.HTTPS
            host = "0f9489584773.ngrok-free.app"
        }
    )

    private val collection = "Machine"
    private var sseJob: Job? = null

    val sseIsConnected = MutableStateFlow(false)
    val sseId = MutableStateFlow<String?>(null)

    private val _token = MutableStateFlow<String?>(null)
    private val _idUser = MutableStateFlow<String?>(null)
    private val _idStore = MutableStateFlow<String?>(null)
    private val _isLoggedIn = MutableStateFlow(false)

    private val _machineRemote = MutableStateFlow<List<MachineRemote>>(emptyList())
    val machineRemote: StateFlow<List<MachineRemote>> = _machineRemote

    private val _machineRemoteFilter = MutableStateFlow<List<MachineRemote>>(emptyList())
    val machineRemoteFilter: StateFlow<List<MachineRemote>> = _machineRemoteFilter

    private val _selectedMachineRemote = MutableStateFlow<MachineRemote?>(null)
    val selectedMachineRemote: StateFlow<MachineRemote?> = _selectedMachineRemote

    fun setSelectedMachine(machineRemote: MachineRemote?) {
        _selectedMachineRemote.value = machineRemote
    }

    init {
        viewModelScope.launch {
            storePreferences.userIdUser.collectLatest { _idUser.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userIdStore.collectLatest { _idStore.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userToken.collectLatest {
                _token.value = it
                val loggedIn = !it.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                if (loggedIn) {
                    client.login(it)
                    fetchMachine()
                }
            }
        }
    }

    fun fetchMachine() {
        viewModelScope.launch {
            try {
                val fetched = client.records.getList<MachineRemote>(
                    collection,
                    page = 1,
                    perPage = 100
                )
                _machineRemote.value = fetched.items.reversed()

                _machineRemote.value.forEach { machine ->
                    val existing = machineLocalViewModel.getMachineById(machine.id!!)

                    if (existing == null) {
                        val newMachine = MachineLocal(
                            id = machine.id!!,
                            numberMachine = machine.numberMachine,
                            typeMachine = machine.typeMachine,
                            sizeMachine = machine.sizeMachine,
                            user = machine.user,
                            store = machine.store,
                            bluetoothAddress = machine.bluetoothAddress,
                            timer = machine.timer,
                            inUse = false,
                            timeOn = null,
                            order = null,
                        )
                        machineLocalViewModel.addMachine(newMachine)
                    } else {
                        val updated = existing.copy(
                            numberMachine = machine.numberMachine,
                            typeMachine = machine.typeMachine,
                            sizeMachine = machine.sizeMachine,
                            bluetoothAddress = machine.bluetoothAddress,
                            timer = machine.timer
                        )
                        machineLocalViewModel.updateMachine(updated)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Fetch machine failed", e)
            }
        }
    }

    fun filterMachine(type: Int, size: Boolean, stepMachine: Int) {
        viewModelScope.launch {
            val filtered = _machineRemote.value.filter { machine ->
                val typeMachine = machine.typeMachine
                val sizeMachine = machine.sizeMachine

                val matchType = when (type) {
                    0 -> !typeMachine
                    1 -> typeMachine
                    2 -> when (stepMachine) {
                        0 -> !typeMachine
                        1 -> typeMachine
                        else -> true
                    }
                    else -> false
                }

                val matchSize = sizeMachine == size

                matchType && matchSize
            }

            _machineRemoteFilter.value = filtered
            Log.d("SseClient", "Filtered machines: ${_machineRemoteFilter.value}")
        }
    }

    fun patchMachine(id: String?, machineRemote: MachineRemote) {
        if (id.isNullOrBlank()) {
            Log.e("MainViewModel", "Patch machine aborted: id is null or blank")
            return
        }

        viewModelScope.launch {
            try {
                client.records.update<MachineRemote>(
                    id = id,
                    sub = collection,
                    body = Json.Default.encodeToString(machineRemote)
                )
            } catch (e: Exception) {
                Log.e("MainViewModel", "Patch machine failed", e)
            }
        }
    }

    fun subscribeRealtimeMachine() {
        sseJob = viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            while (isActive) {
                try {
                    if (!sseIsConnected.value) {
                        Log.d("SseClient", "Trying to connect to SSE...")

                        client.httpClient.sse(path = "/api/realtime") {
                            try {
                                incoming
                                    .onEach { sseEvent ->
                                        sseIsConnected.value = true
                                        sseId.value = sseEvent.id

                                        Log.d("SseClient", "SSE Event: id=${sseEvent.id}, data=${sseEvent.data}")

                                        viewModelScope.launch {
                                            sendSubscribeRequest()
                                        }

                                        processEvent(sseEvent.data)
                                    }
                                    .catch { e ->
                                        sseIsConnected.value = false
                                        Log.e("SseClient", "SSE flow error (catch): ${e.message}", e)
                                    }
                                    .collect()
                            } catch (e: Exception) {
                                Log.e("SseClient", "SSE inner error", e)
                                sseIsConnected.value = false
                            }
                        }
                    } else {
                        Log.d("SseClient", "SSE already connected")
                    }
                } catch (e: EOFException) {
                    Log.e("SseClient", "EOF - SSE Connection closed by server", e)
                    sseIsConnected.value = false
                } catch (e: CancellationException) {
                    Log.w("SseClient", "SSE Cancelled (likely viewModelScope cleared)", e)
                    break // keluar dari loop
                } catch (e: Exception) {
                    Log.e("SseClient", "SSE connection error (outer catch)", e)
                    sseIsConnected.value = false
                }

                Log.d("SseClient", "Waiting 5s before retrying SSE connection...")
                delay(5000)
            }
        }
    }

    private suspend fun sendSubscribeRequest(): Boolean {
        val body = mapOf(
            "clientId" to sseId.value!!.toJsonPrimitive(),
            "subscriptions" to JsonArray(listOf(collection.toJsonPrimitive()))
        )

        val response = client.httpClient.post {
            url { path("/api/realtime") }
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        Log.d("SseClient", "SSE Subscription response: $response")
        return true
    }

    fun processEvent(jsonString: String?) {
        val json = Json { ignoreUnknownKeys = true }

        try {
            val element = json.parseToJsonElement(jsonString ?: return)
            if (element.jsonObject.containsKey("record")) {
                val event = json.decodeFromJsonElement<RealtimeEvent>(element)
                when (event.action) {
                    "create", "update", "delete" -> {
                        fetchMachine()
                        Log.d("SseClient", "SSE Event: ${event.action}")
                    }
                    else -> {
                        Log.d("SseClient", "Unknown SSE Action: ${event.action}")
                    }
                }
            } else {
                Log.d("SseClient", "No record in SSE event, skipping.")
            }
        } catch (e: Exception) {
            Log.e("SseClient", "Failed to parse SSE event JSON: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        sseJob?.cancel()
        Log.d("OrderViewModel", "SSE job cancelled on ViewModel clear")
    }
}