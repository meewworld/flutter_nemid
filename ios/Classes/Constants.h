//
//  Constants.h
//  TestNemIdJavascript
//
//

/**
 * Constants.h contains URLs and signtext
 */

#define BackendUrlPrefix @"https://appletk.danid.dk"

#define GenerateParameterURL @"/developers/mobile/mobileAppParameterGeneratorForJS.jsp"
#define LauncherURL @"/launcher/lmt"
#define SamlReceiverURL @"/developers/mobile/samlReceiverJS.jsp"
#define SignProviderURL @"/developers/mobile/signReceiverJS.jsp"
#define LogoutURL @"/developers/mobile/logOutJS.jsp"


// Definition of the request type used when starting NemID flows
typedef enum {
    // Bank
    RequestTypeOneFactorLogin = 1,
    RequestTypeTwoFactorLogin = 2,
    RequestTypeOneFactorSign = 3,
    RequestTypeTwoFactorSign = 4,
    // OCES
    RequestTypeTwoFactorLoginLongTerm = 5,
    RequestTypeTwoFactorSignLongTerm = 6,
} RequestType;
