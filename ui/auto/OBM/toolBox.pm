#########################################################################
# OBM           - File : OBM::toolBox.pm (Perl Module)                  #
#               - Desc : Librairie Perl pour OBM                        #
#               Les fonctions communes                                  #
#########################################################################
# Cree le 2002-07-22                                                    #
#########################################################################
# $Id$   #
#########################################################################
package OBM::toolBox;

use OBM::Parameters::common;
use OBM::Parameters::toolBoxConf;
use OBM::dbUtils;
require OBM::utils;
use Sys::Syslog;
require Exporter;

@ISA = qw(Exporter);
@EXPORT_function = qw( write_log makeConfigFile getLastUid getLastGid getGroupUsersMailEnable getGroupUsers getGroupUsersSID makeEntityMailAddress getEntityRight aclUpdated getHostIpById getHostNameById getMailServerList getDomains);
@EXPORT = (@EXPORT_function);
@EXPORT_OK = qw();

#
# Necessaire pour le bon fonctionnement du package
$debug=1;


sub write_log {
    local($text, $action) = @_;

    SWITCH: {
        if( !defined( $action ) ) {
            last SWITCH;
        }

    	if( $action eq "O" ) {
    		Sys::Syslog::setlogsock("unix");
	    	openlog( $text, "pid", "$facility_log" );

            last SWITCH;
        }

        if( ($action eq "W") || ($action eq "WC") ) {
            syslog( "notice", $text );

            last SWITCH;
        }
    
        if( ($action eq "C") || ($action eq "WC") ) {
            closelog();

            last SWITCH;
        }
    }

    return 0;
}

#------------------------------------------------------------------------------
# Cette fonction permet de parcourir le fichier modele et de
# remplacer les tags <ALIAMIN-...> par la valeur correspondante.
#------------------------------------------------------------------------------
# Parametres :
#	$template : pointeur sur un tableau contenant 1 ligne du modele
# dans chacune de ses cases.
#	$data : pointeur sur une table de hachage associant cle/valeurs
#   $start : indique la première à traiter
#   $end : indique la dernière ligne à traiter
#
# Retour :
#	1 : tout est ok
#	0 : manque des valeurs pour completer le fichier
#------------------------------------------------------------------------------
sub makeConfigFile
{
	my( $template, $data, $start, $end ) = @_;

    #
    # On determine l'indice de depart et de fin
    if( not defined($start) )
    {
        $start = 0;
    }
    if( not defined($end) )
    {
        $end = $#$template;
    }

    # On stocke le resultat dans cette variable
    my $result = "";

    # On parcours le modele ligne par ligne
    my $i=$start;
    while( $i<=$end )
    {
        #
        # On copie la ligne tel quel dans une variable de travail
        my $tmp = $$template[$i];

        #
        # On remplace les eventuelle balises
        while( $tmp =~ /$findTag/ )
        {
            #
            # On recupere la valeur associee a la balise dans la structure de
            # donnee
            my $tagValue = $data->{$1};

            #
            # On teste le type de la valeur
            if( ref( $tagValue ) eq "ARRAY" )
            {
                #
                # On garde la valeur du tag
                my $lineBeginLoop = $1;

                #
                # On vide la variable $tmp, qui ne contient pour l'instant que
                # la ligne avec le tag de debut de loop
                $tmp = "";

                #
                # On passe a la ligne suivante (une ligne contenant un debut de
                # boucle ne peut contenir autre chose).
                $i++;
                #
                # Si la valeur associe a la balise est un tableau, il va falloir
                # boucler sur une partie du modele avec les differentes valeurs
                # du tableau.
                # On commence par déterminer l'indice de la ligne du modele
                # specifiant le debut et la fin de la boucle.
                my $startLoop = $i;
                #
                # On cherche la fin de la boucle courante
                my $foundEndTag = 0;
                while( !$foundEndTag )
                {
                    if( $$template[$i] =~ /$endLoopTag/ )
                    {
                        if( $1 eq $lineBeginLoop )
                        {
                            $foundEndTag = 1;
                        }else
                        {
                            $i++;
                        }
                    }else
                    {
                        $i++;
                    }
                }

                #
                # Quand on sort, on a trouve l'indice de la ligne de fin de
                # boucle, on appelle en recurcif cette fonction sur le template 
                # en se limitant aux indices de debut et de fin de boucle
                # On fait ceci pour chacune des valeurs du tableau associe a la
                # valeur de la balise.
                for( my $j=0; $j<=$#$tagValue; $j++ )
                {
                    $tmp .= makeConfigFile( $template, $$tagValue[$j], $startLoop, $i-1 );
                }

            }elsif( not defined($tagValue) )
            {
                #
                # Si le tag n'existe pas, on supprime la ligne.
                $tmp = "";
            }elsif( not ref( $tagValue ) )
            {
                #
                # Si la valeur est un scalaire on remplace le tag par la valeur
                # du scalaire
                $tmp =~ s/$findTag/$tagValue/;

            }
        }

        #
        # Si la ligne contient une balise fermante qui n'a jamais ete ouverte,
        # on supprime la ligne.
        if(  $tmp =~ /$endLoopTag/ )
        {
            $tmp = "";
        }

        #
        # Lorsqu'on a remplacee toutes les balises de la ligne, on la concatene
        # avec le resultat
        $result .= $tmp;
        $i++;
    }

    return $result;
}


#
# Cherche le premier UID libre
#
# Parametres :
#       - listUsers : tableau contenant une description d'utilisateur par case.
#
# Retour :
#       - l'UID libre
sub getLastUid {
    my( $listUsers ) = @_;
 
    my $maxUid = $minUID;
 
    for( my $i=0; $i<=$#$listUsers; $i++ )
    {
        if( $listUsers->[$i]->{"user_uid"} > $maxUid )
        {
            $maxUid = $listUsers->[$i]->{"user_uid"};
        }
    }
 
    return ($maxUid + 1);
}


#
# Cherche le permier GID libre
#
# Parametres :
#       - listGroups : tableau contenant une description de groupe par case.
#
# Retour :
#       - le GID libre
sub getLastGid {
    my( $listGroups ) = @_;

    my $maxGid = $minGID;

    for( my $i=0; $i<=$#$listGroups; $i++ ) {
        if( defined($listGroups->[$i]->{"group_gid"}) && ($listGroups->[$i]->{"group_gid"} > $maxGid) ) {
            $maxGid = $listGroups->[$i]->{"group_gid"};
        }
    }

    return ($maxGid + 1);
}


#------------------------------------------------------------------------------
# Cette fonction permet de recuperer le login de tous les utilisateurs ayant
# droit aux mails et faisant partie du groupe dont l'Id est passe en parametre.
#------------------------------------------------------------------------------
# Parametres :
#	$groupId : ID du groupe dont on souhaite recuperer la liste des logins
#	$dbHandler : reference a l'objet de gestion de la connexion a la base
#
# Retour :
#	reference a un tableau contenant un login par case.
#------------------------------------------------------------------------------
sub getGroupUsersMailEnable {
    my( $groupId, $dbHandler ) = @_;

    return getGroupUsers( $groupId, $dbHandler, "AND i.userobm_mail_perms=1" );
}


#------------------------------------------------------------------------------
# Cette fonction permet de recuperer le login de tous les utilisateurs
# faisant partie du groupe dont l'Id est passe en parametre.
#------------------------------------------------------------------------------
# Parametres :
#	$groupId : ID du groupe dont on souhaite recuperer la liste des logins
#	$dbHandler : reference a l'objet de gestion de la connexion a la base
#   $sqlRequest : filter SQL permettant de faire le choix des utilisateurs
#   a recuperer plus finement
#
# Retour :
#	reference a un tableau contenant un login par case.
#------------------------------------------------------------------------------
sub getGroupUsers
{
    my( $groupId, $dbHandler, $sqlRequest ) = @_;

    my @tabResult;
    my $queryResult;

    #
    # Recuperation de la liste d'utilisateur de ce groupe id : $groupId.
    my $query = "SELECT i.userobm_login FROM P_UserObm i, P_UserObmGroup j WHERE j.userobmgroup_group_id=".$groupId." AND j.userobmgroup_userobm_id=i.userobm_id";

    if( defined( $sqlRequest ) && ($sqlRequest ne "") ) {
        $query .= " ".$sqlRequest;
    }
    
    #
    # On execute la requete
    if( !execQuery( $query, $dbHandler, \$queryResult ) ) {
        write_log( "Probleme lors de l'execution de la requete.", "W" );
        if( defined($queryResult) ) {
            write_log( $queryResult->err, "W" );
        }

        write_log( "", "C" );
        exit 1;
    }
    #
    # On stocke le resultat dans le tableau des resultats
    while( my( $userLogin ) = $queryResult->fetchrow_array )
    {
        push( @tabResult, $userLogin );
    }

    #
    # Recuperation de la liste des utilisateurs du groupe id : $groupId.
    $query = "SELECT groupgroup_child_id FROM P_GroupGroup WHERE groupgroup_parent_id=".$groupId;
    #
    # On execute la requete
    if( !execQuery( $query, $dbHandler, \$queryResult ) ) {
        write_log( "Probleme lors de l'execution de la requete.", "W" );
        if( defined($queryResult) ) {
            write_log( $queryResult->err, "W" );
        }

        write_log( "", "C" );
        exit 1;
    }
    #
    # On traite les resultats
    while( my( $groupGroupId ) = $queryResult->fetchrow_array )
    {
        my $userGroupTmp = getGroupUsers( $groupGroupId, $dbHandler, $sqlRequest );

        for( my $i=0; $i<=$#$userGroupTmp; $i++ )
        {
            my $j =0;
            while( ($j<=$#tabResult) && ($$userGroupTmp[$i] ne $tabResult[$j]) )
            {
                $j++;
            }

            if( $j>$#tabResult )
            {
                push( @tabResult, $$userGroupTmp[$i] );
            }
        }
    }
    
    return \@tabResult;
}


#------------------------------------------------------------------------------
# Cette fonction permet de recuperer le SID de tous les utilisateurs
# faisant partie du groupe dont l'Id est passe en parametre.
#------------------------------------------------------------------------------
# Parametres :
#	$groupId : ID du groupe dont on souhaite recuperer la liste des logins
#	$dbHandler : reference a l'objet de gestion de la connexion a la base
#   $baseDN : DN de reference pour les groupes
#
# Retour :
#	reference a un tableau contenant un SID par case.
#------------------------------------------------------------------------------
sub getGroupUsersSID
{
    my( $groupId, $dbHandler, $SID ) = @_;

    my @tabResult;
    my $queryResult;

    #
    # Recuperation de la liste d'utilisateur de ce groupe id : $groupId.
    my $query = "SELECT i.userobm_uid FROM P_UserObm i, P_UserObmGroup j WHERE j.userobmgroup_group_id=".$groupId." AND j.userobmgroup_userobm_id= i.userobm_id";
    #
    # On execute la requete
    if( !execQuery( $query, $dbHandler, \$queryResult ) ) {
        write_log( "Probleme lors de l'execution de la requete.", "W" );
        if( defined($queryResult) ) {
            write_log( $queryResult->err, "W" );
        }

        write_log( "", "C" );
        exit 1;
    }
    #
    # On stocke le resultat dans le tableau des resultats
    while( my( $userSID ) = $queryResult->fetchrow_array )
    {
        push( @tabResult, getUserSID( $SID, $userSID ) );
    }

    #
    # Recuperation de la liste des utilisateurs du groupe id : $groupId.
    $query = "SELECT groupgroup_child_id FROM P_GroupGroup WHERE groupgroup_parent_id=".$groupId;
    #
    # On execute la requete
    if( !execQuery( $query, $dbHandler, \$queryResult ) ) {
        write_log( "Probleme lors de l'execution de la requete.", "W" );
        if( defined($queryResult) ) {
            write_log( $queryResult->err, "W" );
        }

        write_log( "", "C" );
        exit 1;
    }
    #
    # On traite les resultats
    while( my( $groupGroupId ) = $queryResult->fetchrow_array )
    {
        my $userGroupTmp = getGroupUsersSID( $groupGroupId, $dbHandler, $SID );

        for( my $i=0; $i<=$#$userGroupTmp; $i++ )
        {
            my $j =0;
            while( ($j<=$#tabResult) && ($$userGroupTmp[$i] ne $tabResult[$j]) )
            {
                $j++;
            }

            if( $j>$#tabResult )
            {
                push( @tabResult, $$userGroupTmp[$i] );
            }
        }
    }
    
    return \@tabResult;
}


sub makeEntityMailAddress {
    my( $mailPrefix, $domain ) = @_;
    my @mailList;

    my @prefixList = split( "\r\n", $mailPrefix );

    for( my $j=0; $j<=$#prefixList; $j++ ) {
        if( exists($domain->{"domain_name"}) && ($domain->{"domain_name"} ne "") ) {
            push( @mailList, $prefixList[$j]."@".$domain->{"domain_name"} );
        }

        if( exists($domain->{"domain_alias"}) ) {
            for( my $i=0; $i<=$#{$domain->{"domain_alias"}}; $i++ ) {
                push( @mailList, $prefixList[$j]."@".$domain->{"domain_alias"}->[$i] );
            }
        }
    }

    return \@mailList;
}


#------------------------------------------------------------------------------
# Gestion de la table des droits
#------------------------------------------------------------------------------

#------------------------------------------------------------------------------
# Cette fonction permet d'obtenir la liste des droits des consomateurs sur
# une entité.
#------------------------------------------------------------------------------
sub getEntityRight {
    my ( $dbHandler, $domain, $rightDef, $shareId ) = @_;
    my %entityTemplate = ( "read", 0, "writeonly", 0, "write", 0 );
    my %usersList;


    if( !defined($dbHandler) || !defined($rightDef) ||  !defined($shareId) ) {
        return undef;
    }


    # On execute la requete
    if( !execQuery( $rightDef->{"public"}->{"sqlQuery"}, $dbHandler, \$queryResult ) ) {
        write_log( "Echec : probleme lors de l'execution de la requete : ".$queryResult->err, "W" );
        return undef;
    }

    if( my( $read, $write ) = $queryResult->fetchrow_array ) {
        if( $read && !$write ) {
            $usersList{"anyone"}->{"read"} = 1;
            $usersList{"anyone"}->{"writeonly"} = 0;
            $usersList{"anyone"}->{"write"} = 0;

            # Droit a ne pas traiter car droit public
            $rightDef->{"read"}->{"compute"} = 0;

            # construction d'un template utilisateur
            $entityTemplate{"read"} = 1;
        }elsif( !$read && $write ) {
            $usersList{"anyone"}->{"read"} = 0;
            $usersList{"anyone"}->{"writeonly"} = 1;
            $usersList{"anyone"}->{"write"} = 0;
            
            # Droit a ne pas traiter car droit public
            $rightDef->{"writeonly"}->{"compute"} = 0;

            # construction d'un template utilisateur
            $entityTemplate{"writeonly"} = 1;
        }elsif( $read && $write ) {
            $usersList{"anyone"}->{"read"} = 0;
            $usersList{"anyone"}->{"writeonly"} = 0;
            $usersList{"anyone"}->{"write"} = 1;

            # Droit a ne pas traiter car droit public
            $rightDef->{"read"}->{"compute"} = 0;
            $rightDef->{"writeonly"}->{"compute"} = 0;
            $rightDef->{"write"}->{"compute"} = 0;
        }
    }
    $queryResult->finish;


    # Traitement du droit '$right', cles du hachage '%rightDef'
    while( my( $right, $rightDesc ) = each( %{$rightDef} ) ) {
        if( !$rightDesc->{"compute"} ) {
            next;
        }

        # On execute la requête correspondant au droit
        if( !execQuery( $rightDef->{$right}->{"sqlQuery"}, $dbHandler, \$queryResult ) ) {
            write_log( "Echec : probleme lors de l'execution de la requete : ".$queryResult->err, "W" );
            return undef;
        }

        while( my( $userLogin ) = $queryResult->fetchrow_array ) {
            if( defined($domain->{"domain_name"}) ) {
                $userLogin .= "@".$domain->{"domain_name"};
            }

            # Si l'utilisateur n'a pas déjà été trouvé, on l'initialise
            # avec les valeurs du template
            if( !exists( $usersList{$userLogin} ) ) {
                while( my( $templateRight, $templateValue ) = each( %entityTemplate ) ) {
                    $usersList{$userLogin}->{$templateRight} = $templateValue;
                }
            }

            $usersList{$userLogin}->{$right} = 1;
        }

    }

    # Normalisation des droits
    return computeRight( \%usersList );
}


#------------------------------------------------------------------------------
# Permet de convertir les droits décrit en base, en droit Aliamin.
#------------------------------------------------------------------------------
sub computeRight {
    my( $usersList ) = @_;
    use OBM::Parameters::cyrusConf;
    my $rightList = &OBM::utils::cloneStruct(OBM::Parameters::cyrusConf::boxRight);

    while( my( $userName, $right ) = each( %$usersList ) ) {
        if( $right->{"write"} ) {
            $rightList->{"write"}->{$userName} = 1;
        }elsif( $right->{"read"} && $right->{"writeonly"} ) {
            $rightList->{"write"}->{$userName} = 1;
        }elsif( $right->{"read"} ) {
            $rightList->{"read"}->{$userName} = 1;
        }elsif( $right->{"writeonly"} ) {
            $rightList->{"writeonly"}->{$userName} = 1;
        }
    }

    return $rightList;
}


#------------------------------------------------------------------------------
# Permet de savoir si il y a eu mise à jour des ACL ou pas.
#------------------------------------------------------------------------------
sub aclUpdated {
    my( $oldAcl, $newAcl ) = @_;
    my $returnCode = 0;

    while( my( $oldRight, $oldUsers ) = each( %$oldAcl ) ) {
        if( $returnCode ) {
            next;
        }

        if( !exists( $newAcl->{$oldRight} ) ) {
            return 1;
        }

        my @oldUsersList = keys( %$oldUsers );
        my @newUsersList = keys( %{$newAcl->{$oldRight}} );

        if( $#oldUsersList != $#newUsersList ) {
            return 1;
        }

        for( my $i=0; $i<=$#oldUsersList; $i++ ) {
            if( !exists($newAcl->{$oldRight}->{$oldUsersList[$i]}) ) {
                return 1;
            }
        }
    }

    return 0;
}

#------------------------------------------------------------------------------
# Permet d'obtenir l'adresse IP d'un hôte à partir de son identifiant
#------------------------------------------------------------------------------
sub getHostIpById {
    my( $dbHandler, $hostId ) = @_;

    if( !defined($hostId) ) {
        write_log( "Identifiant de l'hôte non défini !", "W" );
        return undef;
    }elsif( $hostId !~ /^[0-9]+$/ ) {
        write_log( "Identifiant de l'hôte '".$hostId."' incorrect !", "W" );
        return undef;
    }elsif( !defined($dbHandler) ) {
        write_log( "Connection à la base de donnée incorrect !", "W" );
        return undef;
    }

    my $query = "SELECT host_ip FROM P_Host WHERE host_id='".$hostId."'";

    #
    # On execute la requete
    my $queryResult;
    if( !execQuery( $query, $dbHandler, \$queryResult ) ) {
        write_log( "Probleme lors de l'execution de la requete.", "W" );
        if( defined($queryResult) ) {
            write_log( $queryResult->err, "W" );
        }

        return undef;
    }

    if( !(my( $hostIp ) = $queryResult->fetchrow_array) ) {
        write_log( "Identifiant de l'hôte '".$hostId."' inconnu !", "W" );

        $queryResult->finish;
        return undef;
    }else{
        $queryResult->finish;
        return $hostIp;
    }

    return undef;

}

#------------------------------------------------------------------------------
# Permet de récupérer le nom d'un hôte à partir de son identifiant
#------------------------------------------------------------------------------
sub getHostNameById {
    my( $dbHandler, $hostId ) = @_;

    if( !defined($hostId) ) {
        write_log( "Identifiant de l'hôte non défini !", "W" );
        return undef;
    }elsif( $hostId !~ /^[0-9]+$/ ) {
        write_log( "Identifiant de l'hôte '".$hostId."' incorrect !", "W" );
        return undef;
    }elsif( !defined($dbHandler) ) {
        write_log( "Connection à la base de donnée incorrect !", "W" );
        return undef;
    }

    my $query = "SELECT host_name FROM P_Host WHERE host_id='".$hostId."'";

    #
    # On execute la requete
    my $queryResult;
    if( !execQuery( $query, $dbHandler, \$queryResult ) ) {
        write_log( "Probleme lors de l'execution de la requete.", "W" );
        if( defined($queryResult) ) {
            write_log( $queryResult->err, "W" );
        }

        return undef;
    }

    if( !(my( $hostName ) = $queryResult->fetchrow_array) ) {
        write_log( "Identifiant de l'hôte '".$hostId."' inconnu !", "W" );

        $queryResult->finish;
        return undef;
    }else{
        $queryResult->finish;
        return $hostName;
    }

    return undef;

}

#------------------------------------------------------------------------------
# Permet d'obtenir la liste des serveurs de boîtes à lettres
#------------------------------------------------------------------------------
sub getMailServerList {
    my( $dbHandler ) = @_;

    if( !defined($dbHandler) ) {
        write_log( "Connection à la base de donnée incorrect !", "W" );
        return undef;
    }

    my $query = "SELECT mail_value FROM Mail WHERE mail_name LIKE '%mailserver'";

    #
    # On execute la requete
    my $queryResult;
    if( !execQuery( $query, $dbHandler, \$queryResult ) ) {
        write_log( "Probleme lors de l'execution de la requete.", "W" );
        if( defined($queryResult) ) {
            write_log( $queryResult->err, "W" );
        }

        return undef;
    }

    my @serverList;
    while( my( $serverId ) = $queryResult->fetchrow_array ) {
        push( @serverList, $serverId );
    }

    return \@serverList;
}

#------------------------------------------------------------------------------
# Permet d'obtenir la liste des domaines OBM
#------------------------------------------------------------------------------
sub getDomains {
    my( $dbHandler, $obmDomainId ) = @_;
    my $domainList = &OBM::utils::cloneStruct(OBM::Parameters::toolBoxConf::domainList);

    if( !defined($dbHandler) ) {
        write_log( "Connection à la base de donnée incorrect !", "W" );
        return undef;
    }


    # Création du meta-domaine
    $domainList[0] = &OBM::utils::cloneStruct(OBM::Parameters::toolBoxConf::domainDesc);
    $domainList[0]->{"meta_domain"} = 1;
    $domainList[0]->{"domain_id"} = 0;
    $domainList[0]->{"domain_label"} = "metadomain";
    $domainList[0]->{"domain_name"} = "metadomain";
    $domainList[0]->{"domain_desc"} = "Informations de l'annuaire ne faisant partie d'aucun domaine";


    # Requete de recuperation des informations des domaines
    my $queryDomain = "SELECT domain_id, domain_label, domain_description, domain_name, domain_alias FROM P_Domain";
    if( defined($obmDomainId) && $obmDomainId =~ /^\d+$/ ) {
        $queryDomain .= " WHERE domain_id=".$obmDomainId;
    }

    # On execute la requete concernant les domaines
    my $queryDomainResult;
    if( !execQuery( $queryDomain, $dbHandler, \$queryDomainResult ) ) {
        write_log( "Probleme lors de l'execution de la requete.", "W" );
        if( defined($queryDomainResult) ) {
            write_log( $queryDomainResult->err, "W" );
        }

        return undef;
    }

    while( my( $domainId, $domainLabel, $domainDesc, $domainName, $domainAlias ) = $queryDomainResult->fetchrow_array ) {
        my $currentDomain = &OBM::utils::cloneStruct(OBM::Parameters::toolBoxConf::domainDesc);
        $currentDomain->{"meta_domain"} = 0;
        $currentDomain->{"domain_id"} = $domainId;
        $currentDomain->{"domain_label"} = $domainLabel;
        $currentDomain->{"domain_desc"} = $domainDesc;
        $currentDomain->{"domain_name"} = $domainName;
        $currentDomain->{"domain_dn"} = $domainName;

        $currentDomain->{"domain_alias"} = [];
        if( defined($domainAlias) ) {
            push( @{$currentDomain->{"domain_alias"}}, split( /\r\n/, $domainAlias ) );
        }

        push( @{domainList}, $currentDomain );
    }

    return \@domainList;
}
