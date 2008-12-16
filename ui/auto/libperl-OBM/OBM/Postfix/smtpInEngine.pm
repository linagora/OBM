package OBM::Postfix::smtpInEngine;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);


sub new {
    my $class = shift;
    my( $serverDesc ) = @_;

    my $self = bless { }, $class;

    # Count entities for which correct update need postfix maps regeneration
    $self->{'entitiesUpdate'} = 0;

    # Save entities which need postfix maps update, but are in error state and
    # cancel postfix maps regeneration
    $self->{'entitiesUpdateErrorDesc'} = ();

    # Which domains SMTP-in to update
    $self->{'smtpInDomainId'} = {};

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub update {
    my $self = shift;
    my( $entity ) = @_;

    if( !defined($entity) ) {
        $self->_log( 'entité non définie', 0 );
        return 1;
    }elsif( !ref($entity) ) {
        $self->_log( 'entité incorrecte', 0 );
        return 1;
    }

    # Is entity need need postfix maps regeneration ?
    SWITCH: {
        if( ref($entity) eq 'OBM::Entities::obmUser' ) {
            last SWITCH;
        }

        if( ref($entity) eq 'OBM::Entities::obmMailshare' ) {
            last SWITCH;
        }

        if( ref($entity) eq 'OBM::Entities::obmGroup' ) {
            last SWITCH;
        }

        $self->_log( 'l\'entité '.$entity->getDescription().' n\' pas d\'impact sur le contenu des maps postfix', 3 );
        return 0;
    }
 
    # If entity is not updated (but only links)
    if( !$entity->getUpdateEntity() ) {
        $self->_log( 'entité '.$entity->getDescription().' mise à jour mais pas d\'impact sur les maps postfix', 1 );
        return 0;
    }

    # If entity update error
    if( !$entity->getUpdated() ) {
        $self->_log( 'entité '.$entity->getDescription().' en erreur de mise à jour', 1 );
        push( @{$self->{'entitiesUpdateErrorDesc'}}, $entity->getDescription() );
        return 0;
    }

    $self->{'entitiesUpdate'}++;
    $self->{'smtpInDomainId'}->{$entity->getDomainId()} = '';

    return 0;
}


sub updateMaps {
    my $self = shift;

    if( $#{$self->{'entitiesUpdateErrorDesc'}} >= 0 ) {
        $self->_log( 'au moins une entité nécessitant la régénération des maps SMTP-in est en erreur de traitement', 0 );
        $self->_log( 'génération des maps SMTP-in annulée', 0 );

        for( my $i=0; $i<=$#{$self->{'entitiesUpdateErrorDesc'}}; $i++ ) {
            $self->_log( $self->{'entitiesUpdateErrorDesc'}->[$i], 4 );
        }

        return 1;
    }

    if( !$self->{'entitiesUpdate'} ) {
        $self->_log( 'pas d\'entité mise à jour, nécessitant la régénération des maps SMTP-in', 2);
        return 0;
    }

    if( $self->_updateSmtpInMaps() ) {
        $self->_log( 'problème à la mise à jour des maps SMTP-in', 0 );
        return 2;
    }

    return 0;
}


sub _updateSmtpInMaps {
    my $self = shift;

    my @smtpInDomainId = keys(%{$self->{'smtpInDomainId'}});
    if( $#smtpInDomainId < 0 ) {
        $self->_log( 'pas d\'ID de domaine à mettre à jour. Pas de régénération des maps SMTP-in', 3 );
        return 0;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    $self->_log( 'obtention des serveurs SMTP-in à mettre à jour', 1 );
    my $query = 'SELECT host_id,
                        host_name,
                        host_ip,
                        host_fqdn
                 FROM ServiceProperty
                 INNER JOIN DomainEntity ON serviceproperty_entity_id=domainentity_entity_id AND serviceproperty_property=\'smtp_in\'
                 INNER JOIN Host ON host_id='.$dbHandler->castAsInteger('serviceproperty_value').' 
                 WHERE domainentity_domain_id IN ('.join( ', ', @smtpInDomainId ).')';

    my $sth;
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'obtention du serveur IMAP impossible', 1 );
        return 1;
    }

    while( my $srvDesc = $sth->fetchrow_hashref() ) {
        require OBM::Postfix::smtpInServer;
        my $srv = OBM::Postfix::smtpInServer->new( $srvDesc );

        if( !defined($srv) ) {
            $self->_log( 'problème d\'initialisation d\'un serveur SMTP-in', 0 );
            $sth->finish();
            return 1;
        }

        if( $srv->update() ) {
            $self->_log( 'problème à la mise à jour d\'un serveur SMTP-in', 0 );
            $sth->finish();
            return 1;
        }
    }

    return 0;
}
