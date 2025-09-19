import Foundation
import CoreBluetooth
import React

@objc
class ReceiverManager: NSObject {
    var bluetoothWatcher: BluetoothConnectionStateReceiver?
    var a2dpConnectionWatcher: A2dpConnectionReceiver?
    private var authWatcher: BluetoothAuthorizationReceiver?
    private var sendEvent: ((EventType, [String: Any]?) -> Void)
    
    weak var delegate: ReceiverManagerDelegate?
    
    init(delegate: ReceiverManagerDelegate?, sendEvent: @escaping (EventType, [String: Any]?) -> Void) {
        self.delegate = delegate
        self.sendEvent = sendEvent
        super.init()
    }
    
    func registerBluetoothReceiver() {
        if bluetoothWatcher != nil { return }
        bluetoothWatcher = BluetoothConnectionStateReceiver(callback: self)
    }
    
    func unregisterBluetoothStateReceiver() {
        bluetoothWatcher = nil
    }
    
    func registerA2dpConnectionReceiver() {
        if a2dpConnectionWatcher != nil { return }
        a2dpConnectionWatcher = A2dpConnectionReceiver(callback: self)
    }
    
    func unregisterA2dpStateReceiver() {
        a2dpConnectionWatcher = nil
    }
    
    func registerAuthorizationReceiver() {
        if authWatcher != nil { return }
        authWatcher = BluetoothAuthorizationReceiver(callback: self)
    }
    
    func unregisterAuthorizationReceiver() {
        authWatcher = nil
    }
}

extension ReceiverManager: ConnectionStateCallbackContract {
    func onStateChange(_ newState: BluetoothConnectionState, _ previousState: BluetoothConnectionState) {
        let map: [String: Any] = ["state": newState.rawValue]
        sendEvent(.bluetoothStateChanged, map)
    }
    
    func onBluetoothEnabled() {
        sendEvent(.bluetoothEnabled, nil)
    }
    
    func onBluetoothDisabled() {
        sendEvent(.bluetoothDisabled, nil)
    }
}

extension ReceiverManager: A2dpConnectionCallbackContract {
    func onDeviceConnectionChanged() {
        sendEvent(.connectionStateChanged, nil)
    }
    
    func onDeviceConnected() {
        sendEvent(.deviceConnected, nil)
    }
    
    func onDeviceDisconnected() {
        sendEvent(.deviceDisconnected, nil)
    }
}

extension ReceiverManager: BluetoothAuthorizationCallbackContract {
    func onAuthorizationGranted() {
        delegate?.onAuthorizationGranted()
    }
    
    func onAuthorizationDenied() {
        delegate?.onAuthorizationDenied()
    }
}
