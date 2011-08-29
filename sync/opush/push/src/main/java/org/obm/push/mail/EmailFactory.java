package org.obm.push.mail;

import java.util.Collection;
import java.util.Set;

import org.minig.imap.FastFetch;
import org.obm.push.bean.Email;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class EmailFactory {
	
	public static Set<Email> listEmailFromFastFetch(Collection<FastFetch> fetchs) {
		Builder<Email> builder = ImmutableSet.builder();
		for (FastFetch fastFetch: fetchs) {
			builder.add( getEmailFromFastFetch(fastFetch) );
		}
		return builder.build();
	}

	public static Email getEmailFromFastFetch(FastFetch fastFetch) {
		return new Email(fastFetch.getUid(), fastFetch.isRead(), fastFetch.getInternalDate());
	}
	
	public static Collection<Long> listUIDFromEmail(Collection<Email> emails) {
		return Collections2.transform(emails, new Function<Email, Long>() {
			@Override
			public Long apply(Email email) {
				return email.getUid();
			}
		});
	}
	
}
