package org.obm.provisioning;

public class LdapServiceImpl implements LdapService {

	@Override
	public Connection create(Configuration configuration) {
		return new ConnectionImpl(configuration);
	}

}
