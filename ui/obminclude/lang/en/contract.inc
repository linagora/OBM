<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : contract.inc                                             //
// 1999-01-25 Rande Mehdi                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes
$l_contract_num="Contract number";
$l_contract_label="Contract label";
$l_contract_company="Company";
$l_contract_contracttype="Kind of contract";
$l_contract_exp = "Expiration";
$l_date_alarm_after = "Expiration date After";
$l_date_alarm_before = "Expiration date Before";
$l_after = "Expiration date After";
$l_before = "Expiration date Before";
$l_incident_list = "List of the Incidents";
$l_incident_new = "Add a new Incident";

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
$fieldnames["contract_label"] = $l_contract_label;
$fieldnames["contract_company_name"] = $l_contract_company;
$fieldnames["contract_number"] = $l_contract_num;
$fieldnames["contract_dateexp"] = $l_contract_exp;
$fieldnames["contracttype_label"] = $l_contract_contracttype;


$l_contract_select_deal = "Select the Deal.";
$l_deal_label = "Deal";
$l_minus="-";
$l_plus="+";
$l_order="Ordrer";
$l_todo = "To Do";
$l_label = "Label";
$l_label_libelle = "Key-word of the label";
$l_label_start = "Number";
$l_kind = "Kind";
$l_category = "Category";
$l_state = "State";
$l_marketing_manager = "Marketing Man.";
$l_technical_manager = "Technical Man.";
$l_client_manager = "Client Man.";
$l_manager= "Manager";
$l_contacts = "Contacts";
$l_clause = "Clauses";
$l_comment = "Comment";
$l_company = "Name of the company";
$l_date = "Date";
$l_date_init = "Initial date";
$l_dateexp = "Expiration date";
$l_date_alarm = "Alarm date";
$l_last_update = "Last update";
$l_at = "&agrave;";
$l_number = "Number";
$l_private = "Private";
$l_public = "Public";
$l_visibility ="Acc&egrave;s";

$l_type_manage = "Support contract kind management";
$l_type_exist = "Existing kinds";
$l_type_new = "New kind";
$l_category_label="Label";
$l_category_minilabel="Mini Label";
$l_select_contract = "Select the contract";

// Titres
$l_deal = "Support contract";
$l_deal_detail = "Details";
$l_deal_new = "New contract";
$l_deal_insert = "Contract insertion";
$l_deal_update = "Contract update";
$l_deal_delete = "Contract deletion";

// Actions
$l_insert = "Insert the contract";
$l_update = "Update the contract";
$l_delete = "Delete the contract";
$l_select_company = "Select the company";

$l_type_delete = "Delete Kind"; 
$l_type_update = "Update Kind";
$l_type_insert = "Create Kind";
$l_status_update = "Update Status";
$l_status_delete = "Delete Status";
$l_status_insert = "Create Status";
$l_category_delete = "Delete Category"; 
$l_category_update = "Update Category";
$l_category_insert = "Create Category";

// Messages
$l_add_contract_deal = "Please fill the fields the of the contract which is linked to the Deal";
$l_error_permission = "You doesn't have the rights to access this page";
$l_error_visibility = "You doesn't have the rights to access this informations";
$l_no_display = "Fill the search fields and press < Find > to retrieve the contract";
$l_found = "contracts found.";
$l_no_found = "No contract found matching the query !";
$l_delete_ok = "The contract has been deleted successfully.";
$l_delete_error = "The deletion of the contract has failed !";
$l_insert_ok = "The contract has been inserted successfully.";
$l_insert_error = "The insertion of the contract has failed !";
$l_update_ok = "The contract has been updated successfully.";
$l_update_error = "The update of the contract has failed !";
$l_query_error = "Critical trouble during the execution of the query.";

$l_type_insert_ok = "The kind has been inserted successfully.";
$l_type_insert_error = "The insertion of the kind has failed !";
$l_type_update_ok = "The kind has been updated successfully";
$l_type_update_error = "The update of the kind has failed !";
$l_type_delete_error = "La suppression du type a &eacute;chou&eacute;e !";
$l_type_del_verif_error = "(s)  sont de ce type.";
$l_type_delete_ok = "The kind has been deleted successfully.";


// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_label = "\\n You have to fill the Label before submitting.";
$l_fill_number = "\\n You have to fill the Number before submitting.";
$l_j_select_company = "You have to select a company !";
$l_fill_date = "You have to fill the Date before submitting..";
$l_invalid_date = "Invalid date format (DD/MM/YYYY or YYYY-MM-DD).";
$l_delete_confirm = "Really delete the Contract ?";

$mj_type_delete_confirm = "Really delete the contract type ?";
$mj_type_delete = "You have to select a type to delete it !";
$mj_type_update = "You have to select a type and enter a modification to submit !"; 
$mj_type_create = "You have to enter a new kind name to create it !";
$l_length_label = " The Label is to long ";
$l_length_number = " The Number of Contract is too long";

// For the display preferences modification screen 
$l_contract_display = "Display options for Support contracts";
$l_updater = "Last update by" ;
$l_date_last_update = "Date of the last update" ;
