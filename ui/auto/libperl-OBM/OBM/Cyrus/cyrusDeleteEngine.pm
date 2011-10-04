package OBM::Cyrus::cyrusDeleteEngine;

$VERSION = '1.0';

use OBM::Cyrus::cyrusEngine;
@ISA = ('OBM::Cyrus::cyrusEngine');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub update {
    my $self = shift;
    my( $entity ) = @_;

    if( ref($entity) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'entité de type \'OBM::Entities::obmDomain\' non prise en charge', 3 );
        return 1;
    }
    $self->{'currentEntity'} = $entity;


    $self->_log( 'obtention des serveurs IMAP du domaine '.$entity->getDescription(), 2 );
    if( $self->_getDomainMailServer() ) {
        return 1;
    }

    # Starting Cyrus remote engine to update Cyrus partition
    require OBM::Cyrus::cyrusRemoteEngine;
    my $cyrusRemoteEngine = OBM::Cyrus::cyrusRemoteEngine->instance();

    $self->_log( 'suppression des BALs du domaine '.$entity->getDescription(), 2 );
    my $cyrusServerError = 0;
    for( my $i=0; $i<=$#{$self->{'imapServerId'}}; $i++ ) {
        $self->{'currentCyrusSrv'} = $self->{'cyrusServers'}->getCyrusServerById( $self->{'imapServerId'}->[$i], $entity->getDomainId() );
        $self->_log( 'traitement de '.$self->{'currentCyrusSrv'}->getDescription(), 2 );

        $self->{'domainMailbox'} = $self->_listDomainMailboxes();
        if( !defined($self->{'domainMailbox'}) ) {
            $self->_log( 'erreur lors de l\'obtention des BALs du domaine \''.$entity->getDesc('domain_name').'\' de '.$self->{'currentCyrusSrv'}->getDescription(), 0 );
            $cyrusServerError++;
            next;
        }

        if( my $errors = $self->_deleteDomainMailboxes() ) {
            $self->_log( $errors.' BALs n\'ont pas pu être supprimées', 0 );
            $cyrusServerError++;
            next;
        }

        if( $cyrusRemoteEngine->delCyrusPartition($self->{'currentCyrusSrv'}) ) {
            $self->_log( 'erreur à la mise à jour des partition de '.$self->{'currentCyrusSrv'}->getDescription(), 0 );
            $cyrusServerError++;
            next;
        }
    }

    return $cyrusServerError;
}


sub _getDomainMailServer {
    my $self = shift;


    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connecteur a la base de donnee invalide', 3 );
        return 1;
    }

    my $query = 'SELECT ServiceProperty.serviceproperty_value
                    FROM ServiceProperty
                    INNER JOIN DomainEntity ON
                    DomainEntity.domainentity_domain_id='.$self->{'currentEntity'}->getDomainId().'
                    WHERE ServiceProperty.serviceproperty_property=\'imap\'';

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'erreur lors de l\'exécution de la requête de récupération des serveurs IMAP du domaine', 0 );
        return 1;
    }

    while( my( $imapServerId ) = $queryResult->fetchrow_array() ) {
        push( @{$self->{'imapServerId'}}, $imapServerId );
    }

    return 0;
}


sub _deleteDomainMailboxes {
    my $self = shift;
    my $entity = $self->{'currentEntity'};

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getConn($entity->getDomainId());
    if( !defined($cyrusSrv) ) {
        return 1;
    }

    my $domainMailboxes = $self->{'domainMailbox'};
    my $imapRight = $self->{'rightDefinition'}->{'admin'};

    my $mailboxDeleteError = 0;
    for( my $i=0; $i<=$#$domainMailboxes; $i++ ) {
        $cyrusSrv->setaclmailbox( $domainMailboxes->[$i], $OBM::Parameters::common::cyrusAdminLogin, $imapRight );
        if( $cyrusSrv->error() ) {
            $self->_log( 'erreur Cyrus au positionnement des ACLs de la BAL \''.$$domainMailboxes->[$i].'\' : '.$cyrusSrv->error(), 0 );
            $mailboxDeleteError++;
            next;
        }

        $cyrusSrv->delete( $domainMailboxes->[$i] );
        if( $cyrusSrv->error() ) {
            $self->_log( 'erreur Cyrus à la suppression de la BAL \''.$domainMailboxes->[$i].'\' : '.$cyrusSrv->error(), 0 );
            $mailboxDeleteError++;
            next;
        }

        $self->_log( 'suppression de la BAL \''.$domainMailboxes->[$i].'\' réussie', 2 );
    }

    return $mailboxDeleteError;
}


sub _listDomainMailboxes {
    my $self = shift;

    my $entity = $self->{'currentEntity'};
    my $domainName = $entity->getDesc('domain_name');

    my $cyrusSrv = $self->{'currentCyrusSrv'}->getConn($entity->getDomainId());
    if( !defined($cyrusSrv) ) {
        return undef;
    }

    my @domainBoxes;
    push( @domainBoxes, $cyrusSrv->listmailbox( '*@'.$domainName, '' ) );

    my $mailbox;
    while( ref( $mailbox = shift( @domainBoxes ) ) eq 'ARRAY' ) {
        if( $mailbox->[0] =~ /[^\@]/ ) {
            $mailbox->[0] .= '@'.$domainName;
        }

        if( $mailbox->[0] =~ /^((user\/[^\/]+)|[^user\/].+)\@$domainName$/ ) {
            push( @domainBoxes, $mailbox->[0] );
        }
    }

    unshift( @domainBoxes, $mailbox ) if $mailbox;

    return \@domainBoxes;
}
