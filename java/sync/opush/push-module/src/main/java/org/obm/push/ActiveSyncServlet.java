/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.obm.annotations.transactional.Transactional;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.exception.DaoException;
import org.obm.push.handler.IContinuationHandler;
import org.obm.push.handler.IRequestHandler;
import org.obm.push.handler.AuthenticatedServlet;
import org.obm.push.impl.PushContinuation;
import org.obm.push.impl.PushContinuation.Factory;
import org.obm.push.impl.Responder;
import org.obm.push.impl.ResponderImpl;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.protocol.request.Base64QueryString;
import org.obm.push.protocol.request.SimpleQueryString;
import org.obm.push.service.DeviceService;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.BadRequestException;
import org.obm.sync.client.login.LoginService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * ActiveSync server implementation. Routes all request to appropriate request
 * handlers.
 */
@Singleton
public class ActiveSyncServlet extends AuthenticatedServlet {

	private Handlers handlers;
	private SessionService sessionService;
	private PushContinuation.Factory continuationFactory;
	private DeviceService deviceService;
	
	private final ResponderImpl.Factory responderFactory;
	private final IBackend backend;

	@Inject
	protected ActiveSyncServlet(LoginService loginService,
			SessionService sessionService, LoggerService loggerService,
			IBackend backend, Factory continuationFactory, DeviceService deviceService, 
			User.Factory userFactory, ResponderImpl.Factory responderFactory, Handlers handlers) {
	
		super(loginService, loggerService, userFactory);
		
		this.sessionService = sessionService;
		this.backend = backend;
		this.continuationFactory = continuationFactory;
		this.deviceService = deviceService;
		this.responderFactory = responderFactory;
		this.handlers = handlers;
	}

	@Override
	public void init() throws ServletException {
		super.init();
		backend.startMonitoring();
	}
	
	@Override
	@Transactional
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		try {
			IContinuation c = continuationFactory.createContinuation(request);

			logger.debug(
					"query = {}, initial = {}, resume = {}, m = {}, num = {}",
					new Object[] { request.getQueryString(), c.isInitial(),
							c.isResumed(), request.getMethod(), c.getReqId() });

			if (c.isResumed() || !c.isInitial()) {
				handleContinuation(response, c);
				return;
			}

			String m = request.getMethod();
			if ("OPTIONS".equals(m)) {
				sendOptionsResponse(response);
				return;
			}

			if ("GET".equals(m)) { // htc sapphire does that
				sendOptionsResponse(response);
				return;
			}

			final ActiveSyncRequest asrequest = getActiveSyncRequest(request);
			/*Marker asXmlRequestMarker = TechnicalLogType.HTTP_REQUEST.getMarker();
			logger.debug(asXmlRequestMarker, asrequest.getHttpServletRequest().toString());*/
			Credentials creds = performAuthentication(asrequest);

			getLoggerService().initSession(creds.getUser(), c.getReqId(), asrequest.getCommand());

			String policy = asrequest.getMsPolicyKey();
			if (policy != null && policy.equals("0") && !asrequest.getCommand().equals("Provision")) {
				logger.debug("forcing device (ua: {}) provisioning", asrequest.getUserAgent());
				response.setStatus(449);
				return;
			} else {
				logger.debug("policy used = {}", policy);
			}

			processActiveSyncMethod(c, creds, asrequest.getDeviceId(), asrequest, response);
			
		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} catch (DaoException e) {
			logger.error(e.getMessage(), e);
		} catch (AuthFault e) {
			logger.warn(e.getMessage(), e);
			returnHttpUnauthorized(request, response);
		} catch (BadRequestException e) {
			logger.warn(e.getMessage());
			returnHttpUnauthorized(request, response);
		} finally {
			getLoggerService().closeSession();
		}
	}

	private void handleContinuation(HttpServletResponse response, IContinuation c) {
		BackendSession bs = c.getBackendSession();

		IListenerRegistration reg = c.getListenerRegistration();
		if (reg != null) {
			reg.cancel();
		}

		if (bs == null) {
			return;
		}
		getLoggerService().initSession(bs.getUser(), c.getReqId(), bs.getCommand());
		logger.debug("continuation");
		IContinuationHandler ph = c.getLastContinuationHandler();
		ICollectionChangeListener ccl = c.getCollectionChangeListener();
		Responder responder = responderFactory.createResponder(response);
		if (c.isError()) {
			ph.sendError(responder, c.getErrorStatus(), c);
		} else if (ccl != null) {
			ph.sendResponseWithoutHierarchyChanges(bs, responder, c);
		}
	}
	
	private Credentials performAuthentication(ActiveSyncRequest request) throws AuthFault, DaoException, BadRequestException {
		Credentials credentials = authentication(request.getHttpServletRequest());
		
		String deviceId = request.getDeviceId();
		String deviceType = request.getDeviceType();
		String userAgent = request.getUserAgent();
		
		boolean initDevice = deviceService.initDevice(credentials.getUser(), deviceId, deviceType, userAgent);
		boolean syncAutho = deviceService.syncAuthorized(credentials.getUser(), deviceId);
		if (initDevice && syncAutho) {
		
			logger.debug("login/password ok & the device has been authorized");
			return credentials;
		} else {
			throw new AuthFault("The device has not been authorized");
		}
	}

	private void processActiveSyncMethod(IContinuation continuation,
			Credentials credentials, String devId,
			ActiveSyncRequest request, HttpServletResponse response)
			throws IOException, DaoException {

		BackendSession bs = sessionService.getSession(credentials, devId, request);
		logger.debug("incoming query");
		
		if (bs.getCommand() == null) {
			logger.warn("POST received without explicit command, aborting");
			return;
		}

		IRequestHandler rh = getHandler(bs);
		if (rh == null) {
			noHandlerError(request, bs);
			return;
		}

		sendASHeaders(response);
		Responder responder = responderFactory.createResponder(response);
		rh.process(continuation, bs, request, responder);
	}
	
	private ActiveSyncRequest getActiveSyncRequest(HttpServletRequest r) {
		String qs = r.getQueryString();
		if (qs.contains("Cmd=")) {
			return new SimpleQueryString(r);
		} else {
			InputStream is = null;
			try {
				is = r.getInputStream();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			return new Base64QueryString(r, is);
		}
	}

	private void noHandlerError(ActiveSyncRequest request, BackendSession bs) {
		logger.warn("no handler for command = {}", bs.getCommand());
		Enumeration<?> heads = request.getHttpServletRequest().getHeaderNames();
		while (heads.hasMoreElements()) {
			String h = (String) heads.nextElement();
			logger.warn("{} : {}", h, request.getHeader(h));
		}
	}

	/**
	 * 
	 * HTTP/1.1 200 OK
	 * Connection: Keep-Alive
	 * Content-Length: 1069
	 * Date: Mon, 01 May 2006 20:15:15 GMT
	 * Content-Type: application/vnd.ms-sync.wbxml
	 * Server: Microsoft-IIS/6.0
	 * X-Powered-By: ASP.NET
	 * X-AspNet-Version: 2.0.50727
	 * S-Server-ActiveSync: 8.0
	 * Cache-Control: private
	 * 
	 */
	private void sendASHeaders(HttpServletResponse response) {
		response.setHeader("Server", "Microsoft-IIS/6.0");
		response.setHeader("MS-Server-ActiveSync", "8.1");
		response.setHeader("Cache-Control", "private");
	}

	private void sendOptionsResponse(HttpServletResponse response) {
		response.setStatus(200);
		response.setHeader("Server", "Microsoft-IIS/6.0");
		response.setHeader("MS-Server-ActiveSync", "8.1");
		response.setHeader("MS-ASProtocolVersions", "1.0,2.0,2.1,2.5,12.0,12.1");
		response.setHeader(
				"MS-ASProtocolCommands",
				"Sync,SendMail,SmartForward,SmartReply,GetAttachment,GetHierarchy,CreateCollection,DeleteCollection,MoveCollection,FolderSync,FolderCreate,FolderDelete,FolderUpdate,MoveItems,GetItemEstimate,MeetingResponse,Search,Settings,Ping,ItemOperations,Provision,ResolveRecipients,ValidateCert");
		response.setHeader("Public", "OPTIONS,POST");
		response.setHeader("Allow", "OPTIONS,POST");
		response.setHeader("Cache-Control", "private");
		response.setContentLength(0);
	}

	private IRequestHandler getHandler(BackendSession p) {
		return handlers.getHandler(p.getCommand());
	}


}
