import Foundation
import CoreBluetooth
import React

@objc
class BluetoothConnectionStateReceiver: NSObject, CBCentralManagerDelegate {
    private var centralManager: CBCentralManager!
    private var callback: ConnectionStateCallbackContract
    private var previousState: BluetoothConnectionState = .disabled
    
    init(callback: ConnectionStateCallbackContract) {
        self.callback = callback
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        let newState: BluetoothConnectionState = (central.state == .poweredOn) ? .enabled : .disabled
        
        if newState != previousState {
            callback.onStateChange(newState, previousState)
            previousState = newState
            
            switch newState {
            case .enabled:
                callback.onBluetoothEnabled()
            case .disabled:
                callback.onBluetoothDisabled()
            }
        }
    }
    
    func isBluetoothEnabled() -> Bool {
        return centralManager.state == .poweredOn
    }
}
