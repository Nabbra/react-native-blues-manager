import Foundation
import AVFoundation
import React

@objcMembers
class BluetoothNativeDevice: NSObject {
    private var nameValue: String
    private var addressValue: String
    private var bondedValue: Bool
    private var extra: [String: Any] = [:]
    
    init(name: String, address: String, bonded: Bool = false) {
        self.nameValue = name
        self.addressValue = address
        self.bondedValue = bonded
        super.init()
    }
    
    var name: String { nameValue }
    var address: String { addressValue }
    var id: String { addressValue }
    var bonded: Bool { bondedValue }
    
    func getExtra(key: String) -> Any? {
        return extra[key]
    }

    func putExtra(key: String, value: Any) -> Any? {
        return extra.updateValue(value, forKey: key)
    }

    func map() -> [String: Any] {
        var mapped: [String: Any] = [
            "name": nameValue,
            "address": addressValue,
            "id": id,
            "bonded": bondedValue
        ]
        mapped["extra"] = extra
        return mapped
    }

    override var description: String {
        return "\(map())"
    }
}
