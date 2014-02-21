/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.obm.annotations.transactional.Transactional;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.configuration.LoggerModule;
import org.obm.push.exception.AuthenticationException;
import org.obm.push.exception.DaoException;
import org.obm.push.handler.IContinuationHandler;
import org.obm.push.handler.IRequestHandler;
import org.obm.push.impl.Responder;
import org.obm.push.impl.ResponderImpl;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.resource.ResourcesService;
import org.obm.push.service.DeviceService;
import org.obm.push.technicallog.bean.KindToBeLogged;
import org.obm.push.technicallog.bean.TechnicalLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * ActiveSync server implementation. Routes all request to appropriate request
 * handlers.
 */
@Singleton
public class ActiveSyncServlet extends HttpServlet {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static final String MS_SERVER_ACTIVESYNC = "14.1";
	private static final String MS_ASPROTOCOL_VERSIONS = "12.0,12.1";
	
	private final Handlers handlers;
	private final SessionService sessionService;
	private final DeviceService deviceService;
	
	private final ResponderImpl.Factory responderFactory;
	private final IBackend backend;
	private final LoggerService loggerService;
	private final Logger authLogger;
	private final HttpErrorResponder httpErrorResponder;

	private final PolicyService policyService;
	private final Set<ResourcesService> resourcesServices;

	@Inject
	@VisibleForTesting ActiveSyncServlet(SessionService sessionService, 
			IBackend backend, DeviceService deviceService,
			PolicyService policyService,
			ResponderImpl.Factory responderFactory, Handlers handlers,
			LoggerService loggerService, @Named(LoggerModule.AUTH)Logger authLogger,
			HttpErrorResponder httpErrorResponder,
			Set<ResourcesService> resourcesServices) {
	
		super();
		
		this.sessionService = sessionService;
		this.backend = backend;
		this.deviceService = deviceService;
		this.policyService = policyService;
		this.responderFactory = responderFactory;
		this.handlers = handlers;
		this.loggerService = loggerService;
		this.authLogger = authLogger;
		this.httpErrorResponder = httpErrorResponder;
		this.resourcesServices = resourcesServices;
	}

	@Override
	public void init() throws ServletException {
		super.init();
		backend.startMonitoring();
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		sendOptionsResponse(response);
	}
	
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		sendOptionsResponse(response);
	}
	
	@Override
	@Transactional
	@TechnicalLogging(kindToBeLogged=KindToBeLogged.TRANSACTION, onStartOfMethod=true, onEndOfMethod=true)
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		try {
			IContinuation continuation = (IContinuation) request.getAttribute(RequestProperties.CONTINUATION);
			if (continuation == null) {
				throw new IllegalStateException("Requests must be handled by " + PushContinuationFilter.class.getSimpleName());
			}
			
			logger.debug(
					"query = {}, m = {}, num = {}",
					request.getQueryString(), request.getMethod(), continuation.getReqId());

			if (continuation.needsContinuationHandling()) {
				handleContinuation(request, response, continuation);
				return;
			}

			/*Marker asXmlRequestMarker = TechnicalLogType.HTTP_REQUEST.getMarker();
			logger.debug(asXmlRequestMarker, asrequest.getHttpServletRequest().toString());*/
			Credentials credentials = (Credentials) request.getAttribute(RequestProperties.CREDENTIALS);
			if (credentials == null) {
				throw new IllegalStateException("Credentials must be handled by " + AuthenticationFilter.class.getSimpleName());
			}
			
			final ActiveSyncRequest asrequest = getActiveSyncRequest(request);

			checkAuthorizedDevice(asrequest, credentials);

			if (policyService.needProvisionning(asrequest, credentials.getUser())) {
				logger.debug("forcing device (ua: {}) provisioning", asrequest.getUserAgent());
				sendNeedProvisionningResponse(response);
				return;
			} else {
				logger.debug("policy used = {}", asrequest.getMsPolicyKey());
			}

			processActiveSyncMethod(continuation, credentials, asrequest.getDeviceId(), asrequest, response, request);
		
		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} catch (AuthenticationException e) {
			logger.info(e.getMessage());
			httpErrorResponder.returnHttpUnauthorized(request, response);
		}
	}

	private void handleContinuation(HttpServletRequest request, HttpServletResponse response, IContinuation c) {
		UserDataRequest udr = c.getUserDataRequest();

		IListenerRegistration reg = c.getListenerRegistration();
		if (reg != null) {
			reg.cancel();
		}

		if (udr == null) {
			return;
		}
		loggerService.startSession(udr.getUser(), c.getReqId(), udr.getCommand());
		logger.debug("continuation");
		IContinuationHandler ph = c.getLastContinuationHandler();
		ICollectionChangeListener ccl = c.getCollectionChangeListener();
		Responder responder = responderFactory.createResponder(request, response);
		if (c.isError()) {
			ph.sendError(udr.getDevice(), responder, c.getErrorStatus(), c);
		} else if (ccl != null) {
			ph.sendResponseWithoutHierarchyChanges(udr, responder, c);
		} else {
			throw new IllegalStateException("Looks like this continuation can't be handled");
		}
	}
	
	private void checkAuthorizedDevice(ActiveSyncRequest request, Credentials credentials) throws AuthenticationException, DaoException {
		DeviceId deviceId = request.getDeviceId();
		String deviceType = request.getDeviceType();
		String userAgent = request.getUserAgent();
		ProtocolVersion protocolVersion = ProtocolVersion.fromSpecificationValue(request.getMSASProtocolVersion());
		
		deviceService.initDevice(credentials.getUser(), deviceId, deviceType, userAgent, protocolVersion);
		boolean syncAutho = deviceService.syncAuthorized(credentials.getUser(), deviceId);
		if (syncAutho) {
			authLogger.info("Authentication success [login:{}], the device [type:{}] has been authorized.", 
					credentials.getUser().getEmail(), deviceType);
		} else {
			throw new AuthenticationException("The device has not been authorized");
		}
	}

	private void processActiveSyncMethod(IContinuation continuation,
			Credentials credentials, DeviceId devId,
			ActiveSyncRequest request, 
			HttpServletResponse response, 
			HttpServletRequest servletRequest)
					throws IOException, DaoException {

		UserDataRequest userDataRequest = null;
		Responder responder = null;
		try {
			userDataRequest = sessionService.getSession(credentials, devId, request);
			for (ResourcesService resourcesService: resourcesServices) {
				resourcesService.initRequest(userDataRequest, servletRequest);
			}
			logger.debug("incoming query");
			
			if (userDataRequest.getCommand() == null) {
				logger.warn("POST received without explicit command, aborting");
				return;
			}
	
			IRequestHandler rh = getHandler(userDataRequest);
			if (rh == null) {
				noHandlerError(request, userDataRequest);
				return;
			}
	
			sendASHeaders(response);
			responder = responderFactory.createResponder(request.getHttpServletRequest(), response);
			rh.process(continuation, userDataRequest, request, responder);
		} finally {
			if (userDataRequest != null) {
				for (ResourcesService resourcesService: resourcesServices) {
					resourcesService.closeResources(userDataRequest);
				}
			}
		}
	}
	
	private ActiveSyncRequest getActiveSyncRequest(HttpServletRequest r) {
		ActiveSyncRequest activeSyncRequest = (ActiveSyncRequest) r.getAttribute(RequestProperties.ACTIVE_SYNC_REQUEST);
		if (activeSyncRequest == null) {
			throw new IllegalStateException("Unable to retrieve ActiveSync request from Servlet attributes, make sure to install " + ActiveSyncRequestFilter.class);
		}
		return activeSyncRequest;
	}

	private void noHandlerError(ActiveSyncRequest request, UserDataRequest udr) {
		logger.warn("no handler for command = {}", udr.getCommand());
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
		response.setHeader("Server", "Microsoft-IIS/7.5");
		response.setHeader("MS-Server-ActiveSync", MS_SERVER_ACTIVESYNC);
		response.setHeader("Cache-Control", "private");
	}

	private void sendASSuppotedCommands(HttpServletResponse response) {
		response.setHeader("MS-ASProtocolCommands",
				"Sync,SendMail,SmartForward,SmartReply,GetAttachment,GetHierarchy,CreateCollection,DeleteCollection,MoveCollection,FolderSync,FolderCreate,FolderDelete,FolderUpdate,MoveItems,GetItemEstimate,MeetingResponse,Search,Settings,Ping,ItemOperations,Provision,ResolveRecipients,ValidateCert");
	}

	private void sendOptionsResponse(HttpServletResponse response) {
		sendASHeaders(response);
		sendASSuppotedCommands(response);
		response.setHeader("MS-ASProtocolVersions", MS_ASPROTOCOL_VERSIONS);
		response.setHeader("Public", "OPTIONS,POST");
		response.setHeader("Allow", "OPTIONS,POST");
		response.setContentLength(0);
		response.setStatus(200);
	}


	private void sendNeedProvisionningResponse(HttpServletResponse response) {
		sendASHeaders(response);
		response.setHeader("Content-type", "text/html");
		response.setStatus(449);
	}

	private IRequestHandler getHandler(UserDataRequest p) {
		return handlers.getHandler(p.getCommand());
	}
}
