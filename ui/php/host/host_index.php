<script language="php">
///////////////////////////////////////////////////////////////////////////////
// ALIAMIN - File : host_index.php                                           //
//         - Desc : Host Index File                                          //
// 2004-09-09 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the host search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new host form
// - detailconsult   -- $param_host    -- show the host detail
// - detailupdate    -- $param_host    -- show the host detail form
// - insert          -- form fields    -- insert the host
// - update          -- form fields    -- update the host
// - check_delete    -- $param_group   -- check the host
// - delete          -- $param_group   -- delete the host
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "host";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");

page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
include("host_display.inc");
include("host_query.inc");

$host = get_param_host();
if ($action == "") $action = "index";
get_host_action();
$perm->check_permissions($module, $action);
update_last_visit("host", $params["host_id"], $action);

page_close();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_id") {
  require("host_js.inc");  
  $display["search"] = html_host_search_form($host);
  if ($set_display == "yes") {
    $display["result"] = dis_host_search_list($host);
  } else {
    $display["msg"] = display_info_msg($l_no_display);
  }

} else if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_host_search_form($host);
  if ($set_display == "yes") {
    $display["result"] = dis_host_search_list("");
  } else {
    $display["msg"] .= display_ok_msg($l_no_display);
  }

} else if ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  require("host_js.inc");
  $display["search"] = html_host_search_form($host);
  $display["result"] = dis_host_search_list($host);

} else if ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_host_form($action, "", $host);

} else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_host_consult($host);

} else if ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_detail($host["id"]);
  $display["detail"] = html_host_form($action, $obm_q, $host);

} else if ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($host)) {
    
    // If the context (same host) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $retour = run_query_insert($host);
      if ($retour) {
	update_update_state();
	$display["msg"] .= display_ok_msg($l_insert_ok);
      } else {
	$display["msg"] .= display_err_msg($l_insert_error);
      }
      $display["search"] = html_host_search_form($host);
      
      // If it is the first try, we warn the user if some hosts seem similar
    } else {
      $obm_q = check_host_context("", $host);
      if ($obm_q->num_rows() > 0) {
	$display["detail"] = dis_host_warn_insert("", $obm_q, $host);
      } else {
	$retour = run_query_insert($host);
	if ($retour) {
	  update_update_state();
	  $display["msg"] .= display_ok_msg($l_insert_ok);
	} else {
	  $display["msg"] .= display_err_msg($l_insert_error);
	}
	$display["search"] = html_host_search_form($host);
      }
    }
    
    // Form data are not valid
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $display["detail"].= html_host_form($action, "", $host, $err_field);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($host)) {
    $retour = run_query_update($host);
    if ($retour) {
      update_update_state();
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    $display["detail"] = dis_host_consult($host);
  } else {
    $display["msg"] .= display_err_msg($err_msg);
    $host_q = run_query_detail($host["id"]);
    $display["detail"] = html_host_form($action, $host_q, $host, $err_field);
  }
} elseif ($action == "showlist")  {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_detail($param_host);
  if ($obm_q->num_rows() == 1) {
    $display["detail"] = html_host_consult_web($obm_q);
  } else {
    $display["msg"] .= display_err_msg($l_query_error . " - " . $query . " !");
  }
} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_warn_delete($host["id"]);

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_delete($host["id"]);
  if ($retour) {
    update_update_state();
    $display["msg"] .= display_ok_msg($l_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_delete_error);
  }
  $display["search"] = html_host_search_form("");

} else if ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($uid, "host", 1);
  $display["detail"] = dis_host_display_pref($prefs);

} else if($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($uid, "host", 1);
  $display["detail"] = dis_host_display_pref($prefs);

} else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($uid, "host", 1);
  $display["detail"] = dis_host_display_pref($prefs);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_host);
$display["end"] = display_end();
// Update actions url in case some values have been updated (id after insert) 
if (! $params["popup"]) {
  $display["header"] = display_menu($module);
}

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $host hash, Host parameters transmited
// returns : $host hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_host() {
  global $action, $param_host, $cdg_param, $popup, $child_res;
  global $new_order, $order_dir, $entity;
  global $tf_name, $tf_ip, $tf_desc, $cb_samba;
  global $HTTP_POST_VARS, $HTTP_GET_VARS;
  global $popup, $ext_title, $ext_target;  
  global $ext_widget, $ext_widget_text;  
  global $cb_web_perms, $cb_ftp_perms, $ta_firewall_perms;
  global $rb_web_all, $ta_web_list;  
  global $param_user;

  if (isset ($popup)) $host["popup"] = $popup;
  if (isset ($ext_title)) $host["ext_title"] = stripslashes(urldecode($ext_title));
  if (isset ($ext_target)) $host["ext_target"] = $ext_target;
  if (isset ($ext_widget)) $host["ext_widget"] = $ext_widget;
  if (isset ($ext_widget_text)) $host["ext_widget_text"] = $ext_widget_text;
  
  // Group fields
  if (isset ($param_host)) $host["id"] = $param_host;
  if (isset ($tf_name)) $host["name"] = trim($tf_name);
  if (isset ($tf_desc)) $host["desc"] = trim($tf_desc);
  if (isset ($tf_ip)) $host["ip"] = $tf_ip;
  if (isset ($cb_samba)) $host["samba"] = $cb_samba;
  if (isset ($cb_web_perms)) $host["web_perms"] = $cb_web_perms;
  if (isset ($cb_ftp_perms)) $host["ftp_perms"] = $cb_ftp_perms;
  if (isset ($ta_firewall_perms)) $host["firewall_perms"] = cleanup($ta_firewall_perms);
  if (isset ($param_user)) $host["user_id"] = $param_user;
  if (isset ($rb_web_all)) $host["web_all"] = $rb_web_all;
  if (isset ($ta_web_list)) $host["web_list"] = $ta_web_list;
  
  if (isset ($new_order)) $host["new_order"] = $new_order;
  if (isset ($order_dir)) $host["order_dir"] = $order_dir;
  if (isset ($entity)) $host["entity"] = $entity;

  if (debug_level_isset($cdg_param)) {
    echo "action=$action";
    if ( $host ) {
      while ( list( $key, $val ) = each( $host ) ) {
        echo "<br>host[$key]=$val";
      }
    }
  }

  return $host;
}

///////////////////////////////////////////////////////////////////////////////
// Clean up a string coming from a textarea : 
//  + remove empty lines
//  + remove space at the beginning of the lines
//  + remove space at the end of the lines
//  + remove first empty line and last \n of the string
// Parameters:
//   - $str String to be cleaned up
// Returns:
//   - The string cleaned up  
///////////////////////////////////////////////////////////////////////////////
// Vieille implementation pas terrible (trop de regexp, pas simple)
// function cleanup($str) {
//   //echo "AVANT : <pre>$str<pre>\n";
//   // Espaces en fin de ligne
//   $str = preg_replace("/  *\r\n/", "\r\n", $str);
//   // Espaces en debut de ligne
//   $str = preg_replace("/\r\n  */", "\r\n", $str);
//   // Lignes vides
//   $str = preg_replace("/(\r\n)(\r\n)*/", "\r\n", $str);
//   // Premiere ligne vide
//   $str = preg_replace("/^ *\r\n/", '', $str);
//   // Derniere ligne vide
//   $str = preg_replace("/ *\r\n$/", '', $str);
//   //echo "APRES : <pre>$str<pre>";
//       
//   return $str;
// }

function cleanup($str) {
  $result = '';
  $lines = explode("\r\n", $str);
  $i = 0;
  while ($i < count($lines)) {
    $currentline = trim($lines[$i]);
    if ($currentline != '') {
      $result .= $currentline."\r\n";
    }
    $i++;
  }

  // Suppression du dernier retour chariot
  // (plus pratique pour les traitements type explode/tokenizer, on obtient pas de derniere chaine vide)
  $result = preg_replace("/\r\n$/", '', $result);
  return $result;
}

///////////////////////////////////////////////////////////////////////////////
// Host Action 
///////////////////////////////////////////////////////////////////////////////
function get_host_action() {
  global $host, $actions, $path, $l_host;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["host"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/host/host_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

// Search
  $actions["host"]["search"] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );

// New
  $actions["host"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/host/host_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('search','index','admin', 'showlist','detailconsult','display')
                                  );

// Detail Consult
  $actions["host"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/host/host_index.php?action=detailconsult&amp;param_host=".$host["id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate','showlist') 
                                  );

// Detail Update
  $actions["host"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/host/host_index.php?action=detailupdate&amp;param_host=".$host["id"]."",
     'Right'    => $cright_write,
     'Condition'=> array ('detailconsult', 'showlist', 'update') 
                                     	   );
					   
// Show List
  $actions["host"]["showlist"] = array (
     'Url'      => "$path/host/host_index.php?action=showlist&amp;param_host=".$obm_user["id"]."",
     'Right'    => $cright_read,
     'Condition'=> array ('None') 
                                     	   );
					   
// Insert
  $actions["host"]["insert"] = array (
    'Url'      => "$path/host/host_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions["host"]["update"] = array (
    'Url'      => "$path/host/host_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Check Delete
  $actions["host"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/host/host_index.php?action=check_delete&amp;param_host=".$host["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'showlist', 'showlist', 'detailupdate') 
                                     	   );

// Delete
  $actions["host"]["delete"] = array (
    'Url'      => "$path/host/host_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Dispay
  $actions["host"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/host/host_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );
// Dispay Prefs
  $actions["host"]["dispref_display"] = array (
    'Url'      => "$path/host/host_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );
// Display
  $actions["host"]["dispref_level"] = array (
    'Url'      => "$path/host/host_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );

// Dispay
  $actions["host"]["ext_get_id"] = array (
    'Url'      => "$path/host/host_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );
}

</script>
