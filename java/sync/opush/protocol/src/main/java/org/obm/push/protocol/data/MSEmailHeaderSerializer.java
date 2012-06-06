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
package org.obm.push.protocol.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.protocol.data.ms.MSEmailEncoder;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;

public class MSEmailHeaderSerializer {

	private final Element element;
	private final MSEmailHeader msEmailHeader;

	public MSEmailHeaderSerializer(Element parentElement, MSEmailHeader msEmailHeader) {
		this.element = parentElement;
		this.msEmailHeader = msEmailHeader;
	}

	public void serializeMSEmailHeader() {
		Preconditions.checkNotNull(msEmailHeader, "The msEmailHeader is required");
		
		serializeTo();
		serializeCC();
		serializeFrom();
		serializeReplyTo();
		serializeSubject();
		serializeDateReceived();
		serializeDisplayTo();
	}

	private void serializeTo() {
		DOMUtils.createElementAndTextIfNotNull(element, 
				ASEMAIL.TO.asASValue(), formatMSAddresses(msEmailHeader.getTo()));
	}
	
	private void serializeCC() {
		DOMUtils.createElementAndTextIfNotNull(element, 
				ASEMAIL.CC.asASValue(), formatMSAddresses(msEmailHeader.getCc()));
	}
	
	private void serializeFrom() {
		DOMUtils.createElementAndTextIfNotNull(element, 
				ASEMAIL.FROM.asASValue(), formatMSAddresses(msEmailHeader.getFrom()));
	}
	
	private void serializeReplyTo() {
		DOMUtils.createElementAndTextIfNotNull(element, 
				ASEMAIL.REPLY_TO.asASValue(), formatMSAddresses(msEmailHeader.getReplyTo()));
	}
	
	private void serializeSubject() {
		DOMUtils.createElementAndTextIfNotNull(element,
				ASEMAIL.SUBJECT.asASValue(), msEmailHeader.getSubject());
	}
	
	private void serializeDateReceived() {
		DOMUtils.createElementAndTextIfNotNull(element,
				ASEMAIL.DATE_RECEIVED.asASValue(), formatDate(msEmailHeader.getDate()));
	}

	private void serializeDisplayTo() {
		if (msEmailHeader.getDisplayTo() != null) {
			DOMUtils.createElementAndText(element, 
					ASEMAIL.DISPLAY_TO.asASValue(), msEmailHeader.getDisplayTo().toMSProtocol());
		}
	}
	
	public static String formatMSAddresses(List<MSAddress> addresses) {
		if (addresses != null && !addresses.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (Iterator<MSAddress> it = addresses.iterator(); it.hasNext();) {
				MSAddress addr = it.next();
				sb.append(addr.toMSProtocol());
				if (it.hasNext()) {
					sb.append(",");
				}
			}
			return sb.toString();
		} else {
			return null;
		}
	}
	
	public static String formatDate(Date date) {
		if (date != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(MSEmailEncoder.UTC_DATE_PATTERN);
			return dateFormat.format(date);
		} else {
			return null;
		}
	}
}
