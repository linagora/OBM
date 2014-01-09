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
package org.obm.push.spushnik.service;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.obm.push.spushnik.bean.Credentials;

import com.google.common.io.ByteStreams;


public class CredentialsServiceTest {

	@Rule 
	public ExpectedException thrown = ExpectedException.none();
	
	private CredentialsService service;

	@Before
	public void setUp() {
		service = new CredentialsService();
	}

	@Test
	public void testCredentialsWithNiceCertificate() throws Exception {
		InputStream certificateInputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("pkcs_pwd_toto.p12");
		service.validate(Credentials.builder()
					.loginAtDomain("user@domain")
					.password("password")
					.pkcs12(ByteStreams.toByteArray(certificateInputStream))
					.pkcs12Password("toto")
					.build());
	}
	
	@Test
	public void testCredentialsWithBadCertificate() throws Exception {
		thrown.expect(InvalidCredentialsException.class);
		thrown.expectMessage("Invalid certificate");
		InputStream certificateInputStream = IOUtils.toInputStream("I'm not a pkcs12 file");
		service.validate(Credentials.builder()
					.loginAtDomain("user@domain")
					.password("password")
					.pkcs12(ByteStreams.toByteArray(certificateInputStream))
					.pkcs12Password("")
					.build());
	}
	
	@Test
	public void testCredentialsWithoutCertificate() throws InvalidCredentialsException {
		service.validate(Credentials.builder()
					.loginAtDomain("user@domain")
					.password("password")
					.build());
	}
	
	@Test
	public void testCredentialsWithEmptyLogin() throws Exception {
		thrown.expect(InvalidCredentialsException.class);
		thrown.expectMessage("Invalid loginAtDomain");
		Credentials credentials = createMock(Credentials.class);
		expect(credentials.getLoginAtDomain()).andReturn("").times(2);
		
		replay(credentials);
		try {
			service.validate(credentials);
		} catch (Exception e) {
			verify(credentials);
			throw e;
		}
	}
	
	@Test
	public void testCredentialsWithEmptyPassword() throws Exception {
		thrown.expect(InvalidCredentialsException.class);
		thrown.expectMessage("Invalid password");
		Credentials credentials = createMock(Credentials.class);
		expect(credentials.getLoginAtDomain()).andReturn("login@domain");
		expect(credentials.getPassword()).andReturn("").times(2);
		
		replay(credentials);
		try {
			service.validate(credentials);
		} catch (Exception e) {
			verify(credentials);
			throw e;
		}
	}
}
