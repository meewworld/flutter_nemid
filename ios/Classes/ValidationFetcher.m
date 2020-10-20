//
//  ValidationFetcher.m
//  TestNemIdJavascript
//
//

#import "ValidationFetcher.h"
#import "NetworkUtilities.h"
#import "Constants.h"

@implementation ValidationFetcher

#pragma mark - Helper methods
+ (void)fetch:(NSURL *)url andSaml:(NSString *)dataString success:(ValidationFetcherSuccessBlock)successBlock error:(ValidationFetcherErrorBlock)errorBlock {
    NSLog(@"Fetching validation from url:%@ with data:%@", url, dataString);
    
    NSURLRequest *request = [NetworkUtilities urlRequestWithData:url andDataString:dataString requestType:@"POST"];
    NSURLSessionConfiguration *sessionConfig = [NSURLSessionConfiguration defaultSessionConfiguration];
    NSURLSession *session = [NSURLSession sessionWithConfiguration:sessionConfig];
    
    [[session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {  
        if (!error) {
            NSMutableDictionary *dict = [NetworkUtilities parseKeyValueResponse:data];
            NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *) response;
            NSInteger statusCode = 0;
            statusCode = httpResponse.statusCode;
            [dict setObject:[NSNumber numberWithInt:statusCode] forKey:@"status"];
            NSLog(@"Validation response fetched from url:%@ with result:%@", url, dict);
            dispatch_sync(dispatch_get_main_queue(), ^{
               successBlock([[ValidationResponse alloc] initWithDictionary:dict]);
            });
        } else {
            NSLog(@"Error validating response: %@", error.description);
            dispatch_sync(dispatch_get_main_queue(), ^{
                errorBlock(error.code, error.localizedDescription);
            });
        }
    }] resume];
}


#pragma mark - Validation

+ (void)fetchValidationWithBackendUrl:(NSString *)urlStr andData:(NSString *)dataStr success:(ValidationFetcherSuccessBlock)successBlock error:(ValidationFetcherErrorBlock)errorBlock {
    [self fetch:[NSURL URLWithString:urlStr] andSaml:dataStr success:successBlock error:errorBlock];
}


#pragma mark - Logout

+ (void)logOut:(NSString*)urlStr success:(ValidationFetcherSuccessBlock)successBlock error:(ValidationFetcherErrorBlock)errorBlock {
    NSString *logOutUrl = [NSString stringWithFormat:@"%@%@", urlStr, LogoutURL];
    [self fetch:[NSURL URLWithString:logOutUrl] andSaml:[NSString stringWithFormat:@"response=%@", [NetworkUtilities urlEncode:@"emptysaml"]] success:successBlock error:errorBlock];
}

@end

