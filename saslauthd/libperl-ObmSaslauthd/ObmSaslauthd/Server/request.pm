package ObmSaslauthd::Server::request;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use Carp;
use strict;


sub new {
    my $class = shift;
    my( $daemon ) = @_;

    my $self = bless { }, $class;

    if( !ref($daemon) ) {
        return undef;
    }

    $self->{'daemon'} = $daemon;
    $self->{'client'} = $daemon->{'server'}->{'client'};

    $self->{'login'} = $self->get_counted_string();
    $self->{'passwd'} = $self->get_counted_string();
    $self->{'service'} = lc($self->get_counted_string());
    $self->{'realm'} = $self->get_counted_string();

    $daemon->log( 4, 'Request: login:'.$self->{'login'}.' password:'.$self->{'passwd'}.' service:'.$self->{'service'}.' realm:'.$self->{'realm'} );

    return $self;
}


sub get_counted_string {
    my $self = shift;
    my $fh = $self->{'client'};
    my ($rd, $data);

    if( ($rd = sysread($fh, $data, 2)) != 2 ) {
        $self->{'daemon'}->log( 0, "Unable to read counted string size ($rd != 2) ($!)" );
        confess "Unable to read counted string size ($rd != 2) ($!)";
    }

    my $size = unpack("n", $data);

    $data = ''; $rd = 0; my $this_data = ''; my $rem_size = $size;
    while (my $this_rd = sysread($fh, $this_data, $rem_size)) {
        $rd += $this_rd;
        $rem_size -= $this_rd;
        $data .= $this_data;
    }

    unless( $rd == $size ) {
        $self->{'daemon'}->log( 0, "Unable to read counted string data ($rd != $size) ($!)" );
        confess "Unable to read counted string data ($rd != $size) ($!)";
    }

    return unpack("A$size", $data);
}


sub getLogin {
    my $self = shift;

    return $self->{'login'};
}


sub getPasswd {
    my $self = shift;

    return $self->{'passwd'};
}


sub getService {
    my $self = shift;

    return $self->{'service'};
}


sub getRealm {
    my $self = shift;

    return $self->{'realm'};
}


sub setDn {
    my $self = shift;
    my( $dn ) = @_;

    $self->{'ldap_dn'} = $dn;

    return 1;
}


sub getDn {
    my $self = shift;

    return $self->{'ldap_dn'};
}
