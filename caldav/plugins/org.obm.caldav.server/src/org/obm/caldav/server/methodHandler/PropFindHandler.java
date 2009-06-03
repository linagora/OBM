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

package org.obm.caldav.server.methodHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.DavPropertyHandler;
import org.obm.caldav.server.propertyHandler.impl.CCalendarHomeSet;
import org.obm.caldav.server.propertyHandler.impl.CCalendarUserAddressSet;
import org.obm.caldav.server.propertyHandler.impl.CSGetCTag;
import org.obm.caldav.server.propertyHandler.impl.CScheduleInboxURL;
import org.obm.caldav.server.propertyHandler.impl.CScheduleOutboxURL;
import org.obm.caldav.server.propertyHandler.impl.DOwner;
import org.obm.caldav.server.propertyHandler.impl.DResourceType;
import org.obm.caldav.server.resultBuilder.PropertyListBuilder;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class PropFindHandler extends DavMethodHandler {

	private Map<String,DavPropertyHandler> propertiesHandler;
	
	public PropFindHandler() {
		
		propertiesHandler = new HashMap<String, DavPropertyHandler>();
		propertiesHandler.put("D:resourcetype", new DResourceType());
		propertiesHandler.put("D:owner", new DOwner());
		propertiesHandler.put("CS:getctag", new CSGetCTag());
		
		propertiesHandler.put("C:calendar-home-set", new CCalendarHomeSet());
		propertiesHandler.put("C:calendar-user-address-set", new CCalendarUserAddressSet());
		propertiesHandler.put("C:schedule-inbox-URL", new CScheduleInboxURL());
		propertiesHandler.put("C:schedule-outbox-URL", new CScheduleOutboxURL());
	}

	
	
	@Override
	public void process(Token t, IProxy proxy, DavRequest req, HttpServletResponse resp) {
		logger.info("process(req, resp)");

		String depth = req.getHeader("Depth");
		logger.info("depth: " + depth);
		
		Set<String> toLoad = new HashSet<String>();
		Document doc = req.getDocument();
		Element root = doc.getDocumentElement();
		Element prop = DOMUtils.getUniqueElement(root, "D:prop");
		NodeList propsToLoad = prop.getChildNodes();

		for (int i = 0; i < propsToLoad.getLength(); i++) {
			Node n = propsToLoad.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
					toLoad.add(n.getNodeName());
			}
		}
		
		Document ret = new PropertyListBuilder().build(t, req,propertiesHandler, toLoad);

		try {
			DOMUtils.logDom(ret);

			resp.setStatus(207); // multi status webdav
			resp.setContentType("text/xml; charset=utf-8");
			DOMUtils.serialise(ret, resp.getOutputStream());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
