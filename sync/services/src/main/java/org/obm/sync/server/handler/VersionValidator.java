package org.obm.sync.server.handler;

import java.util.StringTokenizer;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ConnectorVersion;
import org.obm.sync.auth.OBMConnectorVersionException;

import com.google.inject.Singleton;

@Singleton
public class VersionValidator {

	private ConnectorVersion obmConnectorRequiredVersion;

	protected VersionValidator() {
		this.obmConnectorRequiredVersion = new ConnectorVersion(2, 4, 1, 8);
	}

	// thunderbird[ext: 2.4.1.8-rc11, light: 1.0b2]
	public void checkObmConnectorVersion(AccessToken token)
			throws OBMConnectorVersionException {
		if (token.getOrigin() != null
				&& token.getOrigin().startsWith("thunderbird")) {
			ConnectorVersion actualVersion = parseConnectorVersionString(token
					.getOrigin());
			if (actualVersion.compareTo(obmConnectorRequiredVersion) < 0) {
				throw new OBMConnectorVersionException(token,
						obmConnectorRequiredVersion.getMajor(),
						obmConnectorRequiredVersion.getMinor(),
						obmConnectorRequiredVersion.getRelease(),
						obmConnectorRequiredVersion.getSubRelease());
			}
		}
	}

	// 2.4.1.8-rc11
	private ConnectorVersion parseConnectorVersionString(String origin) {
		String extVertion = getExtVersion(origin);
		if (extVertion != null) {
			StringTokenizer token = new StringTokenizer(extVertion, ".");
			if (token.hasMoreTokens()) {
				try {
					Integer major = getNext(token);
					Integer minor = getNext(token);
					Integer release = getNext(token);
					Integer subRelease = 0;
					// 8-rc11
					String tokenSubRelease = getNextString(token);
					token = new StringTokenizer(tokenSubRelease, "-");
					if (token.hasMoreTokens()) {
						subRelease = getNext(token);
					} else {
						subRelease = getInteger(tokenSubRelease);
					}
					return new ConnectorVersion(major, minor, release,
							subRelease);

				} catch (NumberFormatException e) {
				}
			}
		}
		return new ConnectorVersion(0, 0, 0, 0);
	}

	private Integer getNext(StringTokenizer token) {
		return getInteger(getNextString(token));
	}

	private Integer getInteger(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private String getNextString(StringTokenizer token) {
		if (token.hasMoreTokens()) {
			return token.nextToken();
		}
		return "0";
	}

	// thunderbird[ext: 2.4.1.8-rc11, light: 1.0b2]
	private String getExtVersion(String origin) {
		int indStart = origin.indexOf("thunderbird[ext:");
		if (indStart >= 0) {
			int indEnd = origin.indexOf(",", indStart);
			if (indEnd >= 0) {
				return origin.substring(indStart + "thunderbird[ext:".length(),
						indEnd).trim();
			}
		}
		return null;
	}
}
