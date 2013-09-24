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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.mail.mime.ContentType;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.mail.mime.MimePart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class MimePartSelector {
	
	private static final Logger logger = LoggerFactory.getLogger(MimePartSelector.class);
	
	private static final int DEFAULT_TRUNCATION_SIZE = 32*1024;
	private static final ImmutableList<BodyPreference> DEFAULT_BODY_PREFERENCES = 
			ImmutableList.<BodyPreference> builder()
					.add(BodyPreference.builder().bodyType(MSEmailBodyType.PlainText)
							.truncationSize(DEFAULT_TRUNCATION_SIZE).build())
					.add(BodyPreference.builder().bodyType(MSEmailBodyType.HTML)
							.truncationSize(DEFAULT_TRUNCATION_SIZE).build())
					.add(BodyPreference.builder().bodyType(MSEmailBodyType.MIME)
							.truncationSize(DEFAULT_TRUNCATION_SIZE).build())
					.build();
	
	public FetchInstruction select(BodyPreferencePolicy bodyPreferencePolicy, List<BodyPreference> bodyPreferences, MimeMessage mimeMessage) {
		logger.debug("BodyPreferences {} MimeMessage {}", bodyPreferences, mimeMessage.getMimePart());
		
		List<BodyPreference> safeBodyPreferences = bodyPreferencesMatchingPolicy(bodyPreferencePolicy, bodyPreferences);
		List<FetchInstruction> fetchInstructions = fetchIntructions(bodyPreferencePolicy, safeBodyPreferences, mimeMessage);
		return selectBetterFit(bodyPreferencePolicy, fetchInstructions, safeBodyPreferences);
	}

	private List<BodyPreference> bodyPreferencesMatchingPolicy(BodyPreferencePolicy bodyPreferencePolicy, List<BodyPreference> bodyPreferences) {
		if (BodyPreferencePolicy.STRICT_MATCH.equals(bodyPreferencePolicy)) {
			return bodyPreferences;
		}
		return Objects.firstNonNull(bodyPreferences, ImmutableList.<BodyPreference>of());
	}

	private List<FetchInstruction> fetchIntructions(BodyPreferencePolicy bodyPreferencePolicy, List<BodyPreference> bodyPreferences, MimeMessage mimeMessage) {
		List<FetchInstruction> fetchInstructions = findMatchingInstructions(bodyPreferencePolicy, bodyPreferences, mimeMessage);
		if (!fetchInstructions.isEmpty()) {
			return fetchInstructions;
		} else if (BodyPreferencePolicy.ANY_MATCH.equals(bodyPreferencePolicy)){
			return findMatchingInstructions(bodyPreferencePolicy, DEFAULT_BODY_PREFERENCES, mimeMessage);
		}
		return fetchInstructions;
	}

	@VisibleForTesting FetchInstruction selectBetterFit(BodyPreferencePolicy bodyPreferencePolicy, 
			List<FetchInstruction> fetchInstructions, final List<BodyPreference> bodyPreferences) {
		
		if (BodyPreferencePolicy.STRICT_MATCH.equals(bodyPreferencePolicy) && fetchInstructions.isEmpty()) {
			return null;
		}
		Preconditions.checkArgument(!fetchInstructions.isEmpty());
		return Ordering
				.from(betterFitComparator(bodyPreferences))
				.min(fetchInstructions);
	}

	@VisibleForTesting static Comparator<FetchInstruction> betterFitComparator(final List<BodyPreference> bodyPreferences) {
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
	
	private List<FetchInstruction> findMatchingInstructions(BodyPreferencePolicy bodyPreferencePolicy, List<BodyPreference> bodyPreferences, MimeMessage mimeMessage) {
		List<FetchInstruction> fetchInstructions = Lists.newArrayList();
		for (BodyPreference bodyPreference: bodyPreferences) {
			if (isContentType(bodyPreference)) {
				fetchInstructions.addAll(findMatchingInstruction(bodyPreferencePolicy, mimeMessage, bodyPreference));
			} else {
				fetchInstructions.add(buildFetchInstruction(FetchInstruction.builder(), mimeMessage, bodyPreference));
			}
		}
		return fetchInstructions;
	}

	private List<FetchInstruction> findMatchingInstruction(BodyPreferencePolicy bodyPreferencePolicy, MimeMessage mimeMessage, BodyPreference bodyPreference) {
		List<FetchInstruction> fetchInstructions = Lists.newArrayList();
		for (FetchHints hints: listContentTypes(bodyPreferencePolicy, bodyPreference.getType())) {
			MimePart mimePart =  mimeMessage.findMainMessage(hints.getContentType());
			if (isOptionsMatching(mimePart, bodyPreference)) {
				fetchInstructions.add(buildFetchInstruction(hints.getInstruction(), mimePart, bodyPreference));
			}
		}
		return fetchInstructions;
	}

	private boolean isOptionsMatching(MimePart mimePart, BodyPreference bodyPreference) {
		if (mimePart != null) {
			if (bodyPreference.isAllOrNone() && bodyPreference.getTruncationSize() != null) {
				return mimePart.getSize() < bodyPreference.getTruncationSize();
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	private FetchInstruction buildFetchInstruction(FetchInstruction.Builder instruction, MimePart mimePart, BodyPreference bodyPreference) {
		return instruction
			.mimePart(mimePart)
			.truncation(bodyPreference.getTruncationSize())
			.bodyType(bodyPreference.getType())
			.build();
	}

	private List<FetchHints> listContentTypes(BodyPreferencePolicy bodyPreferencePolicy, MSEmailBodyType bodyType) {
		switch (bodyType) {
		case HTML:
			if (BodyPreferencePolicy.ANY_MATCH.equals(bodyPreferencePolicy)) {
				return Arrays.asList(
							FetchHints.builder()
								.contentType(toContentType(MSEmailBodyType.HTML)).build(),
							FetchHints.builder()
								.contentType(toContentType(MSEmailBodyType.PlainText))
								.instruction(FetchInstruction.builder().mailTransformation(MailTransformation.TEXT_PLAIN_TO_TEXT_HTML))
								.build());
			}
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
	
	private boolean isContentType(BodyPreference bodyPreference) {
		return bodyPreference.getType() != MSEmailBodyType.MIME;
	}
	
	private ContentType toContentType(MSEmailBodyType bodyType) {
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