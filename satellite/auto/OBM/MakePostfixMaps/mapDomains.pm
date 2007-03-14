package OBM::MakePostfixMaps::mapDomains;

require Exporter;

use strict;


sub makeDomainsMap {
    my( $daemonRef, $mailBoxMapDesc, $obmDomains ) = @_;
    my $ldapAttributes = $mailBoxMapDesc->{ldap_attibute};
    my %mapEntries;


    if( !defined($obmDomains) || ( ref($obmDomains) ne "ARRAY" ) ) {
        return 1;
    }

    if( !defined($mailBoxMapDesc->{ldap_filter}) || ( $mailBoxMapDesc->{ldap_filter} !~ /<obmDomain>/ ) ) {
        return 1;
    }

    for( my $i=0; $i<=$#{$obmDomains}; $i++ ) {
        $daemonRef->logMessage( "Obtention des informations du domaine : '".$obmDomains->[$i]."'" );

        my $ldapFilter = $mailBoxMapDesc->{ldap_filter};
        $ldapFilter =~ s/<obmDomain>/$obmDomains->[$i]/;

        my @ldapEntries;
        if( &OBM::MakePostfixMaps::utils::ldapSearch( $daemonRef->{ldap_server}, \@ldapEntries, $ldapFilter, $ldapAttributes ) ) {
            $daemonRef->logMessage( "Echec: lors de l'obtention des informations du domaine '".$obmDomains->[$i]."'" ) ;
            return 1;
        }

        for( my $j=0; $j<=$#ldapEntries; $j++ ) {
            my $entryDomains = $ldapEntries[$j]->get_value( $ldapAttributes->[0], asref => 1 );
            if( $#{$entryDomains} == -1 ) {
                next;
            }

            for( my $k=0; $k<=$#{$entryDomains}; $k++ ) {
                $mapEntries{$entryDomains->[$k]} = "OK";
            }
        }
    }

    return &OBM::MakePostfixMaps::utils::writeMap( $mailBoxMapDesc->{postfix_map}, $mailBoxMapDesc->{postfix_map_separator}, \%mapEntries );

}
