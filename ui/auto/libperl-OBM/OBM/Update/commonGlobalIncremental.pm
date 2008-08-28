package OBM::Update::commonGlobalIncremental;


$debug = 1;


use 5.006_001;
use strict;
use vars qw( @EXPORT_OK $VERSION );
use base qw(Exporter);


require OBM::toolBox;
require OBM::Postfix::smtpInRemoteEngine;
require OBM::Postfix::smtpOutRemoteEngine;
require OBM::Update::utils;


$VERSION = "1.0";

@EXPORT_OK = qw(    _updateState
                    _doRemoteConf
                    _runEngines
                    _doUser
                    _doGroup
                    _doMailShare
                    _doHost
                    _doSystemUser
                    _doSambaDomain
                    _doMailServer
                    _deleteDbEntity
                    _tableNamePrefix
               );


sub _updateState {
    my $self = shift;

    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( '[Update::commonGlobalIncremental]: connecteur a la base de donnee invalide', 'W', 3 );
        return 0;
    }

    if( !defined($self->{"domain"}) || ($self->{"domain"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "[Update::commonGlobalIncremental]: pas de domaine indique pour la MAJ totale", "W" );
        return 0;
    }
    my $domainId = $self->{"domain"};
   
    my $query = "UPDATE DomainPropertyValue SET domainpropertyvalue_value=0 WHERE domainpropertyvalue_property_key='update_state' AND domainpropertyvalue_domain_id=".$domainId;
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    return 1;
}


sub _doRemoteConf {
    my $self = shift;
    my $return = 1;

    # MAJ des maps Postfix sur les serveurs entrant
    my $updateMailSrv = OBM::Postfix::smtpInRemoteEngine->new( $self->{"domainList"} );
    if( $updateMailSrv->init() ) {
        $return = $updateMailSrv->update();
    }

    # MAJ des maps Postfix sur les serveurs sortant
    $updateMailSrv = OBM::Postfix::smtpOutRemoteEngine->new( $self->{"domainList"} );
    if( $updateMailSrv->init() ) {
        $return = $updateMailSrv->update();
    }

    $updateMailSrv->destroy();

    return $return;
}


sub _runEngines {
    my $self = shift;
    my( $object ) = @_;
    my $return = 1;

    if( !defined($object) ) {
        return 0;
    }

    my $engines = $self->{"engine"};
    while( (my( $engineType, $engine ) = each(%{$engines})) && $return ) {
        my $engineReturn;
        if( defined( $engine ) ) {
            $engineReturn = $engine->update( $object );
        }

        if( !$engineReturn ) {
            $return = $engineReturn;
        }elsif( $engineReturn > $return ) {
            $return = $engineReturn;
        }
    }


    if( !$return ) {
        # On ré-initialise le compteur interne de la table de hachage
        keys(%{$engines});
    }

    return $return;
}


sub _doUser {
    my $self = shift;
    my( $links, $delete, $userId ) = @_;

    if( !defined($userId) || ($userId !~ /^\d+$/) ) {
        return 0;
    }

    if( !defined($links) ) {
        $links = 0;
    }

    if( !defined($delete) ) {
        $delete = 0;
    }

    my $userObject = OBM::Entities::obmUser->new( $links, $delete, $userId );
    my $return = $userObject->getEntity( &OBM::Update::utils::findDomainbyId( $self->{"domainList"}, $self->{"domain"} ) );
    if( !$return ) {
        return undef;
    }

    return $userObject;
}


sub _doGroup {
    my $self = shift;
    my( $links, $delete, $groupId ) = @_;

    if( !defined($groupId) || ($groupId !~ /^\d+$/) ) {
        return 0;
    }

    if( !defined($links) ) {
        $links = 0;
    }

    if( !defined($delete) ) {
        $delete = 0;
    }

    my $groupObject = OBM::Entities::obmGroup->new( $links, $delete, $groupId );
    my $return = $groupObject->getEntity( &OBM::Update::utils::findDomainbyId( $self->{"domainList"}, $self->{"domain"} ) );
    if( !$return ) {
        return undef;
    }

    return $groupObject;
}


sub _doMailShare {
    my $self =shift;
    my( $links, $delete, $mailshareId ) = @_;

    if( !defined($mailshareId) || ($mailshareId !~ /^\d+$/) ) {
        return 0;
    }

    if( !defined($links) ) {
        $links = 0;
    }

    if( !defined($delete) ) {
        $delete = 0;
    }

    my $mailShareObject = OBM::Entities::obmMailshare->new( $links, $delete, $mailshareId );
    my $return = $mailShareObject->getEntity( &OBM::Update::utils::findDomainbyId( $self->{"domainList"}, $self->{"domain"} ) );
    if( !$return ) {
        return undef;
    }

    return $mailShareObject;
}


sub _doHost {
    my $self = shift;
    my( $links, $delete, $hostId ) = @_;

    if( !defined($hostId) || ($hostId !~ /^\d+$/) ) {
        return 0;
    }

    if( !defined($links) ) {
        $links = 0;
    }

    if( !defined($delete) ) {
        $delete = 0;
    }

    my $hostObject = OBM::Entities::obmHost->new( $links, $delete, $hostId );
    my $return = $hostObject->getEntity( &OBM::Update::utils::findDomainbyId( $self->{"domainList"}, $self->{"domain"} ) );
    if( !$return ) {
        return undef;
    }

    return $hostObject;
}


sub _doSystemUser {
    my $self = shift;
    my( $links, $delete, $systemUserId ) = @_;

    if( !defined($systemUserId) || $systemUserId !~ /^\d+$/ ) {
        return 0;
    }

    if( !defined($links) ) {
        $links = 0;
    }

    my $systemUserObject = OBM::Entities::obmSystemUser->new( $links, $delete, $systemUserId );
    my $return = $systemUserObject->getEntity( &OBM::Update::utils::findDomainbyId( $self->{"domainList"}, $self->{"domain"} ) );
    if( !$return ) {
        return undef;
    }

    return $systemUserObject;
}


sub _doSambaDomain {
    my $self = shift;
    my( $links, $delete ) = @_;

    if( !defined($links) ) {
        $links = 0;
    }

    if( !defined($delete) ) {
        $delete = 0;
    }

    my $sambaDomainObject = OBM::Entities::obmSambaDomain->new( $links, $delete );
    my $return = $sambaDomainObject->getEntity( &OBM::Update::utils::findDomainbyId( $self->{"domainList"}, $self->{"domain"} ) );
    if( !$return ) {
        return undef;
    }

    return $sambaDomainObject;
}


sub _doMailServer {
    my $self = shift;
    my( $links, $delete ) = @_;

    if( !defined($links) ) {
        $links = 0;
    }

    if( !defined($delete) ) {
        $delete = 0;
    }

    my $mailServerObject = OBM::Entities::obmMailServer->new( $links, $delete );
    my $return = $mailServerObject->getEntity( &OBM::Update::utils::findDomainbyId( $self->{"domainList"}, $self->{"domain"} ) );
    if( !$return ) {
        return undef;
    }

    return $mailServerObject;
}


sub _deleteDbEntity {
    my $self = shift;
    my ( $table, $id ) = @_;

    if( !defined($table) ) {
        return 0;
    }

    if( !defined($id) || ($id !~ /^\d+$/) )  {
        return 0;
    }


    # Gestion des exceptions
    my $columnPrefix = $self->_tableNamePrefix( $table );


    # On supprime les informations de l'entité de la table de travail
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( '[Update::commonGlobalIncremental]: connecteur a la base de donnee invalide', 'W', 3 );
        return 0;
    }

    my $queryResult;
    my $query = "DELETE FROM P_".$table." WHERE ".$columnPrefix."_id=".$id;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }


    return 1;
}


sub _tableNamePrefix {
    my $self = shift;
    my( $table ) = @_;
    my $columnPrefix;
    
    if( lc($table) eq "ugroup" ) {
        $columnPrefix = "group";
    }else {
        $columnPrefix = lc($table);
    }

    return $columnPrefix;
}
