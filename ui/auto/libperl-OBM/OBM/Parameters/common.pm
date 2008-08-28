#########################################################################
# OBM           - File : OBM::Parameters::common.pm (Perl Module)	    #
#               - Desc : Librairie Perl pour OBM                        #
#               Definition des constantes et des parametres communs     #
#########################################################################
package OBM::Parameters::common;

require Exporter;
use Config::IniFiles;
use FindBin qw($Bin);


@ISA = qw(Exporter);
@EXPORT_const = qw($Bin $logLevel $facility_log $sieveSrv $singleNameSpace $backupRoot $ldapAdminLogin $ldapServer $ldapRoot $sambaOldSidMapping $cyrusDomainPartition $ldapAllMainMailAddress $obmModules $renameUserMailbox $userMailboxDefaultFolders $shareMailboxDefaultFolders $baseHomeDir $defaultCharSet $sambaRidBase $minUID $minGID $MAILBOXENTITY $MAILSHAREENTITY $USERCONSUMER);
@EXPORT_dir = qw($automateOBM $templateOBM $tmpOBM);
@EXPORT_command = qw($recode $sambaNTPass $sambaLMPass);
@EXPORT_regexp = qw($regexp_email $regexp_email_left $regexp_rootLdap $regexp_login $regexp_passwd $regexp_domain $regexp_login $regexp_ip);
@EXPORT_db = qw($userDb $userPasswd $dbName $db $dbType);
@EXPORT = (@EXPORT_const, @EXPORT_db, @EXPORT_command, @EXPORT_regexp, @EXPORT_dir);
@EXPORT_OK = qw();

# Necessaire pour le bon fonctionnement du package
$debug=1;

# Détainte la variable '$Bin'
if( (-d $Bin) && ($Bin =~ /^([\p{Alphabetic}0-9\/_\-\s\.]+)$/) ) {
    $Bin = $1;
}else {
    print STDERR "Syntaxe incorrecte du chemin d'acces aux scripts de l'automate. Le chemin d'acces ne peut contenir que les caracteres : [A-Za-z0-9/_-. ]\n";
    exit 1;
}


# Lecture du fichier ini
if( ! -r $Bin."/../conf/obm_conf.ini" ) {
    print STDERR "Le fichier de configuration 'obm_conf.ini' n'existe pas ou n'est pas lisible\n";
    exit 1;
}


$cfgFile = Config::IniFiles->new( -file => $Bin."/../conf/obm_conf.ini" );

# Initialisation du moteur de random
# srand( time ^ $$ ^ unpack "%L*", `ps auwx | gzip` );

# racine relative pour les scripts Perl
$racineOBM = $Bin."/..";

# Definition des bases de donnees
$userDb = $cfgFile->val( 'global', 'user' );
$userPasswd = $cfgFile->val( 'global', 'password' );
if( defined( $userPasswd ) && $userPasswd =~ /^"(.*)"$/ ) {
    $userPasswd = $1;
}else {
    $userPasswd = undef;
}

# La base de travail
#
# La base des mises à jours
$dbName = $cfgFile->val( 'global', 'db' );
$dbHost = $cfgFile->val( 'global', 'host' );
$dbType = lc( $cfgFile->val( 'global', 'dbtype' ));

# Conversion du nom du type de BD utilisée en driver DBI associé
SWITCH: {
    if( $dbType eq 'mysql' ) {
        $dbDriver = 'mysql';
        last SWITCH;
    }

    if( $dbType eq 'pgsql' ) {
        $dbDriver = 'Pg';
        last SWITCH;
    }
}

# Construction de la chaîne DBI de connexion à la BD
$db = "dbi:".$dbDriver.":database=$dbName;host=".$dbHost;

# Mode d'espace de nom OBM
$singleNameSpace = $cfgFile->val( 'global', 'singleNameSpace' );
if( defined( $singleNameSpace ) && lc($singleNameSpace) eq "true" ) {
    $singleNameSpace = 1;
}else {
    $singleNameSpace = 0;
}

# La racine du backup
$backupRoot = $cfgFile->val( 'global', 'backupRoot' );
if( !defined( $backupRoot ) ) {
    $backupRoot = "/var/lib/obm/backup";
}else {
    $backupRoot =~ s/^"//;
    $backupRoot =~ s/"$//;
}

# definition du niveau de log
$logLevel = $cfgFile->val( 'automate', 'logLevel' );
if( !defined($logLevel) || ($logLevel !~ /^[0-9]+$/) ) {
    $logLevel = 2;
}elsif( $logLevel > 4 ) {
    $logLevel = 4;
}elsif( $logLevel < 0 ) {
    $logLevel = 0;
}
$facility_log = "local1";

# Le login de l'administrateur LDAP
$ldapAdminLogin = "ldapadmin";
# Le serveur LDAP
$ldapServer = $cfgFile->val( 'automate', 'ldapServer' );
if( !defined($ldapServer) ) {
    $ldapServer = "127.0.0.1";
}

# Racine LDAP de l'arbre gérée pas OBM-Ldap
# exemple : 'aliasource,local' place l'arbre LDAP d'OBM-Ldap sous le DN: 'dc=aliasource,dc=local' 
$ldapRoot = $cfgFile->val( 'automate', 'ldapRoot' );
if( !defined($ldapRoot) ) {
    $ldapRoot = "local";
}

# Le mapping des UID<->SID
$sambaOldSidMapping = $cfgFile->val( 'automate', 'oldSidMapping' );
if( defined($sambaOldSidMapping) && lc($sambaOldSidMapping) eq "true" ) {
    $sambaOldSidMapping = 1;
}else {
    $sambaOldSidMapping = 0;
}

# Gestion d'une partition cyrus par domaine
$cyrusDomainPartition = $cfgFile->val( 'automate', 'cyrusPartition' );
if( defined( $cyrusDomainPartition ) && lc($cyrusDomainPartition) eq "true" ) {
    $cyrusDomainPartition = 1;
}else {
    $cyrusDomainPartition = 0;
}

# Publication des adresses principales des utilisateurs n'ayant pas le droit
# mail activé
$ldapAllMainMailAddress = $cfgFile->val( 'automate', 'ldapAllMainMailAddress' );
if( defined($ldapAllMainMailAddress) && lc($ldapAllMainMailAddress) eq "true" ) {
    $ldapAllMainMailAddress = 1;
}else {
    $ldapAllMainMailAddress = 0;
}

# Les modules OBM actifs
$obmModules = {
    ldap => 0,
    mail => 0,
    samba => 0,
    web => 0,
    contact => 0
};

$obmModule = $cfgFile->val( 'global', 'obm-ldap' );
if( defined( $obmModule ) && lc($obmModule) eq "true" ) {
    $obmModules->{"ldap"} = 1;
}else {
    $obmModules->{"ldap"} = 0;
}

$obmModule = $cfgFile->val( 'global', 'obm-mail' );
if( defined( $obmModule ) && lc($obmModule) eq "true" ) {
    $obmModules->{"ldap"} = 1;
    $obmModules->{"mail"} = 1;
}else {
    $obmModules->{"mail"} = 0;
}

$obmModule = $cfgFile->val( 'global', 'obm-samba' );
if( defined( $obmModule ) && lc($obmModule) eq "true" ) {
    $obmModules->{"ldap"} = 1;
    $obmModules->{"samba"} = 1;
}else {
    $obmModules->{"samba"} = 0;
}

$obmModule = $cfgFile->val( 'global', 'obm-web' );
if( defined( $obmModule ) && lc($obmModule) eq "true" ) {
    $obmModules->{"ldap"} = 1;
    $obmModules->{"web"} = 1;
}else {
    $obmModules->{"web"} = 0;
}

$obmModule = $cfgFile->val( 'global', 'obm-contact' );
if( defined( $obmModule ) && lc($obmModule) eq "true" ) {
    $obmModules->{"contact"} = 1;
}else {
    $obmModules->{"contact"} = 0;
}

# supporte-t-on le renommage de BAL utilisateur
$renameUserMailbox = $cfgFile->val( 'global', 'renameUserMailbox' );
if( defined( $renameUserMailbox ) && lc($renameUserMailbox) eq "true" ) {
    $renameUserMailbox = 1;
}else {
    $renameUserMailbox = 0;
}

# Creation de repertoires a la creation de l'utilisateur
$userMailboxDefaultFolders = $cfgFile->val( 'automate', 'userMailboxDefaultFolders' );
if( defined( $userMailboxDefaultFolders ) && $userMailboxDefaultFolders =~ /^"(.*)"$/ ) {
    $userMailboxDefaultFolders = $1;
}else {
    $userMailboxDefaultFolders = undef;
}

# Creation de repertoires a la creation de partage
$shareMailboxDefaultFolders = $cfgFile->val( 'automate', 'shareMailboxDefaultFolders' );
if( defined( $shareMailboxDefaultFolders ) && $shareMailboxDefaultFolders =~ /^"(.*)"$/ ) {
    $shareMailboxDefaultFolders = $1;
}else {
    $shareMailboxDefaultFolders = undef;
}

# Le repertoire pere des repertoires personnels
# Ne pas mettre le '/' de la fin du chemin
$baseHomeDir = "/home";

# Definition des fichiers modeles
#
# Le repertoire contenant les modeles
$templateOBM = $racineOBM . "/template";

# Definitions des fichiers temporaires.
#
# Le repertoire temporaire
$tmpOBM = "/tmp";


#
# Definition des divers programmes utiles.
#
# Utilitaire de recodage des caracteres de latin1->UTF8
# Préciser l'encodage du système (apache)
#$defaultCharSet = "ISO-8859-1";
$defaultCharSet = "UTF8";
$recode = "/usr/bin/recode l1..utf8";

#
# Definitions des expressions regulieres
#
# Email
$regexp_email = "^([a-z0-9_\\-]+(\\.[a-z0-9_\\-]*)*)@([a-z0-9\\-]{1,16}\\.)+[a-z]{2,6}\$";
$regexp_email_left = "^([a-z0-9_\\-]+(\\.[a-z0-9_\\-]*)*)\$";
#
# LDAP root
$regexp_rootLdap = "^dc=(.+),dc=.+\$";
#
# Login regexp
$regexp_login = "^([A-Za-z0-9][A-Za-z0-9-._]{1,31})\$";
#
# Passwd regexp
$regexp_passwd = "^[-\\\$&\\\\~#\{\(\[\|_`\^@\);\\\]+=\}%!:\\\/\\\.,?<>\\\"\\p{Alphabetic}0-9]{4,12}\$";
#
# Domain regexp
$regexp_domain = "^[0-9]+\$";
#
# Les adresses IP
$regexp_ip = "^([1-2]?[0-9]{1,2}\\\.){3}[1-2]?[0-9]{1,2}\$";

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
$automateOBM = $racineOBM . "/auto";

#
# ACL : Definition des entites et des consomateurs
$MAILBOXENTITY="mailbox";
$MAILSHAREENTITY="mailshare";
$USERCONSUMER="user";
