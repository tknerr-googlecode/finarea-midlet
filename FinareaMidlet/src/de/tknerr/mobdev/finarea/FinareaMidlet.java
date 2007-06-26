package de.tknerr.mobdev.finarea;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
//TODO: comment for non-JSR75 mobiles
//#######
import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
//#######
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 * @author Torben
 */
public class FinareaMidlet extends MIDlet implements CommandListener {

	
	private final Display display = Display.getDisplay(this);
	
	public final static String SETTINGS_RS_NAME = "finarea_settings_rs";
	public final static String ACCOUNTS_RS_NAME = "finarea_accounts_rs";
	
	private boolean useCallscript = false;
	
	private final Command cmdExit = new Command("exit", Command.EXIT, 1);
	private final Command cmdSave = new Command("save!", Command.SCREEN, 1);
	private final Command cmdSend = new Command("send SMS", Command.SCREEN, 1);
//	private final Command cmdAddMore = new Command("add more recipients", Command.SCREEN, 1);
	private final Command cmdGoto = new Command("find contact...", Command.ITEM, 1);
	private final Command cmdBack = new Command("back", Command.BACK, 1);
	private final Command cmdCall = new Command("call...", Command.ITEM, 1);
	private final Command cmdFreeCall = new Command("free webcall (frecall)", Command.ITEM, 1);
	private final Command cmdSms = new Command("sms...", Command.ITEM, 1);
	private final Command cmdSettings = new Command("settings", Command.OK, 1);
	private final Command cmdAccounts = new Command("accounts", Command.OK, 1);
	private final Command cmdFind = new Command("find contact", Command.OK, 1);
	private final Command cmdEditAccount = new Command("edit account", Command.OK, 1);
	private final Command cmdDeleteAccount = new Command("delete account", Command.OK, 1);
	private final Command cmdAddAccount = new Command("add account...", Command.OK, 1);
	private final Command cmdEnterNumber = new Command("enter number...", Command.OK, 1);
	
	private ContactDetails[] contactsDetails = null;
	private HttpUtil httputil = null;
	private Hashtable accounts = new Hashtable();
	
	private List contactList = null;
	private final List accountsList = new List("Select an account", List.IMPLICIT);
	private final List multipleNumbersList = new List("", List.IMPLICIT);
	private final Form settingsForm = new Form("Settings");
	private final TextBox numberBox = new TextBox("Enter number to call", "+49", 30, TextField.PHONENUMBER);
	private final TextBox smsBox = new TextBox("Write SMS", "", 160, TextField.ANY);
	private final TextBox progressBox = new TextBox("In Progess...", "", 100, TextField.UNEDITABLE);
	private final TextBox findBox = new TextBox("Find contact", "", 10, TextField.NON_PREDICTIVE | TextField.ANY);
	private final ChoiceGroup connectionChoice = new ChoiceGroup("Connection", Choice.EXCLUSIVE, new String[]{"direct", "php scripts"}, null);
	private final TextField callscriptField = new TextField("php callscript URL", "https://", 255, TextField.URL);
	private final TextField smsscriptField = new TextField("php smsscript URL", "https://", 255, TextField.URL);
	private final TextField callbacknumberField = new TextField("Callback number", "", 20, TextField.PHONENUMBER);
	private final List manageAccountsList = new List("Manage accounts", List.IMPLICIT);
	private final Form accountDetailsForm = new Form("Account details");
	private final TextField accNameField = new TextField("Display name", "", 20, TextField.ANY);
	private final TextField accUrlField = new TextField("Service URL", "myaccount.XXX.com", 40, TextField.ANY);
	private final TextField accUsernameField = new TextField("Username", "", 30, TextField.ANY);
	private final TextField accPasswordField = new TextField("Password", "", 20, TextField.PASSWORD);
	
	
	private static int PROGRESSBOX_SIZE = 1000;
	
	private final Command cmdClearDebugBox = new Command("clear", Command.SCREEN, 1);
	private final Command cmdDebug = new Command("debug box", Command.OK, 1);
	
	private boolean busy = false;
	private String lastSelectedNumber;
	private String nextAction;
	private Account lastSelectedAccount;
	private ContactDetails lastSelectedContact;
	
	public FinareaMidlet() {
		
//TODO: comment for non-JSR75 mobiles
//#######		
		String err = null;
		
		//test for prerequisites
		try {
			Class.forName("javax.microedition.pim.Contact");
		} catch (ClassNotFoundException e) {
			err = "PIM API not present!";
			System.out.println(err);
		}
		if (err != null) {
			display.setCurrent(new Alert(err));
			notifyDestroyed();
			return;
		}
		
		//load contact list
		try {
			contactsDetails = loadContacts();
		} catch (Exception e) {
			err = "Exception while loading list: " + e.getMessage();
			e.printStackTrace();
			System.out.println(err);
		}
		if (err != null) {
			display.setCurrent(new Alert(err));
			notifyDestroyed();
			return;
		}
//#######	
		

//TODO: comment for JSR75 mobiles
//#######
//		contactsDetails = new ContactDetails[]{}; //empty list
//#######		
		
		String[] names = new String[contactsDetails.length];
		for (int i = 0; i < contactsDetails.length; i++) {
			names[i] = contactsDetails[i].name;
		}
		
		Debug.getDebugBox().addCommand(cmdClearDebugBox);
		Debug.getDebugBox().addCommand(cmdBack);
		Debug.getDebugBox().setCommandListener(this);
		Debug.setEnabled(true);
		
		numberBox.addCommand(cmdCall);
		numberBox.addCommand(cmdSms);
		numberBox.addCommand(cmdBack);
		if (Debug.isEnabled()) numberBox.addCommand(cmdDebug); 
		numberBox.setCommandListener(this);
		
		//read accounts from jad file
//		for (int i=0; i<10; i++) {
//			Account account = readAccountFromJad(i, this);
//			if (account!=null) {
//				if (!accounts.containsKey(account.getName())) {
//					accounts.put(account.getName(), account);
//					accountsList.append(account.getName(), null);
//				} else {
//					Debug.log("Duplicate entry for service " + account.getName() + ", ignoring...");				
//				}
//			}
//		}
		accountsList.addCommand(cmdBack);
		if (Debug.isEnabled()) accountsList.addCommand(cmdDebug); 
		accountsList.setCommandListener(this);
		
		manageAccountsList.addCommand(cmdBack);
		manageAccountsList.addCommand(cmdAddAccount);
		manageAccountsList.addCommand(cmdEditAccount);
		manageAccountsList.addCommand(cmdDeleteAccount);
		if (Debug.isEnabled()) manageAccountsList.addCommand(cmdDebug); 
		manageAccountsList.setCommandListener(this);
		
		//load accounts
		try {
			accounts = Account.loadAllAccounts();
			Enumeration en = accounts.keys();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				accountsList.append(key, null);
				manageAccountsList.append(key, null);
			}
		} catch (FinareaException e) {
			e.printStackTrace();
			Debug.log("Could not load accounts: " + e.getMessage());
		}
		
		accountDetailsForm.addCommand(cmdBack);
		accountDetailsForm.addCommand(cmdSave);
		accountDetailsForm.append(accNameField);
		accountDetailsForm.append(accUrlField);
		accountDetailsForm.append(accUsernameField);
		accountDetailsForm.append(accPasswordField);
		if (Debug.isEnabled()) accountDetailsForm.addCommand(cmdDebug); 
		accountDetailsForm.setCommandListener(this);
		
		contactList = new List("Contacts", List.IMPLICIT, names, null);
		if (contactsDetails.length>0)contactList.addCommand(cmdGoto);
		if (contactsDetails.length>0) contactList.addCommand(cmdCall);
		if (contactsDetails.length>0) contactList.addCommand(cmdSms);
		//if (contactsDetails.length>0) contactList.addCommand(cmdFreeCall);
		contactList.addCommand(cmdEnterNumber);
		contactList.addCommand(cmdSettings);
		contactList.addCommand(cmdAccounts);
		contactList.addCommand(cmdExit);
		if (Debug.isEnabled()) contactList.addCommand(cmdDebug); 
		contactList.setCommandListener(this);
		
		multipleNumbersList.addCommand(cmdBack);
		if (Debug.isEnabled()) multipleNumbersList.addCommand(cmdDebug);
		multipleNumbersList.setCommandListener(this);		
		
		settingsForm.addCommand(cmdSave);
		settingsForm.addCommand(cmdBack);
		settingsForm.append(connectionChoice);
		settingsForm.append(callscriptField);
		settingsForm.append(smsscriptField);
		settingsForm.append(callbacknumberField);
		if (Debug.isEnabled()) settingsForm.addCommand(cmdDebug);
		settingsForm.setCommandListener(this);
		loadSettings();
		
		smsBox.addCommand(cmdSend);
		smsBox.addCommand(cmdBack);
		if (Debug.isEnabled()) smsBox.addCommand(cmdDebug);
		smsBox.setCommandListener(this);
		
		findBox.addCommand(cmdBack);
		findBox.addCommand(cmdFind);
		findBox.setCommandListener(this);
		
		if (progressBox.getMaxSize() < PROGRESSBOX_SIZE) PROGRESSBOX_SIZE = progressBox.getMaxSize();
		progressBox.setMaxSize(PROGRESSBOX_SIZE);
		
		display.setCurrent(contactList);
		httputil = new HttpUtil();
	}

	/**
	 * @inheritDoc
	 * @see javax.microedition.midlet.MIDlet#startApp()
	 */
	protected void startApp() throws MIDletStateChangeException {
		Debug.log("FinareaMidlet.startApp()");
	}

	/**
	 * @inheritDoc
	 * @see javax.microedition.midlet.MIDlet#pauseApp()
	 */
	protected void pauseApp() {
		Debug.log("FinareaMidlet.pauseApp()");
	}

	/**
	 * @inheritDoc
	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
	 */
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		Debug.log("FinareaMidlet.destroyApp()");
	}
	
	
//	/**
//	 * reads the account from the jad file
//	 * @param number
//	 * @return
//	 */
//	private Account readAccountFromJad(int number, MIDlet midlet) {
//		String name = midlet.getAppProperty("service-" + number + "-name");
//		String provider = midlet.getAppProperty("service-" + number + "-account-provider");
//		String user = midlet.getAppProperty("service-" + number + "-account-username");
//		String pass = midlet.getAppProperty("service-" + number + "-account-password");
//		if (name!=null && provider!=null && user!=null && pass!=null) {
//			return new Account(name, provider, user, pass);
//		} else {
//			return null;
//		}
//	}
	
	
	/**
	 * @inheritDoc
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command,
	 *      javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command cmd, Displayable d) {
		if (busy) return;
		//TODO: scroll-to-contact-as-you-type
		if (cmd == cmdDebug) {
			display.setCurrent(Debug.getDebugBox());
		}
		if (d == accountsList) {
			if (cmd == List.SELECT_COMMAND) {
				lastSelectedAccount = (Account) accounts.get(accountsList.getString(accountsList.getSelectedIndex()));
				if (lastSelectedContact.numbers.length > 1) {
					showMultipleNumbersList();
				} else {
					if (nextAction.equals("doCall")) {
						try {
							startCall(callbacknumberField.getString(), lastSelectedContact.numbers[0], lastSelectedAccount);
						} catch (ArrayIndexOutOfBoundsException e) {
							display.setCurrent(new Alert("Number not given", "The selected contacted has no phone number!", null, AlertType.WARNING), contactList);
						} 
					} else if (nextAction.equals("sendSMS")) {
						try {
							lastSelectedNumber = lastSelectedContact.numbers[0];
							smsBox.setTitle("Write SMS to " + lastSelectedContact.name + "(" + lastSelectedNumber + ")");
							display.setCurrent(smsBox);
						} catch (ArrayIndexOutOfBoundsException e) {
							display.setCurrent(new Alert("Number not given", "The selected contacted has no phone number!", null, AlertType.WARNING), contactList);
						} 
					}
				}
			} else if (cmd == cmdBack) {
				display.setCurrent(contactList);
			}
		} else if (d == contactList) {
			//default: call contact 
			if (cmd == List.SELECT_COMMAND || cmd == cmdCall) {
				if (callbacknumberField.getString().trim().length() < 1) {
					display.setCurrent(new Alert("No Callback Number", "You must specify a call-back number first (see settings)!", null, AlertType.WARNING), contactList);
					return;
				}
				nextAction = "doCall";
				lastSelectedContact = contactsDetails[contactList.getSelectedIndex()];
				display.setCurrent(accountsList);
			//send SMS
			} else if (cmd == cmdSms) {
				nextAction = "sendSMS";
				lastSelectedContact = contactsDetails[contactList.getSelectedIndex()];
				display.setCurrent(accountsList);
			// free webcall service
			} else if (cmd == cmdFreeCall) {
				if (callbacknumberField.getString().trim().length() < 1) {
					display.setCurrent(new Alert("No Callback Number", "You must specify a call-back number first (see settings)!", null, AlertType.WARNING), contactList);
					return;
				}
				nextAction = "doTrialCall";
				lastSelectedContact = contactsDetails[contactList.getSelectedIndex()];
				if (lastSelectedContact.numbers.length > 1) {
					showMultipleNumbersList();
				} else {
					//call only one number for contact
					try {
						startFreeCall(callbacknumberField.getString(), lastSelectedContact.numbers[0]);
					} catch (ArrayIndexOutOfBoundsException e) {
						display.setCurrent(new Alert("Number not given", "The selected contacted has no phone number!", null, AlertType.WARNING), contactList);
					} 
				}
			//settings
			} else if (cmd == cmdEnterNumber) {
				display.setCurrent(numberBox);
			// settings
			} else if (cmd == cmdSettings) {
				display.setCurrent(settingsForm);
			// accounts
			} else if (cmd == cmdAccounts) {
				display.setCurrent(manageAccountsList);
			// goto contact beginning with ...
			} else if (cmd == cmdGoto) {
				display.setCurrent(findBox);
			// quit
			} else if (cmd == cmdExit) {
				notifyDestroyed();
			}
		} else if (d == numberBox) {
			if (cmd == cmdCall) {
				if (callbacknumberField.getString().trim().length() < 1) {
					display.setCurrent(new Alert("No Callback Number", "You must specify a call-back number first (see settings)!", null, AlertType.WARNING), contactList);
					return;
				}
				String number = numberBox.getString();
				nextAction = "doCall";
				lastSelectedContact = new ContactDetails(number, new String[]{number}, new String[]{""});
				display.setCurrent(accountsList);
			} else if (cmd == cmdSms) {
				nextAction = "sendSMS";
				String number = numberBox.getString();
				lastSelectedContact = new ContactDetails(number, new String[]{number}, new String[]{""});
				display.setCurrent(accountsList);
			} else if (cmd == cmdBack) {
				display.setCurrent(contactList);
			}
		} else if (d == multipleNumbersList) {
			if (cmd == List.SELECT_COMMAND) {
				String tmp = multipleNumbersList.getString(multipleNumbersList.getSelectedIndex());
				lastSelectedNumber = tmp.substring(0, tmp.lastIndexOf('(')-1);
				if (nextAction.equals("doCall")) {
					startCall(callbacknumberField.getString(), lastSelectedNumber, lastSelectedAccount);
				} else if (nextAction.equals("doTrialCall")) {
					startFreeCall(callbacknumberField.getString(), lastSelectedNumber);
				} else if (nextAction.equals("sendSMS")) {
					smsBox.setTitle("Write SMS to " + lastSelectedContact.name + " (" + lastSelectedNumber + ")");
					display.setCurrent(smsBox);
				}	
			} else if (cmd == cmdBack) {
				display.setCurrent(contactList);
			}
		} else if (d == manageAccountsList) {
			int idx = manageAccountsList.getSelectedIndex();
			lastSelectedAccount = idx!=-1 ? (Account) accounts.get(manageAccountsList.getString(idx)) : null;
			
			if (cmd == List.SELECT_COMMAND || cmd == cmdEditAccount) {
				if (idx == -1) {
					display.setCurrent(new Alert("No account selected", "You must select an existing account!", null, AlertType.WARNING), manageAccountsList);
					return;
				} 
				accNameField.setString(lastSelectedAccount.getName());
				accNameField.setConstraints(TextField.ANY | TextField.UNEDITABLE);
				accUrlField.setString(lastSelectedAccount.getProvider());
				accUsernameField.setString(lastSelectedAccount.getUsername());
				accPasswordField.setString(lastSelectedAccount.getPassword());
				display.setCurrent(accountDetailsForm);
			} else if (cmd == cmdAddAccount) {
				accNameField.setString("");
				accNameField.setConstraints(TextField.ANY);
				accUrlField.setString("myaccount.XXX.TLD");
				accUsernameField.setString("");
				accPasswordField.setString("");
				lastSelectedAccount = null;
				display.setCurrent(accountDetailsForm);
			} else if (cmd == cmdDeleteAccount) {
				if (idx == -1) {
					display.setCurrent(new Alert("No account selected", "You must select an existing account!", null, AlertType.WARNING), manageAccountsList);
					return;
				} 
				try {
					String oldName = lastSelectedAccount.getName(); 
					lastSelectedAccount.deleteAccount();
					accounts.remove(oldName);
					manageAccountsList.delete(idx);
					accountsList.delete(idx);
					display.setCurrent(new Alert("Delete Success!", "Successfully deleted account", null, AlertType.CONFIRMATION));
				} catch (FinareaException e) {
					display.setCurrent(new Alert("Delete Failed!", "Could not delete account: " + e.getMessage(), null, AlertType.ERROR));
				}
			} else if (cmd == cmdBack) {
				display.setCurrent(contactList);
			}
		} else if (d == accountDetailsForm) {
			if (cmd == cmdSave) {
				try {
					String name = accNameField.getString().trim();
					String provider = accUrlField.getString().trim();
					String username = accUsernameField.getString().trim();
					String password = accPasswordField.getString().trim();
					if (name.length()<1 || provider.length()<1 || username.length()<1 || password.length()<1)
						throw new FinareaException("All fields must be set");
					if (!provider.startsWith("myaccount.") || provider.endsWith("/"))
						throw new FinareaException("Malformed Service URL, must be myaccount.XXX.TLD!");
						
					if (lastSelectedAccount==null) {
						//new account
						if (accounts.containsKey(name)) 
							throw new FinareaException ("Account name already exists!");
						Account acc = new Account(name, accUrlField.getString().trim(), 
								accUsernameField.getString().trim(), 
								accPasswordField.getString().trim());
						acc.saveAccount();
						accounts.put(name, acc);
						accountsList.append(name, null);
						manageAccountsList.append(name, null);
					} else {
						//account edited
						//lastSelectedAccount.setName(name); //uneditable
						lastSelectedAccount.setProvider(accUrlField.getString().trim());
						lastSelectedAccount.setUsername(accUsernameField.getString().trim());
						lastSelectedAccount.setPassword(accPasswordField.getString().trim());
						lastSelectedAccount.saveAccount(); 
					}
					display.setCurrent(new Alert("Save Success!", "Successfully saved account", null, AlertType.CONFIRMATION), manageAccountsList);
				} catch (FinareaException e) {
					display.setCurrent(new Alert("Save Failed!", "Could not save account: " + e.getMessage(), null, AlertType.ERROR));
				}
			} else if (cmd == cmdBack) {
				display.setCurrent(manageAccountsList);
			}
		} else if (d == smsBox) {
			if (cmd == cmdSend) {
				String txt = smsBox.getString();
				if (txt.trim().length()<1) {
					display.setCurrent(new Alert("Empty Message", "Please enter a message text!", null, AlertType.WARNING), smsBox);
					return;
				}
				sendSms(lastSelectedNumber, smsBox.getString(), lastSelectedAccount);
			} else if (cmd == cmdBack) {
				display.setCurrent(contactList);
			}
		} else if (d == findBox) {
			if (cmd == cmdFind) {
				String txt = findBox.getString();
				if (txt.trim().length()<1) {
					contactList.setSelectedIndex(0, true);
				} else {
					//note: assuming alphabetical order for finding a contact
					int targetIndex = 0;
					boolean found = false;
					int matchingChars = txt.length(); 
					while (matchingChars > 0 && !found) {
						for (int i=0; i<contactList.size(); i++) {
							String name = contactList.getString(i);
							if (name!=null && name.toLowerCase().startsWith(txt.toLowerCase().substring(0, matchingChars))) {
								targetIndex = i;
								found = true;
								break;
							}	
						}
						matchingChars--;
					}
					contactList.setSelectedIndex(targetIndex, true);
				}
				display.setCurrent(contactList);
			} else if (cmd == cmdBack) {
				display.setCurrent(contactList);
			}
		} else if (d == settingsForm) {
			if (cmd == cmdSave) {
				try {
					saveSettings();
					display.setCurrent(new Alert("Save Success!", "Successfully saved settings", null, AlertType.CONFIRMATION));
				} catch (FinareaException e) {
					display.setCurrent(new Alert("Save Failed!", "Could not save settings: " + e.getMessage(), null, AlertType.ERROR));
				}
			} else if (cmd == cmdBack) {
				//reload settings when leaving the form
				loadSettings();
				display.setCurrent(contactList);
			}
		} else if (d == Debug.getDebugBox()) {
			if (cmd == cmdClearDebugBox) {
				Debug.clear();
			} else if (cmd == cmdBack) {
				display.setCurrent(contactList);
			}
		} 
	}
	
	
	/**
	 *
	 */
	private void showMultipleNumbersList() {
		//show multiple numbers for contact
		multipleNumbersList.deleteAll();
		multipleNumbersList.setTitle(lastSelectedContact.name);
		for (int i=0; i<lastSelectedContact.numbers.length; i++)
			multipleNumbersList.append(lastSelectedContact.numbers[i] + " (" + lastSelectedContact.numbersDescription[i] + ")", null);
		display.setCurrent(multipleNumbersList);
	}
	
	/**
	 * fills the progress box with new messages
	 * @param msg
	 */
	private void addProgress(String msg, boolean clear) {
		String prev = clear ? "" : progressBox.getString();
		try { 
			progressBox.setString(msg + "\n" + prev);
		} catch (IllegalArgumentException e) {
			//max capacity overflow
			while (prev.length() + msg.length() + "\n".length() > PROGRESSBOX_SIZE) {
				prev = prev.substring(0, prev.lastIndexOf('\n'));
			}
			progressBox.setString(msg + "\n" + prev);
		}
	}
	
	
	/**
	 * initiates a call (requires login)
	 * @param fromNumber
	 * @param toNumber
	 * @param serviceName
	 */
	private void startCall(final String fromNumber, final String toNumber, final Account account) {
		(new Thread() {
			public void run() {
				busy = true;
				FinareaConnection service = null;
				try {
					progressBox.setTitle("Call in progress...");
					display.setCurrent(progressBox);
					addProgress("logging in...", true);
					if (useCallscript)
						service = new CallscriptFinareaConnection(httputil, account, 
								((TextField)settingsForm.get(1)).getString(), ((TextField)settingsForm.get(2)).getString());
					else
						service = new DirectFinareaConnection(httputil, account);
					service.login();
					addProgress("login successful, initiating call...", false);
					service.makeCall(fromNumber, toNumber);
					addProgress("call initiated, logging out...", false);
					service.logout();
					addProgress("logout successful.", false);
					display.setCurrent(new Alert("Call initiated!", "Please wait a second, you will be called back!", null, AlertType.INFO), contactList);
				} catch (FinareaException e) {
					Debug.log("Call failed: " + e.getMessage());
					display.setCurrent(new Alert("Call failed!", e.getMessage(), null, AlertType.ERROR), contactList);
				} finally {
					//log out..
					try {
						if (service!=null) 
							service.logout();
					} catch (FinareaException e) {
						e.printStackTrace();
					}
					busy = false;
				}
			};
		}).start();
	}
	
	/**
	 * initiates a free web call (no login required)
	 * @param fromNumber
	 * @param toNumber
	 */
	private void startFreeCall(final String fromNumber, final String toNumber) {
		(new Thread() {
			public void run() {
				busy = true;
				try {
					progressBox.setTitle("Call in progress...");
					display.setCurrent(progressBox);
					addProgress("initiating free webcall...", true);
					FreeCaller service = new FreeCaller(httputil);
					service.makeFreeTrialCall(fromNumber, toNumber);
					addProgress("free webcall initiated.", true);
					display.setCurrent(new Alert("Call initiated!", "Please wait a second, you will be called back!", null, AlertType.INFO), contactList);
				} catch (FinareaException e) {
					Debug.log("Call failed: " + e.getMessage());
					display.setCurrent(new Alert("Call failed!", e.getMessage(), null, AlertType.ERROR), contactList);
				} finally {
					busy = false;
				}
			};
		}).start();
	}
	
	/**
	 * sends an sms with the given service (login required) 
	 * @param fromNumber
	 * @param toNumber
	 * @param text
	 */
	private void sendSms(final String toNumber, final String text, final Account account) {
		(new Thread() {
			public void run() {
				busy = true;
				FinareaConnection service = null;
				try {
					progressBox.setTitle("SMS in progress...");
					display.setCurrent(progressBox);
					addProgress("logging in...", true);
					if (useCallscript)
						service = new CallscriptFinareaConnection(httputil, account, 
								((TextField)settingsForm.get(1)).getString(), ((TextField)settingsForm.get(2)).getString());
					else
						service = new DirectFinareaConnection(httputil, account);service.login();
					addProgress("login successful, sending SMS...", false);
					service.sendSms(new String[]{toNumber}, text);
					addProgress("SMS sent, logging out...", false);
					service.logout();
					addProgress("logout successful.", false);
					display.setCurrent(new Alert("SMS sent!", "Your SMS has been sent to "+ toNumber +"!", null, AlertType.INFO), contactList);
				} catch (FinareaException e) {
					Debug.log("Sending SMS failed: " + e.getMessage());
					display.setCurrent(new Alert("Sending SMS failed!", e.getMessage(), null, AlertType.ERROR), contactList);
				} finally {
					//log out..
					try {
						if (service!=null) 
							service.logout();
					} catch (FinareaException e) {
						//e.printStackTrace();
					}
					busy = false;
				}
			};
		}).start();
	}
	
	
//TODO: comment for non-JSR75 mobiles
//#######
	
	/**
	 * loads the contacts into the ContactDetails array
	 * @return
	 * @throws Exception
	 */
	private synchronized ContactDetails[] loadContacts() throws Exception {
		ContactList contacts = null;
		Enumeration en = null;
		try {
			try {
				contacts = (ContactList) PIM.getInstance().openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY);	
				en = contacts.items();
			} catch (PIMException e1) {
				throw new Exception("could not open contactlist!");
			}
			
			int listSize = 100;
			ContactDetails[] contactDetails = new ContactDetails[listSize];
			
			int count = 0, grow = 50, timesgrown = 0;
			while (en.hasMoreElements()) {
				Contact contact = (Contact) en.nextElement();
				
				//get first name entry (given name, family name, etc..)
				String contactName = "unknown name";
				String[] contactsNames = contact.getStringArray(Contact.NAME, 0);
				for (int i=0; i<contactsNames.length; i++){
					if (contactsNames[i] != null && !contactsNames[i].trim().equals("")) {
						//TODO: check if given-name + family name are both given, or only name etc..
						contactName = contactsNames[i];
						Debug.log("name: " + contactName);
						continue;
					}
				}
				
				//get all numbers for contact
				int numSize = contact.countValues(Contact.TEL);
				String[] contactNumbers = new String[numSize];
				String[] contactNumbersDesc = new String[numSize];
				for (int j=0; j<numSize; j++) {
					contactNumbers[j] = contact.getString(Contact.TEL, j);
					Debug.log("number: " + contactNumbers[j]);
					if (contact.getAttributes(Contact.TEL, j) != Contact.ATTR_NONE) 
						contactNumbersDesc[j] = contacts.getAttributeLabel(contact.getAttributes(Contact.TEL, j));
					else
						contactNumbersDesc[j] = "default";
					Debug.log("label:" + contactNumbersDesc[j]);
				}
				
				//create new contact details object
				contactDetails[count++] = new ContactDetails(contactName, contactNumbers, contactNumbersDesc);
				if (count == (listSize + timesgrown*grow)) {
					contactDetails = growArray(contactDetails, grow);
					timesgrown += 1;
				}
			}
			Debug.log("Contacts size: " + count);
			return shrinkArrayToFit(contactDetails);
		} finally {
			try {
				contacts.close();
			} catch (PIMException e1) {
				//e1.printStackTrace();
			}	
		}
	}
//#######	

	/**
	 * grows an array by int grow and returns the grown array
	 * @param src the source array
	 * @param grow the number of elements to grow
	 * @return the grown array
	 */
	private ContactDetails[] growArray(ContactDetails[] src, int grow) {
		if (grow <= 0) return src;
		ContactDetails[] tmp = new ContactDetails[src.length + grow];
		System.arraycopy(src, 0, tmp, 0, src.length);
		Debug.log("grown by " + grow);
		return tmp;
	}
	
	/**
	 * shrinks an array by removing the fields filled with null
	 * beginning from the last field
	 * @param src the array to be shrunk
	 * @return the shrunk array
	 */
	private ContactDetails[] shrinkArrayToFit(ContactDetails[] src) {
		int numEmpty = 0;
		for (int i=src.length; i>0; i--) {
			if (src[i-1] == null) 
				numEmpty++;
			else
				continue;
		}
		ContactDetails[] tmp = new ContactDetails[src.length - numEmpty];
		System.arraycopy(src, 0, tmp, 0, tmp.length);
		Debug.log("shrunk from " + src.length + " to " + (src.length - numEmpty));
		return tmp;
	}
	
	
	/**
	 * class representing a contact and its numbers in memory after 
	 * it has been loaded via PIM API
	 * @author Torben
	 *
	 */
	private class ContactDetails {
		String name = null;
		String[] numbers = null;
		String[] numbersDescription = null;
		
		ContactDetails(String name, String[] numbers, String[] numbersDescription) {
			this.name = name;
			this.numbers = numbers;
			this.numbersDescription = numbersDescription;
		}
	}
	

	/**
	 * loads settings
	 */
	private void loadSettings() {
		RecordStore rs = null;
		try {
			 rs = RecordStore.openRecordStore(SETTINGS_RS_NAME, false);
			 //connection setting
			 byte[] raw = rs.getRecord(1);
			 if ("0".equals(new String(raw))) {
				 connectionChoice.setSelectedFlags(new boolean[]{true, false});
				 useCallscript = false;
			 } else if ("1".equals(new String(raw))) {
				 connectionChoice.setSelectedFlags(new boolean[]{false, true});
				 useCallscript = true;
			 }
			 //php-callscript url
			 raw = rs.getRecord(2);
			 callscriptField.setString(raw!=null ? new String(raw) : "");
			 //php-smsscript url
			 raw = rs.getRecord(3);
			 smsscriptField.setString(raw!=null ? new String(raw) : "");
			 //callback number
			 raw = rs.getRecord(4);
			 callbacknumberField.setString(raw!=null ? new String(raw) : "");
		} catch (RecordStoreFullException e) {
			e.printStackTrace();
		} catch (RecordStoreNotFoundException e) {
			//does not exist yet, set defaults
			connectionChoice.setSelectedFlags(new boolean[]{true, false});
			callscriptField.setString("https://");
			smsscriptField.setString("https://");
			callbacknumberField.setString("");
		} catch (RecordStoreException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs!=null) 
					rs.closeRecordStore();
			} catch (RecordStoreNotOpenException e) {
				e.printStackTrace();
			} catch (RecordStoreException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * saves the settings...
	 *
	 */
	private void saveSettings() throws FinareaException {
		RecordStore rs = null;
		try {
			 rs = RecordStore.openRecordStore(SETTINGS_RS_NAME, true);
			 if (rs.getNumRecords() < 1) {
				 //just created, add empty records...
				 for (int i=0; i<settingsForm.size(); i++)
					 rs.addRecord("".getBytes(), 0, "".getBytes().length);
			 }
			 //connection setting
			 byte[] raw = (""+connectionChoice.getSelectedIndex()).getBytes();
			 rs.setRecord(1, raw, 0, raw.length);
			 //php-callscript url
			 raw = callscriptField.getString().getBytes();
			 rs.setRecord(2, raw, 0, raw.length);
			 //php-callscript url
			 raw = smsscriptField.getString().getBytes();
			 rs.setRecord(3, raw, 0, raw.length);
			 //callback number
			 raw = callbacknumberField.getString().getBytes();
			 rs.setRecord(4, raw, 0, raw.length);
		} catch (Exception e) {
			e.printStackTrace();
			throw new FinareaException(e.getMessage());
		} finally {
			try {
				if (rs!=null)
					rs.closeRecordStore();
			} catch (RecordStoreNotOpenException e) {
				e.printStackTrace();
			} catch (RecordStoreException e) {
				e.printStackTrace();
			}
		}
	}

}
