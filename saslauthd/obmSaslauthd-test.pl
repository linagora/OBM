#!/usr/bin/perl -w -T

package obmSaslauthd;

use ObmSaslauthd::SocketServer::socketServer;
@ISA = ('ObmSaslauthd::SocketServer::socketServer');
use strict;

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

#my $server = ObmSaslauthd::SocketServer::socketServer->new();
#$server->run();

obmSaslauthd->run();
exit;

$|=1;
