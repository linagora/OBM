package OBM::Update::commonUpdate;


$debug = 1;


use 5.006_001;
use strict;
use vars qw( @EXPORT_OK $VERSION );
use base qw(Exporter);

use OBM::Parameters::regexp; 


$VERSION = '1.0';
@EXPORT_OK = qw(    _getUserIdFromUserLoginDomain
               );


sub _getUserIdFromUserLoginDomain {
    my $self = shift;
    my( $userLogin, $domainId ) = @_;

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connection à la base de données incorrecte !', 0 );
        return undef;
    }


    require OBM::Parameters::regexp;
    if( $userLogin !~ /$regexp_login/ ) {
        $self->_log( 'nom d\'utilisateur non specifié ou incorrect', 0 );
        return undef;
    }

    if( $domainId !~ /$regexp_id/ ) {
        $self->_log( 'Id BD de domaine non specifié ou incorrect', 0 );
        return undef;
    }

    my $query = 'SELECT userobm_id
                 FROM UserObm
                 WHERE userobm_login='.$dbHandler->quote($userLogin).' AND userobm_domain_id='.$domainId;
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



