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

import org.minig.imap.EncodedWord;
import org.minig.imap.impl.DateParser;
import org.minig.imap.impl.IMAPParsingTools;
import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.MessageSet;
import org.minig.imap.mime.impl.AtomHelper;
import org.minig.imap.mime.impl.ParenListParser;
import org.minig.imap.mime.impl.ParenListParser.TokenType;
import org.obm.push.mail.MailException;
import org.obm.push.mail.bean.Address;
import org.obm.push.mail.bean.Envelope;
import org.obm.push.mail.bean.UIDEnvelope;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;

/**
 * @author tom
 * 
 */
public class UIDFetchEnvelopeCommand extends Command<Collection<UIDEnvelope>> {

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
			List<UIDEnvelope> tmp = new ArrayList<UIDEnvelope>(uids.size());
			Iterator<IMAPResponse> it = rs.iterator();
			for (int i = 0; it.hasNext() && i < uids.size();) {
				IMAPResponse r = it.next();
				String payload = r.getPayload();
				if (!payload.contains(" FETCH")) {
					logger.warn("not a fetch: " + payload);
					continue;
				}
				String fullPayload = AtomHelper.getFullResponse(payload, r.getStreamData());
				long uid = getUid(fullPayload);
				String envel = getEnvelopePayload(fullPayload);

				try {
					Envelope envelope = parseEnvelope(envel.getBytes(Charsets.US_ASCII));
					logger.info("uid: {}  env.from: {}", uid, envelope.getFrom());
					tmp.add( new UIDEnvelope(uid, envelope) );
				} catch (Throwable t) {
					logger.error("fail parsing envelope for message UID " + uid, t);
					logger.error("Envelope payload in error was : " + envel);
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

	private long getUid(String fullPayload) {
		try {
			String longAsString = getNumberForField(fullPayload, "UID ");
			return Long.valueOf(longAsString);
		} catch(NumberFormatException e) {
			throw new MailException("Cannot find UID in response : " + fullPayload);
		}
	}
	
	private String getNumberForField(String fullPayload, String field) {
		String uidStartToken = field;
		int uidIdx = fullPayload.indexOf(uidStartToken);
		String content = fullPayload.substring(uidIdx + uidStartToken.length());
		return IMAPParsingTools.getNextNumber(content);
	}

	@VisibleForTesting String getEnvelopePayload(String fullPayload) {
		String envelopeStartToken = "ENVELOPE ";
		int bsIdx = fullPayload.indexOf(envelopeStartToken);
		if (bsIdx == -1) {
			return null;
		}
		int contentStart = bsIdx + envelopeStartToken.length();
		if (fullPayload.charAt(contentStart) != '(') {
			return null;
		}
		String content = fullPayload.substring(contentStart);
		return IMAPParsingTools.substringFromOpeningToClosingBracket(content);
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
	@VisibleForTesting Envelope parseEnvelope(byte[] envelopeWithContainer) {
		ParenListParser parser = new ParenListParser();
		int pos = 0;
		
		parser.consumeToken(pos, envelopeWithContainer);
		byte[] envelope = parser.getLastReadToken();

		pos = parser.consumeToken(pos, envelope);
		String date = new String(parser.getLastReadToken());
		Date d = null;
		try {
			d = DateParser.parse(date);
		} catch (ParseException e) {
		}

		pos = parser.consumeToken(pos, envelope);
		String subject = "[Empty subject]";
		if (parser.getLastTokenType() == TokenType.STRING
				|| parser.getLastTokenType() == TokenType.ATOM) {
			subject = EncodedWord.decode(new String(parser.getLastReadToken()))
					.toString();
		}

		// FROM
		pos = parser.consumeToken(pos, envelope); // (("Raymon" NIL "ra" "cg82.fr"))
		List<Address> from = null;
		if (parser.getLastTokenType() == TokenType.LIST) {
			from = parseList(parser.getLastReadToken(), parser);
		}

		pos = parser.consumeToken(pos, envelope); // sender
		pos = parser.consumeToken(pos, envelope); // reply to

		// TO
		pos = parser.consumeToken(pos, envelope); // (("Raymon" NIL "ra" "cg82.fr"))
		List<Address> to = parseList(parser.getLastReadToken(), parser);

		// CC
		pos = parser.consumeToken(pos, envelope); // (("Raymon" NIL "ra" "cg82.fr"))
		List<Address> cc = parseList(parser.getLastReadToken(), parser);

		pos = parser.consumeToken(pos, envelope); // (("Raymon" NIL "ra" "cg82.fr"))
		List<Address> bcc = parseList(parser.getLastReadToken(), parser);

		pos = parser.consumeToken(pos, envelope); // In-Reply-To

		parser.consumeToken(pos, envelope); // Message-ID
		String mid = new String(parser.getLastReadToken());
		
		return Envelope.builder().date(d).subject(subject).to(to).cc(cc).bcc(bcc).from(from).
				messageID(mid).build();
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
