<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English for global                                       //
//     - File     : global.inc                                               //
// 2001-12-27 Pierre Baudracco                                               //
// 2003-10-29 / Fink / merged (merge.awk) english with german; fixed translations
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Common actions
$l_find = "Suche";
$l_back = "Zurück";
$l_cancel = "Abbruch";
$l_close = "Schließen";
$l_validate = "Prüfen";
$l_execute = "Ausführen";
$l_confirm = "Bestätigung";

// Common messages
$l_yes = "Ja";
$l_no = "Nein";
$l_all = "Alle";
$l_all_f = "Alle";
$l_none = "---";
$l_comment = "Kommentar";
$l_add_comment = "Add a Kommentar";
$l_upd_comment = "Update Kommentar";
$l_archive = "Archiv";
$l_archive_first = "A";
$l_date = "Date";
$l_name = "Name" ;
$l_label = "Label" ;
$l_invalid_data = "Falsche Daten";
$l_error_permission = "keine Berechtigung";
$l_error_visibility = "private Information";
$l_color = "Farbe";
$l_phone = "Telefon";
$l_address = "Adresse";
$l_postcode = "Postleitzahl";
$l_town = "Ort";
$l_expresspostal = "Eilzustellung";
$l_country = "Land";
$l_priority = "Priority";
$l_datasource = "Data source";
$l_cat = "Kategorie";
$l_visibility = "Zugriff";
$l_private = "Privat";
$l_public = "Öffentlich";

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
$l_invalid_date = "Falsches Datumsformat (ISO/DIN JJJJ-MM-TT oder lokale Einstellung)";

//used by many modules
//tasktype groups
$l_task_fac = "abrechenbare Aufgaben";
$l_task_int = "interne Aufgaben";
$l_task_div = "nicht abrechenbare Aufgaben";

// days and months
$l_monthsofyear = array (	'January','February',
				'March','April','May',
				'June','July','August',
				'September','October','November',
				'December');
$l_monthsofyearshort = array ('jan','feb','mar','apr','may','jun',
			      'jul','aug','sep','oct','nov','dec');
$l_invalid_perm = "keine Berechtigung";

////////////////////////////////////////////////////////
////// Theme headers lang files ////////////////////////
////////////////////////////////////////////////////////

$l_header_com    = "Verwaltung";
$l_header_prod   = "Produktion";
$l_header_compta = "Buchhaltung";
$l_header_users  = "Benutzer";
$l_header_admins = "Administration";

$l_header_company = "Firmen";
$l_header_contact = "Kontakte";
$l_header_deal = "Verträge";
$l_header_list = "Maillisten";
$l_header_agenda = "Kalender";
$l_header_todo    = "Aufgaben";

$l_header_time = "Zeiterfassung";
$l_header_project = "Projekt";
$l_header_contract = "Kontrakt";
$l_header_incident = "Vorfall";
$l_header_document = "Dokument";

$l_header_account = "Konto";
$l_header_invoice = "Rechnung";
$l_header_payment = "Zahlung";

$l_header_settings = "Einstellungen";
$l_header_user = "Benutzer";
$l_header_group = "Gruppe";

$l_header_admin = "Administration";
$l_header_admin_lang = "Sprache";
$l_header_admin_data = "Daten";
$l_header_admin_pref = "Eigenschaften";
$l_header_admin_code = "Code";
$l_header_admin_ref = "Referential";

$l_header_new_f = "Neu";
$l_header_new = "Neu";
$l_header_find = "Suche";
$l_header_display = "Display";
$l_header_global_export = "Export";
$l_header_export = "Export";
$l_header_consult = "?Consult";

$l_header_update = "Ändern";
$l_header_delete =  "Löschen";
$l_header_add_contact = "Neuer Kontakt";
$l_header_add_user = "Neuer Benutzer";
$l_header_add_group = "Neue Gruppe";
$l_header_add_deal = "Neuer Vertrag";
$l_header_todo_list = "Liste";

$l_header_new_child = "Neuer Untervertrag";
$l_header_new_parent = "Neuer Hauptvertrag";

$l_header_reconcile =  "?reconcile";
$l_header_compute_balance =  "?compute balance?";
$l_header_duplicate =  "?duplicate";
$l_header_add_contact = "Neuer Kontakt";

// Project headers
$l_header_man_task = "Aufgaben";
$l_header_man_member = "Mitglieder";
$l_header_man_advance = "Fortschritt";
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
$l_header_tasktype = "Task Type";

$l_header_year = "Jahr";
$l_header_day = "Tag";
$l_header_week = "Woche";
$l_header_month = "Monat";
$l_header_new_event = "Neues Ereignis";

//time headers
$l_header_stats = "Statistik";
$l_header_statsmonth = "Monatsstatistik";
$l_header_statsuser = "Benutzerstatistik";
$l_header_weeklyview = "Wochensicht";
$l_header_monthlyview = "Monatssicht";
$l_header_globalview = "?globalview";

//document headers
$l_document_new = "Neues Dokument";
$l_document_add = "Dokument hinzufügen";

