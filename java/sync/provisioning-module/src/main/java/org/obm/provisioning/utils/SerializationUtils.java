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
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserAddress;
import fr.aliacom.obm.common.user.UserEmails;
import fr.aliacom.obm.common.user.UserExtId;
import fr.aliacom.obm.common.user.UserIdentity;
import fr.aliacom.obm.common.user.UserLogin;
import fr.aliacom.obm.common.user.UserPassword;
import fr.aliacom.obm.common.user.UserPhones;
import fr.aliacom.obm.common.user.UserWork;

public class SerializationUtils {
	
	private static final String PATTERN_AT_STAR = "@*";
	
	public static final ServiceProperty IMAP_SERVICE_PROPERTY = ServiceProperty
			.builder()
			.service("mail")
			.property("imap")
			.build();

	public static ObmHost getMailHostValue(JsonNode jsonNode, ObmDomain domain) {
		ObmHost mailHost = null;

		JsonNode serverNode = jsonNode.findValue(MAIL_SERVER.asSpecificationValue());
		JsonNode emailsNode = jsonNode.findValue(MAILS.asSpecificationValue());

		final Collection<String> mails = !isNullOrNullNode(emailsNode) ? getCurrentTokenTextValues(emailsNode) : null;
		final Collection<ObmHost> imapServices = domain.getHosts().get(IMAP_SERVICE_PROPERTY);

		if (!isNullOrNullNode(serverNode)) {
			mailHost = findMailHostForUser(serverNode.asText(), imapServices);
		}
		else if (mails != null && !mails.isEmpty()) {
			mailHost = Iterables.getFirst(imapServices, null);
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
	
	private static Collection<String> getMailsTokenTextValues(JsonNode value) {
		return Collections2.transform(getCurrentTokenTextValues(value), new Function<String, String>() {
			@Override
			public String apply(String input) {
				return input.replace(PATTERN_AT_STAR, "");
			}
		});
	}
	
	public static void addFieldValueToBuilder(JsonNode jsonNode, UserJsonFields jsonFields, 
			ObmUser.Builder toBuild, UserIdentity.Builder userIdentityBuilder, UserAddress.Builder addressBuilder,
			UserPhones.Builder phonesBuilder, UserWork.Builder userWorkBuilder, UserEmails.Builder emailsBuilder) {
		JsonNode value = jsonNode.findValue(jsonFields.asSpecificationValue());

		if (isNullOrNullNode(value)) {
			return;
		}

		switch (jsonFields) {
			case ADDRESSES:
				addressBuilder.addressParts(getCurrentTokenTextValues(value));
				break;
			case BUSINESS_ZIPCODE:
				addressBuilder.expressPostal(value.asText());
				break;
			case COMMONNAME:
				userIdentityBuilder.commonName(value.asText());
				break;
			case COMPANY:
				userWorkBuilder.company(value.asText());
				break;
			case COUNTRY:
				addressBuilder.countryCode(value.asText());
				break;
			case DESCRIPTION:
				toBuild.description(value.asText());
				break;
			case DIRECTION:
				userWorkBuilder.direction(value.asText());
				break;
			case FAXES:
				phonesBuilder.faxes(getCurrentTokenTextValues(value));
				break;
			case FIRSTNAME:
				userIdentityBuilder.firstName(value.asText());
				break;
			case GROUPS:
				break;
			case ID:
				toBuild.extId(UserExtId.builder().extId(value.asText()).build());
				break;
			case KIND:
				userIdentityBuilder.kind(value.asText());
				break;
			case LASTNAME:
				userIdentityBuilder.lastName(value.asText());
				break;
			case LOGIN:
				toBuild.login(UserLogin.valueOf(value.asText()));
				break;
			case MAILS:
				emailsBuilder.addresses(getMailsTokenTextValues(value));
				break;
			case EFFECTIVEMAILS:
				break;
			case MAIL_QUOTA:
				emailsBuilder.quota(Integer.parseInt(value.asText()));
				break;
			case MAIL_SERVER:
				break;
			case ARCHIVED:
				toBuild.archived(value.asBoolean());
				break;
			case MOBILE:
				phonesBuilder.mobile(value.asText());
				break;
			case PASSWORD:
				toBuild.password(UserPassword.valueOf(value.asText()));
				break;
			case PHONES:
				phonesBuilder.phones(getCurrentTokenTextValues(value));
				break;
			case PROFILE:
				toBuild.profileName(ProfileName.valueOf(value.asText()));
				break;
			case SERVICE:
				userWorkBuilder.service(value.asText());
				break;
			case TIMECREATE:
				toBuild.timeCreate(date(value.asText()));
				break;
			case TIMEUPDATE:
				toBuild.timeUpdate(date(value.asText()));
				break;
			case TITLE:
				userWorkBuilder.title(value.asText());
				break;
			case TOWN:
				addressBuilder.town(value.asText());
				break;
			case ZIPCODE:
				addressBuilder.zipCode(value.asText());
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


	public static String[] serializeUserEmailAddresses(UserEmails emails) {
		return FluentIterable
				.from(emails.getAddresses())
				.transform(new Function<String, String>() {
					@Override
					public String apply(String input) {
						return appendSuffixToEmailIfRequired(input, PATTERN_AT_STAR);
					}
				}).toArray(String.class);
	}

	private static String appendSuffixToEmailIfRequired(String emailAddress, String pattern) {
		if (!emailAddress.contains("@")) {
			return emailAddress + pattern;
		}
		return emailAddress;
	}
	
}
