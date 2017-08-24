<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : projet.inc                                               //
// 2003-07-10 Aliacom - Bastien Continsouzas                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_tasktype = "Catégorie";
$l_task = "Tâche";
$l_soldtime = "Temps vendu";
$l_estimatedtime = "Temps estimé";
$l_shortname = "Nom court";

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

$l_ref = "Référence";
$l_project_date = "Date";
$l_project_duration = "Durée";
$l_project_desc = "Description";
$l_project_tech = "Description technique";

// Labels et textes
$l_parent = "Tâche parent";
$l_soldtime = "Temps vendu";
$l_projtime = "Temps prévu";
$l_total = "Total";
$l_resume = "Résumé";
$l_tasks = "Tâches";
$l_reftask_manage = "Gestion des taches de référence"; 
$l_delete_reftask = "Vous etes sur le point de supprimer une tache de référence, voulez vous continuer?";
$l_reftask = 'Tâche de référence';
$l_closing = 'Clôture';
$l_closing_no = 'Pas de clôture effectuée pour ce projet';

// Titres
$l_members_info = "Participants au projet";
$l_progressinfos = "Détail : (temps : utilisé / estimé restant / total prévu)";
$l_memberstimeinfos = "Renseignez le temps de travail (en jours) prévu pour chaque personne assignée à une tâche";
$l_fill_advance_form = "Renseignez le temps de travail (en jours) : prévu / estimé restant";
$l_task_new = "Nouvelle tâche";
$l_task_update = "Modifier une tâche";
$l_task_list = "Tâches définies";
$l_progress_info = "Renseignez le temps de travail (en jours) estimé pour terminer une tâche";
$l_select_project = "Sélectionner un projet";

// Mail
$l_mail_project_subject = "OBM : Projet";
$l_mail_project_member = "Vous avez été positionnés sur le projet suivant :";

// Actions
$l_insert = "Créer le projet";
$l_update = "Modifier le Projet";
$l_delete = "Supprimer le projet";
$l_task_add = "Ajouter la tâche";
$l_task_upd = "Modifier la tâche";
$l_task_del = "Supprimer les tâches sélectionnées";
$l_progress_update = "Mettre à jour les avancements";
$l_closing_insert = "Enregistrer la clôture";
$l_closing_update = "Modifier la clôture Projet";
$l_closing_delete = "Supprimer la clôture";

// Messages
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des projets correspondants.";
$l_found = "projet(s) trouvé(s).";
$l_no_found = "Aucun projet ne correspond à ces critères !";
$l_no_tasks = "Pas de tâches définies";
$l_no_members = "Pas de participants définis";
$l_no_allocation = "Les participants du projet n'ont pas été affectés à des tâches";
$l_link_timetask = "Le projet est renseigné dans la gestion du temps";
$l_link_timetask_no = "Le projet n'est pas renseigné dans la gestion du temps";
$l_cant_delete = "Le projet ne peut être supprimé !";
$l_can_delete = "Le projet peut être supprimé.";
$l_no_task_del = "Aucune tâche à effacer";
$l_ptask_same_task = "La tâche parente ne peut être la tâche elle-même !";
$l_ptask_and_parent = "La tâche est parente et ne peut donc avoir une tâche parente !";
$l_ptask_has_parent = "La tâche parente a elle-même un parent maintenant !";
$l_ptask_has_user = "La tâche parente est assignée à des utilisateurs !";

$l_del_member_sel = "Enlever ces participants";
$l_member_added = "participant(s) ajouté(s)";
$l_member_removed = "participant(s) enlevé(s)";
$l_no_member_add = "Aucun participant à ajouter";
$l_no_member_del = "Aucun participant à effacer";
$l_add_member = "Ajouter des participants au projet";
$l_board_no_data = "Le tableau de bord ne peut être affiché si les tâches ou les participants ne sont pas définis !";
$l_select_task = "Veuillez sélectionner un tâche";
// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_name = "Vous devez indiquer un nom avant de valider.";
$l_fill_tt = "Vous devez indiquer la catégorie avant de valider.";
$l_fill_soldtime = "Vous devez indiquer le temps vendu avant de valider.";
$l_fill_estimated = "Vous devez indiquer le temps estimé avant de valider.";
$l_fill_tasklabel = "Vous devez donner un nom à cette tâche.";
$l_fill_soldnumber = "Le temps vendu doit être un nombre.";
$l_fill_projected = "Chaque participant avant doit être affecté à au moins une tâche";
$l_fill_projnumber = "Le temps prévu doit être un nombre de jours.";
$l_fill_missing = "Le temps manquant doit être indiqué pour tous les membres du projet";
$l_fill_missnumber = "Le temps manquant doit être un nombre de jours.";
$l_delete_confirm = "Confirmer la suppression du projet ?";
$l_tolong_shortname = "Le nom court est trop long";
$l_err_used = "Le temps passé n'est pas correct !";
$l_err_remaining = "Le temps restant n'est pas correct !";

// For the display preferences modification screen 
$l_project_display = "Options d'affichage des listes de projets";
