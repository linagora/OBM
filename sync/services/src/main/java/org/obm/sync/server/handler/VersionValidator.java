package org.obm.sync.server.handler;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ClientInformations;
import org.obm.sync.auth.LightningVersion;
import org.obm.sync.auth.OBMConnectorVersionException;
import org.obm.sync.auth.Version;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class VersionValidator {

	private Version obmConnectorRequiredVersion;
	private int linagoraVersion; 
	private ClientInformations.Parser parser;

	@Inject
	protected VersionValidator(ClientInformations.Parser parser) {
		this.obmConnectorRequiredVersion = new Version(2, 4, 1, 8);
		this.linagoraVersion = 2;
		this.parser = parser;
	}

	public void checkObmConnectorVersion(AccessToken token)
			throws OBMConnectorVersionException {
		String origin = token.getOrigin();
		if (origin != null) {
			ClientInformations actualVersion = parser.parse(origin);
			if (actualVersion != null) {
				Version connectorVersion = actualVersion.getObmConnectorVersion();
				if (connectorVersion == null || connectorVersion.compareTo(obmConnectorRequiredVersion) < 0) {
					throw new OBMConnectorVersionException(token, obmConnectorRequiredVersion);
				}
				LightningVersion lightningVersion = actualVersion.getLightningVersion();
				if (lightningVersion == null) {
					throw new OBMConnectorVersionException(token, obmConnectorRequiredVersion);
				}
				Integer linagoraVersion = lightningVersion.getLinagoraVersion();
				if (linagoraVersion == null || linagoraVersion < this.linagoraVersion) {
					throw new OBMConnectorVersionException(token, obmConnectorRequiredVersion);
				}
				return;
			}
		}
		throw new OBMConnectorVersionException(token, obmConnectorRequiredVersion);
	}

}
