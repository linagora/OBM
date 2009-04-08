<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : resourcegroup_query.inc                                      //
//     - Desc : Resource group query File                                    //
// 2005-03-15 Florent Goalabr�                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// RGroup Search query execution 
// Parameters :
//   - $resourcegroup[]     : list search criteria
//     keys used     : name, user
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroup_search($group) {
  global $cdg_sql, $ctu_sql_limit, $ctype_resource;
  global $c_public, $c_private;
  
  $name = sql_search_text_parse($group["name"]);
  $resource = sql_search_text_parse($group["resource"]);
  $privacy = $group["privacy"];
  $sql_order_field = $group["sql_order_field"];
  $sql_order_dir = $group["sql_order_dir"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timeupdate = sql_date_format($db_type,"RGroup.rgroup_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"RGroup.rgroup_timecreate", "timecreate");
  $multidomain = sql_multidomain("rgroup");
	
  $where = "rgroup_name $like '$name%' $multidomain";

  if ($privacy == $c_public) {
    $where .= " AND rgroup_privacy = 0";
  } elseif ($privacy == $c_private) { 
    $where .= " AND rgroup_privacy = 1";
  }

  if ($resource != '') {
    $join_resource = "
      LEFT JOIN ResourceGroup as D ON rgroup_id=D.ResourceGroup_rgroup_id
      LEFT JOIN Resource as C ON D.resourcegroup_resource_id=C.resource_id";
    $where .= " and C.resource_name $like '$resource%'";
  }
  // only the one which are allowed (ie. publics )
  $where .=  " and " .sql_obm_entity_privacy("rgroup");
    
  $whereq = "where $where";

  // ORDER construction
  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "rgroup_name";
  $orderq .= " order by $order $sql_order_dir";

  $query = "select
      rgroup_id as id,
      rgroup_id,
      rgroup_domain_id,
      rgroup_privacy,
      rgroup_name,
      rgroup_desc,
      $timecreate,
      $timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate,
      count(rg.resourcegroup_resource_id) as rgroup_nb_resource
    from RGroup
      $join_resource
      LEFT JOIN ResourceGroup as rg ON rgroup_id=rg.ResourceGroup_rgroup_id      
      LEFT JOIN UserObm as A ON RGroup.rgroup_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON RGroup.rgroup_userupdate=B.userobm_id
    $whereq 
    GROUP BY
      rgroup_id,
      rgroup_domain_id,
      rgroup_privacy,
      rgroup_name,
      rgroup_desc,
      timecreate,
      timeupdate,
      usercreate,
      userupdate
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("select count(*) from RGroup $join_resource $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql);
    $obm_q->query($query);
  }

  return $obm_q;
}


/**
 * Search resourcegroups from a single field 
 * 
 * @param mixed $resource 
 * @access public
 * @return array of 'length' => DB query results nb, and 'datas' => DB Search results
 */
function run_query_resourcegroup_ext_search($resourcegroup) {
  global $obm, $c_all, $cdg_sql, $ctu_sql_limit;
  $pattern = $resourcegroup["pattern"];
  $restriction = $resourcegroup["restriction"];
  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain("RGroup");

  $query = "SELECT 
      count(*) as resultscount
    FROM RGroup
    WHERE 
      rgroup_name #LIKE '$pattern%'
      AND (rgroup_privacy = 0  OR (rgroup_privacy = 1 AND rgroup_usercreate = $obm[uid]))
      $multidomain
 ";

  display_debug_msg($query, $cdg_sql, "run_query_resourcegroup_ext_search()");
  $obm_q->xquery($query);
  $obm_q->next_record();
  $resultsCount = $obm_q->f("resultscount") + 0;

  $query = "SELECT 
      rgroup_id,
      rgroup_name,
      rgroup_desc
    FROM RGroup
    WHERE 
      rgroup_name #LIKE '$pattern%' 
      AND (rgroup_privacy = 0  OR (rgroup_privacy = 1 AND rgroup_usercreate = $obm[uid]))
      $multidomain
    ORDER BY rgroup_name,rgroup_desc,rgroup_id";

  display_debug_msg($query, $cdg_sql, "run_query_resourcegroup_ext_search()");
  $obm_q->xquery($query);

  return array('length' => $resultsCount, 'datas' => $obm_q);
}


///////////////////////////////////////////////////////////////////////////////
// Possible children RGroup Search query execution 
// Parameters :
//   - $group[]      : list search criteria
//     keys used     : name, resource 
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroup_search_possible_children($group) {
  global $cdg_sql, $ctype_resource;
  
  $id = sql_parse_id($group["ext_id"], true, true);
  $name = $group["name"];
  $resource = $group["resource"];
  $sql_order_field = $group["sql_order_field"];
  $sql_order_dir = $group["sql_order_dir"];
  $multidomain = sql_multidomain("rgroup");
	
  $parents = get_group_parents($group["ext_id"]);

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $timeupdate = sql_date_format($db_type,"RGroup.rgroup_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"RGroup.rgroup_timecreate", "timecreate");

  // WHERE Clause

  // only the one which are allowed (ie. publics )
  $where .=  " and " .sql_obm_entity_privacy("rgroup");
  if ($resource != '') {
    $where .= " and C.resource_name $like '$resource%'";
  }
  while (list ($key, $g_id) = each($parents)) {
    $g_id = sql_parse_id($g_id, true, true);
    $where .= " and rgroup_id $g_id ";
  }
    
  $query = "SELECT DISTINCT RGroup.*, rgroup_id as id,
      $timecreate,
      $timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate
    FROM 
      (UGroup LEFT JOIN ResourceGroup ON group_id=ResourceGroup_rgroup_id)
      LEFT JOIN Resource as C ON ResourceGroup.resourcegroup_resource_id=C.resource_id
      LEFT JOIN UserObm as A ON RGroup.rgroup_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON RGroup.rgroup_userupdate=B.userobm_id
    WHERE rgroup_name $like '$name%'
      AND rgroup_id $id 
      $where $multidomain";

  // ORDER construction

  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "rgroup_name";
  $query .= " order by $order $sql_order_dir";

  display_debug_msg($query, $cdg_sql, "run_query_resourcegroup_search_possible_children()");
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// RGroup detail query execution
// Parameters:
//   - $id : group id
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroup_detail($id) {
  global $cdg_sql, $ctype_resource;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  
  $multidomain = sql_multidomain("rgroup");
  $timeupdate = sql_date_format($db_type,"RGroup.rgroup_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"RGroup.rgroup_timecreate", "timecreate");

  $id = sql_parse_id($id, true);
  $query = "SELECT *,
      rgroup_privacy as privacy,
      $timecreate,
      $timeupdate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM RGroup
         LEFT JOIN UserObm as c ON rgroup_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON rgroup_userupdate=u.userobm_id
    WHERE rgroup_id $id 
      $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_resourcegroup_detail()");
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : RGroup insertion
// Parameters:
//   - $group[] : group hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroup_insert($group) {
  global $cdg_sql, $ctype_resource, $obm;

  $name = $group["name"];
  $desc = $group["desc"];
  $priv = sql_parse_int($group["privacy"]);
  $domain_id = sql_parse_id($obm["domain_id"]);
	
  $query = "INSERT INTO RGroup (
    rgroup_timeupdate,
    rgroup_timecreate,
    rgroup_userupdate,
    rgroup_usercreate,
    rgroup_domain_id,
    rgroup_privacy,
    rgroup_name,
    rgroup_desc
  ) values (
    null,
    NOW(),
    null,
    $obm[uid],
    $domain_id,
    $priv,
    '$name',
    '$desc'
  )";

  display_debug_msg($query, $cdg_sql, "run_query_resourcegroup_insert()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  
  $g_id = $obm_q->lastid();
  $entity_id = of_entity_insert($GLOBALS['module'], $g_id);  

  return $g_id;
}


///////////////////////////////////////////////////////////////////////////////
// Group ID : Get the id of the group inserted
// Parameters:
//   - $group[] : group hash info : keys used : all
// Returns:
//   group Id if found else false
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroup_id($group) {
  global $cdg_sql, $ctype_resource;

  $name = $group["name"];
  $desc = $group["desc"];
  $multidomain = sql_multidomain("rgroup");
	
  $query = "SELECT rgroup_id
    FROM RGroup
    WHERE
      rgroup_name='$name'
      AND rgroup_desc='$desc' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_resourcegroup_id()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    $obm_q->next_record();
    $id = $obm_q->f("rgroup_id");
  } else {
    $id = false;
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// RGroup Update query execution
// Parameters:
//   - $group[] : group hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroup_update($group) {
  global $obm, $cdg_sql, $ctype_resource;

  $id = sql_parse_id($group["resourcegroup_id"], true);
  $name = $group["name"];
  $desc = $group["desc"];
  $priv = sql_parse_int($group["privacy"]);
  $multidomain = sql_multidomain("rgroup");
  
  $query = "UPDATE RGroup set
    rgroup_timeupdate=NOW(),
    rgroup_userupdate=$obm[uid],
    rgroup_privacy=$priv,
    rgroup_name='$name',
    rgroup_desc='$desc'
  WHERE rgroup_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_resourcegroup_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : group id
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroup_delete($p_id) {
  global $cdg_sql;
  
  of_entity_delete($GLOBALS['module'], $p_id);

  $p_id = sql_parse_id($p_id, true);
  // Delete the Group
  $multidomain = sql_multidomain("rgroup");
  $query = "DELETE FROM RGroup WHERE rgroup_id $p_id $multidomain";
  display_debug_msg($query, $cdg_sql, "run_query_resourcegroup_delete(2)");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ResourceGroup conditionnal insertion
// Parameters:
//   - $group[] : group hash info : keys used : id, resource_nb, resourceX
// Return: number of users inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroup_resourcegroup_insert($group) {
  global $obm, $cdg_sql;

  $id = $group["resourcegroup_id"];
  $sel_id = sql_parse_id($group["resourcegroup_id"], true);
  $ins_id = sql_parse_id($group["resourcegroup_id"]);
  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $group["resource_nb"]) {
    $cpt++;
    $u_id = $group["resource$cpt"];
    $sel_uid = sql_parse_id($group["resource$cpt"], true);
    $ins_uid = sql_parse_id($group["resource$cpt"]);

    $query = "SELECT * FROM ResourceGroup
      WHERE resourcegroup_rgroup_id $sel_id 
        AND resourcegroup_resource_id $sel_uid ";
    display_debug_msg($query, $cdg_sql);
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "INSERT INTO ResourceGroup
      (resourcegroup_rgroup_id,
       resourcegroup_resource_id)
      VALUES ($ins_id, $ins_uid)";

      display_debug_msg($query, $cdg_sql, "run_query_resourcegroup_resourcegroup_insert()");
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ResourceGroup deletion
// Parameters:
//   - $group[] : group hash info : keys used : id, resource_nb, resourceX
// Return: number of resources deleted
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroup_resourcegroup_delete($group) {
  global $cdg_sql;

  $id = sql_parse_id($group["resourcegroup_id"], true);
  $cpt = 0;
  $cpt_del = 0;
  while ($cpt < $group["resource_nb"]) {
    $cpt++;
    $r_id = sql_parse_id($group["resource$cpt"], true);

    $query = "DELETE FROM ResourceGroup
      WHERE resourcegroup_rgroup_id $id 
        AND resourcegroup_resource_id $r_id ";
    display_debug_msg($query, $cdg_sql, "run_query_resourcegroup_resourcegroup_delete()");
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    if ($retour) {
      $cpt_del++;
    }
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ResourceGroupResourceGroup conditionnal insertion
// Parameters:
//   - $group[] : group hash info
// Return: number of resource groups inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroupresourcegroup_insert($group) {
  global $cdg_sql;
  $id = $group["resourcegroup_id"];
  $sel_id = sql_parse_id($group["resourcegroup_id"], true);
  $ins_id = sql_parse_id($group["resourcegroup_id"]);
  $cpt = 0;
  $cpt_ins = 0;

  $parents = get_group_parents($id);

  while ($cpt < $group["resourcegroup_nb"]) {
    $cpt++;
    $g_id = $group["resourcegroup_$cpt"];
    $sel_gid = sql_parse_id($group["resourcegroup_$cpt"], true);
    $ins_gid = sql_parse_id($group["resourcegroup_$cpt"]);

    $query = "SELECT * FROM ResourceGroupResourceGroup
      WHERE resourcegroupresourcegroup_parent_id $sel_id 
        AND resourcegroupresourcegroup_child_id $sel_gid ";
    display_debug_msg($query, $cdg_sql);
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist and doesn't create a loop, insert it
    if ( ($test_q->num_rows() == 0)
         && (! in_array($g_id, $parents))
         && ($id != $g_id)
       ) {
        $query = "INSERT INTO ResourceGroupResourceGroup
        (resourcegroupresourcegroup_parent_id,
        resourcegroupresourcegroup_child_id)
      VALUES ($ins_id, $ins_gid)";

      display_debug_msg($query, $cdg_sql, "run_query_resourcegroupresourcegroup_insert()");
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ResourceGroupResourceGroup deletion
// Parameters:
//   - $group[] : group hash info
// Return: number of resource groups deleted
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroupresourcegroup_delete($group) {
  global $cdg_sql;

  $id = sql_parse_id($group["resourcegroup_id"], true);
  $cpt = 0;
  $cpt_del = 0;
  while ($cpt < $group["resourcegroup_nb"]) {
    $cpt++;
    $g_id = sql_parse_id($group["resourcegroup_$cpt"], true);

    $query = "DELETE FROM ResourceGroupResourceGroup
      WHERE resourcegroupresourcegroup_parent_id $id 
        AND resourcegroupresourcegroup_child_id $g_id ";

    display_debug_msg($query, $cdg_sql, "run_query_resourcegroupresourcegroup_delete()");
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    if ($retour) {
      $cpt_del++;
    }
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the resources of the given group
// Parameters:
//   - $group[] : Group parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_resourcegroup($group) {
  global $cdg_sql, $ctype_resource;

  $obm_q = new DB_OBM; 
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);

  $id = sql_parse_id($group["resourcegroup_id"], true);
  $entity = $group["entity"];
  $sql_order_field = $group["sql_order_field"];
  $sql_order_dir = $group["sql_order_dir"];

  if (($entity == "resourcegroup_resource") && (trim($sql_order_field) != "")) {
    $order = "order by $sql_order_field $sql_order_dir";
  } else {
    $order = "order by resource_name";
  }

  $query = "SELECT resourcegroup_resource_id as resourcegroup_resource_id,
         resourcegroup_resource_id as id,
         resource_timeupdate as group_resource_timeupdate,
         resource_timecreate as group_resource_timecreate,
         resource_userupdate as group_resource_userupdate,
         resource_usercreate as group_resource_usercreate,
         resource_name as resourcegroup_resource_name,
         resource_description as resourcegroup_resource_desc,
         resource_qty as resourcegroup_resource_qty
      FROM ResourceGroup LEFT JOIN Resource ON resourcegroup_resource_id=resource_id
      WHERE resourcegroup_rgroup_id $id 
         $order
         $limit";

  display_debug_msg($query, $cdg_sql, "run_query_resource_resourcegroup()");
  $obm_q->query($query);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the groups members of the given group
// Parameters:
//   - $group[] : Group parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroup_resourcegroup($group) {
  global $cdg_sql, $ctype_resource;

  $obm_q = new DB_OBM; 
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);

  $id = sql_parse_id($group["resourcegroup_id"], true);
  $entity = $group["entity"];
  $sql_order_field = $group["sql_order_field"];
  $sql_order_dir = $group["sql_order_dir"];

  if (($entity == "resourcegroup_resourcegroup") && (trim($sql_order_field) != "")) {
    $order = "order by $sql_order_field $sql_order_dir";
  } else {
    $order = "order by rgroup_name";
  }

  $query = "SELECT resourcegroupresourcegroup_child_id as child_id,
         resourcegroupresourcegroup_child_id as id,
         rgroup_timeupdate,
         rgroup_timecreate,
         rgroup_userupdate,
         rgroup_usercreate,
         rgroup_id,
         rgroup_name,
         rgroup_desc
      FROM ResourceGroupResourceGroup LEFT JOIN RGroup ON resourcegroupresourcegroup_child_id=rgroup_id
      WHERE resourcegroupresourcegroup_parent_id $id 
         $order
         $limit";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql, "run_query_resourcegroup_resourcegroup()");
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return the number of registered resources in the RGroup specified
// Parameters:
//   - $id : group id
///////////////////////////////////////////////////////////////////////////////
function get_resourcegroup_nb_resource($id) {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  $query = "SELECT count(*)
    FROM ResourceGroup
    WHERE resourcegroup_rgroup_id $id ";

  display_debug_msg($query, $cdg_sql, "get_resourcegroup_nb_resource()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f(0);
  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Return the group infos
// Parameters:
//   - $id : group id
///////////////////////////////////////////////////////////////////////////////
function get_resourcegroup_info($id) {
  global $cdg_sql, $ctype_resource;

  if ($id == "") {
    return false;
  }
  $multidomain = sql_multidomain("rgroup");
  $id = sql_parse_id($id, true);	
  $query = "SELECT * FROM RGroup WHERE rgroup_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, "get_resourcegroup_info()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $g["usercreate"] = $obm_q->f("rgroup_usercreate");
  $g["name"] = $obm_q->f("rgroup_name");
  $g["privacy"] = $obm_q->f("rgroup_privacy");

  return $g;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a RGroup (except with id given) with the name given already exists
// Parameters:
//   - $name : name to search for
//   - $id   : group id to exclude
// Returns:
//   - true (if a group exists) or false
///////////////////////////////////////////////////////////////////////////////
function get_resourcegroup_name_exists($name, $id="") {
  global $cdg_sql, $obm, $ctype_resource;

  if ($id != "") {
    $id = sql_parse_id($id, true, true);
    $where_id = "AND rgroup_id $id ";
  }

  $multidomain = sql_multidomain("rgroup");

  $query = "SELECT rgroup_id, rgroup_name
    FROM RGroup
    WHERE rgroup_name='$name'
      AND (rgroup_privacy=0 OR rgroup_usercreate=$obm[uid])
      $where_id $multidomain";

  display_debug_msg($query, $cdg_sql, "get_resourcegroup_name_exists()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Return the groups which matches the name or the desc
// except the one given (update mode)
// Parameters:
//   - $id   : group id
//   - $name : group name
//   - $desc : group desc
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroup_check($id, $name, $desc) {
  global $cdg_sql, $obm, $ctype_resource;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  if ($id != "") {
    $where_id = "and rgroup_id!='$id'";
  }

  // If name is short, we test equality, else similarity
  if (strlen($name) > 2) {
    $wname = "rgroup_name $like '%$name%'";
  } else {
    $wname = "rgroup_name = '$name'";
  }

  // If desc is short, we test equality, else similarity
  if (trim($desc) != "") {
    if (strlen($desc) > 2) {
      $wdesc = "rgroup_desc $like '%$desc%'";
    } else {
      $wdesc = "rgroup_desc = '$desc'";
    }
  }
  if ($wdesc != "") $wdesc = "OR $wdesc";

  $multidomain = sql_multidomain("rgroup");

  $query = "SELECT distinct rgroup_id, rgroup_name, rgroup_desc
     FROM RGroup
     WHERE
       (rgroup_privacy=0 OR rgroup_usercreate=$obm[uid])
       AND ($wname $wdesc)
       $where_id $multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Group context checking (same groups exists ?)
// Parameters:
//   - $id       : group id
//   - $group[]  : group values
//     keys used : name, desc
// Returns:
//   - Group Database object with group of similar groups
///////////////////////////////////////////////////////////////////////////////
function check_resourcegroup_context($id, $group) {
  global $cdg_sql;

  $name = $group["name"];
  $desc = $group["desc"];

  // return the groups with same name or desc
  $g_q = run_query_resourcegroup_check($id, $name, $desc);

  return $g_q;
}


///////////////////////////////////////////////////////////////////////////////
// Group Form Data checking and formatting
// Parameters:
//   - $group[]  : values checked
//     keys used : name, desc
///////////////////////////////////////////////////////////////////////////////
function check_resourcegroup_data_form($group) {
  global $err, $l_fill_name, $l_group_exists;

  $id = $group["resourcegroup_id"];
  $name = $group["name"];
  $desc = $group["desc"];
  $lgroup = get_resourcegroup_info($id);
  $usercreate = $lgroup["usercreate"];

  // MANDATORY: Group name not empty
  if (trim($name) == "") {
    $err["msg"] = $l_fill_name;
    return false;
  }

  // MANDATORY: RGroup name unique
  if (get_resourcegroup_name_exists($name, $id)) {
    $err["msg"] = "$l_group_exists ($name)";
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Get a group parent list
// Parameters:
//   - $id : group id
// Returns:
//   - array of parent groups ids
///////////////////////////////////////////////////////////////////////////////
function get_resourcegroup_parents($id) {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  $query = "select resourcegroupresourcegroup_parent_id
    from ResourceGroupResourceGroup where resourcegroupresourcegroup_child_id $id ";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
 
  $parents = array();
  while ($obm_q->next_record()) {
    $g_id = $obm_q->f("resourcegroupresourcegroup_parent_id");
    array_push($parents, $g_id);
    $child_parents = get_group_parents($g_id);
    $parents = array_merge($parents, $child_parents);
  }

  return array_unique($parents);
}


?>
