package org.obm.push.mail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.descriptor.BodyDescriptor;
import org.apache.james.mime4j.field.Fields;
import org.apache.james.mime4j.field.MailboxListField;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.Field;
import org.columba.ristretto.composer.MimeTreeRenderer;
import org.columba.ristretto.io.ByteBufferSource;
import org.columba.ristretto.io.CharSequenceSource;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimeType;
import org.columba.ristretto.parser.AddressParser;
import org.columba.ristretto.parser.ParserException;
import org.minig.mime.QuotedPrintableDecoderInputStream;
import org.obm.push.utils.FileUtils;

public class SendEmailHandler implements ContentHandler {
	protected Header header;
	private BasicHeader basicHeader;
	protected LocalMimePart root;
	private MimeHeader rootMimeHeader;
	protected LocalMimePart currentMimePart;
	private Boolean inBody;
	private LocalMimePart localMimePart;
	private Set<Address> to;
	private Set<Address> cc;
	private Set<Address> cci;
	private String from;
	private boolean hasFromField;
	private boolean invitation;

	public SendEmailHandler(String defaultFrom) {
		this.from = defaultFrom;

		this.to = new HashSet<Address>();
		this.cc = new HashSet<Address>();
		this.cci = new HashSet<Address>();
		header = new Header();
		basicHeader = new BasicHeader(header);
		rootMimeHeader = new MimeHeader(header);
		root = new LocalMimePart(rootMimeHeader);
		currentMimePart = root;
		localMimePart = root;
		inBody = false;
		hasFromField = false;
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
		return MimeTreeRenderer.getInstance().renderMimePart(root);
	}

	@Override
	public void field(Field field) throws MimeException {
		if (!inBody) {
			if ("to".equalsIgnoreCase(field.getName())) {
				try {
					Address[] adds = AddressParser.parseMailboxList(field
							.getBody());
					for (Address add : adds) {
						this.to.add(add);
					}
				} catch (ParserException e) {
					throw new MimeException(e.getMessage());
				}
				basicHeader.set(field.getName(), field.getBody().trim());
			} else 	if ("CC".equalsIgnoreCase(field.getName())) {
				try {
					Address[] adds = AddressParser.parseMailboxList(field
							.getBody());
					for (Address add : adds) {
						this.cc.add(add);
					}
				} catch (ParserException e) {
					throw new MimeException(e.getMessage());
				}
				basicHeader.set(field.getName(), field.getBody().trim());
			} else  if ("BCC".equalsIgnoreCase(field.getName())) {
				try {
					Address[] adds = AddressParser.parseMailboxList(field
							.getBody());
					for (Address add : adds) {
						this.cci.add(add);
					}
				} catch (ParserException e) {
					throw new MimeException(e.getMessage());
				}
				basicHeader.set(field.getName(), field.getBody().trim());
			} else if ("from".equalsIgnoreCase(field.getName())) {
				hasFromField = true;
				if (field.getBody() == null || "".equals(field.getBody().trim())) {
					if (this.from != null && from.contains("@")) {
						basicHeader.set(field.getName(), this.from);
					}
				} else {
					this.from = field.getBody().trim();
					basicHeader.set(field.getName(), field.getBody().trim());
				}
			} else if ("Content-Type".equalsIgnoreCase(field.getName())) {
				String newBody = changeCharset(field.getBody());
				basicHeader.set(field.getName(), newBody.trim());
			} else {
				basicHeader.set(field.getName(), field.getBody().trim());
			}
		} else {
			if ("Content-Type".equalsIgnoreCase(field.getName())) {
				if(field.getBody().toLowerCase().contains("text/calendar") && field.getBody().toLowerCase().contains("method")){
					this.invitation = true;
				}
				String newBody = changeCharset(field.getBody());

				this.localMimePart.getHeader().set(field.getName(),
						newBody.trim());
			} else {
				this.localMimePart.getHeader().set(field.getName(),
						field.getBody().trim());
			} 
		}
	}

	private String changeCharset(String contentType) {
		String newBody = contentType;
		if (!contentType.contains("ascii")) {
			String oldBody = contentType;

			int is = oldBody.indexOf("charset=");
			if (is >= 0) {
				is += "charset=".length();
				int ie = oldBody.indexOf(";", is);
				if (ie == -1) {
					ie = oldBody.length();
				}
				newBody = oldBody.substring(0, is) + "utf-8"
						+ oldBody.substring(ie);
			}
		}
		return newBody;
	}

	private MimeType parseMultiPart(String body) {
		String[] tab = body.split(";");
		for (String part : tab) {
			if (part.toLowerCase().contains("multipart")) {
				int i = part.indexOf("/");
				if (i > 0) {
					String type = part.substring(0, i);
					String subType = part.substring(i + 1);
					return new MimeType(type, subType);
				}
			}
		}
		return null;
	}

	@Override
	public void startMultipart(BodyDescriptor arg0) throws MimeException {
		MimeType mt = parseMultiPart(arg0.getMimeType());
		this.currentMimePart.getHeader().setMimeType(mt);
	}

	@Override
	public void endMultipart() throws MimeException {
		if (this.currentMimePart.getParent() != null) {
			this.currentMimePart = (LocalMimePart) currentMimePart.getParent();
		}
	}

	@Override
	public void startBodyPart() throws MimeException {
		this.inBody = true;
		MimeHeader localMimeHeader = new MimeHeader();
		localMimePart = new LocalMimePart(localMimeHeader);
		currentMimePart.addChild(this.localMimePart);
		this.currentMimePart = localMimePart;
	}

	@Override
	public void endBodyPart() throws MimeException {
		if (this.currentMimePart.getParent() != null) {
			this.currentMimePart = (LocalMimePart) currentMimePart.getParent();
		}
	}

	@Override
	public void body(BodyDescriptor arg0, InputStream arg1)
			throws MimeException, IOException {
		Charset charset = null;
		try {
			charset = Charset.forName(arg0.getCharset());
		} catch (Throwable e) {
		}

		if ("QUOTED-PRINTABLE".equalsIgnoreCase(arg0.getTransferEncoding())) {
			arg1 = new QuotedPrintableDecoderInputStream(arg1);
		}
		if ("base64".equalsIgnoreCase(arg0.getTransferEncoding())) {
			byte[] b = FileUtils.streamBytes(new Base64InputStream(arg1), false);
			appendToBody(b);
		} else {
			if (charset == null) {
				charset = Charset.forName("UTF-8");
			}
			byte[] value = FileUtils.streamBytes(arg1, false);
			appendToBody(new String(value, charset));
		}
	}

	private void appendToBody(String value) {
		if (value != null && !value.isEmpty() && localMimePart != null) {
			CharSequence seq = localMimePart.getBody();
			StringBuilder sb = new StringBuilder(seq);
			sb.append(value);
			sb.append("\r\n");
			CharSequenceSource css = new CharSequenceSource(sb.toString());
			localMimePart.setBody(css);
		}
	}

	private void appendToBody(byte[] value) {
		if (value != null && localMimePart != null) {
			ByteBufferSource bbs = new ByteBufferSource(value);
			localMimePart.setBody(bbs);
		}
	}

	@Override
	public void startHeader() throws MimeException {
	}

	@Override
	public void endHeader() throws MimeException {
		if (!inBody && !hasFromField) {
			if (this.from != null && from.contains("@")) {
				String[] tab = from.split("@");
				Mailbox mb = new Mailbox(tab[0], tab[1]);
				MailboxListField arg0 = Fields.from(mb);
				basicHeader.set(arg0.getName(), arg0.getBody().trim());
			}
			
		}
	}

	@Override
	public void endMessage() throws MimeException {
	}

	@Override
	public void epilogue(InputStream arg0) throws MimeException, IOException {
		String value = new String(FileUtils.streamBytes(arg0, false));
		appendToBody(value);
	}

	@Override
	public void preamble(InputStream arg0) throws MimeException, IOException {
		String value = new String(FileUtils.streamBytes(arg0, false));
		appendToBody(value);
	}

	@Override
	public void raw(InputStream arg0) throws MimeException, IOException {
		String value = new String(FileUtils.streamBytes(arg0, false));
		appendToBody(value);
	}

	@Override
	public void startMessage() throws MimeException {
	}
}
