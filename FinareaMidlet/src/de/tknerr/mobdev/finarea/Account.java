/**
 * 
 */
package de.tknerr.mobdev.finarea;

/**
 * @author Torben
 *
 */
public class Account {
	private String name;
	private String provider;
	private String username;
	private String password;
	
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
}
