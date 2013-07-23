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
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.bean.UserJsonFields;
import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.ObmUser.Builder;
import fr.aliacom.obm.common.user.UserExtId;

public class ObmUserJsonDeserializer extends JsonDeserializer<ObmUser> {

	private final Provider<ObmDomain> domainProvider;
	private final Builder builder = ObmUser.builder();

	@Inject
	public ObmUserJsonDeserializer(Provider<ObmDomain> domainProvider) {
		this.domainProvider = domainProvider;
	}

	@Override
	public ObmUser deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
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
		addFieldValueToBuilder(jsonNode, MAILS);
		addFieldValueToBuilder(jsonNode, TIMECREATE);
		addFieldValueToBuilder(jsonNode, TIMEUPDATE);

		ObmHost mailHost = null;
		JsonNode emailsNode = getCurrentTokenTextValue(jsonNode, MAILS);
		ObmDomain domain = domainProvider.get();
		Collection<String> mails = emailsNode != null ? getCurrentTokenTextValues(emailsNode) : null;

		if (mails != null && !mails.isEmpty()) {
			JsonNode serverNode = getCurrentTokenTextValue(jsonNode, MAIL_SERVER);

			mailHost = findMailHostForUser(serverNode != null ? serverNode.asText() : null, domain.getHosts().get(ServiceProperty.IMAP));
		}

		return builder
				.domain(domain)
				.mailHost(mailHost)
				.build();
	}

	@VisibleForTesting
	ObmHost findMailHostForUser(String hostName, Collection<ObmHost> domainHosts) {
		if (hostName != null) {
			for (ObmHost host : domainHosts) {
				if (host.getName().equals(hostName)) {
					return host;
				}
			}
		}

		return Iterables.getFirst(domainHosts, null);
	}

	private void addFieldValueToBuilder(JsonNode jsonNode, UserJsonFields jsonFields) {
		JsonNode value = getCurrentTokenTextValue(jsonNode, jsonFields);

		if (value == null) {
			return;
		}

		switch (jsonFields) {
			case ADDRESSES:
				builder.addresses(getCurrentTokenTextValues(value));
				break;
			case BUSINESS_ZIPCODE:
				builder.expresspostal(value.asText());
				break;
			case COMMONNAME:
				builder.commonName(value.asText());
				break;
			case COMPANY:
				builder.company(value.asText());
				break;
			case COUNTRY:
				builder.countryCode(value.asText());
				break;
			case DESCRIPTION:
				builder.description(value.asText());
				break;
			case DIRECTION:
				builder.direction(value.asText());
				break;
			case FAXES:
				builder.faxes(getCurrentTokenTextValues(value));
				break;
			case FIRSTNAME:
				builder.firstName(value.asText());
				break;
			case GROUPS:
				// NOT IMPLEMENTED YET
				break;
			case ID:
				builder.extId(UserExtId.builder().extId(value.asText()).build());
				break;
			case KIND:
				builder.kind(value.asText());
				break;
			case LASTNAME:
				builder.lastName(value.asText());
				break;
			case LOGIN:
				builder.login(value.asText());
				break;
			case MAILS:
				builder.mails(getCurrentTokenTextValues(value));
				break;
			case MAIL_QUOTA:
				builder.mailQuota(Integer.parseInt(value.asText()));
				break;
			case MAIL_SERVER:
				break;
			case MOBILE:
				builder.mobile(value.asText());
				break;
			case PASSWORD:
				builder.password(value.asText());
				break;
			case PHONES:
				builder.phones(getCurrentTokenTextValues(value));
				break;
			case PROFILE:
				builder.profileName(ProfileName.valueOf(value.asText()));
				break;
			case SERVICE:
				builder.service(value.asText());
				break;
			case TIMECREATE:
				builder.timeCreate(date(value.asText()));
				break;
			case TIMEUPDATE:
				builder.timeUpdate(date(value.asText()));
				break;
			case TITLE:
				builder.title(value.asText());
				break;
			case TOWN:
				builder.town(value.asText());
				break;
			case ZIPCODE:
				builder.zipCode(value.asText());
				break;
		}
	}

	private Collection<String> getCurrentTokenTextValues(JsonNode value) {
		List<JsonNode> asList = Lists.newArrayList(value.getElements());

		return Collections2.transform(asList, new Function<JsonNode, String>() {
			@Override
			public String apply(JsonNode input) {
				return input.asText();
			}
		});
	}

	private JsonNode getCurrentTokenTextValue(JsonNode json, UserJsonFields jsonFields) {
		return json.findValue(jsonFields.asSpecificationValue());
	}

}
