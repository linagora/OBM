<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : incident.inc                                             //
// 2001-08-20 : Mehdi RANDE                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Fields Label
$l_status = "Etat";
$l_cat1 = "Catégorie";
$l_date = "Date";
$l_logger = "Récepteur";
$l_owner = "Responsable Technique";
$l_duration = "Durée totale de résolution";
$l_solution = "Solution apportée";
$l_client_manager = "Responsable Client";
$l_date_before ="Date Avant";
$l_date_after ="Date Après";
$l_archive_new = "Archiver";
$l_res_duration = "Durée de résolution";
$l_pri_manage = "Gestion des priorités d'incident";
$l_pri_exist = "Priorités existantes";
$l_pri_new = "Nouvelle";
$l_sta_manage = "Gestion des états d'incident";
$l_sta_exist = "Etats existants";
$l_sta_new = "Nouveau";
$l_cat1_manage = "Gestion des Catégories d'Incident";
$l_cat1_exist = "Catégories existants";
$l_cat1_new = "Nouvelle";

// Title 
$l_redhot = "Très Urgent";
$l_hot = "Urgent";
$l_normal = "Normal";
$l_low = "Basse";
$l_open = "Ouvert";
$l_call = "Appel en abscence";
$l_waitcall = "Attente de retour";
$l_paused = "En Attente";
$l_closed = "Fermé";
$l_lastupdate = "Date de dernière modification";

// Action
$l_select_contract = "Sélectionner le contrat";
$l_insert = "Enregistrer l'incident";
$l_update = "Modifier l'incident";
$l_checkdelete = "Supprimer l'incident";
$l_pri_update = "Modifier la priorité";
$l_pri_checkdelete = "Vérifier ou supprimer la priorité";
$l_pri_delete = "Supprimer la priorité";
$l_pri_insert = "Insérer la priorité";
$l_sta_update = "Modifier l'état";
$l_sta_checkdelete = "Vérifier ou supprimer l'état";
$l_sta_delete = "Supprimer l'état";
$l_sta_insert = "Insérer l'état";
$l_cat1_update = "Modifier la catégorie";
$l_cat1_checkdelete = "Vérifier ou supprimer la catégorie";
$l_cat1_delete = "Supprimer la catégorie";
$l_cat1_insert = "Insérer la catégorie";

// Mail
$l_mail_incident_new_subject = "OBM : Nouvel incident";

// Message
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des incidents correspondants.";
$l_found = "incident(s) trouvé(s).";
$l_no_found = "Aucun Incident ne correspond à ces critères !";
$l_insert_ok = "L'insertion de l'incident a été correctement effectuée.";
$l_update_ok = "La modification de l'incident a été correctement effectuée";
$l_delete_ok = "La suppression de l'incident a été correctement effectuée.";
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des incidents correspondants.";
$l_pri_insert_ok = "La priorité a été insérée avec succès.";
$l_pri_insert_error = "L'insertion de la priorité a échoué !";
$l_pri_update_ok = "La modification de la priorité a été correctement effectuée.";
$l_pri_update_error = "La modification de la priorité a échoué !";
$l_pri_delete_ok = "La suppression de la priorité a été correctement effectuée !.";
$l_pri_delete_error = "La suppression de la priorité a échoué !";
$l_pri_can_delete = "La priorité peut être supprimée !";
$l_pri_cant_delete = "La priorité ne peut être supprimée !";
$l_pri_link_incident = "La priorité est référencée par ces incidents";
$l_pri_link_incident_no = "La priorité n'est pas référencée";
$l_sta_insert_ok = "L'état a été inséré avec succès.";
$l_sta_insert_error = "L'insertion de l'état a échoué !";
$l_sta_update_ok = "La modification de l'état a été correctement effectuée.";
$l_sta_update_error = "La modification de l'état a échoué !";
$l_sta_delete_ok = "La suppression de l'état a été correctement effectuée !.";
$l_sta_delete_error = "La suppression de l'état a échoué !";
$l_sta_can_delete = "L'état peut être supprimé !";
$l_sta_cant_delete = "L'état ne peut être supprimé !";
$l_sta_link_incident = "L'état est référencé par ces incidents";
$l_sta_link_incident_no = "L'état n'est pas référencé";

$l_cat1_insert_ok = "La catégorie a été insérée avec succès.";
$l_cat1_insert_error = "L'insertion de la catégorie a échoué !";
$l_cat1_update_ok = "La modification de la catégorie a été correctement effectuée.";
$l_cat1_update_error = "La modification de la catégorie a échoué !";
$l_cat1_delete_ok = "La suppression de la catégorie a été correctement effectuée !.";
$l_cat1_delete_error = "La suppression de la catégorie a échoué !";
$l_cat1_can_delete = "La catégorie peut être supprimée !";
$l_cat1_cant_delete = "La catégorie ne peut être supprimée !";
$l_cat1_link_incident = "La catégorie est référencée par ces incidents";
$l_cat1_link_incident_no = "La catégorie n'est pas référencée";


// For the display preferences modification screen 
$l_incident_display = "Options d'affichage des Incidents";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_length_label = "Le champ Label est trop long";
$l_fill_contract = "Vous devez sélectionner un contrat avant de valider.";
$l_j_select_contract = "Vous devez sélectionner un Contrat!";
$l_delete_confirm = "Confirmer la suppression de l'Incident ?";
$l_date_after_js = "Date Après";
$l_j_pri_create = "Vous devez saisir une priorité pour la créer !";
$l_j_pri_update = "Vous devez sélectionner une priorité et entrer une modification avant de valider !";
$l_j_pri_delete = "Vous devez sélectionner une priorité pour l'effacer !";
$l_j_sta_create = "Vous devez saisir un état pour le créer !";
$l_j_sta_update = "Vous devez sélectionner un état et entrer une modification avant de valider !";
$l_j_sta_delete = "Vous devez sélectionner un état pour l'effacer !";
$l_j_cat1_create = "Vous devez saisir une actégorie pour la créer !";
$l_j_cat1_update = "Vous devez sélectionner une catégorie et entrer une modification avant de valider !";
$l_j_cat1_delete = "Vous devez sélectionner une catégorie pour l'effacer !";
