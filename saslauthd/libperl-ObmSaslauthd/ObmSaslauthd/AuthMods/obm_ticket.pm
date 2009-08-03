package ObmSaslauthd::AuthMods::obm_ticket;

$VERSION = '1.0';

use ObmSaslauthd::AuthMods::abstract;
@ISA = ('ObmSaslauthd::AuthMods::abstract');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use LWP::UserAgent;
require HTTP::Request;


sub init {
    my $self = shift;

    my $daemonOptions;
    $daemonOptions->{'check_sso_uri'} = $self->{'daemon'}->loadOption('check_sso_uri');

    my $ssoDesc = {
        check_sso_uri => shift( @{$daemonOptions->{'check_sso_uri'}} )
    };

    $self->{'check_sso_uri'} = $ssoDesc->{'check_sso_uri'};
    if( !$self->{'check_sso_uri'} ) {
        $self->{'daemon'}->log( 0, 'Invalid SSO ticket checking URL' );
        return 1;
    }

    $self->{'userAgent'} = LWP::UserAgent->new();
    if( !defined($self->{'userAgent'}) ) {
        $self->{'daemon'}->log( 0, 'can\'t initialize HTTP user agent' );
        return 1;
    }

    return 0;
}


sub checkAuth {
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
