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
package org.obm.push.protocol.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSMessageClass;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.protocol.bean.ASTimeZone;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.IntEncoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;

public class MSEmailEncoder {

	public final static String DEFAULT_TIME_ZONE = 
			"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQBlAAAAAAAAAAAAAAAAA" +
			"AAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZw" +
			"BoAHQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==";
	
	public final static String UTC_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.000'Z'";
	public final static String UTC_DATE_NO_PUNCTUATION_PATTERN = "yyyyMMdd'T'HHmmss'Z'";
	
	private final static String MESSAGE_CLASS = "urn:content-classes:message";
	private final static String CALENDAR_CLASS = "urn:content-classes:calendarmessage";
	private final static String CPID_DEFAULT = "65001";
	
	private final IntEncoder intEncoder;
	private final TimeZoneEncoder timeZoneEncoder;
	private final TimeZoneConverter timeZoneConverter;

	@Inject
	@VisibleForTesting MSEmailEncoder(IntEncoder intEncoder, TimeZoneEncoder timeZoneEncoder,
			TimeZoneConverter timeZoneConverter) {
		
		this.intEncoder = intEncoder;
		this.timeZoneEncoder = timeZoneEncoder;
		this.timeZoneConverter = timeZoneConverter;
	}

	public void encode(Element parent, IApplicationData data) throws IOException {
		MSEmail msEmail = (MSEmail) data;
		new MSEmailHeaderSerializer(parent, msEmail).serializeMSEmailHeader();

		String messageImportance = msEmail.getImportance().asSpecificationValue();
		DOMUtils.createElementAndText(parent, ASEmail.IMPORTANCE.asASValue(), messageImportance);
		
		DOMUtils.createElementAndText(parent, ASEmail.READ.asASValue(), msEmail.isRead());

		serializeBody(parent, msEmail.getBody());
		serializeAttachments(parent, msEmail.getAttachments());

		MSMessageClass messageClass = msEmail.getMessageClass();
		DOMUtils.createElementAndText(parent, ASEmail.MESSAGE_CLASS.asASValue(), messageClass.specificationValue());
		
		MSMeetingRequest meetingRequest = msEmail.getMeetingRequest();
		if (meetingRequest != null) {
			String timeZone = getTimeZone(meetingRequest);
			new MSMeetingRequestSerializer(intEncoder, parent, meetingRequest)
				.serializeMSMeetingRequest(timeZone);
			
			DOMUtils.createElementAndText(parent, ASEmail.CONTENT_CLASS.asASValue(), CALENDAR_CLASS);
		} else {
			DOMUtils.createElementAndText(parent, ASEmail.CONTENT_CLASS.asASValue(), MESSAGE_CLASS);
		}
		
		DOMUtils.createElementAndText(parent, ASEmail.CPID.asASValue(), CPID_DEFAULT);
		DOMUtils.createElementAndText(parent, ASAirs.NATIVE_TYPE.asASValue(), msEmail.getBody().getBodyType().asXmlValue());
	}

	public Element encodedApplicationData(IApplicationData data) throws IOException {
		Document doc = DOMUtils.createDoc(null, null);
		Element root = doc.getDocumentElement();
		encode(root, data);
		return root;
	}

	private void serializeBody(Element parent, MSEmailBody body) throws IOException {
		MSEmailBodyType bodyType = body.getBodyType();
		Integer estimatedDataSize = body.getEstimatedDataSize();

		Element bodyElement = DOMUtils.createElement(parent, ASAirs.BODY.asASValue());
		
		DOMUtils.createElementAndText(bodyElement, ASAirs.TYPE.asASValue(), bodyType.asXmlValue());
		DOMUtils.createElementAndText(bodyElement, ASAirs.TRUNCATED.asASValue(), body.isTruncated());
		DOMUtils.createElementAndTextIfNotNull(bodyElement, ASAirs.ESTIMATED_DATA_SIZE.asASValue(), estimatedDataSize);
		encodeData(body, bodyElement);
	}

	@VisibleForTesting void encodeData(MSEmailBody body, Element bodyElement) throws IOException {
		if (body.getBodyType().isCDataEncoded()) {
			DOMUtils.createElementAndCDataText(bodyElement, ASAirs.DATA.asASValue(), 
					body.getMimeData(), body.getCharset());
		} else {
			String html = CharStreams.toString(new InputStreamReader(body.getMimeData(), body.getCharset()));
			DOMUtils.createElementAndText(bodyElement, ASAirs.DATA.asASValue(), html);
		}
	}

	private void serializeAttachments(Element parent, Set<MSAttachement> attachments) {
		if (!Iterables.isEmpty(attachments)) {
			Element atts = DOMUtils.createElement(parent, ASAirs.ATTACHMENTS.asASValue());
			for (MSAttachement msAtt: attachments) {
				Element att = DOMUtils.createElement(atts, ASAirs.ATTACHMENT.asASValue());
				DOMUtils.createElementAndText(att, ASAirs.DISPLAY_NAME.asASValue(), msAtt.getDisplayName());
				DOMUtils.createElementAndText(att, ASAirs.FILE_REFERENCE.asASValue(), msAtt.getFileReference());
				DOMUtils.createElementAndText(att, ASAirs.METHOD.asASValue(), msAtt.getMethod().asSpecificationValue());
				DOMUtils.createElementAndText(att, ASAirs.ESTIMATED_DATA_SIZE.asASValue(), 
						msAtt.getEstimatedDataSize().toString());
				DOMUtils.createElementAndTextIfNotNull(att, ASAirs.CONTENT_ID.asASValue(), msAtt.getContentId());
				DOMUtils.createElementAndTextIfNotNull(att, ASAirs.CONTENT_LOCATION.asASValue(), msAtt.getContentLocation());
				DOMUtils.createElementAndText(att, ASAirs.IS_INLINE.asASValue(), msAtt.isInline());
			}
		}
	}

	private String getTimeZone(MSMeetingRequest meetingRequest) {
		ASTimeZone asTimeZone = timeZoneConverter.convert(meetingRequest.getTimeZone(), Locale.US);
		byte[] timeZone = timeZoneEncoder.encode(asTimeZone);
		if (timeZone != null) {
			return Base64.encodeBase64String(timeZone);
		}
		return MSEmailEncoder.DEFAULT_TIME_ZONE;
	}
}
