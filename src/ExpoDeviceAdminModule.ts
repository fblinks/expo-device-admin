import { NativeModule, requireNativeModule } from "expo-modules-core";

declare class ExpoDeviceAdminModule extends NativeModule {
  rebootDevice(): Promise<void>;
  setLockTaskFeatures(features: number): Promise<void>;
  lockEverythingExceptPowerButton(): Promise<void>;
  isDeviceOwner(): Promise<boolean>; // Matches the native function return type
}

const ExpoDeviceAdmin = requireNativeModule<ExpoDeviceAdminModule>("ExpoDeviceAdmin");

export default ExpoDeviceAdmin;
