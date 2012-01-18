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
package org.obm.push.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.mail.Flags;

import org.obm.push.bean.Email;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;

@Singleton
public class ImapMailBoxUtils {

	private static final ImmutableMap<Flags.Flag, String> flags = 
			ImmutableMap.<Flags.Flag, String>builder().
			put(Flags.Flag.ANSWERED, "ANSWERED").
			put(Flags.Flag.DELETED, "DELETED").
			put(Flags.Flag.DRAFT, "DRAFT").
			put(Flags.Flag.FLAGGED, "FLAGGED").
			put(Flags.Flag.RECENT, "RECENT").
			put(Flags.Flag.SEEN, "SEEN").
			put(Flags.Flag.USER, "USER").build();
	
	public String flagToString(Flags.Flag flag) {
		if (flags.containsKey(flag)) {
			return "Flag." + flags.get(flag);
		} else {
			throw new IllegalArgumentException("Flag not found !");
		}
	}
	
	public List<Email> orderEmailByUid(Collection<Email> emails) {
		ArrayList<Email> listOfEmail = Lists.newArrayList(emails);
		Collections.sort(listOfEmail, new Comparator<Email>() {
			@Override
			public int compare(Email o1, Email o2) {
				return (int) (o1.getUid() - o2.getUid());
			}
		});
		return listOfEmail;
	}
}
