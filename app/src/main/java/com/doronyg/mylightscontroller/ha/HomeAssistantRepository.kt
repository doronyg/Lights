import com.doronyg.mylightscontroller.Settings
import com.doronyg.mylightscontroller.ha.HaDevice
import com.doronyg.mylightscontroller.ha.HomeAssistantApi
import com.doronyg.mylightscontroller.ha.HomeAssistantInterface
import com.doronyg.mylightscontroller.ha.mock.HomeAssistantApiMock

class HomeAssistantRepository private constructor() : HomeAssistantInterface {

    private val listeners = mutableListOf<HomeAssistantInterface.Listener>()

    private var api: HomeAssistantInterface =
        if (Settings.useMockHaData) HomeAssistantApiMock
        else HomeAssistantApi

    private val apiListener = object : HomeAssistantInterface.Listener {
        override fun onUpdate() {
            listeners.forEach { it.onUpdate() }
        }
    }

    init {
        api.registerListener(apiListener)
    }

    // Switch API if setting changed
    private fun ensureCorrectApi() {
        val shouldUseMock = Settings.useMockHaData
        val newApi =
            if (shouldUseMock) HomeAssistantApiMock
            else HomeAssistantApi

        if (api !== newApi) {
            api.deregisterListener(apiListener)
            api = newApi
            api.registerListener(apiListener)
            listeners.forEach { it.onUpdate() }
        }
    }

    // --- Listener management ---
    override fun registerListener(listener: HomeAssistantInterface.Listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    override fun deregisterListener(listener: HomeAssistantInterface.Listener) {
        listeners.remove(listener)
    }

    // --- Fetch devices ---
    override fun fetchAllLights(
        refresh: Boolean,
        onResult: (List<HaDevice>) -> Unit
    ) {
        ensureCorrectApi()

        api.fetchAllLights(refresh) { devices ->

            // If using real API and saving is enabled → update mock storage
            if (api === HomeAssistantApi && Settings.saveMockHaData) {
                HomeAssistantApiMock.populateMockData(devices)
            }

            onResult(devices)
        }
    }

    // --- Control lights ---
    override fun turnOnLights(devices: List<HaDevice>, brightnessPct: Int?) {
        ensureCorrectApi()
        api.turnOnLights(devices, brightnessPct)
    }

    override fun turnOffLights(devices: List<HaDevice>) {
        ensureCorrectApi()
        api.turnOffLights(devices)
    }

    companion object {
        val instance: HomeAssistantRepository by lazy {
            HomeAssistantRepository()
        }
    }
}