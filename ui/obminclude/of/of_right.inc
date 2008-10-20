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
function of_right_dis_admin($entity, $entity_id, $real_entity='') {
  global $display, $obm, $l_of_right_err_empty;

  if (!isset($entity_id) || ($entity_id == '')) {
    $display['msg'] .= display_err_msg($l_of_right_err_empty);
    return;
  }

  $admin_entity = of_right_entity_for_user($entity, $obm['uid'], 'admin','', $real_entity);
  $display['features'] = html_entityadmin_bar($admin_entity ,$entity_id, $entity);

  $consumers = of_right_consumer_for_entity($entity, $entity_id);

  return html_dis_right_admin($consumers, $entity_id, $entity);
}


///////////////////////////////////////////////////////////////////////////////
// User selection for management admin panel
// Parameters:
//   - $writable_user : Array of user on who you can manage the calandar
///////////////////////////////////////////////////////////////////////////////
function html_entityadmin_bar($writable_entity,$entity_id, $entity) {
  global $l_entity_right, $l_validate, $path, $module;

  if ($module != '') {
    $uri = $path.'/'.$module.'/'.$module.'_index.php';
  }
  // Entity Selection
  if (count($writable_entity['ids']) > 0) {
    $dis_sel .= "<select name=\"entity_id\">";
    foreach ($writable_entity['entity'] as $entity) {
      $id = $entity['id'];
      $label =  $entity['label'];
      $dis_sel .= "\n<option value=\"$id\"";
      if ($id == $entity_id ) {
        $tag = " selected=\"selected\"";
      }
      $dis_sel .= " $tag>$label</option>";
      $tag = '';
    }
    $dis_sel .= "</select>
    <br />
        $dis_duration
        ";

    $block = "
    <div class=\"portlet\" id=\"rightManagerPortlet\">
      <h1>$l_entity_right</h1>
      <div>
      <form action=\"$uri\" method=\"get\" $check_sel_user>
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
function html_dis_right_admin(&$consumers, $entity_id, $entity) {
  global $l_authorize_list,$l_read_permission, $l_write_permission, $l_acces_permission;
  global $l_right_validate ,$l_authorize,$l_deny, $ico_delete;
  global $l_public,$l_admin_permission, $module;
  global $path, $ico_add, $l_users, $l_groups;
  global $cgp_default_right;

  $l_userSearch = phpStringToJsString("$l_users...");
  foreach ($consumers['consumer'] as $one_consumer) {
    $id = $one_consumer['id'];
    $consumer = $one_consumer['entity'];
    $label = $one_consumer['label'];
    $write = $one_consumer['write'];
    $read = $one_consumer['read'];
    $admin = $one_consumer['admin'];
    $acces = $one_consumer['acces'];
    if ($id == 0) {
      if ($read == 1) {
        $style_read = 'visibility:hidden;';
        $public_read_checked = "checked=\"checked\"";
      }
      if ($write == 1) {
        $style_write = 'visibility:hidden;';
        $public_write_checked = "checked=\"checked\"";
      }
      if ($acces == 1) {
        $style_acces = 'visibility:hidden;';
        $public_acces_checked = "checked=\"checked\"";
      }
    } else {
      if ($write == 1) {
	$sel_id = "data-$consumer-$id";
	$div_id = "sel_accept_write-data-$consumer-$id";
        ${'dis_'.$consumer.'_write_ok'} .= "<div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_accept_".$consumer."_write');\"><img src=\"$ico_delete\"></a>
        $label
       <input value=\"$sel_id\" name=\"sel_accept_write[]\" type=\"hidden\" />
       </div>";
      }
      if ($read == 1) {
	$sel_id = "data-$consumer-$id";
	$div_id = "sel_accept_read-data-$consumer-$id";
        ${'dis_'.$consumer.'_read_ok'} .= "<div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_accept_".$consumer."_read');\"><img src=\"$ico_delete\"></a>
        $label
       <input value=\"$sel_id\" name=\"sel_accept_read[]\" type=\"hidden\" />
       </div>";
      }
      if ($admin == 1) {
	$sel_id = "data-$consumer-$id";
        $div_id = "sel_accept_admin-data-$consumer-$id";
        ${'dis_'.$consumer.'_admin_ok'} .= "<div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_accept_".$consumer."_admin');\"><img src=\"$ico_delete\"></a>
        $label
       <input value=\"$sel_id\" name=\"sel_accept_admin[]\" type=\"hidden\" />
       </div>";
      }      
      if ($acces == 1) {
	$sel_id = "data-$consumer-$id";
        $div_id = "sel_accept_acces-data-$consumer-$id";
        ${'dis_'.$consumer.'_acces_ok'} .= "<div class=\"elementRow\" id=\"$div_id\">
        <a href=\"javascript: remove_element('$div_id','sel_accept_".$consumer."_acces');\"><img src=\"$ico_delete\"></a>
        $label
       <input value=\"$sel_id\" name=\"sel_accept_acces[]\" type=\"hidden\" />
       </div>";
      }
    }
  }
  if($cgp_default_right[$entity]['public']['read'] != -1 || ($read == 1 && $id == 0)){ 
    $block_consumer_read_public = "
      <td class=\"detailLabel\">
      $l_public
      </td>
      <td class=\"detailForm\" id=\"cb_read_public\">
      <input onchange=\"togglePrivacy(this,'readRightBox');\" onclick=\"togglePrivacy(this,'readRightBox')\"
      name=\"cb_read_public\" value=\"1\" type=\"checkbox\" $public_read_checked />
      </td>";
  }

  $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_read";
    $popup_user_add_button = "
    <a id=\"lnk_accept_read\" href=\"#\" onclick=\"return popup('$url');
      return false;\">
      <img src=\"$ico_add\" alt=\"[Add]\" />
      </a>";
    if($module != 'mailbox' && $module != 'mailshare') {
      $url = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_read";
      $popup_group_add_button = "
        <a id=\"lnk_accept_admin\" href=\"#\" onclick=\"return popup('$url');\">
         <img src=\"$ico_add\" alt=\"[Add]\" />
         </a>";
      $block_group_read = "
        <tr>
        <td class=\"detailLabel\">
        $l_groups
        $popup_group_add_button
        </td>
        <td>
        <input type=\"text\" id=\"groupReadSearch\" autocomplete=\"off\" /> 
        </td>
        </tr>";
      $block_group_buttons_read = "
      <fieldset id='sel_accept_group_read'>
      <legend>$l_groups</legend>
      $dis_group_read_ok
      </fieldset>";        
    }
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
    $block_group_read
    <tr>
    <td colspan=\"2\" class=\"detailForm\" id=\"sel_accept_read\">
      <fieldset id='sel_accept_user_read'>
      <legend>$l_users</legend>
      $dis_user_read_ok
      </fieldset>
      $block_group_buttons_read
    </td>
    </tr>";

  if($cgp_default_right[$entity]['public']['acces'] != -1 || ($acces == 1 && $id == 0)){ 
     $block_consumer_acces_public = "
      <td class=\"detailLabel\">
      $l_public
      </td>
      <td class=\"detailForm\" id=\"cb_acces_public\">
      <input onchange=\"togglePrivacy(this,'accesRightBox');\" onclick=\"togglePrivacy(this,'accesRightBox')\"
      name=\"cb_acces_public\" value=\"1\" type=\"checkbox\" $public_acces_checked />
      </td>";
  }
    $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_acces";
    $popup_user_add_button = "
    <a id=\"lnk_accept_acces\" href=\"#\" onclick=\"return popup('$url');
      return false;\">
      <img src=\"$ico_add\" alt=\"[Add]\" />
      </a>";
    if($module != 'mailbox' && $module != 'mailshare') {
      $url = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_acces";
      $popup_group_add_button = "
        <a id=\"lnk_accept_admin\" href=\"#\" onclick=\"return popup('$url');\">
         <img src=\"$ico_add\" alt=\"[Add]\" />
         </a>";
      $block_group_acces = "
        <tr>
        <td class=\"detailLabel\">
        $l_groups
        $popup_group_add_button
        </td>
        <td>
        <input type=\"text\" id=\"groupAccesSearch\" autocomplete=\"off\" /> 
        </td>
        </tr>";
      $block_group_buttons_acces = "
      <fieldset id='sel_accept_group_acces'>
      <legend>$l_groups</legend>
      $dis_group_acces_ok
      </fieldset>";        
    }
    $block_consumer_acces = "
    <tr>
    <td class=\"detailLabel\">
    $l_users
    $popup_user_add_button
    </td>
    <td>
    <input type=\"text\" id=\"userAccesSearch\" autocomplete=\"off\" /> 
    </td>
    </tr>
    $block_group_acces
    <tr>
    <td colspan=\"2\" class=\"detailForm\" id=\"sel_accept_acces\">
      <fieldset id='sel_accept_user_acces'>
      <legend>$l_users</legend>
      $dis_user_acces_ok
      </fieldset>
      $block_group_buttons_acces
    </td>
    </tr>";


  if($cgp_default_right[$entity]['public']['write'] != -1 || ($write == 1 && $id == 0)){ 
     $block_consumer_write_public = "
      <td class=\"detailLabel\">
      $l_public
      </td>
      <td class=\"detailForm\" id=\"cb_write_public\">
      <input onchange=\"togglePrivacy(this,'writeRightBox');\" onclick=\"togglePrivacy(this,'writeRightBox')\"
      name=\"cb_write_public\" value=\"1\" type=\"checkbox\" $public_write_checked style=\"$public_write_checked_visibility\" />
      </td>";
  }
    $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_write";
    $popup_user_add_button = "
      <a id=\"lnk_accept_write\" href=\"#\" onclick=\"return popup('$url');\">
       <img src=\"$ico_add\" alt=\"[Add]\" />
     </a>";
    if($module != 'mailbox' && $module != 'mailshare') {
      $url = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_write";
      $popup_group_add_button = "
        <a id=\"lnk_accept_admin\" href=\"#\" onclick=\"return popup('$url');\">
         <img src=\"$ico_add\" alt=\"[Add]\" />
        </a>";
      $block_group_write = "
      <tr>
      <td class=\"detailLabel\">
      $l_groups
      $popup_group_add_button
      </td>
      <td>
      <input type=\"text\" id=\"groupWriteSearch\" autocomplete=\"off\" /> 
      </td>
      </tr>";
      $block_group_buttons_write = "      
      <fieldset id='sel_accept_group_write'>
      <legend>$l_groups</legend>
      $dis_group_write_ok
      </fieldset>";
    }
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
    $block_group_write
    <tr>
    <td colspan=\"2\" class=\"detailForm\" id=\"sel_accept_write\">
      <fieldset id='sel_accept_user_write'>
      <legend>$l_users</legend>
      $dis_user_write_ok
      </fieldset>
      $block_group_buttons_write
    </td>
    </tr>";

    $url = "$path/user/user_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_admin";
    $popup_user_add_button = "
      <a id=\"lnk_accept_admin\" href=\"#\" onclick=\"return popup('$url');\">
       <img src=\"$ico_add\" alt=\"[Add]\" />
      </a>";
    if($module != 'mailbox' && $module != 'mailshare') {
      $url = "$path/group/group_index.php?action=ext_get_ids&amp;popup=1&amp;ext_element=sel_accept_admin";
      $popup_group_add_button = "
        <a id=\"lnk_accept_admin\" href=\"#\" onclick=\"return popup('$url');\">
         <img src=\"$ico_add\" alt=\"[Add]\" />
         </a>";
      $block_group_admin = "    
        <tr>
        <td class=\"detailLabel\">
        $l_groups
        $popup_group_add_button
        </td>
        <td>
        <input type=\"text\" id=\"groupAdminSearch\" autocomplete=\"off\" /> 
        </td>
        </tr>";
      $block_group_buttons_admin =  "      
      <fieldset id='sel_accept_group_admin'>
      <legend>$l_groups</legend>
      $dis_group_admin_ok
      </fieldset>  ";
    }
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
    $block_group_admin
    <tr>
    <tr>
    <td colspan=\"2\" class=\"detailForm\" id=\"sel_accept_admin\">
      <fieldset id='sel_accept_user_admin'>
      <legend>$l_users</legend>
      $dis_user_admin_ok
      </fieldset>
      $block_group_buttons_admin
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
    action=\"$path/$module/${module}_index.php\">

  <fieldset class=\"detail infos\">
  <legend>$l_read_permission</legend>
  <table>
    $block_consumer_read_public
  <tbody style=\"$style_read\" id=\"readRightBox\">
    $block_consumer_read
  </tbody>
  </table>
  </fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_write_permission</legend>
  <table>
  <tr style=\"$public_write_checked_visibility\" >
    $block_consumer_write_public
  </tr>
  <tbody style=\"$style_write\" id=\"writeRightBox\">
    $block_consumer_write
  </tbody>
  </table>
  </fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_acces_permission</legend>
  <table>
  <tr style=\"$public_acces_checked_visibility\" >
    $block_consumer_acces_public
  </tr>
  <tbody style=\"$style_acces\" id=\"accesRightBox\">
    $block_consumer_acces
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
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search&ajax=1', 'sel_accept_user_acces', 'userAccesSearch', {name:'sel_accept_acces'});
      new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search&ajax=1', 'sel_accept_group_admin', 'groupAdminSearch', {name:'sel_accept_admin'});
      new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search&ajax=1', 'sel_accept_group_write', 'groupWriteSearch', {name:'sel_accept_write'});
      new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search&ajax=1', 'sel_accept_group_read', 'groupReadSearch', {name:'sel_accept_read'});
      new obm.AutoComplete.Search('$path/group/group_index.php?action=ext_search&ajax=1', 'sel_accept_group_acces', 'groupAccesSearch', {name:'sel_accept_acces'});
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
function of_right_entity_for_user($entityu, $user_id, $right='read', $ids_set='', $real_entity='') {
  global $cdg_sql, $obm;

  // GLOBAL - superman has all rights on entities
  global $cright_write_admin;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $ent_array = array();
  $id_array = array();

  $entity = strtolower($entityu);
  if ($real_entity != '') {
    $r_entity = $real_entity;
  } else {
    $r_entity = $entity;
  }

  if ($r_entity == 'userobm') {
    $table_entity = 'UserObm';
    $e_id = 'e.userobm_id';
    $ctt[0]['type'] = 'field';
    $ctt[0]['value'] = 'userobm_lastname';
    $ctt[1]['type'] = 'string';
    $ctt[1]['value'] = ' ';
    $ctt[2]['type'] = 'field';
    $ctt[2]['value'] = 'userobm_firstname';
    $e_label = sql_string_concat($db_type, $ctt);
    $u = get_user_info($user_id);
    $u_label = addslashes("$u[lastname] $u[firstname]");    
    $union = "UNION SELECT '$u[id]' as id, '$u_label' as label, '$entity' as entityright_entity" ;
  } else {
    $table_entity = "$entityu";
    $e_id = "e.${entity}_id";
    $e_label = "e.${entity}_name";
  }
  
  // BEGIN - superman has all rights on entities
  
  // IF asked right check AND admin && current user is admin
  if ($right == 'admin' && (Perm::get_module_rights($entityu) & $cright_write_admin) == $cright_write_admin ) {
    $multidomain = sql_multidomain($r_entity);
    $union_admin = "UNION SELECT $e_id AS id, $e_label AS label,
      '$entity' as entityright_entity
      FROM $table_entity AS e
      WHERE 1=1 $multidomain";
  }
  
  // END - superman has all rights on entities
    
  if ($right != 'all') {
    $where_right = "AND entityright_$right = 1";
  }
  $user_id = sql_parse_id($user_id, true);
  $query = "SELECT
      $e_id as id,
      $e_label as label,
      '$entity' as entityright_entity
    FROM EntityRight
      LEFT JOIN $table_entity as e ON entityright_entity_id = $e_id
    WHERE entityright_entity = '$entity'
      AND ( (entityright_consumer = 'user' AND (entityright_consumer_id $user_id OR entityright_consumer_id = 0))
        OR (entityright_consumer = 'group'  AND (entityright_consumer_id IN (SELECT of_usergroup_group_id FROM of_usergroup WHERE of_usergroup_user_id $user_id )
            OR entityright_consumer_id = 0)))
        $where_right
    GROUP BY id, label, entityright_entity
    $union
    $union_admin
    ORDER BY entityright_entity, label";

  display_debug_msg($query, $cdg_sql, 'of_right_entity_for_user()');
  $obm_q->query($query);

  while($obm_q->next_record()) {
    $id = $obm_q->f('id');
    if ( ($ids_set == '')
	 || ( (is_array($ids_set)) && (in_array($id, $ids_set)) ) ) {
      $label = $obm_q->f('label');
      $id_array[] = $id;
      $ent_array[] = array(
			   'id'    => $id,
			   'label' => $label
			   );
    }
  }
  $ret_array = array();
  $ret_array['ids'] = array();
  if (is_array($id_array)) {
    $ret_array['ids'] = $id_array;
  }
  $ret_array['entity'] = array();
  if (is_array($ent_array)) {
    $ret_array['entity'] = $ent_array;
  }
  
  return $ret_array;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list of consumers for the entity given with the corresponding right
// Parameters:
//   - $entity      : target entity
//   - $entity_id   : entity id
//   - $consumer    : consumer type
//   - $right       : right to get and select from : 'all', 'read', 'write' or
//                    'admin'
// Returns:
//   array ('ids'=>(id1, id2, id3), 'entity'=>((id1,label1), (id2,label2))
///////////////////////////////////////////////////////////////////////////////
function of_right_consumer_for_entity($entity, $entity_id, $consumer='all', $right='all') {
  global $cdg_sql;
  
  // GLOBAL - superman has all rights on entities
  global $cright_write_admin, $obm;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  
  // BEGIN - superman has all rights on entities
  
  if (($consumer == 'user' || $consumer == 'all')
    &&  (Perm::get_module_rights($entity) & $cright_write_admin) == $cright_write_admin) {
    
    $multidomain = sql_multidomain('userobm');
    
    $ctt = array();
    $ctt[0]['type'] = 'field';
    $ctt[0]['value'] = 'userobm_lastname';
    $ctt[1]['type'] = 'string';
    $ctt[1]['value'] = ' ';
    $ctt[2]['type'] = 'field';
    $ctt[2]['value'] = 'userobm_firstname';
    $user_label = sql_string_concat($db_type, $ctt);
    
    $union_admin = "UNION SELECT
        userobm_id as id,
        $user_label AS label,
        'user' AS entityright_consumer,
        userobm_id AS entityright_consumer_id,
        0 AS entityright_read,
        0 AS entityright_write,
        1 AS entityright_admin
      FROM UserObm
      WHERE
        userobm_id = $obm[uid]
        $multidomain
      ";
  }
  
  // END - superman has all rights on entities
  
  if(count($entity_id) == 0) {
    return array('ids' => array(), 'entity' => array());
  }
  if(is_array($entity_id)) {
    $entity_id = implode(',',$entity_id);
  }

  $id_array = array();
  $con_array = array();

  if ($consumer == 'user' || $consumer == 'all' ) {
    $ctt = array();
    $ctt[0]['type'] = 'field';
    $ctt[0]['value'] = 'userobm_lastname';
    $ctt[1]['type'] = 'string';
    $ctt[1]['value'] = ' ';
    $ctt[2]['type'] = 'field';
    $ctt[2]['value'] = 'userobm_firstname';
    $user_label = sql_string_concat($db_type, $ctt);
  }

  if ($right != 'all') {
    $where_right = "AND entityright_$right = 1";
  }

  $if_id = sql_if($db_type, "(entityright_consumer = 'user')", "userobm_id", "group_id");
  $if_label = sql_if($db_type, "(entityright_consumer = 'user')", "$user_label", "group_name");

  $query = "SELECT
      $if_id as id,
      $if_label as label,
      entityright_consumer,
      entityright_consumer_id,
      entityright_read,
      entityright_write,
      entityright_admin
    FROM EntityRight
      LEFT JOIN UserObm ON entityright_consumer = 'user' AND entityright_consumer_id = userobm_id
      LEFT JOIN UGroup ON entityright_consumer = 'group' AND entityright_consumer_id = group_id
    WHERE entityright_entity_id IN ($entity_id) AND entityright_entity = '$entity'
      $where_right
    $union_admin
    ORDER BY entityright_consumer, label";

  display_debug_msg($query, $cdg_sql, 'of_right_consumer_for_entity()');
  $obm_q->query($query);
  $ret_array = array();
  $id_array = array();
  while($obm_q->next_record()) {
    $id = $obm_q->f('id');
    $label = $obm_q->f('label');
    $entity_id = $obm_q->f('entityright_entity_id');
    $entity = $obm_q->f('entityright_consumer');
    $read = $obm_q->f('entityright_read');
    $write = $obm_q->f('entityright_write');
    $admin = $obm_q->f('entityright_admin');
    
    if (!in_array($id, $id_array)) {
      $id_array[] = $id;
      $con_array[] = array(
			   'id'    => $id,
			   'label' => $label,
         'entity_id' => $entity_id,
         'entity' => $entity,
         'read' => $read,
         'write' => $write,
         'admin' => $admin
       );
    } else {
    	// superman bonus
    	$con_array[array_search($id, $id_array)]['read'] |= $read;
      $con_array[array_search($id, $id_array)]['write'] |= $write;
    }
  }

  $ret_array['ids'] = array();
  if (is_array($id_array)) {
    $ret_array['ids'] = $id_array;
  }
  $ret_array['consumer'] = array();
  if (is_array($con_array)) {
    $ret_array['consumer'] = $con_array;
  }
  
  return $ret_array;
}


///////////////////////////////////////////////////////////////////////////////
// Read Default Rights given by conf.
// Parameters:
//   - $param  : hash entry with entity and right values
//   - $entity : entity type to update
//   - $real_entity : [optionnal] if set, real data entity (eg mailbox->user)
///////////////////////////////////////////////////////////////////////////////
function of_right_default($params, $entity, $real_entity='',$check_perms=true) {
  global $cgp_default_right;
  $params['write_public'] = ($cgp_default_right[$entity]['public']['write'] == -1)? 0:$cgp_default_right[$entity]['public']['write'];
  $params['read_public'] = ($cgp_default_right[$entity]['public']['read'] == -1)? 0:$cgp_default_right[$entity]['public']['read'];;
  $params['acces_public'] = ($cgp_default_right[$entity]['public']['acces'] == -1)? 0:$cgp_default_right[$entity]['public']['acces'];
  of_right_update_right($params, $entity, $real_entity,$check_perms);
}

///////////////////////////////////////////////////////////////////////////////
// Update Rights given to users.
// Parameters:
//   - $param  : hash entry with entity and right values
//   - $entity : entity type to update
//   - $real_entity : [optionnal] if set, real data entity (eg mailbox->user)
///////////////////////////////////////////////////////////////////////////////
function of_right_update_right($param, $entity, $real_entity='',$check_perms=true) {
  global $cdg_sql, $obm, $err, $l_of_right_err_auth,$obm;

  $consumer_ereg = "/^data-(user|group)-([0-9]+)$/";

  $eid = $param['entity_id'];
  $accept_write = $param['accept_write'];
  $accept_read = $param['accept_read'];
  $accept_acces = $param['accept_acces'];
  $accept_admin = $param['accept_admin'];
  $public_write = $param['write_public'];
  $public_read = $param['read_public'];
  $public_acces = $param['acces_public'];

  if (! is_array($accept_read)) {
    $accept_read = array();
  }
  if (! is_array($accept_write)) {
    $accept_write = array();
  }
  if (! is_array($accept_acces)) {
    $accept_acces = array();
  }
  if (! is_array($accept_admin)) {
    $accept_admin = array();
  }

  $accept_admin = array_unique($accept_admin);
  $accept_write = array_unique($accept_write);
  $accept_read = array_unique($accept_read);
  $accept_acces = array_unique($accept_acces);

  // Rights on external objects : defined by module access
  // Rights on userobm entity : user need write right on the entity
  // If user manipulating NOT its own data, check if he has rights
  if (($eid != $obm['uid'] || $real_entity != 'userobm') && $check_perms) {
    $admin_entity = of_right_entity_for_user($entity, $obm['uid'], 'admin','', $real_entity);
    if (!in_array($eid, $admin_entity['ids'])) {
      $err['msg'] = $l_of_right_err_auth;
      return false;
    }
  }
	
  $obm_q = new DB_OBM;
  $query = "DELETE
    FROM EntityRight
    WHERE entityright_entity_id ".sql_parse_id($eid, true)."
      AND entityright_entity = '$entity'";
  display_debug_msg($query, $cdg_sql, 'of_right_update_right(1)');
  $obm_q->query($query);
  if ($public_read) {
    $accept_read = array();
  }
  if( $public_write) {
    $accept_write = array();
  }
  if( $public_acces) {
    $accept_acces = array();
  }
  if ($public_read || $public_write || $public_acces) {
    $pr = sql_parse_int($public_read);
    $pw = sql_parse_int($public_write);
    $px = ($public_write ? sql_parse_int($public_write) : sql_parse_int($public_acces));
    $query = "INSERT INTO EntityRight (
      entityright_entity,
      entityright_entity_id,
      entityright_consumer,
      entityright_consumer_id,
      entityright_read,
      entityright_write,
      entityright_acces
    ) VALUES (
      '$entity',
      ".sql_parse_id($eid).",
      'user',
      0,
      $pr,
      $pw,
      $px)";
    display_debug_msg($query, $cdg_sql, 'of_right_update_right(2)');
    $obm_q->query($query);
  }

  if (! $public_acces) {
    // Insert all read entries (with write right correctly set)
    foreach ($accept_acces as $key => $ref) {
      if (preg_match($consumer_ereg,$ref,$matches)) {
        $consumer = $matches[1];
        $id = sql_parse_id($matches[2]);        
	$r = (in_array($ref, $accept_read) ? 1 : 0);
	$w = (in_array($ref, $accept_write) ? 1 : 0);
	$a = (in_array($ref, $accept_admin) ? 1 : 0);
	$query = "INSERT INTO EntityRight (
          entityright_entity,
          entityright_entity_id,
          entityright_consumer,
          entityright_consumer_id,
          entityright_acces,
          entityright_read,
          entityright_write,
          entityright_admin
        ) VALUES (
          '$entity',
          ".sql_parse_id($eid).",
          '$consumer',
          $id,
          1,
          $r,
          $w,
          $a)";
	display_debug_msg($query, $cdg_sql, 'of_right_update_right(3)');
	$obm_q->query($query);
      }
    }
  }

  if (! $public_read) {
    // Insert all read entries not already done by the acces loop
    foreach ($accept_read as $key => $ref) {
      if (preg_match($consumer_ereg,$ref,$matches)) {
        if (! in_array($ref, $accept_acces)) {
          $consumer = $matches[1];
          $id = sql_parse_id($matches[2]);          
	  $w = (in_array($ref, $accept_write) ? 1 : 0);
	  $x = (in_array($ref, $accept_write) ? 1 : 0);
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
            ".sql_parse_id($eid).",
            '$consumer',
            $id,
            1,
            $w,
            $a)";
	  display_debug_msg($query, $cdg_sql, 'of_right_update_right(3)');
	  $obm_q->query($query);
        }
      }
    }
  }

  if (! $public_write) {
    // Insert all write entries not already done by the read loop
    foreach ($accept_write as $key => $ref) {
      if (preg_match($consumer_ereg,$ref,$matches)) {
        if (! in_array($ref, $accept_acces)
          && (! in_array($ref, $accept_read))) {
          $consumer = $matches[1];
          $id = sql_parse_id($matches[2]);          
	  $x = 1;
	  $a = (in_array($ref, $accept_admin) ? 1 : 0);
	  $query = "INSERT INTO EntityRight (
            entityright_entity,
            entityright_entity_id,
            entityright_consumer,
            entityright_consumer_id,
            entityright_acces,
            entityright_write,
            entityright_admin
          ) VALUES (
            '$entity',
            ".sql_parse_id($eid).",
            '$consumer',
            $id,
            $x,
            '1',
            $a)";
	  display_debug_msg($query, $cdg_sql, 'of_right_update_right(4)');
	  $obm_q->query($query);
	}
      }
    }
  }

  foreach ($accept_admin as $key => $ref) {
    $ref_ent = substr($ref, 0, 10);
    if (preg_match($consumer_ereg,$ref,$matches)) {
      if ((! in_array($ref, $accept_acces))
        && (! in_array($ref, $accept_read)) 
        && (! in_array($ref, $accept_write))) {
        $consumer = $matches[1];
        $id = sql_parse_id($matches[2]);
        $query = "INSERT INTO EntityRight (
          entityright_entity,
          entityright_entity_id,
          entityright_consumer,
          entityright_consumer_id,
          entityright_admin
        ) VALUES (
          '$entity',
          ".sql_parse_id($eid).",
          '$consumer',
          $id,
          1)";
        display_debug_msg($query, $cdg_sql, 'of_right_update_right(5)');
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
//   - $right       : right to get and select from : 'all', 'read', 'write' or
//                    'admin'
// Returns:
//   array ('ids'=>(id1, id2, id3), 'entity'=>((id1,label1), (id2,label2))
///////////////////////////////////////////////////////////////////////////////
function of_right_users_for_entity($entity, $entity_id, $right='all') {
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  
  // GLOBAL - superman has all rights on entities
  global $cright_write_admin, $obm;
  
  // BEGIN - superman has all rights on entities
  
  if ((Perm::get_module_rights($entity) & $cright_write_admin) == $cright_write_admin) {
    $multidomain = sql_multidomain('userobm');
    
    $union_admin = "UNION SELECT
        userobm_firstname,
        userobm_lastname,
        userobm_id,
        0 AS entityright_read,
        0 AS entityright_write,
        1 AS entityright_admin
      FROM UserObm
      WHERE
        userobm_id = $obm[uid]
        $multidomain
      ";
  }
      
  // END - superman has all rights on entities
  
  if(count($entity_id) == 0) {
    return array('ids' => array(), 'entity' => array());
  }
  if(is_array($entity_id)) {
    $entity_id = implode(',',$entity_id);
  }

  if ($right != 'all') {
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
    WHERE entityright_entity_id IN ($entity_id) AND entityright_entity = '$entity' $where_right
    UNION
    SELECT
      userobm_firstname,
      userobm_lastname,
      userobm_id,
      entityright_read,
      entityright_write,
      entityright_admin
    FROM UserObm
    LEFT JOIN of_usergroup ON userobm_id = of_usergroup_user_id 
    LEFT JOIN EntityRight ON (of_usergroup_group_id = entityright_consumer_id AND entityright_consumer = 'group') 
    WHERE entityright_entity_id IN ($entity_id) AND entityright_entity = '$entity' $where_right
    
    $union_admin
    
    ORDER BY userobm_firstname, userobm_lastname 
";

  display_debug_msg($query, $cdg_sql, 'of_right_users_for_entity()');
  $obm_q->query($query);
  $ret_array = array();
  while($obm_q->next_record()) {
    $id = $obm_q->f('userobm_id');
    $label = $obm_q->f('userobm_lastname').' '.$obm_q->f('userobm_firstname');
    $entity_id = $obm_q->f('entityright_entity_id');
    $read = $obm_q->f('entityright_read');
    $write = $obm_q->f('entityright_write');
    $admin = $obm_q->f('entityright_admin');
    
    if (!in_array($id, $id_array)) {
      $id_array[] = $id;
      $con_array[] = array(
         'id'    => $id,
         'label' => $label,
         'entity_id' => $entity_id,
         'read' => $read,
         'write' => $write,
         'admin' => $admin
        );
    } else {
      // superman bonus
      $con_array[array_search($id, $id_array)]['read'] |= $read;
      $con_array[array_search($id, $id_array)]['write'] |= $write;
    }

  }

  $ret_array['ids'] = array();
  if (is_array($id_array)) {
    $ret_array['ids'] = $id_array;
  }
  $ret_array['consumer'] = array();
  if (is_array($con_array)) {
    $ret_array['consumer'] = $con_array;
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

  function getEntityRoles($uri , $action = 'all') {

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
