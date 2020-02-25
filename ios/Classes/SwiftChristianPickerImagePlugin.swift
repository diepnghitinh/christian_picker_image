import Flutter
import UIKit
import Lightbox

public class SwiftChristianPickerImagePlugin: NSObject, FlutterPlugin, ImagePickerDelegate {
    
    var controller: FlutterViewController!
    var imagesResult: FlutterResult?
    var messenger: FlutterBinaryMessenger;
    var arrImage = [UIImage]()
    
    public func wrapperDidPress(_ imagePicker: ImagePickerController, images: [UIImage]) {
        guard images.count > 0 else { return }
        let lightboxImages = images.map {
            return LightboxImage(image: $0)
        }
        
        let lightbox = LightboxController(images: lightboxImages, startIndex: 0)

        lightbox.modalPresentationStyle = .fullScreen
        lightbox.dynamicBackground = false
        LightboxConfig.CloseButton.text = "Close"
        imagePicker.present(lightbox, animated: true, completion: nil)
    }
    
    public func doneButtonDidPress(_ imagePicker: ImagePickerController, images: [UIImage]) {
        //self.messageImageLabel?.text = images.count == 0 ? "Vui lòng thêm hình ảnh".localized() : ""
        //add image to cell
        //self.updateImageDisplay(0, images)
        
        var results = [NSDictionary]();
        for image in images {
            results.append([
                "path": self.saveToFile(image: image),
            ]);
        }
        
        imagePicker.dismiss(animated: true) {
            self.imagesResult!(results)
        }
    }
    
    public func cancelButtonDidPress(_ imagePicker: ImagePickerController) {
        self.imagesResult!([NSDictionary]())
        imagePicker.dismiss(animated: true, completion: nil)
    }
    
    init(cont: FlutterViewController, messenger: FlutterBinaryMessenger) {
        self.controller = cont;
        self.messenger = messenger;
        super.init();
    }
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        
        let channel = FlutterMethodChannel(name: "christian_picker_image", binaryMessenger: registrar.messenger())
        
        let app =  UIApplication.shared
        let controller : FlutterViewController = app.delegate!.window!!.rootViewController as! FlutterViewController;
        
        let instance = SwiftChristianPickerImagePlugin.init(cont: controller, messenger: registrar.messenger())
        
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        
        self.imagesResult = result
        
        let configuration = Configuration()
        configuration.recordLocation = false
        
        //let limit = maxImages - self.arrImage.count
        switch (call.method) {
        case "pickImages":
            let arguments = call.arguments as! Dictionary<String, AnyObject>
            let maxImages = arguments["maxImages"] as! Int
            let enableGestures = arguments["enableGestures"] as! Bool
            
            let limit = maxImages
            if limit <= 0 {
                return
            }
            
            let imagePicker = ImagePickerController(configuration: configuration)
            imagePicker.modalPresentationStyle = .fullScreen
            imagePicker.delegate = self
            imagePicker.imageLimit = limit
            imagePicker.enableGestures(enableGestures);
            controller!.present(imagePicker, animated: true, completion: nil)
            break;
        case "getPlatformVersion":
            result("iOS " + UIDevice.current.systemVersion)
            break;
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    private func saveToFile(image: UIImage) -> Any {
        guard let data = image.jpegData(compressionQuality: 1.0) else {
            return FlutterError(code: "image_encoding_error", message: "Could not read image", details: nil)
        }
        let tempDir = NSTemporaryDirectory()
        let imageName = "image_picker_\(ProcessInfo().globallyUniqueString).jpg"
        let filePath = tempDir.appending(imageName)
        if FileManager.default.createFile(atPath: filePath, contents: data, attributes: nil) {
            return filePath
        } else {
            return FlutterError(code: "image_save_failed", message: "Could not save image to disk", details: nil)
        }
    }
}
