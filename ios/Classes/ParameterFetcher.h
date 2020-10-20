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

+ (void)fetchParameters:(NSURL *)url
                success:(ParameterFetcherSuccessBlock)successBlock
                  error:(ParameterFetcherErrorBlock)errorBlock;

+ (void)fetch:(NSURL *)url
      success:(ParameterFetcherSuccessBlock)successBlock
        error:(ParameterFetcherErrorBlock)errorBlock;

@end
