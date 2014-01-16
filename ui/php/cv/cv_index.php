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



/**
* @filesource cv_index.php  cv Index File  
* @copyright 2006-05-22 : AliaSource  
*/

//  $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields   -- show the cv search form
// - search          -- search fields   -- show the result set of search
// - new             -- $param_company, -- show the new cv form
// - detailconsult   -- $param_cv -- show the cv detail
// - detailupdate    -- $param_cv -- show the cv detail form
// - insert          -- form fields     -- insert the cv 
// - update          -- form fields     -- update the cv
// - check_delete    -- $param_cv -- check links before delete
// - delete          -- $param_cv -- delete the cv
// - export          -- $param_cv -- export the cv
// - admin       --           -- admin index (kind)
// - display         --                 -- display and set display parameters
// - dispref_display --                 -- update one field display value
// - dispref_level   --                 -- update one field display position 
// External API ---------------------------------------------------------------
// - ext_get_id      -- $title          -- select a cv (return id) 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "cv";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("cv_display.inc");
require("cv_query.inc");
require_once("cv_js.inc");
$params = get_cv_params();
require_once("$obminclude/of/of_defaultodttemplate.inc");
require_once("$obminclude/of/of_category.inc");

get_cv_action();
if ($action == "") $action = "index";
$perm->check_permissions($module, $action);
page_close();

// Main Program 

if (! $popup) {
  $display["header"] = display_menu($module);
}

//External calls (main menu not displayed)
if ($action == "ext_get_id") {
  require("cv_js.inc");
  $display["search"] = dis_cv_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] = dis_cv_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

//Normal calls

} elseif ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  require("cv_js.inc");
  $display["search"] = dis_cv_search_form($params);
  if ($_SESSION['set_display'] == "yes") {
    $display["result"] = dis_cv_search_list($params);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }
  
} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  require("cv_js.inc");
  $display["search"] = dis_cv_search_form($params);
  $display["result"] = dis_cv_search_list($params);
  
} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  require("cv_js.inc");  
  $display["detail"] = dis_cv_form($action,$params);
  
} elseif ($action == "duplicate")  {
///////////////////////////////////////////////////////////////////////////////
  require("cv_js.inc"); 
  $display["detail"] = dis_cv_form($action, $params);
  
} elseif ($action == "detailconsult")  {
//////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_cv_consult($params);

} elseif ($action == "detailexport") {
///////////////////////////////////////////////////////////////////////////////
  require("cv_js.inc"); 
  $display["detail"] = dis_cv_export($params);
  
} elseif ($action == "export") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_cv_hidden_export($params);  
} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  require("cv_js.inc"); 
  $display["detail"] = dis_cv_form($action, $params);
  
} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  require("cv_js.inc");
  if (check_cv_form("", $params)) {
    $params["cv_id"] = run_query_cv_insert($params);
    if ($params["cv_id"]) {
      $display["msg"] .= display_ok_msg($l_insert_ok);
      $display["detail"] = dis_cv_consult($params);
    } else {
      $display["msg"] .= display_err_msg($l_insert_error);
    }
  } else {
    $display["msg"] .= display_err_msg($err["msg"]);
    $display["detail"] = dis_cv_form($action, $params);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_cv_form("", $params)) {  
    $ret = run_query_cv_update($params);         
    if ($ret) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
        $display["msg"] .= display_err_msg($l_update_error);
      }
    $display["search"] = dis_cv_consult($params);      
  } else {
      require("cv_js.inc");
      $display["msg"] .= display_err_msg($l_invalid_da. " : " . $err["msg"]);
      $display["detail"] = dis_cv_form($action, $params);
    }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_cv($params["cv_id"])) {
    require("cv_js.inc");
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_can_delete_cv($params["cv_id"]);
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_cv_consult($params);
  }

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_cv($params["cv_id"])) {
    $ret = run_query_cv_delete($params["cv_id"]);
    if ($ret) {
      $display["msg"] .= display_ok_msg($l_delete_ok);
    } else {
      $display["msg"] .= display_err_msg($l_delete_error);
    }
    $display["search"] = dis_cv_search_form($params);
    if ($_SESSION['set_display'] == "yes") {
      $display["result"] = dis_cv_search_list($params);
    } else {
      $display["msg"] .= display_info_msg($l_no_display);
    }
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_cv_consult($params);
  }
  
} elseif ($action == "defaultodttemplate_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_defaultodttemplate_insert($params);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_defaultodttemplate_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_defaultodttemplate_insert_error);
  }
  require("cv_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "defaultodttemplate_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_defaultodttemplate_update($params);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_defaultodttemplate_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_defaultodttemplate_update_error);
  }
  require("cv_js.inc");
  $display["detail"] .= dis_admin_index();
  
} elseif ($action == "defaultodttemplate_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_defaultodttemplate_delete($params["defaultodttemplate"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_defaultodttemplate_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_defaultodttemplate_delete_error);
  }
  require("cv_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "admin")  {
///////////////////////////////////////////////////////////////////////////////
  require("cv_js.inc");
  $display["detail"] = dis_admin_index();
} elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm["uid"], "cv", 1);
  $display["detail"] = dis_cv_display_pref($prefs);
  
} elseif ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm["uid"], "cv", 1);
  $display["detail"] = dis_cv_display_pref($prefs);
 
 } elseif ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm["uid"], "cv", 1);
  $display["detail"] = dis_cv_display_pref($prefs);
 } 

//Display
update_cv_action();
if (! $popup) {
  $display["header"] = display_menu($module);
}
$display["head"] = display_head($l_cv);
$display["end"] = display_end();

display_page($display);


/**
* Stores cv parameters transmited in $cv hash
* @return array $cv hash with parameters set
*/
function get_cv_params() {
  
 $params = get_global_params("CV");

 $params["additionalrefs"] = format_additionalrefs($params["additionalrefs_date"], $params["additionalrefs_duration"], $params["additionalrefs_project"], $params["additionalrefs_role"], $params["additionalrefs_desc"], $params["additionalrefs_tech"]);

 return $params;
}


//////////////////////////////////////////////////////////////////////////////
// cv actions
//////////////////////////////////////////////////////////////////////////////
function get_cv_action() {
  global $params, $actions, $path, $l_select_user;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult, $l_header_display, $l_header_admin, $l_header_duplicate;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin,$l_header_export;


// Ext Get Id
  $actions["cv"]["ext_get_id"] = array (
    'Url'      => "$path/cv/cv_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      );

// Index
  $actions["cv"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/cv/cv_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      );

// Search
  $actions["cv"]["search"] = array (
    'Url'      => "$path/cv/cv_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      );

// New
  $actions["cv"]["new"] = array (
    'Name'     => $l_header_new,
    'Url' => "$path/cv/cv_index.php?action=new",
   
    //'Url'      => "$path/user/user_index.php?action=ext_get_id&amp;ext_action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_select_user)."&amp;ext_url=".urlencode("$path/cv/cv_index.php?action=new&amp;user_id=")."",
    'Right'    => $cright_write,
    'Condition'=> array ('','search','index','detailconsult','admin','type_insert','type_update','type_delete','display','delete')
                                      );
                                      
// cv duplicate
  $actions["cv"]["duplicate"] = array (
    'Name'     => $l_header_duplicate,
    'Url'      => "$path/user/user_index.php?action=ext_get_id&amp;ext_action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_select_user)."&amp;ext_url=".urlencode("$path/cv/cv_index.php?action=duplicate&amp;cv_id=".$params["cv_id"]."&amp;user_id=")."",
    'Right'    => $cright_write,
    'Popup'    => 1, 
    'Condition'=> array ('detailconsult','detailupdate','update') 
                                       );

// Insert
  $actions["cv"]["insert"] = array (
    'Url'      => "$path/cv/cv_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                      );

// Detail Consult
  $actions["cv"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/cv/cv_index.php?action=detailconsult&amp;cv_id=".$params["cv_id"]."",
    'Right'    => $cright_read, 
    'Privacy'  => true,
    'Condition'=> array ('detailupdate', 'detailexport')
                                      );

// Detail Update
  $actions["cv"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/cv/cv_index.php?action=detailupdate&amp;cv_id=".$params["cv_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('detailconsult', 'update', 'detailexport') 
                                       );

// Update
  $actions["cv"]["update"] = array (
    'Url'      => "$path/cv/cv_index.php?action=update",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                      );

// Check Delete
  $actions["cv"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/cv/cv_index.php?action=check_delete&amp;cv_id=".$params["cv_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,  
    'Condition'=> array ('detailconsult', 'detailupdate', 'update') 
                                       );

// Delete
  $actions["cv"]["delete"] = array (
    'Url'      => "$path/cv/cv_index.php?action=delete&amp;cv_id=".$params["cv_id"]."",
    'Right'    => $cright_write,
    'Privacy'  => true,
    'Condition'=> array ('None') 
                                       );
                                       
// cv export form
  $actions["cv"]["detailexport"] = array (
    'Name'     => $l_header_export,
    'Url'      => "$path/cv/cv_index.php?action=detailexport&amp;cv_id=".$params["cv_id"]."",
    'Right'    => $cright_read,
    'Privacy'  => true,    
    'Condition'=> array ('detailconsult','detailupdate','update') 
                                       );
                                       
// cv export
  $actions["cv"]["export"] = array (
    'Url'      => "$path/cv/cv_index.php?action=export&amp;cv_id=".$params["cv_id"]."",
    'Right'    => $cright_read,
    'Privacy'  => true,    
    'Condition'=> array ('None') 
                                       );

// Default Odt Template Insert
  $actions["cv"]["defaultodttemplate_insert"] = array (
    'Url'      => "$path/cv/cv_index.php?action=defaultodttemplate_insert",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('None') 
            );
            
// Default Odt Template Insert
  $actions["cv"]["defaultodttemplate_update"] = array (
    'Url'      => "$path/cv/cv_index.php?action=defaultodttemplate_update",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('None') 
            );
            
// Default Odt Template Insert
  $actions["cv"]["defaultodttemplate_delete"] = array (
    'Url'      => "$path/cv/cv_index.php?action=defaultodttemplate_delete",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('None') 
            );
            
// Admin
  $actions["cv"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/cv/cv_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
            );                                    

// Display
  $actions["cv"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/cv/cv_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                          );

// Display Preference
  $actions["cv"]["dispref_display"] = array (
    'Url'      => "$path/cv/cv_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                            );

// Display Level
  $actions["cv"]["dispref_level"] = array (
    'Url'      => "$path/cv/cv_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                             );

}


//cv Actions updates (after processing, before displaying menu)  

function update_cv_action() {
  global $params, $actions, $path;

  $id = $params["cv_id"]; 
  if ($id > 0) {
    // Detail Consult
    $actions["cv"]["detailconsult"]['Url'] = "$path/cv/cv_index.php?action=detailconsult&amp;cv_id=$id";
    $actions["cv"]["detailconsult"]['Condition'][] = 'insert';
    
    // Detail Update
    $actions["cv"]["detailupdate"]['Url'] = "$path/cv/cv_index.php?action=detailupdate&amp;cv_id=$id";
    $actions["cv"]["detailupdate"]['Condition'][] = 'insert';

    // Check Delete
    $actions["cv"]["check_delete"]['Url'] = "$path/cv/cv_index.php?action=check_delete&amp;cv_id=$id";
    $actions["cv"]["check_delete"]['Condition'][] = 'insert';
  }
}

?>
