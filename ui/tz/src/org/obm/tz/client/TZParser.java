package org.obm.tz.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TZParser implements EntryPoint {

	private static ZoneInfo zi;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		GWT.log("Exporting static methods...", null);
		exportStaticMethods();
	}

	public native void exportStaticMethods() /*-{
	   $wnd.parseTimeZoneData =
	      @org.obm.tz.client.TZParser::parseTimeZoneData([B);
	   $wnd.getTimeZoneOffset =
	      @org.obm.tz.client.TZParser::getOffset(Ljava/lang/String;);
	}-*/;

	public static void parseTimeZoneData(byte[] tzData) {

		zi = new ZoneInfo(tzData);
	}

	public static int getOffset(String ts) {
		return zi.getOffset(Long.parseLong(ts) / 1000);
	}

}
