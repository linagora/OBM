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
<?php

$path = '..';
$module = 'document';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
//FIXME
$params = get_global_params('Entity');
$cgp_cookie_name = 'OBM_Public_Session';
page_open(array('sess' => 'OBM_Session', 'auth' => 'OBM_Token_Auth', 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
require('document_query.inc');
get_document_action();
$perm->check_permissions($module, $action);

page_close();

// Main (and only) action
require 'document_display.inc';

if (!check_document_access_by_token($params['document_id'])) {
  die("$l_err_file_access_forbidden");
} else {
  $doc_q = run_query_document_detail($params['document_id']);
  if ($doc_q->num_rows() == 1) {
    dis_document_file($doc_q);
  } else {
    die("$l_no_document !");
  }
}

function check_document_access_by_token($document_id) {
  return $GLOBALS['token']['entity'] == 'document' && $GLOBALS['token']['entityId'] == $document_id;
}

function get_document_action() {
  global $actions, $path;
  global $cright_read;

  $actions["document"]["index"] = array(
    'Url'      => "$path/document/document_download.php",
    'Right'    => $cright_read,
    'Condition'=> array('None') 
  );
}
