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

package org.columba.ristretto.auth.mechanism;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

/**
 * Wraps the Sasl classes. This is necessary to compensate for the package
 * change between Java 1.4 and 1.5 .
 * 
 * @author timo
 * 
 */
public class SaslWrapper {

	Object saslClientInstance;

	private Class<?> saslClient;

	private Class<?> sasl;

	public SaslWrapper() throws Exception {
		saslClient = Class.forName("javax.security.sasl.SaslClient");
		sasl = Class.forName("javax.security.sasl.Sasl");

	}

	public static boolean available() {
		return true;
	}

	public void createClient(String[] type, String user, String service,
			String hostname, Map<String, ?> map, CallbackHandler handler)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, SecurityException, NoSuchMethodException {
		Method createSaslClient = sasl.getMethod("createSaslClient",
				new Class[] { String[].class, String.class, String.class,
						String.class, Map.class, CallbackHandler.class });

		saslClientInstance = createSaslClient.invoke(null, new Object[] { type,
				user, service, hostname, map, handler });
	}

	public byte[] evaluateChallenge(byte[] challenge) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		Method evaluateChallenge = saslClient.getMethod("evaluateChallenge",
				new Class[] { byte[].class });

		Object result = evaluateChallenge.invoke(saslClientInstance,
				new Object[] { challenge });

		return (byte[]) result;
	}

	public boolean isComplete() throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		Method evaluateChallenge = saslClient.getMethod("isComplete",
				new Class[] {});

		Boolean result = (Boolean) evaluateChallenge.invoke(saslClientInstance,
				new Object[0]);

		return result.booleanValue();
	}

	public boolean handle(Callback callback) {
		try {
			Class<?> realmCallback = Class
					.forName("javax.security.sasl.RealmCallback");

			if (realmCallback.equals(callback.getClass())) {
				Method setText = realmCallback.getMethod("setText",
						new Class[] { String.class });
				Method getDefaultText = realmCallback.getMethod(
						"getDefaultText", new Class[0]);

				setText.invoke(callback, new Object[] { getDefaultText.invoke(
						callback, new Object[0]) });
				return true;
			}

			Class<?> realmChoiceCallback = Class
					.forName("javax.security.sasl.RealmChoiceCallback");

			if (realmChoiceCallback.equals(callback.getClass())) {
				Method setSelectedIndex = realmChoiceCallback.getMethod(
						"setSelectedIndex", new Class[] { int.class });
				Method getDefaultChoice = realmChoiceCallback.getMethod(
						"getDefaultChoice", new Class[0]);

				setSelectedIndex.invoke(callback,
						new Object[] { getDefaultChoice.invoke(callback,
								new Object[0]) });
				return true;
			}
		} catch (Exception e) {
			return false;
		}

		return false;
	}

}
