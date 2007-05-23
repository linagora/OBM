<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : organizationalchart_query.inc                                //
//     - Desc : Organizational Chart Query File                              //
// 2007-02-27 David PHAN                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id: organizationalchart_query.inc,v 1.106 2007/02/26 16:25:43 pierre Exp $ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Organizational Chart search query execution
// Parametes:
//   - $organizationalchart[]    : search criteria
//     keys used       archive, name, phone, type, zip
///////////////////////////////////////////////////////////////////////////////
function run_query_organizationalchart_search($organizationalchart) {
  global $cdg_sql, $ctu_sql_limit;

  $sql_order_dir = $organizationalchart["sql_order_dir"];
  $sql_order_field = $organizationalchart["sql_order_field"];

  $archive = $organizationalchart["archive"];
  $name = sql_search_text_parse($organizationalchart["name"]);
  $desc = sql_search_text_parse($organizationalchart["desc"]);
  $user = $organizationalchart["user"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $multidomain = sql_multidomain("organizationalchart");

  if($name != "") {
    $where .= " $and organizationalchart_name $like '$name%'";
    $and = "AND";
  }
  
  if ($desc != "") {
    $where .= " $and organizationalchart_description $like '%$desc%'";
    $and = "AND ";
  }

  if ($archive != "1") {
    $where .= " $and organizationalchart_archive='0'";
    $and = "AND ";
  }  

  if ($user != "") {
    $left_user = "
      LEFT JOIN OGroup ON ogroup_organizationalchart_id = organizationalchart_id
      LEFT JOIN OGroupEntity ON ogroupentity_ogroup_id = ogroup_id
      LEFT JOIN UserObmGroup ON (userobmgroup_group_id=ogroupentity_entity_id AND ogroupentity_entity='group') 
      LEFT JOIN UserObm as u ON (u.userobm_id=ogroupentity_entity_id AND ogroupentity_entity='user') 
        OR (u.userobm_id=userobmgroup_userobm_id)";
    $where .= " $and (userobm_lastname $like '$user%' OR userobm_firstname $like '$user%')";
  }
  
  $whereq = "WHERE $where $multidomain";	

  // ORDER construction
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "organizationalchart_name";
  $orderq = " ORDER BY $order $sql_order_dir";

  $select = "SELECT DISTINCT organizationalchart_id,
      organizationalchart_name,
      organizationalchart_description,
      organizationalchart_archive
    FROM OrganizationalChart $left_user";

  if ($ctu_sql_limit) {
    $cq = "SELECT count(*) FROM OrganizationalChart $join $whereq";
    $count = get_query_count($cq);
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    $query = "$select $whereq $orderq $limit";
    display_debug_msg($query, $cdg_sql, "run_query_organizationalchart_search()");
    $obm_q->query($query);
    if (!$ctu_sql_limit) {
	    $count = $obm_q->num_rows();
    }
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query execution
// Parameters:
//   - $organizationalchart[] : Entry's values
///////////////////////////////////////////////////////////////////////////////
function run_query_organizationalchart_insert($organizationalchart) {
  global $obm, $cdg_sql;

  $uid = $obm["uid"];
  $now = date("Y-m-d H:i:s");
  $domain_id = $obm["domain_id"];

  $name = $organizationalchart["oc_name"];
  $archive = ($organizationalchart["archive"] == "1" ? "1" : "0");
  $desc = $organizationalchart["desc"];

  $query = "INSERT INTO OrganizationalChart (
    organizationalchart_domain_id,
    organizationalchart_timeupdate,
    organizationalchart_timecreate,
    organizationalchart_userupdate,
    organizationalchart_usercreate,
    organizationalchart_name,
    organizationalchart_archive,
    organizationalchart_description) VALUES (
    '$domain_id',
    null,
    '$now',
    null,
    '$uid',
    '$name',
    '$archive',
    '$desc')";

  display_debug_msg($query, $cdg_sql, "run_query_organizationalchart_insert()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Get the chart id
  $query = "SELECT organizationalchart_id as id
    FROM OrganizationalChart
    WHERE organizationalchart_name = '$name'
      AND organizationalchart_timecreate='$now'";
  
  display_debug_msg($query, $cdg_sql, "run_query_organizationalchart_insert(2)");
  $obm_q->query($query);
  $obm_q->next_record();
  $id = $obm_q->f("id");
  
  if($id > 0) {
    // Insert OGroup
    run_query_organizational_chart_entity_insert($organizationalchart, $id);
  }
  
  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion Organizational Chart Entity query execution
// Parameters:
//   - $organizationalchart[] : Entry's values
//   - $id : Organizational Chart id
///////////////////////////////////////////////////////////////////////////////
function run_query_organizational_chart_entity_insert($organizationalchart, $organizationalchart_id) {
  global $obm, $cdg_sql;

  $uid = $obm["uid"];
  $now = date("Y-m-d H:i:s");
  $domain_id = $obm["domain_id"];

  $ogroup = $organizationalchart["ogroup"];

  if(is_array($ogroup)) {
    $obm_q = new DB_OBM;
    foreach($ogroup as $key => $value) {
      if($value != "") {
        $name = $value;
        $level = $key;
        $parent_level = $organizationalchart["parent_level"][$level];

        // Select parent id
        $query = "SELECT ogroup_id FROM OGroup
          WHERE ogroup_domain_id='$domain_id'
          AND ogroup_timecreate='$now'
          AND ogroup_usercreate='$uid'
          AND ogroup_organizationalchart_id='$organizationalchart_id'
          AND ogroup_level='$parent_level'";

        display_debug_msg($query, $cdg_sql, "run_query_organizational_chart_ogroup_insert(ogroup_id)");
        $obm_q->query($query);
        $obm_q->next_record();
        $parent_id = $obm_q->f("ogroup_id");

        $query = "INSERT INTO OGroup (
          ogroup_domain_id,
          ogroup_timeupdate,
          ogroup_timecreate,
          ogroup_userupdate,
          ogroup_usercreate,
          ogroup_organizationalchart_id,
          ogroup_parent_id,
          ogroup_name,
          ogroup_level) VALUES (
          '$domain_id',
          null,
          '$now',
          null,
          '$uid',
          '$organizationalchart_id',
          '$parent_id',
          '$name',
          '$level')";

        display_debug_msg($query, $cdg_sql, "run_query_organizational_chart_ogroup_insert($name)");
        $obm_q->query($query);
      
        $query = "SELECT ogroup_id FROM OGroup
          WHERE ogroup_domain_id='$domain_id'
          AND ogroup_timecreate='$now'
          AND ogroup_usercreate='$uid'
          AND ogroup_organizationalchart_id='$organizationalchart_id'
          AND ogroup_name='$name'
          AND ogroup_level='$level'";
        display_debug_msg($query, $cdg_sql, "run_query_organizational_chart_ogroup_insert(ogroup_id)");
        $obm_q->query($query);
        $obm_q->next_record();
        $ogroup_id = $obm_q->f("ogroup_id");

        $sel_ent_id = str_replace(".", "_", $level);
        $sel_entities = $organizationalchart["ent${sel_ent_id}"];
        if(is_array($sel_entities)) {
          $sel_ent = array_unique($sel_entities);
          while (list($key, $value) = each($sel_ent) ) {
            if (strcmp(substr($value, 0, 5),"data-") == 0) {
              $data = explode("-", $value);
              $id = $data[2];
              $entity = $data[1];
              $query = "INSERT INTO OGroupEntity (
                ogroupentity_domain_id,
                ogroupentity_timeupdate,
                ogroupentity_timecreate,
                ogroupentity_userupdate,
                ogroupentity_usercreate,
                ogroupentity_ogroup_id,
                ogroupentity_entity_id,
                ogroupentity_entity) VALUES (
                '$domain_id',
                null,
                '$now',
                null,
                '$uid',
                '$ogroup_id',
                '$id',
                '$entity')";
              display_debug_msg($query, $cdg_sql, "run_query_organizational_chart_ogroup_insert()");
              $obm_q->query($query);
            }
          }
        }
      }
    }
  } 
}


///////////////////////////////////////////////////////////////////////////////
// Update query execution
// Parameters:
//   - $id       : organizationalchart id
//   - $organizationalchart[] : Entry's values
///////////////////////////////////////////////////////////////////////////////
function run_query_organizationalchart_update($id, $organizationalchart) {
  global $obm, $cdg_sql;

  $uid = $obm["uid"];
  $now = date("Y-m-d H:i:s");
  $domain_id = $obm["domain_id"];

  $name = $organizationalchart["oc_name"];
  $archive = ($organizationalchart["archive"] == "1" ? "1" : "0");
  $desc = $organizationalchart["desc"];
  $multidomain = sql_multidomain("organizationalchart");

  $query = "UPDATE OrganizationalChart SET
    organizationalchart_timeupdate='$now',
    organizationalchart_userupdate='$uid',
    organizationalchart_name='$name',
    organizationalchart_description='$desc',
    organizationalchart_archive='$archive'
    WHERE organizationalchart_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_organizationalchart_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  
  // Delete all entities
  run_query_organizational_chart_entity_delete($id);

  // Insert entities
  run_query_organizational_chart_entity_insert($organizationalchart, $id);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution
// Parameters :
//   - $id  : organizational chart id
///////////////////////////////////////////////////////////////////////////////
function run_query_organizationalchart_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  
  $multidomain = sql_multidomain("organizationalchart");
  $timeupdate = sql_date_format($db_type, "organizationalchart_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "organizationalchart_timecreate", "timecreate");
  $lang = get_lang();

  $query = "SELECT *,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate,
      (SELECT COUNT(*) FROM OGroup WHERE ogroup_organizationalchart_id='$id') as node
    FROM OrganizationalChart
         LEFT JOIN UserObm as c ON organizationalchart_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON organizationalchart_userupdate=u.userobm_id
    WHERE organizationalchart_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_organizationalchart_detail()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Detail OGroup query execution
// Parameters :
//   - $id  : organizational chart id
///////////////////////////////////////////////////////////////////////////////
function run_query_organizationchart_ogroup_detail($id) {
  global $cdgl_sql, $obm;

  $query = "SELECT o1.*, o2.ogroup_level as parent_level FROM OGroup as o1 LEFT JOIN OGroup as o2 ON o1.ogroup_parent_id = o2.ogroup_id
    WHERE o1.ogroup_organizationalchart_id='$id' ORDER BY o1.ogroup_level";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_organizationchart_ogroup_detail()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Detail Form OGroup query execution
// Parameters :
//   - $id  : organizational chart id
///////////////////////////////////////////////////////////////////////////////
function run_query_organizationalchart_ogroupentity_detail($id) {
  global $cdgl_sql, $obm;

  $query = "SELECT 
    ogroupentity_id,
    ogroupentity_entity,
    ogroupentity_entity_id
    FROM OGroupEntity
    WHERE ogroupentity_ogroup_id='$id'";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_organizationchart_ogroupentity_detail()");
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $id : organizational chart id
///////////////////////////////////////////////////////////////////////////////
function run_query_organizationalchart_delete($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain("organizationalchart");

  $query = "DELETE FROM OrganizationalChart
    WHERE organizationalchart_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_organizationalchart_delete()");
  $retour = $obm_q->query($query);

  // Delete all entities
  run_query_organizational_chart_entity_delete($id);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion Organizational Chart Entities query execution
// Parameters:
//   - $id : organizational chart id
///////////////////////////////////////////////////////////////////////////////
function run_query_organizational_chart_entity_delete($id) {
  global $cdg_sql, $obm;

  $sel_obm_q = new DB_OBM;
  $del_obm_q = new DB_OBM;

  // Get ogroup_id
  $ogroup_query = "SELECT ogroup_id 
    FROM OGroup 
    WHERE ogroup_organizationalchart_id='$id'";
  display_debug_msg($ogroup_query, $cdg_sql, "run_query_organizational_chart_entity_delete(1)");
  $sel_obm_q->query($ogroup_query);

  while($sel_obm_q->next_record()) {
    $ogroup_id = $sel_obm_q->f("ogroup_id");
    
    $remove_ogroupentity_query = "DELETE FROM OGroupEntity WHERE ogroupentity_ogroup_id='$ogroup_id'";
    display_debug_msg($remove_ogroupentity_query, $cdg_sql, "run_query_organizational_chart_entity_delete(2)");
    $del_obm_q->query($remove_ogroupentity_query);

    $remove_ogroup_query = "DELETE FROM OGroup WHERE ogroup_id='$ogroup_id'";
    display_debug_msg($remove_ogroup_query, $cdg_sql, "run_query_organizational_chart_entity_delete(3)");
    $del_obm_q->query($remove_ogroup_query);

  }
}


///////////////////////////////////////////////////////////////////////////////
// Organizational Chart Form Data checking and formatting
// Parameters:
//   - $organizationalchart[] : values checked
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_organizationalchart_data_form($organizationalchart) {
  global $err, $l_fill_name;

  $name = $organizationalchart["oc_name"];  

  // MANDATORY: Company name
  if (trim($name) == "") {
    $err["msg"] = $l_fill_name;
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the organizational chart can be deleted
// Parameters:
//   - $id : organizational chart id
// Returns:
//   true if the company can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_can_delete_organizationalchart($id) {

  // TODO
  $delete_ok = true;

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Get children node
// Parameters:
//    - $id : parent node id
// Returns:
//    - $t_child : array
///////////////////////////////////////////////////////////////////////////////
function run_query_organizationalchart_node_child($id) {

  $query = "SELECT *
    FROM OGroup 
    WHERE ogroup_parent_id='$id' 
    ORDER BY ogroup_level ASC";

    $obm_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql, "run_query_organizationalchart_node_child()");
    $obm_q->query($query);

    $t_child = array();
    while($obm_q->next_record()) {
      $t_child[] = $obm_q->f("ogroup_id");
    }
    return $t_child;
}


///////////////////////////////////////////////////////////////////////////////
// Get organizational root node
// Parameters:
//    - $organizationalchart_id : organizational chart id
// Returns:
//    - $obm_q
///////////////////////////////////////////////////////////////////////////////
function run_query_organizationalchart_root_node($organizationalchart_id) {
  if($organizationalchart_id) {

  $query = "SELECT *
    FROM OGroup
    WHERE ogroup_organizationalchart_id='$organizationalchart_id'
    AND ogroup_parent_id='0'";
  $obm_q = new DB_OBM;

  display_debug_msg($query, $cdg_sql, "run_query_organizationalchart_root_node()");
  $obm_q->query($query);
 
  $obm_q->next_record();

  return $obm_q;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Get node infos 
///////////////////////////////////////////////////////////////////////////////
function run_query_organizationalchart_node_infos($id) {
  global $cdg_sql;

  $query = "SELECT u1.*,
    u2.ogroup_level as parent_level
    FROM OGroup as u1
    LEFT JOIN OGroup as u2 ON u2.ogroup_id = u1.ogroup_parent_id
    WHERE u1.ogroup_id='$id'";
  $obm_q = new DB_OBM;

  display_debug_msg($query, $cdg_sql, "run_query_organizationalchart_node_infos()");
  $obm_q->query($query);
 
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get node leaves 
///////////////////////////////////////////////////////////////////////////////
function run_query_organizationalchart_get_leaves($node_id, $reset) {
  global $cdg_sql, $nb;

  if($reset)
    $nb= 0;

  $query = "SELECT ogroup_id FROM OGroup WHERE ogroup_parent_id='$node_id'";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_organizationalchart_get_leaves()");
  $obm_q->query($query);


  while($obm_q->next_record()) {
    $t_child[] = $obm_q->f("ogroup_id");
  }

  if(sizeof($t_child) > 0 ) {
    foreach($t_child as $key => $node_id) {
      run_query_organizationalchart_get_leaves($node_id, false);
    }
  } else {
    $nb++;
  }

  return $nb;
}
?>
