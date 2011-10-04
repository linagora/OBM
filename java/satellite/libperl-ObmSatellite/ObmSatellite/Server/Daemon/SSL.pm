#
# This package derived almost entirely from HTTP::Daemon,
# owned by Gisle Aas.  Changes include minor alterations in
# the documentation to reflect the use of IO::Socket::SSL
# and modified new(),accept() functions that use IO::Socket::SSL

use strict;

package ObmSatellite::Server::Daemon::SSL;

=head1 NAME

HTTP::Daemon::SSL - a simple http server class with SSL support

# APR : Linagora
This perl module is include in ObmSatellite module for compatibility with
distribution on which it's not include (RH...)
ObmSatellite use distribution included module by default !

=head1 SYNOPSIS

  use HTTP::Daemon::SSL;
  use HTTP::Status;

  # Make sure you have a certs/ directory with "server-cert.pem"
  # and "server-key.pem" in it before running this!
  my $d = HTTP::Daemon::SSL->new || die;
  print "Please contact me at: <URL:", $d->url, ">\n";
  while (my $c = $d->accept) {
      while (my $r = $c->get_request) {
	  if ($r->method eq 'GET' and $r->url->path eq "/xyzzy") {
              # remember, this is *not* recommened practice :-)
	      $c->send_file_response("/etc/passwd");
	  } else {
	      $c->send_error(RC_FORBIDDEN)
	  }
      }
      $c->close;
      undef($c);
  }

=head1 DESCRIPTION

Instances of the I<HTTP::Daemon::SSL> class are HTTP/1.1 servers that
listen on a socket for incoming requests. The I<HTTP::Daemon::SSL> is a
sub-class of I<IO::Socket::SSL>, so you can perform socket operations
directly on it too.

The accept() method will return when a connection from a client is
available.  In a scalar context the returned value will be a reference
to a object of the I<HTTP::Daemon::ClientConn::SSL> class which is another
I<IO::Socket::SSL> subclass.  In a list context a two-element array
is returned containing the new I<HTTP::Daemon::ClientConn::SSL> reference
and the peer address; the list will be empty upon failure. (Note that version
 1.02 erroneously did not honour list context). Calling
the get_request() method on the I<HTTP::Daemon::ClientConn::SSL> object
will read data from the client and return an I<HTTP::Request> object
reference.

This HTTPS daemon does not fork(2) for you.  Your application, i.e. the
user of the I<HTTP::Daemon::SSL> is reponsible for forking if that is
desirable.  Also note that the user is responsible for generating
responses that conform to the HTTP/1.1 protocol.  The
I<HTTP::Daemon::ClientConn> class provides some methods that make this easier.

=head1 METHODS

The following methods are the only differences from the I<HTTP::Daemon> base class:

=over 4

=cut


use vars qw($VERSION @ISA $PROTO $DEBUG);

use IO::Socket::SSL;
use HTTP::Daemon;

$VERSION = "1.04";
@ISA = qw(IO::Socket::SSL HTTP::Daemon);

=item $d = new HTTP::Daemon::SSL

The constructor takes the same parameters as the
I<IO::Socket::SSL> constructor.  It can also be called without specifying
any parameters, but you will have to make sure that you have an SSL certificate
and key for the server in F<certs/server-cert.pem> and F<certs/server-key.pem>.
See the IO::Socket::SSL documentation for how to change these default locations
and specify many other aspects of SSL behavior. The daemon will then set up a
listen queue of 5 connections and allocate some random port number.  A server
that wants to bind to some specific address on the standard HTTPS port will be
constructed like this:

  $d = new HTTP::Daemon::SSL
        LocalAddr => 'www.someplace.com',
        LocalPort => 443;

=cut

sub new
{
    my ($class, %args) = @_;
    $args{Listen} ||= 5;
    $args{Proto} ||= 'tcp';
    $args{SSL_error_trap} ||= \&ssl_error;
    return $class->SUPER::new(%args);
}

sub accept
{
    my $self = shift;
    my $pkg = shift || "HTTP::Daemon::ClientConn::SSL";
	my ($sock, $peer) = IO::Socket::SSL::accept($self,$pkg);
    if ($sock) {
        ${*$sock}{'httpd_daemon'} = $self;
        return wantarray ? ($sock, $peer) : $sock;
    }
    else {
        return;
    }
}

sub _default_port { 443; }
sub _default_scheme { "https"; }

sub url
{
    my $self = shift;
    my $url = $self->SUPER::url;
    return $url if ($self->can("HTTP::Daemon::_default_port"));
    
    # Workaround for old versions of HTTP::Daemon
    $url =~ s!^http:!https:!;
    $url =~ s!/$!:80/! unless ($url =~ m!:(?:\d+)/$!);
    $url =~ s!:443/$!/!;
    return $url;
}


package ObmSatellite::Server::Daemon::SSL::DummyDaemon;
use vars qw(@ISA);
@ISA = qw(HTTP::Daemon);
sub new { bless [], shift; }

package ObmSatellite::Server::Daemon::SSL;

sub ssl_error {
    my ($self, $error) = @_;
    ${*$self}{'httpd_client_proto'} = 1000;
    ${*$self}{'httpd_daemon'} = new ObmSatellite::Server::Daemon::SSL::DummyDaemon;
    if ($error =~ /http/i and $self->opened) {
	$self->send_error(400, "Your browser attempted to make an unencrypted\n ".
		      "request to this server, which is not allowed.  Try using\n ".
		      "HTTPS instead.\n");
    }
    $self->kill_socket;
}

# we're not overriding any methods here, but we are inserting IO::Socket::SSL
# into the message dispatch tree

package HTTP::Daemon::ClientConn::SSL;
use vars qw(@ISA $DEBUG);
@ISA = qw(IO::Socket::SSL HTTP::Daemon::ClientConn);
*DEBUG = \$HTTP::Daemon::DEBUG;


=head1 SEE ALSO

RFC 2068

L<IO::Socket::SSL>, L<HTTP::Daemon>, L<Apache>

=head1 COPYRIGHT

Code and documentation from HTTP::Daemon Copyright 1996-2001, Gisle Aas
Changes Copyright 2003-2004, Peter Behroozi

This library is free software; you can redistribute it and/or
modify it under the same terms as Perl itself.

=cut

1;
