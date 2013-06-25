package org.obm.provisioning;

import static org.fest.assertions.api.Assertions.assertThat;

import org.apache.directory.api.ldap.model.name.Dn;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.opends.messages.Message;
import org.opends.server.backends.MemoryBackend;
import org.opends.server.core.AddOperation;
import org.opends.server.core.DirectoryServer;
import org.opends.server.protocols.internal.InternalClientConnection;
import org.opends.server.types.DN;
import org.opends.server.types.Entry;
import org.opends.server.types.LDIFImportConfig;
import org.opends.server.types.ResultCode;
import org.opends.server.util.LDIFReader;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;

@GuiceModule(EmbeddedLdapModule.class)
@RunWith(SlowGuiceRunner.class)
public class ConnectionImplTest {
	
	@Inject DirectoryServer directoryServer;
	@Inject Provider<LdapGroupImpl.Builder> builderProvider;
	
	@Before
	public void setup() throws Exception {
		directoryServer.startServer();
		injectBootstrapData();
	}
	
	private void injectBootstrapData()throws Exception  {
		initializeBackend();
		loadLdifBootstrap();
	}

	private void initializeBackend() throws Exception {
		MemoryBackend memoryBackend = new MemoryBackend();
		memoryBackend.setBackendID("test");
		memoryBackend.setBaseDNs(new DN[] {DN.decode("dc=local")});
		memoryBackend.initializeBackend();
		DirectoryServer.registerBackend(memoryBackend);
	}

	private void loadLdifBootstrap() throws Exception {
		LDIFImportConfig ldifImportConfig = new LDIFImportConfig(ClassLoader.getSystemClassLoader().getResource("bootstrap.ldif").getPath());
        LDIFReader reader = new LDIFReader(ldifImportConfig);
        Entry entry;
        while ((entry = reader.readEntry()) != null) {
        	addEntry(entry);
        }
	}
	
	private void addEntry(Entry entry) {
		InternalClientConnection conn = InternalClientConnection.getRootConnection();
		AddOperation addOperation = conn.processAdd(entry.getDN(), entry.getObjectClasses(), entry.getUserAttributes(),
				entry.getOperationalAttributes());
		
		Preconditions.checkState(addOperation.getResultCode() == ResultCode.SUCCESS, "Unable to add bootstrap entry " + entry.getDN());
	}

	@After
	public void tearDown() {
		DirectoryServer.shutDown(ConnectionImplTest.class.toString(), Message.EMPTY);
	}
	
	@Test
	public void testCreateGroup() throws Exception {
		Configuration configuration = new StaticConfiguration();
		
		ConnectionImpl connection = new ConnectionImpl(configuration);
		LdapGroupImpl ldapGroup = builderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn("group1")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(ldapGroup);
		
		LdapGroup.Id groupId = ldapGroup.getId();
		
		assertThat(connection.getGroupDnFromGroupId(groupId).equals(new Dn("cn=group1,ou=groups,dc=test.obm.org,dc=local"))).isTrue();
		//TODO check all attributes
	}

}
