<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global.inc                                                   //
//     - Desc : Global common OBM include file                               //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$obm_version="0.5.3pre4";
//$set_debug=0;
$l_title="O.B.M.";
$status="";
// VARS that go in session
$set_lang_default="en";
$set_theme_default="aliacom";
$set_rows_default=10;
$set_display_default="no";
//$set_debug_default=0;
$last_deal_default=0;
$last_company_default=0;
$last_contact_default=0;
$last_contract_default=0;
$last_invoice_default=0;
$last_account_default=0;
$last_payment_default=0;
$last_contract_default=0;
$last_incident_default=0;
$set_weekstart_default="monday";
//Timemanager module
// In how many parts we can chop a day
$fraction_jour = 8;

// Perms
$perms_user = "user" ;
$perms_editor = "editor" ;
$perms_admin = "admin" ;

// Debug Constants
$cdg_id = 1;     // id display
$cdg_param = 2;  // parameter display
$cdg_sql = 4;    // sql query display

// Global Constants
$c_all = "_ALL_";		// Used in Form select
$c_yes = "Yes";			// Maybe stored in database
$c_no = "No";
$l_sel_none = "---";

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
$js_regexp_web="/^[A-Za-z0-9\-]+(\.[A-Za-z0-9\-]+)*\.[A-Za-z]{2,3}$/";  // Web
$php_regexp_web="^[a-z0-9-]+(\.[a-z0-9-]+)*\.[a-z]{2,3}$"; // Web
$php_regexp_isodate = "^[0-9]{4}-[01][0-9]-[0123][0-9]$";
$js_regexp_list_name = "/^[0-9A-Za-z_-]+$/";  //Name of a list
$js_regexp_number = "/^([0-9][-]?)+$/"; // only numbers
$js_regexp_money = "/^[0-9]+(\\.[0-9][0-9]?)?$/"; // numbers and at most one '.'
$php_regexp_lifetime = "^[0-9]{1,6}$";

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
function display_debug_msg($msg, $debug=0) {
  global $set_debug;

  if (($set_debug > 0) && (($set_debug & $debug) == $debug)) {
    echo $msg;
    echo "<HR>\n";
  }
}  

///////////////////////////////////////////////////////////////////////////////
//Includes
///////////////////////////////////////////////////////////////////////////////
require_once("$obminclude/global_query.inc");
require_once("$obminclude/global_display.inc");


//$config_var = run_query_get_config_var();

//while($config_var->next_record())
//  ${$config_var->f("ConfigObmVars_name")} = $config_var->f("ConfigObmVars_value");



///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
function display_head($l_module) { 
  global $l_title;
  global $col_text,$col_bg,$col_link,$img_bg;
  global $set_theme;

  echo "<html><head><title>$l_title - $l_module</title></head>
    <body TEXT=\"#$col_text\" ";
     if ($img_bg != "") {echo "BACKGROUND=\"/images/$set_theme/$img_bg\" ";}
     if ($col_bg != "") {echo "BGCOLOR=\"#$col_bg\" ";}
     if ($col_link != "") {echo "LINK=\"#$col_link\" ";}
     if ($col_link != "") {echo "VLINK=\"#$col_link\"";}
  echo " marginwidth=0 marginheight=0 topmargin=0 leftmargin=0>";
}


///////////////////////////////////////////////////////////////////////////////
// Displays the end of an HTML page
///////////////////////////////////////////////////////////////////////////////
function display_end() { 
echo "
</body>
</html>
";

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



/////////////////////////////////////////////////////////////////////////////
// Display an error message
/////////////////////////////////////////////////////////////////////////////
function display_err_msg($msg) {
  global $col_error; 

  echo "<FONT color=\"#$col_error\">";
  echo $msg;
  echo "</FONT><BR>";
}


/////////////////////////////////////////////////////////////////////////////
// Display a warnig message
/////////////////////////////////////////////////////////////////////////////
function display_warn_msg($msg) {
  global $col_textem; 

  echo "<FONT color=\"#$col_textem\">";
  echo $msg;
  echo "</FONT><BR>";
}


////////////////////////////////////////////////////////////////////////////
// Display an OK message
////////////////////////////////////////////////////////////////////////////
function display_ok_msg($msg) {
  global $col_ok; 

  echo "<CENTER><FONT color=\"#$col_ok\">";
  echo $msg;
  echo "</FONT><CENTER><BR>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the links to the last visited company, contact and deal
// we need $path to be able to display bookmarks
// from the first screen after login...
///////////////////////////////////////////////////////////////////////////////
function display_bookmarks($path="..") {
  global $last_company,$last_contact,$last_deal, $last_contract;
  global $last_account,$last_invoice, $last_payment, $last_incident;
  global $last_company_name, $last_contact_name, $last_deal_name,$last_contract_name;
  global $last_payment_name, $last_account_name, $last_invoice_name,$last_incident_name;
  global $sess;

  echo "<center>
     <table border=0>
     <tr><td>
     <font size=-2>
     <a href=\"".$sess->url("$path/company/company_index.php?action=detailconsult&amp;param_company=".$last_company)."\">
     ".substr($last_company_name,0,30)."</a></font>
     </td><td>
     <font size=-2>
     <a href=\"".$sess->url("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$last_contact)."\">
     ".substr($last_contact_name,0,30)."</a></font>
     </td><td>
     <font size=-2>
     <a href=\"".$sess->url("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=".$last_deal)."\">
     ".substr($last_deal_name,0,30)."</a></font>
     </td><td>
     <font size=-2>
     <a href=\"".$sess->url("$path/treso/account_index.php?action=detailconsult&amp;param_account=".$last_account)."\">
     ".substr($last_account_name,0,30)."</a></font>
     </td><td>
     <font size=-2>
     <a href=\"".$sess->url("$path/treso/invoice_index.php?action=detailconsult&amp;param_invoice=".$last_invoice)."\">
     ".substr($last_invoice_name,0,30)."</a></font>
     </td><td>
     <font size=-2>
     <a href=\"".$sess->url("$path/treso/payment_index.php?action=detailconsult&amp;param_payment=".$last_payment)."\">
     ".substr($last_payment_name,0,30)."</a></font> 
     </td><td>
     <font size=-2>
     <a href=\"".$sess->url("$path/contract/contract_index.php?action=detailconsult_contract&amp;param_contract=".$last_contract)."\">
     ".substr($last_contract_name,0,30)."</a></font>
    </td><td>
     <font size=-2>
     <a href=\"".$sess->url("$path/contract/incident_index.php?action=detailconsult&amp;param_incident=".$last_incident)."\">
     ".substr($last_incident_name,0,30)."</a></font>"; 
  echo "          </td></tr></table></center>
     <hr>"; 
}


///////////////////////////////////////////////////////////////////////////////
// Display date part of timestamp                                            //
///////////////////////////////////////////////////////////////////////////////
function timestamp_to_date($timestamp) {
  $date=substr($timestamp,0,10);

  return $date;
}


///////////////////////////////////////////////////////////////////////////////
// Display time part of timestamp                                            //
///////////////////////////////////////////////////////////////////////////////
function timestamp_to_time($timestamp) {
  $date=substr(trim($timestamp),11,8);

  return $date;
}


//////////////////////////////////
//
///////////////////////////////////
function display_record_info($p_usercreate_id,$p_userupdate_id,$p_timecreate,$p_timeupdate) {

  $obm_q= new DB_OBM;
  $query_created = "select userobm_login from UserObm where userobm_id=".$p_usercreate_id;
  $query_modified = "select userobm_login from UserObm where userobm_id=".$p_userupdate_id;

  echo "<table width=\"100%\">";
  echo "  <tr><td width=\"60%\">";
  echo "    &nbsp;"; 
  echo "  </td><td align=right>";
  if ($p_usercreate_id != "") {
    $obm_q->query($query_created);
    $obm_q->next_record();
    echo "    <font size=1>";
    echo "      created :" . $obm_q->f("userobm_login") . " (". timestamp_to_date($p_timecreate). " " .timestamp_to_time($p_timecreate) .")";
    echo "    </font>";
  }
  echo "<br>";

  if ($p_userupdate_id != "") {
    $obm_q->query($query_modified);
    $obm_q->next_record();
    echo "    <font size=1>";
    echo "      modified:" . $obm_q->f("userobm_login") . " (". timestamp_to_date($p_timeupdate). " " .timestamp_to_time($p_timeupdate) .")";
    echo "    </font>";
  }
  echo "  </td><tr>";
  echo "</table>";

}


///////////////////////////////////////////////////////////////////////////////
//         Function return yes or no if 1 or 0 is given as argument         ///
///////////////////////////////////////////////////////////////////////////////
function return_yes_no($number) {
  return ($number == 1) ? "yes" : "no" ;
}


///////////////////////////////////////////////////////////////////////////
// Display the permission error message
///////////////////////////////////////////////////////////////////////////
function display_error_permission()
{	
	global $col_error;
	global $l_error_permission;

	echo "<CENTER><FONT color=\"#$col_error\">";
  	echo $l_error_permission;
  	echo "</FONT></CENTER><BR>";
}


///////////////////////////////////////////////////////////////////////////
// Display the "visibility" error message : it occurs when a user tries 
// to access a contact or a deal which access is private.
///////////////////////////////////////////////////////////////////////////
function display_error_visibility()
{	
	global $col_error;
	global $l_error_visibility;

	echo "<CENTER><FONT color=\"#$col_error\">";
  	echo $l_error_visibility;
  	echo "</FONT></CENTER><BR>";
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
