import Foundation
import CoreBluetooth
import AVFoundation
import React

@objc(RNBlues)
class RnBluesManager: RCTEventEmitter, ReceiverManagerDelegate {
    private lazy var receiverManager: ReceiverManager = {
        ReceiverManager(delegate: self, sendEvent: sendRNEvent)
    }()

    private lazy var bluetoothManager: BluetoothManager = {
        BluetoothManager(receiverManager: receiverManager, sendEvent: sendRNEvent)
    }()
    
    private lazy var scanner = BluetoothScannerManager()
    
    private let devicesService = AVAudioSessionService()
    
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    override func supportedEvents() -> [String]! {
        return [
            EventType.bluetoothStateChanging.eventName,
            EventType.bluetoothStateChanged.eventName,
            EventType.bluetoothEnabled.eventName,
            EventType.bluetoothDisabled.eventName,
            EventType.connectionStateChanged.eventName,
            EventType.deviceConnected.eventName,
            EventType.deviceDisconnected.eventName,
            EventType.error.eventName,
            EventType.deviceDiscovered.eventName,
            EventType.scanStarted.eventName,
            EventType.scanStopped.eventName
        ]
    }
    
    override class func moduleName() -> String! {
        return "RNBlues"
    }
    
    private func sendRNEvent(event: EventType, body: [String: Any]?) {
        sendEvent(withName: event.eventName, body: body)
    }
    
    private func isBluetoothEnabled() -> Bool {
        if let watcher = receiverManager.bluetoothWatcher {
            watcher.isBluetoothEnabled()
        } else {
            false
        }
    }
    
    func onAuthorizationGranted() {
        bluetoothManager.initialize()
    }
    
    func onAuthorizationDenied() {}
    
    @objc
    func initializeBluetooth(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        receiverManager.registerAuthorizationReceiver()
        resolve(true)
    }
    
    @objc
    func isBluetoothAvailable(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        resolve(true)
    }
    
    @objc
    func isBluetoothEnabled(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        resolve(isBluetoothEnabled())
    }
    
    @objc
    func requestBluetoothEnabled(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        if isBluetoothEnabled() {
            resolve(true)
        } else {
            reject("BLUETOOTH_NOT_INITIALIZED", "Bluetooth watcher not initialized", nil)
        }
    }
    
    @objc
    func disableBluetooth(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        reject("BLUETOOTH_NOT_SUPPORTED", "Cannot disable Bluetooth programmatically on iOS", nil)
    }
    
    @objc
    func deviceList(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        if !isBluetoothEnabled() {
            reject("BLUETOOTH_NOT_ENABLED", "Bluetooth is not enabled", nil)
            return
        }
        resolve(devicesService.getConnectedDevices())
    }
    
    @objc
    func startScan(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        scanner.startScan()
        sendRNEvent(event: .scanStarted, body: nil)
        resolve(true)
    }
    
    @objc
    func stopScan(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        scanner.stopScan()
        sendRNEvent(event: .scanStopped, body: nil)
        resolve(true)
    }
    
    @objc
    func getConnectedA2dpDevice(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        if let device = devicesService.getConnectedDevice() {
            resolve(device)
        } else {
            reject("NO_CONNECTED_DEVICE", "No A2dp device connected", nil)
        }
    }
    
    @objc
    func connectA2dp(_ address: String, resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
        guard isBluetoothEnabled() else {
            reject("BLUETOOTH_NOT_ENABLED", "Bluetooth is not enabled", nil)
            return
        }
        reject("BLUETOOTH_CONNECT_DEVICE", "Connecting A2dp device is not available on iOS. Mayble use `startScan` instead", nil)
    }
    
    @objc
    func disconnectA2dp(_ removeBond: NSNumber?, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        // On iOS we cannot programmatically disconnect A2DP devices.
        // Instead, we simulate disconnection for RN consistency.
        guard let device = devicesService.getConnectedDevice() else {
            resolve(false)
            return
        }
        sendRNEvent(event: .deviceDisconnected, body: [
            "name": device["name"] ?? "",
            "id": device["id"] ?? ""
        ])
    }
    
    @objc
    func close(_ resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        disconnectA2dp(0, resolver: resolve, rejecter: reject)
        bluetoothManager.unregisterAll()
    }
}
