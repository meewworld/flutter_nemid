//
//  NemIDAppSwitcher.m
//  
//
//  Created by Michael on 14/10/2020.
//

#import <Foundation/Foundation.h>

@interface NemIDAppSwitcher:NSObject

+ (BOOL) codeAppAvailable;
+ (void) doAppSwitchWithReturnUrl:(NSString *) returnUrl;

@end

@implementation NemIDAppSwitcher

+ (BOOL) codeAppAvailable {
	UIApplication *application = [UIApplication sharedApplication];
	NSURL *URL = [NSURL URLWithString:@"nemid-codeapp://codeapp.e-nettet.dk"];
	return [application canOpenUrl:URL];
}

+ (void) doAppSwitchWithReturnUrl:(NSString *) returnUrl {
	if([NemIDAppSwitcher codeAppAvailable]){
		UIApplication *application = [UIApplication sharedApplication];
		NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"https://codeapp.e-nettet.dk?return=%@", returnUrl]];
		if(url != nil){
			[application openUrl:url];
		}
	}
}

@end
