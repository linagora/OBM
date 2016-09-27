/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.provisioning.json;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.beans.Request;
import org.obm.provisioning.processing.impl.OperationUtils;

import com.google.common.collect.Lists;

public class OperationJsonSerializer extends JsonSerializer<Operation> {

	@Override
	public void serialize(Operation value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeObjectField("status", value.getStatus());
		jgen.writeObjectField("entityType", value.getEntityType());
		jgen.writeFieldName("entity");
		writeBody(value,  jgen);
		jgen.writeObjectField("operation", value.getRequest().getVerb());
		jgen.writeStringField("error", value.getError());
		jgen.writeEndObject();
	}

	private void writeBody(Operation value, JsonGenerator jgen) throws JsonGenerationException, IOException{
		String body = value.getRequest().getBody();

		switch (value.getRequest().getVerb()) {
			case POST:
			case PUT:
			case PATCH:
				if (body != null) {
					switch (value.getEntityType()) {
					case EVENT:
					case CONTACT:
						jgen.writeString(body);
						break;
					default:
						jgen.writeRawValue(body);
						break;
					}
				} else {
					jgen.writeNull();
				}
				break;
			case GET:
			case DELETE:
				List<String[]> params = Lists.newArrayList();

				switch (value.getEntityType()) {
					case USER:
						params.add(
								new String[]{"id", Request.USERS_ID_KEY});
						break;
					case GROUP:
						params.add(
								new String[]{"id", Request.GROUPS_ID_KEY});
						break;
					case USER_MEMBERSHIP:
						params.add(
								new String[]{"userId", Request.USERS_ID_KEY});
						params.add(
								new String[]{"groupId", Request.GROUPS_ID_KEY});
						break;
					case GROUP_MEMBERSHIP:
						params.add(
								new String[]{"subgroupId", Request.SUBGROUPS_ID_KEY});
						params.add(
								new String[]{"groupId", Request.GROUPS_ID_KEY});
						break;
					case EVENT:
					case CONTACT:
					case ADDRESS_BOOK:
						params.add(
								new String[]{"userEmail", Request.USERS_EMAIL_KEY});
						break;
				}

				jgen.writeStartObject();
				for (String[] paramData : params) {
					String jsonFieldName = paramData[0];
					String requestKey = paramData[1];
					String paramValue = OperationUtils.getItemIdFromRequest(value, requestKey);
					jgen.writeObjectField(jsonFieldName, paramValue);
				}
				jgen.writeEndObject();
			break;
		}
	}

}