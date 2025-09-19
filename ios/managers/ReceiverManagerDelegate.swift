import Foundation

@objc
protocol ReceiverManagerDelegate: AnyObject {
    func onAuthorizationGranted()
    func onAuthorizationDenied()
}
