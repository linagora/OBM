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

package org.minig.imap.idle;

import java.util.ArrayList;
import java.util.LinkedList;

import org.minig.imap.IMAPException;
import org.minig.imap.impl.ClientSupport;
import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.IMAPResponseParser;
import org.minig.imap.impl.IResponseCallback;
import org.minig.imap.impl.MinaIMAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdleClientCallback implements IResponseCallback {

	private static final Logger logger = LoggerFactory
			.getLogger(IdleClientCallback.class);
	
	private LinkedList<IMAPResponse> responses;
	private IIdleCallback observer;
	private IdleResponseParser rParser;
	private IMAPResponseParser imrParser;
	private ClientSupport cs;
	private Boolean isStart;

	public IdleClientCallback() {
		this.rParser = new IdleResponseParser();
		this.imrParser = new IMAPResponseParser();
		isStart = false;
		this.responses = new LinkedList<IMAPResponse>();
	}

	@Override
	public void connected() {
		logger.info("connected() callback called.");
		imrParser.setServerHelloReceived(false);
	}

	@Override
	public void disconnected() {
		logger.info("disconnected() callback called.");
		if (observer != null) {
			observer.disconnectedCallBack();
		}
		this.isStart = false;
	}

	@Override
	public void imapResponse(MinaIMAPMessage imapResponse) {
		if (isStart) {
			if (observer != null) {
				IdleLine rp = null;
				try {
					rp = rParser.parse(imapResponse);
				} catch (RuntimeException re) {
					logger.warn("Runtime exception on: " + imapResponse);
					throw re;
				}
				observer.receive(rp);
			}
		} else {
			IMAPResponse rp = null;
			try {

				rp = imrParser.parse(imapResponse);
			} catch (RuntimeException re) {
				logger.warn("Runtime exception on: " + imapResponse);
				throw re;
			}
			responses.add(rp);
			if (rp.isClientDataExpected()) {
				ArrayList<IMAPResponse> rs = new ArrayList<IMAPResponse>(
						responses.size());
				rs.addAll(responses);
				responses.clear();
				cs.setResponses(rs);
			}
		}
		if (imapResponse.getMessageLine().toLowerCase().startsWith("+ idling")) {
			isStart = true;
		}
	}

	public void attachIdleCallback(IIdleCallback observer) {
		this.observer = observer;
	}

	public void setClient(ClientSupport cs) {
		this.cs = cs;
	}

	public boolean isStart() {
		return isStart;
	}

	public void stopIdle() {
		this.isStart = false;
	}

	@Override
	public void exceptionCaught(IMAPException cause) throws IMAPException {
		logger.error(cause.getMessage(), cause);
	}

	public void detachIdleCallback() {
		this.observer = null;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

}
