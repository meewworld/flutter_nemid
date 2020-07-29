//
//  MainViewController.m
//  TestNemIdJavascript
//
//     

#import "MainViewController.h"
#import "NemIDViewController.h"
#import "ValidationFetcher.h"
#import "NetworkUtilities.h"
#import "ClientDimensions.h"

@interface MainViewController () <UITextFieldDelegate>
// PARAMETERS
@property (weak, nonatomic) IBOutlet UIButton *languageButton;
@property (strong, nonatomic) NSArray *languageArray;

@property (weak, nonatomic) IBOutlet UIButton *signtextButton;
@property (strong, nonatomic) NSArray *signtextArray;

@property (weak, nonatomic) IBOutlet UISwitch *smallIframeSwitch;

// LOGOUT AND CLEAR USERID TOKEN
@property (weak, nonatomic) IBOutlet UIButton *logOutButton;
@property (weak, nonatomic) IBOutlet UIButton *clearUseridButton;

// ADVANCED PARAMETERS
@property (nonatomic, retain) IBOutlet UITextField *widthTextField;
@property (nonatomic, retain) IBOutlet UITextField *heightTextField;

@property (nonatomic, retain) IBOutlet UISwitch *stepUpSwitch;
@property (weak, nonatomic)   IBOutlet UISwitch *useWKWebKitSwitch;
@property (weak, nonatomic)   IBOutlet UISwitch *useAppSwitch;
@property (weak, nonatomic)   IBOutlet UISwitch *suppressPushToDevice;

@property (nonatomic, retain) IBOutlet UILabel *buildDate;
@property (weak, nonatomic) IBOutlet UILabel *appVersionAndRevision;

// ACTIONS
- (IBAction)languageButtonClicked:(id)sender;
- (IBAction)signtextButtonClicked:(id)sender;

- (IBAction)startFlow:(UIButton*)sender;

- (IBAction)logoutButtonClicked:(id)sender;
- (IBAction)clearUseridToken:(id)sender;

@end


@implementation MainViewController

BOOL _isLoggedIn;
NSString *_parameterResponse;

#pragma mark - UIViewController lifecycle methods

- (void)viewDidLoad {
    [super viewDidLoad];

    self.spBackendURLTextField.text = BackendUrlPrefix;
    self.nemIDBackendURLTextField.text = BackendUrlPrefix;
    
    self.spBackendURLTextField.autocorrectionType = UITextAutocorrectionTypeNo;
    self.nemIDBackendURLTextField.autocorrectionType = UITextAutocorrectionTypeNo;    
    
    self.responseTextView.accessibilityLabel = @"responsetext";
    self.languageArray = @[@"DA (Danish)",
                           @"EN (English)",
                           @"KL (Greenlandic)"];
    [self.languageButton setTitle:self.languageArray[0] forState:UIControlStateNormal];
    self.signtextArray = @[@"default.html",
                           @"default.txt",
                           @"shorttext.txt",
                           @"default.xml",
                           @"invalid.xml",
                           @"default.pdf"];
    [self.signtextButton setTitle:self.signtextArray[0] forState:UIControlStateNormal];
    NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    [dateFormat setDateFormat:@"MM/dd/yyyy"];
    NSDate* buildDate = [dateFormat dateFromString:[[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBuildDate"]];
    [dateFormat setDateFormat:@"dd/MM/yyyy"];
    self.buildDate.text = [NSString stringWithFormat:@"Build date: %@",[dateFormat stringFromDate:buildDate]];
    
    
    NSString* appVersion = (NSString*)[[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"];
    NSString* revisionNumber = (NSString*)[[[NSBundle mainBundle] infoDictionary] objectForKey:@"eu.nets.testjavascript.revisionnumber"];
    self.appVersionAndRevision.text = [NSString stringWithFormat:@"App Version: %@",appVersion];
    
    if([revisionNumber length]){
        NSString *revisionNumberText = [NSString stringWithFormat:@", SVN Revision: %@",revisionNumber];
        self.appVersionAndRevision.text = [self.appVersionAndRevision.text stringByAppendingString:revisionNumberText];
    }
    
    
    // Check if WKWebView is available
    if (!NSClassFromString(@"WKWebView")) {
        self.useWKWebKitSwitch.enabled = NO;
        self.useWKWebKitSwitch.on = NO;
    }
    
    // The SP backend URL and NemID backend URL are separated into two different textfields to make it
    // possible to connect to diffent backends. When changing SP backend URL the URL is copied to the
    // NemID backend URL. Changing NemID backend URL does not affect the SP backend URL.
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(copySPBackendToNIDBackend:) name:UITextFieldTextDidChangeNotification object:nil];
}

-(void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [(UIScrollView*)self.view setContentSize:CGSizeMake(320, 1370)];
    [self updateClearUseridButton];
}


-(void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}


#pragma mark - IBAction methods

- (IBAction)languageButtonClicked:(UIButton *)sender {
    UIAlertController *pickerAlertContoller = [UIAlertController alertControllerWithTitle:@"Choose language" message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    for (NSString *title in self.languageArray) {
        [pickerAlertContoller addAction:[UIAlertAction actionWithTitle:title style:UIAlertActionStyleDefault
                                                                            handler:^(UIAlertAction * action) {
                                                                                [self.languageButton setTitle:title forState:UIControlStateNormal];
                                                                            }]];
    }
    
    [pickerAlertContoller addAction:[UIAlertAction actionWithTitle:@"Cancel" style:UIAlertActionStyleCancel
                                                                        handler:^(UIAlertAction * action) {}]];
    
    UIPopoverPresentationController *popover = [pickerAlertContoller popoverPresentationController];
    if (popover) {
        popover.sourceView = sender;
        popover.sourceRect = sender.bounds;
        popover.permittedArrowDirections = UIPopoverArrowDirectionAny;
    }
    
    [self presentViewController:pickerAlertContoller animated:YES completion:nil];
}

- (IBAction)signtextButtonClicked:(UIButton *)sender {
    UIAlertController *pickerAlertContoller = [UIAlertController alertControllerWithTitle:@"Choose signtext" message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    for (NSString *title in self.signtextArray) {
        [pickerAlertContoller addAction:[UIAlertAction actionWithTitle:title style:UIAlertActionStyleDefault
                                                                       handler:^(UIAlertAction * action) {
                                                                           [self.signtextButton setTitle:title forState:UIControlStateNormal];
                                                                       }]];
    }
    
    [pickerAlertContoller addAction:[UIAlertAction actionWithTitle:@"Cancel" style:UIAlertActionStyleCancel
                                                                   handler:^(UIAlertAction * action) {}]];
    
    UIPopoverPresentationController *popover = [pickerAlertContoller popoverPresentationController];
    if (popover) {
        popover.sourceView = sender;
        popover.sourceRect = sender.bounds;
        popover.permittedArrowDirections = UIPopoverArrowDirectionAny;
    }
    
    [self presentViewController:pickerAlertContoller animated:YES completion:nil];    
}

- (IBAction)startFlow:(UIButton*)sender {
    self.currentRequestType = (RequestType)sender.tag;

    NSLog(@"Starting flow: '%@'", [self getFlowNameFromRequestType:self.currentRequestType]);

    //Reset status messages textview
    [self resetStatusMessages];
    
    //Fetch parameters from SP backend
    NSTimeInterval start = [NSDate timeIntervalSinceReferenceDate];
    [self parameterResponse:self.currentRequestType success:^(NSString *parameters) {
        
        NSLog(@"Fetching parameters from SP backend completed in: %f seconds", [NSDate timeIntervalSinceReferenceDate] - start);
        
        //Pass parameters to next view, and go to view
        if (parameters) {
            NemIDViewController *nemIDViewController;
            if ((UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) && [self isRequestTypeLogin:self.currentRequestType]) {
                nemIDViewController = [[UIStoryboard storyboardWithName:@"NemID_iPad" bundle:nil] instantiateViewControllerWithIdentifier:@"NemID_iPad"];
            } else {
                nemIDViewController = [[UIStoryboard storyboardWithName:@"NemID_iPhone" bundle:nil] instantiateViewControllerWithIdentifier:@"NemIDViewController"];
            }
            
            ClientDimensions *clientDimensions = [self getClientDimensions];
            
            // Set relevant parameters for NemIDViewController
            nemIDViewController.parameters = parameters;
            NSString *launcherUrl =[NSString stringWithFormat:@"%@%@", self.nemIDBackendURLTextField.text, LauncherURL];
            nemIDViewController.nemIDJavascriptURL = launcherUrl;
            nemIDViewController.width = clientDimensions.width;
            nemIDViewController.height = clientDimensions.height;
            nemIDViewController.controller = self;
            nemIDViewController.useWKWebView = self.useWKWebKitSwitch.on;
            
            [self.navigationController pushViewController:nemIDViewController animated:YES];
        }
        else {
            [self.responseTextView setText:[NSString stringWithFormat:@"Error in parameter response from %@", GenerateParameterURL]];
        }
    } error:^(NSInteger errorCode, NSString *errorMessage) {
        NSLog(@"Error while starting flow. ErrorCode was: %lu. ErrorMessage was: %@", (long)errorCode, errorMessage);
    }];
}

- (ClientDimensions *)getClientDimensions {
    ClientDimensions *clientDimensions = [ClientDimensions new];
    
    if (self.smallIframeSwitch.on) {
        if ([self isRequestTypeLogin:self.currentRequestType]) {
            if ([[self getLanguage] isEqualToString:@"KL"]) {
                clientDimensions.width = @"250";
                clientDimensions.height = @"300";
            } else {
                clientDimensions.width = @"200";
                clientDimensions.height = @"250";
            }
        } else {
            clientDimensions.width = @"320";
            clientDimensions.height = @"460";
        }
    }
    
    return clientDimensions;
}

- (IBAction)logoutButtonClicked:(id)sender {
    [ValidationFetcher logOut:self.spBackendURLTextField.text success:^(ValidationResponse *validationResponse) {
        if([validationResponse.logOutResult isEqualToString:@"OK"]){
            [self setLoggedInTo:NO];
            NSLog(@"Did logout. Response was: %@",validationResponse.logOutResult);
        };
    } error:^(NSInteger errorCode, NSString *errorMessage) {
        NSLog(@"Error during logout. ErrorCode was: %lu. ErrorMessage was: %@", (long)errorCode, errorMessage);
    }];
}

- (IBAction)clearUseridToken:(id)sender {
    self.rememberUseridToken = @"";
    [self updateClearUseridButton];
}


#pragma mark - Helper methods

-(NSString *)getFlowNameFromRequestType:(RequestType)requestType{
    NSString* result = @"";
    switch (requestType) {
        case RequestTypeOneFactorLogin:
            result = @"ONE FACTOR LOGIN";
            break;
        case RequestTypeTwoFactorLogin:
            result = @"TWO FACTOR LOGIN";
            break;
        case RequestTypeOneFactorSign:
            result = @"ONE FACTOR SIGN";
            break;
        case RequestTypeTwoFactorSign:
            result = @"TWO FACTOR SIGN";
            break;
        case RequestTypeTwoFactorLoginLongTerm:
            result = @"TWO FACTOR LONG TERM LOGIN";
            break;
        case RequestTypeTwoFactorSignLongTerm:
            result = @"TWO FACTOR LONG TERM SIGN";
            break;
        default:
            break;
    }
    return result;
}

-(NSString *)getLanguage {
    return [self.languageButton.titleLabel.text substringToIndex:2];
}

- (NSString *)getSignTextFilename {
    return self.signtextButton.titleLabel.text;
}

-(NSString*)getSelectedSignText{
    NSString* filename = [self getSignTextFilename];
    if ([filename isEqualToString:self.signtextArray[0]]) { return htmlSignText; }
    if ([filename isEqualToString:self.signtextArray[1]]) { return plainSignText; }
    if ([filename isEqualToString:self.signtextArray[2]]) { return plainSignTextShort; }
    if ([filename isEqualToString:self.signtextArray[3]]) { return xmlSignText; }
    if ([filename isEqualToString:self.signtextArray[4]]) { return @"invalid xml"; }
    if ([filename isEqualToString:self.signtextArray[5]]) { return pdfSignText; }
    return nil;
}

- (NSString *)getSignTextFilenameExtension {
    NSString *temp = [self getSignTextFilename];
    
    if ([[temp pathExtension] isEqualToString:@"html"]) return @"HTML";
    if ([[temp pathExtension] isEqualToString:@"txt"]) return @"TEXT";
    if ([[temp pathExtension] isEqualToString:@"xml"] || [[temp pathExtension] isEqualToString:@"xslt"]) return @"XML";
    else return @"PDF";
}

- (void)parameterResponse:(RequestType)requestType
                  success:(ParameterFetcherSuccessBlock)successBlock
                    error:(ParameterFetcherErrorBlock)errorBlock {
    NSString *samlProviderUrl = [NSString stringWithFormat:@"%@%@", self.spBackendURLTextField.text, GenerateParameterURL];
    NSString *stepUp = self.stepUpSwitch.isOn ? @"TRUE" : @"FALSE";
    NSString *suppressPush = self.suppressPushToDevice.isOn ? @"TRUE" : @"FALSE";
    NSString *useAppSwitch = self.useAppSwitch.isOn ?  @"TRUE" : @"FALSE";
    
    if(requestType == RequestTypeTwoFactorLogin) {
        [ParameterFetcher fetchTwoFactorLoginWithSamlProvider:[NSURL URLWithString:samlProviderUrl]
                                                                            issuer:@"49"
                                                                          language:[self getLanguage]
                                                               rememberuseridtoken:[self getRememberUseridToken]
                                                              suppressPushToDevice:suppressPush
                                                                      useAppSwitch:useAppSwitch
                                                                           success:successBlock
                                                                             error:errorBlock];
    }
    else if(requestType == RequestTypeOneFactorLogin) {
        [ParameterFetcher fetchOneFactorLoginWithSamlProvider:[NSURL URLWithString:samlProviderUrl]
                                                                            issuer:@"49"
                                                                          language:[self getLanguage]
                                                               rememberuseridtoken:[self getRememberUseridToken]
                                                                           success:successBlock
                                                                             error:errorBlock];
    }
    else if(requestType == RequestTypeTwoFactorSign) {
        NSString *transform = [[self getSignTextFilenameExtension] isEqualToString:@"XML"] ? xmlSignStylesheet : nil;
        
        [ParameterFetcher fetchTwoFactorSignWithSamlProvider:[NSURL URLWithString:samlProviderUrl]
                                                                           issuer:@"49"
                                                                         signText:[self getEncodedSignText]
                                                               signTransformation:transform
                                                                   signTextFormat:[self getSignTextFilenameExtension]
                                                                         language:[self getLanguage]
                                                              rememberuseridtoken:[self getRememberUseridToken]
                                                             suppressPushToDevice:suppressPush
                                                                     useAppSwitch:useAppSwitch
                                                                          success:successBlock
                                                                            error:errorBlock];
    }
    else if(requestType == RequestTypeOneFactorSign) {
        [ParameterFetcher fetchOneFactorSignWithSamlProvider:[NSURL URLWithString:samlProviderUrl]
                                                                           issuer:@"49"
                                                                         signText:[self getEncodedSignText]
                                                                           stepUp:stepUp
                                                               signTransformation:xmlSignStylesheet
                                                                   signTextFormat:[self getSignTextFilenameExtension]
                                                                         language:[self getLanguage]
                                                              rememberuseridtoken:[self getRememberUseridToken]
                                                             suppressPushToDevice:suppressPush
                                                                     useAppSwitch:useAppSwitch
                                                                          success:successBlock
                                                                            error:errorBlock];
    }
    
    //New two factor long term flows
    else if(requestType == RequestTypeTwoFactorLoginLongTerm){
        NSLog(@"Starting RequestTypeTwoFactorLoginLongTerm");
        [ParameterFetcher fetchTwoFactorLoginLongTermWithSamlProvider:[NSURL URLWithString:samlProviderUrl]
                                                                                    issuer:@"1"
                                                                                  language:[self getLanguage]
                                                                       rememberuseridtoken:[self getRememberUseridToken]
                                                                      suppressPushToDevice:suppressPush
                                                                              useAppSwitch:useAppSwitch
                                                                                   success:successBlock
                                                                                     error:errorBlock];
    }
    else if(requestType == RequestTypeTwoFactorSignLongTerm){
        NSLog(@"Starting RequestTypeTwoFactorSignLongTerm");
        NSString *transform = [[self getSignTextFilenameExtension] isEqualToString:@"XML"] ? xmlSignStylesheet : nil;

        [ParameterFetcher fetchTwoFactorSignLongTermWithSamlProvider:[NSURL URLWithString:samlProviderUrl]
                                                                                   issuer:@"1"
                                                                                 signText:[self getEncodedSignText]
                                                                       signTransformation:transform
                                                                           signTextFormat:[self getSignTextFilenameExtension]
                                                                                 language:[self getLanguage]
                                                                      rememberuseridtoken:[self getRememberUseridToken]
                                                                     suppressPushToDevice:suppressPush
                                                                             useAppSwitch:useAppSwitch
                                                                                  success:successBlock
                                                                                    error:errorBlock];
    }
}

-(NSString *)getEncodedSignText{
    // PDF is already base64 encoded, but other texts need to be encoded
    return [[self getSignTextFilenameExtension] isEqualToString:@"PDF"] ? [self getSelectedSignText] : [NetworkUtilities base64Encode:[self getSelectedSignText]];
}

- (void)resetStatusMessages {
    [self.responseTextView setText:@""];
}

- (BOOL)currentFlowIsBankFlow {
    if(self.currentRequestType == RequestTypeOneFactorLogin || self.currentRequestType == RequestTypeTwoFactorLogin || self.currentRequestType == RequestTypeTwoFactorSign || self.currentRequestType == RequestTypeOneFactorSign){
        return YES;
    }
    return NO;
}

-(BOOL)isRequestTypeLogin:(RequestType)requestType{
    switch (self.currentRequestType) {
        case RequestTypeOneFactorLogin:
        case RequestTypeTwoFactorLogin:
        case RequestTypeTwoFactorLoginLongTerm:
            return YES;
        default:
            return NO;
    }
}

-(void)setRememberUseridToken:(NSString *)rememberUseridToken{
    NSString *value = @"";
    if (rememberUseridToken != nil) {
        value = rememberUseridToken;
    }
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    [userDefaults setObject:value forKey:@"rememberUseridToken"];
    [userDefaults synchronize];
    NSLog(@"Saved RememberUseridToken: %@", [[NSUserDefaults standardUserDefaults] objectForKey:@"rememberUseridToken"]);
    [self updateClearUseridButton];
}

-(NSString*)getRememberUseridToken{
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    NSString *result = [userDefaults objectForKey:@"rememberUseridToken"];
    return (result != nil) ? result : @"";
}

- (void) setLoggedInTo:(BOOL) state{
    _isLoggedIn = state;
    [self updateLogoutButton];
    if(!_isLoggedIn){
        [self.responseStatusTextField setText:@"Logged out."];
    }
}

-(void)updateLogoutButton{
    if (_isLoggedIn) {
        self.logOutButton.enabled = YES;
    } else {
        self.logOutButton.enabled = NO;
    }
}

-(void)updateClearUseridButton{
    if ([self getRememberUseridToken].length > 0) {
        self.clearUseridButton.enabled = YES;
    } else {
        self.clearUseridButton.enabled = NO;
    }
}

// Copy SP backend URL to NemID backend URL.
-(void)copySPBackendToNIDBackend:(NSNotification*)notification{
    UITextField* textField = notification.object;
    if (textField == self.spBackendURLTextField) {
        self.nemIDBackendURLTextField.text = self.spBackendURLTextField.text;
    }
}


#pragma mark - UITextField delegate methods

-(void)textFieldDidBeginEditing:(UITextField *)textField{
    [UIView animateWithDuration:0.3 delay:0
                        options:UIViewAnimationOptionCurveEaseInOut
                     animations:^{
                         ((UIScrollView*)self.view).contentOffset = CGPointMake(0, textField.frame.origin.y - 100);
                     }
                     completion:^(BOOL finished){}];
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField{
    [textField resignFirstResponder];
    
    if ([textField isEqual:self.spBackendURLTextField]){
        self.nemIDBackendURLTextField.text = self.spBackendURLTextField.text;
    }
    
    [UIView animateWithDuration:0.3 delay:0
                        options:UIViewAnimationOptionCurveEaseInOut
                     animations:^{
                         ((UIScrollView*)self.view).contentOffset = CGPointMake(0, textField.frame.origin.y - 300);
                     }
                     completion:^(BOOL finished){}];
    return YES;
}

@end
