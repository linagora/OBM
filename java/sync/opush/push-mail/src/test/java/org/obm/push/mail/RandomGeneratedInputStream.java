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

import java.io.IOException;
import java.io.InputStream;


public class RandomGeneratedInputStream extends InputStream {

	private static final int NUMBER_CHARACTERS_PER_LINE = 76;
	private static final int CRLF_FIRST_PART_END_LINE = 74;
	private static final int CRLF_SECOND_PART_END_LINE = 75;
	private static final int CHARACTERS = 66;
	
	private final long length;
	private int cpt;

	public RandomGeneratedInputStream(long length) {
		this.length = length;
		this.cpt = 0;
	}
	
	@Override
	public int read() throws IOException {
		cpt += 1;
		if (cpt < length) {
			long modulo = cpt % NUMBER_CHARACTERS_PER_LINE;
			if (modulo == CRLF_FIRST_PART_END_LINE) {
				return '\r';
			} else if (modulo == CRLF_SECOND_PART_END_LINE) {
				return '\n';
			} else {
				return CHARACTERS;
			}
		} else {
			return -1;
		}
	}
	
	@Override
	public synchronized void reset() throws IOException {
		cpt = 0;
	}

}