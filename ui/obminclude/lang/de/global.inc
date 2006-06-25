<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English for global                                       //
//     - File     : global.inc                                               //
// 2001-12-27 Pierre Baudracco                                               //
// 2003-10-29 / Fink / merged (merge.awk) english with german
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Common actions
$l_find = "Suche";
$l_back = "Zurück";
$l_cancel = "Abbruch";
$l_close = "Schließen";
$l_validate = "übernehmen";
$l_execute = "Ausführen";
$l_confirm = "Bestätigung";
$l_add = "Hinzufügen";

// Common messages
$l_yes = "Ja";
$l_no = "Nein";
$l_all = "Alle";
$l_all_f = "Alle";
$l_none = "---";
$l_undef = "---";
$l_id = "Id";
$l_comment = "Kommentar";
$l_add_comment = "Add a Kommentar";
$l_upd_comment = "Update Kommentar";
$l_mail_comment = "Email";
$l_members = "Members";
$l_rights = "Rights";
$l_default = "Default";
$l_status = "Status";
$l_archive = "Archiv";
$l_archive_first = "A";
$l_date = "Date";
$l_datebegin = "Date begin";
$l_dateend = "Date End";
$l_end = "End";
$l_alarm = "Stichdatum";
$l_text = "Text";
$l_name = "Name" ;
$l_label = "Label" ;
$l_invalid_data = "Falsche Daten";
$l_error_permission = "keine Berechtigung";
$l_error_visibility = "private Information";
$l_color = "Farbe";
$l_phone = "Telefon";
$l_fax = "Telefax";
$l_address = "Adresse";
$l_postcode = "PLZ";
$l_town = "Ort";
$l_expresspostal = "Eilzustellung";
$l_country = "Land";
$l_desc = "Beschreibung";
$l_priority = "Priorität";
$l_relation = "Relation";
$l_datasource = "Data source";
$l_visibility = "Zugriff";
$l_private = "Privat";
$l_public = "Öffentlich";
$l_local = "Local";
$l_ext_id = "Id ext";
$l_aka = "Also known as";
$l_category1 = "Kategorie 1";
$l_category2 = "Kategorie 2";
$l_aduplicate = "Duplicate";

// Entities
$l_company = "Firma";
$l_contact = "Contact";
$l_lead = "Lead";
$l_deal = "Verträge";
$l_list = "Liste";
$l_event = "Event";
$l_publication = "Publication";
$l_todo = "A faire";
$l_time = "Gestion du temps";
$l_project = "Projet";
$l_contract = "Contrat";
$l_incident = "Incident";
$l_document = "Document";
$l_account = "Compte";
$l_invoice = "Facture";
$l_payment = "Paiement";
$l_user = "Utilisateur";
$l_resource = "Resource";
$l_group = "Groupe";
$l_resourcegroup = "Groupe de ressources";
$l_settings = "Configuration";

// OBM_DISPLAY
$l_entries = "Einträge";
$l_page_res = "Ergebnisse";
$l_export = "Export";
$l_export_all = "Gesamtexport";
$l_field = "Feld";
$l_displayed = "angezeigt";
$l_order = "Reihenfolge";
$l_move = "Verschieben";
$l_mandatory = "Feld muß angezeigt werden";
$l_display_no = "nicht anzeigen";
$l_display_yes = "anzeigen";
$l_down = "Feld eine Zeile nach oben";
$l_up = "Feld eine Zeile nach unten";

// OBM_DISPLAY help screen
$l_help = "Hilfe";
$l_help_display_yes = "Feld wird angezeigt";
$l_help_display_no = "Feld wird nicht angezeigt";
$l_help_mandatory = "Feld wird immer angezeigt";
$l_help_down = "Feld nach unten schieben";
$l_help_up = "Feld nach oben schieben";
$l_document_added = " Dokumente hinzugefügt";
$l_no_document_added = "keine Dokumente hinzugefügt";

// Javascript common messages
$l_fill_label = "Label muß eingetragen sein";
$l_fill_date = "Datum muß eingetragen sein";
$l_invalid_date = "Falsches Datumsformat";

// Tasktype groups
$l_tt = "Task Type";
$l_tt_sales = "abrechenbare Aufgaben";
$l_tt_research = "interne Aufgaben";
$l_tt_others = "nicht abrechenbare Aufgaben";

// days and months
$l_today = "Heute";
$l_monthsofyear = array (	'Januar','Februar',
				'März','April','Mai',
				'Juni','Juli','August',
				'September','Oktober','November',
				'Dezember');
$l_monthsofyearshort = array ('jan','feb','mar','apr','mai','jun',
			      'jul','aug','sep','okt','nov','dez');
$l_daysofweek = array('Sonntag','Montag','Dienstag','Mittwoch','Donnerstag','Freitag','Samstag');
$l_daysofweekshort = array('So','Mo','Di','Mi','Do','Fr','Sa');
$l_daysofweekfirst = array ('S','M','D','M','D','F','S');

$l_invalid_perm = "keine Berechtigung";

////////////////////////////////////////////////////////
////// Theme headers lang files ////////////////////////
////////////////////////////////////////////////////////

$l_section_com    = "Verwaltung";
$l_section_prod   = "Produktion";
$l_section_compta = "Buchhaltung";
$l_section_user   = "Benutzer";
$l_section_admin  = "Administration";

$l_module_company = "Firmen";
$l_module_contact = "Kontakte";
$l_module_lead = "Lead";
$l_module_deal = "Verträge";
$l_module_list = "Maillisten";
$l_module_agenda = "Kalender";
$l_module_todo    = "Aufgaben";
$l_module_publication  = "Veröffentlichungen";
$l_module_statistic    = "Statistiken";

$l_module_time = "Zeiterfassung";
$l_module_project = "Projekt";
$l_module_contract = "Kontrakt";
$l_module_incident = "Vorfall";
$l_module_document = "Dokument";

$l_module_account = "Konto";
$l_module_invoice = "Rechnung";
$l_module_payment = "Zahlung";

$l_module_settings = "Einstellungen";
$l_module_user = "Benutzer";
$l_module_group = "Gruppe";

$l_module_admin = "Administration";
$l_module_admin_lang = "Sprache";
$l_module_admin_data = "Daten";
$l_module_admin_pref = "Eigenschaften";
$l_module_admin_code = "Code";
$l_module_admin_ref = "Referential";

$l_header_new_f = "Neu";
$l_header_new = "Neu";
$l_header_find = "Suche";
$l_header_display = "Ansicht";
$l_header_global_export = "Export";
$l_header_export = "Export";
$l_header_consult = "?Consult";
$l_header_duplicate = "Duplicate";

$l_header_update = "Ändern";
$l_header_delete =  "Löschen";
$l_header_add_contact = "Neuer Kontakt";
$l_header_add_user = "Neuer Benutzer";
$l_header_add_group = "Neue Gruppe";
$l_header_add_deal = "Neuer Vertrag";
$l_header_list = "Liste";

$l_header_new_child = "Neuer Untervertrag";
$l_header_new_parent = "Neuer Hauptvertrag";

$l_header_reconcile =  "?reconcile";
$l_header_compute_balance =  "?compute balance?";
$l_header_duplicate =  "?duplicate";
$l_header_add_contact = "Neuer Kontakt";

// Project headers
$l_header_man_task = "Aufgaben";
$l_header_man_member = "Mitglieder";
$l_header_advance = "Fortschritt";
$l_header_man_affect = "?affect";
$l_header_add_member = "neues Mitglied";

// Admin headers
$l_header_index = "Main";
$l_header_help = "Hilfe";
$l_header_amp = "Show amp";
$l_header_func_unused = "Functions";
$l_header_clear_sess = "Clear sessions";
$l_header_pref_update = "Reset users preferences";
$l_header_reset = "?reset";
$l_header_country = "Country";
$l_header_datasource = "Data Source";
$l_header_tasktype = "Aufgabe Type";

$l_header_year = "Jahr";
$l_header_day = "Tag";
$l_header_week = "Woche";
$l_header_month = "Monat";
$l_header_new_event = "Neues Ereignis";

//time headers
$l_header_stats = "Statistik";
$l_header_statsmonth = "Monatsstatistik";
$l_header_statsuser = "Benutzerstatistik";
$l_header_weeklyview = "Wochenansicht";
$l_header_monthlyview = "Monatsansicht";
$l_header_globalview = "?globalview";

//document headers
$l_document_new = "Neues Dokument";
$l_document_add = "Dokument hinzufügen";

// Debug
$l_dg_id = "Id";
$l_dg_param = "Parameters";
$l_dg_sess = "Session";
$l_dg_sql = "SQL";