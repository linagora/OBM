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
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.share.Token;
import org.obm.sync.calendar.Event;


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
	public void process(Token token, IBackend proxy, DavRequest req,
			HttpServletResponse resp) throws Exception {
		logger.info("process(req, resp)");
		List<Event> event = proxy.getCalendarService().getAllEvents();
		Event ev = event.get(0);
		Set<String> l = new HashSet<String>();
		l.add(ev.getExtId());
		Map<Event,String>  e = proxy.getCalendarService().getICSFromExtId(l);
		String ics = e.values().iterator().next();
		resp.getOutputStream().write(ics.getBytes());
		
		//throw new CalDavException(StatusCodeConstant.SC_NOT_ALLOWED);
	}

}
