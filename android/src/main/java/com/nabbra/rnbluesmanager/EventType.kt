package com.nabbra.rnbluesmanager

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap

enum class EventType(val eventName: String) {
  BLUETOOTH_STATE_CHANGING("bluetoothStateChanging"),
  BLUETOOTH_STATE_CHANGED("bluetoothStateChanged"),
  BLUETOOTH_ENABLED("bluetoothEnabled"),
  BLUETOOTH_DISABLED("bluetoothDisabled"),
  CONNECTION_STATE_CHANGED("connectionStateChanged"),
  DEVICE_CONNECTED("deviceConnected"),
  DEVICE_DISCONNECTED("deviceDisconnected"),
  DEVICE_READ("deviceRead"),
  ERROR("error"),
  DEVICE_DISCOVERED("deviceDiscovered"),
  SCAN_STARTED("scanStarted"),
  SCAN_STOPPED("scanStopped"),
  SCAN_ERROR("scanError");

  companion object {
    fun eventName(): WritableMap {
      val events: WritableMap = Arguments.createMap()
      EventType.entries.forEach { event ->
        events.putString(event.eventName, event.eventName)
      }
      return events
    }
  }
}
