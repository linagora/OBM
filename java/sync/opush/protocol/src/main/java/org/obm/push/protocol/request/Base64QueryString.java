/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.protocol.request;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.obm.push.bean.DeviceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

public class Base64QueryString extends AbstractActiveSyncRequest {

	/**
	 * <code>
	Size         Field              Description
	1 byte       Protocol version   An integer that specifies the version of the
	                                ActiveSync protocol that is being used. This value
	                                MUST be 121.
	1 byte       Command code       An integer that specifies the command (see table
	                                of command codes in section 2.2.1.1.1.2).
	2 bytes      Locale             An integer that specifies the locale of the
	                                language that is used for the response.
	1 byte       Device ID length   An integer that specifies the length of the device
	                                ID. A value of 0 indicates that the device ID field
	                                is absent.
	0 - 16 bytes Device ID          A string or a GUID that identifies the device. A
	                                Windows Mobile device will use a GUID.
	1 byte       Policy key length  An integer that specifies the length of the policy
	                                key. The only valid values are 0 or 4. A value of 0
	                                indicates that the policy key field is absent.
	0 or 4 bytes Policy key         An integer that indicates the state of policy
	                                settings on the client device.
	1 byte       Device type length An integer that specifies the length of the device
	                                type value.
	0 - 16 bytes Device type        A string that specifies the type of client device.
	                                For details, see section 2.2.1.1.1.3.
	Variable     Command parameters A set of parameters that varies depending on the
	                                command. Each parameter consists of a tag,
	                                </code>
	 */

	private static final Logger logger = LoggerFactory
			.getLogger(Base64QueryString.class);
	
	private final InputStream stream;
	
	private final byte[] data;
	private final String protocolVersion;
	private final String deviceType;
	private DeviceId deviceId;
	private final int cmdCode;
	private int policyKey;
	
	private String attachmentName;
	private String collectionId;
	private String collectionName;
	private String itemId;
	private String longId;
	private String parentId;
	private String occurrence;
	private String saveInSent;
	private String acceptMultiPart;
	

	public Base64QueryString(HttpServletRequest request, InputStream stream) {
		super(request);
		this.stream = stream;
		this.data = Base64.decodeBase64(request.getQueryString());
		int i = 0;
		protocolVersion = "" + (data[i++] / 10.0); // i==0
		cmdCode = data[i++]; // 1
		
		int locale = (data[i++] << 8) + data[i++]; // i==2 and i==3

		// windows mobile 6.5 use a GUID instead of a string, so we cannot
		// create a string from those bytes directly
		if (data[i] > 0) {
			byte[] devId = new byte[data[i]];
			System.arraycopy(data, i + 1, devId, 0, data[i]); // i==4
			i += data[i] + 1; // i is now on policy key size
			deviceId = new DeviceId(new String(Base64.encodeBase64(devId), Charsets.UTF_8));
		}

		policyKey = 0;
		if (data[i++] == 4) { // got a policy key
			policyKey = policyKey + (data[i++] << 24) + (data[i++] << 16)
					+ (data[i++] << 8) + (data[i++]);
		}
		String type = new String(data, i + 1, data[i], Charsets.UTF_8);
		deviceType = type;
		i += data[i] + 1;

		String decodedData = 	"<protocolVersion>" + protocolVersion + "</protocolVersion>" +
								"<command>" + Base64CommandCodes.getCmd(cmdCode) + "</command>" +
								"<localeInt>" + locale + "</localeInt>"+
								"<deviceId>" + deviceId + "</deviceId>" +
								"<policyKey>" + policyKey + "</policyKey>" +
								"<deviceType>" + deviceType + "</deviceType>";
		logRequestInfo(data, decodedData);

		while(data.length>i){
			i = decodeParameters(i);
		}
		// TODO variable parts
		
		
	}

	@SuppressWarnings("unused")
	private void logRequestInfo(byte[] data, String decodedData){

		/*
		 * This code used to generate technical log using technical-log project 
		 * 
		 * Marker asRequestMarker = TechnicalLogType.ACTIVE_SYNC_REQUEST_INFO.getMarker();
		String dataHexadecimalString = StringUtils.getHexadecimalStringRepresentation(data);
		
		String xmlLog = "<rawRequest>"+dataHexadecimalString+"</rawRequest>";
		xmlLog += "<decodedRequestParameters>"+decodedData+"</decodedRequestParameters>";

		logger.info(asRequestMarker, xmlLog);
		 */
	}

	private int decodeParameters(int i) {
		Base64ParameterCodes tag = Base64ParameterCodes.getParam(data[i++]);
		byte length = data[i++];
		byte[] value = new byte[length];
		for(int j = 0; j<length;j++){
			value[j] = data[i++]; 
		}
		switch (tag) {
		case AttachmentName:
			this.attachmentName = new String(value, Charsets.UTF_8);
			break;
		case CollectionId:
			this.collectionId = new String(value, Charsets.UTF_8);
			break;
		case CollectionName:
			this.collectionName = new String(value, Charsets.UTF_8);
			break;
		case ItemId:
			this.itemId = new String(value, Charsets.UTF_8);
			break;
		case LongId:
			this.longId = new String(value, Charsets.UTF_8);
			break;
		case ParentId:
			this.parentId = new String(value, Charsets.UTF_8);
			break;
		case Occurrence:
			this.occurrence = new String(value, Charsets.UTF_8);
			break;
		case Options:
			if (value.length > 0) {
				if (value[0] == 0x01) {
					this.saveInSent = "T";
				} else if (value[0] == 0x02) {
					this.acceptMultiPart = "T";
				}
			}
			break;
		case User:
			break;
		default:
			break;
		}
		return i;
	}
	
	@Override
	public String getParameter(String key) {

		if (key.equalsIgnoreCase("MS-ASProtocolVersion")) {
			return protocolVersion;
		}
		if (key.equalsIgnoreCase("DeviceType")) {
			return deviceType;
		}
		if (key.equalsIgnoreCase("Cmd")) {
			return Base64CommandCodes.getCmd(cmdCode);
		}
		if (key.equalsIgnoreCase("DeviceId")) {
			return deviceId.getDeviceId();
		}
		if (key.equalsIgnoreCase("AttachmentName")) {
			return attachmentName;
		}
		if (key.equalsIgnoreCase("CollectionId")) {
			return collectionId;
		}
		if (key.equalsIgnoreCase("CollectionName")) {
			return collectionName;
		}
		if (key.equalsIgnoreCase("ItemId")) {
			return itemId;
		}
		if (key.equalsIgnoreCase("LongId")) {
			return longId;
		}
		if (key.equalsIgnoreCase("ParentId")) {
			return parentId;
		}
		if (key.equalsIgnoreCase("Occurrence")) {
			return occurrence;
		}
		if (key.equalsIgnoreCase("SaveInSent")) {
			return saveInSent;
		}
		if (key.equalsIgnoreCase("AcceptMultiPart")) {
			return acceptMultiPart;
		}

		logger.warn("cannot fetch '" + key + "' in b64 query string");

		return null;
	}

	@Override
	public InputStream getInputStream() {
		return stream;
	}
	
	@Override
	public String getHeader(String name) {
		return request.getHeader(name);
	}

	@Override
	public HttpServletRequest getHttpServletRequest() {
		return this.request;
	}

	@Override
	public DeviceId getDeviceId() {
		return deviceId;
	}

	@Override
	public String getDeviceType() {
		return deviceType;
	}

	@Override
	public String getMsPolicyKey() {
		return String.valueOf(policyKey);
	}

	@Override
	public String getMSASProtocolVersion() {
		return protocolVersion;
	}

	@Override
	public String getCommand() {
		return Base64CommandCodes.getCmd(cmdCode);
	}
}
