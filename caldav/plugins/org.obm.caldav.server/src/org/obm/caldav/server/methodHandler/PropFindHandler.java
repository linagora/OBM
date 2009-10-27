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

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.propertyHandler.PropfindPropertyHandler;
import org.obm.caldav.server.propertyHandler.impl.CalendarColor;
import org.obm.caldav.server.propertyHandler.impl.CalendarDescription;
import org.obm.caldav.server.propertyHandler.impl.CalendarHomeSet;
import org.obm.caldav.server.propertyHandler.impl.CalendarUserAddressSet;
import org.obm.caldav.server.propertyHandler.impl.DisplayName;
import org.obm.caldav.server.propertyHandler.impl.GetCTag;
import org.obm.caldav.server.propertyHandler.impl.GetContentType;
import org.obm.caldav.server.propertyHandler.impl.GetETag;
import org.obm.caldav.server.propertyHandler.impl.Owner;
import org.obm.caldav.server.propertyHandler.impl.ResourceType;
import org.obm.caldav.server.propertyHandler.impl.ScheduleInboxURL;
import org.obm.caldav.server.propertyHandler.impl.ScheduleOutboxURL;
import org.obm.caldav.server.propertyHandler.impl.SupportedCalendarComponentSet;
import org.obm.caldav.server.resultBuilder.PropertyListBuilder;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.DOMUtils;
import org.obm.sync.calendar.EventTimeUpdate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * http://www.webdav.org/specs/rfc2518.html#METHOD_PROPFIND
 * @author adrienp
 *
 */
public class PropFindHandler extends DavMethodHandler {

	private Map<String,PropfindPropertyHandler> propertiesHandler;
	
	public PropFindHandler() {
		
		propertiesHandler = new HashMap<String, PropfindPropertyHandler>();
		propertiesHandler.put("owner", new Owner());
		propertiesHandler.put("getctag", new GetCTag());
		propertiesHandler.put("getetag", new GetETag());
		propertiesHandler.put("getcontenttype", new GetContentType());
		
		propertiesHandler.put("calendar-home-set", new CalendarHomeSet());
		propertiesHandler.put("calendar-user-address-set", new CalendarUserAddressSet());
		propertiesHandler.put("schedule-inbox-URL", new ScheduleInboxURL());
		propertiesHandler.put("schedule-outbox-URL", new ScheduleOutboxURL());
		
		propertiesHandler.put("displayname", new DisplayName());
		propertiesHandler.put("calendar-description", new CalendarDescription());
		propertiesHandler.put("supported-calendar-component-set", new SupportedCalendarComponentSet());
		propertiesHandler.put("calendar-color", new CalendarColor());
		propertiesHandler.put("resourcetype", new ResourceType());
		
		
	}

	
	
	@Override
	public void process(Token t, IBackend proxy, DavRequest req, HttpServletResponse resp) throws Exception {
		Set<PropfindPropertyHandler> toLoad = new HashSet<PropfindPropertyHandler>();
		Set<Element> toNotImplemented = new HashSet<Element>();
		Document doc = req.getDocument();
		
		Set<Element> propsToLoad = getPropList(doc);
		for (Element node : propsToLoad) {
			PropfindPropertyHandler dph = propertiesHandler.get(node.getLocalName());
			if(dph != null && dph.isUsed()){
				toLoad.add(dph);
			} else {
				toNotImplemented.add(node);
				logger.warn("the Property ["+node.getLocalName()+"] is not used");
			}
		}
		
		Set<String> urls = new HashSet<String>();
		urls.add(req.getURI());
		
		if(toLoad.contains(propertiesHandler.get("getcontenttype"))){
			List<EventTimeUpdate> events = proxy.getCalendarService().getAllLastUpdateEvents();
			for(EventTimeUpdate event : events){
				urls.add(req.getURI()+proxy.getCalendarService().getICSName(event));
			}
		}
		
		Document ret = new PropertyListBuilder().build(t, req,urls, toLoad, toNotImplemented, proxy);

		try {
			if(logger.isDebugEnabled()){
				DOMUtils.logDom(ret);
			}
			resp.setStatus(207); // multi status webdav
			resp.setContentType("text/xml; charset=utf-8");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DOMUtils.serialise(ret, out);

			resp.setContentLength(out.size());
			resp.getOutputStream().write(out.toByteArray());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
