package org.obm.caldav.utils;

import java.net.MalformedURLException;
import java.util.UUID;


public class CalDavUtils {
	
	public static String getExtIdFromURL(String url) throws MalformedURLException{
		String extId = "";
		int indexIcs= url.lastIndexOf("/");
		if(indexIcs != -1){
			extId = url.substring(indexIcs+1);
			extId = extId.replace(".ics", "");
		}
		if(extId == null && "".equals(extId)){
			throw new MalformedURLException("Invalid URI[ "+url+ "]");	
		}
		return extId;
	}
	
	public static String generateExtId() {
			
		return "caldav-"+UUID.randomUUID();
	}
}
