//
//  ParameterFetcher.m
//  TestNemIdJavascript
//
//     


#import "ParameterFetcher.h"
#import "Constants.h"
#import "NetworkUtilities.h"

@implementation ParameterFetcher

+ (void)fetch:(NSURL *)url data:(NSString *)reqString
      success:(ParameterFetcherSuccessBlock)successBlock
        error:(ParameterFetcherErrorBlock)errorBlock {
    NSLog(@"Fetching parameters from url:%@ with data:%@", url, reqString);
    
    NSURLRequest *request = [NetworkUtilities urlRequestWithUrl:url andDataString:reqString];
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


#pragma mark - Bank login flows

+ (void)fetchOneFactorLoginWithSamlProvider:(NSURL *)url
                                     issuer:(NSString *)issuer
                                   language:(NSString *)language
                        rememberuseridtoken:(NSString *)rememberuseridtoken
                                    success:(ParameterFetcherSuccessBlock)successBlock
                                      error:(ParameterFetcherErrorBlock)errorBlock {
    return [self fetch:url data:[NSString stringWithFormat:@"data={\"requestType\": \"banklogin1\", \"enableAwaitingAppApprovalEvent\": \"true\", \"language\": \"%@\", \"rememberuseridtoken\": \"%@\", \"useJson\": \"true\", \"issuer\": \"%@\"}", language, [NetworkUtilities urlEncode:rememberuseridtoken], issuer] success:successBlock error:errorBlock];
}

+ (void)fetchTwoFactorLoginWithSamlProvider:(NSURL *)url
                                     issuer:(NSString *)issuer
                                   language:(NSString *)language
                        rememberuseridtoken:(NSString *)rememberuseridtoken
                       suppressPushToDevice:(NSString *)suppressPushToDevice
                               useAppSwitch:(NSString *)useAppSwitch
                                    success:(ParameterFetcherSuccessBlock)successBlock
                                      error:(ParameterFetcherErrorBlock)errorBlock {
    return [self fetch:url data:[NSString stringWithFormat:@"data={\"requestType\": \"banklogin2\", \"language\": \"%@\", \"rememberuseridtoken\": \"%@\", \"useJson\": \"true\", \"issuer\": \"%@\", \"suppressPushToDevice\": \"%@\", \"enableAwaitingAppApprovalEvent\": \"%@\"}", language, [NetworkUtilities urlEncode:rememberuseridtoken], issuer, suppressPushToDevice, useAppSwitch] success:successBlock error:errorBlock];
}


#pragma mark - Bank signing flows

+ (void)fetchSignWithSamlProvider:(NSURL *)url
                           issuer:(NSString *)issuer
                         signText:(NSString *)signText
                           stepUp:(NSString *)stepUp
               signTransformation:(NSString *)signTransformation
                   signTextFormat:(NSString *)signTextformat
                         signType:(NSString *)signType
                         language:(NSString *)language
              rememberuseridtoken:(NSString *)rememberuseridtoken
             suppressPushToDevice:(NSString *)suppressPushToDevice
                     useAppSwitch:(NSString *)useAppSwitch
                          success:(ParameterFetcherSuccessBlock)successBlock
                            error:(ParameterFetcherErrorBlock)errorBlock {
    NSString *xslt = @"";
    if(signTransformation != nil) {
        xslt  = [NSString stringWithFormat:@", \"signTransformation\": \"%@\"", [NetworkUtilities urlEncode:[NetworkUtilities base64Encode:signTransformation]]];
    }
    
    return [self fetch:url data:[NSString stringWithFormat:@"data={\"requestType\": \"%@\", \"allow_stepup\": \"%@\", \"language\": \"%@\", \"rememberuseridtoken\": \"%@\", \"useJson\": \"true\", \"signText\": \"%@\", \"signTextFormat\": \"%@\"%@, \"issuer\": \"%@\", \"suppressPushToDevice\": \"%@\", \"enableAwaitingAppApprovalEvent\": \"%@\"}", signType, stepUp, language, [NetworkUtilities urlEncode:rememberuseridtoken], [NetworkUtilities urlEncode:signText], signTextformat, xslt, issuer, suppressPushToDevice, useAppSwitch] success:successBlock error:errorBlock];
}

+ (void)fetchTwoFactorSignWithSamlProvider:(NSURL *)url
                                    issuer:(NSString *)issuer
                                  signText:(NSString *)signText
                        signTransformation:(NSString *)signTransformation
                            signTextFormat:(NSString *)signTextformat
                                  language:(NSString *)language
                       rememberuseridtoken:(NSString *)rememberuseridtoken
                      suppressPushToDevice:(NSString *)suppressPushToDevice
                              useAppSwitch:(NSString *)useAppSwitch
                                   success:(ParameterFetcherSuccessBlock)successBlock
                                     error:(ParameterFetcherErrorBlock)errorBlock {
    
    return [ParameterFetcher fetchSignWithSamlProvider:url issuer:issuer signText:signText stepUp:@"FALSE" signTransformation:signTransformation signTextFormat:signTextformat signType:@"banksign2" language:language rememberuseridtoken:rememberuseridtoken suppressPushToDevice:suppressPushToDevice useAppSwitch:useAppSwitch success:successBlock error:errorBlock];
}

+ (void)fetchOneFactorSignWithSamlProvider:(NSURL *)url
                                    issuer:(NSString *)issuer
                                  signText:(NSString *)signText
                                    stepUp:(NSString *)stepUp
                        signTransformation:(NSString *)signTransformation
                            signTextFormat:(NSString *)signTextformat
                                  language:(NSString *)language
                       rememberuseridtoken:(NSString *)rememberuseridtoken
                      suppressPushToDevice:(NSString *)suppressPushToDevice
                              useAppSwitch:useAppSwitch
                                   success:(ParameterFetcherSuccessBlock)successBlock
                                     error:(ParameterFetcherErrorBlock)errorBlock {
    
    return [ParameterFetcher fetchSignWithSamlProvider:url issuer:issuer signText:signText stepUp:stepUp signTransformation:signTransformation signTextFormat:signTextformat signType:@"banksign1" language:language rememberuseridtoken:rememberuseridtoken suppressPushToDevice:suppressPushToDevice useAppSwitch:useAppSwitch success:successBlock error:errorBlock];
}


#pragma mark - OCES long term login flows

+ (void)fetchTwoFactorLoginLongTermWithSamlProvider:(NSURL *)url
                                             issuer:(NSString *)issuer
                                           language:(NSString *)language
                                rememberuseridtoken:(NSString *)rememberuseridtoken
                               suppressPushToDevice:(NSString *)suppressPushToDevice
                                       useAppSwitch:(NSString *)useAppSwitch
                                            success:(ParameterFetcherSuccessBlock)successBlock
                                              error:(ParameterFetcherErrorBlock)errorBlock{
    
    return [self fetch:url data:[NSString stringWithFormat:@"data={\"requestType\": \"oceslogin2\", \"language\": \"%@\", \"rememberuseridtoken\": \"%@\", \"useJson\": \"true\", \"issuer\": \"%@\", \"suppressPushToDevice\": \"%@\", \"enableAwaitingAppApprovalEvent\": \"%@\"}", language, [NetworkUtilities urlEncode:rememberuseridtoken], issuer, suppressPushToDevice, useAppSwitch] success:successBlock error:errorBlock];
}


#pragma mark - OCES long term signing flows

+ (void)fetchTwoFactorSignLongTermWithSamlProvider:(NSURL *)url
                                            issuer:(NSString *)issuer
                                          signText:(NSString *)signText
                                signTransformation:(NSString *)signTransformation
                                    signTextFormat:(NSString *)signTextformat
                                          language:(NSString *)language
                               rememberuseridtoken:(NSString *)rememberuseridtoken
                              suppressPushToDevice:(NSString *)suppressPushToDevice
                                      useAppSwitch:(NSString *)useAppSwitch
                                           success:(ParameterFetcherSuccessBlock)successBlock
                                             error:(ParameterFetcherErrorBlock)errorBlock {
    
    return [ParameterFetcher fetchSignWithSamlProvider:url issuer:issuer signText:signText stepUp:@"FALSE" signTransformation:signTransformation signTextFormat:signTextformat signType:@"ocessign2" language:language rememberuseridtoken:rememberuseridtoken suppressPushToDevice:suppressPushToDevice useAppSwitch:useAppSwitch success:successBlock error:errorBlock];
}

@end
