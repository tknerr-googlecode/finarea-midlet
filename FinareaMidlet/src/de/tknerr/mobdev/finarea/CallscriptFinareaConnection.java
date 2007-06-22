/**
 * 
 */
package de.tknerr.mobdev.finarea;



/**
 * class abstracting a finarea service. all methods throw a 
 * FinareaException if something goes wrong.
 * @author Torben
 *
 */
public class CallscriptFinareaConnection implements FinareaConnection {
	private HttpUtil http = null;
	private boolean loggedIn = false;
	private String callscriptUrl = null;
	private String user = null;
	private String pass = null;
	private Account account = null;
	private String smsscriptUrl = null;
	

	
	public CallscriptFinareaConnection(HttpUtil http, Account account, String callscriptUrl, String smsscriptUrl) throws FinareaException {
		if (http==null)
			throw new FinareaException("http util is null!");
		if (account==null)
			throw new FinareaException("Account is null!");
		if (callscriptUrl == null || smsscriptUrl == null)
			throw new FinareaException("PHP callscript url or sms script url is null!");
		this.http = http;
		this.account  = account;
		this.callscriptUrl = callscriptUrl;
		this.smsscriptUrl  = smsscriptUrl;
	}
	
	/**
	 * logs the user in to the current service
	 * @throws FinareaException
	 */
	public synchronized void login() throws FinareaException {
		Debug.log("logging in...");
		if (loggedIn) throw new FinareaException("already logged in!");
		user = account.getUsername().trim();
		pass = account.getPassword().trim();
		if (user == null || pass == null || user.length()<1 || pass.length()<1)
			throw new FinareaException("Missing login information, user or pass is empty!");
		loggedIn = true;
		Debug.log("username & password set!");
	}
	
	/**
	 * log out from current service
	 * @throws FinareaException
	 */
	public synchronized void logout() throws FinareaException {
		Debug.log("logging out...");
		if (!loggedIn) throw new FinareaException("Can not log out, because not logged in");
		user = null;
		pass = null;
		loggedIn = false;
		Debug.log("logout successful!");
	}
		
	/**
	 * makes a call, costs apply to some destinations. requires login.
	 * @param fromNumber
	 * @param toNumber
	 * @throws FinareaException
	 */
	public synchronized void makeCall(String fromNumber, String toNumber) throws FinareaException {
		if (!callscriptUrl.startsWith("http") || !callscriptUrl.endsWith(".php"))
			throw new FinareaException("Wrong format of php call script url!");
		if (fromNumber.startsWith("+"))
			fromNumber = "00"+fromNumber.substring(1);
		if (toNumber.startsWith("+"))
			toNumber = "00"+toNumber.substring(1);
		Debug.log("initiating call to " + toNumber + "...");
		if (!loggedIn) throw new FinareaException("login required for making a call!");
		String resp = null;
		try {
			resp = http.sendHttpGet(callscriptUrl + "?" + 
					"provider=" + account.getProvider() + "&" +
					"us=" + HttpUtil.urlEncode(user) + "&" +
					"ps=" + HttpUtil.urlEncode(pass) + "&" +
					"tela="+ HttpUtil.urlEncode(fromNumber) +"&" +
					"telb=" + HttpUtil.urlEncode(toNumber), false);
		} catch (Exception e) {
			throw new FinareaException(e.getMessage());
		}
		resp = resp.substring(resp.indexOf("<body>")+6, resp.indexOf("</body>"));
		Debug.log("call status: " + resp);
		Debug.log("call initiated");
	}


	/**
	 * sends an sms via finarea service. login required.
	 * @param fromNumber
	 * @param toNumber
	 * @param text
	 * @throws FinareaException
	 */
	public synchronized void sendSms(String toNumber[], String text) throws FinareaException {
		if (!smsscriptUrl.startsWith("http") || !smsscriptUrl.endsWith(".php"))
			throw new FinareaException("Wrong format of php sms script url!");
		Debug.log("sending sms to " + toNumber[0] + "...");
		if (!loggedIn) throw new FinareaException("login required for making a call!");
		if (text.length() > 160) throw new FinareaException("message text too long!");
		//TODO: multiple recipients support (comma-separated list of numbers)
		if (toNumber.length > 1) throw new FinareaException("currently only one number supported!");
		if (toNumber[0].startsWith("+"))
			toNumber[0] = "00"+toNumber[0].substring(1);
		String resp = null;
		try {
			//TODO: resolve timezone / server-client time difference issues
			resp = http.sendHttpGet(smsscriptUrl + "?" + 
					"provider="+ account.getProvider() +"&" +
					"disp=0&" + 
					"us=" + HttpUtil.urlEncode(user) +"&" +
					"ps=" + HttpUtil.urlEncode(pass) + "&" +
					//"from=FROMNUMBER&" TODO: requires registration of from number
					"tel=" + HttpUtil.urlEncode(toNumber[0]) + "&" +
					"m=" + HttpUtil.urlEncode(text)
					, false);
		} catch (Exception e) {
			throw new FinareaException(e.getMessage());
		}
//		System.out.println(resp);
		resp = resp.substring(resp.indexOf("<body>")+6, resp.indexOf("</body>"));
		Debug.log("sms status: " + resp);
		Debug.log("sms initiated");
	}
}