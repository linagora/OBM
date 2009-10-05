package OBM::Update::updateGlobal;

$VERSION = '1.0';

use OBM::Update::update;
@ISA = ('OBM::Update::update');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


use OBM::Tools::commonMethods qw(_log dump);
use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $parameters ) = @_;

    my $self = bless { }, $class;


    if( !defined($parameters) ) {
        $self->_log( 'Usage: PACKAGE->new(PARAMLIST)', 4 );
        return undef;
    }elsif( !exists($parameters->{'user'}) && !exists($parameters->{'domain-id'}) && !exists($parameters->{'delegation' }) ) {
        $self->_log( 'Usage: PARAMLIST: table de hachage avec la clé \'domain\' et optionnellement les cles \'user\' ou \'delegation\'', 4 );
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connecteur a la base de donnee invalide', 3 );
        return undef;
    }

    # Updater initialization
    $self->{'global'} = $parameters->{'global'};

    # Domain identifier
    if( defined($parameters->{'domain-id'}) ) {
        $self->{'domainId'} = $parameters->{'domain-id'};
    }else {
        $self->_log( 'Le parametre domain-id doit etre precise', 0 );
        return undef;
    }

    # User identifier
    if( defined($parameters->{'user'}) ) {
        $self->{'user'} = $parameters->{'user'};

        my $query = 'SELECT userobm_login FROM UserObm WHERE userobm_id='.$self->{'user'};
        my $queryResult;
        if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
            $self->_log( 'l\'utilisateur d\'identifiant \''.$self->{'user'}.' n\'existe pas', 0 );
            return undef;
        }

        ( $self->{'user_login'} ) = $queryResult->fetchrow_array();
        $queryResult->finish();
    }

    # Delegation
    if( defined($parameters->{'delegation'}) ) {
        $self->{'delegation'} = $parameters->{'delegation'};
    }


    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub _updateInitFactory {
    my $self = shift;

    require OBM::entitiesFactory;
    $self->_log( 'initialisation de l\'entity factory', 2 );
    if( !($self->{'entitiesFactory'} = OBM::entitiesFactory->new( 'GLOBAL', $self->{'domainId'} )) ) {
        $self->_log( 'echec de l\'initialisation de l\'entity factory', 0 );
        return 1;
    }

    return 0;
}


sub _updatePostUpdate {
    my $self = shift;

    require OBM::incrementalTableUpdater;
    $self->_log( 'purge des tables du mode incrémental', 2 );
    my $incrementalTableUpdater;
    if( !($incrementalTableUpdater = OBM::incrementalTableUpdater->new( $self->{'domainId'}, undef, undef )) || $incrementalTableUpdater->purgeBd() ) {
        $self->_log( 'echec du purge des tables incrémentales non effectuée', 0 );
        return 1;
    }

    return 0;
}
