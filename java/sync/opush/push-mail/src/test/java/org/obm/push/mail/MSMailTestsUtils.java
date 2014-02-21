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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;

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
import org.obm.push.bean.Address;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.MethodAttachment;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.configuration.OpushConfiguration;
import org.obm.push.utils.SerializableInputStream;

import com.google.common.base.Charsets;

public class MSMailTestsUtils {

	private static Mime4jUtils mime4jUtils = new Mime4jUtils();
	
	public static MSEmail createMSEmailPlainText(String content, Charset charset) {
		return MSEmail.builder()
				.header(MSEmailHeader.builder()
						.build())
				.body(MSEmailBody.builder()
						.charset(charset)
						.bodyType(MSEmailBodyType.PlainText)
						.mimeData(new SerializableInputStream(content))
						.build())
				.build();
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
		return MSEmail.builder()
				.header(MSEmailHeader.builder()
						.build())
				.body(MSEmailBody.builder()
						.charset(Charsets.UTF_8)
						.bodyType(MSEmailBodyType.HTML)
						.mimeData(new SerializableInputStream(htmlBold(content)))
						.build())
				.build();
	}

	public static MSAttachement attach(String file){
		MSAttachement attach = new MSAttachement();
		attach.setDisplayName("picture");
		attach.setEstimatedDataSize(50000);
		attach.setFileReference(file);
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
		Multipart bodyMulti = MSMailTestsUtils.createMultipartTextAndHtml(
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
        multi.addBodyPart(MSMailTestsUtils.createImagePart(imageData));
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
	

	public static OpushConfiguration mockOpushConfiguration() {
		OpushConfiguration opushConfiguration = EasyMock.createMock(OpushConfiguration.class);
		EasyMock.expect(opushConfiguration.getDefaultEncoding()).andReturn(Charsets.UTF_8).anyTimes();
		EasyMock.replay(opushConfiguration);
		return opushConfiguration;
	}

	public static InputStream loadEmail(String name) {
		return ClassLoader.getSystemResourceAsStream("eml/" + name);
	}
	
	public static Message loadMimeMessage(String name) throws MimeException, IOException {
		InputStream eml = loadEmail(name);
		return loadMimeMessage(eml);
	}
	
	public static Message loadMimeMessage(InputStream stream) throws MimeException, IOException {
		Message message = mime4jUtils.parseMessage(stream);
		return message;
	}
}
