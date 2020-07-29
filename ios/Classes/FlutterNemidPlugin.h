#import <Flutter/Flutter.h>

@interface FlutterNemidPlugin : NSObject<FlutterPlugin>

@property (nonatomic, copy) NSString *spBackendURL;
@property (nonatomic, copy) NSString *nemIDBackendURL;
@property (nonatomic, copy) FlutterResult flutterResult;

- (void)setLoggedInTo:(BOOL) state;
- (void)sendResult;

@end
