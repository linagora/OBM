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
package org.obm.push.context

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GatlingContextConfigurationTest extends FunSuite {
 
	test("GatlingContextConfiguration needs a non-null targetServerUrl") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build(null, "2", "5", "domain.org", "login", "pass", "policyKey", "id", "type")
		}
	}
	   
	test("GatlingContextConfiguration needs a non-empty targetServerUrl") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("", "2", "5", "domain.org", "login", "pass", "policyKey", "id", "type")
		}
	}
	
	test("GatlingContextConfiguration needs a non-null parallels scenarios count") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", null, "5", "domain.org", "login", "pass", "policyKey", "id", "type")
		}
	}
	   
	test("GatlingContextConfiguration needs a non-empty parallels scenarios count") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "", "5", "domain.org", "login", "pass", "policyKey", "id", "type")
		}
	}
	   
	test("GatlingContextConfiguration needs a parsable number for parallels scenarios count") {
		intercept[NumberFormatException] {
			GatlingConfiguration.build("192.168.0.1", "a2", "5", "domain.org", "login", "pass", "policyKey", "id", "type")
		}
	}
	
	test("GatlingContextConfiguration needs a non-null asynchronous change time") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "2", null, "domain.org", "login", "pass", "policyKey", "id", "type")
		}
	}
	   
	test("GatlingContextConfiguration needs a non-empty asynchronous change time") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "2", "", "domain.org", "login", "pass", "policyKey", "id", "type")
		}
	}
	   
	test("GatlingContextConfiguration needs a parsable number for  asynchronous change time") {
		intercept[NumberFormatException] {
			GatlingConfiguration.build("192.168.0.1", "2", "a5", "domain.org", "login", "pass", "policyKey", "id", "type")
		}
	}
	
	test("GatlingContextConfiguration needs a non-null domain") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "2", "5", null, "login", "pass", "policyKey", "id", "type")
		}
	}
	   
	test("GatlingContextConfiguration needs a non-empty domain") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "2", "5", "", "login", "pass", "policyKey", "id", "type")
		}
	}
	
	test("GatlingContextConfiguration needs a non-null login") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "2", "5", "domain.org", null, "pass", "policyKey", "id", "type")
		}
	}
	   
	test("GatlingContextConfiguration needs a non-empty login") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "2", "5", "domain.org", "", "pass", "policyKey", "id", "type")
		}
	}
	
	test("GatlingContextConfiguration needs a non-null password") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "2", "5", "domain.org", "login", null, "policyKey", "id", "type")
		}
	}
	   
	test("GatlingContextConfiguration needs a non-empty password") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "2", "5", "domain.org", "login", "", "policyKey", "id", "type")
		}
	}
	
	test("GatlingContextConfiguration needs a non-null deviceId") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "2", "5", "domain.org", "login", "pass", "policyKey", null, "type")
		}
	}
	   
	test("GatlingContextConfiguration needs a non-empty deviceId") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "2", "5", "domain.org", "login", "pass", "policyKey", "", "type")
		}
	}
	
	test("GatlingContextConfiguration needs a non-null deviceType") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "2", "5", "domain.org", "login", "pass", "policyKey", "id", null)
		}
	}
	   
	test("GatlingContextConfiguration needs a non-empty deviceType") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "2", "5", "domain.org", "login", "pass", "policyKey", "id", "")
		}
	}
	
	test("GatlingContextConfiguration needs a non-null policyKey") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "2", "5", "domain.org", "login", "pass", null, "id", "type")
		}
	}
	   
	test("GatlingContextConfiguration needs a non-empty policyKey") {
		intercept[IllegalArgumentException] {
			GatlingConfiguration.build("192.168.0.1", "2", "5", "domain.org", "login", "pass", "", "id", "type")
		}
	}
}
