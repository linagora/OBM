/* ***** BEGIN LICENSE BLOCK **import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.Credentials;
e user interfaces of the “OBM, Free
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
package fr.aliacom.obm.ldap;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.Credentials;

@RunWith(SlowFilterRunner.class)
public class LDAPAuthServiceTest {

	private LDAPAuthService service;
	private LDAPUtilsFactory ldapUtilsFactory;

	private final IMocksControl mocksControl = createControl();
	private final LDAPDirectory directory = new LDAPDirectory("uri", "u=%u,d=%d", null, null, "dc=local", null, null);
	private final Credentials credentials = Credentials
			.builder()
			.login("login")
			.password("password")
			.hashedPassword(false)
			.domain("domain")
			.build();
	private final Credentials credentialsWithHashedPassword = Credentials
			.builder()
			.login("login")
			.password("letsSayThisIsAHash")
			.hashedPassword(true)
			.domain("domain")
			.build();

	@Before
	public void setUp() {
		ldapUtilsFactory = mocksControl.createMock(LDAPUtilsFactory.class);
		service = new LDAPAuthService(directory, ldapUtilsFactory);
	}

	@After
	public void tearDown() {
		mocksControl.verify();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDoAuthWithHashedPassword() throws Exception {
		mocksControl.replay();

		service.doAuth(credentialsWithHashedPassword);
	}

	@Test(expected = RuntimeException.class)
	public void testDoAuthWhenSearchFails() throws Exception {
		LDAPUtils utils = expectLDAPUtils(null, null);

		expect(utils.getConnection()).andThrow(new NamingException());
		mocksControl.replay();

		service.doAuth(credentials);
	}

	@Test(expected = AuthFault.class)
	public void testDoAuthWhenSearchReturnsNoResult() throws Exception {
		LDAPUtils utils = expectLDAPUtils(null, null);
		DirContext context = mocksControl.createMock(DirContext.class);

		expect(utils.getConnection()).andReturn(context);
		expect(utils.findResultByFilter("u=login,d=domain", context)).andReturn(null);

		context.close();
		expectLastCall();

		mocksControl.replay();

		service.doAuth(credentials);
	}

	@Test(expected = RuntimeException.class)
	public void testDoAuthWhenBindFails() throws Exception {
		LDAPUtils utils = expectLDAPUtils(null, null);
		LDAPUtils bindUtils = expectLDAPUtils("uid=login,dc=local", "password");
		DirContext context = mocksControl.createMock(DirContext.class);
		SearchResult result = new SearchResult("uid=login", null, null);

		expect(utils.getConnection()).andReturn(context);
		expect(bindUtils.getConnection()).andThrow(new NamingException());
		expect(utils.findResultByFilter("u=login,d=domain", context)).andReturn(result);

		context.close();
		expectLastCall();

		mocksControl.replay();

		service.doAuth(credentials);
	}

	@Test(expected = AuthFault.class)
	public void testDoAuthWhenBindCannotAuthenticate() throws Exception {
		LDAPUtils utils = expectLDAPUtils(null, null);
		LDAPUtils bindUtils = expectLDAPUtils("uid=login,dc=local", "password");
		DirContext context = mocksControl.createMock(DirContext.class);
		SearchResult result = new SearchResult("uid=login", null, null);

		expect(utils.getConnection()).andReturn(context);
		expect(bindUtils.getConnection()).andThrow(new AuthenticationException());
		expect(utils.findResultByFilter("u=login,d=domain", context)).andReturn(result);

		context.close();
		expectLastCall();

		mocksControl.replay();

		service.doAuth(credentials);
	}

	@Test
	public void testDoAuth() throws Exception {
		LDAPUtils utils = expectLDAPUtils(null, null);
		LDAPUtils bindUtils = expectLDAPUtils("uid=login,dc=local", "password");
		DirContext context = mocksControl.createMock(DirContext.class);
		SearchResult result = new SearchResult("uid=login", null, null);

		expect(utils.getConnection()).andReturn(context);
		expect(bindUtils.getConnection()).andReturn(context);
		expect(utils.findResultByFilter("u=login,d=domain", context)).andReturn(result);

		context.close();
		expectLastCall().times(2);

		mocksControl.replay();

		service.doAuth(credentials);
	}

	private LDAPUtils expectLDAPUtils(String dn, String password) {
		LDAPUtils utils = mocksControl.createMock(LDAPUtils.class);

		expect(ldapUtilsFactory.create("uri", dn, password, "dc=local")).andReturn(utils);

		return utils;
	}

}
