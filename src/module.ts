import { NativeEventEmitter, NativeModules } from 'react-native';
import { EventHandler } from './types';
import { NativeDevice, fromNative, fromNativeArray } from './native-device';
import { entries, hasValue, isObject, tryCall } from './utils';

const { RNBlues } = NativeModules;

if (!RNBlues) {
  throw new Error('RNBlues native module not found. Did you rebuild after linking?');
}

const eventEmitter = new NativeEventEmitter(RNBlues);
const eventMap: Record<string, { remove: () => void }> = {};
let debugMode = false;

const log = {
  d: (...s: unknown[]) => debugMode && console.log('[NABBRA_BLUES]', ...s),
  w: (...s: unknown[]) => debugMode && console.warn('[NABBRA_BLUES]', ...s),
  e: (...s: unknown[]) => console.error('[NABBRA_BLUES]', ...s),
};

export const setDebugMode = (d: boolean) => {
  debugMode = d;
};

export const getRegisteredEventNames = (): string[] => Object.keys(eventMap);

export const removeBluesEvent = (eventName: string) => {
  if (getRegisteredEventNames().includes(eventName)) {
    eventMap[eventName].remove();
    delete eventMap[eventName];
    log.d(`removeBluesEvent: event ${eventName} removed.`);
  } else {
    throw new Error(`${eventName}`);
  }
};

export const getEventHandler = (eventName: string) => {
  return isObject(eventMap) && eventMap[eventName];
};

export const setEvent = (eventName: string, handler: EventHandler) => {
  if (getRegisteredEventNames().includes(eventName)) {
    log.d(`setEvent: event ${eventName} already registered, replacing.`);
    removeBluesEvent(eventName);
  }
  eventMap[eventName] = eventEmitter.addListener(eventName, handler);
  console.log(eventMap);
  return eventMap[eventName];
};

export const setEvents = (events: Record<string, EventHandler>) => {
  entries(events).forEach(([k, v]) => setEvent(k, v));
};

export const removeAllEvents = () => {
  Object.keys(eventMap).forEach(removeBluesEvent);
};

export const emitBluesEvent = (eventName: string) => {
  eventEmitter.emit(eventName);
};

export const initializeBluetooth = async (): Promise<boolean> => {
  return await RNBlues.initializeBluetooth();
}

export const isBluetoothAvailable = async (): Promise<boolean> => {
  return await RNBlues.isBluetoothAvailable();
};

export const isBluetoothEnabled = async (): Promise<boolean> => {
  return await RNBlues.isBluetoothEnabled();
};

export const enableBluetooth = async (onAlreadyEnabled?: () => void): Promise<boolean> => {
  let enabled = true;
  try {
    enabled = await RNBlues.requestBluetoothEnabled();
    log.d('requestBluetoothEnabled():', enabled);
  } catch (e: any) {
    if (e.message?.includes('already enabled')) {
      tryCall(onAlreadyEnabled);
    } else {
      throw e;
    }
  }
  if (!enabled) {
    throw new Error('failed to enable bluetooth');
  }
  return true;
};

export const disableBluetooth = async (): Promise<boolean> => {
  return await RNBlues.disableBluetooth();
};

export const getPairedDeviceList = async (): Promise<NativeDevice[]> => {
  const devices = await RNBlues.deviceList();
  log.d('getPairedDeviceList:', devices);
  return fromNativeArray(devices);
};

export const getConnectedDevice = async (): Promise<NativeDevice | undefined> => {
  const connected = await RNBlues.getConnectedA2dpDevice();
  if (connected) {
    const device = fromNative(connected);
    log.d('getConnectedDevice:', device);
    return device;
  }
  log.d('there is no connected device.');
  return undefined;
};

export const isConnected = async (): Promise<boolean> => {
  const device = await RNBlues.getConnectedA2dpDevice();
  return hasValue(device);
};

export const startScan = async () => {
  await stopScan();
  return await RNBlues.startScan();
};

export const stopScan = async () => {
  return await RNBlues.stopScan();
};

export const connect = async (deviceId: string) => {
  const result = await RNBlues.connectA2dp(deviceId);
  return fromNative(result);
};

export const disconnect = async (removeBond = false) => {
  return await RNBlues.disconnectA2dp(removeBond);
};

export const close = async () => {
  await RNBlues.close();
};

export default RNBlues;
