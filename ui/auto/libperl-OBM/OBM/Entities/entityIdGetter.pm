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


package OBM::Entities::entityIdGetter;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
use strict;


sub _domainTable {
    my $self = shift;

    return 'Domain';
}


sub _userObmTable {
    my $self = shift;

    return 'UserObm';
}


sub _mailShareTable {
    my $self = shift;

    return 'MailShare';
}


sub _getUserIdFromUserLoginDomain {
    my $self = shift;
    my( $userLogin, $domainId ) = @_;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection à la base de données incorrecte !', 0 );
        return undef;
    }


    use OBM::Parameters::regexp;
    if( $userLogin !~ /$regexp_login/i ) {
        $self->_log( 'nom d\'utilisateur non specifié ou incorrect', 0 );
        return undef;
    }

    if( $domainId !~ /$regexp_id/ ) {
        $self->_log( 'Id BD de domaine non specifié ou incorrect', 0 );
        return undef;
    }

    my $query = 'SELECT userobm_id
                 FROM '.$self->_userObmTable().'
                 WHERE LOWER(userobm_login)='.$dbHandler->quote(lc($userLogin)).' AND userobm_domain_id='.$domainId;
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return undef;
    }

    my( $userId ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( defined($userId) ) {
        $self->_log( 'utilisateur \''.$userLogin.'\' du domaine d\'ID \''.$domainId.'\' a l\'ID BD '.$userId, 3 );
    }

    return $userId;
}


sub _getMailshareIdFromMailshareNameDomain {
    my $self = shift;
    my( $mailshareName, $domainId ) = @_;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection à la base de données incorrecte !', 0 );
        return undef;
    }


    use OBM::Parameters::regexp;
    if( $mailshareName !~ /$regexp_mailsharename/i ) {
        $self->_log( 'nom du mailshare non specifié ou incorrect', 0 );
        return undef;
    }

    if( $domainId !~ /$regexp_id/ ) {
        $self->_log( 'Id BD de domaine non specifié ou incorrect', 0 );
        return undef;
    }

    my $query = 'SELECT mailshare_id
                 FROM MailShare
                 WHERE LOWER(mailshare_name)='.$dbHandler->quote(lc($mailshareName)).' AND mailshare_domain_id='.$domainId;
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return undef;
    }

    my( $mailshareId ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( defined($mailshareId) ) {
        $self->_log( 'mailshare \''.$mailshareName.'\' du domaine d\'ID \''.$domainId.'\' a l\'ID BD '.$mailshareId, 3 );
    }

    return $mailshareId;
}


sub _getGroupIdFromGroupNameDomain {
    my $self = shift;
    my( $groupName, $domainId ) = @_;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection à la base de données incorrecte !', 0 );
        return undef;
    }


    use OBM::Parameters::regexp;
    if( $groupName !~ /$regexp_groupname/i ) {
        $self->_log( 'nom du groupe non specifié ou incorrect', 0 );
        return undef;
    }

    if( $domainId !~ /$regexp_id/ ) {
        $self->_log( 'Id BD de domaine non specifié ou incorrect', 0 );
        return undef;
    }

    my $query = 'SELECT group_id
                 FROM UGroup
                 WHERE group_name='.$dbHandler->quote(lc($groupName)).' AND group_domain_id='.$domainId;
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return undef;
    }

    my( $groupId ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( defined($groupId) ) {
        $self->_log( 'groupe \''.$groupName.'\' du domaine d\'ID \''.$domainId.'\' a l\'ID BD '.$groupId, 3 );
    }

    return $groupId;
}


sub _getHostIdFromHostNameDomain {
    my $self = shift;
    my( $hostName, $domainId ) = @_;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection à la base de données incorrecte !', 0 );
        return undef;
    }


    use OBM::Parameters::regexp;
    if( $hostName !~ /$regexp_hostname/i ) {
        $self->_log( 'nom de l\'hôte non specifié ou incorrect', 0 );
        return undef;
    }

    if( $domainId !~ /$regexp_id/ ) {
        $self->_log( 'Id BD de domaine non specifié ou incorrect', 0 );
        return undef;
    }

    my $query = 'SELECT host_id
                 FROM Host
                 WHERE LOWER(host_name)='.$dbHandler->quote(lc($hostName)).' AND host_domain_id='.$domainId;
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return undef;
    }

    my( $hostId ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( defined($hostId) ) {
        $self->_log( 'hôte \''.$hostName.'\' du domaine d\'ID \''.$domainId.'\' a l\'ID BD '.$hostId, 3 );
    }

    return $hostId;
}


sub getUserIdByDomainId {
    my $self = shift;
    my( $domainList ) = @_;

    if( defined($domainList) && ref($domainList) ne 'ARRAY' ) {
        $self->_log( 'paramètre \'domainList\' incorrect', 3 );
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection à la base de données incorrecte !', 0 );
        return undef;
    }

    my $query = 'SELECT userobm_id,
                        userobm_domain_id
                 FROM '.$self->_userObmTable();

    if( $domainList ) {
        $query .= ' WHERE userobm_domain_id IN ('.join(', ', @{$domainList}).')';
    }

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return undef;
    }

    my %usersByDomainId;
    while( my( $userId, $domainUserId ) = $queryResult->fetchrow_array() ) {
        push( @{$usersByDomainId{$domainUserId}}, $userId );
    }

    return \%usersByDomainId;
}


sub _getMailShareIdFromUserLoginDomain {
    my $self = shift;
    my( $mailShareName, $domainId ) = @_;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection à la base de données incorrecte !', 0 );
        return undef;
    }


    use OBM::Parameters::regexp;
    if( $mailShareName !~ /$regexp_mailsharename/ ) {
        $self->_log( 'nom de partage messagerie non specifié ou incorrect', 0 );
        return undef;
    }

    if( $domainId !~ /$regexp_id/ ) {
        $self->_log( 'Id BD de domaine non specifié ou incorrect', 0 );
        return undef;
    }

    my $query = 'SELECT mailshare_id
                 FROM '.$self->_mailShareTable().'
                 WHERE mailshare_name='.$dbHandler->quote(lc($mailShareName)).' AND mailshare_domain_id='.$domainId;
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return undef;
    }

    my( $mailShareId ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( defined($mailShareId) ) {
        $self->_log( 'utilisateur \''.$mailShareName.'\' du domaine d\'ID \''.$domainId.'\' a l\'ID BD '.$mailShareId, 3 );
    }

    return $mailShareId;
}


sub getDomainId {
    my $self = shift;
    my( $withGlobal, $domainName ) = @_;

    if( !defined($withGlobal) ) {
        $withGlobal = 1;
    }

    if( defined($domainName) && ref($domainName) !~ /$regexp_domain/ ) {
        $self->_log( 'paramètre \'domainName\' incorrect', 3 );
        return undef;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection à la base de données incorrecte !', 0 );
        return undef;
    }

    my $query = 'SELECT domain_id
                 FROM '.$self->_domainTable();

    my $whereClause = '';
    if( !$withGlobal ) {
        if( !$whereClause ) {
            $whereClause .= ' WHERE';
        }
        $whereClause .= ' NOT domain_global';
    }

    if( $domainName ) {
        if( !$whereClause ) {
            $whereClause .= ' WHERE';
        }else {
            $whereClause .= ' AND';
        }
        $whereClause .= ' domain_name=\''.$dbHandler->quote($domainName).'\'';
    }

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query.$whereClause, \$queryResult )) ) {
        return undef;
    }

    my @domainIds;
    while( my( $domainId ) = $queryResult->fetchrow_array() ) {
        push( @domainIds, $domainId );
    }

    return \@domainIds;
}


sub getDomainsId {
    my $self = shift;
    my( $withGlobal ) = @_;

    return $self->getDomainId( $withGlobal, undef );
}
