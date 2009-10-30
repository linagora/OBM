#!/usr/bin/perl -w -T

use strict;


sub _initLdapServer {
    my $ldapServers;

    use OBM::Parameters::common;
    if( !$OBM::Parameters::common::obmModules->{'ldap'} ) {
        print "LDAP engine not started. It's not an error, check 'obm-ldap' in '/etc/obm/obm_conf.ini' if you ever want to update OBM LDAP tree !";
        exit 0;
    }

    require OBM::Ldap::ldapServers;
    if( !($ldapServers = OBM::Ldap::ldapServers->instance()) ) {
        print "Fail to initialise LDAP servers\n";
        exit 1;
    }

    print "LDAP servers initialized successfully\n";
    return $ldapServers;
}


sub getLdapRoot {
    my $rootDnPrefix = '';

    my @root = split( /,/, $OBM::Parameters::common::ldapRoot );
    while( my $part = pop(@root) ) {
        if( $rootDnPrefix ) {
            $rootDnPrefix = ','.$rootDnPrefix;
        }

        $rootDnPrefix = 'dc='.$part.$rootDnPrefix;
    }

    return $rootDnPrefix;
}


sub updateServicesConfiguration {
    my( $ldapServers ) = @_;

    $ldapServer = $ldapServers->getLdapServerConn(0);
    my $result = $ldapServer->search(
        base => getLdapRoot(),
        scope => 'sub',
        filter => '(objectclass=obmMailServer)'
    );

    if( $result->code == 32 ) {
        # L'erreur 'No such object' n'est, dans ce cas, pas considérée comme
        # une erreur
        return 0;
    }elsif( $result->is_error() ) {
        print "LDAP search fail on error ".$result->code." ".$result->error."\n";
        return 1;
    }

    my @entries = $result->entries();
    my $errors = 0;

    for( my $i=0; $i<=$#entries; $i++ ) {
        my $currentDn = $entries[$i]->dn();

        my @dnPart = split( /,/, $currentDn );
        shift(@dnPart);
        my $newRdn = 'cn=mailServer';
        my $newDn = $newRdn.','.join( ',', @dnPart );

        print "Rename LDAP entry '".$currentDn."' to '".$newDn."'\n";
        $entries[$i]->add( newrdn => $newRdn );
        $entries[$i]->replace( deleteoldrdn => $currentDn );
        $entries[$i]->changetype( 'moddn' );
        $entries[$i]->replace(
            cn => 'mailServer'
        );

        my $updateResult = $entries[$i]->update( $ldapServer );
        if( $updateResult->is_error() ) {
            print "Updating LDAP entry '".$currentDn."' fail : ".$updateResult->code()." ".$updateResult->error()."\n";
            $errors++;
        }
    }

    return $errors;
}

my $ldapServers = _initLdapServer();

if( updateServicesConfiguration($ldapServers) ) {
    print "Update to OBM 2.3 FAIL !\n";
    exit 2;
}

print "Update to OBM 2.3 success !\n";
exit 0;
