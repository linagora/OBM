<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : deal.inc                                                 //
// 1999-01-26 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_date_init = "Init Date";
$l_parentdeal = "Parent Deal";
$l_kind = "Kind";
$l_marketing_manager = "Marketing manager";
$l_technical_manager = "Technical manager";
$l_amount = "Amount";
$l_hitrate = "Hit Rate";
$l_commission = "Commission";
$l_commission_p = "% Commission";
$l_commission_balanced = "Commission balanced";
$l_expected = "Expected";
$l_quarter = "Q";
$l_sale_delay = "Sale delay";
$l_date_comment = "Comment Date";
$l_todo = "To Do";
$l_category = "Category";
$l_code = "Code";
$l_add_cats = "Add these categories";

// Labels and texts
$l_infos = "Informations";
$l_minus = "-";
$l_plus = "+";
$l_order = "Order";
$l_amount_in = "Amout of inputs";
$l_amount_out = "Amout of outputs";
$l_potential_balanced = "Weighted Potential";
$l_label_start = "Label start";
$l_proposal = "Proposal";
$l_show_archive = "Show archives";
$l_deal_select_company = "Select the company which whom the deal must be filled";
$l_deal_select_parentdeal = "Select the parentdeal to which the deal must be affected";
$l_affect_deal= "Change Parent Deal";
$l_no_invoice = "No invoice connected";
$l_kind_manage = "Deal Kind Management";
$l_kind_exist = "Existing Kinds";
$l_kind_new = "New Kind";
$l_status_manage = "Deal Status Management";
$l_status_exist = "Existing Status";
$l_status_new = "New Status";
$l_cat_manage = "Category management";
$l_cat_exist = "Existing Category";
$l_cat_new = "New category";
$l_parentdeal_deals = "Deals belonging to this parentdeal";
$l_forecast = "Forecast";

// Actions
$l_select_deal = "Select a Deal";
$l_insert = "Insert the Deal";
$l_update = "Update the Deal";
$l_delete = "Delete the Deal";
$l_select_company = "Select the Company";
$l_affect = "Affect";

$l_insert_parent = "Insert the ParentDeal";
$l_update_parent = "Update the ParentDeal";
$l_delete_parent = "Delete the ParentDeal";

$l_kind_checkdelete = "Check or Delete Kind";
$l_kind_delete = "Delete Kind";
$l_kind_update = "Update Kind";
$l_kind_insert = "Insert Kind";
$l_status_update = "Update Status";
$l_status_checkdelete = "Check or Delete Status";
$l_status_delete = "Delete Status";
$l_status_insert = "Insert Status";
$l_cat_delete = "Delete";
$l_cat_checkdelete = "Check or delete Category"; 
$l_cat_update = "Update category"; 
$l_cat_insert = "Insert a category";

// Messages
$l_link_contract = "The deal is involved in some contracts";
$l_link_contract_no = "The deal isn't involved in any contracts";
$l_link_project = "The deal is involved in some projects";
$l_link_project_no = "The deal isn't involved in any projects";
$l_link_invoice = "The deal is involved in some invoices";
$l_link_invoice_no = "The deal isn't involved in any invoices";
$l_can_delete = "The deal can be deleted.";
$l_cant_delete = "The deal can't be deleted !";

$l_link_deal = "Deals are attached to this ParentDeal";
$l_cant_delete_parent = "The parent deal can't be deleted !";

$l_err_label_empty = "You must fill the Parentdeal label !";
$l_err_kind_empty = "You must fill the deal kind !";
$l_err_task_empty = "You must fill the deal category !";
$l_err_market_empty = "You must fill the marketing manager !";
$l_err_tech_empty = "You must fill the technical manager !";
$l_err_hitrate = "Hit rate must be in the range 0 - 100 !";
$l_updateaffect_ok = "The deal has been successfully affected to a parentdeal";
$l_no_display = "Fill the search fields and press < Find > to retrieve the deals";
$l_found = "deals found.";
$l_no_found = "No deal found matching the query !";
$l_found_parent = "parentdeals found";
$l_no_found_parent ="No parentdeal found matching the query !";

$l_delete_ok = "The deal has been deleted successfully.";
$l_delete_error = "The deletion of the deal has failed !";
$l_insert_ok = "The deal has been inserted successfully.";
$l_insert_error = "The insertion of the deal has failed !";
$l_update_ok = "The deal has been updated successfully.";
$l_update_error = "The update of the deal has failed !";
$l_query_error = "Probl&egrave;me en executant la requete";
$l_no_display_parent ="Fill the search fields and press < Find > to retrieve the parentdeals"; 

$l_kind_insert_ok = "The kind has been inserted successfully.";
$l_kind_insert_error = "The insertion of the kind has failed !";
$l_kind_update_ok = "The kind has been updated successfully.";
$l_kind_update_error = "The kind of the deal has failed !";
$l_kind_delete_error = "The deletion of the kind has failed !";
$l_kind_delete_ok = "The kind has been deleted successfully.";
$l_kind_can_delete = "The Kind can be deleted !";
$l_kind_cant_delete = "The Kind can't be deleted !";
$l_kind_link_deal = "The Kind is involved in these deals";
$l_kind_link_deal_no = "The Kind is not involved in any deal";
$l_status_insert_ok = "The status has been inserted successfully.";
$l_status_insert_error = "The insertion of the status has failed !";
$l_status_update_ok = "The Status has been updated successfully.";
$l_status_update_error = "The status of the deal has failed !";
$l_status_delete_error = "The deletion of the status has failed !";
$l_status_delete_ok = "The status has been deleted successfully.";
$l_status_can_delete = "The Status can be deleted";
$l_status_cant_delete = "The Status can't be deleted";
$l_status_link_deal = "The Status is involved in these deals";
$l_status_link_deal_no = "The Status is not involved in any deals";

$l_cat_insert_ok = "The category has been inserted successfully.";
$l_cat_insert_error = "The insertion of the category has failed !";
$l_cat_delete_ok = "The category has been deleted successfully.";
$l_cat_delete_error = "The deletion of the category has failed !";
$l_cat_update_ok = "The category has been updated successfully.";
$l_cat_update_error = "The category has been updated has failed !";
$l_cat_link_deal = "These deals are referenced as";
$l_cat_link_deal_no = "No deals referenced as";
$l_cat_cant_delete = "The category can't be deleted !";
$l_cat_can_delete = "The category can be deleted.";     
$l_cat_no = "No categories !";

$l_help_deal_forecast = "Forecast take into account deals with these criteria : deal not in an archived state, with an amount estimated and with a hitrate > 0 and < 100.";

// JavaScript Messages (characters like \ have to be backslashed -> \\)
$l_fill_parentdeal = "You have to select a parentdeal before submitting.";
$l_fill_label = "You have to fill the Label before submitting.";
$l_fill_number = "You have to fill the Number before submitting.";
$l_fill_datealarm = "You have to fill the Alarm date before submitting.";
$l_fill_datebegin = "You have to fill the Begin date before submitting.";
$l_confirm_datealarm = "Date Alarm is in the past ! Confirm the action ?";
$l_j_select_company = "You must select a company !";
$l_invalid_date = "Invalid Date Format (MM/JJ/AAAA or AAAA-MM-JJ).";
$l_delete_confirm = "Confirm the deal deletion ?";
$l_j_kind_create = "You have to enter a new kind name to create it !";
$l_j_kind_update = "You have to select a kind and enter a new name to update it !";
$l_j_kind_delete = "You have to select a kind to check or delete it !";
$l_j_status_create = "You have to enter a new status name to create it !";
$l_j_status_update = "You have to select a status and enter a new name to update it !";
$l_j_status_delete = "You have to select a status to check or delete it !";
$l_j_cat_create = "You have to fill the label before submitting.";
$l_j_cat_update = "You have to fill the label before submitting.";
$l_j_cat_delete = "You have to select a category";

// For the display parameters modification screen
$l_deal_display = "Display options for deals list";
$l_parentdeal_display = "Display options for parentdeals list";
$l_invoice_display = "Display options for invoice list";
