import { NativeModules } from 'react-native';
import { BluetoothConnectionStates } from './types';

const { RNBlues } = NativeModules;

if (!RNBlues) {
  throw new Error('RNBlues native module not found. Did you rebuild after linking?');
}

export type NativeDeviceProps = {
  id: string;
  name?: string;
  address: string;
  bonded: boolean;
  extra?: Record<string, unknown>;
};

export type NativeDevice = NativeDeviceProps & {
  isConnected: () => Promise<BluetoothConnectionStates>;
  connect: () => Promise<NativeDevice>;
  disconnect: (removeBond?: boolean) => Promise<boolean>;
};

export function createNativeDevice(props: NativeDeviceProps): NativeDevice {
  const { id, name, address, bonded, extra } = props;

  return {
    id,
    name,
    address,
    bonded,
    extra,
    async isConnected() {
      return RNBlues.getConnectionState(address);
    },
    async connect() {
      const result = await RNBlues.connectA2dp(address);
      return createNativeDevice(result);
    },
    async disconnect(removeBond: boolean = false) {
      return RNBlues.disconnectA2dp(removeBond);
    },
  };
}

export function fromNative(nativeObj: any): NativeDevice {
  return createNativeDevice({
    id: nativeObj.id,
    name: nativeObj.name,
    address: nativeObj.address,
    bonded: nativeObj.bonded,
    extra: nativeObj.extra ?? {},
  });
}

export function fromNativeArray(nativeArr: any[]): NativeDevice[] {
  return nativeArr.map(fromNative);
}
