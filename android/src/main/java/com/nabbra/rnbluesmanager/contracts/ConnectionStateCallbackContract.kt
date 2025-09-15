package com.nabbra.rnbluesmanager.contracts

import com.nabbra.rnbluesmanager.states.BluetoothConnectionState

interface ConnectionStateCallbackContract {
  fun onStateChange(newState: BluetoothConnectionState?, oldState: BluetoothConnectionState?)
  fun onBluetoothEnabled()
  fun onBluetoothDisabled()
}
