/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;
import fr.aliacom.obm.common.user.UserLogin;
import fr.aliacom.obm.common.user.UserPassword;

public interface DBData {

	ObmDomainUuid domainId = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
	ObmDomain domain = ObmDomain.builder()
			.name("mydomain.org")
			.uuid(domainId)
			.id(1)
			.build();
	ObmDomainUuid otherDomainId = ObmDomainUuid.of("31ae9172-ca35-4045-8ea3-c3125dab771e");
	ObmDomain otherDomain = ObmDomain.builder()
			.name("otherDomain.org")
			.uuid(otherDomainId)
			.id(2)
			.build();
	
	ObmUser admin = ObmUser.builder()
		.extId(UserExtId.valueOf("d4ad341d-89eb-4f3d-807a-cb372314845d"))
		.login(UserLogin.valueOf("admin"))
		.password(UserPassword.valueOf("trust3dToken"))
		.admin(true)
		.domain(domain)
		.build();
	ObmUser usera = ObmUser.builder()
		.extId(UserExtId.valueOf("08607f19-05a4-42a2-9b02-6f11f3ceff3b"))
		.login(UserLogin.valueOf("usera"))
		.password(UserPassword.valueOf("usera"))
		.domain(domain)
		.build();
	ObmUser userb = ObmUser.builder()
		.extId(UserExtId.valueOf("8e30e673-1c47-4ca8-85e8-4609d4228c10"))
		.login(UserLogin.valueOf("userb"))
		.password(UserPassword.valueOf("userb"))
		.domain(domain)
		.build();
	ObmUser userc = ObmUser.builder()
		.extId(UserExtId.valueOf("2d7a5942-46ab-4fad-9bd2-608bde249671"))
		.login(UserLogin.valueOf("userc"))
		.password(UserPassword.valueOf("userc"))
		.domain(domain)
		.build();
}
