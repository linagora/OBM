<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : mailshare_index.php                                          //
//     - Desc : MailShare Index File                                         //
// 2005-10-07 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields    -- show the mailshare search form
// - search          -- search fields    -- show the result set of search
// - new             --                  -- show the new mailshare form
// - detailconsult   -- $mailshare_id    -- show the mailshare detail
// - detailupdate    -- $mailshare_id    -- show the mailshare detail form
// - insert          -- form fields      -- insert the mailshare
// - update          -- form fields      -- update the mailshare
// - check_delete    -- $mailshare_id    -- check the mailshare
// - delete          -- $mailshare_id    -- delete the mailshare
// - display         --                  -- display and set display parameters
// - dispref_display --                  -- update one field display value
// - dispref_level   --                  -- update one field display position 
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "mailshare";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_mailshare_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("mailshare_display.inc");
require("mailshare_query.inc");
require("mailshare_js.inc");
require("$obminclude/lib/right.inc");

if ($action == "") $action = "index";
get_mailshare_action();
$perm->check_permissions($module, $action);
update_last_visit("mailshare", $params["mailshare_id"], $action);

page_close();


///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == "ext_get_id") {
  $display["search"] = html_host_search_form($params);
  if ($set_display == "yes") {
    $display["result"] = dis_host_search_list($params);
  } else {
    $display["msg"] = display_info_msg($l_no_display);
  }

} else if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_mailshare_search_form($params);
  if ($set_display == "yes") {
    $display["result"] = dis_mailshare_search_list("");
  } else {
    $display["msg"] .= display_ok_msg($l_no_display);
  }

} else if ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_mailshare_search_form($params);
  $display["result"] = dis_mailshare_search_list($params);

} else if ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = html_mailshare_form($action, "", $params);

} else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_mailshare_consult($params);

} else if ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_mailshare_detail($params["mailshare_id"]);
  $display["detail"] = html_mailshare_form($action, $obm_q, $params);

} else if ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_mailshare_data_form($params)) {
    
    // If the context (same mailshare ?) was confirmed ok, we proceed
    if ($params["confirm"] == $c_yes) {
      $id = run_query_mailshare_insert($params);
      if ($id > 0) {
	$params["mailshare_id"] = $id;
	// ALIAMIN
	//        update_update_state();
        $display["msg"] .= display_ok_msg("$l_mailshare : $l_insert_ok");
      } else {
        $display["msg"] .= display_err_msg("$l_mailshare : $l_insert_error");
      }
      $display["detail"] = dis_mailshare_consult($params);
      
    // If first try, we warn the user if some mailshare seem similar
    } else {
      $obm_q = check_mailshare_context("", $params);
      if ($obm_q->num_rows() > 0) {
        $display["detail"] = dis_mailshare_warn_insert("", $obm_q, $params);
      } else {
        $id = run_query_mailshare_insert($params);

	if ($id > 0) {
	  $params["mailshare_id"] = $id;
	// ALIAMIN
          //update_update_state();
          $display["msg"] .= display_ok_msg("$l_mailshare : $l_insert_ok");
	} else {

          $display["msg"] .= display_err_msg("$l_mailshare : $l_insert_error");
        }
    
        $display["detail"] = dis_mailshare_consult($params);
      }
    }
    
  // Form data are not valid
  } else {
    $display["msg"] .= display_err_msg($err["msg"]);
    $display["detail"].= html_mailshare_form($action, "", $params, $err["field"]);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_mailshare_data_form($params)) {
    $retour = run_query_mailshare_update($params);
    if ($retour) {
	// ALIAMIN
      //      update_update_state();
      $display["msg"] .= display_ok_msg("$l_mailshare : $l_update_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_mailshare : $l_update_error");
    }
    $display["detail"] = dis_mailshare_consult($params);
  } else {
    $display["msg"] .= display_err_msg($err["msg"]);
    $mailshare_q = run_query_mailshare_detail($params["mailshare_id"]);
    $display["detail"] = html_mailshare_form($action, $mailshare_q, $params, $err["field"]);
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_mailshare($params["mailshare_id"])) {
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_can_delete_mailshare($params["mailshare_id"]);
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_mailshare_consult($params);
  }

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_mailshare($params["mailshare_id"])) {
    $retour = run_query_mailshare_delete($params["mailshare_id"]);
    if ($retour) {
	// ALIAMIN
      //      update_update_state();
      $display["msg"] .= display_ok_msg("$l_mailshare : $l_delete_ok");
    } else {
      $display["msg"] .= display_err_msg("$l_mailshare : $l_delete_error");
    }
    $display["search"] = html_mailshare_search_form("");
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_mailshare_consult($params);
  }

} elseif ($action == "rights_admin") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = of_right_dis_admin("mailshare", $params["entity_id"]);

} elseif ($action == "rights_update") {
///////////////////////////////////////////////////////////////////////////////
  of_right_update_right($params, "mailshare");
	// ALIAMIN
  //  update_update_state();
  $display["msg"] .= display_warn_msg($err["msg"]);
  $display["detail"] = of_right_dis_admin("mailshare", $params["entity_id"]);

} else if ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($uid, "mailshare", 1);
  $display["detail"] = dis_mailshare_display_pref($prefs);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($uid, "mailshare", 1);
  $display["detail"] = dis_mailshare_display_pref($prefs);

} else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($uid, "mailshare", 1);
  $display["detail"] = dis_mailshare_display_pref($prefs);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_mailshare);
if (! $params["popup"]) {
  update_mailshare_action();
  $display["header"] = display_menu($module);
}
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, Host parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_mailshare_params() {
  global $action, $cdg_param, $popup;
  global $cb_read_public, $cb_write_public,$sel_accept_write,$sel_accept_read,$param_entity;

  // Get global params
  $params = get_global_params("UserObm");

  // Rights parameters
  if (isset($param_entity)) $params["entity_id"] = $param_entity;
  if (is_array($sel_accept_write)) $params["accept_w"] = $sel_accept_write;
  if (is_array($sel_accept_read)) $params["accept_r"] = $sel_accept_read;
  if (isset($cb_write_public)) $params["public_w"] = $cb_write_public;
  if (isset($cb_read_public)) $params["public_r"] = $cb_read_public;

  if (debug_level_isset($cdg_param)) {
    echo "action=$action";
    if ( $params ) {
      while ( list( $key, $val ) = each( $params ) ) {
        echo "<br>mailshare[$key]=$val";
      }
    }
  }

  return $params;
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
// MailSahre Action 
///////////////////////////////////////////////////////////////////////////////
function get_mailshare_action() {
  global $params, $actions, $path, $l_mailshare;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_consult,$l_header_display,$l_header_admin, $l_header_right;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["mailshare"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/mailshare/mailshare_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );

// Search
  $actions["mailshare"]["search"] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );

// New
  $actions["mailshare"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/mailshare/mailshare_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('search','index','admin', 'showlist','detailconsult','delete','display')
                                  );

// Detail Consult
  $actions["mailshare"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/mailshare/mailshare_index.php?action=detailconsult&amp;mailshare_id=".$params["mailshare_id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate', 'showlist', 'update') 
                                  );

// Detail Update
  $actions["mailshare"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/mailshare/mailshare_index.php?action=detailupdate&amp;mailshare_id=".$params["mailshare_id"]."",
     'Right'    => $cright_write,
     'Condition'=> array ('detailconsult', 'showlist', 'update') 
                                     	   );
					   
// Insert
  $actions["mailshare"]["insert"] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions["mailshare"]["update"] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Check Delete
  $actions["mailshare"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/mailshare/mailshare_index.php?action=check_delete&amp;mailshare_id=".$params["mailshare_id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'showlist', 'showlist', 'detailupdate') 
                                     	   );

// Delete
  $actions["mailshare"]["delete"] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );

// Rights Admin.
  $actions["mailshare"]["rights_admin"] = array (
    'Name'     => $l_header_right,
    'Url'      => "$path/mailshare/mailshare_index.php?action=rights_admin&amp;param_entity=".$params["mailshare_id"]."",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult')
                                     );

// Rights Update
  $actions["mailshare"]["rights_update"] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=rights_update&amp;param_entity=".$params["mailshare_id"]."",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                     );

// Display
  $actions["mailshare"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/mailshare/mailshare_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );
// Dispay Prefs
  $actions["mailshare"]["dispref_display"] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );
// Display
  $actions["mailshare"]["dispref_level"] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );

// External call
  $actions["mailshare"]["ext_get_id"] = array (
    'Url'      => "$path/mailshare/mailshare_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );
}


///////////////////////////////////////////////////////////////////////////////
// MailShare Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_mailshare_action() {
  global $params, $actions, $path;

  $id = $params["mailshare_id"];
  if ($id > 0) {
    // Detail Consult
    $actions["mailshare"]["detailconsult"]["Url"] = "$path/mailshare/mailshare_index.php?action=detailconsult&amp;mailshare_id=$id";
    
    // Detail Update
    $actions["mailshare"]["detailupdate"]['Url'] = "$path/mailshare/mailshare_index.php?action=detailupdate&amp;mailshare_id=$id";
    $actions["mailshare"]["detailupdate"]['Condition'][] = 'insert';
    
    // Check Delete
    $actions["mailshare"]["check_delete"]['Url'] = "$path/mailshare/mailshare_index.php?action=check_delete&amp;mailshare_id=$id";
    $actions["mailshare"]["check_delete"]['Condition'][] = 'insert';

    // Rights admin
    $actions["mailshare"]["rights_admin"]['Url'] = "$path/mailshare/mailshare_index.php?action=rights_admin&amp;mailshare_id=$id";
    $actions["mailshare"]["rights_admin"]['Condition'][] = 'insert';
  }
}


</script>
