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

import java.util.List;

import org.obm.push.bean.SyncKey;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.ASRequestBooleanFieldException;
import org.obm.push.exception.activesync.ASRequestIntegerFieldException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncRequest.Builder;
import org.obm.push.protocol.bean.SyncRequestCollection;
import org.obm.push.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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
		requestBuilder.windowSize(getWindowSize(root));
		
		List<SyncRequestCollection> syncRequestCollections = Lists.newArrayList();
		NodeList collectionNodes = root.getElementsByTagName(SyncRequestFields.COLLECTION.getName());
		for (int i = 0; i < collectionNodes.getLength(); i++) {
			syncRequestCollections.add(getCollection((Element)collectionNodes.item(i)));
		}
		requestBuilder.collections(syncRequestCollections);
		
		return requestBuilder.build();
	}

	@VisibleForTesting Integer getWait(Element root) {
		return uniqueIntegerFieldValue(root, SyncRequestFields.WAIT);
	}

	@VisibleForTesting Boolean isPartial(Element root) {
		return uniqueBooleanFieldValue(root, SyncRequestFields.PARTIAL);
	}

	@VisibleForTesting Integer getWindowSize(Element root) {
		return uniqueIntegerFieldValue(root, SyncRequestFields.WINDOW_SIZE);
	}

	@VisibleForTesting SyncRequestCollection getCollection(Element collection) {
		SyncRequestCollection.Builder builder = new SyncRequestCollection.Builder()
			.id(uniqueIntegerFieldValue(collection, SyncRequestFields.COLLECTION_ID))
			.syncKey(syncKey(uniqueStringFieldValue(collection, SyncRequestFields.SYNC_KEY)))
			.dataClass(uniqueStringFieldValue(collection, SyncRequestFields.DATA_CLASS))
			.windowSize(uniqueIntegerFieldValue(collection, SyncRequestFields.WINDOW_SIZE))
			.options(getOptions(DOMUtils.getUniqueElement(collection, SyncRequestFields.OPTIONS.getName())));
		
		return builder.build();
	}

	private SyncKey syncKey(String syncKey) {
		if(Strings.isNullOrEmpty(syncKey)) {
			return null;
		}
		return new SyncKey(syncKey);
	}
	
	@VisibleForTesting SyncCollectionOptions getOptions(Element optionElement) {
		if(optionElement == null) {
			return null;
		}
		
		SyncCollectionOptions options = new SyncCollectionOptions();
		options.setConflict(uniqueIntegerFieldValue(optionElement, SyncRequestFields.CONFLICT));
		options.setMimeSupport(uniqueIntegerFieldValue(optionElement, SyncRequestFields.MIME_SUPPORT));
		options.setMimeTruncation(uniqueIntegerFieldValue(optionElement, SyncRequestFields.MIME_TRUNCATION));
		
		String filterType = uniqueStringFieldValue(optionElement, SyncRequestFields.FILTER_TYPE);
		if (Strings.isNullOrEmpty(filterType)) {
			options.setFilterType(null);
		} else {
			options.setFilterType(FilterType.fromSpecificationValue(filterType));
		}
		
		options.setBodyPreferences(getBodyPreferences(optionElement));
		return options;
	}

	@VisibleForTesting List<BodyPreference> getBodyPreferences(Element optionElement) {
		NodeList bodyPreferenceNodes = optionElement.getElementsByTagName(SyncRequestFields.BODY_PREFERENCE.getName());
		List<BodyPreference> bodyPreferences = Lists.newArrayList();
		for (int i = 0; i < bodyPreferenceNodes.getLength(); i++) {
			bodyPreferences.add(getBodyPreference((Element)bodyPreferenceNodes.item(i)));
		}
		return bodyPreferences;
	}

	@VisibleForTesting BodyPreference getBodyPreference(Element bodyPreferenceElement) {
		Integer truncation = uniqueIntegerFieldValue(bodyPreferenceElement, SyncRequestFields.TRUNCATION_SIZE);
		Integer type = uniqueIntegerFieldValue(bodyPreferenceElement, SyncRequestFields.TYPE);
		Boolean allOrNone = uniqueBooleanFieldValue(bodyPreferenceElement, SyncRequestFields.ALL_OR_NONE);
		
		BodyPreference.Builder builder = BodyPreference.builder();
		if (truncation != null) {
			builder.truncationSize(truncation);
		}
		if (type != null) {
			builder.bodyType(MSEmailBodyType.getValueOf(type));
		}
		if (allOrNone != null) {
			builder.allOrNone(allOrNone);
		}
		return builder.build();
	}

	private String uniqueStringFieldValue(Element root, SyncRequestFields stringField) {
		Element element = DOMUtils.getUniqueElement(root, stringField.getName());
		if (element == null) {
			return null;
		}
		
		String elementText = DOMUtils.getElementText(element);
		logger.debug(stringField.getName() + " value : " + elementText);
		return elementText;
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

	private Integer uniqueIntegerFieldValue(Element root, SyncRequestFields integerField) {
		String element = DOMUtils.getElementText(root, integerField.getName());
		logger.debug(integerField.getName() + " value : " + element);
		
		if (element != null) {
			try {
				return Integer.parseInt(element);
			} catch (NumberFormatException e) {
				throw new ASRequestIntegerFieldException(e);
			}
		}
		return null;
	}
}
