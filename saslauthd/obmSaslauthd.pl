#!/usr/bin/perl -w -T
#+-------------------------------------------------------------------------+
#|   Copyright (c) 1997-2009 OBM.org project members team                  |
#|                                                                         |
#|  This program is free software; you can redistribute it and/or          |
#|  modify it under the terms of the GNU General Public License            |
#|  as published by the Free Software Foundation; version 2                |
#|  of the License.                                                        |
#|                                                                         |
#|  This program is distributed in the hope that it will be useful,        |
#|  but WITHOUT ANY WARRANTY; without even the implied warranty of         |
#|  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          |
#|  GNU General Public License for more details.                           | 
#+-------------------------------------------------------------------------+
#|  http://www.obm.org                                                     |
#+-------------------------------------------------------------------------+


package obmSaslauthd;

use ObmSaslauthd::Server::configure;
use ObmSaslauthd::Server::bind;
use ObmSaslauthd::Server::processRequest;
use Net::Server::PreForkSimple;
use ObmSaslauthd::Tools::commonMethods;
@ISA = qw(ObmSaslauthd::Server::configure ObmSaslauthd::Server::bind ObmSaslauthd::Server::processRequest Net::Server::PreForkSimple ObmSaslauthd::Tools::commonMethods);
use strict;


delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

obmSaslauthd->run();
exit;

$|=1;


sub log {
    my $self = shift;
    my( $level, $msg ) = @_;

    $self->SUPER::log( $level, $self->log_time.' ['.$$.']: '.$msg );
}
