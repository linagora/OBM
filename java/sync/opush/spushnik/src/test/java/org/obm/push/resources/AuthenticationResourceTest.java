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
package org.obm.push.resources;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.InputStream;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.push.SlowArquillianRunner;
import org.obm.push.SpushnikWebArchive;
import org.obm.push.jaxb.Credentials;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

@RunWith(SlowArquillianRunner.class) @Slow
public class AuthenticationResourceTest {

	private DefaultHttpClient httpClient;

	@Before
	public void setUp() {
		httpClient = new DefaultHttpClient();
	}
	
	@Test
	@RunAsClient
	public void testAuthenticateWithNiceCertificate(@ArquillianResource URL baseURL) throws Exception {
		HttpResponse httpResponse = requestAuthenticationWithCertificate(baseURL, "cert.pem");
		
		InputStream content = httpResponse.getEntity().getContent();
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
		assertThat(IOUtils.toString(content, Charsets.UTF_8)).isEqualTo("ok");
	}
	
	@Test
	@RunAsClient
	public void testAuthenticateWithBadCertificate(@ArquillianResource URL baseURL) throws Exception {
		HttpResponse httpResponse = requestAuthenticationWithCertificate(baseURL, "bad_cert.pem");
		
		InputStream content = httpResponse.getEntity().getContent();
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
		assertThat(IOUtils.toString(content, Charsets.UTF_8)).isEqualTo("nok");
	}

	private HttpResponse requestAuthenticationWithCertificate(URL baseURL, String certificate) throws Exception {
		InputStream certificateInputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(certificate);
		byte[] requestContent = new ObjectMapper()
			.writer()
			.withType(Credentials.class)
			.writeValueAsBytes(
					Credentials.builder()
					.loginAtDomain("user@domain")
					.password("password")
					.certificate(ByteStreams.toByteArray(certificateInputStream))
					.build());

		HttpPost httpPost = new HttpPost(baseURL.toExternalForm() + "conf/authenticate");
		httpPost.setEntity(new ByteArrayEntity(requestContent, ContentType.APPLICATION_JSON));
		return httpClient.execute(httpPost);
	}
	
	@Test
	@RunAsClient
	public void testAuthenticateWithoutCertificate(@ArquillianResource URL baseURL) throws Exception {
		byte[] requestContent = new ObjectMapper()
			.writer()
			.withType(Credentials.class)
			.writeValueAsBytes(
					Credentials.builder()
					.loginAtDomain("user@domain")
					.password("password")
					.build());

		HttpPost httpPost = new HttpPost(baseURL.toExternalForm() + "conf/authenticate");
		httpPost.setEntity(new ByteArrayEntity(requestContent, ContentType.APPLICATION_JSON));
		HttpResponse httpResponse = httpClient.execute(httpPost);
		
		InputStream content = httpResponse.getEntity().getContent();
		assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
		assertThat(IOUtils.toString(content, Charsets.UTF_8)).isEqualTo("ok");
	}

	@Deployment
	public static WebArchive createDeployment() throws Exception {
		return SpushnikWebArchive.buildInstance();
	}
}
