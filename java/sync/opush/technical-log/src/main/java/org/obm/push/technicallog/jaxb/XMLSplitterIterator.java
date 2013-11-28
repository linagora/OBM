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
package org.obm.push.technicallog.jaxb;

import java.io.IOException;
import java.util.Iterator;

import com.google.common.base.Throwables;

public class XMLSplitterIterator implements Iterator<String> {
	
	private Boolean hasMoreElement;
	private String nextElement;
	private final XMLInputStreamSplitter splitter;

	public XMLSplitterIterator(XMLInputStreamSplitter splitter) {
		this.splitter = splitter;
	}
	
	@Override
	public boolean hasNext() {
		if (splitter == null) {
			return false;
		}
		
		if (hasMoreElement == null || hasMoreElement) {
			nextElement = retrieveNext();
		}
		return hasMoreElement;
	}

	@Override
	public String next() {
		if (nextElement != null) {
			String returnValue = nextElement;
			nextElement = null;
			return returnValue;
		}
		return retrieveNext();
	}

	private String retrieveNext() {
		String value = null;
		try {
			value = splitter.readNextXML();
			hasMoreElement = true;
		} catch (EndOfStreamException e) {
			hasMoreElement = false;
		} catch (IOException e) {
			Throwables.propagate(e);
		}
		return value;
	}

	@Override
	public void remove() {
		// we don't need this method yet
		throw new UnsupportedOperationException();
	}
}
