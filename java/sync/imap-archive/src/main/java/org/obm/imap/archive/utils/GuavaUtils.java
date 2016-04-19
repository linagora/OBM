/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2016  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */


package org.obm.imap.archive.utils;

import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.imap.archive.mailbox.MailboxPaths;
import org.obm.imap.archive.services.ProcessedTask;
import org.obm.push.mail.bean.ListInfo;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import fr.aliacom.obm.common.domain.ObmDomain;

public class GuavaUtils {

	public static Function<ListInfo, ListInfo> appendDomainWhenNone(ObmDomain domain) {
		final String domainName = domain.getName();
		return new Function<ListInfo, ListInfo>() {

			@Override
			public ListInfo apply(ListInfo listInfo) {
				if (!hasDomain(listInfo)) {
					return appendDomainToListInfo(listInfo, domainName);
				}
				return listInfo;
			}

			private boolean hasDomain(ListInfo listInfo) {
				return listInfo.getName().contains(MailboxPaths.AT);
			}

			private ListInfo appendDomainToListInfo(ListInfo listInfo, String domainName) {
				return new ListInfo(
						new StringBuilder()
							.append(listInfo.getName())
							.append(MailboxPaths.AT)
							.append(domainName)
							.toString(), 
						listInfo.isSelectable(), listInfo.canCreateSubfolder());
			}
		};
	}

	public static Predicate<ListInfo> filterNoSelected(ProcessedTask processedTask) {
		final Logger logger = processedTask.getLogger();
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				if (!listInfo.isSelectable()) {
					logger.info(String.format("The mailbox %s can't be selected (\\Noselect flag)", listInfo.getName()));
					return false;
				}
				return true;
			}
		};
	}

	public static Predicate<ListInfo> filterDomain(ObmDomain domain, final Logger logger) {
		final DomainName domainName = new DomainName(domain.getName());
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				try {
					MailboxPaths mailboxPaths = MailboxPaths.from(listInfo.getName(), false);
					if (mailboxPaths.belongsTo(domainName)) {
						return true;
					}
				} catch (MailboxFormatException e) {
					logger.error(String.format("The mailbox %s can't be parsed", listInfo.getName()));
				}
				return false;
			}
		};
	}

	public static Predicate<ListInfo> filterArchiveFolder(final Logger logger, final DomainConfiguration domainConfiguration) {
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				try {
					MailboxPaths mailboxPaths = MailboxPaths.from(listInfo.getName(), false);
					if (!mailboxPaths.getSubPaths().startsWith(domainConfiguration.getArchiveMainFolder() + MailboxPaths.IMAP_FOLDER_SEPARATOR)) {
						return false;
					}
				} catch (MailboxFormatException e) {
					logger.error(String.format("The mailbox %s can't be parsed", listInfo.getName()));
					return false;
				}
				return true;
			}
		};
	}
}
