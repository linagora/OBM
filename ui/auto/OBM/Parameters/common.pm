#########################################################################
# OBM           - File : OBM::Parameters::common.pm (Perl Module)	    #
#               - Desc : Librairie Perl pour OBM                        #
#               Definition des constantes et des parametres communs     #
#########################################################################
# Cree le 2002-07-18                                                    #
#########################################################################
# $Id$   #
#########################################################################
package OBM::Parameters::common;

require Exporter;
use Config::IniFiles;
use FindBin qw($Bin);


@ISA = qw(Exporter);
@EXPORT_const = qw($facility_log $enableHook $sieveSrv $ldapServer $sambaSrvHome $sambaOldSidMapping $obmModules $baseHomeDir $defaultCharSet $sambaRidBase $minUID $minGID $MAILBOXENTITY $MAILSHAREENTITY $USERCONSUMER);
@EXPORT_dir = qw($automateOBM $templateOBM $tmpOBM);
@EXPORT_files = qw($automateMailChangeAlias $automateMailChangeSieve $automateCyrusAdmin $automateLdapUpdate $automateLdapUpdatePasswd $automatePostfixUpdate);
@EXPORT_command = qw($recode $sambaNTPass $sambaLMPass);
@EXPORT_regexp = qw($regexp_email $regexp_rootLdap $regexp_login);
@EXPORT_db = qw($userDb $userPasswd $dbName $db);
@EXPORT = (@EXPORT_const, @EXPORT_db, @EXPORT_files, @EXPORT_command, @EXPORT_regexp, @EXPORT_dir);
@EXPORT_OK = qw();

# Necessaire pour le bon fonctionnement du package
$debug=1;

# Lecture du fichier ini
$cfgFile = Config::IniFiles->new( -file => $Bin."/../conf/obm_conf.ini" );

# Initialisation du moteur de random
# srand( time ^ $$ ^ unpack "%L*", `ps auwx | gzip` );

# definition du niveau de log
$facility_log = "local1";

# racine relative pour les scripts Perl
$racineOBM = $Bin."/..";
if( !($racineOBM =~ /\/$/) ) {
    $racineOBM .= "/";
}

# Definition des bases de donnees
$userDb = $cfgFile->val( 'global', 'user' );
$cfgFile->val( 'global', 'password' ) =~ /^"(.*)"$/;
$userPasswd = $1;

# La base de travail
#
# La base des mises à jours
$dbName = $cfgFile->val( 'global', 'db' );
$db = "dbi:".lc( $cfgFile->val( 'global', 'dbtype' )).":database=$dbName;host=".$cfgFile->val( 'global', 'host' );

# Y'a-t-il des operations specifiques a l'installation - utilisation du hook
# dans le script 'ldapModifBase.pl'
if( lc($cfgFile->val( 'automate', 'enableHook' )) eq "true" ) {
    $enableHook = 1;
}else {
    $enableHook = 0;
}

# Le serveur LDAP
$ldapServer = $cfgFile->val( 'automate', 'ldapServer' );

# Les serveurs Samba
$sambaSrvHome = $cfgFile->val( 'automate', 'sambaHomeServer' );

# Le mapping des UID<->SID
if( lc($cfgFile->val( 'automate', 'oldSidMapping' )) eq "true" ) {
    $sambaOldSidMapping = 1;
}else {
    $sambaOldSidMapping = 0;
}

# Les modules OBM actifs
$obmModules = {
    ldap => 0,
    mail => 0,
    samba => 0,
    web => 0
};

if( lc($cfgFile->val( 'global', 'obm-ldap' )) eq "true" ) {
    $obmModules->{"ldap"} = 1;
}else {
    $obmModules->{"ldap"} = 0;
}

if( lc($cfgFile->val( 'global', 'obm-mail' )) eq "true" ) {
    $obmModules->{"ldap"} = 1;
    $obmModules->{"mail"} = 1;
}else {
    $obmModules->{"mail"} = 0;
}

if( lc($cfgFile->val( 'global', 'obm-samba' )) eq "true" ) {
    $obmModules->{"ldap"} = 1;
    $obmModules->{"samba"} = 1;
}else {
    $obmModules->{"samba"} = 0;
}

if( lc($cfgFile->val( 'global', 'obm-web' )) eq "true" ) {
    $obmModules->{"ldap"} = 1;
    $obmModules->{"web"} = 1;
}else {
    $obmModules->{"web"} = 0;
}

# Le repertoire pere des repertoires personnels
# Ne pas mettre le '/' de la fin du chemin
$baseHomeDir = "/home";

# Definition des fichiers modeles
#
# Le repertoire contenant les modeles
$templateOBM = $racineOBM . "template/";

# Definitions des fichiers temporaires.
#
# Le repertoire temporaire
$tmpOBM = "/tmp/";


#
# Definition des divers programmes utiles.
#
# Utilitaire de recodage des caracteres de latin1->UTF8
# Préciser l'encodage du système (apache)
$defaultCharSet = "ISO-8859-1";
#$defaultCharSet = "UTF8";
$recode = "/usr/bin/recode l1..utf8";

#
# Definitions des expressions regulieres
#
# Balise email
$regexp_email = "^([a-z0-9_\\-]{1,16}(\\.[a-z0-9_\\-]{1,16}){0,3})@([a-z0-9\\-]{1,16}\\.){1,3}[a-z]{2,3}\$";
#
# LDAP root
$regexp_rootLdap = "^dc=(.+),dc=.+\$";
#
# Login regexp
$regexp_login = "^[A-Za-z0-9][A-Za-z0-9-._]{1,31}\$";

#
# Definitions des parametres Samba
#
# Base de calcul du RID
$sambaRidBase = 1000;
#
# UID et GID mini
$minUID = 1000;
$minGID = 1000;


#
# Les scripts de l'automate
#
# Le repertoire contenant les scripts de l'automate
$automateOBM = $racineOBM . "auto/";
#
$automateMailChangeAlias = $automateOBM . "mailChangeAlias.pl";
$automateMailChangeSieve = $automateOBM . "mailChangeSieve.pl";
$automateCyrusAdmin = $automateOBM . "mailCyrusAdmin.pl";
$automatePostfixUpdate = $automateOBM . "mailPostfixMapsUpdate.pl";
$automateLdapUpdate = $automateOBM . "ldapModifBase.pl";
$automateLdapUpdatePasswd = $automateOBM . "ldapChangePasswd.pl";

#
# ACL : Definition des entites et des consomateurs
$MAILBOXENTITY="mailbox";
$MAILSHAREENTITY="mailshare";
$USERCONSUMER="user";
