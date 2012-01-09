package org.obm.push.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.stream.Field;
import org.obm.push.bean.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			if (entity.getMimeType().equalsIgnoreCase("text/calendar")) {
				invitation = true;
			}
		}
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
			hasFromField = true;
			if (StringUtils.isNotBlank(field.getBody())) {
				Field newFrom = createFromField();
				if (newFrom != null) {
					return newFrom;
				}
			}
		}
		
		return null;
	}

	private Field createFromField() throws MimeException {
		if (this.from != null && from.contains("@")) {
			return DefaultFieldParser.parse("From: " + this.from);
		}
		return null;
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
