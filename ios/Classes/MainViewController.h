//
//  MainViewController.h
//  TestNemIdJavascript
//
//     

#import <UIKit/UIKit.h>
#import "ParameterFetcher.h"
#import "Constants.h"

/**
 * The MainViewController is responsible for preparing options and flows for the NemID client.
 * It can communicate with a test service provider (SP) backend on appletk.danid.dk, from this backend it
 * can retrieve signed test parameters for starting up the different NemId flows (URLs are defined in Constants.h).
 */
@interface MainViewController : UIViewController

// Properties and methods exposed to NemIDViewController

@property (nonatomic) RequestType currentRequestType;
@property (nonatomic, retain) IBOutlet UITextView *responseTextView;
@property (nonatomic, retain) IBOutlet UITextField *spBackendURLTextField;
@property (nonatomic, retain) IBOutlet UITextField *nemIDBackendURLTextField;
@property (weak, nonatomic) IBOutlet UITextField *responseStatusTextField;

- (void)parameterResponse:(RequestType)requestType
                  success:(ParameterFetcherSuccessBlock)successBlock
                    error:(ParameterFetcherErrorBlock)errorBlock;
- (void)setRememberUseridToken:(NSString*)rememberUseridToken;
- (void)setLoggedInTo:(BOOL) state;
- (BOOL)currentFlowIsBankFlow;

@end
