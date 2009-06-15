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


sub update {
    my $self = shift;

    require OBM::dbUpdater;
    $self->_log( 'initialisation du BD updater', 2 );
    if( !($self->{'dbUpdater'} = OBM::dbUpdater->new()) ) {
        $self->_log( 'echec de l\'initialisation du BD updater', 0 );
        return 1;
    }

    require OBM::entitiesFactory;
    $self->_log( 'initialisation de l\'entity factory', 2 );
    if( !($self->{'entitiesFactory'} = OBM::entitiesFactory->new( 'GLOBAL', $self->{'domainId'} )) ) {
        $self->_log( 'echec de l\'initialisation de l\'entity factory', 0 );
        return 1;
    }

    require OBM::Postfix::smtpInEngine;
    $self->_log( 'initialisation du SMTP-in maps updater', 2 );
    if( !($self->{'smtpInEngine'} = OBM::Postfix::smtpInEngine->new()) ) {
        $self->_log( 'echec de l\'initialisation du SMTP-in maps updater', 0 );
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

    require OBM::Cyrus::sieveEngine;
    $self->_log( 'initialisation du moteur Sieve', 2 );
    $self->{'sieveEngine'} = OBM::Cyrus::sieveEngine->new();
    if( !defined($self->{'sieveEngine'}) ) {
        $self->_log( 'erreur à l\'initialisation du moteur Sieve', 1 );
        return 1
    }elsif( !ref($self->{'sieveEngine'}) ) {
        $self->_log( 'moteur Sieve non démarré', 3 );
        $self->{'sieveEngine'} = undef;
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

        if( !$error && defined($self->{'sieveEngine'}) ) {
            if($self->{'sieveEngine'}->update($entity)) {
                $self->_log( 'problème lors du traitement Sieve de l\'entité '.$entity->getDescription(), 1 );
                $error = 1;
            }
        }

        if( !$error ) {
            $entity->setUpdated();
        }else {
            if( ref($entity) eq 'OBM::Entities::obmDomain' ) {
                $self->_log( 'problème à la mise à jour de '.$entity->getDescription(), 0 );
                $self->_log( 'erreur fatale', 0 );
                return 1;
            }
        }

        if( $self->{'dbUpdater'}->update($entity) ) {
            $self->_log( 'problème à la mise à jour BD de l\'entité '.$entity->getDescription(), 1 );
            $entity->unsetUpdated();
        }else {
            $self->_log( 'entité '.$entity->getDescription().' mise à jour en BD', 1 );
        }

        if( $self->{'smtpInEngine'}->update($entity) ) {
            $self->_log( 'erreur fatale', 0 );
            return 1;
        }
    }

    my $returnCode = $self->{'smtpInEngine'}->updateMaps();
    if( $returnCode == 1 ) {
        $self->_log( 'annulation de la génération des maps SMTP-in', 3 );
        return 1;

    }elsif( $returnCode == 2 ) {
        $self->_log( 'erreur lors de la mise à jour des maps SMTP-in', 0 );
        $self->_log( 'Il peut y avoir des incohérences dans le contenu des maps des différents serveur SMTP-in', 0 );
        return 1;
    }

    require OBM::incrementalTableUpdater;
    $self->_log( 'purge des tables du mode incrémental', 2 );
    my $incrementalTableUpdater;
    if( !($incrementalTableUpdater = OBM::incrementalTableUpdater->new( $self->{'domainId'}, undef, undef )) || $incrementalTableUpdater->purgeBd() ) {
        $self->_log( 'echec du purge des tables incrémentales non effectuée', 0 );
        return 1;
    }


    return 0;
}
