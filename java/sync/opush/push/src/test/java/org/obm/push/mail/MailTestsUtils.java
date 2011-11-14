package org.obm.push.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.util.MimeUtil;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.parser.ParserException;
import org.easymock.EasyMock;
import org.obm.push.OpushConfigurationService;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEmailBody;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.utils.Mime4jUtils;

import com.google.common.base.Charsets;

public class MailTestsUtils {

	private static Mime4jUtils mime4jUtils = new Mime4jUtils();
	
	public static InputStream loadEmail(Class<?> testClass, String name) {
		return testClass.getClassLoader().getResourceAsStream("eml/" + name);
	}
	
	public static Message loadMimeMessage(Class<?> testClass, String name) throws MimeException, IOException {
		InputStream eml = loadEmail(testClass, name);
		Message message = mime4jUtils.parseMessage(eml);
		return message;
	}
	
	public static OpushConfigurationService mockOpushConfigurationService() {
		OpushConfigurationService configurationService = EasyMock.createMock(OpushConfigurationService.class);
		EasyMock.expect(configurationService.getDefaultEncoding()).andReturn(Charsets.UTF_8).anyTimes();
		EasyMock.replay(configurationService);
		return configurationService;
	}

	public static MSEmail createMSEmailPlainText(String content, Charset charset) {
		MSEmail original = new MSEmail();
		MSEmailBody msEmailBody = new MSEmailBody();
		msEmailBody.setCharset(charset.name());
		msEmailBody.addConverted(MSEmailBodyType.PlainText, content);
		original.setBody(msEmailBody);
		return original;
	}

	public static MSEmail createMSEmailPlainText(String content) {
		return createMSEmailPlainText(content, Charsets.UTF_8);
	}
	
	public static MSEmail createMSEmailPlainTextASCII(String content) {
		return createMSEmailPlainText(content, Charsets.US_ASCII);
	}

	private static String htmlBold(String content) {
		return "<b>"+content+"</b>";
	}
	
	public static MSEmail createMSEmailHtmlText(String content) {
		MSEmail original = new MSEmail();
		MSEmailBody msEmailBody = new MSEmailBody();
		msEmailBody.addConverted(MSEmailBodyType.HTML, htmlBold(content));
		original.setBody(msEmailBody);
		return original;
	}
	
	public static MSEmail createMSEmailMultipartAlt(String content) {
		MSEmail original = new MSEmail();
		MSEmailBody msEmailBody = new MSEmailBody();
		msEmailBody.addConverted(MSEmailBodyType.PlainText, content);
		msEmailBody.addConverted(MSEmailBodyType.HTML, htmlBold(content));
		original.setBody(msEmailBody);
		return original;
	}
	
	public static MessageImpl createMessagePlainText(Mime4jUtils mime4jUtils,String text) throws UnsupportedEncodingException {
		MessageImpl msg = mime4jUtils.createMessage();
		msg.setBody(mime4jUtils.createBody(text), ContentTypeField.TYPE_TEXT_PLAIN);
		return msg;
	}
	
	public static MessageImpl createMessageHtml(Mime4jUtils mime4jUtils,String html) throws UnsupportedEncodingException {
		MessageImpl msg = mime4jUtils.createMessage();
		msg.setBody(mime4jUtils.createBody(htmlBold(html)), "text/html");
		return msg;
	}
	
	public static MessageImpl createMessageTextAndHtml(Mime4jUtils mime4jUtils, String text) throws UnsupportedEncodingException {
		Map<String, String> params = mime4jUtils.getContentTypeHeaderParams(Charsets.UTF_8);
		String boundary = MimeUtil.createUniqueBoundary();
		params.put(ContentTypeField.PARAM_BOUNDARY, boundary);

		MessageImpl msg = mime4jUtils.createMessage();
		Multipart bodyMulti = createMultipartTextAndHtml(mime4jUtils, text);
		msg.setBody(bodyMulti, "multipart/alternative", params);
		return msg;
	}
	
	public static Multipart createMultipartTextAndHtml(Mime4jUtils mime4jUtils,String text) throws UnsupportedEncodingException {
		Multipart msg = mime4jUtils.createMultipartAlternative();
		msg.addBodyPart(mime4jUtils.bodyToBodyPart(mime4jUtils.createBody(text), ContentTypeField.TYPE_TEXT_PLAIN));
		msg.addBodyPart(mime4jUtils.bodyToBodyPart(mime4jUtils.createBody(htmlBold(text)), "text/html"));
		return msg;
	}
	
	public static Address addr(String addr) throws ParserException {
		return Address.parse(addr);
	}
}
