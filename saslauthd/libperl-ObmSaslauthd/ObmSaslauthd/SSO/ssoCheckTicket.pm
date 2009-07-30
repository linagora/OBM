package ObmSaslauthd::SSO::ssoCheckTicket;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use LWP::UserAgent;
require HTTP::Request;


sub new {
    my $class = shift;
    my( $daemon, $ldapDesc ) = @_;

    my $self = bless { }, $class;

    $self->{'daemon'} = $daemon;
    $self->{'check_sso_uri'} = $ldapDesc->{'check_sso_uri'};
    if( !$self->{'check_sso_uri'} ) {
        $self->{'daemon'}->log( 0, 'Invalid SSO ticket checking URL' );
        return undef;
    }

    $self->{'userAgent'} = LWP::UserAgent->new();
    if( !defined($self->{'userAgent'}) ) {
        $self->{'daemon'}->log( 0, 'can\'t initialize HTTP user agent' );
        return undef;
    }

    return $self;
}


sub checkTicket {
    my $self = shift;
    my( $request ) = @_;

    my $userPasswd = $request->getPasswd();

    my $ssoRequest = $self->{'check_sso_uri'};
    $ssoRequest =~ s/\%t/$userPasswd/;

    my $response = $self->{'userAgent'}->request( HTTP::Request->new( 'GET', $ssoRequest ) );

    if( !$response->is_success() ) {
        $self->{'daemon'}->log( 0, 'HTTP request fail on error '.$response->status_line().', can\'t contact : '.$ssoRequest );
        return 0;
    }

    $self->{'daemon'}->log( 4, 'SSO server response : '.$response->content );
    return $self->_checkResponse( $request, $response->content() );
}


sub _checkResponse {
    my $self = shift;
    my( $request, $response ) = @_;

    if( $response !~ /^login=([^&]+)/ ) {
        $self->{'daemon'}->log( 2, 'Invalid ticket. SSO authentication fail for user : '.$request->getFullLogin() );
        return 0;
    }

    my $returnedFullLogin = $1;
    if( $1 ne $request->getFullLogin() ) {
        $self->{'daemon'}->log( 4, 'Invalid ticket. Returning login \''.$returnedFullLogin.'\' not same as requested login \''.$request->getFullLogin().'\'' );
        $self->{'daemon'}->log( 2, 'Invalid ticket. SSO authentication fail for user : '.$request->getFullLogin() );
        return 0;
    }

    $self->{'daemon'}->log( 2, 'SSO authentication success for user '.$request->getFullLogin() );
    return 1;
}
