<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : deal.inc                                                 //
// 1999-01-26 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//for list
//$l_task_fac = "Factured Tasks";
//$l_task_int = "Internal Tasks";

// Database fields
$l_number = "Number";
$l_label = "Label";
$l_date_init = "Init Date";
$l_parentdeal="Parent Deal";
$l_kind = "Kind";
$l_category = "Category";
$l_company = "Company";
$l_contact = "Contact";
$l_marketing_manager = "Marketing manager";
$l_technical_manager = "Technical manager";
$l_date_proposal = "Proposal Date";
$l_amount = "Amount";
$l_hitrate = "Hit Rate";
$l_state = "State";
$l_date_alarm = "Alarm Date";
$l_add_comment = "Add a comment";
$l_upd_comment = "Update comment";
$l_todo = "To Do";
$l_visibility = "Access";

// Labels and texts
$l_private = "Private";
$l_public = "Public";
$l_infos = "Informations";
$l_minus = "-";
$l_plus = "+";
$l_order = "Order";
$l_amount_in = "Amout of inputs";
$l_amount_out = "Amout of outputs";
$l_potential = "Weighted Potential";
$l_label_start = "Label start";
$l_manager = "Manager";
$l_proposition = "Proposal";
$l_show_archive = "Show archives";
$l_date_alarm_after = "Alarm Date after";
$l_date_alarm_before = "Alarm Date before";
$l_zipcode = "Zip Code";
$l_after = "After";
$l_before = "Before";
$l_last_update = "Last updated on";
$l_at = "at";
$l_deal_select_company = "Select the company which whom the deal must be filled";
$l_deal_select_parentdeal = "Select the parentdeal to which the deal must be affected";
$l_affect_deal= "Change Parent Deal";
$l_project_new = "New project";
$l_no_invoice = "No invoice connected";
$l_invoice_new = "New invoice";
$l_archived_invoices = " Archived Invoices";
$l_see_archived_invoices = "See archived invoices too";
$l_kind_manage = "Deal Kind Management";
$l_kind_exist = "Existing Kinds";
$l_kind_new = "New Kind";
$l_status_manage = "Deal Status Management";
$l_status_exist = "Existing States";
$l_status_new = "New State";
$l_category_manage = "Deal Category Management";
$l_category_exist = "Existing Categories";
$l_category_new = "New Category";
$l_category_label="Label";
$l_category_minilabel="Mini Label";
$l_parentdeal_deals = "Deals belonging to this parentdeal";
$l_add_contrat = "Add a contract";
$l_contract_new = "New contract";

// Titles
$l_deal = "Deal";

// Actions
$l_select_deal = "Select Deal";
$l_insert = "Insert the Deal";
$l_update = "Update the  Deal";
$l_delete = "Delete the Deal";
$l_archive_update = "Update Archives";
$l_select_company = "Select the Company";
$l_insert_parent = "Insert the ParentDeal";
$l_update_parent = "Update the ParentDeal";
$l_delete_parent = "Delete the ParentDeal";
$l_affect = "Affect";

$l_kind_checkdelete = "Check or Delete Kind";
$l_kind_delete = "Delete Kind";
$l_kind_update = "Update Kind";
$l_kind_insert = "Insert Kind";
$l_status_update = "Update State";
$l_status_checkdelete = "Check or Delete State";
$l_status_delete = "Delete State";
$l_status_insert = "Insert State";
$l_cat_checkdelete = "Check or Delete Category";
$l_cat_delete = "Delete Category";
$l_cat_update = "Update Category";
$l_cat_insert = "Insert Category";


// Messages
$l_link_contract = "The deal is involved in these contracts";
$l_link_contract_no = "The deal isn't involved in any contracts";
$l_cant_delete = "The deal can't be deleted !";
$l_can_delete = "The deal can be deleted.";

$l_err_parent_has_deal = "Deals are attached to this ParentDeal, it can't be deleted !";
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
$l_delete_ok = "The deal has been deleted successfully.";
$l_delete_error = "The deletion of the deal has failed !";
$l_insert_ok = "The deal has been inserted successfully.";
$l_insert_error = "The insertion of the deal has failed !";
$l_update_ok = "The deal has been updated successfully.";
$l_update_error = "The update of the deal has failed !";
$l_query_error = "Probl&egrave;me en executant la requete";
$l_archive_ok = "The archives have been updated successfully.";
$l_archive_error = "The update of the archives has failed !";
$l_archive_number = "deals inserted in the archives.";
$l_found_parent = "parentdeals found";
$l_no_display_parent ="Fill the search fields and press < Find > to retrieve the parentdeals"; 
$l_no_found_parent ="No parentdeal found matching the query !";

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
$l_status_insert_ok = "The state has been inserted successfully.";
$l_status_insert_error = "The insertion of the state has failed !";
$l_status_update_ok = "The State has been updated successfully.";
$l_status_update_error = "The state of the deal has failed !";
$l_status_delete_error = "The deletion of the state has failed !";
$l_status_delete_ok = "The state has been deleted successfully.";
$l_status_can_delete = "The Status can be deleted";
$l_status_cant_delete = "The Status can't be deleted";
$l_status_link_deal = "The Status is involved in these deals";
$l_status_link_deal_no = "The Status is not involved in any deals";
$l_cat_insert_ok = "The category has been inserted successfully.";
$l_cat_insert_error = "The insertion of the category has failed !";
$l_cat_update_ok = "The category has been updated successfully.";
$l_cat_update_error = "The category update has failed !";
$l_cat_delete_error = "The deletion of the category has failed !";
$l_cat_delete_ok = "The category has been deleted successfully.";
$l_cat_can_delete = "The Category can be deleted";
$l_cat_cant_delete = "The Category can't be deleted";
$l_cat_link_deal = "The Category is involved in these deals";
$l_cat_link_deal_no = "The Category is not involved in any deals";

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
$l_j_kind_delete = "You have to select a kind to check or delete it !";
$l_j_status_delete = "You have to select a state to check or delete it !";
$l_j_category_delete = "You have to select a category to check or delete it !";
$l_j_kind_update = "You have to select a kind and enter a new name to update it !";
$l_j_status_update = "You have to select a state and enter a new name to update it !";
$l_j_category_update = "You have to select a category and enter a new name to update it !";
$l_j_kind_create = "You have to enter a new kind name to create it !";
$l_j_status_create = "You have to enter a new state name to create it !";
$l_j_category_create = "You have to enter a new category name to create it !";

// For the display parameters modification screen
$l_deal_display = "Display options for deals list";
$l_parentdeal_display = "Display options for parentdeals list";
$l_invoice_display = "Display options for invoice list";
