package OBM::EntitiesFactory::hostFactory;

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
        _checkUpdateType
        );
use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $source, $updateType, $parentDomain, $ids ) = @_;

    my $self = bless { }, $class;

    $self->{'source'} = $source;
    if( !$self->_checkSource() ) {
        return undef;
    }

    $self->{'updateType'} = $updateType;
    if( !$self->_checkUpdateType() ) {
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

    if( defined($ids) && (ref($ids) ne 'ARRAY') ) {
        $self->_log( 'liste d\'ID à traiter incorrecte', 3 );
        return undef;
    }

    if( $#{$ids} >= 0 ) {
        $self->{'ids'} = $ids;
    }

    $self->{'running'} = undef;
    $self->{'currentEntity'} = undef;
    $self->{'hostDescList'} = undef;


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
    $self->{'hostDescList'} = undef;

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

    if( $self->_loadHosts() ) {
        $self->_log( 'problème lors de l\'obtention de la description des hôtes du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 3 );
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

    while( defined($self->{'hostDescList'}) && (my $userHostDesc = $self->{'hostDescList'}->fetchrow_hashref()) ) {
        require OBM::Entities::obmHost;
        if( !(my $current = OBM::Entities::obmHost->new( $self->{'parentDomain'}, $userHostDesc )) ) {
            next;
        }else {
            $self->{'currentEntity'} = $current;

            SWITCH: {
                if( $self->{'source'} =~ /^SYSTEM$/ ) {
                    $self->{'currentEntity'}->unsetBdUpdate();
                    last SWITCH;
                }

                $self->{'currentEntity'}->setBdUpdate();
            }

            SWITCH: {
                if( $self->{'updateType'} =~ /^(UPDATE_ALL|UPDATE_ENTITY|UPDATE_LINKS)$/ ) {
                    $self->_log( 'mise à jour de l\'entité, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'DELETE' ) {
                    $self->_log( 'suppression de l\'entité, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setDelete();
                    last SWITCH;
                }

                $self->_log( 'type de mise à jour inconnu \''.$self->{'updateType'}.'\'', 0 );
                return undef;
            }

            return $self->{'currentEntity'};
        }
    }

    return undef;
}


sub _loadHosts {
    my $self = shift;

    $self->_log( 'chargement des hôtes du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 2 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return 1;
    }

    my $hostTable = 'Host';
    my $hostEntityTable = 'HostEntity';
    my $serviceTable = 'Service';
    if( $self->{'source'} =~ /^SYSTEM$/ ) {
        $hostTable = 'P_'.$hostTable;
        $hostEntityTable = 'P_'.$hostEntityTable;
        $serviceTable = 'P_'.$serviceTable;
    }

    my $query = 'SELECT '.$hostTable.'.*,
                        service_id AS host_samba,
                        current.host_name as host_name_current
                 FROM '.$hostTable.'
                 INNER JOIN '.$hostEntityTable.' ON hostentity_host_id=host_id
                 LEFT JOIN '.$serviceTable.' ON service_entity_id=hostentity_entity_id AND service_service=\'samba\'
                 LEFT JOIN P_Host current ON current.host_id='.$hostTable.'.host_id
                 WHERE '.$hostTable.'.host_domain_id='.$self->{'domainId'};

    if( $self->{'ids'} ) {
        $query .= ' AND '.$hostTable.'.host_id IN ('.join( ', ', @{$self->{'ids'}} ).')';
    }

    if( !defined($dbHandler->execQuery( $query, \$self->{'hostDescList'} )) ) {
        $self->_log( 'chargement des hôtes depuis la BD impossible', 3 );
        return 1;
    }

    return 0;
}
