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

import org.obm.domain.dao.SharedMailboxDao;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.SharedMailbox;
import org.obm.imap.archive.mailbox.MailboxImpl;
import org.obm.imap.archive.mailbox.MailboxPaths;
import org.obm.imap.archive.mailbox.TemporaryMailbox;
import org.obm.imap.archive.utils.GuavaUtils;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class SharedMailboxesProcessor implements MailboxesProcessor {
	
	protected static final String USERS_REFERENCE_NAME = "*user";
	protected static final String INBOX_MAILBOX_NAME = "/%";
	protected static final String ALL_MAILBOXES_NAME = "*";
	protected static final String ALL_USER_FOLDERS_BUT_INBOX = USERS_REFERENCE_NAME + "/%s/";

	protected final StoreClientFactory storeClientFactory;
	private final SharedMailboxDao sharedMailboxDao;

	@Inject
	@VisibleForTesting SharedMailboxesProcessor(StoreClientFactory storeClientFactory, SharedMailboxDao sharedMailboxDao) {
		this.storeClientFactory = storeClientFactory;
		this.sharedMailboxDao = sharedMailboxDao;
	}

	@Override
	public boolean processMailboxes(ProcessedTask processedTask, Logger logger, MailboxProcessing mailboxProcessing) throws Exception {
		boolean isSuccess = true;
		for (ListInfo listInfo : listSharedMailboxes(processedTask)) {
			String sharedMailboxName = sharedMailboxName(listInfo.getName());
			try {
				if (!isSharedMailbox(sharedMailboxName, processedTask.getDomain())) {
					logger.warn("Shared mailbox {} not found in OBM database", sharedMailboxName);
					continue;
				}

				if (!processSharedMailbox(listInfo, processedTask, mailboxProcessing)) {
					isSuccess = false;
				}
			} catch (DaoException e) {
				logger.warn("Mailshare {} not found in OBM database", sharedMailboxName);
			}
		}
		return isSuccess;
	}
	
	private String sharedMailboxName(String name) {
		return (name.contains(MailboxPaths.AT)) ? Splitter.on(MailboxPaths.AT).split(name).iterator().next()
				: name;
	}

	private boolean isSharedMailbox(String sharedMailboxName, ObmDomain domain) throws DaoException {
		return sharedMailboxDao.findSharedMailboxByName(sharedMailboxName, domain) != null;
	}

	private boolean processSharedMailbox(ListInfo sharedMailbox, ProcessedTask processedTask, MailboxProcessing mailboxProcessing) throws Exception {
		boolean isSuccess = true;
		Logger logger = processedTask.getLogger();
		for (ListInfo listInfo : listImapFolders(sharedMailbox, processedTask)) {
			try {
				mailboxProcessing.processMailbox(MailboxImpl.from(listInfo.getName(), logger, 
						storeClientFactory.createOnSharedMailboxBackend(sharedMailboxName(sharedMailbox.getName()), processedTask.getDomain()), true), 
						processedTask);
			} catch (Exception e) {
				logger.error("Error on archive treatment: ", e);
				isSuccess = false;
			}
		}
		return isSuccess;
	}

	private ImmutableList<ListInfo> listImapFolders(ListInfo sharedMailbox, ProcessedTask processedTask) throws Exception {
		ObmDomain domain = processedTask.getDomain();
		try (StoreClient storeClient = storeClientFactory.createOnSharedMailboxBackend(sharedMailboxName(sharedMailbox.getName()), domain)) {
			storeClient.login(false);

			return FluentIterable.from(
					Iterables.concat(
						Collections.singleton(sharedMailbox),
						storeClient.listAll("*" + sharedMailboxName(sharedMailbox.getName()) + "/", ALL_MAILBOXES_NAME)))
					.filter(filterDomain(domain))
					.filter(filterFolders(processedTask.getDomainConfiguration().getArchiveMainFolder(), TemporaryMailbox.TEMPORARY_FOLDER))
					.filter(GuavaUtils.filterNoSelected(processedTask))
					.toList();
		}
	}

	private Predicate<? super ListInfo> filterDomain(final ObmDomain domain) {
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				String sharedMailboxName = listInfo.getName();
				if (!sharedMailboxName.endsWith(MailboxPaths.AT + domain.getName())) {
					return false;
				}
				return true;
			}
		};
	}

	private Predicate<ListInfo> filterFolders(final String...folders) {
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				String path = listInfo.getName();
				if (!path.contains(String.valueOf(MailboxPaths.IMAP_FOLDER_SEPARATOR))) {
					return true;
				}
				String subPath = Joiner.on(MailboxPaths.IMAP_FOLDER_SEPARATOR)
						.join(Iterables.skip(Splitter.on(MailboxPaths.IMAP_FOLDER_SEPARATOR).split(path), 1));
				for (String folder : folders) {
					if (subPath.startsWith(folder + MailboxPaths.IMAP_FOLDER_SEPARATOR)) {
						return false;
					}
				}
				return true;
			}
		};
	}

	private ImmutableList<ListInfo> listSharedMailboxes(final ProcessedTask processedTask) throws Exception {
		ObmDomain domain = processedTask.getDomain();
		try (StoreClient storeClient = storeClientFactory.create(domain.getName())) {
			storeClient.login(false);
			
			return FluentIterable.from(storeClient.listAll("", ALL_MAILBOXES_NAME))
					.transform(GuavaUtils.appendDomainWhenNone(domain))
					.filter(filterSharedMailboxesDomain(domain, processedTask.getLogger()))
					.filter(filterOutUsersMailboxes(processedTask))
					.filter(filterScopeMailboxes(processedTask))
					.filter(GuavaUtils.filterNoSelected(processedTask))
					.filter(filterFirstLevel())
					.toList();
		}
	}

	private Predicate<ListInfo> filterSharedMailboxesDomain(ObmDomain domain, final Logger logger) {
		final DomainName domainName = new DomainName(domain.getName());
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				return listInfo.getName().endsWith(MailboxPaths.AT + domainName.get());
			}
		};
	}

	private Predicate<? super ListInfo> filterScopeMailboxes(ProcessedTask processedTask) {
		final List<String> scopeSharedMailboxes = scopeMailboxes(processedTask.getDomainConfiguration());
		final boolean scopeIncludes = processedTask.getDomainConfiguration().isScopeSharedMailboxesIncludes();
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				for (String scopeSharedMailbox : scopeSharedMailboxes) {
					if (listInfo.getName().startsWith(scopeSharedMailbox + MailboxPaths.AT)) {
						return scopeIncludes;
					}
				}
				return !scopeIncludes;
			}
		};
	}

	private List<String> scopeMailboxes(DomainConfiguration domainConfiguration) {
		return Lists.transform(domainConfiguration.getScopeSharedMailboxes(), 
				new Function<SharedMailbox, String>() {

					@Override
					public String apply(SharedMailbox sharedMailbox) {
						return sharedMailbox.getName();
					}
				});
	}

	private Predicate<? super ListInfo> filterOutUsersMailboxes(ProcessedTask processedTask) {
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				if (listInfo.getName().startsWith("user/")) {
					return false;
				}
				return true;
			}
		};
	}

	private Predicate<? super ListInfo> filterFirstLevel() {
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				return !listInfo.getName().contains(Character.toString(MailboxPaths.IMAP_FOLDER_SEPARATOR));
			}
		};
	}
}
