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
// Deal actions
//////////////////////////////////////////////////////////////////////////////

if($menu == "DEAL") {

  $actions["DEAL"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/list/list_index.php?action=index",
                                                  'Right' 	  
						  => $list_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

  $actions["DEAL"]["new"]	        = array ('Name' 	  
						 => $l_header_new_f,
            		          	         'Url'   
						 => "$path/company/company_index.php?action=ext_get_id_url&amp;popup=1&amp;title=".urlencode($l_deal_select_company)."&amp;url=".urlencode("$path/deal/deal_index.php?action=new&amp;param_company=")."",
                                     	         'Right' 	  
						 => $deal_write,
                                      		 'Popup' 	 
						 => 1,
				            	 'Condition'      
						 => array ('','search','parent_search','index','detailconsult','parent_detailconsult','admin','display') 
                                    		  );

  $actions["DEAL"]["parent_new"]   	= array ('Name'		  
						 => $l_header_new_parent,
            		            		 'Url' 		  
						 => "$path/deal/deal_index.php?action=parent_new",
                                      		 'Right' 	  
						 => $deal_write,
				      		 'Condition' 	  
						 => array ('','search','parent_search','index','detailconsult','parent_detailconsult','admin','display') 
                                     		 );

  $actions["DEAL"]["detailupdate"]   	= array ('Name'		  
						 => $l_header_modify,
            		            		 'Url' 		  
						 => "$path/deal/deal_index.php?action=detailupdate&amp;param_deal=".$param_deal."",
                                      		 'Right' 	  
						 => $deal_write,
				      		 'Condition' 	  
						 => array ('detailconsult') 
                                     		 );

  $actions["DEAL"]["parent_detailupdate"] = array ('Name'		  
						 => $l_header_modify,
            		            		 'Url' 		  
						 => "$path/deal/deal_index.php?action=parent_detailupdate&amp;param_parent=".$param_parent."",
                                      		 'Right' 	  
						 => $deal_write,
				      		 'Condition' 	  
						 => array ('parent_detailconsult') 
                                     		 );

  $actions["DEAL"]["delete"]  	 	= array ('Name'		  
						 => $l_header_delete,
            		            		 'Url' 		  
						 => "$path/deal/deal_index.php?action=delete&amp;param_deal=".$param_deal."",
                                      		 'Right' 	 
						 => $deal_write,
				      		 'Condition' 	 
						 => array ('detailconsult') 
                                     		 );

  $actions["DEAL"]["parent_delete"]  	 	= array ('Name'		  
						 => $l_header_delete,
            		            		 'Url' 		  
						 => "$path/deal/deal_index.php?action=delete&amp;param_parent=".$param_parent."",
                                      		 'Right' 	 
						 => $deal_write,
				      		 'Condition' 	 
						 => array ('parent_detailconsult') 
                                     		 );

  
  $actions["DEAL"]["admin"]		= array ('Name'	  	 
						 => $l_header_admin,
            		              		 'Url' 	 	 
						 => "$path/deal/deal_index.php?action=admin",
                                     		 'Right' 	
						 => $deal_admin_write,
				      		 'Condition'	 
						 => array ('all') 
                                      		 );

  $actions["DEAL"]["display"]		= array ('Name'	  	 
						 => $l_header_display,
            		              		 'Url' 	 	 
						 => "$path/deal/deal_index.php?action=display",
                                     		 'Right' 	 
						 => $deal_admin_write,
				      		 'Condition'	 
						 => array ('all') 
                                      		 );

}

//////////////////////////////////////////////////////////////////////////////
// LIST actions
//////////////////////////////////////////////////////////////////////////////

elseif($menu == "LIST") {

  $actions["LIST"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/list/list_index.php?action=index",
                                                  'Right' 	  
						  => $list_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

  $actions["LIST"]["new"]	        = array ('Name' 	  
						 => $l_header_new,
            		          	         'Url'   
						 => "$path/list/list_index.php?action=new",
                                     	         'Right' 	  
						 => $list_write,
				            	 'Condition'      
						 => array ('','search','index','detailconsult','admin','display') 
                                    		  );

  $actions["LIST"]["detailupdate"]   	= array ('Name'		  
						 => $l_header_modify,
            		            		 'Url' 		  
						 => "$path/list/list_index.php?action=detailupdate&amp;param_list=".$param_list."",
                                      		 'Right' 	  
						 => $list_write,
				      		 'Condition' 	  
						 => array ('detailconsult','contact_add','contact_del') 
                                     		 );

  $actions["LIST"]["check_delete"]   	= array ('Name'		  
						 => $l_header_delete,
            		            		 'Url' 		  
						 => "$path/list/list_index.php?action=check_delete&amp;hd_list_id=".$param_list."",
                                      		 'Right' 	 
						 => $list_write,
				      		 'Condition' 	 
						 => array ('detailconsult','contact_add','contact_del') 
                                     		 );

  $actions["LIST"]["ext_get_ids"]        = array ('Name' 	  
						 => $l_header_add_contact,
            		          	         'Url'   
						 => "$path/list/list_index.php?action=ext_get_ids&amp;popup=1&amp;title=".urlencode($l_deal_select_company)."&amp;ext_action=export_add&amp;ext_url=".urlencode($path."/list/list_index.php")."",
                                     	         'Right' 	  
						 => $list_write,
                                      		 'Popup' 	 
						 => 1,
				            	 'Condition'      
						 => array ('detailconsult','contact_add','contact_del') 
                                    		  );

  $actions["LIST"]["export_add"]  	 	= array ('Name'		  
						 => $l_header_export,
            		            		 'Url' 		  
						 => "$path/list/list_index.php?action=export_add&amp;cb_list".$param_list."=".$param_list."",
                                      		 'Right' 	 
						 => $list_write,
				      		 'Condition' 	 
						 => array ('detailconsult','contact_add','contact_del') 
                                     		 );

  $actions["LIST"]["display"]		= array ('Name'	  	 
						 => $l_header_display,
            		              		 'Url' 	 	 
						 => "$path/list/list_index.php?action=display",
                                     		 'Right' 	 
						 => $list_admin_write,
				      		 'Condition'	 
						 => array ('all') 
                                      		 );

}
//////////////////////////////////////////////////////////////////////////////
// AGENDA actions
//////////////////////////////////////////////////////////////////////////////
elseif($menu == "AGENDA") {

  $actions["AGENDA"]["index"]		 = array ('Name'
						  => '',
            		          	          'Url'
					 	  => "$path/agenda/agenda_index.php?action=index",
                                                  'Right' 	  
						  => $agenda_read,
				                  'Condition'	  
						  => array ('None') 
                                    		  );

  $actions["AGENDA"]["new"]   		= array ('Name' 	  
						 => $l_header_new_event,
            		          	         'Url'  	  
						 => "$path/agenda/agenda_index.php?action=new",
                                     	         'Right' 	  
						 => $agenda_write,
				            	 'Condition'      
						 => array ('','index','detailconsult','view_month','view_week','view_day') 
                                    		  );

  $actions["AGENDA"]["view_month"]   	= array ('Name' 	  
						 => $l_header_month,
            		          	         'Url'  	  
						 => "$path/agenda/agenda_index.php?action=view_month",
                                     	         'Right' 	  
						 => $agenda_write,
				            	 'Condition'      
						 => array ('all') 
                                    		  );

  $actions["AGENDA"]["view_week"]   	= array ('Name' 	  
						 => $l_header_week,
            		          	         'Url'  	  
						 => "$path/agenda/agenda_index.php?action=view_week",
                                     	         'Right' 	  
						 => $agenda_write,
				            	 'Condition'      
						 => array ('all') 
                                    		  );

  $actions["AGENDA"]["view_day"]   	= array ('Name' 	  
						 => $l_header_day,
            		          	         'Url'  	  
						 => "$path/agenda/agenda_index.php?action=view_day",
                                     	         'Right' 	  
						 => $agenda_write,
				            	 'Condition'      
						 => array ('all') 
                                    		  );

  $actions["AGENDA"]["detailupdate"]    = array ('Name'		  
						 => $l_header_modify,
            		            		 'Url' 		  
						 => "$path/agenda/agenda_index.php?action=detailupdate&amp;param_event=".$param_event."",
                                      		 'Right' 	  
						 => $agenda_write,
				      		 'Condition' 	  
						 => array ('detailconsult') 
                                     		 );
  $actions["AGENDA"]["delete"]          = array ('Name'		  
						 => $l_header_delete,
            		            		 'Url' 		  
						 => "$path/agenda/agenda_index.php?action=delete&amp;param_event=".$param_event."",
                                      		 'Right' 	  
						 => $agenda_write,
				      		 'Condition' 	 
						 => array ('detailconsult') 
                                     		 );

}
//////////////////////////////////////////////////////////////////////////////
// Contract actions
//////////////////////////////////////////////////////////////////////////////

elseif($menu == "CONTRACT") {

  $actions["CONTRACT"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/contract/contract_index.php?action=index",
                                                  'Right' 	  
						  => $contract_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

  $actions["CONTRACT"]["new"]   	= array ('Name' 	  
						 => $l_header_new,
            		          	         'Url'  	  
				            	 => "$path/company/company_index.php?action=ext_get_id_url&amp;popup=1&amp;title=".urlencode($l_select_company)."&amp;url=".urlencode("$path/contract/contract_index.php?action=new&amp;param_company=")."",
                                     	         'Right' 	  
						 => $contract_write,
                                      		 'Popup' 	 
						 => 1,
					  	 'Condition'      
						 => array ('','search','index','detailconsult','admin','display') 
                                    		  );

  $actions["CONTRACT"]["detailupdate"]   = array ('Name'		  
						 => $l_header_modify,
            		            		 'Url' 		  
						 => "$path/contract/contract_index.php?action=detailupdate&amp;param_contract=".$param_contract."",
                                      		 'Right' 	  
						 => $contract_write,
				      		 'Condition' 	  
						 => array ('detailconsult') 
                                     		 );

  $actions["CONTRACT"]["delete"]   	= array ('Name'		  
						 => $l_header_delete,
            		            		 'Url' 		  
						 => "$path/contract/contract_index.php?action=delete&amp;param_contract=".$param_contract."",
                                      		 'Right' 	 
						 => $contract_write,
				      		 'Condition' 	 
						 => array ('detailconsult') 
                                     		 );

  $actions["CONTRACT"]["admin"]		= array ('Name'	  	 
						 => $l_header_admin,
            		              		 'Url' 	 	 
						 => "$path/contract/contract_index.php?action=admin",
                                     		 'Right' 	
						 => $contract_admin_write,
				      		 'Condition'	 
						 => array ('all') 
                                      		 );

  $actions["CONTRACT"]["display"]	= array ('Name'	  	 
						 => $l_header_display,
            		              		 'Url' 	 	 
						 => "$path/contract/contract_index.php?action=display",
                                     		 'Right' 	 
						 => $contract_admin_write,
				      		 'Condition'	 
						 => array ('all') 
                                      		 );

}
//////////////////////////////////////////////////////////////////////////////
// Incident actions
//////////////////////////////////////////////////////////////////////////////

elseif($menu == "INCIDENT") {

  $actions["INCIDENT"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/contract/incident_index.php?action=index",
                                                  'Right' 	  
						  => $incident_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

  $actions["INCIDENT"]["new"]   	= array ('Name' 	  
						 => $l_header_new,
            		          	         'Url'  	  
				            	 => "$path/contract/incident_index.php?action=new",
                                     	         'Right' 	  
						 => $incident_write,
					  	 'Condition'      
						 => array ('','search','index','detailconsult','display') 
                                    		  );

  $actions["INCIDENT"]["detailupdate"]   = array ('Name'		  
						 => $l_header_modify,
            		            		 'Url' 		  
						 => "$path/contract/incident_index.php?action=detailupdate&amp;param_incident=".$param_incident."",
                                      		 'Right' 	  
						 => $incident_write,
				      		 'Condition' 	  
						 => array ('detailconsult') 
                                     		 );

  $actions["INCIDENT"]["delete"]   	= array ('Name'		  
						 => $l_header_delete,
            		            		 'Url' 		  
						 => "$path/contract/incident_index.php?action=delete&amp;param_incident=".$param_incident."",
                                      		 'Right' 	 
						 => $incident_write,
				      		 'Condition' 	 
						 => array ('detailconsult') 
                                     		 );

  $actions["INCIDENT"]["display"]	= array ('Name'	  	 
						 => $l_header_display,
            		              		 'Url' 	 	 
						 => "$path/contract/incident_index.php?action=display",
                                     		 'Right' 	 
						 => $incident_admin_write,
				      		 'Condition'	 
						 => array ('all') 
                                      		 );

}

//////////////////////////////////////////////////////////////////////////////
// Account actions
//////////////////////////////////////////////////////////////////////////////

elseif($menu == "ACCOUNT") {

  $actions["ACCOUNT"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/treso/account_index.php?action=index",
                                                  'Right' 	  
						  => $account_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

  $actions["ACCOUNT"]["new"]   	= array ('Name' 	  
						 => $l_header_new,
            		          	         'Url'  	  
				            	 => "$path/treso/account_index.php?action=new",
                                     	         'Right' 	  
						 => $account_write,
					  	 'Condition'      
						 => array ('','search','index','detailconsult','display') 
                                    		  );

  $actions["ACCOUNT"]["compute_balance"]   = array ('Name'		  
						 => $l_header_compute_balance,
            		            		 'Url' 		  
						 => "$path/treso/account_index.php?action=compute_balance&amp;param_account=".$param_account."",
                                      		 'Right' 	  
						 => $account_write,
				      		 'Condition' 	  
						 => array ('detailconsult') 
                                     		 );


  $actions["ACCOUNT"]["detailupdate"]   = array ('Name'		  
						 => $l_header_modify,
            		            		 'Url' 		  
						 => "$path/treso/account_index.php?action=detailupdate&amp;param_account=".$param_account."",
                                      		 'Right' 	  
						 => $account_write,
				      		 'Condition' 	  
						 => array ('detailconsult') 
                                     		 );

  $actions["ACCOUNT"]["delete"]   	= array ('Name'		  
						 => $l_header_delete,
            		            		 'Url' 		  
						 => "$path/treso/account_index.php?action=delete&amp;param_account=".$param_account."",
                                      		 'Right' 	 
						 => $incident_write,
				      		 'Condition' 	 
						 => array ('detailconsult') 
                                     		 );

  $actions["ACCOUNT"]["display"]	= array ('Name'	  	 
						 => $l_header_display,
            		              		 'Url' 	 	 
						 => "$path/treso/account_index.php?action=display",
                                     		 'Right' 	 
						 => $incident_admin_write,
				      		 'Condition'	 
						 => array ('all') 
                                      		 );

  $actions["ACCOUNT"]["admin"]		= array ('Name'	  	 
						 => $l_header_admin,
            		              		 'Url' 	 	 
						 => "$path/treso/account_index.php?action=admin",
                                     		 'Right' 	
						 => $contract_admin_write,
				      		 'Condition'	 
						 => array ('all') 
                                      		 );


}
//////////////////////////////////////////////////////////////////////////////
// Payment actions
//////////////////////////////////////////////////////////////////////////////

elseif($menu == "PAYMENT") {

  $actions["PAYMENT"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/treso/payment_index.php?action=index",
                                                  'Right' 	  
						  => $payment_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

  $actions["PAYMENT"]["new"]   	= array ('Name' 	  
						 => $l_header_new,
            		          	         'Url'  	  
				            	 => "$path/treso/payment_index.php?action=new",
                                     	         'Right' 	  
						 => $payment_write,
					  	 'Condition'      
						 => array ('','search','index','detailconsult','display') 
                                    		  );

  $actions["PAYMENT"]["reconcile"]   	= array ('Name' 	  
						 => $l_header_reconcile,
            		          	         'Url'  	  
				            	 => "$path/treso/payment_index.php?action=reconcile",
                                     	         'Right' 	  
						 => $payment_write,
					  	 'Condition'      
						 => array ('','search','index','detailconsult','display') 
                                    		  );

  $actions["PAYMENT"]["duplicate"]   = array ('Name'		  
						 => $l_header_dupplicate,
            		            		 'Url' 		  
						 => "$path/treso/payment_index.php?action=duplicate&amp;param_payment=".$param_payment."",
                                      		 'Right' 	  
						 => $payment_write,
				      		 'Condition' 	  
						 => array ('detailconsult') 
                                     		 );


  $actions["PAYMENT"]["detailupdate"]   = array ('Name'		  
						 => $l_header_modify,
            		            		 'Url' 		  
						 => "$path/treso/payment_index.php?action=detailupdate&amp;param_payment=".$param_payment."",
                                      		 'Right' 	  
						 => $payment_write,
				      		 'Condition' 	  
						 => array ('detailconsult') 
                                     		 );

  $actions["PAYMENT"]["delete"]   	= array ('Name'		  
						 => $l_header_delete,
            		            		 'Url' 		  
						 => "$path/treso/payment_index.php?action=delete&amp;param_payment=".$param_payment."",
                                      		 'Right' 	 
						 => $incident_write,
				      		 'Condition' 	 
						 => array ('detailconsult') 
                                     		 );

  $actions["PAYMENT"]["display"]	= array ('Name'	  	 
						 => $l_header_display,
            		              		 'Url' 	 	 
						 => "$path/treso/payment_index.php?action=display",
                                     		 'Right' 	 
						 => $incident_admin_write,
				      		 'Condition'	 
						 => array ('all') 
                                      		 );

  $actions["PAYMENT"]["admin"]		= array ('Name'	  	 
						 => $l_header_admin,
            		              		 'Url' 	 	 
						 => "$path/treso/payment_index.php?action=admin",
                                     		 'Right' 	
						 => $contract_admin_write,
				      		 'Condition'	 
						 => array ('all') 
                                      		 );

}
//////////////////////////////////////////////////////////////////////////////
// Invoice actions
//////////////////////////////////////////////////////////////////////////////

elseif($menu == "INVOICE") {

  $actions["INVOICE"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/treso/invoice_index.php?action=index",
                                                  'Right' 	  
						  => $invoice_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

  $actions["INVOICE"]["new"]   	= array ('Name' 	  
						 => $l_header_new,
            		          	         'Url'  	  
				            	 => "$path/treso/invoice_index.php?action=new",
                                     	         'Right' 	  
						 => $invoice_write,
					  	 'Condition'      
						 => array ('','search','index','detailconsult','display') 
                                    		  );

  $actions["INVOICE"]["duplicate"]   = array ('Name'		  
						 => $l_header_dupplicate,
            		            		 'Url' 		  
						 => "$path/treso/invoice_index.php?action=duplicate&amp;param_invoice=".$param_invoice."",
                                      		 'Right' 	  
						 => $invoice_write,
				      		 'Condition' 	  
						 => array ('detailconsult') 
                                     		 );


  $actions["INVOICE"]["detailupdate"]   = array ('Name'		  
						 => $l_header_modify,
            		            		 'Url' 		  
						 => "$path/treso/invoice_index.php?action=detailupdate&amp;param_invoice=".$param_invoice."",
                                      		 'Right' 	  
						 => $invoice_write,
				      		 'Condition' 	  
						 => array ('detailconsult') 
                                     		 );

  $actions["INVOICE"]["delete"]   	= array ('Name'		  
						 => $l_header_delete,
            		            		 'Url' 		  
						 => "$path/treso/invoice_index.php?action=delete&amp;param_invoice=".$param_invoice."",
                                      		 'Right' 	 
						 => $incident_write,
				      		 'Condition' 	 
						 => array ('detailconsult') 
                                     		 );

  $actions["INVOICE"]["display"]	= array ('Name'	  	 
						 => $l_header_display,
            		              		 'Url' 	 	 
						 => "$path/treso/invoice_index.php?action=display",
                                     		 'Right' 	 
						 => $incident_admin_write,
				      		 'Condition'	 
						 => array ('all') 
                                      		 );

  $actions["INVOICE"]["admin"]		= array ('Name'	  	 
						 => $l_header_admin,
            		              		 'Url' 	 	 
						 => "$path/treso/invoice_index.php?action=admin",
                                     		 'Right' 	
						 => $contract_admin_write,
				      		 'Condition'	 
						 => array ('all') 
                                      		 );


}
//////////////////////////////////////////////////////////////////////////////
// Invoice actions
//////////////////////////////////////////////////////////////////////////////

elseif($menu == "USER") {

  $actions["USER"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/user/user_index.php?action=index",
                                                  'Right' 	  
						  => $user_read,
				                  'Condition'	  
						  => array ('all') 
                                    		  );

  $actions["USER"]["new"]   	= array ('Name' 	  
						 => $l_header_new,
            		          	         'Url'  	  
				            	 => "$path/user/user_index.php?action=new",
                                     	         'Right' 	  
						 => $user_write,
					  	 'Condition'      
						 => array ('','search','index','admin','detailconsult','display') 
                                    		  );

  $actions["USER"]["reset"]   = array ('Name'		  
						 => $l_header_reset,
            		            		 'Url' 		  
						 => "$path/user/user_index.php?action=reset&amp;param_user=".$param_user."",
                                      		 'Right' 	  
						 => $user_write,
				      		 'Condition' 	  
						 => array ('detailconsult') 
                                     		 );


  $actions["USER"]["detailupdate"]   = array ('Name'		  
						 => $l_header_modify,
            		            		 'Url' 		  
						 => "$path/user/user_index.php?action=detailupdate&amp;param_user=".$param_user."",
                                      		 'Right' 	  
						 => $user_write,
				      		 'Condition' 	  
						 => array ('detailconsult') 
                                     		 );

  $actions["USER"]["check_delete"]   	= array ('Name'		  
						 => $l_header_delete,
            		            		 'Url' 		  
						 => "$path/user/user_index.php?action=check_delete&amp;param_user=".$param_user."",
                                      		 'Right' 	 
						 => $incident_write,
				      		 'Condition' 	 
						 => array ('detailconsult') 
                                     		 );


  $actions["USER"]["admin"]		= array ('Name'	  	 
						 => $l_header_admin,
            		              		 'Url' 	 	 
						 => "$path/user/user_index.php?action=admin",
                                     		 'Right' 	
						 => $contract_admin_write,
				      		 'Condition'	 
						 => array ('all') 
                                      		 );


}
//////////////////////////////////////////////////////////////////////////////
// Settings actions
//////////////////////////////////////////////////////////////////////////////

elseif($menu == "SETTINGS") {

  $actions["SETTINGS"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/settings/settings_index.php?action=index",
                                                  'Right' 	  
						  => $settings_read,
				                  'Condition'	  
						  => array ('None') 
                                    		  );
}
//////////////////////////////////////////////////////////////////////////////
// Time actions
//////////////////////////////////////////////////////////////////////////////

elseif($menu == "TIME") {

  $actions["TIME"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/time/time_index.php?action=index",
                                                  'Right' 	  
						  => $settings_read,
				                  'Condition'	  
						  => array ('None') 
                                    		  );
}
//////////////////////////////////////////////////////////////////////////////
// Time actions
//////////////////////////////////////////////////////////////////////////////

elseif($menu == "ADMIN") {

  $actions["ADMIN"]["index"]		 = array ('Name'
						  => $l_header_find,
            		          	          'Url'
					 	  => "$path/admin/admin_index.php?action=index&amp;mode=html",
                                                  'Right' 	  
						  => $admin_read,
				                  'Condition'	  
						  => array ('None') 
                                    		  );

  $actions["ADMIN"]["help"]		 = array ('Name'
						  => $l_header_help,
            		          	          'Url'
					 	  => "$path/admin/admin_index.php?action=help&amp;mode=html",
                                                  'Right' 	  
						  => $admin_read,
				                  'Condition'	  
						  => array ('all') 
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
						  => array ('None') 
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
						  => array ('None') 
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
						  => array ('None') 
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
						  => array ('None') 
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

