import Foundation

@objc
protocol ConnectionStateCallbackContract {
    func onStateChange(_ newState: BluetoothConnectionState, _ previousState: BluetoothConnectionState)
    func onBluetoothEnabled()
    func onBluetoothDisabled()
}
