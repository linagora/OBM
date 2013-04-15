<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : domain.inc                                               //
// 2006-08-17 Phan David                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$l_domain_name = "Nom de domaine";
$l_alias = "Alias";
$l_mail_server = "Choix du serveur de mail";
$l_mail_servers = "Serveurs de mail";
$l_sync_servers = "Serveurs de synchronisation";
$l_solr_servers = "Serveurs d'indexation";
$l_imap_frontend_servers = "Serveurs IMAP frontaux";
$l_max_quota = "Valeur max";
$l_update_state = "Etat de mise à jour";

// Mail chooser labels
$l_mailboxchoice_least_boxes = "gérant le moins de boîtes mail";
$l_mailboxchoice_least_used_disk = "utilisant le moins d'espace disque";
$l_mailshares_quota_default = "Dossiers partagés : quota par défaut";
$l_help_mailshares_quota_default = "Définit la quota par défaut des bals partagées de ce domaine, la valeur 0 correspond à aucun quota.";
$l_mailshares_quota_max = "Dossiers partagés : quota maximum";
$l_help_mailshares_quota_max = "Définit la quota maximum des bals partagées de ce domaine, la valeur 0 correspond à aucun quota.";

// Actions
$l_insert = "Insérer le domaine";
$l_update = "Mettre à jour le domaine";
$l_delete = "Supprimer le Domaine";

// Messages
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des domaines correspondants.";
$l_found = " domaine(s) trouvé(s)";
$l_no_found = "Aucun domaine trouvé !";
$l_label_error = "label invalide!";
$l_name_error = "Le nom de domaine est invalide!";
$l_can_delete = "Le domaine peut être supprimé.";
$l_warn_delete = "ATTENTION : Le domaine n'est pas vide.";
$l_cant_delete = "Le domaine ne peut pas être supprimé !";
$l_cant_add_domain_single = "Impossible de créer un nouveau domaine, OBM est configuré en mode mono domaine (singleNameSpace) ! Attention, le passage en multi-domaine nécessite une intervention système importante.";
$l_mailshares_quota_error = "Le quota par défaut ne peux pas être supèrieur au quota maximum";

$l_max_users = "# utilisateurs max";
$l_max_resources = "# ressources max";
$l_max_mailshares = "# dossiers partagés max";
$l_mail_quota = "Quota mail";
$l_address1 = "$l_address 1";
$l_address2 = "$l_address 2";
$l_address3 = "$l_address 3";
$l_group_admin = "Id groupe Admin";
$l_group_com = "Id groupe Commercial";
$l_group_prod = "Id groupe Production";

$l_link_user = "Le domaine a des utilisateurs";
$l_link_user_no = "Le domaine n'a pas d'utilisateurs";
$l_link_host = "Le domaine a des hôtes";
$l_link_host_no = "Le domaine n'a pas d'hôtes";
$l_link_user_mailserver = "Le serveur de messagerie contient des utilisateurs du domaine";
$l_name_in_use = "Le nom de domaine ou l'un des alias est déjà utilisé";
$l_alias_duplicated = "Les alias suivant sont dupliqués";
$l_alias_user_used = "Impossible de modifier ou supprimer des alias utilisés dans des adresses mails";

// JavaScript Messages
$l_fill_title = "Vous devez renseigner le Label avant de valider.";
$l_domain_confirm_delete = "ATTENTION : Vous allez supprimer un domaine : Cette opération est irréversible.\\nVoulez-vous continuer ?";
$l_mailserver = "Serveur de mail";

$l_mailserver_warn = 'Vous devez créer un hôte de ce type';
$l_syncserver_warn = 'Vous devez créer un hôte de ce type';
$l_opushserver_warn = 'Vous devez créer un hôte de ce type';
$l_opushfrontendserver_warn = 'Vous devez créer un hôte de ce type';
$l_solrserver_warn = 'Vous devez créer un hôte de ce type';
$l_imapfrontendserver_warn = 'Vous devez créer un hôte de ce type';

$l_imap = 'IMAP';
$l_smtp_in = 'SMTP in';
$l_smtp_out = 'SMTP out';
$l_obm_sync = 'OBM sync';
$l_solr_calendar = 'Solr';
$l_opush = 'OPush';
$l_opush_frontend = 'OPush frontend';

$l_samba = 'Serveur windows';
$l_samba_sid = 'SID du domaine';
$l_samba_domain = 'Nom de domaine windows';
$l_samba_profile = 'Chemin par défaut du profil';
$l_samba_home = 'Chemin par défaut des dossiers utilisateurs (%u sera remplacé par le login utilisateur)';
$l_samba_drive = 'Lecteur par défaut des dossiers utilisateurs';
$l_autogen_sid = 'Générer un SID';

$l_samba_sid_error = 'SID invalide, le sid doit être de la forme S-1-5-21-XXXXXXXXXX-XXXXXXXXX-XXXXXXXXXX';
$l_samba_sid_used = 'Ce SID est déjà utilisé par un autre domaine';
$l_samba_domain_error = 'Nom de domaine windows invalide';
$l_samba_domain_used = 'Ce nom de domaine est déjà utilisé par un autre domaine';
$l_samba_profile_error = 'Chemin par défaut du profil invalide, il doit être de la forme \\\\machine\\chemin\profile';
$l_samba_home_error = 'Chemin par défaut des dossiers utilisateurs invalide';
$l_samba_drive_error = 'Lecteur par défaut des dossiers utilisateurs invalide';

$l_backup_ftp_servers = 'Serveurs FTP de dépôt des sauvegardes';
$l_header_select_backupftpserver = 'Sélection du serveur FTP';
$l_backupftpserver_warn = 'Vous devez créer un hôte de ce type';

