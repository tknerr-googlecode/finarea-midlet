/**
 * 
 */
package de.tknerr.mobdev.finarea;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.rms.RecordStoreException;

/**
 * @author Torben
 *
 */
public class Account {
	private String name;
	private String provider;
	private String username;
	private String password;
	private int recordId = -1;
	

	/**
	 * @param name
	 * @param provider
	 * @param username
	 * @param password
	 */
	public Account(String name, String provider, String username, String password) {
		this.name = name;
		this.provider = provider;
		this.username = username;
		this.password = password;
	}
	

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the url
	 */
	public String getProvider() {
		return provider;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the record id
	 */
	public int getRecordId() {
		return recordId;
	}
	
	
	
	/**
	 * reloads the values from the recordstore
	 * @throws FinareaException 
	 */
	public void loadAccount() throws FinareaException {
		if (recordId == -1) 
			throw new FinareaException("Account not stored yet, can not load!");
		try {
			Hashtable data = RecordStoreUtil.getRecord(recordId, FinareaMidlet.ACCOUNTS_RS_NAME);
			name = (String) data.get("name");
			provider = (String) data.get("provider");
			username = (String) data.get("username");
			password = (String) data.get("password");
		} catch (RecordStoreException e) {
			throw new FinareaException(e.getMessage());
		}
	}
	
	/**
	 * saves the account to the record store
	 * @throws FinareaException 
	 *
	 */
	public void saveAccount() throws FinareaException {
		Hashtable data = new Hashtable();
		data.put("name", name);
		data.put("provider", provider);
		data.put("username", username);
		data.put("password", password);
		
		try {
		if (recordId == -1) 
			recordId = RecordStoreUtil.addRecord(data, FinareaMidlet.ACCOUNTS_RS_NAME);
		else
			RecordStoreUtil.updateRecord(recordId, data, FinareaMidlet.ACCOUNTS_RS_NAME);
		} catch(RecordStoreException e) {
			throw new FinareaException(e.getMessage());
		}
	}
	
	/**
	 * deletes the account from the record store
	 * @throws FinareaException 
	 *
	 */
	public void deleteAccount() throws FinareaException {
		if (recordId != -1) {
			try {
				RecordStoreUtil.deleteRecord(recordId, FinareaMidlet.ACCOUNTS_RS_NAME);
				recordId = -1;
				name = provider = username = password = null;
			} catch (RecordStoreException e) {
				throw new FinareaException(e.getMessage());
			}
		} else {
			Debug.log("Account already deleted or does not yet exist");
		}
	}


	/**
	 * static method returning all accounts
	 * @throws FinareaException 
	 */
	public static synchronized Hashtable loadAllAccounts() throws FinareaException {		
		//reload from recordstore
		try {
			Hashtable accounts = new Hashtable();
			Hashtable allRecs = RecordStoreUtil.getAllRecords(FinareaMidlet.ACCOUNTS_RS_NAME);
			Enumeration en = allRecs.keys();
			while (en.hasMoreElements()) {
				Integer key = (Integer) en.nextElement();
				Hashtable rec = (Hashtable) allRecs.get(key);
				Account acc = new Account((String)rec.get("name"), (String)rec.get("provider"),
						(String)rec.get("username"), (String)rec.get("password"));
				acc.recordId = key.intValue();
				accounts.put((String)rec.get("name"), acc);
			}
			return accounts;
		} catch (RecordStoreException e) {
			throw new FinareaException(e.getMessage());
		}
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}


	/**
	 * @param provider the provider to set
	 */
	public void setProvider(String provider) {
		this.provider = provider;
	}


	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
}
