/**
 * 
 */
package de.tknerr.mobdev.finarea;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 * @author Torben
 * 
 */
public class RecordStoreUtil {
	
	/**
	 * data delimiter within one record
	 */
	public final static char DATA_DELIM = '|';
	/**
	 * delimiter for key-value pairs
	 */
	public final static char KV_DELIM = ':';
	/**
	 * character used for escaping delimiters
	 */
	public final static char ESCAPE_CHAR = '!';
	
	
	/**
	 * adds a DELIM delimited record to the recordstore, creates recordstore if
	 * it does not exist
	 * 
	 * @param record
	 * @param recordStore
	 * @throws RecordStoreException
	 */
	public static int addRecord(Hashtable recordData, String recordStore) throws RecordStoreException {
		RecordStore rs = null;
		try {
			rs = RecordStore.openRecordStore(recordStore, true);
			String rec = encodeRecord(recordData);
			return rs.addRecord(rec.getBytes(), 0, rec.length());
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
	 * updates a DELIM delimited record in the recordstore
	 * 
	 * @param recordId
	 * @param newRecordData
	 * @param recordStore
	 * @throws RecordStoreException
	 */
	public static void updateRecord(int recordId, Hashtable newRecordData, String recordStore) throws RecordStoreException {
		RecordStore rs = null;
		try {
			rs = RecordStore.openRecordStore(recordStore, false);
			String rec = encodeRecord(newRecordData);
			rs.setRecord(recordId, rec.getBytes(), 0, rec.length());
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
	 * retrieves all records from the given recordstore. the records (hashtable) are returned 
	 * within a hashtable indexed by each records key.
	 * @param recordStore
	 * @return
	 * @throws RecordStoreException
	 */
	public static Hashtable getAllRecords(String recordStore) throws RecordStoreException {
		RecordStore rs = null;
		try {
			rs = RecordStore.openRecordStore(recordStore, false);
			RecordEnumeration en = rs.enumerateRecords(null, null, false);
			Hashtable allRecords = new Hashtable();
			while (en.hasNextElement()) {
				int id = en.nextRecordId();
				allRecords.put(new Integer(id), getRecord(id, recordStore));
			}
			return allRecords;
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
	 * retrieves the record with the given id from the given recordstore
	 * @param recordId
	 * @param recordStore
	 * @return
	 * @throws RecordStoreException
	 */
	public static Hashtable getRecord(int recordId, String recordStore) throws RecordStoreException {
		RecordStore rs = null;
		try {
			rs = RecordStore.openRecordStore(recordStore, false);
			byte[] raw = rs.getRecord(recordId);
			if (raw == null)
				return null;
			return decodeRecord(new String(raw));
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
	 * removes a record from the record store
	 * 
	 * @param recordId
	 * @param recordStore
	 * @throws RecordStoreException
	 */
	public static void deleteRecord(int recordId, String recordStore) throws RecordStoreException {
		RecordStore rs = null;
		try {
			rs = RecordStore.openRecordStore(recordStore, false);
			rs.deleteRecord(recordId);
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
	 * encodes key-value pairs hashtable to a delimited string representation
	 * @param record
	 * @return
	 */
	private static String encodeRecord(Hashtable record) {
		Enumeration en = record.keys();
		StringBuffer buffer = new StringBuffer();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			String value = (String) record.get(key);
			//TODO: quick hack (not a problem if we assume that the values are already trim()-ed)
			if (value.endsWith(""+ESCAPE_CHAR))
				value += " ";
			buffer.append(escapeDelimiter(key) + KV_DELIM + escapeDelimiter(value));
			buffer.append(DATA_DELIM);
		}
		return buffer.toString();
	}
	
	/**
	 * decodes a delimited string back to a hashtable of KV pairs
	 * @param data
	 * @return
	 */
	public static Hashtable decodeRecord(String data) {
		Hashtable ht = new Hashtable();
		int endIdx = -1, beginIdx = 0;
		while ((endIdx = data.indexOf(DATA_DELIM, endIdx)) > 0) {
			if (data.charAt(endIdx-1) == ESCAPE_CHAR) {
				endIdx++;
				continue;
			}
			String tmp = data.substring(beginIdx, endIdx);
			beginIdx = ++endIdx;
			int kvIdx = -1;
			while ((kvIdx = tmp.indexOf(KV_DELIM, kvIdx)) > 0) {
				if (tmp.charAt(kvIdx-1) == ESCAPE_CHAR) { 
					kvIdx++;
					continue;
				}
				String key = unescapeDelimiters(tmp.substring(0, kvIdx));
				String value = unescapeDelimiters(tmp.substring(kvIdx + 1));
				//TODO: quick hack (not a problem if we assume that the values are already trim()-ed)
				if (value.endsWith(ESCAPE_CHAR + " "))
					value = value.substring(0, value.length()-1);
				ht.put(key, value);
				break;
			}
		}
		return ht;
	}
	
	/**
	 * escapes the delimiter
	 * @param s
	 * @return
	 */
	private static String escapeDelimiter(String s) {
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0; i < s.length(); i++) {
		    char c = s.charAt(i);
			switch (c) {
			    case KV_DELIM: 
			    case DATA_DELIM: 
			    	buf.append (ESCAPE_CHAR);
			    default:
			        buf.append(c);
		    }
		}
		return buf.toString();
	}
	
	
	/**
	 * unescapes the delimiters from the string
	 * @param s
	 * @return
	 */
	private static String unescapeDelimiters(String s) {
		StringBuffer buf = new StringBuffer();
		
		for (int i = 0; i < s.length(); i++) {
		    char c = s.charAt(i);
			switch (c) {
			    case ESCAPE_CHAR: 
			    	if (i!=s.length()-1 && 
			    			(s.charAt(i+1)==KV_DELIM || s.charAt(i+1)==DATA_DELIM)) {
			    		buf.append(s.charAt(i+1));
			    		i++;
			    		break;
			    	}
			    default:
			        buf.append(c);
		    }
		}
		return buf.toString();
	}
	
	public static void main(String[] args) {
		System.out.println("bla".getBytes().length);
	}

	
//	public static void main(String[] args) {
//		String s, se, su;
//		String[] tests = new String[]{"!:||:sa:s!>|:", "hal::lo", "ba!!!z", "boo::", "ha:|:!test!llo", "bla!", ":!foo:!!!"};
//		for (int i=0; i<tests.length; i++) {
//			System.out.println(s = tests[i]);
//			System.out.println(se = escapeDelimiter(s));
//			System.out.println(su = unescapeDelimiters(se));
//			System.out.println();
//		}
//
//		Hashtable ht = new Hashtable();
//		ht.put("name:", ":pater");
//		ht.put("|user| ", "|bls!| ");
//		ht.put("pass", "x!*#:! ");
//		String enc = encodeRecord(ht);
//		System.out.println(enc);
//		Hashtable ht2 = decodeRecord(enc);
//		System.out.println("'"+ht2.get("name:")+"'");
//		System.out.println("'"+ht2.get("|user| ")+"'");
//		System.out.println("'"+ht2.get("pass")+"'");
//	}
}
