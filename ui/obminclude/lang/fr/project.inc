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
$l_status = "Init.";
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
$l_alloproj = "Attribution du temps prévu";
$l_usedproj = "Utilisation du temps prévu";
$l_progress = "Avancement du projet";
$l_progeval = "Evaluation de l'avance retard";

// Labels et textes
$l_list = "Liste";
$l_taskname = "Nom";
$l_parent = "Tâche parent";
$l_manager = "Chef de projet";
$l_member = "Participant";
$l_new = "Nouveaux";
$l_soldtime = "Temps vendu";
$l_projtime = "Temps prévu";
$l_total = "Total";
$l_resume = "Résumé";
$l_nbtask = "tâches";
$l_nbmem = "participants";

// Titres
$l_project = "Projet";
$l_dealinfos = "Partie affaire";
$l_projectinfos = "Partie Projet";
$l_externinfos = "Projet";
$l_interninfos = "Projet interne";
$l_advanceinfos = "Avancement du projet";
$l_tasksinfos = "Tâches du projet";
$l_membersinfos = "Detail de l'avancement";
$l_memberstimeinfos = "Renseignez le temps de travail (en jours) prévu pour chaque personne assignée à une tâche";
$l_advupdateinfos = "Renseignez le temps de travail (en jours) manquant aux participants pour terminer une tâche";

// Actions
$l_insert = "Créer le projet";
$l_update = "Modifier le Projet";
$l_adv_update = "Mettre à jour les avancements";
$l_checkdelete = "Vérifier ou Supprimer la Société";
$l_delete = "suppr";

// Messages
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des projets correspondants.";
$l_new_projects = "Liste des projets non initialisés";
$l_found = "projet(s) trouvés.";
$l_no_found = "Aucun projet ne correspond à ces critères !";
$l_no_tasks = "Il n'y a pas encore de tâches définies";
$l_no_members = "Il n'y a pas de renseignements sur les participants au projet";
$l_no_membertime = "Les personnes qui participent à ce projet n'ont pas encore été affecté à une tâche";
$l_no_projected = "Les personnes suivantes participent à ce projet mais n'ont pas encore été affecté à une tâche";
$l_delete_ok = "----";
$l_delete_error = "----";
$l_status_error = "Ce n'est pas un projet valide";
$l_insert_ok = "La création du projet a été correctement effectuée.";
$l_insert_error = "Impossible de créer cette tâche ; La tâche principale est utilisée par la gestion de temps";
$l_taskdelete_error = "Impossible de supprimer une tâche ; Quelqu'un y est affecté";
$l_memberdelete_error = "Impossible d'enlever ce participant ; Il est affecté à une tâche du projet";
$l_update_ok = "La modification du projet a été correctement effectuée";
$l_update_error = "La modification du projet a échoué !";
$l_proj_update_ok = "La mise à jour des affectation a été correctement effectuée";
$l_proj_update_error = "La mise à jour des affectations a échoué !";
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
$l_del_member_sel = "Enlever ces participants";
$l_member_added = "participant(s) ajouté(s)";
$l_member_removed = "participant(s) enlevé(s)";
$l_no_member_add = "Aucun participant à ajouter";
$l_no_member_del = "Aucun participant à effacer";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_name = "Vous devez indiquer un nom avant de valider.";
$l_fill_tt = "Vous devez indiquer la catégorie avant de valider.";
$l_fill_soldtime = "Vous devez indiquer le temps vendu avant de valider.";
$l_fill_tasklabel = "Vous devez donner un nom à cette tâche.";
$l_fill_soldnumber = "Le temps vendu doit être un nombre entier de jours.";
$l_fill_member = "Vous devez renseigner les participants au projet.";
$l_fill_projected = "Chaque participant avant doit être affecté à au moins une tâche";
$l_fill_projnumber = "Le temps prévu doit être un nombre entier de jours.";
$l_fill_missing = "Le temps manquant doit être indiqué pour tous les membres du projet";
$l_fill_missnumber = "Le temps manquant doit être un nombre entier de jours.";
$l_delete_confirm = "----";

// For the display preferences modification screen 
$l_searchproject_display = "Options d'affichage des listes de projets";
$l_newproject_display = "Options d'affichage de la liste de nouveaux projets";
