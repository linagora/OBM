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
import org.obm.caldav.server.StatusCodeConstant;
import org.obm.caldav.server.exception.AuthorizationException;
import org.obm.caldav.server.exception.ResultBuilderException;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.resultBuilder.ErreurBuiler;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.CalDavUtils;
import org.w3c.dom.Document;


/**
 * RFC4918 
 * 	9.6.  DELETE Requirements
 * 
 * http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-03
 * 
 * @author adrienp
 *
 */
public class DeleteHandler extends DavMethodHandler {

	@Override
	public void process(Token token, IBackend proxy, DavRequest req,
			HttpServletResponse resp) throws ResultBuilderException {
		try {
			String extId = CalDavUtils.getExtIdFromURL(req.getURI());

			proxy.getCalendarService().removeOrUpdateParticipationState(extId);
			
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentLength(0);
			resp.setDateHeader("Delete", new Date().getTime());
		} catch (MalformedURLException e) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.setContentLength(0);
			logger.error(e.getMessage(), e);
		}catch (AuthorizationException e) {
			resp.setStatus(StatusCodeConstant.SC_MULTI_STATUS);
			Document ret = new ErreurBuiler().build(token, req, e.getHttpStatusCode());
			sendDom(ret, resp);
		}catch (Exception e) {
			resp.setStatus(StatusCodeConstant.SC_MULTI_STATUS);
			Document ret = new ErreurBuiler().build(token, req, StatusCodeConstant.SC_METHOD_FAILURE);
			sendDom(ret, resp);
			logger.error("Unable to delete event", e);
		}
	}

}
