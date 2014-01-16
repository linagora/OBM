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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.obm.push.bean.MSAddress;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;

public class MSEmailHeaderSerializer {

	private final Element element;
	private final MSEmail msEmail;

	public MSEmailHeaderSerializer(Element parentElement, MSEmail msEmail) {
		this.element = parentElement;
		this.msEmail = msEmail;
	}

	public void serializeMSEmailHeader() {
		Preconditions.checkNotNull(msEmail, "The msEmailHeader is required");
		
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
				ASEmail.TO.asASValue(), formatMSAddresses(msEmail.getTo()));
	}
	
	private void serializeCC() {
		DOMUtils.createElementAndTextIfNotNull(element, 
				ASEmail.CC.asASValue(), formatMSAddresses(msEmail.getCc()));
	}
	
	private void serializeFrom() {
		DOMUtils.createElementAndTextIfNotNull(element, 
				ASEmail.FROM.asASValue(), formatMSAddresses(msEmail.getFrom()));
	}
	
	private void serializeReplyTo() {
		DOMUtils.createElementAndTextIfNotNull(element, 
				ASEmail.REPLY_TO.asASValue(), formatMSAddresses(msEmail.getReplyTo()));
	}
	
	private void serializeSubject() {
		DOMUtils.createElementAndTextIfNotNull(element,
				ASEmail.SUBJECT.asASValue(), msEmail.getSubject());
	}
	
	private void serializeDateReceived() {
		DOMUtils.createElementAndTextIfNotNull(element,
				ASEmail.DATE_RECEIVED.asASValue(), formatDate(msEmail.getDate()));
	}

	private void serializeDisplayTo() {
		if (msEmail.getDisplayTo() != null) {
			DOMUtils.createElementAndText(element, 
					ASEmail.DISPLAY_TO.asASValue(), msEmail.getDisplayTo().toMSProtocol());
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
