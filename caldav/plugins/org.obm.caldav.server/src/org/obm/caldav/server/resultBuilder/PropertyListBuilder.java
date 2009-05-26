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

package org.obm.caldav.server.resultBuilder;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.DavPropertyHandler;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class PropertyListBuilder {
	
	private Log logger = LogFactory.getLog(PropertyListBuilder.class);
	
	public Document build(Token t, DavRequest req,Map<String,DavPropertyHandler> propertiesHandler, Set<String> toLoad) {
		try {
			Document ret = DOMUtils.createDoc("DAV:", "D:multistatus");
			Element r = ret.getDocumentElement();
			r.setAttribute("xmlns:D", "DAV:");
			r.setAttribute("xmlns:CS", "http://calendarserver.org/ns/");
			r.setAttribute("xmlns:C", "urn:ietf:params:xml:ns:caldav");
			r.setAttribute("xmlns", "urn:ietf:params:xml:ns:caldav");
			Element response = DOMUtils.createElement(r, "D:response");
			DOMUtils.createElementAndText(response, "D:href", req.getHref());
			Element pStat = DOMUtils.createElement(response, "D:propstat");
			Element p = DOMUtils.createElement(pStat, "D:prop");
			
			//DAVStore store = new DAVStore();
			for (String s : toLoad) {
				Element val = DOMUtils.createElement(p, s);
				DavPropertyHandler dph = propertiesHandler.get(s);
				if(dph != null){
					dph.appendPropertyValue(val, t, req);
					//store.appendPropertyValue(val, t, req);
				} else {
					logger.warn("the Property ["+s+"] is not implemented");
				}
			}
			DOMUtils.createElementAndText(pStat, "D:status", "HTTP/1.1 200 OK");
			
			return ret;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}

	}

}
