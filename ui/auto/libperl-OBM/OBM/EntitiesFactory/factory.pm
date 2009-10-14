package OBM::EntitiesFactory::factory;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);
require OBM::Parameters::regexp;


sub new {
    return undef;
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
    $self->{'entitiesDescList'} = undef;
    $self->{'nextEntityDesc'} = undef;

    return 0;
}


sub _start {
    my $self = shift;

    $self->_log( 'debut de traitement', 2 );

    if( $self->_loadEntities() ) {
        $self->_log( 'problème lors du chargement des entités par la factory \''.ref($self).'\' du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 3 );
        return 0;
    }

    $self->{'running'} = 1;
    return $self->{'running'};
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


sub next {
    my $self = shift;

    return undef;
}


sub _loadEntities {
    my $self = shift;

    return 0;
}


sub _checkSource {
    my $self = shift;

    if( !defined($self->{'source'}) ) {
        $self->_log( 'source de données indéfini', 3 );
        return 0;
    }

    if( ref($self->{'source'}) ) {
        $self->_log( 'source de données incorrecte', 3 );
        return 0;
    }

    if( $self->{'source'} !~ /^(WORK|SYSTEM)$/ ) {
        $self->_log( 'source de données \''.$self->{'source'}.'\' incorrecte', 3 );
        return 0;
    }

    $self->_log( 'source de données \''.$self->{'source'}.'\'', 3 );
    return 1;
}


# Allowed update type :
#   UPDATE_ALL : entity+links from work tables
#   UPDATE_ENTITY : entity only from work tables
#   UPDATE_LINKS : links only from work tables
#   SYSTEM_ALL : entity+links from system tables
#   SYSTEM_ENTITY : entity only from system tables
#   SYSTEM_LINKS : links only from system tables
#   DELETE : delete entity
sub _checkUpdateType {
    my $self = shift;

    if( !defined($self->{'updateType'}) ) {
        $self->_log( 'type de mise à jour indéfini', 3 );
        return 0;
    }

    if( ref($self->{'updateType'}) ) {
        $self->_log( 'type de mise à jour incorrecte', 3 );
        return 0;
    }

    if( $self->{'updateType'} !~ /^(UPDATE_ALL|UPDATE_ENTITY|UPDATE_LINKS|SYSTEM_ALL|SYSTEM_ENTITY|SYSTEM_LINKS|DELETE)$/ ) {
        $self->_log( 'type de mise à jour \''.$self->{'updateType'}.'\' incorrecte', 3 );
        return 0;
    }

    $self->_log( 'type de mise à jour \''.$self->{'updateType'}.'\'', 3 );
    return 1;
}


# Get consumer rights on entities
sub _getEntityRight {
    my $self = shift;
    my ( $rightDef ) = @_;
    my %entityTemplate = ( 'read', 0, 'writeonly', 0, 'write', 0, 'admin', 0 );
    my %usersList;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    if( !defined($rightDef) ) {
        return undef;
    }


    my $queryResult;
    if( !defined($dbHandler->execQuery( $rightDef->{'public'}->{'sqlQuery'}, \$queryResult )) ) {
        return undef;
    }

    if( my( $read, $write ) = $queryResult->fetchrow_array ) {
        $usersList{'anyone'}->{'userId'} = 0;
        if( $read && !$write ) {
            $usersList{'anyone'}->{'read'} = 1;
            $usersList{'anyone'}->{'writeonly'} = 0;
            $usersList{'anyone'}->{'write'} = 0;

            # Droit a ne pas traiter car droit public
            $rightDef->{'read'}->{'compute'} = 0;

            # construction d'un template utilisateur
            $entityTemplate{'read'} = 1;
        }elsif( !$read && $write ) {
            $usersList{'anyone'}->{'read'} = 0;
            $usersList{'anyone'}->{'writeonly'} = 1;
            $usersList{'anyone'}->{'write'} = 0;
            
            # Droit a ne pas traiter car droit public
            $rightDef->{'writeonly'}->{'compute'} = 0;

            # construction d'un template utilisateur
            $entityTemplate{'writeonly'} = 1;
        }elsif( $read && $write ) {
            $usersList{'anyone'}->{'read'} = 0;
            $usersList{'anyone'}->{'writeonly'} = 0;
            $usersList{'anyone'}->{'write'} = 1;

            # Droit a ne pas traiter car droit public
            $rightDef->{'read'}->{'compute'} = 0;
            $rightDef->{'writeonly'}->{'compute'} = 0;
            $rightDef->{'write'}->{'compute'} = 0;
        }
    }
    $queryResult->finish;


    # Traitement du droit '$right', cles du hachage '%rightDef'
    my $domainName = $self->{'parentDomain'}->getDesc('domain_name');
    while( my( $right, $rightDesc ) = each( %{$rightDef} ) ) {
        if( !$rightDesc->{'compute'} ) {
            next;
        }

        # On execute la requête correspondant au droit
        if( !defined($dbHandler->execQuery( $rightDef->{$right}->{'sqlQuery'}, \$queryResult )) ) {
            return undef;
        }

        while( my( $userId, $userLogin ) = $queryResult->fetchrow_array ) {
            $userLogin .= '@'.$domainName;

            # Si l'utilisateur n'a pas déjà été trouvé, on l'initialise
            # avec les valeurs du template
            if( !exists( $usersList{$userLogin} ) ) {
                $usersList{$userLogin}->{'userId'} = $userId;
                while( my( $templateRight, $templateValue ) = each( %entityTemplate ) ) {
                    $usersList{$userLogin}->{$templateRight} = $templateValue;
                }
            }

            $usersList{$userLogin}->{$right} = 1;
        }
    }

    # Normalisation des droits
    return $self->_computeRight( \%usersList );
}


sub _computeRight {
    my $self = shift;
    my( $usersList ) = @_;
    my $rightList;

    while( my( $userName, $right ) = each( %$usersList ) ) {
        SWITCH: {
            if( $right->{'write'} && $right->{'admin'} ) {
                $rightList->{'writeAdmin'}->{$userName} = $usersList->{$userName}->{'userId'};
                last SWITCH;
            }
            
            if( $right->{'write'} && !$right->{'admin'} ) {
                $rightList->{'write'}->{$userName} = $usersList->{$userName}->{'userId'};
                last SWITCH;
            }
            
            if( $right->{'read'} && $right->{'writeonly'} && $right->{'admin'} ) {
                $rightList->{'writeAdmin'}->{$userName} = $usersList->{$userName}->{'userId'};
                last SWITCH;
            }
            
            if( $right->{'read'} && $right->{'writeonly'} && !$right->{'admin'} ) {
                $rightList->{'write'}->{$userName} = $usersList->{$userName}->{'userId'};
                last SWITCH;
            }
            
            if( $right->{'read'} && $right->{'admin'} ) {
                $rightList->{'readAdmin'}->{$userName} = $usersList->{$userName}->{'userId'};
                last SWITCH;
            }
            
            if( $right->{'read'} && !$right->{'admin'} ) {
                $rightList->{'read'}->{$userName} = $usersList->{$userName}->{'userId'};
                last SWITCH;
            }
            
            if( $right->{'writeonly'} && $right->{'admin'} ) {
                $rightList->{'writeonlyAdmin'}->{$userName} = $usersList->{$userName}->{'userId'};
                last SWITCH;
            }
            
            if( $right->{'writeonly'} && !$right->{'admin'} ) {
                $rightList->{'writeonly'}->{$userName} = $usersList->{$userName}->{'userId'};
                last SWITCH;
            }

            if( !$right->{'read'} && !$right->{'writeonly'} && !$right->{'write'} && $right->{'admin'} ) {
                $rightList->{'admin'}->{$userName} = $usersList->{$userName}->{'userId'};
                last SWITCH;
            }
        }
    }

    return $rightList;
}


sub _getLinkedEntities {
    my $self = shift;

    my $updateLinkedEntityOn = $self->{'updateLinkedEntityOn'};
    if( !$updateLinkedEntityOn ) {
        $updateLinkedEntityOn = 'UPDATE_ALL|UPDATE_ENTITY';
    }

    if( $self->{'updateType'} !~ /^($updateLinkedEntityOn)$/ ) {
        return undef;
    }

    if( !$self->getUpdateLinkedEntities() ) {
        $self->_log( 'les entités liés ne sont pas à mettre à jour', 3 );
        return undef;
    }

    if( !defined($self->{'linkedEntitiesFactory'}) && (!defined($self->{'currentEntity'}) || !$self->{'currentEntity'}->updateLinkedEntities( $self->{'updateType'} )) ) {
        return undef;
    }

    if( !defined($self->{'linkedEntitiesFactory'}) && (!$self->_loadLinkedEntitiesFactories()) ) {
        $self->_log( 'chargement des factories des entités liées', 3 );
    }

    if ( defined($self->{'linkedEntitiesFactory'}) && (my $currentLinkedEntity = $self->{'linkedEntitiesFactory'}->next()) ) {
        return $currentLinkedEntity;
    }

    $self->{'linkedEntitiesFactory'} = undef;
    return undef;
}


sub _loadLinkedEntitiesFactories {
    my $self = shift;

    return undef;
}


sub _enqueueLinkedEntitiesFactory {
    my $self = shift;
    my( $factoryProgramming ) = @_;

    if( ref($factoryProgramming) ne 'OBM::EntitiesFactory::factoryProgramming' ) {
        $self->_log( 'linked entities factory programmateur incorrect', 4 );
        return 1;
    }

    if( !defined($self->{'linkedEntitiesFactory'}) ) {
        require OBM::entitiesFactory;
        $self->{'linkedEntitiesFactory'} = OBM::entitiesFactory->new( 'PROGRAMMABLEWITHOUTDOMAIN', $self->{'currentEntity'}->getDomainId() );
        if( !defined($self->{'linkedEntitiesFactory'}) ) {
            $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
            return 1;
        }
    }

    if( $self->{'linkedEntitiesFactory'}->loadEntities($factoryProgramming) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }

    return 0;
}


sub setUpdateLinkedEntities {
    my $self = shift;

    $self->{'updateLinkedEntities'} = 1;

    return 0;
}


sub getUpdateLinkedEntities {
    my $self = shift;

    if( !defined($self->{'updateLinkedEntities'}) ) {
        $self->{'updateLinkedEntities'} = 0;
    }

    return $self->{'updateLinkedEntities'};
}
