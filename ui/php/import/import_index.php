<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : import_index.php                                             //
//     - Desc : Import Index File                                            //
// 2004-01-16 - Aliacom - Pierre Baudracco                                   //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the list search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new list form
// - detailconsult   -- $param_list    -- show the list detail
// - detailupdate    -- $param_list    -- show the list detail form
// - insert          -- form fields    -- insert the list
// - update          -- form fields    -- update the list
// - delete          -- $param_list    -- delete the list
// - file_sample     -- 
// - file_test       -- 
// - file_import     -- 
// - file_conflict   -- 
// - contact_del     -- 
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
// - export_add      --                --
// External API ---------------------------------------------------------------
// - ext_get_ids     --                -- select multiple lists (return id) 
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms Management                                             //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "ADMIN";
$menu = "IMPORT";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));

///////////////////////////////////////////////////////////////////////////////
// Company and Contact lang files inclusions
///////////////////////////////////////////////////////////////////////////////
$lang_file = "$obminclude/lang/$set_lang/company.inc";
if (file_exists("$path/../".$lang_file)) {
  include("$lang_file");
}

// Specific site company lang file
if ($cgp_site_include) {
  $lang_file = "$obminclude/site/lang/$set_lang/company.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  }
}

$lang_file = "$obminclude/lang/$set_lang/contact.inc";
if (file_exists("$path/../".$lang_file)) {
  include("$lang_file");
}

// Specific site contact lang file
if ($cgp_site_include) {
  $lang_file = "$obminclude/site/lang/$set_lang/contact.inc";
  if (file_exists("$path/../".$lang_file)) {
    include("$lang_file");
  }
}

///////////////////////////////////////////////////////////////////////////////

require("$obminclude/global_pref.inc");

include("import_display.inc");
include("import_query.inc");

if ($action == "") $action = "index";
$uid = $auth->auth["uid"];
$import = get_param_import();
get_import_action();
$perm->check_permissions($menu, $action);

require("import_js.inc");

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
if (! $popup) {
  $display["header"] = generate_menu($menu, $section); // Menu
}


if (($action == "index") || ($action == "")) {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_import_search_form($import);
  if ($set_display == "yes") {
    $display["result"] = dis_import_search_list("", $popup);
  } else {
    $display["msg"] .= display_info_msg($l_no_display);
  }

} else if ($action == "search") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = dis_import_search_form($import);
  $display["result"] = dis_import_search_list($import, $popup);

} else if ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $dsrc_q = run_query_datasource();
  $usr_q = run_query_all_users_from_group($cg_com);
  $display["detail"] = html_import_form($action, $import, "", $dsrc_q, $usr_q);

} else if ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $import_q = run_query_detail($import["id"]);
  $display["detail"] = html_import_consult($import_q);

} else if ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_detail($import["id"]);
  $dsrc_q = run_query_datasource();
  $usr_q = run_query_all_users_from_group($cg_com);
  $display["detail"] = html_import_form($action, $import, $obm_q, $dsrc_q, $usr_q);

} else if ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form("", $import)) {

    // If the context (same import) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $retour = run_query_insert($import);
      if ($retour) {
        $display["msg"] .= display_ok_msg($l_insert_ok);
      } else {
        $display["msg"] .= display_err_msg($l_insert_error);
      }
      $display["search"] = dis_import_search_form($import_q);

    // If it is the first try, we warn the user if some imports seem similar
    } else {
      $obm_q = check_import_context("", $import);
      if ($obm_q->num_rows() > 0) {
        $display["detail"] = dis_import_warn_insert($obm_q, $import);
      } else {
        $retour = run_query_insert($import);
        if ($retour) {
          $display["msg"] .= display_ok_msg($l_insert_ok);
        } else {
          $display["msg"] .= display_err_msg($l_insert_error);
        }
        $display["search"] = dis_import_search_form($import);
      }
    }

  // Form data are not valid
  } else {
    $display["msg"] .= display_warn_msg($err_msg);
    $dsrc_q = run_query_datasource();
    $usr_q = run_query_all_users_from_group($cg_com);
    $display["detail"] = html_import_form($action, $import, "", $dsrc_q, $usr_q);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($import["id"], $import)) {
    $retour = run_query_update($import);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok);
    } else {
      $display["msg"] .= display_err_msg($l_update_error);
    }
    $import_q = run_query_detail($import["id"]);
    $display["detail"] = html_import_consult($import_q);
  } else {
    $display["msg"] .= display_warn_msg($err_msg);
    $import_q = run_query_detail($import["id"]);
    $dsrc_q = run_query_datasource();
    $usr_q = run_query_all_users_from_group($cg_com);
    $display["detail"] = html_import_form($action, $import, $import_q, $dsrc_q, $usr_q);
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_warn_delete($import["id"]);

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_delete($import["id"]);
  if ($retour) {
    $display["msg"] .= display_ok_msg($l_delete_ok);
  } else {
    $display["msg"] .= display_err_msg($l_delete_error);
  }
  $display["search"] = dis_import_search_form($import);

} elseif ($action == "file_sample")  {
///////////////////////////////////////////////////////////////////////////////
  $import_q = run_query_detail($import["id"]);
  $display["detail"] = html_import_consult_file($import_q);
  $display["detail"] .= html_import_file_sample($import_q, $import, 5);

} elseif ($action == "file_test")  {
///////////////////////////////////////////////////////////////////////////////
  $import_q = run_query_detail($import["id"]);
  $display["detail"] = html_import_consult_file($import_q);
  $display["detail"] .= html_import_file_import($import_q, $import);

} elseif ($action == "file_import")  {
///////////////////////////////////////////////////////////////////////////////
  $import_q = run_query_detail($import["id"]);
  $display["detail"] = html_import_consult_file($import_q);
  $display["detail"] .= html_import_file_import($import_q, $import);

}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_list);
$display["end"] = display_end();

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Construct the Import description field from import parameters hash
// returns:
//   $impd : string with each import parameters concatenated 
///////////////////////////////////////////////////////////////////////////////
function get_import_desc($import) {

  $desc = '\$comp_name="'.$import["comp_name"] . '";';
  $desc .= '\$comp_name_d="'.$import["comp_name_d"] . '";';
  $desc .= '\$comp_num="'.$import["comp_num"] . '";';
  $desc .= '\$comp_num_d="'.$import["comp_num_d"] . '";';
  $desc .= '\$comp_cat="'.$import["comp_cat"] . '";';
  $desc .= '\$comp_cat_d="'.$import["comp_cat_d"] . '";';
  $desc .= '\$comp_ad1="'.$import["comp_ad1"] . '";';
  $desc .= '\$comp_ad1_d="'.$import["comp_ad1_d"] . '";';
  $desc .= '\$comp_ad2="'.$import["comp_ad2"] . '";';
  $desc .= '\$comp_ad2_d="'.$import["comp_ad2_d"] . '";';
  $desc .= '\$comp_ad3="'.$import["comp_ad3"] . '";';
  $desc .= '\$comp_ad3_d="'.$import["comp_ad3_d"] . '";';
  $desc .= '\$comp_zip="'.$import["comp_zip"] . '";';
  $desc .= '\$comp_zip_d="'.$import["comp_zip_d"] . '";';
  $desc .= '\$comp_town="'.$import["comp_town"] . '";';
  $desc .= '\$comp_town_d="'.$import["comp_town_d"] . '";';
  $desc .= '\$comp_ctry="'.$import["comp_ctry"] . '";';
  $desc .= '\$comp_ctry_d="'.$import["comp_ctry_d"] . '";';
  $desc .= '\$comp_pho="'.$import["comp_pho"] . '";';
  $desc .= '\$comp_pho_d="'.$import["comp_pho_d"] . '";';
  $desc .= '\$comp_fax="'.$import["comp_fax"] . '";';
  $desc .= '\$comp_fax_d="'.$import["comp_fax_d"] . '";';
  $desc .= '\$comp_web="'.$import["comp_web"] . '";';
  $desc .= '\$comp_web_d="'.$import["comp_web_d"] . '";';
  $desc .= '\$comp_mail="'.$import["comp_mail"] . '";';
  $desc .= '\$comp_mail_d="'.$import["comp_mail_d"] . '";';
  $desc .= '\$comp_com="'.$import["comp_com"] . '";';
  $desc .= '\$comp_com_d="'.$import["comp_com_d"] . '";';
  $desc .= '\$con_ln="'.$import["con_ln"] . '";';
  $desc .= '\$con_ln_d="'.$import["con_ln_d"] . '";';
  $desc .= '\$con_fn="'.$import["con_fn"] . '";';
  $desc .= '\$con_fn_d="'.$import["con_fn_d"] . '";';
  $desc .= '\$con_tit="'.$import["con_tit"] . '";';
  $desc .= '\$con_tit_d="'.$import["con_tit_d"] . '";';
  $desc .= '\$con_ad1="'.$import["con_ad1"] . '";';
  $desc .= '\$con_ad1_d="'.$import["con_ad1_d"] . '";';
  $desc .= '\$con_ad2="'.$import["con_ad2"] . '";';
  $desc .= '\$con_ad2_d="'.$import["con_ad2_d"] . '";';
  $desc .= '\$con_ad3="'.$import["con_ad3"] . '";';
  $desc .= '\$con_ad3_d="'.$import["con_ad3_d"] . '";';
  $desc .= '\$con_zip="'.$import["con_zip"] . '";';
  $desc .= '\$con_zip_d="'.$import["con_zip_d"] . '";';
  $desc .= '\$con_town="'.$import["con_town"] . '";';
  $desc .= '\$con_town_d="'.$import["con_town_d"] . '";';
  $desc .= '\$con_ctry="'.$import["con_ctry"] . '";';
  $desc .= '\$con_ctry_d="'.$import["con_ctry_d"] . '";';
  $desc .= '\$con_pho="'.$import["con_pho"] . '";';
  $desc .= '\$con_pho_d="'.$import["con_pho_d"] . '";';
  $desc .= '\$con_hpho="'.$import["con_hpho"] . '";';
  $desc .= '\$con_hpho_d="'.$import["con_hpho_d"] . '";';
  $desc .= '\$con_mpho="'.$import["con_mpho"] . '";';
  $desc .= '\$con_mpho_d="'.$import["con_mpho_d"] . '";';
  $desc .= '\$con_fax="'.$import["con_fax"] . '";';
  $desc .= '\$con_fax_d="'.$import["con_fax_d"] . '";';
  $desc .= '\$con_mail="'.$import["con_mail"] . '";';
  $desc .= '\$con_mail_d="'.$import["con_mail_d"] . '";';
  $desc .= '\$con_com="'.$import["con_com"] . '";';
  $desc .= '\$con_com_d="'.$import["con_com_d"] . '";';

  $desc .= '\$comp["comp_name"]["value"] ="'.$import["comp_name"] . '";';
  $desc .= '\$comp["comp_name"]["label"] ="l_company";';
  $desc .= '\$comp["comp_name"]["default"]="'.$import["comp_name_d"] . '";';
  $desc .= '\$comp["comp_num"]["value"] ="'.$import["comp_num"] . '";';
  $desc .= '\$comp["comp_num"]["label"] ="l_number";';
  $desc .= '\$comp["comp_num"]["default"]="'.$import["comp_num_d"] . '";';
  $desc .= '\$comp["comp_cat"]["value"] ="'.$import["comp_cat"] . '";';
  $desc .= '\$comp["comp_cat"]["label"] ="l_cat";';
  $desc .= '\$comp["comp_cat"]["default"]="'.$import["comp_cat_d"] . '";';
  $desc .= '\$comp["comp_ad1"]["value"] ="'.$import["comp_ad1"] . '";';
  $desc .= '\$comp["comp_ad1"]["label"] ="l_address";';
  $desc .= '\$comp["comp_ad1"]["default"]="'.$import["comp_ad1_d"] . '";';
  $desc .= '\$comp["comp_ad2"]["value"] ="'.$import["comp_ad2"] . '";';
  $desc .= '\$comp["comp_ad2"]["label"] ="l_address";';
  $desc .= '\$comp["comp_ad2"]["default"]="'.$import["comp_ad2_d"] . '";';
  $desc .= '\$comp["comp_ad3"]["value"] ="'.$import["comp_ad3"] . '";';
  $desc .= '\$comp["comp_ad3"]["label"] ="l_address";';
  $desc .= '\$comp["comp_ad3"]["default"]="'.$import["comp_ad3_d"] . '";';
  $desc .= '\$comp["comp_zip"]["value"] ="'.$import["comp_zip"] . '";';
  $desc .= '\$comp["comp_zip"]["label"] ="l_postcode";';
  $desc .= '\$comp["comp_zip"]["default"]="'.$import["comp_zip_d"] . '";';
  $desc .= '\$comp["comp_town"]["value"] ="'.$import["comp_town"] . '";';
  $desc .= '\$comp["comp_town"]["label"] ="l_town";';
  $desc .= '\$comp["comp_town"]["default"]="'.$import["comp_town_d"] . '";';
  $desc .= '\$comp["comp_ctry"]["value"] ="'.$import["comp_ctry"] . '";';
  $desc .= '\$comp["comp_ctry"]["label"] ="l_country";';
  $desc .= '\$comp["comp_ctry"]["default"]="'.$import["comp_ctry_d"] . '";';
  $desc .= '\$comp["comp_pho"]["value"] ="'.$import["comp_pho"] . '";';
  $desc .= '\$comp["comp_pho"]["label"] ="l_phone";';
  $desc .= '\$comp["comp_pho"]["default"]="'.$import["comp_pho_d"] . '";';
  $desc .= '\$comp["comp_fax"]["value"] ="'.$import["comp_fax"] . '";';
  $desc .= '\$comp["comp_fax"]["label"] ="l_fax";';
  $desc .= '\$comp["comp_fax"]["default"]="'.$import["comp_fax_d"] . '";';
  $desc .= '\$comp["comp_web"]["value"] ="'.$import["comp_web"] . '";';
  $desc .= '\$comp["comp_web"]["label"] ="l_web";';
  $desc .= '\$comp["comp_web"]["default"]="'.$import["comp_web_d"] . '";';
  $desc .= '\$comp["comp_mail"]["value"] ="'.$import["comp_mail"] . '";';
  $desc .= '\$comp["comp_mail"]["label"] ="l_email";';
  $desc .= '\$comp["comp_mail"]["default"]="'.$import["comp_mail_d"] . '";';
  $desc .= '\$comp["comp_com"]["value"] ="'.$import["comp_com"] . '";';
  $desc .= '\$comp["comp_com"]["label"] ="l_comment";';
  $desc .= '\$comp["comp_com"]["default"]="'.$import["comp_com_d"] . '";';

  $desc .= '\$con["con_ln"]["value"] ="'.$import["con_ln"] . '";';
  $desc .= '\$con["con_ln"]["label"] ="l_lastname";';
  $desc .= '\$con["con_ln"]["default"]="'.$import["con_ln_d"] . '";';
  $desc .= '\$con["con_fn"]["value"] ="'.$import["con_fn"] . '";';
  $desc .= '\$con["con_fn"]["label"] ="l_firstname";';
  $desc .= '\$con["con_fn"]["default"]="'.$import["con_fn_d"] . '";';
  $desc .= '\$con["con_tit"]["value"] ="'.$import["con_tit"] . '";';
  $desc .= '\$con["con_tit"]["label"] ="l_title";';
  $desc .= '\$con["con_tit"]["default"]="'.$import["con_tit_d"] . '";';
  $desc .= '\$con["con_ad1"]["value"] ="'.$import["con_ad1"] . '";';
  $desc .= '\$con["con_ad1"]["label"] ="l_address";';
  $desc .= '\$con["con_ad1"]["default"]="'.$import["con_ad1_d"] . '";';
  $desc .= '\$con["con_ad2"]["value"] ="'.$import["con_ad2"] . '";';
  $desc .= '\$con["con_ad2"]["label"] ="l_address";';
  $desc .= '\$con["con_ad2"]["default"]="'.$import["con_ad2_d"] . '";';
  $desc .= '\$con["con_ad3"]["value"] ="'.$import["con_ad3"] . '";';
  $desc .= '\$con["con_ad3"]["label"] ="l_address";';
  $desc .= '\$con["con_ad3"]["default"]="'.$import["con_ad3_d"] . '";';
  $desc .= '\$con["con_zip"]["value"] ="'.$import["con_zip"] . '";';
  $desc .= '\$con["con_zip"]["label"] ="l_postcode";';
  $desc .= '\$con["con_zip"]["default"]="'.$import["con_zip_d"] . '";';
  $desc .= '\$con["con_town"]["value"] ="'.$import["con_town"] . '";';
  $desc .= '\$con["con_town"]["label"] ="l_town";';
  $desc .= '\$con["con_town"]["default"]="'.$import["con_town_d"] . '";';
  $desc .= '\$con["con_ctry"]["value"] ="'.$import["con_ctry"] . '";';
  $desc .= '\$con["con_ctry"]["label"] ="l_country";';
  $desc .= '\$con["con_ctry"]["default"]="'.$import["con_ctry_d"] . '";';
  $desc .= '\$con["con_pho"]["value"] ="'.$import["con_pho"] . '";';
  $desc .= '\$con["con_pho"]["label"] ="l_phone";';
  $desc .= '\$con["con_pho"]["default"]="'.$import["con_pho_d"] . '";';
  $desc .= '\$con["con_hpho"]["value"] ="'.$import["con_hpho"] . '";';
  $desc .= '\$con["con_hpho"]["label"] ="l_hphone";';
  $desc .= '\$con["con_hpho"]["default"]="'.$import["con_hpho_d"] . '";';
  $desc .= '\$con["con_mpho"]["value"] ="'.$import["con_mpho"] . '";';
  $desc .= '\$con["con_mpho"]["label"] ="l_mphone";';
  $desc .= '\$con["con_mpho"]["default"]="'.$import["con_mpho_d"] . '";';
  $desc .= '\$con["con_fax"]["value"] ="'.$import["con_fax"] . '";';
  $desc .= '\$con["con_fax"]["label"] ="l_fax";';
  $desc .= '\$con["con_fax"]["default"]="'.$import["con_fax_d"] . '";';
  $desc .= '\$con["con_mail"]["value"] ="'.$import["con_mail"] . '";';
  $desc .= '\$con["con_mail"]["label"] ="l_email";';
  $desc .= '\$con["con_mail"]["default"]="'.$import["con_mail_d"] . '";';
  $desc .= '\$con["con_com"]["value"] ="'.$import["con_com"] . '";';
  $desc .= '\$con["con_com"]["label"] ="l_comment";';
  $desc .= '\$con["con_com"]["default"]="'.$import["con_com_d"] . '";';
  
  return $desc;
}


///////////////////////////////////////////////////////////////////////////////
// Stores in $list hash, List parameters transmited
// returns : $list hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_import() {
  global $tf_name, $sel_dsrc, $sel_market, $rd_sep, $tf_enclosed,$cb_auto_mode;
  global $param_import, $cdg_param;
  global $tmp_path, $action, $new_order, $order_dir;
  global $tf_comp_name, $tf_comp_name_d, $tf_comp_num, $tf_comp_num_d;
  global $tf_comp_cat, $tf_comp_cat_d;
  global $tf_comp_ad1, $tf_comp_ad1_d, $tf_comp_ad2, $tf_comp_ad2_d;
  global $tf_comp_ad3, $tf_comp_ad3_d, $tf_comp_zip, $tf_comp_zip_d;
  global $tf_comp_town, $tf_comp_town_d, $tf_comp_ctry, $tf_comp_ctry_d;
  global $tf_comp_pho, $tf_comp_pho_d, $tf_comp_fax, $tf_comp_fax_d;
  global $tf_comp_web, $tf_comp_web_d, $tf_comp_mail, $tf_comp_mail_d;
  global $tf_comp_com, $tf_comp_com_d;
  global $tf_con_ln, $tf_con_ln_d, $tf_con_fn, $tf_con_fn_d;
  global $tf_con_tit, $tf_con_tit_d;
  global $tf_con_ad1, $tf_con_ad1_d, $tf_con_ad2, $tf_con_ad2_d;
  global $tf_con_ad3, $tf_con_ad3_d, $tf_con_zip, $tf_con_zip_d;
  global $tf_con_town, $tf_con_town_d, $tf_con_ctry, $tf_con_ctry_d;
  global $tf_con_pho, $tf_con_pho_d, $tf_con_hpho, $tf_con_hpho_d;
  global $tf_con_mpho, $tf_con_mpho_d, $tf_con_fax, $tf_con_fax_d;
  global $tf_con_mail, $tf_con_mail_d, $tf_con_com, $tf_con_com_d;
  global $sample_file, $sample_file_name, $sample_file_size;
  global $test_file, $test_file_name, $test_file_size;
  global $import_file, $import_file_name, $import_file_size, $file_saved;
  global $row_start, $rd_conflict, $company_id, $contact_id;

  // Import fields
  if (isset ($param_import)) $import["id"] = $param_import;
  if (isset ($tf_name)) $import["name"] = trim($tf_name);
  if (isset ($sel_dsrc)) $import["datasource"] = $sel_dsrc;
  if (isset ($sel_market)) $import["market"] = $sel_market;
  if (isset ($rd_sep)) $import["sep"] = $rd_sep;
  if (isset ($tf_enclosed)) $import["enclosed"] = $tf_enclosed;

  if (isset ($row_start)) $import["row_start"] = $row_start;
  if (isset ($rd_conflict)) $import["conflict_action"] = $rd_conflict;
  if (isset ($company_id)) $import["company_id"] = $company_id;
  if (isset ($contact_id)) $import["contact_id"] = $contact_id;

  // Mapping : company
  if (isset ($tf_comp_name)) $import["comp_name"] = trim($tf_comp_name);
  if (isset ($tf_comp_name_d)) $import["comp_name_d"] = trim($tf_comp_name_d);
  if (isset ($tf_comp_num)) $import["comp_num"] = trim($tf_comp_num);
  if (isset ($tf_comp_num_d)) $import["comp_num_d"] = trim($tf_comp_num_d);
  if (isset ($tf_comp_cat)) $import["comp_cat"] = trim($tf_comp_cat);
  if (isset ($tf_comp_cat_d)) $import["comp_cat_d"] = trim($tf_comp_cat_d);
  if (isset ($tf_comp_ad1)) $import["comp_ad1"] = trim($tf_comp_ad1);
  if (isset ($tf_comp_ad1_d)) $import["comp_ad1_d"] = trim($tf_comp_ad1_d);
  if (isset ($tf_comp_ad2)) $import["comp_ad2"] = trim($tf_comp_ad2);
  if (isset ($tf_comp_ad2_d)) $import["comp_ad2_d"] = trim($tf_comp_ad2_d);
  if (isset ($tf_comp_ad3)) $import["comp_ad3"] = trim($tf_comp_ad3);
  if (isset ($tf_comp_ad3_d)) $import["comp_ad3_d"] = trim($tf_comp_ad3_d);
  if (isset ($tf_comp_zip)) $import["comp_zip"] = trim($tf_comp_zip);
  if (isset ($tf_comp_zip_d)) $import["comp_zip_d"] = trim($tf_comp_zip_d);
  if (isset ($tf_comp_town)) $import["comp_town"] = trim($tf_comp_town);
  if (isset ($tf_comp_town_d)) $import["comp_town_d"] = trim($tf_comp_town_d);
  if (isset ($tf_comp_ctry)) $import["comp_ctry"] = trim($tf_comp_ctry);
  if (isset ($tf_comp_ctry_d)) $import["comp_ctry_d"] = trim($tf_comp_ctry_d);
  if (isset ($tf_comp_pho)) $import["comp_pho"] = trim($tf_comp_pho);
  if (isset ($tf_comp_pho_d)) $import["comp_pho_d"] = trim($tf_comp_pho_d);
  if (isset ($tf_comp_fax)) $import["comp_fax"] = trim($tf_comp_fax);
  if (isset ($tf_comp_fax_d)) $import["comp_fax_d"] = trim($tf_comp_fax_d);
  if (isset ($tf_comp_web)) $import["comp_web"] = trim($tf_comp_web);
  if (isset ($tf_comp_web_d)) $import["comp_web_d"] = trim($tf_comp_web_d);
  if (isset ($tf_comp_mail)) $import["comp_mail"] = trim($tf_comp_mail);
  if (isset ($tf_comp_mail_d)) $import["comp_mail_d"] = trim($tf_comp_mail_d);
  if (isset ($tf_comp_com)) $import["comp_com"] = trim($tf_comp_com);
  if (isset ($tf_comp_com_d)) $import["comp_com_d"] = trim($tf_comp_com_d);
  // Mapping : contact
  if (isset ($tf_con_ln)) $import["con_ln"] = trim($tf_con_ln);
  if (isset ($tf_con_ln_d)) $import["con_ln_d"] = trim($tf_con_ln_d);
  if (isset ($tf_con_fn)) $import["con_fn"] = trim($tf_con_fn);
  if (isset ($tf_con_fn_d)) $import["con_fn_d"] = trim($tf_con_fn_d);
  if (isset ($tf_con_tit)) $import["con_tit"] = trim($tf_con_tit);
  if (isset ($tf_con_tit_d)) $import["con_tit_d"] = trim($tf_con_tit_d);
  if (isset ($tf_con_ad1)) $import["con_ad1"] = trim($tf_con_ad1);
  if (isset ($tf_con_ad1_d)) $import["con_ad1_d"] = trim($tf_con_ad1_d);
  if (isset ($tf_con_ad2)) $import["con_ad2"] = trim($tf_con_ad2);
  if (isset ($tf_con_ad2_d)) $import["con_ad2_d"] = trim($tf_con_ad2_d);
  if (isset ($tf_con_ad3)) $import["con_ad3"] = trim($tf_con_ad3);
  if (isset ($tf_con_ad3_d)) $import["con_ad3_d"] = trim($tf_con_ad3_d);
  if (isset ($tf_con_zip)) $import["con_zip"] = trim($tf_con_zip);
  if (isset ($tf_con_zip_d)) $import["con_zip_d"] = trim($tf_con_zip_d);
  if (isset ($tf_con_town)) $import["con_town"] = trim($tf_con_town);
  if (isset ($tf_con_town_d)) $import["con_town_d"] = trim($tf_con_town_d);
  if (isset ($tf_con_ctry)) $import["con_ctry"] = trim($tf_con_ctry);
  if (isset ($tf_con_ctry_d)) $import["con_ctry_d"] = trim($tf_con_ctry_d);
  if (isset ($tf_con_pho)) $import["con_pho"] = trim($tf_con_pho);
  if (isset ($tf_con_pho_d)) $import["con_pho_d"] = trim($tf_con_pho_d);
  if (isset ($tf_con_hpho)) $import["con_hpho"] = trim($tf_con_hpho);
  if (isset ($tf_con_hpho_d)) $import["con_hpho_d"] = trim($tf_con_hpho_d);
  if (isset ($tf_con_mpho)) $import["con_mpho"] = trim($tf_con_mpho);
  if (isset ($tf_con_mpho_d)) $import["con_mpho_d"] = trim($tf_con_mpho_d);
  if (isset ($tf_con_fax)) $import["con_fax"] = trim($tf_con_fax);
  if (isset ($tf_con_fax_d)) $import["con_fax_d"] = trim($tf_con_fax_d);
  if (isset ($tf_con_mail)) $import["con_mail"] = trim($tf_con_mail);
  if (isset ($tf_con_mail_d)) $import["con_mail_d"] = trim($tf_con_mail_d);
  if (isset ($tf_con_com)) $import["con_com"] = trim($tf_con_com);
  if (isset ($tf_con_com_d)) $import["con_com_d"] = trim($tf_con_com_d);

  // File
  if (isset ($sample_file)) $import["file"] = $sample_file;
  if (isset ($sample_file_name)) $import["file_name"] = $sample_file_name;
  if (isset ($sample_file_size)) $import["file_size"] = $sample_file_size;

  if (isset ($test_file)) $import["file"] = $test_file;
  if (isset ($test_file_name)) $import["file_name"] = $test_file_name;
  if (isset ($test_file_size)) $import["file_size"] = $test_file_size;

  if (isset ($import_file)) $import["file"] = $import_file;
  if (isset ($import_file_name)) $import["file_name"] = $import_file_name;
  if (isset ($import_file_size)) $import["file_size"] = $import_file_size;
  if (isset ($file_saved)) $import["file_saved"] = $file_saved;

  if (($action == "file_import")  && ($import["file"] != "")
      && ($import["file_saved"] == "")) {
    $file_saved = $tmp_path . '/' . $import["file_name"];
    copy ($import["file"], $file_saved);
    $import["file_saved"] = $file_saved;
  }

  // Run mode
  if ($action == "file_test") {
    $import["run_mode"] = "test";
  } else if ($action == "file_import") {
    if (isset ($cb_auto_mode)) {
      $import["run_mode"] = "auto";
    } else {
      $import["run_mode"] = "run";
    }
  }

  if (debug_level_isset($cdg_param)) {
    echo "action=$action";
    if ( $import ) {
      while ( list( $key, $val ) = each( $import ) ) {
        echo "<br />import[$key]=$val";
      }
    }
  }

  return $import;
}


//////////////////////////////////////////////////////////////////////////////
// Import actions
//////////////////////////////////////////////////////////////////////////////
function get_import_action() {
  global $import, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_import,$l_header_display;
  global $l_header_consult, $l_header_add_contact;
  global $l_select_list, $l_add_contact;
  global $cright_read_admin, $cright_write_admin;

// Index
  $actions["IMPORT"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/import/import_index.php?action=index",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                    );

// Search
  $actions["IMPORT"]["search"] = array (
    'Url'      => "$path/import/import_index.php?action=search",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('None') 
                                      );

// New
  $actions["IMPORT"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/import/import_index.php?action=new",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('','search','index','detailconsult','admin','display') 
                                  );
// Detail Consult
  $actions["IMPORT"]["detailconsult"] = array (
     'Name'     => $l_header_consult,
     'Url'      => "$path/import/import_index.php?action=detailconsult&amp;param_import=".$import["id"]."",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('detailupdate', 'file_sample', 'file_test', 'file_import') 
                                      );

// Detail Update
  $actions["IMPORT"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/import/import_index.php?action=detailupdate&amp;param_import=".$import["id"]."",
     'Right'    => $cright_write_admin,
     'Condition'=> array ('detailconsult', 'update', 'file_sample', 'file_test') 
                                           );

// Insert
  $actions["IMPORT"]["insert"] = array (
    'Url'      => "$path/import/import_index.php?action=insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                      );

// Update
  $actions["IMPORT"]["update"] = array (
    'Url'      => "$path/import/import_index.php?action=update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                      );

// Check Delete
  $actions["IMPORT"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/import/import_index.php?action=check_delete&amp;param_import=".$import["id"]."",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'update') 
                                           );

// Delete
  $actions["IMPORT"]["delete"] = array (
    'Url'      => "$path/import/import_index.php?action=delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                      );

// Sample File
  $actions["IMPORT"]["file_sample"] = array (
    'Url'      => "$path/import/import_index.php?action=file_sample&amp;param_import=".$import["id"]."",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult') 
                                      );

// Test File
  $actions["IMPORT"]["file_test"] = array (
    'Url'      => "$path/import/import_index.php?action=file_test&amp;param_import=".$import["id"]."",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult') 
                                      );

// Import File
  $actions["IMPORT"]["file_import"] = array (
    'Url'      => "$path/import/import_index.php?action=file_import&amp;param_import=".$import["id"]."",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult') 
                                      );





// Display
  $actions["LIST"]["display"] = array (
   'Name'     => $l_header_display,
   'Url'      => "$path/list/list_index.php?action=display",
   'Right'    => $list_read,
   'Condition'=> array ('all') 
                                      );

// Display Préférence
  $actions["LIST"]["dispref_display"] = array (
   'Url'      => "$path/list/list_index.php?action=dispref_display",
   'Right'    => $cright_write_admin,
   'Condition'=> array ('None') 
                                               );

// Display level
  $actions["LIST"]["dispref_level"] = array (
   'Url'      => "$path/list/list_index.php?action=dispref_level",
   'Right'    => $cright_write_admin,
   'Condition'=> array ('None') 
                                            );


}

</script>
