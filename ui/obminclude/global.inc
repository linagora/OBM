<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global.inc                                                   //
//     - Desc : Global common OBM include file                               //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


$obm_version = "0.8.7";
$l_obm_title = "O.B.M.";

// Document constants 
$link_to = "Link : "; // Deprecated but necessary for update from older version
                     //Name of the prefix of a link document
$default_mime = "application/octet-stream"; // Default Mime Type
$default_path = "/Default"; // Default repository

// Import constants
$tmp_path = "/tmp";

// Agenda constants
$set_weekstart_default = "monday + 2 hours"; // First day of the week, 
//english The +2 hours is a quick fix
//for a bug php (see dst bug in bugzilla.aliacom.fr)

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
//Id of the Publication reception type which is dedicated to the auto
//subscription.
$c_focsys = 3;

//---------------------------------------------------------------------------//
// OBM Default configurable Constants : Default values
// To change site values => change these in obm_conf.inc 
//---------------------------------------------------------------------------//

// OBM host (for mail links)
$cgp_host = "http://obm/";

// Generic E-mail for Incident module
$cmail_incident = "";

// Query limits (for user queries)
$cql_max_row = 50000;    // Maximum rows to be returned
$cql_max_cost = 100000;  // Maximum query cost (Postgres)

// Popup Display size
$popup_width = 620;
$popup_height = 480;
$popup_contact_height = 600;

// OBM System groups (groups id)
$cg_adm = 1;
$cg_com = 2;
$cg_prod = 3;

// Use Database sessions
$cgp_sess_db = false;

// Tuning constants
$ctu_sql_limit = false;

// Todo lines to display
$cgp_todo_nb = 5;

//-------------------------//
// Default Displayed Infos //
//-------------------------//
// sections -- Urls
$cgp_show["section"]["com"] = "$path/company/company_index.php";
$cgp_show["section"]["prod"] = "$path/time/time_index.php";
$cgp_show["section"]["compta"] = "$path/account/account_index.php";
$cgp_show["section"]["user"] = "$path/settings/settings_index.php";
$cgp_show["section"]["admin"] = "$path/admin/admin_index.php?mode=html";
$cgp_show["section"]["aliamin"] = "http://aliamin/";

// modules
$cgp_show["module"]["company"] = true;
$cgp_show["module"]["contact"] = true;
$cgp_show["module"]["deal"] = true;
$cgp_show["module"]["list"] = true;
$cgp_show["module"]["agenda"] = true;
$cgp_show["module"]["todo"] = true;
$cgp_show["module"]["publication"] = true;
$cgp_show["module"]["statistic"] = true;
$cgp_show["module"]["time"] = true;
$cgp_show["module"]["project"] = true;
$cgp_show["module"]["contract"] = true;
$cgp_show["module"]["incident"] = true;
$cgp_show["module"]["document"] = true;
$cgp_show["module"]["account"] = true;
$cgp_show["module"]["invoice"] = true;
$cgp_show["module"]["payment"] = true;
$cgp_show["module"]["settings"] = true;
$cgp_show["module"]["user"] = true;
$cgp_show["module"]["group"] = true;

require_once("$obminclude/obm_conf.inc");
require_once("$obminclude/phplib/obmlib.inc");

//----------------------------//
// Calculated Displayed Infos //
//----------------------------//
if (! $cgp_show["module"]["deal"]) {
  $cgp_hide["company"]["company_deal_number"] = true;
}

if (! $cgp_show["module"]["contact"]) {
  $cgp_hide["company"]["company_contact_number"] = true;
  $cgp_hide["company"]["company_new_contact"] = true;
}

//---------------------------------------------------------------------------//
// OBM internal Constants : Do not change 
//---------------------------------------------------------------------------//

// TaskType caterogies (used by Project and time management) (field : internal)
$ctt_sales = '0';    // Sales Activity tasks
$ctt_research = '1'; // Internal R&D tasks
$ctt_others = '2';   // Tasks not related to production

// Menu display options constants
$cme_txt = "text";  // text
$cme_ico = "icon";  // icon
$cme_both = "both"; // both

// Todo sort order options constants
$cts_pri = "todo_priority";  // Priority
$cts_dead = "todo_deadline"; // Deadline

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
$ccsvd_sc = ";";    // Semi-colon
$ccsvd_tab = "tab"; // Tab

// Perms XXXXX Deprecated : to be removed
$perms_user = "user";
$perms_editor = "editor";
$perms_admin = "admin";

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

// JavaScript and PHP Fields masks (PCRE regexp)
$js_regexp_phone="/^[0-9+ \(\)-\.]+$/"; // digits + SPACE ( ) - in phone number
$php_regexp_phone="/^[0-9+ \(\)-\.]+$/";
$js_regexp_fax="/^[0-9+ \(\)-\.]+$/";   // digits + SPACE ( ) - in fax number
$php_regexp_fax="/^[0-9+ \(\)-\.]+$/";
$js_regexp_zip="/^[0-9A-Z ]{2,8}$/";     // 2-8 digits or letters
$php_regexp_zip="/^[0-9A-Z ]{2,8}$/";
$js_regexp_email="/^\w([\.\w-_]+)?@([\w\-]+\.)+[A-Za-z]{2,3}$/";  // Email
$php_regexp_email="/^[a-z0-9_-]+(\.[a-z0-9_-]+)*@([a-z0-9-]+\.)+[a-z]{2,3}$/i"; // Email
$php_regexp_email_name="/^[a-z0-9_-]+(\.[a-z0-9_-]+)*$/"; // Email name
$js_regexp_web="/^(http:\/\/)?(https:\/\/)?[A-Za-z0-9\-]+(\.[A-Za-z0-9\-]+)*\.[A-Za-z]{2,3}([\-\/A-Za-z0-9])*$/";  // Web
$php_regexp_web="/^(http:\/\/)?(https:\/\/)?[a-z0-9-]+(\.[a-z0-9-]+)*\.[a-z]{2,3}([\-\/A-Za-z0-9]*)$/i"; // Web
$php_regexp_isodate = "/^[0-9]{4}-[01][0-9]-[0123][0-9]$/";
$js_regexp_list_name = "/^[0-9A-Za-z_- ]+$/";  // List name
$js_regexp_import_name = "/^[0-9A-Za-z_- ]+$/";  // Import name
$js_regexp_number = "/^([0-9][-]?)+$/"; // only numbers
$php_regexp_number = "/^[0-9]*$/"; // only numbers
$js_regexp_money = "/^[0-9]+(\\.[0-9][0-9]?)?$/"; // numbers and at most one '.'
$php_regexp_lifetime = "/^[0-9]{1,6}$/";


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

  if (($set_debug > 0) && (($set_debug & $debug) == $debug)) {
    echo "<b>$function_name</b> : $msg<hr>";
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
//   - $timestamp  : Unix timestamp
//   - $null_empty : tell if null should returns empty (true) or today ("")
// Returns:
//   date formated as user settings
///////////////////////////////////////////////////////////////////////////////
function date_format($timestamp="", $null_empty="") {
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
// Iso Format a date                                                         //
// Parameters:
//   - $timestamp  : Unix timestamp
//   - $null_empty : tell if null should returns empty (true) or today ("")
// Returns:
//   datetime formated iso
///////////////////////////////////////////////////////////////////////////////
function isodate_format($timestamp="", $null_empty="") {
  global $c_php_isodate_format;

  if ($timestamp != "") {
    if ($timestamp == "0") {
      $date = "";
    } else {
      $date = date($c_php_isodate_format, $timestamp);
    }
  } else {
    if ($null_empty != "") {
      $date = "";
    } else {
      $date = date($c_php_isodate_format);
    }
  }

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Format a date-time according to user preference                           //
// Parameters:
//   - $timestamp  : Unix timestamp
//   - $null_empty : tell if null should returns empty (true) or today ("")
// Returns:
//   datetime formated as user settings
///////////////////////////////////////////////////////////////////////////////
function datetime_format($timestamp="", $null_empty="") {
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
// Return the document related parameters in the array given
// Parameters:
//   - $mod_params
// Returns:
//   $lang
///////////////////////////////////////////////////////////////////////////////
function get_global_param_document(&$mod_params) {
  global $HTTP_POST_VARS, $HTTP_GET_VARS;

  if ((is_array ($HTTP_POST_VARS)) && (count($HTTP_POST_VARS) > 0)) {
    $http_obm_vars = $HTTP_POST_VARS;
  } elseif ((is_array ($HTTP_GET_VARS)) && (count($HTTP_GET_VARS) > 0)) {
    $http_obm_vars = $HTTP_GET_VARS;
  }

  if (isset ($http_obm_vars)) {
    $nb_d = 0;
    while ( list( $key ) = each( $http_obm_vars ) ) {
      if (strcmp(substr($key, 0, 4),"cb_d") == 0) {
	$nb_d++;
	$d_num = substr($key, 4);
	$mod_params["doc$nb_d"] = $d_num;
      }
    }
    if ($nb_d > 0) {
      $mod_params["doc_nb"] = $nb_d;
    }
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

    $pattern = "/(^|\n)([12][0-9]{3}-[0-1][0-9]-[0-3][0-9]:.*:)/U";
    $replace = "</td>
    </tr>
    <tr>
      <td bgcolor=\"#$col1\">\\2</td>
    </tr><tr>
      <td bgcolor=\"#$col0\">";

    $res = preg_replace($pattern, $replace, $com, 1);
    while (preg_match($pattern, $res)) {
      $nb++;
      $res = preg_replace($pattern, $replace, $res, 1);
    }
    
    $result = "
    <table border=\"1\" width=\"100%\">
    <tr>
      <td>$res</td>
    </tr>
    </table>";

  } else {
  // reverse Chronological Order
    // pattern modifiers :
    // U : Ungreedy
    // s : "." matches all chars even newlines
    // D : $ matches only end of text, not end of line
    $pattern = "/(^|\n)([12][0-9]{3}-[0-1][0-9]-[0-3][0-9]:.*:)(.*)($|((^|\n)([12][0-9]{3}-[0-1][0-9]-[0-3][0-9]:.*:)))/UsD";
    $replace = "\\4";
    $tmp = $com;
    
    while (preg_match($pattern, $tmp, $matches)) {
      $nb++;
      $who = $matches[2];
      $text = $matches[3];
      $row = "
    <tr>
      <td bgcolor=\"#$col1\">$who</td>
    </tr><tr>
      <td bgcolor=\"#$col0\">$text</td>
    </tr>";
      $rows = $row . $rows;
      $tmp = preg_replace($pattern, $replace, $tmp, 1);
    }

    $result = "
    <table border=\"1\" width=\"100%\">
    $rows
    </table>";
  }

  return $result;
}


///////////////////////////////////////////////////////////////////////////////
// Display an entity dates and users creation / update infos
// Parameters:
//   - $obm_q : DBO object : used fields
//              usercreate userupdate timecreate timeupdate datasource_name
///////////////////////////////////////////////////////////////////////////////
function display_record_info($obm_q) {
  global $set_theme, $ico_created, $ico_updated;

  if (is_object($obm_q)) {
    $usercreate = $obm_q->f("usercreate");
    $userupdate = $obm_q->f("userupdate");
    $timecreate = datetime_format($obm_q->f("timecreate"), 1);
    $timeupdate = datetime_format($obm_q->f("timeupdate"), 1);
    $datasource = $obm_q->f("datasource_name");
    
    $created = "<img src=\"/images/$set_theme/$ico_created\" alt=\"\" />$usercreate : $timecreate";
    $updated = "<img src=\"/images/$set_theme/$ico_updated\" alt=\"\" />$userupdate : $timeupdate";
    
    if ($datasource != "") {
      $dis_dsrc = "<br />$datasource";
    } else {
      $dis_dsrc = "";
    }
    
    $block = "<div class=\"detailInfo\">$created<br />$updated $dis_dsrc
  </div>";
  } else {
    $block = "<div class=\"detailInfo\">$created<br />$updated $dis_dsrc
  </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Check Privacy access
// if action has privacy enable, check if entity is visible by the user
// Parameters:
//   - $menu   : menu to check (get $actions from)
//   - $table  : table (entity) to check (can be parentdeal for DEAL menu)
//   - $action : action to check
//   - $id     : entity id
//   - $p_uid  : user id to check access
// Returns true if entity is visible, else false
///////////////////////////////////////////////////////////////////////////////
function check_privacy($menu, $table, $action, $id, $p_uid="") {
  global $actions, $auth, $cdg_sql;

  // If action does not require privacy, grant access
  if (! $actions[$menu][$action]["Privacy"]) {
    return true;
  }
 
  // Set uid if not given
  if ($p_uid == "") {
    $uid = $auth->auth["uid"];
  } else {
    $uid = $p_uid;
  }

  $entity = strtolower($table);

  $field_pri = "${entity}_privacy";
  $field_uc = "${entity}_usercreate";
  $field_id = "${entity}_id";

  $query = "select $field_pri, $field_uc from $table where $field_id = '$id'";
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
// Transform a name in a simplified version (suppress .,-,',accent)
// Used for aka (Also Know As) names
// Parameters:
//   - $name : name to transform
// Returns:
//   - $result : name converted
///////////////////////////////////////////////////////////////////////////////
function reduce_name($name) {

  $res = preg_replace("/[טכיך]/i", "e", $name);
  $res = preg_replace("/[אה]/i", "a", $res);
  $res = preg_replace("/[מן]/i", "i", $res);
  $res = preg_replace("/[פצ]/i", "o", $res);
  $res = preg_replace("/[ûüש]/i", "u", $res);
  $res = preg_replace("/[ח]/i", "c", $res);
  $res = preg_replace("/[\.\-\'\\\]/", "", $res);

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Wrapper around phonetic encoding (to ease phonetic algo change)           //
// Parameters:
//   - $name : name to encode
// Returns:
//   $res : phonetic encoded value of name
///////////////////////////////////////////////////////////////////////////////
function phonetic_key($name) {

  $res = metaphone($name);

  return $res;
}


//functions used by many modules
function month_name ($num_month) {
  global $l_monthsofyear;

  return $l_monthsofyear[$num_month-1];
  
}


function month_short_name ($num_month) {
  global $l_monthsofyearshort;

  return $l_monthsofyearshor[$num_month-1];
}


</script>
