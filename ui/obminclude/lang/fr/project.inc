<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : projet.inc                                               //
// 2003-07-10 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_number = "Numéro";
$l_name = "Nom";
$l_label = "Nom";
$l_company_name = "Nom Société";
$l_tasktype = "Catégorie";
$l_archive = "Archive";
$l_soldtime = "Temps vendu";

$l_sold = "Vendu";
$l_allotted = "Attribué";
$l_projected = "Prévu";
$l_used = "Utilisé";
$l_remaining = "Restant";
$l_missing = "Manquant";
$l_prevmissing = "Ancienne estimation";
$l_nextmissing = "Nouvelle estimation";
$l_diff = "Avance/Retard";

$l_allosold = "Attribution du temps vendu";
$l_usedsold = "Utilisation du temps vendu";
$l_progress = "Avancement du projet";
$l_progeval = "Evaluation de l'avance retard";

// for list
$l_task_fac = "Taches Facturées";
$l_task_div = "Taches Diverses";
$l_task_int = "Tâches R&D";

// Labels et textes
$l_list = "Liste";
$l_taskname = "Nom";
$l_parent = "Tâche parent";
$l_manager = "Chef de projet";
$l_member = "Participant";
$l_soldtime = "Temps vendu";
$l_total = "Total";

// Titres
$l_dealinfos = "Partie affaire";
$l_projectinfos = "Partie Projet";
$l_externinfos = "Projet commercial";
$l_interninfos = "Projet interne";
$l_advanceinfos = "Avancement du projet";
$l_tasksinfos = "Sous-tâches du projet";
$l_membersinfos = "Participants au projet";

// Actions
$l_insert = "Créer le projet";
$l_update = "Modifier le Projet";
$l_adv_update = "Mettre à jour les avancements";
$l_checkdelete = "Vérifier ou Supprimer la Société";
$l_delete = "Supprimer la Société";
$l_kind_update = "Modifier le Type";
$l_kind_checkdelete = "Vérifier ou Supprimer le Type";
$l_kind_delete = "Supprimer le Type";
$l_kind_insert = "Créer le Type";
$l_act_update = "Modifier le secteur d'activité";
$l_act_checkdelete = "Vérifier ou Supprimer le secteur d'activité";
$l_act_delete = "Supprimer le secteur d'activité";
$l_act_insert = "Créer le secteur d'activité";
$l_confirm = "Confirmer l'action";
$l_select_company = "Sélectionner la société";

// Messages
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des projets correspondants.";
$l_new_projects = "Liste des projets non initialisés";
$l_found = "projets trouvés.";
$l_no_found = "Aucun projet ne correspond à ces critères !";
$l_no_tasks = "Il n'y a pas encore de sous-tâches définies";
$l_no_members = "Il n'y a pas de renseignements sur les participants au projet";
$l_delete_ok = "La suppression de la société a été correctement effectuée.";
$l_status_error = "Ce n'est pas un projet valide";
$l_delete_error = "La suppression de la société a échoué !";
$l_insert_ok = "La création du projet a été correctement effectuée.";
$l_insert_error = "La création du projet a échoué !";
$l_update_ok = "La modification du projet a été correctement effectuée";
$l_update_error = "La modification du projet a échoué !";
$l_adv_update_ok = "La mise à jour des avancements a été correctement effectuée";
$l_adv_update_error = "La mise à jour des avancements a échoué !";
$l_close_ok = "Le projet est maintenant marqué comme terminé";
$l_close_error = "Le changement d'état du projet à échoué !";
$l_query_error = "Problème en executant la requete";
$l_cant_delete = "La société ne peut être supprimée !";
$l_can_delete = "La société peut être supprimée.";
$l_member_add = "Ajouter des participants";
$l_task_add = "Ajouter une tâche";
$l_del_task_sel = "Enlever des tâches";
$l_task_added = "une tâche ajoutée";
$l_task_removed = "tâche(s) enlevé(s)";
$l_no_task_add = "Aucune tâche à ajouter";
$l_no_task_del = "Aucune tâche à effacer";
$l_valid_task = "Confirmer";
$l_del_member_sel = "Enlever ces participants";
$l_member_added = "participant(s) ajouté(s)";
$l_member_removed = "participant(s) enlevé(s)";
$l_no_member_add = "Aucun participant à ajouter";
$l_no_member_del = "Aucun participant à effacer";
$l_valid_member = "Confirmer";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_name = "Vous devez indiquer un nom avant de valider.";
$l_fill_tt = "Vous devez indiquer le type de tâche avant de valider.";
$l_fill_soldtime = "Vous devez indiquer le temps vendu avant de valider.";
$l_fill_tasklabel = "Vous devez donner un nom à cette tâche.";
$l_fill_soldnumber = "Le temps vendu doit être un nombre entier de jours.";
$l_fill_member = "Vous devez renseigner les participants au projet.";
$l_fill_projected = "Chaque participant avant doit être affecté à au moins une tâche";
$l_fill_projnumber = "Le temps prévu doit être un nombre entier de jours.";
$l_fill_missing = "Le temps manquant doit être indiqué pour tous les membres du projet";
$l_fill_missnumber = "Le temps manquant doit être un nombre entier de jours.";
$l_delete_confirm = "Confirmer la suppression de la Société ?";

// For the display preferences modification screen 
$l_searchproject_display = "Options d'affichage des listes de projets";
$l_newproject_display = "Options d'affichage de la liste de nouveaux projets";
