package org.obm.caldav.utils;

import java.net.MalformedURLException;


public class CalDavUtils {
	
	public static String getExtIdFromURL(String url) throws MalformedURLException{
		String extId = "";
		int indexIcs= url.lastIndexOf("/");
		if(indexIcs != -1){
			extId = url.substring(indexIcs+1);
			extId = extId.replace(".ics", "");
		}
		if(extId == null && extId.equals("")){
			throw new MalformedURLException("Invalid URI[ "+url+ "]");	
		}
		return extId;
	}
}
