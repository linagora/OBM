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

package org.minig.imap.command;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.minig.imap.Address;
import org.minig.imap.EncodedWord;
import org.minig.imap.Envelope;
import org.minig.imap.impl.DateParser;
import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.MessageSet;
import org.minig.imap.mime.impl.AtomHelper;
import org.minig.imap.mime.impl.ParenListParser;
import org.minig.imap.mime.impl.ParenListParser.TokenType;

/**
 * @author tom
 * 
 */
public class UIDFetchEnvelopeCommand extends Command<Collection<Envelope>> {

	private Collection<Long> uids;

	public UIDFetchEnvelopeCommand(Collection<Long> uid) {
		this.uids = uid;
	}

	@Override
	protected CommandArgument buildCommand() {
		StringBuilder sb = new StringBuilder();
		if (!uids.isEmpty()) {
			sb.append("UID FETCH ");
			sb.append(MessageSet.asString(uids));
			sb.append(" (UID ENVELOPE)");
		} else {
			sb.append("NOOP");
		}
		String cmd = sb.toString();
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		boolean isOK = isOk(rs);

		if (uids.isEmpty()) {
			data = Collections.emptyList();
			return;
		}
		
		if (isOK) {
			List<Envelope> tmp = new ArrayList<Envelope>(uids.size());
			Iterator<IMAPResponse> it = rs.iterator();
			for (int i = 0; it.hasNext() && i < uids.size();) {
				IMAPResponse r = it.next();
				String payload = r.getPayload();
				if (!payload.contains(" FETCH")) {
					logger.warn("not a fetch: " + payload);
					continue;
				}
				int uidIdx = payload.indexOf("(UID ") + "(UID ".length();
				int endUid = payload.indexOf(' ', uidIdx);
				String uidStr = payload.substring(uidIdx, endUid);
				long uid = 0;
				try {
					uid = Long.parseLong(uidStr);
				} catch (NumberFormatException nfe) {
					logger.error("cannot parse uid for string '" + uid
							+ "' (payload: " + payload + ")");
					continue;
				}

				String envel = payload.substring(
						endUid + " ENVELOPE (".length(), payload.length());

				String envelData = AtomHelper.getFullResponse(envel,
						r.getStreamData());

				try {
					Envelope envelope = parseEnvelope(envelData.substring(0, envelData.length() - 1).getBytes());
					envelope.setUid(uid);
					logger.info("uid: " + uid + " env.from: "
								+ envelope.getFrom());
					tmp.add(envelope);
				} catch (Throwable t) {
					logger.error("fail parsing envelope for message UID " + uid, t);
					data = Collections.emptyList();
					return;
				}
				i++;
			}
			data = tmp;
		} else {
			IMAPResponse ok = rs.get(rs.size() - 1);
			logger.warn("error on fetch: " + ok.getPayload());
			data = Collections.emptyList();
		}
	}

	/**
	 * <pre>
	 * (        
	 *          "Tue, 19 Jan 2010 09:11:54 +0100" 
	 * 		     "Pb =?ISO-8859-1?Q?r=E9plication_annuaire_ldap?=" 
	 * 		     ( // FROM
	 * 		         ("Raymond Barthe" NIL "raymond.barthe" "cg82.fr")
	 * 		     ) 
	 * 		     ( // SENDER
	 * 		         ("Raymond Barthe" NIL "raymond.barthe" "cg82.fr")
	 * 		     ) 
	 * 		     ( // REPLY TO
	 * 		         ("Raymond Barthe" NIL "raymond.barthe" "cg82.fr")
	 * 		     ) 
	 * 		     ( // TO
	 * 		         (NIL NIL "aliasource" "buffle.tlse.lng:support")
	 * 		         (NIL NIL "support" "aliasource.fr")
	 * 		     ) 
	 * 		     ( // CC
	 * 		         (NIL NIL "admin.info" "cg82.fr")
	 * 		         ("OLIVIER MOLINA" NIL "olivier.molina" "cg82.fr")
	 * 		         ("aliasource anthony prades" NIL "anthony.prades" "aliasource.fr")
	 * 		     ) 
	 * 		     NIL // BCC
	 * 		     NIL  // IN REPLY TO
	 * 		     "<4B55694A.3000106@cg82.fr>"
	 * 		)
	 * </pre>
	 **/
	private Envelope parseEnvelope(byte[] env) {
		ParenListParser parser = new ParenListParser();
		int pos = 0;

		pos = parser.consumeToken(pos, env);
		String date = new String(parser.getLastReadToken());
		Date d = null;
		try {
			d = DateParser.parse(date);
		} catch (ParseException e) {
		}

		pos = parser.consumeToken(pos, env);
		String subject = "[Empty subject]";
		if (parser.getLastTokenType() == TokenType.STRING
				|| parser.getLastTokenType() == TokenType.ATOM) {
			subject = EncodedWord.decode(new String(parser.getLastReadToken()))
					.toString();
		}

		// FROM
		pos = parser.consumeToken(pos, env); // (("Raymon" NIL "ra" "cg82.fr"))
		List<Address> from = null;
		if (parser.getLastTokenType() == TokenType.LIST) {
			from = parseList(parser.getLastReadToken(), parser);
		}

		pos = parser.consumeToken(pos, env); // sender
		pos = parser.consumeToken(pos, env); // reply to

		// TO
		pos = parser.consumeToken(pos, env); // (("Raymon" NIL "ra" "cg82.fr"))
		List<Address> to = parseList(parser.getLastReadToken(), parser);

		// CC
		pos = parser.consumeToken(pos, env); // (("Raymon" NIL "ra" "cg82.fr"))
		List<Address> cc = parseList(parser.getLastReadToken(), parser);

		pos = parser.consumeToken(pos, env); // (("Raymon" NIL "ra" "cg82.fr"))
		List<Address> bcc = parseList(parser.getLastReadToken(), parser);

		pos = parser.consumeToken(pos, env); // In-Reply-To
		String inReplyTo = null;
		if (parser.getLastTokenType() != TokenType.NIL) {
			inReplyTo = new String(parser.getLastReadToken());
		}

		parser.consumeToken(pos, env); // Message-ID
		String mid = new String(parser.getLastReadToken());
		
		Address address = null;
		if (from != null && from.size() > 0) {
			address = from.get(0);
		} else {
			address = new Address();
		}
		return new Envelope(d, subject, to, cc, bcc, address, mid, inReplyTo);
	}

	private List<Address> parseList(byte[] token, ParenListParser parser) {
		LinkedList<Address> ret = new LinkedList<Address>();

		if (parser.getLastTokenType() != TokenType.LIST) {
			return ret;
		}

		int pos = 0;
		do {
			pos = parser.consumeToken(pos, token);
			byte[] parts = parser.getLastReadToken();
			int p = 0;
			p = parser.consumeToken(p, parts);
			String displayName = null;
			if (parser.getLastTokenType() == TokenType.STRING) {
				displayName = EncodedWord.decode(
						new String(parser.getLastReadToken())).toString();
			}
			p = parser.consumeToken(p, parts);
			p = parser.consumeToken(p, parts);
			String left = new String(parser.getLastReadToken());
			p = parser.consumeToken(p, parts);
			String right = new String(parser.getLastReadToken());
			Address ad = new Address(displayName, left + "@" + right);
			ret.add(ad);
		} while (pos < token.length);
		return ret;
	}

}
