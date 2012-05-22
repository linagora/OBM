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
package org.obm.push.protocol.data.ms;

import java.io.IOException;
import java.util.Set;

import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSMessageClass;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.protocol.data.ASAIRS;
import org.obm.push.protocol.data.ASEMAIL;
import org.obm.push.protocol.data.IntEncoder;
import org.obm.push.protocol.data.MSEmailHeaderSerializer;
import org.obm.push.protocol.data.MSMeetingRequestSerializer;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.SerializableInputStream;
import org.w3c.dom.Element;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

public class MSEmailEncoder {

	private final static String MESSAGE_CLASS = "urn:content-classes:message";
	private final static String CALENDAR_CLASS = "urn:content-classes:calendarmessage";
	private final static String CPID_DEFAULT = "65001";
	
	private final IntEncoder intEncoder;

	@Inject
	@VisibleForTesting MSEmailEncoder(IntEncoder intEncoder) {
		this.intEncoder = intEncoder;
	}

	public void encode(Element parent, IApplicationData data) throws IOException {
		MSEmail msEmail = (MSEmail) data;
		new MSEmailHeaderSerializer(parent, msEmail.getHeader()).serializeMSEmailHeader();

		String messageImportance = msEmail.getImportance().asIntString();
		DOMUtils.createElementAndText(parent, ASEMAIL.IMPORTANCE.asASValue(), messageImportance);
		
		DOMUtils.createElementAndText(parent, ASEMAIL.READ.asASValue(), msEmail.isRead());

		serializeBody(parent, msEmail.getBody());
		serializeAttachments(parent, msEmail.getAttachements());

		MSMessageClass messageClass = msEmail.getMessageClass();
		DOMUtils.createElementAndText(parent, ASEMAIL.MESSAGE_CLASS.asASValue(), messageClass.specificationValue());
		
		if (msEmail.getMeetingRequest() != null) {
			new MSMeetingRequestSerializer(intEncoder, parent, msEmail.getMeetingRequest()).serializeMSMeetingRequest();
			DOMUtils.createElementAndText(parent, ASEMAIL.CONTENT_CLASS.asASValue(), CALENDAR_CLASS);
		} else {
			DOMUtils.createElementAndText(parent, ASEMAIL.CONTENT_CLASS.asASValue(), MESSAGE_CLASS);
		}
		
		DOMUtils.createElementAndText(parent, ASEMAIL.CPID.asASValue(), CPID_DEFAULT);
		DOMUtils.createElementAndText(parent, ASAIRS.NATIVE_TYPE.asASValue(), msEmail.getBody().getBodyType().asXmlValue());
	}
	
	private void serializeBody(Element parent, MSEmailBody body) throws IOException {
		MSEmailBodyType bodyType = body.getBodyType();
		SerializableInputStream mimeData = body.getMimeData();
		Integer truncation = body.getTruncationSize();

		Element bodyElement = DOMUtils.createElement(parent, ASAIRS.BODY.asASValue());
		
		DOMUtils.createElementAndText(bodyElement, ASAIRS.DATA.asASValue(), mimeData);
		DOMUtils.createElementAndText(bodyElement, ASAIRS.TYPE.asASValue(), bodyType.asXmlValue());
		DOMUtils.createElementAndText(bodyElement, ASAIRS.TRUNCATED.asASValue(), body.isTruncated());
		DOMUtils.createElementAndTextIfNotNull(bodyElement, ASAIRS.ESTIMATED_DATA_SIZE.asASValue(), truncation.intValue());
	}

	private void serializeAttachments(Element parent, Set<MSAttachement> attachments) {
		if (!Iterables.isEmpty(attachments)) {
			Element atts = DOMUtils.createElement(parent, ASAIRS.ATTACHMENTS.asASValue());
			for (MSAttachement msAtt: attachments) {
				Element att = DOMUtils.createElement(atts, ASAIRS.ATTACHMENT.asASValue());
				DOMUtils.createElementAndText(att, ASAIRS.DISPLAY_NAME.asASValue(), msAtt.getDisplayName());
				DOMUtils.createElementAndText(att, ASAIRS.FILE_REFERENCE.asASValue(), msAtt.getFileReference());
				DOMUtils.createElementAndText(att, ASAIRS.METHOD.asASValue(), msAtt.getMethod().asIntString());
				DOMUtils.createElementAndText(att, ASAIRS.ESTIMATED_DATA_SIZE.asASValue(), 
						msAtt.getEstimatedDataSize().toString());
			}
		}
	}
}
