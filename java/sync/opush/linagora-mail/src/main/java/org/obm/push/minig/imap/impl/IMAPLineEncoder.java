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

package org.obm.push.minig.imap.impl;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.textline.LineDelimiter;

public class IMAPLineEncoder extends ProtocolEncoderAdapter {
	private static final String ENCODER = IMAPLineEncoder.class.getName()
			+ ".encoder";

	private final Charset charset;

	private final LineDelimiter delimiter;

	public IMAPLineEncoder() {
		this(Charset.forName("US-ASCII"), LineDelimiter.WINDOWS);
	}

	private IMAPLineEncoder(Charset charset, LineDelimiter delimiter) {
		if (charset == null) {
			throw new NullPointerException("charset");
		}
		if (delimiter == null) {
			throw new NullPointerException("delimiter");
		}
		if (LineDelimiter.AUTO.equals(delimiter)) {
			throw new IllegalArgumentException(
					"AUTO delimiter is not allowed for encoder.");
		}

		this.charset = charset;
		this.delimiter = delimiter;
	}

	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out) throws Exception {
		CharsetEncoder encoder = (CharsetEncoder) session.getAttribute(ENCODER);
		if (encoder == null) {
			encoder = charset.newEncoder();
			session.setAttribute(ENCODER, encoder);
		}
		if (message instanceof String) {
			String value = message.toString();
			IoBuffer buf = IoBuffer.allocate(value.length() + 2);
			buf.putString(value, encoder);
			buf.putString(delimiter.getValue(), encoder);
			buf.flip();
			out.write(buf);
		} else if (message instanceof byte[]) {
			byte[] value = (byte[]) message;
			IoBuffer buf = IoBuffer.allocate(value.length + 2);
			buf.put(value);
			buf.putString(delimiter.getValue(), encoder);
			buf.flip();
			out.write(buf);
		}
	}

	public void dispose() {
	}
}
