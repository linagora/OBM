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
package org.obm.push.protocol.data;

import org.obm.push.bean.SyncDefaultValues;
import org.obm.push.exception.activesync.ASRequestBooleanFieldException;
import org.obm.push.exception.activesync.ASRequestIntegerFieldException;
import org.obm.push.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.google.common.base.Objects;
import com.google.common.primitives.Ints;

public class ActiveSyncDecoder implements SyncDefaultValues {

	private static final Logger logger = LoggerFactory.getLogger(ActiveSyncDecoder.class);
	
	private static final String AS_BOOLEAN_TRUE = "1";
	
	public String uniqueStringFieldValue(Element root, ActiveSyncFields stringField) {
		Element element = DOMUtils.getUniqueElement(root, stringField.getName());
		if (element == null) {
			return null;
		}
		
		String elementText = DOMUtils.getElementText(element);
		logger.debug(stringField.getName() + " value : " + elementText);
		return elementText;
	}

	public Boolean uniqueBooleanFieldValue(Element root, ActiveSyncFields booleanField) {
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

	public boolean uniqueBooleanFieldValue(Element root, ActiveSyncFields booleanField, boolean defaultValue) {
		return Objects.firstNonNull(uniqueBooleanFieldValue(root, booleanField), defaultValue);
	}

	public Integer uniqueIntegerFieldValue(Element root, ActiveSyncFields integerField) {
		Long value = uniqueNumberFieldValue(root, integerField);
		if (value != null) {
			return Ints.checkedCast(value);
		}
		return null;
	}

	public Long uniqueLongFieldValue(Element root, ActiveSyncFields longField) {
		return uniqueNumberFieldValue(root, longField);
	}

	private Long uniqueNumberFieldValue(Element root, ActiveSyncFields numberField) {
		String element = DOMUtils.getElementText(root, numberField.getName());
		logger.debug(numberField.getName() + " value : " + element);
		
		if (element != null) {
			try {
				return Long.parseLong(element);
			} catch (NumberFormatException e) {
				throw new ASRequestIntegerFieldException(e);
			}
		}
		return null;
	}

	public void appendBoolean(Element root, ActiveSyncFields booleanField, Boolean value) {
		if (value != null) {
			DOMUtils.createElementAndText(root, booleanField.getName(), value);
		}
	}

	public void appendInteger(Element root, ActiveSyncFields integerField, Integer value) {
		if (value != null) {
			DOMUtils.createElementAndText(root, integerField.getName(), value);
		}
	}

	public void appendString(Element root, ActiveSyncFields stringField, String value) {
		if (value != null) {
			DOMUtils.createElementAndText(root, stringField.getName(), value);
		}
	}
}
