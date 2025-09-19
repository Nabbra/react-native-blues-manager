import Foundation

@objc
enum EventType: Int {
    case bluetoothStateChanging
    case bluetoothStateChanged
    case bluetoothEnabled
    case bluetoothDisabled
    case connectionStateChanged
    case deviceConnected
    case deviceDisconnected
    case error
    case deviceDiscovered
    case scanStarted
    case scanStopped

    var eventName: String {
        switch self {
        case .bluetoothStateChanging: return "bluetoothStateChanging"
        case .bluetoothStateChanged: return "bluetoothStateChanged"
        case .bluetoothEnabled: return "bluetoothEnabled"
        case .bluetoothDisabled: return "bluetoothDisabled"
        case .connectionStateChanged: return "connectionStateChanged"
        case .deviceConnected: return "deviceConnected"
        case .deviceDisconnected: return "deviceDisconnected"
        case .error: return "error"
        case .deviceDiscovered: return "deviceDiscovered"
        case .scanStarted: return "scanStarted"
        case .scanStopped: return "scanStopped"
        }
    }
}
