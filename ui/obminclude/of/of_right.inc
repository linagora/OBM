<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : right.inc                                                    //
//     - Desc : OBM Rights Objects and functions                             //
// 2005-08-08 Florent Goalabré                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Display Rights admin panel
// Parameters:
//   - $entity    : entity to admminister the right for
//   - $entity_id : entity id
//   - $real_entity : [optionnal] if set, real data entity (eg mailbox->user)
///////////////////////////////////////////////////////////////////////////////
function of_right_dis_admin($entity, $entity_id, $real_entity="") {
  global $display, $obm, $l_of_right_err_empty;

  if (!isset($entity_id) || ($entity_id == "")) {
    $display["msg"] .= display_err_msg($l_of_right_err_empty);
    return;
  }

  $admin_entity = of_right_entity_for_user($entity, $obm["uid"], "admin","", $real_entity);
  $display["features"] = html_entityadmin_bar($admin_entity ,$entity_id, $entity);

  $consumers = of_right_consumer_for_entity($entity, $entity_id);

  return html_dis_right_admin($consumers, $entity_id);
}


///////////////////////////////////////////////////////////////////////////////
// User selection for management admin panel
// Parameters:
//   - $writable_user : Array of user on who you can manage the calandar
///////////////////////////////////////////////////////////////////////////////
function html_entityadmin_bar($writable_entity,$entity_id, $entity) {
  global $l_entity_right, $l_validate;

  // Entity Selection
  if (count($writable_entity["ids"]) > 0) {
    $dis_sel .= "<select name=\"entity_id\">";
    foreach ($writable_entity["entity"] as $entity) {
      $id = $entity["id"];
      $label =  $entity["label"];
      $dis_sel .= "\n<option value=\"$id\"";
      if ($id == $entity_id ) {
        $tag = " selected=\"selected\"";
      }
      $dis_sel .= " $tag>$label</option>";
      $tag = "";
    }
    $dis_sel .= "</select>
    <br />
        $dis_duration
        ";

    $block = "
    <div class=\"portlet\" id=\"rightManagerPortlet\">
      <h1>$l_entity_right</h1>
      <div>
      <form action=\"".$_SERVER["PHP_SELF"]."\" method=\"get\" $check_sel_user>
      <fieldset class=\"detail\">
      $dis_sel
      </fieldset>
      <fieldset class=\"buttons\">
      <input type=\"hidden\" name=\"action\" value=\"rights_admin\" />
      <input type=\"submit\" value=\"$l_validate\" />
      </fieldset>
      </form>
      </div>  
    </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Rights admin panel
// Parameters:
//   - $consumers : array of all consumers for the entity
//   - $entity_id : entity selected
///////////////////////////////////////////////////////////////////////////////
function html_dis_right_admin(&$consumers, $entity_id) {
  global $l_authorize_list,$l_read_permission, $l_write_permission;
  global $l_right_validate ,$l_authorize,$l_deny, $ico_delete;
  global $l_public,$l_admin_permission;
  global $ico_add,$l_users,$path, $l_groups;

  $l_userSearch = phpStringToJsString("$l_users...");
  foreach ($consumers["consumer"] as $one_consumer) {
    $id = $one_consumer["id"];
    $entity = $one_consumer["entity"];
    $label = $one_consumer["label"];
    $write = $one_consumer["write"];
    $read = $one_consumer["read"];
    $admin = $one_consumer["admin"];

    if ($id == 0) {
      if ($read == 1) {
        $style_read = "visibility:hidden;";
        $public_read_checked = "checked=\"checked\"";
      }
      if ($write == 1) {
        $style_write = "visibility:hidden;";
        $public_write_checked = "checked=\"checked\"";
      }
    } else {
      if ($write == 1) {
	$sel_id = "data-$entity-$id";
	$div_id = "sel_accept_write-data-$entity-$id";
        ${'dis_'.$entity.'_write_ok'} .= "<div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_accept_".$entity."_write');\"><img src=\"$ico_delete\"></a>
        $label
       <input value=\"$sel_id\" name=\"sel_accept_write[]\" type=\"hidden\" />
       </div>";
      }
      if ($read == 1) {
	$sel_id = "data-$entity-$id";
	$div_id = "sel_accept_read-data-$entity-$id";
        ${'dis_'.$entity.'_read_ok'} .= "<div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_accept_".$entity."_read');\"><img src=\"$ico_delete\"></a>
        $label
       <input value=\"$sel_id\" name=\"sel_accept_read[]\" type=\"hidden\" />
       </div>";
      }
      if ($admin == 1) {
	$sel_id = "data-$entity-$id";
        $div_id = "sel_accept_admin-data-$entity-$id";
        ${'dis_'.$entity.'_admin_ok'} .= "<div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_accept_".$entity."_admin');\"><img src=\"$ico_delete\"></a>
        $label
       <input value=\"$sel_id\" name=\"sel_accept_admin[]\" type=\"hidden\" />
       </div>";
      }      
    }
  }

  $block_consumer_read_public = "
    <td class=\"detailLabel\">
    $l_public
    </td>
    <td class=\"detailForm\" id=\"cb_read_public\">
    <input onchange=\"togglePrivacy(this,'readRightBox');\" onclick=\"togglePrivacy(this,'readRightBox')\"
    name=\"cb_read_public\" value=\"1\" type=\"checkbox\" $public_read_checked />
    </td>";

    $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_user_read";
    $popup_user_add_button = "
    <a id=\"lnk_accept_read\" href=\"javascript: return false;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
      <img src=\"$ico_add\" alt=\"[Add]\" />
    </a>";
    $url = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_group_read";
    $popup_group_add_button = "
      <a id=\"lnk_accept_admin\" href=\"javascript: return false;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
       <img src=\"$ico_add\" alt=\"[Add]\" />
      </a>";

    $block_consumer_read = "
    <tr>
    <td class=\"detailLabel\">
    $l_users
    $popup_user_add_button
    </td>
    <td>
    <input type=\"text\" id=\"userReadSearch\" autocomplete=\"off\" /> 
    </td>
    </tr>
    <tr>
    <td class=\"detailLabel\">
    $l_groups
    $popup_group_add_button
    </td>
    <td>
    <input type=\"text\" id=\"groupReadSearch\" autocomplete=\"off\" /> 
    </td>
    </tr>       
    <tr>
    <td colspan=\"2\" class=\"detailForm\" id=\"sel_accept_read\">
      <fieldset id='sel_accept_user_read'>
      <legend>$l_users</legend>
      $dis_user_read_ok
      </fieldset>
      <fieldset id='sel_accept_group_read'>
      <legend>$l_groups</legend>
      $dis_group_read_ok
      </fieldset>
    </td>
    </tr>";

   $block_consumer_write_public = "
    <tr>
    <td class=\"detailLabel\">
    $l_public
    </td>
    <td class=\"detailForm\" id=\"cb_write_public\">
    <input onchange=\"togglePrivacy(this,'writeRightBox');\" onclick=\"togglePrivacy(this,'writeRightBox')\"
    name=\"cb_write_public\" value=\"1\" type=\"checkbox\" $public_write_checked />
    </td>";

    $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_user_write";
    $popup_user_add_button = "
      <a id=\"lnk_accept_write\" href=\"javascript: return false;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
       <img src=\"$ico_add\" alt=\"[Add]\" />
     </a>";
    $url = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_group_write";
    $popup_group_add_button = "
      <a id=\"lnk_accept_admin\" href=\"javascript: return false;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
       <img src=\"$ico_add\" alt=\"[Add]\" />
      </a>";

    $block_consumer_write = "
    <tr>
    <td class=\"detailLabel\">
    $l_users
    $popup_user_add_button
    </td>
    <td>
    <input type=\"text\" id=\"userWriteSearch\" autocomplete=\"off\" /> 
    </td>
    </tr>
    <tr>
    <td class=\"detailLabel\">
    $l_groups
    $popup_group_add_button
    </td>
    <td>
    <input type=\"text\" id=\"groupWriteSearch\" autocomplete=\"off\" /> 
    </td>
    </tr>       
    <tr>
    <td colspan=\"2\" class=\"detailForm\" id=\"sel_accept_write\">
      <fieldset id='sel_accept_user_write'>
      <legend>$l_users</legend>
      $dis_user_write_ok
      </fieldset>
      <fieldset id='sel_accept_group_write'>
      <legend>$l_groups</legend>
      $dis_group_write_ok
      </fieldset>    
    </td>
    </tr>";

    $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_user_admin";
    $popup_user_add_button = "
      <a id=\"lnk_accept_admin\" href=\"javascript: return false;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
       <img src=\"$ico_add\" alt=\"[Add]\" />
      </a>";
    $url = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_group_admin";
    $popup_group_add_button = "
      <a id=\"lnk_accept_admin\" href=\"javascript: return false;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
       <img src=\"$ico_add\" alt=\"[Add]\" />
      </a>";

    $block_consumer_admin = "
    <tr>
    <td class=\"detailLabel\">
    $l_users
    $popup_user_add_button
    </td>
    <td>
    <input type=\"text\" id=\"userAdminSearch\" autocomplete=\"off\" /> 
    </td>
    </tr>
    <td class=\"detailLabel\">
    $l_groups
    $popup_group_add_button
    </td>
    <td>
    <input type=\"text\" id=\"groupAdminSearch\" autocomplete=\"off\" /> 
    </td>
    </tr>       
    <tr>
    <tr>
    <td colspan=\"2\" class=\"detailForm\" id=\"sel_accept_admin\">
      <fieldset id='sel_accept_user_admin'>
      <legend>$l_users</legend>
      $dis_user_admin_ok
      </fieldset>
      <fieldset id='sel_accept_group_admin'>
      <legend>$l_groups</legend>
      $dis_group_admin_ok
      </fieldset>    
    </td>
    </tr>";
    

  $block = "
  <script type=\"text/javascript\">
    function togglePrivacy(formElement, dataElement) {
      if(formElement.checked) {
        $(dataElement).setStyle('visibility','hidden');
      } else {
        $(dataElement).setStyle('visibility','');
      }
    }
  </script>
  <form method=\"post\" name=\"f_mod_right\"
    action=\"".url_prepare($_SERVER["PHP_SELF"])."\">

  <fieldset class=\"detail infos\">
  <legend>$l_read_permission</legend>
  <table>
  <tr>
    $block_consumer_read_public
  </tr>
  <tbody style=\"$style_read\" id=\"readRightBox\">
    $block_consumer_read
  </tbody>
  </table>
  </fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_write_permission</legend>
  <table>
  <tr>
    $block_consumer_write_public
  </tr>
  <tbody style=\"$style_write\" id=\"writeRightBox\">
    $block_consumer_write
  </tbody>
  </table>
  </fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_admin_permission</legend>
  <table>
  <tbody>
    $block_consumer_admin
  </tbody>
  </table>
  </fieldset>

  <fieldset class=\"buttons\">
    <input type=\"hidden\" name=\"entity_id\" value=\"$entity_id\" />
    <input type=\"hidden\" name=\"action\" value=\"rights_update\" />
    <input type=\"submit\" value=\"$l_right_validate\" />
  </fieldset>
  </form>
  <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search&ajax=1', 'sel_accept_user_admin', 'userAdminSearch', {name:'sel_accept_admin'});
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search&ajax=1', 'sel_accept_user_write', 'userWriteSearch', {name:'sel_accept_write'});
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search&ajax=1', 'sel_accept_user_read', 'userReadSearch', {name:'sel_accept_read'});
      new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search&ajax=1', 'sel_accept_group_admin', 'groupAdminSearch', {name:'sel_accept_admin'});
      new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search&ajax=1', 'sel_accept_group_write', 'groupWriteSearch', {name:'sel_accept_write'});
      new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search&ajax=1', 'sel_accept_group_read', 'groupReadSearch', {name:'sel_accept_read'});
    });
  </script>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
//////////////////////////           QUERY               //////////////////////
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Get the list of entities for the consumer given with the corresponding right
// Parameters:
//   - $entityu     : target entity (table name with capital)
//   - $user_id : user id
//   - $right       : right to get and select from : "read", "write" or "admin"
//   - $ids_set     : array of ids restricting the result
//   - $real_entity : real data entity (eg: userobm for entity calendar)
// Returns:
//   array ("ids"=>(id1, id2, id3), "entity"=>((id1,label1), (id2,label2)))
///////////////////////////////////////////////////////////////////////////////
function of_right_entity_for_user($entityu, $user_id, $right="read", $ids_set="", $real_entity="") {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $ent_array = array();
  $id_array = array();

  $entity = strtolower($entityu);
  if ($real_entity != "") {
    $r_entity = $real_entity;
  } else {
    $r_entity = $entity;
  }

  if ($r_entity == "userobm") {
    $table_entity = "UserObm";
    $e_id = "e.userobm_id";
    $ctt[0]["type"] = "field";
    $ctt[0]["value"] = "userobm_lastname";
    $ctt[1]["type"] = "string";
    $ctt[1]["value"] = " ";
    $ctt[2]["type"] = "field";
    $ctt[2]["value"] = "userobm_firstname";
    $e_label = sql_string_concat($db_type, $ctt);
    $u = get_user_info($user_id);
    $union = "UNION SELECT '$u[id]' as id, '$u[lastname] $u[firstname]' as label, '$entity' as entityright_entity" ;
  } else {
    $table_entity = "$entityu";
    $e_id = "e.${entity}_id";
    $e_label = "e.${entity}_name";
  }

  if ($right != "all") {
    $where_right = "AND entityright_$right = 1";
  }

  $query = "SELECT
      $e_id as id,
      $e_label as label,
      '$entity' as entityright_entity
    FROM EntityRight
      LEFT JOIN $table_entity as e ON entityright_entity_id = $e_id
    WHERE entityright_entity = '$entity'
      AND ( (entityright_consumer = 'user' AND (entityright_consumer_id = '$user_id' OR entityright_consumer_id = 0))
        OR (entityright_consumer = 'group'  AND (entityright_consumer_id IN (SELECT of_usergroup_group_id FROM of_usergroup WHERE of_usergroup_user_id = '$user_id')
            OR entityright_consumer_id = 0)))
        $where_right
    GROUP BY id, label, entityright_entity
    $union
    ORDER BY entityright_entity, label";

  display_debug_msg($query, $cdg_sql, "of_right_entity_for_user()");
  $obm_q->query($query);

  while($obm_q->next_record()) {
    $id = $obm_q->f("id");
    if ( ($ids_set == "")
	 || ( (is_array($ids_set)) && (in_array($id, $ids_set)) ) ) {
      $label = $obm_q->f("label");
      $id_array[] = $id;
      $ent_array[] = array(
			   "id"    => $id,
			   "label" => $label
			   );
    }
  }
  $ret_array = array();
  $ret_array["ids"] = array();
  if (is_array($id_array)) {
    $ret_array["ids"] = $id_array;
  }
  $ret_array["entity"] = array();
  if (is_array($ent_array)) {
    $ret_array["entity"] = $ent_array;
  }

  return $ret_array;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of consumers for the entity given with the corresponding right
// Parameters:
//   - $entity      : target entity
//   - $entity_id   : entity id
//   - $consumer    : consumer type
//   - $right       : right to get and select from : "all", "read", "write" or
//                    "admin"
// Returns:
//   array ("ids"=>(id1, id2, id3), "entity"=>((id1,label1), (id2,label2))
///////////////////////////////////////////////////////////////////////////////
function of_right_consumer_for_entity($entity, $entity_id, $consumer="all", $right="all") {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  if(count($entity_id) == 0) {
    return array("ids" => array(), "entity" => array());
  }
  if(is_array($entity_id)) {
    $entity_id = implode(",",$entity_id);
  }

  $id_array = array();
  $con_array = array();

  if ($consumer == "user" || $consumer == "all" ) {

    $table_consumer = "UserObm";
    $c_id = "userobm_id";
    $ctt[0]["type"] = "field";
    $ctt[0]["value"] = "userobm_lastname";
    $ctt[1]["type"] = "string";
    $ctt[1]["value"] = " ";
    $ctt[2]["type"] = "field";
    $ctt[2]["value"] = "userobm_firstname";
    $c_label = sql_string_concat($db_type, $ctt);
    $entity_table[] = "Select $c_label as entitydata_label, $c_id as entitydata_id, 'user' as entitydata_kind FROM UserObm";
    $u = get_user_info($user_id);
  }
  if ($consumer == "group" || $consumer == "all" ) {
    $table_consumer = "UGroup";
    $c_id = "group_id";
    $c_label = "group_name";
    $entity_table[] = "Select $c_label as entitydata_label, $c_id as entitydata_id, 'group' as entitydata_kind FROM UGroup";
  }

  if ($right != "all") {
    $where_right = "AND entityright_$right = 1";
  }
  $table_consumer = implode(" UNION ", $entity_table);
  $query = "SELECT
      entitydata_id as id,
      entitydata_label as label,
      entitydata_kind as consumer,
      entityright_consumer_id,
      entityright_read,
      entityright_write,
      entityright_admin
    FROM EntityRight
      LEFT JOIN ($table_consumer) as entity ON entityright_consumer_id = entitydata_id AND entityright_consumer = entitydata_kind
    WHERE entityright_entity_id IN ($entity_id) AND entityright_entity = '$entity'
      $where_right
    ORDER BY entitydata_kind, entitydata_label";
  display_debug_msg($query, $cdg_sql, "of_right_consumer_for_entity()");
  $obm_q->query($query);
  $ret_array = array();
  while($obm_q->next_record()) {
    $id = $obm_q->f("id");
    $label = $obm_q->f("label");
    $entity_id = $obm_q->f("entityright_entity_id");
    $entity = $obm_q->f("consumer");
    $read = $obm_q->f("entityright_read");
    $write = $obm_q->f("entityright_write");
    $admin = $obm_q->f("entityright_admin");
    $id_array[] = $id;
    $con_array[] = array(
			 "id"    => $id,
			 "label" => $label,
                         "entity_id" => $entity_id,
                         "entity" => $entity,
			 "read" => $read,
			 "write" => $write,
			 "admin" => $admin
			 );
  }

  $ret_array["ids"] = array();
  if (is_array($id_array)) {
    $ret_array["ids"] = $id_array;
  }
  $ret_array["consumer"] = array();
  if (is_array($con_array)) {
    $ret_array["consumer"] = $con_array;
  }

  return $ret_array;
}


///////////////////////////////////////////////////////////////////////////////
// Update Rights given to users.
// Parameters:
//   - $param  : hash entry with entity and right values
//   - $entity : entity type to update
//   - $real_entity : [optionnal] if set, real data entity (eg mailbox->user)
///////////////////////////////////////////////////////////////////////////////
function of_right_update_right($param, $entity, $real_entity="",$check_perms=true) {
  global $cdg_sql, $obm, $err, $l_of_right_err_auth,$obm;

  $consumer_ereg = "/^data-(user|group)-([0-9]+)$/";

  $eid = $param["entity_id"];
  $accept_write = $param["accept_write"];
  $accept_read = $param["accept_read"];
  $accept_admin = $param["accept_admin"];
  $public_write = $param["write_public"];
  $public_read = $param["read_public"];

  if (! is_array($accept_read)) {
    $accept_read = array();
  }
  if (! is_array($accept_write)) {
    $accept_write = array();
  }
  if (! is_array($accept_admin)) {
    $accept_admin = array();
  }

  $accept_admin = array_unique($accept_admin);
  $accept_write = array_unique($accept_write);
  $accept_read = array_unique($accept_read);

  // Rights on external objects : defined by module access
  // Rights on userobm entity : user need write right on the entity
  // If user manipulating NOT its own data, check if he has rights
  if (($eid != $obm["uid"] || $real_entity != 'userobm') && $check_perms) {
    $admin_entity = of_right_entity_for_user($entity, $obm["uid"], "admin","", $real_entity);
    if (!in_array($eid, $admin_entity["ids"])) {
      $err["msg"] = $l_of_right_err_auth;
      return false;
    }
  }
	
  $obm_q = new DB_OBM;
  $query = "DELETE
    FROM EntityRight
    WHERE entityright_entity_id = '$eid'
      AND entityright_entity = '$entity'";
  display_debug_msg($query, $cdg_sql, "of_right_update_right(1)");
  $obm_q->query($query);
	
  if ($public_read || $public_write) {
    $pr = ($public_read ? 1 : 0);
    $pw = ($public_write ? 1 : 0);
    $query = "INSERT INTO EntityRight (
      entityright_entity,
      entityright_entity_id,
      entityright_consumer,
      entityright_consumer_id,
      entityright_read,
      entityright_write
    ) VALUES (
      '$entity',
      '$eid',
      'user',
      '0',
      '$pr',
      '$pw')";
    display_debug_msg($query, $cdg_sql, "of_right_update_right(2)");
    $obm_q->query($query);
  }

  if (! $public_read) {
    // Insert all read entries (with write right correctly set)
    foreach ($accept_read as $key => $ref) {
      if (preg_match($consumer_ereg,$ref,$matches)) {
        $consumer = $matches[1];
        $id = $matches[2];          
	$w = (in_array($ref, $accept_write) ? 1 : 0);
	$a = (in_array($ref, $accept_admin) ? 1 : 0);
	$query = "INSERT INTO EntityRight (
          entityright_entity,
          entityright_entity_id,
          entityright_consumer,
          entityright_consumer_id,
          entityright_read,
          entityright_write,
          entityright_admin
        ) VALUES (
          '$entity',
          '$eid',
          '$consumer',
          '$id',
          '1',
          '$w',
          '$a')";
	display_debug_msg($query, $cdg_sql, "of_right_update_right(3)");
	$obm_q->query($query);
      }
    }
  }

  if (! $public_write) {
    // Insert all write entries not already done by the read loop
    foreach ($accept_write as $key => $ref) {
      if (preg_match($consumer_ereg,$ref,$matches)) {
        if (! in_array($ref, $accept_read)) {
          $consumer = $matches[1];
          $id = $matches[2];          
	  $a = (in_array($ref, $accept_admin) ? 1 : 0);
	  $query = "INSERT INTO EntityRight (
            entityright_entity,
            entityright_entity_id,
            entityright_consumer,
            entityright_consumer_id,
            entityright_write,
            entityright_admin
          ) VALUES (
            '$entity',
            '$eid',
            '$consumer',
            '$id',
            '1',
            '$a')";
	  display_debug_msg($query, $cdg_sql, "of_right_update_right(4)");
	  $obm_q->query($query);
	}
      }
    }
  }

  foreach ($accept_admin as $key => $ref) {
    $ref_ent = substr($ref, 0, 10);
    if (preg_match($consumer_ereg,$ref,$matches)) {
      if ((! in_array($ref, $accept_read))
        && (! in_array($ref, $accept_write))) {
        $consumer = $matches[1];
        $id = $matches[2];
        $query = "INSERT INTO EntityRight (
          entityright_entity,
          entityright_entity_id,
          entityright_consumer,
          entityright_consumer_id,
          entityright_admin
        ) VALUES (
          '$entity',
          '$eid',
          '$consumer',
          '$id',
          '1')";
        display_debug_msg($query, $cdg_sql, "of_right_update_right(5)");
        $obm_q->query($query);
      }
    }
  }

  return true;
}
///////////////////////////////////////////////////////////////////////////////
// Get the list of users for the entity given with the corresponding right
// Parameters:
//   - $entity      : target entity
//   - $entity_id   : entity id
//   - $right       : right to get and select from : "all", "read", "write" or
//                    "admin"
// Returns:
//   array ("ids"=>(id1, id2, id3), "entity"=>((id1,label1), (id2,label2))
///////////////////////////////////////////////////////////////////////////////
function of_right_users_for_entity($entity, $entity_id, $right="all") {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  if(count($entity_id) == 0) {
    return array("ids" => array(), "entity" => array());
  }
  if(is_array($entity_id)) {
    $entity_id = implode(",",$entity_id);
  }

  if ($right != "all") {
    $where_right = "AND entityright_$right = 1";
  }
  $query = "SELECT
      userobm_firstname,
      userobm_lastname,
      userobm_id,
      entityright_read,
      entityright_write,
      entityright_admin
    FROM UserObm
    LEFT JOIN of_usergroup ON userobm_id = of_usergroup_user_id
    LEFT JOIN EntityRight ON (userobm_id = entityright_consumer_id AND entityright_consumer = 'user')
                          OR (of_usergroup_group_id = entityright_consumer_id AND entityright_consumer = 'group') 
    WHERE entityright_entity_id IN ($entity_id) AND entityright_entity = '$entity' $where_right
    ORDER BY userobm_firstname, userobm_lastname 
  ";
    
  display_debug_msg($query, $cdg_sql, "of_right_users_for_entity()");
  $obm_q->query($query);
  $ret_array = array();
  while($obm_q->next_record()) {
    $id = $obm_q->f("id");
    $label = $obm_q->f("userobm_lastname")." ".$obm_q->f("userobm_firstname");
    $entity_id = $obm_q->f("entityright_entity_id");
    $read = $obm_q->f("entityright_read");
    $write = $obm_q->f("entityright_write");
    $admin = $obm_q->f("entityright_admin");
    $id_array[] = $id;
    $con_array[] = array(
			 "id"    => $id,
			 "label" => $label,
			 "entity_id" => $entity_id,
			 "read" => $read,
			 "write" => $write,
                         "admin" => $admin
			 );
  }

  $ret_array["ids"] = array();
  if (is_array($id_array)) {
    $ret_array["ids"] = $id_array;
  }
  $ret_array["consumer"] = array();
  if (is_array($con_array)) {
    $ret_array["consumer"] = $con_array;
  }

  return $ret_array;

}
/* ACL object model
class OBM_Acl {
  
  var cache;

  function &getInstance() {
    static $singleton = null;
    if (is_null($singleton)) {
      $singleton = new OccurrenceFactory();
    }
    return $singleton;
  }

  function isAllowed($userId, $resourceUri , $action) {
    return $this->getACL($userId, $entityId, $entity,$action);
  }

  function canRead($userId, $resourceUri) {
    return this->isAllowed($userId, $entityId, $entity, 'read');
  }

  function canWrite($userId, $resourceUri) {
    return this->isAllowed($userId, $entityId, $entity, 'write');
  }

  function canAdmin($userId, $resourceUri) {
    return this->isAllowed($userId, $entityId, $entity, 'admin');
  }

  function getEntityRoles($uri , $action = "all") {

  }

  function getEntityReaders($resourceUri) {
    return $this->getEntityRoles($entityId,$entity,'read');
  }
  
  function getEntityWriters($resourceUri) {
    return $this->getEntityRoles($entityId,$entity,'write');
  }

  function getEntityAdmins($resourceUri) {
    return $this->getEntityRoles($entityId,$entity,'admin');
  }

  function getACL($userId, $resourceUri, $action) {
    $uri = explode('/',$res)
  }
  

  function isCached($userId, $resourceUri) {
    if(isset($this->cache['acl'][$userId][$entity][$entityId][$action])) {
      
    }
  }
}
  */
?>
