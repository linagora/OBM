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
package org.obm.imap.sieve.commands;

import java.util.ArrayList;
import java.util.List;

import org.obm.imap.sieve.SieveArg;
import org.obm.imap.sieve.SieveCommand;
import org.obm.imap.sieve.SieveConstants;
import org.obm.imap.sieve.SieveResponse;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;

public class SieveGetScript extends SieveCommand<String> {

	private String scriptName;

	public SieveGetScript(String scriptName) {
		retVal = null;
		this.scriptName = scriptName;
	}

	@Override
	protected List<SieveArg> buildCommand() {
		List<SieveArg> args = new ArrayList<SieveArg>(1);
		args.add(new SieveArg(String.format("GETSCRIPT \"%s\"", scriptName).getBytes(Charsets.UTF_8), false));
		return args;
	}

	@Override
	public void responseReceived(List<SieveResponse> rs) {
		if (commandSucceeded(rs)) {
			String data = rs.get(0).getData();
			Iterable<String> splitData = Splitter.on(SieveConstants.SPLIT_EXPR).split(data);
			FluentIterable<String> splitDataNoByteCount = FluentIterable.from(splitData).skip(1);
			FluentIterable<String> splitDataNoByteCountAndNoReturnCode = splitDataNoByteCount.limit(splitDataNoByteCount.size() -1);
			if (!splitDataNoByteCountAndNoReturnCode.isEmpty()) {
				this.retVal = Joiner.on(SieveConstants.SEP).join(splitDataNoByteCountAndNoReturnCode) + "\r\n";
			}
			else {
				throw new RuntimeException("Couldn't parse sieve response");
			}
		} else {
			reportErrors(rs);
		}
		logger.info("returning a sieve script");
	}

}
