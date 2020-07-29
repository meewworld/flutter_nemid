//
//  ValidationResponse.h
//  TestNemIdJavascript
//
//     

#import <Foundation/Foundation.h>

/**
 * The ValidationResponse is used to hold responses from validating output from the NemID 
 * flows against the SP backend which is available on appletk.danid.dk.
 */
@interface ValidationResponse : NSObject

@property (nonatomic, retain) NSString *validationResult;
@property (nonatomic, retain) NSString *resultDetails;
@property (nonatomic, retain) NSString *rememberUseridToken;
@property (nonatomic, retain) NSString *logOutResult;

- (id)initWithDictionary:(NSDictionary*)dict;

@end
