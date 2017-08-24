<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : backup.inc                                               //
// 2005-08-22 Aliacom - Pierre Baudracco                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

// Labels et textes
$l_bytes = "octets";

// Message
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des sauvegardes correspondantes.";
$l_backup_found = "sauvegarde(s) trouvée(s)";
$l_no_found = "Aucune sauvegarde ne correspond à ces critères !";
$l_restore_ok = "La restauration de la sauvegarde a été correctement effectuée.";
$l_restore_error = "La restauration de la sauvegarde a échoué !";

$l_err_backup_no_mail = "Cette entité ne peut être sauvegardée, car sa messagerie est désactivée.";
$l_err_host = "Cette entité ne peut être sauvegardée, car sa messagerie n'a jamais été créée.";
$l_create_backup_error = "Erreur lors de la création du fichier de sauvegarde";
$l_delete_file_error = "La suppression du fichier de sauvegarde a échoué !";
$l_err_backup_name = "Le nom du fichier de sauvegarde n'est pas correct";
$l_err_backup_file = "Le fichier de sauvegarde ne peut être lu !";
$l_err_backup_dir_not_exist = "Le répertoire de sauvegarde configuré n'existe pas !";
$l_err_backup_dir_not_writable = "Le répertoire de sauvegarde n'est pas accessible en écriture !";
$l_can_delete = "La sauvegarde peut être supprimée.";
$l_cant_delete = "La sauvegarde ne peut être supprimée !";
$l_can_create = "La sauvegarde peut être créée.";
$l_cant_create = "La sauvegarde ne peut être créée !";
$l_err_unknown_db = "Type de base de données inconnu !";

// Actions
$l_delete = "Supprimer";
$l_insert = "Créer la sauvegarde";
$l_restore = "Restaurer";

//backup
$l_restore_mailbox = "Boîte aux lettres";
$l_restore_calendar = "Agenda";
$l_restore_contact = "Contacts";
$l_restore_all = "Tout";
$l_backup_complete = "La sauvegarde a été effectuée avec succès.";
$l_restore_complete = "La restauration a été effectuée avec succès.";
$l_no_backup_available = "Aucune sauvegarde disponible !";
$l_err_cant_backup = "Une erreur est survenue et la sauvegarde n'a pas été effectuée !";
$l_err_cant_restore = "Une erreur est survenue et la restauration a échoué !";
$l_err_cant_get_availablebackup = "Impossible de récupérer la liste des sauvegardes disponibles !";
$l_cant_backup_pending_modifications = "Impossible d'effectuer une sauvegarde. L'entité a des modifications en attente, veuillez utiliser l'outil de mise à jour système pour les valider.";
$l_err_obm_satellite_usersystem = "Echec de connexion à obm-satellite : Impossible de récupérer l'utilisateur système utilisé.";

$l_push_backup_ftp_failed = "La sauvegarde n'a pas été ajoutée au serveur FTP";
$l_retrieve_from_ftp = "Récupérer les archives";
$l_retrieve_from_ftp_success = "Les archives disponibles sur le serveur FTP ont été récupérées";
$l_no_backup_ftp_specified = "Aucun serveur FTP n'a été spécifié pour le domaine.";
$l_retrieve_from_backup_ftp = "Cliquez sur le bouton ci-dessous pour récupérer toutes les archives pour '%s', disponibles sur le serveur FTP du domaine.";
$l_unable_to_retrieve_backup = "Impossible de récupérer les sauvegardes archivées sur le serveur FTP";

