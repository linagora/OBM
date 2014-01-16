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


package OBM::Update::updateIncremental;

$VERSION = '1.0';

use OBM::Update::update;
use OBM::Log::log;
@ISA = ('OBM::Update::update', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $parameters ) = @_;

    my $self = bless { }, $class;


    if( !defined($parameters) ) {
        $self->_log( 'Usage: PACKAGE->new(PARAMLIST)', 4 );
        return undef;
    }elsif( !exists($parameters->{'user'}) && !exists($parameters->{'domain-id'}) && !exists($parameters->{'delegation' }) ) {
        $self->_log( 'Usage: PARAMLIST: table de hachage avec la clé \'domain-id\' et optionnellement les cles \'user\' ou \'delegation\'', 4 );
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connecteur a la base de donnee invalide', 3 );
        return undef;
    }

    # Updater initialization
    $self->{'incremental'} = $parameters->{'incremental'};

    # Domain identifier
    if( defined($parameters->{'domain-id'}) ) {
        $self->{'domainId'} = $parameters->{'domain-id'};
    }else {
        $self->_log( 'Le parametre domain-id doit etre precise', 0 );
    }

    # User identifier
    if( defined($parameters->{'user'}) ) {
        $self->{'user'} = $parameters->{'user'};

        my $query = 'SELECT userobm_login FROM UserObm WHERE userobm_id='.$self->{'user'};
        my $queryResult;
        if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
            $self->_log( 'erreur lors de l\'exécution de la requête de vérification du paramètre \'--user\'', 0 );
            return undef;
        }

        ( $self->{'user_login'} ) = $queryResult->fetchrow_array();
        $queryResult->finish();

        if( !defined($self->{'user_login'}) ) {
            $self->_log( 'l\'utilisateur d\'identifiant \''.$self->{'user'}.' n\'existe pas', 0 );
            return undef;
        }
    }

    # Delegation
    if( defined($parameters->{'delegation'}) ) {
        $self->{'delegation'} = $parameters->{'delegation'};
    }


    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub _updatePreInit {
    my $self = shift;

    require OBM::incrementalTableUpdater;
    $self->_log( 'initialisation de l\'incrémental table updater', 4 );
    if( !($self->{'incrementalTableUpdater'} = OBM::incrementalTableUpdater->new( $self->{'domainId'}, $self->{'user'}, $self->{'delegation'} )) ) {
        $self->_log( 'echec de l\'initialisation de l\'incrémental table updater', 0 );
        return 1;
    }

    return 0;
}


sub _updateInitFactory {
    my $self = shift;

    require OBM::entitiesFactory;
    $self->_log( 'initialisation de l\'entity factory', 4 );
    if( !($self->{'entitiesFactory'} = OBM::entitiesFactory->new( 'INCREMENTAL', $self->{'domainId'}, $self->{'user'}, $self->{'delegation'} )) ) {
        $self->_log( 'echec de l\'initialisation de l\'entity factory', 0 );
        return 1;
    }

    return 0;
}


sub _updateEntityEndProcess {
    my $self = shift;
    my( $entity ) = @_;

    $self->{'incrementalTableUpdater'}->update($entity);
    return 0;
}


sub _updatePostUpdate {
    my $self = shift;

    my $incrTableUpdReturnCode = $self->{'incrementalTableUpdater'}->updateBd();
    if( $incrTableUpdReturnCode ) {
        $self->_log( 'erreur au nettoyage des tables BD du mode incrémental', 0 );
    }

    return $incrTableUpdReturnCode;
}
