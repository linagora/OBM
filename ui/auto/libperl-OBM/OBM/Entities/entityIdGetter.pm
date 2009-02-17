package OBM::Entities::entityIdGetter;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
use strict;


my $domain = 'Domain';
my $userObmTable = 'UserObm';
my $mailShareTable = 'MailShare';


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
    if( $userLogin !~ /$regexp_login/ ) {
        $self->_log( 'nom d\'utilisateur non specifié ou incorrect', 0 );
        return undef;
    }

    if( $domainId !~ /$regexp_id/ ) {
        $self->_log( 'Id BD de domaine non specifié ou incorrect', 0 );
        return undef;
    }

    my $query = 'SELECT userobm_id
                 FROM '.$userObmTable.'
                 WHERE userobm_login='.$dbHandler->quote(lc($userLogin)).' AND userobm_domain_id='.$domainId;
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
                 FROM '.$userObmTable;

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
                 FROM '.$mailShareTable.'
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
                 FROM '.$domain;

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
