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
        self.result = [dict objectForKey:@"result"];
        self.status = [dict objectForKey:@"status"];
    }
    return self;
}

- (NSString*)description{
    NSString* resultFormat = @"{\"status\": \"%@\", \"result\": %@}";
    return [NSString stringWithFormat:resultFormat, self.status, self.result];
}

@end
