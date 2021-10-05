<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : deal.inc                                                 //
// 1999-01-26 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Modified on 2009-09-09T15:20:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T13:49:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T13:49:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T13:54:17+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T13:57:07+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T13:59:44+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:02:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:03:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:03:31+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:04:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:06:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:08:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:11:19+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:19:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:22:29+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:23:05+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:23:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:24:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:25:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:26:00+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:26:26+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:27:04+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:27:29+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:27:57+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:29:37+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:30:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:31:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:32:28+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:33:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:34:40+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:37:22+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:38:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:40:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:40:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:40:58+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:42:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:43:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:43:52+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:45:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:45:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:47:57+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:53:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:54:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:54:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T15:00:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T15:01:06+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T16:51:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T16:51:26+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T16:53:47+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T16:54:25+02:00 using OBMtranslation (0.4.1)

$l_date_init = "Initial Date";
$l_parentdeal = "Parent Deal";
$l_marketing_manager = "Marketing manager";
$l_technical_manager = "Technical manager";
$l_amount = "Amount";
$l_margin = "Margin";
$l_hitrate = "Hit Rate";
$l_commission = "Commission";
$l_commission_p = "% Commission";
$l_commission_balanced = "Balanced Commission ";
$l_expected = "Expected";
$l_quarter = "Q";
$l_sale_delay = "Sale delay";
$l_date_comment = "Comment Date";
$l_todo = "To Do";
$l_category = "Category";
$l_code = "Code";
$l_role = "Role";
$l_other_relation = "Other relations";
$l_infos = "Information";
$l_minus = "-";
$l_plus = "+";
$l_order = "Order";
$l_amount_in = "Imputs amount";
$l_amount_out = "Outputs amount"; 
$l_potential_balanced = "Weighted Potential";
$l_label_start = "Label start";
$l_proposal = "Proposal";
$l_deal_select_company = "Select the company with whom the deal must be filled in.";
$l_deal_select_parentdeal = "Select the parent deal to which the deal must be affected";
$l_affect_deal= "Change Parent Deal";
$l_no_invoice = "No invoice connected";
$l_type_manage = "Deal Type Management";
$l_type_exist = "Existing Types";
$l_type_new = "New Type";
$l_status_manage = "Deal Status Management";
$l_status_exist = "Existing Status";
$l_status_new = "New Status";
$l_parentdeal_deals = "Deals belonging to this parent deal";
$l_forecast = "Forecast";
$l_deal_panel = "Deal panel";
$l_select_deal = "Select Deal";
$l_insert = "Insert Deal";
$l_update = "Update Deal";
$l_delete = "Delete Deal";
$l_select_company = "Select Company";
$l_affect = "Affect";
$l_insert_parent = "Insert the Parent Deal";
$l_update_parent = "Update the Parent Deal";
$l_delete_parent = "Delete the Parent Deal";
$l_type_checkdelete = "Check or Delete Type";
$l_type_delete = "Delete Type";
$l_type_update = "Update Type";
$l_type_insert = "Insert Type";
$l_status_update = "Update Status";
$l_status_checkdelete = "Check or Delete Status";
$l_status_delete = "Delete Status";
$l_status_insert = "Insert Status";
$l_add_cats = "Add these categories";
$l_link_contract = "The deal is involved in some contracts";
$l_link_contract_no = "The deal is not involved in any contracts";
$l_link_project = "The deal is involved in some projects";
$l_link_project_no = "The deal is not involved in any projects";
$l_link_invoice = "The deal is involved in some invoices";
$l_link_invoice_no = "The deal is not involved in any invoices";
$l_can_delete = "The deal can be deleted.";
$l_cant_delete = "The deal cannot be deleted !";
$l_link_deal = "Deals are attached to this ParenDeal";
$l_cant_delete_parent = "The parent deal cannot be deleted !";
$l_err_label_empty = "You must fill in the Parent Deal label !";
$l_err_type_empty = "You must fill in the deal type !";
$l_err_task_empty = "You must fill in the deal category !";
$l_err_market_empty = "You must fill in the marketing manager !";
$l_err_tech_empty = "You must fill in the technical manager !";
$l_err_hitrate = "Hit rate must be in the range 0 - 100 !";
$l_updateaffect_ok = "The deal has been successfully affected to a parent deal";
$l_no_display = "Fill in the search fields and press < Find > to retrieve the deals";
$l_found = "deals found.";
$l_no_found = "No deal matching the query has been found !";
$l_found_parent = "Parent Deals found";
$l_no_found_parent = "No parent deal matching the query has been found !";
$l_no_display_parent ="Fill the search fields and press < Find > to retrieve the parentdeals";
$l_type_can_delete = "The Type can be deleted !";
$l_type_cant_delete = "The Type cannot be deleted !";
$l_type_link_deal = "The Type is involved in these deals";
$l_type_link_deal_no = "The Type is not involved in any deal";
$l_status_can_delete = "The Status can be deleted";
$l_status_cant_delete = "The Status cannot be deleted";
$l_status_link_deal = "The Status is involved in these deals";
$l_status_link_deal_no = "The Status is not involved in any deal";
$l_help_deal_forecast = "Forecast takes into account deals with these criteria : deal not in an archived state, with an estimated amount and with a hit rate > 0 and < 100.";
$l_fill_parentdeal = "You have to select a parent deal before submitting.";
$l_fill_label = "You have to fill in the Label before submitting.";
$l_fill_number = "You have to fill in the Number before submitting.";
$l_fill_datealarm = "You have to fill in the Alarm date before submitting.";
$l_fill_datebegin = "You have to fill in the Beginning date before submitting.";
$l_confirm_datealarm = "Date Alarm is in the past ! Confirm the action ?";
$l_invalid_date = "Invalid Date Format (MM/JJ/AAAA or AAAA-MM-JJ).";
$l_delete_confirm = "Confirm deal deletion ?";
$l_j_type_create = "You have to enter a new type name to create it !";
$l_j_type_update = "You have to select a type and enter a new name to update it !";
$l_j_type_delete = "You have to select a type to check or delete!";
$l_j_status_create = "You have to enter a new status name to create it !";
$l_j_status_update = "You have to select a status and enter a new name to update it !";
$l_j_status_delete = "You have to select a status to check or delete !";
$l_deal_display = "Display options for deals list";
$l_parentdeal_display = "Display options for parent deals list";
$l_invoice_display = "Display options for invoice list";
$l_invoice_ttc = "Tax included amount";
$l_invoice_number = "Number";
$l_category1 = "Category";
$l_number = "Number";
$l_invoice_date = "Date";
$l_type_label = "Type";
$l_label = "Label";
$l_invoice_ht = "Tax exlusion amount";
$l_invoice_status = "Status";
$l_invoice_label = "Label";

