package org.obm.sync.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.obm.sync.XTrustProvider;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.MavenVersion;
import org.obm.sync.client.ISyncClient;
import org.obm.sync.locators.Locator;
import org.obm.sync.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public abstract class AbstractClientImpl implements ISyncClient {

	private static final int MAX_CONNECTIONS = 8;
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final SyncClientException exceptionFactory;

	protected HttpClient hc;
	
	protected abstract Locator getLocator();
	
	static {
		XTrustProvider.install();
	}

	private static final HttpMethodRetryHandler retryH = new HttpMethodRetryHandler() {
		public boolean retryMethod(HttpMethod arg0, IOException arg1, int arg2) {
			return false;
		}
	};

	protected AbstractClientImpl(SyncClientException exceptionFactory) {
		this.exceptionFactory = exceptionFactory;
		this.hc = createHttpClient();
	}

	private static HttpClient createHttpClient() {
		MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = 
				new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setMaxTotalConnections(MAX_CONNECTIONS);
		params.setDefaultMaxConnectionsPerHost(MAX_CONNECTIONS);
		multiThreadedHttpConnectionManager.setParams(params);
		HttpClient ret = new HttpClient(multiThreadedHttpConnectionManager);
		ret.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryH);
		return ret;
	}

	protected Document execute(AccessToken token, String action,
			Multimap<String, String> parameters) {
		
		PostMethod pm = new PostMethod(getBackendUrl(token.getUserWithDomain()) + action);
		pm.setRequestHeader("Content-Type",
				"application/x-www-form-urlencoded; charset=utf-8");
		try {
			InputStream is = executeStream(pm, parameters);
			if (is != null) {
				return DOMUtils.parse(is);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			pm.releaseConnection();
		}
		return null;
	}

	protected void setToken(Multimap<String, String> parameters, AccessToken token) {
		if (token != null) {
			parameters.put("sid", token.getSessionId());
		}
	}

	protected Multimap<String, String> initParams(AccessToken at) {
		Multimap<String, String> m = ArrayListMultimap.create();
		setToken(m, at);
		return m;
	}

	private InputStream executeStream(PostMethod pm, Multimap<String, String> parameters) {
		InputStream is = null;
		try {
			for (Entry<String, String> entry: parameters.entries()) {
				pm.setParameter(entry.getKey(), entry.getValue());
			}
			int ret = hc.executeMethod(pm);
			if (ret != HttpStatus.SC_OK) {
				logger.error("method failed:\n" + pm.getStatusLine() + "\n"
						+ pm.getResponseBodyAsString());
			} else {
				is = pm.getResponseBodyAsStream();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return is;
	}

	protected void executeVoid(AccessToken at, String action, Multimap<String, String> parameters) {
		PostMethod pm = new PostMethod(getBackendUrl(at.getUserWithDomain()) + action);
		pm.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		try {
			executeStream(pm, parameters);
		} finally {
			pm.releaseConnection();
		}
	}

	public AccessToken login(String loginAtDomain, String password, String origin) {
		Multimap<String, String> params = ArrayListMultimap.create();
		params.put("login", loginAtDomain);
		params.put("password", password);
		params.put("origin", origin);

		AccessToken token = new AccessToken(0, 0, origin);
		token.setUser(loginAtDomain.split("@", 2)[0]);
		token.setDomain(loginAtDomain.split("@", 2)[1]);
		
		Document doc = execute(token, "/login/doLogin", params);
		Element root = doc.getDocumentElement();
		String sid = DOMUtils.getElementText(root, "sid");
		Element v = DOMUtils.getUniqueElement(root, "version");
		MavenVersion version = new MavenVersion();
		if (v != null) {
			version.setMajor(v.getAttribute("major"));
			version.setMinor(v.getAttribute("minor"));
			version.setRelease(v.getAttribute("release"));
		}
		token.setSessionId(sid);
		token.setVersion(version);
		return token;
	}

	public void logout(AccessToken at) {
		Multimap<String, String> params = initParams(at);
		executeVoid(at, "/login/doLogout", params);
	}
	
	private String getBackendUrl(String loginAtDomain) {
		Locator locator = getLocator();
		return locator.backendUrl(loginAtDomain);
	}

}
