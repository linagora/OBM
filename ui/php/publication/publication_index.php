<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : publication_index.php                                            //
//     - Desc : Company Index File                                           //
// 2004-01-28 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions              -- Parameter
// - index (default)    -- search fields  -- show the publication search form
// - search             -- search fields  -- show the result set of search
// - new                --                -- show the new publication form
// - detailconsult      -- $param_publication -- show the publication detail
// - detailupdate       -- $param_publication -- show the publication detail form
// - insert             -- form fields    -- insert the publication
// - update             -- form fields    -- update the publication
// - check_delete       -- $param_publication -- check links before delete
// - delete             -- $hd_publication_id -- delete the publication
// - admin              --                -- admin index (type)
// - type_insert        -- form fields    -- insert the type
// - type_update        -- form fields    -- update the type
// - type_checklink     --                -- check if type is used
// - type_delete        -- $sel_type      -- delete the type
// - activity_insert    -- form fields    -- insert the type
// - activity_update    -- form fields    -- update the type
// - activity_checklink --                -- check if type is used
// - activity_delete    -- $sel_type      -- delete the type
// - display            --                -- display and set display parameters
// - dispref_display    --                -- update one field display value
// - dispref_level      --                -- update one field display position 
// External API ---------------------------------------------------------------
// - ext_get_id         -- $title         -- select a publication (return id) 
// - ext_get_id_url     -- $url, $title   -- select a publication (id), load URL 
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Session, Auth, Perms  Management                                          //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "COM";
$menu = "PUBLICATION";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("publication_query.inc");
require("publication_display.inc");


page_close();
if ($action == "") $action = "index";
$publication = get_param_publication();
get_publication_action();
$perm->check();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////

if ($action == "ext_get_id") {
  require("publication_js.inc");  
  $type_q = run_query_publicationtype();
  $display["search"] = html_publication_search_form($type_q, $publication);
  $display["result"] = dis_publication_search_list($publication);  
///////////////////////////////////////////////////////////////////////////////
// Normal calls
///////////////////////////////////////////////////////////////////////////////
}elseif ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $type_q = run_query_publicationtype();
  $display["search"] = html_publication_search_form($type_q, $publication);
  if ($set_display == "yes") {
    $display["result"] = dis_publication_search_list($publication);
  } else {
    $display["msg"] = display_info_msg($l_no_display);
  }

} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  require("publication_js.inc");  
  $type_q = run_query_publicationtype();
  $display["search"] = html_publication_search_form($type_q, $publication);
  $display["result"] = dis_publication_search_list($publication);

} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  $type_q = run_query_publicationtype();
  require("publication_js.inc");
  $display["detail"] = html_publication_form($action,"", $type_q, $publication);

} elseif ($action == "new_subscription")  {
///////////////////////////////////////////////////////////////////////////////
  $renew_q = run_query_subscriptionrenewal();
  $recept_q = run_query_subscriptionreception();
  $publication["lang"] = run_query_get_contact_lang($publication["contact_id"]);
  require("publication_js.inc");
  $display["detail"] =html_subscription_form($action,$cont_q, $renew_q, $recept_q, $publication);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_publication > 0) {
    $pub_q = run_query_detail($param_publication);
    if ($pub_q->num_rows() == 1) {
      $display["detailInfo"] = display_record_info($pub_q);
      $display["detail"] = html_publication_consult($pub_q);
    } else {
      $display["msg"] .= display_err_msg($l_query_error . " - " . $pub_q->query . " !");
    }
  }

} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_publication > 0) {
    $pub_q = run_query_detail($param_publication);
    if ($pub_q->num_rows() == 1) {
      $type_q = run_query_publicationtype();
      require("publication_js.inc");
      $display["detailInfo"] = display_record_info($pub_q);
      $display["detail"] = html_publication_form($action, $pub_q,$type_q, $publication);
    } else {
      $display["msg"] .= display_err_msg($l_query_error . " - " . $pub_q->query . " !");
    }
  }

} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form("", $publication)) {

    // If the context (same publications) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $retour = run_query_insert($publication);
      if ($retour) {
        $display["msg"] .= display_ok_msg($l_insert_ok);
      } else {
        $display["msg"] .= display_err_msg($l_insert_error);
      }
      $type_q = run_query_publicationtype();
      $display["search"] = html_publication_search_form($type_q, $publication);
    // If it is the first try, we warn the user if some publications seem similar
    } else {
      $obm_q = check_publication_context("", $publication);
      if ($obm_q->num_rows() > 0) {
        $display["detail"] = dis_publication_warn_insert("", $obm_q, $publication);
      } else {
        $retour = run_query_insert($publication);
        if ($retour) {
          $display["msg"] .= display_ok_msg($l_insert_ok);
        } else {
          $display["msg"] .= display_err_msg($l_insert_error);
        }
        $type_q = run_query_publicationtype();
        $display["search"] = html_publication_search_form($type_q,$publication);
      }
    }

  // Form data are not valid
  } else {
    $display["msg"] = display_warn_msg($l_invalid_data . " : " . $err_msg);
    $type_q = run_query_publicationtype();
    $display["search"] = html_publication_form($action, "",  $type_q, $publication);
  }
} elseif ($action == "insert_subscription")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_publication_form("", $publication)) {
    $retour = run_query_insert_subscription($publication);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_insert_ok);
    } else {
      $display["msg"] .= display_err_msg($l_insert_error);
    }
    $type_q = run_query_publicationtype();
    $display["search"] = html_publication_search_form($type_q,$publication);
  // Form data are not valid
  } else {
    $display["msg"] = display_warn_msg($l_invalid_data . " : " . $err_msg);
    $renew_q = run_query_subscriptionrenewal();
    $recept_q = run_query_subscriptionreception();
    $publication["lang"] = run_query_get_contact_lang($publication["contact_id"]);
    require("publication_js.inc");
    $display["detail"] =html_subscription_form($action,$cont_q, $renew_q, $recept_q, $publication);    
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($param_publication, $publication)) {
    $retour = run_query_update($param_publication, $publication);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    $pub_q = run_query_detail($param_publication);
    $display["detailInfo"] .= display_record_info($pub_q);
    $display["detail"] = html_publication_consult($pub_q, $cat_q);
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $type_q = run_query_publicationtype();
    $pub_q = run_query_detail($param_publication);
    $display["detail"] = html_publication_form($action, $pub_q, $type_q, $publication);
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  require("publication_js.inc");
  $display["detail"] = dis_check_links($param_publication);

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_delete($hd_publication_id);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_delete_error);
  }
  $type_q = run_query_publicationtype();
  $act_q = run_query_publicationactivity();
  $usr_q = run_query_userobm_active();
  $display["search"] = html_publication_search_form($type_q, $act_q, $usr_q, $publication);

} elseif ($action == "admin")  {
///////////////////////////////////////////////////////////////////////////////
  require("publication_js.inc");
  $display["detail"] = dis_admin_index();

} elseif ($action == "type_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_type_insert($publication);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_type_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_type_insert_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "type_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_type_update($publication);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_type_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_type_update_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "type_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_type_links($publication);
  require("publication_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "type_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_type_delete($publication["type"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_type_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_type_delete_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "renew_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_renew_insert($publication);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_renew_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_renew_insert_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "renew_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_renew_update($publication);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_renew_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_renew_update_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "renew_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_renew_links($publication);
  require("publication_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "renew_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_renew_delete($publication["renew"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_renew_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_renew_delete_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "recept_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_recept_insert($publication);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_recept_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_recept_insert_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "recept_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_recept_update($publication);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_recept_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_recept_update_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "recept_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_recept_links($publication);
  require("publication_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "recept_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_recept_delete($publication["recept"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_recept_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_recept_delete_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_admin_index();


}  elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $pref_q = run_query_display_pref($auth->auth["uid"], "publication", 1);
  $display["detail"] = dis_publication_display_pref($pref_q);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $disstatus);
  $pref_q = run_query_display_pref($auth->auth["uid"], "publication", 1);
  $display["detail"] = dis_publication_display_pref($pref_q);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);
  $pref_q = run_query_display_pref($auth->auth["uid"], "publication", 1);
  $display["detail"] = dis_publication_display_pref($pref_q);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_publication);
$display["end"] = display_end();
// Update actions url in case some values have been updated (id after insert) 
if (! $publication["popup"]) {
  update_publication_action_url();
  $display["header"] = generate_menu($menu, $section);
}

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Company parameters transmited in $publication hash
// returns : $publication hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_publication() {
  global $tf_title, $tf_year, $tf_lang;
  global $sel_type,$ta_desc, $param_publication,$tf_type,$param_contact;
  global $sel_renew,$tf_renew,$sel_recept,$tf_recept,$tf_quantity;
  global $popup, $ext_action, $ext_url, $ext_id, $ext_title, $ext_target;    
  global $cdg_param;
  global $HTTP_POST_VARS,$HTTP_GET_VARS;
  
  if (isset ($popup)) $publication["popup"] = $popup;
  if (isset ($ext_action)) $publication["ext_action"] = $ext_action;
  if (isset ($ext_url)) $publication["ext_url"] = urldecode($ext_url);
  if (isset ($ext_id)) $publication["ext_id"] = $ext_id;
  if (isset ($ext_id)) $publication["id"] = $ext_id;
  if (isset ($ext_title)) $publication["ext_title"] = stripslashes(urldecode($ext_title));
  if (isset ($ext_target)) $publication["ext_target"] = $ext_target;
  
  if (isset ($param_contact)) $publication["contact_id"] = $param_contact;
  if (isset ($param_publication)) $publication["id"] = $param_publication;
  if (isset ($tf_title)) $publication["title"] = $tf_title;
  if (isset ($sel_type)) $publication["type"] = $sel_type;
  if (isset ($sel_renew)) $publication["renew"] = $sel_renew;
  if (isset ($sel_recept)) $publication["recept"] = $sel_recept;
  if (isset ($tf_year)) $publication["year"] = $tf_year;
  if (isset ($tf_lang)) $publication["lang"] = $tf_lang;
  if (isset ($tf_quantity)) $publication["quantity"] = $tf_quantity;
  if (isset ($ta_desc)) $publication["desc"] = $ta_desc;

  if (isset ($tf_type)) $publication["type_label"] = $tf_type;
  if (isset ($tf_renew)) $publication["renew_label"] = $tf_renew;
  if (isset ($tf_recept)) $publication["recept_label"] = $tf_recept;

  if (debug_level_isset($cdg_param)) {
    if ( $publication ) {
      while ( list( $key, $val ) = each( $publication ) ) {
        echo "<br />publication[$key]=$val";
      }
    }
  }

  return $publication;
}


///////////////////////////////////////////////////////////////////////////////
// Company Actions 
///////////////////////////////////////////////////////////////////////////////
function get_publication_action() {
  global $publication, $actions, $path;
  global $l_header_find,$l_header_new_f,$l_header_update,$l_header_delete;
  global $l_header_consult, $l_header_display,$l_header_admin;
  global $publication_read, $publication_write, $publication_admin_read, $publication_admin_write;

// Index
  $actions["PUBLICATION"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/publication/publication_index.php?action=index",
    'Right'    => $publication_read,
    'Condition'=> array ('all') 
                                    	 );

// Search
  $actions["PUBLICATION"]["search"] = array (
    'Url'      => "$path/publication/publication_index.php?action=search",
    'Right'    => $publication_read,
    'Condition'=> array ('None') 
                                    	 );

// New
  $actions["PUBLICATION"]["new"] = array (
    'Name'     => $l_header_new_f,
    'Url'      => "$path/publication/publication_index.php?action=new",
    'Right'    => $publication_write,
    'Condition'=> array ('search','index','detailconsult','insert','update','admin','display') 
                                     );

// Detail Consult
  $actions["PUBLICATION"]["detailconsult"]  = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/publication/publication_index.php?action=detailconsult&amp;param_publication=".$publication["id"]."",
    'Right'    => $publication_read,
    'Condition'=> array ('detailupdate') 
                                     		 );

// Detail Update
  $actions["PUBLICATION"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/publication/publication_index.php?action=detailupdate&amp;param_publication=".$publication["id"]."",
    'Right'    => $publication_write,
    'Condition'=> array ('detailconsult', 'update') 
                                     	      );

// Insert
  $actions["PUBLICATION"]["insert"] = array (
    'Url'      => "$path/publication/publication_index.php?action=insert",
    'Right'    => $publication_write,
    'Condition'=> array ('None') 
                                     	 );

// Update
  $actions["PUBLICATION"]["update"] = array (
    'Url'      => "$path/publication/publication_index.php?action=update",
    'Right'    => $publication_write,
    'Condition'=> array ('None') 
                                     	 );

// Check Delete
  $actions["PUBLICATION"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/publication/publication_index.php?action=check_delete&amp;param_publication=".$publication["id"]."",
    'Right'    => $publication_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update') 
                                     	      );

// Delete
  $actions["PUBLICATION"]["delete"] = array (
    'Url'      => "$path/publication/publication_index.php?action=delete",
    'Right'    => $publication_write,
    'Condition'=> array ('None') 
                                     	 );

// Admin
  $actions["PUBLICATION"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/publication/publication_index.php?action=admin",
    'Right'    => $publication_admin_read,
    'Condition'=> array ('all') 
                                       );

// Kind Insert
  $actions["PUBLICATION"]["type_insert"] = array (
    'Url'      => "$path/publication/publication_index.php?action=type_insert",
    'Right'    => $publication_admin_write,
    'Condition'=> array ('None') 
                                     	     );

// Kind Update
  $actions["PUBLICATION"]["type_update"] = array (
    'Url'      => "$path/publication/publication_index.php?action=type_update",
    'Right'    => $publication_admin_write,
    'Condition'=> array ('None') 
                                     	      );

// Kind Check Link
  $actions["PUBLICATION"]["type_checklink"] = array (
    'Url'      => "$path/publication/publication_index.php?action=type_checklink",
    'Right'    => $publication_admin_write,
    'Condition'=> array ('None') 
                                     		);

// Kind Delete
  $actions["PUBLICATION"]["type_delete"] = array (
    'Url'      => "$path/publication/publication_index.php?action=type_delete",
    'Right'    => $publication_admin_write,
    'Condition'=> array ('None') 
                                     	       );
// Renewal Insert
  $actions["PUBLICATION"]["renew_insert"] = array (
    'Url'      => "$path/publication/publication_index.php?action=renew_insert",
    'Right'    => $publication_admin_write,
    'Condition'=> array ('None') 
                                     	     );

// Renewal Update
  $actions["PUBLICATION"]["renew_update"] = array (
    'Url'      => "$path/publication/publication_index.php?action=renew_update",
    'Right'    => $publication_admin_write,
    'Condition'=> array ('None') 
                                     	      );

// Renewal Check Link
  $actions["PUBLICATION"]["renew_checklink"] = array (
    'Url'      => "$path/publication/publication_index.php?action=renew_checklink",
    'Right'    => $publication_admin_write,
    'Condition'=> array ('None') 
                                     		);

// Renewal Delete
  $actions["PUBLICATION"]["renew_delete"] = array (
    'Url'      => "$path/publication/publication_index.php?action=renew_delete",
    'Right'    => $publication_admin_write,
    'Condition'=> array ('None') 
                                     	       );
// Reception Insert
  $actions["PUBLICATION"]["recept_insert"] = array (
    'Url'      => "$path/publication/publication_index.php?action=recept_insert",
    'Right'    => $publication_admin_write,
    'Condition'=> array ('None') 
                                     	     );

// Reception Update
  $actions["PUBLICATION"]["recept_update"] = array (
    'Url'      => "$path/publication/publication_index.php?action=recept_update",
    'Right'    => $publication_admin_write,
    'Condition'=> array ('None') 
                                     	      );

// Reception Check Link
  $actions["PUBLICATION"]["recept_checklink"] = array (
    'Url'      => "$path/publication/publication_index.php?action=recept_checklink",
    'Right'    => $publication_admin_write,
    'Condition'=> array ('None') 
                                     		);

// Reception Delete
  $actions["PUBLICATION"]["recept_delete"] = array (
    'Url'      => "$path/publication/publication_index.php?action=recept_delete",
    'Right'    => $publication_admin_write,
    'Condition'=> array ('None') 
                                     	       );					       
// Display
  $actions["PUBLICATION"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/publication/publication_index.php?action=display",
    'Right'    => $publication_read,
    'Condition'=> array ('all') 
                                      	 );

// Display Préférences
  $actions["PUBLICATION"]["dispref_display"] = array (
    'Url'      => "$path/publication/publication_index.php?action=dispref_display",
    'Right'    => $publication_read,
    'Condition'=> array ('None') 
                                     		 );

// Display Level
  $actions["PUBLICATION"]["dispref_level"]  = array (
    'Url'      => "$path/publication/publication_index.php?action=dispref_level",
    'Right'    => $publication_read,
    'Condition'=> array ('None') 
                                     		 );

}


///////////////////////////////////////////////////////////////////////////////
// Company Actions URL updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_publication_action_url() {
  global $publication, $actions, $path;

  // Detail Consult
  $actions["PUBLICATION"]["detailconsult"]["Url"] = "$path/publication/publication_index.php?action=detailconsult&amp;param_publication=".$publication["id"];

  // Detail Update
  $actions["PUBLICATION"]["detailupdate"]['Url'] = "$path/publication/publication_index.php?action=detailupdate&amp;param_publication=".$publication["id"];

  // Check Delete
  $actions["PUBLICATION"]["check_delete"]['Url'] = "$path/publication/publication_index.php?action=check_delete&amp;param_publication=".$publication["id"];

}

</script>
