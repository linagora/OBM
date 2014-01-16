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


package OBM::Update::updatePassword;

$VERSION = '1.0';

use OBM::Entities::systemEntityIdGetter;
use OBM::Log::log;
@ISA = ('OBM::Entities::systemEntityIdGetter', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub new {
    my $class = shift;
    my( $parameters ) = @_;

    my $self = bless { }, $class;


    if( !defined($parameters) ) {
        $self->_log( 'paramètres d\'initialisation non définis', 0 );
        return undef;
    }


    if( !$parameters->{'no-old'} ) {
        $self->{'oldPassword'} = $parameters->{'old-passwd'};
    }

    $self->{'newPasswordDesc'}->{'newPassword'} = $parameters->{'passwd'};
    $self->{'newPasswordDesc'}->{'unix'} = $parameters->{'unix'};
    $self->{'newPasswordDesc'}->{'samba'} = $parameters->{'samba'};
    $self->{'newPasswordDesc'}->{'sql'} = $parameters->{'sql'};

    $self->{'userLogin'} = $parameters->{'login'};
    $self->{'domainId'} = $parameters->{'domain-id'};

    require OBM::Ldap::ldapServers;
    if( !($self->{'ldapservers'} = OBM::Ldap::ldapServers->instance()) ) {
        $self->_log( 'initialisation du gestionnaire de serveur LDAP impossible', 3 );
        return undef;
    }

    if( $self->_getEntity() ) {
        return undef;
    }

    return $self;
}


sub _getEntity {
    my $self = shift;

    # Getting BD user ID
    my $userId = $self->_getUserIdFromUserLoginDomain( $self->{'userLogin'}, $self->{'domainId'} );
    if( !defined($userId) ) {
        $self->_log( 'utilisateur \''.$self->{'userLogin'}.'\', domaine d\'ID '.$self->{'domainId'}.' inconnu', 1 );
        return 1;
    }

    # Getting user entity
    require OBM::EntitiesFactory::factoryProgramming;
    my $programmingObj = OBM::EntitiesFactory::factoryProgramming->new();
    if( !defined($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }
    if( $programmingObj->setEntitiesType( 'USER' ) || $programmingObj->setUpdateType( 'SYSTEM_ENTITY' ) || $programmingObj->setEntitiesIds( [$userId] ) ) {
        $self->_log( 'problème lors de l\'initialisation du programmateur de factory', 4 );
        return 1;
    }

    require OBM::entitiesFactory;
    my $entitiesFactory = OBM::entitiesFactory->new( 'PROGRAMMABLEWITHOUTDOMAIN', $self->{'domainId'} );
    if( !defined($entitiesFactory) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }
    if( $entitiesFactory->loadEntities($programmingObj) ) {
        $self->_log( 'probleme lors de la programmation de la factory d\'entités', 3 );
        return 1;
    }

    while( my $userEntity = $entitiesFactory->next() ) {
        $self->{'userEntity'} = $userEntity;
    }

    if( !defined($self->{'userEntity'}) ) {
        $self->_log( 'problème lors de la récupération de la description de l\'utilisateur', 0 );
        return 1;
    }

    return 0;
}


sub update {
    my $self = shift;

    my $entityDns = $self->{'userEntity'}->getCurrentDnPrefix();

    if( $self->{oldPassword} ) {
        if( $self->_checkPasswd() ) {
            $self->_log( 'erreur lors de la vérification de l\'ancien mot de passe de '.$self->{'userEntity'}->getDescription(), 0 );
            return 1;
        }else {
            $self->_log( 'ancien mot de passe correct', 0 );
        }
    }

    if( $self->_updatePassword() ) {
        $self->_log( 'erreur lors de la mise a jour du mot de passe de '.$self->{'userEntity'}->getDescription(), 0 );
        return 1;
    }

    return 0;
}


sub _checkPasswd {
    my $self = shift;

    my $entityLdapServer = $self->{'ldapservers'}->getLdapServer($self->{'userEntity'}->getLdapServerId());
    if( ref($entityLdapServer) ne 'OBM::Ldap::ldapServer' ) {
        $self->_log( 'erreur lors de l\'obtention du serveur LDAP de '.$self->{'userEntity'}->getDescription(), 3 );
        return 1;
    }

    my $currentEntityDNs = $self->{'userEntity'}->getCurrentDnPrefix();
    if( !defined($currentEntityDNs) ) {
        $self->_log( 'erreur lors de l\'obtention du DN de '.$self->{'userEntity'}->getDescription(), 3 );
        return 1;
    }

    for( my $i=0; $i<=$#{$currentEntityDNs}; $i++ ) {
        $entityLdapServer->resetConn();

        $entityLdapServer->setDnLogin( $currentEntityDNs->[$i] );
        $entityLdapServer->setPasswd( $self->{oldPassword} );

        if( !$entityLdapServer->getConn() ) {
            $entityLdapServer->resetConn();
            return 1;
        }
    }

    $entityLdapServer->resetConn();
    return 0;
}


sub _updatePassword {
    my $self = shift;

    if( $self->{'newPasswordDesc'}->{'unix'} ) {
        $self->_log( 'mise à jour du mot de passe Unix', 4 );

        if( $self->_updateUnixPasswd() ) {
            $self->_log( 'probleme lors de la mise à jour du mot de passe Unix', 3 );
            return 1;
        }else {
            $self->_log( 'succès de la mise à jour du mot de passe Unix', 2 );
        }
    }

    if( $self->{'newPasswordDesc'}->{'samba'} ) {
        $self->_log( 'mise à jour du mot de passe Samba', 3 );

        if( $self->_updateSambaPasswd() ) {
            $self->_log( 'probleme lors de la mise à jour du mot de passe Samba', 3 );
            return 1;
        }else {
            $self->_log( 'succès de la mise à jour du mot de passe Samba', 2 );
        }
    }

    if( $self->{'newPasswordDesc'}->{'sql'} ) {
        $self->_log( 'mise à jour du mot de passe SQL', 4 );

        if( $self->_updateSqlPasswd() ) {
            $self->_log( 'probleme lors de la mise à jour du mot de passe SQL', 3 );
            return 1;
        }else {
            $self->_log( 'succès de la mise à jour du mot de passe SQL', 2 );
        }
    }


    return 0;
}


sub _updateUnixPasswd {
    my $self = shift;

    require OBM::Password::unixPasswdUpdater;
    my $passwordUpdater = OBM::Password::unixPasswdUpdater->new();
    if( $passwordUpdater->update( $self->{'userEntity'}, $self->{'newPasswordDesc'}->{'newPassword'} ) ) {
        $self->_log( 'problème a la mise à jour du mot de passe Unix', 0 );
        return 1;
    }

    return 0;
}


sub _updateSqlPasswd {
    my $self = shift;

    require OBM::Password::sqlPasswdUpdater;
    my $passwordUpdater = OBM::Password::sqlPasswdUpdater->new();
    if( $passwordUpdater->update( $self->{'userEntity'}, $self->{'newPasswordDesc'}->{'newPassword'} ) ) {
        $self->_log( 'problème à la mise à jour du mot de passe SQL', 0 );
        return 1;
    }

    return 0;
}


sub _updateSambaPasswd {
    my $self = shift;

    require OBM::Password::sambaPasswdUpdater;
    my $passwordUpdater = OBM::Password::sambaPasswdUpdater->new();
    if( $passwordUpdater->update( $self->{'userEntity'}, $self->{'newPasswordDesc'}->{'newPassword'} ) ) {
        $self->_log( 'problème a la mise à jour du mot de passe Samba', 0 );
        return 1;
    }

    return 0;
}
