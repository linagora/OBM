package org.obm.sync.push.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class XMLOPClient extends OPClient {

	public XMLOPClient(String loginAtDomain, String password, String devId,
			String devType, String userAgent, int port) {
		
		super(loginAtDomain, password, devId, devType, userAgent, buildServiceUrl(port));
	}

	private RequestEntity getRequestEntity(Document doc) throws UnsupportedEncodingException, TransformerException {
		try {
			String xmlData = DOMUtils.serialise(doc);
			return new ByteArrayRequestEntity(xmlData.getBytes("UTF8"), "text/xml");
		} catch (TransformerException e) {
			throw new TransformerException("Cannot serialize data to xml", e);
		}
	}

	private static String buildServiceUrl(int port) {
		return "http://localhost:" + port + "/Autodiscover/";
	}

	@Override
	public Document postXml(String namespace, Document doc, String cmd, String policyKey, boolean multipart)
			throws TransformerException, HttpException, IOException, HttpStatusException {
		
		DOMUtils.logDom(doc);
		
		RequestEntity requestEntity = getRequestEntity(doc);

		PostMethod pm = null;
		pm = new PostMethod(ai.getUrl() + "?User=" + ai.getLogin());
		pm.setRequestHeader("Content-Length", String.valueOf(requestEntity.getContentLength()));
		pm.setRequestEntity(requestEntity);
		pm.setRequestHeader("Content-Type", requestEntity.getContentType());
		pm.setRequestHeader("Authorization", ai.authValue());
		pm.setRequestHeader("Accept", "*/*");
		pm.setRequestHeader("Accept-Language", "fr-fr");
		pm.setRequestHeader("Connection", "keep-alive");
		
		try {
			int ret = 0;
			ret = hc.executeMethod(pm);
			Header[] hs = pm.getResponseHeaders();
			for (Header h: hs) {
				logger.error("head[" + h.getName() + "] => " + h.getValue());
			}
			if (ret != HttpStatus.SC_OK) {
				throw new HttpStatusException(ret, "method failed:\n" + pm.getStatusLine() + "\n" + pm.getResponseBodyAsString());
			} else {
				InputStream in = pm.getResponseBodyAsStream();
				Document docResponse = DOMUtils.parse(in);
				DOMUtils.logDom(docResponse);
				return docResponse;
			}
		} catch (SAXException e) {
			throw new TransformerException(e);
		} catch (FactoryConfigurationError e) {
			throw new TransformerException(e);
		} finally {
			pm.releaseConnection();
		}
	}
	
}
