<SCRIPT LANGUAGE="PHP">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : incident.inc                                             //
// 2001-08-20 : Mehdi RANDE                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Fields Label
$l_label = "Label";
$l_priority = "Priority";
$l_status = "Status";
$l_date = "Date";
$l_owner = "Owner";
$l_logger = "Technical manager";
$l_duration = "Resolution duration";
$l_description = "Description";
$l_solution = "Solution founded";
$l_num = "Number";
$l_contract = "Contract";
$l_date_before ="Date Before";
$l_date_after ="Date After";
$l_to = "To"; 
$l_from = "From";
$l_manager = "Managers";
$l_client_manager = "Client Managers";
$l_lastupdate = "Date of last update";
$l_archive_new = "To File";
$l_archive = "Archives";
$l_archive_consult = "Filed";
$l_company = "Company";
$l_incident = "Incident";

$l_pri_manage = "Incident Priority Management";
$l_pri_exist = "Existing Priorities";
$l_pri_new = "New Priority";

// Title 
$l_redhot = "Redhot";
$l_hot = "Hot";
$l_normal = "Normal";
$l_low = "Low";
$l_open = "Open";
$l_call = "Call";
$l_waitcall = "Wait Call";
$l_paused = "Paused";
$l_closed = "Closed";

// Action
$l_select_contract = "Select the Contract";
$l_insert = "Record the Incident";
$l_update = "Modify the Incident";
$l_checkdelete = "Delete the Incident";
$l_pri_update = "Update Priority";
$l_pri_checkdelete = "Check or Delete Priority";
$l_pri_delete = "Delete Priority";
$l_pri_insert = "Insert Priority";
$l_sta_update = "Update Status";
$l_sta_checkdelete = "Check or Delete Status";
$l_sta_delete = "Delete Status";
$l_sta_insert = "Insert Status";

// Message
$l_found = "incidents found";
$l_no_found = "No incident found matching the query !";
$l_insert_ok = "The incident has been inserted successfully.";
$l_delete_ok = "The incident has been deleted successfully.";
$l_no_display = "Fill the search fields and press &lt; Find &gt; to retrieve the Incidents";
$l_select_contract = "Select the contract of the Incident";

$l_pri_insert_ok = "The priority has been inserted successfully.";
$l_pri_insert_error = "The insertion of the priority has failed !";
$l_pri_update_ok = "The priority has been updated successfully.";
$l_pri_update_error = "The priority of the deal has failed !";
$l_pri_delete_error = "The deletion of the priority has failed !";
$l_pri_delete_ok = "The priority has been deleted successfully.";
$l_pri_can_delete = "The Priority can be deleted";
$l_pri_cant_delete = "The Priority can't be deleted";
$l_pri_link_incident = "The Priority is referenced by these incidents";
$l_sta_insert_ok = "The status has been inserted successfully.";
$l_sta_insert_error = "The insertion of the status has failed !";
$l_sta_update_ok = "The status has been updated successfully.";
$l_sta_update_error = "The status update has failed !";
$l_sta_delete_error = "The deletion of the status has failed !";
$l_sta_delete_ok = "The status has been deleted successfully.";
$l_sta_can_delete = "The Status can be deleted";
$l_sta_cant_delete = "The Status can't be deleted";
$l_sta_link_incident = "The Status is referenced by these incidents";

// For the display preferences modification screen 
$l_incident_display = "Display options for Incidents";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_length_label = "The Label is too long";
$l_fill_contract = "You have to assigned a Contract before submitting.";
$l_j_select_contract = "You have to select a contract !";
$l_delete_confirm = "Really delete the Incident ?";
$l_date_after_js = "Date After";
$l_j_pri_update = "You have to select a priority and enter a new name and order update it !";
