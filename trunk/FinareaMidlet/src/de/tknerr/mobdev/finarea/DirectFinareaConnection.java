/**
 * 
 */
package de.tknerr.mobdev.finarea;

import java.util.Calendar;


/**
 * class abstracting a finarea service. all methods throw a 
 * FinareaException if something goes wrong.
 * @author Torben
 *
 */
public class DirectFinareaConnection implements FinareaConnection {
	private HttpUtil http = null;
	private boolean loggedIn = false;
	private Account account = null;
	
	public DirectFinareaConnection(HttpUtil http, Account account) throws FinareaException {
		if (http==null)
			throw new FinareaException("http util is null!");
		if (account==null)
			throw new FinareaException("Account is null!");
		this.http = http;
		this.account  = account;
	}
	
	/**
	 * logs the user in to the current service
	 * @throws FinareaException
	 */
	public synchronized void login() throws FinareaException {
		Debug.log("logging in...");
		if (loggedIn) throw new FinareaException("already logged in!");
		String url = "https://"+ account.getProvider() +"/clx/index.php";
		String params = "?part=plogin&" +
				"username="+HttpUtil.urlEncode(jsSimpleMask(account.getUsername()))+"&" +
				"password="+HttpUtil.urlEncode(jsSimpleMask(account.getPassword()));
		String resp = null;
		try {
			resp = http.sendHttpGet(url + params, true);
		} catch (Exception e) {
			throw new FinareaException(e.getMessage());
		}
//		System.out.println("\n### login response1\n" + resp + "\n###\n");
		String errMarker = "Username and/or password incorrect";
		if (resp.indexOf(errMarker) > 0) { 
			http.clearCookies(account.getProvider());
			throw new FinareaException("Username and/or password incorrect");
		}

		try {
			resp = http.sendHttpGet(url + "?part=menu&justloggedin=true", true);
		} catch (Exception e) {
			throw new FinareaException(e.getMessage());
		}
		//hack for nokia handsets which do not receive multiple set-cookies
		//have to add it manually
		String[] cooks = (String[]) http.cookies.get(account.getProvider());
		Debug.log("cooks = "  + cooks);
		boolean found = false;
		if (cooks != null) {
			for(int i=0; i<cooks.length; i++) {
				Debug.log("Found cookie: " + cooks[i]);
				if (cooks[i].startsWith("voipusername=")) {
					found = true;
					break;
				}
			}
		}
		if (!found) {
			Debug.log("added manually: voipusername=" + account.getUsername());
			cooks = http.growArray(cooks, 1);
			cooks[cooks.length-1] = "voipusername="+account.getUsername();
			http.cookies.put(account.getProvider(), cooks);
		}

		//TODO: it always says session lost here, but in fact it is not!!
//		System.out.println("\n### login response2\n" + resp + "\n###\n");
//		errMarker = "Username and/or password incorrect";
//		if (resp.indexOf(errMarker) > 0) { 
//			http.clearCookie(service +".com");
//			throw new FinareaException("Username and/or password incorrect");
//		}
		
		loggedIn = true;
		Debug.log("login successful!");
	}
	
	/**
	 * log out from current service
	 * @throws FinareaException
	 */
	public synchronized void logout() throws FinareaException {
		Debug.log("logging out...");
		if (!loggedIn) throw new FinareaException("Can not log out, because not logged in");
		try {
			String resp = http.sendHttpGet("https://" + account.getProvider() + "/clx/index.php?part=logoff", true);
//			System.out.println("\n### logout response\n" + resp + "\n###\n");
		} catch (Exception e) {
			throw new FinareaException(e.getMessage());
		}
		loggedIn = false;
		http.clearCookies(account.getProvider());
		Debug.log("logout successful!");
	}
	
	
	/**
	 * makes a call, costs apply to some destinations. requires login.
	 * @param fromNumber
	 * @param toNumber
	 * @throws FinareaException
	 */
	public synchronized void makeCall(String fromNumber, String toNumber) throws FinareaException {
		Debug.log("initiating call to " + toNumber + "...");
		if (!loggedIn) throw new FinareaException("login required for making a call!");
		String resp = null;
		try {
			resp = http.sendHttpPost("https://" + account.getProvider() + "/clx/webcalls2.php", 
					"action=initcall&" +
					"panel=&" +
					"anrphonenr="+ HttpUtil.urlEncode(fromNumber) +"&" +
					"bnrphonenr=" + HttpUtil.urlEncode(toNumber), true);
		} catch (Exception e) {
			throw new FinareaException(e.getMessage());
		}
		//note: can't get all error messages here because they are pushed by ajax
//		System.out.println("\n### make call response\n" + resp + "\n###\n");
		String errMarker = "<body>Session lost<br>";
		if (resp.indexOf(errMarker) > 0) {
			http.clearCookies(account.getProvider());
			throw new FinareaException("Call error: Session lost");
		}
		Debug.log("call initiated successfully");
	}


	/**
	 * sends an sms via finarea service. login required.
	 * @param fromNumber
	 * @param toNumber
	 * @param text
	 * @throws FinareaException
	 */
	public synchronized void sendSms(String toNumber[], String text) throws FinareaException {
		Debug.log("sending sms to " + toNumber[0] + "...");
		if (!loggedIn) throw new FinareaException("login required for making a call!");
		if (text.length() > 160) throw new FinareaException("message text too long!");
		//TODO: multiple recipients support (comma-separated list of numbers)
		if (toNumber.length > 1) throw new FinareaException("currently only one number supported!");
		String resp = null;
		try {
			//TODO: resolve timezone / server-client time difference issues
			Calendar today = Calendar.getInstance();
			resp = http.sendHttpPost("https://"+ account.getProvider() +"/clx/websms2.php", 
					"action=send&" +
					"panel=&" +
					"message=" + HttpUtil.urlEncode(text) + "&" +
					"bnrphonenumber=" + HttpUtil.urlEncode(toNumber[0]) + "&" +
					"day=" + HttpUtil.urlEncode(prependZero(today.get(Calendar.DAY_OF_MONTH))) + "&" +
					"month=" + HttpUtil.urlEncode(prependZero(today.get(Calendar.MONTH)+1)) + "&" +
					"hour=" + HttpUtil.urlEncode(prependZero(today.get(Calendar.HOUR_OF_DAY))) + "&" +
					"minute=" + HttpUtil.urlEncode(prependZero(today.get(Calendar.MINUTE))) + "&" +
					"gmt=" + HttpUtil.urlEncode(""+(today.getTimeZone().getRawOffset()/1000/60/60)) 
					, true);
		} catch (Exception e) {
			throw new FinareaException(e.getMessage());
		}
//		System.out.println(resp);
		String errMarker = "<span style=\"color: #FF0000; font-size: 9px; width: 300px;\">";
		int idx = resp.indexOf(errMarker, 0) + errMarker.length();
		if (idx > errMarker.length()) 
			throw new FinareaException("SMS error: " + resp.substring(idx, resp.indexOf('<', idx)));
		Debug.log("sms sent successfully");
	}
	
	
	/**
	 * prepends a '0' to numbers smaller than 10
	 * @param number
	 * @return
	 */
	private String prependZero(int number) {
		return number>9 ? ""+number : "0"+number;
	}
	
	/**
	 * emulates the javascript simpleMask function used by betamax/finarea
	 * sites to encode user & password
	 * @param in
	 * @return
	 */
	private static String jsSimpleMask(String theVar) {
		int modulo = 13;
		String maskedValue = "";
		for (int i=0; i<theVar.length(); i++) 
		{
			if (theVar.charAt(i)>=65 && theVar.charAt(i)<=90) {
				int v = theVar.charAt(i);
				v = v + modulo;
				int rest = 0;
				if (v > 90) rest = v % 90;
				if (rest > 0) v = 64 + rest;					
				maskedValue = maskedValue.concat(String.valueOf((char)v));
			} 
			else if (theVar.charAt(i) >= 97 && theVar.charAt(i) <= 122) 
			{
				int v = theVar.charAt(i);
				v = v + modulo;
				int rest = 0;
				if (v > 122) rest = v % 122;
				if (rest > 0) v = 96 + rest;
				maskedValue = maskedValue+ String.valueOf((char)v);
			}
			else if (theVar.charAt(i) >= 48 && theVar.charAt(i) <= 57)
			{
				int v = theVar.charAt(i);
				v =  v + 5;
				int rest = 0;
				if (v > 57) rest = v % 57;
				if (rest > 0) v = 47 + rest;	
				maskedValue = maskedValue.concat(String.valueOf((char)v));				
			}
			else
			{
				maskedValue = maskedValue.concat(""+theVar.charAt(i));					
			}
		}
		return maskedValue;
	}
}