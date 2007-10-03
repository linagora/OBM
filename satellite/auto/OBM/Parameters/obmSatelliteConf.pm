package OBM::Parameters::obmSatelliteConf;

require Exporter;

@ISA = qw(Exporter);
@EXPORT_const = qw();
@EXPORT_struct = qw();
@EXPORT = (@EXPORT_const, @EXPORT_struct);
@EXPORT_OK = qw();

#
# Necessaire pour le bon fonctionnement du package
$debug=1;

$postfixMapsDesc = {
    mailbox => {
        postfix_map => "/etc/postfix/virtual_mailbox",
        postfix_map_type => "hash",
        postfix_map_separator => "\t",
        postfix_map_process => 1,
        postfix_map_postmap => 1,
        ldap_filter => "(&(|(objectclass=obmuser)(objectclass=obmmailshare))(mailAccess=PERMIT)(obmdomain=<obmDomain>))",
        ldap_attibute => [ "mailbox" ],
        make_map => sub {
            my( $daemonRef, $mailBoxMapFile, $obmDomains ) = @_;
            require OBM::ObmSatellite::mapMailbox;
            return &OBM::ObmSatellite::mapMailbox::makeMailboxMap( $daemonRef, $mailBoxMapFile, $obmDomains );
        }
    },
    alias => {
        postfix_map => "/etc/postfix/virtual_alias",
        postfix_map_type => "hash",
        postfix_map_separator => "\t",
        postfix_map_process => 1,
        postfix_map_postmap => 1,
        ldap_filter => "(&(mailAccess=PERMIT)(obmdomain=<obmDomain>))",
        ldap_attibute => [ "mailbox", "mail", "mailAlias" ],
        make_map => sub {
            my( $daemonRef, $aliasMapFile, $obmDomains ) = @_;
            require OBM::ObmSatellite::mapAlias;
            return &OBM::ObmSatellite::mapAlias::makeAliasMap( $daemonRef, $aliasMapFile, $obmDomains );
        }
    },
    transport => {
        postfix_map => "/etc/postfix/transport",
        postfix_map_type => "hash",
        postfix_map_separator => "\t",
        postfix_map_process => 1,
        postfix_map_postmap => 1,
        ldap_filter => "(&(|(objectClass=obmUser)(objectClass=obmMailShare))(mailAccess=PERMIT)(obmDomain=<obmDomain>))",
        ldap_attibute => [ "mailbox", "mailBoxServer" ],
        make_map => sub {
            my( $daemonRef, $transportMapFile, $obmDomains ) = @_;
            require OBM::ObmSatellite::mapTransport;
            return &OBM::ObmSatellite::mapTransport::makeTransportMap( $daemonRef, $transportMapFile, $obmDomains );
        }
    },
    domain => {
        postfix_map => "/etc/postfix/virtual_domains",
        postfix_map_type => "hash",
        postfix_map_separator => "\t",
        postfix_map_process => 1,
        postfix_map_postmap => 1,
        ldap_filter => "(&(objectClass=obmMailServer)(obmDomain=<obmDomain>))",
        ldap_attibute => [ "myDestination" ],
        make_map => sub {
            my( $daemonRef, $domainMapFile, $obmDomains ) = @_;
            require OBM::ObmSatellite::mapDomains;
            return &OBM::ObmSatellite::mapDomains::makeDomainsMap( $daemonRef, $domainMapFile, $obmDomains );
        }
    },
    networks => {
        postfix_map => "/etc/postfix/networks"
    }
};

