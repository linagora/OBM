package org.obm.push;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.naming.ConfigurationException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IBackendFactory;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.impl.ActiveSyncRequest;
import org.obm.push.impl.Credentials;
import org.obm.push.impl.FolderSyncHandler;
import org.obm.push.impl.GetAttachmentHandler;
import org.obm.push.impl.GetItemEstimateHandler;
import org.obm.push.impl.HintsLoader;
import org.obm.push.impl.IContinuationHandler;
import org.obm.push.impl.IRequestHandler;
import org.obm.push.impl.ItemOperationsHandler;
import org.obm.push.impl.MeetingResponseHandler;
import org.obm.push.impl.MoveItemsHandler;
import org.obm.push.impl.PingHandler;
import org.obm.push.impl.ProvisionHandler;
import org.obm.push.impl.PushContinuation;
import org.obm.push.impl.Responder;
import org.obm.push.impl.SearchHandler;
import org.obm.push.impl.SendMailHandler;
import org.obm.push.impl.SettingsHandler;
import org.obm.push.impl.SmartForwardHandler;
import org.obm.push.impl.SmartReplyHandler;
import org.obm.push.logging.TechnicalLogType;
import org.obm.push.store.IStorageFactory;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.SyncHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.core.sift.AppenderTracker;
import com.google.inject.Injector;

/**
 * ActiveSync server implementation. Routes all request to appropriate request
 * handlers.
 * 
 */
public class ActiveSyncServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory
			.getLogger(ActiveSyncServlet.class);

	private Injector injector;

	public static final String SYNC_HANDLER = "Sync";
	public static final String PING_HANDLER = "Ping";

	private Map<String, IRequestHandler> handlers;
	private Map<String, BackendSession> sessions;

	private ISyncStorage storage;
	private IBackend backend;

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		IContinuation c = new PushContinuation(request);

		logger.debug(
				"query = {}, initial = {}, resume = {}, m = {}, num = {}",
				new Object[] { request.getQueryString(), c.isInitial(),
						c.isResumed(), request.getMethod(), c.getReqId() });

		if (c.isResumed() || !c.isInitial()) {
			BackendSession bs = c.getBackendSession();
			IContinuationHandler ph = null;

			IListenerRegistration reg = c.getListenerRegistration();
			if (reg != null) {
				reg.cancel();
			}

			if (bs == null) {
				return;
			}
			synchronized (handlers) {
				ph = (IContinuationHandler) handlers.get(bs
						.getLastContinuationHandler());
			}

			ICollectionChangeListener ccl = c.getCollectionChangeListener();
			if (c.isError()) {
				ph.sendError(new Responder(response),
						ccl.getDirtyCollections(), c.getErrorStatus(), c);
			} else if (ccl != null) {
				ph.sendResponseWithoutHierarchyChanges(bs, new Responder(
						response), ccl.getDirtyCollections(), c);
			}
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
		logger.info(asXmlRequestMarker, asrequest.getHttpServletRequest().toString());
		Credentials creds = performAuthentification(asrequest, response);
		if (creds == null) {
			return;
		}

		String policy = p(asrequest, "X-Ms-PolicyKey");
		if (policy != null && policy.equals("0")
				&& !p(asrequest, "Cmd").equals("Provision")) {

			logger.info("forcing device (ua: {}) provisioning",
					p(asrequest, "User-Agent"));
			response.setStatus(449);
			return;
		} else {
			logger.info("policy used = {}", policy);
		}

		processActiveSyncMethod(c, creds.getLoginAtDomain(),
				creds.getPassword(), p(asrequest, "DeviceId"), asrequest,
				response);

	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			super.init(config);
			
			injector = (Injector) config.getServletContext().getAttribute(
					GuiceServletContextListener.ATTRIBUTE_NAME);
			configureLogger();

			storage = injector.getInstance(IStorageFactory.class)
					.createStorage();
			backend = injector.getInstance(IBackendFactory.class).loadBackend();

			sessions = Collections
					.synchronizedMap(new HashMap<String, BackendSession>());
			handlers = Collections
					.synchronizedMap(new HashMap<String, IRequestHandler>());
			
			synchronized (handlers) {
				handlers.put("FolderSync",
						injector.getInstance(FolderSyncHandler.class));
				handlers.put("Sync", injector.getInstance(SyncHandler.class));
				handlers.put("GetItemEstimate",
						injector.getInstance(GetItemEstimateHandler.class));
				handlers.put("Provision",
						injector.getInstance(ProvisionHandler.class));
				handlers.put("Ping", injector.getInstance(PingHandler.class));
				handlers.put("Settings",
						injector.getInstance(SettingsHandler.class));
				handlers.put("Search",
						injector.getInstance(SearchHandler.class));
				handlers.put("SendMail",
						injector.getInstance(SendMailHandler.class));
				handlers.put("MoveItems",
						injector.getInstance(MoveItemsHandler.class));
				handlers.put("SmartReply",
						injector.getInstance(SmartReplyHandler.class));
				handlers.put("SmartForward",
						injector.getInstance(SmartForwardHandler.class));
				handlers.put("MeetingResponse",
						injector.getInstance(MeetingResponseHandler.class));
				handlers.put("GetAttachment",
						injector.getInstance(GetAttachmentHandler.class));
				handlers.put("ItemOperations",
						injector.getInstance(ItemOperationsHandler.class));
			}
			logger.info("Opush ActiveSync servlet initialised.");
		} catch (ConfigurationException e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Checks authentification headers. Returns non null value if login/password
	 * is valid & the device has been authorized.
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	private Credentials performAuthentification(ActiveSyncRequest request,
			HttpServletResponse response) {
		Credentials creds = null;
		boolean valid = false;
		Marker asXmlRequestMarker = TechnicalLogType.HTTP_REQUEST.getMarker();
		logger.info(asXmlRequestMarker, request.getHttpServletRequest().toString());
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
						String loginAtDomain = getLoginAtDomain(userId);
						String deviceId = p(request, "DeviceId");
						valid = storage.initDevice(loginAtDomain, deviceId,
								extractDeviceType(request))
								&& validatePassword(loginAtDomain, password)
								&& storage.syncAuthorized(loginAtDomain,
										deviceId);
						if (valid) {
							initLoggerSession(loginAtDomain);
							
							logger.info("login/password ok & the device has been authorized");
							return new Credentials(loginAtDomain, password);
						}
					}
				}
			}
		}

		if (!valid) {
			String uri = request.getHttpServletRequest().getMethod() + " "
					+ request.getHttpServletRequest().getRequestURI() + " "
					+ request.getHttpServletRequest().getQueryString();

			logger.warn("invalid auth, sending http 401 ( uri = {} )", uri);
			String s = "Basic realm=\"OBMPushService\"";
			response.setHeader("WWW-Authenticate", s);
			response.setStatus(401);
		}
		return creds;
	}

	private void initLoggerSession(String loginAtDomain) {
		Calendar date = Calendar.getInstance();
		SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy.MM.dd_hh:mm:ss");
		String now = dateformatter.format(date.getTime());

		closePrecedentLogFile();
		String sessionId = loginAtDomain+"-"+now;
		configureLogger("user", loginAtDomain);
		configureLogger("sessionId", sessionId);
	}

	private String extractDeviceType(ActiveSyncRequest request) {
		String ret = p(request, "DeviceType");
		if (ret.startsWith("IMEI")) {
			ret = p(request, "User-Agent");
		}
		return ret;
	}

	/**
	 * Parameters can be in query string or in header, whether a base64 query
	 * string is used.
	 * 
	 * @param r
	 * @param name
	 * @return
	 */
	private String p(ActiveSyncRequest r, String name) {
		String ret = r.getParameter(name);
		if (ret == null) {
			ret = r.getHeader(name);
		}
		return ret;
	}

	private void processActiveSyncMethod(IContinuation continuation,
			String userID, String password, String devId,
			ActiveSyncRequest request, HttpServletResponse response)
			throws IOException {

		BackendSession bs = getSession(userID, password, devId, request);
		logger.info("activeSyncMethod = {}", bs.getCommand());
		String proto = p(request, "MS-ASProtocolVersion");
		if (proto == null) {
			proto = "12.1";
		}

		try {
			bs.setProtocolVersion(Double.parseDouble(proto));
			logger.info("Client supports protocol = {}", proto);
		} catch (NumberFormatException nfe) {
			logger.warn("invalid MS-ASProtocolVersion = {}", proto);
			bs.setProtocolVersion(12.1);
		}

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
		rh.process(continuation, bs, request, new Responder(response));
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

	@SuppressWarnings("rawtypes")
	private void noHandlerError(ActiveSyncRequest request, BackendSession bs) {
		logger.warn("no handler for command = {}", bs.getCommand());
		Enumeration heads = request.getHttpServletRequest().getHeaderNames();
		while (heads.hasMoreElements()) {
			String h = (String) heads.nextElement();
			logger.warn("{} : {}", h, request.getHeader(h));
		}
	}

	private BackendSession getSession(String userID, String password,
			String devId, ActiveSyncRequest r) {

		String uid = getLoginAtDomain(userID);
		String sessionId = uid + "/" + devId;

		BackendSession bs = null;
		synchronized (sessions) {
			if (sessions.containsKey(sessionId)) {
				bs = sessions.get(sessionId);
				bs.setPassword(password);
				logger.info("Existing session = {} | {} ", bs,
						bs.getLastMonitored());
				bs.setCommand(p(r, "Cmd"));
			} else {
				bs = new BackendSession(uid, password, p(r, "DeviceId"),
						extractDeviceType(r), p(r, "Cmd"));
				sessions.put(sessionId, bs);
				new HintsLoader().addHints(r, bs);
				logger.info("New session = {}", sessionId);
			}
			return bs;
		}
	}

	private String getLoginAtDomain(String userID) {
		String uid = userID;
		String domain = null;
		int idx = uid.indexOf("\\");
		if (idx > 0) {
			domain = uid.substring(0, idx);
			if (!uid.contains("@")) {
				uid = uid.substring(idx + 1) + "@" + domain;
			} else {
				uid = uid.substring(idx + 1);
			}
		}
		uid = uid.toLowerCase();
		return uid;
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
		synchronized (handlers) {
			return handlers.get(p.getCommand());
		}
	}

	private boolean validatePassword(String loginAtDomain, String password) {
		return backend.validatePassword(loginAtDomain, password);
	}

	private String getLastSessionLogFileName(){
		String sessionId = MDC.get("sessionId");
		if(sessionId == null) {
			return "no-session";
		} else {
			return sessionId;
		}
	}

	private void closePrecedentLogFile(){
		String logFileName = getLastSessionLogFileName();
		if (logFileName != null) {

			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			SiftingAppender siftingAppender = (SiftingAppender) loggerContext
														.getLogger(Logger.ROOT_LOGGER_NAME)
														.getAppender("SIFTING");
			if (siftingAppender != null) {
				AppenderTracker<?> appenderTracker = siftingAppender.getAppenderTracker();
				appenderTracker.stopAndRemoveNow(logFileName);
			}
		}
	}

	private void configureLogger(String key, String value) {
		MDC.put(key, value);
		configureLogger();
	}

	private void configureLogger() {
		MDC.put("title", "Opush ActiveSync");
	}

}
