<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : agenda.inc                                               //
// 2001-06-28 by Francois Bloque                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
//////////////////////////////////////////////////////////////////////////////


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
$l_timebegin = "Heure de d&eacute;but (HH:MM)";
$l_timeend = "Heure de fin (HH:MM)";
$l_occupied_day = "Journ&eacute;e entiere";

$l_format = "(AAAA-MM-JJ)";
$l_repeat = "Répétition";
$l_periodicity = "Fr&eacute;quence";
$l_repeatkind = "Type de r&eacute;p&eacute;tition";
$l_repeatdays = "Jours de r&eacute;p&eacute;tition (type hebdomadaire)";
$l_use_endrepeat = "Date de fin de r&eacute;p&eacute;tition";
$l_interval = "Intervalle (en jours)";
$l_carac = "Propriétés";
$l_force = "Forcer l'insertion de l'evenement";

$l_waiting_events = "Rendez-vous en attente";

$l_accepted = "Accept&eacute;";
$l_rejected = "Rejet&eacute;";
$l_waiting  = "En attente de confirmation";
$l_short_accepted = "A";
$l_short_rejected = "R";
$l_short_waiting  = "Att";

$l_daily = "quotidienne";
$l_weekly = "hebdomadaire";
$l_monthlybyday = "mensuelle";
$l_monthlybydate = "mensuelle(date)";
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

$l_fill_title = "Veuillez remplir le champ <Titre>";
$l_fill_dateend = "Veuillez remplir le champ <Date de fin>";
$l_fill_datebegin = "Veuillez remplir le champ <Date de debut>";
$l_incorect_datebegin = "Format du champ <Date de debut> incorrecte";
$l_incorect_dateend = "Format du champ <Date de Fin> incorrecte";
$l_incorect_repeat = "Format du champ <Date de fin de répétition> incorrecte";

$l_monday="Lundi";
$l_tuesday="Mardi";
$l_wednesday="Mercredi";
$l_thursday="Jeudi";
$l_friday="Vendredi";
$l_saturday="Samedi";
$l_sunday="Dimanche";
$l_go_to = "Aller à";
$l_tomorrow_event ="Evenements de demain";
$l_daysofweek = array (	'Dimanche',
			'Lundi',
			'Mardi',
			'Mercredi',
			'Jeudi',
			'Vendredi',
			'Samedi');
$l_daysofweekshort = array (	'Dim',
				'Lun',
				'Mar',
				'Mer',
				'Jeu',
				'Ven',
				'Sam');

$l_daysofweekreallyshort = array ('D','L','M','M','J','V','S');

$l_monthsofyear = array (	'Janvier','Février',
				'Mars','Avril','Mai',
				'Juin','Juillet','Août',
				'Septembre','Octobre','Novembre',
				'Décembre');
$l_monthsofyearshort = array ('jan','fév','mar','avr','mai','juin',
			      'juil','aoû','sep','oct','nov','déc');


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
