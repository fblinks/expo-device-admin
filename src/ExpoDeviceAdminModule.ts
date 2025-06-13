import { NativeModule, requireNativeModule } from "expo-modules-core";

declare class ExpoDeviceAdminModule extends NativeModule {
  isDeviceOwner(): Promise<boolean>;
  startKioskMode(): void;
  stopKioskMode(): void;
  checkIfKioskEnabled(): boolean;
  rebootDevice(): Promise<void>;
  addToLockTaskMode(): Promise<void>;
  setLockTaskFeatures(features: number): Promise<void>;
  enableImmersiveMode(): void;
  disableImmersiveMode(): void;
}

const ExpoDeviceAdmin = requireNativeModule<ExpoDeviceAdminModule>("ExpoDeviceAdmin");

export default ExpoDeviceAdmin;
