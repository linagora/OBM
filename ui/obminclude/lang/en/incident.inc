<SCRIPT LANGUAGE="PHP">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : incident.inc                                             //
// 2001-08-20  Mehdi RANDE                                                   //
///////////////////////////////////////////////////////////////////////////////
//   $Id   //
///////////////////////////////////////////////////////////////////////////////

//Fields Label
$l_incident_label = "Label";
$l_incident_priority = "Priority";
$l_incident_status = "Status";
$l_incident_date = "Date";
$l_incident_owner = "Owner";
$l_incident_logger = "Technical manager";
$l_incident_description = "Description";
$l_incident_solution = "Solution founded";
$l_contract_num = "Number of Contract";
$l_contract_label = "Label of Contract";
$l_contract_date_init = "Begin Date";
$l_contract_date_expir = "Expiration Date";
$l_date_before ="Date Before";
$l_date_after ="Date After";
$l_to = "To"; 
$l_from = "From";
$l_manager = "Managers";
$l_client_manager = "Client Managers";
$l_incident_lastupdate = "Date of last update";
$l_archive_new = "To File";
$l_archive = "Include Files";
$l_archive_consult = "Filed";
$l_incident_company = "Company";


//Title 
$l_incident_select_contract = "Select the company";
$l_redhot = "Redhot";
$l_hot = "Hot";
$l_normal = "Normal";
$l_low = "Low";
$l_open = "Open";
$l_call = "Call";
$l_waitcall = "Wait Call";
$l_paused = "Paused";
$l_closed = "Closed";

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames["incident_etat"] = $l_incident_status;
$fieldnames["incident_label"] = $l_incident_label;
$fieldnames["incident_priority"] = $l_incident_priority;
$fieldnames["incident_date"] = $l_incident_date;
// Calculate fields
$fieldnames["timeupdate"] = $l_incident_lastupdate;
$fieldnames["incident_owner_lastname"] = $l_incident_owner;
$fieldnames["incident_logger_lastname"] = $l_incident_logger;
$fieldnames["incident_company_name"] = $l_incident_company;

// Action

$l_select_contract = "Select the Contract";
$l_insert = "Record the Incident";
$l_update = "Modify the Incident";
$l_checkdelete = "Delete the Incident";

//Message

$l_error_permission = "You doesn't have the rights to access this page";
$l_found = "incidents found";
$l_no_found = "No incident found matching the query !";
$l_insert_ok = "The incident has been inserted successfully.";
$l_no_display = "Fill the search fields and press < Find > to retrieve the Incidents";
$l_incident_select_contract = "Select the contract of the Incident";

// For the display preferences modification screen 

$l_incident_display = "Display options for Incidents";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_label = "You have to fill the Label before submitting.";
$l_length_label = "The Label is too long";
$l_fill_date = "You have to fill the Date before submitting.";
$l_invalid_date = "Invalid date format (DD/MM/YYYY or YYYY-MM-DD).";
$l_j_select_contract = "You have to select a contract !";
$l_delete_confirm="Really delete the Incident ?";
$l_date_after_js ="Date After";
