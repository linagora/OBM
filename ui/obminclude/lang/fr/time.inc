<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : time.inc                                                 //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//for list
$l_task_fac = "Taches Facturées";
$l_task_int = "Taches Internes";


// Database fields
$l_taskdate="Date tâche";
$l_tasktype = "Type tâche";
$l_tasklabel="Label tâche";
$l_deallabel="Projet/Affaire";
$l_task_del="Supprimer ?";
$l_company = "Société";
$l_delete = "Supprimer les tâches";
$l_length="Durée";
$l_total_length="Durée cumulée";
$l_task_del="Suppr.";
$l_week="Semaine";
$l_user="Utilisateur";
$l_see = "Voir";
$l_validate = "Validée";
$l_valid = "Valider";
$l_add = "Ajouter";
$l_modify = "Modifier";


$l_lastname = "Nom";
$l_firstname = "Prénom";
$l_kind = "Genre";
$l_function = "Fonction";
$l_phone = "Tel";
$l_hphone = "Tel personnel";
$l_minihphone = "(Perso)";
$l_mphone = "Tel mobile";
$l_minimphone = "(Mobile)";
$l_email = "@M";
$l_fax = "Fax";
$l_address = "Adresse";
$l_postcode = "CP";
$l_expresspostal = "Cedex";
$l_town = "Ville";
$l_country = "Pays";
$l_comment = "Commentaire";
$l_visibility ="Accès";

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames["task_date"] = $l_taskdate;
$fieldnames["task_label"] = $l_tasklabel;
$fieldnames["task_deal_label"] = $l_deallabel;
$fieldnames["task_company_name"] = $l_company;
$fieldnames["task_length"] = $l_length;
$fieldnames["task_totallength"] = $l_total_length;
$fieldnames["tasktype_label"] = $l_tasktype;
$fieldnames["task_id"] = $l_modify;
$fieldnames["contact_function"] = $l_function;
$fieldnames["contact_phone"] = $l_phone;
$fieldnames["contact_homephone"] = $l_hphone;
$fieldnames["contact_mobilephone"] = $l_mphone;
$fieldnames["contact_email"] = $l_email;
// Calculated fields
$fieldnames["contact_company_name"] = $l_company;

$l_week_sel = "Semaine recherchée";
$l_from_company = "&nbsp;&nbsp;Société";
$l_telother = "Autres tel";
$l_private = "Privé";
$l_public = "Publique";
$l_contact_select_company = "Sélectionner la société du Contact.";
$l_week_next = "Semaine Suivante";
$l_week_prev = "Semaine précédente";
$l_date = "Date:";
$l_project = "Projet:";
$l_task = "Tache:";
$l_label = "Label:";
$l_duration = "Durée:";

// Titres
$l_time = "Gestion des temps";

// Actions
$l_insert = "Enregistrer le Contact";
$l_update = "Modifier le Contact";
$l_checkdelete = "Vérifier ou Supprimer le Contact";
$l_select_company = "Sélectionner la société";
$l_confirm= "Confirmer l'action";
$l_close = "Fermer";

// Messages
$l_projects_manager = "Confirmation de la validation des taches par l'administrateur:";
$l_check_samecontact = "Ces contacts semblent similaires à cette nouvelle entrée !";
$l_link_deal = "Le contact est référencé par ces affaires";
$l_link_deal_no = "Le contact n'est pas référencé dans les affaires";
$l_link_list = "Le contact est enregistré dans ces listes";
$l_link_list_no = "Le contact n'est pas référencé dans les listes";
$l_cant_delete = "Le contact ne peut être supprimé !";
$l_can_delete = "Le contact peut être supprimé.";
$l_error_permission = "Vous n'avez pas les droits d'acces à cette page.";
$l_error_visibility = "Les informations contenues dans cette page sont privées. Vous ne pouvez pas les consulter";
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des contacts correspondants.";
$l_found_task = "Voici les tâches réalisées et saisies pour la semaine en cours:";
$l_found = "Taches trouvées.";
$l_no_found = "Aucune Taches pour cet utilisateur!";
$l_addtask_start = "Saisissez les tâches réalisées pour la semaine en cours. La durée s'entend en fraction de journée (entre 1/";
$l_addtask_end = " et 1).";
$l_modtask = "Vous pouvez modifier votre tache ci dessous:";
$l_total_day = "Récapitulatif des tâches par jour.Si une journée a été intégralement saisie,<BR> vous devez confirmer ci-dessous en sélectionnant les journées à valider et en validant le formulaire.";
$l_total_deal = "Récapitulatif par taches:";
$l_delete_ok = "La suppression du contact a été correctement effectuée.";
$l_delete_error = "La suppression du contact a échoué !";
$l_insert_ok = "L'insertion du contact a été correctement effectuée.";
$l_insert_error = "L'insertion du contact a échoué !";
$l_update_ok = "La modification du contact a été correctement effectuée";
$l_update_error = "La modification du contact a échoué !";
$l_query_error = "Problème en executant la requete";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_lastname = "Vous devez renseigner le Nom avant de valider.";
$l_fill_phone = "Vous devez renseigner le telephone avant de valider.";
$l_delete_confirm = "Confirmer la suppression de ces taches ?";
$l_j_select_company = "Vous devez sélectionner une société !";
$l_j_check_phone = "Le numéro de téléphone n'est pas correct.";
$l_j_check_hphone = "Le numéro de téléphone personnel n'est pas correct.";
$l_j_check_mphone = "Le numéro de téléphone mobile n'est pas correct.";
$l_j_check_fax = "Le numéro de fax n'est pas correct.";
$l_j_check_email = "L'adresse email n'est pas valide. Vérifiez le domaine.";

// champs specifiques
// format de date et heure pour MySQL pour affichage dans ecran detail affaire
$l_jour_format = "%a %M %d";


// For the display preferences modification screen 
$l_contact_display = "Options d'affichage des listes de contacts";
