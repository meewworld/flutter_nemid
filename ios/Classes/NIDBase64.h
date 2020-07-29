//
//  NSData+Base64.h
//  TestNemIdJavascript
//
//

#import <Foundation/Foundation.h>

@interface NIDBase64 : NSObject {
    
}

+ (NSData *)dataFromBase64String:(NSString *)aString;
+ (NSString *)stringFromBase64String:(NSString *)aString;
+ (NSString *)base64EncodedString:(NSData*)data;
+ (NSString *)base64EncodedStringWithString:(NSString*)aString;

+ (NSString *)base64EncodedStringNoCRLF:(NSData *)data;
+ (NSString *)base64EncodedStringWithStringNoCRLF:(NSString *)aString;

@end
