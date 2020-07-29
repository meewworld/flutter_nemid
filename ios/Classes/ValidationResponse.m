//
//  ValidationResponse.m
//  TestNemIdJavascript
//
//     

#import "ValidationResponse.h"

@implementation ValidationResponse

- (id)initWithDictionary:(NSDictionary*)dict {
    self = [super init];
    if (self) {
        self.validationResult = [dict objectForKey:@"VALIDATION_RESULT"];
        self.resultDetails = [dict objectForKey:@"RESULT_DETAILS"];
        self.rememberUseridToken = [dict objectForKey:@"REMEMBER_USERID"];
        self.logOutResult = [dict objectForKey:@"LOGOUT"];
    }
    return self;
}

- (NSString*)description{
    NSString* resultFormat = @"validationResult = \"%@\", resultdetails = \"%@\", rememberUseridToken = \"%@\"";
    return [NSString stringWithFormat:resultFormat, self.validationResult, self.resultDetails, self.rememberUseridToken];
}

@end
