<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : publication.inc                                              //
// 1999-01-25 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_number = "Number";
$l_aka = "Also known as";
$l_publication_name = "Publication Name";
$l_kind = "Kind";
$l_category = "Categories";
$l_activity = "Activity";
$l_market = "Marketing manager";
$l_publication_kind = "Publication Kind";
$l_expresspostal = "Express postal";
$l_country = "Country";
$l_fax = "Fax";
$l_web = "Web";
$l_email = "E-Mail";
$l_coord = "Details";

// Labels and texts
$l_comment = "Comment";
$l_contact_new = "New Contact";
$l_deal_new = "New Deal";
$l_project_new = "New Projet";
$l_contract_new = "New Contract";
$l_kind_manage = "Publication Kind Management";
$l_kind_exist = "Existing Kinds";
$l_kind_new = "New Kind";
$l_internal = "Internal";
$l_cat_manage = "Publication Category Management";
$l_cat_exist = "Existing Categories";
$l_cat_new = "New Category";
$l_cat_label = "Label";
$l_cat_code = "Code";
$l_act_manage = "Publication Activity Management";
$l_act_exist = "Existing Activities";
$l_act_new = "New Activity";
$l_add = "Add";
// Titres
$l_publication = "Publication";

// Actions
$l_insert = "Insert the Publication";
$l_update = "Update the Publication";
$l_checkdelete = "Check or Delete the Publication";
$l_delete = "Delete the Publication";
$l_kind_update = "Update Kind";
$l_kind_checkdelete = "Check or Delete Kind";
$l_kind_delete = "Delete Kind";
$l_kind_insert = "Insert Kind";

$l_cat_update = "Update Category";
$l_cat_checkdelete = "Check or Delete category";
$l_cat_delete = "Delete category";
$l_cat_insert = "Insert category";

$l_act_update = "Update Activity";
$l_act_checkdelete = "Check or Delete Activity";
$l_act_delete = "Delete Activity";
$l_act_insert = "Insert Activity";
$l_select_publication = "Select the Publication";

// Messages
$l_err_act_empty = "You must fill the publication activity !";
$l_cat_no = "No categories defined";
$l_no_display = "Fill the search fields and press < Find > to retrieve the companies";
$l_found = "companies found.";
$l_no_found = "No publication found matching the query !";
$l_delete_ok = "The publication has been deleted successfully.";
$l_delete_error = "The deletion of the publication has failed !";
$l_insert_ok = "The publication has been inserted successfully.";
$l_insert_error = "The insertion of the publication has failed !";
$l_check_samepublication = "These companies seem similar to this new entry !";
$l_update_ok = "The publication has been updated successfully.";
$l_update_error = "The update of the publication has failed !";
$l_query_error = "Critical trouble during the execution of the query.";
$l_link_contact = "These contacts are from this publication";
$l_link_contact_no = "The publication hasn't any contacts";
$l_link_deal = "The publication is involved in these deals";
$l_link_deal_no = "The publication isn't involved in any deal";
$l_link_contract = "The publication is involved in these contracts";
$l_link_contract_no = "The publication isn't involved in any contract";
$l_link_project = "The publication is involved in these projects";
$l_link_project_no = "The publication isn't involved in any projects";
$l_cant_delete = "The publication can't be deleted !";
$l_can_delete = "The publication can be deleted.";
$l_kind_insert_ok = "The kind has been inserted successfully.";
$l_kind_insert_error = "The insertion of the kind has failed !";
$l_kind_delete_ok = "The kind has been deleted successfully.";
$l_kind_delete_error = "The deletion of the kind has failed !";
$l_kind_update_ok = "The kind has been updated successfully.";
$l_kind_update_error = "The update of the kind has failed !";
$l_kind_link_publication = "These companies are referenced as";
$l_kind_link_publication_no = "No publication referenced as";
$l_kind_cant_delete = "The Kind can't be deleted !";
$l_kind_can_delete = "The Kind can be deleted.";

$l_cat_insert_ok = "The category has been inserted successfully.";
$l_cat_insert_error = "The insertion of the category has failed !";
$l_cat_delete_ok = "The category has been deleted successfully.";
$l_cat_delete_error = "The deletion of the category has failed !";
$l_cat_update_ok = "The category has been updated successfully.";
$l_cat_update_error = "The update of the category has failed !";
$l_cat_link_publication = "These companies are referenced as";
$l_cat_link_publication_no = "No publication referenced as";
$l_cat_cant_delete = "The category can't be deleted !";
$l_cat_can_delete = "The category can be deleted.";

$l_act_delete_ok = "Activity has been deleted successfully.";
$l_act_delete_error = "Activity deletion has failed !";
$l_act_insert_ok = "Activity has been inserted successfully.";
$l_act_insert_error = "Activity insertion has failed !";
$l_act_update_ok = "Activity has been updated successfully.";
$l_act_update_error = "Activity update has failed !";
$l_act_link_publication = "These companies are referenced as";
$l_act_link_publication_no = "No publication from activity";
$l_act_cant_delete = "Activity can't be deleted !";
$l_act_can_delete = "Activity can be deleted.";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_delete_confirm = "Really delete the publication ?";
$l_fill_name = "You have to fill the Name before submitting.";
$l_j_check_zip = "The zip code is not valid.";
$l_j_check_phone = "The phone number is not valid.";
$l_j_check_fax = "The fax number is not valid.";
$l_j_check_email = "The email adress is not valid.";
$l_j_check_web = "The web adress is not valid.";
$l_j_kind_delete = "You have to select a kind to check or delete it !";
$l_j_kind_update = "You have to select a kind and enter a new name to update it !";
$l_j_kind_create = "You have to enter a new kind name to create it !";

$l_j_cat_delete = "You have to select a category to check or delete it !";
$l_j_cat_update = "You have to select a category and enter a new name to update it !";
$l_j_cat_create = "You have to enter a new category name and code to create it !";
$l_j_cat_deep = "Categories can only have three levels.";

$l_j_act_delete = "You have to select an activity to check or delete it !";
$l_j_act_update = "You have to select an activity and enter a new name to update it !";
$l_j_act_create = "You have to enter a new activity name to create it !";
$l_j_select_publication = "You have to select a publication !";

// For the display parameters modification screen
$l_publication_display = "Display options for companies lists"; 
