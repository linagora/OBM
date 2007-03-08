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
@EXPORT_const = qw($facility_log $securinetMode $enableHook $sieveSrv $ldapServer $ldapAdminLogin $sambaSrvHome $baseHomeDir $defaultCharSet $sambaRidBase $minUID $minGID $rsaPrivateKey $rsaPublicKey $MAILBOXENTITY $MAILSHAREENTITY $USERCONSUMER);
@EXPORT_dir = qw($automateAliamin $templateAliamin $tmpAliamin);
@EXPORT_files = qw($templatePostfixAliases $tmpPostfixAliases $aliaminPostfixAliases $automateMailAliases $automateMailChangeAlias $automateMailChangeSieve $automateMailStat $automateCyrusAdmin $automateLdapDatabase $automateLdapCommit $automateLdapUpdate $automateLdapUpdatePasswd $automatePostfixConf $automateNameServer $automateSquidCache $automateNetwork $automateFirewall $automateVPN $automateAmavis $aliaminMailLog $templateLdapDatabase $tmpLdapDatabase $aliaminSlapdConf $aliaminSlapdConfNew $aliaminSlapdRep $aliaminSlapdRepNew $slapdControl $templateSquidConf $tmpSquidUserURLList $tmpSquidHostURLList $tmpSquidConf $squidUserURLList $squidHostURLList $squidAuthenticateProgram $squidConf $aliaminVPNKernelConf $aliaminPareFeuFirewallsh $aliaminPareFeuFlushfirewallsh $aliaminPareFeuEnablessh);
@EXPORT_command = qw($ldapPasswdSSHAGenerator $ldapPasswdMD5Generator $aliaminMailStat $ldapMakeNewBase $recode $aliaminPasswd $sambaNTPass $sambaLMPass $automateStateSSHScript $automateSpecificCmd $automateBackup);
@EXPORT_regexp = qw($findTag $endLoopTag $regexp_email $regexp_rootLdap $regexp_login);
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

# Fonctionne-t-on enmode Securinet
$securinetMode = 0;
if( $cfgFile->val( 'global', 'securinet' ) eq "true" ) {
    $securinetMode = 1;
}

# racine relative pour les scripts Perl
$racineAliamin = $Bin."/..";
if( !($racineAliamin =~ /\/$/) ) {
    $racineAliamin .= "/";
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
if( $cfgFile->val( 'automate', 'enableHook' ) eq "true" ) {
    $enableHook = 1;
}else {
    $enableHook = 0;
}

# Le serveur LDAP
$ldapServer = $cfgFile->val( 'automate', 'ldapServer' );
# Le login de l'administrateur LDAP
$ldapAdminLogin = "ldapadmin";

# Les serveurs Samba
$sambaSrvHome = $cfgFile->val( 'automate', 'sambaHomeServer' );

# Le repertoire pere des repertoires personnels
# Ne pas mettre le '/' de la fin du chemin
$baseHomeDir = "/home";

# Definition des fichiers modeles
#
# Le repertoire contenant les modeles
$templateAliamin = $racineAliamin . "template/";
# Messagerie :
$templatePostfixAliases = $templateAliamin . "templatePostfixAliases";
# LDAP :
$templateLdapDatabase = $templateAliamin . "templateLdapDatabase";  # fichier ldif

# Squid :
$templateSquidConf = $templateAliamin . "templateSquidConf";

# Definitions des fichiers temporaires.
#
# Le repertoire temporaire
$tmpAliamin = "/tmp/";
# Messagerie :
$tmpPostfixAliases = $tmpAliamin . "aliases";
# LDAP :
$tmpLdapDatabase = $tmpAliamin . "ldapDatabase.ldif";

# Squid
$tmpSquidConf = $tmpAliamin . "squid.conf";
$tmpSquidUserURLList = $tmpAliamin . "UserURLList";
$tmpSquidHostURLList = $tmpAliamin . "HostURLList";

# Definition des fichiers correspondants aux fichiers modeles.
#
# Messagerie :
$aliaminPostfixAliases = "/etc/aliases";
#
# LDAP :
$aliaminSlapdConf = "/etc/ldap/slapd.conf"; # Fichier de configuration du service
$aliaminSlapdConfNew = "/etc/ldap/slapd.conf-new";    # Fichier de configuration utilise pour creer un nouvel annuaire
$aliaminSlapdRep = "/var/lib/ldap"; # Le repertoire contenant les fichier de l'annuaire de production
$aliaminSlapdRepNew = "/var/lib/ldap-new";  # Le repertoire des fichiers contenant le nouvel annuaire
#
# Squid
$squidAuthenticateProgram = "/usr/lib/squid/ldap_auth";
$squidConf = "/etc/squid/squid.conf";
$squidUserURLList = "/var/squid/UserURLList";
$squidHostURLList = "/var/squid/HostURLList";
#
# VPN et Reseau
$aliaminVPNKernelConf = "/sbin/enableVPN.sh";
$aliaminPareFeuFirewallsh = "/sbin/firewall.sh";
$aliaminPareFeuFlushfirewallsh = "/sbin/flushFirewall.sh";
$aliaminPareFeuEnablessh =  "/sbin/enableSSH.sh";


#
# Definition des divers programmes utiles.
#
# Les statistiques de messageries
$aliaminMailStat = "/usr/sbin/pflogsumm.pl";
$aliaminMailLog = "/var/log/mail.log";
$aliaminPasswd = "/usr/sbin/saslpasswd2";
#
# Les utilitaires LDAP
$slapdControl = "/etc/init.d/slapd";
$ldapPasswdSSHAGenerator = "/usr/sbin/slappasswd -h {SSHA}";
$ldapPasswdMD5Generator = "/usr/sbin/slappasswd -h {MD5}";
$ldapMakeNewBase = "/usr/sbin/slapadd -c -f $aliaminSlapdConfNew -l";
#
# Utilitaire de recodage des caracteres de latin1->UTF8
# Préciser l'encodage du système (apache)
#$defaultCharSet = "ISO-8859-1";
$defaultCharSet = "UTF8";
$recode = "/usr/bin/recode l1..utf8";

#
# Definitions des expressions regulieres
#
# Recherche des balises dans les fichiers template
$findTag = "<ALIAMIN-([0-9a-zA-Z_]*)>";
#
# Balise de fin de boucle dans les fichiers template
$endLoopTag = "<ALIAMIN-END-([0-9a-zA-Z_]*)>";
#
# Balise email
$regexp_email = "^([a-z0-9_\\-]{1,16}(\\.[a-z0-9_\\-]{1,16}){0,3})@([a-z0-9\\-]{1,16}\\.){1,3}[a-z]{2,3}\$";
#
# LDAP root
$regexp_rootLdap = "^dc=(.+),dc=.+\$";
#
# Login regexp
if( $loginDotsEnable ) {
    $regexp_login = "^[A-Za-z0-9][A-Za-z0-9-._]{1,31}\$";
}else {
    $regexp_login = "^[A-Za-z0-9][A-Za-z0-9-_]{1,31}\$";
}

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
$automateAliamin = $racineAliamin . "auto/";
#
$automateMailChangeAlias = $automateAliamin . "mailChangeAlias.pl";
$automateMailChangeSieve = $automateAliamin . "mailChangeSieve.pl";
$automateCyrusAdmin = $automateAliamin . "mailCyrusAdmin.pl";
$automateLdapUpdate = $automateAliamin . "ldapModifBase.pl";
$automateLdapUpdatePasswd = $automateAliamin . "ldapChangePasswd.pl";
#
# Securinet
$automateStateSSHScript = $automateAliamin . "securinet/sshState.pl";
$automateBackup = $automateAliamin . "securinet/backupSecurinet.pl";
#
# Samba
# Calcul du mot de passe NT ou LM
$sambaNTPass = $automateAliamin . "mkntlmpwd -N";
$sambaLMPass = $automateAliamin . "mkntlmpwd -L";

#
# ACL : Definition des entites et des consomateurs
$MAILBOXENTITY="mailbox";
$MAILSHAREENTITY="mailshare";
$USERCONSUMER="user";
