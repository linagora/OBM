/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class Pkcs12HttpClientBuilder extends SSLHttpClientBuilder {

	private final InputStream pkcs12Stream;
	private final char[] pkcs12Password;

	public Pkcs12HttpClientBuilder(InputStream pkcs12Stream, char[] pkcs12Password) {
		Preconditions.checkArgument(pkcs12Stream != null, "pkcs12 store is required");
		Preconditions.checkArgument(pkcs12Password != null, "pkcs12 store password is required");
		this.pkcs12Stream = pkcs12Stream;
		this.pkcs12Password = pkcs12Password;
	}
	
	@Override
	protected SSLContext buildSSLContext(KeyManager[] keyManagers) throws NoSuchAlgorithmException, KeyManagementException {
		try {
			KeyStore keyStore = loadPKCS12KeyStore(pkcs12Stream, pkcs12Password);
			KeyManagerFactory keyManager = configureKeyManagerFactory(keyStore, pkcs12Password);
			return super.buildSSLContext(keyManager.getKeyManagers());
		} catch (KeyStoreException e) {
			throw new IllegalArgumentException("Given PKCS12 is wrong or password is invalid", e);
		} catch (CertificateException e) {
			throw new IllegalArgumentException("Given PKCS12 is wrong or password is invalid", e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Given PKCS12 is wrong or password is invalid", e);
		} catch (UnrecoverableKeyException e) {
			throw new IllegalArgumentException("Given PKCS12 is wrong or password is invalid", e);
		} finally {
			try {
				pkcs12Stream.close();
			} catch (Exception ignore) {
				// Ignore this exception
			}
		}
	}

	private KeyManagerFactory configureKeyManagerFactory(KeyStore keyStore, char[] pkcs12Password)
			throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
		KeyManagerFactory keyManager = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManager.init(keyStore, pkcs12Password);
		return keyManager;
	}

	@VisibleForTesting KeyStore loadPKCS12KeyStore(InputStream pkcs12Stream, char[] pkcs12Password)
			throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(pkcs12Stream, pkcs12Password);
		return keyStore;
	}
}
