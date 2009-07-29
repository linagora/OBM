package org.obm.caldav.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.obm.caldav.client.httpmethod.PropfindMethod;
import org.obm.caldav.client.httpmethod.ReportMethod;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;

public abstract class AbstractPushTest extends TestCase {

	private String url;
	private HttpClient hc;
	private String login;
	private String userAgent;
	private String password;
	private String authenticate = "";

	protected AbstractPushTest() {
		XTrustProvider.install();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.login = confValue("login");
		this.password = confValue("password");
		this.url = confValue("obm-caldav_url");

		this.hc = createHttpClient();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private HttpClient createHttpClient() {
		HttpClient ret = new HttpClient(
				new MultiThreadedHttpConnectionManager());
		HttpConnectionManagerParams mp = ret.getHttpConnectionManager()
				.getParams();
		mp.setDefaultMaxConnectionsPerHost(4);
		mp.setMaxTotalConnections(8);

		return ret;
	}

	protected InputStream loadDataFile(String name) {
		return AbstractPushTest.class.getClassLoader().getResourceAsStream(
				"data/" + name);
	}

	// PROPFIND /adrien@zz.com/event/ HTTP/1.1"
	protected Document propFindQuery(Document doc) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DOMUtils.serialise(doc, out);
		PropfindMethod pfq = new PropfindMethod(url);
		appendHeader(pfq, out);
		appendBody(pfq, out);
		Document ret = doRequest(pfq);
		if(ret == null && authenticate!=null && !"".equals(authenticate)){
			ret = propFindQuery(doc);
		}
		return ret; 
	}

	protected Document deleteQuery(Document doc) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DOMUtils.serialise(doc, out);
		DeleteMethod pfq = new DeleteMethod(url);
		appendHeader(pfq, out);
		return doRequest(pfq);
	}

	protected Document putQuery(Document doc) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DOMUtils.serialise(doc, out);
		PutMethod pfq = new PutMethod(url);
		appendHeader(pfq, out);
		appendBody(pfq, out);
		Document ret = doRequest(pfq);
		return ret;
	}

	protected Document reportQuery(Document doc) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DOMUtils.serialise(doc, out);
		ReportMethod pfq = new ReportMethod(url);
		appendHeader(pfq, out);
		appendBody(pfq, out);
		return doRequest(pfq);
	}

	private synchronized Document doRequest(HttpMethod hm) {
		Document xml = null;
		try {
			int ret = hc.executeMethod(hm);
			Header[] hs = hm.getResponseHeaders();
			for (Header h : hs) {
				System.err.println("head[" + h.getName() + "] => "
						+ h.getValue());
			}
			if (ret == HttpStatus.SC_UNAUTHORIZED) {
				UsernamePasswordCredentials upc = new UsernamePasswordCredentials(login, password);
				authenticate = hm.getHostAuthState().getAuthScheme().authenticate(upc, hm);
				return null;
			} else if (ret == HttpStatus.SC_OK || ret == HttpStatus.SC_MULTI_STATUS) {
				InputStream is = hm.getResponseBodyAsStream();
				xml = DOMUtils.parse(is);
				DOMUtils.logDom(xml);
			} else {
				System.err.println("method failed:\n" + hm.getStatusLine()
						+ "\n" + hm.getResponseBodyAsString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			hm.releaseConnection();
		}
		return xml;
	}

	@SuppressWarnings("deprecation")
	private void appendBody(EntityEnclosingMethod hm, ByteArrayOutputStream out) {
		hm.setRequestBody(new ByteArrayInputStream(out.toByteArray()));
	}

	private void appendHeader(HttpMethod hm, ByteArrayOutputStream out) {
		hm.setRequestHeader("Host", "lemurien.tlse.lng:8008");
		hm.setRequestHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.22) Gecko/20090608 Lightning/0.9 Thunderbird/2.0.0.22");
		hm.setRequestHeader("Accept", "text/xml");
		hm.setRequestHeader("Accept-Charset", "utf-8,*;q=0.1");
		hm.setRequestHeader("Content-Length", "" + out.size());
		hm.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
		hm.setRequestHeader("Depth", "0");
		if(authenticate != null && !"".equals(authenticate)){
			hm.setRequestHeader("Authorization", authenticate);
		}
	}

	private String confValue(String key) {
		InputStream is = getConf();
		Properties props = new Properties();
		if (is != null) {
			try {
				props.load(is);
				return props.getProperty(key);
			} catch (IOException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	protected abstract InputStream getConf();
}
