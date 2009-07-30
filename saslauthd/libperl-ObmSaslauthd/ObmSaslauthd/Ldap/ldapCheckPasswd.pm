package ObmSaslauthd::Ldap::ldapCheckPasswd;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use ObmSaslauthd::Ldap::ldapServer;


sub new {
    my $class = shift;
    my( $daemon, $ldapDesc ) = @_;

    my $self = bless { }, $class;

    $self->{'daemon'} = $daemon;
    $self->{'ldapFilter'} = $ldapDesc->{'ldap_filter'};
    if( !$self->{'ldapFilter'} ) {
        $self->{'daemon'}->log( 0, 'Invalid LDAP filter' );
        return undef;
    }

    $self->{'ldapBase'} = $ldapDesc->{'ldap_base'};
    if( !defined($self->{'ldapBase'}) ) {
        $self->{'ldapBase'} = '';
    }


    $self->{'ldapServer'} = ObmSaslauthd::Ldap::ldapServer->new( $self->{'daemon'}, $ldapDesc );
    if( !defined($self->{'ldapServer'}) ) {
        return undef;
    }

    return $self;
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


sub checkPasswd {
    my $self = shift;
    my( $request ) = @_;

    $request->setDn( $self->_searchDn( $request ) );
    if( !defined($request->getDn()) ) {
        return 0;
    }

    $self->{'daemon'}->log( 2, 'Checking password to LDAP server for user '.$request->getLogin().', DN '.$request->getDn() );
    return $self->{'ldapServer'}->checkAuthentication( $request );
}
