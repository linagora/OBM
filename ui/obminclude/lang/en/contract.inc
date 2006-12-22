<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : contract.inc                                             //
// 1999-01-25 Rande Mehdi                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes
$l_archive = "Archive";
$l_contract_contracttype = "Type of contract";
$l_after = "Expiration After";
$l_before = "Expiration Before";
$l_incident_list = "Incidents";
$l_incident_new = "New";
$l_format = "Format";
$l_ccf_period = "Period";
$l_ccf_ticket = "Tickets";
$l_ccf_duration = "Duration";
$l_signature = "Signature";
$l_exp = "Expiration";
$l_renew = "Renewal";
$l_date_cancel = "Cancellation";
$l_autorenewal =" Renewal Auto";
$l_priority = "Priority";
$l_pri_manage = "Contract Priority Management";
$l_pri_exist = "Existing Priorities";
$l_pri_new = "New Priority";
$l_sta_manage = "Contract Status Management";
$l_sta_exist = "Existing Status";
$l_sta_new = "New Status";
$l_total_ticket ="Tickets used";
$l_disp_ticket = "Tickets available";
$l_ticket_final = "Last"; 
$l_ticket_final_coupon = "tickets";
$l_type = "Type";
$l_duration_used ="Duration used (h)";
$l_duration = "Support duration (h)";
$l_ticket = "Contract type";
$l_ticket_nb = "# Tickets";
$l_yes = "Yes";
$l_no = "No";
$l_cck_customer = "Customer";
$l_cck_supplier = "Supplier";
$l_contract_kind = "Contract kind";
$l_contract_select_deal = "Select the Deal.";
$l_label_libelle = "Key-word of the label";
$l_renewalkind = "Renewal Kind";
$l_marketing_manager = "Marketing Man.";
$l_technical_manager = "Technical Man.";
$l_client_manager = "Client Man.";
$l_clause = "Clauses";
$l_select_contract = "Select the contract";

$l_company_town = "town";
$l_company_zipcode = "zipcode";
$l_company_expresspostalcode = "express postal code";
$l_company_phonenumber = "phone number";
$l_company_fax = "fax number";
$l_company_web = "web address";
$l_company_email = "email";

// Actions
$l_pri_update = "Update Priority";
$l_pri_checkdelete = "Check or Delete Priority";
$l_pri_delete = "Delete Priority";
$l_pri_insert = "Insert Priority";
$l_sta_update = "Update Status";
$l_sta_checkdelete = "Check or Delete Status";
$l_sta_delete = "Delete Status";
$l_sta_insert = "Insert Status";
$l_insert = "Insert the contract";
$l_update = "Update the contract";
$l_delete = "Delete the contract";
$l_select_company = "Select the company";

// Messages
$l_pri_can_delete = "The Priority can be deleted";
$l_pri_cant_delete = "The Priority can't be deleted";
$l_pri_link_contract = "The Priority is referenced by these contracts";
$l_pri_link_contract_no = "The Priority isn't referenced";
$l_sta_can_delete = "The Status can be deleted";
$l_sta_cant_delete = "The Status can't be deleted";
$l_sta_link_contract = "The Status is referenced by these contracts";
$l_sta_link_contract_no = "The Status isn't referenced";
$l_type_link_contract = "The Type is referenced by these contracts";
$l_type_link_contract_no = "The Type isn't referenced";
$l_sta_link_incident = "The Status is referenced by these incidents";
$l_sta_link_incident_no = "The Status isn't referenced";
$l_no_display = "Fill the search fields and press < Find > to retrieve the contracts";
$l_found = "contracts found.";
$l_no_found = "No contract found matching the query !";
$l_link_incident = "The contract has some incidents";
$l_link_incident_no = "The contract hasn't any incident";
$l_cant_delete = "The contract can't be deleted !";
$l_can_delete = "The contract can be deleted.";
$l_type_manage = "Support contract type management";
$l_type_exist = "Existing type";
$l_type_new = "New type";
$l_type_checkdelete = "Check or Delete Type"; 
$l_type_delete = "Delete Type"; 
$l_type_update = "Update Type";
$l_type_insert = "Create type";
$l_type_cant_delete = "The type can't be deleted !";
$l_type_can_delete = "The type can be deleted.";
$l_type_link_contract = "The type is involved in these contracts";
$l_check_samecontract = "These contracts seem similar to this new entry !";
$l_err_label_empty = "You must fill the label !";
$l_err_duration_empty = "You must fill the Support duration !";
$l_err_ticket_empty = "You must fill the Tickets number !";
$l_err_daterenew = "Renewal date is not valid !";
$l_err_datecancel = "Cancellation date is not valid !";
$l_err_date = "Initial date is higher than Expiration date !";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_j_select_contract = "You have to select a contract !";
$l_delete_confirm = "Really delete the Contract ?";
$l_j_type_delete = "You have to select a type to delete it !";
$l_j_type_update = "You have to select a type and enter a modification to submit !"; 
$l_j_type_create = "You have to enter a new type name to create it !";

// For the display preferences modification screen 
$l_contract_display = "Display options for Support contracts";
$l_j_pri_create = "You have to enter a priority to create it !";
$l_j_pri_update = "You have to select a priority and enter a new name and order to update it !";
$l_j_pri_delete = "You have to select a priority to delete it !";
$l_j_sta_create = "You have to enter a status to create it !";
$l_j_sta_update = "You have to select a status and enter a new name and order to update it !";
$l_j_sta_delete = "You have to select a status to delete it !";

