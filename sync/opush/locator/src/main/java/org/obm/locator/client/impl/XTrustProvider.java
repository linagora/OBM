/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.locator.client.impl;

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivilegedAction;
import java.security.Security;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

public final class XTrustProvider extends java.security.Provider {

	private static final long serialVersionUID = -818891961979231358L;
	
	
	private final static String NAME = "XTrustJSSE";
	private final static String INFO = "XTrust JSSE Provider (implements trust factory with truststore validation disabled)";
	private final static double VERSION = 1.0D;

	public XTrustProvider() {
		super(NAME, VERSION, INFO);

		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				put("TrustManagerFactory."
						+ TrustManagerFactoryImpl.getAlgorithm(),
						TrustManagerFactoryImpl.class.getName());
				return null;
			}
		});
	}

	public static void install() {
		if (Security.getProvider(NAME) == null) {
			Security.insertProviderAt(new XTrustProvider(), 2);
			Security.setProperty("ssl.TrustManagerFactory.algorithm",
					TrustManagerFactoryImpl.getAlgorithm());
		}
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
	}

	public final static class TrustManagerFactoryImpl extends
			TrustManagerFactorySpi {
		public TrustManagerFactoryImpl() {
		}

		public static String getAlgorithm() {
			return "XTrust509";
		}

		protected void engineInit(KeyStore keystore) throws KeyStoreException {
		}

		protected void engineInit(ManagerFactoryParameters mgrparams)
				throws InvalidAlgorithmParameterException {
			throw new InvalidAlgorithmParameterException(XTrustProvider.NAME
					+ " does not use ManagerFactoryParameters");
		}

		protected TrustManager[] engineGetTrustManagers() {
			return new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {
				}
			} };
		}
	}
}
