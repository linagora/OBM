package OBM::DbUpdater::groupUpdater;

$VERSION = '1.0';

use OBM::Log::log;
@ISA = ('OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub new {
    my $class = shift;

    my $self = bless { }, $class;

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );
}


sub update {
    my $self = shift;
    my( $entity ) = @_;

    if( ref($entity) ne 'OBM::Entities::obmGroup' ) {
        $self->_log( 'entité incorrecte, traitement impossible', 0 );
        return 1;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler;
    my $sth;
    if( !($dbHandler = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 1;
    }

    if( $self->_delete($entity) ) {
        $self->_log( 'problème à la mise à jour BD du groupe '.$entity->getDescription(), 1 );
        return 1;
    }


    my $query;
    if( !$entity->getDelete() && $entity->getUpdateEntity() ) {
        $query = 'INSERT INTO P_UGroup
                    (   group_id,
                        group_domain_id,
                        group_timecreate,
                        group_userupdate,
                        group_usercreate,
                        group_system,
                        group_archive,
                        group_privacy,
                        group_local,
                        group_ext_id,
                        group_samba,
                        group_gid,
                        group_mailing,
                        group_delegation,
                        group_manager_id,
                        group_name,
                        group_desc,
                        group_email
                    ) SELECT    group_id,
                                group_domain_id,
                                group_timecreate,
                                group_userupdate,
                                group_usercreate,
                                group_system,
                                group_archive,
                                group_privacy,
                                group_local,
                                group_ext_id,
                                group_samba,
                                group_gid,
                                group_mailing,
                                group_delegation,
                                group_manager_id,
                                group_name,
                                group_desc,
                                group_email
                      FROM UGroup
                      WHERE group_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 1 );
            return 1;
        }

        $query = 'INSERT INTO P_GroupEntity
                    (   groupentity_entity_id,
                        groupentity_group_id
                    ) SELECT    groupentity_entity_id,
                                groupentity_group_id
                      FROM GroupEntity
                      WHERE groupentity_group_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 1 );
            return 1;
        }

        $query = 'INSERT INTO P_CategoryLink
                    (   categorylink_category_id,
                        categorylink_entity_id,
                        categorylink_category
                    ) SELECT    categorylink_category_id,
                                categorylink_entity_id,
                                categorylink_category
                      FROM CategoryLink
                      WHERE categorylink_entity_id=(SELECT groupentity_entity_id
                                                    FROM GroupEntity
                                                    WHERE groupentity_group_id = '.$entity->getId().')';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 1 );
            return 1;
        }

        $query = 'INSERT INTO P_field
                    (   id,
                        entity_id,
                        field,
                        value
                    ) SELECT    id,
                                entity_id,
                                field,
                                value
                      FROM field
                      WHERE entity_id=(SELECT groupentity_entity_id
                                                    FROM GroupEntity
                                                    WHERE groupentity_group_id = '.$entity->getId().')';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 1 );
            return 1;
        }
    }

    if( !$entity->getDelete() && $entity->getUpdateLinks() ) {
        # Group members
        $query = 'INSERT INTO P_of_usergroup
                    (   of_usergroup_group_id,
                        of_usergroup_user_id
                    ) SELECT    of_usergroup_group_id,
                                of_usergroup_user_id
                      FROM of_usergroup
                      WHERE of_usergroup_group_id='.$entity->getId();

        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour des liens '.$entity->getDescription(), 1 );
            return 1;
        }

        # Group contacts
        $query = 'INSERT INTO P__contactgroup
                    (   contact_id,
                        group_id
                    ) SELECT    contact_id,
                                group_id
                      FROM _contactgroup
                      WHERE group_id='.$entity->getId();

        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour des liens '.$entity->getDescription(), 1 );
            return 1;
        }
    }


    return 0;
}


sub delete {
    my $self = shift;
    my( $entity ) = @_;

    if( !$entity->getDelete() ) {
        $self->_log( 'l\'entité '.$entity->getDescription().' n\'est pas à supprimer. Suppression annulée', 3 );
        return 0;
    }

    return $self->_delete( $entity );
}


sub _delete {
    my $self = shift;
    my( $entity ) = @_;

    if( ref($entity) ne 'OBM::Entities::obmGroup' ) {
        $self->_log( 'entité incorrecte, traitement impossible', 0 );
        return 1;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler;
    my $sth;
    if( !($dbHandler = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 1;
    }


    my $query;
    if( $entity->getDelete() || $entity->getUpdateLinks() ) {
        $query = 'DELETE FROM P_of_usergroup WHERE of_usergroup_group_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD de liens '.$entity->getDescription(), 1 );
            return 1;
        }

        $query = 'DELETE FROM P__contactgroup WHERE group_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD de liens '.$entity->getDescription(), 1 );
            return 1;
        }
    }

    if( $entity->getDelete() || $entity->getUpdateEntity() ) {
        $query = 'DELETE FROM P_CategoryLink
                    WHERE categorylink_entity_id=(SELECT groupentity_entity_id
                                                    FROM GroupEntity
                                                    WHERE groupentity_group_id = '.$entity->getId().')';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 1 );
            return 1;
        }

        $query = 'DELETE FROM P_field
                    WHERE entity_id=(SELECT groupentity_entity_id
                                                    FROM GroupEntity
                                                    WHERE groupentity_group_id = '.$entity->getId().')';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 1 );
            return 1;
        }

        $query = 'DELETE FROM P_GroupEntity WHERE groupentity_group_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 1 );
            return 1;
        }

        $query = 'DELETE FROM P_UGroup WHERE group_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 1 );
            return 1;
        }
    }


    return 0;
}
