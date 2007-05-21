<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global.inc                                                   //
//     - Desc : Global common OBM include file                               //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$obm_version = "2.1.0pre4";
$l_obm_title = "OBM";

// Id of the Publication reception type dedicated to the auto subscription.
$c_focsys = 3;

//---------------------------------------------------------------------------//
// OBM Default configurable Constants : Default values
// To change these values => change these in conf/obm_conf.inc 
//---------------------------------------------------------------------------//

// Global parameters --------------------------------------------------------//

// OBM host
$cgp_host = "http://obmlocal/";

// OBM default resources path
$resources_path = "/images";

// Multidomain
$c_multidomain = true;

// Todo lines to display
$cgp_todo_nb = 5;

// Popup Display size
$popup_width = 620;
$popup_height = 480;
$popup_contact_height = 600;

// Access Keys
$cak_prev = "P";
$cak_next = "N";
$cak_page_prev = "4";
$cak_page_next = "6";
$cak_begin = "B";
$cak_end = "E";

// Default domain id
$c_default_domain = 1;

// OBM System groups (groups id)
$cg_adm = 1;
$cg_com = 2;
$cg_prod = 3;

// Query limits (for user queries)
$cql_max_row = 50000;    // Maximum rows to be returned
$cql_max_cost = 300000;  // Maximum query cost (Postgres)

// Allow * in search field
$cgp_sql_star = true;

// Tuning constants
$ctu_sql_limit = false;

// MySQL (only 4.1+) charset used (for database and files !)
//$cmy_character_set = "_latin1";

// MySQL (only 4.1+) charset collation used (for database and files !)
//$cmy_charset_collation = "latin1_general_ci";

// is Mail enabled (is a mail server configured for OBM)
$cgp_mail_enabled = false;

// is Demo enabled (login screen array with default account)
$cgp_demo_enabled = true;

// Session Cookie lifetime (in seconds, 0=session cookie)
$cs_lifetime = 7200;

// Use Database sessions
$cgp_sess_db = false;

// Cookie name and domain
$cgp_cookie_name = "OBM_Session";
$cgp_cookie_domain = "aliasource.local";

// authentification : 'CAS' (SSO AliaSuite) or 'standalone' (default)
// CAS specific parameters are in conf/obm_conf.inc.
$auth_kind = "standalone"; 

// Encyption used to store password : "PLAIN", "MD5" or "CRYPT"
$password_encryption = "PLAIN";

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

// Calendar : show public groups
$ccalendar_public_groups = true;
// Calendar : First day of the week
$phpversion = substr(phpversion(),0,1);
if ($phpversion >= 5) {
  $ccalendar_weekstart = "monday";
} else {
  $ccalendar_weekstart = "monday + 2 hours"; // +2h to fix a DST php4 bug //
}
// Calendar : First day of the week (+2h to fix a DST php bug)
//$ccalendar_weekstart = "monday + 2 hours";
// Calendar max user displayed in the calendar
$ccalendar_max_users = 25;
// Calendar first and last hour display
$ccalendar_first_hour = 8;
$ccalendar_last_hour = 20;
// Calendar Resource usage
$ccalendar_resource = true;

// Time : In how many parts we split up a day (Time management)
$c_day_fraction = 8;

// Time : Worked days in a week (start at sunday)
$c_working_days = array(0,1,1,1,1,1,0);

// Time : Day beginning a week (0=Sunday, 1=Monday,...)
$c_week_first_day = 1;

// Incident : Generic E-mail for Incident module
$cmail_incident = "";

// Document : constants
$cdocument_root = "/var/www/docs";           // Root of the document repository
$default_path = "/Default";                  // Default directory
$default_mime = "application/octet-stream";  // Default Mime Type

// Group : is a new group private by default
$cgroup_private_default = true;

// Import : temporary path
$tmp_path = "/tmp";

// default taxes values
$cdefault_tax = array ("TVA 19,6" => 1.196, "TVA 5,5" => 1.055, "Pas de TVA" => 1);

//---------------------------------------------------------------------------//
// Default access rights bits meanings
// 0 - read  : data
// 1 - own   : owner data
// 2 - write : data
// 3 - read  : admin 
// 4 - write : admin 
$cright_read = 1;
$cright_own = 2;
$cright_write = 4;
$cright_read_admin = 8;
$cright_write_admin = 16;

// reader : 01 (bit 0) : can see data
// user   : 03 (bits 0+1) : can see data + insert or update his own data
// editor : 05 (bits 0+2) : can see data + write data
// admin  : 1D (bits 0+2+3+4) : can do everything
$perm_reader = "01";
$perm_user = "03";
$perm_editor = "05";
$perm_admin = "1D";

//------------------------------//
// Default Profiles definitions //
//------------------------------//
$profiles["user"] = array (
  'section' => array (
    'default' => 0,
    'com' => 1,
    'prod' => 1,
    'user' => 1,
    'my' => 1),
  'module' => array (
    'default' => $perm_user)
);

$profiles["editor"] = array (
  'section' => array (
    'default' => 0,
    'com' => 1,
    'prod' => 1,
    'user' => 1,
    'my' => 1),
  'module' => array (
    'default' => $perm_editor)
);

$profiles["admin"] = array (
  'section' => array (
    'default' => 1),
  'module' => array (
    'default' => $perm_admin,
    'domain' => 0
    )
);
//-------------------------//
// Default Displayed Infos //
//-------------------------//
// sections -- Urls
$cgp_show["section"]["com"]["url"] = "$path/company/company_index.php";
$cgp_show["section"]["prod"]["url"] = "$path/time/time_index.php";
$cgp_show["section"]["compta"]["url"] = "$path/invoice/invoice_index.php";
$cgp_show["section"]["user"]["url"] = "$path/settings/settings_index.php";
$cgp_show["section"]["admin"]["url"] = "$path/admin/admin_index.php?mode=html";
$cgp_show["section"]["dic"]["url"] = "$path/admin_ref/admin_ref_index.php?mode=html";
$cgp_show["section"]["my"]["url"] = "$path/settings/settings_index.php";
//$cgp_show["section"]["aliamin"]["url"] = "http://aliamin/";

// modules
$cgp_show["module"]["company"] = "com";
$cgp_show["module"]["contact"] = "com";
$cgp_show["module"]["lead"] = "com";
$cgp_show["module"]["deal"] = "com";
$cgp_show["module"]["list"] = "com";
$cgp_show["module"]["calendar"] = "com";
$cgp_show["module"]["cv"] = "false";
$cgp_show["module"]["todo"] = "com";
$cgp_show["module"]["publication"] = "com";
$cgp_show["module"]["statistic"] = "com";
$cgp_show["module"]["time"] = "prod";
$cgp_show["module"]["project"] = "prod";
$cgp_show["module"]["contract"] = "prod";
$cgp_show["module"]["incident"] = "prod";
$cgp_show["module"]["document"] = "prod";
$cgp_show["module"]["invoice"] = "compta";
$cgp_show["module"]["payment"] = "compta";
$cgp_show["module"]["account"] = "compta";
$cgp_show["module"]["user"] = "user";
$cgp_show["module"]["group"] = "user";
$cgp_show["module"]["resource"] = "user";
$cgp_show["module"]["resourcegroup"] = "user";
$cgp_show["module"]["host"] = "user";
$cgp_show["module"]["mailshare"] = "user";
$cgp_show["module"]["tools"] = "admin";
$cgp_show["module"]["control"] = "admin";
$cgp_show["module"]["admin"] = "admin";
$cgp_show["module"]["import"] = "admin";
$cgp_show["module"]["backup"] = "admin";
$cgp_show["module"]["mailserver"] = "admin";
$cgp_show["module"]["samba"] = "admin";
$cgp_show["module"]["admin_pref"] = "dic";
$cgp_show["module"]["admin_data"] = "dic";
$cgp_show["module"]["admin_lang"] = "dic";
$cgp_show["module"]["admin_code"] = "dic";
$cgp_show["module"]["admin_ref"] = "dic";
$cgp_show["module"]["settings"] = "my";
$cgp_show["module"]["password"] = "my";
$cgp_show["module"]["mailforward"] = "my";
$cgp_show["module"]["vacation"] = "my";
$cgp_show["module"]["mailbox"] = "my";

// Display domain module only when multidomain is activate
if ($c_multidomain) {
  $cgp_show["module"]["domain"] = "admin";
}

// OBM-LDAP, OBM-Mail, OBM-Windows values

//------------------//
// Services enabled //
//------------------//
$cgp_use["service"]["web"] = false;
$cgp_use["service"]["web_local"] = false;
$cgp_use["service"]["mail"] = false;
$cgp_use["service"]["mail_nomad"] = false;
$cgp_use["service"]["mail_vacation"] = false;
$cgp_use["service"]["samba"] = false;
$cgp_use["service"]["ldap"] = false;
$cgp_use["service"]["dns"] = false;
$cgp_use["service"]["firewall"] = false;
$cgp_use["service"]["network"]  = false;
	   
// UID : First User
$c_first_uid = 1001;
// First GID for simple users - first GID for group creation
$c_first_gid_user = 1001;
// First GID for admin users - by default, simple and admin users have same
$c_first_gid_admin = $c_first_gid_user;

// OBM-Windows Samba Groups
$cg_gid_smb_admin = 512;
$cg_gid_smb_user = 513;
$cg_gid_smb_guest = 514;
$cg_gid_smb_host = 515;

//---------------------------------------------------------------------------//
// END : OBM Default configurable Constants : Default values
//---------------------------------------------------------------------------//

///////////////////////////////////////////////////////////////////////////////
// Recuperation des parametres du fichier de configuration global obm_conf.ini
///////////////////////////////////////////////////////////////////////////////
// Code repris du fichier www/aliainclude/global.inc d'Aliamin

$ini_array = parse_ini_file("$path/../conf/obm_conf.ini");

$obmdb_host = $ini_array["host"];
$obmdb_dbtype = $ini_array["dbtype"]; 
$obmdb_db = $ini_array["db"];
$obmdb_user = $ini_array["user"];
$obmdb_password = $ini_array["password"];

///////////////////////////////////////////////////////////////////////////////
// Fin du chargement
///////////////////////////////////////////////////////////////////////////////

require_once("conf/obm_conf.inc");
require_once("$obminclude/phplib/obmlib.inc");

define("C_IMAGE_PATH",$cgp_host."images");

// Update password encryptation in uppercase
$password_encryption = strtoupper($password_encryption);

// Automate commands
$cmd_halt = "sudo /sbin/halt";
$cmd_update = "../$path/auto/update.pl";
$cmd_check_update = "ps aux | grep auto/update.pl | grep -v grep";
$cmd_db_update = "../$path/auto/dbUpdate.pl --valid";
$cmd_db_cancel = "../$path/auto/dbUpdate.pl --cancel";
$cmd_mailforward = "../$path/auto/mailChangeSieve.pl";
$cmd_vacation = "../$path/auto/mailChangeSieve.pl";
$cmd_ldap_passwd = "../$path/auto/ldapChangePasswd.pl";

$section = $cgp_show["module"][$module];

//-----------------------------//
// Calculated Enabled services //
//-----------------------------//
if (! $cgp_use["service"]["mail"]) {
  $cgp_use["service"]["mail_nomad"] = false;
  $cgp_use["service"]["mail_vacation"] = false;
  $cgp_show["module"]["mailserver"] = false;
  $cgp_show["module"]["mailforward"] = false;
  $cgp_show["module"]["vacation"] = false;
  $cgp_show["module"]["mailshare"] = false;
  $cgp_show["module"]["mailbox"] = false;
}

if (! $cgp_use["service"]["samba"]) {
  $cgp_show["module"]["samba"] = false;
}

if ((! $cgp_use["service"]["mail"]) && (! $cgp_use["service"]["samba"])) {
  $cgp_show["module"]["host"] = false;
}

if (! $cgp_use["service"]["web"]) {
  $cgp_use["service"]["web_local"] = false;
}

//----------------------------//
// Calculated Displayed Infos //
//----------------------------//
if (! $cgp_show["module"]["company"]) {
  $cgp_show["module"]["deal"] = false;
  $cgp_show["module"]["project"] = false;
  $cgp_show["module"]["contract"] = false;
  $cgp_show["module"]["invoice"] = false;
}

if (! $cgp_show["module"]["contact"]) {
  $cgp_show["module"]["list"] = false;
  $cgp_hide["company"]["company_contact_number"] = true;
  $cgp_hide["company"]["company_new_contact"] = true;
}

if (! $cgp_show["module"]["project"]) {
  $cgp_show["module"]["time"] = false;
}

if (! $cgp_show["module"]["contract"]) {
  $cgp_show["module"]["incident"] = false;
}

if (! $cgp_show["module"]["invoice"]) {
  $cgp_show["module"]["payment"] = false;
}

if (! $cgp_show["module"]["deal"]) {
  $cgp_hide["company"]["company_deal_number"] = true;
}

if ($cgp_hide["company"]["company_address3"]) {
  $cgp_hide["contact"]["contact_address3"] = true;
  $cgp_hide["list_contact"]["address3"] = true;
  $cgp_hide["list_contact_2"]["address3"] = true;
}

if ($cgp_hide["company"]["company_expresspostal"]) {
  $cgp_hide["contact"]["contact_expresspostal"] = true;
  $cgp_hide["list_contact"]["expresspostal"] = true;
  $cgp_hide["list_contact_2"]["expresspostal"] = true;
}

if ($cgp_hide["contact"]["function"]) {
  $cgp_hide["list_contact"]["function"] = true;
  $cgp_hide["list_contact_2"]["function"] = true;
}

//---------------------------------------------------------------------------//
// OBM authentication Management : 
// Just change $cas_version according to your phpCAS lib
//---------------------------------------------------------------------------//
if ($auth_kind == "CAS") {
  $auth_class_name = "CAS_Auth";
} elseif ($auth_kind == "standalone") {
  $auth_class_name = "OBM_Challenge_Auth";
} else {
  echo "unknown Authentication kind !<br/>";
  echo "please edit \$auth_kind in conf/obm_conf.inc";
  exit;
}

//---------------------------------------------------------------------------//
// OBM internal Constants : Do not change 
//---------------------------------------------------------------------------//

// Global Constants
$c_all = "_ALL_";           // Used in Form select
$c_undef = "0";             // Used in Form select
$c_none = "-1";             // Used in Form select
$c_null = "NULL";           // Used in queries (date is null)
$c_auto = "_AUTO_";
$c_yes = "Yes";	            // Maybe stored in database
$c_no = "No";
$c_public = "__PUBLIC__";
$c_private = "__PRIVATE__";

// Debug Constants
$cdg_id = 1;     // id display
$cdg_param = 2;  // parameter display
$cdg_sess = 4;   // session display
$cdg_sql = 8;    // sql query display
$cdg_exe = 16;   // program execution

// Date constants
$c_php_isodate_format = "Y-m-d";  // AAAA-MM-JJ iso format for php date()
$c_php_isodatetime_format = "Y-m-d H:i:s";  // AAAA-MM-JJ H:m:s iso format
$cda_iso = "Y-m-d"; // Iso format
$cda_en = "m/d/Y";  // us format
$cda_fr = "d/m/Y";  // fr format
$cda_txt = "d M Y"; // txt format

$cda_upd_dmy = "dmy";  // day month year format
$cda_upd_mdy = "mdy"; // month day year format

// Menu display options constants
$cme_txt = "text";  // text
$cme_ico = "icon";  // icon
$cme_both = "both"; // both

// Comment dislay order
$cco_chro = 0; // Chronological
$cco_rev = 1;  // Reverse

// Export delimiter constants
$ccsvd_sc = ";";    // Semi-colon
$ccsvd_tab = "tab"; // Tab

// Todo sort order options constants
$cts_pri = "todo_priority";  // Priority
$cts_dead = "todo_deadline"; // Deadline

// Calendar Interval display constant
$ccal_4 = 4;  // 1/4 h
$ccal_2 = 2;  // 1/2 h
$ccal_1 = 1;  // 1 h
$l_cal_4 = "1/4";  // 1/4 h
$l_cal_2 = "1/2";  // 1/2 h
$l_cal_1 = "1";    // 1 h

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

// Perms XXXXX Deprecated : to be removed
$perms_user = "user";
$perms_editor = "editor";
$perms_admin = "admin";

// DST (Daily Saving Time) correction to avoid dates problems (2h)
$c_dst_err = 7200;

// Default preferences data
$set_lang_default = "en";
$set_theme_default = "default";
$set_menu_default = $cme_both;
$set_display_default = "no";
$set_rows_default = 10;
$set_todo_default = 'todo_priority';
$set_date_upd_default = $cda_dmy;
$set_date_default = $cda_iso;
$set_commentorder_default = $cco_chro;
$set_debug_default = 0;
$set_cal_interval_default = 2;

// JavaScript and PHP Fields masks (PCRE regexp)
$js_regexp_phone="/^[0-9+x \(\)-\.]+$/"; // digits + SPACE ( ) - phone number
$php_regexp_phone="/^[0-9+x \(\)-\.]+$/";
$js_regexp_fax="/^[0-9+x \(\)-\.]+$/";   // digits + SPACE ( ) - fax number
$php_regexp_fax="/^[0-9+x \(\)-\.]+$/";
$js_regexp_zip="/^[0-9A-Z- ]{2,14}$/";     // 2-8 digits or letters
$php_regexp_zip="/^[0-9A-Z- ]{2,14}$/";
$js_regexp_web="/^(http:\/\/)?(https:\/\/)?[A-Za-z0-9\-]+(\.[A-Za-z0-9\-]+)*\.[A-Za-z]{2,3}([\-_\.\/A-Za-z0-9])*$/";  // Web
$php_regexp_web="/^(http:\/\/)?(https:\/\/)?[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[a-z]{2,3}([\-_\.\/A-Za-z0-9]*)$/i"; // Web
$php_regexp_isodate = "/^[0-9]{4}-[01][0-9]-[0123][0-9]$/";
$php_regexp_isodatetime = "/^[0-9]{4}-[01][0-9]-[0123][0-9] [012][0-9]:[0-5][0-9]:[0-5][0-9]$/";
$php_regexp_pub_lang = "/^[A-Za-z-]+$/";  // Publication lang
$js_regexp_list_name = "/^[0-9A-Za-z_\- ]+$/";  // List name
$js_regexp_import_name = "/^[0-9A-Za-z_\-\. ]+$/";  // Import name
$php_regexp_number = "/^[-]?[0-9]*$/"; // only numbers
$js_regexp_number = "/^([0-9][-]?)+$/"; // only numbers
$php_regexp_amount = "/^[0-9]+(\.[0-9][0-9]?)?$/";
$js_regexp_money = "/^[0-9]+(\\.[0-9][0-9]?)?$/"; // numbers and at most one '.'
$php_regexp_percent = "/^[1]?[0-9]?[0-9]$/"; // 0-100
$php_regexp_backup = "/^obmdb-[0-9]{8}:[0-9]{6}-[0-9]{1,2}.[0-9]{1,2}(.[0-9]{1,2})?.dump$/";

$php_regexp_password = "/^.{4,12}$/";

$php_regexp_groupname = "/^[A-Za-z0-9][A-Za-z0-9-._]{1,31}$/"; // group name
$php_regexp_hostname = "/^[A-Za-z0-9][A-Za-z0-9-]{0,30}[A-Za-z0-9]$/"; // host
$php_regexp_fqdn = "/^[a-z0-9-]+(\.[a-z0-9-]+)*\.[a-z]{2,6}$/"; // Fqdn
$php_regexp_ip = '/^([1-2]?[0-9]{1,2}\.){3}[1-2]?[0-9]{1,2}$/'; // IP
$php_regexp_network = '/^([1-2]?[0-9]{1,2}.){3}[1-2]?[0-9]{1,2}(\/[1-2]?[0-9])?$/'; // IP[/MASK]
$php_regexp_port_list ='/^[0-9]+(-[0-9]*){0,1}$/'; // Une liste de ports TCP/IP num[-num]
$php_regexp_network_port_list = '/^([1-2]?[0-9]{1,2}.){3}[1-2]?[0-9]{1,2}:[0-9]+(-[0-9]*){0,1}$/'; // IP[:num[-num]]

// Login
$php_regexp_login = "/^[A-Za-z0-9][A-Za-z0-9-._]{1,31}$/";

// Email
$php_regexp_domain = "/^([a-z0-9-]+\.)+[a-z]{2,6}$/"; // Domain
$php_regexp_localdomain = "/^([a-z0-9-]+\.)+[a-z]{2,6}$/"; // Local Domain
$php_regexp_email_name = "/^[a-zA-Z0-9_&\'-]+(\.[a-zA-Z0-9_\'-]+)*$/";
$php_regexp_email = "/^[a-zA-Z0-9_&\'-]+(\.[a-zA-Z0-9_\'-]+)*@([a-z0-9-]+\.)+[a-z]{2,6}$/";
$js_regexp_email="/^\w([\.'\w-_&]+)?@([\w'\-]+\.)+[A-Za-z]{2,4}$/";  // Email
$php_regexp_quota = "/^[0-9]{1,6}$/";

// Samba
// Backslashes \ needs 2 backslash passes (PHP + regexp) so for "\" -> "\\\\"
$php_regexp_home = "/^\\\\\\\\[a-zA-Z0-9-]{1,32}\\\\[a-zA-Z0-9_\\\\.-]{1,48}$/";
$php_regexp_dflt_home = "/^\\\\\\\\[a-zA-Z0-9-]{1,32}\\\\([a-zA-Z0-9_\\\\.]{1,48}|%u)$/";
$php_regexp_drive = "/^[D-Z]?$/";
// Virus log
$php_regexp_main_virus_db = "/^main\.cvd.*$/";
$php_regexp_daily_virus_db ="/^daily\.cvd.*$/";
$php_regexp_virus_db_update = "/^ClamAV update process started at.*$/";
$php_regexp_virus_db_version = "(version: (.*),)sU";


$global_dis_debug_msg = "";
$global_dis_debug_param = "";

// Path to the OBM php root (if not set)
if ($path == "") {
  if ($module == "")
    $path = "";
  else 
    $path = "..";
}


//those functions must be defined before the global_query and global display
//includes.
///////////////////////////////////////////////////////////////////////////////
// Display a debug message
// Parameters:
//   - $msg         : message to display
//   - $debug (opt) : debug flag required
///////////////////////////////////////////////////////////////////////////////
function display_debug_msg($msg, $debug=0, $function_name="") {
  global $set_debug, $global_dis_debug_msg;

  if (($set_debug > 0) && (($set_debug & $debug) == $debug)) {
    $global_dis_debug_msg .= "<b>$function_name</b> : $msg<hr />";
  }
  return $global_dis_debug_msg;
}  


function display_debug_variables() {
  global  $global_dis_debug_custom;

  $vars = func_get_args();
  foreach($vars as $var) {
    $global_dis_debug_custom .="<pre>".var_export($var,true)."</pre><hr />";
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
  $fd = fopen($tpl, "rb");
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
  global $set_debug;

  if (($set_debug > 0) && (($set_debug & $debug) == $debug)) {
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
function of_date_format($timestamp="", $null_empty="") {
  global $set_date_default, $set_date;

  $format = $set_date;
  if (trim($format) == "") $format = $set_date_default;
  if ($timestamp != "") {
    if ($timestamp == "0") {
      $date = "";
    } else {
      $date = date($format, $timestamp);
    }
  } else {
    if ($null_empty != "") {
      $date = "";
    } else {
      $date = date($format);
    }
  }

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Iso Format a date
// Parameters:
//   - $timestamp  : Unix timestamp
//   - $null_empty : tell if null should returns empty (true) or today ("")
//   - $time_flag  : tell if time info should be returned (true)
// Returns:
//   datetime formated iso
///////////////////////////////////////////////////////////////////////////////
function of_isodate_format($timestamp="", $null_empty="", $time_flag=false) {
  global $c_php_isodate_format, $c_php_isodatetime_format;

  if ($time_flag) {
    $format = $c_php_isodatetime_format;
  } else {
    $format = $c_php_isodate_format;
  }
  if ($timestamp != "") {
    if ($timestamp == "0") {
      $date = "";
    } else {
      $date = date($format, $timestamp);
    }
  } else {
    if ($null_empty != "") {
      $date = "";
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
//   - $null_empty : tell if null should returns empty (true) or today ("")
// Returns:
//   datetime formated as user settings
///////////////////////////////////////////////////////////////////////////////
function of_datetime_format($timestamp="", $null_empty="") {
  global $set_date_default, $set_date;

  $format = $set_date;
  if (trim($format) == "") $format = $set_date_default;
  if ($timestamp != "") {
    if ($timestamp == "0") {
      $date = "";
    } else {
      $date = date("$format H:i:s", $timestamp);
    }
  } else {
    if ($null_empty != "") {
      $date = "";
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
function of_date_get_year($timestamp="") {

  if ($timestamp != "")
    $date = date("Y", $timestamp);
  else
    $date = date("Y");

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Return month portion of a datetime
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   month (1-12)
///////////////////////////////////////////////////////////////////////////////
function of_date_get_month($timestamp="") {

  if ($timestamp != "")
    $date = date("m", $timestamp);
  else
    $date = date("m");

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Return day portion of a datetime
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   day (1-31)
///////////////////////////////////////////////////////////////////////////////
function of_date_get_day($timestamp="") {

  if ($timestamp != "")
    $date = date("d", $timestamp);
  else
    $date = date("d");

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Return hour portion of a datetime
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   hour (0-23)
///////////////////////////////////////////////////////////////////////////////
function of_date_get_hour($timestamp="") {

  if ($timestamp != "")
    $date = date("H", $timestamp);
  else
    $date = date("H");

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Return min portion of a datetime
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   min (0-59)
///////////////////////////////////////////////////////////////////////////////
function of_date_get_min($timestamp="") {

  if ($timestamp != "")
    $date = date("i", $timestamp);
  else
    $date = date("i");

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Return the current lang used by the user
// Returns:
//   $lang
///////////////////////////////////////////////////////////////////////////////
function get_lang() {
  global $set_lang;

  if ($set_lang != "")
    return strtoupper($set_lang);
  else
    return "EN";
}


///////////////////////////////////////////////////////////////////////////////
// Return OBM params
// Parameters :
// Returns:
//   $params : hash table which contains form params
///////////////////////////////////////////////////////////////////////////////
function get_global_params($table="") {
  global $fieldnames, $action,$popup;

  $order_dir_values = array("ASC", "DESC");
  
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
  foreach($parameters as $var => $value) {
    if ($parameters["ajax"] == 1 && is_string($value)) {
      $value = utf8_decode($value);
    }
    // Handle automatic checkbox (1,0) paramaters (cba_)
    $key = preg_replace("/^cba_/", "", $var);
    if ($key != $var) {
      $value = ($value == 1 ? 1 : 0);
      $params[$key] = $value;
      continue;
    }

    $key = preg_replace($replace,"",$var);

    // sql_order_dir value control
    if ($key == "sql_order_dir") {
      if (!in_array($value, $order_dir_values)) {
        $value = $c_order_dir[0];
      }
    }

    // XXXX SQL_order below can't access here $fieldnames...
    // sql_order_field value control
    //    if ($key == "sql_order_field") {
    //      if (!in_array($value, array_keys($fieldnames)))
    //        $value = "";
    //    }

    // XXXX SQL_order below can't access here $fieldnames...
    // sql_order_field value control
    if ($key == "sql_order_field") {
      if (preg_match("/ /", $value))
	$value = "";
    }

    // auto decode urls
    if (preg_match('/_url$/', $key)) {
      $value = urldecode($value);
    }

    // external text parameters (title)
    if ($key == "ext_title") {
      $value = stripslashes(urldecode($value));
    }

    // Add value in hash table
    if (is_array($value)) {
      $params[$key] = $value;
    } else if ($value != "") {
      // some tags can be in queries; eg: <
      if ($key == "query") {
	$params[$key] = trim($value);
      } else {
	$params[$key] = trim(strip_tags($value));
      }
    }
  }

  // Session not here cause session not opened at this moment
/*  if (is_array($_SESSION)) {
      foreach($_SESSION as $var => $value) {
        if (is_array($value)) {
	  $params[$key] = $value;
        } else if ($value != "") {
	  $params[$key] = trim(strip_tags($value));
        }
      }
    } */

  $action = $params["action"];
  if ($action == "") $action = "index";
  $popup = $params["popup"];
  
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
    if (strcmp(substr($key, 0, 4),"cb_d") == 0) {
      $nb_d++;
      $d_num = substr($key, 4);
      $mod_params["doc$nb_d"] = $d_num;
    }
  }
  if ($nb_d > 0) {
    $mod_params["doc_nb"] = $nb_d;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Parse and prepare the layout of a comment as a blog
// Separe comment in section displayed with different colors
// The secion separator is a line beginning with "nnnn-nn-nn:aa:"
// Parameters:
//   - $com     : comment text to handle
//   - $p_order : Comment order (1 = reverse, else chronological)
//              : if not set, take $set_commentorder pref
///////////////////////////////////////////////////////////////////////////////
function beautify_comment($com, $p_order="") {
  global $cco_chro, $cco_rev, $set_commentorder;

  $col0 = "FFFFFF";
  $col1 = "BBBBBB";

  if ($p_order != "") {
    $order = $p_order;
  } else {
    $order = $set_commentorder;
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
function check_privacy($module, $table, $action, $id="", $p_uid="") {
  global $actions, $obm, $cdg_sql;

  // If action does not require privacy, grant access
  if (! $actions[$module][$action]["Privacy"]) {
    return true;
  }

  // If id not set, grant access
  if ($id == "") {
    return true;
  }

  // Set uid if not given
  if ($p_uid == "") {
    $uid = $obm["uid"];
  } else {
    $uid = $p_uid;
  }
 
  $entity = strtolower($table);
  if ($entity == "ugroup") { $entity = "group"; }

  $field_pri = "${entity}_privacy";
  $field_uc = "${entity}_usercreate";
  $field_id = "${entity}_id";

  $query = "SELECT $field_pri, $field_uc FROM $table WHERE $field_id = '$id'";
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
    $res = preg_replace("/[����]/i", "e", $name);
    $res = preg_replace("/[����]/i", "E", $res);
    $res = preg_replace("/[��]/i", "a", $res);
    $res = preg_replace("/[��]/i", "A", $res);
    $res = preg_replace("/[��]/i", "i", $res);
    $res = preg_replace("/[��]/i", "I", $res);
    $res = preg_replace("/[��]/i", "o", $res);
    $res = preg_replace("/[��]/i", "O", $res);
    $res = preg_replace("/[���]/i", "u", $res);
    $res = preg_replace("/[���]/i", "U", $res);
    $res = preg_replace("/[�]/i", "c", $res);
    $res = preg_replace("/[�]/i", "C", $res);
    $res = preg_replace("/[�]/i", "s", $res);
    $res = preg_replace("/[�]/i", "S", $res);
  }

  if ($dot) {
    $res = preg_replace("/[\.\-\'\\\]/", "", $res);
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

  $jsArray = "new Array(";
  if(is_array($var)) {
   foreach($var as $val) {
      $jsArray .= "'$val',";
    }
  $jsArray = substr($jsArray, 0, -1);
  }
  $jsArray .= ")";

  return $jsArray;
}

////////////////////////////////////////////////////////////////////////////////
// Convert a php string to a javascript string
////////////////////////////////////////////////////////////////////////////////
function phpStringToJsString($var) {
  $jsString = str_replace("\n","\\n",addslashes($var));
  $jsString = str_replace("\r","",$jsString);
  return $jsString;
}

///////////////////////////////////////////////////////////////////////////////
// Cette fonction permet de générer la liste des hôtes dans une boîte de
// sélection avec prise en compte de l'hôte par défaut
// Parametre :
//      mail_server : identifiant du serveur à sélectionner
///////////////////////////////////////////////////////////////////////////////
function get_select_host_server($mail_server="",$domain="") {
    $mail = run_query_get_mailserver($domain);

    // On construit une boite de selection
    // La valeur par défaut est le serveur par défaut, sauf si $mail_server
    // est positionné
    $dis_mailserver = "
    <select name=\"sel_mail_server\">";
    while($mail->next_record()) {
      $name = $mail->f("host_name");
      $id = $mail->f("mailserver_id");
      if($id == $mail_server) {
        $dis_mailserver .= "<option selected=\"selected\" value=\"$id\">$name</option>";
      } else {
        $dis_mailserver .= "<option value=\"$id\">$name</option>";
      }
    }
    $dis_mailserver .= "
    </select>";

    return $dis_mailserver;
}


</script>
