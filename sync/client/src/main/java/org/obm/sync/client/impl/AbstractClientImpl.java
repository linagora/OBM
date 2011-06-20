package org.obm.sync.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.XTrustProvider;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.MavenVersion;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.ISyncClient;
import org.obm.sync.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Utility methods for client implementations
 * 
 * @author tom
 * 
 */
public abstract class AbstractClientImpl implements ISyncClient {

	protected HttpClient hc;
	protected Log logger = LogFactory.getLog(getClass());
	private String url;

	static {
		XTrustProvider.install();
	}

	private static final HttpMethodRetryHandler retryH = new HttpMethodRetryHandler() {
		public boolean retryMethod(HttpMethod arg0, IOException arg1, int arg2) {
			return false;
		}
	};

	protected AbstractClientImpl(String backendUrl, HttpClient cli) {
		this.hc = cli;
		this.url = backendUrl;
	}

	protected AbstractClientImpl(String backendUrl) {
		this(backendUrl, createHttpClient());
	}

	private static HttpClient createHttpClient() {
		HttpClient ret = new HttpClient();
		ret.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryH);
		return ret;
	}

	protected synchronized Document execute(String action,
			Multimap<String, String> parameters) {
		PostMethod pm = new PostMethod(url + action);
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

	private class PMInputStream extends InputStream {

		private InputStream delegate;
		private PostMethod pm;

		public PMInputStream(InputStream delegate, PostMethod pm) {
			this.delegate = delegate;
			this.pm = pm;
		}

		@Override
		public int read() throws IOException {
			return delegate.read();
		}

		@Override
		public void close() throws IOException {
			super.close();
			pm.releaseConnection();
		}

	}

	protected synchronized InputStream executeStream(String action,
			Multimap<String, String> parameters) {
		PostMethod pm = new PostMethod(url + action);
		pm.setRequestHeader("Content-Type",
				"application/x-www-form-urlencoded; charset=utf-8");
		try {
			return new PMInputStream(executeStream(pm, parameters), pm);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
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

	private synchronized InputStream executeStream(PostMethod pm,
			Multimap<String, String> parameters) {
		InputStream is = null;
		try {
			for (Entry<String, String> entry: parameters.entries()) {
				pm.setParameter(entry.getKey(), entry.getValue());
			}
			int ret = 0;
			synchronized (hc) {
				ret = hc.executeMethod(pm);
			}
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

	protected synchronized void executeVoid(String action,
			Multimap<String, String> parameters) {
		PostMethod pm = new PostMethod(url + action);
		pm.setRequestHeader("Content-Type",
				"application/x-www-form-urlencoded; charset=utf-8");
		executeStream(pm, parameters);
		pm.releaseConnection();
	}

	public AccessToken login(String login, String password, String origin) {
		Multimap<String, String> params = ArrayListMultimap.create();
		params.put("login", login);
		params.put("password", password);
		params.put("origin", origin);

		Document doc = execute("/login/doLogin", params);
		Element root = doc.getDocumentElement();
		String sid = DOMUtils.getElementText(root, "sid");
		Element v = DOMUtils.getUniqueElement(root, "version");
		MavenVersion version = new MavenVersion();
		if (v != null) {
			version.setMajor(v.getAttribute("major"));
			version.setMinor(v.getAttribute("minor"));
			version.setRelease(v.getAttribute("release"));
		}
		AccessToken token = new AccessToken(0, 0, origin);
		token.setSessionId(sid);
		token.setUser(login.split("@", 2)[0]);
		token.setVersion(version);

		return token;
	}

	public void logout(AccessToken at) {
		Multimap<String, String> params = initParams(at);
		executeVoid("/login/doLogout", params);
	}

	protected void checkServerError(Document doc) throws ServerFault {
		if (documentIsError(doc)) {
			String message = DOMUtils.getElementText(doc.getDocumentElement(), "message");
			String type = DOMUtils.getElementText(doc.getDocumentElement(), "type");
			if(EventAlreadyExistException.class.getName().equals(type)){
				throw new EventAlreadyExistException(message);
			} else {
				throw new ServerFault(message);
			}
		}
	}

	protected boolean documentIsError(Document doc) {
		return doc.getDocumentElement().getNodeName().equals("error");
	}

}
