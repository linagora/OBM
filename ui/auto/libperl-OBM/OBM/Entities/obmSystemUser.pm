package OBM::Entities::obmSystemUser;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Entities::commonEntities qw(getType setDelete getDelete getArchive getLdapObjectclass isLinks getEntityId _log);
use OBM::Parameters::common;
require OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
require OBM::Tools::obmDbHandler;
require OBM::passwd;
use URI::Escape;


sub new {
    my $self = shift;
    my( $links, $deleted, $userId ) = @_;

    my %obmSystemUserAttr = (
        type => undef,
        links => undef,
        toDelete => undef,
        archive => undef,
        sieve => undef,
        objectId => undef,
        domainId => undef,
        userDesc => undef,
        objectclass => undef,
        dnPrefix => undef,
        dnValue => undef
    );


    if( !defined($links) || !defined($deleted) || !defined($userId) ) {
        $self->_log( 'Usage: PACKAGE->new(LINKS, DELETED, USERID)', 1 );
        return undef;
    }elsif( $userId !~ /^\d+$/ ) {
        $self->_log( '[Entities::obmSystemUser]: identifiant d\'utilisateur incorrect', 2 );
        return undef;
    }else {
        $obmSystemUserAttr{"objectId"} = $userId;
    }


    $obmSystemUserAttr{"links"} = $links;
    $obmSystemUserAttr{"toDelete"} = $deleted;

    $obmSystemUserAttr{"type"} = $OBM::Parameters::ldapConf::SYSTEMUSERS;
    $obmSystemUserAttr{"archive"} = 0;
    $obmSystemUserAttr{"sieve"} = 0;

    # Définition de la représentation LDAP de ce type
    $obmSystemUserAttr{objectclass} = $OBM::Parameters::ldapConf::attributeDef->{$obmSystemUserAttr{"type"}}->{objectclass};
    $obmSystemUserAttr{dnPrefix} = $OBM::Parameters::ldapConf::attributeDef->{$obmSystemUserAttr{"type"}}->{dn_prefix};
    $obmSystemUserAttr{dnValue} = $OBM::Parameters::ldapConf::attributeDef->{$obmSystemUserAttr{"type"}}->{dn_value};

    bless( \%obmSystemUserAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $domainDesc ) = @_;

    my $userId = $self->{"objectId"};
    if( !defined($userId) ) {
        $self->_log( '[Entities::obmSystemUser]: aucun identifiant d\'utilisateur systeme definit', 3 );
        return 0;
    }


    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( '[Entities::obmSystemUser]: connecteur a la base de donnee invalide', 3 );
        return 0;
    }

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        $self->_log( '[Entities::obmSystemUser]: description de domaine OBM incorrecte', 3 );
        return 0;

    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }


    my $yserSystemTable = "UserSystem";
    if( $self->getDelete() ) {
        $yserSystemTable = "P_".$yserSystemTable;
    }

    my $query = "SELECT COUNT(*) FROM ".$yserSystemTable." WHERE usersystem_id=".$userId;

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    my( $numRows ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( $numRows == 0 ) {
        $self->_log( '[Entities::obmSystemUser]: pas d\'utilisateur d\'identifiant : '.$userId, 3 );
        return 0;
    }elsif( $numRows > 1 ) {
        $self->_log( '[Entities::obmSystemUser]: plusieurs utilisateurs d\'identifiant : '.$userId.' ???', 3 );
        return 0;
    }


    # La requete a executer - obtention des informations sur l'utilisateur
    $query = "SELECT usersystem_id, usersystem_login, usersystem_password, usersystem_uid, usersystem_gid, usersystem_homedir, usersystem_lastname, usersystem_firstname, usersystem_shell FROM ".$yserSystemTable." WHERE usersystem_id=".$userId;

    # On execute la requete
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    # On range les resultats dans la structure de donnees des resultats
    my( $user_id, $user_login, $user_password, $user_uid, $user_gid, $user_homedir, $user_lastname, $user_firstname, $user_shell ) = $queryResult->fetchrow_array();
    $queryResult->finish();


    $self->_log( '[Entities::obmSystemUser]: gestion de l\'utilisateur \''.$user_login.'\', domaine \''.$domainDesc->{'domain_label'}.'\'', 1 );
        
    # On cree la structure correspondante a l'utilisateur
    # Cette structure est composee des valeurs recuperees dans la base
    $self->{"userDesc"} = {
        "user_id"=>$user_id,
        "user_login"=>$user_login,
        "user_uid"=>$user_uid,
        "user_gid"=>$user_gid,
        "user_lastname"=>$user_lastname,
        "user_firstname"=>$user_firstname,
        "user_homedir"=>$user_homedir,
        "user_passwd_type"=>"PLAIN",
        "user_passwd"=>$user_password,
        "user_shell"=>$user_shell,
        "user_domain" => $domainDesc->{"domain_label"}
    };


    return 1;
}


sub updateDbEntity {
    my $self = shift;
    # Pas de table de production pour les entités de type utilisateur système

#    my $dbHandler = OBM::Tools::obmDbHandler->instance();
#    if( !defined($dbHandler) ) {
#        return 0;
#    }

    return 1;
}


sub updateDbEntityLinks {
    my $self = shift;
    # Pas de table de production pour les entités de type utilisateur système

#    my $dbHandler = OBM::Tools::obmDbHandler->instance();
#    if( !defined($dbHandler) ) {
#        return 0;
#    }

    return 1;
}


sub getEntityLinks {
    my $self = shift;
    my( $domainDesc ) = @_;

    return 1;
}


sub getEntityDescription {
    my $self = shift;
    my $entry = $self->{"userDesc"};
    my $description = "";


    if( defined($entry->{user_login}) ) {
        $description .= "identifiant '".$entry->{user_login}."'";
    }

    if( defined($entry->{user_domain}) ) {
        $description .= ", domaine '".$entry->{user_domain}."'";
    }

    if( ($description ne "") && defined($self->{type}) ) {
        $description .= ", type '".$self->{type}."'";
    }

    if( $description ne "" ) {
        return $description;
    }

    if( defined($self->{objectId}) ) {
        $description .= "ID BD '".$self->{objectId}."'";
    }

    if( defined($self->{type}) ) {
        $description .= ",type '".$self->{type}."'";
    }

    return $description;
}


sub getLdapDnPrefix {
    my $self = shift;
    my $dnPrefix = undef;

    if( defined($self->{"dnPrefix"}) && defined($self->{"userDesc"}->{$self->{"dnValue"}}) ) {
        $dnPrefix = $self->{"dnPrefix"}."=".$self->{"userDesc"}->{$self->{"dnValue"}};
    }

    return $dnPrefix;
}


sub createLdapEntry {
    my $self = shift;
    my ( $ldapEntry ) = @_;
    my $entry = $self->{"userDesc"};

    # Gestion du mot de passe
    if( !defined( $entry->{"user_passwd_type"} ) || ($entry->{"user_passwd_type"} eq "") ) {
        return 0;
    }

    my $userPasswd = &OBM::passwd::convertPasswd( $entry->{"user_passwd_type"}, $entry->{"user_passwd"} );
    if( !defined( $userPasswd ) ) {
        return 0;
    }


    # On construit la nouvelle entree
    #
    # Les parametres nécessaires
    if( $entry->{'user_login'} && $entry->{'user_firstname'} && $entry->{'user_lastname'} && $entry->{'user_uid'} && defined($entry->{'user_gid'})  && $entry->{'user_homedir'} ) {

        my $longName;
        if( $entry->{'user_firstname'} ) {
            $longName = $entry->{'user_firstname'}.' '.$entry->{'user_lastname'};
        }else {
            $longName = $entry->{'user_lastname'};
        }
                
        $ldapEntry->add(
            objectClass => $self->{'objectclass'},
            uid => $entry->{'user_login'},
            cn => $longName,
            sn => $entry->{'user_lastname'},
            uidNumber => $entry->{'user_uid'},
            gidNumber => $entry->{'user_gid'},
            homeDirectory => $entry->{'user_homedir'},
            loginShell => '/bin/bash',
            userpassword => $userPasswd,
            obmDomain => $entry->{'user_domain'}
        );

    }else {
        return 0;
    }

    return 1;
}


sub updateLdapEntryDn {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $update = 0;


    if( !defined($ldapEntry) ) {
        return 0;
    }


    return $update;
}


sub updateLdapEntry {
    my $self = shift;
    my( $ldapEntry, $objectclassDesc ) = @_;
    my $entry = $self->{"userDesc"};

    require OBM::Entities::entitiesUpdateState;
    my $update = OBM::Entities::entitiesUpdateState->new();


    if( !defined($ldapEntry) ) {
        return undef;
    }


    # Le champs nom, prenom de l'utilisateur
    my $longName = $entry->{"user_firstname"}." ".$entry->{"user_lastname"};
    if( &OBM::Ldap::utils::modifyAttr( $longName, $ldapEntry, "cn" ) ) {
        # On synchronise le surname
        &OBM::Ldap::utils::modifyAttr( $longName, $ldapEntry, "sn" );

        $update->setUpdate();
    }

    # Le mot de passe
    if( defined( $entry->{"user_passwd_type"} ) && ($entry->{"user_passwd_type"} ne"") ) {
        my $userPasswd = &OBM::passwd::convertPasswd( $entry->{"user_passwd_type"}, $entry->{"user_passwd"} );
        if( defined( $userPasswd ) ) {
            if( &OBM::Ldap::utils::modifyAttr( $userPasswd, $ldapEntry, "userpassword" ) ) {
                $update->setUpdate();
            }
        }
    }

    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"user_domain"}, $ldapEntry, "obmDomain") ) {
        $update->setUpdate();
    }


    if( $self->isLinks() ) {
        if( $self->updateLdapEntryLinks( $ldapEntry ) ) {
            $update->setUpdate();
        }
    }


    return $update;
}


sub updateLdapEntryLinks {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $update = 0;


    if( !defined($ldapEntry) ) {
        return 0;
    }


    return $update;
}


sub getMailboxName {
    my $self = shift;

    return undef;
}


sub getMailboxPartition {
    my $self = shift;

    return undef;
}


sub getMailboxSieve {
    my $self = shift;

    return $self->{"sieve"};
}


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );
    
    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}
