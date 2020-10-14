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
        self.resultDetails = [dict objectForKey:@"response"];
    }
    return self;
}

- (NSString*)description{
    NSString* resultFormat = @"validationResult = \"%@\", resultdetails = \"%@\"";
    return [NSString stringWithFormat:resultFormat, self.validationResult, self.resultDetails];
}

@end
