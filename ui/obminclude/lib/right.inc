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
//   - $user_q :
///////////////////////////////////////////////////////////////////////////////
function dis_right_admin($entity_id, $entity) {
  global $display,$auth,$cagenda_entities;

  $writable_entity = run_query_entity_label_writable($entity);
  if (!isset($entity_id) || ($entity_id == "")) {
    if ($entity == $cagenda_entities["user"]) {
      $entity_id = $auth->auth["uid"];
    } elseif ($entity == $cagenda_entities["resource"]) {
      $entity_id = $writable_entity[0]["id"];
    }
  }
  $entity_right = run_query_entity_right($entity_id, $entity);

  $display["features"] = html_entitywritable_bar($writable_entity,$entity_id, $entity);

  return html_dis_right_admin($entity_right,$entity_id);
}

///////////////////////////////////////////////////////////////////////////////
// User selection for agenda management admin panel
// Parameters:
//   - $writable_user : Array of user on who you can manage the calandar
///////////////////////////////////////////////////////////////////////////////
function html_entitywritable_bar($writable_entity,$entity_id, $entity) {
  global $l_entity_right, $l_validate;

  // Entity Selection
  if (count($writable_entity) > 0) {
    $dis_sel .= "<select name=\"param_entity\">";
    foreach ($writable_entity as $entity) {
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

    $block = "<div class=\"agendaBar\">
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
//   - $user_q :
///////////////////////////////////////////////////////////////////////////////
function html_dis_right_admin($entity_right,$entity_id) {
  global $l_authorize_list,$l_read_permission, $l_write_permission;
  global $l_list_right,$l_authorize,$l_deny,$set_theme, $ico_delete;
  global $l_public;
  global $ico_add_user,$popup_height,$popup_width,$l_users,$path;
  global $cagenda_entities;

  foreach ($entity_right as $h_entity) {
    $id = $h_entity["id"];
    $customer_id = $h_entity["customer_id"];
    $label = $h_entity["label"];
    $write = $h_entity["write"];
    $read = $h_entity["read"];

    if ($customer_id == 0) {
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

   $block_entity_read_public = "
    <td class=\"detailLabel\">
    $l_public
    </td>
    <td class=\"detailForm\" id=\"cb_read_public\">
      <input onchange=\"show_hide_read_right(this)\" onclick=\"show_hide_read_right(this)\" name=\"cb_read_public\" value=\"1\" type=\"checkbox\" $public_read_checked />
    </td>
   ";
    $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_read";
    $block_entity_read = "
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
     </td>
     ";

   $block_entity_write_public = "
    <td class=\"detailLabel\">
    $l_public
    </td>
    <td class=\"detailForm\" id=\"cb_write_public\">
      <input onchange=\"show_hide_write_right(this)\" onclick=\"show_hide_write_right(this)\" name=\"cb_write_public\" value=\"1\" type=\"checkbox\" $public_write_checked />
    </td>
   ";
    $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_write";
    $block_entity_write = "
    <td class=\"detailLabel\" id=\"head_accept_write\">
    $l_users
    <a style=\"$style_write\" id=\"lnk_accept_write\" href=\"javascript: return false;\"
      onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes');
      return false;\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add_user\" alt=\"[Add]\" />
      </a>
     </td>
     <td class=\"detailForm\" id=\"sel_accept_write\">
       $dis_write_ok
     </td>
     ";

  $block = "
  <form method=\"get\" name=\"f_mod_calendar\"
   action=\"".url_prepare($_SERVER["PHP_SELF"])."\">

   <div class=\"detailHead\">$l_read_permission</div>
  <table class=\"detail\">
   <tr>
   $block_entity_read_public
   </tr>
   <tr>
   $block_entity_read
   </tr>
  </table>

   <div class=\"detailHead\">$l_write_permission</div>
  <table class=\"detail\">
   <tr>
   $block_entity_write_public
   </tr>
   <tr>
   $block_entity_write
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
///////////////////////////////////////////////////////////////////////////////
//////////////////////////           QUERY               //////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Get entity with rights set with their permissions
///////////////////////////////////////////////////////////////////////////////
function run_query_entity_label_writable($entity) {
  global $cagenda_entities; 

  if ($entity == $cagenda_entities["user"]) {
    return run_query_userobm_label_writable(); 
  } elseif ($entity == $cagenda_entities["resource"]) {
    return run_query_resource_label_writable();
  }
}

///////////////////////////////////////////////////////////////////////////////
// Get the list of writable agenda users for the current user
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_label_writable() {
  global $cdg_sql,$auth,$cagenda_entities;

  $uid = $auth->auth["uid"];
  $query = "SELECT u.userobm_id,u.userobm_lastname,u.userobm_firstname,
      c.calendarentityright_write
    FROM UserObm as u
      LEFT OUTER JOIN CalendarEntityRight as c
        ON
          u.userobm_id = c.calendarentityright_entity_id AND
          (c.calendarentityright_customer_id = '$uid' OR c.calendarentityright_customer_id = '0') AND
          c.calendarentityright_entity = '".$cagenda_entities['user']."'
    WHERE (c.calendarentityright_write = 1 OR u.userobm_id='$uid')
    ORDER BY u.userobm_lastname";

  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

  $ret_array = array();
  while($obm_db->next_record()) {
       $ret_array[] = array(
         "id"    => $obm_db->f("userobm_id"),
         "label" => $obm_db->f("userobm_lastname")." ".$obm_db->f("userobm_firstname"),
       );
  }
  return $ret_array;

}

///////////////////////////////////////////////////////////////////////////////
// Get the list of writable agenda users for the current user
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_label_writable() {
  global $cdg_sql,$auth,$cagenda_entities;

  $uid = $auth->auth["uid"];
  $query = "SELECT
      r.resource_id,
      r.resource_label,
      c.calendarentityright_write
    FROM Resource as r
      LEFT OUTER JOIN CalendarEntityRight as c
        ON
          r.resource_id = c.calendarentityright_entity_id AND
          (c.calendarentityright_customer_id = '$uid' OR c.calendarentityright_customer_id = '0') AND
          c.calendarentityright_entity = '".$cagenda_entities['resource']."'
    WHERE (c.calendarentityright_write = 1)
    ORDER BY r.resource_label";

  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

  $ret_array = array();
  while($obm_db->next_record()) {
       $ret_array[] = array(
         "id"    => $obm_db->f("resource_id"),
         "label" => $obm_db->f("resource_label"),
       );
  }
  return $ret_array;

}

///////////////////////////////////////////////////////////////////////////////
// Get entity with rights set with their permissions
///////////////////////////////////////////////////////////////////////////////
function run_query_entity_right($id, $entity) {
  global $cagenda_entities; 

  if ($entity == $cagenda_entities["user"]) {
    return run_query_userobm_right($id); 
  } elseif ($entity == $cagenda_entities["resource"]) {
    return run_query_resource_right($id);
  }
}

///////////////////////////////////////////////////////////////////////////////
// Get active Users or archived with rights set with their permissions
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_right($uid) {
  global $cdg_sql, $cagenda_entities;

  $query = "
    SELECT
      userobm_id,
      calendarentityright_customer_id,
      u.userobm_lastname,
      u.userobm_firstname,
      c.calendarentityright_write,
      c.calendarentityright_read
    FROM CalendarEntityRight as c
    LEFT JOIN UserObm as u
      ON
        c.calendarentityright_customer_id = userobm_id
    WHERE
      (userobm_id IS NULL OR userobm_id != '$uid') AND
      (userobm_archive IS NULL OR userobm_archive != '1') AND
      c.calendarentityright_entity_id = '$uid' AND
      c.calendarentityright_entity = '".$cagenda_entities['user']."' AND
      (c.calendarentityright_write != '0' OR  c.calendarentityright_read != '0')
    ORDER BY u.userobm_lastname";

  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

  $ret_array = array();
  while($obm_db->next_record()) {
       $ret_array[] = array(
         "id" => $obm_db->f("userobm_id"),
         "customer_id" => $obm_db->f("calendarentityright_customer_id"),
         "label" => $obm_db->f("userobm_lastname")." ".$obm_db->f("userobm_firstname"),
         "write" => $obm_db->f("calendarentityright_write"),
         "read" => $obm_db->f("calendarentityright_read"),
       );
  }

  return $ret_array;
}

///////////////////////////////////////////////////////////////////////////////
// Get active Users or archived with rights set with their permissions
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_right($rid) {
  global $cdg_sql, $cagenda_entities;

  $query = "
    SELECT
      userobm_id,
      calendarentityright_customer_id,
      u.userobm_lastname,
      u.userobm_firstname,
      c.calendarentityright_write,
      c.calendarentityright_read
    FROM CalendarEntityRight as c
    LEFT JOIN UserObm as u
    ON
      c.calendarentityright_customer_id = userobm_id
    WHERE
      (userobm_archive IS NULL OR userobm_archive != '1') AND
      c.calendarentityright_entity_id = '$rid' AND
      c.calendarentityright_entity = '".$cagenda_entities['resource']."' AND
      (c.calendarentityright_write != '0' OR  c.calendarentityright_read != '0')
    ORDER BY u.userobm_lastname";
  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

  $ret_array = array();
  while($obm_db->next_record()) {
       $ret_array[] = array(
         "id" => $obm_db->f("userobm_id"),
         "customer_id" => $obm_db->f("calendarentityright_customer_id"),
         "label" => $obm_db->f("userobm_lastname")." ".$obm_db->f("userobm_firstname"),
         "write" => $obm_db->f("calendarentityright_write"),
         "read" => $obm_db->f("calendarentityright_read"),
       );
  }

  return $ret_array;
}

///////////////////////////////////////////////////////////////////////////////
// Get entity with rights set with their permissions
///////////////////////////////////////////////////////////////////////////////
function run_query_entity_writable($entity) {
  global $cagenda_entities;

  if ($entity == $cagenda_entities["user"]) {
    return run_query_userobm_writable();
  } elseif ($entity == $cagenda_entities["resource"]) {
    return run_query_resource_writable();
  }
}

///////////////////////////////////////////////////////////////////////////////
// Get the list of writable agenda users for the current user
///////////////////////////////////////////////////////////////////////////////
function run_query_userobm_writable() {
  global $cdg_sql,$auth,$cagenda_entities;

  $uid = $auth->auth["uid"];
  $query = "SELECT u.userobm_id, c.calendarentityright_write
    FROM UserObm as u
      LEFT OUTER JOIN CalendarEntityRight as c
        ON
          u.userobm_id = c.calendarentityright_entity_id AND
          (c.calendarentityright_customer_id = '$uid' OR c.calendarentityright_customer_id = '0') AND
          c.calendarentityright_entity = '".$cagenda_entities['user']."'
    WHERE (c.calendarentityright_write = 1 OR u.userobm_id='$uid')
    ORDER BY u.userobm_lastname";

  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

  $ret_array = array();
  while($obm_db->next_record()) {
      $ret_array[] = $obm_db->f("userobm_id");
  }
  return $ret_array;

}

///////////////////////////////////////////////////////////////////////////////
// Get the list of writable agenda users for the current user
///////////////////////////////////////////////////////////////////////////////
function run_query_resource_writable() {
  global $cdg_sql,$auth,$cagenda_entities;

  $uid = $auth->auth["uid"];
  $query = "SELECT
      r.resource_id,
      r.resource_label,
      c.calendarentityright_write
    FROM Resource as r
      LEFT OUTER JOIN CalendarEntityRight as c
        ON
          r.resource_id = c.calendarentityright_entity_id AND
          (c.calendarentityright_customer_id = '$uid' OR c.calendarentityright_customer_id = '0') AND
          c.calendarentityright_entity = '".$cagenda_entities['resource']."'
    WHERE (c.calendarentityright_write = 1)
    ORDER BY r.resource_label";

  $obm_db = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

  $ret_array = array();
  while($obm_db->next_record()) {
       $ret_array[] = $obm_db->f("resource_id");
  }
  return $ret_array;

}

/////////////////////////////////////////////////////////////////////////////
// Update the list of the rights give to users.
//////////////////////////////////////////////////////////////////////////////
function run_query_update_right($param, $entity) {
  global $cdg_sql,$auth,$cagenda_entities;

  $eid = $param["entity_id"];
  $accept_write = $param["accept_w"];
  $accept_read = $param["accept_r"];
  $public_write = $param["public_w"];
  $public_read = $param["public_r"];

  if (($eid != $auth->auth["uid"]) && ($entity == $cagenda_entities["user"])) {
    $writable_entity = run_query_entity_writable($entity);
    if(!in_array($eid,$writable_entity))
      return 0;
  }

  $obm_db = new DB_OBM;
  $query = "DELETE From CalendarEntityRight
            where
              calendarentityright_entity_id = '$eid' AND
              calendarentityright_entity = '$entity'";
  display_debug_msg($query, $cdg_sql);
  $obm_db->query($query);

  if ($public_write) {
    $query = "INSERT INTO CalendarEntityRight VALUES ( '$eid','$entity','0',1,0)";
    display_debug_msg($query, $cdg_sql);
    $obm_db->query($query);
    $accept_write = "";
  }
  if (is_array($accept_write)) {
    foreach ($accept_write as $key => $id) {
      $query = "INSERT INTO CalendarEntityRight VALUES('$eid','$entity','$id',1,0)";
      display_debug_msg($query, $cdg_sql);
      $obm_db->query($query);
    }
  }
  if ($public_read) {
    $query = "UPDATE CalendarEntityRight SET calendarentityright_read = 1
      WHERE calendarentityright_entity_id = '$eid'
        AND calendarentityright_customer_id = '0'
        AND calendarentityright_entity = '$entity'";
    display_debug_msg($query, $cdg_sql);
    $obm_db->query($query);
    if ($obm_db->affected_rows() == 0) {
      $query = "INSERT INTO CalendarEntityRight VALUES('$eid','$entity','0',0,1)";
      display_debug_msg($query, $cdg_sql);
      $obm_db->query($query);
    }
  }
  if (is_array($accept_read)) {
    foreach($accept_read as $key => $id) {
      $query = "UPDATE CalendarEntityRight SET calendarentityright_read = 1
        WHERE calendarentityright_entity_id = '$eid'
          AND calendarentityright_customer_id = '$id'
          AND calendarentityright_entity = '$entity'";
      display_debug_msg($query, $cdg_sql);
      $obm_db->query($query);
      if ($obm_db->affected_rows() == 0) {
        $query = "INSERT INTO CalendarEntityRight VALUES('$eid','$entity','$id',0,1)";
        display_debug_msg($query, $cdg_sql);
        $obm_db->query($query);
      }
    }
  }

}

?>

