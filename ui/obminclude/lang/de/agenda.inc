<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : agenda.inc                                               //
// 2001-06-28 by Francois Bloque                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


$l_users = "Contacts";
$l_groups = "Groupes";
$l_title = "Titre";
$l_priority = "Priorit&eacute;";
$l_private = "Priv&eacute;";
$l_public  = "Public";
$l_category = "Cat&eacute;gorie";
$l_description = "Description";
$l_state = "Etat";
$l_none = "aucune";

$l_high = "Haute";
$l_low = "Basse";
$l_medium = "Normale";

$l_datebegin = "Date de d&eacute;but";
$l_dateend = "Date de fin";
$l_timebegin = "Heure de d&eacute;but";
$l_timeend = "Heure de fin";
$l_occupied_day = "Journ&eacute;e entiere";

$l_repeat = "REPETITION";
$l_periodicity = "Fr&eacute;quence";
$l_repeatkind = "Type de r&eacute;p&eacute;tition";
$l_repeatdays = "Jours de r&eacute;p&eacute;tition (type hebdomadaire)";
$l_use_endrepeat = "Date de fin de r&eacute;p&eacute;tition";
$l_interval = "Intervalle ";

$l_waiting_events = "Rendez-vous en attente";

$l_accepted = "Accept&eacute;";
$l_rejected = "Rejet&eacute;";
$l_waiting  = "En attente de confirmation";
$l_short_accepted = "A";
$l_short_rejected = "R";
$l_short_waiting  = "Att";

$l_daily = "quotidienne";
$l_weekly = "hebdomadaire";
$l_monthlybydate = "mensuelle (date)";
$l_monthlybyday = "mensuelle(jour)";
$l_yearly = "annuelle";

$l_from = "du";
$l_to = "au";


// actions 
$l_insert = "Enregistrer le rdv";
$l_update = "Modifier le rdv";
$l_delete = "Supprimer le rdv";
$l_delete_all = "Supprimer tous";
$l_validate = "Valider";
$l_accept = "Accepter";
$l_reject = "Refuser";
$l_select = "S&eacute;lectionner";

// Messages 
$l_error_permission = "Vous n'avez pas les droits d'acces à cette page.";
$l_error_visibility = "Les informations contenues dans cette page sont priv&eacute;es. Vous ne pouvez pas les consulter";
$l_select_contacts = "Vous devez choisir au moins un contact ou un groupe.";
$l_select_repeat_day = "Vous devez s&eacute;lectionner le ou les jours de r&eacute;p&eacute;tition.";
$l_event_insert_ok = "L'enregistrement du rendez-vous a &eacute;t&eacute; correctement effectu&eacute;";
$l_event_insert_error = "L'enregistrement du rendez-vous a &eacute;chou&eacute;";
$l_event_update_ok = "La modification du rendez-vous a &eacute;t&eacute; correctement effectu&eacute;e";
$l_event_update_error = "La modification du rendez-vous a &eacute;chou&eacute;";
$l_event_delete_ok = "La suppression du rendez-vous a &eacute;t&eacute; correctement effectu&eacute;e";
$l_event_delete_error = "La suppression du rendez-vous a &eacute;chou&eacute;";
$l_event_delete_all_ok = "La suppression du rendez-vous et de toutes ses r&eacute;p&eacute;titions a &eacute;t&eacute; correctement effectu&eacute;e"; 
$l_event_delete_all_error = "La suppression du rendez-vous et de toutes ses r&eacute;p&eacute;titions a &eacute;chou&eacute;";
$l_event_approve_ok = "Vos choix ont &eacute;t&eacute; enregistr&eacute;s";
$l_event_approve_error = "L'enregistrement de vos choix a &eacute;chou&eacute;";
$l_event_update_cancel_ok = "La modification du rendez-vous a bien &eacute;t&eacute; annul&eacute;e";
$l_event_insert_cancel_ok = "L'insertion du rendez-vous a bien &eacute;t&eacute; annul&eacute;e";
$l_conflict_events = "Le ou les cr&eacute;neaux horaires choisis sont occup&eacute;s pour "; 

// days and months
$l_january="Janvier";
$l_february="F&eacute;vrier";
$l_march="Mars";
$l_april="Avril";
$l_may="Mai";
$l_june="Juin";
$l_july="Juillet";
$l_august="Aout";
$l_september="Septembre";
$l_october="Octobre";
$l_november="Novembre";
$l_december="Decembre";

$l_short_january="Jan";
$l_short_february="Fev";
$l_short_march="Mar";
$l_short_april="Avr";
$l_short_may="Mai";
$l_short_june="Jun";
$l_short_july="Jul";
$l_short_august="Aou";
$l_short_september="Sep";
$l_short_october="Oct";
$l_short_november="Nov";
$l_short_december="Dec";

$l_monday="Lundi";
$l_tuesday="Mardi";
$l_wednesday="Mercredi";
$l_thursday="Jeudi";
$l_friday="Vendredi";
$l_saturday="Samedi";
$l_sunday="Dimanche";

$l_short_monday="Lun";
$l_short_tuesday="Mar";
$l_short_wednesday="Mer";
$l_short_thursday="Jeu";
$l_short_friday="Ven";
$l_short_saturday="Sam";
$l_short_sunday="Dim";

$l_unknown="Inconnu";


// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$mj_fill_title_event = "\\n Vous devez saisir un titre pour le rdv.";
$mj_fill_time_begin = "\\n Vous devez saisir une heure de debut.";
$mj_fill_time_end = "\\n Vous devez saisir une heure de fin.";
$mj_fill_valid_time_begin = "\\n Vous devez saisir une heure de debut valide.";
$mj_fill_valid_time_end = "\\n Vous devez saisir une heure de fin valide.";
$mj_delete_confirm = "Confirmer la suppression du rdv ?";
$mj_invalid_day = "Ce jour n'existe pas pour le mois selectionne.";
$mj_date_incompatibility = "La date de fin ne peut etre anterieure a la date de debut.";
$mj_repeat_date_incompatibility = "La date de fin de repetition ne peut etre anterieure a la date de debut."; 
$mj_event_delete_confirm = "Confirmer la suppression du rdv ?"; 
$mj_event_delete_all_confirm = "Confirmer la suppression du rdv et de toutes ses repetitions ?"; 



?>
