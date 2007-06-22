/**
 * 
 */
package de.tknerr.mobdev.finarea;

/**
 * @author Torben
 *
 */
public interface FinareaConnection {
	
	/**
	 * logs the user in to the current service
	 * @throws FinareaException
	 */
	public void login() throws FinareaException;
	
	/**
	 * log out from current service
	 * @throws FinareaException
	 */
	public void logout() throws FinareaException;
	
	/**
	 * makes a call, costs apply to some destinations. requires login.
	 * @param fromNumber
	 * @param toNumber
	 * @throws FinareaException
	 */
	public void makeCall(String fromNumber, String toNumber) throws FinareaException;

	/**
	 * sends an sms via finarea service. login required.
	 * @param fromNumber
	 * @param toNumber
	 * @param text
	 * @throws FinareaException
	 */
	public void sendSms(String toNumber[], String text) throws FinareaException;

	//TODO: remaining credit
	//public int getRemainingCredit() throws FinareaException;
}
