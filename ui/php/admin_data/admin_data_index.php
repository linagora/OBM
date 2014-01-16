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
// OBM - File : admin_data_index.php                                         //
//     - Desc : Update static database data (company contact number,...)     //
// 2002-06-28 - Pierre Baudracco                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions
// - index
// - data_show
// - data_update
// - sound_aka_update
///////////////////////////////////////////////////////////////////////////////
// Ce script s'utilise avec PHP en mode commande (php sous debian)          //
///////////////////////////////////////////////////////////////////////////////

$debug = 1;
$path = '..';
$module = 'admin_data';

$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';

include("$obminclude/global.inc");
require('admin_data_display.inc');
require('admin_data_query.inc');
require("$path/document/document_query.inc");
include("$obminclude/of/of_category.inc");

$target_modules = array ('company', 'deal', 'list', 'document');
$target_upd_modules = array ('company', 'deal', 'list', 'group','user');
$acts = array ('help', 'index', 'data_show', 'data_update', 'sound_aka_update');

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
$params = get_admin_data_params();

switch ($params['mode']) {
 case 'txt':
   // Check that this is not a fake txt attempt from a browser
   if (isset($_SERVER['SERVER_PROTOCOL']) && ($_SERVER['SERVER_PROTOCOL'] != '')) {
     echo "TXT mode can only be used from CLI !!";
     exit;
   }
   include("$obminclude/global_pref.inc"); 
   $retour = parse_admin_data_arg($argv);
   if (! $retour) { end; }
   break;
 case 'html':
   $debug = $_SESSION['set_debug'];
   page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
   include("$obminclude/global_pref.inc"); 
   if ($action == '') $action = 'index';
   get_admin_data_action();
   $perm->check_permissions($module, $action);
   $display['head'] = display_head('Admin_Data');
   $display['header'] = display_menu($module);
   echo $display['head'] . $display['header'] . "<p>&nbsp;</p>". $display['action'];
   break;
}


switch ($action) {
  case 'help':
    dis_admin_data_help($params['mode']);
    break;
  case 'index':
    dis_admin_data_index($params['mode'], $acts, $target_modules, $target_upd_modules);
    break;
  case 'data_show':
    dis_admin_data($action, $params['mode'], $params['target_module']);
    break;
  case 'data_update':
    dis_admin_data($action, $params['mode'], $params['target_module']);
    break;
  case 'sound_aka_update':
    dis_admin_data_sound_aka_update($params['mode']);
    break;
  default:
    echo 'No action specified !';
    break;
}

// Program End
switch ($params['mode']) {
 case 'txt':
   // echo "bye...\n";
   break;
 case 'html':
   page_close();
   $display['end'] = display_end();
   echo $display['end'];
   break;
}


///////////////////////////////////////////////////////////////////////////////
// Stores Admin parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_admin_data_params() {

  $params = get_global_params('admin_data');

  if (($params['mode'] == '') || ($params['mode'] != 'html')) {
    $params['mode'] = 'txt';
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function dis_admin_data_command_use($msg='') {
  global $acts, $target_modules;

  while (list($nb, $val) = each ($acts)) {
    if ($nb == 0) $lactions .= "$val";
    else $lactions .= ", $val";
  }
  while (list($nb, $val) = each ($target_modules)) {
    if ($nb == 0) $lmodules .= "$val";
    else $lmodules .= ", $val";
  }

  echo "$msg
Usage: $argv[0] [Options]
where Options:
-h, --help help screen
-a action  ($lactions)
-m module  ($lmodules)

Ex: php admin_data_index.php -a data_show -m company
";
}


///////////////////////////////////////////////////////////////////////////////
// Agrgument parsing                                                         //
///////////////////////////////////////////////////////////////////////////////
function parse_admin_data_arg($argv) {
  global $debug, $acts, $target_modules, $target_upd_modules;
  global $action, $params;

  // We skip the program name [0]
  next($argv);
  while (list ($nb, $val) = each ($argv)) {
    switch($val) {
    case '-h':
    case '--help':
      $params['action'] = 'help';
      dis_admin_data_help($params['mode']);
      return true;
      break;
    case '-m':
      list($nb2, $val2) = each ($argv);
      if ((in_array($val2, $target_modules))
	  || (in_array($val2, $target_upd_modules))) {
        $params['target_module'] = $val2;
        if ($debug > 0) { echo "-m -> \$target_module=$val2\n"; }
      }
      else {
        dis_admin_data_command_use("Invalid target_module ($val2)");
	return false;
      }
      break;
    case '-a':
      list($nb2, $val2) = each ($argv);
      if (in_array($val2, $acts)) {
        $params['action'] = $val2;
        if ($debug > 0) { echo "-a -> \$action=$val2\n"; }
      }
      else {
	dis_admin_data_command_use("Invalid action ($val2)");
	return false;
      }
      break;
    }
  }

  if (! $params['target_module']) $params['target_module'] = 'company';
  if (! $params['action']) $params['action'] = 'data_show';
  $action = $params['action'];
}


///////////////////////////////////////////////////////////////////////////////
// ADMIN DATA actions
///////////////////////////////////////////////////////////////////////////////
function get_admin_data_action() {
  global $actions, $path;
  global $l_header_index,$l_header_help;
  global $cright_read_admin, $cright_write_admin;

  // index
  $actions['admin_data']['index'] = array (
     'Name'     => $l_header_index,
     'Url'      => "$path/admin_data/admin_data_index.php?action=index&amp;mode=html",
     'Right'    => $cright_read_admin,
     'Condition'=> array ('all')
                                    	  );

 $actions['admin_data']['help'] = array (
     'Name'     => $l_header_help,
     'Url'      => "$path/admin_data/admin_data_index.php?action=help&amp;mode=html",
     'Right' 	=> $cright_read_admin,
     'Condition'=> array ('all')
                                    	);

 $actions['admin_data']['data_show'] = array (
     'Url'      => "$path/admin_data/admin_data_index.php?action=data_show&amp;mode=html",
     'Right' 	=> $cright_read_admin,
     'Condition'=> array ('None')
                                    	);

 $actions['admin_data']['data_update'] = array (
     'Url'      => "$path/admin_data/admin_data_index.php?action=data_update&amp;mode=html",
     'Right' 	=> $cright_write_admin,
     'Condition'=> array ('None')
                                    	);

 $actions['admin_data']['sound_aka_update'] = array (
     'Url'      => "$path/admin_data/admin_data_index.php?action=sound_search_update&amp;mode=html",
     'Right' 	=> $cright_write_admin,
     'Condition'=> array ('None')
                                    	);

}

?>
