package OBM::Ldap::typePostfixConf;

require Exporter;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
require OBM::passwd;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;


sub initStruct {
    return 1;
}


sub getDbValues {
    my( $parentDn, $domainId ) = @_;
    my $currentDomain = $main::domainList->[$domainId];


    if( !defined($currentDomain->{"domain_id"}) ) {
        &OBM::toolBox::write_log( "Identifiant de domaine non définie", "W" );
        return undef;
    }

    # On se connecte a la base
#    my $dbHandler;
#    if( !&OBM::dbUtils::dbState( "connect", \$dbHandler ) ) {
#        &OBM::toolBox::write_log( "Probleme lors de l'ouverture de la base de donnee : ".$dbHandler->err, "W" );
#        return undef;
#    }

    # La requete a executer - obtention des informations sur les utilisateurs de
    # l'organisation.
    my $query = "";

    # On execute la requete
#    my $queryResult;
#    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
#        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete des utilisateurs : ".$dbHandler->err, "W" );
#        return undef;
#    }

    # On range les resultats dans la structure de donnees des resultats
#    my $i = 0;
#    while( my( $user_id, $user_login, $user_passwd_type, $user_passwd, $user_uid, $user_gid, $user_lastname, $user_firstname, $user_address1, $user_address2, $user_address3, $user_zipcode, $user_town, $user_title, $user_service, $user_description, $user_mail_perms, $user_mail_ext_perms, $user_email, $user_mail_server_id, $user_web_perms, $user_phone, $user_phone2, $user_fax, $user_fax2, $user_mobile, $user_nomade_perms, $user_nomade_enable, $user_nomade_local_copy, $email_nomade, $user_vacation_enable ) = $queryResult->fetchrow_array ) {

    if( !defined($currentDomain->{"domain_label"}) ) {
        return undef;
    }

    &OBM::toolBox::write_log( "Gestion de la configuration de Postfix pour le domaine '".$currentDomain->{"domain_label"}."'", "W" );
        
    #
    # On cree la structure correspondante a la configuration de Postfix par
    # domaine
    my @postfixConfs;
    $postfixConfs[0]->{"postfixconf_name"} = $currentDomain->{"domain_label"};
    $postfixConfs[0]->{"postfixconf_domain"} = $currentDomain->{"domain_label"};

    $postfixConfs[0]->{"postfixconf_mail_domains"} = [];
    push( @{$postfixConfs[0]->{"postfixconf_mail_domains"}}, $currentDomain->{"domain_name"} );
    for( my $i=0; $i<=$#{$currentDomain->{"domain_alias"}}; $i++ ) {
        push( @{$postfixConfs[0]->{"postfixconf_mail_domains"}}, $currentDomain->{"domain_alias"}->[$i] );
    }

    # On ajoute les informations de la structure
    $postfixConfs[0]->{"node_type"} = $POSTFIXCONF;
    $postfixConfs[0]->{"name"} = $postfixConfs[0]->{$attributeDef->{$postfixConfs[0]->{"node_type"}}->{"dn_value"}};
    $postfixConfs[0]->{"domain_id"} = $domainId;
    $postfixConfs[0]->{"dn"} = &OBM::ldap::makeDn( $postfixConfs[0], $parentDn );

    #
    # On referme la connexion a la base
#    if( !&OBM::dbUtils::dbState( "disconnect", \$dbHandler ) ) {
#        &OBM::toolBox::write_log( "Probleme lors de la fermeture de la base de donnees...", "W" );
#    }

    return \@postfixConfs;
}


sub createLdapEntry {
    my ( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};

    if( !defined( $entry->{"postfixconf_name"} ) ) {
        return 0;
    }

    #
    # On construit la nouvelle entree
    #
    $ldapEntry->add(
        objectClass => $attributeDef->{$type}->{"objectclass"},
        cn => to_utf8({ -string => $entry->{"postfixconf_name"}, -charset => $defaultCharSet }),
    );

    # Les domaines de messagerie
    if( $entry->{"postfixconf_mail_domains"} ) {
        $ldapEntry->add( myDestination => $entry->{"postfixconf_mail_domains"} );
    }

    # Le domaine
    if( $entry->{"postfixconf_domain"} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entry->{"postfixconf_domain"}, -charset => $defaultCharSet }) );
    }

    return 1;
}


sub updateLdapEntry {
    my( $entry, $ldapEntry ) = @_;
    my $type = $entry->{"node_type"};
    my $update = 0;

    # Les domaines de messagerie
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"postfixconf_mail_domains"}, $ldapEntry, "myDestination" ) ) {
        $update = 1;
    }

    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"postfixconf_domain"}, $ldapEntry, "obmDomain") ) {
        $update = 1;
    }

    return $update;
}

