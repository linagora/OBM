/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.minig.imap.impl;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
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
			ByteBuffer buf = ByteBuffer.allocate(value.length() + 2);
			buf.putString(value, encoder);
			buf.putString(delimiter.getValue(), encoder);
			buf.flip();
			out.write(buf);
		} else if (message instanceof byte[]) {
			byte[] value = (byte[]) message;
			ByteBuffer buf = ByteBuffer.allocate(value.length + 2);
			buf.put(value);
			buf.putString(delimiter.getValue(), encoder);
			buf.flip();
			out.write(buf);
		}
	}

	public void dispose() {
	}
}
