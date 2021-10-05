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
// OBM - File : php/campaign/campaign_query.inc
//     - Desc : campaign query file
// 2008-02-11 Christophe Liou Kee On
///////////////////////////////////////////////////////////////////////////////
// $Id:  $ //
///////////////////////////////////////////////////////////////////////////////


/*
 * Name : run_query_campaign_get_targets
 * Description : campaign collection query execution
 * Parameters :
 *    - integer $id : campaign id
 * Return : integer
 */
function run_query_campaign_get_targets ($id) {
  $obm_q = new DB_OBM;

  $db_type = $obm_q->type;

  $id = run_query_campaign_get_top_parent($id);

  $query = "
    SELECT
    group_id as source_id,
    campaigntarget_entity_id as entity_id,
    'Group' as entity,
    group_name as label
    FROM CampaignTarget
    INNER JOIN GroupEntity
    ON groupentity_entity_id = campaigntarget_entity_id
    INNER JOIN UGroup
    ON groupentity_group_id = group_id
    WHERE campaigntarget_campaign_id = '$id'

    UNION

    SELECT
    userobm_id as source_id,
    campaigntarget_entity_id as entity_id,
    'User' as entity,
    #CONCAT(userobm_firstname, ' ', userobm_lastname) as label
    FROM CampaignTarget
    INNER JOIN UserEntity
    ON userentity_entity_id = campaigntarget_entity_id
    INNER JOIN UserObm
    ON userentity_user_id = userobm_id
    WHERE campaigntarget_campaign_id = '$id'

    UNION

    SELECT
    list_id as source_id,
    campaigntarget_entity_id as entity_id,
    'List' as entity,
    list_name as label
    FROM CampaignTarget
    INNER JOIN ListEntity
    ON listentity_entity_id = campaigntarget_entity_id
    INNER JOIN List
    ON listentity_list_id = list_id
    WHERE campaigntarget_campaign_id = '$id'
    ";

  display_debug_msg($query, $GLOBALS['cdg_sql'], "run_query_campaign_get_targets()");

  $obm_q->xquery($query);

  $res = array();
  while ($obm_q->next_record()) {
    $res[] = array(
      'entity_id' => $obm_q->f('entity_id'),
      'entity' => $obm_q->f('entity'),
      'label' => $obm_q->f('label'),
      'source_id' => $obm_q->f('source_id'),
    );
  }

  return $res;
}

/*
 * Name : run_query_campaign_target_insert
 * Description : Insertion collection items query
 * Parameters :
 *    - string $entity_name : item attribute name
 *    - array $campaign_id : container id
 *    - array $target_ids : item ids to insert
 * Return : array
 */
function run_query_campaign_target_insert ($entity_name, $campaign_id, $target_ids) {
  $obm_q = new DB_OBM;

  $campaign_id = run_query_campaign_get_top_parent($campaign_id);

  foreach ($target_ids as $target_id) {
    $target_id = $target_id +0;
    $entity_id = of_entity_get($entity_name, $target_id);
    $query = "INSERT INTO CampaignTarget (campaigntarget_campaign_id, campaigntarget_entity_id)
      VALUES ('$campaign_id', '$entity_id')";

    display_debug_msg($query, $GLOBALS['cdg_sql'], "run_query_campaign_target_insert()");
    $obm_q->query($query);
  }
}

/*
 * Name : run_query_campaign_target_delete
 * Description : Delete collection items query
 * Parameters :
 *    - string $entity_name : item attribute name
 *    - array $target_ids : item ids to delete
 * Return : array
 */
function run_query_campaign_target_delete ($campaign_id, $target_ids) {
  $campaign_id = run_query_campaign_get_top_parent($campaign_id);

  $obm_q = new DB_OBM;

  if (count($target_ids) != 0) {
    $query = "DELETE FROM CampaignTarget
      WHERE campaigntarget_campaign_id = '$campaign_id'
      AND campaigntarget_entity_id IN ('" . join("','", $target_ids) . "')
      ";

    display_debug_msg($query, $GLOBALS['cdg_sql'], "run_query_campaign_target_delete()");
    $obm_q->query($query);
  }
}


/*
 * Name : run_query_campaign_update
 * Description : Insertion query construction
 * Parameters :
 *    - array $params : campaign data to insert
 *    - array $col_names_to_update : names of attributes to update
 * Return : array
 */
function run_query_campaign_update ($params, $col_names_to_update) {
  global $cdg_sql, $obm;
  $obm_q = new DB_OBM;

  $id = $params['campaign_id'];

  $old_obm_q = run_query_campaign_detail($id);

  foreach (array('name', 'objective', 'comment') as $e) {
    $col[$e] = "'" . sql_search_text_parse($obm_q->escape($params[$e])) . "'";
  }

  foreach (array('manager_id', 'parent') as $e) {
    $col[$e] = "'" . ($params[$e] +0) . "'";
  }

  foreach (array('start_date', 'end_date') as $e) {
    if (isset($params[$e]) && !empty($params[$e]))
      $col[$e] = "'" . (of_isodate_convert($params[$e])) . "'";
    else
      $col[$e] = 'null';
  }

  $mquery = array();

  if (in_array('email', $col_names_to_update) && isset($params['fi_email'])) {
    $old_document_id = $old_obm_q->f('campaign_email');

    if ($old_document_id)
      run_query_global_delete_document($old_document_id);

    // run_query_global_insert_document function inconsistence
    $params['fi_email']['campaign_id'] = $id;
    $mime_id = run_query_campaign_get_type_by_ext($_FILES['fi_email']['type']);
    $params['fi_email']['type'] = ($mime_id ? $mime_id : '0');    
    $document_id = run_query_global_insert_document($params['fi_email'], $id, 'campaign');
    $d_id = sql_parse_id($document_id);
    $mquery[] = "campaign_email = $d_id";
    // echo  "campaign_email = '". run_query_global_insert_document($params['fi_email'], $id, 'campaign'). "'";;
  }

  foreach ($col_names_to_update as $e) {
    if (isset($col[$e])) $mquery[] = "campaign_$e = $col[$e]";
  }
  $query = join(', ', $mquery). ", campaign_userupdate = '$obm[uid]',
    campaign_timeupdate = now()";

  if (!empty($mquery)) {
    $query = "UPDATE Campaign SET $query
      WHERE campaign_id = '$id'
      AND campaign_domain_id = $obm[domain_id]";

    display_debug_msg($query, $cdg_sql, "run_query_campaign_update()");
    $obm_q->query($query);
  }

  // Changement de statut d'une campagne

  //~ Statut ' Crée ' appliqué lors de la création de la campagne

  //~ Passage au statut 'Prête' lorsque la campagne comprend :
  //~ - un fichier .eml uploadé
  //~ - au moins une cible

  //~ Passage au statut 'Planifiée'  lorsque :
  //~ - statut 'Prête'
  //~ - date de début de campagne

  global $c_campaign_status_enum;

  $new_obm_q = run_query_campaign_detail($id);

  if ($new_obm_q && $new_obm_q->f('campaign_email')) {
    $new_status = $c_campaign_status_enum['ready'];
    $targets = run_query_campaign_get_targets($id);

    if ($new_obm_q->f('campaign_start_date') && count($targets) != 0) {
      $new_status = $c_campaign_status_enum['planified'];
    }

    if ($new_status != $new_obm_q->f('campaign_status')) {
      $query = "UPDATE Campaign SET campaign_status = '".$obm_q->escape($new_status)."' WHERE campaign_id = '$id'";
      $obm_q->query($query);
      display_debug_msg($query, $cdg_sql, "run_query_campaign_update(update status)");
    }
  }

  return true;
}


/*
 * Name : run_query_campaign_search
 * Description : campaign search query
 * Parameters :
 *    - array $params : campaign search criterias value
 *    - array $criterias : criterias properties to search on
 * Return : array
 */
function run_query_campaign_search ($params, $criterias) {
  global $cdg_sql, $c_all, $ctu_sql_limit;

  $obm_q = new DB_OBM;

  $multidomain = sql_multidomain('campaign');
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);

  $col = array();

  foreach (array('name', 'refer_url', 'type', 'objective', 'comment') as $e) {
    $$e = "LIKE '%" . sql_search_text_parse($obm_q->escape($params[$e])) . '%\'';
  }

  $start_date = " = '".of_isodate_convert($params['start_date'],true). "'";
  $end_date = " = '".of_isodate_convert($params['end_date'],true). "'";

  $all_cols = array('name', 'refer_url', 'start_date', 'end_date',
    'type', 'objective', 'comment');

  if (empty($params['search_all'])) {
    $col[] = '1=1';
    foreach ($all_cols as $e) {
      if (isset($params[$e]) && in_array($e, $criterias))
        $col[$e] = "campaign_$e ${$e}";
    }
    $where = 'WHERE '. join($col, ' AND ');

  } else  {
    $col[] = '1=0';
    foreach ($all_cols as $e) {
      if (in_array($e, $criterias))
        $col[$e] = "campaign_$e ${$e}";
    }
    $where = 'WHERE '. join($col, ' OR ');
  }

  if (isset($params['sql_order_field']))
    $order = "ORDER BY $params[sql_order_field] $params[sql_order_dir]";

  if (!(isset($params['archive'])) || $params['archive'] != 1) {
    $where .= ' AND campaign_status != '. $GLOBALS['c_campaign_status_enum']['archived'];
  }

  $query = "SELECT
    Campaign.*,
    #TIMESTAMP(campaign_start_date) as campaign_start_date,
    #TIMESTAMP(campaign_end_date) as campaign_end_date

    FROM Campaign
    $where
    AND campaign_parent IS NULL
    $multidomain
    $order
    $limit
    ";

  if ($ctu_sql_limit) {
    $cq = "SELECT count(*) FROM Campaign $where $multidomain";
    $count = get_query_count($cq);
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, "run_query_campaign_search()");

    $obm_q->xquery($query);
  }

  return($obm_q);

}

/**
 * Create prefs campaign query
 */
/*
///////////////////////////////////////////////////////////////////////////////
// campaign's query functions
///////////////////////////////////////////////////////////////////////////////

/*
 * Name : run_query_campaign_insert
 * Description : Insertion query construction
 * Parameters :
 *    - array $params : campaign data to insert
 *    - array $col_names_to_update : names of attributes to insert
 * Return : array
 */
function run_query_campaign_insert ($params, $col_names_to_update) {
  global $obm, $cdg_sql;

  $obm_q = new DB_OBM;

  $status = $GLOBALS['c_campaign_status_enum']['created'];

  $query = "INSERT INTO Campaign (
    campaign_domain_id,
    campaign_usercreate,
    campaign_status,
    campaign_progress)
    VALUES (
      $obm[domain_id],
      '$obm[uid]',
      '".$obm_q->escape($status)."',
      0)";

  display_debug_msg($query, $cdg_sql, "run_query_campaign_insert()");
  $obm_q->query($query);

  $insert_id = $obm_q->lastid();

  if ($insert_id) {
    of_entity_insert($GLOBALS['module'], $insert_id);
    $params['campaign_id'] = $insert_id;
    run_query_campaign_update ($params, $col_names_to_update);
  } else {
    $insert_id = false;
  }

  return $insert_id;
}


/*
 * Name : check_campaign_form
 * Description : campaign Form Data checking and formatting
 * Parameters :
 *    - integer $cid : campaign id : (empty on insertion)
 *    - array $params : campaign data content to check
 *    - array $attribs : names of data attributes to check
 * Return : array
 */
function check_campaign_form ($cid, $params, $attribs = null) {
  global $err;
  $lang = &$GLOBALS;

  if (in_array('name', $attribs) && trim($params['name']) == '') {
    $err['msg'] = $lang['l_err_name_empty'];
    return false;
  }

  if (in_array('start_date', $attribs) && in_array('end_date', $attribs)
    && trim($params['start_date']) != '' && trim($params['end_date']) != ''
      && check_compare_convert_to_value (of_isodate_convert($params['start_date']))
        > check_compare_convert_to_value (of_isodate_convert($params['end_date']))) {
          $err['msg'] = $lang['l_err_dateanteriority'];
          return false;
        }

  return true;
}


/*
 * Name : run_query_campaign_duplicate
 * Description : Duplicate query construction
 * Parameters :
 *    - array $oid : campaign id to duplicate
 * Return : array
 */
function run_query_campaign_duplicate ($oid) {
  $id = run_query_campaign_insert(array(), array());

  if ($id) {
    $campaign_q = run_query_campaign_detail($oid);

    $params = array();
    $cols = array();
    foreach (array('name', 'manager_id', 'start_date', 'end_date',
      'comment', 'objective') as $e) {
        if ($campaign_q->f("campaign_$e")) {
          $params[$e] = $campaign_q->f("campaign_$e");
          $cols[] = $e;
        }
      }

    $params['campaign_id'] = $id;

    run_query_campaign_update($params, $cols);

    /*
    $obm_q = new DB_OBM;

    $query = array();
    foreach (array('name', 'manager_id', 'start_date', 'end_date',
      'comment', 'objective', 'domain_id') as $e) {
        if ($campaign_q->f("campaign_$e"))
          $query[] = "campaign_$e = '". str_replace('\'', '\\\'', $campaign_q->f("campaign_$e")). "'";
      }
    $query = join($query, ',');
    $obm_q->query("UPDATE Campaign SET $query WHERE campaign_id = '$id'");
 */

$targets = run_query_campaign_get_targets($oid);
foreach ($targets as $target) {
  run_query_campaign_target_insert($target['entity'], $id, array($target['source_id']));
}
  }

  return $id;
}


/*
 * Name : run_query_campaign_detail
 * Description : campaign detail query execution
 * Parameters :
 *    - integer $id : campaign id
 * Return : DB_OBM
 */
function run_query_campaign_detail ($id) {
  global $cdg_sql;

  $id = $id +0;

  $obm_q = new DB_OBM;

  $db_type = $obm_q->type;

  $multidomain = sql_multidomain("campaign"); 
  $query = "SELECT
    Campaign.*,
    #TIMESTAMP(campaign_start_date) as campaign_start_date,
    #TIMESTAMP(campaign_end_date) as campaign_end_date,
    manager.userobm_firstname as campaign_manager_firstname,
    manager.userobm_lastname as campaign_manager_lastname

    FROM Campaign
    LEFT JOIN UserObm manager ON userobm_id = campaign_manager_id
    WHERE campaign_id = '$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_campaign_detail()");
  $obm_q->xquery($query);
  $obm_q->next_record();
  return $obm_q;
}


/**
 * @param Integer $campaign_id
 * @return DB_OBM
 */
function run_query_campaign_get_subcampaigns ($campaign_id) {
  $obm_q = new DB_OBM;

  $query = "SELECT campaign_id, campaign_name FROM Campaign WHERE campaign_parent = $campaign_id";

  display_debug_msg($query, $GLOBALS['cdg_sql'], "run_query_campaign_get_children()");
  $obm_q->query($query);

  return $obm_q;
}


function run_query_campaign_get_top_parent ($campaign_id) {
  $obm_q = new DB_OBM;

  $query = "SELECT campaign_parent FROM Campaign WHERE campaign_id = '$campaign_id'";
  display_debug_msg($query, $GLOBALS['cdg_sql'], "run_query_campaign_get_top_parent()");
  $obm_q->query($query);
  $obm_q->next_record();

  if ($obm_q->f('campaign_parent'))
    return run_query_campaign_get_top_parent($obm_q->f('campaign_parent'));
  else
    return $campaign_id;
}


/*
 * Name : run_query_campaign_delete
 * Description : delete the campaign from Campaign
 * Parameters :
 *    - integer $id : campaign id to delete
 * Return : integer
 */
function run_query_campaign_delete ($id) {
  global $cdg_sql;

  $old_obm_q = run_query_campaign_detail($id);
  $obm_q = new DB_OBM;
  of_entity_delete($GLOBALS['module'], $id);
  $query = "DELETE FROM Campaign WHERE campaign_id='$id'";

  display_debug_msg($query, $cdg_sql, "run_query_campaign_delete(1)");
  $ret = $obm_q->query($query);
  $old_document_id = $old_obm_q->f('campaign_email');
  if ($old_document_id)
    run_query_global_delete_document($old_document_id);
  if ($ret) {
    $query = "DELETE FROM CampaignTarget WHERE campaigntarget_campaign_id = '$id'
      $multidomain";

    display_debug_msg($query, $cdg_sql, 'run_query_campaign_delete(2)');
    $obm_q->query($query);
  }

  return $ret;
}


/**
 * Check if the campaign can be deleted
 * @param integer $p_id campaign id
 * @return bool true if the campaign can be deleted, else false
 */
function check_campaign_can_delete($p_id) {
  global $ok_msg;

  $delete_ok = true;

  // no links

  return $delete_ok;
}


function check_compare_convert_to_value ($str) {
  if (preg_match('/..\/..\/../', $str)) {
    list($month, $day, $year) = explode('/', $str);
    return mktime(0,0,0, $month, $day, $year);
  }
  return $str;
}

///////////////////////////////////////////////////////////////////////////////
// Get the Mime Type of a file
// Parameters:
//   - $ext : Extension 
///////////////////////////////////////////////////////////////////////////////
function run_query_campaign_get_type_by_ext($ext) {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  $multidomain = sql_multidomain("documentmimetype");

  $query = "SELECT *
    FROM DocumentMimeType
    WHERE documentmimetype_extension ='".$obm_q->escape($ext)."'
    $multidomain";
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, "run_query_campaign_get_type_by_ext()");
  if ($obm_q->nf() > 0) {
    $obm_q->next_record();
    return $obm_q->f("documentmimetype_id");
  } else {
    return false;
  }
}

function can_update_campaign($campaign_q) {
  global $c_campaign_status_enum;

  return !($campaign_q->f('campaign_status') == $c_campaign_status_enum['running']
    || $campaign_q->f('campaign_status') == $c_campaign_status_enum['finished']
    || $campaign_q->f('campaign_status') == $c_campaign_status_enum['archived']);
}

function run_query_campaign_archive ($campaign_id) {
  global $cdg_sql, $c_campaign_status_enum;

  $campaign_id = $campaign_id +0;

  $obm_q = new DB_OBM;

  $query = "UPDATE Campaign SET campaign_status = '"
    . $c_campaign_status_enum['archived']. "' WHERE campaign_id = '$campaign_id'";

  display_debug_msg($query, $cdg_sql, "run_query_campaign_archive()");

  $obm_q->query($query);
}

function run_query_campaign_unarchive ($campaign_id) {
  global $c_campaign_status_enum;

  $campaign_id = $campaign_id +0;

  $obm_q = new DB_OBM;

  $query = "UPDATE Campaign
    SET campaign_status = '$c_campaign_status_enum[finished]'
    WHERE campaign_id = '$campaign_id'";

  display_debug_msg($query, $GLOBALS['cdg_sql'], "run_query_campaign_unarchive()");

  $obm_q->query($query);
}

function run_query_push_mail_campaign ($campaign_id) {
  require 'ws/classes.php';

  $campaign_q = run_query_campaign_detail($campaign_id);
  $campaign_q->f('campaign_email');
}

/**
 * @param Integer $campaign_id
 * @return DB_OBM
 */
function run_query_campaign_get_mailtargets ($campaign_id) {
  $obm_q = new DB_OBM;

  $query =  "
    SELECT
    campaignmailtarget_status,
    userobm_id as source_id,
    userobm_email as email,
    campaignmailtarget_entity_id as entity_id,
    'User' as entity,
    #CONCAT(userobm_firstname, ' ', userobm_lastname) as label
    FROM CampaignMailTarget
    INNER JOIN UserEntity
    ON userentity_entity_id = campaignmailtarget_entity_id
    INNER JOIN UserObm
    ON userentity_user_id = userobm_id
    WHERE campaignmailtarget_campaign_id = '$campaign_id'
    ";

  display_debug_msg($query, $GLOBALS['cdg_sql'], "run_query_campaign_get_mailtarget()");

  $obm_q->xquery($query);

  return $obm_q;
}

$c_campaign_mailtarget_status = array(
  'in_queue' => 1,
  'sent' => 2,
  'error' => 3,

);

function run_query_campaign_import_sent_emails () {
  global $c_campaign_mailtarget_status;
  global $c_campaign_status_enum;

  $obm_q = new DB_OBM;
  $obm_q2 = new DB_OBM;

  $query = "SELECT CampaignPushTarget.*,
    campaignmailcontent_refext_id as campaign_id
    FROM CampaignPushTarget
    INNER JOIN CampaignMailContent ON campaignpushtarget_mailcontent_id = campaignmailcontent_id
    WHERE
    campaignpushtarget_status != '$c_campaign_mailtarget_status[in_queue]'";

  display_debug_msg($query, $GLOBALS['cdg_sql'], "run_query_campaign_import_sent_emails(get pushtarget)");

  $obm_q->query($query);

  while ($obm_q->next_record()) {
    foreach (explode("\n", $obm_q->f('campaignpushtarget_properties')) as $line) {
      list($var, $value) = explode('=', $line);
      if ($var == 'id')
        $entity_id = $value +0;

      if ($var == 'type')
        $entity = $value;
    }
    $status = $obm_q->f('campaignpushtarget_status');
    $campaign_id = $obm_q->f('campaign_id');

    $query = "INSERT INTO CampaignMailTarget (
      campaignmailtarget_campaign_id,
      campaignmailtarget_entity_id,
      campaignmailtarget_status)
      VALUES (
        '$campaign_id',
        '$entity_id',
        '".$obm_q->escape($status)."')";


    display_debug_msg($query, $GLOBALS['cdg_sql'], "run_query_campaign_import_sent_emails(insert mailtarget)");
    $obm_q2->query($query);

    $query = "DELETE FROM CampaignPushTarget WHERE campaignpushtarget_id = '"
      . $campaign_id = $obm_q->f('campaignpushtarget_id'). "'";

    display_debug_msg($query, $GLOBALS['cdg_sql'], "run_query_campaign_import_sent_emails(delete pushtarget)");
    $obm_q2->query($query);
  }

  $query = "SELECT
    campaignmailcontent_refext_id,
    campaignpushtarget_id,
    campaignmailcontent_id
    FROM CampaignMailContent
    LEFT JOIN CampaignPushTarget
    ON campaignmailcontent_id = campaignpushtarget_mailcontent_id
    ";

  display_debug_msg($query, $GLOBALS['cdg_sql'], "run_query_campaign_import_sent_emails(check finished campaign)");

  $obm_q->query($query);

  while ($obm_q->next_record()) {
    if (!$obm_q->f('campaignpushtarget_id')) {
      $query = "DELETE FROM CampaignMailContent
        WHERE campaignmailcontent_id = ". $obm_q->f('campaignmailcontent_id');
      display_debug_msg($query, $GLOBALS['cdg_sql'], "run_query_campaign_import_sent_emails(delete mailcontent)");
      $obm_q->query($query);

      $query = "UPDATE Campaign
        SET campaign_status = '$c_campaign_status_enum[finished]'
        WHERE campaign_id = ". $obm_q->f('campaignmailcontent_refext_id');
      display_debug_msg($query, $GLOBALS['cdg_sql'], "run_query_campaign_import_sent_emails(delete mailcontent)");
      $obm_q->query($query);
    }
  }
}

?>
