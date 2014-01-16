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
package org.obm.push.wbxml;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagsTables {

	private static final Logger logger = LoggerFactory
			.getLogger(TagsTables.class);

	/**
	 * AirSync
	 */
	public static final String[] CP_0 = {
	// AirSync
			"Sync", // 0x05
			"Responses", // 0x06
			"Add", // 0x07
			"Change", // 0x08
			"Delete", // 0x09
			"Fetch", // 0x0A
			"SyncKey", // 0x0B
			"ClientId", // 0x0C
			"ServerId", // 0x0D
			"Status", // 0x0E
			"Collection", // 0x0F
			"Class", // 0x10
			"Version", // 0x11
			"CollectionId", // 0x12
			"GetChanges", // 0x13
			"MoreAvailable", // 0x14
			"WindowSize", // 0x15
			"Commands", // 0x16
			"Options", // 0x17
			"FilterType", // 0x18
			"Truncation", // 0x19
			"RTFTruncation", // 0x1A
			"Conflict", // 0x1B
			"Collections", // 0x1C
			"ApplicationData", // 0x1D
			"DeletesAsMoves", // 0x1E
			"NotifyGUID", // 0x1F
			"Supported", // 0x20
			"SoftDelete", // 0x21
			"MIMESupport", // 0x22
			"MIMETruncation", // 0x23
			"Wait", // 0x24
			"Limit", // 0x25
			"Partial", // 0x26
	};

	/**
	 * Contacts
	 */
	public static final String[] CP_1 = {
	// Contacts
			"Anniversary", // 0x05
			"AssistantName", // 0x06
			"AssistantTelephoneNumber", // 0x07
			"Birthday", // 0x08
			"Body", // 0x09
			"BodySize", // 0x0A
			"BodyTruncated", // 0x0B
			"Business2TelephoneNumber", // 0x0C
			"BusinessAddressCity", // 0x0D
			"BusinessAddressCountry", // 0x0E
			"BusinessAddressPostalCode", // 0x0F
			"BusinessAddressState", // 0x10
			"BusinessAddressStreet", // 0x11
			"BusinessFaxNumber", // 0x12
			"BusinessTelephoneNumber", // 0x13
			"CarTelephoneNumber", // 0x14
			"Categories", // 0x15
			"Category", // 0x16
			"Children", // 0x17
			"Child", // 0x18
			"CompanyName", // 0x19
			"Department", // 0x1A
			"Email1Address", // 0x1B
			"Email2Address", // 0x1C
			"Email3Address", // 0x1D
			"FileAs", // 0x1E
			"FirstName", // 0x1F
			"Home2TelephoneNumber", // 0x20
			"HomeAddressCity", // 0x21
			"HomeAddressCountry", // 0x22
			"HomeAddressPostalCode", // 0x23
			"HomeAddressState", // 0x24
			"HomeAddressStreet", // 0x25
			"HomeFaxNumber", // 0x26
			"HomeTelephoneNumber", // 0x27
			"JobTitle", // 0x28
			"LastName", // 0x29
			"MiddleName", // 0x2A
			"MobileTelephoneNumber", // 0x2B
			"OfficeLocation", // 0x2C
			"OtherAddressCity", // 0x2D
			"OtherAddressCountry", // 0x2E
			"OtherAddressPostalCode", // 0x2F
			"OtherAddressState", // 0x30
			"OtherAddressStreet", // 0x31
			"PagerNumber", // 0x32
			"RadioTelephoneNumber", // 0x33
			"Spouse", // 0x34
			"Suffix", // 0x35
			"Title", // 0x36
			"Webpage", // 0x37
			"YomiCompanyName", // 0x38
			"YomiFirstName", // 0x39
			"YomiLastName", // 0x3A
			"CompressedRTF", // 0x3B
			"Picture", // 0x3C
	};

	/**
	 * Email
	 */
	public static final String[] CP_2 = {
	// Email
			"Attachment", // 0x05 <2>
			"Attachments", // 0x06 <2>
			"AttName", // 0x07 <2>
			"AttSize", // 0x08 <2>
			"Att0Id", // 0x09 <2>
			"AttMethod", // 0x0A <2>
			"AttRemoved", // 0x0B <2>
			"Body", // 0x0C <2>
			"BodySize", // 0x0D <2>
			"BodyTruncated", // 0x0E <2>
			"DateReceived", // 0x0F <2>
			"DisplayName", // 0x10 <2>
			"DisplayTo", // 0x11 <2>
			"Importance", // 0x12 <2>
			"MessageClass", // 0x13 <2>
			"Subject", // 0x14 <2>
			"Read", // 0x15 <2>
			"To", // 0x16 <2>
			"CC", // 0x17 <2>
			"From", // 0x18 <2>
			"ReplyTo", // 0x19 <2>
			"AllDayEvent", // 0x1A <2>
			"Categories", // 0x1B <2>
			"Category", // 0x1C <2>
			"DTStamp", // 0x1D <2>
			"EndTime", // 0x1E <2>
			"InstanceType", // 0x1F <2>
			"IntDBusyStatus", // 0x20 <2>
			"Location", // 0x21 <2>
			"MeetingRequest", // 0x22 <2>
			"Organizer", // 0x23 <2>
			"RecurrenceId", // 0x24 <2>
			"Reminder", // 0x25 <2>
			"ResponseRequested", // 0x26 <2>
			"Recurrences", // 0x27 <2>
			"Recurrence", // 0x28 <2>
			"Recurrence_Type", // 0x29 <2>
			"Recurrence_Until", // 0x2A <2>
			"Recurrence_Occurrences", // 0x2B <2>
			"Recurrence_Interval", // 0x2C <2>
			"Recurrence_DayOfWeek", // 0x2D <2>
			"Recurrence_DayOfMonth", // 0x2E <2>
			"Recurrence_WeekOfMonth", // 0x2F <2>
			"Recurrence_MonthOfYear", // 0x30 <2>
			"StartTime", // 0x31 <2>
			"Sensitivity", // 0x32 <2>
			"TimeZone", // 0x33 <2>
			"GlobalObjId", // 0x34 <2>
			"ThreadTopic", // 0x35 <2>
			"MIMEData", // 0x36 <2>
			"MIMETruncated", // 0x37 <2>
			"MIMESize", // 0x38 <2>
			"InternetCPID", // 0x39 <2>
			"Flag", // 0x3A <3>
			"FlagStatus", // 0x3B <3>
			"ContentClass", // 0x3C <3>
			"FlagType", // 0x3D <3>
			"CompleteTime", // 0x3E <3>
	};

	/**
	 * AirNotify
	 */
	public static final String[] CP_3 = {
	// AirNotify
			"Notify", // 0x05 <4>
			"Notification", // 0x06
			"Version", // 0x07
			"LifeTime", // 0x08
			"DeviceInfo", // 0x09
			"Enable", // 0x0A
			"Folder", // 0x0B
			"ServerId", // 0x0C
			"DeviceAddress", // 0x0D
			"ValidCarrierProfiles", // 0x0E
			"CarrierProfile", // 0x0F
			"Status", // 0x10
			"Responses", // 0x11
			"Devices", // 0x12
			"Device", // 0x13
			"Id", // 0x14
			"Expiry", // 0x15
			"NotifyGUID", // 0x16
			"DeviceFriendlyName", // 0x17
	};

	/**
	 * Calendar
	 */
	public static final String[] CP_4 = {
	// Calendar
			"TimeZone", // 0x05 <2>
			"AllDayEvent", // 0x06 <2>
			"Attendees", // 0x07 <2>
			"Attendee", // 0x08 <2>
			"AttendeeEmail", // 0x09 <2>
			"AttendeeName", // 0x0A <2>
			"Body", // 0x0B <2>
			"BodyTruncated", // 0x0C <2>
			"BusyStatus", // 0x0D <2>
			"Categories", // 0x0E <2>
			"Category", // 0x0F <2>
			"Compressed_RTF", // 0x10 <2>
			"DTStamp", // 0x11 <2>
			"EndTime", // 0x12 <2>
			"Exception", // 0x13 <2>
			"Exceptions", // 0x14 <2>
			"ExceptionIsDeleted", // 0x15 <2>
			"ExceptionStartTime", // 0x16 <2>
			"Location", // 0x17 <2>
			"MeetingStatus", // 0x18 <2>
			"OrganizerEmail", // 0x19 <2>
			"OrganizerName", // 0x1A <2>
			"Recurrence", // 0x1B <2>
			"RecurrenceType", // 0x1C <2>
			"RecurrenceUntil", // 0x1D <2>
			"RecurrenceOccurrences", // 0x1E <2>
			"RecurrenceInterval", // 0x1F <2>
			"RecurrenceDayOfWeek", // 0x20 <2>
			"RecurrenceDayOfMonth", // 0x21 <2>
			"RecurrenceWeekOfMonth", // 0x22 <2>
			"RecurrenceMonthOfYear", // 0x23 <2>
			"ReminderMinsBefore", // 0x24 <2>
			"Sensitivity", // 0x25 <2>
			"Subject", // 0x26 <2>
			"StartTime", // 0x27 <2>
			"UID", // 0x28 <2>
			"AttendeeStatus", // 0x29 <3>
			"AttendeeType", // 0x2A <3>
	};

	/**
	 * Move
	 */
	public static final String[] CP_5 = {
	// Move
			"MoveItems", // 0x05
			"Move", // 0x06
			"SrcMsgId", // 0x07
			"SrcFldId", // 0x08
			"DstFldId", // 0x09
			"Response", // 0x0A
			"Status", // 0x0B
			"DstMsgId", // 0x0C
	};

	/**
	 * GetItemEstimate
	 */
	public static final String[] CP_6 = {
	// GetItemEstimate
			"GetItemEstimate", // 0x05
			"Version", // 0x06
			"Collections", // 0x07
			"Collection", // 0x08
			"Class", // 0x09
			"CollectionId", // 0x0A
			"DateTime", // 0x0B
			"Estimate", // 0x0C
			"Response", // 0x0D
			"Status", // 0x0E
	};

	/**
	 * FolderHierarchy
	 */
	public static final String[] CP_7 = {
	// FolderHierarchy
			"Folders", // 0x05
			"Folder", // 0x06
			"DisplayName", // 0x07
			"ServerId", // 0x08
			"ParentId", // 0x09
			"Type", // 0x0A
			"Response", // 0x0B
			"Status", // 0x0C
			"ContentClass", // 0x0D
			"Changes", // 0x0E
			"Add", // 0x0F
			"Delete", // 0x10
			"Update", // 0x11
			"SyncKey", // 0x12
			"FolderCreate", // 0x13
			"FolderDelete", // 0x14
			"FolderUpdate", // 0x15
			"FolderSync", // 0x16
			"Count", // 0x17
			"Version", // 0x18
	};

	/**
	 * MeetingResponse
	 */
	public static final String[] CP_8 = {
	// MeetingResponse
			"CalId", // 0x05
			"CollectionId", // 0x06
			"MeetingResponse", // 0x07
			"ReqId", // 0x08
			"Request", // 0x09
			"Result", // 0x0A
			"Status", // 0x0B
			"UserResponse", // 0x0C
			"Version", // 0x0D
	};

	/**
	 * Tasks
	 */
	public static final String[] CP_9 = {
	// Tasks
			"Body", // 0x05 <2>
			"BodySize", // 0x06 <2>
			"BodyTruncated", // 0x07 <2>
			"Categories", // 0x08 <2>
			"Category", // 0x09 <2>
			"Complete", // 0x0A <2>
			"DateCompleted", // 0x0B <2>
			"DueDate", // 0x0C <2>
			"UTCDueDate", // 0x0D <2>
			"Importance", // 0x0E <2>
			"Recurrence", // 0x0F <2>
			"RecurrenceType", // 0x10 <2>
			"RecurrenceStart", // 0x11 <2>
			"RecurrenceUntil", // 0x12 <2>
			"RecurrenceOccurrences", // 0x13 <2>
			"RecurrenceInterval", // 0x14 <2>
			"RecurrenceDayOfMonth", // 0x15 <2>
			"RecurrenceDayOfWeek", // 0x16 <2>
			"RecurrenceWeekOfMonth", // 0x17 <2>
			"RecurrenceMonthOfYear", // 0x18 <2>
			"RecurrenceRegenerate", // 0x19 <2>
			"RecurrenceDeadOccur", // 0x1A <2>
			"ReminderSet", // 0x1B <2>
			"ReminderTime", // 0x1C <2>
			"Sensitivity", // 0x1D <2>
			"StartDate", // 0x1E <2>
			"UTCStartDate", // 0x1F <2>
			"Subject", // 0x20 <2>
			"CompressedRTF", // 0x21 <2>
			"OrdinalDate", // 0x22 <3>
			"SubOrdinalDate", // 0x23 <3>
	};

	/**
	 * ResolveRecipients
	 */
	public static final String[] CP_10 = {
	// ResolveRecipients
			"ResolveRecipients", // 0x05
			"Response", // 0x06
			"Status", // 0x07
			"Type", // 0x08
			"Recipient", // 0x09
			"DisplayName", // 0x0A
			"EmailAddress", // 0x0B
			"Certificates", // 0x0C
			"Certificate", // 0x0D
			"MiniCertificate", // 0x0E
			"Options", // 0x0F
			"To", // 0x10
			"CertificateRetrieval", // 0x11
			"RecipientCount", // 0x12
			"MaxCertificates", // 0x13
			"MaxAmbiguousRecipients", // 0x14
			"CertificateCount", // 0x15
	};

	/**
	 * ValidateCert
	 */
	public static final String[] CP_11 = {
	// ValidateCert
			"ValidateCert", // 0x05
			"Certificates", // 0x06
			"Certificate", // 0x07
			"CertificateChain", // 0x08
			"CheckCRL", // 0x09
			"Status", // 0x0A
	};

	/**
	 * Contacts2
	 */
	public static final String[] CP_12 = {
	// Contacts2
			"CustomerId", // 0x05
			"GovernmentId", // 0x06
			"IMAddress", // 0x07
			"IMAddress2", // 0x08
			"IMAddress3", // 0x09
			"ManagerName", // 0x0A
			"CompanyMainPhone", // 0x0B
			"AccountName", // 0x0C
			"NickName", // 0x0D
			"MMS", // 0x0E
	};

	/**
	 * Ping
	 */
	public static final String[] CP_13 = {
	// Ping
			"Ping", // 0x05
			"AutdState", // 0x06
			"Status", // 0x07
			"HeartbeatInterval", // 0x08
			"Folders", // 0x09
			"Folder", // 0x0A
			"Id", // 0x0B
			"Class", // 0x0C
			"MaxFolders", // 0x0D
	};

	/**
	 * Provision
	 */
	public static final String[] CP_14 = {
	// Provision
			"Provision", // 0x05 <2>
			"Policies", // 0x06 <2>
			"Policy", // 0x07 <2>
			"PolicyType", // 0x08 <2>
			"PolicyKey", // 0x09 <2>
			"Data", // 0x0A <2>
			"Status", // 0x0B <2>
			"RemoteWipe", // 0x0C <2>
			"EASProvisionDoc", // 0x0D <3>
			"DevicePasswordEnabled", // 0x0E <3>
			"AlphanumericDevicePasswordRequired", // 0x0F <3>
			"DeviceEncryptionEnabled", // 0x10 <3>
			"PasswordRecoveryEnabled", // 0x11 <3>
			"DocumentBrowseEnabled", // 0x12 <3>
			"AttachmentsEnabled", // 0x13 <3>
			"MinDevicePasswordLength", // 0x14 <3>
			"MaxInactivityTimeDeviceLock", // 0x15 <3>
			"MaxDevicePasswordFailedAttempts", // 0x16 <3>
			"MaxAttachmentSize", // 0x17 <3>
			"AllowSimpleDevicePassword", // 0x18 <3>
			"DevicePasswordExpiration", // 0x19 <3>
			"DevicePasswordHistory", // 0x1A <3>
			"AllowStorageCard", // 0x1B <5>
			"AllowCamera", // 0x1C <4>
			"RequireDeviceEncryption", // 0x1D <4>
			"AllowUnsignedApplications", // 0x1E <4>
			"AllowUnsignedInstallationPackages", // 0x1F <4>
			"MinDevicePasswordComplexCharacters", // 0x20 <4>
			"AllowWiFi", // 0x21 <4>
			"AllowTextMessaging", // 0x22 <4>
			"AllowPOPIMAPEmail", // 0x23 <4>
			"AllowBluetooth", // 0x24 <4>
			"AllowIrDA", // 0x25 <4>
			"RequireManualSyncWhenRoaming", // 0x26 <4>
			"AllowDesktopSync", // 0x27 <4>
			"MaxCalendarAgeFilter", // 0x28 <4>
			"AllowHTMLEmail", // 0x29 <4>
			"MaxEmailAgeFilter", // 0x2A <4>
			"MaxEmailBodyTruncationSize", // 0x2B <4>
			"MaxEmailHTMLBodyTruncationSize", // 0x2C <4>
			"RequireSignedSMIMEMessages", // 0x2D <4>
			"RequireEncryptedSMIMEMessages", // 0x2E <4>
			"RequireSignedSMIMEAlgorithm", // 0x2F <4>
			"RequireEncryptionSMIMEAlgorithm", // 0x30 <4>
			"AllowSMIMEEncryptionAlgorithmNegotiation", // 0x31 <4>
			"AllowSMIMESoftCerts", // 0x32 <4>
			"AllowBrowser", // 0x33 <4>
			"AllowConsumerEmail", // 0x34 <4>
			"AllowRemoteDesktop", // 0x35 <4>
			"AllowInternetSharing", // 0x36 <4>
			"UnapprovedInROMApplicationList", // 0x37 <4>
			"ApplicationName", // 0x38 <4>
			"ApprovedApplicationList", // 0x39 <4>
			"Hash", // 0x3A <4>
	};

	/**
	 * Search
	 */
	public static final String[] CP_15 = {
	// Search
			"Search", // 0x05 <2>
			"UNUSED", // 0x06
			"Store", // 0x07 <2>
			"Name", // 0x08 <2>
			"Query", // 0x09 <2>
			"Options", // 0x0A <2>
			"Range", // 0x0B <2>
			"Status", // 0x0C <2>
			"Response", // 0x0D <2>
			"Result", // 0x0E <2>
			"Properties", // 0x0F <2>
			"Total", // 0x10 <2>
			"EqualTo", // 0x11 <3>
			"Value", // 0x12 <3>
			"And", // 0x13 <3>
			"Or", // 0x14 <3>
			"FreeText", // 0x15 <3>
			"UNUSED", // 0x16
			"DeepTraversal", // 0x17 <3>
			"LongId", // 0x18 <3>
			"RebuildResults", // 0x19 <3>
			"LessThan", // 0x1A <3>
			"GreaterThan", // 0x1B <3>
			"Schema", // 0x1C <3>
			"Supported", // 0x1D <3>
	};

	/**
	 * GAL
	 */
	public static final String[] CP_16 = {
	// GAL
			"DisplayName", // 0x05
			"Phone", // 0x06
			"Office", // 0x07
			"Title", // 0x08
			"Company", // 0x09
			"Alias", // 0x0A
			"FirstName", // 0x0B
			"LastName", // 0x0C
			"HomePhone", // 0x0D
			"MobilePhone", // 0x0E
			"EmailAddress", // 0x0F
	};

	/**
	 * AirSyncBase
	 */
	public static final String[] CP_17 = {
	// AirSyncBase
			"BodyPreference", // 0x05
			"Type", // 0x06
			"TruncationSize", // 0x07
			"AllOrNone", // 0x08
			"UNDEFINED_IN_MICROSOFT_SPEC", "Body", // 0x0A
			"Data", // 0x0B
			"EstimatedDataSize", // 0x0C
			"Truncated", // 0x0D
			"Attachments", // 0x0E
			"Attachment", // 0x0F
			"DisplayName", // 0x10
			"FileReference", // 0x11
			"Method", // 0x12
			"ContentId", // 0x13
			"ContentLocation", // 0x14
			"IsInline", // 0x15
			"NativeBodyType", // 0x16
			"ContentType", // 0x17
	};

	/**
	 * Settings
	 */
	public static final String[] CP_18 = {
	// Settings
			"Settings", // 0x05
			"Status", // 0x06
			"Get", // 0x07
			"Set", // 0x08
			"Oof", // 0x09
			"OofState", // 0x0A
			"StartTime", // 0x0B
			"EndTime", // 0x0C
			"OofMessage", // 0x0D
			"AppliesToInternal", // 0x0E
			"AppliesToExternalKnown", // 0x0F
			"AppliesToExternalUnknown", // 0x10
			"Enabled", // 0x11
			"ReplyMessage", // 0x12
			"BodyType", // 0x13
			"DevicePassword", // 0x14
			"Password", // 0x15
			"DeviceInformaton", // 0x16
			"Model", // 0x17
			"IMEI", // 0x18
			"FriendlyName", // 0x19
			"OS", // 0x1A
			"OSLanguage", // 0x1B
			"PhoneNumber", // 0x1C
			"UserInformation", // 0x1D
			"EmailAddresses", // 0x1E
			"SmtpAddress", // 0x1F

			// Exchange 2k10
			"UserAgent", // 0x20
			"EnableOutboundSMS", // 0x21
			"MobileOperator", // 0x22

	};

	/**
	 * DocumentLibrary
	 */
	public static final String[] CP_19 = {
	// DocumentLibrary
			"LinkId", // 0x05
			"DisplayName", // 0x06
			"IsFolder", // 0x07
			"CreationDate", // 0x08
			"LastModifiedDate", // 0x09
			"IsHidden", // 0x0A
			"ContentLength", // 0x0B
			"ContentType", // 0x0C
	};

	/**
	 * ItemOperations
	 */
	public static final String[] CP_20 = {
	// ItemOperations
			"ItemOperations", // 0x05
			"Fetch", // 0x06
			"Store", // 0x07
			"Options", // 0x08
			"Range", // 0x09
			"Total", // 0x0A
			"Properties", // 0x0B
			"Data", // 0x0C
			"Status", // 0x0D
			"Response", // 0x0E
			"Version", // 0x0F
			"Schema", // 0x10
			"Part", // 0x11
			"EmptyFolderContents", // 0x12
			"DeleteSubFolders", // 0x13
	};

	public static final Map<String, Integer> NAMESPACES_IDS;
	public static final Map<Integer, String[]> NAMESPACES_TAGS;
	public static final Map<String, Map<String, Integer>> NAMESPACES_MAPPINGS;

	static {
		NAMESPACES_IDS = new HashMap<String, Integer>();
		NAMESPACES_TAGS = new HashMap<Integer, String[]>();
		NAMESPACES_MAPPINGS = new HashMap<String, Map<String, Integer>>();

		NAMESPACES_IDS.put("AirSync", 0);
		NAMESPACES_TAGS.put(0, CP_0);
		createMappings("AirSync");

		NAMESPACES_IDS.put("Contacts", 1);
		NAMESPACES_TAGS.put(1, CP_1);
		createMappings("Contacts");

		NAMESPACES_IDS.put("Email", 2);
		NAMESPACES_TAGS.put(2, CP_2);
		createMappings("Email");

		NAMESPACES_IDS.put("AirNotify", 3);
		NAMESPACES_TAGS.put(3, CP_3);
		createMappings("AirNotify");

		NAMESPACES_IDS.put("Calendar", 4);
		NAMESPACES_TAGS.put(4, CP_4);
		createMappings("Calendar");

		NAMESPACES_IDS.put("Move", 5);
		NAMESPACES_TAGS.put(5, CP_5);
		createMappings("Move");

		NAMESPACES_IDS.put("GetItemEstimate", 6);
		NAMESPACES_TAGS.put(6, CP_6);
		createMappings("GetItemEstimate");

		NAMESPACES_IDS.put("FolderHierarchy", 7);
		NAMESPACES_TAGS.put(7, CP_7);
		createMappings("FolderHierarchy");

		NAMESPACES_IDS.put("MeetingResponse", 8);
		NAMESPACES_TAGS.put(8, CP_8);
		createMappings("MeetingResponse");

		NAMESPACES_IDS.put("Tasks", 9);
		NAMESPACES_TAGS.put(9, CP_9);
		createMappings("Tasks");

		NAMESPACES_IDS.put("ResolveRecipients", 10);
		NAMESPACES_TAGS.put(10, CP_10);
		createMappings("ResolveRecipients");

		NAMESPACES_IDS.put("ValidateCert", 11);
		NAMESPACES_TAGS.put(11, CP_11);
		createMappings("ValidateCert");

		NAMESPACES_IDS.put("Contacts2", 12);
		NAMESPACES_TAGS.put(12, CP_12);
		createMappings("Contacts2");

		NAMESPACES_IDS.put("Ping", 13);
		NAMESPACES_TAGS.put(13, CP_13);
		createMappings("Ping");

		NAMESPACES_IDS.put("Provision", 14);
		NAMESPACES_TAGS.put(14, CP_14);
		createMappings("Provision");

		NAMESPACES_IDS.put("Search", 15);
		NAMESPACES_TAGS.put(15, CP_15);
		createMappings("Search");

		NAMESPACES_IDS.put("GAL", 16);
		NAMESPACES_TAGS.put(16, CP_16);
		createMappings("GAL");

		NAMESPACES_IDS.put("AirSyncBase", 17);
		NAMESPACES_TAGS.put(17, CP_17);
		createMappings("AirSyncBase");

		NAMESPACES_IDS.put("Settings", 18);
		NAMESPACES_TAGS.put(18, CP_18);
		createMappings("Settings");

		NAMESPACES_IDS.put("DocumentLibrary", 19);
		NAMESPACES_TAGS.put(19, CP_19);
		createMappings("DocumentLibrary");

		NAMESPACES_IDS.put("ItemOperations", 20);
		NAMESPACES_TAGS.put(20, CP_20);
		createMappings("ItemOperations");
	}

	public static String[] getTagsTableForNamespace(String nsName) {
		Integer codePage = NAMESPACES_IDS.get(nsName);
		String[] ret = NAMESPACES_TAGS.get(codePage);
		return ret;
	}

	private static void createMappings(String namespace) {
		Integer tableId = NAMESPACES_IDS.get(namespace);
		if (logger.isDebugEnabled()) {
			logger.info("id for namespace '" + namespace + "' is "
					+ Integer.toHexString(tableId));
		}
		String[] stab = NAMESPACES_TAGS.get(tableId);
		int start = 0x05;
		Map<String, Integer> mapping = new HashMap<String, Integer>();
		for (String tag : stab) {
			mapping.put(tag, start++);
		}
		NAMESPACES_MAPPINGS.put(namespace, mapping);
	}

	public static Map<String, Integer> getElementMappings(String newNs) {
		return NAMESPACES_MAPPINGS.get(newNs);
	}

}
