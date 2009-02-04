package OBM::EntitiesFactory::incrementalFactories;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);
use OBM::EntitiesFactory::commonFactory qw(
            _getSourceByUpdateType
            _checkUpdateType
            );


sub new {
    my $class = shift;
    my( $entitiesFactory ) = @_;

    my $self = bless { }, $class;

    if( !defined($entitiesFactory) || (ref($entitiesFactory) !~ /^OBM::entitiesFactory$/) ) {
        $self->_log( 'la factory doit être de type \'OBM::entitiesFactory\'', 3 );
        return undef;
    }

    $self->{'entitiesFactory'} = $entitiesFactory;

    if( $self->_initDeleteFactory() ) {
        $self->_log( 'problème à l\'initialisation des factory des entités supprimées', 0 );
        return undef;
    }

    if( $self->_initUpdateFactory() ) {
        $self->_log( 'problème à l\'initialisation des factory', 0 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub _initDeleteFactory {
    my $self = shift;
    my $entitiesFactory = $self->{'entitiesFactory'};

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    my $query = 'SELECT deleted_id,
                        deleted_table,
                        deleted_entity_id
                 FROM Deleted
                 WHERE deleted_domain_id='.$entitiesFactory->{'domain'}->getId();

    if( defined($entitiesFactory->{'userId'}) ) {
        $query .= ' AND deleted_user_id='.$entitiesFactory->{'userId'};
    }

    if( defined($entitiesFactory->{'delegation'}) ) {
        $query .= ' AND deleted_delegation='.$entitiesFactory->{'delegation'};
    }


    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'chargement des hôtes à supprimer depuis la BD impossible', 3 );
        return 1;
    }

    my $idByType;
    while( my $deletedDesc = $queryResult->fetchrow_hashref() ) {
        if( !defined($deletedDesc->{'deleted_table'}) ) {
            $self->_log( 'table de l\'entité à supprimer d\'ID '.$deletedDesc->{'deleted_id'}.' non définie, traitement impossible', 0 );
            next;
        }

        push( @{$idByType->{$deletedDesc->{'deleted_table'}}}, $deletedDesc->{'deleted_entity_id'} );
    }

    $self->{'updateType'} = 'DELETE';
    while( my( $entityType, $entitiesIds ) = each(%{$idByType}) ) {
        SWITCH: {
            if( lc($entityType) eq 'host' ) {
                if( $self->_initHostFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des hôtes à supprimer', 0 );
                    return 1;
                }

                last SWITCH;
            }

            if( lc($entityType) eq 'ugroup' ) {
                if( $self->_initGroupFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des groupes à supprimer', 0 );
                    return 1;
                }

                last SWITCH;
            }

            if( lc($entityType) eq 'mailshare' ) {
                if( $self->_initMailshareFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des partages de messagerie à supprimer', 0 );
                    return 1;
                }

                last SWITCH;
            }

            if( lc($entityType) eq 'userobm' ) {
                if( $self->_initUserFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des utilisateurs à supprimer', 0 );
                    return 1;
                }

                last SWITCH;
            }
        }
    }

    return 0;
}


sub _initHostFactory {
    my $self = shift;
    my( $entitiesIds ) = @_;
    my $entitiesFactory = $self->{'entitiesFactory'};

    if( !$self->_checkUpdateType() ) {
        return 1;
    }

    my $source = $self->_getSourceByUpdateType();
    if( !defined($source) ) {
        return 1;
    }

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::hostFactory;
        if( !($entityFactory = OBM::EntitiesFactory::hostFactory->new( $source, $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des hôtes à supprimer', 3 );
            return 1;
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initGroupFactory {
    my $self = shift;
    my( $entitiesIds ) = @_;
    my $entitiesFactory = $self->{'entitiesFactory'};

    if( !$self->_checkUpdateType() ) {
        return 1;
    }

    my $source = $self->_getSourceByUpdateType();
    if( !defined($source) ) {
        return 1;
    }

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::groupFactory;
        if( !($entityFactory = OBM::EntitiesFactory::groupFactory->new( $source, $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des groupes à supprimer', 3 );
            return 1;
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initMailshareFactory {
    my $self = shift;
    my( $entitiesIds ) = @_;
    my $entitiesFactory = $self->{'entitiesFactory'};

    if( !$self->_checkUpdateType() ) {
        return 1;
    }

    my $source = $self->_getSourceByUpdateType();
    if( !defined($source) ) {
        return 1;
    }

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::mailshareFactory;
        if( !($entityFactory = OBM::EntitiesFactory::mailshareFactory->new( $source, $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des partages de messagerie à supprimer', 3 );
            return 1;
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initUserFactory {
    my $self = shift;
    my( $entitiesIds ) = @_;
    my $entitiesFactory = $self->{'entitiesFactory'};

    if( !$self->_checkUpdateType() ) {
        return 1;
    }

    my $source = $self->_getSourceByUpdateType();
    if( !defined($source) ) {
        return 1;
    }

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::userFactory;
        if( !($entityFactory = OBM::EntitiesFactory::userFactory->new( $source, $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des utilisateurs à supprimer', 3 );
            return 1;
        }

        $entitiesFactory->enqueueFactory( $entityFactory );
    }

    return 0;
}


sub _initUpdateFactory {
    my $self = shift;
    my $entitiesFactory = $self->{'entitiesFactory'};

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return 1;
    }

    my $query = 'SELECT updated_id,
                        updated_table,
                        updated_entity_id,
                        updatedlinks_entity_id
                 FROM Updated
                 LEFT JOIN Updatedlinks ON updated_user_id=updatedlinks_user_id AND updated_delegation=updatedlinks_delegation AND updated_entity_id=updatedlinks_entity_id
                 WHERE updated_domain_id='.$entitiesFactory->{'domain'}->getId();

    if( defined($entitiesFactory->{'userId'}) ) {
        $query .= ' AND updated_user_id='.$entitiesFactory->{'userId'};
    }

    if( defined($entitiesFactory->{'delegation'}) ) {
        $query .= ' AND updated_delegation='.$entitiesFactory->{'delegation'};
    }

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'chargement des entités à mettre à jour depuis la BD impossible', 3 );
        return 1;
    }

    my $idByUpdateType;
    while( my $updateDesc = $queryResult->fetchrow_hashref() ) {
        if( !defined($updateDesc->{'updated_table'}) ) {
            $self->_log( 'table de l\'entité à mettre à jour d\'ID '.$updateDesc->{'updated_id'}.' non définie, traitement impossible', 0 );
            next;
        }

        if( defined($updateDesc->{'updatedlinks_entity_id'}) ) {
            push( @{$idByUpdateType->{'UPDATE_ALL'}->{$updateDesc->{'updated_table'}}}, $updateDesc->{'updated_entity_id'} );
        }else {
            push( @{$idByUpdateType->{'UPDATE_ENTITY'}->{$updateDesc->{'updated_table'}}}, $updateDesc->{'updated_entity_id'} );
        }
    }

    while( my( $updateType, $idByType ) = each(%{$idByUpdateType}) ) {
        $self->{'updateType'} = $updateType;

        while( my( $entityType, $entitiesIds ) = each(%{$idByType}) ) {
            SWITCH: {
                if( lc($entityType) eq 'host' ) {
                    if( $self->_initHostFactory( $entitiesIds ) ) {
                        if( $self->_initHostFactory( $entitiesIds ) ) {
                            $self->_log( 'problème au chargement des hôtes à mettre à jour', 0 );
                            return 1;
                        }
                    }

                    last SWITCH;
                }

                if( lc($entityType) eq 'ugroup' ) {
                    if( $self->_initHostFactory( $entitiesIds ) ) {
                        if( $self->_initGroupFactory( $entitiesIds ) ) {
                            $self->_log( 'problème au chargement des groupes à mettre à jour', 0 );
                            return 1;
                        }
                    }

                    last SWITCH;
                }

                if( lc($entityType) eq 'mailshare' ) {
                    if( $self->_initHostFactory( $entitiesIds ) ) {
                        if( $self->_initMailshareFactory( $entitiesIds ) ) {
                            $self->_log( 'problème au chargement des partages de messagerie à mettre à jour', 0 );
                            return 1;
                        }
                    }

                    last SWITCH;
                }

                if( lc($entityType) eq 'userobm' ) {
                    if( $self->_initUserFactory( $entitiesIds ) ) {
                        if( $self->_initMailshareFactory( $entitiesIds ) ) {
                            $self->_log( 'problème au chargement des utilisateurs à mettre à jour', 0 );
                            return 1;
                        }
                    }

                    last SWITCH;
                }
            }
        }
    }


    $query = 'SELECT    updatedlinks_id,
                        updatedlinks_table,
                        updatedlinks_entity_id
                 FROM Updatedlinks
                 LEFT JOIN Updated ON updated_domain_id=updatedlinks_domain_id AND updated_user_id=updatedlinks_user_id AND updated_delegation=updatedlinks_delegation AND updatedlinks_table=updated_table
                 WHERE updated_entity_id IS NULL AND updatedlinks_domain_id='.$entitiesFactory->{'domain'}->getId();

    if( defined($entitiesFactory->{'userId'}) ) {
        $query .= ' AND updatedlinks_user_id='.$entitiesFactory->{'userId'};
    }

    if( defined($entitiesFactory->{'delegation'}) ) {
        $query .= ' AND updatedlinks_delegation='.$entitiesFactory->{'delegation'};
    }

    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'chargement des entités dont les liens sont à mettre à jour depuis la BD impossible', 3 );
        return 1;
    }

    my $idByType;
    while( my $updatedlinksDesc = $queryResult->fetchrow_hashref() ) {
        if( !defined($updatedlinksDesc->{'updatedlinks_table'}) ) {
            $self->_log( 'table de l\'entité dont les liens sont à mettre à jour d\'ID '.$updatedlinksDesc->{'updatedlinks_id'}.' non définie, traitement impossible', 0 );
            next;
        }

        push( @{$idByType->{$updatedlinksDesc->{'updatedlinks_table'}}}, $updatedlinksDesc->{'updatedlinks_entity_id'} );
    }

    $self->{'updateType'} = 'UPDATE_LINKS';
    while( my( $entityType, $entitiesIds ) = each(%{$idByType}) ) {
        SWITCH: {
            if( lc($entityType) eq 'host' ) {
                if( $self->_initHostFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des hôtes à mettre à jour', 0 );
                    return 1;
                }

                last SWITCH;
            }

            if( lc($entityType) eq 'ugroup' ) {
                if( $self->_initGroupFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des groupes à mettre à jour', 0 );
                    return 1;
                }

                last SWITCH;
            }

            if( lc($entityType) eq 'mailshare' ) {
                if( $self->_initMailshareFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des partages de messagerie à mettre à jour', 0 );
                    return 1;
                }

                last SWITCH;
            }

            if( lc($entityType) eq 'userobm' ) {
                if( $self->_initUserFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des utilisateurs à mettre à jour', 0 );
                    return 1;
                }

                last SWITCH;
            }
        }
    }


    return 0;
}
