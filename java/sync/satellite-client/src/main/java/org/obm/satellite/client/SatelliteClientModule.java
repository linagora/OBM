/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.satellite.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class SatelliteClientModule extends AbstractModule {

	private static final KeyManager[] TRUST_ALL_KEY_MANAGERS = null;

	@Override
	protected void configure() {
		bind(SatelliteService.class).to(SatelliteServiceImpl.class);
	}

	@Provides
	private HttpClient provideHttpClient() {
		try {
			SSLContext sslContext = buildSSLContext(TRUST_ALL_KEY_MANAGERS);
			return HttpClientBuilder.create()
				.setSslcontext(sslContext)
				.setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
				.useSystemProperties()
				.build();
		} catch (KeyManagementException e) {
			throw new IllegalArgumentException("Could not initialize a ssl context", e);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Could not initialize a ssl context", e);
		}
	}

	protected SSLContext buildSSLContext(KeyManager[] keyManagers) throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		sslContext.init(keyManagers, trustAllx509Manager(), null);

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
