<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global.inc                                                   //
//     - Desc : Global common OBM include file                               //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$obm_version = "0.6.0";
//$set_debug=0;
$l_title = "O.B.M.";

// VARS that go in session
$set_lang_default = "en";
$set_theme_default = "aliacom";
$set_rows_default = 10;
$set_display_default = "no";
//$set_debug_default=0;
$last_deal_default = 0;
$last_company_default = 0;
$last_contact_default = 0;
$last_contract_default = 0;
$last_invoice_default = 0;
$last_account_default = 0;
$last_payment_default = 0;
$last_contract_default = 0;
$last_incident_default = 0;
//Agenda constants
$set_weekstart_default = "monday"; //First day of the week, in english
$set_start_time = '5'; // first hour of the day
$set_stop_time = '23'; // last hour of the day
$set_time_unit = 2 ;// define the time unit for the agenda display.
		   // For exemple 2 = 1/2 hour(30min), 4 = 1/4 hour(15 mins)...  
//Timemanager module CONSTANTS
// In how many parts we can chop a day
$c_day_fraction = 8;
// How many days we want to fill for each week:
// 5 : Mon -> Fri; 6 : Mon -> Sat... (not tested for anything else than 5...
//  and 5 could be hard coded at some places...
$c_days_in_a_week= 5;
// We define a working week to cope with all cases of working week 
//  for us, we are working from Monday to Friday, so : (week start at Sun)
// $c_days_in_a_week= 5 will become absolete
$working_days = array(0,1,1,1,1,1,0);
// task_status flag
// task not valided by the user : $c_task_notvalid
// task valided by user : $c_task_uservalid
// task valided by admin : $c_task_adminvalid
$c_task_notvalid = 0;
$c_task_uservalid = 1;
$c_task_adminvalid = 2;

// Date constants
$c_php_isodate_format = "Y-m-d";  // AAAA-MM-JJ iso format for php date()

// Perms
$perms_user = "user" ;
$perms_editor = "editor" ;
$perms_admin = "admin" ;


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

///////////////////////////////////////////////////////////////////////////////
//                         PRODUCTION RIGTHS                                 //
///////////////////////////////////////////////////////////////////////////////
$prod_read 		= 16;
$prod_write 		= 32;
$prod_admin_read	= 64;
$prod_admin_write	= 128;
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
			//USER    //
			////////////
$user_read		= $users_admin_read;
$user_write	 	= $users_admin_read + $users_admin_write;
$user_admin_read 	= $users_admin_read;
$user_admin_write	= $users_admin_read + $users_admin_write;
			////////////
			//TIME    //
			////////////
$time_read		= $users_read;
$time_write	 	= $users_read + $users_write;
$time_admin_read 	= $users_admin_read;
$time_admin_write	= $users_admin_read + $users_admin_write;
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

// Debug Constants
$cdg_id = 1;     // id display
$cdg_param = 2;  // parameter display
$cdg_sql = 4;    // sql query display

// Global Constants
$c_all = "_ALL_";		// Used in Form select
$c_yes = "Yes";			// Maybe stored in database
$c_no = "No";

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
function display_debug_msg($msg, $debug=0, $function_name="") {
  global $set_debug;

  if (! empty($function_name))
	$function_name .= " : ";
  if (($set_debug > 0) && (($set_debug & $debug) == $debug)) {
    echo "<font color=\"#$col_green\">$function_name</font>$msg";
    echo "<HR>\n";
  }
}  

///////////////////////////////////////////////////////////////////////////////
//Includes
///////////////////////////////////////////////////////////////////////////////
require_once("$obminclude/global_query.inc");
require_once("$obminclude/global_display.inc");


///////////////////////////////////////////////////////////////////////////////
// Display HTML Head
///////////////////////////////////////////////////////////////////////////////
function display_head($l_module) { 
  global $l_title;
  global $col_text,$col_bg,$col_link,$img_bg;
  global $set_theme, $obminclude;
  $headers_e =  "
   <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"
    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">
   <html xmlns=\"http://www.w3.org/1999/xhtml\"> ";
  $headers_et = "<html>";

  echo "
    $headers_e
    <head>
     <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />
     <title>$l_title - $l_module</title>
     <link rel=\"stylesheet\" type=\"text/css\" href=\"/images/$set_theme/style.css\" />
    </head>
    <body >";
    /*TEXT=\"#$col_text\" ";
     if ($img_bg != "") {echo "background=\"/images/$set_theme/$img_bg\" ";}
     if ($col_bg != "") {echo "bgcolor=\"#$col_bg\" ";}
     if ($col_link != "") {echo "link=\"#$col_link\" ";}
     if ($col_link != "") {echo "vlink=\"#$col_link\"";}
  echo " marginwidth=0 marginheight=0 topmargin=0 leftmargin=0>";
*/
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


/////////////////////////////////////////////////////////////////////////////
// Display an error message
/////////////////////////////////////////////////////////////////////////////
function display_err_msg($msg) {
  echo "<p class=\"messageError\">".htmlspecialchars($msg)."</p>";
}


/////////////////////////////////////////////////////////////////////////////
// Display a warnig message
/////////////////////////////////////////////////////////////////////////////
function display_warn_msg($msg) {
  echo "<p class=\"messageWarning\">".htmlspecialchars($msg)."</p>";
}


////////////////////////////////////////////////////////////////////////////
// Display an OK message
////////////////////////////////////////////////////////////////////////////
function display_ok_msg($msg) {
  echo "<p class=\"messageOk\">".htmlspecialchars($msg)."</p>";
}

////////////////////////////////////////////////////////////////////////////
// Display an Info message
////////////////////////////////////////////////////////////////////////////
function display_info_msg($msg) {
  echo "<p class=\"messageInfo\">".htmlspecialchars($msg)."</p>";
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

  echo "
     <table class=\"bookmarks\">
       <tr>
         <td class=\"bookmarks\">
     <a class=\"bookmarks\" 
     href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=".$last_company)."\">
     ".substr($last_company_name,0,30)."
     </a>
         </td>
	 <td class=\"bookmarks\">
     <a class=\"bookmarks\"
     href=\"".url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$last_contact)."\">
     ".substr($last_contact_name,0,30)."</a>
         </td>
	 <td class=\"bookmarks\">
     <a class=\"bookmarks\"
     href=\"".url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=".$last_deal)."\">
     ".substr($last_deal_name,0,30)."
     </a>
         </td>
	 <td class=\"bookmarks\">
     <a class=\"bookmarks\"
     href=\"".url_prepare("$path/treso/account_index.php?action=detailconsult&amp;param_account=".$last_account)."\">
     ".substr($last_account_name,0,30)."</a>
         </td>
	 <td class=\"bookmarks\">
     <a class=\"bookmarks\"
     href=\"".url_prepare("$path/treso/invoice_index.php?action=detailconsult&amp;param_invoice=".$last_invoice)."\">
     ".substr($last_invoice_name,0,30)."</a>
         </td>
	 <td class=\"bookmarks\">
     <a class=\"bookmarks\"
     href=\"".url_prepare("$path/treso/payment_index.php?action=detailconsult&amp;param_payment=".$last_payment)."\">
     ".substr($last_payment_name,0,30)."</a>
         </td>
	 <td class=\"bookmarks\">
     <a class=\"bookmarks\"
     href=\"".url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;param_contract=".$last_contract)."\">
     ".substr($last_contract_name,0,30)."</a>
         </td>
	 <td class=\"bookmarks\">
     <a class=\"bookmarks\"
     href=\"".url_prepare("$path/contract/incident_index.php?action=detailconsult&amp;param_incident=".$last_incident)."\">
     ".substr($last_incident_name,0,30)."</a>
         </td>
       </tr>
     </table>
     <hr />"; 
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

  $ret_url = str_replace('%', '%25', $sess->url($url));

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

  echo "<table class=\"recordInfo\">";
  echo "<tr><td class=\"recordInfo\">";
  if ($p_usercreate_id != "") {
    $obm_q->query($query_created);
    $obm_q->next_record();
    echo "created :".$obm_q->f("userobm_login")." (".timestamp_to_date($p_timecreate)." ".timestamp_to_time($p_timecreate).")";
  }
  echo "</td>
	</tr><tr>
	<td class=\"recordInfo\">";

  if ($p_userupdate_id != "") {
    $obm_q->query($query_modified);
    $obm_q->next_record();
    echo "modified:" . $obm_q->f("userobm_login") . " (". timestamp_to_date($p_timeupdate). " " .timestamp_to_time($p_timeupdate) .")";
  }
  echo "  </td></tr>";
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


  	 display_err_msg($l_error_visibility);

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
