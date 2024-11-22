import { NativeModule, requireNativeModule } from "expo";

declare class ExpoDeviceAdminModule extends NativeModule {
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoDeviceAdminModule>("ExpoDeviceAdmin");
