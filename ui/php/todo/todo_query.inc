<?php
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

  $sql_order_dir = $params['sql_order_dir'];
  $sql_order_field = $params['sql_order_field'];

  $text = sql_search_text_parse($params['text']);
  $priority = $params['priority'];
  $percent = $params['percent'];
  $date_field = ($params['date_field'] ? $params['date_field'] : 'deadline');
  if ($date_field == 'date') {
    $date_field = 'date';
  }
  $dateafter = of_isodate_convert($params['date_after'],true);
  $datebefore = of_isodate_convert($params['date_before'],true);

  $uid = sql_parse_id($obm['uid'], true);
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'todo_priority';
  $sql_order_dir = (strcmp($sql_order_dir,'') != 0) ? $sql_order_dir : 'desc';

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $date = sql_date_format($db_type, 'event_date', 'todo_date');
  $date_create = sql_date_format($db_type, 'event_timecreate', 'date_create');
  $multidomain = sql_multidomain('event');

  if ($text != '') {
    $where .= " AND (todo_title $like '$text%' OR event_description $like '$text%')";
  }
  if (($priority != '') && ($priority != $c_all)) {
    $where .= " AND todo_priority = '$priority'";
  }
  if ($percent != '') {
    $where .= " AND todo_percent > '$percent'";
  }
  if ($dateafter != '') {
    $where .= " AND event_$date_field > '$dateafter'";
  }
  if ($datebefore != '') {
    $where .= " AND event_$date_field < '$datebefore'";
  }

  $fromq = 'FROM EventLink
    INNER JOIN Event on eventlink_event_id=event_id
    INNER JOIN UserEntity on eventlink_entity_id=userentity_entity_id';

  $whereq = "WHERE event_type = 'VTODO'
    AND userentity_user_id $uid
    $where
    $multidomain";

  // Order exceptions (order on calculated rows)
  if (strcmp($sql_order_field, 'todo_update') == 0) {
    $order = 'todo_priority';
  } else if (strcmp($sql_order_field,'') == 0) {
    $order = 'todo_priority';
  }
  $orderq = " ORDER BY $order $sql_order_dir $order_next";

  $query = "SELECT
    event_id as id,
    event_id,
    $date,
    $date_create,
    event_priority as todo_priority,
    eventlink_percent as todo_percent,
    event_title as todo_title
  $fromq
  $whereq
  $orderq
  $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("select count(*) $fromq $whereq");
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
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
  $date = sql_date_format($db_type, 'event_date', 'todo_date');
  $multidomain = sql_multidomain('event');
	
  $query = "
  SELECT
    Event.*,
    uu.userobm_login as userupdate,
    uc.userobm_login as usercreate,
    $timeupdate,
    $timecreate,
    $date,
    eventlink_percent,
    u.userobm_lastname as user_firstname,
    u.userobm_firstname as user_lastname,
    Author.userobm_lastname as author_firstname,
    Author.userobm_firstname as author_lastname
  FROM Event
    LEFT JOIN UserObm as uc ON event_usercreate = uc.userobm_id
    LEFT JOIN UserObm as uu ON event_userupdate = uu.userobm_id
    INNER JOIN EventLink on eventlink_event_id=event_id
    INNER JOIN UserEntity on eventlink_entity_id=userentity_entity_id
    INNER JOIN UserObm AS u on userentity_user_id=u.userobm_id,
    UserObm as Author
  WHERE event_id $t_id
    AND event_type='VTODO'
    $multidomain
    AND Author.userobm_id = event_usercreate";

  display_debug_msg($query, $cdg_sql, 'run_query_todo_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Todo insert query execution
// Parametes:
//   - $todo[]       : company search criteria
//     keys used       uid, title, sel_user, priority, deadline, description
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_insert($todo) {
  global $cdg_sql, $obm;

  $now = date('Y-m-d H:i:s');
  $uid = sql_parse_id($obm['uid']);
  $title = $todo['title'];
  $users = $todo['userid'];
  $priority = $todo['priority'];
  $percent = $todo['percent'];
  if ($percent == '') {
    $percent = '0';
  }
  $date = of_isodate_convert($todo['date'],true);
  $description = $todo['description'];
  $domain_id = sql_parse_id($obm['domain_id']);

  $obm_q = new DB_OBM;

  if (is_array($users) && (count($users)>0)) {
    $query = "INSERT INTO Event (
        event_domain_id,
        event_timeupdate,
        event_timecreate,
        event_userupdate,
        event_usercreate,
        event_type,
        event_origin,
        event_date,
        event_deadline,
        event_priority,
        event_percent,
        event_title,
        event_description)
      VALUES (
        $domain_id,
        '$now',
        '$now',
        $uid,
        $uid,
        'VTODO',
        'ui',
        $user,
        '$now',
        '$deadline',
        '$priority',
        '$percent',
        '$title',
        '$description'
      )";
    display_debug_msg($query, $cdg_sql, 'run_query_todo_insert()');
    $retour = $obm_q->query($query);

    foreach ($users as $user) {
      $user = sql_parse_id($user);
      // insert user link
    }

  }





  if ($retour) {
    if (in_array($obm['uid'], $users)) {
      $where_user = "AND todo_user ='$uid'";
    } else {
      $where_user = "";
    }
    $uid = sql_parse_id($obm['uid'], true);
    $query = "SELECT MAX(todo_id) as todo_id
        FROM Todo WHERE 
        1=1
        $where_user
        AND todo_usercreate $uid";

    display_debug_msg($query, $cdg_sql, 'run_query_todo_insert(2)');
    $obm_q->query($query);
    $obm_q->next_record();
    $id = $obm_q->f('todo_id');
  }

  if (in_array($obm['uid'], $users)) {
    $retour = $id;
  } else {
    $retour = 'other_user';
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Todo update query execution
// Parametes:
//   - $todo[]       : company search criteria
//     keys used       id, uid, title, sel_user, priority, deadline, content
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_update($todo) {
  global $obm, $cdg_sql;

  $now = date('Y-m-d H:i:s');

  $t_id = sql_parse_id($todo['todo_id'], true);
  $uid = sql_parse_id($obm['uid']);
  $title = $todo['title'];
  $users = $todo['userid'];
  $user = sql_parse_id($users[0]);
  $priority = $todo['priority'];
  $date = of_isodate_convert($todo['date'],true);
  $description = $todo['description'];
  $percent = $todo['percent'];
  if ($percent == '') {
    $percent = '0';
  }

  $multidomain = sql_multidomain('todo');
		
  $query = "UPDATE Event SET
    event_timeupdate = '$now',
    event_userupdate = $uid,
    event_date = '$date',
    event_priority = '$priority',
    event_title = '$title',
    event_description = '$description'
  WHERE event_id $t_id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_todo_update()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Delete todo pass in $params
// Parameters:
//   - $parms : correponding to page parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_todo_delete($parms) {
  global $cdg_sql, $c_use_connectors;
  
  $multidomain = sql_multidomain('todo');
	
  foreach($parms as $key => $val) {
    $id_todo = strstr($key,'_');

    if ($id_todo != '') {
      $id_todo = substr($id_todo, 1);
      $sel_id = sql_parse_id($id_todo, true); 
      if ($val == 'on') {
        $query = "DELETE FROM Todo WHERE todo_id $sel_id $multidomain";
	display_debug_msg($query, $cdg_sql, 'run_query_todo_delete(1)');
        $obm_q = new DB_OBM;
        $obm_q->query($query);

	// If connectors in use
	if ($c_use_connectors) {
          $ins_id = sql_parse_id($id_todo);
	  $now = date('Y-m-d H:i:s');
	  $query = "INSERT INTO
            DeletedTodo (deletedtodo_todo_id, deletedtodo_timestamp)
            VALUES ($ins_id, '$now')";
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
  global $cdg_sql, $c_use_connectors;

  $multidomain = sql_multidomain('todo');
  $id = sql_parse_id($t_id, true);
  $query = "DELETE FROM Todo WHERE todo_id $id $multidomain";
  
  display_debug_msg($query, $cdg_sql, 'run_query_todo_delete_unique(1)');
  $obm_q = new DB_OBM;
  $ret = $obm_q->query($query);

  // If connectors in use
  if ($c_use_connectors) {
    $id = sql_parse_id($t_id);
    $now = date('Y-m-d H:i:s');
    $query = "INSERT INTO
      DeletedTodo (deletedtodo_todo_id, deletedtodo_timestamp)
      VALUES ($t_id, '$now')";

    display_debug_msg($query, $cdg_sql, 'run_query_todo_delete_unique(2)');
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
  $users = $todo['userid'];
  $user = $users[0];
  $priority = $todo['priority'];
  $deadline = $todo['deadline'];
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
