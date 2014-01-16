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
