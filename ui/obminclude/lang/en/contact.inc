<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : contact.inc                                              //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_company = "Company";
$l_lastname = "Lastname";
$l_firstname = "Firstname";
$l_kind = "Kind";
$l_market = "Person in charge";
$l_function = "Function";
$l_title = "Job Title";
$l_hphone = "Home Phone";
$l_minihphone = "(Home)";
$l_mphone = "Mobile Phone";
$l_minimphone = "(Mobile)";
$l_email = "E-Mail";
$l_mailing_ok = "Mailing enabled";
$l_fax = "Fax";
$l_expresspostal = "Express Postal";
$l_country = "Country";
$l_comment = "Comment";
$l_visibility = "Access";
$l_coord = "Details";
$l_cat1_manage = "Category 1 management";
$l_cat1_exist = "Existing Category 1";
$l_cat1_new = "New category 1";
$l_cat1_delete = "Delete";
$l_cat2_manage = "Category 2 management";
$l_cat2_exist = "Existing Category 2";
$l_cat2_new = "New category 2"; 
$l_cat2_delete = "Delete";
$l_category1 = "Category 1";
$l_category2 = "Category 2";
$l_code = "Code";
$l_lalbel = "Label";
$l_letter = "Letter Head";
// Labels and texts
$l_contact_name = "Contact Name";
$l_from_company = "From Company";
$l_telother = "Other tel";
$l_private = "Private";
$l_public = "Public";
$l_contact_select_company = "Select the company for the contact.";
$l_func_manage = "Contact Function Management";
$l_func_exist = "Existing Functions";
$l_func_no = "No functions defined";
$l_func_new = "New Function";
$l_kind_manage = "Contact Kind Management";
$l_kind_exist = "Existing Kinds";
$l_kind_no = "No kind defined";
$l_kind_new = "New Kind";
$l_lang = "Lang";
$l_header = "Header";
$l_subscription = "Subscriptions";
$l_subscription_new= "New subscription";
// Titles
$l_contact = "Contact";

// Actions
$l_insert = "Insert the Contact";
$l_update = "Update the Contact";
$l_checkdelete = "Check or Delete the Contact";
$l_delete = "Delete the Contact";
$l_func_update = "Update Function";
$l_func_checkdelete = "Check or Delete Function";
$l_func_delete = "Delete Function";
$l_func_insert = "Insert Function";
$l_kind_update = "Update Kind";
$l_kind_checkdelete = "Check or Delete Kind";
$l_kind_delete = "Delete Kind";
$l_kind_insert = "Insert Kind";
$l_add = "Add these contacts";

// Messages
$l_cat1_checkdelete = "Check or delete Category 1"; 
$l_cat1_update = "Update category 1"; 
$l_cat1_insert = "Insert a category 1";
$l_cat1_insert_ok = "The category 1 has been inserted successfully.";
$l_cat1_insert_error = "The insertion of the category 1 has failed !";
$l_cat1_delete_ok = "The category 1 has been deleted successfully.";
$l_cat1_delete_error = "The deletion of the category 1 has failed !";
$l_cat1_update_ok = "The category 1 has been updated successfully.";
$l_cat1_update_error = "The category 1 has been updated has failed !";
$l_cat2_checkdelete = "Check or delete Category 2"; 
$l_cat2_update = "Update category 2"; 
$l_cat2_insert = "Insert a category 2";
$l_cat2_insert_ok = "The category 2 has been inserted successfully.";
$l_cat2_insert_error = "The insertion of the category 2 has failed !";
$l_cat2_delete_ok = "The category 2 has been deleted successfully.";
$l_cat2_delete_error = "The deletion of the category 2 has failed !";
$l_cat2_update_ok = "The category 2 has been updated successfully.";
$l_cat2_update_error = "The category 2 has been updated has failed !";
$l_cat1_link_contact = "These category1 are referenced as";
$l_cat1_link_contact_no = "No contacts referenced as";
$l_cat1_cant_delete = "The category1 can't be deleted !";
$l_cat1_can_delete = "The category1 can be deleted.";     
$l_cat2_link_contact = "These category2 are referenced as";
$l_cat2_link_contact_no = "No contacts referenced as";
$l_cat2_cant_delete = "The category2 can't be deleted !";
$l_cat2_can_delete = "The category2 can be deleted.";
$l_check_samecontact = "These contacts seem similar to this new entry !";
$l_link_deal = "The contact is involved in these deals";
$l_link_deal_no = "The contact isn't involved in any deal";
$l_link_contract = "The contact is involved in these contracts";
$l_link_contract_no = "The contact isn't involved in any contract";
$l_link_list = "The contact is registered in these lists";
$l_link_list_no = "The contact isn't registered in any list";
$l_link_publication = "The contact has these subscriptions";
$l_link_publication_no = "The contact hasn't any subscription";
$l_cant_delete = "The contact can't be deleted !";
$l_can_delete = "The contact can be deleted.";
$l_no_display = "Fill the search fields and press < Find > to retrieve the contacts";
$l_found = "contacts found.";
$l_no_found = "No contact found matching the query !";
$l_delete_ok = "The contact has been deleted successfully.";
$l_delete_error = "The deletion of the contact has failed !";
$l_insert_ok = "The contact has been inserted successfully.";
$l_insert_error = "The insertion of the contact has failed !";
$l_update_ok = "The contact has been updated successfully.";
$l_update_error = "The update of the contact has failed !";
// Function Messages
$l_func_insert_ok = "The function has been inserted successfully.";
$l_func_insert_error = "The insertion of the function has failed !";
$l_func_delete_ok = "The function has been deleted successfully.";
$l_func_delete_error = "The deletion of the function has failed !";
$l_func_update_ok = "The function has been updated successfully.";
$l_func_update_error = "The update of the function has failed !";
$l_func_link_contact = "These contacts are referenced as";
$l_func_link_contact_no = "No contact referenced as";
$l_func_cant_delete = "The Function can't be deleted !";
$l_func_can_delete = "The Function can be deleted.";
// Kind Messages
$l_kind_insert_ok = "The kind has been inserted successfully.";
$l_kind_insert_error = "The insertion of the kind has failed !";
$l_kind_delete_ok = "The kind has been deleted successfully.";
$l_kind_delete_error = "The deletion of the kind has failed !";
$l_kind_update_ok = "The kind has been updated successfully.";
$l_kind_update_error = "The update of the kind has failed !";
$l_kind_link_company = "These companies are referenced as";
$l_kind_link_company_no = "No company referenced as";
$l_kind_cant_delete = "The Kind can't be deleted !";
$l_kind_can_delete = "The Kind can be deleted.";

// JavaScript Messages (specials charcaters must be escaped whith \ -> \\)
$l_fill_lastname = "You have to fill the Lastname before submitting.";
$l_fill_company = "You have to select a company before submitting.";
$l_fill_phone = "You have to fill the Phone before submitting.";
$l_delete_confirm = "Really delete the contact ?";
$l_j_select_company =  "You must select a company !";
$l_j_check_phone = "The phone number is not valid.";
$l_j_check_hphone = "The personal phone number is not valid.";
$l_j_check_mphone = "The mobile phone number is not valid.";
$l_j_check_fax = "The fax number is not valid.";
$l_j_check_email = "The email adress is not valid.";
$l_j_cat1_delete = "You have to select a category1";
$l_j_cat2_delete = "You have to select a category2";
$l_j_cat1_update = "You have to fill the label before submitting.";
$l_j_cat2_update = "You have to fill the label before submitting.";
$l_j_cat1_create = "You have to fill the label before submitting.";
$l_j_cat2_create = "You have to fill the label before submitting.";
// JavaScript messages - Function
$l_j_func_delete = "You have to select a function to check or delete it !";
$l_j_func_update = "You have to select a function and enter a new name to update it !";
$l_j_func_create = "You have to enter a new function label to create it !";
// JavaScript messages - Kind
$l_j_kind_delete = "You have to select a kind to check or delete it !";
$l_j_kind_update = "You have to select a kind and enter a new name to update it !";
$l_j_kind_create = "You have to fill all kind informations kind to create it !";
// For the display parameters modification screen
$l_contact_display = "Display options for contacts list"; 
//External Field
$l_timecreate = "Creation";
$l_timeupdate = "Last update";
$l_publication_title = "Title";
$l_year = "Year";
$l_renewal = "Automatic renewal";
$l_recept = "Reception mode";
$l_quantity = "Quantity";
$l_no_found_subscription = "No subscription found for this contact !";

