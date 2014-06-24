/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.provisioning.dto;

import java.util.UUID;

import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import com.google.common.base.Objects;

public class ObmDomainDto {

	private UUID id;
	private String name;
	private String label;
	private ImmutableList<String> aliases;

	ObmDomainDto() { //for jackson
	}
	
	public ObmDomainDto(UUID id, String name, String label, ImmutableList<String> aliases) {
		this.id = id;
		this.name = name;
		this.label = label;
		this.aliases = aliases;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String getLabel() {
		return label;
	}
	
	public ImmutableList<String> getAliases() {
		return aliases;
	}
	
	public ObmDomain toDomainObject() {
		return ObmDomain.builder()
			.uuid(ObmDomainUuid.of(getId().toString()))
			.name(getName())
			.label(getLabel())
			.aliases(getAliases())
			.build();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(id, name, label, aliases);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof ObmDomainDto) {
			ObmDomainDto that = (ObmDomainDto) object;
			return Objects.equal(this.id, that.id)
				&& Objects.equal(this.name, that.name)
				&& Objects.equal(this.label, that.label)
				&& Objects.equal(this.aliases, that.aliases);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("id", id)
			.add("name", name)
			.add("label", label)
			.add("aliases", aliases)
			.toString();
	}
	
}
