package lib::common;

require Exporter;
use Config::IniFiles;
use FindBin qw($Bin);

@ISA = qw(Exporter);
@EXPORT_vars = (    $modulePath,
                    $iniFileName,
                    $userDb,
                    $userPasswd,
                    $dbName,
                    $dbHost,
                    $dbType);
@EXPORT_function = (getIniParms);
@EXPORT = ( @EXPORT_vars, @EXPORT_function );
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

$iniFileName = $modulePath.'/unitTests.ini';

# Read ini file
my $obmConfIni = '/etc/obm/obm_conf.ini';
if( ! -r $obmConfIni ) {
    print STDERR "Le fichier de configuration 'obm_conf.ini' n'existe pas ou n'est pas lisible\n";
    exit 1;
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
$dbType = lc($cfgFile->val( 'global', 'dbtype' ));


sub getIniParms {
    my( $sectionName, $iniParms ) = @_;

    if( !$sectionName ) {
        print STDERR "Invalid Ini section name\n";
        exit 1;
    }

    if( ! -e $iniFileName ) {
        print STDERR "File ".$iniFileName." doesn\'t exist !\n";
        exit 1;
    }

    if( ! -r $iniFileName ) {
        print STDERR "File ".$iniFileName." isn't readable !\n";
    }

    my $testCfgFile = Config::IniFiles->new( -file => $iniFileName );
    if( !$testCfgFile->SectionExists( $sectionName ) ) {
        print STDERR "Section ".$sectionName." doesn\'t exist in ".$iniFileName;
        exit 1;
    }

    my @params = $testCfgFile->Parameters($sectionName);
    for( my $i=0; $i<=$#params; $i++ ) {
        $iniParms->{$params[$i]} = $testCfgFile->val($sectionName, $params[$i]);
        if( $iniParms->{$params[$i]} eq lc('true') ) {
            $iniParms->{$params[$i]} = 1;
        }elsif( $iniParms->{$params[$i]} eq lc('false') ) {
            $iniParms->{$params[$i]} = 0;
        }
    }

    return 0;
}
