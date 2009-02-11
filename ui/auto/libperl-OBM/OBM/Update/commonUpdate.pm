package OBM::Entities::entityIdsGetter;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
use strict;


my $userObmTable = 'UserObm';


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
                 WHERE userobm_login='.$dbHandler->quote($userLogin).' AND userobm_domain_id='.$domainId;
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        $self->_log( 'utilisateur \''.$userLogin.'\' du domaine d\'ID \''.$domainId.'\' non existant', 3 );
        return undef;
    }

    my( $userId ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( defined($userId) ) {
        $self->_log( 'utilisateur \''.$userLogin.'\' du domaine d\'ID \''.$domainId.'\' a l\'ID BD '.$userId, 3 );
    }

    return $userId;
}
