package gay.pancake.reflex;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;

import java.util.List;

/**
 * Interface for the NvLowLatencyVk API.
 *
 * @since Sept 23, 2020
 * @version 1.0
 */
public interface NvLowLatencyVk extends StdCallLibrary {
    NvLowLatencyVk INSTANCE = Native.load("NvLowLatencyVk", NvLowLatencyVk.class);

    /**
     * NvLL_VK_Status Values
     * All NvLLVk functions return one of these codes.
     *
     * @author Pancake
     */
    interface NvLL_VK_Status {

        /** Success. Request is completed. */
        int NVLL_VK_OK = 0;
        /** Generic error. */
        int NVLL_VK_ERROR = -1;
        /** NvLLVk support library cannot be loaded. */
        int NVLL_VK_LIBRARY_NOT_FOUND = -2;
        /** Not implemented in current driver installation. */
        int NVLL_VK_NO_IMPLEMENTATION = -3;
        /** NvLLVk support library has not been initialized. */
        int NVLL_VK_API_NOT_INITIALIZED = -4;
        /** The argument/parameter value is not valid or NULL. */
        int NVLL_VK_INVALID_ARGUMENT = -5;
        /** Invalid handle. */
        int NVLL_VK_INVALID_HANDLE = -8;
        /** An argument's structure version is not supported. */
        int NVLL_VK_INCOMPATIBLE_STRUCT_VERSION = -9;
        /** An invalid pointer, usually NULL, was passed as a parameter. */
        int NVLL_VK_INVALID_POINTER = -14;
        /** Could not allocate sufficient memory to complete the call. */
        int NVLL_VK_OUT_OF_MEMORY = -130;
        /** An API is still being called. */
        int NVLL_VK_API_IN_USE = -209;
        /** No Vulkan support. */
        int NVLL_VK_NO_VULKAN = -229;

    }

    /**
     * This function initializes the NvLLVk library (if not already initialized) but always increments the ref-counter.
     * This must be called before calling other NvLLVk functions.
     * Note: It is now mandatory to call NvLL_Initialize before calling any other NvLLVk API.
     * NvLL_VK_Unload should be called to unload the NvLLVk Library.
     *
     * @since Release: 80
     * @return This API can return any of the error codes enumerated in NvLL_VK_Status. If there are return error codes with specific meaning for this API, they are listed below.<br>NVLL_VK_LIBRARY_NOT_FOUND  Failed to load the NvLLVk support library
     */
    int NvLL_VK_Initialize();

    /**
     * Decrements the ref-counter and when it reaches ZERO, unloads NvLLVk library.
     * This must be called in pairs with NvLL_VK_Initialize.
     * If the client wants unload functionality, it is recommended to always call NvLL_VK_Initialize and NvLL_VK_Unload in pairs.
     * Unloading NvLLVk library is not supported when the library is in a resource locked state.
     * Some functions in the NvLLVk library initiates an operation or allocates certain resources and there are corresponding functions available, to complete the operation or free the allocated resources. All such function pairs are designed to prevent unloading NvLLVk library.
     *
     * @since Release: 80
     * @return This API can return any of the error codes enumerated in NvLL_VK_Status. If there are return error codes with specific meaning for this API, they are listed below.<br>NVLL_VK_API_IN_USE  An API is still being called, hence cannot unload requested driver.
     */
    int NvLL_VK_Unload();

    /**
     * This function has to be used to initialize a Vulkan device as a low latency device.
     * The driver initializes a set of parameters to be used in subsequent low latency API calls for this device.
     * The API will allocate and return a VkSemaphore (signalSemaphoreHandle) which will be signaled based on subsequent calls to NvLL_VK_Sleep.
     *
     * @since Release: 455
     * @param vkDevice The Vulkan device handle.
     * @param signalSemaphoreHandle Pointer to a VkSemaphore handle that is signaled in Sleep.
     * @return This API can return any of the error codes enumerated in NvLL_VK_Status. If there are return error codes with specific meaning for this API, they are listed below.
     */
    int NvLL_VK_InitLowLatencyDevice(long vkDevice, PointerByReference signalSemaphoreHandle);

    /**
     * This function releases the set of low latency device parameters.
     *
     * @since Release: 455
     * @param vkDevice The Vulkan device handle.
     * @return This API can return any of the error codes enumerated in NvLL_VK_Status. If there are return error codes with specific meaning for this API, they are listed below.
     */
    int NvLL_VK_DestroyLowLatencyDevice(long vkDevice);

    class NVLL_VK_GET_SLEEP_STATUS_PARAMS extends Structure {

        /** Is low latency mode enabled? */
        public boolean bLowLatencyMode;

        @Override
        protected List<String> getFieldOrder() {
            return List.of("bLowLatencyMode");
        }

    }

    /**
     * This function can be used to get the latest sleep status.
     * bLowLatencyMode indicates whether low latency mode is currently enabled in the driver.
     * Note that it may not always reflect the previously requested sleep mode, as the feature may not be available on the platform, or the setting may have been overridden by the control panel, for example.
     *
     * @since Release: 455
     * @param vkDevice The Vulkan device handle.
     * @param pGetSleepStatusParams Sleep status params.
     * @return This API can return any of the error codes enumerated in NvLL_VK_Status. If there are return error codes with specific meaning for this API, they are listed below.
     */
    int NvLL_VK_GetSleepStatus(long vkDevice, NVLL_VK_GET_SLEEP_STATUS_PARAMS pGetSleepStatusParams);

    class NVLL_VK_SET_SLEEP_MODE_PARAMS extends Structure {

        /** Low latency mode enable/disable. */
        public boolean bLowLatencyMode;
        /** Request maximum GPU clock frequency regardless of workload. */
        public boolean bLowLatencyBoost;
        /** Minimum frame interval in microseconds. 0 = No frame rate limit. */
        public int minimumIntervalUs;

        @Override
        protected List<String> getFieldOrder() {
            return List.of("bLowLatencyMode", "bLowLatencyBoost", "minimumIntervalUs");
        }

    }

    /**
     * This function can be used to update the sleep mode dynamically.
     * The settings are not dependent on each other, meaning low latency mode can be enabled/disabled regardless of whether minimum interval is set or not.
     * The former is to intelligently lower latency without impacting frame rate. The latter is to limit frame rate (e.g. minimumIntervalUs = 10000 limits frame rate to 100 FPS).
     * They work well separately and/or together.
     * Note that minimumIntervalUs usage is not limited to lowering latency, so feel free to use it to limit frame rate for menu, cut scenes, etc.
     * The bLowLatencyBoost parameter will request for the GPU to run at max clocks even in scenarios where it is idle most of the frame and would normally try to downclock to save power.
     * This can decrease latency in certain CPU-limited scenarios. While this function can be called as often as needed, it is not necessary or recommended to call this too frequently (e.g. every frame), as the settings persist for the target device.
     *
     * @since Release: 455
     * @param vkDevice The Vulkan device handle.
     * @param pSetSleepModeParams Sleep mode params.
     * @return This API can return any of the error codes enumerated in NvLL_VK_Status. If there are return error codes with specific meaning for this API, they are listed below.
     */
    int NvLL_VK_SetSleepMode(long vkDevice, NVLL_VK_SET_SLEEP_MODE_PARAMS pSetSleepModeParams);

    /**
     * It is recommended to call this function at the very start of each frame (e.g. before input sampling).
     * If there is a need to sleep, due to frame rate limit and/or low latency features for example, this call provides an entry point for the driver to sleep at the optimal point in time to achieve the lowest latency.
     * It is recommended to call this function even when low latency mode is disabled and minimum interval is 0.
     * Other features, such as the Maximum Frame Rate setting, could be enabled in the NVIDIA control panel and benefit from this.
     * It is OK to start (or stop) using this function at any time. However, when using this function, it must be called exactly once on each frame.
     * Each frame, the signalValue should usually be increased by 1, and this function should be called with the new value.
     * Then, vkWaitSemaphores (with a large timeout specified) should be called and will block until the semaphore is signaled.
     *
     * @since Release: 455
     * @param vkDevice The Vulkan device handle.
     * @param signalValue Value that will be signaled in signalSemaphoreHandle semaphore at Sleep.
     * @return This API can return any of the error codes enumerated in NvLL_VK_Status. If there are return error codes with specific meaning for this API, they are listed below.
     */
    int NvLL_VK_Sleep(long vkDevice, long signalValue);

    class NVLL_VK_LATENCY_RESULT_PARAMS extends Structure {

        public static class vkFrameReport extends Structure {

            public long frameID;
            public long inputSampleTime;
            public long simStartTime;
            public long simEndTime;
            public long renderSubmitStartTime;
            public long renderSubmitEndTime;
            public long presentStartTime;
            public long presentEndTime;
            public long driverStartTime;
            public long driverEndTime;
            public long osRenderQueueStartTime;
            public long osRenderQueueEndTime;
            public long gpuRenderStartTime;
            public long gpuRenderEndTime;

            @Override
            protected List<String> getFieldOrder() {
                return List.of("frameID", "inputSampleTime", "simStartTime", "simEndTime", "renderSubmitStartTime", "renderSubmitEndTime", "presentStartTime", "presentEndTime", "driverStartTime", "driverEndTime", "osRenderQueueStartTime", "osRenderQueueEndTime", "gpuRenderStartTime", "gpuRenderEndTime");
            }
        }

        public vkFrameReport[] frameReport = new vkFrameReport[64];

        @Override
        protected List<String> getFieldOrder() {
            return List.of("frameReport");
        }

    }

    /**
     * Get a latency report including the timestamps of the application latency markers set with NvLL_VK_SetLatencyMarker as well as driver, OS queue and graphics hardware times.
     * Requires calling NvLL_VK_SetLatencyMarker with incrementing frameID for valid results.
     * Rendering for at least 90 frames is recommended to properly fill out the structure.
     * The newest completed frame is at the end (element 63) and is preceded by older frames.
     * If not enough frames are valid then all frames members are returned with all zeroes.
     *
     * @since Release: 455
     * @param vkDevice The Vulkan device handle.
     * @param pGetLatencyResultParams The latency result structure.
     * @return This API can return any of the error codes enumerated in NvLL_VK_Status. If there are return error codes with specific meaning for this API, they are listed below.
     */
    int NvLL_VK_GetLatency(long vkDevice, NVLL_VK_LATENCY_RESULT_PARAMS pGetLatencyResultParams);

    interface NVLL_VK_LATENCY_MARKER_TYPE {
        int VK_SIMULATION_START = 0;
        int VK_SIMULATION_END = 1;
        int VK_RENDERSUBMIT_START = 2;
        int VK_RENDERSUBMIT_END = 3;
        int VK_PRESENT_START = 4;
        int VK_PRESENT_END = 5;
        int VK_INPUT_SAMPLE = 6;
        int VK_TRIGGER_FLASH = 7;
        int VK_PC_LATENCY_PING = 8;
        int VK_OUT_OF_BAND_RENDERSUBMIT_START = 9;
        int VK_OUT_OF_BAND_RENDERSUBMIT_END = 10;
        int VK_OUT_OF_BAND_PRESENT_START = 11;
        int VK_OUT_OF_BAND_PRESENT_END = 12;
    }

    /**
     * Parameters for NvLLVkSetLatencyMarker.
     *
     * @author Pancake
     */
    class NVLL_VK_LATENCY_MARKER_PARAMS extends Structure {

        public long frameID;
        public int markerType;

        @Override
        protected List<String> getFieldOrder() {
            return List.of("frameID", "markerType");
        }

    }

    /**
     * Set a latency marker to be tracked by the NvLL_VK_GetLatency function.
     * VK_SIMULATION_START must be the first marker sent in a frame, after the frame's Sleep call (if used).
     * VK_INPUT_SAMPLE may be sent to record the moment user input was sampled and should come between VK_SIMULATION_START and VK_SIMULATION_END.
     * VK_RENDERSUBMIT_START should come before any Vulkan API calls are made for the given frame.
     * VK_RENDERSUBMIT_END should come at the end of the frame render work submission.
     * VK_PRESENT_START and VK_PRESENT_END should wrap the Present call and may be used either before or after the VK_RENDERSUBMIT_END.
     * VK_TRIGGER_FLASH tells the driver to render its flash indicator on top of the frame for latency testing, typically driven by a mouse click.
     * The frameID can start at an abitrary value but must strictly increment from that point forward for consistent results.
     *
     * @since Release: 455
     * @param vkDevice The Vulkan device handle.
     * @param pSetLatencyMarkerParams The latency marker structure.
     * @return This API can return any of the error codes enumerated in NvLL_VK_Status. If there are return error codes with specific meaning for this API, they are listed below.
     */
    int NvLL_VK_SetLatencyMarker(long vkDevice, NVLL_VK_LATENCY_MARKER_PARAMS pSetLatencyMarkerParams);

    interface NVLL_VK_OUT_OF_BAND_QUEUE_TYPE {
        int VK_OUT_OF_BAND_QUEUE_TYPE_RENDER = 0;
        int VK_OUT_OF_BAND_QUEUE_TYPE_PRESENT = 1;
    }

    /**
     * Notifies the driver that this command queue runs out of band from the application's frame cadence.
     *
     * @since Release: 520
     * @param vkDevice The Vulkan device handle.
     * @param queueHandle The VkQueue
     * @param queueType The type of out of band VkQueue
     * @return This API can return any of the error codes enumerated in NvLL_VK_Status. If there are return error codes with specific meaning for this API, they are listed below.
     */
    int NvLL_VK_NotifyOutOfBandQueue(long vkDevice, long queueHandle, int queueType);

}
