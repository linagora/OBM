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

import java.util.ArrayList;
import java.util.LinkedList;

import org.minig.imap.IMAPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreClientCallback implements IResponseCallback {

	private final static Logger logger = LoggerFactory
			.getLogger(MessageSet.class);

	IMAPResponseParser rParser;
	private LinkedList<IMAPResponse> responses;
	private ClientSupport client;

	public StoreClientCallback() {
		this.rParser = new IMAPResponseParser();
		this.responses = new LinkedList<IMAPResponse>();
	}

	@Override
	public void connected() {
		logger.info("connected() callback called.");
		rParser.setServerHelloReceived(false);
	}

	@Override
	public void disconnected() {
		logger.info("disconnected() callback called.");
	}

	@Override
	public void imapResponse(MinaIMAPMessage imapResponse) {
		IMAPResponse rp = null;
		try {
			rp = rParser.parse(imapResponse);
		} catch (RuntimeException re) {
			logger.warn("Runtime exception on: " + imapResponse);
			throw re;
		}

		responses.add(rp);

		if (rp.isClientDataExpected()) {
			ArrayList<IMAPResponse> rs = new ArrayList<IMAPResponse>(responses.size());
			rs.addAll(responses);
			responses.clear();
			client.setResponses(rs);
		}
	}

	@Override
	public void setClient(ClientSupport cs) {
		this.client = cs;
	}

	@Override
	public void exceptionCaught(IMAPException cause) throws IMAPException {
		throw cause;
	}

}
