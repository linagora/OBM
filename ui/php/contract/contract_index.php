<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : contract_index.php                                           //
//     - Desc : Contract Support Index File                                  //
// 2001-07-17 : Richard FILIPPI                                              //
///////////////////////////////////////////////////////////////////////////////
//  $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the Contract search form
// - search          -- search fields  -- show the result set of search
// - company_new     --                -- show the company selection form
// - new             -- $param_company -- show the new Contract form
// - detailconsult   -- $param_contract -- show the Contract detail
// - detailupdate    -- $param_contract -- show the Contract detail form
// - insert          -- form fields    -- insert the Contract 
// - update          -- form fields    -- update the Contract
// - delete          -- $param_contract -- delete the Contract
// - admin	     --		       -- admin index (kind)
// - admin_insert     -- form fields    -- insert the kind
// - admin_update     -- form fields    -- update the kind
// - admin_delete     -- form fields    -- delete the kind
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms Management                                             //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$menu="CONTRACT";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/phplib/obmlib.inc");
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
$perm->check("user");
include("$obminclude/global_pref.inc");

require("contract_query.inc");
require("contract_display.inc");



// Updating the "last contract" bookmark 
if ( ($param_contract == $last_contract) && (strcmp($action,"delete")==0) ) {
  $last_contract=$last_contract_default;
} elseif  ( ($param_contract > 0) && ($last_contract != $param_contract) ) {
    $last_contract=$param_contract;
    run_query_set_user_pref($auth->auth["uid"],"last_contract",$param_contract);
    $last_contract_name = run_query_global_contract_label($last_contract);
    //$sess->register("last_contract");
}

page_close();


$contract=get_param_contract();
display_head($l_contract);     // Head & Body

if ($popup) {
///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
  if ($action == "ext_get_id") {
    require("contract_js.inc");
    $cont_q = run_query_contract();
    html_select_contract($cont_q, stripslashes($title));
  } elseif ($action == "ext_get_id_url") {
    require("contract_js.inc");
    $cont_q = run_query_contract();
    html_select_contract($cont_q, stripslashes($title), $url);
  } else {
    display_error_permission();
  }

  display_end();
  exit();
}

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
generate_menu($menu);      // Menu
display_bookmarks();


///////////////////////////////////////////////////////////////////////////////
// Programe principal                                                        //
///////////////////////////////////////////////////////////////////////////////


if ($action == "index") {
///////////////////////////////////////////////////////////////////////////////
  require("contract_js.inc");
  $usr_q = run_query_userobm();
  html_contract_search_form($contract, $usr_q, run_query_contracttype());
  if ($set_display == "yes") {
    dis_contract_search_list($contract);
  } else {
    display_ok_msg($l_no_display);
  }
} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  require("contract_js.inc");
  $usr_q = run_query_userobm();
  html_contract_search_form($contract, $usr_q, run_query_contracttype());
  dis_contract_search_list($contract);

} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  if ($auth->auth["perm"] != $perms_user) {
    require("contract_js.inc");
    display_ok_msg(stripslashes($ok_message)."<br>".$l_add_contract_deal);
    html_contract_form($action,new DB_OBM,"",run_query_contracttype(),run_query_userobm(),run_query_company_info($param_company),run_query_contact_contract($param_company), $contract);
  } else {
    display_error_permission();
  }

} elseif ($action == "display") {
//OK///////////////////////////////////////////////////////////////////////////
  $pref_q=run_query_display_pref($auth->auth["uid"], "contract",1);
  dis_contract_display_pref($pref_q);

}else if($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $display);
  $pref_q=run_query_display_pref($auth->auth["uid"], "contract",1);
  dis_contract_display_pref($pref_q);

} else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);
  $pref_q=run_query_display_pref($auth->auth["uid"], "contract",1);
  dis_contract_display_pref($pref_q);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  if ($param_contract > 0) {
    $contract_q = run_query_detail($param_contract);
    display_record_info($contract_q->f("contract_usercreate"),$contract_q->f("contract_userupdate"),$contract_q->f("timecreate"),$contract_q->f("timeupdate"));
    html_contract_consult($contract_q,run_query_contracttype(),run_query_company_info($contract_q->f("contract_company_id")),run_query_contact_contract($contract_q->f("contract_company_id")),$contract_q->f("contract_company_id"),run_query_deal($contract_q->f("contract_deal_id")));
  }

} elseif ($action == "detailupdate")  {
///////////////////////h//////////////////////////////////////////////////////
  if ($param_contract > 0) {
    $contract_q = run_query_detail($param_contract);
    require("contract_js.inc");
    display_record_info($contract_q->f("contract_usercreate"),$contract_q->f("contract_userupdate"),$contract_q->f("timecreate"),$contract_q->f("timeupdate"));

    html_contract_form($action,$contract_q,run_query_deal($contract_q->f("contract_deal_id")),run_query_contracttype(),run_query_userobm(),run_query_company_info($contract_q->f("contract_company_id")),run_query_contact_contract($contract_q->f("contract_company_id")), $contract);
  }

} elseif ($action == "insert")  {
//OK///////////////////////////////////////////////////////////////////////////

 if (check_data_form("", $contract)) {
   run_query_insert($contract);
    display_ok_msg($l_insert_ok);
  } else {
    display_err_msg($l_invalid_data . " : " . $err_msg);
  }
  require("contract_js.inc");
  $usr_q = run_query_userobm();
  html_contract_search_form($contract, $usr_q, run_query_contracttype());
} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form("", $contract)) {  
    run_query_update($contract);         
    display_ok_msg($l_update_ok);
  } else  display_err_msg($l_invalid_data . " : " . $err_msg);
  require("contract_js.inc");
  $usr_q = run_query_userobm();
  html_contract_search_form($contract, $usr_q, run_query_contracttype());

} elseif ($action == "delete")  {
///OK//////////////////////////////////////////////////////////////////////////
  run_query_delete($param_contract);
  display_ok_msg($l_delete_ok);
  $usr_q = run_query_userobm();
  html_contract_search_form($contract, $usr_q, run_query_contracttype());

} elseif ($action == "admin")  {
//////////////////////////////////////////////////////////////////////////////
  if ($auth->auth["perm"] != $perms_user) {
    require("contract_js.inc");
    html_contract_admin_form(run_query_contracttype());
  } else {
    display_error_permission();
  }

} elseif ($action == "admintypeinsert")  {
///////////////////////////////////////////////////////////////////////////////
  $query = query_type_insert();
  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  if ($obm_q->query($query)) {
    display_ok_msg($l_type_insert_ok);
  } else {
    display_err_msg($l_type_insert_error);
  }
  require("contract_js.inc");
  html_contract_admin_form(run_query_contracttype());
  
} elseif ($action == "admintypedelete")  {
  ///////////////////////////////////////////////////////////////////////////////
  $obm_q = new DB_OBM;
  $query = query_type_verif();  // kind referenced in a Contract Contract ?
  $obm_q->query($query);
  if ($obm_q->num_rows() > 0) {
    //Font?
    display_err_msg($l_type_delete_error);
    echo $obm_q->num_rows() . " " . $l_deal . $l_type_del_verif_error . "<P>\n"; 
    while ($obm_q->next_record()) {
      echo "<A HREF=\"contract_index.php?action=detailconsult&amp;param_contract=" . $obm_q->f("contract_id") ."\">" . $obm_q->f("contract_label") . "</A><BR>\n";
    }
  } else {
    $query = query_type_delete(); 
    if ($obm_q->query($query)) {
      display_ok_msg($l_type_delete_ok);
    } else {
      display_err_msg($l_type_delete_error);
    }
  }
  require("contract_js.inc");
  html_contract_admin_form(run_query_contracttype());
  
} elseif ($action == "admintypeupdate")  {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = new DB_OBM;
  $query = query_type_update();
  display_debug_msg($query, $cdg_sql);
  if ($obm_q->query($query)) {
    display_ok_msg($l_type_update_ok);
  } else {
    display_err_msg($l_type_update_error);
  }
  
  require("contract_js.inc");
  html_contract_admin_form(run_query_contracttype());
}


///////////////////////////////////////////////////////////////////////////////
// Stores Contract parameters transmited in $contract hash
// returns : $contract hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_contract() {
  global $tf_label,$tf_company_name,$sel_type;
  global $tf_dateafter,$tf_datebefore,$sel_manager,$cb_arc,$param_company;
  global $param_contract,$tf_num,$sel_market, $sel_tech, $hd_soc;
  global $ta_clause,$ta_com,$sel_con1, $sel_con2,$tf_datebegin,$tf_dateexp;
  global $hd_usercreate,$hd_timeupdate, $param_deal, $deal_new_id;
  global $hd_company_ad1, $hd_company_zip, $hd_company_town;
  global $cdg_param, $action;

  if (isset ($param_contract)) $contract["id"] = $param_contract;
  if (isset ($param_company)) $contract["company_id"] = $param_company;

  if (isset ($tf_label)) $contract["label"] = $tf_label;
  if (isset ($tf_datebegin)) $contract["datebegin"] = $tf_datebegin;
  if (isset ($tf_dateexp)) $contract["dateexp"] = $tf_dateexp;
  if (isset ($tf_num)) $contract["number"] = $tf_num;

  if (isset ($sel_market)) $contract["market"] = $sel_market;
  if (isset ($sel_tech)) $contract["tech"] = $sel_tech;
  if (isset ($sel_con1)) $contract["contact1"] = $sel_con1;
  if (isset ($sel_con2)) $contract["contact2"] = $sel_con2;
  if (isset ($sel_type)) $contract["type"] = $sel_type;

  if (isset ($cb_arc)) $contract["arc"] = $cb_arc;

  if (isset ($ta_clause)) $contract["clause"] = $ta_clause;  
  if (isset ($ta_com)) $contract["comment"] = $ta_com;  

  if (isset ($hd_soc)) $contract["company_id"] = $hd_soc;

  if (isset ($hd_usercreate)) $contract["usercreate"] = $hd_usercreate;
  if (isset ($hd_timeupdate)) $contract["timeupdate"] = $hd_timeupdate;

  // Search fields
  if (isset ($tf_dateafter)) $contract["dateafter"] = $tf_dateafter;
  if (isset ($tf_datebefore)) $contract["datebefore"] = $tf_datebefore;
  if (isset ($sel_manager)) $contract["manager"] = $sel_manager;
  if (isset ($tf_company_name)) $contract["company_name"] = $tf_company_name;

  // Company infos (with company_name)
  if (isset ($hd_company_ad1)) $contract["company_ad1"] = $hd_company_ad1;
  if (isset ($hd_company_zip)) $contract["company_zip"] = $hd_company_zip;
  if (isset ($hd_company_town)) $contract["company_town"] = $hd_company_town;

  if (isset ($param_deal)) $contract["deal_id"] = $param_deal;
  if (isset ($deal_new_id)) $contract["deal_new_id"] = $deal_new_id;
  if (debug_level_isset($cdg_param)) {
    echo "<br>action=$action";
    if ( $contract ) {
      while ( list( $key, $val ) = each( $contract ) ) {
        echo "<br>contract[$key]=$val";
      }
    }
  }

  return $contract;
}

///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
display_end();


</SCRIPT>
