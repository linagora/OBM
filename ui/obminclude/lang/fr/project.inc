<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : projet.inc                                               //
// 2003-07-10 Bastien Continsouzas                                           //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_company = "Société";
$l_tasktype = "Catégorie";
$l_task = "Tâche";
$l_soldtime = "Temps vendu";
$l_estimatedtime = "Temps estimé";
$l_datebegin = "Date de début";
$l_dateend = "Date de fin";

$l_sold = "Vendu";
$l_estimated = "Estimé";
$l_allotted = "Attribué";
$l_projected = "Prévu";
$l_used = "Utilisé";
$l_missing = "Estimé restant";

$l_alloestimated = "Attribution du temps prévu";
$l_usedestimated = "Utilisation du temps prévu";
$l_progress = "Avancement";
$l_progeval = "Evaluation de l'avance/retard";

// Labels et textes
$l_parent = "Tâche parent";
$l_manager = "Chef de projet";
$l_member = "Participant";
$l_soldtime = "Temps vendu";
$l_projtime = "Temps prévu";
$l_total = "Total";
$l_resume = "Résumé";
$l_tasks = "tâches";
$l_members = "participants";
$l_deal = "Affaire";

// Titres
$l_project = "Projet";
$l_members_info = "Participants au projet";
$l_progressinfos = "Détail : (temps : utilisé / estimé restant / total prévu)";
$l_memberstimeinfos = "Renseignez le temps de travail (en jours) prévu pour chaque personne assignée à une tâche";
$l_task_new = "Nouvelle tâche";
$l_task_list = "Tâches définies";
$l_progress_info = "Renseignez le temps de travail (en jours) estimé pour terminer une tâche";

// Actions
$l_insert = "Créer le projet";
$l_update = "Modifier le Projet";
$l_delete = "Supprimer le projet";
$l_task_add = "Ajouter la tâche";
$l_task_del = "Supprimer les tâches sélectionnées";
$l_progress_update = "Mettre à jour les avancements";

// Messages
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des projets correspondants.";
$l_found = "projet(s) trouvés.";
$l_no_found = "Aucun projet ne correspond à ces critères !";
$l_no_tasks = "Pas de tâches définies";
$l_no_members = "Pas de participants définis";
$l_no_allocation = "Les participants du projet n'ont pas été affectés à des tâches";
$l_insert_ok = "La création du projet a été correctement effectuée.";
$l_insert_error = "La création du projet a échoué !0000";
$l_update_ok = "La modification du projet a été correctement effectuée";
$l_update_error = "La modification du projet a échoué !";
$l_delete_ok = "La suppression du projet a été correctement effectuée";
$l_delete_error = "La suppression du projet a échoué !";
$l_task_insert_ok = "La création de la tâche a été correctement effectuée.";
$l_task_insert_error = "Impossible de créer cette tâche; La tâche parente est déjà assignée à des utilisateurs";
$l_task_delete_ok = "La suppression des tâches a été correctement effectuée.";
$l_task_delete_error = "Impossible de supprimer une tâche ; Quelqu'un y est affecté (ou à ses filles), ou la tâche est référencée en gestion du temps";
$l_member_delete_error = "Impossible d'enlever ce participant ; Il est affecté à une tâche du projet ou a déjà passé du temps sur le projet.";
$l_allocate_update_ok = "La mise à jour des affectations a été correctement effectuée";
$l_allocate_update_error = "La mise à jour des affectations a échoué !";
$l_progress_update_ok = "La mise à jour des avancements a été correctement effectuée";
$l_progress_update_error = "La mise à jour des avancements a échoué !";
$l_query_error = "Problème en executant la requete";
$l_no_task_del = "Aucune tâche à effacer";
$l_del_member_sel = "Enlever ces participants";
$l_member_added = "participant(s) ajouté(s)";
$l_member_removed = "participant(s) enlevé(s)";
$l_no_member_add = "Aucun participant à ajouter";
$l_no_member_del = "Aucun participant à effacer";
$l_add_member = "Ajouter des participants au projet";

$l_link_timetask = "Le projet est renseigné dans la gestion du temps";
$l_link_timetask_no = "Le projet n'est pas renseigné dans la gestion du temps";
$l_cant_delete = "Le projet ne peut être supprimé !";
$l_can_delete = "Le projet peut être supprimé.";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_name = "Vous devez indiquer un nom avant de valider.";
$l_fill_tt = "Vous devez indiquer la catégorie avant de valider.";
$l_fill_soldtime = "Vous devez indiquer le temps vendu avant de valider.";
$l_fill_estimated = "Vous devez indiquer le temps estimé avant de valider.";
$l_fill_tasklabel = "Vous devez donner un nom à cette tâche.";
$l_fill_soldnumber = "Le temps vendu doit être un nombre entier de jours.";
$l_fill_projected = "Chaque participant avant doit être affecté à au moins une tâche";
$l_fill_projnumber = "Le temps prévu doit être un nombre entier de jours.";
$l_fill_missing = "Le temps manquant doit être indiqué pour tous les membres du projet";
$l_fill_missnumber = "Le temps manquant doit être un nombre entier de jours.";
$l_delete_confirm = "Confirmer la suppression du projet ?";

// For the display preferences modification screen 
$l_project_display = "Options d'affichage des listes de projets";
