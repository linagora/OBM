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
package org.obm.push.protocol.data;

import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.ASRequestIntegerFieldException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.exception.activesync.ASRequestBooleanFieldException;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncRequest.Builder;
import org.obm.push.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SyncDecoder {

	private static final Logger logger = LoggerFactory.getLogger(SyncDecoder.class);

	private static final String AS_BOOLEAN_TRUE = "1";
	
	@Inject
	protected SyncDecoder() {}

	public SyncRequest decodeSync(Document doc) 
			throws PartialException, ProtocolException, DaoException, CollectionPathException {
		Builder requestBuilder = new SyncRequest.Builder();
		Element root = doc.getDocumentElement();
		
		requestBuilder.waitInMinute(getWait(root));
		requestBuilder.partial(isPartial(root));
		return requestBuilder.build();
	}

	@VisibleForTesting Integer getWait(Element root) {
		String wait = DOMUtils.getElementText(root, SyncRequestFields.WAIT.getName());
		logger.debug("Wait value : " + wait);
		
		if (wait != null) {
			try {
				return Integer.parseInt(wait);
			} catch (NumberFormatException e) {
				throw new ASRequestIntegerFieldException("Failed to parse field : " + SyncRequestFields.WAIT.getName(), e);
			}
		}
		return null;
	}

	@VisibleForTesting Boolean isPartial(Element root) {
		return uniqueBooleanFieldValue(root, SyncRequestFields.PARTIAL);
	}

	private Boolean uniqueBooleanFieldValue(Element root, SyncRequestFields booleanField) {
		Element element = DOMUtils.getUniqueElement(root, booleanField.getName());
		if (element == null) {
			return null;
		}
		
		String elementText = DOMUtils.getElementText(element);
		logger.debug(booleanField.getName() + " value : " + elementText);
		if (elementText == null) {
			return true;
		} else if( !elementText.equals("1") && !elementText.equals("0")) {
			throw new ASRequestBooleanFieldException("Failed to parse field : " + booleanField.getName());
		}
		return elementText.equalsIgnoreCase(AS_BOOLEAN_TRUE);
	}
}
