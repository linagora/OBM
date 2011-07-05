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

package fr.aliasource.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is responsible of loading an ini file.
 * 
 * @author tom
 * 
 */
public abstract class IniFile {

	private Map<String, String> settings;
	private Log logger;

	public IniFile(String path) {
		logger = LogFactory.getLog(getClass());
		settings = new HashMap<String, String>();
		File f = new File(path);
		if (f.exists()) {
			loadIniFile(f);
		} else {
			logger.warn(path+ " does not exist.");
		}
	}

	protected String getSetting(String settingName) {
		return settings.get(settingName);
	}
	
	public Map<String, String> getData() {
		return settings;
	}

	public abstract String getCategory();
	
	private void loadIniFile(File f) {
		FileInputStream in = null;
		try {
			Properties p = new Properties();
			in = new FileInputStream(f);
			p.load(in);
			for (Object key : p.keySet()) {
				settings.put((String) key, p.getProperty((String) key));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
