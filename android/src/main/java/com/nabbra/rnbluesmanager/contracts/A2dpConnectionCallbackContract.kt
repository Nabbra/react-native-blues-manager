package com.nabbra.rnbluesmanager.contracts

interface A2dpConnectionCallbackContract {
  fun onDeviceConnectionChanged()

  fun onDeviceConnected()

  fun onDeviceDisconnected()
}
