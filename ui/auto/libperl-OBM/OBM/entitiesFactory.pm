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


package OBM::entitiesFactory;

$VERSION = '1.0';

use OBM::Log::log;
@ISA = ('OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $mode, $domainId, $userId, $delegation ) = @_;

    my $self = bless { }, $class;

    if( !defined($domainId) || ref($domainId) || ($domainId !~ /$regexp_id/) ) {
        $self->_log( 'un et un seul identifiant de domaine doit être indiqué', 0 );
        return undef;
    }

    $self->{'domainId'} = $domainId;
    $self->{'userId'} = $userId;
    $self->{'delegation'} = $delegation;

    $self->{'domain'} = undef;
    $self->{'entitiesQueue'} = undef;
    $self->{'factoriesQueue'} = undef;

    $self->{'mode'} = $mode;
    if( $self->_initMode() ) {
        $self->_log( 'problème à l\'initialisation de la factory', 0 );
        return undef;
    }

    $self->{'running'} = undef;

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );

    $self->_reset();
}


sub _reset {
    my $self = shift;

    $self->_log( 'factory reset', 4 );

    $self->{'running'} = undef;
    $self->{'domain'} = undef;
    $self->{'entitiesQueue'} = undef;
    $self->{'factoriesQueue'} = undef;
}


sub _initMode {
    my $self = shift;

    if( !defined($self->{'mode'}) ) {
        $self->_log( 'mode d\'exécution non définit', 1 );
        return 1;
    }

    if( ref($self->{'mode'}) ) {
        $self->_log( 'mode d\'exécution incorrect', 1 );
        return 1;
    }


    if( $self->{'mode'} =~ /^GLOBAL$/ ) {
        $self->_log( 'mode d\'exécution global', 3 );
        return $self->_initGlobal();
    }

    if( $self->{'mode'} =~ /^INCREMENTAL$/ ) {
        $self->_log( 'mode d\'exécution incrémental', 3 );
        return $self->_initIncremental();
    }

    if( $self->{'mode'} =~ /^PROGRAMMABLEWITHOUTDOMAIN$/ ) {
        $self->_log( 'mode d\'exécution programmable', 3 );
        return $self->_initProgrammable();
    }

    if( $self->{'mode'} =~ /^PROGRAMMABLE$/ ) {
        $self->_log( 'mode d\'exécution programmable', 3 );
        return $self->_initProgrammable( 1 );
    }


    $self->_log( 'mode d\'exécution \''.$self->{'mode'}.'\' incorrect', 0 );

    return 1;
}


sub enqueueFactory {
    my $self = shift;
    my( $factory ) = @_;

    if( !defined($factory) || ref($factory) !~ /^OBM::EntitiesFactory::/ ) {
        $self->_log( 'factory incorrecte, ajout dans la file d\'attente impossible', 1 );
        return 1;
    }

    push( @{$self->{'factoriesQueue'}}, $factory );

    return 0;
}


sub enqueueEntity {
    my $self = shift;
    my( $entity ) = @_;

    if( !defined($entity) || ref($entity) !~ /^OBM::Entities::/ ) {
        $self->_log( 'entity incorrecte, ajout dans la file d\'attente impossible', 1 );
        return 1;
    }

    push( @{$self->{'entitiesQueue'}}, $entity );

    return 0;
}


sub _initGlobal {
    my $self = shift;

    # For any update mode, domains must be loaded
    require OBM::EntitiesFactory::domainFactory;
    my $entityFactory = OBM::EntitiesFactory::domainFactory->new( 'WORK', $self->{'domainId'} );

    if( $self->_loadDomains( $entityFactory, 1 ) ) {
        return 1;
    }


    require OBM::EntitiesFactory::globalFactories;
    my $globalFactories = OBM::EntitiesFactory::globalFactories->new( $self );
    if( !defined($globalFactories) ) {
        $self->_log( 'problème à l\'initialisation des factories nécessaire pour le mode global', 0 );
        return 1;
    }

    return 0;
}


sub _initIncremental {
    my $self = shift;

    require OBM::EntitiesFactory::domainFactory;
    my $entityFactory = OBM::EntitiesFactory::domainFactory->new( 'SYSTEM', $self->{'domainId'} );

    if( $self->_loadDomains( $entityFactory, 1 ) ) {
        return 1;
    }

    require OBM::EntitiesFactory::incrementalFactories;
    my $incrementalFactories = OBM::EntitiesFactory::incrementalFactories->new( $self );
    if( !defined($incrementalFactories) ) {
        $self->_log( 'problème à l\'initialisation des factories nécessaire pour le mode incrémental', 0 );
        return 1;
    }

    return 0;
}


sub _initProgrammable {
    my $self = shift;
    my( $withDomain ) = @_;

    require OBM::EntitiesFactory::domainFactory;
    my $entityFactory = OBM::EntitiesFactory::domainFactory->new( 'SYSTEM', $self->{'domainId'} );

    if( $self->_loadDomains( $entityFactory, $withDomain ) ) {
        return 1;
    }

    return 0;
}


sub loadEntities {
    my $self = shift;
    my( $programmingObj ) = @_;

    if( ref($programmingObj) ne 'OBM::EntitiesFactory::factoryProgramming' ) {
        $self->_log( 'liste d\'entités à charger incorrecte', 1 );
        return 1;
    }

    require OBM::EntitiesFactory::programmableFactories;
    my $programmableFactories = OBM::EntitiesFactory::programmableFactories->new( $self );
    if( !defined($programmableFactories) ) {
        return 1;
    }

    if( $programmableFactories->addEntities($programmingObj) ) {
        $self->_log( 'problème lors de la programmation de la factory', 0 );
        return 1;
    }

    return 0;
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

    $self->{'running'} = 1;
    return $self->{'running'};
}


sub next {
    my $self = shift;

    $self->_log( 'obtention de l\'entité suivante', 4 );

    if( !$self->isRunning() ) {
        if( !$self->_start() ) {
            return undef;
        }
    }

    if( my $currentEntity = shift( @{$self->{'entitiesQueue'}} ) ) {
        return $currentEntity;
    }

    while( my $currentFactory = shift( @{$self->{'factoriesQueue'}} ) ) {
        if( my $currentEntity = $currentFactory->next() ) {
            unshift( @{$self->{'factoriesQueue'}}, $currentFactory );
            return $currentEntity;
        }
    }

    return undef;
}


sub _loadDomains {
    my $self = shift;
    my( $domainFactory, $enqueueDomain ) = @_;

    if( !defined($domainFactory) || ref($domainFactory) ne 'OBM::EntitiesFactory::domainFactory' ) {
        $self->_log( 'problème au chargement de la factory de domaine', 1 );
        return 1;
    }

    while( my $currentDomain = $domainFactory->next() ) {
        SWITCH: {
            if( $currentDomain->getId() == $self->{'domainId'} ) {
                $self->{'domain'} = $currentDomain;
            }

            if( $enqueueDomain ) {
                $self->enqueueEntity( $currentDomain );
            }
        }
    }

    if( !defined($self->{'domain'}) || (ref($self->{'domain'}) ne 'OBM::Entities::obmDomain') ) {
        $self->_log( 'domain d\'identifiant '.$self->{'domainId'}.' non trouvé', 0 );
        return 1;
    }

    return 0;
}


sub getDomainEntity {
    my $self = shift;

    return $self->{'domain'};
}
