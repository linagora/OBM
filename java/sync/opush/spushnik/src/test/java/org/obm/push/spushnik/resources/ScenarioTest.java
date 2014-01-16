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
package org.obm.push.spushnik.resources;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.spushnik.bean.CheckResult;
import org.obm.push.spushnik.bean.Credentials;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.Pkcs12HttpClientBuilder;
import org.obm.sync.push.client.PoolingHttpClientBuilder;
import org.obm.sync.push.client.SSLHttpClientBuilder;

@RunWith(SlowFilterRunner.class)
public class ScenarioTest {

	private Scenario testee;
	private Credentials noCertificateCredentials;
	private Credentials pkcs12CertificateCredentials;
	private String httpServiceUrl;
	private String httpsServiceUrl;

	@Before
	public void setUp() {
		testee = new Scenario(){
			@Override
			protected CheckResult scenarii(OPClient client) throws Exception {
				return null;
			}};
			
		noCertificateCredentials = Credentials.builder()
				.loginAtDomain("user@domain")
				.password("pwd")
				.build();
		
		pkcs12CertificateCredentials = Credentials.builder()
				.loginAtDomain("user@domain")
				.password("pwd")
				.pkcs12(new byte[]{1, 2, 3, 4})
				.pkcs12Password("pkcs12Password")
				.build();
		httpServiceUrl = "http://localhost";
		httpsServiceUrl = "https://localhost";
	}

	@Test(expected=NullPointerException.class)
	public void testChooseHttpClientWhenNullCredentials() {
		Credentials credentials = null;
		testee.chooseHttpClientBuilder(credentials, httpServiceUrl);
	}
	
	@Test(expected=NullPointerException.class)
	public void testChooseHttpClientWhenNullUrl() {
		String serviceUrl = null;
		testee.chooseHttpClientBuilder(noCertificateCredentials, serviceUrl);
	}
	
	@Test
	public void testChooseHttpClientWhenHttpServiceAndNoCertificateCredentials() {
		assertThat(testee.chooseHttpClientBuilder(noCertificateCredentials, httpServiceUrl))
			.isExactlyInstanceOf(PoolingHttpClientBuilder.class);
	}
	
	@Test
	public void testChooseHttpClientWhenHttpServiceAndClientCertificateCredentials() {
		assertThat(testee.chooseHttpClientBuilder(pkcs12CertificateCredentials, httpServiceUrl))
		.isExactlyInstanceOf(PoolingHttpClientBuilder.class);
	}
	
	@Test
	public void testChooseHttpClientWhenHttpsServiceAndNoCertificateCredentials() {
		assertThat(testee.chooseHttpClientBuilder(noCertificateCredentials, httpsServiceUrl))
			.isExactlyInstanceOf(SSLHttpClientBuilder.class);
	}
	
	@Test
	public void testChooseHttpClientWhenHttpsServiceAndClientCertificateCredentials() {
		assertThat(testee.chooseHttpClientBuilder(pkcs12CertificateCredentials, httpsServiceUrl))
			.isExactlyInstanceOf(Pkcs12HttpClientBuilder.class);
	}
	
}
