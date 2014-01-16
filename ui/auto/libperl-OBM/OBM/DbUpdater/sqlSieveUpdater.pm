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


package OBM::DbUpdater::sqlSieveUpdater;

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

    require OBM::Tools::obmDbHandler;
    if( !($self->{'dbHandler'} = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    return $self;
}


sub update {
    my $self = shift;
    my( $entity ) = @_;


    if( !defined($entity) ) {
        $self->_log( 'entité non définie', 1 );
        return 1;
    }elsif( !ref($entity) ) {
        $self->_log( 'entité incorrecte', 1 );
        return 1;
    }elsif( ref($entity) ne 'OBM::Entities::obmUser' ) {
        $self->_log( 'type d\'entité \''.ref($entity).' non supporté', 0 );
        return 1;
    }


    my $query = 'UPDATE P_UserObm
                    SET     userobm_nomade_perms = (SELECT userobm_nomade_perms FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_nomade_enable = (SELECT userobm_nomade_enable FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_nomade_local_copy = (SELECT userobm_nomade_local_copy FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_email_nomade = (SELECT userobm_email_nomade FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_vacation_enable = (SELECT userobm_vacation_enable FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_vacation_datebegin = (SELECT userobm_vacation_datebegin FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_vacation_dateend = (SELECT userobm_vacation_dateend FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_vacation_message = (SELECT userobm_vacation_message FROM UserObm WHERE userobm_id='.$entity->getId().')
                    WHERE userobm_id='.$entity->getId();

    my $sth;
    if( !defined($self->{'dbHandler'}->execQuery( $query, \$sth )) ) {
        $self->_log( 'échec de mise à jour des informations Sieve en BD', 1 );
        return 1;
    }

    $self->_log( 'mise à jour de la BD terminée avec succès', 1 );

    return 0;
}
