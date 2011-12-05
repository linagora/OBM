package org.obm.sync.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.obm.locator.LocatorClientException;
import org.obm.sync.XTrustProvider;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.MavenVersion;
import org.obm.sync.client.ISyncClient;
import org.obm.sync.client.exception.ObmSyncClientException;
import org.obm.sync.locators.Locator;
import org.obm.sync.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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

	protected Document execute(AccessToken token, String action, Multimap<String, String> parameters) {
		PostMethod pm = null;
		try {
			pm = getPostMethod(token, action);
			InputStream is = executePostAndGetResultStream(pm, parameters);
			if (is != null) {
				return DOMUtils.parse(is);
			} else {
				throw new ObmSyncClientException("An error occurs: cannot get the request result stream");
			}
		} catch (LocatorClientException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} catch (SAXException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} catch (FactoryConfigurationError e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} finally {
			releaseConnection(pm);
		}
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

	private InputStream executePostAndGetResultStream(PostMethod pm, Multimap<String, String> parameters) throws HttpException, IOException {
		InputStream is = null;
		setPostMethodParameters(pm, parameters);
		int httpResultStatus = hc.executeMethod(pm);
		if (isHttpStatusOK(httpResultStatus)) {
			is = pm.getResponseBodyAsStream();
		} else {
			logger.error("method failed:\n" + pm.getStatusLine() + "\n"
					+ pm.getResponseBodyAsString());
		}
		return is;
	}

	private boolean isHttpStatusOK(int httpResultStatus) {
		return httpResultStatus == HttpStatus.SC_OK;
	}

	private void setPostMethodParameters(PostMethod pm, Multimap<String, String> parameters) {
		for (Entry<String, String> entry: parameters.entries()) {
			pm.setParameter(entry.getKey(), entry.getValue());
		}
	}

	protected void executeVoid(AccessToken at, String action, Multimap<String, String> parameters) {
		PostMethod pm = null; 
		try {
			pm = getPostMethod(at, action);
			executePostAndGetResultStream(pm, parameters);
		} catch (LocatorClientException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} catch (HttpException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} finally {
			releaseConnection(pm);
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
		String email = DOMUtils.getElementText(root, "email");
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
		token.setEmail(email);
		return token;
	}

	public void logout(AccessToken at) {
		Multimap<String, String> params = initParams(at);
		executeVoid(at, "/login/doLogout", params);
	}
	
	private String getBackendUrl(String loginAtDomain) throws LocatorClientException {
		Locator locator = getLocator();
		return locator.backendUrl(loginAtDomain);
	}
	
	private PostMethod getPostMethod(AccessToken at, String action) throws LocatorClientException {
		String backendUrl = getBackendUrl(at.getUserWithDomain());
		PostMethod pm = new PostMethod(backendUrl + action );
		pm.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		return pm;
	}
	
	private void releaseConnection(PostMethod pm){
		if (pm != null) {
			pm.releaseConnection();
		}
	}

}
