#!/usr/bin/php
<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : update-aliamin2.4-2.0.php                                    //
//     - Desc : Data migration from the aliamin 2.4 database to the obm2 one //
// 2007-04-13 Vincent Alquier                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id: update-aliamin2.4-2.0.php 0 2007-04-13 12:00:00Z vincenta $
///////////////////////////////////////////////////////////////////////////////

/*
   Script de migration de base de données aliamin vers obm2
   aliamin 2.4 vers obm 2.0

IMPORTANT:

Pré-requis:
 - La BD obm2 et toutes ses tables sont créées.
 - Les tables suivantes sont vides : Domain, Host, MailServer, MailServerNetwork,
   MailShare, Samba, UGroup, UserObm, UserObmPref, UserObmGroup, GroupGroup,
   UserSystem et EntityRight.

Utilisation:
 - modifier les chemins d'accès aux fichiers de conf d'aliamin et d'obm (voir 
   CONFIG FICHIERS CONF.) ou préciser directement les informations de connexion 
   (voir CONFIG BD ALIAMIN et CONFIG BD OBM)
 - php aliamin_2.4_vers_obm_2.0.php

Précisions:
 - Le script actualise la date de mise à jour de chaque élément inséré
 - Les données sont récupérées sur les tables de la base de prod, pour toutes les
   tables présentes dans les deux bases aliamin.
 - Le champs mobile de la table UserObm d'obm est renseigné par le contenu du 
   champs phone3 d'aliamin.
 - Le script ne renseigne pas la table MailServerNetwork.
 - Le script ne récupère pas les données des tables ActiveUserObm, DisplayPref,
   GlobalPref, Ldap, Network, ObmSession, Parameters, Stats, UserObm_SessionLog
 - Les Données récupérées proviennent des tables d'aliamin suivantes :
   Mail (prod), Host (prod), MailShareDir (prod), Samba (prod), UGroup (prod),
   UserObm (prod), UserObmPref (update), UserObmGroup (prod), GroupGroup (prod),
   UserSystem (prod), EntityRight (prod)
 - Les données sont insérées dans les tables d'obm suivantes:
   Domain, Host, MailServer, MailShare, Samba, UGroup, UserObm, UserObmPref,
   UserObmGroup, GroupGroup, UserSystem, EntityRight

*/




// CONFIG FICHIERS CONF.
// configuration des chemins d'accès au fichiers de conf d'aliamin et d'obm
// à modifier dans le cas où les chemins sont erronés et où les informations
// de connexion aux bases de données ne sont pas renseignées
$obm_config_file = '/u/vincenta/svn/obm/conf/obm_conf.ini';
$aliamin_config_file = '/etc/aliamin/aliamin_conf.ini';

// CONFIG BD ALIAMIN
// informations de connexion à la bd aliamin
// laisser en commentaire pour récupérer ses informations dans le fichier de 
// configuration d'aliamin - voir (CONFIG FICHIERS CONF.)
//$aliamin_user	= 'root';
//$aliamin_pass	= 'pass';
//$aliamin_host	= 'localhost';
//$aliamin_db		= 'aliamin_db';
//$aliamin_dbprod	= 'aliamin_dbprod';

// CONFIG BD OBM
// informations de connexion à la bd obm
// laisser en commentaire pour récupérer ses informations dans le fichier de 
// configuration d'obm - voir (CONFIG FICHIERS CONF.)
//$obm_user	= 'root';
//$obm_pass	= 'pass';
//$obm_host	= 'localhost';
//$obm_db		= 'obm2';




// Recuperation des informations de connexion à la bd d'aliamin dans le
// fichier de configuration d'aliamin
if (    empty($aliamin_db) || empty($aliamin_dbprod)
     || empty($aliamin_user) || empty($aliamin_host)) {
	echo "Lecture des infos de connexion dans le fichier $aliamin_config_file... ";
	if (!file_exists($aliamin_config_file))
		die("Le fichier de config d'aliamin $aliamin_config_file n'existe pas...\n");
	//else

	// Code repris du fichier www/aliainclude/global.inc d'Aliamin
	$ini_array = parse_ini_file($aliamin_config_file);
	$aliamin_host = $ini_array["host"];
	$aliamin_db = $ini_array["db"];
	$aliamin_dbprod = $ini_array["dbprod"];
	$aliamin_user = $ini_array["user"];
	$aliamin_pass = $ini_array["password"];

	if (empty($aliamin_db) || empty($aliamin_dbprod) || empty($aliamin_user) || empty($aliamin_host))
		die("Echec\nImpossible de lire les informations, ou informations incomplètes\n");
	//else
	echo "OK\n";
}

// Recuperation des informations de connexion à la bd d'obm dans le
// fichier de configuration d'obm
if (empty($obm_db) || empty($obm_user) || empty($obm_host)) {
	echo "Lecture des infos de connexion dans le fichier $obm_config_file... ";
	if (!file_exists($obm_config_file))
		die("Le fichier de config d'obm $obm_config_file n'existe pas...\n");
	//else

	// Code repris du fichier www/aliainclude/global.inc d'Aliamin
	$ini_array = parse_ini_file($obm_config_file);
	$obm_host = $ini_array["host"];
	$obm_db = $ini_array["db"];
	$obm_user = $ini_array["user"];
	$obm_pass = $ini_array["password"];

	if (empty($obm_db) || empty($obm_user) || empty($obm_host))
		die("Echec\nImpossible de lire les informations, ou informations incomplètes\n");
	//else
	echo "OK\n";
}




function my_query ($db, $sql, $link) {
//echo "\n------\n$sql\n";
//if (!preg_match("`SELECT`i", $sql)) return true;
	if ($res = mysql_db_query($db, $sql, $link))
		return $res;
    //else
    die('Erreur MySQL : '.mysql_error().'\n\nrequete:\n'.$sql);
}

function get_last_inserted_id ($db, $link) {
//return 1;
	$sql = " SELECT LAST_INSERT_ID(); ";
	$res = my_query($db, $sql, $link);
	if (mysql_num_rows($res)==1) {
		$row = mysql_fetch_array($res);
		return $row[0];
	}
	//else
	die ('Echec\nImpossible de récupérer l\'id...\n\nrequete:\n'.$sql);
}

function value_or_NULL ($value) {
	return ( ( empty($value) && !($value==='0')) ? 'NULL' : $value);
}
function value_or_zero ($value) {
	return ( empty($value) ? '0' : $value);
}
function value_or_one ($value) {
	return ( ( empty($value) && !($value==='0')) ? '1' : $value);
}
function protect_quote ($value) {
	return str_replace("'","''",$value);
}




/* * * * * * * * * * * * * * * * * * * * * * * *
 *   CONNEXION AUX BD
 */

// aliamin db Connexion
echo "connexion à la base de données aliamin... ";
$aliamin_link = mysql_connect($aliamin_host, $aliamin_user, $aliamin_pass)
    or die("Echec\nImpossible de se connecter à la base aliamin\n");
echo "OK\n";

// obm db Connexion
echo "connexion à la base de données obm... ";
$obm_link = mysql_connect($obm_host, $obm_user, $obm_pass)
    or die("Echec\nImpossible de se connecter à la base obm\n");
echo "OK\n";




/* * * * * * * * * * * * * * * * * * * * * * * *
 *   VIDAGE DES TABLES
 */

echo "suppression du contenu des tables... ";
$sql = " DELETE FROM Domain; ";
$res = my_query($obm_db, $sql, $obm_link);
$sql = " DELETE FROM Host; ";
$res = my_query($obm_db, $sql, $obm_link);
$sql = " DELETE FROM MailServer; ";
$res = my_query($obm_db, $sql, $obm_link);
//$sql = " DELETE FROM MailServerNetwork; ";
//$res = my_query($obm_db, $sql, $obm_link);
$sql = " DELETE FROM MailShare; ";
$res = my_query($obm_db, $sql, $obm_link);
$sql = " DELETE FROM Samba; ";
$res = my_query($obm_db, $sql, $obm_link);
$sql = " DELETE FROM UGroup; ";
$res = my_query($obm_db, $sql, $obm_link);
$sql = " DELETE FROM UserObm; ";
$res = my_query($obm_db, $sql, $obm_link);
$sql = " DELETE FROM UserObmPref; ";
$res = my_query($obm_db, $sql, $obm_link);
$sql = " DELETE FROM UserObmGroup; ";
$res = my_query($obm_db, $sql, $obm_link);
$sql = " DELETE FROM GroupGroup; ";
$res = my_query($obm_db, $sql, $obm_link);
$sql = " DELETE FROM UserSystem; ";
$res = my_query($obm_db, $sql, $obm_link);
$sql = " DELETE FROM EntityRight; ";
$res = my_query($obm_db, $sql, $obm_link);

echo "OK\n";




/* * * * * * * * * * * * * * * * * * * * * * * *
 *   INSERTION DU DOMAINE
 */

// on récupère les infos du domaine
echo "récupération des infos du domaine... ";

$sql = " SELECT mail_name, mail_value ";
$sql.= " FROM   Mail ";
$sql.= " WHERE  mail_name = 'dflt_mailserver' ";
$sql.= "    OR  mail_name = 'main_domain' ";
$sql.= "    OR  mail_name = 'domain' ";
$sql.= "    OR  mail_name = 'relayhost' ";
$res = my_query($aliamin_dbprod, $sql, $aliamin_link);

$domain_alias = "";
while ($row = mysql_fetch_array($res)) {
	if ($row[0]== 'dflt_mailserver')
		$mailserver_host_id = $row[1];
	elseif ($row[0]== 'main_domain')
		$domain_name = $row[1];
	elseif ($row[0]== 'relayhost')
		$relayhost_id = $row[1];
	elseif ($row[0]== 'domain')
		$domain_alias .= (empty($domain_alias) ? "" : " ").$row[1];
}

if (empty($domain_name))
    die ('Echec\nImpossible de trouver le nom de domaine (table Mail)');

echo "OK\n";


// insertion du domaine
echo "insertion du domaine... ";

$sql = " INSERT INTO Domain ( ";
$sql.= "    domain_timecreate, ";
$sql.= "    domain_label, ";
$sql.= "    domain_description, ";
$sql.= "    domain_name, ";
$sql.= "    domain_alias ";
$sql.= " ) ";
$sql.= " VALUES ( ";
$sql.= "    CURRENT_TIMESTAMP, ";
$sql.= "    '$domain_name', ";
$sql.= "    '', ";
$sql.= "    '$domain_name', ";
$sql.= "    '$domain_alias'";
$sql.= " ); ";
my_query($obm_db, $sql, $obm_link);

echo "OK (1)\n";


// on récupère l'id du domaine inséré
$domain_id = get_last_inserted_id ($obm_db, $obm_link);




/* * * * * * * * * * * * * * * * * * * * * * * *
 *   INSERTION DES HOTES
 */

echo "insertion des hotes... ";
$sql = " SELECT * FROM Host ";
$res = my_query($aliamin_dbprod, $sql, $aliamin_link);
if (mysql_num_rows($res)<=0) {
    die ('Echec\nAucun hote dans la table Host');
}

$nb = 0;
$champs = " host_id, ";
$champs.= " host_domain_id, ";
$champs.= " host_timeupdate, ";
$champs.= " host_timecreate, ";
$champs.= " host_userupdate, ";
$champs.= " host_usercreate, ";
$champs.= " host_uid, ";
$champs.= " host_gid, ";
$champs.= " host_samba, ";
$champs.= " host_name, ";
$champs.= " host_ip, ";
$champs.= " host_description, ";
$champs.= " host_web_perms, ";
$champs.= " host_web_list, ";
$champs.= " host_web_all, ";
$champs.= " host_ftp_perms, ";
$champs.= " host_firewall_perms ";
while ($row = mysql_fetch_assoc($res)) {
	$sql = " INSERT INTO Host ( ";
	$sql.= $champs;
	$sql.= " ) ";
	$sql.= " VALUES ( ";
	$sql.= "  ".$row["host_id"].", ";
	$sql.= "  ".$domain_id.", ";
	$sql.= "  CURRENT_TIMESTAMP, ";
	$sql.= " '".$row["host_timecreate"]."', ";
	$sql.= "  ".value_or_NULL($row["host_userupdate"]).", ";
	$sql.= "  ".value_or_NULL($row["host_usercreate"]).", ";
	$sql.= "  ".value_or_NULL($row["host_uid"]).", ";
	$sql.= "  ".value_or_NULL($row["host_gid"]).", ";
	$sql.= "  ".value_or_zero($row["host_samba"]).", ";
	$sql.= " '".protect_quote($row["host_name"])."', ";
	$sql.= " '".$row["host_ip"]."', ";
	$sql.= " '".protect_quote($row["host_description"])."', ";
	$sql.= "  ".value_or_zero($row["host_web_perms"]).", ";
	$sql.= " '".protect_quote($row["host_web_list"])."', ";
	$sql.= "  ".value_or_zero($row["host_web_all"]).", ";
	$sql.= "  ".value_or_zero($row["host_ftp_perms"]).", ";
	$sql.= " '".protect_quote($row["host_firewall_perms"])."' ";
	$sql.= " ); ";
	my_query($obm_db, $sql, $obm_link);
	$nb++;
}

echo "OK ($nb)\n";




/* * * * * * * * * * * * * * * * * * * * * * * *
 *   INSERTION DES SERVEURS DE COURRIER
 */

echo "insertion des serveurs de courrier... ";
$sql = " INSERT INTO MailServer (mailserver_host_id, mailserver_relayhost_id) ";
$sql.= " VALUES (".$mailserver_host_id.", ".(empty($relayhost_id)?0:$relayhost_id)."); ";
my_query($obm_db, $sql, $obm_link);

echo "OK (1)\n";


// on récupère l'id du serveur de courrier inséré
$mailserver_id = get_last_inserted_id ($obm_db, $obm_link);


// on met à jour le domaine pour lui donner l'id du serveur de courrier
echo "mise à jour du serveur de courrier du domaine... ";
$sql = " UPDATE Domain SET domain_mail_server_id = $mailserver_id ";
$sql.= " WHERE domain_id = $domain_id; ";
my_query($obm_db, $sql, $obm_link);

echo "OK\n";




/* * * * * * * * * * * * * * * * * * * * * * * *
 *   INSERTION DES REPERTOIRES DE PARTAGE
 */

echo "insertion des répertoires de partage... ";
$sql = " SELECT * FROM MailShareDir; ";
$res = my_query($aliamin_dbprod, $sql, $aliamin_link);

$nb = 0;
$champs = " mailshare_id, ";
$champs.= " mailshare_domain_id, ";
$champs.= " mailshare_timeupdate, ";
$champs.= " mailshare_timecreate, ";
$champs.= " mailshare_userupdate, ";
$champs.= " mailshare_usercreate, ";
$champs.= " mailshare_name, ";
$champs.= " mailshare_quota, ";
$champs.= " mailshare_mail_server_id, ";
$champs.= " mailshare_description, ";
$champs.= " mailshare_email ";
while ($row = mysql_fetch_assoc($res)) {
	$sql = " INSERT INTO MailShare ( ";
	$sql.= $champs;
	$sql.= " ) ";
	$sql.= " VALUES ( ";
	$sql.= "  ".$row["mailsharedir_id"].", ";
	$sql.= "  ".$domain_id.", ";
	$sql.= "  CURRENT_TIMESTAMP, ";
	$sql.= " '".$row["mailsharedir_timecreate"]."', ";
	$sql.= "  ".value_or_NULL($row["mailsharedir_userupdate"]).", ";
	$sql.= "  ".value_or_NULL($row["mailsharedir_usercreate"]).", ";
	$sql.= " '".protect_quote($row["mailsharedir_name"])."', ";
	$sql.= "  ".$row["mailsharedir_quota"].", ";
	$sql.= "  ".value_or_zero($mailserver_id).", ";
	$sql.= " '".protect_quote($row["mailsharedir_description"])."', ";
	$sql.= " '".$row["mailsharedir_email"]."' ";
	$sql.= " ); ";
	my_query($obm_db, $sql, $obm_link);
	$nb++;
}

echo "OK ($nb)\n";




/* * * * * * * * * * * * * * * * * * * * * * * *
 *   INSERTION DES INFOS SAMBA
 */

echo "insertion des infos de partage samba... ";
$sql = " SELECT * FROM Samba; ";
$res = my_query($aliamin_dbprod, $sql, $aliamin_link);

$nb = 0;
$champs = " samba_domain_id, ";
$champs.= " samba_name, ";
$champs.= " samba_value ";
while ($row = mysql_fetch_assoc($res)) {
	$sql = " INSERT INTO Samba ( ";
	$sql.= $champs;
	$sql.= " ) ";
	$sql.= " VALUES ( ";
	$sql.= "  ".$domain_id.", ";
	$sql.= " '".protect_quote($row["samba_name"])."', ";
	$sql.= " '".protect_quote($row["samba_value"])."' ";
	$sql.= " ); ";
	my_query($obm_db, $sql, $obm_link);
	$nb++;
}

echo "OK ($nb)\n";




/* * * * * * * * * * * * * * * * * * * * * * * *
 *   INSERTION DES GROUPES D'UTILISATEURS
 */

echo "insertion des groupes d'utilisateurs... ";
$sql = " SELECT * FROM UGroup; ";
$res = my_query($aliamin_dbprod, $sql, $aliamin_link);

$nb = 0;
$champs = " group_id, ";
$champs.= " group_domain_id, ";
$champs.= " group_timeupdate, ";
$champs.= " group_timecreate, ";
$champs.= " group_userupdate, ";
$champs.= " group_usercreate, ";
$champs.= " group_system, ";
$champs.= " group_privacy, ";
$champs.= " group_local, ";
$champs.= " group_ext_id, ";
$champs.= " group_samba, ";
$champs.= " group_gid, ";
$champs.= " group_mailing, ";
$champs.= " group_name, ";
$champs.= " group_desc, ";
$champs.= " group_email, ";
$champs.= " group_contacts ";
while ($row = mysql_fetch_assoc($res)) {
	$sql = " INSERT INTO UGroup ( ";
	$sql.= $champs;
	$sql.= " ) ";
	$sql.= " VALUES ( ";
	$sql.= "  ".$row["group_id"].", ";
	$sql.= "  ".$domain_id.", ";
	$sql.= "  CURRENT_TIMESTAMP, ";
	$sql.= " '".$row["group_timecreate"]."', ";
	$sql.= "  ".value_or_NULL($row["group_userupdate"]).", ";
	$sql.= "  ".value_or_NULL($row["group_usercreate"]).", ";
	$sql.= "  ".value_or_zero($row["group_system"]).", ";
	$sql.= "  ".value_or_zero($row["group_privacy"]).", ";
	$sql.= "  ".value_or_one($row["group_local"]).", ";
	$sql.= " '".protect_quote($row["group_ext_id"])."', ";
	$sql.= "  ".value_or_zero($row["group_samba"]).", ";
	$sql.= "  ".value_or_NULL($row["group_gid"]).", ";
	$sql.= "  0, ";
	$sql.= " '".protect_quote($row["group_name"])."', ";
	$sql.= " '".protect_quote($row["group_desc"])."', ";
	$sql.= " '".protect_quote($row["group_email"])."', ";
	$sql.= " '".protect_quote($row["group_contacts"])."' ";
	$sql.= " ); ";
	my_query($obm_db, $sql, $obm_link);
	$nb++;
}

echo "OK ($nb)\n";




/* * * * * * * * * * * * * * * * * * * * * * * *
 *   INSERTION DES GROUPES D'UTILISATEURS
 */

echo "insertion des utilisateurs... ";
$sql = " SELECT * FROM UserObm; ";
$res = my_query($aliamin_dbprod, $sql, $aliamin_link);

$nb = 0;
$champs = " userobm_id, ";
$champs.= " userobm_domain_id, ";
$champs.= " userobm_timeupdate, ";
$champs.= " userobm_timecreate, ";
$champs.= " userobm_userupdate, ";
$champs.= " userobm_usercreate, ";
$champs.= " userobm_local, ";
$champs.= " userobm_ext_id, ";
$champs.= " userobm_system, ";
$champs.= " userobm_archive, ";
$champs.= " userobm_timelastaccess, ";
$champs.= " userobm_login, ";
$champs.= " userobm_password_type, ";
$champs.= " userobm_password, ";
$champs.= " userobm_perms, ";
$champs.= " userobm_calendar_version, ";
$champs.= " userobm_uid, ";
$champs.= " userobm_gid, ";
$champs.= " userobm_datebegin, ";
$champs.= " userobm_lastname, ";
$champs.= " userobm_firstname, ";
$champs.= " userobm_title, ";
$champs.= " userobm_sound, ";
$champs.= " userobm_service, ";
$champs.= " userobm_address1, ";
$champs.= " userobm_address2, ";
$champs.= " userobm_address3, ";
$champs.= " userobm_zipcode, ";
$champs.= " userobm_town, ";
$champs.= " userobm_expresspostal, ";
$champs.= " userobm_country_iso3166, ";
$champs.= " userobm_phone, ";
$champs.= " userobm_phone2, ";
$champs.= " userobm_mobile, ";
$champs.= " userobm_fax, ";
$champs.= " userobm_fax2, ";
$champs.= " userobm_web_perms, ";
$champs.= " userobm_web_list, ";
$champs.= " userobm_web_all, ";
$champs.= " userobm_mail_perms, ";
$champs.= " userobm_mail_ext_perms, ";
$champs.= " userobm_email, ";
$champs.= " userobm_mail_server_id, ";
$champs.= " userobm_mail_quota, ";
$champs.= " userobm_nomade_perms, ";
$champs.= " userobm_nomade_enable, ";
$champs.= " userobm_nomade_local_copy, ";
$champs.= " userobm_email_nomade, ";
$champs.= " userobm_vacation_enable, ";
$champs.= " userobm_vacation_message, ";
$champs.= " userobm_samba_perms, ";
$champs.= " userobm_samba_home, ";
$champs.= " userobm_samba_home_drive, ";
$champs.= " userobm_samba_logon_script, ";
$champs.= " userobm_host_id, ";
$champs.= " userobm_description, ";
$champs.= " userobm_location, ";
$champs.= " userobm_education ";
while ($row = mysql_fetch_assoc($res)) {
	$sql = " INSERT INTO UserObm ( ";
	$sql.= $champs;
	$sql.= " ) ";
	$sql.= " VALUES ( ";
	$sql.= "  ".$row["userobm_id"].", ";
	$sql.= "  ".$domain_id.", ";
	$sql.= "  CURRENT_TIMESTAMP, ";
	$sql.= " '".$row["userobm_timecreate"]."', ";
	$sql.= "  ".value_or_NULL($row["userobm_userupdate"]).", ";
	$sql.= "  ".value_or_NULL($row["userobm_usercreate"]).", ";
	$sql.= "  ".value_or_one($row["userobm_local"]).", ";
	$sql.= " '".$row["userobm_ext_id"]."', ";
	$sql.= "  ".value_or_zero($row["userobm_system"]).", ";
	$sql.= "  ".value_or_zero($row["userobm_archive"]).", ";
	$sql.= " '".$row["userobm_timelastaccess"]."', ";
	$sql.= " '".protect_quote($row["userobm_login"])."', ";
	if (empty($row["userobm_password_plain"])) {
		$sql.= " 'MD5SUM', ";
		$sql.= " '".protect_quote($row["userobm_password"])."', ";
	} else {
		$sql.= " 'PLAIN', ";
		$sql.= " '".protect_quote($row["userobm_password_plain"])."', ";
	}
	$sql.= " '".$row["userobm_perms"]."', ";
	$sql.= " '".$row["userobm_calendar_version"]."', ";
	$sql.= "  ".value_or_NULL($row["userobm_uid"]).", ";
	$sql.= "  ".value_or_NULL($row["userobm_gid"]).", ";
	$sql.= " '".$row["userobm_datebegin"]."', ";
	$sql.= " '".protect_quote($row["userobm_lastname"])."', ";
	$sql.= " '".protect_quote($row["userobm_firstname"])."', ";
	$sql.= " '', ";
	$sql.= " '', ";
	$sql.= " '".protect_quote($row["userobm_service"])."', ";
	$sql.= " '".protect_quote($row["userobm_address1"])."', ";
	$sql.= " '', ";
	$sql.= " '', ";
	$sql.= " '".protect_quote($row["userobm_zipcode"])."', ";
	$sql.= " '".protect_quote($row["userobm_town"])."', ";
	$sql.= " '', ";
	$sql.= " '', ";
	$sql.= " '".protect_quote($row["userobm_phone"])."', ";
	$sql.= " '".protect_quote($row["userobm_phone2"])."', ";
	$sql.= " '".protect_quote($row["userobm_phone3"])."', "; // phone3 considéré comme mobile ?
	$sql.= " '".protect_quote($row["userobm_fax"])."', ";
	$sql.= " '".protect_quote($row["userobm_fax2"])."', ";
	$sql.= "  ".value_or_NULL($row["userobm_web_perms"]).", ";
	$sql.= " '".protect_quote($row["userobm_web_list"])."', ";
	$sql.= "  ".value_or_zero($row["userobm_web_all"]).", ";
	$sql.= "  ".value_or_NULL($row["userobm_mail_perms"]).", ";
	$sql.= "  ".value_or_NULL($row["userobm_mail_ext_perms"]).", ";
	$sql.= " '".$row["userobm_email"]."', ";
	$sql.= "  ".(empty($row["userobm_mail_server_id"]) ? 'NULL' : $mailserver_id).", ";
	$sql.= "  ".value_or_zero($row["userobm_mail_quota"]).", ";
	$sql.= "  ".value_or_zero($row["userobm_nomade_perms"]).", ";
	$sql.= "  ".value_or_zero($row["userobm_nomade_enable"]).", ";
	$sql.= "  ".value_or_zero($row["userobm_nomade_local_copy"]).", ";
	$sql.= " '".$row["userobm_email_nomade"]."', ";
	$sql.= "  ".value_or_zero($row["userobm_vacation_enable"]).", ";
	$sql.= " '".protect_quote($row["userobm_vacation_message"])."', ";
	$sql.= "  ".value_or_zero($row["userobm_samba_perms"]).", ";
	$sql.= " '".protect_quote($row["userobm_samba_home"])."', ";
	$sql.= " '".protect_quote($row["userobm_samba_home_drive"])."', ";
	$sql.= " '".protect_quote($row["userobm_samba_logon_script"])."', ";
	$sql.= "  ".value_or_zero($row["userobm_host_id"]).", ";
	$sql.= " '".protect_quote($row["userobm_description"])."', ";
	$sql.= " '', ";
	$sql.= " '' ";
	$sql.= " ); ";
	my_query($obm_db, $sql, $obm_link);
	$nb++;
}

echo "OK ($nb)\n";




/* * * * * * * * * * * * * * * * * * * * * * * *
 *   INSERTION DES PREFERENCES UTILISATEURS
 */

echo "insertion des préférences utilisateurs... ";
$sql = " SELECT * FROM UserObmPref; ";
$res = my_query($aliamin_db, $sql, $aliamin_link);

$nb = 0;
$champs = " userobmpref_user_id, ";
$champs.= " userobmpref_option, ";
$champs.= " userobmpref_value ";
while ($row = mysql_fetch_assoc($res)) {
	$sql = " INSERT INTO UserObmPref ( ";
	$sql.= $champs;
	$sql.= " ) ";
	$sql.= " VALUES ( ";
	$sql.= "  ".$row["userobmpref_user_id"].", ";
	$sql.= " '".protect_quote($row["userobmpref_option"])."', ";
	if ($row["userobmpref_option"]=="set_theme")
		$sql.= " 'default' ";
	else
		$sql.= " '".protect_quote($row["userobmpref_value"])."' ";
	$sql.= " ); ";
	my_query($obm_db, $sql, $obm_link);
	$nb++;
}

echo "OK ($nb)\n";




/* * * * * * * * * * * * * * * * * * * * * * * *
 *   LIAISON ENTRE UTILISATEURS ET GROUPES
 */

echo "insertion des liens utilisateurs/groupes... ";
$sql = " SELECT * FROM UserObmGroup; ";
$res = my_query($aliamin_dbprod, $sql, $aliamin_link);

$nb = 0;
$champs = " userobmgroup_group_id, ";
$champs.= " userobmgroup_userobm_id ";
while ($row = mysql_fetch_assoc($res)) {
	$sql = " INSERT INTO UserObmGroup ( ";
	$sql.= $champs;
	$sql.= " ) ";
	$sql.= " VALUES ( ";
	$sql.= "  ".$row["userobmgroup_group_id"].", ";
	$sql.= "  ".$row["userobmgroup_userobm_id"]." ";
	$sql.= " ); ";
	my_query($obm_db, $sql, $obm_link);
	$nb++;
}

echo "OK ($nb)\n";




/* * * * * * * * * * * * * * * * * * * * * * * *
 *   LIAISON ENTRE GROUPES
 */

echo "insertion des liens entre groupes... ";
$sql = " SELECT * FROM GroupGroup; ";
$res = my_query($aliamin_dbprod, $sql, $aliamin_link);

$nb = 0;
$champs = " groupgroup_parent_id, ";
$champs.= " groupgroup_child_id ";
while ($row = mysql_fetch_assoc($res)) {
	$sql = " INSERT INTO GroupGroup ( ";
	$sql.= $champs;
	$sql.= " ) ";
	$sql.= " VALUES ( ";
	$sql.= "  ".$row["groupgroup_parent_id"].", ";
	$sql.= "  ".$row["groupgroup_child_id"]." ";
	$sql.= " ); ";
	my_query($obm_db, $sql, $obm_link);
	$nb++;
}

echo "OK ($nb)\n";




/* * * * * * * * * * * * * * * * * * * * * * * *
 *   INSERTION DES UTILISATEURS SYSTEMES
 */

echo "insertion des utilisateurs système... ";
$sql = " SELECT * FROM UserSystem; ";
$res = my_query($aliamin_dbprod, $sql, $aliamin_link);

$nb = 0;
$champs = " usersystem_id, ";
$champs.= " usersystem_login, ";
$champs.= " usersystem_password, ";
$champs.= " usersystem_uid, ";
$champs.= " usersystem_gid, ";
$champs.= " usersystem_homedir, ";
$champs.= " usersystem_lastname, ";
$champs.= " usersystem_firstname, ";
$champs.= " usersystem_shell ";
while ($row = mysql_fetch_assoc($res)) {
	$sql = " INSERT INTO UserSystem ( ";
	$sql.= $champs;
	$sql.= " ) ";
	$sql.= " VALUES ( ";
	$sql.= "  ".$row["usersystem_id"].", ";
	$sql.= " '".protect_quote($row["usersystem_login"])."', ";
	$sql.= " '".protect_quote($row["usersystem_password"])."', ";
	$sql.= " '".protect_quote($row["usersystem_uid"])."', ";
	$sql.= " '".protect_quote($row["usersystem_gid"])."', ";
	$sql.= " '".protect_quote($row["usersystem_homedir"])."', ";
	$sql.= " '".protect_quote($row["usersystem_lastname"])."', ";
	$sql.= " '".protect_quote($row["usersystem_firstname"])."', ";
	$sql.= " '".protect_quote($row["usersystem_shell"])."' ";
	$sql.= " ); ";
	my_query($obm_db, $sql, $obm_link);
	$nb++;
}

echo "OK ($nb)\n";




/* * * * * * * * * * * * * * * * * * * * * * * *
 *   INSERTION DANS ENTITYRIGHT
 */

echo "insertion dans EntityRight... ";
$sql = " SELECT * FROM EntityRight ";
$res = my_query($aliamin_dbprod, $sql, $aliamin_link);

$nb = 0;
$champs = " entityright_entity_id, ";
$champs.= " entityright_entity, ";
$champs.= " entityright_consumer_id, ";
$champs.= " entityright_consumer, ";
$champs.= " entityright_write, ";
$champs.= " entityright_read ";
while ($row = mysql_fetch_assoc($res)) {
	$sql = " INSERT INTO EntityRight ( ";
	$sql.= $champs;
	$sql.= " ) ";
	$sql.= " VALUES ( ";
	$sql.= "  ".$row["entityright_entity_id"].", ";
	$sql.= " '".protect_quote($row["entityright_entity"])."', ";
	$sql.= "  ".$row["entityright_consumer_id"].", ";
	$sql.= " '".protect_quote($row["entityright_consumer"])."', ";
	$sql.= "  ".value_or_zero($row["entityright_write"]).", ";
	$sql.= "  ".value_or_zero($row["entityright_read"])." ";
	$sql.= " ); ";
	my_query($obm_db, $sql, $obm_link);
	$nb++;
}

echo "OK ($nb)\n";


?>
