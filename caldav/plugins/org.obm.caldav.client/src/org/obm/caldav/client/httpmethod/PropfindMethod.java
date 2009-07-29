package org.obm.caldav.client.httpmethod;


import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Adds a PROPFIND method to Apache Commons HTTPClient library. It's logically
 * just like a POST request, so we can just extend that class and change its
 * protocol verb (getName()).
 */
public class PropfindMethod extends PostMethod {
	/**
	 * @param url
	 *            target WebDAV resource
	 */
	public PropfindMethod(String url) {
		super(url);
	}

	public String getName() {
		return "PROPFIND";
	}
}
