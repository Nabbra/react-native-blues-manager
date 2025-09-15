package com.nabbra.rnbluesmanager.receivers

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.nabbra.rnbluesmanager.contracts.ConnectionStateCallbackContract
import com.nabbra.rnbluesmanager.states.BluetoothConnectionState

class BluetoothConnectionStateReceiver(private val callback: ConnectionStateCallbackContract): BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    val action = intent?.action ?: return

    if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
      val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
      when (state) {
        BluetoothAdapter.STATE_OFF -> {
          callback.onStateChange(BluetoothConnectionState.DISABLED, BluetoothConnectionState.ENABLED)
          callback.onBluetoothDisabled()
        }
        BluetoothAdapter.STATE_ON -> {
          callback.onStateChange(BluetoothConnectionState.ENABLED, BluetoothConnectionState.DISABLED)
          callback.onBluetoothEnabled()
        }
      }
    }
  }

  companion object {
    fun intentFilter(): IntentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
  }
}
