<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : list.inc                                                 //
// 2001-02-16 Vincent MARGUERIT                                              //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


// Database fields
$l_name = "Name";
$l_subject = "Subject";
$l_email = "E-mail";
$l_query = "Query";
$l_creator = "Creator";
$l_date_creation = "Creation";
$l_updater = "Last updater";
$l_date_last_update = "Last update";

// Labels and texts
$l_list = "List";
$l_yes = "yes";
$l_no = "no";
$l_nb_contact = "Number of contact";
$l_list_contact = "Having as contact";
$l_list_display = "Display options for Lists list";
$l_contact_display = "Display options for Contacts list";
$l_list_set = "List Set";
$l_export_format = "Available export formats";
$l_desc = "Description";
$l_export_intro = "Select the export format needed";

// Contact
$l_lastname = "Lastname";
$l_firstname = "Fisrtname";
$l_function = "Function";
$l_company = "Company";
$l_phone = "Phone";
$l_hphone = "Home phone";
$l_town = "Town";

// Actions
$l_insert = "Insert the list";
$l_update = "Update the list";
$l_delete = "Delete the list";
$l_change_auth = "Change auth";
$l_confirm = "Confirm the action";
$l_add = "Add these lists";
$l_mail = "Send mail to members" ;

// Messages
$l_insert_ok = "The list has been inserted successfully.";
$l_insert_error = "The list insertion has failed !";
$l_update_ok = "The list has been updated successfully.";
$l_update_error = "The list update has failed !";
$l_delete_ok = "The list has been deleted successfully.";
$l_delete_error = "The list deletion has failed !";
$l_fill_name = "You have to fill the list name";
$l_name_list_nomatch = "List name is not valid";
$l_list_exists = "Sorry but this list already exists";
$l_check_samelist = "These lists seem similar to this new entry !";
$l_j_check_email = "The email adress is not valid.";
$l_warn_delete = "registered contacts in this list";

$l_no_display = "Fill the search fields and press < Find > to retrieve the lists";
$l_no_list = "No list can be found with these criterias." ;
$l_found = "lists found.";
$l_no_found = "No lists found matching the query";

$l_add_contact = "Add contacts" ;
$l_del_contact_sel = "Remove these contacts";
$l_contact_added = "added Contact(s)";
$l_contact_removed = "removed Contact(s)";

$l_fill_name = "The list Name must be filled !";
$l_invalid_query = "The query is invalid !";



$l_con_found = "registered contacts";
$l_no_contact = "No contact registered";
$l_nb_contact_deleted = "Number of contact deleted" ;
$l_list_add_contact = "Add" ;
$l_list_add_contact_chosen = "Add selected contacts" ;
$l_nb_contact_added = "Number of contact added" ;
$l_delete_confirm = "Do you comfirm removal of list" ;
$l_complete_mail_form = "Please fill mail fields to sent mail" ;


//------------------------------------//
// Export Format informations         //
//------------------------------------//
// Format 0 : HTML Stickers
$c_export_format[0]["name"] = "HTML Stickers";
$c_export_format[0]["info"] = "HTML Preview for stickers";
// Format 1 :
$c_export_format[1]["name"] = "HTML Table";
$c_export_format[1]["info"] = "HTML Preview for table";
// Format 2 : RTF Stickers
$c_export_format[2]["name"] = "RTF Stickers";
$c_export_format[2]["info"] = "RTF Sticker creation";
// Format 3 : Excel data
$c_export_format[3]["name"] = "Excel Table";
$c_export_format[3]["info"] = "Excel table data";
