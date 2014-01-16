#################################################################################
# Copyright (C) 2011-2014 Linagora
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version, provided you comply with the Additional Terms applicable for OBM
# software by Linagora pursuant to Section 7 of the GNU Affero General Public
# License, subsections (b), (c), and (e), pursuant to which you must notably (i)
# retain the displaying by the interactive user interfaces of the “OBM, Free
# Communication by Linagora” Logo with the “You are using the Open Source and
# free version of OBM developed and supported by Linagora. Contribute to OBM R&D
# by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
# links between OBM and obm.org, between Linagora and linagora.com, as well as
# between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
# from infringing Linagora intellectual property rights over its trademarks and
# commercial brands. Other Additional Terms apply, see
# <http://www.linagora.com/licenses/> for more details.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License and
# its applicable Additional Terms for OBM along with this program. If not, see
# <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
# version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
# applicable to the OBM software.
#################################################################################


package OBM::EntitiesFactory::incrementalFactories;

$VERSION = '1.0';

use OBM::EntitiesFactory::factory;
use OBM::Log::log;
@ISA = ('OBM::EntitiesFactory::factory', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub new {
    my $class = shift;
    my( $entitiesFactory ) = @_;

    my $self = bless { }, $class;

    if( !defined($entitiesFactory) || (ref($entitiesFactory) !~ /^OBM::entitiesFactory$/) ) {
        $self->_log( 'la factory doit être de type \'OBM::entitiesFactory\'', 1 );
        return undef;
    }

    $self->{'entitiesFactory'} = $entitiesFactory;

    if( $self->_initDeleteFactory() ) {
        $self->_log( 'problème à l\'initialisation des factory des entités supprimées', 1 );
        return undef;
    }

    if( $self->_initUpdateFactory() ) {
        $self->_log( 'problème à l\'initialisation des factory', 1 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );
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
        $query .= ' AND deleted_delegation=\''.$entitiesFactory->{'delegation'}.'\'';
    }


    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'chargement des hôtes à supprimer depuis la BD impossible', 1 );
        return 1;
    }

    my $idByType;
    while( my $deletedDesc = $queryResult->fetchrow_hashref() ) {
        if( !defined($deletedDesc->{'deleted_table'}) ) {
            $self->_log( 'table de l\'entité à supprimer d\'ID '.$deletedDesc->{'deleted_id'}.' non définie, traitement impossible', 1 );
            next;
        }

        push( @{$idByType->{$deletedDesc->{'deleted_table'}}}, $deletedDesc->{'deleted_entity_id'} );
    }

    $self->{'updateType'} = 'DELETE';
    while( my( $entityType, $entitiesIds ) = each(%{$idByType}) ) {
        SWITCH: {
            if( lc($entityType) eq 'host' ) {
                if( $self->_initHostFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des hôtes à supprimer', 1 );
                    return 1;
                }

                last SWITCH;
            }

            if( lc($entityType) eq 'ugroup' ) {
                if( $self->_initGroupFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des groupes à supprimer', 1 );
                    return 1;
                }

                last SWITCH;
            }

            if( lc($entityType) eq 'mailshare' ) {
                if( $self->_initMailshareFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des partages de messagerie à supprimer', 1 );
                    return 1;
                }

                last SWITCH;
            }

            if( lc($entityType) eq 'userobm' ) {
                if( $self->_initUserFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des utilisateurs à supprimer', 1 );
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

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::hostFactory;
        if( !($entityFactory = OBM::EntitiesFactory::hostFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des hôtes', 1 );
            return 1;
        }

        $entityFactory->setUpdateLinkedEntities();
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

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::groupFactory;
        if( !($entityFactory = OBM::EntitiesFactory::groupFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des groupes', 1 );
            return 1;
        }

        $entityFactory->setUpdateLinkedEntities();
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

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::mailshareFactory;
        if( !($entityFactory = OBM::EntitiesFactory::mailshareFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des partages de messagerie', 1 );
            return 1;
        }

        $entityFactory->setUpdateLinkedEntities();
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

    if( $#{$entitiesIds} >= 0 ) {
        my $entityFactory;

        require OBM::EntitiesFactory::userFactory;
        if( !($entityFactory = OBM::EntitiesFactory::userFactory->new( $self->{'updateType'}, $entitiesFactory->{'domain'}, $entitiesIds )) ) {
            $self->_log( 'problème au chargement de la factory des utilisateurs', 1 );
            return 1;
        }

        $entityFactory->setUpdateLinkedEntities();
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
        $self->_log( 'connexion à la base de données impossible', 1 );
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
        $query .= ' AND updated_delegation=\''.$entitiesFactory->{'delegation'}.'\'';
    }

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'chargement des entités à mettre à jour depuis la BD impossible', 1 );
        return 1;
    }

    my $idByUpdateType;
    while( my $updateDesc = $queryResult->fetchrow_hashref() ) {
        if( !defined($updateDesc->{'updated_table'}) ) {
            $self->_log( 'table de l\'entité à mettre à jour d\'ID '.$updateDesc->{'updated_id'}.' non définie, traitement impossible', 1 );
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
                        $self->_log( 'problème au chargement des hôtes à mettre à jour', 1 );
                        return 1;
                    }

                    last SWITCH;
                }

                if( lc($entityType) eq 'ugroup' ) {
                    if( $self->_initGroupFactory( $entitiesIds ) ) {
                        $self->_log( 'problème au chargement des groupes à mettre à jour', 1 );
                        return 1;
                    }

                    last SWITCH;
                }

                if( lc($entityType) eq 'mailshare' ) {
                    if( $self->_initMailshareFactory( $entitiesIds ) ) {
                        $self->_log( 'problème au chargement des partages de messagerie à mettre à jour', 1 );
                        return 1;
                    }

                    last SWITCH;
                }

                if( lc($entityType) eq 'userobm' ) {
                    if( $self->_initUserFactory( $entitiesIds ) ) {
                        $self->_log( 'problème au chargement des utilisateurs à mettre à jour', 1 );
                        return 1;
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
                 LEFT JOIN Updated ON updated_domain_id=updatedlinks_domain_id AND updated_user_id=updatedlinks_user_id AND updated_delegation=updatedlinks_delegation AND updatedlinks_table=updated_table AND updatedlinks_entity_id = updated_entity_id
                 WHERE updated_entity_id IS NULL AND updatedlinks_domain_id='.$entitiesFactory->{'domain'}->getId();

    if( defined($entitiesFactory->{'userId'}) ) {
        $query .= ' AND updatedlinks_user_id='.$entitiesFactory->{'userId'};
    }

    if( defined($entitiesFactory->{'delegation'}) ) {
        $query .= ' AND updatedlinks_delegation=\''.$entitiesFactory->{'delegation'}.'\'';
    }

    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'chargement des entités dont les liens sont à mettre à jour depuis la BD impossible', 3 );
        return 1;
    }

    my $idByType;
    while( my $updatedlinksDesc = $queryResult->fetchrow_hashref() ) {
        if( !defined($updatedlinksDesc->{'updatedlinks_table'}) ) {
            $self->_log( 'table de l\'entité dont les liens sont à mettre à jour d\'ID '.$updatedlinksDesc->{'updatedlinks_id'}.' non définie, traitement impossible', 1 );
            next;
        }

        push( @{$idByType->{$updatedlinksDesc->{'updatedlinks_table'}}}, $updatedlinksDesc->{'updatedlinks_entity_id'} );
    }

    $self->{'updateType'} = 'UPDATE_LINKS';
    while( my( $entityType, $entitiesIds ) = each(%{$idByType}) ) {
        SWITCH: {
            if( lc($entityType) eq 'host' ) {
                if( $self->_initHostFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des hôtes à mettre à jour', 1 );
                    return 1;
                }

                last SWITCH;
            }

            if( lc($entityType) eq 'ugroup' ) {
                if( $self->_initGroupFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des groupes à mettre à jour', 1 );
                    return 1;
                }

                last SWITCH;
            }

            if( lc($entityType) eq 'mailshare' ) {
                if( $self->_initMailshareFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des partages de messagerie à mettre à jour', 1 );
                    return 1;
                }

                last SWITCH;
            }

            if( lc($entityType) eq 'userobm' ) {
                if( $self->_initUserFactory( $entitiesIds ) ) {
                    $self->_log( 'problème au chargement des utilisateurs à mettre à jour', 1 );
                    return 1;
                }

                last SWITCH;
            }
        }
    }


    return 0;
}
