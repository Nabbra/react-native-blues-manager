import Foundation
import CoreBluetooth

@objc
class BluetoothAuthorizationReceiver: NSObject, CBCentralManagerDelegate {
    private var centralManager: CBCentralManager!
    private var callback: BluetoothAuthorizationCallbackContract
    private var previousStatus: BluetoothAuthorizationState = .denied
    
    init(callback: BluetoothAuthorizationCallbackContract) {
        self.callback = callback
        super.init()
        
        centralManager = CBCentralManager(delegate: self, queue: nil, options: [
            CBCentralManagerOptionShowPowerAlertKey: false
        ])
        previousStatus = Self.mapAuthorization()
        
        if previousStatus == .authorized {
            callback.onAuthorizationGranted()
        }
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        let newStatus = Self.mapAuthorization()
        if newStatus != previousStatus {
            previousStatus = newStatus
            
            switch newStatus {
            case .authorized:
                callback.onAuthorizationGranted()
            case .denied:
                callback.onAuthorizationDenied()
            default:
                break
            }
        }
    }
    
    private static func mapAuthorization() -> BluetoothAuthorizationState {
        if #available(iOS 13.0, *) {
            switch CBManager.authorization {
            case .allowedAlways: return .authorized
            case .denied:        return .denied
            case .restricted:    return .denied
            case .notDetermined: return .denied
            @unknown default:    return .denied
            }
        } else {
            return .authorized
        }
    }
}
