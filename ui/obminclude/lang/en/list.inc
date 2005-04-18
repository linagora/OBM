<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : list.inc                                                 //
// 2001-02-16 Aliacom - Vincent MARGUERIT                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_subject = "Subject";
$l_email = "E-mail";
$l_query = "Query";
$l_creator = "Creator";
$l_date_creation = "Creation";
$l_updater = "Last updater";
$l_date_last_update = "Last update";
$l_static = "Static";

// Labels and texts
$l_list_contact = "Having as contact";
$l_list_display = "Display options for Lists list";
$l_contact_display = "Display options for Contacts list";
$l_list_set = "List Set";
$l_export_format = "Available export formats";
$l_export_intro = "Select the export format needed";
$l_criterion = "Search criteria";
$l_select_list = "Select the lists to be exported";
$l_timeafter = "Updated after";
$l_timebefore = "Updated before";
$l_market = "Responsible";
$l_contact_cat1 = "Category 1";
$l_contact_cat2 = "Category 2";
$l_expert_mode = "Expert Mode";
$l_normal_mode = "Normal Mode";
$l_renew = "Automatic renewal";
$l_recept = "Reception mode";
$l_mailing = "Mailing enabled only";
$l_contact_archive = "Archived contacts";
$l_info_pub = "Publications informations";
// Contact
$l_lastname = "Lastname";
$l_firstname = "Firstname";
$l_function = "Function";
$l_service = "Service";
$l_status = "Status";
$l_letter = "L. Head";

//Publication
$l_title = "Title";
$l_year = "Year";
$l_quantity = "Quantity";

// Actions
$l_insert = "Insert";
$l_update = "Update";
$l_delete = "Delete";
$l_add = "Add these lists";
$l_add_criterion = "Add a criterion";

//Logical
$l_or = "OR";
$l_not = "NOT";
$l_and = "AND";
$l_empty = "&nbsp;";

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
$l_j_check_email = "The email address is not valid.";
$l_warn_delete = "registered contacts in this list";
$l_list_wizard = "Global search";
$l_no_display = "Fill the search fields and press < Find > to retrieve the lists";
$l_found = "lists found.";
$l_no_found = "No lists found matching the query";
$l_static_con_found = "static registered contacts";
$l_no_static_contact = "No static contact registered";
$l_dyn_con_found = "Contacts found";
$l_no_dyn_contact = "No contacts found";

$l_add_contact = "Add contacts to the list" ;
$l_del_contact_sel = "Remove these contacts";
$l_contact_added = "added Contact(s)";
$l_contact_removed = "removed Contact(s)";

$l_fill_name = "The list Name must be filled !";
$l_fill_subject = "The subject must be filled !";
$l_invalid_query = "The query is invalid !";
$l_banned_word = "Banned word";
$l_hungry_query = "The query need too much resource !";
$l_warning_exp_to_nor = "Warning, You will lose your current query. Continue ?";
$l_warning_nor_to_exp = "Warning, You will preserve the criteria in the query but lose these if you return to normal mode. Continue ?";

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
