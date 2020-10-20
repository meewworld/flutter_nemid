//
//  ParameterFetcher.m
//  TestNemIdJavascript
//
//     


#import "ParameterFetcher.h"
#import "Constants.h"
#import "NetworkUtilities.h"

@implementation ParameterFetcher

+ (void)fetch:(NSURL *)url
      success:(ParameterFetcherSuccessBlock)successBlock
        error:(ParameterFetcherErrorBlock)errorBlock {
    NSLog(@"Fetching parameters from url:%@", url);
    
    NSURLRequest *request = [NetworkUtilities urlRequestWithUrl:url andDataString:@"" requestType:@"GET"];
    NSURLSessionConfiguration *sessionConfig = [NSURLSessionConfiguration defaultSessionConfiguration];
    NSURLSession *session = [NSURLSession sessionWithConfiguration:sessionConfig];
    
    [[session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        if (!error) {
            NSString *jsonReceived = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
            jsonReceived = [jsonReceived stringByReplacingOccurrencesOfString:@"\n" withString:@""];
            NSLog(@"Parameters fetched from url:%@ with result:%@", url, jsonReceived);
            dispatch_sync(dispatch_get_main_queue(), ^{                
                successBlock(jsonReceived);
            });
        } else {
            NSLog(@"Error fetching parameters: %@", error.description);
            dispatch_sync(dispatch_get_main_queue(), ^{
                errorBlock(error.code, error.localizedDescription);
            });
        }
    }] resume];
}

+ (void)fetchParameters:(NSURL *)url
                                            success:(ParameterFetcherSuccessBlock)successBlock
                                              error:(ParameterFetcherErrorBlock)errorBlock{
    
    return [self fetch:url success:successBlock error:errorBlock];
}

@end
