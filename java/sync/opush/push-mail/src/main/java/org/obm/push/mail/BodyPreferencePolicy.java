/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.mail.mime.ContentType;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public abstract class BodyPreferencePolicy {

	protected static final int DEFAULT_TRUNCATION_SIZE = 32*1024;
	protected static final ImmutableList<BodyPreference> DEFAULT_BODY_PREFERENCES = 
			ImmutableList.<BodyPreference> builder()
					.add(BodyPreference.builder().bodyType(MSEmailBodyType.PlainText)
							.truncationSize(DEFAULT_TRUNCATION_SIZE).build())
					.add(BodyPreference.builder().bodyType(MSEmailBodyType.HTML)
							.truncationSize(DEFAULT_TRUNCATION_SIZE).build())
					.add(BodyPreference.builder().bodyType(MSEmailBodyType.MIME)
							.truncationSize(DEFAULT_TRUNCATION_SIZE).build())
					.build();
	
	public abstract List<BodyPreference> bodyPreferencesMatchingPolicy(List<BodyPreference> bodyPreferences);
	
	public abstract boolean mayUsesDefaultBodyPreferences();
	
	public abstract FetchInstruction selectBetterFit(List<FetchInstruction> fetchInstructions, List<BodyPreference> bodyPreferences);
	
	public List<FetchHints> listContentTypes(MSEmailBodyType bodyType) {
		switch (bodyType) {
			case HTML:
				return Arrays.asList(
						FetchHints.builder()
							.contentType(toContentType(MSEmailBodyType.HTML)).build());
			case PlainText:
				return Arrays.asList(
						FetchHints.builder()
							.contentType(toContentType(MSEmailBodyType.PlainText)).build());
			case RTF:
				return Arrays.asList(
						FetchHints.builder()
							.contentType(toContentType(MSEmailBodyType.RTF)).build());
			default:
				throw new IllegalArgumentException("Unexpected MSEmailBodyType");
		}
	}

	protected static Comparator<FetchInstruction> betterFitComparator(final List<BodyPreference> bodyPreferences) {
		final List<MSEmailBodyType> preferences = FluentIterable
				.from(Iterables.concat(bodyPreferences, DEFAULT_BODY_PREFERENCES))
				.transform(new Function<BodyPreference, MSEmailBodyType>() {
					@Override
					public MSEmailBodyType apply(BodyPreference input) {
						return input.getType();
					}
				}).toList();
		
		return new Comparator<FetchInstruction>() {
			
			private int computeWeight(FetchInstruction instruction) {
				int transformationWeight = instruction.getMailTransformation() == MailTransformation.NONE ? 0 : 1;
				return preferences.indexOf(instruction.getBodyType()) + (preferences.size() * transformationWeight);
			}
			
			@Override
			public int compare(FetchInstruction o1, FetchInstruction o2) {
				if (o1.getBodyType().equals(o2.getBodyType()) 
						&& o1.getMailTransformation().equals(o2.getMailTransformation())) {
					return 0;
				}
				if (o1.getBodyType() == MSEmailBodyType.MIME) {
					return 1;
				} else if (o2.getBodyType() == MSEmailBodyType.MIME) {
					return -1;
				} else {
					return computeWeight(o1) - computeWeight(o2);
				}
			}
		};
	}
	
	protected ContentType toContentType(MSEmailBodyType bodyType) {
		String contentType = toMimeType(bodyType);
		return ContentType.builder().contentType(contentType).build();
	}
	
	private String toMimeType(MSEmailBodyType bodyType) {
		switch (bodyType) {
		case PlainText:
			return "text/plain";
		case HTML:
			return "text/html";
		case RTF:
			return "text/rtf";
		default:
			throw new IllegalArgumentException("Unexpected MSEmailBodyType");
		}
	}
}
