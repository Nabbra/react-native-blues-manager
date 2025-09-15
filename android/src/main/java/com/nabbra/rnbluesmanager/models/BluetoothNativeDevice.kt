package com.nabbra.rnbluesmanager.models

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.os.ParcelUuid
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.nabbra.rnbluesmanager.contracts.MappableContract

@SuppressLint("MissingPermission")
class BluetoothNativeDevice(private val device: BluetoothDevice): MappableContract {
  private val extra: MutableMap<String, Any> = mutableMapOf()

  val address: String
    get() = device.address

  val name: String?
    get() = device.name

  val bondState: Int
    get() = device.bondState

  val bluetoothClass: BluetoothClass?
    get() = device.bluetoothClass

  val uuids: Array<ParcelUuid>?
    get() = device.uuids

  fun getDevice(): BluetoothDevice = device

  fun getExtra(key: String): Any? = extra[key]

  fun putExtra(key: String, value: Any): Any? = extra.put(key, value)

  override fun map(): WritableMap? {
    val mapped = Arguments.createMap().apply {
      putString("name", device.name ?: device.address)
      putString("address", device.address)
      putString("id", device.address)
      putBoolean("bonded", device.bondState == BluetoothDevice.BOND_BONDED)

      device.bluetoothClass?.let { btClass ->
        putMap("deviceClass", Arguments.createMap().apply {
          putInt("deviceClass", btClass.deviceClass)
          putInt("majorClass", btClass.majorDeviceClass)
        })
      }

      putMap("extra", Arguments.createMap().apply {
        extra.forEach { (key, value) ->
          when (value) {
            is Int -> putInt(key, value)
            is String -> putString(key, value)
            is Boolean -> putBoolean(key, value)
            is Double -> putDouble(key, value)
            else -> putString(key, value.toString())
          }
        }
      })
    }

    return mapped
  }

  override fun toString(): String = map().toString()
}
