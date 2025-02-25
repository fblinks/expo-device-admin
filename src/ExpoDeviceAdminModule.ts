import { NativeModule, requireNativeModule } from "expo";

declare class ExpoDeviceAdminModule extends NativeModule {
  rebootDevice(): Promise<void>;
  setLockTaskFeatures(features: number): Promise<void>;
  lockEverythingExceptPowerButton(): Promise<void>;
  isDeviceOwner(): Promise<boolean>; // Ensure this matches the native function
}

// Load the native module from JSI
const ExpoDeviceAdmin = requireNativeModule<ExpoDeviceAdminModule>("ExpoDeviceAdmin");

export default ExpoDeviceAdmin;

