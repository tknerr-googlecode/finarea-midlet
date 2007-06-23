/**
 * 
 */
package de.tknerr.mobdev.finarea;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * utility class for http connections. manages cookies from different hosts,
 * but currently only one cookie per host.
 * @author Torben
 *
 */
public class HttpUtil {
	private static final String HTTP_UA = "Nokia6600/1.0 (4.03.24) SymbianOS/6.1 Series60/2.0 Profile/MIDP-2.0 Configuration/CLDC-1.0";
	private static final String HTTP_PROFILE = "nds.nokia.com/uaprof/N6600r100.xml";
	Hashtable cookies = new Hashtable();
	
	/**
	 * clears the cookie associated with host
	 * @param host
	 */
	public void clearCookies(String host) {
		if (cookies.containsKey(host)) {
			cookies.remove(host);
			Debug.log("Cookies from host " + host + " cleared...");
		} else {
			Debug.log("No cookie from host " + host + " to clear...");
		}
	}
	
	
	/**
	 * returns a String[] of http header values for a headers occuring multiple times
	 * with the same headerName
	 * @param con
	 * @param headerName
	 * @return
	 * @throws IOException 
	 */
	private String[] getHeaderFields(HttpConnection con, String headerName) throws IOException {
		String[] headers = new String[0];
		String tmp = null;
		int i = 0;
		while ((tmp = con.getHeaderField(i)) != null) {
			if (con.getHeaderFieldKey(i).equals(headerName) && !tmp.trim().equals("")) {
				Debug.log("######## header " + headerName + ": " + tmp);
				headers = growArray(headers, 1);
				headers[headers.length-1] = tmp;
			}
			i++;
		}
		return headers;
	}
	
	/**
	 * stores the cookies associated with host
	 * @param newCookie
	 */
	private void cacheCookie(String newCookie, String host) {
		if (newCookie == null || newCookie.trim().equals("")) {
			Debug.log("new cookie to be set is null or \"\"!");
		} else {
			String[] cookiesArray = cookies.get(host)!=null ? (String[]) cookies.get(host) : new String[0];
			String nameValue = (newCookie.indexOf(';')>0) ? newCookie.substring(0, newCookie.indexOf(';')).trim() : newCookie.trim();
			Debug.log("full cookie: " + newCookie);
			if (cookiesArray == null) {
				Debug.log("initial set-cookie for host: " + host + " (cookie: " + nameValue + ")");
			} else {
				for (int i=0; i<cookiesArray.length; i++) {
					//TODO: quick fix: if sipdiscoutn et al send session id twice then use the latter one
					//TODO: unfortunately the nokia phones seem to filter the second PHPSESSID before it is passed to J2ME :(
					if (cookiesArray[i].startsWith("PHPSESSID") && nameValue.startsWith("PHPSESSID")) {
						cookiesArray[i] = nameValue;
						Debug.log("replaced previous PHPSESSID header with a newer one");
						return;
					}
					if (cookiesArray[i].equals(nameValue)) {
						Debug.log("ignoring same cookie for host " + host + ": " + nameValue);
						return;
					}
				}
				Debug.log("adding new cookie for host " + host + ": " + nameValue);
			}
			cookiesArray = growArray(cookiesArray, 1);
			cookiesArray[cookiesArray.length-1] = nameValue;
			cookies.put(host, cookiesArray);
		}
	}
	
	/**
	 * grows a String[] by int grow
	 * @param src
	 * @param grow
	 * @return
	 */
	public String[] growArray(String[] src, int grow) {
		if (grow <= 0) return src;
		String[] tmp = new String[src.length + grow];
		System.arraycopy(src, 0, tmp, 0, src.length);
		return tmp;
	}
	
	/**
	 * url-encodes the given string for transmission of http get/post parameters
	 * @param s
	 * @return
	 */
	public static String urlEncode(String s)
	{
		if (s!=null) {
			StringBuffer tmp = new StringBuffer();
			int i=0;
			try {
				while (true) {
					int b = (int)s.charAt(i++);
					if ((b>=0x30 && b<=0x39) || (b>=0x41 && b<=0x5A) || (b>=0x61 && b<=0x7A)) {
						tmp.append((char)b);
					}
					else {
						tmp.append("%");
						if (b <= 0xf) tmp.append("0");
						tmp.append(Integer.toHexString(b));
					}
				}
			}
			catch (Exception e) {}
//			Debug.log("url-encoded from " + s + " to " + tmp.toString());
			return tmp.toString();
		}
		return null;
	}
	
	/**
	 * returns the host part of an url
	 * @param url
	 * @return
	 */
	public static String getHostPart(String url) throws Exception {
		int first = url.indexOf(':');
		if (first < 0) throw new Exception("malformed url: " + url);
		int last = url.indexOf('/', first+3);
		String host = url.substring(first+3, (last<0) ? url.length() : last);
//		Debug.log("url = " + url + ", host = " + host);
		return host;
	}
	
	/**
	 * issues a http get request to the given url (including url params). 
	 * if useCookie is true th ecookie for that host will be tracked and reused,
	 * otherwise this http get call is 'stateless'
	 * @param url
	 * @param useCookie
	 * @return
	 * @throws Exception
	 */
	public String sendHttpGet(String url, boolean useCookie) throws Exception {
		
		String host = getHostPart(url);
		String[] cookiesArray = cookies.get(host)!=null ? (String[]) cookies.get(host) : null;
		
		if (useCookie && cookiesArray == null) {
			Debug.log("No cookies set for host "+ host +", yet.");
		}
		
		HttpConnection con = null;
		DataInputStream din = null;
		StringBuffer responseBuffer = new StringBuffer();

		try {
			con = (HttpConnection) Connector.open(url);
			//set headers
			con.setRequestProperty("User-Agent", HTTP_UA);
			con.setRequestProperty("Profile", HTTP_PROFILE);
			
			if (useCookie && cookiesArray != null) { 
				String cookieString = "";
				for (int i=0; i<cookiesArray.length; i++) {
					cookieString += cookiesArray[i] + ((i==cookiesArray.length-1) ? "" : "; ");
				}
				Debug.log("setting cookie header: " + cookieString);
				con.setRequestProperty("Cookie", cookieString);
			}
			
			//read response
			din = new DataInputStream(con.openInputStream());
			if (con.getResponseCode() == 200) {
				int ch;
				while ((ch = din.read()) != -1) {
					responseBuffer.append((char) ch);
				}
				if (useCookie) {
					String[] tmp = getHeaderFields(con, "set-cookie");
					for (int i=0; i<tmp.length; i++)
						cacheCookie(tmp[i], host);
				}
				return responseBuffer.toString();
			}
			//unexpected
			throw new Exception("unexpected response status: " + con.getResponseCode());
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		} finally {
			try {
				if (con != null)
					con.close();
				if (din != null)
					din.close();
			} catch (IOException ioe) {
			}
		}
	}

	
	public String sendHttpPost(String url, String params, boolean useCookie) throws Exception {
		
		String host = getHostPart(url);
		String[] cookiesArray = cookies.get(host)!=null ? (String[]) cookies.get(host) : null;
		
		if (useCookie && cookiesArray == null) {
			Debug.log("Cookie not set, yet.");
		}
		
		HttpConnection con = null;
		DataInputStream din = null;
		DataOutputStream dout = null;
		StringBuffer responseBuffer = new StringBuffer();

		try {
			con = (HttpConnection) Connector.open(url, Connector.READ_WRITE);

			//set headers
			con.setRequestProperty("User-Agent", HTTP_UA);
			con.setRequestProperty("Profile", HTTP_PROFILE);
			con.setRequestProperty("Host", host);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("Content-Length", "" + params.length());
			if (useCookie && cookiesArray != null) { 
				String cookieString = "";
				for (int i=0; i<cookiesArray.length; i++) {
					cookieString += cookiesArray[i] + ((i==cookiesArray.length-1) ? "" : "; ");
				}
				Debug.log("setting cookie header: " + cookieString);
				con.setRequestProperty("Cookie", cookieString);
			}
			con.setRequestMethod(HttpConnection.POST);
			Debug.log("POST params: " + params);
			
			//send request
			dout = con.openDataOutputStream();
			byte[] request_body = params.getBytes();
			for (int i = 0; i < request_body.length; i++) {
				dout.writeByte(request_body[i]);
			}

			//read response
			din = new DataInputStream(con.openInputStream());
			if (con.getResponseCode() == 200) {
				int ch;
				while ((ch = din.read()) != -1) {
					responseBuffer.append((char) ch);
				}
				if (useCookie) {
					String[] tmp = getHeaderFields(con, "set-cookie");
					for (int i=0; i<tmp.length; i++)
						cacheCookie(tmp[i], host);
				}
				return responseBuffer.toString();
			} 
			//unexpected
			throw new Exception("unexpected response status: " + con.getResponseCode());
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		} finally {
			try {
				if (con != null)
					con.close();
				if (din != null)
					din.close();
				if (dout != null)
					dout.close();
			} catch (IOException ioe) {
			}
		}
	}
}