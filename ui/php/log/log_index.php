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
// OBM - File : log_index.php                                                //
//     - Desc : log Index File                                               //
// 2007-07-02 AliaSource - Pierre Baudracco                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "log";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_param_log();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("log_query.inc");
require("log_display.inc");
require("$obminclude/javascript/check_js.inc");
require("$obminclude/of/of_right.inc");
require_once("$obminclude/of/of_category.inc");

get_log_action();
$perm->check_permissions($module, $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if ($action == "index") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_log_search_form($params);

} elseif ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  if ((is_array($params['sel_user_id']) && count($params['sel_user_id']) > 0)
      || (! is_array($params['sel_user_id']) && $params['sel_user_id'] != '')) {
    $display["search"] = dis_log_search_form($params);
    $display["result"] = dis_log_search_list($params);
  } else {
    $display["msg"] = display_warn_msg($l_param_empty);
    $display["search"] = dis_log_search_form($params);
  }

}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_log);
if (! $params["popup"]) {
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Log parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_log() {
  global $tf_filename, $tf_version, $tf_date, $param_backup, $popup; 

  // Get global params
  $params = get_global_params("log");

  // sel_user_id can be filled by sel_user_id or sel_ent (see below)
  if (is_array($params["user_id"])) {
    while (list($key, $value) = each($params["user_id"]) ) {
      // sel_user_id contains select infos (data-user-$id)
      if (strcmp(substr($value, 0, 10),"data-user-") == 0) {
        $data = explode("-", $value);
        $id = $data[2];
        $params["sel_user_id"][] = $id;
      } else {
        // direct id
        $params["sel_user_id"][] = $value;
      }
    }
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Log Action
///////////////////////////////////////////////////////////////////////////////
function get_log_action() {
  global $actions, $path;
  global $l_header_list, $l_header_find, $l_header_new, $l_header_delete;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index  
  $actions["log"]["index"] = array (
    'Name'     => $l_header_list,
    'Url'      => "$path/log/log_index.php?action=index",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                    	 );

// Search
  $actions['log']['search'] = array (
    'Url'      => "$path/log/log_index.php?action=search",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('None') 
                                    	 );

}

?>
