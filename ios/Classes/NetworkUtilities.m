//
//  NetworkUtilities.m
//  TestNemIdJavascript
//
//     

#import "NetworkUtilities.h"
#import "NIDBase64.h"

@implementation NetworkUtilities

+ (NSString *)urlEncode:(NSString *)str{
    return [str stringByAddingPercentEncodingWithAllowedCharacters:[[NSCharacterSet characterSetWithCharactersInString:@"?=&+"] invertedSet]];
}

+ (NSString *)base64Encode:(NSString *)str{
    return [NIDBase64 base64EncodedStringWithStringNoCRLF:str];
}

+ (NSString *)base64Decode:(NSString *)str{
    return [NIDBase64 stringFromBase64String:str];
}

+ (NSURLRequest*)urlRequestWithUrl:(NSURL*)url andDataString:(NSString*)dataStr requestType:(NSString*)type {
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:5.0];
    request.HTTPShouldHandleCookies = YES;
    NSData *data = [dataStr dataUsingEncoding:NSUTF8StringEncoding];
    [request setHTTPMethod:type];
    [request setValue:@"application/x-www-form-urlencoded;charset=UTF-8" forHTTPHeaderField:@"Content-Type"];
    [request setValue:[NSString stringWithFormat:@"%lu", (unsigned long)[data length]] forHTTPHeaderField:@"Content-length"];
    [request setHTTPBody:data];
    return request;
}

+ (NSURLRequest*)urlRequestWithData:(NSURL*)url andDataString:(NSString*)dataStr requestType:(NSString*)type {
    NSData *data = [dataStr dataUsingEncoding:NSUTF8StringEncoding];
    NSString *string = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    NSString *logString = [NSString stringWithFormat:@"{\"response\":\"%@\"}", string];
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
    NSData *requestData = [NSData dataWithBytes:[logString UTF8String] length:[logString length]];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    [request setValue:@"application/json;charset=UTF-8" forHTTPHeaderField:@"Content-Type"];
    [request setValue:[NSString stringWithFormat:@"%d", [requestData length]] forHTTPHeaderField:@"Content-Length"];
    [request setHTTPBody: requestData];
    return request;
}

+ (NSDictionary*)parseKeyValueResponse:(NSData*)data{
    NSString *response = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    NSLog(@"Response from SP backend was: %@", response);
    
    NSArray *keyValuePairs = [response componentsSeparatedByString:@";"];
    if ([keyValuePairs count] == 0) {
        [NSException raise:@"Failed to parse parameter response from server" format:@"The response was %@", response];
    }
    
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithCapacity:3];
    for (int i = 0; i < [keyValuePairs count]; i++) {
        NSString *keyValuePairStr = [keyValuePairs objectAtIndex:i];
        
        [dict setObject:keyValuePairStr forKey:@"result"];
    }
    return dict;
}

@end
