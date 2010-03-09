package lib::common;

require Exporter;
use Config::IniFiles;
use FindBin qw($Bin);

@ISA = qw(Exporter);
@EXPORT_vars = ($modulePath, $userDb, $userPasswd, $dbName, $dbHost, $dbType);
@EXPORT = ( @EXPORT_vars );
@EXPORT_OK = qw();

# Necessaire pour le bon fonctionnement du package
$debug=1;


# Define specific include path
use File::Basename;

$modulePath = dirname($0);
if( $modulePath !~ /^([\/\.-_a-zA-Z0-9]+)$/ ) {
    print STDERR "unable to find needed perl modules !\n";
    exit 10;
}
$modulePath = $1;
push( @INC, $modulePath );


# Read ini file
my $obmConfIni = '/etc/obm/obm_conf.ini';
if( ! -r $obmConfIni ) {
    $obmConfIni = $Bin.'/../conf/obm_conf.ini';
    if( ! -r $obmConfIni ) {
        print STDERR "Le fichier de configuration 'obm_conf.ini' n'existe pas ou n'est pas lisible\n";
        exit 1;
    }
}
$cfgFile = Config::IniFiles->new( -file => $obmConfIni );

# Define variables
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
