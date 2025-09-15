package com.nabbra.rnbluesmanager.managers

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.nabbra.rnbluesmanager.EventType
import com.nabbra.rnbluesmanager.contracts.A2dpConnectionCallbackContract
import com.nabbra.rnbluesmanager.contracts.ConnectionStateCallbackContract
import com.nabbra.rnbluesmanager.contracts.DiscoveryCallbackContract
import com.nabbra.rnbluesmanager.exceptions.BluesManagerException
import com.nabbra.rnbluesmanager.models.BluetoothNativeDevice
import com.nabbra.rnbluesmanager.receivers.A2dpConnectionReceiver
import com.nabbra.rnbluesmanager.receivers.BluetoothConnectionStateReceiver
import com.nabbra.rnbluesmanager.receivers.DiscoveryReceiver
import com.nabbra.rnbluesmanager.states.BluetoothConnectionState

class ReceiversManager(
  private val reactContext: ReactApplicationContext,
  private val sendEvent: (EventType, WritableMap?) -> Unit
) {
  private var mDiscoveryReceiver: DiscoveryReceiver? = null
  private var mBluetoothConnectionStateReceiver: BluetoothConnectionStateReceiver? = null
  private var mConnectionStateReceiver: A2dpConnectionReceiver? = null

  fun registerBluetoothStateReceiver() {
    if (mBluetoothConnectionStateReceiver != null) {
      return
    }

    mBluetoothConnectionStateReceiver = BluetoothConnectionStateReceiver(
      object : ConnectionStateCallbackContract {
        override fun onStateChange(
          newState: BluetoothConnectionState?,
          oldState: BluetoothConnectionState?
        ) {
          val map = Arguments.createMap()
          map.putInt("state", newState?.code ?: 0)
          sendEvent(EventType.BLUETOOTH_STATE_CHANGED, map)
        }

        override fun onBluetoothEnabled() {
          sendEvent(EventType.BLUETOOTH_ENABLED, null)
        }

        override fun onBluetoothDisabled() {
          sendEvent(EventType.BLUETOOTH_DISABLED, null)
        }
      }
    )

    reactContext.registerReceiver(
      mBluetoothConnectionStateReceiver,
      BluetoothConnectionStateReceiver.intentFilter()
    )
  }

  fun registerConnectionStateReceiver(onDisconnect: () -> Unit) {
    if (mConnectionStateReceiver != null) {
      return
    }

    mConnectionStateReceiver = A2dpConnectionReceiver(object : A2dpConnectionCallbackContract {
      override fun onDeviceConnectionChanged() {
        sendEvent(EventType.CONNECTION_STATE_CHANGED, null)
      }

      override fun onDeviceConnected() {
        sendEvent(EventType.DEVICE_CONNECTED, null)
        unregisterDiscoveryReceiver()
      }

      override fun onDeviceDisconnected() {
        onDisconnect()
      }
    })

    reactContext.registerReceiver(
      mConnectionStateReceiver,
      A2dpConnectionReceiver.intentFilter()
    )
  }

  fun registerDiscoveryReceiver(promise: Promise) {
    if (mDiscoveryReceiver != null) {
      return
    }

    mDiscoveryReceiver = DiscoveryReceiver(object : DiscoveryCallbackContract {
      override fun onDeviceDiscovered(device: BluetoothNativeDevice) {
        sendEvent(EventType.DEVICE_DISCOVERED, device.map())
      }

      @Synchronized
      override fun onDiscoveryFinished(devices: Collection<BluetoothNativeDevice>) {
        val result = Arguments.createMap()
        val array = Arguments.createArray()
        for (device in devices) {
          array.pushMap(device.map())
        }
        result.putArray("result", array)
        promise.resolve(result)
        unregisterDiscoveryReceiver()
      }

      override fun onDiscoveryFailed(e: Throwable) {
        promise.reject(
          BluesManagerException.DISCOVERY_FAILED.name,
          BluesManagerException.DISCOVERY_FAILED.message(e.message ?: "")
        )
        sendEvent(EventType.ERROR, BluesManagerException.DISCOVERY_FAILED.map())
        unregisterDiscoveryReceiver()
      }
    })
  }

  fun unregisterDiscoveryReceiver() {
    try {
      reactContext.unregisterReceiver(mDiscoveryReceiver)
      mDiscoveryReceiver = null
    } catch (e: IllegalArgumentException) {
      Log.w("RNBLUES", e.message ?: "")
    }
  }

  fun unregisterBluetoothStateReceiver() {
    try {
      reactContext.unregisterReceiver(mBluetoothConnectionStateReceiver)
      mBluetoothConnectionStateReceiver = null
    } catch (e: IllegalArgumentException) {
      Log.w("RNBLUES", "IllegalArgumentException: ${e.message}")
    }
  }

  fun unregisterConnectionStateReceiver() {
    try {
      reactContext.unregisterReceiver(mConnectionStateReceiver)
      mConnectionStateReceiver = null
    } catch (e: IllegalArgumentException) {
      Log.d("RNBLUES", "IllegalArgumentException: ${e.message}")
    }
  }

  fun isDiscoverReceiverAvailable() = mDiscoveryReceiver != null
}
