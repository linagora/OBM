/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2015  Linagora
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
package org.obm.provisioning.processing.impl.users.sieve;

import java.util.Set;

import org.obm.imap.sieve.SieveConstants;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

public class SieveSerializer {
	private final NewSieveContent newSieveContent;
	private StringBuilder lines;

	public SieveSerializer(NewSieveContent newSieveContent) {
		this.newSieveContent = newSieveContent;
		this.lines = null;
	}
	
	public String serialize() {
		this.lines = new StringBuilder();
		this.appendRequires();
		this.appendUserRules();
		this.appendObmRules();
		return lines.toString();
	}

	private void appendRequires() {
		Set<String> requires = this.newSieveContent.getAllRequires();
		if (requires.isEmpty()) {
			return;
		}
		Iterable<String> requiresWithQuotes = Iterables.transform(requires, new Function<String, String>() {

			@Override
			public String apply(String require) {
				return String.format("\"%s\"", require);
			}
			
		});
		String requireList = Joiner.on(", ").join(requiresWithQuotes);
		String requireLine = String.format("require [%s];", requireList);
		addLine(requireLine);
	}
	
	private void appendUserRules() {
		Optional<OldSieveContent> maybeOldSieveContent = this.newSieveContent.getMaybeOldSieveContent();
		if (maybeOldSieveContent.isPresent()) {
			for (String line : maybeOldSieveContent.get().getUserRules()) {
				addLine(line);
			}
		}
	}
	
	private void appendObmRules() {
		for (ObmRule obmRule : newSieveContent.getObmRules()) {
			String header = String.format("# rule:[OBM %s]", obmRule.getName());
			addLine(header);
			for (String line : obmRule.getContent()) {
				addLine(line);
			}
		}
	}
	
	private void addLine(String line) {
		this.lines.append(line);
		this.lines.append(SieveConstants.SEP);
	}
}
