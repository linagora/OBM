<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : global_pref.inc                                              //
//     - Desc : Global preference OBM include file                           //
// 2002-07-31 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

// Update the cookie expires time : now + lifetime or session wide
// but not in txt mode or if headers have already been sent
if ( (! headers_sent()) && ($mode != "txt"))
  $sess->setcookie();

if ($set_lang == "") $set_lang = $set_lang_default;
if ($set_theme == "") $set_theme = $set_theme_default;
if ($set_display == "") $set_display = $set_display_default;
if ($set_rows == "") $set_rows = $set_rows_default;
if ($set_debug == "") $set_debug = $set_debug_default;

if ($last_company=="") $last_company=$last_company_default;
if ($last_deal=="") $last_deal=$last_deal_default;
if ($last_contact=="") $last_contact=$last_contact_default;
if ($last_account=="") $last_account=$last_account_default;
if ($last_invoice=="") $last_invoice=$last_invoice_default;
if ($last_payment=="") $last_payment=$last_payment_default;
if ($last_contract=="") $last_contract=$last_contract_default;
if ($last_incident=="") $last_incident=$last_incident_default;

///////////////////////////////////////////////////////////////////////////////
// Inclusion of the correct Theme.                                           //
///////////////////////////////////////////////////////////////////////////////
$theme_file = "$obminclude/themes/$set_theme/theme.inc";
if (file_exists("$path/../$theme_file")) {
  include("$theme_file");
} else {
  include ("$obminclude/themes/$set_theme_default/theme.inc");
}


///////////////////////////////////////////////////////////////////////////////
// Inclusion of the correct Language File, depending from which module this  //
// page is called and the selected language.                                 //
///////////////////////////////////////////////////////////////////////////////
$lang_file = "$obminclude/lang/$set_lang/global.inc";
if (file_exists("$path/../".$lang_file)) {
  include("$lang_file");
}

if ($menu != "") {
  $lang_file = "$obminclude/lang/$set_lang/".strtolower($menu).".inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  } else {//if (file_exists("$path/../$obminclude/lang/$set_lang_default/".strtolower($menu).".inc")) {
    include("$obminclude/lang/$set_lang_default/".strtolower($menu).".inc");
  }
}


///////////////////////////////////////////////////////////////////////////////
// Inclusion of the correct Menu (function generate_menu($menu) in menu.inc) //
///////////////////////////////////////////////////////////////////////////////
$menu_include_file = "$obminclude/themes/$set_theme/menu.inc";
include("$menu_include_file");


// List Export Formats (use theme info so after theme inclusion)
// Format 0 : HTML Stickers
$c_export_format[0]["template"] = false;
$c_export_format[0]["url"] = "addresses.php?fmt=htm&amp;form=label";
$c_export_format[0]["icon"] = $ico_html;
// Format 1 : HTML Table
$c_export_format[1]["template"] = false;
$c_export_format[1]["url"] = "addresses.php?fmt=htm&amp;form=table";
$c_export_format[1]["icon"] = $ico_html;
// Format 2 : RTF Stickers
$c_export_format[2]["template"] = false;
$c_export_format[2]["url"] = "addresses.php?fmt=rtf&amp;form=label";
$c_export_format[2]["icon"] = $ico_rtf;
// Format 3 : Excel data
$c_export_format[3]["template"] = true;
$c_export_format[3]["url"] = "addresses.php?fmt=xls&amp;form=table";
$c_export_format[3]["icon"] = $ico_excel;

//////////////////////////////////////////////////////////////////////////////
// Menu item defintion : Sections
//////////////////////////////////////////////////////////////////////////////
$menus["COM"]	 =	array('Name'=> $l_header_com,
		 	      'Url' => "$path/company/company_index.php");
$menus["PROD"]	 =	array('Name'=> $l_header_prod,
		 	      'Url' => "$path/contract/contract_index.php");
$menus["ADMIN"]  =	array('Name'=> $l_header_admins,
		              'Url' => "$path/admin/admin_index.php?mode=html");
$menus["COMPTA"] =	array('Name'=> $l_header_compta,
			      'Url' => "$path/treso/account_index.php");
$menus["USERS"]   =	array('Name'=> $l_header_users,
			      'Url' => "$path/user/user_index.php");
//////////////////////////////////////////////////////////////////////////////
// Menu item defintion : Modules
//////////////////////////////////////////////////////////////////////////////
if($section == "PROD") {

  $menus["PROD"]["CONTRACT"] = array('Name'=> $l_header_contract,
  			             'Url' => "$path/contract/contract_index.php");
  $menus["PROD"]["INCIDENT"] = array('Name'=> $l_header_incident,
  			      	     'Url' => "$path/contract/incident_index.php");
}elseif($section == "COM") {

  $menus["COM"]["COMPANY"] = array('Name'=> $l_header_company,
			           'Url' => "$path/company/company_index.php");
  $menus["COM"]["CONTACT"] = array('Name'=> $l_header_contact,
			           'Url' => "$path/contact/contact_index.php");
  $menus["COM"]["DEAL"]	   = array('Name'=> $l_header_deal,
  			         'Url' => "$path/deal/deal_index.php");
  $menus["COM"]["LIST"]	   = array('Name'=> $l_header_list,
			           'Url' => "$path/list/list_index.php");
  $menus["COM"]["AGENDA"]  = array('Name'=> $l_header_agenda,
			           'Url' => "$path/agenda/agenda_index.php");

}elseif($section == "ADMIN") {

  $menus["ADMIN"]["ADMIN"]       = array('Name'=> $l_header_admin,
			                 'Url' => "$path/admin/admin_index.php?mode=html");
  $menus["ADMIN"]["ADMIN_CODE"]  = array('Name'=> $l_header_admin_code,
			                 'Url' => "$path/admin_code/admin_code_index.php?mode=html");
  $menus["ADMIN"]["ADMIN_PREF"]  = array('Name'=> $l_header_admin_pref,
			                 'Url' => "$path/admin_pref/admin_pref_index.php?mode=html");
  $menus["ADMIN"]["ADMIN_DATA"]  = array('Name'=> $l_header_admin_data,
			                 'Url' => "$path/admin_data/admin_data_index.php?mode=html");
  $menus["ADMIN"]["ADMIN_LANG"]  = array('Name'=> $l_header_admin_lang,
			                 'Url' => "$path/admin_lang/admin_lang_index.php?mode=html");

}elseif($section == "COMPTA") {

  $menus["COMPTA"]["ACCOUNT"] = array('Name'=> $l_header_account,
			              'Url' => "$path/treso/account_index.php");
  $menus["COMPTA"]["INVOICE"] = array('Name'=> $l_header_invoice,
			              'Url' => "$path/treso/invoice_index.php");
  $menus["COMPTA"]["PAYMENT"] = array('Name'=> $l_header_payment,
			              'Url' => "$path/treso/payment_index.php");

}elseif($section == "USERS") {

  $menus["USERS"]["USER"]     = array('Name'=> $l_header_user,
			              'Url' => "$path/user/user_index.php");
  $menus["USERS"]["TIME"]     = array('Name'=> $l_header_time,
			              'Url' => "$path/time/time_index.php");
  $menus["USERS"]["SETTINGS"] = array('Name'=> $l_header_settings,
			              'Url' => "$path/settings/settings_index.php");
}

//////////////////////////////////////////////////////////////////////////////
// Company actions
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
// Contact actions
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
// LIST actions
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
// AGENDA actions
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
// Contract actions
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
// Incident actions
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
// Account actions
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
// Payment actions
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
// Invoice actions
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
// User actions
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
// Settings actions
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
// Time actions
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
// Time actions
//////////////////////////////////////////////////////////////////////////////

if($menu == "ADMIN") {

//Index 

  $actions["ADMIN"]["index"] = array (
    'Name'    => $l_header_find,   'Url'     => "$path/admin/admin_index.php?action=index&amp;mode=html",
    'Right'   => $admin_read,
   'Condition'=> array ('all') 
                                     );

//data_show 

  $actions["ADMIN"]["data_show"] = array (
    'Url'     => "$path/admin/admin_index.php?action=data_show&amp;mode=html",
    'Right'   => $admin_read,
    'Condition'=> array ('None') 
                                     );

//Data Update 

  $actions["ADMIN"]["data_update"] = array (
    'Url'      => "$path/admin/admin_index.php?action=data_update&amp;mode=html",
    'Right'    => $admin_read,
    'Condition'=> array ('None') 
                                     );

//Help

  $actions["ADMIN"]["help"] = array (
     'Name'     => $l_header_help,
     'Url'      => "$path/admin/admin_index.php?action=help&amp;mode=html",
     'Right'    => $admin_read,
     'Condition'=> array ('all') 
                                    );

}
//////////////////////////////////////////////////////////////////////////////
// Time actions
//////////////////////////////////////////////////////////////////////////////

elseif($menu == "ADMIN_LANG") {

  $actions["ADMIN_LANG"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/admin_lang/admin_lang_index.php?action=index&amp;mode=htm",
                                                  'Right' 	  
						  => $admin_lang_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  ); 

 $actions["ADMIN_LANG"]["help"]		 = array ('Name'
						  => $l_header_help,
            		          	          'Url'
					 	  => "$path/admin_lang/admin_lang_index.php?action=help&amp;mode=html",
                                                  'Right' 	  
						  => $admin_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

}
//////////////////////////////////////////////////////////////////////////////
// Time actions
//////////////////////////////////////////////////////////////////////////////

elseif($menu == "ADMIN_CODE") {

  $actions["ADMIN_CODE"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/admin_code/admin_code_index.php?action=index&amp;mode=html",
                                                  'Right' 	  
						  => $admin_code_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

 $actions["ADMIN_CODE"]["help"]		 = array ('Name'
						  => $l_header_help,
            		          	          'Url'
					 	  => "$path/admin_code/admin_code_index.php?action=help&amp;mode=html",
                                                  'Right' 	  
						  => $admin_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

}
//////////////////////////////////////////////////////////////////////////////
// Time actions
//////////////////////////////////////////////////////////////////////////////

elseif($menu == "ADMIN_PREF") {

  $actions["ADMIN_PREF"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/admin_pref/admin_pref_index.php?action=index&amp;mode=html",
                                                  'Right' 	  
						  => $admin_pref_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

 $actions["ADMIN_PREF"]["help"]		 = array ('Name'
						  => $l_header_help,
            		          	          'Url'
					 	  => "$path/admin_pref/admin_pref_index.php?action=help&amp;mode=html",
                                                  'Right' 	  
						  => $admin_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

}
//////////////////////////////////////////////////////////////////////////////
// Time actions
//////////////////////////////////////////////////////////////////////////////

elseif($menu == "ADMIN_DATA") {

  $actions["ADMIN_DATA"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/admin_data/admin_data_index.php?action=index&amp;mode=html",
                                                  'Right' 	  
						  => $admin_data_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

 $actions["ADMIN_DATA"]["help"]		 = array ('Name'
						  => $l_header_help,
            		          	          'Url'
					 	  => "$path/admin_data/admin_data_index.php?action=help&amp;mode=html",
                                                  'Right' 	  
						  => $admin_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

}

