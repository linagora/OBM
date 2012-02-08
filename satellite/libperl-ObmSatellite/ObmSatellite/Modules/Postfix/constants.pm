package ObmSatellite::Modules::Postfix::constants;

use base qw/Exporter/;

use 5.006_001;
use strict;
use warnings;

our @EXPORT_OK = qw/POSTMAP_CMD MAILBOX_MAP ALIAS_MAP TRANSPORT_MAP DOMAIN_MAP
                    LDAP_MAILBOX_QUERY LDAP_ALIAS_QUERY LDAP_TRANSPORT_QUERY
                    LDAP_DOMAIN_QUERY/;

use constant POSTMAP_CMD    => '/usr/sbin/postmap';
use constant MAILBOX_MAP    => '/etc/postfix/virtual_mailbox';
use constant ALIAS_MAP      => '/etc/postfix/virtual_alias';
use constant TRANSPORT_MAP  => '/etc/postfix/transport';
use constant DOMAIN_MAP     => '/etc/postfix/virtual_domains';

use constant LDAP_MAILBOX_QUERY     => {
    ldapAttributePostfixKey         => [ 'mailbox' ],
    ldapAttributePostfixValue       => 'OK',
    ldapFilter                      => '(&(|(objectclass=obmuser)(objectclass=obmmailshare))(mailAccess=PERMIT)%d)'
};

use constant LDAP_ALIAS_QUERY       => {
    ldapAttributePostfixKey         => [ 'mail', 'mailAlias' ],
    ldapAttributePostfixValue       => [ 'mailbox', 'externalContactEmail' ],
    ldapFilter                      => '(&(mailAccess=PERMIT)%d)'
};

use constant LDAP_TRANSPORT_QUERY   => {
    ldapAttributePostfixKey         => [ 'mailbox' ],
    ldapAttributePostfixValue       => [ 'mailBoxServer' ],
    ldapFilter                      => '(&(|(objectClass=obmUser)(objectClass=obmMailShare))(mailAccess=PERMIT)%d)'
};

use constant LDAP_DOMAIN_QUERY      => {
    ldapAttributePostfixKey         => [ 'myDestination' ],
    ldapAttributePostfixValue       => 'OK',
    ldapFilter                     => '(&(objectClass=obmMailServer)%d)'
};

1;
