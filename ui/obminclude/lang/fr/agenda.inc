<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : agenda.inc                                               //
// 2001-06-28 by Francois Bloque                                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
//////////////////////////////////////////////////////////////////////////////


$l_agenda = "Calendrier";
$l_users = "Utilisateurs";
$l_groups = "Groupes";
$l_title = "Titre";
$l_priority = "Priorité";
$l_private = "Privé";
$l_public  = "Public";
$l_category = "Catégorie";
$l_description = "Description";
$l_state = "Etat";

$l_high = "Haute";
$l_low = "Basse";
$l_medium = "Normale";

$l_datebegin = "Date de début";
$l_dateend = "Date de fin";
$l_timebegin = "Heure de début (HH:MM)";
$l_timebegin2 = "Heure de début";
$l_timeend = "Heure de fin (HH:MM)";
$l_timeend2 = "Heure de fin";
$l_occupied_day = "Journée entière";

$l_format = "(AAAA-MM-JJ)";
$l_repeat = "Répétition";
$l_periodicity = "Fréquence";
$l_repeatkind = "Type de répétition";
$l_repeatdays = "Jours de répétition (type hebdomadaire)";
$l_repeatdays2 = "Jours de répétition";

$l_use_endrepeat = "Date de fin de répétition";
$l_carac = "Propriétés";
$l_force = "Forcer l'insertion de l'evenement";

$l_waiting_events = "Rendez-vous en attente";

$l_accepted = "Accepté";
$l_rejected = "Rejeté";
$l_waiting  = "En attente de confirmation";
$l_short_accepted = "A";
$l_short_rejected = "R";
$l_short_waiting  = "Att";
$l_wait = "En attente";

$l_daily = "quotidienne";
$l_weekly = "hebdomadaire";
$l_monthlybyday = "mensuelle(jour)";
$l_monthlybydate = "mensuelle(date)";
$l_yearly = "annuelle";

$l_from = "du";
$l_to = "au";

$l_conflicts = "Conflit(s) rencontré(s)";
$l_date = "Date";
$l_user = "Utilisateur";
$l_back_mono = "Revenir à votre vue";
// actions 
$l_insert = "Enregistrer le rdv";
$l_update = "Modifier le rdv";
$l_accept = "Accepter";
$l_reject = "Refuser";
$l_select = "Sélectionner";
$l_repeat_update ="Mettre à jour toutes les répétitions à venir";
$l_go_to = "Aller à";
$l_refuse = "Refuser";
$l_change_state = "Votre participation à l'évenement";
$l_force_all = "Tout forcer";
$l_replace_all = "Tout remplacer";
$l_cancel_all = "Tout annuler";
$l_force_event = "Forcer";
$l_cancel_event = "Annuler";
$l_replace_event = "Remplacer";
$l_too_much_user = "Vous ne pouvez selectionner au maximum que six utilisateurs";
$l_insert_ok = "Insertion effectuée avec succès";
$l_update_ok = "Mise a jour effectuée avec succès";
$l_update_warning = "Attention des conflits ont été détectés";
$l_insert_warning = "Attention des conflits ont été détectés";
$l_update_error = "Des conflits ont été détectés, mise à jour annulée";
$l_insert_error = "Des conflits ont été détectés, insertion annulée";
$l_sendamail = "Notifier les participants par mail";
$l_category_update = "Modifier la Categorie";
$l_category_checkdelete = "Vérifier ou Supprimer la Categorie";
$l_category_delete = "Supprimer la Categorie";
$l_category_insert = "Créer la Categorie";

	
// Messages
$l_category_delete_ok = "La suppression de la categorie a été correctement effectuée.";
$l_category_delete_error = "La suppression de la categorie a échoué !";
$l_category_insert_ok = "L'insertion de la categorie a été correctement effectuée.";
$l_category_insert_error = "L'insertion de la categorie a échoué !";
$l_category_update_ok = "La modification de la categorie a été correctement effectuée";
$l_category_update_error = "La modification de la categorie a &eacute;chou&eacute;e !";
$l_category_link_company = "Certain événements sont de la categorie";
$l_category_link_company_no = "Pas d'événements de la categorie";
$l_category_cant_delete = "Le categorie ne peut être supprimée !";
$l_category_can_delete = "Le categorie peut être supprimée.";
$l_select_contacts = "Vous devez choisir au moins un contact ou un groupe.";
$l_select_repeat_day = "Vous devez sélectionner le ou les jours de répétition";
$l_confirm_delete = "Cette opération supprimer toutes les occurences de cet évenement pour tout les utilisateur, êtes-vous sûre de vouloit poursuivre ?";
$l_confirm = "Confirmer";
$l_cancel = "Annuler";
$l_conflict_events = "Le ou les créneaux horaires choisis sont occupés pour ";
$l_fill_title = "Veuillez remplir le champ [ $l_title ]";
$l_fill_dateend = "Veuillez remplir le champ [ $l_dateend ]";
$l_fill_datebegin = "Veuillez remplir le champ [ $l_datebegin ]";
$l_err_datebegin = "Format du champ [ $l_datebegin ] incorrect";
$l_err_dateend = "Format du champ [ $l_dateend ] incorrect";
$l_err_repeat = "Format du champ [ $l_use_endrepeat ] incorrect";
$l_err_begin_end = "La date de fin doit etre supérieure à la date de début";
$l_err_end_repeat = "La fin de répétition doit etre supérieure à la date de fin";
$l_err_end_repeat2 = "La durée maximum d'une répétition est de 1 an";
$l_err_end_repeat3 = "La durée maximum d'une répétition est de 10 ans lorsque la répétition est de type annuelle.";
$l_err_days_repeat = "Vous devez spécifier des jours de répétitions.";
$l_err_weekly_repeat = "Lors d'une répétition hebdomadaire la date de fin de répétition doit au moins etre de 7 jours supérieure à celle de fin d'événement.";
$l_monday="Lundi";
$l_tuesday="Mardi";
$l_wednesday="Mercredi";
$l_thursday="Jeudi";
$l_friday="Vendredi";
$l_saturday="Samedi";
$l_sunday="Dimanche";
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

$l_category_manage = "Gestion des Categories d'évenements";
$l_category_exist = "Categories existantes";
$l_category_new = "Nouvelle Categorie";
$l_header_right = "Gestion des droits";
$l_header_admin = "Administration";
$l_authorize_list = "Personnes authorisées";
$l_denie_list = "Personnes non-authorisées";
$l_read_permission = "Gestion des permissions de lecture de votre agenda";
$l_write_permission = "Gestion des permissions d'ecriture sur votre agenda";
$l_everyone = "Authoriser tout le monde";
$l_noone = "N'authoriser personne";
$l_list = "Authoriser les personnes de la liste";
$l_authorize = "&lt;&lt;&lt;&lt;";
$l_deny = "&gt;&gt;&gt;&gt;";
//MAIL

$l_add_event_mail_head = "Un nouvel évenement a été programmé pour vous sur Obm.\nIl a pour titre : ";
$l_update_event_mail_head = "Un événement vous concernant a été mis à jour sur Obm.\nIl a pour titre : ";
$l_delete_event_mail_head = "Un événement vous concernant a été supprimé sur Obm.\nIl avait pour titre : ";

$l_add_event_mail_subject = "Nouvel evenement sur Obm";
$l_update_event_mail_subject = "Mise a jour d'un evenement sur Obm";
$l_delete_event_mail_subject = "Suppression d'un evenement sur Obm";

$l_event_mail_body = "Il aura lieu : ";
$l_update_event_mail_body = "\nAu lieu de :";
$l_delete_event_mail_body = "Il aurait du avoir lieu : ";
$l_confirm_mail = "Confirmer+envoie de mail";


$l_unknown="Inconnu";
$l_user_calendar = "Vue Multi-utilisateurs";
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
