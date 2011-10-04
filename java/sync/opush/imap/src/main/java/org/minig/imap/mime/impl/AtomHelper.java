package org.minig.imap.mime.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.obm.push.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AtomHelper {

	private final static Logger logger = LoggerFactory
			.getLogger(AtomHelper.class);

	public static final String getFullResponse(String resp, InputStream followUp) {
		String orig = resp;
		byte[] envelData = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		try {
			out.write(orig.getBytes());
			if (followUp != null) {
				FileUtils.transfer(followUp, out, true);
			}
		} catch (IOException e) {
			logger.error("error loading stream part of answer", e);
		}
		envelData = out.toByteArray();
		return new String(envelData, Charset.forName("ASCII"));
	}

}
