import ExpoDeviceAdmin from 'expo-device-admin';
import { useState } from 'react';
import { Button, SafeAreaView, ScrollView, Text, View } from 'react-native';

export default function App() {
  const [isDeviceOwner, setIsDeviceOwner] = useState<boolean | null>(null);
  const [isKioskEnabled, setIsKioskEnabled] = useState<boolean | null>(null);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.container}>
        <Text style={styles.header}>Module API Example</Text>
        <Group name="Device owner">
          <Button
            title="Check if device owner"
            onPress={async () => {
              setIsDeviceOwner(await ExpoDeviceAdmin.isDeviceOwner());
            }}
          />
          <Text>{isDeviceOwner === null ? 'Unknown' : String(isDeviceOwner)}</Text>
        </Group>
        <Group name="Kiosk mode">
          <Button title="Start kiosk mode" onPress={() => ExpoDeviceAdmin.startKioskMode()} />
          <Button title="Stop kiosk mode" onPress={() => ExpoDeviceAdmin.stopKioskMode()} />
          <Button
            title="Check if kiosk enabled"
            onPress={() => setIsKioskEnabled(ExpoDeviceAdmin.checkIfKioskEnabled())}
          />
          <Text>{isKioskEnabled === null ? 'Unknown' : String(isKioskEnabled)}</Text>
        </Group>
        <Group name="Lock task">
          <Button
            title="Add current package to lock task"
            onPress={async () => {
              await ExpoDeviceAdmin.addToLockTaskMode();
            }}
          />
          <Button
            title="Set as persistent Home activity"
            onPress={async () => {
              await ExpoDeviceAdmin.setAsPersistentHomeActivity();
            }}
          />
          <Button
            title="Set lock task features (home + overview)"
            onPress={async () => {
              await ExpoDeviceAdmin.setLockTaskFeatures(
                ExpoDeviceAdmin.LOCK_TASK_FEATURE_HOME | ExpoDeviceAdmin.LOCK_TASK_FEATURE_OVERVIEW
              );
            }}
          />
        </Group>
        <Group name="Device control">
          <Button
            title="Reboot device"
            onPress={async () => {
              await ExpoDeviceAdmin.rebootDevice();
            }}
          />
          <Button
            title="Enable immersive mode"
            onPress={async () => {
              await ExpoDeviceAdmin.enableImmersiveMode();
            }}
          />
          <Button
            title="Disable immersive mode"
            onPress={async () => {
              await ExpoDeviceAdmin.disableImmersiveMode();
            }}
          />
        </Group>
      </ScrollView>
    </SafeAreaView>
  );
}

function Group(props: { name: string; children: React.ReactNode }) {
  return (
    <View style={styles.group}>
      <Text style={styles.groupHeader}>{props.name}</Text>
      {props.children}
    </View>
  );
}

const styles = {
  header: {
    fontSize: 30,
    margin: 20,
  },
  groupHeader: {
    fontSize: 20,
    marginBottom: 20,
  },
  group: {
    margin: 20,
    backgroundColor: '#fff',
    borderRadius: 10,
    padding: 20,
  },
  container: {
    flex: 1,
    backgroundColor: '#eee',
  },
};
