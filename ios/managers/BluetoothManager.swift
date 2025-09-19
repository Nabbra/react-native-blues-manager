import Foundation
import CoreBluetooth
import AVFoundation
import React

@objc
class BluetoothManager: NSObject {
    private var receiverManager: ReceiverManager
    private var sendEvent: ((EventType, [String: Any]?) -> Void)
    
    init(receiverManager: ReceiverManager, sendEvent: @escaping (EventType, [String: Any]?) -> Void) {
        self.receiverManager = receiverManager
        self.sendEvent = sendEvent
        super.init()
    }
    
    func initialize() {
        receiverManager.registerBluetoothReceiver()
        receiverManager.registerA2dpConnectionReceiver()
    }
    
    func unregisterAll() {
        receiverManager.unregisterBluetoothStateReceiver()
        receiverManager.unregisterA2dpStateReceiver()
    }
    
    deinit {
        receiverManager.unregisterBluetoothStateReceiver()
        receiverManager.unregisterA2dpStateReceiver()
    }
}
