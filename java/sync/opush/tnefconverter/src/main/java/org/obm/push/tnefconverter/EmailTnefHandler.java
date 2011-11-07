package org.obm.push.tnefconverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import net.freeutils.tnef.Message;
import net.freeutils.tnef.TNEFInputStream;
import net.freeutils.tnef.TNEFUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.columba.ristretto.coder.EncodedWord;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.parser.AddressParser;
import org.columba.ristretto.parser.ParserException;
import org.obm.push.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailTnefHandler implements ContentHandler {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private Set<Address> to;
	private Set<Address> cc;
	private Address from;
	private String subject;

	private Message tnefMsg;
	private InputStream tnefDoc;

	private String threadTopic;

	public EmailTnefHandler() {
		this.to = new HashSet<Address>();
		this.cc = new HashSet<Address>();
	}

	public Address getFrom() {
		return from;
	}

	public Set<Address> getTo() {
		return to;
	}

	public Set<Address> getCc() {
		return cc;
	}

	public String getSubject() {
		return threadTopic != null && threadTopic.length()>0 ? threadTopic : subject;
	}

	public Message getTNEFMsg() {
		return tnefMsg;
	}

	public InputStream getTnefDoc() {
		return tnefDoc;
	}

	@Override
	public void field(Field field) throws MimeException {
		if ("subject".equalsIgnoreCase(field.getName())) {
			this.subject = EncodedWord.decode(field.getBody()).toString().trim();
		} else if ("thread-topic".equalsIgnoreCase(field.getName())) {
			this.threadTopic = EncodedWord.decode(field.getBody()).toString().trim();
		} else if ("to".equalsIgnoreCase(field.getName())) {
			try {
				Address[] adds = AddressParser.parseMailboxList(field.getBody());
				for (Address add : adds) {
					this.to.add(add);
				}
			} catch (ParserException e) {
				throw new MimeException(e.getMessage());
			}
		} else if ("cc".equalsIgnoreCase(field.getName())) {
			try {
				Address[] adds = AddressParser.parseMailboxList(field.getBody());
				for (Address add : adds) {
					this.cc.add(add);
				}
			} catch (ParserException e) {
				throw new MimeException(e.getMessage());
			}
		} else if ("from".equalsIgnoreCase(field.getName())) {
			try {
				this.from = AddressParser.parseAddress(field.getBody());
			} catch (ParserException e) {
				throw new MimeException(e.getMessage());
			}
		}
	}

	@Override
	public void body(BodyDescriptor arg0, InputStream arg1)
			throws MimeException, IOException {
		if (TNEFUtils.isTNEFMimeType(arg0.getMimeType())) {
			byte[] bb = FileUtils.streamBytes(arg1, false);
			this.tnefDoc = new ByteArrayInputStream(bb);
			try {
				if ("base64".equalsIgnoreCase(arg0.getTransferEncoding())) {
					bb = Base64.decodeBase64(new String(bb));
					arg1 = new ByteArrayInputStream(bb);
				}
				TNEFInputStream tnef = new TNEFInputStream(arg1);
				this.tnefMsg = new Message(tnef);
			} catch (Exception e) {
				storeTnef(bb);
				throw new MimeException(e);
			}
		}
	}

	private void storeTnef(byte[] b) {
		FileOutputStream fout = null;
		try {
			if (b != null) {
				File tmp = File.createTempFile("debug_", ".tnef");
				fout = new FileOutputStream(tmp);
				fout.write(b);
				logger.error("unparsable tnef saved in " + tmp.getAbsolutePath());
			}
		} catch (Throwable t) {
			logger.error("error storing debug file", t);
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					logger.error("error storing debug file", e);
				}
			}
		}
	}

	@Override
	public void startMultipart(BodyDescriptor arg0) throws MimeException {
	}

	@Override
	public void endMultipart() throws MimeException {
	}

	@Override
	public void startBodyPart() throws MimeException {
	}

	@Override
	public void endBodyPart() throws MimeException {
	}

	@Override
	public void startHeader() throws MimeException {
	}

	@Override
	public void endHeader() throws MimeException {
	}

	@Override
	public void endMessage() throws MimeException {
	}

	@Override
	public void epilogue(InputStream arg0) throws MimeException, IOException {
	}

	@Override
	public void preamble(InputStream arg0) throws MimeException, IOException {
	}

	@Override
	public void raw(InputStream arg0) throws MimeException, IOException {
	}

	@Override
	public void startMessage() throws MimeException {
	}
}
