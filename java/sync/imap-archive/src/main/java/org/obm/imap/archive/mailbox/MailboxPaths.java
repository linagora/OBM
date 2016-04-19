/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
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


package org.obm.imap.archive.mailbox;

import java.util.Iterator;
import java.util.List;

import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.sync.base.DomainName;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class MailboxPaths {

	public static final char IMAP_FOLDER_SEPARATOR = '/';
	public static final String AT = "@";
	public static final String INBOX = "INBOX";
	
	public static MailboxPaths from(String mailbox, boolean sharedMailbox) throws MailboxFormatException {
		return parse(mailbox, null, new Builder(), sharedMailbox);
	}
	
	public static class Builder {
		
		private String mainPath;
		private String user;
		private String subPaths;
		private DomainName domainName;
		private boolean sharedMailbox;
		
		@VisibleForTesting Builder() {
		}
		
		public Builder mainPath(String mainPath) {
			Preconditions.checkNotNull(mainPath);
			this.mainPath = mainPath;
			return this;
		}
		
		public Builder user(String user) {
			Preconditions.checkNotNull(user);
			this.user = user;
			return this;
		}
		
		public Builder subPaths(String subPaths) {
			Preconditions.checkNotNull(subPaths);
			this.subPaths = subPaths;
			return this;
		}
		
		public Builder domainName(DomainName domainName) {
			Preconditions.checkNotNull(domainName);
			this.domainName = domainName;
			return this;
		}
	
		public Builder sharedMailbox(boolean sharedMailbox) {
			this.sharedMailbox = sharedMailbox;
			return this;
		}

		public MailboxPaths build() {
			Preconditions.checkState(!Strings.isNullOrEmpty(mainPath));
			if (!sharedMailbox) {
				Preconditions.checkState(!Strings.isNullOrEmpty(user));
				Preconditions.checkState(!Strings.isNullOrEmpty(subPaths));
				Preconditions.checkState(domainName != null);
			}
			return new MailboxPaths(mainPath, user, subPaths, domainName, sharedMailbox);
		}
	}

	@VisibleForTesting static MailboxPaths parse(String mailbox, String mainSubPath, Builder builder, boolean sharedMailbox) throws MailboxFormatException {
		Iterator<String> split = Splitter.on(IMAP_FOLDER_SEPARATOR).split(mailbox).iterator();
		if (!sharedMailbox) {
			parseUserMailbox(mailbox, mainSubPath, builder, split);
		} else {
			parseSharedMailbox(mailbox, mainSubPath, builder, split);
		}
		return builder.sharedMailbox(sharedMailbox)
				.build();
	}

	private static void parseUserMailbox(String mailbox, String mainSubPath, Builder builder, Iterator<String> split) throws MailboxFormatException {
		builder.mainPath(nextMandatoryElement(split, mailbox));

		String userPart = nextMandatoryElement(split, mailbox);
		if (userPart.contains(AT)) {
			Iterator<String> splitPathAtDomain = splitPathAtDomain(userPart);
			builder.user(nextMandatoryElement(splitPathAtDomain, mailbox));
			builder.subPaths(inbox(mainSubPath));
			builder.domainName(new DomainName(nextMandatoryElement(splitPathAtDomain, mailbox)));
		} else {
			parseMailboxWithSubPaths(mailbox, mainSubPath, userPart, builder, split, false);
		}
	}

	private static void parseSharedMailbox(String mailbox, String mainSubPath, Builder builder, Iterator<String> split) throws MailboxFormatException {
		String firstElement = nextMandatoryElement(split, mailbox);

		if (firstElement.contains(AT)) {
			Iterator<String> splitPathAtDomain = splitPathAtDomain(firstElement);
			builder.mainPath(nextMandatoryElement(splitPathAtDomain, mailbox));
			builder.domainName(new DomainName(nextMandatoryElement(splitPathAtDomain, mailbox)));
		} else {
			builder.mainPath(firstElement);
			parseMailboxWithSubPaths(mailbox, mainSubPath, firstElement, builder, split, true);
		}
	}

	private static void parseMailboxWithSubPaths(String mailbox, String mainSubPath, String userPart, Builder builder, Iterator<String> split, boolean sharedMailbox) throws MailboxFormatException {
		if (!sharedMailbox) {
			builder.user(userPart);
		}
		
		List<String> subPaths = Lists.newArrayList();
		if (!Strings.isNullOrEmpty(mainSubPath)) {
			subPaths.add(mainSubPath);
		}
		
		while (split.hasNext()) {
			String subPath = split.next();
			if (subPath.contains(AT)) {
				Iterator<String> splitPathAtDomain = splitPathAtDomain(subPath);
				subPaths.add(nextMandatoryElement(splitPathAtDomain, mailbox));
				builder.domainName(new DomainName(nextMandatoryElement(splitPathAtDomain, mailbox)));
				break;
			}
			subPaths.add(subPath);
		}
		builder.subPaths(Joiner.on(IMAP_FOLDER_SEPARATOR).join(subPaths));
	}

	private static String inbox(String mainSubPath) {
		if (!Strings.isNullOrEmpty(mainSubPath)) {
			return Joiner.on(IMAP_FOLDER_SEPARATOR).join(mainSubPath, INBOX);
		}
		return INBOX;
	}

	private static Iterator<String> splitPathAtDomain(String pathAtDomain) {
		return Splitter.on(AT).split(pathAtDomain).iterator();
	}
	
	private static String nextMandatoryElement(Iterator<String> iterator, String mailbox) throws MailboxFormatException {
		if (!iterator.hasNext()) {
			throw new MailboxFormatException(mailbox);
		}
		return iterator.next();
	}
	
	private final String mainPath;
	private final String user;
	private final String subPaths;
	private final DomainName domainName;
	private final boolean sharedMailbox;
	
	protected MailboxPaths(String mainPath, String user, String subPaths, DomainName domainName, boolean sharedMailbox) {
		this.mainPath = mainPath;
		this.user = user;
		this.subPaths = subPaths;
		this.domainName = domainName;
		this.sharedMailbox = sharedMailbox;
	}
	
	public String getMainPath() {
		return mainPath;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getSubPaths() {
		return subPaths;
	}
	
	public DomainName getDomainName() {
		return domainName;
	}
	
	public boolean isSharedMailbox() {
		return sharedMailbox;
	}
	
	public String getName() {
		if( !isSharedMailbox()) {
			if (subPaths != null) {
				return Joiner.on(AT).join(Joiner.on(IMAP_FOLDER_SEPARATOR).join(mainPath, user, subPaths), domainName.get());
			}
			return Joiner.on(AT).join(Joiner.on(IMAP_FOLDER_SEPARATOR).join(mainPath, user), domainName.get());
		} else {
			if (subPaths != null) {
				return Joiner.on(AT).join(Joiner.on(IMAP_FOLDER_SEPARATOR).join(mainPath, subPaths), domainName.get());
			}
			return Joiner.on(AT).join(mainPath, domainName.get());
		}
	}
	
	public String getUserAtDomain() {
		if( !isSharedMailbox()) {
			return Joiner.on(AT).join(user, domainName.get());
		}
		return null;
	}
	
	public MailboxPaths prepend(String mainSubPath) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(mainSubPath));
		Builder builder = new Builder()
			.mainPath(getMainPath())
			.domainName(getDomainName())
			.sharedMailbox(isSharedMailbox());
		
		String subPaths = getSubPaths();
		if (!isSharedMailbox()) {
			builder.subPaths(Joiner.on(IMAP_FOLDER_SEPARATOR).join(mainSubPath, subPaths))
				.user(getUser());
		} else {
			if (!Strings.isNullOrEmpty(subPaths)) {
				builder.subPaths(Joiner.on(IMAP_FOLDER_SEPARATOR).join(mainSubPath, subPaths));
			} else {
				builder.subPaths(mainSubPath);
			}
		}
		return builder
			.build();
	}
	
	public boolean belongsTo(DomainName domainName) {
		return this.domainName.equals(domainName);
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(mainPath, user, subPaths, domainName, sharedMailbox);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof MailboxPaths) {
			MailboxPaths that = (MailboxPaths) object;
			return Objects.equal(this.mainPath, that.mainPath)
				&& Objects.equal(this.user, that.user)
				&& Objects.equal(this.subPaths, that.subPaths)
				&& Objects.equal(this.domainName, that.domainName)
				&& Objects.equal(this.sharedMailbox, that.sharedMailbox);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("mainPath", mainPath)
			.add("user", user)
			.add("subPaths", subPaths)
			.add("domainName", domainName)
			.add("sharedMailbox", sharedMailbox)
			.toString();
	}
}
