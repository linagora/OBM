package org.obm.push.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.storage.Storage;
import org.apache.james.mime4j.storage.StorageBodyFactory;
import org.apache.james.mime4j.storage.StorageOutputStream;
import org.apache.james.mime4j.storage.StorageProvider;
import org.apache.james.mime4j.util.MimeUtil;
import org.easymock.EasyMock;
import org.obm.configuration.ConfigurationService;
import org.obm.push.bean.Address;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEmailBody;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MethodAttachment;
import org.obm.push.utils.Mime4jUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;

public class MailTestsUtils {

	private static Mime4jUtils mime4jUtils = new Mime4jUtils();
	
	public static InputStream loadEmail(Class<?> testClass, String name) {
		return testClass.getClassLoader().getResourceAsStream("eml/" + name);
	}
	
	public static Message loadMimeMessage(Class<?> testClass, String name) throws MimeException, IOException {
		InputStream eml = loadEmail(testClass, name);
		return loadMimeMessage(eml);
	}
	
	public static Message loadMimeMessage(InputStream stream) throws MimeException, IOException {
		Message message = mime4jUtils.parseMessage(stream);
		return message;
	}
	
	public static ConfigurationService mockOpushConfigurationService() {
		ConfigurationService configurationService = EasyMock.createMock(ConfigurationService.class);
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
	
	public static MSEmail createMSEmailMultipartMixed(String content) {
		MSEmail original = createMSEmailMultipartAlt(content);
		
		Set<MSAttachement> attached = Sets.newHashSet();
		attached.add(attach("/test/file.png"));
		attached.add(attach("/test/file2.png"));
		original.setAttachements(attached);
		return original;
	}

	public static MSAttachement attach(String file){
		MSAttachement attach = new MSAttachement();
		attach.setContentId(String.valueOf(Math.random()));
		attach.setDisplayName("picture");
		attach.setContentLocation(file);
		attach.setEstimatedDataSize(50000);
		attach.setFileReference(file);
		attach.setIsInline("isInLineTrue");
		attach.setMethod(MethodAttachment.EmbeddedMessage);
		return attach;
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

	public static MessageImpl createMessageTextAndHtml(Mime4jUtils mime4jUtils,
			String text, String html) throws UnsupportedEncodingException {
		Map<String, String> params = mime4jUtils
				.getContentTypeHeaderParams(Charsets.UTF_8);
		String boundary = MimeUtil.createUniqueBoundary();
		params.put(ContentTypeField.PARAM_BOUNDARY, boundary);

		MessageImpl msg = mime4jUtils.createMessage();
		Multipart bodyMulti = MailTestsUtils.createMultipartTextAndHtml(
				mime4jUtils, text, html);
		msg.setBody(bodyMulti, "multipart/alternative", params);
		return msg;
	}

	public static Multipart createMultipartTextAndHtml(Mime4jUtils mime4jUtils,
			String text, String html) throws UnsupportedEncodingException {
		Multipart msg = mime4jUtils.createMultipartAlternative();
		msg.addBodyPart(mime4jUtils.bodyToBodyPart(
				mime4jUtils.createBody(text), ContentTypeField.TYPE_TEXT_PLAIN));
		msg.addBodyPart(mime4jUtils.bodyToBodyPart(
				mime4jUtils.createBody(html), "text/html"));
		return msg;
	}

	public static MessageImpl createMessageMultipartMixed(Mime4jUtils mime4jUtils, String text, byte[] imageData) throws IOException {
		Multipart multi = mime4jUtils.createMultipartMixed();
		multi.addBodyPart(mime4jUtils.bodyToBodyPart(mime4jUtils.createBody(text), ContentTypeField.TYPE_TEXT_PLAIN));
        multi.addBodyPart(MailTestsUtils.createImagePart(imageData));
        MessageImpl msg = mime4jUtils.createMessage();
        msg.setMultipart(multi);
        return msg;
	}

	private static BodyPart createImagePart(byte[] data) throws IOException {
		StorageBodyFactory bodyFactory = new StorageBodyFactory();
		StorageProvider storageProvider = bodyFactory.getStorageProvider();
		StorageOutputStream out = storageProvider.createStorageOutputStream();
		out.write(data);
		Storage storage = out.toStorage();
		BinaryBody body = bodyFactory.binaryBody(storage);
		
        // Create a body part with the correct MIME-type and transfer encoding
        BodyPart bodyPart = new BodyPart();
        bodyPart.setBody(body, "image/png");
        bodyPart.setContentTransferEncoding("base64");

        // Specify a filename in the Content-Disposition header (implicitly sets
        // the disposition type to "attachment")
        bodyPart.setFilename("smiley.png");

        return bodyPart;
	}

	public static Address addr(String addr) {
		return new Address(addr);
	}
}
