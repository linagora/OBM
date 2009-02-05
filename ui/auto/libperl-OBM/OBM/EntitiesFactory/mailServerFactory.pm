package OBM::EntitiesFactory::mailServerFactory;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(
        _log
        dump
        );
use OBM::EntitiesFactory::commonFactory qw(
        _checkSource
        _getSourceByUpdateType
        _checkUpdateType
        );
use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $updateType, $parentDomain ) = @_;

    my $self = bless { }, $class;

    $self->{'updateType'} = $updateType;
    if( !$self->_checkUpdateType() ) {
        return undef;
    }

    $self->{'source'} = $self->_getSourceByUpdateType();
    if( !$self->_checkSource() ) {
        return undef;
    }

    if( !defined($parentDomain) ) {
        $self->_log( 'description du domaine père indéfini', 3 );
        return undef;
    }

    if( ref($parentDomain) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'description du domaine père incorrecte', 3 );
        return undef;
    }
    $self->{'parentDomain'} = $parentDomain;
    
    $self->{'domainId'} = $parentDomain->getId();
    if( ref($self->{'domainId'}) || ($self->{'domainId'} !~ /$regexp_id/) ) {
        $self->_log( 'identifiant de domaine \''.$self->{'domainId'}.'\' incorrect', 3 );
        return undef;
    }

    $self->{'running'} = undef;
    $self->{'currentEntity'} = undef;
    $self->{'mailServerDescList'} = undef;


    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );

    $self->_reset();
}


sub _reset {
    my $self = shift;

    $self->_log( 'factory reset', 3 );

    $self->{'running'} = undef;
    $self->{'currentEntity'} = undef;
    $self->{'mailServerDescList'} = undef;

    return 1;
}


sub isRunning {
    my $self = shift;

    if( $self->{'running'} ) {
        $self->_log( 'la factory est en cours d\'exécution', 4 );
        return 1;
    }

    $self->_log( 'la factory n\'est pas en cours d\'exécution', 4 );

    return 0;
}


sub _start {
    my $self = shift;

    $self->_log( 'debut de traitement', 2 );

    if( $self->_loadMailServer() ) {
        $self->_log( 'problème lors de l\'obtention de la description de la configuration des serveurs de courriers du '.$self->{'parentDomain'}->getDescription(), 3 );
        return 0;
    }

    $self->{'running'} = 1;
    return $self->{'running'};
}


sub next {
    my $self = shift;

    $self->_log( 'obtention de l\'entité suivante', 2 );

    if( !$self->isRunning() ) {
        if( !$self->_start() ) {
            $self->_reset();
            return undef;
        }
    }

    if( defined($self->{'mailServerDescList'}) && (my $mailServerDesc = $self->{'mailServerDescList'}->fetchall_arrayref({})) ) {
        require OBM::Entities::obmMailServer;
        if( my $current = OBM::Entities::obmMailServer->new( $self->{'parentDomain'}, $mailServerDesc ) ) {
            $self->{'currentEntity'} = $current;

            SWITCH: {
                if( $self->{'source'} =~ /^SYSTEM$/ ) {
                    $self->{'currentEntity'}->unsetBdUpdate();
                    last SWITCH;
                }

                $self->{'currentEntity'}->setBdUpdate();
            }

            SWITCH: {
                if( $self->{'updateType'} eq 'UPDATE_ALL' ) {
                    if( $self->_loadMailServerLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
                        return undef;
                    }

                    $self->_log( 'mise à jour de l\'entité et des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    $self->{'currentEntity'}->setUpdateLinks();

                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'UPDATE_ENTITY' ) {
                    $self->_log( 'mise à jour de l\'entité, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'UPDATE_LINKS' ) {
                    if( $self->_loadMailServerLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
                        return undef;
                    }

                    $self->_log( 'mise à jour des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateLinks();
                    last SWITCH;
                }

                $self->_log( 'type de mise à jour inconnu \''.$self->{'updateType'}.'\'', 0 );
                return undef;
            }

            $self->{'mailServerDescList'} = undef;
            return $self->{'currentEntity'};
        }
    }

    return undef;
}


sub _loadMailServer {
    my $self = shift;

    $self->_log( 'chargement de la configuration des serveurs de courriers du domaine '.$self->{'parentDomain'}->getDescription().'\'', 2 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return 1;
    }

    my $hostTable = 'Host';
    my $domainEntity = 'DomainEntity';
    my $serviceProperty = 'ServiceProperty';
    if( $self->{'source'} =~ /^SYSTEM$/ ) {
        $hostTable = 'P_'.$hostTable;
        $domainEntity = 'P_'.$domainEntity;
        $serviceProperty = 'P_'.$serviceProperty;
    }

    my $query = 'SELECT host_name as server_name,
                        serviceproperty_property as server_role
                 FROM '.$hostTable.'
                 INNER JOIN DomainEntity ON domainentity_domain_id='.$self->{'domainId'}.'
                 INNER JOIN ServiceProperty ON serviceproperty_entity_id=domainentity_entity_id
                 WHERE host_id='.$dbHandler->castAsInteger('serviceproperty_value').' AND serviceproperty_service=\'mail\'';

    if( !defined($dbHandler->execQuery( $query, \$self->{'mailServerDescList'} )) ) {
        $self->_log( 'chargement de la configuration des serveurs de courriers depuis la BD impossible', 3 );
        return 1;
    }

    return 0;
}


sub _loadMailServerLinks {
    my $self = shift;

    return 0;
}
