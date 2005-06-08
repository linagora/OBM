<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Norwegian                                                //
//     - File     : deal.inc                                                 //
// 2000-07-20 Magni Onsoien                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// This translation has lots of errors due to misunderstandings. A better 
// version will come.

// Labels and texts
$l_minus="-";
$l_plus="+";
$l_order="Order";
$l_todo = "To Do";
$l_date_proposal="Proposal Date";
$l_amount_in="Amout of inputs";
$l_amount_out="Amout of outputs";
$l_potential = "Weighted Potential";
$l_label = "Namn";
$l_label_start = "namnet starter med";
$l_kind = "Type";
$l_project_manager = "Prosjektleiar";
$l_origin = "Opprinnelse";
$l_marketing_manager = "Selgar";
$l_technical_manager = "Teknisk ansvarleg";
$l_manager= "Manager";
$l_proposition = "Forslag";
$l_amount = "Sum";
$l_contacts = "Kontaktar";
$l_company = "Firma";
$l_show_archive = "Vis arkiv";
$l_date = "Dato";
$l_date_init = "Startdato";
$l_date_alarm = "Varslingsdato";
$l_date_alarm_after = "Varslingsdato etter";
$l_date_alarm_before = "Varslingsdato før";
$l_after = "Etter";
$l_before = "Før";
$l_last_update = "Sist oppdatert";
$l_at = "at";

$l_deal_select_company = "Velg firma det skal inngås avtale med.";
$l_number_deal="Number";
$l_type_manage = "Deal Kind Management";
$l_type_exist = "Existing Kinds";
$l_type_new = "New Kind";
$l_status_manage = "Deal Status Management";
$l_status_exist = "Existing States";
$l_status_new = "New State";
$l_category_manage = "Deal Category Management";
$l_category_exist = "Existing Categories";
$l_category_new = "New Category";
$l_category_label="Label";
$l_category_minilabel="Mini Label";
$l_affect_deal= "Affect to a parentdeal";
$l_deal_select_parentdeal = "Select the parentdeal whom the deal must be affected";
$l_parentdeal="ParentDeal";


// Titles
$l_deal = "Avtale";
$l_deal_detail = "Avtaledetaljer";
$l_deal_new_company = "Firma for ny avtale";
$l_deal_new = "Ny avtale";
$l_deal_insert = "Legg til avtale";
$l_deal_update = "Oppdater avtale";
$l_deal_delete = "Slett avtale";
$l_deal_archive_update = "Arkiver avtale";
$l_parentdeal_infos = "ParentDeal's information";
$l_parentdeal_deals = "Deals belonging to this parentdeal";


// Actions
$l_insert = "Legg til avtale";
$l_update = "Oppdater avtalen";
$l_delete = "Slett avtalen";
$l_archive_update = "Oppdater arkiv";
$l_select_company = "Velg firma";
$l_insert_parent = "Insert the ParentDeal";
$l_update_parent = "Update the ParentDeal";
$l_delete_parent = "Delete the ParentDeal";
$l_affect = "Affect";


$l_type_delete = "Delete Kind";
$l_type_update = "Update Kind";
$l_type_insert = "Insert Kind";
$l_status_update = "Update State";
$l_status_delete = "Delete State";
$l_status_insert = "Insert State";
$l_category_delete = "Delete Category";
$l_category_update = "Update Category";
$l_category_insert = "Insert Category";

// Messages
$l_updateaffect_ok = "The deal has been successfully affected to a parentdeal";
$l_error_visibility = "You can't consult this page : the information is private.";
$l_no_display = "Skriv inn søkekriteria og trykk < Finn > for å finne avtalane";
$l_found = "found (to translate) !";
$l_no_found = "Ingen avtalar som passa til søket !";
$l_delete_ok = "Avtalen er sletta.";
$l_delete_error = "Avtalen kunne ikkje slettast.";
$l_insert_ok = "Avtalen er lagt til.";
$l_insert_error = "Avtalen kunne ikkje leggast til.";
$l_update_ok = "Avtalen er oppdatert.";
$l_update_error = "Avtalen kunne ikkje oppdaterast.";
$l_query_error = "Problem med å utføre søket.";
$l_archive_ok = "Arkivet er oppdatert.";
$l_archive_error = "Arkivet kunne ikkje oppdaterast.";
$l_archive_number = "avtaler lagt til i arkivet.";
$l_found_parent = "parentdeals found";
$l_no_display_parent ="Fill the search fields and press < Find > to retrieve the parentdeals"; 
$l_no_found_parent ="No parentdeal found matching the query !";

$l_error_permission = "You have not the rights to access this page.";
$l_type_insert_ok = "The kind has been inserted successfully.";
$l_type_insert_error = "The insertion of the kind has failed !";
$l_type_update_ok = "The kind has been updated successfully.";
$l_type_update_error = "The kind of the deal has failed !";
$l_type_delete_error = "The deletion of the kind has failed !";
$l_type_del_verif_error = "(ies) of this kind exist.";
$l_type_delete_ok = "The kind has been deleted successfully.";

$l_status_insert_ok = "The state has been inserted successfully.";
$l_status_insert_error = "The insertion of the state has failed !";
$l_status_update_ok = "The State has been updated successfully.";
$l_status_update_error = "The state of the deal has failed !";
$l_status_del_verif_error = "(ies) of this status exist.";
$l_status_delete_error = "The deletion of the state has failed !";
$l_status_delete_ok = "The state has been deleted successfully.";

$l_category_insert_ok = "The category has been inserted successfully.";
$l_category_insert_error = "The insertion of the category has failed !";
$l_category_update_ok = "The category has been updated successfully.";
$l_category_update_error = "The category of the deal has failed !";
$l_category_delete_error = "The deletion of the category has failed !";
$l_category_del_verif_error = "(ies) of this categorie exist.";
$l_category_delete_ok = "The category has been deleted successfully.";


// JavaScript Messages (characters like \ have to be backslashed -> \\)
$l_fill_parentdeal = "\\n You have to select a parentdeal before submitting.";
$l_fill_label = "\\n Du må fylle inn namn før du legg inn.";
$l_fill_number = "\\n You have to fill the Number before submitting.";
$l_fill_date = "\\n Du må fylle inn dato før du legg inn.";
$l_j_select_company = "Du må velge eit firma.";
$l_invalid_date = "Ugyldig datoformat (MM/DD/YYYY eller YYYY-MM-DD).";
$l_delete_confirm = "Slette avtale?";

$mj_type_delete_confirm = "Confirm the deal kind deletion ?";
$mj_type_delete = "You have to select a kind to delete it !";
$mj_status_delete_confirm = "Confirm the deal status deletion ?";
$mj_status_delete = "You have to select a state to delete it !";
$mj_category_delete_confirm = "Confirm the deal category deletion ?";
$mj_category_delete = "You have to select a category to delete it !";
$mj_type_update = "You have to select a kind and enter a new name to update it !";
$mj_status_update = "You have to select a state and enter a new name to update it !";
$mj_category_update = "You have to select a category and enter a new name to update it !";
$mj_type_create = "You have to enter a new kind name to create it !";
$mj_status_create = "You have to enter a new state name to create it !";
$mj_category_create = "You have to enter a new category name to create it !";

// For the display parameters modification screen
$l_deal_options = "Display options for deals list"; 
$l_parentdeal_options = "Display options for parentdeals list";
