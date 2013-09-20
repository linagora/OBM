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
package org.obm.sync.push.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;

public class SSLHttpClientBuilder extends PoolingHttpClientBuilder {

	private static final KeyManager[] TRUST_ALL_KEY_MANAGERS = null;

	@Override
	public HttpClient build() {
		return configureSsl(super.build());
	}

	private HttpClient configureSsl(HttpClient httpClient) {
		try {
			SSLContext sslContext = buildSSLContext(TRUST_ALL_KEY_MANAGERS);
			httpClient
					.getConnectionManager()
					.getSchemeRegistry()
					.register(new Scheme("https", 443, new SSLSocketFactory(sslContext, new AllowAllHostnameVerifier())));
			
		} catch (KeyManagementException e) {
			throw new IllegalArgumentException("Could not initialize a ssl context", e);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Could not initialize a ssl context", e);
		}
		return httpClient;
	}

	protected SSLContext buildSSLContext(KeyManager[] keyManagers) throws NoSuchAlgorithmException, KeyManagementException {
		SecureRandom secureRandom = null;
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagers, trustAllx509Manager(), secureRandom);
		return sslContext;
	}

	private TrustManager[] trustAllx509Manager() {
		X509TrustManager trustAllx509Manager = new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		};
		return new TrustManager[] {trustAllx509Manager};
	}
}
