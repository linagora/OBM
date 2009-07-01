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
import org.obm.caldav.server.NameSpaceConstant;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.PropfindPropertyHandler;
import org.obm.caldav.server.propertyHandler.impl.CalendarHomeSet;
import org.obm.caldav.server.propertyHandler.impl.CalendarUserAddressSet;
import org.obm.caldav.server.propertyHandler.impl.GetCTag;
import org.obm.caldav.server.propertyHandler.impl.ScheduleInboxURL;
import org.obm.caldav.server.propertyHandler.impl.ScheduleOutboxURL;
import org.obm.caldav.server.propertyHandler.impl.Owner;
import org.obm.caldav.server.propertyHandler.impl.ResourceType;
import org.obm.caldav.server.resultBuilder.PropertyListBuilder;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class PropFindHandler extends DavMethodHandler {

	private Map<String,PropfindPropertyHandler> propertiesHandler;
	
	public PropFindHandler() {
		
		propertiesHandler = new HashMap<String, PropfindPropertyHandler>();
		propertiesHandler.put("D:resourcetype", new ResourceType());
		propertiesHandler.put("D:owner", new Owner());
		propertiesHandler.put("CS:getctag", new GetCTag());
		
		propertiesHandler.put("C:calendar-home-set", new CalendarHomeSet());
		propertiesHandler.put("C:calendar-user-address-set", new CalendarUserAddressSet());
		propertiesHandler.put("C:schedule-inbox-URL", new ScheduleInboxURL());
		propertiesHandler.put("C:schedule-outbox-URL", new ScheduleOutboxURL());
	}

	
	
	@Override
	public void process(Token t, IProxy proxy, DavRequest req, HttpServletResponse resp) {
		logger.info("process(req, resp)");

		Set<String> toLoad = new HashSet<String>();
		Document doc = req.getDocument();
		Element root = doc.getDocumentElement();
		Element prop = DOMUtils.getUniqueElement(root, "prop", NameSpaceConstant.DPROP_NAMESPACE);
		NodeList propsToLoad = prop.getChildNodes();

		for (int i = 0; i < propsToLoad.getLength(); i++) {
			Node n = propsToLoad.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
					toLoad.add(n.getNodeName());
			}
		}
		
		Document ret = new PropertyListBuilder().build(t, req,propertiesHandler, toLoad, proxy);

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
