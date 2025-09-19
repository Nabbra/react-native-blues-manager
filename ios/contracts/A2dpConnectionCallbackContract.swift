import Foundation

@objc
protocol A2dpConnectionCallbackContract {
    func onDeviceConnectionChanged()
    func onDeviceConnected()
    func onDeviceDisconnected()
}
