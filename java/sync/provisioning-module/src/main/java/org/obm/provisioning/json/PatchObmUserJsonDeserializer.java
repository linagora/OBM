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
package org.obm.provisioning.json;

import static org.obm.provisioning.utils.SerializationUtils.addFieldValueToBuilder;
import static org.obm.provisioning.utils.SerializationUtils.getMailHostValue;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.obm.provisioning.bean.UserJsonFields;
import org.obm.sync.host.ObmHost;

import fr.aliacom.obm.common.user.ObmUser;

public class PatchObmUserJsonDeserializer extends JsonDeserializer<ObmUser> {
	
	public PatchObmUserJsonDeserializer(ObmUser oldUser) {
		this.oldUser = oldUser;
	}

	private final ObmUser oldUser;
	
	@Override
	public ObmUser deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		ObmUser.Builder builder = ObmUser.builder().from(oldUser);
		
		JsonNode jsonNode = jp.readValueAsTree();
		
		for(UserJsonFields field: UserJsonFields.fields) {
			addFieldValueToBuilder(jsonNode, field, builder);
		}
		
		ObmHost mailHost = getMailHostValue(jsonNode, oldUser.getDomain());
		
		if (mailHost != null) {
			builder.mailHost(mailHost);
		}

		return builder.build();
	}

}
