package com.nabbra.rnbluesmanager.contracts

import com.nabbra.rnbluesmanager.models.BluetoothNativeDevice

interface DiscoveryCallbackContract {
  /**
   * Alerts when [android.bluetooth.BluetoothDevice.ACTION_FOUND] is fired. During discovery
   * devices may be found multiple times; differing values (such as RSSI) will be updated.
   */
  fun onDeviceDiscovered(device: BluetoothNativeDevice)

  /**
   * When discovery is completed ([android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED])
   * a collection of [NativeDevice]s is returned.
   */
  fun onDiscoveryFinished(devices: Collection<BluetoothNativeDevice>)

  /**
   * If an exception occurs during the discovery process.
   */
  fun onDiscoveryFailed(e: Throwable)
}
