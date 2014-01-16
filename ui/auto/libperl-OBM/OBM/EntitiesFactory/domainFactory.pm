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


package OBM::EntitiesFactory::domainFactory;

$VERSION = '1.0';

use OBM::EntitiesFactory::factory;
use OBM::Log::log;
@ISA = ('OBM::EntitiesFactory::factory', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::regexp;

use constant OBM_GLOBAL_DOMAIN_NAME => 'global.virt';


sub new {
    my $class = shift;
    my( $source, $domainId ) = @_;

    my $self = bless { }, $class;

    $self->{'source'} = $source;
    if( !$self->_checkSource() ) {
        return undef;
    }

    if( !defined($domainId) ) {
        $self->_log( 'identifiant de domaine non défini', 1 );
        return undef;
    }
    
    if( ref($domainId) || ($domainId !~ /$regexp_id/) ) {
        $self->_log( 'identifiant de domaine \''.$self->{'domainId'}.'\' incorrect', 1 );
        return undef;
    }

    $self->{'domainId'} = $domainId;
    $self->{'domains'} = undef;
    $self->{'running'} = undef;
    $self->{'currentEntity'} = undef;


    return $self;
}


sub _start {
    my $self = shift;

    $self->_log( 'debut de traitement', 4 );

    my $domain;
    if( !($domain = $self->_getDomain()) ) {
        $self->_log( 'problème à l\'obtention du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 1 );
        return 0;
    }

    if( !$domain->isGlobal && !$self->_getParentDomain( $domain ) ) {
        $self->_log( 'problème à l\'obtention des domaines pères du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 1 );
        return 0;
    }

    if( !$domain->isGlobal && !$domain->getParent() ) {
        $self->_log( 'problème lors du chargement du domaine père de '.$domain->getDescription(), 1 );
        $self->_log( 'le domaine père (global) doit être créé dans le système avant de pouvoir créer '.$domain->getDescription(), 2 );
        return 0;
    }


    $self->{'running'} = 1;
    return $self->{'running'};
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

    $self->{'currentEntity'} = shift( @{$self->{'domains'}} );

    return $self->{'currentEntity'};
}


sub _getDomain {
    my $self = shift;

    $self->_log( 'obtention du domaine d\'identifiant \''.$self->{'domainId'}.'\'', 3 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return undef;
    }

    my $domainTable = 'Domain';
    my $domainEntityTable = 'DomainEntity';
    my $servicePropertyTable = 'ServiceProperty';
    if( $self->{'source'} =~ /^SYSTEM$/ ) {
        $domainTable = 'P_'.$domainTable;
        $domainEntityTable = 'P_'.$domainEntityTable;
        $servicePropertyTable = 'P_'.$servicePropertyTable;
    }
    my $queryDomain = 'SELECT   domain_id,
                                domain_global,
                                domain_label,
                                domain_description,
                                domain_name,
                                domain_alias,
                                sambadomain.serviceproperty_value as samba_domain_name,
                                sid.serviceproperty_value as samba_sid,
                                profile.serviceproperty_value as samba_user_profile
                        FROM '.$domainTable.'
                        INNER JOIN '.$domainEntityTable.' ON domainentity_domain_id=domain_id
                        LEFT JOIN '.$servicePropertyTable.' sambadomain ON sambadomain.serviceproperty_entity_id=domainentity_entity_id AND sambadomain.serviceproperty_service=\'samba\' AND sambadomain.serviceproperty_property=\'domain\'
                        LEFT JOIN '.$servicePropertyTable.' sid ON sid.serviceproperty_entity_id=domainentity_entity_id AND sid.serviceproperty_service=\'samba\' AND sid.serviceproperty_property=\'sid\'
                        LEFT JOIN '.$servicePropertyTable.' profile ON profile.serviceproperty_entity_id=domainentity_entity_id AND profile.serviceproperty_service=\'samba\' AND profile.serviceproperty_property=\'profile\'
                        WHERE domain_id = '.$self->{'domainId'};

    my $sth;
    if( !defined( $dbHandler->execQuery( $queryDomain, \$sth ) ) ) {
        $self->_log( 'chargement des domaines depuis la BD impossible', 1 );
        return undef;
    }

    while( my $domaindesc = $sth->fetchrow_hashref() ) {
        if($domaindesc->{'domain_global'}) {
            $domaindesc->{'domain_name'} = OBM_GLOBAL_DOMAIN_NAME;
        }
        require OBM::Entities::obmDomain;
        my $domainEntity = OBM::Entities::obmDomain->new( $domaindesc );

        if( $self->{'source'} =~ /^SYSTEM$/ ) {
            $domainEntity->unsetBdUpdate();
        }else {
            $domainEntity->setBdUpdate();
        }

        unshift( @{$self->{'domains'}}, $domainEntity );
    }

    if( $#{$self->{'domains'}} > 0 ) {
        $self->_log( 'obtention de plusieurs domaines d\'identifiant \''.$self->{'domainId'}.'\'', 1 );
        return undef;
    }elsif( $#{$self->{'domains'}} < 0 ) {
        $self->_log( 'domaine d\'identifiant \''.$self->{'domainId'}.'\' non trouvé', 1 );
        return undef;
    }

    return $self->{'domains'}->[0];
}


sub _getParentDomain {
    my $self = shift;
    my( $childDomain ) = @_;

    $self->_log( 'obtention du domain global', 3 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 0;
    }

    my $domainTable = 'P_Domain';
    my $domainEntityTable = 'P_DomainEntity';
    my $servicePropertyTable = 'P_ServiceProperty';
    my $queryDomain = 'SELECT   domain_id,
                                domain_global,
                                domain_label,
                                domain_description,
                                \''.OBM_GLOBAL_DOMAIN_NAME.'\' as domain_name,
                                domain_alias,
                                sid.serviceproperty_value as samba_sid
                        FROM '.$domainTable.'
                        INNER JOIN '.$domainEntityTable.' ON domainentity_domain_id=domain_id
                        LEFT JOIN '.$servicePropertyTable.' sid ON sid.serviceproperty_entity_id=domainentity_entity_id AND sid.serviceproperty_service=\'samba\' AND sid.serviceproperty_property=\'sid\'
                        WHERE domain_global';


    my $sth;
    if( !defined( $dbHandler->execQuery( $queryDomain, \$sth ) ) ) {
        $self->_log( 'chargement des domaines depuis la BD impossible', 1 );
        return 0;
    }

    while( my $domaindesc = $sth->fetchrow_hashref() ) {
        require OBM::Entities::obmDomain;
        my $current = OBM::Entities::obmDomain->new( $domaindesc );

        if( $childDomain->setParent($current) ) {
            $self->_log( 'père de '.$current->getDescription().' incorrect', 1 );
            return 0;
        }

        $childDomain = $current;
    }


    return 1;
}
