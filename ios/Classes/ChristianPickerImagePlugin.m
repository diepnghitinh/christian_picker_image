#import "ChristianPickerImagePlugin.h"
#import <christian_picker_image/christian_picker_image-Swift.h>

@implementation ChristianPickerImagePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftChristianPickerImagePlugin registerWithRegistrar:registrar];
}
@end
