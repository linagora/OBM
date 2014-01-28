/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014 Linagora
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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.junit.Test;


public class SSLContextFactoryTest {

	@Test(expected=IllegalArgumentException.class)
	public void testRequirePKCS12() {
		SSLContextFactory.create(null, "password".toCharArray());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testRequirePKCS12Password() {
		SSLContextFactory.create(new ByteArrayInputStream("test".getBytes()), null);
	}
	
	@Test(expected=EOFException.class)
	public void testKeyStoreFailsIfNotPKCS12() throws Throwable {
		InputStream pkcs12Stream = IOUtils.toInputStream("I'm not a pkcs12 key store");
		char[] pkcs12Password = "toto".toCharArray();
		
		try {
			SSLContextFactory.create(pkcs12Stream, pkcs12Password);
		} catch (RuntimeException e) {
			assertThat(e.getCause()).isNotNull();
			throw e.getCause();
		}
	}
	
	@Test
	public void testKeyStoreIsPKCS12() throws Exception {
		InputStream pkcs12Stream = ClassLoader.getSystemClassLoader().getResourceAsStream("pkcs_pwd_toto.p12");
		char[] pkcs12Password = "toto".toCharArray();
		
		KeyStore keyStore = SSLContextFactory.loadPKCS12KeyStore(pkcs12Stream, pkcs12Password);
		
		InputStream pkcs12InnerX509 = ClassLoader.getSystemClassLoader().getResourceAsStream("pkcs_inner_x509.crt");
		Certificate pkcs12InnerCertificate = CertificateFactory.getInstance("x509").generateCertificate(pkcs12InnerX509);
		assertThat(keyStore.getType()).isEqualToIgnoringCase("pkcs12");
		assertThat(keyStore.getCertificate("client2")).isEqualTo(pkcs12InnerCertificate);
	}
	
	@Test
	public void testCorrectSSLContextFactoryCall() {
		InputStream pkcs12Stream = ClassLoader.getSystemClassLoader().getResourceAsStream("pkcs_pwd_toto.p12");
		char[] pkcs12Password = "toto".toCharArray();
		
		SSLContext sslContext = SSLContextFactory.create(pkcs12Stream, pkcs12Password);
		
		assertThat(sslContext).isNotNull();
	}
}
