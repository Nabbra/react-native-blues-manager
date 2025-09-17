package com.nabbra.rnbluesmanager.receivers

import android.bluetooth.BluetoothA2dp
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.nabbra.rnbluesmanager.contracts.A2dpConnectionCallbackContract

class A2dpConnectionReceiver(private val callback: A2dpConnectionCallbackContract): BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    val action = intent?.action ?: return

    if (action == BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) {
      val state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1)
      callback.onDeviceConnectionChanged()

      when (state) {
        BluetoothA2dp.STATE_CONNECTED -> callback.onDeviceConnected()
        BluetoothA2dp.STATE_DISCONNECTED -> callback.onDeviceDisconnected()
      }
    }
  }

  companion object {
    fun intentFilter(): IntentFilter = IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
  }
}
