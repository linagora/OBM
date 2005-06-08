<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Deutsch (German)                                         //
//     - File     : deal.inc                                                 //
// 1999-01-26 Pierre Baudracco                                               //
// 1999-12-06 / Fink / created from english version                          //
// 2001-07    / Fink / check                                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels and texts
$l_minus="-";
$l_plus="+";
$l_order="Auftrag";
$l_todo = "To Do";
$l_date_proposal="Angebotsdatum";
$l_amount_in="Amout of inputs";
$l_amount_out="Amout of outputs";
$l_potential = "Weighted Potential";
$l_label = "Label";
$l_label_start = "Label start with";
$l_kind = "Branche";
$l_project_manager = "Projektleiter";
$l_origin = "Origin";
$l_marketing_manager = "Vertriebsleiter";
$l_technical_manager = "Produktionsleiter";
$l_manager= "Manager";
$l_proposition = "Vorschlag";
$l_amount = "Bemerkung";
$l_hitrate = "Hit Rate";
$l_contacts = "Kontakte";
$l_show_archive = "anzeigen Archiv";
$l_date = "Datum";
$l_date_init = "Startdatum";
$l_date_alarm = "Stichdatum";
$l_date_alarm_after = "Alarm Date after";
$l_date_alarm_before = "Alarm Date before";
$l_after = "nach";
$l_before = "vor";
$l_last_update = "Letzte Änderung am";
$l_at = "bei";
$l_deal_select_company = "Bitte Firma auswählen";
$l_number_deal = "Vertragsnummer";
$l_affect_deal= "Hauptvertrag zuordnen";
$l_deal_select_parentdeal = "Hauptvertrag auswählen";
$l_parentdeal="Hauptvertrag";


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

// Titles
$l_deal = "Vertrag";
$l_deal_detail = "Vertrags-Detail";
$l_deal_new_company = "Firma";
$l_deal_new = "neuer Vertrag";
$l_deal_insert = "Vertrag anlegen";
$l_deal_update = "Vertrag ändern";
$l_deal_delete = "Vertrag löschen";
$l_deal_archive_update = "Vertrag ablegen";
$l_parentdeal_infos = "Hauptvertrags-Information";
$l_parentdeal_deals = "Verträge dieses Hauptvertrags";

// Actions
$l_insert = "anlegen Vertrag";
$l_update = "ändern Vertrag";
$l_delete = "löschen Vertrag";
$l_archive_update = "aktualisieren Archiv";
$l_select_company = "Firma auswählen";
$l_insert_parent = "anlegen Hauptvertrag";
$l_update_parent = "ändern Hauptvertrag";
$l_delete_parent = "löschen Hauptvertrag";
$l_affect = "zuordnen";

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
$l_link_contract = "The deal is involved in these contracts";
$l_link_contract_no = "The deal isn't involved in any contracts";
$l_link_project = "The deal is involved in these projects";
$l_link_project_no = "The deal isn't involved in any projects";
$l_cant_delete = "The deal can't be deleted !";
$l_can_delete = "The deal can be deleted.";

$l_updateaffect_ok = "Vertrag zugeordnet";
$l_found = "Verträge gefunden";
$l_no_found = "No Vertrag found matching the query !";
$l_delete_ok = "The vertrag has been deleted successfully.";
$l_delete_error = "The deletion of the vertrag has failed !";
$l_insert_ok = "The vertrag has been inserted successfully.";
$l_insert_error = "The insertion of the vertrag has failed !";
$l_update_ok = "The vertrag has been updated successfully.";
$l_update_error = "The update of the vertrag has failed !";
$l_archive_ok = "The archives have been updated successfully.";
$l_archive_error = "The update of the archives has failed !";
$l_archive_number = "vertrags inserted in the archives.";
$l_found_parent = "Hauptverträge gefunden";
$l_no_display_parent ="Fill the search fields and press < $l_find > to retrieve the parentdeals"; 
$l_no_found_parent ="No parentdeal found matching the query !";

$l_err_hitrate = "Hit rate must be in the range 0 - 100 !";



// JavaScript Messages (characters like \ have to be backslashed -> \\)
$l_fill_parentdeal = "\\n You have to select a parentdeal before submitting.";
$l_fill_label = "\\n You have to fill the Label before submitting.";
$l_fill_number = "\\n You have to fill the Number before submitting.";
$l_fill_date = "\\n You have to fill the Date before submitting.";
$l_invalid_date = "Invalid Date Format (MM/JJ/AAAA or AAAA-MM-JJ).";
$l_delete_confirm = "Confirm the vertrag deletion ?";


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
$l_deal_options = "Anzeige-Optionen für Verträge"; 
$l_parentdeal_options = "Anzeige-Optionen für Hauptverträge";

