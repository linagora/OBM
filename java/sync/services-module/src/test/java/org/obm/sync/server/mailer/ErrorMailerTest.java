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
package org.obm.sync.server.mailer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.sync.ObmSmtpConf;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.server.template.ITemplateLoader;
import org.obm.sync.server.template.TemplateLoaderFreeMarkerImpl;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.MailService;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class ErrorMailerTest {

	private static final TimeZone TIMEZONE = TimeZone.getTimeZone("Europe/Paris");
	private AccessToken at;
	private ITemplateLoader templateLoader;
	private IMocksControl control;
	private ObmSmtpConf smtpConf;

	@Before
	public void setup() {
		control = createControl();
		at = getMockAccessToken();
		smtpConf = control.createMock(ObmSmtpConf.class);
		expect(smtpConf.getServerAddr(anyObject(String.class))).andReturn("1.2.3.4").anyTimes();
		expect(smtpConf.getServerPort(anyObject(String.class))).andReturn(234).anyTimes();
		templateLoader = new ITemplateLoader() {
			@Override
			public Template getTemplate(String templateName, Locale locale, TimeZone timezone)
					throws IOException {
				Configuration cfg = new Configuration();
				cfg.setClassForTemplateLoading(getClass(), TemplateLoaderFreeMarkerImpl.getTemplatePathPrefix(locale));
				return cfg.getTemplate(templateName, locale);
			}
		};
	}

	protected static AccessToken getMockAccessToken(){
		AccessToken at = new AccessToken(1, "unitTest");
		at.setDomain(ToolBox.getDefaultObmDomain());
		at.setUserEmail("adrien@test.tlse.lng");
		return at;
	}

	private String getRawMessage(MimeMessage actualMessage)
			throws IOException, MessagingException,	UnsupportedEncodingException {

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		actualMessage.writeTo(output);
		String rawMessage = new String(output.toByteArray(), Charsets.UTF_8.displayName());
		return rawMessage;
	}
	
	@Test
	public void testErrorMailing() throws Exception {
		ObmSyncConfigurationService constantService = control.createMock(ObmSyncConfigurationService.class);
		expect(constantService.getObmSyncMailer(at)).andReturn("x-obm-sync@test.tlse.lng").once();
		expect(constantService.getResourceBundle(Locale.FRENCH)).andReturn(
				ResourceBundle.getBundle("Messages", Locale.FRENCH));
		Capture<MimeMessage> capturedMessage = new Capture<MimeMessage>();

		List<InternetAddress> expectedRecipients = ImmutableList.of(new InternetAddress("adrien@test.tlse.lng"));
		MailService mailService = control.createMock(MailService.class);
		mailService.sendMessage(
				eq(expectedRecipients),
				capture(capturedMessage),
				anyObject(Session.class));
		expectLastCall();
		ErrorMailer errorMailer = new ErrorMailer(mailService, constantService, templateLoader, smtpConf);

		control.replay();
		errorMailer.notifyConnectorVersionError(at, "1.1.1", Locale.FRENCH, TIMEZONE);

		control.verify();
		MimeMessage mimeMessage = capturedMessage.getValue();

		String rawMessage = getRawMessage(mimeMessage);
		assertThat(rawMessage)
			.contains("From: x-obm-sync@test.tlse.lng")
			.contains("To: adrien@test.tlse.lng")
			.contains("Subject: ");
		assertThat(mimeMessage.getContentType()).startsWith("text/plain; charset=UTF-8");
		assertThat(mimeMessage.getContent()).isInstanceOf(String.class);
		String plainText = (String)mimeMessage.getContent();
		assertThat(plainText)
			.contains("La synchronisation de votre agenda et de vos contacts n'a pas pu aboutir car la\nversion de l'extension \"OBM Connector\" que vous utilisez est obsolète.\n")
			.contains("Merci de contacter votre administrateur afin qu'il vous fournisse une mise à\njour");
	}

	@Test
	public void testNotificationSentOnlyOnce() throws MessagingException {
		AccessToken at = getMockAccessToken();
		ObmSyncConfigurationService constantService = control.createMock(ObmSyncConfigurationService.class);
		expect(constantService.getObmSyncMailer(at)).andReturn("x-obm-sync@test.tlse.lng").once();
		expect(constantService.getResourceBundle(Locale.FRENCH)).andReturn(
				ResourceBundle.getBundle("Messages", Locale.FRENCH));
		
		List<InternetAddress> expectedRecipients = ImmutableList.of(new InternetAddress("adrien@test.tlse.lng"));

		MailService mailService = createMock(MailService.class);
		mailService.sendMessage(
				eq(expectedRecipients), 
				anyObject(MimeMessage.class),
				anyObject(Session.class));
		expectLastCall().once();
		
		control.replay();

		ErrorMailer errorMailer = new ErrorMailer(mailService, constantService, templateLoader, smtpConf);

		errorMailer.notifyConnectorVersionError(at, "1.1.1", Locale.FRENCH, TIMEZONE);
		errorMailer.notifyConnectorVersionError(at, "1.1.1", Locale.FRENCH, TIMEZONE);
		control.verify();
	}

}
