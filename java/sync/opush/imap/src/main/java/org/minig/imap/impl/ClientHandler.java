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

import org.apache.mina.common.IoFilter;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.minig.imap.IMAPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler extends IoHandlerAdapter {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	private final IResponseCallback callback;

	private IoFilter getIoFilter() {
		ProtocolCodecFactory pcf = new IMAPCodecFactory();
		return new ProtocolCodecFilter(pcf);
	}

	public ClientHandler(IResponseCallback callback) {
		this.callback = callback;
	}

	public void sessionCreated(IoSession session) throws Exception {
		session.getFilterChain().addLast("codec", getIoFilter());
	}

	public void sessionOpened(IoSession session) throws Exception {
		callback.connected();
	}

	public void messageReceived(IoSession session, Object message)
			throws Exception {
		MinaIMAPMessage msg = (MinaIMAPMessage) message;
		callback.imapResponse(msg);
	}

	public void sessionClosed(IoSession session) throws Exception {
		callback.disconnected();
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		logger.error(cause.getMessage(),cause);
		callback.exceptionCaught(new IMAPException(cause.getMessage(),cause));
	}
	
	

}
