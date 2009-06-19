package org.obm.caldav.server.methodHandler;

import java.net.MalformedURLException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.obm.caldav.server.AuthorizationException;
import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.StatusCodeConstant;
import org.obm.caldav.server.impl.DavRequest;
import org.obm.caldav.server.resultBuilder.ErreurBuiler;
import org.obm.caldav.server.resultBuilder.ResultBuilderException;
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
	public void process(Token token, IProxy proxy, DavRequest req,
			HttpServletResponse resp) throws ResultBuilderException {
		try {
			String extId = CalDavUtils.getExtIdFromURL(req.getURI());

			logger.info("new ext id "+extId);
			proxy.getCalendarService().removeOrUpdateParticipationState(extId);
			
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentLength(0);
			resp.setDateHeader("Delete", new Date().getTime());
			resp.setHeader("ETag", extId);
		} catch (MalformedURLException e) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.setContentLength(0);
			logger.error(e.getMessage(), e);
		}catch (AuthorizationException e) {
			//FIXME RETOURNE LE BON STATUS D'ERREUR
			resp.setStatus(StatusCodeConstant.SC_MULTI_STATUS);
			Document ret = new ErreurBuiler().build(token, req, StatusCodeConstant.SC_METHOD_FAILURE);
			sendDom(ret, resp);
		}catch (Exception e) {
			resp.setStatus(StatusCodeConstant.SC_MULTI_STATUS);
			Document ret = new ErreurBuiler().build(token, req, StatusCodeConstant.SC_METHOD_FAILURE);
			sendDom(ret, resp);
			logger.error("Unable to delete event", e);
		}
	}

}
