<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : publication_index.php                                        //
//     - Desc : Company Index File                                           //
// 2004-01-28 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions              -- Parameter
// - index (default)    -- search fields  -- show the publication search form
// - search             -- search fields  -- show the result set of search
// - new                --                -- show the new publication form
// - detailconsult      -- $param_publication -- show publication detail
// - detailupdate       -- $param_publication -- show publication detail form
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
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Session, Auth, Perms  Management                                          //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$module = "publication";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("publication_query.inc");
require("publication_display.inc");

update_last_visit("publication", $param_publication, $action);

page_close();
if ($action == "") $action = "index";
$publication = get_param_publication();
get_publication_action();
$perm->check_permissions($module, $action);


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
} elseif ($action == "index" || $action == "") {
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
  $recept_q = run_query_subscriptionreception(); 
  $concat1_q = run_query_publication_contactcategory1();
  $type_q = run_query_publicationtype();
  require("publication_js.inc");
  $display["detail"] = html_publication_form($action,"", $type_q,$concat1_q,$recept_q, $publication);

} elseif ($action == "new_subscription")  {
///////////////////////////////////////////////////////////////////////////////
  $recept_q = run_query_subscriptionreception();
  $publication["lang"] = run_query_get_contact_lang($publication["contact_id"]);
  require("publication_js.inc");
  $display["detail"] =html_subscription_form($action,$sub_q, $recept_q, $publication);
} elseif ($action == "new_auto")  {
///////////////////////////////////////////////////////////////////////////////
  $recept_q = run_query_subscriptionreception();
  $pub_q = run_query_detail($param_publication);
  if($pub_q->nf() == 1) {
    require("publication_js.inc");
    $display["detail"] = html_auto_subscription_form($action,$pub_q, $recept_q, $publication);
  }

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
      $recept_q = run_query_subscriptionreception(); 
      require("publication_js.inc");
      $display["detailInfo"] = display_record_info($pub_q);
      $display["detail"] = html_publication_form($action, $pub_q,$type_q,"",$recept_q, $publication);
    } else {
      $display["msg"] .= display_err_msg($l_query_error . " - " . $pub_q->query . " !");
    }
  }

} elseif ($action == "detailupdate_subscription")  {
///////////////////////////////////////////////////////////////////////////////
  $recept_q = run_query_subscriptionreception();
  $publication["lang"] = run_query_get_contact_lang($publication["contact_id"]);
  $sub_q = run_query_subscription_detail($param_subscription);
  require("publication_js.inc");
  $display["detail"] =html_subscription_form($action,$sub_q,  $recept_q, $publication);

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
    $recept_q = run_query_subscriptionreception(); 
    $concat1_q = run_query_publication_contactcategory1();
    $display["search"] = html_publication_form($action, "",  $type_q,$concat1_q,$recept_q, $publication);
  }

} elseif ($action == "insert_subscription")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_subscription_form("", $publication)) {
    $retour = run_query_insert_subscription($publication);
    $quit = "
    <br />
    <a href=\"javascript: void(0);\" onclick=\"window.opener.location.reload();window.close();\" >
    $l_close
    </a>";
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_insert_ok.$quit);
    } else {
      $display["msg"] .= display_err_msg($l_insert_error.$quit);
    }
  // Form data are not valid
  } else {
    $display["msg"] = display_warn_msg($l_invalid_data . " : " . $err_msg);
    $recept_q = run_query_subscriptionreception();
    $publication["lang"] = run_query_get_contact_lang($publication["contact_id"]);
    require("publication_js.inc");
    $display["detail"] =html_subscription_form($action,$cont_q, $recept_q, $publication);        
  }

} elseif ($action == "new_group_subscription")  {
///////////////////////////////////////////////////////////////////////////////
  $pub_q = run_query_detail($param_publication);
  $concat1_q = run_query_publication_contactcategory1();
  $display["detail"] = html_group_subscription_form($action,$pub_q, $concat1_q,$publication);

} elseif ($action == "insert_group_subscription")  {
///////////////////////////////////////////////////////////////////////////////
  // If the context (same publications) was confirmed ok, we proceed
  $pub_q = run_query_detail($param_publication);
  $publication["lang"] = $pub_q->f("publication_lang");
  if(is_array($publication["auto_sub"]) &&
     count($publication["auto_sub"])>0) {
    $retour = run_query_auto_subscription($publication,$param_publication);
  }
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_insert_error);
  }
  $display["detailInfo"] .= display_record_info($pub_q);
  $display["detail"] = html_publication_consult($pub_q, $cat_q);

} elseif ($action == "insert_auto")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_auto_insert($publication);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_insert_error);
  } 
  $pub_q = run_query_detail($param_publication);
  $display["detailInfo"] .= display_record_info($pub_q);
  $display["detail"] = html_publication_consult($pub_q, $cat_q);
  
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
    $recept_q = run_query_subscriptionreception();     
    $pub_q = run_query_detail($param_publication);
    $display["detail"] = html_publication_form($action, $pub_q, $type_q,"",$recept_q, $publication);
  }

} elseif ($action == "update_subscription")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_subscription_form("", $publication)) {
    $retour = run_query_update_subscription($publication);
    $quit = "
    <br />
    <a href=\"javascript: void(0);\" onclick=\"window.opener.location.reload();window.close();\" >
    $l_close
    </a>";
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok.$quit);
    } else {
      $display["msg"] .= display_err_msg($l_update_error.$quit);
    }
  // Form data are not valid
  } else {
    $display["msg"] = display_warn_msg($l_invalid_data . " : " . $err_msg);
    $recept_q = run_query_subscriptionreception();
    $publication["lang"] = run_query_get_contact_lang($publication["contact_id"]);
    require("publication_js.inc");
    $display["detail"] =html_subscription_form($action,$cont_q, $recept_q, $publication);        
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  require("publication_js.inc");
  $display["detail"] = dis_check_publication_links($param_publication);

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_delete($hd_publication_id);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_delete_error);
  }
    require("publication_js.inc");  
  $type_q = run_query_publicationtype();
  $display["search"] = html_publication_search_form($type_q, $publication);
  
} elseif ($action == "delete_subscription")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_delete_subscription($param_subscription);
  $quit = "
  <br />
  <a href=\"javascript: void(0);\" onclick=\"window.opener.location.reload();window.close();\" >
  $l_close
  </a>";
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_delete_ok.$quit);
  } else {
    $display["msg"] .= display_err_msg($l_delete_error.$quit);
  }

} elseif ($action == "admin")  {
///////////////////////////////////////////////////////////////////////////////
  require("publication_js.inc");
  $display["detail"] = dis_publication_admin_index();

} elseif ($action == "type_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_type_insert($publication);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_type_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_type_insert_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_publication_admin_index();

} elseif ($action == "type_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_type_update($publication);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_type_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_type_update_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_publication_admin_index();

} elseif ($action == "type_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_type_links($publication);

} elseif ($action == "type_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_type_delete($publication["type"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_type_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_type_delete_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_publication_admin_index();

} elseif ($action == "recept_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_recept_insert($publication);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_recept_insert_ok);
  } else {
    $display["msg"] .= display_err_msg($l_recept_insert_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_publication_admin_index();

} elseif ($action == "recept_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_recept_update($publication);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_recept_update_ok);
  } else {
    $display["msg"] .= display_err_msg($l_recept_update_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_publication_admin_index();

} elseif ($action == "recept_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= dis_recept_links($publication);

} elseif ($action == "recept_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_recept_delete($publication["recept"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_recept_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_recept_delete_error);
  }
  require("publication_js.inc");
  $display["detail"] .= dis_publication_admin_index();


}  elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($auth->auth["uid"], "publication", 1);
  $display["detail"] = dis_publication_display_pref($prefs);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($auth->auth["uid"], "publication", 1);
  $display["detail"] = dis_publication_display_pref($prefs);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($auth->auth["uid"], "publication", 1);
  $display["detail"] = dis_publication_display_pref($prefs);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_publication);
$display["end"] = display_end();
// Update actions url in case some values have been updated (id after insert) 
if (! $publication["popup"]) {
  update_publication_action_url();
  $display["header"] = generate_menu($module, $section);
}

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Company parameters transmited in $publication hash
// returns : $publication hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_publication() {
  global $tf_title, $tf_year, $tf_lang, $param_subscription;
  global $sel_type,$ta_desc, $param_publication,$tf_type,$param_contact;
  global $param_publication_orig,$sel_contactcategory1;
  global $cb_renewal,$sel_recept,$tf_recept,$tf_quantity;
  global $popup, $ext_action, $ext_url, $ext_id, $ext_title, $ext_target;    
  global $HTTP_POST_VARS,$HTTP_GET_VARS;
  
  if (isset ($popup)) $publication["popup"] = $popup;
  if (isset ($ext_action)) $publication["ext_action"] = $ext_action;
  if (isset ($ext_url)) $publication["ext_url"] = urldecode($ext_url);
  if (isset ($ext_id)) $publication["ext_id"] = $ext_id;
  if (isset ($ext_id)) $publication["id"] = $ext_id;
  if (isset ($ext_title)) $publication["ext_title"] = stripslashes(urldecode($ext_title));
  if (isset ($ext_target)) $publication["ext_target"] = $ext_target;
  
  if (isset ($sel_contactcategory1)) $publication["auto_sub"] = $sel_contactcategory1;
  if (isset ($param_contact)) $publication["contact_id"] = $param_contact;
  if (isset ($param_publication)) $publication["id"] = $param_publication;
  if (isset ($param_subscription)) $publication["subscription_id"] = $param_subscription;
  if (isset ($param_publication_orig)) $publication["id_orig"] = $param_publication_orig;
  if (isset ($tf_title)) $publication["title"] = $tf_title;
  if (isset ($sel_type)) $publication["type"] = $sel_type;
  if (isset ($cb_renewal)) $publication["renew"] = $cb_renewal;
  else $publication["renew"] = 0;
  
  if (isset ($sel_recept)) $publication["recept"] = $sel_recept;
  if (isset ($tf_year)) $publication["year"] = $tf_year;
  if (isset ($tf_lang)) $publication["lang"] = $tf_lang;
  if (isset ($tf_quantity)) $publication["quantity"] = $tf_quantity;
  if (isset ($ta_desc)) $publication["desc"] = $ta_desc;

  if (isset ($tf_type)) $publication["type_label"] = $tf_type;
  if (isset ($tf_recept)) $publication["recept_label"] = $tf_recept;

  display_debug_param($publication);

  return $publication;
}


///////////////////////////////////////////////////////////////////////////////
// Company Actions 
///////////////////////////////////////////////////////////////////////////////
function get_publication_action() {
  global $publication, $actions, $path;
  global $l_header_find,$l_header_new_f,$l_header_update,$l_header_delete;
  global $l_header_consult, $l_header_display,$l_header_admin,$l_header_new_auto;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;
  global $l_subscription;

// Index
  $actions["publication"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/publication/publication_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    	 );
// Index
  $actions["publication"]["ext_get_id"] = array (
    'Url'      => "$path/publication/publication_index.php?action=ext_get_id",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                	 );
					 
// Search
  $actions["publication"]["search"] = array (
    'Url'      => "$path/publication/publication_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    	 );

// New
  $actions["publication"]["new"] = array (
    'Name'     => $l_header_new_f,
    'Url'      => "$path/publication/publication_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
                                     );
// New Publication from an other one.
  $actions["publication"]["new_auto"] = array (
    'Name'     => $l_header_new_auto,
    'Url'      => "$path/publication/publication_index.php?action=new_auto&amp;param_publication=".$publication["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('insert_group_subscription','detailconsult', 'update','insert_auto')
                                     );
	     
// Detail Consult
  $actions["publication"]["detailconsult"]  = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/publication/publication_index.php?action=detailconsult&amp;param_publication=".$publication["id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('insert_group_subscription','new_group_subscription','insert_auto','detailupdate') 
                                     		 );

// Detail Update
  $actions["publication"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/publication/publication_index.php?action=detailupdate&amp;param_publication=".$publication["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('insert_group_subscription','new_group_subscription','detailconsult', 'update','insert_auto') 
                                     	      );

// Subscribe a group of contact to a publication.
  $actions["publication"]["new_group_subscription"] = array (
    'Name'     => $l_subscription,
    'Url'      => "$path/publication/publication_index.php?action=new_group_subscription&amp;param_publication=".$publication["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('insert_group_subscription','new_group_subscription','detailconsult', 'update','insert_auto')
                                     );		
// Subscribe a group of contact to a publication.
  $actions["publication"]["insert_group_subscription"] = array (
    'Url'      => "$path/publication/publication_index.php?action=insert_group_subscription",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     );						     
// Subscription Update
  $actions["publication"]["detailupdate_subscription"] = array (
    'Url'      => "$path/publication/publication_index.php?action=detailupdate_subscription",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	      );
// Insert
  $actions["publication"]["insert"] = array (
    'Url'      => "$path/publication/publication_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );
// Insert
  $actions["publication"]["insert_auto"] = array (
    'Url'      => "$path/publication/publication_index.php?action=insert_auto",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                         );

// Update
  $actions["publication"]["update"] = array (
    'Url'      => "$path/publication/publication_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );

// Check Delete
  $actions["publication"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/publication/publication_index.php?action=check_delete&amp;param_publication=".$publication["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('insert_group_subscription','new_group_subscription','detailconsult', 'detailupdate', 'update') 
                                     	      );

// Delete
  $actions["publication"]["delete"] = array (
    'Url'      => "$path/publication/publication_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );

// Delete
  $actions["publication"]["delete_subscription"] = array (
    'Url'      => "$path/publication/publication_index.php?action=delete_subscription",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     	 );

// Admin
  $actions["publication"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/publication/publication_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );

// Kind Insert
  $actions["publication"]["type_insert"] = array (
    'Url'      => "$path/publication/publication_index.php?action=type_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Kind Update
  $actions["publication"]["type_update"] = array (
    'Url'      => "$path/publication/publication_index.php?action=type_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Kind Check Link
  $actions["publication"]["type_checklink"] = array (
    'Url'      => "$path/publication/publication_index.php?action=type_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Kind Delete
  $actions["publication"]["type_delete"] = array (
    'Url'      => "$path/publication/publication_index.php?action=type_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );
// Reception Insert
  $actions["publication"]["recept_insert"] = array (
    'Url'      => "$path/publication/publication_index.php?action=recept_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Reception Update
  $actions["publication"]["recept_update"] = array (
    'Url'      => "$path/publication/publication_index.php?action=recept_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Reception Check Link
  $actions["publication"]["recept_checklink"] = array (
    'Url'      => "$path/publication/publication_index.php?action=recept_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Reception Delete
  $actions["publication"]["recept_delete"] = array (
    'Url'      => "$path/publication/publication_index.php?action=recept_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );
// New Subscription
  $actions["publication"]["new_subscription"] = array (
    'Url'      => "$path/publication/publication_index.php?action=new_subscription",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     	       );
// Insert Subscription
  $actions["publication"]["insert_subscription"] = array (
    'Url'      => "$path/publication/publication_index.php?action=insert_subscription",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     	       );
// Update Subscription
  $actions["publication"]["update_subscription"] = array (
    'Url'      => "$path/publication/publication_index.php?action=update_subscription",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     	       );
					       
// Display
  $actions["publication"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/publication/publication_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

// Display Préférences
  $actions["publication"]["dispref_display"] = array (
    'Url'      => "$path/publication/publication_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// Display Level
  $actions["publication"]["dispref_level"]  = array (
    'Url'      => "$path/publication/publication_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

}


///////////////////////////////////////////////////////////////////////////////
// Company Actions URL updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_publication_action_url() {
  global $publication, $actions, $path;

  // Detail Consult
  $actions["publication"]["detailconsult"]["Url"] = "$path/publication/publication_index.php?action=detailconsult&amp;param_publication=".$publication["id"];

  // Detail Update
  $actions["publication"]["detailupdate"]['Url'] = "$path/publication/publication_index.php?action=detailupdate&amp;param_publication=".$publication["id"];

  // Check Delete
  $actions["publication"]["check_delete"]['Url'] = "$path/publication/publication_index.php?action=check_delete&amp;param_publication=".$publication["id"];

}

</script>
