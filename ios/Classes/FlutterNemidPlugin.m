#import "FlutterNemidPlugin.h"
#import "NemIDViewController.h"
#import "ValidationFetcher.h"
#import "NetworkUtilities.h"
#import "ClientDimensions.h"
#import "Constants.h"

@implementation FlutterNemidPlugin

BOOL _isLoggedIn;

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel methodChannelWithName:@"flutter_nemid" binaryMessenger:[registrar messenger]];
    FlutterNemidPlugin* instance = [[FlutterNemidPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];
}

- (UIViewController *)viewController:(UIWindow *)window {
    UIWindow *windowToUse = window;
    if (windowToUse == nil) {
        for (UIWindow *window in [UIApplication sharedApplication].windows) {
            if(window.isKeyWindow){
                windowToUse = window;
                break;
            }
        }
    }
    UIViewController *topController = windowToUse.rootViewController;
    while(topController.presentingViewController)
        topController = topController.presentingViewController;
    return topController;
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    self.flutterResult = result;
    
  if ([@"startNemIDLogin" isEqualToString:call.method]) {
            
      NSString *bundlePath = [[NSBundle mainBundle] pathForResource:@"nemid_bundle" ofType:@"bundle"];
      NSBundle *bundle = [NSBundle bundleWithPath:bundlePath];

      _isLoggedIn = NO;
      self.spBackendURL = @"https://appletk.danid.dk";
      self.nemIDBackendURL = @"https://appletk.danid.dk";
      
      [self parameterResponse:@"RequestTypeTwoFactorLoginLongTerm+" success:^(NSString *parameters) {
          //Pass parameters to next view, and go to view
          if (parameters) {
              NemIDViewController *nemIDViewController;
              if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
                  nemIDViewController = [[UIStoryboard storyboardWithName:@"NemID_iPad" bundle:bundle] instantiateViewControllerWithIdentifier:@"NemID_iPad"];
              } else {
                  nemIDViewController = [[UIStoryboard storyboardWithName:@"NemID_iPhone" bundle:bundle] instantiateViewControllerWithIdentifier:@"NemIDViewController"];
              }
              
              ClientDimensions *clientDimensions = [self getClientDimensions];
              
              // Set relevant parameters for NemIDViewController
              nemIDViewController.parameters = parameters;
              NSString *launcherUrl =[NSString stringWithFormat:@"%@%@", self.nemIDBackendURL, LauncherURL];
              nemIDViewController.nemIDJavascriptURL = launcherUrl;
              nemIDViewController.width = clientDimensions.width;
              nemIDViewController.height = clientDimensions.height;
              nemIDViewController.controller = self;
              nemIDViewController.useWKWebView = @"FALSE";
              
              [[UIApplication sharedApplication].keyWindow.rootViewController presentViewController:nemIDViewController animated:YES completion:nil];
          }
          else {
              NSLog(@"Error in parameter response from %@", GenerateParameterURL);
          }
      } error:^(NSInteger errorCode, NSString *errorMessage) {
          NSLog(@"Error while starting flow. ErrorCode was: %lu. ErrorMessage was: %@", (long)errorCode, errorMessage);
      }];
  } else {
    result(FlutterMethodNotImplemented);
  }
}

- (void) setLoggedInTo:(BOOL) state{
    _isLoggedIn = state;
}

- (void) sendResult {
    self.flutterResult([NSNumber numberWithBool:_isLoggedIn]);
}

- (ClientDimensions *)getClientDimensions {
    ClientDimensions *clientDimensions = [ClientDimensions new];

    clientDimensions.width = @"320";
    clientDimensions.height = @"460";
    
    return clientDimensions;
}

- (void)parameterResponse:(NSString *)requestType
                  success:(ParameterFetcherSuccessBlock)successBlock
                    error:(ParameterFetcherErrorBlock)errorBlock {
    NSString *samlProviderUrl = [NSString stringWithFormat:@"%@%@", self.spBackendURL, GenerateParameterURL];

    NSLog(@"Starting RequestTypeTwoFactorLoginLongTerm");
    [ParameterFetcher fetchTwoFactorLoginLongTermWithSamlProvider:[NSURL URLWithString:samlProviderUrl]
                                                                                issuer:@"1"
                                                                              language:@"DA"
                                                                   rememberuseridtoken:@""
                                                                  suppressPushToDevice:@"FALSE"
                                                                          useAppSwitch:@"FALSE"
                                                                               success:successBlock
                                                                                 error:errorBlock];
}


@end
