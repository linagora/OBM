package ObmSatellite::mapTransportSmtp;

require Exporter;
use strict;


sub makeTransportSmtpMap {
    my( $daemonRef, $transportSmtpMapFile, $obmDomains ) = @_;
    my %mapEntries;


    if( !defined($obmDomains) || ( ref($obmDomains) ne "ARRAY" ) ) {
        return 1;
    }

    if( !defined($transportSmtpMapFile->{ldap_filter}) || ( $transportSmtpMapFile->{ldap_filter} !~ /<obmDomain>/ ) ) {
        return 1;
    }

    for( my $i=0; $i<=$#{$obmDomains}; $i++ ) {
        my @ldapEntries;

        my $ldapFilter = "(&(objectClass=obmMailServer)(obmDomain=".$obmDomains->[$i]."))";
        my $ldapAttributes = [ "smtpInHost" ];
        if( &ObmSatellite::utils::ldapSearch( $daemonRef->{ldap_server}, \@ldapEntries, $ldapFilter, $ldapAttributes ) ) {
            $daemonRef->logMessage( "Echec: lors de l'obtention des serveurs SMTP-in du domaine '".$obmDomains->[$i]."'" );
            return 1;
        }

        # Obtention d'un hôte SMTP-in du domaine
        my $smtpInHost;
        if( $#ldapEntries == 0 ) {
            my $smtpInHosts = $ldapEntries[0]->get_value( $ldapAttributes->[0], asref => 1 );

            if( $#{$smtpInHosts} < 0 ) {
                $daemonRef->logMessage( "Pas d'hote SMTP-in associe au domaine '".$obmDomains->[$i]."'" );
                next;
            }else {
                $smtpInHost = $smtpInHosts->[0];
                $daemonRef->logMessage( "L'hote '".$smtpInHost."' est serveur SMTP-in pour le domaine '".$obmDomains->[$i]."'" );
            }

        }else {
            $daemonRef->logMessage( "Description LDAP de la configuration Postfix pour le domaine '".$obmDomains->[$i]."' absente ou invalide" );
            next;
        }

        # Obtention de l'IP de l'hôte SMTP-in
        $ldapFilter = "(&(objectClass=obmHost)(cn=$smtpInHost))";
        $ldapAttributes = [ "ipHostNumber" ];
        if( &ObmSatellite::utils::ldapSearch( $daemonRef->{ldap_server}, \@ldapEntries, $ldapFilter, $ldapAttributes ) ) {
            $daemonRef->logMessage( "Echec: lors de l'obtention de l'adresse IP de l'hote '".$smtpInHost."'" );
            return 1;
        }

        my $smtpInHostIp;
        if( $#ldapEntries == 0 ) {
            my $smtpInHostIps = $ldapEntries[0]->get_value( $ldapAttributes->[0], asref => 1 );

            if( $#{$smtpInHostIps} < 0 ) {
                $daemonRef->logMessage( "Pas d'adresse IP associee a l'hote '".$smtpInHost."'" );
                next;
            }else {
                $smtpInHostIp = $smtpInHostIps->[0];
                $daemonRef->logMessage( "L'hote '".$smtpInHost."' a l'IP '".$smtpInHostIp."'" );
            }

        }else {
            $daemonRef->logMessage( "Description LDAP de l'hote '".$smtpInHost."' du domaine '".$obmDomains->[$i]."' absente ou invalide" );
            next;
        }


        $daemonRef->logMessage( "Obtention des adresses mails du domaine '".$obmDomains->[$i]."'" );

        $ldapFilter = $transportSmtpMapFile->{ldap_filter};
        $ldapFilter =~ s/<obmDomain>/$obmDomains->[$i]/;
        $ldapAttributes = $transportSmtpMapFile->{ldap_attibute};
        if( &ObmSatellite::utils::ldapSearch( $daemonRef->{ldap_server}, \@ldapEntries, $ldapFilter, $ldapAttributes ) ) {
            $daemonRef->logMessage( "Echec: lors de l'obtention des informations du domaine '".$obmDomains->[$i]."'" ) ;
            return 1;
        }

        for( my $j=0; $j<=$#ldapEntries; $j++ ) {
            for( my $k=0; $k<=$#{$ldapAttributes}; $k++ ) {
                my $entryAlias = $ldapEntries[$j]->get_value( $ldapAttributes->[$k], asref => 1 );

                for( my $l=0; $l<=$#{$entryAlias}; $l++ ) {
                    $mapEntries{$entryAlias->[$l]} = "smtp:[".$smtpInHostIp."]";
                }
            }
	}
    }

    return &ObmSatellite::utils::writeMap( $transportSmtpMapFile->{postfix_map}, $transportSmtpMapFile->{postfix_map_separator}, \%mapEntries );
}
