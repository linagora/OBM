<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : invoice_index.inc
//     - Desc : Invoice Main file
// 2001-07-30 Aliacom - Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default)    -- search fields  -- show the invoice search form
// - search             -- search fields  -- show the result set of search
// - new                -- $params        -- show the new invoice form
// - detailconsult      -- $param_invoice -- show the invoice detail
// - detailupdate       -- $param_invoice -- show the invoice detail form
// - duplicate          -- $invoice       -- new invoice form from existing one
// - insert             -- form fields    -- insert the invoice
// - update             -- form fields    -- update the invoice
// - check_delete       -- $param_invoice -- check links before delete
// - delete             -- $param_invoice -- delete the invoice
// - display            --                -- display and set display parameters
// - dispref_display    --                -- update one field display value
// - dispref_level      --                -- update one field display position 
// - document_add       -- $invoice sess  -- link documents to an invoice
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "invoice";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("invoice_display.inc");
require("invoice_query.inc");

$uid = $auth->auth["uid"];

if ($action == "") $action = "index";
$invoice = get_param_invoice();
get_invoice_action();
$perm->check_permissions($module, $action);

update_last_visit("invoice", $invoice["id"], $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main program
///////////////////////////////////////////////////////////////////////////////

if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  require("invoice_js.inc");
  $display["search"] = dis_invoice_search_form($invoice); 
  if ($set_display == "yes") { 
    $display["result"] = dis_invoice_search_list($invoice);
  } else { 
    $display["msg"] .= display_ok_msg($l_no_display); 
  } 

} elseif ($action == "search")  { 
///////////////////////////////////////////////////////////////////////////////
  require("invoice_js.inc");
  $display["search"] = dis_invoice_search_form($invoice); 
  $display["result"] = dis_invoice_search_list($invoice);
  
} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  require("invoice_js.inc"); 
  $display["detail"] = dis_invoice_form($action, $invoice);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_invoice_consult($invoice);

} elseif ($action == "detailupdate")  { 
///////////////////////////////////////////////////////////////////////////////
  require("invoice_js.inc");
  $display["detail"] = dis_invoice_form($action, $invoice);

} elseif ($action == "duplicate") {
///////////////////////////////////////////////////////////////////////////////
  // we give the user the traditionnal form to modify this invoice :
  require("invoice_js.inc"); 
  $display["detail"] = dis_invoice_form($action, $invoice);
  
} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  require("invoice_js.inc");
  if (check_invoice_data_form("", $invoice)) {
    $retour = run_query_invoice_insert($invoice);
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_insert_ok);
    } else {
      $display["msg"] .= display_err_msg($l_insert_error);
    }
    $display["search"] = dis_invoice_search_form($invoice);
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_invoice_form($action, $invoice);
  }
  
} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  require("invoice_js.inc"); 
  if (check_invoice_data_form($invoice["id"], $invoice)) {
    $retour = run_query_invoice_update($invoice); 
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_update_ok); 
    } else {
      $display["msg"] .= display_ok_msg($l_update_error); 
    }
    $display["detail"] = dis_invoice_consult($invoice);
  } else {
    $display["msg"] .= display_err_msg($l_invalid_data . " : " . $err_msg);
    $display["search"] = dis_invoice_form($action, $invoice); 
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_invoice($invoice["id"])) {
    require("invoice_js.inc");
    $display["msg"] .= display_info_msg($ok_msg, false);
    $display["detail"] = dis_can_delete_invoice($invoice["id"]);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_invoice_consult($invoice);
  }
  //  $display["detail"] = dis_check_invoice_links($invoice["id"]);

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_invoice($invoice["id"])) {
    $retour = run_query_invoice_delete($invoice["id"]); 
    if ($retour) {
      $display["msg"] .= display_ok_msg($l_delete_ok);
      require ("invoice_js.inc");
      $display["search"] = dis_invoice_search_form($invoice);
    } else {
      $display["msg"] .= display_err_msg ($l_delete_error);
      $display["detail"] = dis_invoice_consult($invoice);
    }
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_invoice_consult($invoice);
  }

} elseif ($action == "dashboard")  {
///////////////////////////////////////////////////////////////////////////////
  include_once("$obminclude/Artichow/BarPlot.class.php");
  //  include("$obminclude/libchart/libchart/libchart.php");
  $display["detail"] = dis_invoice_dashboard($invoice);

} elseif ($action == "document_add")  {
///////////////////////////////////////////////////////////////////////////////
  if ($invoice["doc_nb"] > 0) {
    $nb = run_query_insert_documents($invoice, "invoice");
    $display["msg"] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display["msg"] .= display_err_msg($l_no_document_added);
  }
  $display["detail"] = dis_invoice_consult($invoice);

} elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($uid, "invoice", 1);
  $display["detail"] = dis_invoice_display_pref($prefs);
  
} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($uid, "invoice", 1);
  $display["detail"] = dis_invoice_display_pref($prefs);
  
} else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($uid, "invoice", 1);
  $display["detail"] = dis_invoice_display_pref($prefs);
}
  

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head("$l_invoice");
update_invoice_action();
$display["header"] = display_menu($module);
$display["end"] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Invoice parameters transmitted in $invoice hash
// returns : $invoice hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_invoice() {
  global $year, $tf_label, $tf_number, $tf_amount_ht, $tf_amount_ttc;
  global $ta_comment, $sel_status, $param_invoice, $tf_date, $tf_payment_date;
  global $tf_expiration_date, $tf_date_after, $tf_date_before;
  global $tf_date_exp_after, $tf_date_exp_before;
  global $tf_deal, $tf_company, $cb_archive, $rd_inout, $hd_inout;
  global $ta_comment, $sel_usercomment, $tf_datecomment, $ta_add_comment;
  global $param_company, $company_name, $company_new_name, $company_new_id;
  global $param_deal, $deal_label, $deal_new_label, $deal_new_id;
  global $param_project, $project_name, $project_new_name, $project_new_id;
  global $ext_id;

  get_global_param_document($invoice);

  if (isset ($param_invoice)) $invoice["id"] = $param_invoice;
  if (isset ($tf_label)) $invoice["label"] = $tf_label;
  if (isset ($tf_number)) $invoice["number"] = $tf_number;
  if (isset ($tf_amount_ht)) $invoice["ht"] = $tf_amount_ht;
  if (isset ($tf_amount_ttc)) $invoice["ttc"] = $tf_amount_ttc;
  if (isset ($sel_status)) $invoice["status"] = $sel_status;
  if (isset ($tf_date)) $invoice["date"] = $tf_date;
  if (isset ($tf_payment_date)) $invoice["pdate"] = $tf_payment_date;
  if (isset ($tf_expiration_date)) $invoice["edate"] = $tf_expiration_date;
  if (isset ($tf_date_after)) $invoice["date_after"] = $tf_date_after;
  if (isset ($tf_date_before)) $invoice["date_before"] = $tf_date_before;
  if (isset ($tf_date_exp_after)) $invoice["dateexp_after"] = $tf_date_exp_after;
  if (isset ($tf_date_exp_before)) $invoice["dateexp_before"] = $tf_date_exp_before;
  if (isset ($rd_inout)) $invoice["inout"] = $rd_inout;
  if (isset ($hd_inout)) $invoice["inout"] = $hd_inout;
  if (isset ($tf_balance)) $invoice["balance"] = $tf_balance;
  if (isset ($tf_bank)) $invoice["bank"] = $tf_bank;
  if (isset ($ta_comment)) $invoice["comment"] = $ta_comment;
  if (isset ($tf_datecomment)) $invoice["datecomment"] = $tf_datecomment;
  if (isset ($sel_usercomment)) $invoice["usercomment"] = $sel_usercomment;
  if (isset ($ta_add_comment)) $invoice["add_comment"] = trim($ta_add_comment);
  if (isset ($tf_deal)) $invoice["deal"] = $tf_deal;
  if (isset ($cb_archive)) $invoice["archive"] = $cb_archive;
  if (isset ($year)) $invoice["year"] = $year;

  // Company params
  if (isset ($param_company)) $invoice["company_id"] = $param_company;
  if (isset ($tf_company)) $invoice["company"] = $tf_company;
  if (isset ($company_name)) $invoice["company_name"] = $company_name;
  if (isset ($company_new_name)) $invoice["comp_new_name"] = $company_new_name;
  if (isset ($company_new_id)) $invoice["comp_new_id"] = $company_new_id;

  // Deal params
  if (isset ($param_deal)) $invoice["deal_id"]= $param_deal; 
  if (isset ($deal_label)) $invoice["deal_label"] = $deal_label;
  if (isset ($deal_new_label)) $invoice["deal_new_label"] = $deal_new_label;
  if (isset ($deal_new_id)) $invoice["deal_new_id"] = $deal_new_id;

  // Project params
  if (isset ($param_project)) $invoice["project_id"]= $param_project; 
  if (isset ($project_name)) $invoice["project_name"] = $project_name;
  if (isset ($project_new_name)) $invoice["proj_new_name"] = $project_new_name;
  if (isset ($project_new_id)) $invoice["proj_new_id"] = $project_new_id;

  // External parameters
  if (isset ($ext_id)) $invoice["id"] = $ext_id;

  display_debug_param($invoice);

  return $invoice;
}


///////////////////////////////////////////////////////////////////////////////
// Invoice actions
///////////////////////////////////////////////////////////////////////////////
function get_invoice_action() {
  global $invoice, $actions, $path;
  global $l_header_find,$l_header_new_f,$l_header_update,$l_header_delete;
  global $l_header_consult, $l_header_display,$l_header_duplicate,$l_header_admin;
  global $l_header_add_deal, $l_header_dashboard, $invoice_admin_write;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index 
  $actions["invoice"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/invoice/invoice_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                       );

// Search
  $actions["invoice"]["search"] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                   );

// New
  $actions["invoice"]["new"] = array (
    'Name'     => $l_header_new_f,
    'Url'      => "$path/invoice/invoice_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('','search','index','detailconsult','insert', 'update','delete','display')
                                   );

//Insert
  $actions["invoice"]["insert"] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                   );

// Detail Consult
  $actions["invoice"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/invoice/invoice_index.php?action=detailconsult&amp;param_invoice=".$invoice["id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailconsult', 'detailupdate', 'duplicate', 'update')
                                   );

// Duplicate
  $actions["invoice"]["duplicate"] = array (
    'Name'     => $l_header_duplicate,
    'Url'      => "$path/invoice/invoice_index.php?action=duplicate&amp;param_invoice=".$invoice["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update')
                                     	   );

// Detail Update
  $actions["invoice"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/invoice/invoice_index.php?action=detailupdate&amp;param_invoice=".$invoice["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update')
                                     	       );

// Update
  $actions["invoice"]["update"] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                        );

// Check Delete
  $actions["invoice"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/invoice/invoice_index.php?action=check_delete&amp;param_invoice=".$invoice["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update')
                                     	      );

// Delete
  $actions["invoice"]["delete"] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=delete&amp;param_invoice=".$invoice["id"],
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     	);

// Dashboard
  $actions["invoice"]["dashboard"] = array (
    'Name'     => $l_header_dashboard,
    'Url'      => "$path/invoice/invoice_index.php?action=dashboard",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all')
                                        );

// Display
  $actions["invoice"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/invoice/invoice_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                        );

// Display Preferences
  $actions["invoice"]["dispref_display"] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                        );

// Display Préférences
  $actions["invoice"]["dispref_level"] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                        );

// Document add
  $actions["invoice"]["document_add"] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
  );     

}


///////////////////////////////////////////////////////////////////////////////
// Invoice Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_invoice_action() {
  global $invoice, $actions, $path;

  $id = $invoice["id"];
  if ($id > 0) {
    // Detail Consult
    $actions["invoice"]["detailconsult"]["Url"] = "$path/invoice/invoice_index.php?action=detailconsult&amp;param_invoice=$id";
    $actions["invoice"]["detailconsult"]['Condition'][] = 'insert';

    // Detail Update
    $actions["invoice"]["detailupdate"]['Url'] = "$path/invoice/invoice_index.php?action=detailupdate&amp;param_invoice=$id";
    $actions["invoice"]["detailupdate"]['Condition'][] = 'insert';

    // Duplicate
    $actions["invoice"]["duplicate"]['Url'] = "$path/invoice/invoice_index.php?action=duplicate&amp;param_invoice=$id";
    $actions["invoice"]["duplicate"]['Condition'][] = 'insert';

    // Check Delete
    $actions["invoice"]["check_delete"]['Url'] = "$path/invoice/invoice_index.php?action=check_delete&amp;param_invoice=$id";
    $actions["invoice"]["check_delete"]['Condition'][] = 'insert';
  }
}

</script>
