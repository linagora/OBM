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


package OBM::EntitiesFactory::obmSettingsFactory;

$VERSION = '1.0';

use OBM::EntitiesFactory::factory;
use OBM::Log::log;
@ISA = ('OBM::EntitiesFactory::factory', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $updateType, $parentDomain ) = @_;

    my $self = bless { }, $class;

    $self->{'updateType'} = $updateType;
    if( !$self->_checkUpdateType() ) {
        return undef;
    }

    if( !defined($parentDomain) ) {
        $self->_log( 'description du domaine père indéfini', 1 );
        return undef;
    }

    if( ref($parentDomain) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'description du domaine père incorrecte', 1 );
        return undef;
    }
    $self->{'parentDomain'} = $parentDomain;
    
    $self->{'domainId'} = $parentDomain->getId();
    if( ref($self->{'domainId'}) || ($self->{'domainId'} !~ /$regexp_id/) ) {
        $self->_log( 'identifiant de domaine \''.$self->{'domainId'}.'\' incorrect', 1 );
        return undef;
    }

    $self->{'running'} = undef;
    $self->{'currentEntity'} = undef;
    $self->{'entitiesDescList'} = undef;


    return $self;
}


sub next {
    my $self = shift;

    $self->_log( 'obtention de l\'entité suivante', 4 );

    if( !$self->isRunning() ) {
        if( !$self->_start() ) {
            $self->_reset();
            return undef;
        }
    }

    while( defined($self->{'entitiesDescList'}) && (my $obmSettingsDesc = $self->{'entitiesDescList'}->fetchall_arrayref({})) ) {
        require OBM::Entities::obmObmSettings;
        if( $self->{'currentEntity'} = OBM::Entities::obmObmSettings->new( $self->{'parentDomain'}, $obmSettingsDesc ) ) {
            $self->_log( 'mise à jour de l\'entité et des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
            $self->{'currentEntity'}->setUpdateEntity();
            $self->{'currentEntity'}->setUpdateLinks();

            $self->{'entitiesDescList'} = undef;
            return $self->{'currentEntity'};
        }
    }

    $self->{'currentEntity'} = undef;

    return undef;
}


sub _loadEntities {
    my $self = shift;

    $self->_log( 'chargement de la configuration OBM du domaine '.$self->{'parentDomain'}->getDescription().'\'', 3 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 1;
    }

    my $query = 'SELECT \'lang\' as setting,
                        userobmpref_value as value
                 FROM UserObmPref
                 WHERE userobmpref_user_id is null
                    AND userobmpref_option=\'set_lang\'';

    if( !defined($dbHandler->execQuery( $query, \$self->{'entitiesDescList'} )) ) {
        $self->_log( 'chargement de la configuration OBM depuis la BD impossible', 1 );
        return 1;
    }

    return 0;
}
