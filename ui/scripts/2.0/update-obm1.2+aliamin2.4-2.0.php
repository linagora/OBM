<?php

///////////////////////////////////////////////////////////////////////////////
// OBM - File : update-obm1.2+aliamin2.4-2.0.php                             //
//     - Desc : Data migration from an obm 1.2 + aliamin 2.4 database to obm2//
// 2007-04-13 Vincent Alquier                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id:
///////////////////////////////////////////////////////////////////////////////

/*
   Script de migration de base de données obm 1.2 + aliamin 2.4 vers obm 2.0

IMPORTANT:
 - Le script modifie la base d'update d'une install aliamin 2.4 + obm 1.2 vers
   une base de données obm 2.0
 - Il utilise le script update-1.2-2.0.php
 - Ne pas oublier de faire un dump de la BD avant l'execution du script...

Pré-requis:
 - BD source en Aliamin version 2.4 et OBM version 2.0
 - Le script update-1.2-2.0.php doit être présent dans le même répertoire ou 
   lancé à la main à la fin de l'execution de ce script

Utilisation:
 - modifier les chemins d'accès aux fichiers de conf d'aliamin (voir 
   CONFIG FICHIERS CONF.) ou préciser directement les informations de connexion 
   (voir CONFIG BD ALIAMIN)
 - php update-obm1.2+aliamin2.4-2.0.php

Précisions:
 - Le script ne renseigne pas la table MailServerNetwork.
 - Un domaine est créé à partir des informations récupérées dans la table Mail

*/





$default_mailserver_host_id = 2; // id de l'hote qui heberge le serveur de courrier par defaut
// utile uniquement si le dflt_mailserver n'est pas défini dans la table Mail


// CONFIG FICHIERS CONF.
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





///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// CONNEXION A LA BD

echo "\n\n******** Connexion a la BD ********\n\n";


// Recuperation des informations de connexion à la bd d'aliamin dans le
// fichier de configuration d'aliamin
if (    empty($aliamin_db) || empty($aliamin_dbprod)
     || empty($aliamin_user) || empty($aliamin_host)) {
	echo "Lecture du fichier $aliamin_config_file...";
	if (!file_exists($aliamin_config_file))
		die("Le fichier de config d'aliamin $aliamin_config_file n'existe pas...\n");
	//else

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

echo "host:   $aliamin_host\n";
echo "user:   $aliamin_user\n";
echo "db:     $aliamin_db\n";
echo "dbprod: $aliamin_dbprod\n";


///////////////////////////////////////////////////////////////////////////////
// aliamin db Connexion
echo str_pad("Connexion a la base de donnees aliamin...",70);
$aliamin_link = mysql_connect($aliamin_host, $aliamin_user, $aliamin_pass)
    or die("Echec\nImpossible de se connecter à la base aliamin\n");
echo "OK\n";





///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// DEBUT DES MODIFICATIONS DU SCHEMA BD

echo "\n\n******** Debut des modifications du schema BD ********\n\n";


///////////////////////////////////////////////////////////////////////////////
// Mise à jour du numéro de version d'obm (dans la bd)
echo str_pad("Mise a jour du numero de version d'Obm...",70);
$sql = "UPDATE ObmInfo set obminfo_value='2.0' where obminfo_name='db_version'";
my_query($aliamin_db, $sql, $aliamin_link);
$sql = "INSERT INTO ObmInfo (obminfo_name, obminfo_value) VALUES ('update_state', '0'), ('remote_access', '0')";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";



///////////////////////////////////////////////////////////////////////////////
// Mailshare table
echo str_pad("Renommage de la table MailShareDir en MailShare...",70);
$sql = "RENAME TABLE MailShareDir TO MailShare";
my_query($aliamin_db, $sql, $aliamin_link);
$sql = "ALTER TABLE MailShare
  CHANGE mailsharedir_id          mailshare_id             INT(8)       NOT NULL AUTO_INCREMENT ,
  CHANGE mailsharedir_timeupdate  mailshare_timeupdate     TIMESTAMP    ON UPDATE CURRENT_TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  CHANGE mailsharedir_timecreate  mailshare_timecreate     TIMESTAMP    NOT NULL DEFAULT '0000-00-00 00:00:00',
  CHANGE mailsharedir_userupdate  mailshare_userupdate     INT(8)       DEFAULT NULL ,
  CHANGE mailsharedir_usercreate  mailshare_usercreate     INT(8)       DEFAULT NULL ,
  CHANGE mailsharedir_name        mailshare_name           VARCHAR(32)  DEFAULT NULL ,
  CHANGE mailsharedir_quota       mailshare_quota          INT(11)      NOT NULL DEFAULT '0',
  CHANGE mailsharedir_description mailshare_description    VARCHAR(255) DEFAULT NULL ,
  CHANGE mailsharedir_email       mailshare_email          TEXT         DEFAULT NULL ,
  ADD                             mailshare_mail_server_id INT(8)       DEFAULT '0'";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";





///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// MODIFICATIONS LIEES AU DOMAINE

echo "\n\n******** Modifications liées au domaine ********\n\n";


///////////////////////////////////////////////////////////////////////////////
// Domaine
echo str_pad("Creation de la table Domain...",70);
$sql = "CREATE TABLE Domain (
  domain_id             int(8) auto_increment,
  domain_timeupdate     timestamp(14),
  domain_timecreate     timestamp(14),
  domain_usercreate     int(8),
  domain_userupdate     int(8),
  domain_label          varchar(32) NOT NULL,
  domain_description    varchar(255),
  domain_name           varchar(128),
  domain_alias          text,  
  domain_mail_server_id int(8) DEFAULT NULL,
  PRIMARY KEY (domain_id)
)";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";

echo str_pad("Recuperation des infos du domaine...",70);
$sql = "SELECT mail_name, mail_value
FROM   Mail
WHERE  mail_name = 'dflt_mailserver'
   OR  mail_name = 'main_domain'
   OR  mail_name = 'domain'
   OR  mail_name = 'relayhost'";
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
if (!isset($mailserver_host_id)) $mailserver_host_id = $default_mailserver_host_id;
if (empty($domain_name))
    die ('Echec\nImpossible de trouver le nom de domaine (table Mail)');
echo "OK\n";

echo str_pad("Insertion du domaine...",70);

$sql = "INSERT INTO Domain (
  domain_timecreate,
  domain_label,
  domain_description,
  domain_name,
  domain_alias
)
VALUES (
  CURRENT_TIMESTAMP,
  '$domain_name',
  '',
  '$domain_name',
  '$domain_alias'); ";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK (".get_affected_rows().")\n";

// on récupère l'id du domaine inséré
$domain_id = get_last_inserted_id ($aliamin_db, $aliamin_link);



///////////////////////////////////////////////////////////////////////////////
// Update All tables to include Domain info
echo "Modification du schema et des donnees pour prendre en compte le domaine :\n";

alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "UserObm", "userobm", "after userobm_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "DataSource", "datasource", "after datasource_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Country", "country", "first");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Region", "region", "after region_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "CompanyType", "companytype", "after companytype_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "CompanyActivity", "companyactivity", "after companyactivity_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "CompanyNafCode", "companynafcode", "after companynafcode_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Company", "company", "after company_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Contact", "contact", "after contact_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Kind", "kind", "after kind_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "ContactFunction", "contactfunction", "after contactfunction_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "LeadSource", "leadsource", "after leadsource_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Lead", "lead", "after lead_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "ParentDeal", "parentdeal", "after parentdeal_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Deal", "deal", "after deal_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "DealStatus", "dealstatus", "after dealstatus_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "DealType", "dealtype", "after dealtype_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "DealCompanyRole", "dealcompanyrole", "after dealcompanyrole_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "List", "list", "after list_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "CalendarEvent", "calendarevent", "after calendarevent_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "CalendarCategory1", "calendarcategory1", "after calendarcategory1_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Todo", "todo", "after todo_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Publication", "publication", "after publication_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "PublicationType", "publicationtype", "after publicationtype_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Subscription", "subscription", "after subscription_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "SubscriptionReception", "subscriptionreception", "after subscriptionreception_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Document", "document", "after document_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "DocumentMimeType", "documentmimetype", "after documentmimetype_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Project", "project", "after project_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "TaskType", "tasktype", "after tasktype_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Contract", "contract", "after contract_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "ContractType", "contracttype", "after contracttype_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "ContractPriority", "contractpriority", "after contractpriority_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "ContractStatus", "contractstatus", "after contractstatus_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Incident", "incident", "after incident_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "IncidentPriority", "incidentpriority", "after incidentpriority_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "IncidentStatus", "incidentstatus", "after incidentstatus_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Invoice", "invoice", "after invoice_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Payment", "payment", "after payment_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Account", "account", "after account_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "UGroup", "group", "after group_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Import", "import", "after import_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Resource", "resource", "after resource_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "RGroup", "rgroup", "after rgroup_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "Host", "host", "after host_id");
alter_and_update_for_domains ($aliamin_db, $aliamin_link, $domain_id, "MailShare", "mailshare", "after mailshare_id");
echo str_pad("    table Samba...",70);
$sql = "ALTER TABLE Samba ADD Column samba_domain_id int(8) default 0 first";
my_query($aliamin_db, $sql, $aliamin_link);
$sql = "UPDATE Samba SET samba_domain_id = $domain_id";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK (".get_affected_rows().")\n";
echo str_pad("    table InvoiceStatus...",70);
$sql = "ALTER TABLE InvoiceStatus ADD Column invoicestatus_domain_id int(8) default 0 after invoicestatus_id";
my_query($aliamin_db, $sql, $aliamin_link);
$sql = "UPDATE InvoiceStatus SET invoicestatus_domain_id = $domain_id";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK (".get_affected_rows().")\n";
echo str_pad("    table PaymentKind...",70);
$sql = "ALTER TABLE PaymentKind ADD Column paymentkind_domain_id int(8) default 0 after paymentkind_id";
my_query($aliamin_db, $sql, $aliamin_link);
$sql = "UPDATE PaymentKind SET paymentkind_domain_id = $domain_id";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK (".get_affected_rows().")\n";





///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// MODIFICATIONS LIEES AUX SERVEURS DE COURRIER

echo "\n\n******** Modifications liées aux serveurs de courrier ********\n\n";


///////////////////////////////////////////////////////////////////////////////
// Table MailServerNetwork
echo str_pad("Creation de la table MailServerNetwork...",70);
$sql = "CREATE TABLE MailServerNetwork (
  mailservernetwork_host_id  int(8) NOT NULL default 0,
  mailservernetwork_ip       varchar(16) NOT NULL default ''
)";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";



///////////////////////////////////////////////////////////////////////////////
// INSERTION DES SERVEURS DE COURRIER

echo str_pad("Creation de la table MailServer...",70);
$sql="CREATE TABLE MailServer (
  mailserver_id            int(8) NOT NULL auto_increment,
  mailserver_host_id       int(8) NOT NULL default 0,
  mailserver_relayhost_id  int(8) default NULL,
  PRIMARY KEY (mailserver_id)
)";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";

echo str_pad("Insertion des serveurs de courrier... ",70);
$sql = "SELECT distinct(userobm_mail_server_id) FROM UserObm";
$res = my_query($aliamin_db, $sql, $aliamin_link);
$nb = 0;
while ($row = mysql_fetch_array($res)) {
  if (!empty($row[0])) {
    $sql = "INSERT INTO MailServer (mailserver_host_id, mailserver_relayhost_id)
      VALUES (".$row[0].", ".(empty($relayhost_id)?0:$relayhost_id)."); ";
    my_query($aliamin_db, $sql, $aliamin_link);
    $nb += get_affected_rows();
  }
}

$sql = "SELECT mailserver_id FROM MailServer WHERE mailserver_host_id=$mailserver_host_id";
$res = my_query($aliamin_db, $sql, $aliamin_link);
// si le serveur de courrier par defaut a été créé
if ($row = mysql_fetch_array($res)) {
  // on récupère l'id du serveur de courrier par defaut
  $mailserver_id = $row[0];
} else {
  // on insère le serveur de courrier par defaut
  $sql = " INSERT INTO MailServer (mailserver_host_id, mailserver_relayhost_id) ";
  $sql.= " VALUES (".$mailserver_host_id.", ".(empty($relayhost_id)?0:$relayhost_id).") ";
  my_query($aliamin_db, $sql, $aliamin_link);
  $nb += get_affected_rows();
  // on récupère l'id du serveur de courrier par defaut
  $mailserver_id = get_last_inserted_id ($aliamin_db, $aliamin_link);
}
echo "OK ($nb)\n";

// on met à jour le domaine pour lui donner l'id du serveur de courrier par defaut
echo str_pad("mise a jour du serveur de courrier du domaine... ",70);
$sql = "UPDATE Domain SET domain_mail_server_id = $mailserver_id 
  WHERE domain_id = $domain_id";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK (".get_affected_rows().")\n";

// on met à jour les mailshares pour leur donner l'id du serveur de courrier par defaut
echo str_pad("mise a jour du serveur de courrier des mailshares... ",70);
$sql = " UPDATE MailShare SET mailshare_mail_server_id = $mailserver_id";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK (".get_affected_rows().")\n";

// on met à jour les utilisateurs pour leur donner l'id de leur serveur de courrier
echo str_pad("mise a jour des utilisateurs : affectation à leur serveur de courrier...",70);
$sql = "SELECT mailserver_id, mailserver_host_id FROM MailServer";
$res = my_query($aliamin_db, $sql, $aliamin_link);
while ($row = mysql_fetch_array($res)) {
  $sql = "UPDATE UserObm SET userobm_mail_server_id = ".$row[0]."+10000 WHERE userobm_mail_server_id = ".$row[1];
  my_query($aliamin_db, $sql, $aliamin_link);
}
$sql = "UPDATE UserObm SET userobm_mail_server_id = $mailserver_id WHERE userobm_mail_server_id < 10000 OR userobm_mail_server_id IS NULL";
my_query($aliamin_db, $sql, $aliamin_link);
$nb = get_affected_rows();
$sql = "UPDATE UserObm SET userobm_mail_server_id = userobm_mail_server_id-10000 WHERE userobm_mail_server_id >= 10000";
my_query($aliamin_db, $sql, $aliamin_link);
$nb += get_affected_rows();
echo "OK ($nb)\n";





///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// MODIFICATIONS LIEES AU PASSAGE OBM 1.2 vers OBM 2.0

echo "\n\n******** Modifications liées au passage obm 1.2 -> 2.0 ********\n\n";


///////////////////////////////////////////////////////////////////////////////
// Global Category table
echo str_pad("Creation de la table Category...",70);
$sql = "CREATE TABLE Category (
  category_id          int(8) auto_increment,
  category_domain_id   int(8) NOT NULL default 0,
  category_timeupdate  timestamp(14),
  category_timecreate  timestamp(14),
  category_userupdate  int(8) NOT NULL default 0,
  category_usercreate  int(8) NOT NULL default 0,
  category_category    varchar(24) NOT NULL default '',
  category_code        varchar(10) NOT NULL default '',
  category_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (category_id),
  INDEX cat_idx_cat (category_category)
)";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";

echo str_pad("Creation de la table CategoryLink...",70);
$sql = "CREATE TABLE CategoryLink (
  categorylink_category_id int(8) NOT NULL default 0,
  categorylink_entity_id   int(8) NOT NULL default 0,
  categorylink_category    varchar(24) NOT NULL default '',
  categorylink_entity      varchar(32) NOT NULL default '',
  PRIMARY KEY (categorylink_category_id, categorylink_entity_id),
  INDEX catl_idx_ent (categorylink_entity_id),
  INDEX catl_idx_cat (categorylink_category)
)";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";



///////////////////////////////////////////////////////////////////////////////
// Move IncidentCategory1 to IncidentResolutionType
echo str_pad("Creation de la table IncidentResolutionType...",70);
$sql = "CREATE TABLE IncidentResolutionType (
  incidentresolutiontype_id          int(8) auto_increment,
  incidentresolutiontype_domain_id   int(8) default 0,
  incidentresolutiontype_timeupdate  timestamp(14),
  incidentresolutiontype_timecreate  timestamp(14),
  incidentresolutiontype_userupdate  int(8) default NULL,
  incidentresolutiontype_usercreate  int(8) default NULL,
  incidentresolutiontype_code        varchar(10) default '',
  incidentresolutiontype_label       varchar(32) default NULL,
PRIMARY KEY (incidentresolutiontype_id)
)";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";

echo str_pad("Insertion d'un element dans la table IncidentResolutionType...",70);
$sql = "INSERT INTO IncidentResolutionType (
  incidentresolutiontype_id,
  incidentresolutiontype_domain_id,
  incidentresolutiontype_timeupdate,
  incidentresolutiontype_timecreate,
  incidentresolutiontype_userupdate,
  incidentresolutiontype_usercreate,
  incidentresolutiontype_code,
  incidentresolutiontype_label)
SELECT
  incidentcategory1_id,
  $domain_id,
  incidentcategory1_timeupdate,
  incidentcategory1_timecreate,
  incidentcategory1_userupdate,
  incidentcategory1_usercreate,
  incidentcategory1_code,
  incidentcategory1_label
FROM IncidentCategory1";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK (".get_affected_rows().")\n";

echo str_pad("Modif. de la table Incident liee au type de resolution...",70);
$sql = "ALTER TABLE Incident ADD COLUMN incident_resolutiontype_id int(8) DEFAULT 0 after incident_status_id";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK (".get_affected_rows().")\n";

echo str_pad("Mise a jours des incidents liee au champ IncidentResolutionType...",70);
$sql = "UPDATE Incident set incident_resolutiontype_id=incident_category1_id, incident_timeupdate = incident_timeupdate";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK (".get_affected_rows().")\n";

echo str_pad("Suppression de la colonne categorie d'incident...",70);
$sql = "ALTER TABLE Incident DROP COLUMN incident_category1_id";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";

echo str_pad("Suppression de la table IncidentCategory1...",70);
$sql = "DROP TABLE IncidentCategory1";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";



///////////////////////////////////////////////////////////////////////////
// Add Project reference and cv infos

echo str_pad("Ajout de colonnes manquantes a la table Project...",70);
$sql = "ALTER TABLE Project
  ADD COLUMN project_reference_date varchar(32) DEFAULT '',
  ADD COLUMN project_reference_duration varchar(16) DEFAULT '',
  ADD COLUMN project_reference_desc text DEFAULT '',
  ADD COLUMN project_reference_tech text DEFAULT ''";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";

echo str_pad("Creation de la table CV...",70);
$sql = "CREATE TABLE CV (
  cv_id              int(8) auto_increment,
  cv_domain_id       int(8) default 0,
  cv_timeupdate      timestamp(14),
  cv_timecreate      timestamp(14),
  cv_userupdate      int(8),
  cv_usercreate      int(8),
  cv_userobm_id      int(8) NOT NULL,
  cv_title           varchar(255),
  cv_additionnalrefs text,
  cv_comment         text,
  PRIMARY KEY(cv_id)
)";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";

echo str_pad("Creation de la table ProjectCV...",70);
$sql = "CREATE TABLE ProjectCV (
  projectcv_project_id int(8) NOT NULL,
  projectcv_cv_id      int(8) NOT NULL,
  projectcv_role       varchar(128) DEFAULT '',
  PRIMARY KEY(projectcv_project_id, projectcv_cv_id)
)";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";

echo str_pad("Creation de la table ProjectCV...",70);
$sql = "CREATE TABLE DefaultOdtTemplate (
  defaultodttemplate_id           int(8) auto_increment,
  defaultodttemplate_domain_id    int(8) DEFAULT 0,
  defaultodttemplate_entity       varchar(32),
  defaultodttemplate_document_id  int(8) NOT NULL,
  defaultodttemplate_label        varchar(64) DEFAULT '',
  PRIMARY KEY(defaultodttemplate_id)
)";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";

echo str_pad("Ajout d'une colonne manquante a la table Contact...",70);
$sql = "ALTER TABLE Contact ADD COLUMN contact_newsletter char(1) DEFAULT '0' AFTER contact_mailing_ok";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";

echo str_pad("Mise a NULL des dates d'expiration et de paiement vides (table Invoice)...",70);
$sql = "UPDATE Invoice SET invoice_expiration_date = NULL where invoice_expiration_date = '0000-00-00'";
my_query($aliamin_db, $sql, $aliamin_link);
$nb = get_affected_rows();
$sql = "UPDATE Invoice SET invoice_payment_date = NULL where invoice_payment_date = '0000-00-00'";
my_query($aliamin_db, $sql, $aliamin_link);
$nb += get_affected_rows();
echo "OK ($nb)\n";





///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// MODIFICATION DE LA TABLE UserObm

echo "\n\n******** Modifications de la table UserObm ********\n\n";


///////////////////////////////////////////////////////////////////////////////
// Update UserObm table from Aliamin and to OBM2

echo str_pad("Modification de la structure de la table UserObm...",70);
$sql = "ALTER TABLE UserObm
  ADD COLUMN userobm_location        varchar(255) DEFAULT '' AFTER userobm_description,
  ADD COLUMN userobm_education       varchar(255) DEFAULT '' AFTER userobm_location,
  ADD COLUMN userobm_mobile          varchar(32)  DEFAULT '' AFTER userobm_phone2,
  ADD COLUMN userobm_password_type   char(6)      DEFAULT 'PLAIN' AFTER userobm_login,
  ADD COLUMN userobm_title           varchar(64)  DEFAULT '' AFTER userobm_firstname,
  ADD COLUMN userobm_sound           varchar(48)  AFTER userobm_title,
  ADD COLUMN userobm_address2        varchar(64)  AFTER userobm_address1,
  ADD COLUMN userobm_address3        varchar(64)  AFTER userobm_address2,
  ADD COLUMN userobm_expresspostal   varchar(16)  AFTER userobm_town,
  ADD COLUMN userobm_country_iso3166 char(2)      DEFAULT '0' AFTER userobm_expresspostal";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";

echo str_pad("Modif. de la gestion des passwords utilisateurs (table UserObm)...",70);
$sql = "UPDATE UserObm
  SET userobm_password_type = 'MD5SUM'
  WHERE userobm_password_plain=''";
my_query($aliamin_db, $sql, $aliamin_link);
$nb = get_affected_rows();
$sql = "UPDATE UserObm
  SET userobm_password_type = 'PLAIN',
      userobm_password = userobm_password_plain
  WHERE userobm_password_type <> 'MD5SUM'";
my_query($aliamin_db, $sql, $aliamin_link);
$nb += get_affected_rows();
echo "OK ($nb)\n";

echo str_pad("Modif. de la structure de la table UserObm pour gerer les passwords...",70);
$sql = "ALTER TABLE UserObm
  DROP COLUMN userobm_password_plain,
  CHANGE COLUMN userobm_password userobm_password varchar(64)";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";

echo str_pad("Mise a jour des preferences utilisateurs...",70);
$sql = "UPDATE UserObmPref set userobmpref_value='default' WHERE userobmpref_option='set_theme';";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK (".get_affected_rows().")\n";

/*echo str_pad("creation de la table UserSystem...",70);
$sql = "CREATE TABLE UserSystem (
  usersystem_id         int(8) NOT NULL auto_increment,
  usersystem_login      varchar(32) NOT NULL default '',
  usersystem_password   varchar(32) NOT NULL default '',
  usersystem_uid        varchar(6) default NULL,
  usersystem_gid        varchar(6) default NULL,
  usersystem_homedir    varchar(32) NOT NULL default '/tmp',
  usersystem_lastname   varchar(32) default NULL,
  usersystem_firstname  varchar(32) default NULL,
  usersystem_shell      varchar(32) default NULL,
  PRIMARY KEY (usersystem_id),
  UNIQUE KEY k_login_user (usersystem_login)
)";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";

echo "insertion des utilisateurs système... ";
$sql = " SELECT * FROM UserSystem; ";
$res = my_query($aliamin_dbprod, $sql, $aliamin_link);
$nb = 0;
while ($row = mysql_fetch_assoc($res)) {
  $sql = "  INSERT INTO UserSystem (
    usersystem_id,
    usersystem_login,
    usersystem_password,
    usersystem_uid,
    usersystem_gid,
    usersystem_homedir,
    usersystem_lastname,
    usersystem_firstname,
    usersystem_shell 
  )
  VALUES ( ";
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
  my_query($aliamin_db, $sql, $aliamin_link);
  $nb++;
}
echo "OK ($nb)\n";*/





///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// SUPPRESSION DE TABLES ALIAMIN

echo "\n\n******** Suppressions de tables ********\n\n";


///////////////////////////////////////////////////////////////////////////////
// Suppression de tables aliamin
drop_table($aliamin_db,$aliamin_link,"Mail");
drop_table($aliamin_db,$aliamin_link,"GlobalPref");
drop_table($aliamin_db,$aliamin_link,"Ldap");
drop_table($aliamin_db,$aliamin_link,"Network");
drop_table($aliamin_db,$aliamin_link,"Parameters");





///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// CREATION DES TABLES DE PROD

echo "\n\n******** Creation des tables de production ********\n\n";


///////////////////////////////////////////////////////////////////////////////
// OBM-Mail, OBM-LDAP Production tables (used by automate)

create_production_table($aliamin_db, $aliamin_link, "Domain");
create_production_table($aliamin_db, $aliamin_link, "UserObm");
create_production_table($aliamin_db, $aliamin_link, "UGroup");
create_production_table($aliamin_db, $aliamin_link, "UserObmGroup");
create_production_table($aliamin_db, $aliamin_link, "GroupGroup");
create_production_table($aliamin_db, $aliamin_link, "Host");
create_production_table($aliamin_db, $aliamin_link, "Samba");
create_production_table($aliamin_db, $aliamin_link, "MailServer");
create_production_table($aliamin_db, $aliamin_link, "MailServerNetwork");
create_production_table($aliamin_db, $aliamin_link, "MailShare");
create_production_table($aliamin_db, $aliamin_link, "EntityRight");
echo str_pad("creation de of_usergroup a partir de UserObmGroup...",70);
//CREATE TABLE P_of_usergroup like UserObmGroup
$sql = "CREATE TABLE of_usergroup like UserObmGroup";
my_query($aliamin_db, $sql, $aliamin_link);
echo "OK\n";





///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// QUELQUES PETITES VERIFICATIONS

echo "\n\n******** Quelques verifications ********\n\n";

// Il existe un domaine...
$sql = "SELECT count(domain_id) FROM Domain";
$res = my_query($aliamin_db, $sql, $aliamin_link);
$row = mysql_fetch_array($res);
$nb = $row[0];
if ($nb <= 0)
  echo "ATTENTION: Il n'existe pas de domaine !!!\n";

// Il existe un serveur de courrier...
$sql = "SELECT count(mailserver_id) FROM MailServer";
$res = my_query($aliamin_db, $sql, $aliamin_link);
$row = mysql_fetch_array($res);
$nb = $row[0];
if ($nb <= 0)
  echo "ATTENTION: Il n'existe pas de serveur de courrier !!!\n";

// Aucun utilisateur n'a de mail_server_id à NULL...
$sql = "SELECT count(userobm_id) FROM UserObm WHERE userobm_mail_server_id IS NULL";
$res = my_query($aliamin_db, $sql, $aliamin_link);
$row = mysql_fetch_array($res);
$nb = $row[0];
if ($nb>0)
  echo "ATTENTION: $nb utilisateurs (table UserObm) ont le mail_server_id à NULL !!!\n";

// Aucun utilisateur n'a un gid ou uid à NULL...
$sql = "SELECT count(userobm_id) FROM UserObm WHERE userobm_uid IS NULL OR userobm_gid IS NULL";
$res = my_query($aliamin_db, $sql, $aliamin_link);
$row = mysql_fetch_array($res);
$nb = $row[0];
if ($nb>0)
  echo "ATTENTION: $nb utilisateurs (table UserObm) ont un uid et/ou gid à NULL !!!\n";

// Aucun groupe n'a un gid à NULL...
$sql = "SELECT count(group_id) FROM UGroup WHERE group_gid IS NULL";
$res = my_query($aliamin_db, $sql, $aliamin_link);
$row = mysql_fetch_array($res);
$nb = $row[0];
if ($nb>0)
  echo "ATTENTION: $nb groupes (table UGroup) ont un gid à NULL !!!\n";





///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// EXECUTION DU SCRIPT update-1.2-2.0.php

echo "\n\n******** Exécution du script update-1.2-2.0.php ********\n\n";

include("update-1.2-2.0.php");





exit;



///////////////////////////////////////////////////////////////////////////////
// fonctions outils

function my_query ($db, $sql, $link) {
//echo "\n------\n$sql\n";
//if (!preg_match('`^SELECT`i', $sql)) return true;
  if ($res = mysql_db_query($db, $sql, $link))
    return $res;
  //else
  die ("ERREUR
  * Erreur MySQL : ".mysql_error()."
  * requete:
  * $sql
");
}

function get_last_inserted_id ($db, $link) {
//return 1;
  $sql = "SELECT LAST_INSERT_ID();";
  $res = my_query($db, $sql, $link);
  if (mysql_num_rows($res)==1) {
    $row = mysql_fetch_array($res);
    return $row[0];
  }
  //else
  die ("ERREUR
  * Echec
  * Impossible de récupérer l'id...
  * requete:
  * $sql
");
}

function get_affected_rows () {
//return 1;
  return mysql_affected_rows();
}

// very specific function
function drop_table ($db, $link, $table) {
  echo str_pad("Suppression de la table $table...",70);
  $sql = "DROP TABLE $table";
  my_query($db, $sql, $link);
  echo "OK\n";
}

function alter_and_update_for_domains ($db, $link, $domain_id, $table, $prefix, $position) {
  echo str_pad("    table $table...",70);
  $sql = "ALTER TABLE $table ADD Column ${prefix}_domain_id int(8) default 0 $position";
  my_query($db, $sql, $link);
  $sql = "UPDATE $table SET ${prefix}_timeupdate = ${prefix}_timeupdate, ${prefix}_domain_id = $domain_id";
  my_query($db, $sql, $link);
  echo "OK (".get_affected_rows().")\n";
}

function create_production_table ($db, $link, $table) {
  echo str_pad("creation de P_$table a partir de $table...",70);
  $sql = "CREATE TABLE P_$table like $table";
  my_query($db, $sql, $link);
  echo "OK\n";
}

?>
