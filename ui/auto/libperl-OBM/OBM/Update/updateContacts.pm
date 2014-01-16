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


package OBM::Update::updateContacts;

$VERSION = '1.0';

use OBM::Entities::entityIdGetter;
use OBM::Log::log;
@ISA = ('OBM::Entities::entityIdGetter', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $parameters ) = @_;

    my $self = bless { }, $class;


    require OBM::Parameters::common;
    if( !$OBM::Parameters::common::obmModules->{'contact'} ) {
        $self->_log( 'module OBM-Contact désactivé, mise à jour annulée', 1 );
        return undef;
    }

    if( !defined($parameters) ) {
        $self->_log( 'paramètres d\'initialisation non définis', 1 );
        return undef;
    }

    $self->{'entitiesFactories'} = {};

    $self->{'incremental'} = $parameters->{'incremental'};
    $self->{'global'} = $parameters->{'global'};

    require OBM::Ldap::ldapServers;
    if( !($self->{'ldapservers'} = OBM::Ldap::ldapServers->instance()) ) {
        $self->_log( 'initialisation du gestionnaire de serveur LDAP impossible', 1 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );

    $self->{'entitiesFactories'} = undef;
}


sub update {
    my $self = shift;
    my( $domainIdList ) = @_;

    if( !defined($domainIdList) || (ref($domainIdList) ne 'ARRAY') ) {
        $domainIdList = $self->getDomainsId( 0 );
    }

    if( $self->_initEngines() ) {
        $self->_log( 'problème a l\'initialisation des moteurs de mises à jour', 0 );
        return 1;
    }

    if( $self->_initFactories( $domainIdList ) ) {
        $self->_log( 'problème a l\'initialisation des factories d\'entités', 0 );
        return 1;
    }


    for( my $i=0; $i<=$#{$domainIdList}; $i++ ) {
        $self->_setDomainLastUpdateDate( $domainIdList->[$i] );

        if( $self->_updateUpdatedDomainContacts( $domainIdList->[$i] ) ) {
            delete( $self->{'entitiesFactories'}->{$domainIdList->[$i]} );
        }

        if( $self->_deleteDeletedContact( $domainIdList->[$i] ) ) {
            return 1;
        }

        if( $self->_doUpdate() ) {
            return 1;
        }

        $self->_log( 'Mise à jour de la configuration du service LDAP', 3 );
        my @engines = values(%{$self->{'engines'}}); 
        for( my $i=0; $i<=$#engines; $i++ ) {
            $engines[$i]->update( $self->{'newContactService'} );
        }
        delete( $self->{'entitiesFactories'}->{$domainIdList->[$i]} );
    }

    return 0;
}


sub _initEngines {
    my $self = shift;

    require OBM::Ldap::ldapEngine;
    $self->_log( 'initialisation du moteur LDAP', 4 );
    $self->{'engines'}->{'ldapEngine'} = OBM::Ldap::ldapEngine->new();
    if( !defined($self->{'engines'}->{'ldapEngine'}) ) {
        $self->_log( 'erreur à l\'initialisation du moteur LDAP', 0 );
        return 1;
    }elsif( !ref($self->{'ldapEngine'}) ) {
        $self->_log( 'moteur LDAP non démarré', 4 );
        $self->{'ldapEngine'} = undef;
    }

    return 0;
}


sub _programEntitiesFactory {
    my $self = shift;
    my( $factoryProgramming, $domainId ) = @_;

    if( ref($factoryProgramming) ne 'OBM::EntitiesFactory::factoryProgramming' ) {
        $self->_log( 'programmeur de factory incorrect', 1 );
        return 1;
    }

    if( defined($domainId) && $domainId !~ /$regexp_id/ ) {
        $self->_log( 'Id de domain incorrect', 1 );
        return 1;
    }

    if( defined($domainId) ) {
        my $entitiesFactory = $self->{'entitiesFactories'}->{$domainId};
        if( !defined($entitiesFactory) ) {
            $self->_log( 'factory du domaine d\'ID '.$domainId.' incorrecte', 1 );
            return 1;
        }

        if( $entitiesFactory->loadEntities($factoryProgramming) ) {
            $self->_log( 'problème lors de la programmation de la factory du domaine d\'ID '.$domainId, 1 );
            return 1;
        }
    }else {
        my @entitiesFactories = values(%{$self->{'entitiesFactories'}});

        for( my $i=0; $i<=$#entitiesFactories; $i++ ) {
            if( $entitiesFactories[$i]->loadEntities($factoryProgramming) ) {
                $self->_log( 'problème lors de la programmation de la factory du domaine d\'ID '.$domainId, 1 );
                return 1;
            }
        }
    }

    return 0;
}


sub _initFactories {
    my $self = shift;
    my( $domainIdList ) = @_;

    for( my $i=0; $i<=$#{$domainIdList}; $i++ ) {
        require OBM::entitiesFactory;
        $self->_log( 'initialisation de l\'entity factory pour le domaine '.$domainIdList->[$i], 4 );

        my $entitiesFactory;
        if( !( $entitiesFactory = OBM::entitiesFactory->new( 'PROGRAMMABLEWITHOUTDOMAIN', $domainIdList->[$i], undef, undef ) ) ) {
            $self->_log( 'echec de l\'initialisation de l\'entity factory pour le domaine d\'ID '.$domainIdList->[$i], 0 );
            return 1;
        }

        $self->{'entitiesFactories'}->{$domainIdList->[$i]} = $entitiesFactory;
    }

    return 0;
}


sub _updateUpdatedDomainContacts {
    my $self = shift;
    my( $domainId ) = @_;

    $self->_log( 'Programmation de la suppression des contacts qui ne sont plus publics', 3 );
    if( $self->_deleteDomainContacts( $domainId ) ) {
        $self->_log( 'Erreur à la programmation de la suppression des contacts qui ne sont plus publics', 1 );
        return 1;
    }

    $self->_log( 'Programmation de la mise à jour des contacts publics', 3 );
    if( $self->_updateDomainContacts( $domainId ) ) {
        $self->_log( 'Erreur à la programmation de la mise à jour des contacts publics', 1 );
        return 1;
    }

    return 0;
}


sub _deleteDeletedContact {
    my $self = shift;
    my( $domainId ) = @_;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection à la base de données incorrecte !', 1 );
        return 1;
    }

    my $query = 'SELECT contactentity_entity_id
                 FROM ContactEntity
                 INNER JOIN Contact
                    ON contactentity_contact_id = contact_id
                 WHERE contact_domain_id = '.$domainId;

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 1;
    }

    my %contactIds;
    while( my $contact = $queryResult->fetchrow_hashref ) {
        $contactIds{$contact->{'contactentity_entity_id'}} = 1;
    }

    my $domainEntity = $self->{'entitiesFactories'}->{$domainId}->getDomainEntity();

    require OBM::Ldap::ldapContactEngine;
    my $ldapContactEngine = OBM::Ldap::ldapContactEngine->new();
    if( !$ldapContactEngine ) {
        return 1;
    }

    if( $ldapContactEngine->setEntitiesIds(\%contactIds) || $ldapContactEngine->deleteLdapContacts($domainEntity) ) {
        return 1;
    }


    return 0;
}


sub _deleteDomainContacts {
    my $self = shift;
    my( $domainId ) = @_;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection à la base de données incorrecte !', 1 );
        return 1;
    }

    my $query = 'SELECT Contact.contact_id
                    FROM Contact
                    WHERE (Contact.contact_addressbook_id NOT IN (
                        SELECT AddressBook.id
                        FROM AddressBook
                        INNER JOIN AddressbookEntity ON AddressbookEntity.addressbookentity_addressbook_id=AddressBook.id
                        INNER JOIN EntityRight ON AddressbookEntity.addressbookentity_entity_id=EntityRight.entityright_entity_id
                        WHERE EntityRight.entityright_consumer_id is NULL
                            AND EntityRight.entityright_read=1)
                    OR Contact.contact_archive=1)
                    AND Contact.contact_domain_id='.$domainId;

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 1;
    }

    my %contactIds;
    while( my $contact = $queryResult->fetchrow_hashref ) {
        $contactIds{$contact->{'contact_id'}} = 1;
    }

    my @contactIds = keys(%contactIds);

    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 1 );
        return 1;
    }
    if( $programmingObj->setEntitiesType( 'CONTACT' ) || $programmingObj->setUpdateType( 'DELETE' ) || $programmingObj->setEntitiesIds( \@contactIds )) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 0 );
        return 1;
    }

    if( $self->_programEntitiesFactory( $programmingObj, $domainId ) ) {
        $self->_log( 'probleme lors de la programmation  des contacts supprimés', 1 );
        return 1;
    }


    return 0;
}


sub _updateDomainContacts {
    my $self = shift;
    my( $domainId ) = @_;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection à la base de données incorrecte !', 1 );
        return 1;
    }

    my $query = 'SELECT Contact.contact_id
                    FROM Contact
                    INNER JOIN AddressBook ON Contact.contact_addressbook_id=AddressBook.id
                    INNER JOIN AddressbookEntity ON AddressbookEntity.addressbookentity_addressbook_id=AddressBook.id
                    INNER JOIN EntityRight ON AddressbookEntity.addressbookentity_entity_id=EntityRight.entityright_entity_id
                    WHERE EntityRight.entityright_consumer_id IS NULL
                        AND EntityRight.entityright_read=1
                        AND Contact.contact_archive=0
                        AND Contact.contact_domain_id='.$domainId;
    if( $self->{'domainLastUpdateTime'} ) {
        $query .= ' AND contact_timeupdate > \''.$self->{'domainLastUpdateTime'}.'\'';
    }

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 1;
    }

    my %contactIds;
    while( my $contact = $queryResult->fetchrow_hashref ) {
        $contactIds{$contact->{'contact_id'}} = 1;
    }

    my @contactIds = keys(%contactIds);

    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 1 );
        return 1;
    }
    if( $programmingObj->setEntitiesType( 'CONTACT' ) || $programmingObj->setUpdateType( 'UPDATE_ENTITY' ) || $programmingObj->setEntitiesIds( \@contactIds )) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 1 );
        return 1;
    }

    if( $self->_programEntitiesFactory( $programmingObj, $domainId ) ) {
        $self->_log( 'probleme lors de la programmation des contacts supprimés', 1 );
        return 1;
    }


    return 0;
}


sub _doUpdate {
    my $self = shift;
    my @engines = values(%{$self->{'engines'}});

    while( my( $domainId, $entitiesFactory ) = each(%{$self->{'entitiesFactories'}}) ) {
        if( !defined($entitiesFactory) ) {
            next;
        }

        $self->_log( 'traitement des contacts du domaine d\'ID '.$domainId, 3 );

        while( my $entity = $entitiesFactory->next() ) {
            for( my $i=0; $i<=$#engines; $i++ ) {
                $engines[$i]->update( $entity );
            }
        }
    }

    return 0;
}


sub _setDomainLastUpdateDate {
    my $self = shift;
    my( $domainId ) = @_;

    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 0 );
        return 1;
    }
    if($programmingObj->setEntitiesType( 'CONTACT_SERVICE' ) || $programmingObj->setUpdateType( 'UPDATE_ENTITY' )) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 0 );
        return 1;
    }

    if( $self->_programEntitiesFactory( $programmingObj, $domainId ) ) {
        $self->_log( 'probleme lors de la programmation des contacts supprimés', 0 );
        return 1;
    }

    my $entitiesFactory = $self->{'entitiesFactories'}->{$domainId};
    $self->{'newContactService'} = $entitiesFactory->next(); 


    if( $self->{'global'} ) {
        $self->{'domainLastUpdateTime'} = undef;
    }

    if( $self->{'incremental'} ) {
        require OBM::Ldap::ldapContactEngine;
        my $ldapContactEngine = OBM::Ldap::ldapContactEngine->new();
        if( !$ldapContactEngine ) {
            $self->_log( 'impossible d\'obtenir la date de dernière mise à jour. Mise à jour globale.', 2 );
            return 0;
        }

        $self->{'domainLastUpdateTime'} = $ldapContactEngine->getLastUpdateDate($self->{'newContactService'});

        if( defined($self->{'domainLastUpdateTime'}) ) {
            $self->_log( 'date de dernière mise à jour : '.$self->{'domainLastUpdateTime'}, 3 );
        }
    }

    return 0;
}
