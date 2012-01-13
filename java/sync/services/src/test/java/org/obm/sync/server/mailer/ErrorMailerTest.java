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
package org.obm.sync.server.mailer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.server.template.ITemplateLoader;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.MailService;
import fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import freemarker.template.Configuration;
import freemarker.template.Template;

@RunWith(Suite.class)
@SuiteClasses({ErrorMailerTest.Error.class, ErrorMailerTest.Expire.class})
public class ErrorMailerTest {

	private static final TimeZone TIMEZONE = TimeZone.getTimeZone("Europe/Paris");
	
	protected static AccessToken getMockAccessToken(){
		AccessToken at = new AccessToken(1, "unitTest");
		at.setDomain(ToolBox.getDefaultObmDomain());
		at.setUserEmail("adrien@test.tlse.lng");
		return at;
	}
	
	public abstract static class Common {
		
		AccessToken at;
		ITemplateLoader templateLoader;
		
		public Common(){
			at = getMockAccessToken();
			
			templateLoader = new ITemplateLoader() {
				@Override
				public Template getTemplate(String templateName, Locale locale, TimeZone timezone)
						throws IOException {
					Configuration cfg = new Configuration();
					cfg.setClassForTemplateLoading(getClass(), "template");
					return cfg.getTemplate(templateName, locale);
				}
			};
			
		}
		
		protected ITemplateLoader getMockTemplateLoader(){
			return templateLoader;
		}
		
		protected MailService defineMailServiceExpectations(
				List<InternetAddress> expectedRecipients,
				Capture<MimeMessage> capturedMessage) throws MessagingException {
			
			MailService mailService = EasyMock.createMock(MailService.class);
			mailService.sendMessage(
					EventChangeHandlerTestsTools.compareCollections(expectedRecipients), 
					EasyMock.capture(capturedMessage),
					EasyMock.anyObject(AccessToken.class));
			EasyMock.expectLastCall();
			EasyMock.replay(mailService);
			return mailService;
		}

		protected abstract List<InternetAddress> getExpectedRecipients() throws AddressException;

		public static class ErrorParts {
			public String rawMessage;
			public String plainText;
		}

		
		private String getRawMessage(MimeMessage actualMessage)
		throws IOException, MessagingException,	UnsupportedEncodingException {

			ByteArrayOutputStream output = new ByteArrayOutputStream();
			actualMessage.writeTo(output);
			String rawMessage = new String(output.toByteArray(), Charsets.UTF_8.displayName());
			return rawMessage;
		}


		protected void test() throws UnsupportedEncodingException, IOException, MessagingException {
			ObmSyncConfigurationService constantService = EasyMock.createMock(ObmSyncConfigurationService.class);
			EasyMock.expect(constantService.getObmSyncMailer(at)).andReturn("x-obm-sync@test.tlse.lng").once();
			EasyMock.expect(constantService.getResourceBundle(Locale.FRENCH)).andReturn(
					ResourceBundle.getBundle("Messages", Locale.FRENCH));
			Capture<MimeMessage> capturedMessage = new Capture<MimeMessage>();
			EasyMock.replay(constantService);
			List<InternetAddress> expectedRecipients = getExpectedRecipients();
			MailService mailService = defineMailServiceExpectations(expectedRecipients, capturedMessage);
			ErrorMailer errorMailer = new ErrorMailer(mailService, constantService, getMockTemplateLoader());
			
			executeProcess(errorMailer);

			EasyMock.verify(mailService, constantService);
			MimeMessage mimeMessage = capturedMessage.getValue();

			ErrorParts parts = checkErrorStructure(mimeMessage);
			checkContent(parts);
		}

		protected abstract void checkContent(ErrorParts parts) throws IOException, MessagingException;

		protected ErrorParts checkErrorStructure(MimeMessage mimeMessage) throws UnsupportedEncodingException, IOException, MessagingException {
			ErrorParts parts = new ErrorParts();
			parts.rawMessage = getRawMessage(mimeMessage);
			Assert.assertTrue(mimeMessage.getContentType().startsWith("text/plain; charset=UTF-8"));
			parts.plainText = (String)mimeMessage.getContent();
			return parts;
		}

		protected void checkStringContains(String text, String... expected) {
			for (String s: expected) {
				Assert.assertThat(text, StringContains.containsString(s));
			}
		}

		protected void checkPlainMessage(String plainText) {
			Assert.assertThat(plainText, IsInstanceOf.instanceOf(String.class));
			String text = plainText;
			checkStringContains(text, getExpectedPlainStrings());
		}
		
		protected abstract String[] getExpectedPlainStrings();


		protected abstract void executeProcess(ErrorMailer errorMailer);
	}

	public static class Error extends Common {

		@Override
		protected void executeProcess(ErrorMailer errorMailer) {
			errorMailer.notifyConnectorVersionError(at, "1.1.1", Locale.FRENCH, TIMEZONE);
		}
		
		@Test
		public void errorRequest() throws IOException, MessagingException {
			super.test();
		}

		@Override
		protected void checkContent(ErrorParts parts) throws IOException, MessagingException {
			checkStringContains(parts.rawMessage, 
					"From: x-obm-sync@test.tlse.lng",
					"To: adrien@test.tlse.lng",
					"Subject: ");
			checkPlainMessage(parts.plainText);
		}
		
		@Override
		protected String[] getExpectedPlainStrings() {
			return new String[] {
				"La synchronisation de votre agenda et de vos contacts n'a pas pu aboutir car la\nversion de l'extension \"OBM Connector\" que vous utilisez est obsolète.\n",
				"Merci de contacter votre administrateur afin qu'il vous fournisse une mise à\njour",
			};
		}
		
		@Override
		protected List<InternetAddress> getExpectedRecipients() throws AddressException {
			return ImmutableList.of(new InternetAddress("adrien@test.tlse.lng"));
		}

	}
	
	public static class Expire {
		
		@Test
		public void testExpireAfterWrite() throws MessagingException {
			AccessToken at = getMockAccessToken();
			ObmSyncConfigurationService constantService = EasyMock.createMock(ObmSyncConfigurationService.class);
			EasyMock.expect(constantService.getObmSyncMailer(at)).andReturn("x-obm-sync@test.tlse.lng").once();
			EasyMock.expect(constantService.getResourceBundle(Locale.FRENCH)).andReturn(
					ResourceBundle.getBundle("Messages", Locale.FRENCH));
			Capture<MimeMessage> capturedMessage = new Capture<MimeMessage>();
			EasyMock.replay(constantService);
			List<InternetAddress> expectedRecipients = ImmutableList.of(new InternetAddress("adrien@test.tlse.lng"));
			
			MailService mailService = EasyMock.createMock(MailService.class);
			mailService.sendMessage(
					EventChangeHandlerTestsTools.compareCollections(expectedRecipients), 
					EasyMock.capture(capturedMessage),
					EasyMock.anyObject(AccessToken.class));
			EasyMock.expectLastCall();
			EasyMock.replay(mailService);

			ITemplateLoader templateLoader = new ITemplateLoader() {
				@Override
				public Template getTemplate(String templateName, Locale locale, TimeZone timezone)
						throws IOException {
					Configuration cfg = new Configuration();
					cfg.setClassForTemplateLoading(getClass(), "template");
					return cfg.getTemplate(templateName, locale);
				}
			};
			
			ErrorMailer errorMailer = new ErrorMailer(mailService, constantService, templateLoader);
			
			errorMailer.notifyConnectorVersionError(at, "1.1.1", Locale.FRENCH, TIMEZONE);
			errorMailer.notifyConnectorVersionError(at, "1.1.1", Locale.FRENCH, TIMEZONE);

			EasyMock.verify(mailService, constantService);
			

		}
		
	}
	

}
