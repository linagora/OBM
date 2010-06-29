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

import java.net.MalformedURLException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.share.DavComponent;
import org.obm.caldav.server.share.CalDavToken;
import org.obm.caldav.utils.CalDavUtils;


public class PutHandler extends DavMethodHandler {

	public PutHandler() {
	}

	@Override
	public void process(CalDavToken token, IBackend proxy, DavRequest req, HttpServletResponse resp) {
		logger.info("process(req, resp)");
		try {
			String extId = CalDavUtils.getExtIdFromURL(req.getURI());
			String parentComp = "";
			int ls = req.getURI().lastIndexOf("/");
			if(ls > 0){
				parentComp = req.getURI().substring(0, ls);
			} else {
				parentComp = req.getURI();
			}
			
			String ics = req.getICS();
			DavComponent dav = proxy.getCalendarService().updateOrCreateEvent(token,parentComp,ics, extId);
			
			resp.setStatus(HttpServletResponse.SC_CREATED);
			resp.setContentLength(0);
			resp.setDateHeader("Created", new Date().getTime());
			logger.info("send Etag["+dav.getETag()+"]");
			resp.addHeader("ETag", dav.getETag());
		} catch (MalformedURLException e) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.setContentLength(0);
			logger.error(e.getMessage(), e);
		}catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp.setContentLength(0);
			logger.error("Unable to create event", e);
		}
	}

}
