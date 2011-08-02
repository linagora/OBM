package org.obm.push.mail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.descriptor.BodyDescriptor;
import org.columba.ristretto.io.CharSequenceSource;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.minig.mime.QuotedPrintableDecoderInputStream;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.utils.FileUtils;

public class ReplyEmailHandler extends SendEmailHandler {

	private MSEmail originMail;

	public ReplyEmailHandler(String defaultFrom, MSEmail originMail) {
		super(defaultFrom);
		this.originMail = originMail;

	}

	@Override
	public void body(BodyDescriptor arg0, InputStream arg1)
			throws MimeException, IOException {
		MimeHeader mimeHeader = new MimeHeader(header);
		root = new LocalMimePart(mimeHeader);
		StringBuilder body = new StringBuilder();

		if ("QUOTED-PRINTABLE".equalsIgnoreCase(arg0.getTransferEncoding())) {
			arg1 = new QuotedPrintableDecoderInputStream(arg1);
		}

		if ("base64".equalsIgnoreCase(arg0.getTransferEncoding())) {
			Charset charset = null;
			try {
				charset = Charset.forName(arg0.getCharset());
			} catch (Throwable e) {
			}
			byte[] b = FileUtils.streamBytes(arg1, false);
			byte[] bb = Base64.decodeBase64(b);
			if (charset == null) {
				body.append(new String(bb));
			} else {
				body.append(new String(bb, charset));
			}
		} else {
			body.append(FileUtils.streamString(arg1, false));
		}

		String oldBody = this.originMail.getBody().getValue(
				MSEmailBodyType.PlainText);
		if (oldBody != null) {
			body.append("\n");
			for(String next : oldBody.split("\n")){
				body.append("> "+next);
			}
		}
		CharSequenceSource css = new CharSequenceSource(body.toString().trim());
		root.setBody(css);
	}
}
