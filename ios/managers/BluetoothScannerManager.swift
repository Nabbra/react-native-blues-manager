import Foundation
import AVKit
import React

@objc
class BluetoothScannerManager: NSObject {
    private var routePicker: AVRoutePickerView?
    
    @objc
    func startScan() {
        DispatchQueue.main.async {
            self.routePicker?.removeFromSuperview()
            
            let picker = AVRoutePickerView(frame: CGRect(x: 0, y: 0, width: 1, height: 1))
            picker.prioritizesVideoDevices = false
            picker.backgroundColor = .clear
            
            if let window = UIApplication.shared.windows.first(where: { $0.isKeyWindow }) {
                window.addSubview(picker)
                self.routePicker = picker
            }
            
            if let button = picker.subviews.first(where: { $0 is UIButton }) as? UIButton {
                button.sendActions(for: .touchUpInside)
            }
        }
    }
    
    @objc
    func stopScan() {
        DispatchQueue.main.async {
            self.routePicker?.removeFromSuperview()
            self.routePicker = nil
        }
    }
}
