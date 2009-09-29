package OBM::Update::update;


$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);


sub new {
    return undef;
}


sub update {
    my $self = shift;

    return 1 if $self->_updatePreInit();

    require OBM::dbUpdater;
    $self->_log( 'initialisation du BD updater', 2 );
    if( !($self->{'dbUpdater'} = OBM::dbUpdater->new()) ) {
        $self->_log( 'echec de l\'initialisation du BD updater', 0 );
        return 1;
    }

    return 1 if $self->_updateInitFactory();

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

        $self->_updateEntityEndProcess($entity);

        if( $self->{'smtpInEngine'}->update($entity) ) {
            $self->_log( 'erreur fatale', 0 );
            return 1;
        }
    }

    my $smtpInReturnCode = $self->{'smtpInEngine'}->updateMaps();
    if( $smtpInReturnCode == 1 ) {
        $self->_log( 'annulation de la génération des maps SMTP-in', 3 );

    }elsif( $smtpInReturnCode == 2 ) {
        $self->_log( 'erreur lors de la mise à jour des maps SMTP-in', 0 );
        $self->_log( 'Il peut y avoir des incohérences dans le contenu des différents serveurs SMTP-in', 0 );
    }

    my $updateEnd = $self->_updatePostUpdate();

    return ($smtpInReturnCode || $updateEnd);
}


sub _updatePreInit {
    my $self = shift;

    return 0;
}


sub _updateInitFactory {
    my $self = shift;

    $self->_log( 'La factory d\'entité doit être initialisée', 0 );
    return 1;
}


sub _updateEntityEndProcess {
    my $self = shift;
    my( $entity ) = @_;

    return 0;
}


sub _updatePostUpdate {
    my $self = shift;

    return 0;
}
