# The main menu #

The main menu of the finarea-midlet application consists of the following menu entries:

  * **find contact...**: allows you to jump to the contact starting with the letters you enter
  * **call...**: initiates a call to the selected contact
  * **sms...**: allows you to send an sms to the selected contact
  * **enter number...**: allows you to enter an arbitray number to call (instead of selecting a contact)
  * **settings**: configuration settings, see below
  * **accounts**: here you manage your finarea/betamax accounts
  * **debug box**: shows some debug output

# Configuring the Connection and Phone Number #

When you first start the midlet you should go to the settings dialog and configure how you want to connect to the Finarea services and the number to which you want to receive the call (most likely the number of your mobile):

  * **Connection**
    * **direct**: the midlet logs in to the service (e.g. sipdiscount.com) directly <sup>[note 2]</sup>
    * **php scripts**: the midlet uses the php scripts below to initiate a call <sup>[note 3]</sup>
  * **php callscript URL**
    * URL of the PHP script for initiating a call, e.g. https://example.com/call.php (HTTPS recommended)
  * **php smsscript URL**
    * URL of the PHP script for sending sms, e.g. https://example.com/sms.php (HTTPS recommended)
  * **Callback number**
    * the number you want to be called back after the call has been initiated

# Configuring your Finarea/Betamax Services #

Since version 0.2 no manual editing of the jad file is needed anymore. Instead, after starting up the application, select "accounts" and then "Add account" and provide the following details:

  * **display name**: the display name of the service
  * **serive url**: the url of the finarea service after you log in on their web interface.  it always starts with, e.g. "myaccount.sipdiscount.com" (for sipdiscount.com) or "myaccount.voiparound.com" (for freecall.com) <sup>[note 1]</sup>
  * **username**: your username
  * **password**: your password

# Notes #

<sup>[note 1]</sup>: for freecall.com you have to use **voiparound.com** for the service url field (if you are logged in to the freecall web interface you can see that the url is https://myaccount.voiparound.com/).

<sup>[note 2]</sup>: this does not require to set up a php script as it uses the service directly. However, it (a) creates more network traffic (multiple HTTPS requests are necessary in order to log in and initiate a call) and (b) it does not work on some nokia handsets (the  reason is that sipdiscount.com et al respond with two Set-Cookie headers of which only the  first one is valid, but my N6630 always picks the second one)

<sup>[note 3]</sup>: using this setting the midlet calls the [betamax callserver scripts](http://www.aldeaglobal.net/callserver/) instead of using the service directly. It (a) creates much less traffic (only one HTTP request) and (b) is the only solution if the direct connection does not work. I don't recommend to use the scripts located at http://www.aldeaglobal.net/callserver/call.php and http://www.aldeaglobal.net/callserver/sms.php, better set up your own php server with HTTPS enabled in order to not leave traces of your username/password in the various server log files...