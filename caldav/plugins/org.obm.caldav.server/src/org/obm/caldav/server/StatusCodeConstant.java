package org.obm.caldav.server;

import java.util.HashMap;
import java.util.Map;

public class StatusCodeConstant {
	
	private static Map<Integer, String> messages;
	
	static{
		messages = new HashMap<Integer, String>();
		messages.put(207, "Multi-Status");
		
		messages.put(403, "Internal Server Error");
		messages.put(404, "Not Found");
		messages.put(424, "Method failure");

		messages.put(500, "Internal Server Error");
	}
	
	
	public static int SC_MULTI_STATUS = 207;
	
	
	public static int SC_FORBIDDEN = 403;
	public static int SC_NOT_FOUND = 404;
	public static int SC_METHOD_FAILURE = 424;
	
	public static int SC_INTERNAL_SERVER_ERROR = 500;


	public static String getStatusMessage(int code){
		return messages.get(code);
	}
}
