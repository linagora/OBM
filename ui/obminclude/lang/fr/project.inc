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
$l_usedsold = "Utilisation deu temps vendu";
$l_progress = "Avancement du projet";
$l_progeval = "Evaluation de l'avance retard";

// for list
$l_task_fac = "Taches Facturées";
$l_task_div = "Taches Diverses";
$l_task_int = "Tâches R&D";

// Labels et textes
$l_list = "Liste";
$l_manager = "Chef de projet";
$l_member = "Participant";
$l_soldtime = "Temps vendu";

// Titres
$l_dealinfos = "Partie affaire";
$l_projectinfos = "Partie Projet";
$l_externinfos = "Projet commercial";
$l_interninfos = "Projet interne";
$l_advanceinfos = "Avancement du projet";
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
$l_no_members = "Il n'y a pas de renseignements sur les participants au projet";
$l_delete_ok = "La suppression de la société a été correctement effectuée.";
$l_state_error = "Ce n'est pas un projet valide";
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
$l_del_member_sel = "Enlever ces participants";
$l_member_added = "participant(s) ajouté(s)";
$l_member_removed = "participant(s) enlevé(s)";
$l_valid_member = "Confirmer";
// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_delete_confirm = "Really delete the company ?";
$l_fill_name = "Vous devez renseigner le Nom avant de valider.";
$l_delete_confirm = "Confirmer la suppression de la Société ?";
$l_j_check_zip = "Le code postal n'est pas correct.";
$l_j_check_phone = "Le numéro de téléphone n'est pas correct.";
$l_j_check_fax = "Le numéro de fax n'est pas correct.";
$l_j_check_email = "L'adresse email n'est pas valide. Vérifiez le domaine.";
$l_j_check_web = "L'adresse web n'est pas valide.";
$l_j_kind_delete = "Sélectionnez un type pour le vérifier ou supprimer !";
$l_j_kind_update = "Vous devez sélectionner un type et entrer une modification avant de valider !";
$l_j_kind_create = "Vous devez saisir un nom de type pour le créer !";
$l_j_act_delete = "Sélectionnez un secteur d'activité pour le vérifier ou supprimer !";
$l_j_act_update = "Vous devez sélectionner un secteur d'activité et entrer une modification avant de valider !";
$l_j_act_create = "Vous devez saisir un nom de secteur d'activité pour le créer !";
$l_j_select_company = "Vous devez sélectionner une société !";

// For the display preferences modification screen 
$l_searchproject_display = "Options d'affichage des listes de projets";
$l_newproject_display = "Options d'affichage de la liste de nouveaux projets";
