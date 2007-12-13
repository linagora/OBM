/**
 * 
 */
package fr.aliasource.funambol.engine.source;

import java.io.Serializable;
import java.security.Principal;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.source.AbstractSyncSource;
import com.funambol.framework.engine.source.SyncContext;
import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.engine.source.SyncSourceInfo;
import com.funambol.framework.security.Sync4jPrincipal;
import com.funambol.framework.server.Sync4jDevice;
import com.funambol.framework.server.store.PersistentStore;
import com.funambol.framework.server.store.PersistentStoreException;
import com.funambol.framework.tools.beans.LazyInitBean;
import com.funambol.server.config.Configuration;

/**
 */
public abstract class ObmSyncSource extends AbstractSyncSource implements
		SyncSource, Serializable, LazyInitBean {

	protected Principal principal = null;

	protected Sync4jDevice device = null;
	protected String deviceTimezoneDescr = null;
	protected TimeZone deviceTimezone = null;
	protected String deviceCharset = null;

	public static final String MSG_TYPE_VCARD = "text/x-vcard";
	public static final String MSG_TYPE_ICAL = "text/x-vcalendar";

	private boolean encode = true;

	private int restrictions = 1; // default private
	private String obmAddress = null;

	// protected FunambolLogger logger =
	// FunambolLoggerFactory.getLogger("funambol");
	private Log logger = LogFactory.getLog(getClass());

	// ------------------------------------------------------------ Constructors

	/** Creates a new instance of AbstractSyncSource */
	public ObmSyncSource() {
		logger.info("obmSyncSource ctor");
	}

	// ---------------------------------------------------------- Public methods
	public void init() {
		logger.info("init");
	}

	/**
	 * Equivalent of deprecated method
	 * 
	 * @return syncsource type
	 */
	public String getSourceType() {
		if (getInfo() != null && getInfo().getPreferredType() != null) {
			return getInfo().getSupportedTypes()[0].getType();
		} else {
			return "";
		}
	}

	/**
	 * Equivalent of deprecated method
	 * 
	 * @param type
	 * @param version
	 */
	/*
	 * public void setSourceType(String type, String version) { ContentType[]
	 * contents = new ContentType[1]; ContentType content = new
	 * ContentType(type,version); contents[0] = content; setInfo(new
	 * SyncSourceInfo(contents, 0)); }
	 */

	public boolean isEncode() {
		return encode;
	}

	public void setEncode(boolean encode) {
		this.encode = encode;
	}

	/**
	 * Returns a string representation of this object.
	 * 
	 * @return a string representation of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString());

		sb.append(" - {name: ").append(getName());
		sb.append(" type: ").append(getSourceType());
		sb.append(" uri: ").append(getSourceURI());
		sb.append("}");
		return sb.toString();
	}

	/**
	 * SyncSource's beginSync()
	 * 
	 * @param context
	 *            the context of the sync
	 */
	public void beginSync(SyncContext context) throws SyncSourceException {
		super.beginSync(context);

		this.principal = context.getPrincipal();

		String deviceId = null;
		deviceId = ((Sync4jPrincipal) context.getPrincipal()).getDeviceId();
		try {
			device = getDevice(deviceId);
			String timezone = device.getTimeZone();
			if (device.getConvertDate()) {
				if (timezone != null && timezone.length() > 0) {
					deviceTimezoneDescr = timezone;
					deviceTimezone = TimeZone.getTimeZone(deviceTimezoneDescr);
				}
			}

			deviceCharset = device.getCharset();
			logger.info("device charset : " + deviceCharset);
		} catch (PersistentStoreException e1) {
			logger.error("obm : error getting device");
		}

	}

	/**
	 * @see SyncSource
	 */
	public void setOperationStatus(String operation, int statusCode,
			SyncItemKey[] keys) {

		StringBuffer message = new StringBuffer("Received status code '");
		message.append(statusCode).append("' for a '").append(operation)
				.append("'").append(" for this items: ");

		for (int i = 0; i < keys.length; i++) {
			message.append("\n  - " + keys[i].getKeyAsString());
		}

		logger.info(message.toString());
	}

	public SyncItemKey[] getSyncItemKeysFromKeys(String[] keys) {
		int nb = 0;
		if (keys != null) {
			nb = keys.length;
		}
		SyncItemKey[] syncKeys = new SyncItemKey[nb];
		for (int i = 0; i < nb; i++) {
			syncKeys[i] = new SyncItemKey(keys[i]);
		}

		return syncKeys;
	}

	/**
	 * Return the device with the given deviceId
	 * 
	 * @param deviceId
	 *            String
	 * @return Sync4jDevice
	 * @throws PersistentStoreException
	 */
	private Sync4jDevice getDevice(String deviceId)
			throws PersistentStoreException {
		Sync4jDevice device = new Sync4jDevice(deviceId);
		PersistentStore store = Configuration.getConfiguration().getStore();
		store.read(device);
		return device;
	}

	public int getRestrictions() {
		if (logger.isTraceEnabled()) {
			logger.trace(" getRestrcitions:" + restrictions);
		}
		return restrictions;
	}

	public void setRestrictions(int restrictions) {
		if (logger.isTraceEnabled()) {
			logger.trace(" setRestrcitions:" + restrictions);
		}
		this.restrictions = restrictions;
	}

	public String getObmAddress() {
		return obmAddress;
	}

	public void setObmAddress(String obmAddress) {
		this.obmAddress = obmAddress;
	}

	@Override
	public void commitSync() throws SyncSourceException {
		super.commitSync();
		logger.info("commit sync");
	}

	@Override
	public void endSync() throws SyncSourceException {
		super.endSync();
		logger.info("end sync");
	}

	@Override
	public SyncSourceInfo getInfo() {
		SyncSourceInfo info = super.getInfo();
		logger.info("getinfo: " + info);
		return info;
	}

	@Override
	public String getName() {
		String name = super.getName();
		logger.info("getName: " + name);
		return name;
	}

	@Override
	public String getSourceQuery() {
		String ret = super.getSourceQuery();
		logger.info("getsourcequery: " + ret);
		return ret;
	}

	@Override
	public String getSourceURI() {
		String ret = super.getSourceURI();
		logger.info("getsourceuri: " + ret);
		return ret;
	}

}
