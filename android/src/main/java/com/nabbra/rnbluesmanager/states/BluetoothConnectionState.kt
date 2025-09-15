package com.nabbra.rnbluesmanager.states

import android.bluetooth.BluetoothAdapter

enum class BluetoothConnectionState(val code: Int) {
  DISABLED(BluetoothAdapter.STATE_OFF),
  ENABLED(BluetoothAdapter.STATE_ON)
}
