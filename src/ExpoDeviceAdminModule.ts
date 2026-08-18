import { NativeModule, requireNativeModule } from "expo-modules-core";

declare class ExpoDeviceAdminModule extends NativeModule {
  isDeviceOwner(): Promise<boolean>;
  startKioskMode(): void;
  stopKioskMode(): void;
  checkIfKioskEnabled(): boolean;
  rebootDevice(): Promise<void>;
  addToLockTaskMode(): Promise<void>;
  setLockTaskFeatures(features: number): Promise<void>;
  enableImmersiveMode(): Promise<void>;

  LOCK_TASK_FEATURE_NONE: number;
  LOCK_TASK_FEATURE_GLOBAL_ACTIONS: number;
  LOCK_TASK_FEATURE_HOME: number;
  LOCK_TASK_FEATURE_OVERVIEW: number;
  LOCK_TASK_FEATURE_NOTIFICATIONS: number;
}

const ExpoDeviceAdmin =
  requireNativeModule<ExpoDeviceAdminModule>("ExpoDeviceAdmin");

export default ExpoDeviceAdmin;
