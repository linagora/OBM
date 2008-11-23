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
    none => 'none',
    read => 'lrs',
    readAdmin => 'lrsc',
    writeonly => 'li',
    writeonlyAdmin => 'lic',
    write => 'lrswid',
    writeAdmin => 'lrswidc',
    admin => 'lc',
    post => 'p'
};

use constant cyrusAdmin => {
    login => cyrusAdminLogin,
    passwd => undef,
};
