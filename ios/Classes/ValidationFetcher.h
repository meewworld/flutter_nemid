//
//  ValidationFetcher.h
//  TestNemIdJavascript
//
//     

#import <Foundation/Foundation.h>
#import "ValidationResponse.h"

/**
 * The ValidationFetcher is used for validating flow results and getting back a response from the SP backend.
 * The ValidationFetcher has three separate methods. The two are used to validate the response received from 
 * the NemID Client upon a completed flow. These methods are used to send the response to the SP backend, and 
 * to in turn receive a response telling whether the signature of the response could be validated, and whether 
 * the response could be parsed. The SP backend also retrieves the Hsession value  (and holds this on its own 
 * session - Hsession is only relevant in bank flows) and unpacks the rememberuserid token and sends this back 
 * to the app for storage, such that it can be used in subsequent flows.
 * The third method (logOut) of the validationfetcher lets the user clear the session on the SP backend (and 
 * the HSession associated with it - only relevant in bank flows).
 */

typedef void(^ValidationFetcherSuccessBlock)(ValidationResponse *validationResponse);
typedef void(^ValidationFetcherErrorBlock)(NSInteger errorCode, NSString *errorMessage);

@interface ValidationFetcher : NSObject

+ (void)fetchValidationWithBackendUrl:(NSString *)urlStr andData:(NSString *)dataStr success:(ValidationFetcherSuccessBlock)successBlock error:(ValidationFetcherErrorBlock)errorBlock;
+ (void)logOut:(NSString*)urlStr success:(ValidationFetcherSuccessBlock)successBlock error:(ValidationFetcherErrorBlock)errorBlock;

@end
