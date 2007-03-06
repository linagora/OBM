package OBM::Parameters::toolBoxConf;

require Exporter;

@ISA = qw(Exporter);
@EXPORT_const = qw();
@EXPORT_struct = qw();
@EXPORT = (@EXPORT_const, @EXPORT_struct);
@EXPORT_OK = qw();

#
# Necessaire pour le bon fonctionnement du package
$debug=1;


# Attribut 'domain_alias' : tableau contenant un alias par case
use constant domainDesc => {
    meta_domain => 0,
    domain_id => undef,
    domain_label => undef,
    domain_name => undef,
    domain_alias => [],
    domain_desc => undef,
    domain_dn => undef,
    ldap_admin_server => undef,
    ldap_admin_login => undef,
    ldap_admin_passwd => undef
};


# Tableau de '$domainDesc'
use constant domainList => [];
