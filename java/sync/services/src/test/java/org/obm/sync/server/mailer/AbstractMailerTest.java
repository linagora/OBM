/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.sync.server.mailer;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.data.MapEntry.entry;

import javax.mail.Session;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.sync.ObmSmtpConf;

import fr.aliacom.obm.common.domain.ObmDomain;


public class AbstractMailerTest {

	private AbstractMailer testee;
	private ObmSmtpConf smtpConf;
	private IMocksControl control;

	@Before
	public void setup() {
		control = createControl();
		smtpConf = control.createMock(ObmSmtpConf.class);
		testee = new AbstractMailer(null, null, null, smtpConf) {};
	}
	
	@Test
	public void testBuildSession() {
		expect(smtpConf.getServerAddr("linagora.com")).andReturn("1.2.3.4");
		expect(smtpConf.getServerPort("linagora.com")).andReturn(25);
		control.replay();
		Session session = testee.buildSession(ObmDomain.builder().name("linagora.com").build());
		control.verify();
		assertThat(session.getProperties())
			.contains(entry("mail.smtp.host", "1.2.3.4"))
			.contains(entry("mail.smtp.port", 25))
			.hasSize(2);
	}
	
	@Test
	public void testBuildSessionTwice() {
		expect(smtpConf.getServerAddr("linagora.com")).andReturn("1.2.3.4");
		expect(smtpConf.getServerPort("linagora.com")).andReturn(25);
		expect(smtpConf.getServerAddr("obm.org")).andReturn("5.6.7.8");
		expect(smtpConf.getServerPort("obm.org")).andReturn(589);
		control.replay();
		testee.buildSession(ObmDomain.builder().name("linagora.com").build());
		Session session = testee.buildSession(ObmDomain.builder().name("obm.org").build());
		control.verify();
		assertThat(session.getProperties())
			.contains(entry("mail.smtp.host", "5.6.7.8"))
			.contains(entry("mail.smtp.port", 589))
			.hasSize(2);
	}

	
}
