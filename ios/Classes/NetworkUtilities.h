//
//  NetworkUtilities.h
//  TestNemIdJavascript
//
//     

#import <Foundation/Foundation.h>

/**
 * NetworkUtilities contains helper methods for encoding/decoding, setting up requests and parsing key-value responses.
 */
@interface NetworkUtilities : NSObject

+ (NSString *)urlEncode:(NSString *)str;
+ (NSString *)base64Encode:(NSString *)str;
+ (NSString *)base64Decode:(NSString *)str;
+ (NSURLRequest*)urlRequestWithUrl:(NSURL*)url andDataString:(NSString*)dataStr;
+ (NSDictionary*)parseKeyValueResponse:(NSData*)data;

@end
