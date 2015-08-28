<?php 
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



///////////////////////////////////////////////////////////////////////////////
// OBM - File : global.inc                                                   //
//     - Desc : Global common OBM include file                               //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$obm_version = '3.2.0-SNAPSHOT';
$l_obm_title = 'OBM';

// Id of the Publication reception type dedicated to the auto subscription.
$c_focsys = 3;

//---------------------------------------------------------------------------//
// OBM Default configurable Constants : Default values
// To change these values => change these in conf/obm_conf.inc 
//---------------------------------------------------------------------------//

// Global parameters --------------------------------------------------------//

// OBM default resources path
$resources_path = '/images';

// Todo lines to display
$cgp_todo_nb = 5;

// Maximum rows allowed to display
$conf_display_max_rows = 200;

// Popup Display size
$popup_width = 800;
$popup_height = 600;
$popup_contact_height = 600;

// Access Keys
$cak_prev = 'P';
$cak_next = 'N';
$cak_page_prev = '4';
$cak_page_next = '6';
$cak_begin = 'B';
$cak_end = 'E';

// Default domain id
$c_default_domain = 2;

// Query limits (for user queries)
$cql_max_row = 50000;    // Maximum rows to be returned
$cql_max_cost = 300000;  // Maximum query cost (Postgres)

// Allow * in search field
$cgp_sql_star = true;

// Tuning constants
$ctu_sql_limit = false;

// is Mail enabled (is a mail server configured for OBM)
$cgp_mail_enabled = false;

// is Demo enabled (login screen array with default account)
$cgp_demo_enabled = true;

// Session Cookie lifetime (in seconds, 0=session cookie)
$cs_lifetime = 7200;

// Use Database sessions
$cgp_sess_db = false;

// Cookie name and domain
$cgp_cookie_name = 'OBM_Session';
$cgp_cookie_domain = 'aliasource.local';

// if the archive checkbox is checked search returns ONLY
// archived results
$cgp_archive_only = false;
// authentification : 'CAS' (SSO AliaSuite), 'LemonLDAP' (SS0) or 'standalone' (default)
// CAS specific parameters are in conf/obm_conf.inc.
$auth_kind = 'standalone'; 
// $auth_kind = 'LemonLDAP';

// Encyption used to store password : 'PLAIN', 'MD5' or 'CRYPT'
$password_encryption = 'PLAIN';

// Handle connectors data
$c_use_connectors = true;

// Modules specific parameters ----------------------------------------------//

// Company and Contact : Copy address to clipboard
$cclipboard_address = array ('address1' , 'address2', 'address3', 'zipcode', 'town', 'expresspostal', 'country', 'phone');

// Company : Auto format fields
$caf_company_name = false;
$caf_town = false;

// Contact : is mailing enabled by default for a new contact
$cgp_mailing_default = true;

// Contact : is a new contact private by default
$ccontact_private_default = false;

// Contact : archived contact expiration time (in month), 0 to disable
// a contact archived since more than $cgp_contact_expiration month is deleted by the cron ContactExpiration
$cgp_contact_expiration = 0;

// Calendar: display my rights panel
$c_calendar_display_my_rights = false;

// Calendar : show public groups
$ccalendar_public_groups = true;

// Calendar : First day of the week
$phpversion = substr(phpversion(),0,1);
if ($phpversion >= 5) {
  $ccalendar_weekstart = 'monday';
} else {
  $ccalendar_weekstart = 'monday + 2 hours'; // +2h to fix a DST php4 bug //
}

// calendar alert values (seconds => label or minutes displayed)
$ccalendar_alerts = array (
  '60'   => '',
  '300'  => '',
  '600'  => '',
  '900'  => '',
  '1800' => '',
  '3600' => '',
  '7200' => '',
  '18000'=> '',
  '43200'=> '',
  '86400'=> '',
  '172800' => '',
  '604800' => '');


// Calendar : First day of the week (+2h to fix a DST php bug)
//$ccalendar_weekstart = 'monday + 2 hours';
// Calendar max user displayed in the calendar
$ccalendar_max_users = 25;

// Calendar first and last hour display
$ccalendar_first_hour = 8;
$ccalendar_last_hour = 20;

// Calendar Resource usage
$ccalendar_resource = true;

// Calendar send ics file
$ccalendar_send_ics = true;

// Ics mime encoding for emails
$ccalendar_ics_encoding = 'base64';

// Calendar send ics through stomp
//$ccalendar_ics_eventStompObserver = true;
//$stomp_host = '127.0.0.1';
//$stomp_port = '61613';
//$stomp_login = '';
//$stomp_passcode = '';

// Block users from changing their status if $block_status_str is the organizer
//$ccalendar_block_status = true;
//$block_status_str = "login";


// Remote calendar cache time to live
$cremote_calendar_ics_ttl = 60;

// Calendar hour fraction (minutes select granularity)
$ccalendar_hour_fraction = 4;

// Resource can be reserved for the same period more than once 
$ccalendar_resource_overbooking = true;

// Allow to set wheter calendar event creation happend on simple or double click
// onClick or onDblClick (default is onDblClick)
$ccalendar_invocation_method = 'onDblClick';

$ccalendar_conflicts_limit = 10;

// Time : In how many parts we split up a day (Time management)
$c_day_fraction = 8;

// Time : Worked days in a week (start at sunday)
$c_working_days = array(0,1,1,1,1,1,0);

// Mailshare : default mailshare quota
$cmailshare_default_quota = 0;

// User : default user mailbox quota
$cuser_default_quota = 0;


// Incident : Generic E-mail for Incident module
$cmail_incident = '';

// Document : constants
$default_path = '/Default';                  // Default directory
$default_mime = 'application/octet-stream';  // Default Mime Type
$c_quota_private_document = 20;

// Group : is a new group private by default
$cgroup_private_default = true;

// Import : temporary path
$tmp_path = '/tmp';

// default taxes values
$cdefault_tax = array ('TVA 19,6' => 1.196, 'TVA 5,5' => 1.055, 'Pas de TVA' => 1);

// Imagetype constants
$c_imagetype = array('1' => 'GIF',
                     '2' => 'JPEG',
                     '3' => 'PNG');

// Max image size
$c_max_image_size = array('width' => '128',
                          'height' => '128',
                          'size' => '102400');

// Page layout
$c_pagelayout_portrait = 'portrait';
$c_pagelayout_landscape = 'landscape';

// Freeze
$c_nb_login_failed = 10;
$c_login_interval = 10;
$c_frozen_duration = 10;

// Activate user defined code : check rules, hook functions
$conf_modules = false;

$c_addressbook_type = array('contacts' => 'true', 'collected_contacts' => 'true');

// Max domain alias, users, group and mailshares mail alias (0=unlimted)
$c_max_user_alias = 0;
$c_max_domain_alias = 0;
$c_max_mailshare_alias = 0;
$c_max_group_alias = 1;

// Max user nomade mail
$c_max_email_nomade = 1;

// Access restriction

//---------------------------------------------------------------------------//
// Default access rights bits meanings
// 0 - read  : data
// 1 - own   : owner data
// 2 - write : data
// 3 - read  : admin 
// 4 - write : admin 
$cright_none = 0;
$cright_read = 1;
$cright_own = 2;
$cright_write = 4;
$cright_read_admin = 8;
$cright_write_admin = 16;

// reader : 01 (bit 0) : can see data
// user   : 03 (bits 0+1) : can see data + insert or update his own data
// editor : 07 (bits 0+1+2) : can see data + write data
// admin  : 1F (bits 0+1+2+3+4) : can do everything
$perm_reader = '01';
$perm_user = '03';
$perm_editor = '07';
$perm_admin = '1F';

///////////////////////////////////////////////////////////////////////////////
// Load global parameters from .ini file
///////////////////////////////////////////////////////////////////////////////
$enable_confidential_events = true;

$ini_array = parse_ini_file("$path/../conf/obm_conf.ini");

// Crappy hack to work-around the PHP bug about parsing booleans in ini files
if (isset($ini_array['enable-confidential-events'])) {
  $enable_confidential_events = $ini_array['enable-confidential-events'] == 1;
}

$obmdb_host = $ini_array['host'];
$obmdb_dbtype = $ini_array['dbtype']; 
$obmdb_db = $ini_array['db'];
$obmdb_user = $ini_array['user'];
$obmdb_password = $ini_array['password'];

// Single or multi domain
$c_singleNameSpace = $ini_array['singleNameSpace'];

// Max ActiveUserObm rows to diplay
$c_activeUserObmMaxRows = 10000;

// Backup root
if (is_null($ini_array['backupRoot'])) {
    $cbackup_root = "/var/lib/obm/backup";
}else {
    $cbackup_root = $ini_array['backupRoot'];
}

$cgp_host = $ini_array['external-protocol'].'://'.$ini_array['external-url'].$ini_array['obm-prefix'];

// OBMFULL-6218
// Because PHP can't parse a false state boolean in a ini file we must do this to keep boolean value
// reference: https://bugs.php.net/bug.php?id=38409 - must be fixed in 5.6.1
$anonymize_private_events_default = true;

$anonymize_private_events = parse_anonymize_private_event($ini_array['anonymize-private-events'], $anonymize_private_events_default);

function parse_anonymize_private_event($value, $default) {
  if (!isset($value) && empty($value)) {
    return $default;
  } else {
    return ($value == 1);
  }
}

// ldapauth ini variables
$auth_ldap_server = $ini_array['auth-ldap-server'];
$auth_ldap_basedn = $ini_array['auth-ldap-basedn'];
$auth_ldap_binddn = $ini_array['auth-ldap-binddn'];
$auth_ldap_bindpw = $ini_array['auth-ldap-bindpw'];
$auth_ldap_filter = $ini_array['auth-ldap-filter'];
$auth_ldap_userid_attribute = $ini_array['auth-ldap-userid-attribute'];

// satellite ini variables
$admin_mail_address = $ini_array['admin_mail_address'];

//------------------//
// Services enabled //
//------------------//
$cgp_use['service']['web'] = $ini_array['obm-web'];
$cgp_use['service']['mail'] = $ini_array['obm-mail'];
$cgp_use['service']['mail_nomad'] = $ini_array['obm-mail'];
$cgp_use['service']['mail_vacation'] = $ini_array['obm-mail'];
$cgp_use['service']['samba'] = $ini_array['obm-samba'];
$cgp_use['service']['ldap'] = $ini_array['obm-ldap'];
$cgp_use['service']['dns'] = false;
$cgp_use['service']['firewall'] = false;
$cgp_use['service']['network'] = false;

if ($cgp_use['service']['ldap'] == false) {
  $cgp_use['service']['mail'] = false;
  $cgp_use['service']['samba'] = false;
}

if ($cgp_use['service']['mail'] == false) {
  $cgp_use['service']['mail_nomad'] = false;
  $cgp_use['service']['mail_vacation'] = false;
}

// User UID and GID
// First UID for users
$c_first_uid = 1001;

// GID for users (without samba)
$cg_gid_user = 1000;
// GID for admin users with Samba
$cg_gid_smb_admin = 512;
// GID for simple users with Samba
$cg_gid_smb_user = 513;
// GID for guest users with Samba
$cg_gid_smb_guest = 514;


// Host UID and GID
$cg_gid_smb_host = 515;
$cg_gid_host = 515;

$c_origin_web = 'obm-ui-web-'.$obm_version;
$c_origin_ajax = 'obm-ui-ajax-'.$obm_version;
$c_origin_cron = 'obm-ui-cron-'.$obm_version;
$c_origin_import = 'obm-ui-import-'.$obm_version;
// Mail chooser constant hooks
$c_mailchooser_global_hooks = array(
  array(
    'id'            => 98,
    'hook_function' => 'least_used_memory',
    'name'          => 'l_mailboxchoice_least_used_disk',
  ),
  array(
    'id'            => 99,
    'hook_function' => 'least_boxes',
    'name'          => 'l_mailboxchoice_least_boxes',
  ),
);

//-------------------------//
// Default Displayed Infos //
//-------------------------//
// sections -- Urls
$cgp_show['section']['gw']['url'] = "$path/calendar/calendar_index.php";
$cgp_show['section']['prod']['url'] = "$path/time/time_index.php";
$cgp_show['section']['com']['url'] = "$path/company/company_index.php";
$cgp_show['section']['compta']['url'] = "$path/invoice/invoice_index.php";
$cgp_show['section']['user']['url'] = "$path/settings/settings_index.php";
$cgp_show['section']['admin']['url'] = "$path/admin/admin_index.php?mode=html";
$cgp_show['section']['dic']['url'] = "$path/admin_ref/admin_ref_index.php?mode=html";
$cgp_show['section']['my']['url'] = "$path/settings/settings_index.php";
$cgp_show['section']['webmail']['url'] = "$path/webmail/webmail_index.php";

// modules
$cgp_show['module']['company'] = 'com';
$cgp_show['module']['lead'] = 'com';
$cgp_show['module']['deal'] = 'com';
//$cgp_show['module']['list'] = 'com';
$cgp_show['module']['publication'] = 'com';
$cgp_show['module']['statistic'] = 'com';
$cgp_show['module']['cv'] = false;
$cgp_show['module']['calendar'] = 'gw';
$cgp_show['module']['contact'] = 'gw';
$cgp_show['module']['todo'] = 'gw';
$cgp_show['module']['document'] = 'gw';
$cgp_show['module']['time'] = 'prod';
$cgp_show['module']['project'] = 'prod';
$cgp_show['module']['planning'] = 'prod';
$cgp_show['module']['contract'] = 'prod';
$cgp_show['module']['incident'] = 'prod';
$cgp_show['module']['invoice'] = 'compta';
$cgp_show['module']['payment'] = 'compta';
$cgp_show['module']['account'] = 'compta';
$cgp_show['module']['people'] = 'user';
$cgp_show['module']['user'] = 'user';
$cgp_show['module']['group'] = 'user';
$cgp_show['module']['resource'] = 'user';
$cgp_show['module']['resourcegroup'] = 'user';
$cgp_show['module']['host'] = 'user';
if ($cgp_use['service']['mail']) {
  $cgp_show['module']['mailshare'] = 'user';
}
$cgp_show['module']['organizationalchart'] = 'user';
$cgp_show['module']['tools'] = 'admin';
$cgp_show['module']['control'] = 'admin';
$cgp_show['module']['admin'] = 'admin';
$cgp_show['module']['import'] = 'admin';
$cgp_show['module']['backup'] = 'admin';
$cgp_show['module']['imap_archive'] = 'admin';
$cgp_show['module']['report'] = 'admin';
if ($cgp_use['service']['samba']) {
  $cgp_show['module']['samba'] = 'admin';
}
$cgp_show['module']['domain'] = 'admin';
$cgp_show['module']['admin_pref'] = 'dic';
$cgp_show['module']['admin_data'] = 'dic';
$cgp_show['module']['admin_lang'] = 'dic';
$cgp_show['module']['admin_code'] = 'dic';
$cgp_show['module']['admin_ref'] = 'dic';
$cgp_show['module']['settings'] = 'my';
$cgp_show['module']['password'] = 'my';

$cgp_show['module']['webmail_pref'] = 'my';

if ($cgp_use['service']['mail_nomad']) {
  $cgp_show['module']['mailforward'] = 'my';
}
if ($cgp_use['service']['mail_vacation']) {
  $cgp_show['module']['vacation'] = 'my';
}
if ($cgp_use['service']['mail']) {
  $cgp_show['module']['mailbox'] = 'my';
}
$cgp_show['module']['profile'] = 'admin';
$cgp_show['module']['userpattern'] = 'admin';
$cgp_show['module']['log'] = false;

if (!$cgp_use['service']['ldap']) {
  $cgp_show['module']['tools'] = false;
}

$cgp_use['property']['delegation'] = true;

$cgp_show['module']['device'] = 'my';
$cgp_show['module']['admin_device'] = 'admin';
$cgp_show['module']['userbackup'] = 'my';

// Specific user data
$cgp_user = array();
$clog_root = '/var/log';

// Default preferences data
$set_lang_default = $ini_array['lang'];
$set_theme_default = 'default';
$set_menu_default = $cme_both;
$set_display_default = 'no';
$set_rows_default = 10;
$set_todo_default = 'todo_priority';
$set_date_upd_default = $cda_dmy;
$set_date_default = $cda_iso;
$set_commentorder_default = $cco_chro;
$set_debug_default = 0;
$set_cal_interval_default = 2;
$set_timezone_default = 'Europe/Paris';

$ics_sharing_max_months = 3;

//---------------------------------------------------------------------------//
// END : OBM Default configurable Constants : Default values
//---------------------------------------------------------------------------//

require_once(dirname(dirname(__file__)).'/conf/obm_conf.inc');
require_once("$obminclude/phplib/obmlib.inc");
require_once("$obminclude/of/of_date.inc");
require_once("$obminclude/of/of_template.php");
require_once("$obminclude/of/of_i18n.php");

if (!$path) {
  $path = pathinfo(__FILE__);
  $path = $path['dirname'];
}
set_include_path(get_include_path().":$path/../$obminclude/lib");
set_include_path(get_include_path().":$path/../lib:$path/../app:$path/../app/default/models");

// Autoloading
require_once 'OBM/Autoloader.php';
require_once 'OBM/Autoloader/Standard.php';
$autoloader = OBM_Autoloader::getInstance();
$autoloader->autoloaders['OBM'] =  new OBM_Autoloader_Standard();
$autoloader->autoloaders['Stato'] =  new OBM_Autoloader_Standard();
$autoloader->autoloaders['*'] = new OBM_Autoloader_Resource();

require_once("$obminclude/lib/Mustache/Autoloader.php");
Mustache_Autoloader::register();

// Document root
if (is_null($ini_array['documentRoot'])) {
    $cdocument_root = "/var/lib/obm/documents";
}else {
    $cdocument_root = $ini_array['documentRoot'];
}

// Document default directory
if (is_null($ini_array['documentDefaultPath'])) {
    $default_path = "/";
}else {
    $default_path = $ini_array['documentDefaultPath'];
}

define('C_IMAGE_PATH',$cgp_host.'images');

// Update password encryptation in uppercase
$password_encryption = strtoupper($password_encryption);

// Automate commands
$cmd_halt = "sudo /sbin/halt";
$cmd_update = "$path/../auto/update.pl";
$cmd_check_update = "ps aux | grep auto/update.pl | grep -v grep";
$cmd_mailforward = "$path/../auto/updateSieve.pl";
$cmd_vacation = "$path/../auto/updateSieve.pl";
$cmd_ldap_passwd = "$path/../auto/changePasswd.pl";
$cmd_update_bal_acl = "$path/../auto/updateCyrusAcl.pl";
$cmd_update_ftp_entity = "$path/../auto/update.pl --entity --domain-id";

$section = $cgp_show['module'][$module];

//---------------------------------------------------------------------------------------------//
//Setting up the default date timezone before using any date function to avoid PHP 5.3 Warnings //
if(!isset($_SESSION['set_timezone']) && !isset($cronJobProcessing)) {
	date_default_timezone_set($set_timezone_default);
}

//---------------------------------------------------------------------------------------------//
// Time : Day beginning a week (0=Sunday, 1=Monday,...)
$c_week_first_day = date('w', strtotime($ccalendar_weekstart));
//-----------------------------//
// Calculated Enabled services //
//-----------------------------//
if (! $cgp_use['service']['mail']) {
  $cgp_use['service']['mail_nomad'] = false;
  $cgp_use['service']['mail_vacation'] = false;
  $cgp_show['module']['mailforward'] = false;
  $cgp_show['module']['vacation'] = false;
  $cgp_show['module']['mailshare'] = false;
  $cgp_show['module']['mailbox'] = false;
}

if (! $cgp_use['service']['samba']) {
  $cgp_show['module']['samba'] = false;
}

//----------------------------//
// Calculated Displayed Infos //
//----------------------------//
if (! $cgp_show['module']['company']) {
  $cgp_show['module']['deal'] = false;
  $cgp_show['module']['contract'] = false;
  $cgp_show['module']['invoice'] = false;
  $cgp_show['module']['import'] = false;
}

if (! $cgp_show['module']['contact']) {
  $cgp_show['module']['list'] = false;
  $cgp_hide['company']['company_contact_number'] = true;
  $cgp_hide['company']['company_new_contact'] = true;
}

if (! $cgp_show['module']['project']) {
  $cgp_show['module']['time'] = false;
}

if (! $cgp_show['module']['contract']) {
  $cgp_show['module']['incident'] = false;
}

if (! $cgp_show['module']['invoice']) {
  $cgp_show['module']['payment'] = false;
}

if (! $cgp_show['module']['deal']) {
  $cgp_hide['company']['company_deal_number'] = true;
}

if ($cgp_hide['company']['company_address3']) {
  $cgp_hide['contact']['contact_address3'] = true;
  $cgp_hide['list_contact']['address3'] = true;
  $cgp_hide['list_contact_2']['address3'] = true;
}

if ($cgp_hide['company']['company_expresspostal']) {
  $cgp_hide['contact']['contact_expresspostal'] = true;
  $cgp_hide['list_contact']['expresspostal'] = true;
  $cgp_hide['list_contact_2']['expresspostal'] = true;
}

if ($cgp_hide['contact']['function']) {
  $cgp_hide['list_contact']['function'] = true;
  $cgp_hide['list_contact_2']['function'] = true;
}


// If delegation not used, disable delegation fields
if (! $cgp_use['property']['delegation']) {
  $cgp_hide['user']['userobm_delegation'] = true;
  $cgp_hide['group']['group_delegation'] = true;
  $cgp_hide['mailshare']['mailshare_delegation'] = true;
  $cgp_hide['host']['host_delegation'] = true;
}

// Public right on contact is not possible
$cgp_default_right['contact'] = array (
      'public' => array(
        'access' => -1,
        'read' => -1,
        'write' => -1,
        'admin' => 0
      )
    );
//---------------------------------------------------------------------------//
// OBM authentication Management : 
// Just change $cas_version according to your phpCAS lib
//---------------------------------------------------------------------------//
if ($auth_kind == 'CAS') {
  $auth_class_name = 'CAS_Auth';
} elseif (strcasecmp($auth_kind,'LemonLDAP') == 0) {
  require_once dirname(__FILE__) . '/lib/LemonLDAP/package.inc';
  $auth_class_name = 'LemonLDAP_Auth';
} elseif ($auth_kind == 'standalone') {
  $auth_class_name = 'OBM_Challenge_Auth';
} elseif ($auth_kind == 'ldap') {
  $auth_class_name = 'LDAP_Auth';
} else {
  echo 'unknown Authentication kind !<br/>';
  echo 'please edit $auth_kind in conf/obm_conf.inc';
  exit;
}

//---------------------------------------------------------------------------//
// OBM internal Constants : Do not change 
//---------------------------------------------------------------------------//

// Global Constants
$c_all = '_ALL_';           // Used in Form select
$c_all_f = $c_all;
$c_undef = '0';             // Used in Form select
$c_none = '-1';             // Used in Form select
$c_null = 'NULL';           // Used in queries (date is null)
$c_auto = '_AUTO_';
$c_yes = 'Yes';	            // Maybe stored in database
$c_no = 'No';
$c_public = '__PUBLIC__';
$c_private = '__PRIVATE__';

// Debug Constants
$cdg_id = 1;     // id display
$cdg_param = 2;  // parameter display
$cdg_sess = 4;   // session display
$cdg_sql = 8;    // sql query display
$cdg_exe = 16;   // program execution
$cdg_solr = 32;   // solr query display

// Date constants
$c_php_isodate_format = 'Y-m-d';  // AAAA-MM-JJ iso format for php date()
$c_php_isodatetime_format = 'Y-m-d H:i:s';  // AAAA-MM-JJ H:m:s iso format
$cda_iso = 'Y-m-d'; // Iso format
$cda_en = 'm/d/Y';  // us format
$cda_fr = 'd/m/Y';  // fr format
$cda_txt = 'd M Y'; // txt format

// Menu display options constants
$cme_txt = 'text';  // text
$cme_ico = 'icon';  // icon
$cme_both = 'both'; // both

// Comment dislay order
$cco_chro = 0; // Chronological
$cco_rev = 1;  // Reverse

// Export delimiter constants
$ccsvd_sc = ';';    // Semi-colon
$ccsvd_tab = 'tab'; // Tab

// Todo sort order options constants
$cts_pri = 'todo_priority';  // Priority
$cts_dead = 'todo_deadline'; // Deadline

// Calendar Interval display constant
$ccal_4 = 4;  // 1/4 h
$ccal_2 = 2;  // 1/2 h
$ccal_1 = 1;  // 1 h
$l_cal_4 = '1/4';  // 1/4 h
$l_cal_2 = '1/2';  // 1/2 h
$l_cal_1 = '1';    // 1 h

// List modes
$clist_mode_normal = 0;
$clist_mode_expert = 1;

// TaskType caterogies (used by Project and time management) (field : internal)
$ctt_sales = '0';    // Sales Activity tasks
$ctt_research = '1'; // Internal R&D tasks
$ctt_others = '2';   // Tasks not related to production

// task_status flag
$c_task_notvalid = 0;    // task not validated
$c_task_uservalid = 1;   // validated by user (auto when day is full)
$c_task_adminvalid = 2;  // validated by admin

// Contract Kind
$cck_customer = 0;
$cck_supplier = 1;

// Contract Format
$ccf_period = 0;
$ccf_duration = 1;
$ccf_ticket = 2;

// Contract date ranges
$cr_date_tosign = 0;
$cr_date_tobegin = 1;
$cr_date_current = 2;
$cr_date_torenew = 3;
$cr_date_ended = 4;

// Document Kind
$cdoc_kind_dir = 0;
$cdoc_kind_file = 1;
$cdoc_kind_link = 2;

// Invoice status
$cinv_status['tocreate'] = 0;
$cinv_status['sent'] = 1;
$cinv_status['received'] = 2;
$cinv_status['ppaid'] = 3;
$cinv_status['paid'] = 4;
$cinv_status['conflict'] = 5;
$cinv_status['cancelled'] = 6;
$cinv_status['loss'] = 7;
$cinv_status['factor'] = 8;

// Incident status
$cincident_status['new'] = 0;
$cincident_status['open'] = 1;
$cincident_status['affected'] = 2;
$cincident_status['standby'] = 3;
$cincident_status['solved'] = 4;
$cincident_status['closed'] = 5;

// Perms XXXXX Deprecated : to be removed
$perms_user = 'user';
$perms_editor = 'editor';
$perms_admin = 'admin';

// DST (Daily Saving Time) correction to avoid dates problems (2h)
$c_dst_err = 7200;

// JavaScript and PHP Fields masks (PCRE regexp)
$js_regexp_phone="/^[0-9+x \(\)-\.]+$/"; // digits + SPACE ( ) - phone number
$php_regexp_phone="/^[0-9+x \(\)-\.]+$/";
$js_regexp_fax="/^[0-9+x \(\)-\.]+$/";   // digits + SPACE ( ) - fax number
$php_regexp_fax="/^[0-9+x \(\)-\.]+$/";
$js_regexp_zip="/^[0-9A-Z- ]{2,14}$/";     // 2-8 digits or letters
$php_regexp_zip="/^[0-9A-Z- ]{2,14}$/";
$js_regexp_web="/^(http:\/\/)?(https:\/\/)?[A-Za-z0-9\-]+(\.[A-Za-z0-9\-]+)*\.[A-Za-z]{2,3}([\-_\.\,\/A-Za-z0-9])*$/";  // Web
$php_regexp_web="/^(http:\/\/)?(https:\/\/)?[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[a-z]{2,3}([\-_\.\,\/A-Za-z0-9]*)$/i"; // Web
$php_regexp_isodate = "/^[0-9]{4}-[01][0-9]-[0123][0-9]$/";
$php_regexp_isodatetime = "/^[0-9]{4}-[01][0-9]-[0123][0-9] [012][0-9]:[0-5][0-9]:[0-5][0-9]$/";
$php_regexp_pub_lang = "/^[A-Za-z-]+$/";  // Publication lang
$js_regexp_list_name = "/^[0-9A-Za-z_\- ]+$/";  // List name
$js_regexp_import_name = "/^[0-9A-Za-z_\-\. ]+$/";  // Import name
$php_regexp_number = "/^[-]?[0-9]*$/"; // only numbers
$js_regexp_number = "/^([0-9][-]?)+$/"; // only numbers
$php_regexp_amount = "/^[0-9]+(\.[0-9][0-9]?)?$/";
$js_regexp_money = "/^[0-9]+(\\.[0-9][0-9]?)?$/"; // numbers and at most one '.'
$php_regexp_percent = "/^100$|^(\d{0,2})$/"; // 0-100
$php_regexp_backup = "/^obmdb-[0-9]{8}:[0-9]{6}-[0-9]{1,2}.[0-9]{1,2}(.[0-9]{1,2})?.dump$/";

if($cgp_use['service']['samba']) {
  $php_regexp_password = "/^[-\$\\\&~#{([|_`^@);\]+=}%!:\/\.,?<>\"\w0-9]{4,12}$/";
  $php_regexp_login = "/^[a-z0-9][a-z0-9-._]{0,31}$/";
} else {
  $php_regexp_password = "/^[-\$\\\&~#{([|_`^@);\]+=}%!:\/\.,?<>\"\w0-9]{4,20}$/";
  $php_regexp_login = "/^[a-z0-9][a-z0-9-._]{0,128}$/";
}

$php_regexp_groupname = "/^[\w0-9]([\w0-9-._ ]{0,252}[\w0-9]){0,1}$/"; // group name
$php_regexp_hostname = "/^[A-Za-z0-9][A-Za-z0-9-]{0,30}[A-Za-z0-9]$/"; // host
$php_regexp_fqdn = "/^[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,6}$/"; // Fqdn
$php_regexp_ip = '/^([1-2]?[0-9]{1,2}\.){3}[1-2]?[0-9]{1,2}$/'; // IP
$php_regexp_ip = '/^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/';
$php_regexp_network = '/^([1-2]?[0-9]{1,2}.){3}[1-2]?[0-9]{1,2}(\/[1-2]?[0-9])?$/'; // IP[/MASK]
$php_regexp_port_list ='/^[0-9]+(-[0-9]*){0,1}$/'; // Une liste de ports TCP/IP num[-num]
$php_regexp_network_port_list = '/^([1-2]?[0-9]{1,2}.){3}[1-2]?[0-9]{1,2}:[0-9]+(-[0-9]*){0,1}$/'; // IP[:num[-num]]

$php_regexp_samba_sid = '/^S-1-5-21-[0-9]{1,10}-[0-9]{1,10}-[0-9]{1,10}$/';
$php_regexp_samba_domain ='/^[aA-zZ0-9!@#$%^&()-_{}.~]{1,15}$/';
$php_regexp_samba_profile = "/^\\\\\\\\[a-zA-Z0-9-]{1,15}\\\\([a-zA-Z0-9_\\\\.-]{1,48})*$/";
$php_regexp_samba_home = "/^\\\\\\\\[a-zA-Z0-9-]{1,15}\\\\([a-zA-Z0-9_\\\\.-]{1,48}|%u)*$/";
$php_regexp_samba_drive = '/^[D-Z]?$/';




// Email
$php_regexp_domain = "/^([a-z0-9-]+\.)+[a-z]{2,6}$/"; // Domain
$php_regexp_email_name = "/^[a-zA-Z0-9!#\$%&\'*+\/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#\$%&\'*+\/=?^_`{|}~-]+)*$/";
$php_regexp_email = "/^[a-zA-Z0-9!#\$%&\'*+\/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#\$%&\'*+\/=?^_`{|}~-]+)*@([aA-zZ0-9-]+\.)+[aA-zZ]{2,6}$/";
$js_regexp_email = "/^[\w!#$%&'*+/=?^_`{|}~\-]+(\.[\w!#$%&'*+/=?^_`{|}~\-]+)?@([\w'\-]+\.)+[A-Za-z]{2,6}$/"; // Email
$php_regexp_quota = "/^[0-9]{1,6}$/";

// Samba
// Backslashes \ needs 2 backslash passes (PHP + regexp) so for "\" -> "\\\\"
$php_regexp_home = "/^\\\\\\\\[a-zA-Z0-9-]{1,15}\\\\[a-zA-Z0-9_\\\\.-]{1,48}$/";
$php_regexp_dflt_home = "/^\\\\\\\\[a-zA-Z0-9-]{1,15}\\\\([a-zA-Z0-9_\\\\.]|%u){1,48}$/";
$php_regexp_drive = "/^[D-Z]?$/";
// Virus log
$php_regexp_main_virus_db = "/^main\.cvd.*$/";
$php_regexp_daily_virus_db = "/^daily\.cvd.*$/";
$php_regexp_virus_db_update = "/^ClamAV update process started at.*$/";
$php_regexp_virus_db_version = "(version: (.*),)sU";

// Profile
$php_regexp_profile_name = "/^[A-Za-z0-9][A-Za-z0-9-._]{1,64}$/";
$js_regexp_profile_name = "/^[A-Za-z0-9][A-Za-z0-9-._]{1,64}$/";

$php_regexp_profile_level = "/^[0-9]+$/";
$php_regexp_profile_level_managepeers = "/^[01]$/";
$php_regexp_profile_access_restriction = "/^ALLOW_ALL|DENY_ALL$/"; // TODO
$php_regexp_profile_admin_realm = "/^[A-Za-z0-9][A-Za-z0-9-._ ]{1,64}$/";

// IMAP sub-folder
$php_regexp_imap_sub_folder = "/[\/@]+/";

$global_dis_debug_msg = '';
$global_dis_debug_param = '';

// Path to the OBM php root (if not set)
if ($path == '') {
  if ($module == '')
    $path = '.';
  else 
    $path = '..';
}

//those functions must be defined before the global_query and global display
//includes.
///////////////////////////////////////////////////////////////////////////////
// Display a debug message
// Parameters:
//   - $msg         : message to display
//   - $debug (opt) : debug flag required
///////////////////////////////////////////////////////////////////////////////
function display_debug_msg($msg, $debug=0, $function_name='') {
  global $global_dis_debug_msg,$cdg_sql;

  if (($_SESSION['set_debug'] > 0) && (($_SESSION['set_debug'] & $debug) == $debug)) {
    if($debug == $cdg_sql) {
      $msg = DB_OBM::xParser($msg);
    }
    $global_dis_debug_msg .= "<b>$function_name</b> : $msg<hr />";
  }

  return $global_dis_debug_msg;
}  

function display_debug_solr($msg, $debug=0, $function_name='') {
  global $global_dis_debug_solr, $cdg_solr;

  if (($_SESSION['set_debug'] > 0) && (($_SESSION['set_debug'] & $debug) == $debug)) {
    if($debug == $cdg_solr) {
      $msg = DB_OBM::xParser($msg);
    }
    $global_dis_debug_solr .= "<b>$function_name</b> : $msg<hr />";
  }

  return $global_dis_debug_solr;
}  

function display_debug_variables() {
  global  $global_dis_debug_custom;

  $vars = func_get_args();
  foreach($vars as $var) {
    $global_dis_debug_custom .='<pre>'.var_export($var,true).'</pre><hr />';
  }

  return $global_dis_debug_custom;
}  


///////////////////////////////////////////////////////////////////////////////
//Includes
///////////////////////////////////////////////////////////////////////////////
require_once("$obminclude/global_query.inc");
require_once("$obminclude/global_aliamin.inc");


///////////////////////////////////////////////////////////////////////////////
// Display parameters debug block
// Parameters:
//   - $params : parameters hash
///////////////////////////////////////////////////////////////////////////////
function display_debug_param($params) {
  global $cdg_param, $action, $global_dis_debug_param;

  if (debug_level_isset($cdg_param)) {
    $global_dis_debug_param .= "action=$action";
    if ( $params ) {
      foreach ($params as $key => $val) {
        if (is_array($val)) {
          $global_dis_debug_param .= "<br />params[$key]=";
	  $global_dis_debug_param .= print_r($val, true);
	} else {
          $global_dis_debug_param .= "<br />params[$key]=$val";
	}
      }
    }
  }

  return $global_dis_debug_param;
}  


/////////////////////////////////////////////////////////////////////////////
// Get a template file, add slashes to ", and return it in a string
// Parameters:
//   - $tpl : template name
/////////////////////////////////////////////////////////////////////////////
function get_template($tpl) {

  // could be replace by file_get_contents (but PHP >= 4.3.0)
  $fd = fopen($tpl, 'rb');
  $template = fread($fd, filesize($tpl));
  fclose($fd);
  $template = str_replace('"', '\"', $template);

  return $template;
}


///////////////////////////////////////////////////////////////////////////////
// Prepare an URL to be displayed in a link
// Perfoms various operations as escape '%', call sess->url,...
// Parameters:
//   - $url : URL string
// Returns:
//   - $ret_url : prepared URL
///////////////////////////////////////////////////////////////////////////////
function url_prepare($url) {

  $ret_url = $url;

  return $ret_url;
}


///////////////////////////////////////////////////////////////////////////////
// Test if the debug flag is set
// Parameters:
//   - $debug : debug flag
// Returns:
//   - true || false : true is flag set
///////////////////////////////////////////////////////////////////////////////
function debug_level_isset($debug) {

  if (($_SESSION['set_debug'] > 0) && (($_SESSION['set_debug'] & $debug) == $debug)) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Format a date according to user preference
// Parameters:
//   - $timestamp  : Unix timestamp
//   - $null_empty : tell if null should returns empty (true) or today ("")
// Returns:
//   date formated as user settings
///////////////////////////////////////////////////////////////////////////////
function of_date_format($timestamp='', $null_empty='') {
  global $set_date_default;

  $format = $_SESSION['set_date'];
  if (trim($format) == '') $format = $set_date_default;
  if ($timestamp != '') {
    if ($timestamp == '0') {
      $date = '';
    } else {
      $date = date($format, $timestamp);
    }
  } else {
    if ($null_empty != '') {
      $date = '';
    } else {
      $date = date($format);
    }
  }

  return $date;
}


/**
 * of_date_format
 * 
 * @param string $timestamp 
 * @param string $null_empty 
 * @access public
 * @return void
 */
function of_date_upd_format($date='', $null_empty=false) {
  
  if ($date) {
    $type = of_date_guess_format($date);
  } elseif (!$null_empty) {
    $date = time();
    $type = 't';
  } else {
    return null;
  }
  if ($type == 'user') {
    $type = $_SESSION['set_date_upd'];
  }
  switch ($type) {
  case 'Y-m-d' :
    preg_match("/^(\d{4})[-\/](\d{2})[-\/](\d{2})/",$date, $d);
    $date = new Of_Date("$d[1]-$d[2]-$d[3]");
    $date = $date->getInputDate();
    break;
  case 'd/m/Y' :
    preg_match("/^(\d{2})[-\/]?(\d{2})[-\/]?(\d{4})/",$date, $d);
    $date = new Of_Date("$d[3]-$d[2]-$d[1]");
    $date = $date->getInputDate();
    break;
  case 'm/d/Y' :
    preg_match("/^(\d{2})[-\/]?(\d{2})[-\/]?(\d{4})/",$date, $d);
    $date = new Of_Date("$d[3]-$d[1]-$d[2]");
    $date = $date->getInputDate();
    break;
  case 't' :
    $date = date($_SESSION['set_date_upd'], $date);
    break;
     
  default :
  }
  return $date;
}


/**
 * Guess the date format 
 *   case 't' :
    $date = date($_SESSION['set_date_upd'], $date);
    break;

 * @param mixed $date 
 * @access public
 * @return void
 */
function of_date_guess_format ($date) {

  $reg = array();
  $reg['Y-m-d'] = "/^[0-9]{4}[-\/]((0[1-9])|(1[012]))[-\/]((0[1-9])|([1-2][0-9])|(3[0-1]))$/";
  $reg['d/m/Y'] = "/^((1[3-9])|(2[0-9])|(3[0-1]))[-\/]?((0[1-9])|(1[012]))[-\/]?[0-9]{4}$/";
  $reg['m/d/Y'] = "/^((0[1-9])|(1[012]))[-\/]?((1[3-9])|(2[0-9])|(3[0-1]))[-\/]?[0-9]{4}$/";
  $reg['user'] = "/^((0[1-9])|(1[012]))[-\/]?((0[1-9])|(1[012]))[-\/]?[0-9]{4}$/";

  foreach($reg as $format => $regexp) {
    if(preg_match($regexp,$date)) {
      return "$format";
    }
  }
  // 31130000 is the lowest number which cannot be a date (31129999 => 31/12/9999)
  if(is_numeric($date) && $date > 31130000) {
    return 't';
  }
  return false;
}  


/**
 * of_isodate_convert 
 * 
 * @param mixed $date 
 * @param mixed $null_empty 
 * @access public
 * @return void
 */
function of_isodate_convert($date, $null_empty=false, $strict=false) {
  global $c_php_isodate_format;

  if ($date) {
    $type = of_date_guess_format($date);
  } elseif(!$null_empty) {
    $date = time();
    $type = 't';
  } else {
    return null;
  }
  if ($type == 'user') {
    $type = $_SESSION['set_date_upd'];
  }
  switch ($type) {
  case 'Y-m-d' :
    preg_match("/^(\d{4})[-\/](\d{2})[-\/](\d{2})/",$date, $d);
    $do = new Of_Date("$d[1]-$d[2]-$d[3]");
    $date = $do->getDateIso();
    break;    
  case 'd/m/Y' :
    preg_match("/^(\d{2})[-\/]?(\d{2})[-\/]?(\d{4})/",$date, $d);
    $do = new Of_Date("$d[3]-$d[2]-$d[1]");
    $date = $do->getDateIso();
    break;
  case 'm/d/Y' :
    preg_match("/^(\d{2})[-\/]?(\d{2})[-\/]?(\d{4})/",$date, $d);
    $do = new Of_Date("$d[3]-$d[1]-$d[2]");
    $date = $do->getDateIso();
    break;
  case 't' :
    $date = date($c_php_isodate_format, $date);
    break;
    
  default :
    if($strict) {
      $date = false;
    }
  } 
  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Iso Format a date
// Parameters:
//   - $timestamp  : Unix timestamp
//   - $null_empty : tell if null should returns empty (true) or today ('')
//   - $time_flag  : tell if time info should be returned (true)
// Returns:
//   datetime formated iso
///////////////////////////////////////////////////////////////////////////////
function of_isodate_format($timestamp='', $null_empty='', $time_flag=false) {
  global $c_php_isodate_format, $c_php_isodatetime_format;

  if ($time_flag) {
    $format = $c_php_isodatetime_format;
  } else {
    $format = $c_php_isodate_format;
  }
  if ($timestamp != '') {
    if ($timestamp == '0') {
      $date = '';
    } else {
      $date = date($format, $timestamp);
    }
  } else {
    if ($null_empty != '') {
      $date = '';
    } else {
      $date = date($format);
    }
  }

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Format a date-time according to user preference
// Parameters:
//   - $timestamp  : Unix timestamp
//   - $null_empty : tell if null should returns empty (true) or today ('')
// Returns:
//   datetime formated as user settings
///////////////////////////////////////////////////////////////////////////////
function of_datetime_format($timestamp='', $null_empty='') {
  global $set_date_default;

  $format = $_SESSION['set_date'];
  if (trim($format) == '') $format = $set_date_default;
  if ($timestamp != '') {
    if ($timestamp == '0') {
      $date = '';
    } else {
      $date = date("$format H:i:s", $timestamp);
    }
  } else {
    if ($null_empty != '') {
      $date = '';
    } else {
      $date = date("$format H:i:s");
    }
  }

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Get the timestamp of a date from its ISO format
// Parameters:
//   - $iso : date (Iso format)
// Returns:
//   timestamp of the date
///////////////////////////////////////////////////////////////////////////////
function isodatetime_to_timestamp($iso) {

  preg_match("/^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})/",$iso, $d);
  $ts = mktime($d[4],$d[5],$d[6],$d[2],$d[3],$d[1]);

  return $ts;
}


///////////////////////////////////////////////////////////////////////////////
// Return year portion of a datetime
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   year (1900-9999)
///////////////////////////////////////////////////////////////////////////////
function of_date_get_year($timestamp='') {

  if ($timestamp != '')
    $date = date('Y', $timestamp);
  else
    $date = date('Y');

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Return month portion of a datetime
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   month (1-12)
///////////////////////////////////////////////////////////////////////////////
function of_date_get_month($timestamp='') {

  if ($timestamp != '')
    $date = date('m', $timestamp);
  else
    $date = date('m');

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Return day portion of a datetime
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   day (1-31)
///////////////////////////////////////////////////////////////////////////////
function of_date_get_day($timestamp='') {

  if ($timestamp != '')
    $date = date('d', $timestamp);
  else
    $date = date('d');

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Return hour portion of a datetime
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   hour (0-23)
///////////////////////////////////////////////////////////////////////////////
function of_date_get_hour($timestamp='') {

  if ($timestamp != '')
    $date = date('H', $timestamp);
  else
    $date = date('H');

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Return min portion of a datetime
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   min (0-59)
///////////////////////////////////////////////////////////////////////////////
function of_date_get_min($timestamp='') {

  if ($timestamp != '')
    $date = date('i', $timestamp);
  else
    $date = date('i');

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Return full hour (ie. 08:00:00)
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   hour
///////////////////////////////////////////////////////////////////////////////
function of_date_get_full_hour($timestamp) {

  $hour = of_date_get_hour($timestamp).':'.of_date_get_min($timestamp);

  return $hour;
}


/**
 * of_date_get_first_day_week
 * Get the timestamp of the first day of the week of the date given
 *
 * @param mixed $date date in iso format 'yyyy-mm-dd' 
 * @param mixed $null_empty 
 * @access public
 * @return timestamp
 */
function of_date_get_first_day_week($iso_date) {
  global $c_week_first_day;

  $ts_date = strtotime($iso_date);
  $year = of_date_get_year($ts_date);
  $month = of_date_get_month($ts_date);
  $day = of_date_get_day($ts_date);
  $dow = date('w', $ts_date);
  // Get obm day of week (as obm week first day can be configured)
  $obm_dow = ($dow - $c_week_first_day + 7) % 7;
  $ts_weekstart = mktime(6, 0, 0, $month, $day - $obm_dow, $year);

  return ($ts_weekstart); 
}



///////////////////////////////////////////////////////////////////////////////
// Return date in letter
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   date
///////////////////////////////////////////////////////////////////////////////
function of_date_in_letter($timestamp) {
  global $l_daysofweek, $l_monthsofyear;

  $date = $l_daysofweek[date('w', $timestamp)%7].' '.
          date('d', $timestamp).' '.
          $l_monthsofyear[date('n', $timestamp)-1];

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Return the current lang used by the user
// Returns:
//   $lang
///////////////////////////////////////////////////////////////////////////////
function get_lang() {

  if ($_SESSION['set_lang'] != '')
    return strtoupper($_SESSION['set_lang']);
  else
    return 'EN';
}


///////////////////////////////////////////////////////////////////////////////
// Return OBM params
// Parameters :
// Returns:
//   $params : hash table which contains form params
///////////////////////////////////////////////////////////////////////////////
function get_global_params($table='') {
  global $fieldnames, $action, $popup;

  $order_dir_values = array('ASC', 'DESC');
  
  /*
    tf_  = textfield
    sel_ = select
    cb_  = checkbox
    cba_ = checkbox with auto handling
    ta_  = textarea
    rd_  = radio
    hd_  = hidden
    data-= external call
  */
  if (!is_array($params)) $params = array();
  $replace = array("/^tf_/", "/^sel_/", "/^cb_/", "/^ta_/", "/^rd_/", "/^hd_/");
  $parameters = array_merge($_GET,$_POST);

  // File
  if (isset ($_FILES['fi_file'])) {
    $params['file'] = $_FILES['fi_file']['tmp_name'];
    $params['file_name'] = $_FILES['fi_file']['name'];
    $params['file_size'] = $_FILES['fi_file']['size'];
    $params['file_type'] = $_FILES['fi_file']['type'];
  }

  foreach($parameters as $var => $value) {

    // Handle automatic checkbox (1,0) paramaters (cba_)
    $key = preg_replace("/^cba_/", '', $var);
    if ($key != $var) {
      $value = ($value == 1 ? 1 : 0);
      $params[$key] = $value;
      continue;
    }

    $key = preg_replace($replace,'',$var);

    // sql_order_dir value control
    if ($key == 'sql_order_dir') {
      if (!in_array($value, $order_dir_values)) {
        $value = $c_order_dir[0];
      }
    }

    // XXXX SQL_order below can't access here $fieldnames...
    // sql_order_field value control
    //    if ($key == 'sql_order_field') {
    //      if (!in_array($value, array_keys($fieldnames)))
    //        $value = '';
    //    }

    // XXXX SQL_order below can't access here $fieldnames...
    // sql_order_field value control
    if ($key == 'sql_order_field') {
      if (preg_match("/ /", $value))
	$value = '';
    }

    // auto decode urls
    if (preg_match('/_url$/', $key)) {
      $value = urldecode($value);
    }

    // external text parameters (title)
    if ($key == 'ext_title') {
      $value = stripslashes(urldecode($value));
    }

    // Add value in hash table
    if (is_array($value)) {
      $params[$key] = $value;
    } else if ($value != '') {
      // some tags can be in queries; eg: <
      if ($key == 'query') {
        $params[$key] = trim($value);
      } elseif (($key == 'passwd') || ($key == 'password') || ($key == 'old_password') || ($key == 'new_password') || ($key == 'new_password_bis')) {
        $params[$key] = trim($value);
      } else {
        // protection from xss vulnerabilities
        //TODO
        $params[$key] = trim(strip_tags($value));
      }
    }
  }

  // Session not here cause session not opened at this moment
/*  if (is_array($_SESSION)) {
      foreach($_SESSION as $var => $value) {
        if (is_array($value)) {
	  $params[$key] = $value;
        } else if ($value != '') {
	  $params[$key] = trim(strip_tags($value));
        }
      }
    } */

  $action = $params['action'];
  if ($action == '') {
    $action = 'index';
    $params['action'] = $action;
  }
  $popup = $params['popup'];
  
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Return the document related parameters in the array given
// Parameters:
//   - $mod_params
// Returns:
//   $lang
///////////////////////////////////////////////////////////////////////////////
function get_global_params_document(&$mod_params) {
  $nb_d = 0;
  $parameters = array_merge($_POST, $_GET);
  foreach($parameters as $key => $value) {
    if (strcmp(substr($key, 0, 4),'cb_d') == 0) {
      $nb_d++;
      $d_num = substr($key, 4);
      $mod_params["doc$nb_d"] = $d_num;
    }
  }
  if ($nb_d > 0) {
    $mod_params['doc_nb'] = $nb_d;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Parse and prepare the layout of a comment as a blog
// Separe comment in section displayed with different colors
// The secion separator is a line beginning with 'nnnn-nn-nn:aa:'
// Parameters:
//   - $com     : comment text to handle
//   - $p_order : Comment order (1 = reverse, else chronological)
//              : if not set, take $set_commentorder pref
///////////////////////////////////////////////////////////////////////////////
function beautify_comment($com, $p_order='') {
  global $cco_chro, $cco_rev;

  $col0 = 'FFFFFF';
  $col1 = 'BBBBBB';

  if ($p_order != '') {
    $order = $p_order;
  } else {
    $order = $_SESSION['set_commentorder'];
  }

  // Chronological Order
  if ($order == $cco_chro) {

    $pattern = "/(^|\n)([12][0-9]{3}-[0-1][0-9]-[0-3][0-9]( [012][0-9]:[0-5][0-9]:[0-5][0-9])?:.*:)/U";
    $replace = "<h1>\\2</h1>";

    $res = preg_replace($pattern, $replace, $com, 1);
    while (preg_match($pattern, $res)) {
      $nb++;
      $res = preg_replace($pattern, $replace, $res, 1);
    }
    $result = "
    <div class=\"comments\">  
      $res
    </div>";    
  } else {
  // reverse Chronological Order
    // pattern modifiers :
    // U : Ungreedy
    // s : "." matches all chars even newlines
    // D : $ matches only end of text, not end of line
    $pattern = "/(^|\n)([12][0-9]{3}-[0-1][0-9]-[0-3][0-9]( [012][0-9]:[0-5][0-9]:[0-5][0-9])?:.*:)(.*)($|((^|\n)([12][0-9]{3}-[0-1][0-9]-[0-3][0-9]( [012][0-9]:[0-5][0-9]:[0-5][0-9])?:.*:)))/UsD";
    $replace = "\\5";
    $tmp = $com;
    
    $nb = 0;
    while (preg_match($pattern, $tmp, $matches)) {
      $nb++;
      $who = $matches[2];
      $text = $matches[4];
      $row = "<h1>$who</h1>
              <p>$text</p>";
      $rows = $row . $rows;
      $tmp = preg_replace($pattern, $replace, $tmp, 1);
    }

    // If no time-user stamp match, we display the coment as is
    if ($nb == 0) {
      $rows = $tmp;
    }

    $result = "
    <div class=\"comments\">  
      $rows
    </div>";
  }

  return $result;
}


///////////////////////////////////////////////////////////////////////////////
// Check Privacy access
// if action has privacy enable, check if entity is visible by the user
// Parameters:
//   - $module : module to check (get $actions from)
//   - $table  : table (entity) to check (can be parentdeal for DEAL menu)
//   - $action : action to check
//   - $id     : entity id
//   - $p_uid  : user id to check access
// Returns true if entity is visible, else false
///////////////////////////////////////////////////////////////////////////////
function check_privacy($module, $table, $action, $id='', $p_uid='') {
  global $actions, $obm, $cdg_sql;

  // If action does not require privacy, grant access
  if (! $actions[$module][$action]['Privacy']) {
    return true;
  }

  // If id not set, grant access
  if ($id == '') {
    return true;
  }

  // Set uid if not given
  if ($p_uid == '') {
    $uid = $obm['uid'];
  } else {
    $uid = $p_uid;
  }
 
  $entity = strtolower($table);
  if ($entity == 'ugroup') { $entity = 'group'; }

  $field_pri = "${entity}_privacy";
  $field_uc = "${entity}_usercreate";
  $field_id = "${entity}_id";
  $id = sql_parse_id($id, true);

  $query = "SELECT $field_pri, $field_uc FROM $table WHERE $field_id $id";
  display_debug_msg($query, $cdg_sql, "${entity}_search()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  
  // If entity not found, grant access !
  if ($obm_q->num_rows() == 0) {
    return true;
  }
  $obm_q->next_record();

  // If entity is public or uid is the owner, grant access
  if ( ($obm_q->f("$field_pri") == 0)
    || ($obm_q->f("$field_uc") == $uid) ) {
    return true;
  } else {
    return false;
  }
}

function getMyId(){
  global $obm;
  return $obm['uid'];
}


///////////////////////////////////////////////////////////////////////////////
// Return the privacy select
// Parameters:
//   - $privacy : entry selected
// Returns:
//   privacy select
///////////////////////////////////////////////////////////////////////////////
function dis_privacy_search_select($privacy) {
  global $c_all, $l_all, $c_public, $l_public, $c_private, $l_private;

  $sel = "<select id=\"sel_privacy\" name=\"sel_privacy\">
<option value=\"$c_all\">$l_all</option>";
  $sel .= "\n<option value=\"$c_public\"";
  if ($privacy == $c_public) {
    $sel .= " selected=\"selected\"";
  }
  $sel .= ">$l_public</option>
<option value=\"$c_private\"";
  if ($privacy == $c_private) {
    $sel .= " selected=\"selected\"";
  }
  $sel .= ">$l_private</option>
</select>";

  return $sel;
}


///////////////////////////////////////////////////////////////////////////////
// Format a disk path to ensure it is correct: Add the final slash if necessary
// Parameters:
//   - $p_path : path parameter
// Returns:
//   correct path
///////////////////////////////////////////////////////////////////////////////
function format_path($p_path) {

  // Ensure that the path end with a /
  if (strrpos($p_path,'/') != strlen($p_path)-1) {
    $path = $p_path.'/';
  } else {
    $path = $p_path;
  }
  return $path; 
}


///////////////////////////////////////////////////////////////////////////////
// Transform a name in a simplified version according to parameters given
// Used for fields auto format
// Parameters:
//   - $name   : name to transform
//   - $up     : 0 : as is, 1 lower case, 2 upper case
//   - $accent : if true, suppress Accents
//   - $dot    : if true, suppress dot, -, '
// Returns:
//   - $result : name converted
///////////////////////////////////////////////////////////////////////////////
function format_name($name, $up, $accent, $dot=false) {

  $res = $name;

  if ($accent) {
    $res = htmlentities($res, ENT_COMPAT, 'UTF-8');
    $res = preg_replace('/&([a-zA-Z])(uml|acute|grave|circ|tilde|cedil|ring);/','$1',$res);
    $res = html_entity_decode($res);
  }

  if ($dot) {
    $res = preg_replace("/[\.\-\'\\\]/", '', $res);
  }

  if ($up == 1) {
    $res = strtolower($res);
  } else if ($up == 2) {
    $res = strtoupper($res);
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Auto transform Town (should become a HOOK)
// Parameters:
//   - $name : town name
// Returns:
//   $name formatted
///////////////////////////////////////////////////////////////////////////////
function get_format_town($name) {
  global $caf_town;

  $res = $name;
  if ($caf_town) {
    $res = strip_tags(format_name($res, 2, true, false));
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Wrapper around phonetic encoding (to ease phonetic algo change)
// Parameters:
//   - $name : name to encode
// Returns:
//   $res : phonetic encoded value of name
///////////////////////////////////////////////////////////////////////////////
function phonetic_key($name) {

  $res = metaphone($name);

  return $res;
}

////////////////////////////////////////////////////////////////////////////////
// Convert a php array to a javascript array
////////////////////////////////////////////////////////////////////////////////
function phpArrayToJsArray($var) {

  $jsArray = 'new Array(';
  if(is_array($var) && !empty($var)) {
   foreach($var as $val) {
      $jsArray .= "'$val',";
    }
  $jsArray = substr($jsArray, 0, -1);
  }
  $jsArray .= ')';

  return $jsArray;
}

////////////////////////////////////////////////////////////////////////////////
// Convert a php string to a javascript string
////////////////////////////////////////////////////////////////////////////////
function phpStringToJsString($var) {
  $jsString = str_replace("\n","\\n",addslashes($var));
  $jsString = str_replace('"','&quot;',$jsString);
  $jsString = str_replace("\r","",$jsString);
  return $jsString;
}


///////////////////////////////////////////////////////////////////////////////
// Create File query execution
// Parameters:
//   - $document[] : Entry's values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function run_query_global_create_document($params) {

  $id = $params['document_id'];
  $file = $params['file_tmp'];

  $disk_path = get_global_document_disk_path($id);
  $disk_fullname = $disk_path . $id; 

  // Ensure that the final path is a directory
  if (! is_dir($disk_path)) {
    return false;
  }

  if (is_writable($disk_path)) {
    if (!move_uploaded_file($file, $disk_fullname)) {
      return false;
    }
  } else {
    return false;
  }

  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// Delete File from Disk
// Parameters:
//   - $id : id of file to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_global_remove_document($id) {

  $disk_path = get_global_document_disk_path($id);
  $disk_fullname = $disk_path . $id; 

  @rmdir($disk_fullname);
  @unlink($disk_fullname);

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Calculate the real disk document path for a document
// Parameters:
//   - $id   : document id
// Returns:
//   real disk path
///////////////////////////////////////////////////////////////////////////////
function get_global_document_disk_path($id) {
  global $cdocument_root;

  // The document disk path set in the subdirectory named 'last id number'
  // Get the last number from Id
  $rel = substr($id, -1, 1);

  $disk_path = $cdocument_root . '/' . $rel . '/';
  
  return $disk_path; 
}


/**
 * Get the ip addresses from HTTP_X_FORWARDED_FOR and REMOTE_ADDR
 *
 * @access public 
 * @return array of ip adresses
 */
function get_global_ip_array() {
  $ip_list = getenv('HTTP_X_FORWARDED_FOR');
  $remote_addr = getenv('REMOTE_ADDR');
  if (!empty($remote_addr)) {
    if (!empty($ip_list))
      $ip_list.= ", ";
    $ip_list.= $remote_addr;
  }

  return array_map(trim, explode(',',$ip_list));
}


/** 
 * Test if the given ip address is in the given network/mask
 *
 * @params $ip: IP address to test
 *         $network: IP address of the network
 *          $mask: network mask
 * @access public
 * @returns boolean (true if the ip is in the network)
 */
function global_is_ip_in_network($ip,$network,$mask) {
  $b_ip   = ip2long($ip);
  $b_net  = ip2long($network);
  $b_mask = ip2long($mask);

  return ( $b_net == ($b_ip & $b_mask) );
}


/**
 * Test if the given ip address is in a network of the given list
 *
 * @params $ip: IP address to test
 *         $network_list: array of list(network, mask)
 * @access public
 * @return boolean (true if the ip is in a network of the list)
 */
function global_is_ip_in_network_list($ip,$network_list) {
  foreach ($network_list as $network) {
    list($net,$mask) = $network;
    if (global_is_ip_in_network($ip,$net,$mask))
      return true;
  }
  return false;
}


/** 
 * Convert a given network address to a list(network, mask)
 * example
 *  convert
 *    192.168.1 or 192.168.1.0/24 or 192.168.1.0/255.255.255.0
 *  to
 *    array('192.168.1.0', '255.255.255.0')
 *
 * @params $address: the network address to convert
 * @access public
 * @return array(network, mask)
 */
function global_convert_network_address($address) {
  list($ip, $mask) = array_map(trim, explode('/',$address));

  $t = array_pad( explode('.',$ip), 4, '0');
  $b_ip = ip2long( implode('.',$t) );
  if ( $b_ip==-1 || $b_ip===FALSE )
    return FALSE;

  if ($mask=='0') {
    return FALSE;

  // NO MASK
  } elseif (empty($mask)) {
    $cidr = ($t[3]==0 ? ($t[2]==0 ? ($t[1]==0 ? 8 : 16) : 24) : 32);
    $b_mask = 0xffffffff << (32 - $cidr);

  // CIDR
  } elseif (strpos($mask,'.')===FALSE && intval(trim($mask))<=32) {
    $cidr = intval(trim($mask));
    $b_mask = 0xffffffff << (32 - $cidr);

  // MASK
  } else {
    $t = array_pad( explode('.',$mask), 4, '0');
    $b_mask = ip2long( implode('.',$t) );
    if ( $b_mask==-1 || $b_mask===FALSE )
      return FALSE;

  }

  if ( $b_ip != ($b_ip & $b_mask) )
    return FALSE;

  return array( long2ip($b_ip), long2ip($b_mask) );
}

/**
 * Test if ip client if allowed to access
 * 
 * @access public
 * @param $perm user's permission
 * @return boolean: true if it's ok :) or false if access if forbidden
 */
function global_allow_access($perm) {
  global $profiles;

  $access_restriction = $profiles[$perm]['properties']['access_restriction'];
  $access_exceptions = $profiles[$perm]['properties']['access_exceptions'];

  if (empty($access_restriction)) {
    $access_restriction = "ALLOW_ALL";
  }

  $client_ip = get_global_ip_array();
  $except_list = array();
  if (is_array($access_exceptions)) {
    foreach($access_exceptions as $network_address) {
      if (($network = global_convert_network_address($network_address))!==FALSE)
        $except_list[] = $network;
    }
  }

  if (empty($except_list)) {
    $allowed = ($access_restriction=="ALLOW_ALL");

  } elseif ($access_restriction=="DENY_ALL") {
    // check that every ip in the $client_ip array are in the $access_exceptions
    $allowed = true;
    foreach ($client_ip as $ip) {
      if (!global_is_ip_in_network_list($ip,$except_list)) {
        $allowed = false;
        echo "\"$ip\"<br/>";
      }
    }

  } else {
    // check that every ip in the $client_ip array are not in the $access_exceptions
    $allowed = true;
    foreach ($client_ip as $ip) {
      if (global_is_ip_in_network_list($ip,$except_list))
        $allowed = false;
    }

  }

  return $allowed;
}


/**
 * Create an array of color.
 * 
 * @param mixed $nb amount of color
 * @access public
 * @return void
 */
function random_color($nb) {

  $colors[] = '238, 136, 0';
  $colors[] = '221, 68, 119';
  $colors[] = '204, 51, 51';
  $colors[] = '153, 68, 153';
  $colors[] = '102, 51, 204';
  $colors[] = '51, 102, 153';
  $colors[] = '51, 102, 204';
  $colors[] = '34, 170, 153';
  $colors[] = '50, 146, 98';
  $colors[] = '16, 150, 24';
  $colors[] = '102, 170, 0';
  $colors[] = '170, 170, 17';
  $colors[] = '214, 174, 0';
  $colors[] = '221, 85, 17';
  $colors[] = '168, 112, 112';
  $colors[] = '140, 109, 140';
  $colors[] = '98, 116, 135';
  $colors[] = '112, 131, 168';
  $colors[] = '92, 141, 135';
  $colors[] = '137, 137, 81';
  $colors[] = '176, 139, 89';
  $return = array();
  do {
    $return = array_merge($return, array_slice($colors,0,$nb));
    $nb -= 20;
  } while ($nb > 0);
     
  return $return;
}

function get_color_data($color) {
  if ($color) {
    $color = str_replace('#','', $color);
    return array('body' => '#'.$color, 'header' => '#'.changeBrightness($color));
  }
}

function changeBrightness ($hex, $adjust=-50) {
    $red   = hexdec( $hex[0] . $hex[1] );
    $green = hexdec( $hex[2] . $hex[3] );
    $blue  = hexdec( $hex[4] . $hex[5] );

    $cb = $red + $green + $blue;
    $adjust = $cb + $adjust;
    if ( $cb > $adjust ) {
        $db = ( $cb - $adjust ) % 255;

        $red -= $db; $green -= $db; $blue -= $db;
        if ( $red < 0 ) $red = 0;
        if ( $green < 0 ) $green = 0;
        if ( $blue < 0 ) $blue = 0;
    } else {
        $db = ( $adjust - $cb ) % 255;

        $red += $db; $green += $db; $blue += $db;
        if ( $red > 255 ) $red = 255;
        if ( $green > 255 ) $green = 255;
        if ( $blue > 255 ) $blue = 255;
    }

    return str_pad( dechex( $red ), 2, '0', 0 )
         . str_pad( dechex( $green ), 2, '0', 0 )
         . str_pad( dechex( $blue ), 2, '0', 0 );
}

if (!function_exists('quoted_printable_encode')) {
  function quoted_printable_encode($input) { 
    $hex = array('0','1','2','3','4','5','6','7', 
                          '8','9','A','B','C','D','E','F'); 
    $lines = preg_split("/(?:\r\n|\r|\n)/", $input); 
    $linebreak = "=0D=0A=\r\n"; 
    /* the linebreak also counts as characters in the mime_qp_long_line 
    * rule of spam-assassin */ 
    $escape = "="; 
    $output = ""; 
    $cur_conv_line = ""; 
    $whitespace_pos = 0; 
    $addtl_chars = 0; 
    
    // iterate lines 
    for ($j=0; $j<count($lines); $j++) { 
      $line = $lines[$j]; 
      $linlen = strlen($line); 
      
      // iterate chars 
      for ($i = 0; $i < $linlen; $i++) { 
        $c = substr($line, $i, 1); 
        $dec = ord($c); 
        
        if ($dec == 32) { 
          // space occurring at end of line, need to encode 
          if (($i == ($linlen - 1))) { 
            $c = "=20"; 
          } 
        
          $addtl_chars = 0; 
          $whitespace_pos = $i; 
        } elseif ( ($dec == 61) || ($dec < 32 ) || ($dec > 126) ) { 
          $h2 = floor($dec/16); $h1 = floor($dec%16); 
          $c = $escape . $hex["$h2"] . $hex["$h1"]; 
          $addtl_chars += 2; 
        } 
        
        $cur_conv_line .= $c; 
      }
      
      $whitespace_pos = 0; 
      $output .= $cur_conv_line; 
      $cur_conv_line = ""; 
      
      if ($j<=count($lines)-1) { 
        $output .= $linebreak; 
      } 
    }
    
    return trim($output); 
  }
}

function mb_chunk_split($str, $len, $glue) {
  mb_internal_encoding('UTF-8');

  $mbStrLen = mb_strlen($str);
  
  if ($mbStrLen == 0) {
    return false;
  } else {
    if ($mbStrLen <= $len) {
      return $str;
    } else if (strlen($str) == $mbStrLen) {
      return preg_replace("/(.{0," . $len . "})/", "\\0" . $glue, $str);
    } else {
      return mb_ereg_replace("(.{0," . $len . "})", "\\0" . $glue, $str);
    }
  }
}

function is_global_admin() {
  return $GLOBALS["obm"]["domain_global"];
}

?>
