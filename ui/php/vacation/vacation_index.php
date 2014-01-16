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



?>
<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : vacation_index.php
//     - Desc : vacation Index File
// 2007-01-22 Pierre Baudracco
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "vacation";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_global_params('Entity');
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("vacation_display.inc");
require("vacation_query.inc");
require("vacation_js.inc");

$params = get_vacation_params();

if ($action == "") $action = "index";
get_vacation_action();
$perm->check_permissions($module, $action);


if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_vacation_detail($obm["uid"]);
  if ($obm_q->num_rows() == 1) {
    $display["detailInfo"] = display_record_info($obm_q);
    $display["detail"] = html_vacation_consult($obm_q) ;
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }
  
} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_vacation_detail($obm["uid"]);
  if ($obm_q->num_rows() == 1) {
    $display["detailInfo"] = display_record_info($obm_q);
    $display["detail"] = html_vacation_form($obm_q, $params);
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_vacation_data_form($params)) {
    $obm_q = run_query_vacation_detail($obm["uid"]);
    if (update_vacation($params, $obm_q)) {
      $display["msg"] .= display_ok_msg("$l_vacation : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_vacation : $l_update_error : " . $err["msg"]);
    }
  
    $obm_q = run_query_vacation_detail($obm["uid"]);
    $display["detailInfo"] = display_record_info($obm_q);
    $display["detail"] = html_vacation_consult($obm_q) ;
    
  } else {
    $display["msg"] = display_err_msg($err["field"] . " : " . $err["msg"]);
    $display["detailInfo"] = display_record_info($obm_q);
    $display["detail"] .= html_vacation_form(null, $params, $err);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display page
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_vacation);
if (! $params["popup"]) {
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Vacation parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_vacation_params() {

  // Get global params
  $params = get_global_params("vacation");

  if(isset ($params['vacation_datebegin'])) {
    $params['vacation_datebegin'] = of_isodate_convert($params['vacation_datebegin']);
    $params['vacation_datebegin'] = new Of_Date($params['vacation_datebegin']);
    $params['vacation_datebegin']->setHour($params["time_begin"])->setMinute($params["min_begin"])->setSecond(0);
  }

  if(isset ($params['vacation_dateend'])) {
    $params['vacation_dateend'] = of_isodate_convert($params['vacation_dateend']);
    $params['vacation_dateend'] = new Of_Date($params['vacation_dateend']);
    $params['vacation_dateend']->setHour($params["time_end"])->setMinute($params["min_end"])->setSecond(0);
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Forward Action 
///////////////////////////////////////////////////////////////////////////////
function get_vacation_action() {
  global $actions, $path;
  global $l_header_consult, $l_header_update;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["vacation"]["index"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/vacation/vacation_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

// Detail Update
  $actions["vacation"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/vacation/vacation_index.php?action=detailupdate",
     'Right'    => $cright_read,
     'Condition'=> array ('index', 'detailconsult', 'update') 
                                     	   );

// Update
  $actions["vacation"]["update"] = array (
    'Url'      => "$path/vacation/vacation_index.php?action=update",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     );
}


</script>
