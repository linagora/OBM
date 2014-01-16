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
// OBM - File : php/campaign/campaign_test.inc
//     - Desc : campaign test file
// 2009-02-11 Christophe Liou Kee On 
///////////////////////////////////////////////////////////////////////////////
// $Id:  $ //
///////////////////////////////////////////////////////////////////////////////

require_once 'obm_test.inc';

function test_campaign_module_admin () {
  global $display;
  
  $display['detail'] .= OBM_TestResult::getStyleAndJS();
  
  test_campaign_check_actions();
  test_campaign_ws();
  test_campaign_detailconsult();
}

function test_campaign_check_actions () {
  global $display;
  
  $campaign_index = file_get_contents('campaign_index.php');
  
  $display['msg'] .= display_info_msg('run_query_campaign_test_module_admin()');
  
  $params = array (
    'name' => 'tmp test campaign 1',
    'start_date' => date('Y-m-d', time() - 3600 * 24 * 3),
    'end_date' => date('Y-m-d', time() + 3600 * 24 * 5),
    'objective' => "Objective content date",
    'comment' => "Comment content date",
  );
  
  $context = new OBM_TestContext;
  
  $context->data = array(
    'params' => array(),
  );
  
  $source = OBM_Test::extract_action($campaign_index, 'new');
  $result = OBM_Test::exec($source, $context);
  $display['msg'] .= $result->toHTML('NEW FORM');
  
  $context->data = array(
    'params' => $params,
  );
  
  $source = OBM_Test::extract_action($campaign_index, 'insert');
  $result = OBM_Test::exec($source, $context);
  $display['msg'] .= $result->toHTML('INSERT');
  
  foreach (array('detailconsult', 'detailupdate', 'monitor') as $e) {
    $source = OBM_Test::extract_action($campaign_index, $e);
    $result = OBM_Test::exec($source, $context);
    $display['msg'] .= $result->toHTML(strtoupper($e));
  }
  
  $orig_context = clone $context;
  
  $source = OBM_Test::extract_action($campaign_index, 'duplicate');
  $result = OBM_Test::exec($source, $context);
  $display['msg'] .= $result->toHTML('DUPLICATE');
  
  $source = OBM_Test::extract_action($campaign_index, 'delete');
  $result = OBM_Test::exec($source, $context);
  $display['msg'] .= $result->toHTML('DELETE CLONE');
  
  $context = $orig_context;
  
  $source = OBM_Test::extract_action($campaign_index, 'delete');
  $result = OBM_Test::exec($source, $context);
  $display['msg'] .= $result->toHTML('DELETE');
  
  if (! is_dir($GLOBALS['cdocument_root'])) {
    $display['msg'] .= display_err_msg('OBM documents dir does not exist.');
  } else {
    if (is_writable($GLOBALS['cdocument_root'])) {
    } else {
      $display['msg'] .= display_err_msg('OBM documents dir is not writable.');
    }
  }
}

function test_campaign_detailconsult () {
  global $display;
  
  $display['msg'] .= display_info_msg('test_campaign_detailconsult()');
  
  $actionExec = new OBM_Test_ActionExecutor;
  
  $campaign_index = file_get_contents('campaign_index.php');
  $actionExec->setCode($campaign_index);
  
  $context = new OBM_TestContext(
    array(
      'params' => array (
        'name' => 'tmp test campaign 2',
        'start_date' => date('Y-m-d', time() - 3600 * 24 * 3),
        'end_date' => date('Y-m-d', time() + 3600 * 24 * 5),
        'objective' => "Objective content date",
        'comment' => "Comment content date",
      ),
    ));
  
  $actionExec->setContext($context);
  
  $result = $actionExec->exec('insert');
  $display['msg'] .= $result->toHTML('INSERT');
  
  $campaign_q = run_query_campaign_detail($actionExec->getContext()->data['params']['campaign_id']);
  $result = $actionExec->exec('detailconsult');
  
  $testHTML = new OBM_Test_HTML_Field($result->display['detail']);
  
  if (!$testHTML->assert_equals($GLOBALS['l_name'], $campaign_q->f('campaign_name'))) {
    $result->errors[] = 'error on campaign_name field';
  }
  if ($testHTML->assert_empty($GLOBALS['l_start_date'])) {
    $result->errors[] = 'error on campaign_start_date field';
  }
  if ($testHTML->assert_empty($GLOBALS['l_end_date'])) {
    $result->errors[] = 'error on campaign_end_date field';
  }
  
  $display['msg'] .= $result->toHTML('DETAILCONSULT');
  
  $result = $actionExec->exec('delete');
  $display['msg'] .= $result->toHTML('DELETE');
  
  /////////////////////////////////////////////////////////////////////////////////
  
  $context = new OBM_TestContext(
    array(
      'params' => array (
        'name' => 'tmp test campaign 3',
        'start_date' => '',
        'end_date' => '',
        'objective' => "Objective content date",
        'comment' => "Comment content date",
      ),
    ));
  
  $actionExec->setContext($context);
  
  $result = $actionExec->exec('insert');
  $display['msg'] .= $result->toHTML('INSERT');
  
  $campaign_q = run_query_campaign_detail($actionExec->getContext()->data['params']['campaign_id']);
  $result = $actionExec->exec('detailconsult');
  
  $testHTML = new OBM_Test_HTML_Field($result->display['detail']);
  if (!$testHTML->assert_equals($GLOBALS['l_name'], $campaign_q->f('campaign_name'))) {
    $result->errors[] = 'error on campaign_name field';
  }
  if (!$testHTML->assert_empty($GLOBALS['l_start_date'])) {
    $result->errors[] = 'error on campaign_start_date field';
  }
  if (!$testHTML->assert_empty($GLOBALS['l_end_date'])) {
    $result->errors[] = 'error on campaign_end_date field';
  }
  
  $display['msg'] .= $result->toHTML('DETAILCONSULT');
  
  $result = $actionExec->exec('delete');
  $display['msg'] .= $result->toHTML('DELETE');
}

function test_campaign_ws () {
  global $display, $path;
  
  require_once 'ws/classes.php';
  
  $obm_q = new OBM_Test_DB;
  
  $obm_q->query_rand("SELECT * FROM List WHERE list_query_nb < 300", 'list_id', 10);
  $counts = $obm_q->num_rows();
  
  $lists = array();
  while ($obm_q->next_record()) {
    $lists[] = array(
     'list_id' => $obm_q->f('list_id'),
     'query' => $obm_q->f('list_query'),
    );
  }
  
  $context = new OBM_TestContext;
  
  $context->data = array(
    'lists' => $lists,
    'obm_q' => new DB_OBM,
  );
  
  if (file_exists('ws/backport/campaignws_eachtargets.2.1.inc')) {
    $eachtargets2_1 = file_get_contents('ws/backport/campaignws_eachtargets.2.1.inc');
    $source = OBM_Test::extract_piece_of_code($eachtargets2_1, 'lists');
    $result = OBM_Test::exec($source, $context);
    $display['msg'] .= $result->toHTML("RETRIEVE $counts contacts FROM list_query");
  }
}

?>
