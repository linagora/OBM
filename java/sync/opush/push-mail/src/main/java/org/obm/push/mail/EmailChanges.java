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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.obm.push.mail.bean.Email;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;

public class EmailChanges implements Serializable {
	
	public static EmailChanges empty() {
		return builder().build();
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
	
		private Set<Email> additions;
		private Set<Email> changes;
		private Set<Email> deletions;
		
		private Builder() {
			super();
			additions = Sets.newHashSet();
			changes = Sets.newHashSet();
			deletions = Sets.newHashSet();
		}

		public Builder deletion(Email... emails) {
			this.deletions.addAll(Arrays.asList(emails));
			return this;
		}
		
		public Builder deletions(Set<Email> deletions) {
			Preconditions.checkArgument(deletions != null, "deletions must not be null");
			this.deletions.addAll(deletions);
			return this;
		}

		public Builder change(Email... emails) {
			this.changes.addAll(Arrays.asList(emails));
			return this;
		}
		
		public Builder changes(Set<Email> changes) {
			Preconditions.checkArgument(changes != null, "changes must not be null");
			this.changes.addAll(changes);
			return this;
		}

		public Builder addition(Email... emails) {
			this.additions.addAll(Arrays.asList(emails));
			return this;
		}
		
		public Builder additions(Set<Email> additions) {
			Preconditions.checkArgument(additions != null, "additions must not be null");
			this.additions.addAll(additions);
			return this;
		}
		
		public Builder merge(EmailChanges changes) {
			additions(changes.additions());
			changes(changes.changes());
			deletions(changes.deletions());
			return this;
		}
		
		public int sumOfChanges() {
			return additions.size() + changes.size() + deletions.size();
		}
		
		public EmailChanges build() {
			return new EmailChanges(
					ImmutableSet.copyOf(deletions), 
					ImmutableSet.copyOf(changes),
					ImmutableSet.copyOf(additions));
		}

	}

	private final Set<Email> deletions;
	private final Set<Email> changes;
	private final Set<Email> additions;
	
	private EmailChanges(Set<Email> deletions, Set<Email> changes, Set<Email> additions) {
		this.deletions = deletions;
		this.changes = changes;
		this.additions = additions;
	}
	
	public Set<Email> deletions() {
		return deletions;
	}

	public Set<Email> changes() {
		return changes;
	}

	public Set<Email> additions() {
		return additions;
	}

	public boolean hasChanges() {
		return !additions.isEmpty() || !changes.isEmpty() || !deletions.isEmpty();
	}

	public int sumOfChanges() {
		return additions.size() + changes.size() + deletions.size();
	}
	
	public Splitter splitToFit(int firstPartSize) {
		return new Splitter(this, firstPartSize);
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(deletions, changes, additions);
	}
	
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof EmailChanges) {
			EmailChanges other = (EmailChanges) obj;
			return Objects.equal(this.deletions, other.deletions)
				&& Objects.equal(this.changes, other.changes)
				&& Objects.equal(this.additions, other.additions);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("additions", additions)
				.add("changes", changes)
				.add("deletions", deletions)
				.toString();
	}
	
	public static class Splitter {
			
		private final EmailChanges fit;
		private final EmailChanges left;
		
		public Splitter(EmailChanges origin, int limit) {
			Preconditions.checkArgument(limit > 0, "limit must be a positive integer");
			
			if (limit >= origin.sumOfChanges()) {
				this.fit = origin;
				this.left = EmailChanges.empty();
			} else {
				Iterable<EmailPartitionEntry> entries = origin.toEntries();
				this.fit = fromEntries(Iterables.limit(entries, limit));
				this.left = fromEntries(Iterables.skip(entries, limit));
			}
		}

		public EmailChanges getFit() {
			return fit;
		}
		
		public EmailChanges getLeft() {
			return left;
		}
	}

	private static class EmailPartitionEntry {

		enum Type {
			ADD, CHANGE, DELETION
		}
		
		private final Type type;
		private final Email email;

		public EmailPartitionEntry(Type type, Email email) {
			this.type = type;
			this.email = email;
		}

		public Type getType() {
			return type;
		}

		public Email getEmail() {
			return email;
		}
	}

	private final class EntryProducer implements Function<Email, EmailPartitionEntry> {

		private final EmailPartitionEntry.Type type;

		public EntryProducer(EmailPartitionEntry.Type type) {
			this.type = type;
		}

		@Override
		public EmailPartitionEntry apply(Email input) {
			return new EmailPartitionEntry(type, input);
		}
	}
	
	private Iterable<EmailPartitionEntry> toEntries() {
		return FluentIterable.from(
				Iterables.concat(
					FluentIterable.from(additions).transform(new EntryProducer(EmailPartitionEntry.Type.ADD)),
					FluentIterable.from(changes).transform(new EntryProducer(EmailPartitionEntry.Type.CHANGE)),
					FluentIterable.from(deletions).transform(new EntryProducer(EmailPartitionEntry.Type.DELETION)))
					).toSortedImmutableList(new Comparator<EmailPartitionEntry>() {

						@Override
						public int compare(EmailPartitionEntry o1, EmailPartitionEntry o2) {
							return Longs.compare(o2.getEmail().getUid(), o1.getEmail().getUid());
						}
					});
	}

	private static EmailChanges fromEntries(Iterable<EmailPartitionEntry> entries) {
		Builder builder = EmailChanges.builder();
		for (EmailPartitionEntry entry: entries) {
			switch (entry.getType()) {
			case ADD:
				builder.addition(entry.getEmail());
				break;
			case CHANGE:
				builder.change(entry.getEmail());
				break;
			case DELETION:
				builder.deletion(entry.getEmail());
				break;
			}
		}
		return builder.build();
	}
	
	public Iterable<EmailChanges> partition(int windowSize) {
		Preconditions.checkArgument(windowSize > 0);
		if (sumOfChanges() == 0) {
			return ImmutableList.<EmailChanges>of();
		}
		if (sumOfChanges() < windowSize) {
			return ImmutableList.of(this);
		}
		return FluentIterable
			.from(Iterables.partition(toEntries(), windowSize))
			.transform(new Function<List<EmailPartitionEntry>, EmailChanges>() {
				@Override
				public EmailChanges apply(List<EmailPartitionEntry> input) {
					return EmailChanges.fromEntries(input);
				}
			});
	}
	

}
