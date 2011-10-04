package org.obm.sync.utils;

public class DisplayNameUtils {

	public static String getDisplayName(String commonName, String firstName, String lastName){
		if(isNotEmpty(commonName)){
			return commonName;
		} else if(isNotEmpty(lastName)){
			return isNotEmpty(firstName) ? firstName + " " + lastName : lastName;
		} else if(isNotEmpty(firstName)){
			return firstName;
		}
		return "";
	}
	
	private static boolean isNotEmpty(String s){
		return s != null && !s.isEmpty();
	}
}
