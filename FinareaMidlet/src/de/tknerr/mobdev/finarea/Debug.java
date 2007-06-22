/**
 * 
 */
package de.tknerr.mobdev.finarea;

import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

/**
 * @author Torben
 *
 */
public class Debug {
	private static boolean enabled = false;
	private static int TEXTBOX_SIZE = 10000;
	private static final TextBox debugBox = new TextBox("DEBUG", "", TEXTBOX_SIZE, TextField.ANY | TextField.UNEDITABLE);
	
	//hide
	private Debug() {}
	
	/**
	 * enables logging
	 * @param enabled
	 */
	public static void setEnabled(boolean enabled) {
		if (debugBox.getMaxSize() < TEXTBOX_SIZE) TEXTBOX_SIZE = debugBox.getMaxSize();
		debugBox.setMaxSize(TEXTBOX_SIZE);
		Debug.enabled = enabled;
		log("set debug box size to " + TEXTBOX_SIZE + " (max "+debugBox.getMaxSize()+")");
	}
	
	/**
	 * true if debug logging is enabled
	 * @return
	 */
	public static boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * get the TextBox with the log
	 * @return
	 */
	public static TextBox getDebugBox() {
		return debugBox;
	}
	
	/**
	 * clears the debug textbox 
	 */
	public static void clear() {
		debugBox.setString("");
	}
	
	/**
	 * write msg to debug box + sysout if debug is enabled
	 * @param msg
	 */
	public static void log(String msg) {
		if (enabled) {
			System.out.println(msg);
			String prev = debugBox.getString();
			try { 
				debugBox.setString(msg + "\n" + prev);
			} catch (IllegalArgumentException e) {
				//max capacity overflow
				while (prev.length() + msg.length() + "\n".length() > TEXTBOX_SIZE) {
					prev = prev.substring(0, prev.lastIndexOf('\n'));
				}
				debugBox.setString(msg + "\n" + prev);
			}
		}
	}
}
