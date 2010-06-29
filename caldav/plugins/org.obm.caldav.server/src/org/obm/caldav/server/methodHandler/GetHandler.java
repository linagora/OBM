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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.StatusCodeConstant;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.share.CalendarResourceICS;
import org.obm.caldav.server.share.CalDavToken;


/**
 * 5.3.4.  Calendar Object Resource Entity Tag
 * 
 * 		A response to a GET request targeted at a calendar object resource
 * 		MUST contain an ETag response header field indicating the current
 * 		value of the strong entity tag of the calendar object resource.
 * 
 * @author adrienp
 *
 */
public class GetHandler extends DavMethodHandler {

	@Override
	public void process(CalDavToken token, IBackend backend, DavRequest req,
			HttpServletResponse resp) throws Exception {
		logger.info("process(req, resp)");
		if(req.getURI().endsWith(".ics")){
			int ids = req.getURI().lastIndexOf("/");
			String eventName = req.getURI().substring(ids+1);
			int idp = eventName.indexOf(".");
			eventName = eventName.substring(0,idp);
			String compUrl = req.getURI().substring(0,ids);
			logger.info("Try to get event["+eventName+"] in component "+compUrl);
			Set<String> l = new HashSet<String>();
			l.add(eventName);
			List<CalendarResourceICS> crs = backend.getCalendarService().getICSFromExtId(token,compUrl, l);
			if(crs.iterator().hasNext()){
				CalendarResourceICS ics = crs.iterator().next(); 
				logger.info("Send:"+ics.getIcs());
				resp.getOutputStream().write(ics.getIcs().getBytes());
				resp.setHeader("ETag", ics.getETag());
				resp.setContentType("text/calendar");
				resp.setContentLength(ics.getIcs().getBytes().length);
			} else {
				resp.sendError(StatusCodeConstant.SC_NOT_FOUND);
			}
		}
//		List<Event> event = proxy.getCalendarService().getAll(DavComponent.VEVENT);
//		Event ev = event.get(0);
//		Set<String> l = new HashSet<String>();
//		l.add(ev.getExtId());
//		Map<Event,String>  e = proxy.getCalendarService().getICSFromExtId(l);
//		String ics = e.values().iterator().next();
//		resp.getOutputStream().write(ics.getBytes());
		//throw new CalDavException(StatusCodeConstant.SC_NOT_ALLOWED);
	}

}
