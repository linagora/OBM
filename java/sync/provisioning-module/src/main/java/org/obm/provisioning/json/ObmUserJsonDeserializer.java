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
package org.obm.provisioning.json;

import static org.obm.provisioning.bean.UserJsonFields.ADDRESSES;
import static org.obm.provisioning.bean.UserJsonFields.BUSINESS_ZIPCODE;
import static org.obm.provisioning.bean.UserJsonFields.COMMONNAME;
import static org.obm.provisioning.bean.UserJsonFields.COMPANY;
import static org.obm.provisioning.bean.UserJsonFields.COUNTRY;
import static org.obm.provisioning.bean.UserJsonFields.DESCRIPTION;
import static org.obm.provisioning.bean.UserJsonFields.DIRECTION;
import static org.obm.provisioning.bean.UserJsonFields.FAXES;
import static org.obm.provisioning.bean.UserJsonFields.FIRSTNAME;
import static org.obm.provisioning.bean.UserJsonFields.ID;
import static org.obm.provisioning.bean.UserJsonFields.KIND;
import static org.obm.provisioning.bean.UserJsonFields.LASTNAME;
import static org.obm.provisioning.bean.UserJsonFields.LOGIN;
import static org.obm.provisioning.bean.UserJsonFields.MAILS;
import static org.obm.provisioning.bean.UserJsonFields.MAIL_QUOTA;
import static org.obm.provisioning.bean.UserJsonFields.MAIL_SERVER;
import static org.obm.provisioning.bean.UserJsonFields.MOBILE;
import static org.obm.provisioning.bean.UserJsonFields.PASSWORD;
import static org.obm.provisioning.bean.UserJsonFields.PHONES;
import static org.obm.provisioning.bean.UserJsonFields.PROFILE;
import static org.obm.provisioning.bean.UserJsonFields.SERVICE;
import static org.obm.provisioning.bean.UserJsonFields.TIMECREATE;
import static org.obm.provisioning.bean.UserJsonFields.TIMEUPDATE;
import static org.obm.provisioning.bean.UserJsonFields.TITLE;
import static org.obm.provisioning.bean.UserJsonFields.TOWN;
import static org.obm.provisioning.bean.UserJsonFields.ZIPCODE;
import static org.obm.push.utils.DateUtils.date;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.obm.provisioning.bean.UserJsonFields;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.ObmUser.Builder;
import fr.aliacom.obm.common.user.UserExtId;

public class ObmUserJsonDeserializer extends JsonDeserializer<ObmUser> {
	
	private Builder builder = ObmUser.builder();

	@Override
	public ObmUser deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode jsonNode = jp.readValueAsTree();
		addFieldValueToBuilder(jsonNode, ID);
		addFieldValueToBuilder(jsonNode, LOGIN);
		addFieldValueToBuilder(jsonNode, LASTNAME);
		addFieldValueToBuilder(jsonNode, PROFILE);
		addFieldValueToBuilder(jsonNode, FIRSTNAME);
		addFieldValueToBuilder(jsonNode, COMMONNAME);
		addFieldValueToBuilder(jsonNode, PASSWORD);
		addFieldValueToBuilder(jsonNode, KIND);
		addFieldValueToBuilder(jsonNode, TITLE);
		addFieldValueToBuilder(jsonNode, DESCRIPTION);
		addFieldValueToBuilder(jsonNode, COMPANY);
		addFieldValueToBuilder(jsonNode, SERVICE);
		addFieldValueToBuilder(jsonNode, DIRECTION);
		addFieldValueToBuilder(jsonNode, ADDRESSES);
		addFieldValueToBuilder(jsonNode, TOWN);
		addFieldValueToBuilder(jsonNode, ZIPCODE);
		addFieldValueToBuilder(jsonNode, BUSINESS_ZIPCODE);
		addFieldValueToBuilder(jsonNode, COUNTRY);
		addFieldValueToBuilder(jsonNode, PHONES);
		addFieldValueToBuilder(jsonNode, MOBILE);
		addFieldValueToBuilder(jsonNode, FAXES);
		addFieldValueToBuilder(jsonNode, MAIL_QUOTA);
		addFieldValueToBuilder(jsonNode, MAIL_SERVER);
		addFieldValueToBuilder(jsonNode, MAILS);
		addFieldValueToBuilder(jsonNode, TIMECREATE);
		addFieldValueToBuilder(jsonNode, TIMEUPDATE);
		
		ObmDomain domain = (ObmDomain) ctxt.findInjectableValue(ObmDomain.class.getName(), null, null);
		
		return builder.domain(domain).build();
	}

	private void addFieldValueToBuilder(JsonNode jsonNode, UserJsonFields jsonFields) {
		switch(jsonFields) {
		case ADDRESSES:
			Collection<String> addresses = getCurrentTokenTextValues(jsonNode, jsonFields);
			builder.addresses(addresses);
			break;
		case BUSINESS_ZIPCODE:
			// NOT IMPLEMENTED YET
			break;
		case COMMONNAME:
			builder.commonName(getCurrentTokenTextValue(jsonNode, jsonFields));
			break;
		case COMPANY:
			// NOT IMPLEMENTED YET
			break;
		case COUNTRY:
			// NOT IMPLEMENTED YET
			break;
		case DESCRIPTION:
			builder.description(getCurrentTokenTextValue(jsonNode, jsonFields));
			break;
		case DIRECTION:
			// NOT IMPLEMENTED YET
			break;
		case FAXES:
			// NOT IMPLEMENTED YET
			break;
		case FIRSTNAME:
			builder.firstName(getCurrentTokenTextValue(jsonNode, jsonFields));
			break;
		case GROUPS:
			// NOT IMPLEMENTED YET
			break;
		case ID:
			builder.extId(UserExtId.builder().extId(getCurrentTokenTextValue(jsonNode, jsonFields)).build());
			break;
		case KIND:
			// NOT IMPLEMENTED YET
			break;
		case LASTNAME:
			builder.lastName(getCurrentTokenTextValue(jsonNode, jsonFields));
			break;
		case LOGIN:
			builder.login(getCurrentTokenTextValue(jsonNode, jsonFields));
			break;
		case MAILS:
			Collection<String> mails = getCurrentTokenTextValues(jsonNode, jsonFields);
			builder.mails(mails);
			break;
		case MAIL_QUOTA:
			// NOT IMPLEMENTED YET
			break;
		case MAIL_SERVER:
			// NOT IMPLEMENTED YET
			break;
		case MOBILE:
			builder.mobile(getCurrentTokenTextValue(jsonNode, jsonFields));
			break;
		case PASSWORD:
			// NOT IMPLEMENTED YET
			break;
		case PHONES:
			break;
		case PROFILE:
			// NOT IMPLEMENTED YET
			break;
		case SERVICE:
			builder.service(getCurrentTokenTextValue(jsonNode, jsonFields));
			break;
		case TIMECREATE:
			builder.timeCreate(date(getCurrentTokenTextValue(jsonNode, jsonFields)));
			break;
		case TIMEUPDATE:
			builder.timeUpdate(date(getCurrentTokenTextValue(jsonNode, jsonFields)));
			break;
		case TITLE:
			builder.title(getCurrentTokenTextValue(jsonNode, jsonFields));
			break;
		case TOWN:
			builder.town(getCurrentTokenTextValue(jsonNode, jsonFields));
			break;
		case ZIPCODE:
			builder.zipCode(getCurrentTokenTextValue(jsonNode, jsonFields));
			break;
		}
	}

	private Collection<String> getCurrentTokenTextValues(JsonNode jsonNode, UserJsonFields jsonFields) {
		Iterator<JsonNode> values = jsonNode.findValue(jsonFields.asSpecificationValue()).getElements();
		List<JsonNode> asList = Lists.newArrayList();
		Iterators.addAll(asList, values);
		Collection<String> asStrings = Collections2.transform(asList, new Function<JsonNode, String>() {
			@Override
			public String apply(JsonNode input) {
				return input.asText();
			}
		});
		return asStrings;
	}

	private String getCurrentTokenTextValue(JsonNode json, UserJsonFields jsonFields) {
		return json.findValue(jsonFields.asSpecificationValue()).asText();
	}
}
