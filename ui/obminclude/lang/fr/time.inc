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
$l_total_before="Durée cumulée précédente";
$l_total_period="Durée sur la période";
$l_total="Total";
$l_day="Jour";

$l_task_del="Suppr.";
$l_week="Semaine";
$l_month="Mois";
$l_user="Utilisateur";
$l_all_users="Tous ?";
$l_see = "Voir";
$l_validate_task = "Validée";
$l_valid = "Valider";
$l_add = "Ajouter";
$l_update = "Modifier";


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
$fieldnames["task_totalbefore"] = $l_total_before;
$fieldnames["task_totalperiod"] = $l_total_period;
$fieldnames["tasktype_label"] = $l_tasktype;
$fieldnames["task_id"] = $l_update;
$fieldnames["total_length"] = $l_total;
$fieldnames["day_in_month"] = $l_day;
// Calculated fields
$fieldnames["contact_company_name"] = $l_company;

$l_week_sel = "Semaine recherchée";
$l_from_company = "&nbsp;&nbsp;Société";
$l_telother = "Autres tel";
$l_private = "Privé";
$l_public = "Publique";
$l_contact_select_company = "Sélectionner la société du Contact.";
$l_week_next = "Semaine suivante";
$l_week_prev = "Semaine précédente";
$l_month_next = "Mois suivant";
$l_month_prev = "Mois précédent";
$l_date = "Date:";
$l_project = "Projet:";
$l_task = "Tache:";
$l_label = "Label:";
$l_duration = "Durée:";

// Titres
$l_time = "Gestion des temps";
$l_header_admin = "Administration";
$l_header_stats = "Statistiques";

// Actions
$l_insert = "Enregistrer le Contact";
$l_checkdelete = "Vérifier ou Supprimer le Contact";
$l_select_company = "Sélectionner la société";
$l_confirm= "Confirmer l'action";
// Defined in global
// $l_close = "Fermer";

// Messages
$l_projects_manager = "Confirmation de la validation des taches par l'administrateur:";
$l_check_samecontact = "Ces contacts semblent similaires à cette nouvelle entrée !";
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des contacts correspondants.";
$l_found_task = "Voici les tâches réalisées et saisies pour la semaine en cours:";
$l_found = "Taches trouvées.";
$l_no_found = "Aucune Taches pour cet utilisateur!";
$l_addtask_start = "Saisissez les tâches réalisées pour la semaine en cours. La durée s'entend en fraction de journée (entre 1/";
$l_addtask_end = " et 1).";
$l_update_task = "Vous pouvez modifier votre tache ci dessous:";
$l_total_day = "Récapitulatif des tâches par jour.Si une journée a été intégralement saisie,<BR> vous devez confirmer ci-dessous en sélectionnant les journées à valider et en validant le formulaire.";
$l_total_deal = "Récapitulatif par taches :";
$l_total_month = "Récapitulatif mensuel :";
$l_query_error = "Problème en executant la requete";

// Type de tâches à rechercher
$l_tasktype = "Type des tâches";
$l_task_all = "Toutes";
$l_task_uservalid = "Validées par l'admin ou l'utilisateur";
$l_task_adminvalid= "Validées par l'admin uniquement";
// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_j_select_tasktype = "Vous devez sélectionner un type de tâche.";
$l_j_select_project = "Vous devez sélectionner un project pour une tâche facturée.";


$l_fill_lastname = "Vous devez renseigner le Nom avant de valider.";
$l_fill_phone = "Vous devez renseigner le telephone avant de valider.";
$l_delete_confirm = "Confirmer la suppression de ces taches ?";
$l_j_select_company = "Vous devez sélectionner une société !";

// champs specifiques
// format de date et heure pour MySQL pour affichage dans ecran detail affaire
$l_jour_format = "%a %M %d";


// For the display preferences modification screen 
$l_contact_display = "Options d'affichage des listes de contacts";
