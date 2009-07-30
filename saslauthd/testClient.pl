#!/usr/bin/perl -w
use Socket;
use strict;
my ($rendezvous, $line);

$rendezvous = shift || '/tmp/catsock';
socket(SOCK, PF_UNIX, SOCK_STREAM, 0)   || die "socket: $!";
connect(SOCK, sockaddr_un($rendezvous)) || die "connect: $!";

send( SOCK, 'popoppop'."\r\n", 0 );

while (defined($line = <SOCK>)) {
    print $line;
}
exit;
