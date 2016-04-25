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


package org.obm.imap.archive.services;

import java.util.Collections;
import java.util.List;

import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.ScopeUser;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.imap.archive.mailbox.MailboxImpl;
import org.obm.imap.archive.mailbox.MailboxPaths;
import org.obm.imap.archive.mailbox.TemporaryMailbox;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import fr.aliacom.obm.common.domain.ObmDomain;

public class UserMailboxesProcessing {
	
	protected static final String USERS_REFERENCE_NAME = "*user";
	protected static final String INBOX_MAILBOX_NAME = "/%";
	protected static final String ALL_MAILBOXES_NAME = "*";
	protected static final String ALL_USER_FOLDERS_BUT_INBOX = USERS_REFERENCE_NAME + "/%s/";

	protected final StoreClientFactory storeClientFactory;
	private final MailboxProcessing mailboxProcessing;

	
	public UserMailboxesProcessing(StoreClientFactory storeClientFactory, MailboxProcessing mailboxProcessing) {
		this.storeClientFactory = storeClientFactory;
		this.mailboxProcessing = mailboxProcessing;
	}

	public boolean processUsers(ProcessedTask processedTask, Logger logger) throws Exception {
		boolean isSuccess = true;
		for (ListInfo inboxListInfo : listUsers(processedTask)) {
			Optional<String> maybeUserName = inboxListInfo.getUserName();

			if (!maybeUserName.isPresent()) {
				continue;
			}

			String user = maybeUserName.get();

			try {
				if (!processUser(inboxListInfo, user, processedTask)) {
					isSuccess = false;
				}
			} catch (UserNotFoundException e) {
				logger.warn("User {} not found in OBM database", user);
			}
		}
		return isSuccess;
	}
	
	private boolean processUser(ListInfo inboxListInfo, String user, ProcessedTask processedTask) throws Exception {
		boolean isSuccess = true;
		Logger logger = processedTask.getLogger();
		for (ListInfo listInfo : listImapFolders(inboxListInfo, user, processedTask)) {
			try {
				mailboxProcessing.processMailbox(MailboxImpl.from(listInfo.getName(), logger, storeClientFactory.createOnUserBackend(user, processedTask.getDomain())), 
						processedTask);
			} catch (Exception e) {
				logger.error("Error on archive treatment: ", e);
				isSuccess = false;
			}
		}
		return isSuccess;
	}

	@VisibleForTesting ImmutableList<ListInfo> listUsers(final ProcessedTask processedTask) throws Exception {
		ObmDomain domain = processedTask.getDomain();
		try (StoreClient storeClient = storeClientFactory.create(domain.getName())) {
			storeClient.login(false);
			
			return FluentIterable.from(storeClient.listAll(USERS_REFERENCE_NAME, INBOX_MAILBOX_NAME))
					.transform(appendDomainWhenNone(domain))
					.filter(filterDomain(domain, processedTask.getLogger()))
					.filter(filterScopeUsers(processedTask))
					.toList();
		}
	}

	@VisibleForTesting ImmutableList<ListInfo> listImapFolders(ListInfo inboxListInfo, final String user, final ProcessedTask processedTask) throws Exception {
		ObmDomain domain = processedTask.getDomain();
		try (StoreClient storeClient = storeClientFactory.createOnUserBackend(user, domain)) {
			storeClient.login(false);

			return FluentIterable.from(
					Iterables.concat(
						Collections.singleton(inboxListInfo),
						storeClient.listAll(String.format(ALL_USER_FOLDERS_BUT_INBOX, user), ALL_MAILBOXES_NAME)))
					.transform(appendDomainWhenNone(domain))
					.filter(filterExcludedFolder(processedTask))
					.filter(filterFolders(processedTask, processedTask.getDomainConfiguration().getArchiveMainFolder(), TemporaryMailbox.TEMPORARY_FOLDER))
					.filter(filterNoSelected(processedTask))
					.toList();
		}
	}

	protected Function<ListInfo, ListInfo> appendDomainWhenNone(ObmDomain domain) {
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

	protected Predicate<ListInfo> filterDomain(ObmDomain domain, final Logger logger) {
		final DomainName domainName = new DomainName(domain.getName());
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				try {
					MailboxPaths mailboxPaths = MailboxPaths.from(listInfo.getName());
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

	private Predicate<ListInfo> filterExcludedFolder(final ProcessedTask processedTask) {
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				if (listInfo.getName().contains("/" + processedTask.getDomainConfiguration().getExcludedFolder() + "/") || 
						listInfo.getName().contains("/" + processedTask.getDomainConfiguration().getExcludedFolder() + "@")) {
					return false;
				}
				return true;
			}
		};
	}

	private Predicate<? super ListInfo> filterScopeUsers(ProcessedTask processedTask) {
		final List<String> scopeUserLogins = scopeUserLogins(processedTask.getDomainConfiguration());
		final boolean scopeIncludes = processedTask.getDomainConfiguration().isScopeUsersIncludes();
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				for (String scopeUserLogin : scopeUserLogins) {
					if (listInfo.getName().startsWith("user/" + scopeUserLogin + "/") || 
							listInfo.getName().startsWith("user/" + scopeUserLogin + "@")) {
						return scopeIncludes;
					}
				}
				return !scopeIncludes;
			}
		};
	}

	private List<String> scopeUserLogins(DomainConfiguration domainConfiguration) {
		return FluentIterable.from(domainConfiguration.getScopeUsers())
				.transform(new Function<ScopeUser, String>() {

					@Override
					public String apply(ScopeUser scopeUser) {
						return scopeUser.getLogin();
					}
				}).toList();
	}

	private Predicate<ListInfo> filterFolders(ProcessedTask processedTask, final String...folders) {
		final Logger logger = processedTask.getLogger();
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				try {
					MailboxPaths mailboxPaths = MailboxPaths.from(listInfo.getName());
					for (String folder : folders) {
						if (mailboxPaths.getSubPaths().startsWith(folder + MailboxPaths.IMAP_FOLDER_SEPARATOR)) {
							return false;
						}
					}
				} catch (MailboxFormatException e) {
					logger.error(String.format("The mailbox %s can't be parsed", listInfo.getName()));
					return false;
				}
				return true;
			}
		};
	}

	private Predicate<ListInfo> filterNoSelected(ProcessedTask processedTask) {
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
}
