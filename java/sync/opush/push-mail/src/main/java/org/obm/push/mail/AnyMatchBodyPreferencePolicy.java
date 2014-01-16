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
import java.util.List;

import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.MSEmailBodyType;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

public class AnyMatchBodyPreferencePolicy extends BodyPreferencePolicy {

	@Override
	public List<BodyPreference> bodyPreferencesMatchingPolicy(List<BodyPreference> bodyPreferences) {
		return Objects.firstNonNull(bodyPreferences, ImmutableList.<BodyPreference>of());
	}

	@Override
	public boolean mayUsesDefaultBodyPreferences() {
		return true;
	}

	@Override
	public FetchInstruction selectBetterFit(List<FetchInstruction> fetchInstructions, List<BodyPreference> bodyPreferences) {
		Preconditions.checkArgument(!fetchInstructions.isEmpty());
		return Ordering
				.from(betterFitComparator(bodyPreferences))
				.min(fetchInstructions);
	}

	@Override
	public List<FetchHints> listContentTypes(MSEmailBodyType bodyType) {
		switch (bodyType) {
			case HTML:
				return Arrays.asList(
							FetchHints.builder()
								.contentType(toContentType(MSEmailBodyType.HTML)).build(),
							FetchHints.builder()
								.contentType(toContentType(MSEmailBodyType.PlainText))
								.instruction(FetchInstruction.builder().mailTransformation(MailTransformation.TEXT_PLAIN_TO_TEXT_HTML))
								.build());
			case PlainText:
			case RTF:
			default:
				return super.listContentTypes(bodyType);
		}
	}
}
