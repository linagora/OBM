<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : calendar.inc                                             //
// 2001-06-28 by AliaSource                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
//////////////////////////////////////////////////////////////////////////////

$l_calendar = "Calendrier";
$l_attendees = "Participants";
$l_users = "Utilisateurs";
$l_resources = "Ressources";
$l_free_resources = "Resources libres : ";
$l_groups = "Groupes";
$l_title = "Titre";
$l_location = "Lieu";
$l_private_description = "Cet événement est personnel. Impossible de voir le détail.";
$l_high = "Haute";
$l_low = "Basse";
$l_medium = "Normale";
$l_go = "Voir";
$l_hour = "Heure";
$l_occupied_day = "Journée entière";
$l_event_duration = "Durée de la réunion";
$l_format = "(AAAA-MM-JJ)";
$l_repeat = "Répétition";
$l_periodicity = "Fréquence";
$l_repeatkind = "Type de répétition";
$l_repeatdays = "Jours de répétition (type hebdomadaire)";
$l_repeatdays2 = "Jours de répétition";
$l_user_right = "Gérer les droits de";
$l_date_repeatend = "Fin de répétition";
$l_carac = "Propriétés";
$l_force = "Forcer l'insertion de l'événement";
$l_group_members = "Membres du groupe";
$l_everybody = "Tout le monde";
$l_everyresources = "Toutes les ressources";
$l_all_day = "Toute la journée";
$l_begin = "Début";
$l_end = "Fin";
$l_link_events = " événements liés à cette catégorie.";
$l_waiting_events = "Rendez-vous en attente";
$l_free = "Libre mais restreint";
$l_possible = "Libre";
$l_occupied = "Non disponible";
$l_meeting_legend = "Légende";
$l_selection = "Sélection";
$l_repeatfrequency = "Fréquence";
$l_accepted = "Accepté";
$l_rejected = "Rejeté";
$l_waiting  = "En attente de confirmation";
$l_short_accepted = "A";
$l_short_rejected = "R";
$l_short_waiting  = "Att";
$l_wait = "En attente";
$l_exception = "Exceptions";
$l_items = "Accessoires";

$l_daily = "quotidienne";
$l_weekly = "hebdomadaire";
$l_monthlybyday = "mensuelle(jour)";
$l_monthlybydate = "mensuelle(date)";
$l_yearly = "annuelle";
$l_task = "Tache";

$l_from = "du";
$l_to = "au";

$l_conflicts = "Conflit(s) rencontré(s)";
$l_date = "Date";
$l_user = "Utilisateur";
$l_back_mono = "Revenir à ma vue";

$l_collapse_all = "Tout réduire";
$l_expand_all = "Tout développer";

// actions 
$l_meeting_perform = "Rechercher les créneaux libres";
$l_insert = "Enregistrer le rdv";
$l_accept = "Accepter";
$l_reject = "Refuser";
$l_select = "Sélectionner";
$l_select_group = "Choisir groupe";
$l_repeat_update = "Mettre à jour toutes les répétitions à venir";
$l_go_to = "Aller à";
$l_refuse = "Refuser";
$l_change_state = "Votre participation à l'évenement";
$l_force_insert = "Forcer l'insertion";
$l_refuse_insert = "Refuser l'évenement";
$l_cancel_insert = "Ne rien faire";
$l_too_much_user = "Vous ne pouvez sélectionner au maximum que six utilisateurs";
$l_too_much_resource = "Vous ne pouvez sélectionner au maximum que six ressources";
$l_update_warning = "Attention des conflits ont été détectés";
$l_insert_warning = "Attention des conflits ont été détectés";
$l_update_error = "Des conflits ont été détectés, mise à jour annulée";
$l_insert_error = "Des conflits ont été détectés, insertion annulée";
$l_sendamail = "Forcer la notification par mail";
$l_calendar_select_group = "Sélectionner les groupes.";
$l_view_type = "Type de vue";

// Messages
$l_select_contacts = "Vous devez choisir au moins un contact ou un groupe.";
$l_select_repeat_day = "Vous devez sélectionner le ou les jours de répétition";
$l_confirm_delete = "Cette opération va supprimer toutes les occurences de cet événement pour tous les utilisateurs, êtes-vous sûr de vouloir poursuivre ?";
$l_conflict_events = "Le ou les créneaux horaires choisis sont occupés pour ";
$l_fill_title = "Veuillez remplir le champ [ $l_title ]";
$l_fill_dateend = "Veuillez remplir le champ [ $l_dateend ]";
$l_fill_datebegin = "Veuillez remplir le champ [ $l_datebegin ]";
$l_err_no_calendar_selected = "Aucun agenda séléctionné";
$l_err_begin_end = "La date de fin doit être supérieure à la date de début";
$l_err_end_repeat = "La fin de répétition doit être supérieure à la date de fin";
$l_err_end_repeat2 = "La durée maximum d'une répétition est de 1 an";
$l_err_end_repeat3 = "La durée maximum d'une répétition est de 10 ans lorsque la répétition est de type annuelle.";
$l_err_days_repeat = "Vous devez spécifier des jours de répétitions.";
$l_err_days_repeat_not_weekly = "Vous ne pouvez préciser des jours de répétition qu'en mode de répétition hebdomadaire.";
$l_err_weekly_repeat = "Lors d'une répétition hebdomadaire la date de fin de répétition doit au moins être de 7 jours supérieure à celle de fin d'événement.";
$l_err_repeatfrequency = "La fréquence est invalide";
$l_event_cant_delete = "Vous ne pouvez supprimer que vos événements !";

$l_header_planning = "Planning";
$l_header_meeting = "Nouvelle réunion";
$l_header_admin = "Administration";
$l_header_waiting_events = "Evts en attente";
$l_authorize_list = "Personnes autorisées";
$l_denie_list = "Personnes non-autorisées";
$l_read_permission = "Gestion des permissions de lecture de l'agenda";
$l_write_permission = "Gestion des permissions d'écriture sur l'agenda";
$l_everyone = "Autoriser tout le monde";
$l_noone = "N'autoriser personne";
$l_authorize = "&lt;&lt;&lt;&lt;";
$l_deny = "&gt;&gt;&gt;&gt;";

//MAIL
$l_add_event_mail_head = "Nouvel événement dans Obm : ";
$l_update_event_mail_head = "Modification événement : ";
$l_delete_event_mail_head = "Suppression événement : ";

$l_add_event_mail_subject = "Nouvel événement sur Obm : ";
$l_update_event_mail_subject = "Modification événement sur Obm : ";
$l_delete_event_mail_subject = "Suppression événement sur Obm : ";

$l_update_event_mail_body = "\nAu lieu de :";
$l_delete_event_mail_body = "Il aurait du avoir lieu : ";
$l_confirm_mail = "Confirmer+forcer l'envoi de mail";

$l_unknown = "Inconnu";
$l_calendar_visible = "Agendas Visibles";
$l_calendar_displayed = "Agendas affichés";
$l_resource_visible = "Ressources";
$l_resource_displayed = "Ressources choisies";
$l_resource_allocated = "Ressources allouées";
// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_interval_null = "La durée ne peux pas être nulle";
$mj_fill_title_event = "\\n Vous devez saisir un titre pour le rdv.";
$mj_fill_time_begin = "\\n Vous devez saisir une heure de début.";
$mj_fill_time_end = "\\n Vous devez saisir une heure de fin.";
$mj_fill_valid_time_begin = "\\n Vous devez saisir une heure de début valide.";
$mj_fill_valid_time_end = "\\n Vous devez saisir une heure de fin valide.";
$mj_delete_confirm = "Confirmer la suppression du rdv ?";
$mj_invalid_day = "Ce jour n'existe pas pour le mois sélectionné.";
$mj_date_incompatibility = "La date de fin ne peut être antérieure a la date de début.";
$mj_repeat_date_incompatibility = "La date de fin de répétition ne peut être antérieure a la date de début."; 
$mj_event_delete_confirm = "Confirmer la suppression du rdv ?"; 
$mj_event_delete_all_confirm = "Confirmer la suppression du rdv et de toutes ses répétitions ?"; 
