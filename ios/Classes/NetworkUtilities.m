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

-(void) JSLog:(NSString*)logString{
        int stepLog = 800;
        NSInteger strLen = [@([logString length]) integerValue];
        NSInteger countInt = strLen / stepLog;
        if (strLen > stepLog) {
        for (int i=1; i <= countInt; i++) {
            NSString *character = [logString substringWithRange:NSMakeRange((i*stepLog)-stepLog, stepLog)];
            NSLog(@"%@", character);
        }
        NSString *character = [logString substringWithRange:NSMakeRange((countInt*stepLog), strLen-(countInt*stepLog))];
        NSLog(@"%@", character);
        } else {
        NSLog(@"%@", logString);
        }
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
        if ([keyValuePairStr length] < 3) {
            continue; //cannot be parsed as a key-value pair - ignore
        }
        
        NSArray *keyValue = [keyValuePairStr componentsSeparatedByString:@"="];
        if ([keyValue count] == 0) {
            NSLog(@"No key-value pairs found in response: %@", response);
            continue; //cannot be parsed as a key-value pair - ignore
        }
        
        if ([keyValue count] > 1) {
            NSString *key = [[keyValue objectAtIndex:0] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
            NSString *value = [NIDBase64 stringFromBase64String:[keyValue objectAtIndex:1]];
            
            if (value && key) {
                [dict setObject:value forKey:key];
            }
        }
    }
    return dict;
}

@end
