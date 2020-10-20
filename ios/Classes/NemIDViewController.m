//
//  NemIDViewController.m
//  TestNemIdJavascript
//
//

#import "NemIDViewController.h"
#import "Constants.h"
#import "NetworkUtilities.h"
#import "ValidationFetcher.h"
#import "QuartzCore/QuartzCore.h"

@interface NemIDViewController () <WKNavigationDelegate, UIPrintInteractionControllerDelegate> {
    float iframeWidth;
    float iframeHeight;
}

@property (strong, nonatomic) IBOutlet UIView *placeholderForWebViews;
@property (strong, nonatomic) WKWebView *wkWebView;
@end

@implementation NemIDViewController

#pragma mark - UIViewController lifecycle methods

- (void)viewDidLoad {
    [super viewDidLoad];
    // Make sure self.view adapts to UINavigationController view.
    self.edgesForExtendedLayout = UIRectEdgeNone;
}

-(void)viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
    // The loadNemID method is only called after viewDidAppear for backwards compatibility with iOS 7.0.x, since
    // width and height calculation depends on all views having valid frame information. If the client app is
    // built for iOS 7.1 or later, loading should be done as early as possible in the view controller lifecycle.
    [self loadNemID];
}

#pragma mark - MainViewController methods

-(void)loadNemID{
    // If width or height is not specified, use full screen.
    if ((self.width.length > 0) && (self.height.length > 0)){
        iframeWidth = [self.width floatValue];
        iframeHeight = [self.height floatValue];
    } else {
        iframeWidth = self.placeholderForWebViews.frame.size.width;
        iframeHeight = self.placeholderForWebViews.frame.size.height;
    }
    
    //Add random number to URL to avoid any caching
    long r = arc4random() % LONG_MAX;
    NSString *url = [NSString stringWithFormat:@"%@/%ld", self.nemIDJavascriptURL, r];

    NSString *headContent = [NSString stringWithFormat:@"<meta name=\"viewport\" content=\"initial-scale=1.0,user-scalable=no\">"];
    NSString *html = [NSString stringWithFormat:@"<html><head>%@</head><body style='text-align:center'><iframe id=\"nemid_iframe\" name=\"nemid_iframe\" scrolling=\"no\" frameborder=\"0\" style=\"width:%fpx;height:%fpx;border:0;margin:0px;\" src=\"%@\"></iframe><script>%@</script></body></html>", headContent, iframeWidth, iframeHeight, url, [self getJavascript]];
    
    // Prepare frame for web view
    CGRect frame = self.placeholderForWebViews.frame;
    frame.origin.x = 0;
    frame.origin.y = 0;
    
	WKWebViewConfiguration *theConfiguration = [[WKWebViewConfiguration alloc] init];
	self.wkWebView = [[WKWebView alloc] initWithFrame:frame configuration:theConfiguration];
	self.wkWebView.navigationDelegate = self;
	[self.placeholderForWebViews addSubview:self.wkWebView];
	self.wkWebView.scrollView.scrollEnabled = YES;
	self.wkWebView.autoresizingMask = UIViewAutoresizingFlexibleWidth|UIViewAutoresizingFlexibleHeight;
	[self.wkWebView loadHTMLString:html baseURL:[NSURL URLWithString:url]];
}

#pragma mark - Helper methods

- (NSString*)getJavascript{
    NSString *js =
    @"function onNemIDMessage(e) { \
    var event = e || event; \
    var win = document.getElementById(\"nemid_iframe\").contentWindow, postMessage = {}, message; \
    var origin = event.origin; \
    if (!\"%@\".startsWith(origin)) { \
    console.log(\"Error, event is not from expected origin\"); \
    return; \
    } \
    message = JSON.parse(event.data); \
    if (message.command === \"SendParameters\") { \
    postMessage.command = \"parameters\"; \
    postMessage.content = \'%@\'; \
    win.postMessage(JSON.stringify(postMessage), \"*\"); \
    } \
    if (message.command === \"changeResponseAndSubmit\") { \
    window.globalContent = message.content; \
    window.location = 'changeResponseAndSubmit:/' \
    } \
    if (message.command === \"changeResponseAndSubmitSign\") { \
    window.globalContent = message.content; \
    window.location = 'changeResponseAndSubmit:/' \
    } \
    if (message.command === \"RequestPrint\") { \
    window.globalContent = message.content; \
    window.location = 'RequestPrint:/' \
    } \
    if (message.command === \"AwaitingAppApproval\") { \
    window.location = 'AwaitingAppApproval:/' \
    } \
    } \
    if (window.addEventListener) { \
    window.addEventListener(\"message\", onNemIDMessage); \
    }else if (window.attachEvent) { \
    window.attachEvent(\"onmessage\", onNemIDMessage); \
    } \
    function getContent() { \
    return window.globalContent; \
    } \
    function autoResize(){\
    var F = document.getElementById(\"nemid_iframe\");\
    document.getElementById(F).height= (%d) + \"px\";\
    document.getElementById(F).width= (%d) + \"px\";\
    }" ;
    
    NSLog(@"The parameters for this flow look like this: %@", self.parameters);
    return [NSString stringWithFormat:js, self.nemIDJavascriptURL, self.parameters, (int)iframeHeight, (int)iframeWidth];
}

#pragma mark - App Switch

- (BOOL) codeAppAvailable {
	UIApplication *application = [UIApplication sharedApplication];
	NSURL *URL = [NSURL URLWithString:@"nemid-codeapp://codeapp.e-nettet.dk"];
	return [application canOpenURL:URL];
}

- (void) doAppSwitchWithReturnUrl:(NSString *) returnUrl {
	if([self codeAppAvailable]){
		UIApplication *application = [UIApplication sharedApplication];
		NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"https://codeapp.e-nettet.dk?return=%@", returnUrl]];
		if(url != nil){
			[application openURL:url];
		}
	}
}

- (void)enableAppSwitch {
	[self doAppSwitchWithReturnUrl:@"flutternemid://nemidfinished"];
}

- (void)putSignResponse:(NSString *) xmlDsig withSuccess:(ValidationFetcherSuccessBlock)successBlock error:(ValidationFetcherErrorBlock)errorBlock {
    [ValidationFetcher fetchValidationWithBackendUrl:self.controller.validationEndpoint andData:xmlDsig success:successBlock
    error:^(NSInteger errorCode, NSString *errorMessage) {
        NSLog(@"Error during putSignResponse. ErrorCode was: %lu. ErrorMessage was: %@", (long)errorCode, errorMessage);
        errorBlock(errorCode,errorMessage);
    }
     ];
}

- (void)validateResponse:(ValidationResponse *)response{
    NSLog(@"VALIDATION RESPONSE: %@",response);
    NSString *responseString = [NSString stringWithFormat:@"%@", response];
    [self.controller sendResult:responseString];
}

- (NSString *)getFlowDetailsFromValidationResponse:(ValidationResponse *) valResponse andJSClientResponse:(NSString *) content{
    
    NSString *resultDetailsHeader = @"----------------------------- \
                                     Validation Backend Details: \
                                     ----------------------------- \
                                    %@ \n\n";
    
    NSString *flowDetails = @"--------------------- \
                            JS Client Response: \
                            --------------------- \
                            %@";
    
    NSMutableString *result = [[NSMutableString alloc] init];
    if(valResponse.result!=nil){
        [result appendString:[NSString stringWithFormat:resultDetailsHeader, valResponse.result]];
    }
    [result appendString:[NSString stringWithFormat:flowDetails, content]];
    
    return result;
}


#pragma mark - WKNavigationDelegate delegate methods

- (void)webView:(WKWebView *)webView decidePolicyForNavigationAction:(WKNavigationAction *)navigationAction decisionHandler:(void (^)(WKNavigationActionPolicy))decisionHandler
{
    NSString *urlStr = [navigationAction.request.URL absoluteString];
    NSLog(@"WKWebView handle request: %@", urlStr);
    
    // Evaluate JavaScript
    if ([[navigationAction.request.URL scheme] isEqualToString:@"changeresponseandsubmit"]) {
        [webView evaluateJavaScript:@"getContent();"
                  completionHandler: ^(NSString *content, NSError *error) {
                      NSString *contentNormalized;
                      if (error) {
                          NSLog(@"Got error while evaluating getContent(): %@", error.localizedDescription);
                          //[self.controller.responseTextView setText:error.localizedDescription];
                      } else {
                          contentNormalized = [NetworkUtilities base64Decode:content];
                          NSLog(@"Got content while evaluating getContent(): %@", contentNormalized);
                          [self putSignResponse:content withSuccess:^(ValidationResponse *validationResponse) {
                              [self validateResponse:validationResponse];
                              [self getFlowDetailsFromValidationResponse:validationResponse andJSClientResponse:contentNormalized];
                              [self dismissViewControllerAnimated:NO completion:nil];
                              //[self.controller.responseTextView setText:flowDetails];
                          } error:^(NSInteger errorCode, NSString *errorMessage) {
                              [NSString stringWithFormat:@"Internal app error.\nError code: %lu\nError message: %@",errorCode,errorMessage];
                          }];
                          [self.navigationController popToRootViewControllerAnimated:YES];
                      }
                  } ];
        decisionHandler(WKNavigationActionPolicyCancel);
        return;
    }
    
    if ([[navigationAction.request.URL scheme] isEqualToString:@"awaitingappapproval"]) {
        [self enableAppSwitch];
        decisionHandler(WKNavigationActionPolicyCancel);
        return;
    }
    
    // Open external NemID links in native browser
    if (navigationAction.navigationType == WKNavigationTypeLinkActivated)
    {
        NSLog(@"Opening link in native browser: %@", urlStr);
        [[UIApplication sharedApplication] openURL:navigationAction.request.URL];
        decisionHandler(WKNavigationActionPolicyCancel);
        return;
    }
    
    //Pass back to the decision handler
    decisionHandler(WKNavigationActionPolicyAllow);
}

- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation {
    // Removing the margin of the webview
    NSString *padding = @"document.body.style.margin='0';document.body.style.padding='0'";
    
    // Check that all content in Webview was loaded via HTTPS
    
        if (![webView hasOnlySecureContent]) {
            UIAlertController *alertController = [UIAlertController
                                                  alertControllerWithTitle:@"Warning!"
                                                  message:@"Some content was not loaded over a secure connection."
                                                  preferredStyle:UIAlertControllerStyleAlert];
            
            UIAlertAction *okAction = [UIAlertAction
                                       actionWithTitle:@"OK"
                                       style:UIAlertActionStyleDefault
                                       handler:^(UIAlertAction *action)
                                       {
                                           [alertController dismissViewControllerAnimated:YES completion:nil];
                                           [self.navigationController popToRootViewControllerAnimated:YES];
                                       }];
            
            [alertController addAction:okAction];
            
            [self presentViewController:alertController animated:YES completion:nil];
        }
    
    
    // Evaluate JavaScript
    [webView evaluateJavaScript:padding completionHandler:nil];
}
@end
