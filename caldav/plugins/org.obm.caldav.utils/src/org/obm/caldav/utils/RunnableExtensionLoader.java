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

package org.obm.caldav.utils;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;


/**
 * Loads plugins based on some interface
 * 
 * @author tom
 * 
 * @param <T>
 */
public class RunnableExtensionLoader<T> {

	private Log logger;

	public RunnableExtensionLoader() {
		logger = LogFactory.getLog(getClass());
	}

	/**
	 * Loads plugins declaring an executable extension
	 * 
	 * @param pluginId
	 * @param pointName
	 * @param element
	 * @param attribute
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<T> loadExtensions(String pluginId, String pointName,
			String element, String attribute) {

		List<T> factories = new LinkedList<T>();
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(pluginId, pointName);
		if (point == null) {
			logger.error("point " + pluginId + "." + pointName + " [" + element
					+ " " + attribute + "=XXX] not found.");
			return factories;
		}
		IExtension[] extensions = point.getExtensions();

		for (IExtension ie : extensions) {
			for (IConfigurationElement e : ie.getConfigurationElements()) {
				if (element.equals(e.getName())) {
					try {
						T factory = (T) e.createExecutableExtension(attribute);
						factories.add(factory);
						logger.info(factory.getClass().getSimpleName()
								+ " loaded.");
					} catch (CoreException ce) {
						ce.printStackTrace();
					}

				}
			}
		}
		logger.info("Loaded " + factories.size() + " implementors of "
				+ pluginId + "." + pointName);
		return factories;
	}

}
