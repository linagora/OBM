<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French
//     - File     : planning.inc
// 2008-10-14 Guillaume Lardon
///////////////////////////////////////////////////////////////////////////////
// $Id: planning.inc 16 2009-09-24 07:15:46Z glardon $
///////////////////////////////////////////////////////////////////////////////

$l_see = "Voir";
$l_overrun = "Depassement";
$l_reporting = "Reporting";
$l_activity = "Activité";
$l_activities_of = "Activité de";

$l_period = "Période";
$l_plannedtask_periods = array (
    'MORNING' => 'Matin',
    'AFTERNOON' => 'Après-midi',
    'ALLDAY' => 'Toute la journée'
  );

$l_err_plannedtask_user_empty = "Un utilisateur doit être renseigné";
$l_err_plannedtask_activity_empty = "Il faut sélectionner une activité";
$l_err_plannedtask_activity_one = "Il ne faut sélectionner qu'une seule activité";
$l_err_plannedtask_datebegin_empty = "Une date de début doit être renseignée";
$l_err_plannedtask_dateend_empty = "Une date de fin doit être renseigné";
$l_err_plannedtask_dateinverted = "La date de début ne peut être posterieure à la date de fin";
$l_err_plannedtask_detected_conflict = "Conflit détecté : cette personne est déjà affectée durant cette période. Voir l'évènement ci-dessous.";
$l_err_plannedtask_event_in_conflit = "Événement en conflit";
$l_err_planning_no_group_selected = "Aucun groupe n'est selectionné";

$l_tasktypegroup="Groupe de type de tâche";
$l_tasktypegroup_checkdelete="Vérifier ou Supprimer le groupe de type de tâche";
$l_tasktypegroup_delete="Supprimer le groupe de type de tâche";
$l_tasktypegroup_update = "Modifier le groupe de type de tache";
$l_tasktypegroup_insert = "Créer le groupe de type de tache";
$l_tasktypegroup_manage="Gestion des groupes de types de taches";
$l_bgcolor="Arrière plan";
$l_fgcolor="Premier plan";

$l_can_delete = "Le groupe de type de tâche peut être supprimée.";
$l_cant_delete = "Le groupe de type de tâche ne peut être supprimé !";
$l_tasktypegroup_link_tasktype = "Le groupe de type de tâche est référencé par des types de tâches";
$l_tasktypegroup_link_tasktype_no = "Le groupe de type de tâche n'est pas référencé par des types de tâches";

$l_tasktype_affect = "Affectation des types de tâches aux groupes";

