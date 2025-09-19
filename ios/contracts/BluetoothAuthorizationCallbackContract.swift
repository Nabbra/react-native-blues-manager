import Foundation

@objc
protocol BluetoothAuthorizationCallbackContract {
    func onAuthorizationGranted()
    func onAuthorizationDenied()
}
