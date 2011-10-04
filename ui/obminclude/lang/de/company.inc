<script language="php">
//////////////////////////////////////////////////////////////////////////////
// OBM - Language : Deutsch (German)
//     - File     : company.inc
// 1999-01-25 Pierre Baudracco
// 1999-12-06 / Fink / created from english version
// 2000-01-10 / Fink / check
// 2001-07    / Fink / check for version 0.4
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Modified on 2009-09-24T17:31:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-29T15:20:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-29T16:02:38+02:00 using OBMtranslation (0.4.1)

$l_number = "Nummer";
$l_company_name = "Firma";
$l_type = "Firmenart";
$l_activity = "Bereich"; 
$l_market = "Marketing manager";
$l_postcode = "Postleitzahl";
$l_contact_first = "K";
$l_newcontact_first = "NK";
$l_listdoc_first = "D";
$l_newdoc_first = "ND";
$l_address = "Adresse";
$l_postcode_first = "PLZ";
$l_web = "Web";
$l_contact_new = "Neuer Kontakt";
$l_deal_new = "Neuer Vertrag";
$l_project_new = "Neuer Project";
$l_contract_new = "Neuer Contract";
$l_internal = "Intern";
$l_cat_tree = "Category tree";
$l_company_detail = "Firmen-Daten";
$l_company_new = "Neue Firma";
$l_company_insert = "Firma anlegen";
$l_company_update = "Firma ändern";
$l_company_delete = "Firma löschen";
$l_company_admin = "Firma verwalten";
$l_insert = "Speichern Firma"; 
$l_update = "ändern Firma";
$l_delete = "löschen Firma"; 
$l_link_contact = "Das Unternehmen hat Kontakte."; 
$l_link_contact_no = "Das Unternehmen hat keine Kontakten"; 
$l_link_deal = "Das Unternehmen wird durch die Business verwiesen"; 
$l_link_deal_no = "Das Unternehmen ist nicht im Geschäft verwiesen"; 
$l_link_contract = "Das Unternehmen ist in den Verträgen verwiesen."; 
$l_link_contract_no = "Das Unternehmen ist nicht in den Verträgen verwiesen"; 
$l_link_project = "Das Unternehmen wird durch Projekte verwiesen"; 
$l_link_project_no = "Das Unternehmen ist nicht im Projekt verwiesen"; 
$l_cant_delete = "Das Unternehmen kann nicht gelöscht werden!"; 
$l_can_delete = "Das Unternehmen kann löschen."; 
$l_company_del_verif_error = " es sind noch Verträge zugeordnet: Firma kann nicht gelöscht werden";
$l_found = "Firmen gefunden.";
$l_no_found = "Keine Firma entsprechen zum Suchkriterium !"; 
$l_fill_name = "Sie müssen den Namen füllen, bevor die Validierung."; 
$l_fill_number = "\\n Nummer muß eingetragen sein.";
$l_fill_postcode = "\\n Postleitzahl muß eingetragen sein.";
$l_fill_address = "\\n Adresse muß eingetragen sein.";
$l_fill_phone = "\\n Telefonnummer muß eingetragen sein.";
$l_delete_confirm = "Bestätigen Sie das Löschen des Unternehmen ?"; 
$lj_check_zip = "Postleitzahl ungültig";
$lj_check_phone = "Telefonnummer ungültig";
$lj_check_pphone = "Telefonnummer privat ungültig";
$lj_check_mphone = "Telefonnummer mobil ungültig";
$lj_check_fax = "Telefaxnummer ungültig";
$lj_check_email = "E-Mail-Adresse ungültig";
$lj_check_web = "Web-Adresse ungültig";
$l_company_options = "Anzeige-Optionen für Firma";
$l_j_select_company = "Sie müssen eine Firma wählen !"; 
$l_j_check_zip = "Die Postleitzahl ist falsch."; 
$l_coord = "Kontakt"; 
$l_naf_link_company = "Gesellschaften sind im Code NAF"; 
$l_naf_cant_delete = "NAF-Code kann nicht gelöscht werden!"; 
$l_j_check_email = "Die E-Mail ist nicht gültig. Überprüfen Sie das Feld."; 
$l_j_check_phone = "Die Telefonnummer ist nicht korrekt."; 
$l_naf_manage = "NAF Codes Management"; 
$l_j_naf_update = "Sie müssen einen NAF-Code auswählen und informieren eine Veränderung ein, bevor die Validierung !"; 
$l_website_labels = array ('URL' => 'Website', 'OTHER' => 'Andere'); 
$l_naf_delete = "Löschen Sie den Code NAF"; 
$l_j_cat_deep = "Die Kategorien können nicht mehr als drei Ebenen tief."; 
$l_j_naf_delete = "Wählen Sie einen NAF-Code, um ihn zu überprüfen oder zu löschen!"; 
$l_j_check_web = "Die Internet-Adresse ist nicht gültig."; 
$l_company_display = "Anzeigeoptionen Liste der Unternehmen"; 
$l_naf_can_delete = "NAF-Code kann gelöscht werden"; 
$l_naf_istitle = "ist ein Titel"; 
$l_naf_exist = "bestehende NAF-Code"; 
$l_naf_no = "Keine definierte NAF-Code"; 
$l_link_invoice = "Das Unternehmen wird von Rechnungen verwiesen"; 
$l_nafcode = "NAF-Code"; 
$l_naf_checkdelete = "Prüfen oder Löschen Code NAF"; 
$l_email = "E-mail"; 
$l_vat = "Umsatzsteuer-Identifikationsnummer"; 
$l_email_labels = array ('INTERNET' => 'Internet', 'OTHER' => 'Andere'); 
$l_j_check_fax = "Die Fax-Nummer ist nicht richtig."; 
$l_no_display = "Informieren Sie die gewünschten Kriterien ein und klicken Sie auf <Suchen> um eine Liste der entsprechenden Firmen zu haben."; 
$l_check_samecompany = "Diese Unternehmen scheinen vergleichbar mit diesem neuen Eintrag !"; 
$l_checkdelete = "Prüfen oder Löschen das Unternehmen"; 
$l_link_invoice_no = "Das Unternehmen ist nicht in den Rechnungen verwiesen"; 
$l_j_naf_create = "Sie müssen einen NAF-Code eingeben, um es zu schaffen !"; 
$l_siret = "SIRET"; 
$l_comment = "Kommentar"; 
$l_naf_code = "Code"; 
$l_err_act_empty = "Der Bereich muss ausgefüllt werden !"; 
$l_naf_insert = "Erschaffen NAF-Code"; 
$l_add = "Hinzufügen"; 
$l_select_company = "Wählen Sie das Unternehmen"; 
$l_naf_label = "Etikett"; 
$l_address_labels = array ('HQ' => 'HQ', 'OTHER' => 'Andere'); 
$l_naf_update = "Ändern Sie den NAF-Code "; 
$l_naf_link_company_no = "Kein Unternehmen im Zusammenhang mit diesem NAF-Code"; 
$l_naf_new = "Neue NAF-Code"; 
$l_phone_labels = array ('WORK_VOICE' => 'Telefon', 'CELL_VOICE' => 'Mobil', 'WORK_FAX' => 'Fax', 'OTHER' => 'Andere'); 

