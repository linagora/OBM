<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/
?>
<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : report_index.php                                             //
//     - Desc : Report panel Index File                                      //
// 2009-04-07 Benoît Caudesaygues                                            //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index        --         -- show the system control board
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "report";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");

require("report_display.inc");
require("report_query.inc");

$acts = array('index','download_report');

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
$params = get_admin_params();

switch ($params["mode"]) {
 case "txt":
   include("$obminclude/global_pref.inc");
   $service = get_report_user_service();
   $delegation = get_report_delegation_user();
   $report = array(
    'UserReportCommand',
    'GroupReportCommand',
    'MailShareReportCommand',
    'UserArchiveReportCommand',
    'UserAliasReportCommand',
    'UserExpirationReportCommand',
    'UserMailQuotaReportCommand',
    'UserRedirectionReportCommand',
    'VacationReportCommand'
    );
   $retour = parse_report_arg($argv);
   if (! $retour) { end; }
   break;
 case "html":
   page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
   include("$obminclude/global_pref.inc");
   $service = get_report_user_service();
   $report = array(
    "$l_user_report"  => 'UserReportCommand',
    "$l_group_report" => 'GroupReportCommand',
    "$l_mailshare_report" => 'MailShareReportCommand',
    "$l_userarchive_report" => 'UserArchiveReportCommand',
    "$l_alias_report" => 'UserAliasReportCommand',
    "$l_expiration_report" => 'UserExpirationReportCommand',
    "$l_mailquota_report" => 'UserMailQuotaReportCommand',
    "$l_mailredirection_report" => 'UserRedirectionReportCommand',
    "$l_vacation_report" => 'VacationReportCommand'
    );
   if ($action == "") $action = "index";
   get_control_action();
   $perm->check_permissions($module, $action);
   $display["head"] = display_head($module);
   $display["header"] = display_menu($module);
   $u = get_user_info();
   $params['report_delegation'] = $u['delegation_target'];
   break;
}

switch ($action) {
  case "help":
    dis_report_help($params["mode"]);
    break;
  case "index":
    $display['detail'] = dis_report_index($params["mode"]);
    break;
  case "download_report":
    if (isset($params['type']) && $params['type'] != '') {
      $params['domain_id'] = $obm['domain_id'];
      $params['report_email'] = get_report_mail_admin($params);
      dis_download_report($params['type']);
      exit();
    } else {
      $display['msg'] = display_warn_msg($l_warn_download,false);
      $display['detail'] = dis_report_index($params["mode"]);
    }
    break;
  default:
    echo "No action specified !";
    break;
}


// Program End
switch ($params["mode"]) {
 case "txt":
   echo "bye...\n";
   break;
 case "html":
   //if ($action != 'download_report' || $params['type'] =='') {
   page_close();
   $display["end"] = display_end();
   $display["header"] = display_menu($module);
   display_page($display);
   /*} else {
     ob_clean();
}*/
   break;
}

///////////////////////////////////////////////////////////////////////////////
// Stores Admin parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_admin_params() {

  $params = get_global_params("report");

  if (($params["mode"] == "") || ($params["mode"] != "html")) {
    $params["mode"] = "txt";
  }

  return $params;
}

///////////////////////////////////////////////////////////////////////////////
// Display command use                                                       //
///////////////////////////////////////////////////////////////////////////////
function dis_report_command_use($msg="") {
  global $acts, $target_modules, $langs, $themes, $report, $service;
  global $delegation;

  while (list($nb, $val) = each ($acts)) {
    if ($nb == 0) $lactions .= "$val";
    else $lactions .= ", $val";
  }
  while (list($nb2, $val2) = each ($report)) {
    if ($nb2 == 0) $lreport .= "$val2";
    else $lreport .= ", $val2";
  }
  while (list($nb3, $val3) = each ($service)) {
    if ($nb3 == 0) $lservice .= "$val3";
    else $lservice .= ", $val3";
  }
  while (list($nb4, $val4) = each ($delegation)) {
    if ($nb4 == 0) $ldelegation .= "$val4";
    else $ldelegation .= ", $val4";
  }

  echo "$msg
Usage: $argv[0] [Options]
where Options:
-h, --help help screen
-a action  ($lactions)
-r report  ($lreport)
-d delegation  ($ldelegation)
-s service ($lservice)

Ex: php4 admin_index.php -a index
";
}

///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function parse_report_arg($argv) {
  global $debug, $acts, $report, $service, $delegation;
  global $params, $type, $action;

  // We skip the program name [0]
  next($argv);
  while (list ($nb, $val) = each ($argv)) {
    switch($val) {
    case '-h':
    case '--help':
      $params["action"] = "help";
      dis_report_help($params["mode"]);
      return true;
      break;
    case '-a':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $acts)) {
        $params["action"] = $val2;
        if ($debug > 0) { echo "-a -> \$action=$val2\n"; }
      } else {
	      dis_report_command_use("Invalid action ($val2)");
	      return false;
      }
      break;
    case '-r':
      list($nb3, $val3) = each ($argv);
      if (in_array($val3, $report)) {
        $params["type"] = $val3;
        if ($debug > 0) { echo "-r -> \$type=$val3\n"; }
      } else {
	      dis_report_command_use("Invalid report ($val3)");
	      return false;
      }
      break;
    case '-s':
      list($nb4, $val4) = each ($argv);
      if (in_array($val4, $service)) {
        $params["service"] = $val4;
        if ($debug > 0) { echo "-r -> \$type=$val4\n"; }
      } else {
	      dis_report_command_use("Invalid service ($val4)");
	      return false;
      }
      break;
    case '-d':
      list($nb5, $val5) = each ($argv);
      if (in_array($val5, $delegation)) {
        $params["report_delegation"] = $val5;
        if ($debug > 0) { echo "-d -> \$type=$val5\n"; }
      } else {
	      dis_report_command_use("Invalid delegation ($val5)");
	      return false;
      }
      break;
    }
  }

  if (! $params["action"]) $params["action"] = "index";
  $action = $params["action"];
}

///////////////////////////////////////////////////////////////////////////////
// Control Action 
///////////////////////////////////////////////////////////////////////////////
function get_control_action() {
  global $params, $actions, $path;
  global $l_header_consult;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["report"]["index"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/report/report_index.php?action=index&amp;mode=html",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

  $actions["report"]["download_report"] = array (
    'Url'      => "$path/report/report_index.php?action=download_report&amp;mode=html",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('none') 
                                    );
				    

}
</script>
