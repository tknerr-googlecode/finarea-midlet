/**
 * 
 */
package de.tknerr.mobdev.finarea;

/**
 * @author Torben
 *
 */
public class FreeCaller {
	
	private HttpUtil http;

	public FreeCaller(HttpUtil http) {
		this.http = http;
	}
	
	/**
	 * makes a free web-call using freecall.com. no login is needed.
	 * //TODO: support other free/trial webcall services, e.g. voipbuster etc...
	 * @param fromNumber
	 * @param toNumber
	 * @throws FinareaException
	 */
	public void makeFreeTrialCall(String fromNumber, String toNumber) throws FinareaException {
		Debug.log("initiating free webcall to "+ toNumber +"...");
		String resp = null;
		try {
			resp = http.sendHttpPost("http://www.freecall.com/en/callpanel.php", 
					"action=initcall&" +
					"anrphonenr="+ HttpUtil.urlEncode(fromNumber) +"&" +
					"bnrphonenr=" + HttpUtil.urlEncode(toNumber), false);
		} catch (Exception e) {
			throw new FinareaException(e.getMessage());
		}
//		System.out.println(resp);
		String errMarker = "<span style=\"color: #FF0000; font-size: 10px;font-family: Arial;\">";
		int idx = resp.indexOf(errMarker, 0) + errMarker.length();
		if (idx > errMarker.length()) 
			throw new FinareaException("Free Webcall error: " + resp.substring(idx, resp.indexOf('<', idx)));
		Debug.log("free webcall initiated successfully!");
	}
}
