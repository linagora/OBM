package org.obm.sync;

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

	private static final long serialVersionUID = -7974272976534935697L;

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
			super();
		}

		public static String getAlgorithm() {
			return "XTrust509";
		}

		@Override
		protected void engineInit(KeyStore keystore) throws KeyStoreException {
			//do nothing on init
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

				@Override
				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {
					//check nothing
				}

				@Override
				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {
					//check nothing
				}
			} };
		}
	}
}
