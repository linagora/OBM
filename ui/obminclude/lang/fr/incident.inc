<SCRIPT LANGUAGE="PHP">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : incident.inc                                             //
// 2001-08-20  Mehdi RANDE                                                   //
///////////////////////////////////////////////////////////////////////////////
//   $Id   //
///////////////////////////////////////////////////////////////////////////////

//Fields Label
$l_incident_label = "Label";
$l_incident_priority = "Priorité";
$l_incident_status = "Etat";
$l_incident_date = "Date";
$l_incident_logger = "Récepteur";
$l_incident_owner = "Responsable Technique";
$l_incident_description = "Description";
$l_incident_solution = "Solution apportée";
$l_contract_num = "Numéro de Contrat";
$l_contract_label = "Contrat";
$l_contract_date_init = "Date Debut";
$l_contract_date_expir = "Date Expiration";
$l_to = "Au"; 
$l_from = "Du";
$l_manager = "Responsables";
$l_client_manager = "Responsables Client";
$l_date_before ="Date Avant";
$l_date_after ="Date Apr&egrave;s";
$l_archive_new = "Archiver";
$l_archive = "Voir les archives";
$l_archive_consult = "Archivé";
$l_incident_company = "Société";

//Title 
$l_incident_select_contract = "Sélectionner le Contrat de l'Incident.";
$l_redhot = "Très Urgent";
$l_hot = "Urgent";
$l_normal = "Normal";
$l_low = "Basse";
$l_open = "Ouvert";
$l_call = "Appel en abscence";
$l_waitcall = "Attente de retour";
$l_paused = "En Attente";
$l_closed = "Fermé";
$l_incident_lastupdate = "Date de dernière modification";

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames["incident_state"] = $l_incident_status;
$fieldnames["incident_label"] = $l_incident_label;
$fieldnames["incident_priority"] = $l_incident_priority;
$fieldnames["incident_date"] = $l_incident_date;
// Calculate fields
$fieldnames["timeupdate"] = $l_incident_lastupdate;
$fieldnames["incident_owner_lastname"] = $l_incident_owner;
$fieldnames["incident_logger_lastname"] = $l_incident_logger;
$fieldnames["incident_company_name"] = $l_incident_company;
// Action

$l_select_contract = "S&eacute;lectionner le contrat";
$l_insert = "Enregistrer l'incident";
$l_update = "Modifier l'incident";
$l_checkdelete = "Supprimer l'incident";
$l_incident_select_contract = "Selectionner le contrat lié à cet incident";
//Message
$l_error_permission = "Vous n'avez pas les droits d'acces à cette page.";
$l_found = "incident(s) trouv&eacute;(s).";
$l_no_found = "Aucun Incident ne correspond &agrave; ces crit&egrave;res !";
$l_insert_ok = "L'insertion de l'incident a été correctement effectuée.";
$l_no_display = "Renseigner les crit&egrave;res souhait&eacute;s et cliquer sur < Chercher > pour obtenir la liste des contrats de support correspondants.";
// For the display preferences modification screen 

$l_incident_display = "Options d'affichage des Incidents";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_label = "Vous devez renseigner le label avant de valider.";
$l_length_label = "Le champ Label est trop long";
$l_fill_contract = "Vous devez sélectionner un contrat avant de valider.";
$l_fill_date = "La date doit être renseignee.";
$l_invalid_date = "Format de date invalide (JJ/MM/AAAA ou AAAA-MM-JJ).";
$l_j_select_contract = "Vous devez sélectionner un Contrat!";
$l_delete_confirm="Confirmer la suppression de l'Incident ?";
$l_date_after_js ="Date Après";

$l_time_format = "%Y-%m-%d %H:%i:%s";


