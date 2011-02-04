package org.obm.sync.server.mailer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.Session;
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

import fr.aliacom.obm.common.MailService;
import fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools;
import fr.aliacom.obm.services.constant.ConstantService;
import freemarker.template.Configuration;
import freemarker.template.Template;

@RunWith(Suite.class)
@SuiteClasses({ErrorMailerTest.Error.class})
public class ErrorMailerTest {

	public abstract static class Common {
		
		AccessToken at;
		ITemplateLoader templateLoader;
		
		public Common(){
			at = new AccessToken(1, 1, "unitTest");
			
			templateLoader = new ITemplateLoader() {
				@Override
				public Template getTemplate(String templateName, Locale locale)
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
		
		protected AccessToken getMockAccessToken(){
			at.setDomain("test.tlse.lng");
			at.setEmail("adrien@test.tlse.lng");
			return at;
		}

		protected MailService defineMailServiceExpectations(
				List<InternetAddress> expectedRecipients,
				Capture<MimeMessage> capturedMessage) throws MessagingException {
			
			MailService mailService = EasyMock.createMock(MailService.class);
			mailService.sendMessage(
					EasyMock.anyObject(Session.class),
					EventChangeHandlerTestsTools.compareCollections(expectedRecipients), 
					EasyMock.capture(capturedMessage));
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

			ConstantService constantService = EasyMock.createMock(ConstantService.class);
			EasyMock.expect(constantService.getObmSyncMailer(getMockAccessToken())).andReturn("x-obm-sync@test.tlse.lng").once();
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

		protected void checkPlainMessage(String plainText) throws IOException, MessagingException {
			Assert.assertThat(plainText, IsInstanceOf.instanceOf(String.class));
			String text = (String) plainText;
			checkStringContains(text, getExpectedPlainStrings());
		}
		
		protected abstract String[] getExpectedPlainStrings();


		protected abstract void executeProcess(ErrorMailer errorMailer);
	}

	public static class Error extends Common {

		@Override
		protected void executeProcess(ErrorMailer errorMailer) {
			errorMailer.notifyConnectorVersionError(getMockAccessToken(), "1", "1", "1", Locale.FRENCH);
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

}
