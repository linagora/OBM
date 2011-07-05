package org.obm.push.search;


public enum StoreName {
	Mailbox,DocumentLibrary,GAL;
	
	public static StoreName getValue(String value) {
		if("Mailbox".equals(value)){
			return Mailbox;
		} else if("Document Library".equals(value)){
			return DocumentLibrary;
		} else if("GAL".equals(value)){
			return GAL;
		} 
		return null;
	}
}
