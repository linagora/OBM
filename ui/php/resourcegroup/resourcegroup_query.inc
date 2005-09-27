<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : resourcegroup_query.inc                                      //
//     - Desc : Resource group query File                                    //
// 2005-03-15 Florent Goalabré                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// RGroup Search query execution 
// Parameters :
//   - $resourcegroup[]     : list search criteria
//     keys used     : name, user
///////////////////////////////////////////////////////////////////////////////
function run_query_search($group) {
  global $cdg_sql, $ctu_sql_limit, $ctype_resource;
  
  $name = sql_search_text_parse($group["name"]);
  $resource = sql_search_text_parse($group["resource"]);
  $new_order = $group["new_order"];
  $order_dir = $group["order_dir"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timeupdate = sql_date_format($db_type,"RGroup.rgroup_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"RGroup.rgroup_timecreate", "timecreate");

  $where = "rgroup_name $like '$name%'";
  if ($resource != '') {
    $join_resource = "
      LEFT JOIN ResourceGroup ON rgroup_id=ResourceGroup_rgroup_id
      LEFT JOIN Resource as C ON ResourceGroup.resourcegroup_resource_id=C.resource_id";
    $where .= " and C.resource_name $like '$resource%'";
  }
  // only the one which are allowed (ie. publics )
  $where .=  " and " .sql_obm_entity_privacy("rgroup");
    
  $whereq = "where $where";

  // ORDER construction
  $order = (strcmp($new_order,"") != 0) ? $new_order : "rgroup_name";
  $orderq .= " order by $order $order_dir";

  $query = "select distinct RGroup.*, rgroup_id as id,
      $timecreate,
      $timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate
    from RGroup
      $join_resource
      LEFT JOIN UserObm as A ON RGroup.rgroup_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON RGroup.rgroup_userupdate=B.userobm_id
    $whereq 
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


///////////////////////////////////////////////////////////////////////////////
// Possible children RGroup Search query execution 
// Parameters :
//   - $group[]      : list search criteria
//     keys used     : name, resource 
///////////////////////////////////////////////////////////////////////////////
function run_query_search_possible_children($group) {
  global $cdg_sql, $ctype_resource;
  
  $id = $group["ext_id"];
  $name = $group["name"];
  $resource = $group["resource"];
  $new_order = $group["new_order"];
  $order_dir = $group["order_dir"];

  $parents = get_group_parents($id);

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
    $where .= " and rgroup_id != '$g_id'";
  }
    
  $query = "select distinct RGroup.*, rgroup_id as id,
      $timecreate,
      $timeupdate,
      A.userobm_login as usercreate,
      B.userobm_login as userupdate
    from 
      (UGroup LEFT JOIN ResourceGroup ON group_id=ResourceGroup_rgroup_id)
      LEFT JOIN Resource as C ON ResourceGroup.resourcegroup_resource_id=C.resource_id
      LEFT JOIN UserObm as A ON RGroup.rgroup_usercreate=A.userobm_id
      LEFT JOIN UserObm as B ON RGroup.rgroup_userupdate=B.userobm_id
    where rgroup_name $like '$name%'
      and rgroup_id != '$id'
      $where";

  // ORDER construction

  $order = (strcmp($new_order,"") != 0) ? $new_order : "rgroup_name";
  $query .= " order by $order $order_dir";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// RGroup detail query execution                                              //
// Parameters:
//   - $id : group id
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($id) {
  global $cdg_sql, $ctype_resource;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type,"RGroup.rgroup_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type,"RGroup.rgroup_timecreate", "timecreate");

  $query = "select *,
      $timecreate,
      $timeupdate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    from RGroup
         left join UserObm as c on rgroup_usercreate=c.userobm_id
         left join UserObm as u on rgroup_userupdate=u.userobm_id
    where rgroup_id = '$id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : RGroup insertion
// Parameters:
//   - $group[] : group hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($group) {
  global $uid, $cdg_sql, $ctype_resource;

  $id = $group["id"];
  $name = $group["name"];
  $desc = $group["desc"];
  $priv = (isset($group["priv"]) ? $group["priv"] : '0');
  $priv=1; // Modif GT
  $now = date("Y-m-d H:i:s");

  $query = "insert into RGroup (
    rgroup_timeupdate,
    rgroup_timecreate,
    rgroup_userupdate,
    rgroup_usercreate,
    rgroup_privacy,
    rgroup_name,
    rgroup_desc
  ) values (
    null,
    '$now',
    null,
    '$uid',
    '$priv',
    '$name',
    '$desc'
  )";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $g_id = run_query_resourcegroup_id($group);

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
  $priv = (isset($group["priv"]) ? $group["priv"] : '0');

  $query = "SELECT rgroup_id
    FROM RGroup
    WHERE
      rgroup_name='$name'
      and rgroup_desc='$desc'";

  display_debug_msg($query, $cdg_sql);
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
// RGroup Update query execution                                              //
// Parameters:
//   - $group[] : group hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_update($group) {
  global $uid, $cdg_sql, $ctype_resource;

  $id = $group["id"];
  $name = $group["name"];
  $desc = $group["desc"];
  $priv = 1; // Modif GT 
  $now = date("Y-m-d H:i:s");
  
  $query = "update RGroup set
    rgroup_timeupdate='$now',
    rgroup_userupdate='$uid',
    rgroup_privacy='$priv',
    rgroup_name='$name',
    rgroup_desc='$desc'
  where rgroup_id='$id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution                                                  //
// Parameters:
//   - $p_id : group id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_id) {
  global $cdg_sql;

  // Delete all resources registrations to this group
  $query = "delete from ResourceGroup where resourcegroup_rgroup_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Delete all groups registration referencing this group
/*
  $query = "delete from ResourceGroupResourceGroup
    where resourcegroupresourcegroup_child_id='$p_id'
      or resourcegroupresourcegroup_parent_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
*/

  // Delete the Group
  $query = "delete from RGroup where rgroup_id='$p_id'";
  display_debug_msg($query, $cdg_sql);
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ResourceGroup conditionnal insertion                     //
// Parameters:
//   - $group[] : group hash info : keys used : id, resource_nb, resourceX
// Return: number of users inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_resourceresourcegroup_insert($group) {
  global $auth, $cdg_sql;

  $id = $group["id"];
  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $group["resource_nb"]) {
    $cpt++;
    $u_id = $group["resource$cpt"];

    $query = "select * from ResourceGroup
      where resourcegroup_rgroup_id='$id'
        and resourcegroup_resource_id='$u_id'";
    display_debug_msg($query, $cdg_sql);
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $query = "insert into ResourceGroup (resourcegroup_rgroup_id,
        resourcegroup_resource_id) values ($id, $u_id)";

      display_debug_msg($query, $cdg_sql);
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ResourceGroup deletion                                   //
// Parameters:
//   - $group[] : group hash info : keys used : id, resource_nb, resourceX
// Return: number of resources deleted
///////////////////////////////////////////////////////////////////////////////
function run_query_resourceresourcegroup_delete($group) {
  global $auth, $cdg_sql;

  $id = $group["id"];
  $cpt = 0;
  $cpt_del = 0;
  while ($cpt < $group["resource_nb"]) {
    $cpt++;
    $r_id = $group["resource$cpt"];

    $query = "delete from ResourceGroup
      where resourcegroup_rgroup_id='$id' and resourcegroup_resource_id='$r_id'";
    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    if ($retour) {
      $cpt_del++;
    }
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ResourceGroupResourceGroup conditionnal insertion       //
// Parameters:
//   - $group[] : group hash info : keys used : id, resourcegroup_nb, resourcegroup_X
// Return: number of resource groups inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroupresourcegroup_insert($group) {
  global $auth, $cdg_sql;

  $id = $group["id"];
  $cpt = 0;
  $cpt_ins = 0;

  $parents = get_group_parents($id);

  while ($cpt < $group["resourcegroup_nb"]) {
    $cpt++;
    $g_id = $group["resourcegroup_$cpt"];

    $query = "select * from ResourceGroupResourceGroup
      where resourcegroupresourcegroup_parent_id='$id'
        and resourcegroupresourcegroup_child_id='$g_id'";
    display_debug_msg($query, $cdg_sql);
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist and doesn't create a loop, insert it
    if ( ($test_q->num_rows() == 0)
         && (! in_array($g_id, $parents))
         && ($id != $g_id)
       ) {
        $query = "insert into ResourceGroupResourceGroup (resourcegroupresourcegroup_parent_id,
        resourcegroupresourcegroup_child_id) values ($id, $g_id)";

      display_debug_msg($query, $cdg_sql);
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ResourceGroupResourceGroup deletion                     //
// Parameters:
//   - $group[] : group hash info : keys used : id, resourcegroup_nb, resourcegroup_X
// Return: number of resource groups deleted
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroupresourcegroup_delete($group) {
  global $auth, $cdg_sql;

  $id = $group["id"];
  $cpt = 0;
  $cpt_del = 0;
  while ($cpt < $group["resourcegroup_nb"]) {
    $cpt++;
    $g_id = $group["resourcegroup_$cpt"];

    $query = "delete from ResourceGroupResourceGroup
      where resourcegroupresourcegroup_parent_id='$id' and resourcegroupresourcegroup_child_id='$g_id'";

    display_debug_msg($query, $cdg_sql);
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    if ($retour) {
      $cpt_del++;
    }
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the resources of the given group                    //
// Parameters:
//   - $group[] : Group parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_resourcegroup($group) {
  global $cdg_sql, $ctype_resource;

  $obm_q = new DB_OBM; 
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);

  $id = $group["id"];
  $entity = $group["entity"];
  $new_order = $group["new_order"];
  $order_dir = $group["order_dir"];

  if (($entity == "resourcegroup_resource") && (trim($new_order) != "")) {
    $order = "order by $new_order $order_dir";
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
      WHERE resourcegroup_rgroup_id='$id'
         $order
         $limit";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the groups members of the given group               //
// Parameters:
//   - $group[] : Group parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_resourcegroup_resourcegroup($group) {
  global $cdg_sql, $ctype_resource;

  $obm_q = new DB_OBM; 
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);

  $id = $group["id"];
  $entity = $group["entity"];
  $new_order = $group["new_order"];
  $order_dir = $group["order_dir"];

  if (($entity == "resourcegroup_resourcegroup") && (trim($new_order) != "")) {
    $order = "order by $new_order $order_dir";
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
      WHERE resourcegroupresourcegroup_parent_id='$id' 
         $order
         $limit";

  $obm_q = new DB_OBM; 
  display_debug_msg($query, $cdg_sql);
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

  $query = "select count(*) from ResourceGroup
    where resourcegroup_rgroup_id='$id'";

  display_debug_msg($query, $cdg_sql);
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

  $query = "select * from RGroup where rgroup_id='$id'";

  display_debug_msg($query, $cdg_sql);
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
  global $cdg_sql, $uid, $ctype_resource;

  if ($id != "") {
    $where_id = "and rgroup_id!='$id'";
  }

  $query = "SELECT rgroup_id, rgroup_name
    FROM RGroup
    WHERE rgroup_name='$name'
      and (rgroup_privacy=0 OR rgroup_usercreate='$uid')
      $where_id";

  display_debug_msg($query, $cdg_sql);
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
function run_query_check_resourcegroup($id, $name, $desc) {
  global $cdg_sql, $uid, $ctype_resource;

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
  if ($wdesc != "") $wdesc = "or $wdesc";

  $query = "SELECT distinct rgroup_id, rgroup_name, rgroup_desc
     FROM RGroup
     WHERE
       (rgroup_privacy=0 OR rgroup_usercreate='$uid')
       and ($wname $wdesc)
       $where_id";

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
  $g_q = run_query_check_resourcegroup($id, $name, $desc);

  return $g_q;
}


///////////////////////////////////////////////////////////////////////////////
// Group Form Data checking and formatting
// Parameters:
//   - $group[]  : values checked
//     keys used : name, desc
///////////////////////////////////////////////////////////////////////////////
function check_data_form($group) {
  global $l_fill_name;
  global $l_group_exists;
  global $uid, $err_msg, $l_system_no_priv, $l_only_owner_priv;

  $id = $group["id"];
  $name = $group["name"];
  $desc = $group["desc"];
  $lgroup = get_resourcegroup_info($id);
  $usercreate = $lgroup["usercreate"];

  // MANDATORY: Group name not empty
  if (trim($name) == "") {
    $err_msg = $l_fill_name;
    return false;
  }

  // MANDATORY: RGroup name unique
  if (get_resourcegroup_name_exists($name, $id)) {
    $err_msg = "$l_group_exists ($name)";
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

  $query = "select resourcegroupresourcegroup_parent_id
    from ResourceGroupResourceGroup where resourcegroupresourcegroup_child_id='$id'";

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


</script>
