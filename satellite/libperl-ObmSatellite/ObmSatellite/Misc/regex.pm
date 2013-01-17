#########################################################################
# OBM       - file : ObmSatellite::Misc::regex (Perl Module)            #
#           - Desc : common regexp used on OBM                          #
#########################################################################
package ObmSatellite::Misc::regex;

require Exporter;


@ISA = qw(Exporter);
@EXPORT_regex = qw(
    $REGEX_ID
    $REGEX_DOMAIN
    $REGEX_EMAIL
    $REGEX_EMAIL_LEFT
    $REGEX_EMAIL_RIGHT
    $REGEX_ROOTLDAP
    $REGEX_LOGIN
    $REGEX_REALM
    $REGEX_PASSWD
    $REGEX_IP
    $REGEX_SERVER_ID
    $REGEX_UID
    $REGEX_HOSTNAME
    $REGEX_GROUPNAME
    $REGEX_MAILSHARE_NAME
    );
@EXPORT = (@EXPORT_regex);


# Regexp generic
$REGEX_ID = '^[0-9]+$';

# Domain regexp
$REGEX_DOMAIN = '^([a-z0-9-]+\.)+[a-z]{2,6}$';

# Email
$REGEX_EMAIL = '^[a-z0-9!#$%&\'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@([a-z0-9-]+\.)+[a-z]{2,6}$';
$REGEX_EMAIL_LEFT = '^[a-z0-9!#$%&\'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&\'*+/=?^_`{|}~-]+)*$';
$REGEX_EMAIL_RIGHT = $REGEX_DOMAIN;

# LDAP root
$REGEX_ROOTLDAP = "^dc=(.+),dc=.+\$";

# Login regexp
$REGEX_LOGIN = '^([a-zA-Z0-9][\w\.-]{0,63})$';

# Realm regexp
$REGEX_REALM = $REGEX_DOMAIN;

# Passwd regexp
$REGEX_PASSWD = '^[-\$\\\&~#\{\(\[\|_`\^@\);\]+=\}%!:\/\.,?<>"\w0-9]{4,12}$';

# Les adresses IP
$REGEX_IP = '^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$';

# Server regexp
$REGEX_SERVER_ID = '^[0-9]+$';

# User regexp
$REGEX_UID = '^[0-9]+$';

# Host
$REGEX_HOSTNAME = '^[A-Za-z0-9][A-Za-z0-9-]{0,30}[A-Za-z0-9]$';

# Group
$REGEX_GROUPNAME = '^[\W\w0-9]([\W\w0-9-._ ]{0,252}[\W\w0-9]){0,1}$';

# Mailshare
$REGEX_MAILSHARE_NAME = $REGEX_LOGIN;
