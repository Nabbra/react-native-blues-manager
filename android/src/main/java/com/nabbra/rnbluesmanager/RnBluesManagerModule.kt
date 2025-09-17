package com.nabbra.rnbluesmanager

import android.Manifest
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.nabbra.rnbluesmanager.exceptions.BluesManagerException
import com.nabbra.rnbluesmanager.managers.BluetoothManager
import com.nabbra.rnbluesmanager.managers.ReceiversManager
import com.nabbra.rnbluesmanager.models.BluetoothNativeDevice


class RnBluesManagerModule(reactContext: ReactApplicationContext): ReactContextBaseJavaModule(reactContext), LifecycleEventListener {
  private var mAdapter: BluetoothAdapter? = null

  private val mReceiversManager = ReceiversManager(reactContext, ::sendRNEvent)
  private val mBluetoothManager = BluetoothManager(reactContext, mReceiversManager, ::sendRNEvent)

  override fun getName() = "RNBlues"

  private fun sendRNEvent(event: EventType, params: WritableMap?) {
    reactApplicationContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(event.eventName, params)
  }

  private fun initBlues() {
    val bluetoothManager = reactApplicationContext
      .getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
    mAdapter = bluetoothManager.adapter

    reactApplicationContext.addLifecycleEventListener(this)

    mBluetoothManager.initialize(mAdapter)
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
  private fun closeBlues() {
    mBluetoothManager.close(mAdapter)
    mAdapter = null
  }

  private fun isBluetoothAvailable(): Boolean = mAdapter != null
  private fun isBluetoothEnabled(): Boolean = mAdapter?.isEnabled == true

  @ReactMethod
  fun isBluetoothAvailable(promise: Promise) {
    promise.resolve(isBluetoothAvailable())
  }

  @ReactMethod
  fun isBluetoothEnabled(promise: Promise) {
    promise.resolve(isBluetoothEnabled())
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  @ReactMethod
  fun getConnectionState(address: String, promise: Promise) {
    if (isBluetoothEnabled()) {
      val device = mAdapter!!.getRemoteDevice(address)
      promise.resolve(mBluetoothManager.getA2dpAdapter()!!.getConnectionState(device))
    } else {
      promise.reject(
        BluesManagerException.BLUETOOTH_NOT_AVAILABLE.name,
        BluesManagerException.BLUETOOTH_NOT_AVAILABLE.message()
      )
    }
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  @ReactMethod
  fun requestBluetoothEnabled(promise: Promise) {
    sendRNEvent(EventType.BLUETOOTH_STATE_CHANGING, null)
    when {
      !isBluetoothAvailable() -> {
        promise.reject(
          BluesManagerException.BLUETOOTH_NOT_AVAILABLE.name,
          BluesManagerException.BLUETOOTH_NOT_AVAILABLE.message()
        )
      }
      isBluetoothEnabled() -> {
        promise.reject(
          BluesManagerException.ALREADY_ENABLED.name,
          BluesManagerException.ALREADY_ENABLED.message()
        )
      }
      else -> {
        val enabled = mAdapter!!.enable()
        if (enabled) {
          Log.d("RNBLUES", "Bluetooth enabled")
          sendRNEvent(EventType.BLUETOOTH_ENABLED, null)
          promise.resolve(true)
        } else {
          promise.resolve(false)
        }
      }
    }
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  @ReactMethod
  fun disableBluetooth(promise: Promise) {
    if (!isBluetoothAvailable()) {
      promise.reject(
        BluesManagerException.BLUETOOTH_NOT_AVAILABLE.name,
        BluesManagerException.BLUETOOTH_NOT_AVAILABLE.message()
      )
    } else {
      promise.resolve(mAdapter!!.disable())
    }
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  @ReactMethod
  fun deviceList(promise: Promise) {
    if (!isBluetoothEnabled()) {
      promise.reject(
        BluesManagerException.BLUETOOTH_NOT_ENABLED.name,
        BluesManagerException.BLUETOOTH_NOT_ENABLED.message()
      )
    } else {
      val bonded = Arguments.createArray()
      for (device in mAdapter!!.bondedDevices) {
        val nativeDevice = BluetoothNativeDevice(device)
        bonded.pushMap(nativeDevice.map())
      }
      promise.resolve(bonded)
    }
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
  @ReactMethod
  fun startScan(promise: Promise) {
    when {
      !isBluetoothEnabled() -> {
        promise.reject(
          BluesManagerException.BLUETOOTH_NOT_ENABLED.name,
          BluesManagerException.BLUETOOTH_NOT_ENABLED.message()
        )
      }
      mReceiversManager.isDiscoverReceiverAvailable() -> {
        promise.reject(
          BluesManagerException.BLUETOOTH_IN_DISCOVERY.name,
          BluesManagerException.BLUETOOTH_IN_DISCOVERY.message()
        )
      }
      else -> {
        mReceiversManager.registerDiscoveryReceiver(promise)
        mAdapter!!.startDiscovery()
        sendRNEvent(EventType.SCAN_STARTED, null)
      }
    }
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
  @ReactMethod
  fun stopScan() {
    mAdapter?.cancelDiscovery()
    sendRNEvent(EventType.SCAN_STOPPED, null)
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  @ReactMethod
  fun connectA2dp(address: String, promise: Promise) {
    if (!isBluetoothEnabled()) {
      promise.reject(
        BluesManagerException.BLUETOOTH_NOT_ENABLED.name,
        BluesManagerException.BLUETOOTH_NOT_ENABLED.message()
      )
      return
    }

    mReceiversManager.unregisterDiscoveryReceiver()
    mBluetoothManager.setPromise(promise)

    val device = mAdapter?.getRemoteDevice(address)
    if (device != null) {
      mBluetoothManager.setDevice(BluetoothNativeDevice(device))

      try {
        val mtdBond = device.javaClass.getMethod("createBond")
        mtdBond.invoke(device)
      } catch (e: Exception) {
        e.printStackTrace()
        mBluetoothManager.getPromise()?.reject(
          BluesManagerException.BONDING_UNAVAILABLE_API.name,
          BluesManagerException.BONDING_UNAVAILABLE_API.message()
        )
        mBluetoothManager.setPromise(null)
      }

      try {
        val connectMethod = mBluetoothManager.getA2dpAdapter()!!.javaClass.getMethod("connect", BluetoothDevice::class.java)
        connectMethod.invoke(mBluetoothManager.getA2dpAdapter(), device)
        mBluetoothManager.getPromise()?.resolve(mBluetoothManager.getDevice()!!.map())
      } catch (e: Exception) {
        e.printStackTrace()
        mBluetoothManager.getPromise()?.reject(
          BluesManagerException.CONNECTION_FAILED.name,
          BluesManagerException.CONNECTION_FAILED.message(device.name)
        )
      }
    } else {
      mBluetoothManager.getPromise()?.reject(
        BluesManagerException.BLUETOOTH_DEVICE_NOT_FOUND.name,
        BluesManagerException.BLUETOOTH_DEVICE_NOT_FOUND.message()
      )
    }

    mBluetoothManager.setPromise(null)
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  @ReactMethod
  fun getConnectedA2dpDevice(promise: Promise) {
    val device = mBluetoothManager.getConnectedA2dpDevice()
    promise.resolve(device?.map())
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  @ReactMethod
  fun disconnectA2dp(removeBond: Boolean?, promise: Promise) {
    val _removeBond = removeBond == true
    val device = mBluetoothManager.getConnectedA2dpDevice()
    if (device != null) {
      try {
        val disconnectMethod =
          BluetoothA2dp::class.java.getMethod("disconnect", BluetoothDevice::class.java)
        disconnectMethod.invoke(mBluetoothManager.getA2dpAdapter(), device.getDevice())
      } catch (e: Exception) {
        e.printStackTrace()
        promise.reject(
          BluesManagerException.DISCONNECTION_FAILED.name,
          BluesManagerException.DISCONNECTION_FAILED.message("${mBluetoothManager.getDevice()?.name}, ${mBluetoothManager.getDevice()?.address}")
        )
      }
      if (_removeBond) {
        try {
          val mtdRemoveBond =
            device.getDevice().javaClass.getMethod("removeBond")
          mtdRemoveBond.invoke(device.getDevice())
        } catch (e: Exception) {
          e.printStackTrace()
          promise.reject(
            BluesManagerException.REMOVE_BOND_FAILED.name,
            BluesManagerException.REMOVE_BOND_FAILED.message()
          )
        }
      }
    } else {
      promise.resolve(false)
    }
    mBluetoothManager.setDevice(null)
    sendRNEvent(EventType.DEVICE_DISCONNECTED, null)
    promise.resolve(true)
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  @ReactMethod
  fun close(promise: Promise) {
    disconnectA2dp(false, promise)
    closeBlues()
  }

  override fun initialize() {
    super.initialize()
    initBlues()
  }

  override fun onHostResume() {
    if (mAdapter == null) {
      initBlues()
    }
  }

  override fun onHostPause() {}
  override fun onHostDestroy() {}
}
