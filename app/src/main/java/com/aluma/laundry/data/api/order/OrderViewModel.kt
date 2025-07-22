package com.aluma.laundry.data.api.order

import android.util.Log
import com.aluma.laundry.data.api.machine.Machine
import com.aluma.laundry.data.api.order.model.Order
import com.aluma.laundry.data.api.order.model.RealtimeEvent
import com.aluma.laundry.data.datastore.StorePreferences
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.toJsonPrimitive
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.URLProtocol
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.io.EOFException
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.coroutines.cancellation.CancellationException

class OrderViewModel(
    private val storePreferences: StorePreferences
) : ViewModel() {

    private val client = PocketbaseClient(
        baseUrl = {
            protocol = URLProtocol.HTTPS
            host = "0f9489584773.ngrok-free.app"
        }
    )

    private val collection = "Order"

    private var sseJob: Job? = null

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _ordersFilter = MutableStateFlow<List<Order>>(emptyList())
    val ordersFilter: StateFlow<List<Order>> = _ordersFilter

    private val _selectedOrder = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder

    val sseIsConnected = MutableStateFlow(false)
    val sseId = MutableStateFlow<String?>(null)

    private val _token = MutableStateFlow<String?>(null)
    private val _idStore = MutableStateFlow<String?>(null)
    private val _idUser = MutableStateFlow<String?>(null)
    private val _isLoggedIn = MutableStateFlow(false)

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
                    fetchOrder()
                    subscribeRealtimeOrders()
                }
            }
        }
    }

    fun setSelectedOrder(order: Order?) {
        _selectedOrder.value = order
    }

    fun fetchOrder() {
        viewModelScope.launch {
            try {
                val fetched = client.records.getList<Order>(collection, page = 1, perPage = 200)
                _orders.value = fetched.items.reversed()
                filterOrdersByStepMachine()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("OrderViewModel", "❌ Fetch Orders failed", e)
            }
        }
    }

    fun filterOrdersByStepMachine(maxStep: Int = 4) {
        viewModelScope.launch {
            val filtered = _orders.value.filter { order ->
                order.stepMachine < maxStep
            }
            _ordersFilter.value = filtered
            Log.d("OrderViewModel", "Filtered orders (step < 4): ${filtered.size}")
        }
    }

    fun createOrder(order: Order) {
        viewModelScope.launch {
            try {
                client.records.create<Order>(
                    collection,
                    Json.encodeToString(order)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("OrderViewModel", "❌ Create Order failed", e)
            }
        }
    }

    fun patchOrder(id: String?, order: Order) {
        viewModelScope.launch {
            if (id.isNullOrBlank()) {
                Log.e("OrderViewModel", "❌ Patch Order skipped: ID is null or blank")
                return@launch
            }

            try {
                client.records.update<Order>(
                    id = id,
                    sub = collection,
                    body = Json.encodeToString(order)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("OrderViewModel", "❌ Patch Order failed", e)
            }
        }
    }

    fun subscribeRealtimeOrders() {
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
                        fetchOrder()
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