<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global.inc                                                   //
//     - Desc : Global common OBM include file                               //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$obm_version = "0.7.1pre1";
$l_title = "O.B.M.";

// Bookmarks default
$last_deal_default = 0;
$last_parentdeal_default = 0;
$last_company_default = 0;
$last_contact_default = 0;
$last_contract_default = 0;
$last_invoice_default = 0;
$last_account_default = 0;
$last_payment_default = 0;
$last_contract_default = 0;
$last_incident_default = 0;

// Document constants 
$link_to = "Link : "; // Name of the prefix of a link document
$default_mime = "application/octet-stream"; //Default Mime Type
$default_path = "/Default"; //Default repository

// Agenda constants
$set_weekstart_default = "monday"; //First day of the week, in english
$set_start_time = '8'; // first hour of the day
$set_stop_time = '20'; // last hour of the day

// Timemanager module CONSTANTS
// In how many parts we can chop a day
$c_day_fraction = 8;
// How many days we want to fill for each week:
// 5 : Mon -> Fri; 6 : Mon -> Sat... (not tested for anything else than 5...
//  and 5 could be hard coded at some places...
$c_days_in_a_week = 5;
// We define a working week to cope with all cases of working week 
//  for us, we are working from Monday to Friday, so : (week start at Sun)
// $c_days_in_a_week= 5 will become absolete
$working_days = array(0,1,1,1,1,1,0);
// task_status flag
// task not valided by the user : $c_task_notvalid
// task valided by user : $c_task_uservalid (automatically done when a day is full)
// task valided by admin : $c_task_adminvalid
$c_task_notvalid = 0;
$c_task_uservalid = 1;
$c_task_adminvalid = 2;

// Menu display options constants
$cme_txt = "text";  // text
$cme_ico = "icon";  // icon
$cme_both = "both"; // both

// Date constants
$c_php_isodate_format = "Y-m-d";  // AAAA-MM-JJ iso format for php date()
$cda_iso = "Y-m-d"; // Iso format
$cda_en = "m/d/Y";  // us format
$cda_fr = "d/m/Y";  // fr format
$cda_txt = "d M Y"; // txt format

// Comment dislay order
$cco_chro = 0; // Chronological
$cco_rev = 1;  // Reverse

// Calendar Interval display constant
$ccal_4 = 4;  // 1/4 h
$ccal_2 = 2;  // 1/2 h
$ccal_1 = 1;  // 1 h
$l_cal_4 = "1/4";  // 1/4 h
$l_cal_2 = "1/2";  // 1/2 h
$l_cal_1 = "1";    // 1 h

// Export delimiter constants
$ccsvd_sc = ";";   // Semi-colon
$ccsvd_tab = "tab"; // Tab

// Perms
$perms_user = "user" ;
$perms_editor = "editor" ;
$perms_admin = "admin" ;

// Debug Constants
$cdg_id = 1;     // id display
$cdg_param = 2;  // parameter display
$cdg_sess = 4;   // session display
$cdg_sql = 8;    // sql query display

// Global Constants
$c_all = "_ALL_";		// Used in Form select
$c_auto = "_AUTO_";
$c_yes = "Yes";			// Maybe stored in database
$c_no = "No";

// Mail.
$gp_mail_enabled = 0;

// Display size
$popup_width = 620;
$popup_height = 400;
$popup_contact_height = 600;

// JavaScript and PHP Fields masks (regexp)
$js_regexp_phone="/^[0-9+ \(\)-\.]+$/"; // digits + SPACE ( ) - in phone number
$php_regexp_phone="^[0-9+ \(\)-\.]+$";  // digits + SPACE ( ) - in phone number
$js_regexp_fax="/^[0-9+ \(\)-\.]+$/";   // digits + SPACE ( ) - in fax number
$php_regexp_fax="^[0-9+ \(\)-\.]+$";    // digits + SPACE ( ) - in fax number
$js_regexp_zip="/^[0-9A-Z]{4,8}$/";     // 4-8 digits or letters
$php_regexp_zip="^[0-9A-Z]{4,8}$";      // 4-8 digits or letters
$js_regexp_email="/^\w([\.\w-_]+)?@([\w\-]+\.)+[A-Za-z]{2,3}$/";  // Email
$php_regexp_email="^[a-z0-9_-]+(\.[a-z0-9_-]+)*@([a-z0-9-]+\.)+[a-z]{2,3}$"; // Email
$php_regexp_email_name="/^[a-z0-9_-]+(\.[a-z0-9_-]+)*$/"; // Email name
$js_regexp_web="/^[A-Za-z0-9\-]+(\.[A-Za-z0-9\-]+)*\.[A-Za-z]{2,3}$/";  // Web
$php_regexp_web="^[a-z0-9-]+(\.[a-z0-9-]+)*\.[a-z]{2,3}$"; // Web
$php_regexp_isodate = "^[0-9]{4}-[01][0-9]-[0123][0-9]$";
$js_regexp_list_name = "/^[0-9A-Za-z_-]+$/";  //Name of a list
$js_regexp_number = "/^([0-9][-]?)+$/"; // only numbers
$php_regexp_number = "/^[0-9]*$/"; // only numbers
$js_regexp_money = "/^[0-9]+(\\.[0-9][0-9]?)?$/"; // numbers and at most one '.'
$php_regexp_lifetime = "^[0-9]{1,6}$";


// Perms and Profil
///////////////////////////////////////////////////////////////////////////////
//                         COMMERCIAL RIGTHS                                 //
///////////////////////////////////////////////////////////////////////////////
$com_read 		= 1;
$com_write 		= 2;
$com_admin_read 	= 4;
$com_admin_write	= 8;
			///////////
			//COMPANY//
			///////////
$company_read 		= $com_read;
$company_write 		= $com_read + $com_write ;
$company_admin_read 	= $com_admin_read;
$company_admin_write	= $com_admin_read + $com_admin_write;
			///////////
			//CONTACT//
			///////////
$contact_read 		= $com_read;
$contact_write 		= $com_read + $com_write ;
$contact_admin_read 	= $com_admin_read;
$contact_admin_write	= $com_admin_read + $com_admin_write;
			///////////
			// DEAL  //
			///////////
$deal_read 		= $com_read;
$deal_write 		= $com_read + $com_write ;
$deal_admin_read 	= $com_admin_read;
$deal_admin_write	= $com_admin_read + $com_admin_write;
			///////////
			// LIST  //
			///////////
$list_read 		= $com_read;
$list_write 		= $com_read + $com_write ;
$list_admin_read 	= $com_admin_read;
$list_admin_write	= $com_admin_read + $com_admin_write;
			///////////
			//AGENDA //
			///////////
$agenda_read 		= $com_read;
$agenda_write 		= $com_read + $com_write ;
$agenda_admin_read 	= $com_admin_read;
$agenda_admin_write	= $com_admin_read + $com_admin_write;
			///////////
			// TODO  //
			///////////
$todo_read 		= $com_read;
$todo_write 		= $com_read + $com_write ;
$todo_admin_read 	= $com_admin_read;
$todo_admin_write	= $com_admin_read + $com_admin_write;

///////////////////////////////////////////////////////////////////////////////
//                         PRODUCTION RIGTHS                                 //
///////////////////////////////////////////////////////////////////////////////
$prod_read 		= 16;
$prod_write 		= 32;
$prod_admin_read	= 64;
$prod_admin_write	= 128;
			////////////
			//TIME    //
			////////////
$time_read		= $prod_read;
$time_write	 	= $prod_read + $prod_write;
$time_admin_read 	= $prod_admin_read;
$time_admin_write	= $prod_admin_read + $prod_admin_write;
			////////////
			//PROJECT //
			////////////
$project_read		= $prod_read;
$project_write	 	= $prod_read + $prod_write;
$project_admin_read 	= $prod_admin_read;
$project_admin_write	= $prod_admin_read + $prod_admin_write;
			////////////
			//CONTRACT//
			////////////
$contract_read		= $prod_read;
$contract_write 	= $prod_read + $prod_write;
$contract_admin_read 	= $prod_admin_read;
$contract_admin_write	= $prod_admin_read + $prod_admin_write;
			////////////
			//INCIDENT//
			////////////
$incident_read		= $prod_read;
$incident_write 	= $prod_read + $prod_write;
$incident_admin_read 	= $prod_admin_read;
$incident_admin_write	= $prod_admin_read + $prod_admin_write;
			////////////
			//DOCUMENT//
			////////////
$document_read		= $prod_read;
$document_write 	= $prod_read + $prod_write;
$document_admin_read 	= $prod_admin_read;
$document_admin_write	= $prod_admin_read + $prod_admin_write;

///////////////////////////////////////////////////////////////////////////////
//                         COMPTA RIGTHS                                     //
///////////////////////////////////////////////////////////////////////////////
$compta_read 		= 256;
$compta_write 		= 512;
$compta_admin_read 	= 1024;
$compta_admin_write 	= 2048;
			////////////
			//ACCOUNT //
			////////////
$account_read		= $compta_read;
$account_write 	= $compta_read + $compta_write;
$account_admin_read 	= $compta_admin_read;
$account_admin_write	= $compta_admin_read + $compta_admin_write;
			////////////
			//INVOICE //
			////////////
$invoice_read		= $compta_read;
$invoice_write	 	= $compta_read + $compta_write;
$invoice_admin_read 	= $compta_admin_read;
$invoice_admin_write	= $compta_admin_read + $compta_admin_write;
			////////////
			//PAYMENT //
			////////////
$payment_read		= $compta_read;
$payment_write	 	= $compta_read + $compta_write;
$payment_admin_read 	= $compta_admin_read;
$payment_admin_write	= $compta_admin_read + $compta_admin_write;

///////////////////////////////////////////////////////////////////////////////
//                         USERS RIGTHS                                      //
///////////////////////////////////////////////////////////////////////////////
$users_read 		= 4096;
$users_write 		= 8192;
$users_admin_read	= 16384;
$users_admin_write	= 32768;
			////////////
			// USER   //
			////////////
$user_read		= $users_read;
$user_write	 	= $users_read + $users_write;
$user_admin_read 	= $users_admin_read;
$user_admin_write	= $users_admin_read + $users_admin_write;
			////////////
			// GROUP  //
			////////////
$group_read		= $users_read;
$group_write	 	= $users_read + $users_write;
$group_admin_read 	= $users_admin_read;
$group_admin_write	= $users_admin_read + $users_admin_write;
			////////////
			//SETTINGS//
			////////////
$settings_read		= $users_read;
$settings_write	 	= $users_read + $users_write;
$settings_admin_read 	= $users_admin_read;
$settings_admin_write	= $users_admin_read + $users_admin_write;

///////////////////////////////////////////////////////////////////////////////
//                        ADMIN RIGTHS                                       //
///////////////////////////////////////////////////////////////////////////////
$admins_read		= 65536;
$admins_write		= 131072;
$admins_admin_read	= 262144;
$admins_admin_write	= 524288;
			////////////
			//ADMIN   //
			////////////
$admin_read		= $admins_read;
$admin_write	 	= $admins_read + $admins_write;
$admin_admin_read 	= $admins_admin_read;
$admin_admin_write	= $admins_admin_read + $admins_admin_write;		
			//////////////
			//ADMIN CODE//
			//////////////
$admin_code_read	= $admins_read;
$admin_code_write 	= $admins_read + $admins_write;
$admin_code_admin_read 	= $admins_admin_read;
$admin_code_admin_write	= $admins_admin_read + $admins_admin_write;	
			//////////////
			//ADMIN DATA//
			//////////////
$admin_data_read	= $admins_read;
$admin_data_write 	= $admins_read + $admins_write;
$admin_data_admin_read 	= $admins_admin_read;
$admin_data_admin_write	= $admins_admin_read + $admins_admin_write;	
			//////////////
			//ADMIN PREF//
			//////////////
$admin_pref_read	= $admins_read;
$admin_pref_write 	= $admins_read + $admins_write;
$admin_pref_admin_read 	= $admins_admin_read;
$admin_pref_admin_write	= $admins_admin_read + $admins_admin_write;	

///////////////////////////////////////////////////////////////////////////////
//                       PROFILES DEFINITIONS                                //
///////////////////////////////////////////////////////////////////////////////
$perm_user   =  $com_read + $prod_read + $users_read ;
$perm_editor =  $com_read + $prod_read + $users_read + $com_write +
	        $prod_write + $users_write;
$perm_admin = $com_read + $com_write + $com_admin_read + $com_admin_write +
      $prod_read + $prod_write + $prod_admin_read + $prod_admin_write +
      $compta_read + $compta_write + $compta_admin_read + $compta_admin_write +
      $users_read + $users_write + $users_admin_read + $users_admin_write +
      $admins_read + $admins_write + $admins_admin_read + $admins_admin_write;


// Path to the OBM php root (if not set)
if ($path == "") {
  if ($menu == "")
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
  global $set_debug;

  if (! empty($function_name))
	$function_name .= " : ";
  if (($set_debug > 0) && (($set_debug & $debug) == $debug)) {
    echo "$function_name : $msg<hr>";
  }
}  


///////////////////////////////////////////////////////////////////////////////
//Includes
///////////////////////////////////////////////////////////////////////////////
require_once("$obminclude/global_query.inc");
require_once("$obminclude/global_display.inc");


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
  global $sess;

  $ret_url = $sess->url($url);

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
// Format a date according to user preference                                //
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   date formated as user settings
///////////////////////////////////////////////////////////////////////////////
function date_format($timestamp="") {
  global $set_date_default, $set_date;

  $format = $set_date;
  if (trim($format) == "") $format = $set_date_default;
  if ($timestamp != "")
    $date = date($format, $timestamp);
  else
    $date = date($format);

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Iso Format a date                                                         //
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   datetime formated iso
///////////////////////////////////////////////////////////////////////////////
function isodate_format($timestamp="") {
  global $c_php_isodate_format;

  if ($timestamp != "")
    $date = date($c_php_isodate_format, $timestamp);
  else
    $date = date($c_php_isodate_format);

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Format a date-time according to user preference                           //
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   datetime formated as user settings
///////////////////////////////////////////////////////////////////////////////
function datetime_format($timestamp="") {
  global $set_date_default, $set_date;

  $format = $set_date;
  if (trim($format) == "") $format = $set_date_default;
  if ($timestamp != "")
    $date = date("$format H:i:s", $timestamp);
  else
    $date = date("$format H:i:s");

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Return hour portion of a datetime                                         //
// Parameters:
//   - $timestamp : Unix timestamp
// Returns:
//   hour (0-23)
///////////////////////////////////////////////////////////////////////////////
function get_hour($timestamp="") {

  if ($timestamp != "")
    $date = date("H", $timestamp);
  else
    $date = date("H");

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Dispaly an entity dates and users creation / update infos
// Parameters:
//   - $uc_id : usercreate id
//   - $uu_id : userupdate id
//   - $tc    : timecreate
//   - $tu    : timeupdate
///////////////////////////////////////////////////////////////////////////////
function display_record_info($uc_id, $uu_id, $tc, $tu) {
  global $set_theme, $ico_created, $ico_updated;

  $obm_q = new DB_OBM;
  $query_created = "select userobm_login from UserObm where userobm_id='$uc_id'";
  $query_updated = "select userobm_login from UserObm where userobm_id='$uu_id'";

  if ($uc_id != "") {
    $obm_q->query($query_created);
    $obm_q->next_record();
    $created = "<img src=\"/images/$set_theme/$ico_created\" alt=\"\" /> ".$obm_q->f("userobm_login")." : ".datetime_format($tc);
  }
  if ($uu_id != "") {
    $obm_q->query($query_updated);
    $obm_q->next_record();
    $updated = "<img src=\"/images/$set_theme/$ico_updated\" alt=\"\" /> " . $obm_q->f("userobm_login") . " : ". datetime_format($tu);
  }

  $block = "<div class=\"detailInfo\">$created<br />$updated
  </div>";

  return $block;
}



//functions used by many modules
function month_name ($num_month) {
 
  global $l_january,$l_february,$l_march,$l_april,$l_may,$l_june,$l_july,$l_august,$l_september,$l_october,$l_november,$l_december,$l_unknown;

  switch($num_month) {
    case 1 : $month_name=$l_january;     break; 
    case 2 : $month_name=$l_february;    break; 
    case 3 : $month_name=$l_march;       break;
    case 4 : $month_name=$l_april;       break;
    case 5 : $month_name=$l_may;         break;
    case 6 : $month_name=$l_june;        break;
    case 7 : $month_name=$l_july;        break;
    case 8 : $month_name=$l_august;      break;
    case 9 : $month_name=$l_september;   break; 
    case 10 : $month_name=$l_october;    break;
    case 11 : $month_name=$l_november;   break;
    case 12 : $month_name=$l_december;   break;
    default : $month_name=$l_unknown;    break;    
  }

  return $month_name;
}


function month_short_name ($num_month) {
 
  global $l_short_january,$l_short_february,$l_short_march,$l_short_april,$l_short_may,$l_short_june,$l_short_july,$l_short_august,$l_short_september,$l_short_october,$l_short_november,$l_short_december,$l_unknown;
  
  switch($num_month) {
  case 1 : $month_name=$l_short_january;      break;
    case 2 : $month_name=$l_short_february;   break;
    case 3 : $month_name=$l_short_march;      break;
    case 4 : $month_name=$l_short_april;      break;
    case 5 : $month_name=$l_short_may;        break;
    case 6 : $month_name=$l_short_june;       break;
    case 7 : $month_name=$l_short_july;       break;
    case 8 : $month_name=$l_short_august;     break;
    case 9 : $month_name=$l_short_september;  break; 
    case 10 : $month_name=$l_short_october;   break;
    case 11 : $month_name=$l_short_november;  break;
    case 12 : $month_name=$l_short_december;  break;
    default : $month_name=$l_unknown;         break;    
  }

  return $month_name;
}


</SCRIPT>
