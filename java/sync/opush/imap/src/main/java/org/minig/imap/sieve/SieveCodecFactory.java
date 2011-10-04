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

package org.minig.imap.sieve;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SieveCodecFactory implements ProtocolCodecFactory {
	
	private static final Logger logger = LoggerFactory
			.getLogger(SieveClientSupport.class);

	private ProtocolDecoder decoder = new ProtocolDecoderAdapter() {

		@Override
		public void decode(IoSession arg0, ByteBuffer arg1,
				ProtocolDecoderOutput arg2) throws Exception {
			java.nio.ByteBuffer received = arg1.buf();
			java.nio.ByteBuffer copy = java.nio.ByteBuffer.allocate(received
					.remaining());
			copy.put(received);
			// copy.flip();
			byte[] data = copy.array();
			if (logger.isDebugEnabled()) {
				logger.debug("decoded: " + new String(data));
			}
			SieveMessage sm = new SieveMessage();
			sm.addLine(new String(data));
			arg2.write(sm);
		}
	};

	private ProtocolEncoder encoder = new ProtocolEncoderAdapter() {

		@Override
		public void encode(IoSession arg0, Object arg1,
				ProtocolEncoderOutput arg2) throws Exception {
			byte[] raw = (byte[]) arg1;
			ByteBuffer b = ByteBuffer.wrap(raw);
			arg2.write(b);
		}
	};

	@Override
	public ProtocolDecoder getDecoder() throws Exception {
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder() throws Exception {
		return encoder;
	}

}
