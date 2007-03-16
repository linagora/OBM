package OBM::Parameters::cyrusConf;

require Exporter;

@ISA = qw(Exporter);
@EXPORT_const = qw();
@EXPORT_struct = qw();
@EXPORT = (@EXPORT_const, @EXPORT_struct);
@EXPORT_OK = qw();

#
# Necessaire pour le bon fonctionnement du package
$debug=1;


#
# Le login de l'administrateur IMAP
use constant cyrusAdminLogin => "cyrus";


use constant definedRight => {
    none => "none",
    read => "lrs",
    writeonly => "li",
    write => "lrswicd",
    admin => "dc",
    post => "p"
};

# Les clés des tables de hachage sont les identifiants utilisateurs, les valeurs
# associés sont 1, si cet utilisateur a ce droit.
use constant boxRight => {
    read => {},
    writeonly => {},
    write => {}
};


use constant cyrusAdmin => {
    login => cyrusAdminLogin,
    passwd => undef,
};


use constant srvDesc => {
    imap_server_id => undef,
    imap_server_name => undef,
    imap_server_ip => undef,
    imap_server_login => undef,
    imap_server_passwd => undef,
    imap_server_conn => undef,
    imap_sieve_server_conn => undef,
    BD_BAL => {},
    SRV_BAL => {},
    BD_SHARE => {},
    SRV_SHARE => {}
};


# Attribut 'box_acl' : type 'boxRight'
use constant imapBox => {
    box_name => undef,
    box_login => undef,
    box_quota => 0,
    box_srv_id => undef,
    box_acl => {},
    box_vacation_enable => 0,
    box_vacation_message => undef,
    box_email => [],
    box_nomade_perms => 0,
    box_nomade_enable => undef,
    box_nomade_local_copy => undef,
    box_nomade_dst => undef
};

# Tableau de type 'imapBox'
# Les clés sont les logins, les valeurs correspondantes sont de type 'imapBox'
use constant listImapBox => {};


$boxTypeDef = {
    BAL => {
        prefix => "user",
        separator => "/",
        get_db_values => sub {
            my( $dbHandler, $domain, $obmSrvId, $obmUserLogin ) = @_;
            require OBM::Cyrus::typeBal;
            return OBM::Cyrus::typeBal::getDbValues( $dbHandler, $domain, $obmSrvId, $obmUserLogin );
        },
        update_sieve => sub {
            my( $srvDesc, $imapBox ) = @_;
            require OBM::Cyrus::typeBal;
            return OBM::Cyrus::typeBal::updateSieve( $srvDesc, $imapBox );
        }
    },
    SHARE => {
        prefix => "",
        separator => "",
        get_db_values => sub {
            my( $dbHandler, $domain, $obmSrvId, $obmMaishareName ) = @_;
            require OBM::Cyrus::typeShare;
            return OBM::Cyrus::typeShare::getDbValues( $dbHandler, $domain, $obmSrvId, $obmMaishareName );
        },
    }
};

