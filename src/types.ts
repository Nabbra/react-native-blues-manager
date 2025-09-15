export type EventHandler = (event: any) => void;
export type EventKeyValue = [string, EventHandler];

export enum BluetoothConnectionStates {
  STATE_DISCONNECTED,
  STATE_CONNECTING,
  STATE_CONNECTED,
  STATE_DISCONNECTING
}
