<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : agenda.inc                                               //
// 2002-01-06 by Pierre Baudracco                                            //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$l_agenda = "Calendar";
$l_users = "Users";
$l_groups = "Groups";
$l_title = "Title";
$l_priority = "Priority";
$l_private = "Private";
$l_public  = "Public";
$l_category = "Catégory";
$l_description = "Description";
$l_state = "Status";
$l_back_mono = "Current user view";

$l_high = "High";
$l_low = "Low";
$l_medium = "Medium";

$l_datebegin = "Begin date";
$l_dateend = "End date";
$l_timebegin = "Begin time";
$l_timeend = "End time";
$l_occupied_day = "Whole day";

$l_format = "(AAAA-MM-JJ)";
$l_repeat = "REPETITION";
$l_periodicity = "Frequency";
$l_repeatkind = "Frequency type";
$l_repeatdays = "Repetition days (weekly type)";
$l_use_endrepeat = "End repetition date";

$l_waiting_events = "Waiting events";
$l_user_calendar = "Multi-user view";

$l_accepted = "Accept";
$l_rejected = "Reject";
$l_waiting  = "Waiting for confirmation";
$l_short_accepted = "A";
$l_short_rejected = "R";
$l_short_waiting  = "Wait";
$l_wait = "Waiting";

$l_daily = "daily";
$l_weekly = "weekly";
$l_monthlybydate = "monthly (date)";
$l_monthlybyday = "monthly (day)";
$l_yearly = "yearly";

$l_from = "from";
$l_to = "to";

$l_conflicts = "Conflit(s) found";
$l_date = "Date";
$l_user = "User";

// actions 
$l_insert = "Insert the event";
$l_update = "Update the event";
$l_accept = "Accept";
$l_reject = "Refuse";
$l_select = "Select";
$l_repeat_update = "Update repetitions";
$l_go_to = "Go to";
$l_refuse = "Refuse";
$l_change_state = "Your event participation";
$l_force_all = "Force all";
$l_replace_all = "Replace all";
$l_cancel_all = "Cancel all";
$l_force_event = "Force";
$l_cancel_event = "Cancel";
$l_replace_event = "Replace";

$l_force = "Force the event insertion";
$l_carac = "Caracteristics";

$l_insert_ok = "Insertion successful";
$l_update_ok = "Update successful";
$l_update_warning = "Warning : some conflicts have been detected";
$l_insert_warning = "Warning : some conflicts have been detected";
$l_update_error = "Conflicts have been detected, update cancelled";
$l_insert_error = "Conflicts have been detected, insertion cancelled";


$l_daysofweek = array (	'Sunday',
			'Monday',
			'Tuesday',
			'Wednesday',
			'Thursday',
			'Friday',
			'Saturday');

$l_daysofweekshort = array (	'Sun',
				'Mon',
				'Tue',
				'Wed',
				'Thu',
				'Fri',
				'Sat');

$l_daysofweekreallyshort = array ('S','M','T','W','T','F','S');

$l_monthsofyear = array ('January','February', 'March','April','May','June',
                         'July','August','September','October','November',
			 'December');
$l_monthsofyearshort = array ('jan','feb','mar','apr','may','jun','jul',
			      'aug','sep','oct','nov','dec');

// Messages
$l_select_contacts = "Vous devez choisir au moins un contact ou un groupe.";
$l_select_repeat_day = "Vous devez s&eacute;lectionner le ou les jours de r&eacute;p&eacute;tition.";
$l_conflict_events = "Le ou les cr&eacute;neaux horaires choisis sont occup&eacute;s pour "; 
$l_fill_title = "You have to fill the [ $l_title ] field";
$l_fill_dateend = "You have to fill the [ $l_dateend ] field";
$l_fill_datebegin = "You have to fill the field [ $l_datebegin ]";
$l_err_datebegin = "[ $l_datebegin ] format incorrect !";
$l_err_dateend = "[ $l_dateend ] format incorrect !";
$l_err_repeat = "[ $l_use_endrepeat ] format incorrect !";
$l_err_begin_end = "End date must be after begin date !";
$l_err_end_repeat = "Repetion end must be after end date";

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
