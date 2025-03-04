import { NativeModule, requireNativeModule } from "expo-modules-core";

declare class ExpoDeviceAdminModule extends NativeModule {
  rebootDevice(): Promise<void>;
  setLockTaskFeatures(features: number): Promise<void>;
  enableKioskMode(): Promise<void>;
  isDeviceOwner(): Promise<boolean>;
  startKioskMode(): void;
  exitKioskMode(): void;
  checkIfKioskEnabled(): boolean;
}

const ExpoDeviceAdmin = requireNativeModule<ExpoDeviceAdminModule>("ExpoDeviceAdmin");

export default ExpoDeviceAdmin;
