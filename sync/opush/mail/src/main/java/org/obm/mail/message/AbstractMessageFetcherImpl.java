package org.obm.mail.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

import org.apache.james.mime4j.codec.Base64InputStream;
import org.minig.imap.IMAPHeaders;
import org.minig.imap.command.parser.HeadersParser;
import org.minig.imap.mime.IMimePart;
import org.minig.imap.mime.MimeAddress;
import org.minig.imap.mime.MimeMessage;
import org.minig.mime.QuotedPrintableDecoderInputStream;
import org.obm.mail.conversation.MailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractMessageFetcherImpl implements MessageFetcher {

	private static final Logger logger = LoggerFactory
			.getLogger(AbstractMessageFetcherImpl.class);

	public AbstractMessageFetcherImpl() {
		super();
	}
	
	protected abstract InputStream uidFetchPart(MimeMessage message, String part);
	
	protected abstract InputStream uidFetchPart(MimeMessage message, IMimePart mimePart);

	protected abstract InputStream uidFetchPart(MailMessage message, IMimePart mimePart);


	@Override
	public IMAPHeaders fetchPartHeaders(MimeMessage message, IMimePart mimePart) throws IOException {
		MimeAddress messageAddress = mimePart.getAddress();
		String part = null;
		if (messageAddress == null) {
			part = "HEADER";
		} else {
			part = messageAddress.toString() + ".HEADER";
		}
		InputStream is = uidFetchPart(message, part);
		InputStreamReader reader = new InputStreamReader(is, getHeaderCharsetDecoder(mimePart));
		Map<String, String> rawHeaders = new HeadersParser().parseRawHeaders(reader);
		IMAPHeaders h = new IMAPHeaders();
		h.setRawHeaders(rawHeaders);
		h.setUid(message.getUid());
		return h;
	}

	/**
	 * Tries to return a suitable {@link Charset} to decode the headers
	 */
	private Charset getHeaderCharsetDecoder(IMimePart part) {
		String encoding = part.getContentTransfertEncoding();
		if (encoding == null) {
			return Charset.forName("utf-8");
		} else if (encoding.equalsIgnoreCase("8bit")) {
			return Charset.forName("iso-8859-1");
		} else {
			try {
				return Charset.forName(encoding);
			} catch (UnsupportedCharsetException uee) {
				logger.debug("illegal charset: " + encoding + ", defaulting to utf-8");
				return Charset.forName("utf-8");
			}
		}
	}
	
	@Override
	public InputStream fetchPart(MimeMessage message, IMimePart mimePart) throws IOException {
		InputStream encodedStream = uidFetchPart(message, mimePart);
		return decodeStream(mimePart, encodedStream);
	}

	@Override
	public InputStream fetchPart(MailMessage message, IMimePart mimePart) throws IOException {
		InputStream encodedStream = uidFetchPart(message, mimePart);
		return decodeStream(mimePart, encodedStream);
	}

	protected InputStream decodeStream(IMimePart chosenPart, InputStream encodedStream) {
		
		if ("QUOTED-PRINTABLE".equals(chosenPart.getContentTransfertEncoding())) {
			return new QuotedPrintableDecoderInputStream(encodedStream);
		} else if ("BASE64".equals(chosenPart.getContentTransfertEncoding())) {
			return new Base64InputStream(encodedStream);
		}
		return encodedStream;
	}
	
	
}
