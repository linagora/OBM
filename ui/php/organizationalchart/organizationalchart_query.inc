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
    $where .= " $and organizationalchart_name $like '".$obm_q->escape($name)."%'";
    $and = "AND";
  }

  if ($desc != "") {
    $where .= " $and organizationalchart_description $like '%".$obm_q->escape($desc)."%'";
    $and = "AND ";
  }

  if ($archive != "1") {
    $where .= " $and organizationalchart_archive=0";
    $and = "AND ";
  }  

  if ($user != "") {
    $left_user = "
      LEFT JOIN OGroup ON ogroup_organizationalchart_id = organizationalchart_id
      LEFT JOIN OGroupLink ON ogrouplink_ogroup_id = ogroup_id
      LEFT JOIN GroupEntity ON groupentity_entity_id=ogrouplink_entity_id
      LEFT JOIN of_usergroup ON of_usergroup_group_id=groupentity_group_id 
      LEFT JOIN UserEntity ON userentity_entity_id=ogrouplink_entity_id
      LEFT JOIN UserObm as u ON (u.userobm_id=userentity_user_id OR (u.userobm_id=of_usergroup_user_id AND u.userobm_archive = 0))";
    $where .= " $and (userobm_lastname $like '".$obm_q->escape($user)."%' OR userobm_firstname $like '".$obm_q->escape($user)."%')";
  }

  if ($where == "") {
    $where = "1=1";
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
    $cq = "SELECT count(*) FROM OrganizationalChart $left_user $whereq";
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

  $obm_q = new DB_OBM;
  $uid = sql_parse_id($obm["uid"]);
  $domain_id = sql_parse_id($obm["domain_id"]);

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
      $domain_id,
      null,
      NOW(),
      null,
      $uid,
      '".$obm_q->escape($name)."',
      '$archive',
      '".$obm_q->escape($desc)."')";

  display_debug_msg($query, $cdg_sql, "run_query_organizationalchart_insert()");
  $retour = $obm_q->query($query);
  $uid = sql_parse_id($obm['uid'], true);
  $id = $obm_q->lastid();

  if($id > 0) {
    // Insert OGroup
    $entity_id = of_entity_insert($GLOBALS['module'], $id);    
    run_query_organizational_chart_entity_insert($organizationalchart, $id);
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion Organizational Chart Entity query execution
// Parameters:
//   - $organizationalchart[] : Entry's values
//   - $id : Organizational Chart id
// FIXME: ogroup_level varchar(16) ===> ogroup_level varchar(255);
///////////////////////////////////////////////////////////////////////////////
function run_query_organizational_chart_entity_insert($organizationalchart, $organizationalchart_id) {
  global $obm, $cdg_sql;

  $sel_uid = sql_parse_id($obm["uid"], true);
  $ins_uid = sql_parse_id($obm["uid"]);
  $sel_domain_id = sql_parse_id($obm["domain_id"], true);
  $ins_domain_id = sql_parse_id($obm["domain_id"]);
  $sel_organizationalchart_id = sql_parse_id($organizationalchart_id, true);
  $ins_organizationalchart_id = sql_parse_id($organizationalchart_id);

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
          WHERE ogroup_domain_id $sel_domain_id 
          AND ogroup_usercreate $sel_uid 
          AND ogroup_organizationalchart_id $sel_organizationalchart_id 
          AND ogroup_level='$parent_level'";

        display_debug_msg($query, $cdg_sql, "run_query_organizational_chart_ogroup_insert(ogroup_id)");
        $obm_q->query($query);
        $obm_q->next_record();
        $parent_id = sql_parse_id($obm_q->f("ogroup_id"));

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
            $ins_domain_id,
            null,
            NOW(),
            null,
            $ins_uid,
            $ins_organizationalchart_id,
            $parent_id,
            '".$obm_q->escape($name)."',
            '".$obm_q->escape($level)."')";

        display_debug_msg($query, $cdg_sql, "run_query_organizational_chart_ogroup_insert($name)");
        $obm_q->query($query);
        

        
        $ogroup_id = sql_parse_id($obm_q->lastid());

        $sel_ent_id = str_replace(".", "_", $level);
        $sel_entities = $organizationalchart["ent${sel_ent_id}"];
        if(is_array($sel_entities)) {
          $sel_ent = array_unique($sel_entities);
          while (list($key, $value) = each($sel_ent) ) {
            if (strcmp(substr($value, 0, 5),"data-") == 0) {
              $data = explode("-", $value);
              $entity_id = of_entity_get($data[1], $data[2]);
              $entity = $data[1];
              $query = "
                INSERT INTO OGroupLink (
                  ogrouplink_domain_id,
                  ogrouplink_timeupdate,
                  ogrouplink_timecreate,
                  ogrouplink_userupdate,
                  ogrouplink_usercreate,
                  ogrouplink_ogroup_id,
                  ogrouplink_entity_id
                ) VALUES (
                  $ins_domain_id,
                  null,
                  NOW(),
                  null,
                  $ins_uid,
                  $ogroup_id,
                  $entity_id
                )";
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

  $obm_q = new DB_OBM;
  $uid = sql_parse_id($obm["uid"]);
  $domain_id = sql_parse_id($obm["domain_id"]);

  $name = $organizationalchart["oc_name"];
  $archive = sql_parse_int($organizationalchart["archive"]);
  $desc = $organizationalchart["desc"];
  $multidomain = sql_multidomain("organizationalchart");
  $sid = sql_parse_id($id, true);
  $query = "UPDATE OrganizationalChart SET
    organizationalchart_timeupdate=NOW(),
    organizationalchart_userupdate=$uid,
    organizationalchart_name='".$obm_q->escape($name)."',
    organizationalchart_description='".$obm_q->escape($desc)."',
    organizationalchart_archive=$archive
    WHERE organizationalchart_id $sid $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_organizationalchart_update()");
  $retour = $obm_q->query($query);

  // Delete all entities
  run_query_organizational_chart_entity_delete($id);

  // Insert entities
  run_query_organizational_chart_entity_insert($organizationalchart, $id);

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
    
    $remove_ogrouplink_query = "DELETE FROM OGroupLink WHERE ogrouplink_ogroup_id='$ogroup_id'";
    display_debug_msg($remove_ogrouplink_query, $cdg_sql, "run_query_organizational_chart_entity_delete(2)");
    $del_obm_q->query($remove_ogrouplink_query);

    $remove_ogroup_query = "DELETE FROM OGroup WHERE ogroup_id='$ogroup_id'";
    display_debug_msg($remove_ogroup_query, $cdg_sql, "run_query_organizational_chart_entity_delete(3)");
    $del_obm_q->query($remove_ogroup_query);

  }
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
  $id = sql_parse_id($id, true);
  $query = "SELECT *,
    $timeupdate,
    $timecreate,
    c.userobm_login as usercreate,
    u.userobm_login as userupdate,
    (SELECT COUNT(*) FROM OGroup WHERE ogroup_organizationalchart_id $id ) as node
    FROM OrganizationalChart
    LEFT JOIN UserObm as c ON organizationalchart_usercreate=c.userobm_id
    LEFT JOIN UserObm as u ON organizationalchart_userupdate=u.userobm_id
    WHERE organizationalchart_id $id $multidomain";

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
  $id = sql_parse_id($id, true);
  $query = "SELECT o1.*, o2.ogroup_level as parent_level FROM OGroup as o1 LEFT JOIN OGroup as o2 ON o1.ogroup_parent_id = o2.ogroup_id
    WHERE o1.ogroup_organizationalchart_id $id ORDER BY o1.ogroup_level";

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
function run_query_organizationalchart_ogrouplink_detail($id) {
  global $cdgl_sql, $obm;

  $id = sql_parse_id($id, true);
  $query = "
    SELECT 
      ogrouplink_id,
      userobm_lastname,
      userobm_firstname,
      userobm_login,
      userobm_id,
      userobm_title,
      userobm_phone,
      userobm_mobile,
      userobm_description,
      userobm_email,
      domain_name,
      group_manager_id
    FROM OGroupLink    
      INNER JOIN GroupEntity ON groupentity_entity_id = ogrouplink_entity_id
      INNER JOIN UGroup ON group_id = groupentity_group_id
      INNER JOIN of_usergroup ON of_usergroup_group_id=groupentity_group_id 
      INNER JOIN UserObm ON userobm_id = of_usergroup_user_id
      INNER JOIN Domain ON domain_id = userobm_domain_id
    WHERE ogrouplink_ogroup_id $id AND userobm_archive = 0
  UNION
    SELECT 
      ogrouplink_id,
      userobm_lastname,
      userobm_firstname,
      userobm_login,
      userobm_id,
      userobm_title,
      userobm_phone,
      userobm_mobile,
      userobm_description,
      userobm_email,
      domain_name,
      NULL
    FROM OGroupLink    
      INNER JOIN UserEntity ON userentity_entity_id = ogrouplink_entity_id
      INNER JOIN UserObm ON userobm_id = userentity_user_id
      INNER JOIN Domain ON domain_id = userobm_domain_id
    WHERE ogrouplink_ogroup_id $id 
";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_organizationchart_ogrouplink_detail()");
  $obm_q->query($query);
  $users = array();
  while($obm_q->next_record()) {
    $users[$obm_q->f('userobm_id')] = array (
      'lastname' => $obm_q->f('userobm_lastname'),
      'firstname' => $obm_q->f('userobm_firstname'),
      'manager' => ($obm_q->f('userobm_id') == $obm_q->f('group_manager_id')),
      'title' => $obm_q->f('userobm_title'),
      'id' => $obm_q->f('userobm_id'),
      'login' => $obm_q->f('userobm_login'),
      'description' => $obm_q->f('userobm_description'),
      'phone' => $obm_q->f('phone'),
      'mobile' => $obm_q->f('mobile'),
      'email' => get_entity_email($obm_q->f('userobm_email'), $obm_q->f('domain_name'))
    );  
  }

  return $users;
}


///////////////////////////////////////////////////////////////////////////////
// Detail Form OGroup query execution
// Parameters :
//   - $id  : organizational chart id
///////////////////////////////////////////////////////////////////////////////
function run_query_organizationalchart_ogrouplink_entities($id) {
  global $cdgl_sql, $obm;
  $id = sql_parse_id($id, true);
  $query = "
    SELECT 
      ogrouplink_id,
      #CONCAT(userobm_lastname, ' ', userobm_firstname) as entity_name ,
      userobm_id as entity_id,
      'user' as entity
    FROM OGroupLink    
      INNER JOIN UserEntity ON userentity_entity_id = ogrouplink_entity_id
      INNER JOIN UserObm ON userobm_id = userentity_user_id
      INNER JOIN Domain ON domain_id = userobm_domain_id
    WHERE ogrouplink_ogroup_id $id 
    UNION 
    SELECT 
      ogrouplink_id,
      group_name as entity_name,
      group_id as entity_id,
      'group' as entity
    FROM OGroupLink    
      INNER JOIN GroupEntity ON groupentity_entity_id = ogrouplink_entity_id
      INNER JOIN UGroup ON group_id = groupentity_group_id
      INNER JOIN Domain ON domain_id = group_domain_id
    WHERE ogrouplink_ogroup_id $id";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, "run_query_organizationchart_ogrouplink_detail()");
  $obm_q->xquery($query);

  return $obm_q;
}
///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $id : organizational chart id
///////////////////////////////////////////////////////////////////////////////
function run_query_organizationalchart_delete($id) {
  global $cdg_sql;

  of_entity_delete($GLOBALS['module'], $id);
  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain("organizationalchart");
  $sid = sql_parse_id($id, true);
  $query = "DELETE FROM OrganizationalChart
    WHERE organizationalchart_id $sid $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_organizationalchart_delete()");
  $retour = $obm_q->query($query);

  return $retour;
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

  $id = sql_parse_id($id, true);
  $query = "SELECT *
    FROM OGroup 
    WHERE ogroup_parent_id $id 
    ORDER BY ogroup_id ASC";

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
    $organizationalchart_id = sql_parse_id($organizationalchart_id, true);
    $query = "SELECT *
      FROM OGroup
      WHERE ogroup_organizationalchart_id $organizationalchart_id 
      AND ogroup_parent_id IS NULL";
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
  $id = sql_parse_id($id, true);
  $query = "SELECT u1.*,
    u2.ogroup_level as parent_level
    FROM OGroup as u1
    LEFT JOIN OGroup as u2 ON u2.ogroup_id = u1.ogroup_parent_id
    WHERE u1.ogroup_id $id ";
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
  $snode_id = sql_parse_id($node_id, true);
  $query = "SELECT ogroup_id FROM OGroup WHERE ogroup_parent_id $snode_id ";

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


function contains_ascii($str) {
  for ($i=0; $i<strlen($str); $i++) {
    if (ord($str[$i]) > 127) {
      return true;
    } 
  }
  return false;
}
?>
