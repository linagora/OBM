<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : right.inc                                                    //
//     - Desc : OBM Rights Objects and functions                             //
// 2005-08-08 Florent Goalabré                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$extra_js .= "
function remove_allelements(parentId) {
  // Remove all elements in the list form
  target = window.document;
  container = target.getElementById(parentId)
  while(container.hasChildNodes()==true){
    var ChildN=container.childNodes[0];
    container.removeChild(ChildN);
  }
}

function show_hide_read_right(field) {
  // Show/Hide read rights
  target = window.document;
  lnk_read = target.getElementById('lnk_accept_read');
  sel_read = target.getElementById('sel_accept_read');
  if (field.checked) {
    remove_allelements('sel_accept_read');
    lnk_read.style.display = 'none';
  } else {
    lnk_read.style.display = 'inline';
  }
}

function show_hide_write_right(field) {
  // Show/Hide write rights 
  target = window.document;
  lnk_write = target.getElementById('lnk_accept_write');
  sel_write = target.getElementById('sel_accept_write');
  if (field.checked) {
    remove_allelements('sel_accept_write');
    lnk_write.style.display = 'none';
  } else {
    lnk_write.style.display = 'inline';
  }
}
";


///////////////////////////////////////////////////////////////////////////////
// Display Rights admin panel
// Parameters:
//   - $entity    : entity to admminister the right for
//   - $entity_id : entity id
///////////////////////////////////////////////////////////////////////////////
function of_right_dis_admin($entity, $entity_id) {
  global $display, $auth;

  $uid = $auth->auth["uid"];
  $consumer = "user";
  $consumer_id = "$uid";
  $writable_entity = of_right_entity_for_consumer($entity, $consumer, $consumer_id, "write");

  // Default value in case entity id not set (eg: action admin_right)
  if (!isset($entity_id) || ($entity_id == "")) {
    if ($entity == "calendar") {
      $entity_id = $auth->auth["uid"];
    } elseif ($entity == "resource") {
      $entity_id = $writable_entity["entity"][0]["id"];
    }
  }
  $consumers = of_right_consumer_for_entity($entity, $entity_id, $consumer);

  $display["features"] = html_entitywritable_bar($writable_entity ,$entity_id, $entity);

  return html_dis_right_admin($consumers, $entity_id);
}


///////////////////////////////////////////////////////////////////////////////
// User selection for agenda management admin panel
// Parameters:
//   - $writable_user : Array of user on who you can manage the calandar
///////////////////////////////////////////////////////////////////////////////
function html_entitywritable_bar($writable_entity,$entity_id, $entity) {
  global $l_entity_right, $l_validate;

  // Entity Selection
  if (count($writable_entity["ids"]) > 0) {
    $dis_sel .= "<select name=\"param_entity\">";
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
    <input type=\"hidden\" name=\"action\" value=\"rights_admin\" />
    $dis_duration
    <input type=\"submit\" value=\"$l_validate\" />
    ";

    $block = "
    <div class=\"agendaBar\">
    <table cellpadding=\"0\" cellspacing=\"0\" class=\"agendaCal\">
    <tr>
      <td class=\"agendaHead2\">$l_entity_right</td>
    </tr>
    <tr>
      <td class=\"agendaNoEvent\" align=\"left\">
      <form action=\"".$_SERVER["PHP_SELF"]."\" method=\"get\" $check_sel_user>
      $dis_sel
      </form>
      </td>
    </tr>
    </table>
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
  global $l_list_right,$l_authorize,$l_deny,$set_theme, $ico_delete;
  global $l_public;
  global $ico_add_user,$popup_height,$popup_width,$l_users,$path;

  foreach ($consumers["consumer"] as $one_consumer) {
    $id = $one_consumer["id"];
    $label = $one_consumer["label"];
    $write = $one_consumer["write"];
    $read = $one_consumer["read"];

    if ($id == 0) {
      if ($read == 1) {
        $style_read = "display:none;";
        $public_read_checked = "checked=\"checked\"";
      }
      if ($write == 1) {
        $style_write = "display:none;";
        $public_write_checked = "checked=\"checked\"";
      }
    } else {
      if ($write == 1) {
        $dis_write_ok .= "<div class=\"elementRow\" id=\"sel_accept_write$id\">
        <a href=\"javascript: remove_element('sel_accept_write$id','sel_accept_write');\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\"></a>
        $label
       <input value=\"$id\" name=\"sel_accept_write[]\" type=\"hidden\" />
       </div>";
      }
      if ($read == 1) {
        $dis_read_ok .= "<div class=\"elementRow\" id=\"sel_accept_read$id\">
        <a href=\"javascript: remove_element('sel_accept_read$id','sel_accept_read');\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_delete\"></a>
        $label
       <input value=\"$id\" name=\"sel_accept_read[]\" type=\"hidden\" />
       </div>";
      }
    }
  }

  $block_consumer_read_public = "
    <td class=\"detailLabel\">
    $l_public
    </td>
    <td class=\"detailForm\" id=\"cb_read_public\">
      <input onchange=\"show_hide_read_right(this)\" onclick=\"show_hide_read_right(this)\" name=\"cb_read_public\" value=\"1\" type=\"checkbox\" $public_read_checked />
    </td>";
    $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_read";
    $block_consumer_read = "
    <td class=\"detailLabel\" id=\"head_accept_read\">
    $l_users
    <a style=\"$style_read\" id=\"lnk_accept_read\" href=\"javascript: return false;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add_user\" alt=\"[Add]\" />
    </a>
    </td>
    <td class=\"detailForm\" id=\"sel_accept_read\">
      $dis_read_ok
    </td>";

   $block_consumer_write_public = "
    <td class=\"detailLabel\">
    $l_public
    </td>
    <td class=\"detailForm\" id=\"cb_write_public\">
      <input onchange=\"show_hide_write_right(this)\" onclick=\"show_hide_write_right(this)\" name=\"cb_write_public\" value=\"1\" type=\"checkbox\" $public_write_checked />
    </td>";
    $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_write";
    $block_consumer_write = "
    <td class=\"detailLabel\" id=\"head_accept_write\">
    $l_users
      <a style=\"$style_write\" id=\"lnk_accept_write\" href=\"javascript: return false;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add_user\" alt=\"[Add]\" />
     </a>
     </td>
     <td class=\"detailForm\" id=\"sel_accept_write\">
       $dis_write_ok
     </td>";

  $block = "
  <form method=\"get\" name=\"f_mod_right\"
    action=\"".url_prepare($_SERVER["PHP_SELF"])."\">

  <div class=\"detailHead\">$l_read_permission</div>
  <table class=\"detail\">
  <tr>
    $block_consumer_read_public
  </tr>
  <tr>
    $block_consumer_read
  </tr>
  </table>

  <div class=\"detailHead\">$l_write_permission</div>
  <table class=\"detail\">
  <tr>
    $block_consumer_write_public
  </tr>
  <tr>
    $block_consumer_write
  </tr>
  </table>

  <div class=\"detailButton\">
    <input type=\"hidden\" name=\"param_entity\" value=\"$entity_id\" />
    <input type=\"hidden\" name=\"action\" value=\"rights_update\" />
    <input type=\"submit\" value=\"$l_list_right\" />
  </div>
  </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
//////////////////////////           QUERY               //////////////////////
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Get the list of entities for the consumer given with the corresponding right
// Parameters:
//   - $entity      : target entity
//   - $consumer    : consumer type
//   - $consumer_id : consumer id
//   - $right       : right to get and select from : "read" or "write"
//   - $ids_set     : array of ids restricting the result
// Returns:
//   array ("ids"=>(id1, id2, id3), "entity"=>((id1,label1), (id2,label2)))
///////////////////////////////////////////////////////////////////////////////
function of_right_entity_for_consumer($entity, $consumer, $consumer_id, $right="read", $ids_set="") {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  if ($entity == "calendar") {
    $table_entity = "UserObm";
    $e_id = "e.userobm_id";
    $ctt[0]["type"] = "field";
    $ctt[0]["value"] = "userobm_lastname";
    $ctt[1]["type"] = "string";
    $ctt[1]["value"] = " ";
    $ctt[2]["type"] = "field";
    $ctt[2]["value"] = "userobm_firstname";
    $e_label = sql_string_concat($db_type, $ctt);
  } else if ($entity == "resource") {
    $table_entity = "Resource";
    $e_id = "e.resource_id";
    $e_label = "e.resource_label";
  }

  if ($right != "all") {
    $where_right = "AND entityright_$right = 1";
  }

  $query = "SELECT
      $e_id as id,
      $e_label as label,
      entityright_consumer_id,
      entityright_read,
      entityright_write
    FROM EntityRight
      LEFT JOIN $table_entity as e ON entityright_entity_id = $e_id
    WHERE entityright_entity = '$entity'
      AND entityright_consumer = '$consumer'
      AND (entityright_consumer_id = '$consumer_id'
           OR entityright_consumer_id = 0)
      $where_right
    ORDER BY $e_label";

  display_debug_msg($query, $cdg_sql, "of_right_entity_for_consumer()");
  $obm_q->query($query);

  $ret_array = array();
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
//   - $right       : right to get and select from : "all", "read" or "write"
// Returns:
//   array ("ids"=>(id1, id2, id3), "entity"=>((id1,label1), (id2,label2))
///////////////////////////////////////////////////////////////////////////////
function of_right_consumer_for_entity($entity, $entity_id, $consumer, $right="all") {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  if ($consumer == "user") {
    $table_consumer = "UserObm";
    $c_id = "c.userobm_id";
    $ctt[0]["type"] = "field";
    $ctt[0]["value"] = "userobm_lastname";
    $ctt[1]["type"] = "string";
    $ctt[1]["value"] = " ";
    $ctt[2]["type"] = "field";
    $ctt[2]["value"] = "userobm_firstname";
    $c_label = sql_string_concat($db_type, $ctt);
  } else if ($consumer == "group") {
    $table_consumer = "UGroup";
    $c_id = "c.group_id";
    $c_label = "e.group_name";
  }

  if ($right != "all") {
    $where_right = "AND entityright_$right = 1";
  }

  $query = "SELECT
      $c_id as id,
      $c_label as label,
      entityright_consumer_id,
      entityright_read,
      entityright_write
    FROM EntityRight
      LEFT JOIN $table_consumer as c ON entityright_consumer_id = $c_id
    WHERE entityright_entity = '$entity'
      AND entityright_consumer = '$consumer'
      AND entityright_entity_id = '$entity_id'
      $where_right
    ORDER BY $c_label";

  display_debug_msg($query, $cdg_sql, "of_right_consumer_for_entity()");
  $obm_q->query($query);

  $ret_array = array();
  while($obm_q->next_record()) {
    $id = $obm_q->f("id");
    $label = $obm_q->f("label");
    $entity_id = $obm_q->f("entityright_entity_id");
    $read = $obm_q->f("entityright_read");
    $write = $obm_q->f("entityright_write");
    $id_array[] = $id;
    $con_array[] = array(
			 "id"    => $id,
			 "label" => $label,
			 "entity_id" => $entity_id,
			 "read" => $read,
			 "write" => $write
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
// Update the list of the rights give to users.
// Parameters:
//   - $param  : hash entry with entity and right values
//   - $entity : entity type to update
///////////////////////////////////////////////////////////////////////////////
function of_right_update_right($param, $entity) {
  global $cdg_sql, $auth, $err_msg, $l_of_right_err_auth;

  $consumer = "user";

  $eid = $param["entity_id"];
  $accept_write = $param["accept_w"];
  $accept_read = $param["accept_r"];
  $public_write = $param["public_w"];
  $public_read = $param["public_r"];

  if (! is_array($accept_read)) {
    $accept_read = array();
  }
  if (! is_array($accept_write)) {
    $accept_write = array();
  }

  // Check the user have the sufficient right to update rights
  if (($eid != $auth->auth["uid"]) && ($entity == "calendar")) {
    $writable_entity = of_right_entity_for_consumer($entity, $consumer, $consumer_id, "write");
    if (!in_array($eid, $writable_entity["ids"])) {
      $err_msg = $l_of_right_err_auth;
      return 0;
    }
  }

  $obm_db = new DB_OBM;
  $query = "DELETE
    FROM EntityRight
    WHERE entityright_entity_id = '$eid'
      AND entityright_entity = '$entity'";
  display_debug_msg($query, $cdg_sql, "of_right_update_right(1)");
  $obm_db->query($query);

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
      '$consumer',
      '0',
      '$pr',
      '$pw')";
    display_debug_msg($query, $cdg_sql, "of_right_update_right(2)");
    $obm_db->query($query);
  }

  if (! $public_read) {
    // Insert all read entries (with write right correctly set)
    foreach ($accept_read as $key => $id) {
      $w = (in_array($id, $accept_write) ? 1 : 0);
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
        '$consumer',
        '$id',
        '1',
        '$w')";
      display_debug_msg($query, $cdg_sql, "of_right_update_right(3)");
      $obm_db->query($query);
    }
  }

  if (! $public_write) {
    // Insert all write entries not already done by the read loop
    foreach ($accept_write as $key => $id) {
      if (! in_array($id, $accept_read)) {
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
          '$consumer',
          '$id',
          '0',
          '1')";
	display_debug_msg($query, $cdg_sql, "of_right_update_right(4)");
	$obm_db->query($query);
      }
    }
  }
}

?>
