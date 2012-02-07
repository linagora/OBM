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
import java.security.InvalidParameterException;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.obm.annotations.transactional.Transactional;
import org.obm.configuration.module.LoggerModule;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.exception.DaoException;
import org.obm.push.handler.FolderSyncHandler;
import org.obm.push.handler.GetAttachmentHandler;
import org.obm.push.handler.GetItemEstimateHandler;
import org.obm.push.handler.IContinuationHandler;
import org.obm.push.handler.IRequestHandler;
import org.obm.push.handler.ItemOperationsHandler;
import org.obm.push.handler.MeetingResponseHandler;
import org.obm.push.handler.MoveItemsHandler;
import org.obm.push.handler.PingHandler;
import org.obm.push.handler.ProvisionHandler;
import org.obm.push.handler.SearchHandler;
import org.obm.push.handler.SendMailHandler;
import org.obm.push.handler.SettingsHandler;
import org.obm.push.handler.SmartForwardHandler;
import org.obm.push.handler.SmartReplyHandler;
import org.obm.push.handler.SyncHandler;
import org.obm.push.impl.PushContinuation;
import org.obm.push.impl.PushContinuation.Factory;
import org.obm.push.impl.Responder;
import org.obm.push.impl.ResponderImpl;
import org.obm.push.protocol.logging.TechnicalLogType;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.protocol.request.Base64QueryString;
import org.obm.push.protocol.request.SimpleQueryString;
import org.obm.push.service.DeviceService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * ActiveSync server implementation. Routes all request to appropriate request
 * handlers.
 * 
 */
@Singleton
public class ActiveSyncServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(ActiveSyncServlet.class);
	
	private Injector injector;
	private Map<String, IRequestHandler> handlers;
	private SessionService sessionService;
	private LoggerService loggerService; 
	private IBackend backend;
	private PushContinuation.Factory continuationFactory;
	private DeviceService deviceService;
	private final User.Factory userFactory;

	private final org.obm.push.impl.ResponderImpl.Factory responderFactory;
	private final Logger authLogger;

	
	@Inject
	protected ActiveSyncServlet(SessionService sessionService, LoggerService loggerService,
			IBackend backend, Factory continuationFactory,
			DeviceService deviceService, User.Factory userFactory,
			ResponderImpl.Factory responderFactory, @Named(LoggerModule.AUTH)Logger authLogger) {
		super();
		this.sessionService = sessionService;
		this.loggerService = loggerService;
		this.backend = backend;
		this.continuationFactory = continuationFactory;
		this.deviceService = deviceService;
		this.userFactory = userFactory;
		this.responderFactory = responderFactory;
		this.authLogger = authLogger;
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
			Marker asXmlRequestMarker = TechnicalLogType.HTTP_REQUEST.getMarker();
			logger.debug(asXmlRequestMarker, asrequest.getHttpServletRequest().toString());
			Credentials creds = performAuthentification(asrequest, response);
			if (creds == null) {
				return;
			}

			loggerService.initSession(creds.getUser(), c.getReqId(), asrequest.getCommand());

			String policy = asrequest.getMsPolicyKey();
			if (policy != null && policy.equals("0")
					&& !asrequest.getCommand().equals("Provision")) {

				logger.debug("forcing device (ua: {}) provisioning",
						asrequest.getUserAgent());
				response.setStatus(449);
				return;
			} else {
				logger.debug("policy used = {}", policy);
			}

			try {
				processActiveSyncMethod(c, 
						creds, 
						asrequest.getDeviceId(), 
						asrequest,
						response);
			} catch (DaoException e) {
				logger.error(e.getMessage(), e);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			loggerService.closeSession();
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
		loggerService.initSession(bs.getUser(), c.getReqId(), bs.getCommand());
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

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.injector = 
				(Injector) config
				.getServletContext()
				.getAttribute(GuiceServletContextListener.ATTRIBUTE_NAME);
		handlers = createHandlersMap();
	}

	private ImmutableMap<String, IRequestHandler> createHandlersMap() {
		return ImmutableMap.<String, IRequestHandler>builder()
				.put("FolderSync",		getInstance(FolderSyncHandler.class))
				.put("Sync", 			getInstance(SyncHandler.class))
				.put("GetItemEstimate", getInstance(GetItemEstimateHandler.class))
				.put("Provision", 		getInstance(ProvisionHandler.class))
				.put("Ping", 			getInstance(PingHandler.class))
				.put("Settings", 		getInstance(SettingsHandler.class))
				.put("Search", 			getInstance(SearchHandler.class))
				.put("SendMail", 		getInstance(SendMailHandler.class))
				.put("MoveItems", 		getInstance(MoveItemsHandler.class))
				.put("SmartReply", 		getInstance(SmartReplyHandler.class))
				.put("SmartForward", 	getInstance(SmartForwardHandler.class))
				.put("MeetingResponse",	getInstance(MeetingResponseHandler.class))
				.put("GetAttachment", 	getInstance(GetAttachmentHandler.class))
				.put("ItemOperations", 	getInstance(ItemOperationsHandler.class))
				.build();
	}

	private IRequestHandler getInstance(Class<? extends IRequestHandler> clazz) {
		return injector.getInstance(clazz);
	}

	
	/**
	 * Checks authentification headers. Returns non null value if login/password
	 * is valid & the device has been authorized.
	 */
	private Credentials performAuthentification(ActiveSyncRequest request,
			HttpServletResponse response) {

		Marker asXmlRequestMarker = TechnicalLogType.HTTP_REQUEST.getMarker();
		logger.debug(asXmlRequestMarker, request.getHttpServletRequest().toString());
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();
				if (basic.equalsIgnoreCase("Basic")) {
					String credentials = st.nextToken();
					String userPass = new String(
							Base64.decodeBase64(credentials));
					int p = userPass.indexOf(":");
					if (p != -1) {
						String userId = userPass.substring(0, p);
						String password = userPass.substring(p + 1);
						String deviceId = request.getDeviceId();
						String deviceType = request.getDeviceType();
						String userAgent = request.getUserAgent();
						try {
							return login(userId, password, deviceId, deviceType, userAgent);
						} catch (DaoException e) {
							logger.error("Database exception while authenticating user", e);
						} catch (InvalidParameterException e) {
							//will be logged later
						} catch (AuthFault e) {
							authLogger.info(e.getMessage());
						}
					}
				}
			}
		}
		returnHttpUnauthorized(request.getHttpServletRequest(), response);
		return null;
	}

	private Credentials login(String userId, String password, String deviceId, 
			String deviceType, String userAgent) throws DaoException, AuthFault {
		
		AccessToken accessToken = backend.login(userFactory.getLoginAtDomain(userId), password);
		User user = userFactory.createUser(userId, accessToken.getEmail());
		boolean initDevice = deviceService.initDevice(user, deviceId, deviceType, userAgent);
		boolean syncAutho = deviceService.syncAuthorized(user, deviceId);
		if (initDevice && syncAutho) {
			Credentials credentials = new Credentials(user, password);
			authLogger.info("Authentication success [login:{}], the device [type:{}] has been authorized.", 
					new Object[]{ credentials.getUser().getEmail(), deviceType });
			return credentials;
		} else {
			throw new AuthFault("The device has not been authorized");
		}
	}

	private void returnHttpUnauthorized(HttpServletRequest httpServletRequest,
			HttpServletResponse response) {

		authLogger.info("Invalid auth, sending http 401 ( uri = {}{}{} )",
				new Object[] { 
					httpServletRequest.getMethod(), 
					httpServletRequest.getRequestURI(), 
					httpServletRequest.getQueryString()});
		
		String s = "Basic realm=\"OBMPushService\"";
		response.setHeader("WWW-Authenticate", s);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
		return handlers.get(p.getCommand());
	}

}
