import AVFoundation

@objc
class AVAudioSessionService: NSObject {
    private let audioSession = AVAudioSession.sharedInstance()
   
    @objc
    func getConnectedDevices() -> [[String: Any]] {
        return audioSession.currentRoute.outputs
            .filter { $0.portType == .bluetoothA2DP }
            .map { output in
                BluetoothNativeDevice(
                    name: output.portName,
                    address: output.uid,
                    bonded: true
                ).map()
            }
    }
    
    @objc
    func getConnectedDevice() -> [String: Any]? {
        return getConnectedDevices().first
    }
}
