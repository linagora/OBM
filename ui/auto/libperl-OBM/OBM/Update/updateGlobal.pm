package OBM::Update::updateGlobal;


$VERSION = '1.0';

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
    }elsif( !exists($parameters->{'user'}) && !exists($parameters->{'domain'}) && !exists($parameters->{'delegation' }) ) {
        $self->_log( 'Usage: PARAMLIST: table de hachage avec les cles \'user\', \'domain\' et \'delegation\'', 4 );
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connecteur a la base de donnee invalide', 3 );
        return undef;
    }

    # Initialisation de l'objet
    $self->{'global'} = $parameters->{'global'};

    # Identifiant utilisateur
    if( defined($parameters->{'user'}) ) {
        $self->{'user'} = $parameters->{'user'};

        my $query = 'SELECT userobm_login FROM UserObm WHERE userobm_id='.$self->{'user'};
        my $queryResult;
        if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
            $self->_log( 'l\'utilisateur d\'identifiant \''.$self->{'user'}.' n\'existe pas', 0 );
            return undef;
        }

	    ( $self->{'user_name'} ) = $queryResult->fetchrow_array();
	    $queryResult->finish();
    }

    # Identifiant de délégation
    if( defined($parameters->{'delegation'}) ) {
        $self->{'delegation'} = $parameters->{'delegation'};
    }

    # Identifiant de domaine
    if( defined($parameters->{'domain'}) ) {
        $self->{'domain'} = $parameters->{'domain'};
    }else {
        $self->_log( 'Le parametre domaine doit etre precise', 0 );
    }


    return $self;
}


sub DESTROY {
    my $self = shift;
}


sub update {
    my $self = shift;
    my $return = 0;

    require OBM::dbUpdater;
    $self->_log( 'initialisation du BD updater', 2 );
    if( !($self->{'dbUpdater'} = OBM::dbUpdater->new()) ) {
        $self->_log( 'echec de l\'initialisation du BD updater', 0 );
        return 1;
    }

    require OBM::entitiesFactory;
    $self->_log( 'initialisation de l\'entity factory', 2 );
    if( !($self->{'entitiesFactory'} = OBM::entitiesFactory->new( 'GLOBAL', $self->{'domain'} )) ) {
        $self->_log( 'echec de l\'initialisation de l\'entity factory', 0 );
        return 1;
    }

    require OBM::Ldap::ldapEngine;
    $self->_log( 'initialisation du moteur LDAP', 2 );
    $self->{'ldapEngine'} = OBM::Ldap::ldapEngine->new();
    if( !defined($self->{'ldapEngine'}) ) {
        $self->_log( 'erreur à l\'initialisation du moteur LDAP', 1 );
        return 1
    }elsif( !ref($self->{'ldapEngine'}) ) {
        $self->_log( 'moteur LDAP non démarré', 3 );
        $self->{'ldapEngine'} = undef;
    }

    require OBM::Cyrus::cyrusEngine;
    $self->_log( 'initialisation du moteur Cyrus', 2 );
    $self->{'cyrusEngine'} = OBM::Cyrus::cyrusEngine->new();
    if( !defined($self->{'cyrusEngine'}) ) {
        $self->_log( 'erreur à l\'initialisation du moteur Cyrus', 1 );
        return 1
    }elsif( !ref($self->{'cyrusEngine'}) ) {
        $self->_log( 'moteur Cyrus non démarré', 3 );
        $self->{'cyrusEngine'} = undef;
    }

    while( my $entity = $self->{'entitiesFactory'}->next() ) {
        my $error = 0;
        $self->_log( 'traitement de '.$entity->getDescription(), 1 );

        if( !$error && defined($self->{'ldapEngine'}) ) {
            if($self->{'ldapEngine'}->update($entity)) {
                $self->_log( 'problème lors du traitement LDAP de l\'entité '.$entity->getDescription(), 1 );
                $error = 1;
            }
        }

        if( !$error && defined($self->{'cyrusEngine'}) ) {
            if($self->{'cyrusEngine'}->update($entity)) {
                $self->_log( 'problème lors du traitement Cyrus de l\'entité '.$entity->getDescription(), 1 );
                $error = 1;
            }
        }

        if( !$error ) {
            $entity->setUpdated();
            if( $self->{'dbUpdater'}->update($entity) ) {
                $self->_log( 'problème à la mise à jour BD de l\'entité '.$entity->getDescription(), 1 );
            }else {
                $self->_log( 'entité '.$entity->getDescription().' mise à jour en BD', 1 );
            }
        }else {
            $self->_log( 'entité '.$entity->getDescription().' en erreur de mise à jour, mise à jour BD annulée', 1 );
        }
    }

    return 0;
}
