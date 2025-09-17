package com.nabbra.rnbluesmanager.managers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.util.Log
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.nabbra.rnbluesmanager.EventType
import com.nabbra.rnbluesmanager.exceptions.BluesManagerException
import com.nabbra.rnbluesmanager.models.BluetoothNativeDevice

@SuppressLint("MissingPermission")
class BluetoothManager(
    private val reactContext: ReactApplicationContext,
    private val mReceiversManager: ReceiversManager,
    private val sendEvent: (EventType, WritableMap?) -> Unit
) {
    private var mConnectPromise: Promise? = null

    private var mDevice: BluetoothNativeDevice? = null

    private var mA2dp: BluetoothA2dp? = null

    fun initialize(mAdapter: BluetoothAdapter?) {
        mAdapter?.getProfileProxy(
            reactContext,
            object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    if (profile == BluetoothProfile.A2DP) {
                        mA2dp = proxy as BluetoothA2dp
                    }
                }

                override fun onServiceDisconnected(profile: Int) {
                    if (profile == BluetoothProfile.A2DP) {
                        mA2dp = null
                    }
                }
            },
            BluetoothProfile.A2DP
        )

        mReceiversManager.registerBluetoothStateReceiver()
        mReceiversManager.registerConnectionStateReceiver {
          mDevice = null
          if (mConnectPromise != null) {
            mConnectPromise!!.reject(
              BluesManagerException.ALREADY_CONNECTING.name,
              BluesManagerException.ALREADY_CONNECTING.message(mDevice?.name ?: "")
            )
            mConnectPromise = null
          } else {
            sendEvent(EventType.DEVICE_DISCONNECTED, null)
          }
        }
    }

    fun close(mAdapter: BluetoothAdapter?) {
        mReceiversManager.unregisterBluetoothStateReceiver()
        mReceiversManager.unregisterConnectionStateReceiver()
        mAdapter?.let {
            it.cancelDiscovery()
            it.closeProfileProxy(BluetoothProfile.A2DP, mA2dp)
        }
    }

    fun getConnectedA2dpDevice(): BluetoothNativeDevice? {
        return if (mA2dp != null) {
            val devices = mA2dp!!.connectedDevices
            mDevice = if (devices.isNotEmpty()) {
                BluetoothNativeDevice(devices[0])
            } else {
                null
            }
            mDevice
        } else null
    }

    fun getA2dpAdapter() = mA2dp

    fun getDevice() = mDevice

    fun setDevice(device: BluetoothNativeDevice?) {
        mDevice = device
    }

    fun setPromise(mPromise: Promise?) {
        mConnectPromise = mPromise
    }

    fun getPromise() = mConnectPromise
}
