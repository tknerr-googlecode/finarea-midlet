# What is it about? #

Finarea/Betamax is a VOIP service provider offering cheap calling rates by various services, e.g. sipdiscount.com, freecall.com, voipbuster.com etc...

The finarea-midlet emulates the actions neccessary to initiate a call using the web interface of these services by sending the same HTTP requests as the browser does if done manually. This way you can have ultra-cheap (or even free) calls from your mobile!
Here is what happens:

  1. start the finarea-midlet
  1. choose the contact you want to call
  1. select one of the [services you configured](http://code.google.com/p/finarea-midlet/wiki/Instructions), e.g. freecall.com
  1. finarea-midlet opens a HTTP connection (additional costs may apply depending on your data plan!), logs in your freecall.com account and initiates the call
  1. a few seconds later you will receive the callback. Once you accept the call you will be
connected to the other party.

# How to get started? #

  1. get a freecall.com account (it's absolutely free, but you need to download their software in order to register). With freecall.com you don't need to pay anything, you can start making free calls using their software or their web interface right now!
  1. download and install FinareMidlet
  1. [configure it](http://code.google.com/p/finarea-midlet/wiki/Instructions) by adding your callback number and your Finarea/Betamax accounts
  1. make free calls from your mobile!


# Requirements #

You need a J2ME enabled phone, optionally with the JSR 75 PIM (Personal Information Management) API in order to access your contacts. Since version 0.2 you can also download a non-JSR75 version of the FinareaMidlet. Also a data plan would be a good idea because the finarea-midlet will connect to a web server in order to initate a cheap/free call.

# Links #

  * how to set up and configure the midlet for different Finarea/Betamax services? Check out the [instructions in the wiki](http://code.google.com/p/finarea-midlet/wiki/Instructions)
  * Comparison (calling rates updated daily) of the various Finarea/Betamax services: http://backsla.sh/betamax

# Known Limitations and Bugs #

  * it has been reported that v0.1 did not work with some Sony-Ericsson phones as it crashed during the start of the application when the contacts are loaded. Unfortunately I have no SE device to test with. Please try the non-JSR75 version of the FinareaMidlet instead.
  * the finarea-midlet sometimes says "you will be called back" but you will not be called back due to some error message or bad call status which could not be caught by finarea-midlet (those who are pushed to the client via AJAX). If you believe that the call should succeed, please test it on the web interface of your Finarea service first before reporting a bug.
  * registration of new users can not be done with Finarea Midlet, you need to download the phone software of the Finarea service in order to register. It seems that the registration is done via a non-HTTP protocol and is therefore hard to implement in finarea-midlet.

# Changelog #

  * **v0.2**
    * Finarea/Betamax accounts are now managed and stored within the application, no manual editing of the .jad file anymore
    * enter and call arbitrary numbers
    * added non-JSR75 version to support more devices
  * **v0.1**
    * initial version
    * call numbers from your phone's contact list
    * either direct connection to service or usage of PHP callscript possible