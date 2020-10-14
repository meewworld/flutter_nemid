#import <Flutter/Flutter.h>

@interface FlutterNemidPlugin : NSObject<FlutterPlugin>

@property (nonatomic, copy) NSString *spBackendURL;
@property (nonatomic, copy) NSString *nemIDBackendURL;
@property (nonatomic, copy) NSString *signingEndpoint;
@property (nonatomic, copy) NSString *validationEndpoint;
@property (nonatomic, copy) FlutterResult flutterResult;

- (void)setLoggedInTo:(BOOL) state;
- (void)sendResult;

@end
