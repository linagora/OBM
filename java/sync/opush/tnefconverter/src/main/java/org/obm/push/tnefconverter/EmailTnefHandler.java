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
package org.obm.push.tnefconverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.freeutils.tnef.Message;
import net.freeutils.tnef.TNEFInputStream;
import net.freeutils.tnef.TNEFUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import org.obm.push.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

public class EmailTnefHandler implements ContentHandler {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private Message tnefMsg;

	@Override
	public void body(BodyDescriptor arg0, InputStream arg1) throws MimeException, IOException {
		if (TNEFUtils.isTNEFMimeType(arg0.getMimeType())) {
			byte[] bb = FileUtils.streamBytes(arg1, false);
			try {
				if ("base64".equalsIgnoreCase(arg0.getTransferEncoding())) {
					bb = Base64.decodeBase64(new String(bb, Charsets.UTF_8));
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

	public Message getTNEFMsg() {
		return tnefMsg;
	}

	@Override
	public void field(Field field) throws MimeException {
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
