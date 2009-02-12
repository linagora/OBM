package OBM::Entities::entityIdGetter;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
use strict;


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
        $self->_log( 'partage de messagerie \''.$mailShareName.'\' du domaine d\'ID \''.$domainId.'\' non existant', 3 );
        return undef;
    }

    my( $mailShareId ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( defined($mailShareId) ) {
        $self->_log( 'utilisateur \''.$mailShareName.'\' du domaine d\'ID \''.$domainId.'\' a l\'ID BD '.$mailShareId, 3 );
    }

    return $mailShareId;
}
