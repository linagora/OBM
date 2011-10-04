package org.obm.push;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.obm.annotations.transactional.Transactional;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

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

	
	@Inject
	protected ActiveSyncServlet(SessionService sessionService, LoggerService loggerService,
			IBackend backend, Factory continuationFactory,
			DeviceService deviceService) {
		super();
		this.sessionService = sessionService;
		this.loggerService = loggerService;
		this.backend = backend;
		this.continuationFactory = continuationFactory;
		this.deviceService = deviceService;
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

			loggerService.initSession(creds.getLoginAtDomain(), c.getReqId(), asrequest.getCommand());

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
		loggerService.initSession(bs.getLoginAtDomain(), c.getReqId(), bs.getCommand());
		logger.debug("continuation");
		IContinuationHandler ph = c.getLastContinuationHandler();
		ICollectionChangeListener ccl = c.getCollectionChangeListener();
		Responder responder = new ResponderImpl(response);
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
							String loginAtDomain = getLoginAtDomain(userId);
							boolean valid = deviceService.initDevice(loginAtDomain, deviceId, deviceType, userAgent)
									&& validatePassword(loginAtDomain, password)
									&& deviceService.syncAuthorized(loginAtDomain, deviceId);
							if (valid) {
								logger.debug("login/password ok & the device has been authorized");
								return new Credentials(loginAtDomain, password);
							}
						} catch (DaoException e) {
							logger.error("Database exception while authenticating user", e);
						} catch (InvalidParameterException e) {
							//will be logged later
						}
					}
				}
			}
		}
		returnHttpUnauthorized(request.getHttpServletRequest(), response);
		return null;
	}

	private void returnHttpUnauthorized(HttpServletRequest httpServletRequest,
			HttpServletResponse response) {

		logger.warn("invalid auth, sending http 401 ( uri = {}{}{} )", 
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
		Responder responder = new ResponderImpl(response);
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

	/* package */ String getLoginAtDomain(String userID) {
		Iterable<String> parts = splitOnSlashes(userID);
		User user = buildUserFromLoginParts(parts);
		if (user == null) {
			parts = splitOnAtSign(userID);
			user = buildUserFromLoginParts(parts);
		}
		if (user == null) {
			throw new InvalidParameterException();
		}
		return user.getLoginAtDomain();
	}

	private Iterable<String> splitOnSlashes(String userID) {
		Iterable<String> parts = Splitter.on("\\").split(userID);
		return parts;
	}

	private Iterable<String> splitOnAtSign(String userID) {
		Iterable<String> parts = Splitter.on("@").split(userID);
		return ImmutableList.copyOf(parts).reverse();
	}

	private User buildUserFromLoginParts(Iterable<String> parts) {
		int nbParts = Iterables.size(parts);
		if (nbParts > 2) {
			throw new InvalidParameterException();
		} else if (nbParts == 2) {
			Iterator<String> iterator = parts.iterator();
			String domainName = iterator.next();
			String userName = iterator.next();
			return new User(userName, domainName);
		}
		return null;
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

	private boolean validatePassword(String loginAtDomain, String password) {
		return backend.validatePassword(loginAtDomain, password);
	}
	
}
