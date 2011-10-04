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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.obm.push.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IMAPLineDecoder implements ProtocolDecoder {

	private final static Logger logger = LoggerFactory
			.getLogger(IMAPLineDecoder.class);
	
	private final static byte[] delimBuf = new byte[] { (byte) '\r',
			(byte) '\n' };

	private static final int MAX_PACKET_SIZE = 10 * 1024 * 1024;

	private static final String CONTEXT = IMAPLineDecoder.class.getName()
			+ ".context";

	public IMAPLineDecoder() {
		super();
	}

	@Override
	public void decode(IoSession session, ByteBuffer in,
			ProtocolDecoderOutput out) throws Exception {

		ImapLineContext lc = (ImapLineContext) session.getAttribute(CONTEXT);
		if (lc == null) {
			lc = new ImapLineContext();
			session.setAttribute(CONTEXT, lc);
		}
		lc.decodeNormal(in, out);
	}

	@Override
	public void dispose(IoSession session) throws Exception {
		ImapLineContext lc = (ImapLineContext) session.removeAttribute(CONTEXT);
		if (lc != null) {
			lc.dispose();
		}
	}

	private class ImapLineContext {

		private final ByteBuffer buf;

		private java.nio.ByteBuffer literalBuffer;
		private int matchCount = 0;

		private MinaIMAPMessage currentMessage;

		private ImapLineContext() {
			buf = ByteBuffer.allocate(80).setAutoExpand(true);
		}

		public void dispose() {
			buf.release();
		}

		public boolean bufferFollows(String line) {
			int endPos = line.length() - 1;
			if (endPos < 0) {
				return false;
			}
			if (line.charAt(endPos) == '}') {
				endPos--;
				if (line.charAt(endPos) == '+') {
					endPos--;
				}
				int numberStart = endPos;
				for (; numberStart > 0 && numeric(line.charAt(numberStart)); numberStart--) {

				}
				String number = line.substring(numberStart + 1, endPos + 1);

				int literalSize = Integer.parseInt(number);
				if (literalSize > MAX_PACKET_SIZE) {
					//OBMFULL-1944
					logger.warn("Literal " + literalSize
							+ " is bigger than the allowed max:"
							+ MAX_PACKET_SIZE);
				}
				literalBuffer = java.nio.ByteBuffer.allocate(literalSize);

				return true;
			}
			return false;
		}

		private void decodeNormal(ByteBuffer in, ProtocolDecoderOutput out) {

			if (literalBuffer != null) {
				// we are in a middle of a literal
				appendBuffer(in);
			}

			// Try to find a match
			int oldPos = in.position();
			int oldLimit = in.limit();
			while (in.hasRemaining()) {
				byte b = in.get();
				if (delimBuf[matchCount] == b) {
					matchCount++;
					if (matchCount == delimBuf.length) {
						// Found a match.
						int pos = in.position();
						in.limit(pos);
						in.position(oldPos);

						buf.put(in);
						buf.flip();
						buf.limit(buf.limit() - matchCount);

						InputStream inStream = buf.asInputStream();
						ByteArrayOutputStream bout = new ByteArrayOutputStream();
						try {
							FileUtils.transfer(inStream, bout, false);
						} catch (IOException e) {
							logger.error(e.getMessage(), e);
						}
						byte[] line = bout.toByteArray();
						String lineAsString = new String(line);

						buf.clear();
						in.limit(oldLimit);
						in.position(pos);
						oldPos = pos;
						matchCount = 0;

						if (currentMessage != null) {
							currentMessage.addLine(line);
						} else {
							currentMessage = new MinaIMAPMessage(lineAsString);
						}

						// a literal string follows?
						if (bufferFollows(lineAsString)) {
							appendBuffer(in);
						} else {
							out.write(currentMessage);
							currentMessage = null;
						}
					}
				} else {
					matchCount = 0;
				}
			}

			// Put remainder to buf.
			in.position(oldPos);
			buf.put(in);

		}

		public void appendBuffer(ByteBuffer in) {
			java.nio.ByteBuffer receivedBuffer = in.buf();

			int arrivedDataSize = receivedBuffer.remaining();
			int expectedMaximum = literalBuffer.remaining();
			if (arrivedDataSize > expectedMaximum) {
				// the last ByteBuffer is contains more data, than we expected
				// for this literal, this can be the next command,
				// or it's just multiple literal in the same command
				int oldLimit = receivedBuffer.limit();
				receivedBuffer.limit(expectedMaximum
						+ receivedBuffer.position());
				literalBuffer.put(receivedBuffer);
				receivedBuffer.limit(oldLimit);
			} else {
				literalBuffer.put(receivedBuffer);
			}

			if (literalBuffer.remaining() == 0) {
				// we have read the literal,
				// add the buffer to the already built command
				currentMessage.addBuffer(literalBuffer);
				literalBuffer = null;

				// we should process the remaining as a string command:
				// recursively we call ourself, to decode the line:
			}

		}

	}

	final static boolean numeric(char charAt) {
		// fast and ugly hack
		int x = charAt - '0';
		return x >= 0 && x <= 9;
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out)
			throws Exception {
		// TODO Auto-generated method stub
	}

}
