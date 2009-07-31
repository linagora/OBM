/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   obm.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.StatusCodeConstant;
import org.obm.caldav.server.exception.CalDavException;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;


public class DavRequest {

	private static final Log logger = LogFactory.getLog(DavRequest.class);

	private Document document;
	private String calendarName;
	private HttpServletRequest req;

	@SuppressWarnings("unchecked")
	public DavRequest(HttpServletRequest req) throws CalDavException {
		this.req = req;
		Enumeration headerNames = req.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String hn = (String) headerNames.nextElement();
			String val = req.getHeader(hn);
			logger.debug(hn + ": " + val);
		}

		initRequest();
	}
	
	private void initRequest() throws CalDavException {
		
		if (req.getHeader("Content-Type") != null
				&& !req.getContentType().contains("calendar")) {
			try {
				InputStream in = req.getInputStream();
				document = DOMUtils.parse(in);
				if(logger.isDebugEnabled()){
					DOMUtils.logDom(document);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		String uri = req.getRequestURI(); 
		if(uri.startsWith("/")){
			uri = uri.substring(1);
		}
		String[] comp = uri.split("/");
		if(comp.length == 0 || !comp[0].contains("@")){
			throw new CalDavException(StatusCodeConstant.SC_NOT_FOUND);
		} else {
			this.calendarName = comp[0];
		}
	}

	public InputStream getInputStream() throws IOException {
		return req.getInputStream();
	}

	public String getHeader(String string) {
		return req.getHeader(string);
	}

	public Document getDocument() {
		return document;
	}

	public String getHref() {
		return req.getRequestURL().toString();
	}
	
	public String getURI() {
		return req.getRequestURI();
	}
	
	public String getCalendarComponantName(){
		return this.calendarName;
	}
	
	public HttpSession getSession(){
		return this.req.getSession();
	}
}
