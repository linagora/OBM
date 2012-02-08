package ObmSatellite::Modules::Postfix::ldap;

=head1 NAME

ObmSatellite::Modules::Postfix::ldap - queries the LDAP for the mailbox server
of a single user.

=cut

use strict;
use warnings;

use 5.006_001;
use base qw/Exporter/;

use ObmSatellite::Log::log;

our @ISA = qw(ObmSatellite::Log::log);
our $VERSION = '1.0';

sub new {
    my ($klass, $ldapService) = @_;
    bless {
        ldapService     => $ldapService,
        attributes      => ['mailBoxServer'],
        filterPattern   => '(&(uid=%u)(objectClass=obmUser)(mailAccess=PERMIT)(obmDomain=%d))',
    }, $klass;
}

sub query {
    my ($self, $login, $domain) = @_;

    my ($attributes, $filter) = $self->_buildQuery($login, $domain);
    my $results = $self->_executeQuery($attributes, $filter, $login);
    return $results;
}

sub _buildQuery {
    my ($self, $login, $domain) = @_;

    my $attributes = $self->{attributes};
    my $filter = $self->{filterPattern};
    $filter =~ s!%u!$login!g;
    $filter =~ s!%d!$domain!g;

    return ($attributes, $filter);
}

sub _executeQuery {
    my ($self, $attributes, $filter, $login) = @_;

    my $ldapRoot = $self->{ldapService}->getLdapRoot();
    my $ldapConnection =  $self->{ldapService}->getConn() or return 0;

    my $ldapResult;
    if (defined $ldapRoot) {
        $self->_log( "Searching LDAP root ".$self->{ldapRoot}.", filter $filter", 5 );
        $ldapResult = $ldapConnection->search(
                            base    => $ldapRoot,
                            scope   => 'sub',
                            filter  => $filter,
                            attrs   => $attributes
                        );
    } else {
        $self->_log( "Searching default LDAP server root, filter $filter", 5 );
        $ldapResult = $ldapConnection->search(
                            scope   => 'sub',
                            filter  => $filter,
                            attrs   => $attributes
                        );
    }

    if( $ldapResult->is_error() ) {
        $self->_log( 'LDAP search failed with error : '.$ldapResult->error(), 1 );
        return undef;
    }

    my @results = $ldapResult->entries();
    if (!@results) {
        $self->_log( "No record for the user $login", 1 );
        return undef;
    }
    elsif (@results > 1) {
        $self->_log("Too many records for the user $login", 1);
        return undef;
    }
    my ($mailboxServer) = $results[0]->get_value('mailBoxServer');

    return $mailboxServer;
}

1;
