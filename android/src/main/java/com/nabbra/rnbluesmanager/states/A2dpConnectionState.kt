package com.nabbra.rnbluesmanager.states

import android.bluetooth.BluetoothA2dp

enum class A2dpConnectionState(val code: Int) {
  DISCONNECTED(BluetoothA2dp.STATE_DISCONNECTED),
  CONNECTED(BluetoothA2dp.STATE_CONNECTED)
}
