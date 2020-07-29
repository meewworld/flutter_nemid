//
//  ParameterFetcher.h
//  TestNemIdJavascript
//
//     

#import <Foundation/Foundation.h>

/**
 * The ParameterFetcher is used to get the correct login or signing parameters from the SP backend.
 */

typedef void(^ParameterFetcherSuccessBlock)(NSString *parameters);
typedef void(^ParameterFetcherErrorBlock)(NSInteger errorCode, NSString *errorMessage);

@interface ParameterFetcher : NSObject

+ (void)fetchTwoFactorLoginWithSamlProvider:(NSURL *)url
                                           issuer:(NSString *)issuer
                                         language:(NSString *)language
                              rememberuseridtoken:(NSString *)rememberuseridtoken
                             suppressPushToDevice:(NSString *)suppressPushToDevice
                                     useAppSwitch:(NSString *)useAppSwitch
                                          success:(ParameterFetcherSuccessBlock)successBlock
                                            error:(ParameterFetcherErrorBlock)errorBlock;

+ (void)fetchOneFactorLoginWithSamlProvider:(NSURL *)url
                                           issuer:(NSString *)issuer
                                         language:(NSString *)language
                              rememberuseridtoken:(NSString *)rememberuseridtoken
                                          success:(ParameterFetcherSuccessBlock)successBlock
                                            error:(ParameterFetcherErrorBlock)errorBlock;

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
                                           error:(ParameterFetcherErrorBlock)errorBlock;

+ (void)fetchOneFactorSignWithSamlProvider:(NSURL *)url
                                          issuer:(NSString *)issuer
                                        signText:(NSString *)signText
                                          stepUp:(NSString *)stepUp
                              signTransformation:(NSString *)signTransformation
                                  signTextFormat:(NSString *)signTextformat
                                        language:(NSString *)language
                             rememberuseridtoken:(NSString *)rememberuseridtoken
                            suppressPushToDevice:(NSString *)suppressPushToDevice
                                    useAppSwitch:(NSString *)useAppSwitch
                                         success:(ParameterFetcherSuccessBlock)successBlock
                                           error:(ParameterFetcherErrorBlock)errorBlock;

+ (void)fetchTwoFactorLoginLongTermWithSamlProvider:(NSURL *)url
                                                   issuer:(NSString *)issuer
                                                 language:(NSString *)language
                                      rememberuseridtoken:(NSString *)rememberuseridtoken
                                     suppressPushToDevice:(NSString *)suppressPushToDevice
                                             useAppSwitch:(NSString *)useAppSwitch
                                                  success:(ParameterFetcherSuccessBlock)successBlock
                                                    error:(ParameterFetcherErrorBlock)errorBlock;

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
                                                   error:(ParameterFetcherErrorBlock)errorBlock;

+ (void)fetch:(NSURL *)url
         data:(NSString *)reqString
      success:(ParameterFetcherSuccessBlock)successBlock
        error:(ParameterFetcherErrorBlock)errorBlock;

@end
