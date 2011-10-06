#!/usr/bin/perl
#Autor: Sylvain Garcia sylvain.garcia@obm.org
#This script generate /etc/obm/obm_conf.ini
#it is inspired by getmapchoice.pl in console-common package.

use Debconf::Client::ConfModule ':all';
use Debconf::Log;
use Config::IniFiles;
use IO::Zlib;

sub my_warn { 
# Don't dump warnings into STDERR, as it will choke the return
# of results from debconf to install-keymap. Instead put the 
# warnings into debconf's debugging channel.
  Debconf::Log::debug developer => " genobmconf warning: ".join(" ",@_);
}

sub my_die {
# Don't die horribly, as install-keymap will then crash.
# Instead, dump the warning into debconf's logs,
# and return NONE, which hopefully will Do The Right Thing.
  my_warn @_;
  print STDERR "NONE";
  exit 0;
}

#Create New File, then use ucf to copy in /etc/obm/obm_conf.ini
my $TEMPDIR="/var/run/obm/upgrades";
my $NEWFILE="$TEMPDIR/obm_conf.ini";

open(FILE_OBM_CONF, ">$NEWFILE");

$fh = new IO::Zlib;
if ($fh->open("/usr/share/doc/obm-conf/obm_conf.ini.sample.gz", "rb")) {
  print FILE_OBM_CONF <$fh>;
  $fh->close;
}else {
  open(SAMPLE_OBM_CONF, "</usr/share/doc/obm-conf/obm_conf.ini.sample");
  print FILE_OBM_CONF  <SAMPLE_OBM_CONF>;
  close(SAMPLE_OBM_CONF);
}
close(FILE_OBM_CONF);


#Modify INI file
$obm_cfg = new Config::IniFiles -file => "$NEWFILE";

($ret, $policy) = get ('obm-conf/ldapserver');
$obm_cfg->setval('automate', 'ldapServer', $policy);

($ret, $policy) = get ('obm-conf/dbtype');
$obm_cfg->setval('global', 'dbtype', $policy);

($ret, $policy) = get ('obm-conf/dbhost');
$obm_cfg->setval('global', 'host', $policy);

($ret, $policy) = get ('obm-conf/dbname');
$obm_cfg->setval('global', 'db', $policy);

($ret, $policy) = get ('obm-conf/dbuser');
$obm_cfg->setval('global', 'user', $policy);

($ret, $policy) = get ('obm-conf/dbpasswd');
$obm_cfg->setval('global', 'password', '"'.$policy.'"');

($ret, $policy) = get ('obm-conf/externalurl');
$obm_cfg->setval('global', 'external-url', $policy);

($ret, $policy) = get ('obm-conf/externalprotocol');
$obm_cfg->setval('global', 'external-protocol', $policy);

($ret, $policy) = get ('obm-conf/module_obmldap');
$obm_cfg->setval('global', 'obm-ldap', $policy);

($ret, $policy) = get ('obm-conf/module_obmmail');
$obm_cfg->setval('global', 'obm-mail', $policy);

($ret, $policy) = get ('obm-conf/module_obmsamba');
$obm_cfg->setval('global', 'obm-samba', $policy);

($ret, $policy) = get ('obm-conf/module_obmweb');
$obm_cfg->setval('global', 'obm-web', $policy);

($ret, $policy) = get ('obm-conf/module_obmcontact');
$obm_cfg->setval('global', 'obm-contact', $policy);


$obm_cfg->RewriteConfig;

#Now update configuration file with ucf in postisnt
