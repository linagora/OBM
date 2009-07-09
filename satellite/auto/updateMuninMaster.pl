#!/usr/bin/perl -w
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

#####################################################################
# OBM               - File : updateMuninMaster.pl                   #
#                   - Desc : Update /etc/munin/munin.conf		    #
#                                 									#
#####################################################################

package updateMuninMaster;

use OBM::Tools::commonMethods qw(
  _log
  dump
);
use Data::Dumper;

my $conffile = '/etc/munin/munin.conf';

my $muninObmComment = "#This is generate by OBM\n";
my $muninDbDir = "dbdir\t/var/lib/munin\n";
my $muninHtmlDir = "htmldir\t/var/www/munin\n";
my $muninLogDir = "logdir\t/var/log/munin\n";
my $muninRunDir = "rundir\t/var/run/munin\n";
my $muninTplDir = "rundir\t/etc/munin/templates\n";

my $muninConf = $muninObmComment.$muninDbDir.$muninHtmlDir.$muninLogDir.$muninRunDir.$muninTplDir;

sub getHost(){

	require OBM::Tools::obmDbHandler;
	my $dbHandler = OBM::Tools::obmDbHandler->instance();

	if ( !$dbHandler ) {
		$self->_log( 'connexion à la base de données impossible', 4 );
		return undef;
	}

	my $query = 'SELECT DISTINCT host_fqdn, host_ip, host_id, hostentity_host_id, hostentity_entity_id, service_entity_id from host
					LEFT JOIN hostentity ON host_id=hostentity_host_id
					LEFT JOIN service ON hostentity_entity_id=service_entity_id
					WHERE service_service = \'monitor\' 
						AND host_fqdn != \'\' AND host_ip != \'\'
						AND host_ip is not null AND host_fqdn is not null';
              
	my $result;             
	if( !defined($dbHandler->execQuery( $query, \$result )) ) {
    	    $self->_log( 'chargement des hosts depuis la BD impossible', 3 );
        	return undef;
    }

	my %hostByFqdn;
	while( my( $hostFqdn, $hostIp ) = $result->fetchrow_array() ) {
		$hostByFqdn{$hostFqdn} = $hostIp;
	}
	return \%hostByFqdn;
}

sub writeConf(){
    my ($hostBytest) = @_;
	while ( my ($key, $value) = each(%$hostBytest) ) {
    	    $muninConf .= "[".$key."]\n";
        	$muninConf .= "\tadress ".$value."\n";
        	$muninConf .= "\tuse_node_name yes\n";
	}
	open (MUNINCONF, ">$conffile");
	print MUNINCONF $muninConf;
	close (MUNINCONF);
}

my $host = &getHost();
&writeConf($host);

exit 0;
