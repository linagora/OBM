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


package OBM::Update::update;

$VERSION = '1.0';

use OBM::Log::log;
@ISA = ('OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub new {
    return undef;
}


sub update {
    my $self = shift;

    return 1 if $self->_updatePreInit();

    require OBM::dbUpdater;
    $self->_log( 'initialisation du BD updater', 4 );
    if( !($self->{'dbUpdater'} = OBM::dbUpdater->new()) ) {
        $self->_log( 'echec de l\'initialisation du BD updater', 0 );
        return 1;
    }

    return 1 if $self->_updateInitFactory();

    require OBM::Postfix::smtpInEngine;
    $self->_log( 'initialisation du SMTP-in maps updater', 4 );
    if( !($self->{'smtpInEngine'} = OBM::Postfix::smtpInEngine->new()) ) {
        $self->_log( 'echec de l\'initialisation du SMTP-in maps updater', 0 );
        return 1;
    }

    require OBM::Ldap::ldapEngine;
    $self->_log( 'initialisation du moteur LDAP', 4 );
    $self->{'ldapEngine'} = OBM::Ldap::ldapEngine->new();
    if( !defined($self->{'ldapEngine'}) ) {
        $self->_log( 'erreur à l\'initialisation du moteur LDAP', 0 );
        return 1
    }elsif( !ref($self->{'ldapEngine'}) ) {
        $self->_log( 'moteur LDAP non démarré', 4 );
        $self->{'ldapEngine'} = undef;
    }

    require OBM::Cyrus::cyrusEngine;
    $self->_log( 'initialisation du moteur Cyrus', 4 );
    $self->{'cyrusEngine'} = OBM::Cyrus::cyrusEngine->new();
    if( !defined($self->{'cyrusEngine'}) ) {
        $self->_log( 'erreur à l\'initialisation du moteur Cyrus', 0 );
        return 1
    }elsif( !ref($self->{'cyrusEngine'}) ) {
        $self->_log( 'moteur Cyrus non démarré', 4 );
        $self->{'cyrusEngine'} = undef;
    }

    require OBM::Cyrus::sieveEngine;
    $self->_log( 'initialisation du moteur Sieve', 4 );
    $self->{'sieveEngine'} = OBM::Cyrus::sieveEngine->new();
    if( !defined($self->{'sieveEngine'}) ) {
        $self->_log( 'erreur à l\'initialisation du moteur Sieve', 0 );
        return 1
    }elsif( !ref($self->{'sieveEngine'}) ) {
        $self->_log( 'moteur Sieve non démarré', 4 );
        $self->{'sieveEngine'} = undef;
    }


    while( my $entity = $self->{'entitiesFactory'}->next() ) {
        my $error = 0;
        $self->_log( 'traitement de '.$entity->getDescription(), 3 );

        if( !$error && defined($self->{'ldapEngine'}) ) {
            if($self->{'ldapEngine'}->update($entity)) {
                $self->_log( 'problème lors du traitement LDAP de l\'entité '.$entity->getDescription(), 1 );
                $error = 1;
            }
        }

        if( !$error && defined($self->{'cyrusEngine'}) ) {
            if($self->{'cyrusEngine'}->update($entity)) {
                $self->_log( 'problème lors du traitement Cyrus de l\'entité '.$entity->getDescription(), 1 );
                $error = 1;
            }
        }

        if( !$error && defined($self->{'sieveEngine'}) ) {
            if($self->{'sieveEngine'}->update($entity)) {
                $self->_log( 'problème lors du traitement Sieve de l\'entité '.$entity->getDescription(), 1 );
                $error = 1;
            }
        }

        if( !$error ) {
            $entity->setUpdated();
        }else {
            if( ref($entity) eq 'OBM::Entities::obmDomain' ) {
                $self->_log( 'problème à la mise à jour de '.$entity->getDescription(), 0 );
                return 1;
            }
        }

        if( $self->{'dbUpdater'}->update($entity) ) {
            $self->_log( 'problème à la mise à jour BD de l\'entité '.$entity->getDescription(), 1 );
            $entity->unsetUpdated();
        }else {
            $self->_log( 'entité '.$entity->getDescription().' mise à jour en BD', 4 );
        }

        $self->_updateEntityEndProcess($entity);

        if( $self->{'smtpInEngine'}->update($entity) ) {
            $self->_log( 'erreur fatale', 0 );
            return 1;
        }
    }

    my $smtpInReturnCode = $self->{'smtpInEngine'}->updateMaps();
    if( $smtpInReturnCode == 1 ) {
        $self->_log( 'annulation de la génération des maps SMTP-in', 3 );

    }elsif( $smtpInReturnCode == 2 ) {
        $self->_log( 'erreur lors de la mise à jour des maps SMTP-in', 0 );
        $self->_log( 'Il peut y avoir des incohérences dans le contenu des différents serveurs SMTP-in', 0 );
    }

    my $updateEnd = $self->_updatePostUpdate();

    return ($smtpInReturnCode || $updateEnd);
}


sub _updatePreInit {
    my $self = shift;

    return 0;
}


sub _updateInitFactory {
    my $self = shift;

    $self->_log( 'La factory d\'entité doit être initialisée', 0 );
    return 1;
}


sub _updateEntityEndProcess {
    my $self = shift;
    my( $entity ) = @_;

    return 0;
}


sub _updatePostUpdate {
    my $self = shift;

    return 0;
}
