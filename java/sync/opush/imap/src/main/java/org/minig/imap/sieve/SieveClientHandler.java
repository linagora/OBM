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

import java.util.ArrayList;

import org.apache.mina.common.IoFilter;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SieveClientHandler extends IoHandlerAdapter {

	private static final Logger logger = LoggerFactory
			.getLogger(SieveClientHandler.class);

	private SieveResponseParser srp = new SieveResponseParser();

	private SieveClientSupport cs;

	private IoFilter getSieveFilter() {
		ProtocolCodecFactory pcf = new SieveCodecFactory();
		return new ProtocolCodecFilter(pcf);
	}

	public SieveClientHandler(SieveClientSupport cb) {
		// TODO Auto-generated constructor stub
		this.cs = cb;
	}

	public void sessionCreated(IoSession session) throws Exception {
		session.getFilterChain().addLast("codec", getSieveFilter());
	}

	public void sessionOpened(IoSession session) throws Exception {
	}

	public void messageReceived(IoSession session, Object message)
			throws Exception {
		SieveMessage msg = (SieveMessage) message;
		ArrayList<SieveResponse> copy = new ArrayList<SieveResponse>(10);
		srp.parse(copy, msg);
		cs.setResponses(copy);
	}

	public void sessionClosed(IoSession session) throws Exception {
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		logger.error(cause.getMessage(), cause);
		throw new SieveException(cause.getMessage(), cause);
	}

}
