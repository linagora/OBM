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
package org.obm.provisioning.json;

import static org.obm.provisioning.bean.UserJsonFields.ADDRESSES;
import static org.obm.provisioning.bean.UserJsonFields.ARCHIVED;
import static org.obm.provisioning.bean.UserJsonFields.BUSINESS_ZIPCODE;
import static org.obm.provisioning.bean.UserJsonFields.COMMONNAME;
import static org.obm.provisioning.bean.UserJsonFields.COMPANY;
import static org.obm.provisioning.bean.UserJsonFields.COUNTRY;
import static org.obm.provisioning.bean.UserJsonFields.DELEGATION;
import static org.obm.provisioning.bean.UserJsonFields.DELEGATIONTARGET;
import static org.obm.provisioning.bean.UserJsonFields.DESCRIPTION;
import static org.obm.provisioning.bean.UserJsonFields.DIRECTION;
import static org.obm.provisioning.bean.UserJsonFields.EFFECTIVEMAILS;
import static org.obm.provisioning.bean.UserJsonFields.EXPIRATIONDATE;
import static org.obm.provisioning.bean.UserJsonFields.FAXES;
import static org.obm.provisioning.bean.UserJsonFields.FIRSTNAME;
import static org.obm.provisioning.bean.UserJsonFields.GROUPS;
import static org.obm.provisioning.bean.UserJsonFields.HIDDEN;
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
import static org.obm.provisioning.bean.UserJsonFields.SAMBA_ALLOWED;
import static org.obm.provisioning.bean.UserJsonFields.SAMBA_HOME_DRIVE;
import static org.obm.provisioning.bean.UserJsonFields.SAMBA_HOME_FOLDER;
import static org.obm.provisioning.bean.UserJsonFields.SAMBA_LOGON_SCRIPT;
import static org.obm.provisioning.bean.UserJsonFields.SERVICE;
import static org.obm.provisioning.bean.UserJsonFields.TIMECREATE;
import static org.obm.provisioning.bean.UserJsonFields.TIMEUPDATE;
import static org.obm.provisioning.bean.UserJsonFields.TITLE;
import static org.obm.provisioning.bean.UserJsonFields.TOWN;
import static org.obm.provisioning.bean.UserJsonFields.ZIPCODE;
import static org.obm.provisioning.bean.UserJsonFields.NOMAD_ENABLED;
import static org.obm.provisioning.bean.UserJsonFields.NOMAD_EMAIL;
import static org.obm.provisioning.bean.UserJsonFields.NOMAD_ALLOWED;

import java.io.IOException;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.obm.provisioning.Group;
import org.obm.provisioning.bean.GroupIdentifier;
import org.obm.provisioning.utils.SerializationUtils;
import org.obm.sync.host.ObmHost;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;


public class ObmUserJsonSerializer extends JsonSerializer<ObmUser> {

	@Override
	public void serialize(ObmUser value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		
		jgen.writeStartObject();
		jgen.writeObjectField(ID.asSpecificationValue(), value.getExtId());
		jgen.writeStringField(LOGIN.asSpecificationValue(), value.getLogin());
		jgen.writeStringField(LASTNAME.asSpecificationValue(), value.getLastName());
		jgen.writeStringField(PROFILE.asSpecificationValue(),
				value.getProfileName() != null ? value.getProfileName().getName() : null);
		jgen.writeStringField(FIRSTNAME.asSpecificationValue(), value.getFirstName());
		jgen.writeStringField(COMMONNAME.asSpecificationValue(), value.getCommonName());
		jgen.writeStringField(PASSWORD.asSpecificationValue(), 
				value.getPassword() != null ? value.getPassword().getStringValue() : null);
		jgen.writeStringField(KIND.asSpecificationValue(), value.getKind());
		jgen.writeStringField(TITLE.asSpecificationValue(), value.getTitle());
		jgen.writeStringField(DESCRIPTION.asSpecificationValue(), value.getDescription());
		jgen.writeStringField(COMPANY.asSpecificationValue(), value.getCompany());
		jgen.writeStringField(SERVICE.asSpecificationValue(), value.getService());
		jgen.writeStringField(DIRECTION.asSpecificationValue(), value.getDirection());
		writeObjectsField(jgen, ADDRESSES.asSpecificationValue(),
				value.getAddress1(), value.getAddress2(), value.getAddress3());
		jgen.writeStringField(TOWN.asSpecificationValue(), value.getTown());
		jgen.writeStringField(ZIPCODE.asSpecificationValue(), value.getZipCode());
		jgen.writeStringField(BUSINESS_ZIPCODE.asSpecificationValue(), value.getExpresspostal());
		jgen.writeStringField(COUNTRY.asSpecificationValue(), value.getCountryCode());
		writeObjectsField(jgen, PHONES.asSpecificationValue(), value.getPhone(), value.getPhone2());
		jgen.writeStringField(MOBILE.asSpecificationValue(), value.getMobile());
		writeObjectsField(jgen, FAXES.asSpecificationValue(), value.getFax(), value.getFax2());
		jgen.writeBooleanField(ARCHIVED.asSpecificationValue(), value.isArchived());
		jgen.writeStringField(MAIL_QUOTA.asSpecificationValue(), String.valueOf(Objects.firstNonNull(value.getMailQuota(), 0)));
		jgen.writeStringField(MAIL_SERVER.asSpecificationValue(), getMailHostName(value));
		jgen.writeObjectField(MAILS.asSpecificationValue(), SerializationUtils.serializeUserEmailAddresses(value.getUserEmails()));
		jgen.writeObjectField(EFFECTIVEMAILS.asSpecificationValue(), Iterables.toArray(value.expandAllEmailDomainTuples(), String.class));
		jgen.writeBooleanField(HIDDEN.asSpecificationValue(), value.isHidden());

		jgen.writeBooleanField(NOMAD_ENABLED.asSpecificationValue(), value.getNomad().isEnabled());
		jgen.writeStringField(NOMAD_EMAIL.asSpecificationValue(), value.getNomad().getEmail());
		jgen.writeBooleanField(NOMAD_ALLOWED.asSpecificationValue(), value.getNomad().isAllowed());

		jgen.writeObjectField(TIMECREATE.asSpecificationValue(), value.getTimeCreate());
		jgen.writeObjectField(TIMEUPDATE.asSpecificationValue(), value.getTimeUpdate());
		jgen.writeObjectField(EXPIRATIONDATE.asSpecificationValue(), value.getExpirationDate());
		jgen.writeStringField(DELEGATION.asSpecificationValue(), value.getDelegation());
		jgen.writeStringField(DELEGATIONTARGET.asSpecificationValue(), value.getDelegationTarget());
		jgen.writeObjectField(GROUPS.asSpecificationValue(), extractGroupIdentifiers(value.getGroups(), value.getDomain()));
		jgen.writeBooleanField(SAMBA_ALLOWED.asSpecificationValue(), value.isSambaAllowed());
		jgen.writeStringField(SAMBA_HOME_DRIVE.asSpecificationValue(), value.getSambaHomeDrive());
		writeRawField(jgen, SAMBA_HOME_FOLDER.asSpecificationValue(), value.getSambaHomeFolder());
		jgen.writeStringField(SAMBA_LOGON_SCRIPT.asSpecificationValue(), value.getSambaLogonScript());
		jgen.writeEndObject();
	}

	
	private String getMailHostName(ObmUser user) {
		ObmHost host = user.getMailHost();

		return host != null ? host.getName() : null;
	}

	private void writeObjectsField(JsonGenerator jgen, String fieldName, Object... values)
			throws JsonGenerationException, IOException {
		jgen.writeFieldName(fieldName);
		jgen.writeStartArray();
		for(Object value: values) {
			if (value != null) {
				jgen.writeObject(value);
			}
		}
		jgen.writeEndArray();
	}
	
	private void writeRawField(JsonGenerator jgen, String fieldName, String value) throws JsonGenerationException, IOException {
		if (value == null) {
			jgen.writeStringField(fieldName, null);
		} else {
			String asStringValue = ",\"" + fieldName + "\":\"" + value.replace("\"", "") + "\"";
			
			jgen.writeRaw(asStringValue, 0, asStringValue.length());
		}
	}
	
	private Set<GroupIdentifier> extractGroupIdentifiers(Set<Group> groups, ObmDomain domain) {
		if(groups == null) {
			return ImmutableSet.of();
		}
		
		Set<GroupIdentifier> identifiers = Sets.newHashSet();
		for(Group group: groups) {
			GroupIdentifier identifier = 
					GroupIdentifier.builder()
								.id(group.getExtId())
								.domainUuid(domain.getUuid())
								.build();
			identifiers.add(identifier);
		}
		
		return identifiers;
	}
 }
