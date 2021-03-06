//
//  JaguarExtensions.m
//  OSXvnc
//
//  Created by Jonathan Gillaspie on Fri Jul 11 2003.
//  Copyright (c) 2003 RedstoneSoftware, Inc. All rights reserved.


#import "JaguarExtensions.h"

#import <ApplicationServices/ApplicationServices.h>
#import <Cocoa/Cocoa.h>
#import <Carbon/Carbon.h>

#include "keysymdef.h"
#include "kbdptr.h"

#include <sys/stat.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <fcntl.h>
#include <pthread.h>
#include <unistd.h>
#include <signal.h>

#include "rfb.h"

#include "rfbserver.h"
#import "VNCServer.h"

#import "../RFBBundleProtocol.h"

@implementation JaguarExtensions

static NSNetService *rfbService;
static NSNetService *vncService;
static BOOL keyboardLoading = FALSE;

static KeyboardLayoutRef loadedKeyboardRef;
static BOOL useIP6 = TRUE;
static BOOL listenerFinished = FALSE;

rfbserver *theServer;

+ (void) loadGUI {
	//	ProcessSerialNumber psn = { 0, kCurrentProcess }; 
	//	OSStatus returnCode = TransformProcessType(& psn, kProcessTransformToForegroundApplication);
	//	returnCode = SetFrontProcess(& psn );
	//	if( returnCode != 0) {
	//		NSLog(@"Could not bring the application to front. Error %d", returnCode);
	//	}
	//[[NSApplication sharedApplication] activateIgnoringOtherApps: YES];
}

+ (void) rfbStartup: (rfbserver *) aServer {
	[[NSUserDefaults standardUserDefaults] registerDefaults:[NSDictionary dictionaryWithObjectsAndKeys:
		@"NO", @"keyboardLoading", // allows OSXvnc to look at the users selected keyboard and map keystrokes using it
		@"YES", @"pressModsForKeys", // If OSXvnc finds the key you want it will temporarily toggle the modifier keys to produce it
		[NSArray arrayWithObjects:[NSNumber numberWithInt:kUCKeyActionDisplay], [NSNumber numberWithInt:kUCKeyActionAutoKey], nil], @"KeyStates", // Key States to review to find KeyCodes
		//[NSNumber numberWithInt:kUCKeyActionDisplay], [NSNumber numberWithInt:kUCKeyActionDown], nil],
		[NSArray arrayWithObjects:[NSNumber numberWithInt:kUCKeyActionAutoKey], [NSNumber numberWithInt:kUCKeyActionDisplay], [NSNumber numberWithInt:kUCKeyActionDown], nil], @"KeyStates", // Key States to review to find KeyCodes
		nil]];

    theServer = aServer;
	
	keyboardLoading = [[NSUserDefaults standardUserDefaults] boolForKey:@"keyboardLoading"];	
    if (keyboardLoading) {
		OSErr result;
		
        NSLog(@"Keyboard Loading - Enabled");

        *(theServer->pressModsForKeys) = [[NSUserDefaults standardUserDefaults] boolForKey:@"pressModsForKeys"];
        if (*(theServer->pressModsForKeys))
            NSLog(@"Press Modifiers For Keys - Enabled");
        else
            NSLog(@"Press Modifiers For Keys - Disabled");

		result = KLGetCurrentKeyboardLayout(&loadedKeyboardRef);
        if (result == noErr)
            [self loadKeyboard: loadedKeyboardRef];
		else
			NSLog(@"Error (%u) unabled to load current keyboard layout", result);
    }
	
    if ([[[NSProcessInfo processInfo] arguments] indexOfObject:@"-ipv4"] != NSNotFound) {
		useIP6 = FALSE;
	}
}

+ (void) rfbUsage {
    fprintf(stderr,
            "\nJAGUAR BUNDLE OPTIONS (10.2+):\n"
            "-keyboardLoading flag  This feature allows OSXvnc to look at the users selected keyboard and map keystrokes using it.\n"
            "                       Disabling this returns OSXvnc to standard (U.S. Keyboard) which will work better with Dead Keys.\n"
            "                       (default: no), 10.2+ ONLY\n"
            "-pressModsForKeys flag If OSXvnc finds the key you want it will temporarily toggle the modifier keys to produce it.\n"
            "                       This flag works well if you have different keyboards on the local and remote machines.\n"
            "                       Only works if -keyboardLoading is on\n"
            "                       (default: yes), 10.2+ ONLY\n"
	        "-bonjour flag       Allow OSXvnc to advertise VNC server using Bonjour discovery services.\n"
			"                       'VNC' will enable the service named VNC (For Eggplant & Chicken 2.02b)\n"
			"                       'Both' or '2' will enable the services named RFB and VNC\n"
			"                       (default: RFB:YES VNC:NO), 10.2+ ONLY\n"
	        "-ipv4                  Listen For Connections on IPv4 ONLY (Default: Off). 10.2+ ONLY\n"
	        "-ipv6                  Listen For Connections on IPv6 ONLY (Default: Off). 10.2+ ONLY\n"
			);
}


// Here are the resources we can think about using for Int'l keyboard support

// http://developer.apple.com/documentation/Carbon/Reference/KeyboardLayoutServices/

// This will let us determine the current keyboard
// KLGetCurrentKeyboardLayout

// This will set the current keyboard
// KLSetCurrentKeyboardLayout

// This will get the Properties of a keyboard (like the key code to char tables) but...
// KLGetKeyboardLayoutProperty

// UCKeyTranslate - This is the opposite of what we need, if you give it a table and some keyCode (key input) it will tell you what Unicode char (or string) you get
// we need the opposite - we want to know what keys to hit to get a given key (or string)

// This will use the KeyboardLayoutRef to produce a static table of lookups
// By iterating through all possible KeyCodes
+ (void) loadKeyboard: (KeyboardLayoutRef) keyboardLayoutRef {
    int i, j;
    UCKeyboardLayout *uchrHandle = NULL;
    const void *kchrHandle = NULL;
    CFStringRef keyboardName;
    KeyboardLayoutKind layoutKind;
    static UInt32 modifierKeyStates[] = {0, shiftKey, optionKey, controlKey, optionKey | shiftKey, optionKey | controlKey, controlKey | shiftKey, optionKey | shiftKey | controlKey};
	UInt32 modifierKeyState = 0;	
	NSArray *keyStates = [[NSUserDefaults standardUserDefaults] arrayForKey:@"KeyStates"];
	
    /* modifiers */
    //cmdKey                        = 1 << cmdKeyBit,
    //shiftKey                      = 1 << shiftKeyBit,
    //alphaLock                     = 1 << alphaLockBit,
    //optionKey                     = 1 << optionKeyBit,
    //controlKey                    = 1 << controlKeyBit,
    
    // KLGetKeyboardLayoutProperty is 10.2 only how do I access these resources in early versions?
    if (keyboardLayoutRef) {
        KLGetKeyboardLayoutProperty(keyboardLayoutRef, kKLName, (const void **) &keyboardName);
        KLGetKeyboardLayoutProperty(keyboardLayoutRef, kKLKind, (const void **) &layoutKind);
        NSLog(@"Keyboard Detected: %@ (Type:%d) - Loading Keys\n", keyboardName, layoutKind);
        if (layoutKind == kKLKCHRuchrKind || layoutKind == kKLuchrKind)
            KLGetKeyboardLayoutProperty(keyboardLayoutRef, kKLuchrData, (const void **) &uchrHandle);
        else
            KLGetKeyboardLayoutProperty(keyboardLayoutRef, kKLKCHRData, (const void **) &kchrHandle);
    }

    // Initialize them all to 0xFFFF
    memset(theServer->keyTable, 0xFF, keyTableSize * sizeof(CGKeyCode));
    memset(theServer->keyTableMods, 0xFF, keyTableSize * sizeof(unsigned char));
  
    if (uchrHandle) {
        // Ok - we could get the LIST of Modifier Key States out of the Keyboard Layout
        // some of them are duplicates so we need to compare them, then we'll iterate through them in reverse order
        // UCKeyModifiersToTableNum = ; EventRecord
        // This layout gets a little harry

        UInt16 keyCode;
        UInt32 keyboardType = LMGetKbdType();
        UInt32 deadKeyState = 0;
        UniCharCount actualStringLength;
        UniChar unicodeChar[255];

        // Iterate Over Each Modifier Keyset
        for (i=0; i < (sizeof(modifierKeyStates) / sizeof(UInt32)); i++) {
            modifierKeyState = (modifierKeyStates[i] >> 8) & 0xFF;
            //NSLog(@"Loading Keys For Modifer State: %#04x", modifierKeyState);
            // Iterate Over Each Key Code
            for (keyCode = 0; keyCode < 127; keyCode++) {
				for (j=0; j < [keyStates count]; j++) {
					int keyActionState = [[keyStates objectAtIndex:j] intValue];
					OSStatus resultCode = UCKeyTranslate (uchrHandle,
														  keyCode,
														  keyActionState,
														  modifierKeyState,
														  keyboardType,
														  kUCKeyTranslateNoDeadKeysBit,
														  &deadKeyState,
														  255, // Only 1 key allowed due to VNC behavior
														  &actualStringLength,
														  unicodeChar);
					
					if (resultCode == noErr) {
						if (actualStringLength > 1) {
							NSLog(@"Multiple Characters For %d (%#04x): %S",  keyCode, modifierKeyState, unicodeChar);
							//unicodeChar[0] = unicodeChar[actualStringLength-1];
						}
						else {
							// We'll use the FIRST keyCode that we find for that UNICODE character
							if (theServer->keyTable[unicodeChar[0]] == 0xFFFF) {
								theServer->keyTable[unicodeChar[0]] = keyCode;
								theServer->keyTableMods[unicodeChar[0]] = modifierKeyState;
							}
						}
					}
					else {
						NSLog(@"Error Translating %d (%#04x): %d",  keyCode, modifierKeyState, resultCode);
					}
				}
			}
        }
    }
    else if (kchrHandle) {
        UInt16 keyCode;
        UInt32 state=0;
        UInt32 kchrCharacters;

        // Ok - we need to get the LIST of Modifier Key States out of the Keyboard Layout
        // some of them are duplicates so we need to compare them, then we'll iterate through them in reverse order
        //UCKeyModifiersToTableNum = ;
        for (i=0; i < (sizeof(modifierKeyStates) / sizeof(UInt32)); i++) {
            modifierKeyState = (modifierKeyStates[i] >> 8) & 0xFF;
            //NSLog(@"Loading Keys For Modifer State:%#04x", modifierKeyState);

            // Iterate Over Each Key Code
            for (keyCode = 0; keyCode < 127; keyCode++) {
                // We pass the modifierKeys as the top 8 bits of keycode
                kchrCharacters = KeyTranslate(kchrHandle, (modifierKeyState<<8 | keyCode), &state);

                if (kchrCharacters & 0xFFFF0000) {
                    NSLog(@"Unable To Convert KeyCode, Multiple Characters (%#04x) (%#04x) For: %d (%#04x)",
                          kchrCharacters>>16 & 0xFFFF, kchrCharacters & 0xFFFF, keyCode, modifierKeyState);
                }
                else {
                    // We'll use the FIRST keyCode that we find for that UNICODE character
                    if (theServer->keyTable[kchrCharacters & 0xFFFF] == 0xFFFF) {
                        //NSLog(@"KeyCode:%d UniCode:%d", keyCode, kchrCharacters & 0xFFFF);
                        theServer->keyTable[kchrCharacters & 0xFFFF] = keyCode;
                        theServer->keyTableMods[kchrCharacters & 0xFFFF] = modifierKeyState;
                    }
                }
            }
        }
    }
    else {
        // This is the old US only keyboard mapping
        // Map the above key table into a static array so we can just look them up directly
        NSLog(@"Unable To Determine Key Map - Reverting to US Mapping\n");
        for (i = 0; i < (sizeof(USKeyCodes) / sizeof(int)); i += 2)
            theServer->keyTable[(unsigned short)USKeyCodes[i]] = (CGKeyCode) USKeyCodes[i+1];
    }

    // This is the old SpecialKeyCodes keyboard mapping
    // Map the above key table into a static array so we can just look them up directly
    NSLog(@"Loading %d XKeysym Special Keys\n", (sizeof(SpecialKeyCodes) / sizeof(int))/2);
    for (i = 0; i < (sizeof(SpecialKeyCodes) / sizeof(int)); i += 2) {
        theServer->keyTable[(unsigned short)SpecialKeyCodes[i]] = (CGKeyCode) SpecialKeyCodes[i+1];
	}
}

+ (void) rfbRunning {	
	[JaguarExtensions registerRendezvous];
	if (useIP6) {
		[NSThread detachNewThreadSelector:@selector(setupIPv6:) toTarget:self withObject:nil];
		// Wait for the IP6 to bind, if it binds later it confuses the IPv4 binding into allowing others on the port
		while (!listenerFinished)
			usleep(1000); 
	}
}

+ (void) setupIPv6: argument {
    int listen_fd6=0, client_fd=0;
	int value=1;  // Need to pass a ptr to this
	struct sockaddr_in6 sin6, peer6;
	unsigned int len6=sizeof(sin6);
	
	bzero(&sin6, sizeof(sin6));
	sin6.sin6_len = sizeof(sin6);
	sin6.sin6_family = AF_INET6;
	sin6.sin6_port = htons(theServer->rfbPort);
	if (theServer->rfbLocalhostOnly)
		sin6.sin6_addr = in6addr_loopback;
	else
		sin6.sin6_addr = in6addr_any;
	
	if ((listen_fd6 = socket(PF_INET6, SOCK_STREAM, 0)) < 0) {
		NSLog(@"IPv6: Unable to open socket");
	}
	/*
	    else if (fcntl(listen_fd6, F_SETFL, O_NONBLOCK) < 0) {
			NSLog(@"IPv6: fcntl O_NONBLOCK failed\n");
		}
	 */
	else if (setsockopt(listen_fd6, SOL_SOCKET, SO_REUSEADDR, &value, sizeof(value)) < 0) {
		NSLog(@"IPv6: setsockopt SO_REUSEADDR failed\n");
	}
	else if (bind(listen_fd6, (struct sockaddr *) &sin6, len6) < 0) {
		NSLog(@"IPv6: Failed to Bind Socket: Port %d may be in use by another VNC\n", theServer->rfbPort);
	}
	else if (listen(listen_fd6, 5) < 0) {
		NSLog(@"IPv6: Listen failed\n");
	}
	else {
		NSLog(@"IPv6: Started Listener Thread on port %d\n", theServer->rfbPort);
		listenerFinished = TRUE;
		
	    while ((client_fd = accept(listen_fd6, (struct sockaddr *) &peer6, &len6)) !=-1) {
			NSAutoreleasePool *pool=[[NSAutoreleasePool alloc] init];
			
			[[NSNotificationCenter defaultCenter] postNotification:
				[NSNotification notificationWithName:@"NewRFBClient" object:[NSNumber numberWithInt:client_fd]]];
			
			// We have to trigger a signal so the event loop will pickup (if no clients are connected)
			// Turning this off for Dimdim -- Bharat Varma
//			pthread_cond_signal(&(theServer->listenerGotNewClient));
			
			[pool release];
		}
		
		NSLog(@"IPv6: Accept failed %d\n", errno);
	}
	listenerFinished = TRUE;
	
	return;
}

+ (void) registerRendezvous {
	BOOL loadRendezvousVNC = NO;
	BOOL loadRendezvousRFB = YES;
	int argumentIndex = [[[NSProcessInfo processInfo] arguments] indexOfObject:@"-rendezvous"];
	RendezvousDelegate *rendezvousDelegate = [[RendezvousDelegate alloc] init];
	
    if (argumentIndex == NSNotFound) {
		argumentIndex = [[[NSProcessInfo processInfo] arguments] indexOfObject:@"-bonjour"];
	}
	
    if (argumentIndex != NSNotFound) {
        NSString *value = nil;
        
        if ([[[NSProcessInfo processInfo] arguments] count] > argumentIndex + 1)
            value = [[[NSProcessInfo processInfo] arguments] objectAtIndex:argumentIndex+1];
        
        if ([value hasPrefix:@"n"] || [value hasPrefix:@"N"] || [value hasPrefix:@"0"]) {
            loadRendezvousVNC = NO; loadRendezvousRFB = NO;
		}
		else if ([value hasPrefix:@"y"] || [value hasPrefix:@"Y"] || [value hasPrefix:@"1"] || [value hasPrefix:@"rfb"]) {
			loadRendezvousVNC = NO; loadRendezvousRFB = YES;
		}
		else if ([value hasPrefix:@"b"] || [value hasPrefix:@"B"] || [value hasPrefix:@"2"]) {
			loadRendezvousVNC = YES; loadRendezvousRFB = YES; 
		}
		else if ([value hasPrefix:@"vnc"]) {
			loadRendezvousVNC = YES; loadRendezvousRFB = NO;
		}
    }
	
	// Register For Rendezvous
    if (loadRendezvousRFB) {
		rfbService = [[NSNetService alloc] initWithDomain:@""
												   type:@"_rfb._tcp." 
												   name:[NSString stringWithCString:theServer->desktopName]
												   port:(int) theServer->rfbPort];
		[rfbService setDelegate:rendezvousDelegate];		
		[rfbService publish];
	}
//	else
//		NSLog(@"Bonjour (_rfb._tcp) - Disabled");

	if (loadRendezvousVNC) {
		vncService = [[NSNetService alloc] initWithDomain:@""
												  type:@"_vnc._tcp." 
												  name:[NSString stringWithCString:theServer->desktopName]
												  port:(int) theServer->rfbPort];
		[vncService setDelegate:rendezvousDelegate];		
		
		[vncService publish];
	}
//	else
//		NSLog(@"Bonjour (_vnc._tcp) - Disabled");
}

+ (void) rfbPoll {
    // Check if keyboardLayoutRef has changed
    if (keyboardLoading) {
        KeyboardLayoutRef currentKeyboardLayoutRef;
		
        if (KLGetCurrentKeyboardLayout(&currentKeyboardLayoutRef) == noErr) {
            if (currentKeyboardLayoutRef != loadedKeyboardRef) {
                loadedKeyboardRef = currentKeyboardLayoutRef;
				[self loadKeyboard: loadedKeyboardRef];
            }
        }
    }
}

+ (void) rfbReceivedClientMessage {
    return;
}

+ (void) rfbShutdown {
    NSLog(@"Unloading Jaguar Extensions");
	[rfbService stop];
	[vncService stop];
}

@end

@implementation RendezvousDelegate

// Sent when the service is about to publish

- (void)netServiceWillPublish:(NSNetService *)netService {
	NSLog(@"Registering Bonjour Service(%@) - %@", [netService type], [netService name]);
}

// Sent if publication fails
- (void)netService:(NSNetService *)netService didNotPublish:(NSDictionary *)errorDict {
    NSLog(@"An error occurred with service %@.%@.%@, error code = %@",		  
		  [netService name], [netService type], [netService domain], [errorDict objectForKey:NSNetServicesErrorCode]);
}

// Sent when the service stops
- (void)netServiceDidStop:(NSNetService *)netService {	
	NSLog(@"Disabling Bonjour Service - %@", [netService name]);
    // You may want to do something here, such as updating a user interfac
}


@end

#include <netdb.h>

@implementation NSProcessInfo (VNCExtension)

- (CGDirectDisplayID) CGMainDisplayID {
	return CGMainDisplayID();
}

- (struct hostent *) getHostByName:(char *) host {
	return gethostbyname(host);
}

@end

