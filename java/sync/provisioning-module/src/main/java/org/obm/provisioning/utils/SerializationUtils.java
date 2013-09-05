/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2013  Linagora
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
package org.obm.provisioning.utils;

import static org.obm.provisioning.bean.UserJsonFields.MAILS;
import static org.obm.provisioning.bean.UserJsonFields.MAIL_SERVER;
import static org.obm.push.utils.DateUtils.date;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.obm.provisioning.Group;
import org.obm.provisioning.GroupExtId;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.bean.UserJsonFields;
import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

public class SerializationUtils {
	
	public static final ServiceProperty IMAP_SERVICE_PROPERTY = ServiceProperty
			.builder()
			.service("mail")
			.property("imap")
			.build();

	public static ObmHost getMailHostValue(JsonNode jsonNode, ObmDomain domain) {
		ObmHost mailHost = null;
		JsonNode emailsNode = jsonNode.findValue(MAILS.asSpecificationValue());
		Collection<String> mails = emailsNode != null ? getCurrentTokenTextValues(emailsNode) : null;

		if (mails != null && !mails.isEmpty()) {
			JsonNode serverNode = jsonNode.findValue(MAIL_SERVER.asSpecificationValue());

			final Collection<ObmHost> imapServices = domain.getHosts().get(IMAP_SERVICE_PROPERTY);
			if (serverNode != null) {
				mailHost = findMailHostForUser(serverNode.asText(), imapServices);
			} else {
				mailHost = Iterables.getFirst(imapServices, null);
			}
		}
		
		return mailHost;
	}
	
	@VisibleForTesting static ObmHost findMailHostForUser(String hostName, Collection<ObmHost> domainHosts) {
		for (ObmHost host : domainHosts) {
			if (host.getName().equals(hostName)) {
				return host;
			}
		}
		
		return Iterables.getFirst(domainHosts, null);
	}

	private static Collection<String> getCurrentTokenTextValues(JsonNode value) {
		Iterator<String> it = Iterators.transform(value.getElements(), new Function<JsonNode, String>() {
				@Override
				public String apply(JsonNode input) {
					return input.asText();
				}
			});
		
		Collection<String> textValues = Lists.newArrayList();
		Iterators.addAll(textValues, it);
		
		return textValues;
	}
	
	public static void addFieldValueToBuilder(JsonNode jsonNode, UserJsonFields jsonFields, ObmUser.Builder toBuild) {
		JsonNode value = jsonNode.findValue(jsonFields.asSpecificationValue());

		if (isNullOrNullNode(value)) {
			return;
		}

		switch (jsonFields) {
			case ADDRESSES:
				toBuild.addresses(getCurrentTokenTextValues(value));
				break;
			case BUSINESS_ZIPCODE:
				toBuild.expresspostal(value.asText());
				break;
			case COMMONNAME:
				toBuild.commonName(value.asText());
				break;
			case COMPANY:
				toBuild.company(value.asText());
				break;
			case COUNTRY:
				toBuild.countryCode(value.asText());
				break;
			case DESCRIPTION:
				toBuild.description(value.asText());
				break;
			case DIRECTION:
				toBuild.direction(value.asText());
				break;
			case FAXES:
				toBuild.faxes(getCurrentTokenTextValues(value));
				break;
			case FIRSTNAME:
				toBuild.firstName(value.asText());
				break;
			case GROUPS:
				break;
			case ID:
				toBuild.extId(UserExtId.builder().extId(value.asText()).build());
				break;
			case KIND:
				toBuild.kind(value.asText());
				break;
			case LASTNAME:
				toBuild.lastName(value.asText());
				break;
			case LOGIN:
				toBuild.login(value.asText());
				break;
			case MAILS:
				toBuild.mails(getCurrentTokenTextValues(value));
				break;
			case MAIL_QUOTA:
				toBuild.mailQuota(Integer.parseInt(value.asText()));
				break;
			case MAIL_SERVER:
				break;
			case ARCHIVED:
				toBuild.archived(value.asBoolean());
				break;
			case MOBILE:
				toBuild.mobile(value.asText());
				break;
			case PASSWORD:
				toBuild.password(value.asText());
				break;
			case PHONES:
				toBuild.phones(getCurrentTokenTextValues(value));
				break;
			case PROFILE:
				toBuild.profileName(ProfileName.valueOf(value.asText()));
				break;
			case SERVICE:
				toBuild.service(value.asText());
				break;
			case TIMECREATE:
				toBuild.timeCreate(date(value.asText()));
				break;
			case TIMEUPDATE:
				toBuild.timeUpdate(date(value.asText()));
				break;
			case TITLE:
				toBuild.title(value.asText());
				break;
			case TOWN:
				toBuild.town(value.asText());
				break;
			case ZIPCODE:
				toBuild.zipCode(value.asText());
				break;
			case HIDDEN:
				toBuild.hidden(value.asBoolean());
				break;
		}
	}

	public static void readJsonGroup(JsonParser jp, Group.Builder builder) throws IOException, JsonProcessingException {
		JsonNode root = jp.readValueAsTree();

		JsonNode at = root.findValue("id");
		if (!isNullOrNullNode(at)) {
			builder.extId(GroupExtId.valueOf(at.asText()));
		}
		
		at = root.findValue("name");
		if (!isNullOrNullNode(at)) {
			builder.name(root.findValue("name").asText());
		}
		
		at = root.findValue("email");
		if (!isNullOrNullNode(at)) {
			builder.email(at.asText());
		}
		
		at = root.findValue("description");
		if (!isNullOrNullNode(at)) {
			builder.description(at.asText());
		}
	}

	private static boolean isNullOrNullNode(JsonNode at) {
		return at == null || at.isNull();
	}
}
