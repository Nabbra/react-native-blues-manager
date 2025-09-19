import Foundation
import AVFoundation
import React

@objc
class A2dpConnectionReceiver: NSObject {
    private var callback: A2dpConnectionCallbackContract
    private var connectedDeviceUID: String?
    
    init(callback: A2dpConnectionCallbackContract) {
        self.callback = callback
        super.init()
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(audioRouteChanged),
            name: AVAudioSession.routeChangeNotification,
            object: nil
        )
        
        updateCurrentDevice()
    }
    
    @objc
    private func audioRouteChanged(_ notification: Notification) {
        updateCurrentDevice()
    }
    
    private func updateCurrentDevice() {
        let session = AVAudioSession.sharedInstance()
        if let output = session.currentRoute.outputs.first(where: { $0.portType == .bluetoothA2DP }) {
            if output.uid != connectedDeviceUID {
                connectedDeviceUID = output.uid
                callback.onDeviceConnectionChanged()
                callback.onDeviceConnected()
            }
        } else {
            if connectedDeviceUID != nil {
                connectedDeviceUID = nil
                callback.onDeviceConnectionChanged()
                callback.onDeviceDisconnected()
            }
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}
