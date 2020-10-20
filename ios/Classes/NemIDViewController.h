//
//  NemIDViewController.h
//  TestNemIdJavascript
//
//     

#import <UIKit/UIKit.h>
#import <WebKit/WebKit.h>
#import "ParameterFetcher.h"
#import "FlutterNemidPlugin.h"

/**
 * The NemIDViewController is responsible for presenting the web view with proper HTML for starting 
 * the selected flow. It communicates with the NemID client by sending and receiving javascript
 * messages.
 */
@interface NemIDViewController : UIViewController

// Properties and methods used by MainViewController

@property (nonatomic) NSString *parameters;
@property (nonatomic) NSString *width;
@property (nonatomic) NSString *height;
@property (nonatomic) NSString *nemIDJavascriptURL;
@property (nonatomic) FlutterNemidPlugin *controller;

@end
