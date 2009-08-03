package ObmSaslauthd::AuthMods::ldap;

$VERSION = '1.0';

use ObmSaslauthd::AuthMods::abstract;
@ISA = ('ObmSaslauthd::AuthMods::abstract');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use ObmSaslauthd::Ldap::ldapServer;


sub init {
    my $self = shift;

    my $daemonOptions = {
        ldap_server => [],
        ldap_server_tls => [],
        ldap_base => [],
        ldap_login => [],
        ldap_password => [],
        ldap_filter => []
        };
    $self->{'daemon'}->configure( $daemonOptions );

    $daemonOptions->{'ldap_filter'} = $self->{'daemon'}->loadOption( 'ldap_filter' );


    my $ldapDesc = {
        ldap_server => shift( @{$daemonOptions->{'ldap_server'}} ),
        ldap_server_tls => shift( @{$daemonOptions->{'ldap_server_tls'}} ),
        ldap_base => shift( @{$daemonOptions->{'ldap_base'}} ),
        ldap_login => shift( @{$daemonOptions->{'ldap_login'}} ),
        ldap_password => shift( @{$daemonOptions->{'ldap_password'}} ),
        ldap_filter => shift( @{$daemonOptions->{'ldap_filter'}} )
    };

    $self->{'ldapFilter'} = $ldapDesc->{'ldap_filter'};
    if( !$self->{'ldapFilter'} ) {
        $self->{'daemon'}->log( 0, 'Invalid LDAP filter' );
        return 1;
    }

    $self->{'ldapBase'} = $ldapDesc->{'ldap_base'};
    if( !defined($self->{'ldapBase'}) ) {
        $self->{'ldapBase'} = '';
    }


    $self->{'ldapServer'} = ObmSaslauthd::Ldap::ldapServer->new( $self->{'daemon'}, $ldapDesc );
    if( !defined($self->{'ldapServer'}) ) {
        return 1;
    }

    return 0;
}


sub checkAuth {
    my $self = shift;
    my( $request ) = @_;

    $request->setDn( $self->_searchDn( $request ) );
    if( !defined($request->getDn()) ) {
        return 0;
    }

    $self->{'daemon'}->log( 2, 'Checking password to LDAP server for user '.$request->getLogin().', DN '.$request->getDn() );
    return $self->{'ldapServer'}->checkAuthentication( $request );
}


sub _searchDn {
    my $self = shift;
    my( $request ) = @_;

    my $userLogin = $request->getLogin();
    my $userRealm = $request->getRealm();

    my $ldapFilter = $self->{'ldapFilter'};
    $ldapFilter =~ s/\%U/$userLogin/g;
    $ldapFilter =~ s/\%u/$userLogin\@$userRealm/g;
    $ldapFilter =~ s/\%d/$userRealm/g;

    my $ldapServerConn = $self->{'ldapServer'}->getConn();
    if( ref($ldapServerConn) ne 'Net::LDAP' ) {
        $self->{'daemon'}->log( 0, 'bad LDAP server connection' );
        return undef;
    }

    my $result = $ldapServerConn->search(
                    base => $self->{'ldapBase'},
                    scope => 'sub',
                    filter => $ldapFilter
                    );

    if( $result->is_error() && ($result->code != 32) ) {
        $self->{'daemon'}->log( 0, 'LDAP search fail with error code '.$result->code.', '.$result->error );
        return undef;
        
    }elsif( $result->count() > 1 ) {
        $self->{'daemon'}->log( 0, 'more than one LDAP entry match current search filter '.$ldapFilter );
        return undef;

    }elsif( $result->count() < 1 ) {
        $self->{'daemon'}->log( 2, 'no LDAP entry found matching current search filter '.$ldapFilter );
        return undef;
    }

    my $entry = $result->entry(0);
    return $entry->dn();
}
