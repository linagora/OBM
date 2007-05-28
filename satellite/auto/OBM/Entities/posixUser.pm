package OBM::Ldap::posixUser;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
require overload;
use Carp;
use strict;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
require OBM::passwd;
require OBM::toolBox;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);


my %ldapEngineAttr = (
    type => undef,
    typeDesc => undef,
    incremental => undef,
    userId => undef,
    domainId => undef,
    userDesc => undef
);


sub new {
    my( $obj, $incremental, $userId ) = @_;
    $obj = ref($obj) || $obj;

    if( !defined($userId) ) {
        croak( "Usage: PACKAGE->new(USERID)" );
    }else {
        $ldapEngineAttr{"userId"} = $userId;
    }

    if( $incremental ) {
        $ldapEngineAttr{"incremental"} = 1;
    }else {
        $ldapEngineAttr{"incremental"} = 0;
    }

    $ldapEngineAttr{"type"} = $POSIXUSERS;
    $ldapEngineAttr{"typeDesc"} = $attributeDef->{$ldapEngineAttr{"type"}};

    my $self = \%ldapEngineAttr;
    bless( $self, $obj );

    return $self;
}


sub getTableName {
    my $self = shift;
    my( $tableName ) = @_;

    if( $self->{"incremental"} ) {
        $tableName = "P_".$tableName;
    }

    return $tableName;
}


sub getDbValue {
    my $self = shift;
    my( $dbHandler, $domainDesc, $userId ) = @_;


    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "posixUser: connecteur a la base de donnee invalide", "W" );
        return 0;
    }

    if( !defined($domainDesc->{"domain_id"}) ) {
        &OBM::toolBox::write_log( "posixUser: description de domaine OBM incorrecte", "W" );
        return 0;
    }

    if( !defined($userId) || ($userId !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "posixUser: identifiant d'utilisateur incorrect", "W" );
        return 0;

    }else {
        my $query = "SELECT COUNT(*) FROM FROM ".$self->getTableName("UserObm")." LEFT JOIN ".$self->getTableName("MailServer")." ON userobm_mail_server_id=mailserver_id WHERE userobm_archive=0 AND userobm_id=".$userId;

        my $queryResult;
        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "posixUser: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
            return undef;
        }

        my( $numRows ) = $queryResult->fetchrow_array();
        $queryResult->finish();

        if( $numRows == 0 ) {
            &OBM::toolBox::write_log( "posixUser: pas d'utilisateur d'identifiant : ".$userId, "W" );
            return undef;
        }elsif( $numRows > 1 ) {
            &OBM::toolBox::write_log( "posixUser: plusieurs utilisateurs d'identifiant : ".$userId." ???", "W" );
            return undef;
        }
    }


    # La requete a executer - obtention des informations sur l'utilisateur
    my $query = "SELECT userobm_id, userobm_perms, userobm_login, userobm_password_type, userobm_password, userobm_uid, userobm_gid, userobm_lastname, userobm_firstname, userobm_address1, userobm_address2, userobm_address3, userobm_zipcode, userobm_town, userobm_title, userobm_service, userobm_description, userobm_mail_perms, userobm_mail_ext_perms, userobm_email, mailserver_host_id, userobm_web_perms, userobm_phone, userobm_phone2, userobm_fax, userobm_fax2, userobm_mobile FROM P_UserObm LEFT JOIN P_MailServer ON userobm_mail_server_id=mailserver_id WHERE userobm_archive=0 AND userobm_id=".$userId;

    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "posixUser: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return undef;
    }

    # On range les resultats dans la structure de donnees des resultats
    my( $user_id, $user_perms, $user_login, $user_passwd_type, $user_passwd, $user_uid, $user_gid, $user_lastname, $user_firstname, $user_address1, $user_address2, $user_address3, $user_zipcode, $user_town, $user_title, $user_service, $user_description, $user_mail_perms, $user_mail_ext_perms, $user_email, $user_mail_server_id, $user_web_perms, $user_phone, $user_phone2, $user_fax, $user_fax2, $user_mobile ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    &OBM::toolBox::write_log( "posixUser: gestion de l'utilisateur '".$user_login."', domaine '".$domainDesc->{"domain_label"}."'", "W" );

    # Gestion de l'UID
    my $user_real_uid = $user_uid;
    if( lc($user_perms) eq "admin" ) {
        $user_real_uid = 0;
    }
        
    # On cree la structure correspondante a l'utilisateur
    # Cette structure est composee des valeurs recuperees dans la base
    $self->{"userDesc"} = {
        "user_id"=>$user_id,
        "user_login"=>$user_login,
        "user_uid"=>$user_real_uid,
        "user_gid"=>$user_gid,
        "user_lastname"=>$user_lastname,
        "user_firstname"=>$user_firstname,
        "user_homedir"=>"$baseHomeDir/$user_login",
        "user_mailperms"=>$user_mail_perms,
        "user_webperms"=>$user_web_perms,
        "user_passwd_type"=>$user_passwd_type,
        "user_passwd"=>$user_passwd,
        "user_title"=>$user_title,
        "user_service"=>$user_service,
        "user_description"=>$user_description,
        "user_zipcode"=>$user_zipcode,
        "user_town"=>$user_town,
        "user_mobile"=>$user_mobile,
        "user_domain" => $domainDesc->{"domain_label"}
    };

    # gestion de l'adresse
    if( defined($user_address1) && ($user_address1 ne "") ) {
        $self->{"userDesc"}->{"user_address"} = $user_address1;
    }
        
    if( defined($user_address2) && ($user_address2 ne "") ) {
        $self->{"userDesc"}->{"user_address"} .= "\r\n".$user_address2;
    }
        
    if( defined($user_address3) && ($user_address3 ne "") ) {
        $self->{"userDesc"}->{"user_address"} .= "\r\n".$user_address3;
    }
        

    # Gestion du téléphone
    if( defined($user_phone) && ($user_phone ne "") ) {
        push( @{$self->{"userDesc"}->{"user_phone"}}, $user_phone );
    }

    if( defined($user_phone2) && ($user_phone2 ne "") ) {
        push( @{$self->{"userDesc"}->{"user_phone"}}, $user_phone2 );
    }

    # Gestion du fax
    if( defined($user_fax) && ($user_fax ne "") ) {
        push( @{$self->{"userDesc"}->{"user_fax"}}, $user_fax );
    }

    if( defined($user_fax2) && ($user_fax2 ne "") ) {
        push( @{$self->{"userDesc"}->{"user_fax"}}, $user_fax2 );
    }

    # Gestion du droit de messagerie
    if( $user_mail_perms ) {
        my $localServerIp = &OBM::toolBox::getHostIpById( $dbHandler, $user_mail_server_id );

        if( !defined($localServerIp) ) {
            &OBM::toolBox::write_log( "posixUser: droit mail de l'utilisateur '".$user_login."' annule - Serveur inconnu !", "W" );
            $self->{"userDesc"}->{"user_mailperms"} = 0;

        }else {

            # Limite la messagerie aux domaines locaux
            if( !$user_mail_ext_perms ) {
                $self->{"userDesc"}->{"user_mailLocalOnly"} = "local_only";
            }

            # Gestions des e-mails des utilisateurs.
            my @email = split( /\r\n/, $user_email );
            for( my $j=0; $j<=$#email; $j++ ) {
                push( @{$self->{"userDesc"}->{"user_email"}}, $email[$j]."@".$domainDesc->{"domain_name"} );

                for( my $k=0; $k<=$#{$domainDesc->{"domain_alias"}}; $k++ ) {
                    push( @{$self->{"userDesc"}->{"user_email_alias"}}, $email[$j]."@".$domainDesc->{"domain_alias"}->[$k] );
                }
            }

            # Gestion des BAL destination
            $self->{"userDesc"}->{"user_mailbox"} = $self->{"userDesc"}->{"user_login"}."@".$domainDesc->{"domain_name"};

            # On ajoute le serveur de mail associé
            $self->{"userDesc"}->{"user_mailLocalServer"} = "lmtp:".$localServerIp.":24";
        }
    }

    # On ajoute les informations de la structure
    $self->{"domain_id"} = $domainDesc->{"domain_id"};


    return 1;
}


sub getLdapDnPrefix {
    my $self = shift;
    my $dnPrefix = undef;

    if( defined($self->{"typeDesc"}->{"dn_prefix"}) && defined($self->{"userDesc"}->{$self->{"typeDesc"}->{"dn_value"}}) ) {
        $dnPrefix = $self->{"typeDesc"}->{"dn_prefix"}."=".$self->{"userDesc"}->{$self->{"typeDesc"}->{"dn_value"}};
    }

    return $dnPrefix;
}


sub createLdapEntry {
    my $self = shift;
    my ( $ldapEntry ) = @_;
    my $entry = $self->{"userDesc"};

    #
    # Gestion du mot de passe
    if( !defined( $entry->{"user_passwd_type"} ) || ($entry->{"user_passwd_type"} eq "") ) {
        return 0;
    }

    my $userPasswd = &OBM::passwd::convertPasswd( $entry->{"user_passwd_type"}, $entry->{"user_passwd"} );
    if( !defined( $userPasswd ) ) {
        return 0;
    }


    #
    # On construit la nouvelle entree
    #
    # Les parametres nececessaires
    if( $entry->{"user_login"} && $entry->{"user_lastname"} && defined($entry->{"user_uid"}) && ($entry->{"user_uid"} ne "") && defined($entry->{"user_gid"})  && $entry->{"user_homedir"} ) {

        # Creation de la valeur du champs CN
        my $longName;
        if( $entry->{"user_firstname"} ) {
            $longName = $entry->{"user_firstname"}." ".$entry->{"user_lastname"};
        }else {
            $longName = $entry->{"user_lastname"};
        }
                
        $ldapEntry->add(
            objectClass => $self->{"typeDesc"}->{"objectclass"},
            uid => to_utf8({ -string => $entry->{"user_login"}, -charset => $defaultCharSet }),
            cn => to_utf8({ -string => $longName, -charset => $defaultCharSet }),
            sn => to_utf8({ -string => $entry->{"user_lastname"}, -charset => $defaultCharSet }),
            displayName => to_utf8({ -string => $longName, -charset => $defaultCharSet }),
            uidNumber => $entry->{"user_uid"},
            gidNumber => $entry->{"user_gid"},
            homeDirectory => $entry->{"user_homedir"},
            loginShell => "/bin/bash"
        );

    }else {
        return 0;
    }

    # Le prenom
    if( $entry->{"user_firstname"} ) {
        $ldapEntry->add( givenName => to_utf8({ -string => $entry->{"user_firstname"}, -charset => $defaultCharSet }) );
    }

    # Le mot de passe
    if( $userPasswd ) {
        $ldapEntry->add( userPassword => $userPasswd );
    }

    # Le telephone
    if( $entry->{"user_phone"} ) {
        $ldapEntry->add( telephoneNumber => $entry->{"user_phone"} );
    }

    # Le fax
    if( $entry->{"user_fax"} ) {
        $ldapEntry->add( facsimileTelephoneNumber => $entry->{"user_fax"} );
    }

    # Le mobile
    if( $entry->{"user_mobile"} ) {
        $ldapEntry->add( mobile => $entry->{"user_mobile"} );
    }

    # Le titre
    if( $entry->{"user_title"} ) {
        $ldapEntry->add( title => to_utf8({ -string => $entry->{"user_title"}, -charset => $defaultCharSet }) );
    }

    # Le service
    if( $entry->{"user_service"} ) {
        $ldapEntry->add( ou => to_utf8({ -string => $entry->{"user_service"}, -charset => $defaultCharSet }) );
    }

    # La description
    if( $entry->{"user_description"} ) {
        $ldapEntry->add( description => to_utf8({ -string => $entry->{"user_description"}, -charset => $defaultCharSet }) );
    }

    # L'acces web
    if( $entry->{"user_webperms"} || ( defined( $entry->{"user_webperms"} ) && ($entry->{"user_webperms"} == 0) ) ) {
        $ldapEntry->add( webAccess => $entry->{"user_webperms"} );
    }

    # La boite a lettres de l'utilisateur
    if( $entry->{"user_mailbox"} ) {
        $ldapEntry->add( mailBox => $entry->{"user_mailbox"} );
    }

    # Le serveur de BAL local
    if( $entry->{"user_mailLocalServer"} ) {
        $ldapEntry->add( mailBoxServer => $entry->{"user_mailLocalServer"} );
    }

    # L'acces mail
    if( $entry->{"user_mailperms"} ) {
        $ldapEntry->add( mailAccess => "PERMIT" );
    }else {
        $ldapEntry->add( mailAccess => "REJECT" );
    }

    # La limite aux domaines locaux
    if( $entry->{"user_mailLocalOnly"} ) {
        $ldapEntry->add( mailLocalOnly => $entry->{"user_mailLocalOnly"} );
    }

    # Les adresses mails
    if( $entry->{"user_email"} && ($#{$entry->{"user_email"}} != -1) ) {
        $ldapEntry->add( mail => $entry->{"user_email"} );
    }

    # Les adresses mail secondaires
    if( $entry->{"user_email_alias"} && ($#{$entry->{"user_email_alias"}} != -1) ) {
        $ldapEntry->add( mailAlias => $entry->{"user_email_alias"} );
    }

    # L'adresse postale
    if( $entry->{"user_address"} ) {
        # Thunderbird, IceDove... : ne comprennent que cet attribut
        $ldapEntry->add( street => to_utf8({ -string => $entry->{"user_address"}, -charset => $defaultCharSet }) );
        # Outlook : ne comprend que cet attribut
        # Outlook Express : préfère celui-là à 'street'
        $ldapEntry->add( postalAddress => to_utf8({ -string => $entry->{"user_address"}, -charset => $defaultCharSet }) );
    }

    # Le code postal
    if( $entry->{"user_zipcode"} ) {
        $ldapEntry->add( postalCode => to_utf8({ -string => $entry->{"user_zipcode"}, -charset => $defaultCharSet }) );
    }

    # La ville
    if( $entry->{"user_town"} ) {
        $ldapEntry->add( l => to_utf8({ -string => $entry->{"user_town"}, -charset => $defaultCharSet }) );
    }

    # Le domaine
    if( $entry->{"user_domain"} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entry->{"user_domain"}, -charset => $defaultCharSet }) );
    }

    return 1;
}


sub updateLdapEntry {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $entry = $self->{"userDesc"};
    my $update = 0;

    # Le champs nom, prenom de l'utilisateur
    my $longName;
    if( $entry->{"user_firstname"} ) {
        $longName = $entry->{"user_firstname"}." ".$entry->{"user_lastname"};
    }else {
        $longName = $entry->{"user_lastname"};
    }

    if( &OBM::Ldap::utils::modifyAttr( $longName, $ldapEntry, "cn" ) ) {
        # On synchronise le nom affichable
        &OBM::Ldap::utils::modifyAttr( $longName, $ldapEntry, "displayName" );

        $update = 1;
    }

    # Le nom de famille
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_lastname"}, $ldapEntry, "sn" ) ) {
        $update = 1;
    }

    # Le prenom
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_firstname"}, $ldapEntry, "givenName" ) ) {
        $update = 1;
    }

    # Le titre
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_title"}, $ldapEntry, "title" ) ) {
        $update = 1;
    }

    # Le service
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_service"}, $ldapEntry, "ou" ) ) {
        $update = 1;
    }

    # La description
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_description"}, $ldapEntry, "description" ) ) {
        $update = 1;
    }

    # L'adresse
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_address"}, $ldapEntry, "street" ) ) {
        &OBM::Ldap::utils::modifyAttr( $entry->{"user_address"}, $ldapEntry, "postalAddress" );
        $update = 1;
    }

    # Le code postal
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_zipcode"}, $ldapEntry, "postalCode" ) ) {
        $update = 1;
    }

    # La ville
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_town"}, $ldapEntry, "l" ) ) {
        $update = 1;
    }

    # Le repertoire personnel
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_homedir"}, $ldapEntry, "homeDirectory" ) ) {
        $update = 1;
    }
            
    # L'UID
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_uid"}, $ldapEntry, "uidNumber" ) ) {
        $update = 1;
    }

    # Le GID
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_gid"}, $ldapEntry, "gidNumber" ) ) {
        $update = 1;
    }

    # Le telephone
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"user_phone"}, $ldapEntry, "telephoneNumber" ) ) {
        $update = 1;
    }

    # Le fax
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"user_fax"}, $ldapEntry, "facsimileTelephoneNumber" ) ) {
        $update = 1;
    }

    # Le mobile
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_mobile"}, $ldapEntry, "mobile" ) ) {
        $update = 1;
    }

    # L'acces au web
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_webperms"}, $ldapEntry, "webAccess" ) ) {
        $update = 1;
    }

    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_domain"}, $ldapEntry, "obmDomain") ) {
        $update = 1;
    }

    # L'acces au mail
    if( $entry->{"user_mailperms"} && ( &OBM::Ldap::utils::modifyAttr( "PERMIT", $ldapEntry, "mailAccess" ) ) ) {
        $update = 1;

    }elsif( !$entry->{"user_mailperms"} && ( &OBM::Ldap::utils::modifyAttr( "REJECT", $ldapEntry, "mailAccess" ) ) ) {
        $update = 1;

    }

    # La boite a lettres de l'utilisateur
    if( !$entry->{"user_mailperms"} ) {
        if( &OBM::Ldap::utils::modifyAttr( undef, $ldapEntry, "mailBox" ) ) {
            $update = 1;
        }

    }elsif( &OBM::Ldap::utils::modifyAttr( $entry->{"user_mailbox"}, $ldapEntry, "mailBox" ) ) {
        $update = 1;
    }

    # Le serveur de BAL local
    if( !$entry->{"user_mailperms"} ) {
        if( &OBM::Ldap::utils::modifyAttr( undef, $ldapEntry, "mailBoxServer" ) ) {
            $update = 1;
        }

    }elsif( &OBM::Ldap::utils::modifyAttr( $entry->{"user_mailLocalServer"}, $ldapEntry, "mailBoxServer" ) ) {
        $update = 1;
    }

    # La limitation au domaine local
    if( !$entry->{"user_mailperms"} ) {
        if( &OBM::Ldap::utils::modifyAttr( undef, $ldapEntry, "mailLocalOnly" ) ) {
            $update = 1;
        }

    }elsif( &OBM::Ldap::utils::modifyAttr( $entry->{"user_mailLocalOnly"}, $ldapEntry, "mailLocalOnly" ) ) {
        $update = 1;
    }

    # Le cas des adresses mails
    if( !$entry->{"user_mailperms"} ) {
        # Adresse principales
        if( &OBM::Ldap::utils::modifyAttrList( undef, $ldapEntry, "mail" ) ) {
            $update = 1;
        }

        # Adresses secondaires
        if( &OBM::Ldap::utils::modifyAttrList( undef, $ldapEntry, "mailAlias" ) ) {
            $update = 1;
        }


    }else {
        # Adresse principales
        if( &OBM::Ldap::utils::modifyAttrList( $entry->{"user_email"}, $ldapEntry, "mail" ) ) {
            $update = 1;
        }

        # Adresses secondaires
        if( &OBM::Ldap::utils::modifyAttrList( $entry->{"user_email_alias"}, $ldapEntry, "mailAlias" ) ) {
            $update = 1;
        }
    }

    return $update;
}
