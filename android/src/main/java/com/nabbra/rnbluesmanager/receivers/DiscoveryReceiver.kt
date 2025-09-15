package com.nabbra.rnbluesmanager.receivers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.nabbra.rnbluesmanager.contracts.DiscoveryCallbackContract
import com.nabbra.rnbluesmanager.models.BluetoothNativeDevice

class DiscoveryReceiver(private val callback: DiscoveryCallbackContract): BroadcastReceiver() {
  private val unpairedDevices: MutableMap<String, BluetoothNativeDevice> = mutableMapOf()

  fun getUnpairedDevices(): Map<String, BluetoothNativeDevice> = unpairedDevices

  override fun onReceive(context: Context?, intent: Intent?) {
    val action = intent?.action ?: return

    when (action) {
      BluetoothDevice.ACTION_FOUND -> {
        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        if (device != null && !unpairedDevices.containsKey(device.address)) {
          val nativeDevice = BluetoothNativeDevice(device)
          nativeDevice.putExtra(
            "rssi",
            intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
          )
          unpairedDevices[device.address] = nativeDevice
          callback.onDeviceDiscovered(nativeDevice)
        }
      }
      BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
        callback.onDiscoveryFinished(unpairedDevices.values)
      }
    }
  }

  companion object {
    fun intentFilter(): IntentFilter {
      return IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
      }
    }
  }
}
