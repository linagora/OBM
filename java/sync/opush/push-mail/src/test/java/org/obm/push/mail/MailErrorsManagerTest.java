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
package org.obm.push.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.field.address.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.sync.date.DateProvider;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;


public class MailErrorsManagerTest {

	private DateProvider dateProvider;
	private Date messageHeaderDate;
	
	@Before
	public void setUp() {
		messageHeaderDate = date("2012-05-04T11:30:12");
		dateProvider = new DateProvider() {
			
			@Override
			public Date getDate() {
				return messageHeaderDate;
			}
		}; 
	}

	@Test
	public void testPrepareMessage() throws ParseException, FileNotFoundException, IOException {
		User user = User.Factory.create().createUser("test@domain", "test@domain", "displayName");
		UserDataRequest userDataRequest = new UserDataRequest(new Credentials(user, "password"), null, null);
		
		Mime4jUtils mime4jUtils = new Mime4jUtils();
		
		MailErrorsManager errorsManager = new MailErrorsManager(null, null, mime4jUtils, dateProvider);
		Message message = errorsManager.prepareMessage(userDataRequest, "Subject", "Body", 
				StreamMailTestsUtils.newInputStreamFromString("It's mail content !"));
		
		InputStream stream = mime4jUtils.toInputStream(message);
		String messageAsString = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
		
		assertThat(message).isNotNull();
		assertThat(message.getDate()).isEqualTo(messageHeaderDate);
		assertThat(message.isMultipart()).isTrue();
		assertThat(messageAsString).contains("test@domain")
					.contains("Subject: Subject")
					.contains("Body")
					.contains("Content-Disposition: attachment; filename=error_message.eml")
					.contains("It's mail content !");
	}

}
