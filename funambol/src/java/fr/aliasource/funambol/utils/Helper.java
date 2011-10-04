package fr.aliasource.funambol.utils;

import java.util.List;
import java.util.Set;

import com.funambol.framework.engine.SyncItem;

/**
 * @author Lascombes
 *
 */
public class Helper {

	public static final int RESTRICT_PRIVATE = 1;
	public static final int RESTRICT_OWNER 	= 2;
	public static final int RESTRICT_REFUSED = 4;
	public static final int RESTRICT_PUBLIC_R = 8;
	public static final int RESTRICT_PUBLIC_W = 16;
	
	public static final int RESTRICTS_DEFAULT = 1;
	
	public Helper() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * Repalce a null or "null" string by "" or return the same string
	 * @param str
	 * @return
	 */
	public static String nullToEmptyString(String str) {

	    String result = null;

	    if ((str == null) || (str.equalsIgnoreCase("null"))) {
	        result = "";
	    } else {
	        result = str;
	    }

	    return result;
	}


	public static String getContentOfSyncItem(SyncItem item, boolean encode) {
		
		String result = null;
		
		byte[] itemContent = item.getContent();
		
		   // Add content processing here, if needed
		result = new String(itemContent == null ? new byte[0] : itemContent);
		
		return result.trim();
	}


	public static String getPriority(int priority) {
		return ""+(priority-1);
	}

	public static Integer getPriorityFromFoundation(String priority) {
		Integer value = 1;
		if ( !nullToEmptyString(priority).equals("") ) {
			try {
			value = Integer.parseInt(priority)+1;
			} catch (NumberFormatException nfe) {
			}
		}
		return value;
	}


	public static Short nullToZero(Short value) {
		if ( ! (value == null) ) {			
			return value;
		} else {
			return new Short((short) 0);
		}
	}

	public static String[] listToTab(List<String> list) {
		String[] result = (String[]) list.toArray(new String[list.size()]);
		return result;
	}


	public static String[] setToTab(Set<String> set) {
		String[] result = (String[]) set.toArray(new String[set.size()]);
		return result;
	}

}
