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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.field.FieldName;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.stream.Field;
import org.obm.push.bean.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

public class SendEmail {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Set<Address> to;
	private Set<Address> cc;
	private Set<Address> cci;
	private final String from;
	protected final Message originalMessage;
	protected Message message; 
	protected byte[] mimeData;
	private boolean hasFromField;
	private boolean invitation;

	public SendEmail(String defaultFrom, Message message) throws MimeException {
		Preconditions.checkNotNull(Strings.emptyToNull(defaultFrom));
		this.from = defaultFrom;
		this.originalMessage = message;
		
		setMessage(message);
	}

	private void lookForInvitation(Entity entity) {
		if (entity.isMultipart()) {
			Multipart multipart = (Multipart)entity.getBody();
			for (Entity part: multipart.getBodyParts()) {
				lookForInvitation(part);
			}
		} else {
			if (containsCalendarMethod(entity)) {
				invitation = true;
			}
		}
	}

	private boolean containsCalendarMethod(Entity entity) {
		if ("text/calendar".equals(entity.getMimeType())) {
			String body = entity.getHeader().getField(FieldName.CONTENT_TYPE).getBody();
			if (body.contains("method")) {
				return true;
			}
		}
		return false;
	}

	protected ByteArrayOutputStream serializeMimeData() {
		return serializeMimeData(message);
	}
	
	protected ByteArrayOutputStream serializeMimeData(Message message) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DefaultMessageWriter defaultMessageWriter = new DefaultMessageWriter();
		try {
			defaultMessageWriter.writeMessage(message, byteArrayOutputStream);
		} catch (IOException e) {
			//Won't append because we are using byteArrayStreams
			logger.error("error serializing a mime message", e);
		}
		return byteArrayOutputStream;
	}

	private void filterHeaders(org.apache.james.mime4j.dom.Header header) throws MimeException {
		ArrayList<Field> modifiedFields = new ArrayList<Field>();
		for (Field field: header) {
			Field filtered = filterField(field);
			if (filtered != null) {
				modifiedFields.add(filtered);
			}
		}
		if (!hasFromField) {
			Field newFrom = createFromField();
			if (newFrom != null) {
				modifiedFields.add(newFrom);
			}
		}
		for (Field field: modifiedFields) {
			header.setField(field);
		}
	}

	private Field filterField(Field field) throws MimeException {
		if (field.getName().equalsIgnoreCase("From")) {
			Field newFrom = createFromField();
			hasFromField = true;
			return newFrom;
		}
		return null;
	}

	private Field createFromField() throws MimeException {
		return DefaultFieldParser.parse("From: " + this.from);
	}

	private Iterable<Address> convertAddressListToRistretoAddresses(AddressList addressList) {
		if (addressList != null) {
			ArrayList<Address> list = new ArrayList<Address>();
			for (org.apache.james.mime4j.dom.address.Address source: addressList) {
				list.add(new Address(source.toString()));
			}
			return list;
		}
		return ImmutableList.<Address>of();
	}
	
	public Set<Address> getTo() {
		return to;
	}
	
	public Set<Address> getCc() {
		return cc;
	}

	public Set<Address> getCci() {
		return cci;
	}

	public String getFrom() {
		return from;
	}
	
	public boolean isInvitation() {
		return invitation;
	}

	public InputStream getMessage() {
		return new ByteArrayInputStream(mimeData);
	}
	
	/* package*/ Message getMimeMessage() {
		return message;
	}
	
	protected void setMessage(Message messageToSend) throws MimeException {
		this.message = messageToSend;
		this.to = Sets.newHashSet(convertAddressListToRistretoAddresses(message.getTo()));
		this.cc = Sets.newHashSet(convertAddressListToRistretoAddresses(message.getCc()));
		this.cci = Sets.newHashSet(convertAddressListToRistretoAddresses(message.getBcc()));
		filterHeaders(message.getHeader());
		lookForInvitation(message);
		this.hasFromField = false;
		this.mimeData = serializeMimeData().toByteArray();
	}
}
