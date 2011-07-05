/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Ristretto Mail API.
 *
 * The Initial Developers of the Original Code are
 * Timo Stich and Frederik Dietz.
 * Portions created by the Initial Developers are Copyright (C) 2004
 * All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.columba.ristretto.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Factory to create a new SSL socket or on top of an existing plain socket.
 * 
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */

public class RistrettoSSLSocketFactory {

	private static RistrettoSSLSocketFactory myInstance;

	private SSLSocketFactory socketFactory;

	/**
	 * Gets the instance of the RistrettoSSLSocketFactory.
	 * 
	 * @return the singleton instance of the factory.
	 */
	public static RistrettoSSLSocketFactory getInstance() {
		if (myInstance == null) {
			myInstance = new RistrettoSSLSocketFactory();
		}

		return myInstance;
	}

	protected RistrettoSSLSocketFactory() {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");

			sslContext.init(null,
					new TrustManager[] { new DefaultTrustManager() },
					new java.security.SecureRandom());

			socketFactory = sslContext.getSocketFactory();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace(System.out);
		} catch (KeyManagementException e) {
			e.printStackTrace(System.out);
		}
	}

	/**
	 * Set the TrustManager of the used SSLContext.
	 * 
	 * @param tm
	 *            the Trustmanager used by the SSLContext.
	 */
	public void setTrustManager(TrustManager tm) {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");

			sslContext.init(null, new TrustManager[] { tm },
					new java.security.SecureRandom());

			socketFactory = sslContext.getSocketFactory();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace(System.out);
		} catch (KeyManagementException e) {
			e.printStackTrace(System.out);
		}
	}

	/**
	 * Set the KeyManager of the SSLContext.
	 * 
	 * @param km
	 *            the KeyManager used by the SSLContext
	 */
	public void setKeyManager(KeyManager km) {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");

			sslContext.init(new KeyManager[] { km }, null,
					new java.security.SecureRandom());

			socketFactory = sslContext.getSocketFactory();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace(System.out);
		} catch (KeyManagementException e) {
			e.printStackTrace(System.out);
		}
	}

	/**
	 * Creates a new SSL Socket connected to the specified address and port.
	 * 
	 * @param address
	 *            the address to connect to
	 * @param port
	 *            the port to connect to
	 * @return a new SSL Socket
	 * @throws IOException
	 */
	public Socket createSocket(InetAddress address, int port)
			throws IOException {
		return socketFactory.createSocket(address, port);
	}

	/**
	 * Creates a new SSL Socket connected to the specified address and port.
	 * 
	 * @param address
	 *            the address to connect to
	 * @param port
	 *            the port to connect to
	 * @param localAddress
	 *            the local InetAddress
	 * @param localPort
	 *            the local port
	 * @return a new SSL Socket
	 * @throws IOException
	 */
	public Socket createSocket(InetAddress address, int port,
			InetAddress localAddress, int localPort) throws IOException {
		return socketFactory.createSocket(address, port, localAddress,
				localPort);
	}

	/**
	 * Creates a new SSL Socket connected to the specified name address and
	 * port.
	 * 
	 * @param host
	 *            the name address to connect to
	 * @param port
	 *            the port to connect to
	 * @return a new SSL Socket
	 * @throws IOException
	 */
	public Socket createSocket(String host, int port) throws IOException {
		return socketFactory.createSocket(host, port);
	}

	/**
	 * Creates a new SSL Socket connected to the specified name address and
	 * port.
	 * 
	 * @param host
	 *            the name address to connect to
	 * @param port
	 *            the port to connect to
	 * @param localHost
	 *            the local InetAddress
	 * @param localPort
	 *            the local port
	 * @return a new SSL Socket
	 * @throws IOException
	 */
	public Socket createSocket(String host, int port, InetAddress localHost,
			int localPort) throws IOException {
		return socketFactory.createSocket(host, port, localHost, localPort);
	}

	/**
	 * Creates a SSL Socket on top of the given Socket.
	 * 
	 * @param socket
	 *            plain socket on which the SSL Socket is built
	 * @param host
	 *            the local port
	 * @param port
	 *            the port to connect to
	 * @param autoClose
	 *            shall the socket be closed when the SSL socket is closed?
	 * @return a new SSL Socket
	 * @throws IOException
	 */
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException {
		return socketFactory.createSocket(socket, host, port, autoClose);
	}
}
