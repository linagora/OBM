<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : host.inc                                                 //
// 2004-09-09 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_smb = "Windows";
$l_smb_only = "Windows seul.";
$l_host_select_user = "Choix de l'utilisateur de la machine";
$l_user = "Utilisateur";
$l_web = "Accès Web";
$l_web_mode = "Accès web autorisés";
$l_web_list = "Liste d'accès";
$l_web_all = "Tous sauf liste";
$l_web_list_only = "Uniquement liste";
$l_ftp = "Accès FTP";
$l_firewall = "Permissions avancées";
$l_perms = "Permissions";
$l_web_access = "Accès Internet";
$l_restrict = "restrictions";
$l_not_assigned = "Non assigné";

// Labels et textes
$l_host_display = "Options d'affichage des listes d'hôtes";
$l_consult_web_list = "Consulter la liste d'url";

// Actions
$l_clear = "Rompre l'association";

// Messages
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir les hôtes correspondants.";
$l_found = "hôtes trouvés.";
$l_no_found = "Aucun hôte ne correspond à ces critères.";

$l_insert_ok = "L'insertion de l'hôte a été correctement effectuée.";
$l_insert_error = "L'insertion de l'hôte a échoué !";
$l_update_ok = "La modification de l'hôte a été correctement effectuée.";
$l_update_error = "La modification de l'hôte a échoué !";
$l_delete_ok = "La suppression de l'hôte a été correctement effectuée.";
$l_delete_error = "La suppression de l'hôte a échoué !";
$l_can_delete = "L'hôte peut être supprimé";

$l_err_host_name_empty = "Le nom de l'hôte doit être renseigné";
$l_err_host_name_exist = "Le nom de l'hôte est déjà utilisé";
$l_err_host_name = "Le nom de l'hôte est incorrect";
$l_err_host_windows_not_global = "Un hôte windows ne peut pas appartenir au domaine global !";
$l_check_samehost = "Ces hôtes semblent similaires à l'hôte saisi";

$l_err_host_ip_exist = "L'adresse IP de l'hôte est déjà utilisée";
$l_err_host_ip = "L'adresse IP de l'hôte est incorrecte";
$l_err_host_ip_not_in_lan="L'adresse IP de cet hôte n'appartient à aucun réseau local";
$l_err_firewall_perms = "La syntaxe des permissions avancées n'est pas correcte";

$l_help_system = "Un hôte système est un hôte nécessaire au fonctionnement de votre plate-forme.";

$l_cant_delete = 'Cet hôte est lié à un ou plusieurs domaines et ne peux donc être supprimé';

$l_imap = 'IMAP';
$l_smtp_in = 'SMTP in';
$l_smtp_out = 'SMTP out';
$l_obm_sync = 'OBM Sync';

$l_fqdn = 'Nom complet de la machine';
$l_err_host_fqdn = 'Le nom complet de la machine est incorrct (nommachine.nom.domaine)';
