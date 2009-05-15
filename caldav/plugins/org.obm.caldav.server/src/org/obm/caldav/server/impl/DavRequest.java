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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;


public class DavRequest {

	private static final Log logger = LogFactory.getLog(DavRequest.class);

	private Document document;

	private HttpServletRequest req;

	@SuppressWarnings("unchecked")
	public DavRequest(HttpServletRequest req) {
		this.req = req;
		Enumeration headerNames = req.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String hn = (String) headerNames.nextElement();
			String val = req.getHeader(hn);
			logger.info(hn + ": " + val);
		}

		if (req.getHeader("Content-Type") != null
				&& !req.getContentType().contains("calendar")) {
			try {
				InputStream in = req.getInputStream();
				document = DOMUtils.parse(in);
				DOMUtils.logDom(document);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
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

}
