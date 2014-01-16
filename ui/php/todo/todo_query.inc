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
// OBM - File : todo_query.inc                                               //
//     - Desc : todo query File                                              //
// 2003-15-09 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Todo search query execution
// Parametes:
//   - $todo[]       : company search criteria
//     keys used       uid
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_search($params) {
  global $obm, $cdg_sql, $ctu_sql_limit, $c_all;
  $obm_q = new DB_OBM;

  $sql_order_dir = $params['sql_order_dir'];
  $sql_order_field = $params['sql_order_field'];

  if ($params['text'] == '') $params['text'] = $params['title'];
  $text = sql_search_text_parse($obm_q->escape($params['text']));
  $priority = $params['priority'];
  $percent = $params['percent'];
  $date_field = ($params['date_field'] ? $params['date_field'] : 'date');
  $date_field = ($date_field == 'date_create')? 'timecreate' : $date_field;
  $dateafter = of_isodate_convert($params['date_after'],true);
  $datebefore = of_isodate_convert($params['date_before'],true);

  $uid = sql_parse_id($obm['uid'], true);
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'event_priority';
  $sql_order_dir = (strcmp($sql_order_dir,'') != 0) ? $sql_order_dir : 'desc';

  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $date = sql_date_format($db_type, 'event_date', 'event_date');
  $date_create = sql_date_format($db_type, 'event_timecreate', 'date_create');
  $multidomain = sql_multidomain('event');

  if ($text != '') {
    $where .= " AND (event_title $like '$text%' OR event_description $like '$text%')";
  }
  if (($priority != '') && ($priority != $c_all)) {
    $where .= " AND event_priority = '$priority'";
  }
  if ($percent != '' && is_numeric($percent)) {
    $where .= " AND eventlink_percent = '$percent'";
  }
  if ($dateafter != '') {
    $dateafter = new Of_Date($dateafter);
    $where .= " AND event_$date_field > '".$dateafter->getDateIso()."'";
  }
  if ($datebefore != '') {
    $datebefore = new Of_Date($datebefore);
    $where .= " AND event_$date_field < '".$datebefore->getDateIso()."'";
  }

  $fromq = 'FROM EventLink
    INNER JOIN Event on eventlink_event_id=event_id
    INNER JOIN UserEntity on eventlink_entity_id=userentity_entity_id';

  $whereq = "WHERE event_type = 'VTODO'
    AND userentity_user_id $uid
    $where
    $multidomain";

  // Order exceptions (order on calculated rows)
  if (strcmp($sql_order_field, 'event_update') == 0) {
    $order = 'event_priority';
  } else if (strcmp($sql_order_field,'') == 0) {
    $order = 'event_priority';
  }
  $orderq = " ORDER BY $order $sql_order_dir $order_next";

  $query = "SELECT
    event_id as id,
    event_id,
    $date,
    $date_create,
    event_priority,
    eventlink_percent,
    event_title
  $fromq
  $whereq
  $orderq
  $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("select count(*) $fromq $whereq");
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'todo_search()');
    $obm_q->query($query);
  }
  
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Todo detail query execution
// Parameters:
//   - $todo[]       : company search criteria
//     keys used       id
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_detail($todo) {
  global $cdg_sql;

  $t_id = sql_parse_id($todo['todo_id'], true);
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, 'event_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'event_timecreate', 'timecreate');
  $date = sql_date_format($db_type, 'event_date', 'event_date');
  $multidomain = sql_multidomain('event');
	
  $query = "
  SELECT
    Event.*,
    uu.userobm_login as userupdate,
    uc.userobm_login as usercreate,
    $timeupdate,
    $timecreate,
    $date,
    uc.userobm_lastname as author_firstname,
    uc.userobm_firstname as author_lastname,
    eventlink_percent
  FROM Event
    LEFT JOIN UserObm as uc ON event_usercreate = uc.userobm_id
    LEFT JOIN UserObm as uu ON event_userupdate = uu.userobm_id
    INNER JOIN EventLink ON eventlink_event_id = event_id
  WHERE event_id $t_id
    AND event_type='VTODO'
    $multidomain
    ";

  display_debug_msg($query, $cdg_sql, 'run_query_todo_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


/*
 * Get users associated with the todo
 * Parameters:
 * @param $id : todo id
 * @return $users array
 */
function get_todo_users($id) {
  global $cdg_sql;

  $users = array();
  $event_id = sql_parse_id($id, true);
  $query = "SELECT
    eventlink_percent,
    userobm_id,
    userobm_lastname,
    userobm_firstname
  FROM EventLink
    INNER JOIN UserEntity on eventlink_entity_id=userentity_entity_id
    INNER JOIN UserObm AS u on userentity_user_id=u.userobm_id
  WHERE eventlink_event_id $event_id";

  display_debug_msg($query, $cdg_sql, 'get_todo_users()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while ($obm_q->next_record()) {
    $uid = $obm_q->f('userobm_id');
    $lastname = $obm_q->f('userobm_lastname');
    $firstname = $obm_q->f('userobm_firstname');
    $percent = $obm_q->f('eventlink_percent');
    $users[$uid] = array('lastname' => $lastname,
			 'firstname' => $firstname,
			 'percent' => $percent);
  }
  return $users;
}


///////////////////////////////////////////////////////////////////////////////
// Todo insert query execution
// Parametes:
//   - $todo[]       : company search criteria
//     keys used       uid, title, sel_user, priority, date, description
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_insert($todo) {
  global $cdg_sql, $obm;

  $uid = sql_parse_id($obm['uid']);
  $title = $todo['title'];
  $users = $todo['user_id'];
  $priority = $todo['priority'];
  $percent = $todo['percent'];
  if ($percent == '') {
    $percent = '0';
  }
  $date = of_isodate_convert($todo['date'],true);
  $date = ($date)?"'$date'":'NULL';
  $description = $todo['description'];
  $domain_id = sql_parse_id($obm['domain_id']);

  $obm_q = new DB_OBM;
  $ext_id = generateUniqueKey();

  if (is_array($users) && (count($users)>0)) {
    $query = "INSERT INTO Event (
        event_ext_id,
        event_domain_id,
        event_timeupdate,
        event_timecreate,
        event_userupdate,
        event_usercreate,
        event_owner,
        event_type,
        event_origin,
        event_date,
        event_priority,
        event_title,
        event_description, 
        event_repeatkind)
      VALUES (
        '$ext_id',
        $domain_id,
        NOW(),
        NOW(),
        $uid,
        $uid,
        $uid,
        'VTODO',
        '$GLOBALS[c_origin_web]',
        $date,
        '".$obm_q->escape($priority)."',
        '".$obm_q->escape($title)."',
        '".$obm_q->escape($description)."',
	'none'
      )";
    display_debug_msg($query, $cdg_sql, 'run_query_todo_insert()');
    $retour = $obm_q->query($query);

    $id = $obm_q->lastid();
    if ($id > 0) {
      $entity_id = of_entity_insert('event', $id);

      foreach ($users as $user_id) {
        $user = str_replace('data-user-','',$user_id) ;
	// insert user link
	$query = "INSERT INTO EventLink (
          eventlink_timeupdate,
          eventlink_timecreate,
          eventlink_userupdate,
          eventlink_usercreate,
          eventlink_event_id,
          eventlink_entity_id,
          eventlink_state,
          eventlink_required,
          eventlink_percent)
        VALUES (
          NOW(),
          NOW(),
          '$uid',
          '$uid',
          '$id',
          ".of_entity_subquery('user', $user).",
          'ACCEPTED',
          'REQ',
          '".$obm_q->escape($percent)."')";
	display_debug_msg($query, $cdg_sql, 'run_query_todo_insert(2)');
	$retour = $obm_q->query($query);
      }
    }
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Todo update query execution
// Parametes:
//   - $todo[]   : company search criteria
//     keys used  id, uid, title, sel_user, priority, date, content
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_update($todo) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $todo_id = $todo['todo_id'];
  $t_id = sql_parse_id($todo['todo_id'], true);
  $uid = sql_parse_id($obm['uid']);
  $title = $todo['title'];
  $priority = $todo['priority'];
  $date = of_isodate_convert($todo['date'],true);
  if($date) {
    $date = new Of_Date($date);
  }
  $description = $todo['description'];
  $percent = $todo['percent'];
  if (!$percent) {
    $percent = '0';
  }
  $multidomain = sql_multidomain('event');
		
  $query = "UPDATE Event SET
    event_timeupdate = NOW(),
    event_userupdate = $uid,
    event_date = '$date',
    event_priority = '".$obm_q->escape($priority)."',
    event_title = '".$obm_q->escape($title)."',
    event_description = '".$obm_q->escape($description)."'
  WHERE event_id $t_id
    AND event_type='VTODO' $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_todo_update()');
  $retour = $obm_q->query($query);

  $user_ids = $todo['user_ids'];
  $users = get_todo_users($todo_id);

  // Remove the deleted user associations
  foreach($users as $u_id => $one_user) {
    if (! in_array($u_id, $user_ids)) {
      $query = "DELETE FROM EventLink
      WHERE eventlink_event_id=$todo_id
        AND eventlink_entity_id=".of_entity_subquery('user', $u_id);
      display_debug_msg($query, $cdg_sql, 'run_query_todo_update_userdelete()');
      $retour = $obm_q->query($query);
    }
  }

  // Add the new user associations
  foreach($user_ids as $u_id) {
    if (! array_key_exists($u_id, $users)) {
      $query = "INSERT INTO EventLink (
          eventlink_timeupdate,
          eventlink_timecreate,
          eventlink_userupdate,
          eventlink_usercreate,
          eventlink_event_id,
          eventlink_entity_id,
          eventlink_state,
          eventlink_required,
          eventlink_percent)
        VALUES (
          NOW(),
          NOW(),
          '$uid',
          '$uid',
          '$todo_id',
          ".of_entity_subquery('user', $u_id).",
          'ACCEPTED',
          'REQ',
          '".$obm_q->escape($percent)."')";
      display_debug_msg($query, $cdg_sql, 'run_query_todo_update_useradd()');
      $retour = $obm_q->query($query);
    }
  }
  
  // XXXXX Update the existing users (if percent has changed)
  $query = 'UPDATE EventLink SET eventlink_percent = '.$percent.' WHERE eventlink_event_id = '.$todo_id;
  display_debug_msg($query, $cdg_sql, 'run_query_todo_update_percent_update()');
  $retour = $obm_q->query($query);  
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Delete todo pass in $params
// Parameters:
//   - $parms : correponding to page parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_delete($params) {
  global $cdg_sql, $c_use_connectors, $obm;

  $uid = $obm['uid'];
  $multidomain = sql_multidomain('event');

  $obm_q = new DB_OBM;
  foreach($params as $key => $val) {
    $id_todo = strstr($key,'_');

    if ($id_todo != '') {
      $id_todo = substr($id_todo, 1);
      $sel_id = sql_parse_id($id_todo, true); 
      if ($val == 'on') {
        $query = "SELECT event_ext_id FROM Event WHERE event_type = 'VTODO' AND  event_id $sel_id";
	display_debug_msg($query, $cdg_sql, 'run_query_todo_delete(0)');
        $obm_q->query($query);
        $obm_q->next_record();
        $ext_id = $obm_q->f('event_ext_id');
        $query = "DELETE FROM Event WHERE event_type='VTODO' AND event_id $sel_id $multidomain";
        $obm_q->query($query);
	display_debug_msg($query, $cdg_sql, 'run_query_todo_delete(1)');
	// If connectors in use
	if ($c_use_connectors) {
          $ins_id = sql_parse_id($id_todo);
	  $query = "INSERT INTO DeletedEvent (
                      deletedevent_event_id,
                      deletedevent_event_ext_id,
                      deletedevent_user_id,
                      deletedevent_origin,
                      deletedevent_type,
                      deletedevent_timestamp)
            VALUES (
                      $ins_id,
                      '$ext_id',
                      $uid,
                      '$GLOBALS[c_origin_web]',
                      'VTODO',
                      NOW())";
	  display_debug_msg($query, $cdg_sql, 'run_query_todo_delete(2)');
	  $retour = $obm_q->query($query);
	}
      }    
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Delete one todo
// Parameters:
//   - $t_id = todo id
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_delete_unique($t_id) {
  global $cdg_sql, $c_use_connectors, $obm;

  $uid = $obm['uid'];
  $multidomain = sql_multidomain('event');
  $id = sql_parse_id($t_id, true);
  $obm_q = new DB_OBM;
  $query = "SELECT event_ext_id FROM Event WHERE event_type = 'VTODO' AND  event_id $id";
  display_debug_msg($query, $cdg_sql, 'run_query_todo_delete(0)');
  $obm_q->query($query);  
  $obm_q->next_record();
  $ext_id = $obm_q->f('event_ext_id');
  $query = "DELETE FROM Event WHERE event_type='VTODO' AND event_id $id $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_todo_delete_unique(1)');
  $ret = $obm_q->query($query);

  // If connectors in use
  if ($c_use_connectors) {
    $id = sql_parse_id($t_id);
    $query = "INSERT INTO DeletedEvent (
                      deletedevent_event_id,
                      deletedevent_event_ext_id,
                      deletedevent_user_id,
                      deletedevent_origin,
                      deletedevent_type,
                      deletedevent_timestamp)
            VALUES (
                      $id,
                      '$ext_id',
                      $uid,
                      '$GLOBALS[c_origin_web]',
                      'VTODO',
                      NOW())";
    display_debug_msg($query, $cdg_sql, 'run_query_todo_delete(2)');
    $retour = $obm_q->query($query);
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Todo Form Data checking and formatting
// Parameters:
//   - $todo[] : values checked
///////////////////////////////////////////////////////////////////////////////
function check_todo_data_form($todo) {
  global $php_regexp_percent, $l_err_percent, $l_j_fill_title;
  global $err, $obm;

  $t_id = $todo['todo_id'];
  $uid = $obm['uid'];
  $title = $todo['title'];
  $users = $todo['user_id'];
  $user = $users[0];
  $priority = $todo['priority'];
  $date = $todo['date'];
  $description = $todo['description'];
  $percent = $todo['percent'];

  // MANDATORY: Todo title
  if (trim($title) == '') {
    $err['msg'] = $l_j_fill_title;
    return false;
  }

  // Percent
  if (($percent != '') && (preg_match($php_regexp_percent, $percent) == 0)) {
    $err['msg'] = $l_err_percent . " : $percent";
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the todo can be deleted
// Parameters:
//   - $p_id : todo id
// Returns:
//   true if the todo can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_todo_can_delete($p_id) {
  global $ok_msg;

  $delete_ok = true;

  return $delete_ok;
}

?>
