<?php
/******************************************************************************
Copyright (C) 2014 Linagora

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
// OBM - File : imap_archive_index.php                                       //
//     - Desc : User Backup Index File                                       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


$debug = 1;
$path = '..';
$module = 'imap_archive';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';

include_once("$obminclude/global.inc");

$params = get_global_params('Entity');
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
$params = get_imap_archive_params();
include_once("$obminclude/global_pref.inc");
require_once('imap_archive_display.inc');
require_once('imap_archive_query.inc');
require_once("$obminclude/of/of_category.inc");
require_once("$obminclude/of/of_right.inc");

if ($action == 'index') $action = 'detailconsult';

get_imap_archive_action();
$perm->check_permissions($module, $action);

update_last_visit('imap_archive', $params['user_id'], $action);

page_close();

$params['user_id'] = $obm['uid'];

$status = connect_to_imap_archive_service() ;
if ($status[0] != 1) {
  $display['msg'] .= display_err_msg("$status[1]");
} else {
  if ($action == 'detailconsult') {
    $display['detail'] = dis_imap_archive_consult($backup, $params);
  } elseif ($action == 'detailupdate') {
    $status = load_configuration_from_imap_archive_service();
    if ($status[0] != 1) {
      $display['msg'] .= display_err_msg("$status[1]");
    } else {
      $configuration = $status[1];
      $display['detail'] = dis_imap_archive_form($backup, $params, $configuration);
    }
  }
}

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head('imap_archive');
if (! $params['popup']) {
  $display['header'] = display_menu($module);
}
$display['end'] = display_end();

display_page($display);
        
///////////////////////////////////////////////////////////////////////////////
// Stores User parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_imap_archive_params() {
  
    // Get global params
    $params = get_global_params();

    return $params;
  }


///////////////////////////////////////////////////////////////////////////////
// IMAP Archive Action 
///////////////////////////////////////////////////////////////////////////////
function get_imap_archive_action() {
  global $actions, $cright_read_admin, $cright_write_admin;
  global $l_header_consult, $l_header_update;
  
// Consult
  $actions['imap_archive']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/imap_archive/imap_archive_index.php?action=detailconsult",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
  );

// Update
  $actions['imap_archive']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/imap_archive/imap_archive_index.php?action=detailupdate",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('all')
  );
}


?>
