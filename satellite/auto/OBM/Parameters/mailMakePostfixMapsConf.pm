package OBM::Parameters::mailMakePostfixMapsConf;

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
        postfix_map_generate => 1,
        ldap_filter => "(&(|(objectclass=obmuser)(objectclass=obmmailshare))(obmdomain=<obmDomain>))",
        ldap_attibute => [ "mailbox" ],
        make_map => sub {
            my( $daemonRef, $mailBoxMapFile, $obmDomains ) = @_;
            require OBM::MakePostfixMaps::mapMailbox;
            return &OBM::MakePostfixMaps::mapMailbox::makeMailboxMap( $daemonRef, $mailBoxMapFile, $obmDomains );
        }
    },
    alias => {
        postfix_map => "/etc/postfix/virtual_alias",
        postfix_map_type => "hash",
        postfix_map_separator => "\t",
        postfix_map_generate => 1,
        ldap_filter => "(&(mailAccess=PERMIT)(obmdomain=<obmDomain>))",
        ldap_attibute => [ "mailbox", "mail", "mailAlias" ],
        make_map => sub {
            my( $daemonRef, $mailBoxMapFile, $obmDomains ) = @_;
            require OBM::MakePostfixMaps::mapAlias;
            return &OBM::MakePostfixMaps::mapAlias::makeAliasMap( $daemonRef, $mailBoxMapFile, $obmDomains );
        }
    },
    transport => {
        postfix_map => "/etc/postfix/transport",
        postfix_map_type => "hash",
        postfix_map_separator => "\t",
        postfix_map_generate => 1,
        ldap_filter => "(&(|(objectclass=obmUser)(objectclass=obmMailShare))(mailAccess=PERMIT)(obmdomain=<obmDomain>))",
        ldap_attibute => [ "mailbox", "mailBoxServer" ],
        make_map => sub {
            my( $daemonRef, $mailBoxMapFile, $obmDomains ) = @_;
            require OBM::MakePostfixMaps::mapTransport;
            return &OBM::MakePostfixMaps::mapTransport::makeTransportMap( $daemonRef, $mailBoxMapFile, $obmDomains );
        }
    },
    domain => {
        postfix_map => "/etc/postfix/virtual_domains"
    },
    networks => {
        postfix_map => "/etc/postfix/networks"
    }
};

