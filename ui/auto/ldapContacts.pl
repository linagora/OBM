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

#####################################################################
# OBM               - File : ldapContacts.pl                        #
#                   - Desc : Script permettant de gérer le contenu  #
#                   de la branche des contacts publics              #
#####################################################################

use strict;
use Getopt::Long;

require OBM::Ldap::ldapEngine;
require OBM::Update::utils;
require OBM::Entities::obmContact;
require OBM::Tools::obmDbHandler;

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

sub getParameter {
    my( $parameters ) = @_;

    # Analyse de la ligne de commande
    &GetOptions( $parameters, "global", "incremental", "help" );

    if( exists($parameters->{'incremental'}) && exists($parameters->{'global'}) ) {
        print STDERR 'ERREUR: Parametres \'--incremental\' et \'--global\' incompatibles\n\n';
        $parameters->{'help'} = '';

    }elsif( exists($parameters->{'incremental'}) ) {
        &OBM::toolBox::write_log( 'Mise a jour incrementale', 'W' );
        $parameters->{'incremental'} = 1;
        $parameters->{'global'} = 0;

    }elsif( exists($parameters->{'global'}) ) {
        &OBM::toolBox::write_log( 'Mise a jour globale', 'W' );
        $parameters->{'incremental'} = 0;
        $parameters->{'global'} = 1;

    }else{
        &OBM::toolBox::write_log( 'Mise a jour globale', 'W' );
        $parameters->{'incremental'} = 0;
        $parameters->{'global'} = 1;
    }

    if( exists( $parameters->{'help'} ) ) {
        &OBM::toolBox::write_log( 'Affichage de l\'aide', 'WC' );
        print 'Script permettant de faire une synchronisation des contacts publics d\'OBM dans une branche de l\'annuaire LDAP (ou=contacts par défaut)'."\n\n";

        print 'Veuillez indiquer le critere de mise a jour :'."\n";
        print "Syntaxe: $0 [--global | --incremental]\n";
        print "\t".'--global : Fait une mise a jour globale des contacts ;'."\n";
        print "\t".'--incremental : Fait une mise a jour incrementale du domaine (Option par defaut)'."\n";

        exit 0;
    }
}

# On prépare le log
my ($scriptname) = ($0=~'.*/([^/]+)');
&OBM::toolBox::write_log( $scriptname.': ', 'O' );

# Traitement des paramètres
&OBM::toolBox::write_log( 'Analyse des parametres du script', 'W', 3 );
my %parameters;
&getParameter( \%parameters );

# On se connecte à la base
my $dbHandler = OBM::Tools::obmDbHandler->instance();
if( !defined($dbHandler) ) {
    &OBM::toolBox::write_log( 'Probleme lors de l\'ouverture de la base de donnees', 'WC', 0 );
    exit 1;
}


# Obtention de la liste des domaines
&OBM::toolBox::write_log( 'Obtention des domaines OBM', 'W' );
my $domainList = &OBM::Update::utils::getDomains( undef );

# Obtention des serveurs LDAP par domaines
&OBM::Update::utils::getLdapServer( $domainList );

# On démarre un moteur LDAP
&OBM::toolBox::write_log( 'Initialisation du moteur LDAP', 'W' );
my $ldapEngine = OBM::Ldap::ldapEngine->new( $domainList );
$ldapEngine->init();

# Traitement des contacts publics, domaine/domaine
for( my $i=0; $i<=$#{$domainList}; $i++ ) {
    my $domain = $domainList->[$i];
    my $parentDn = undef;
    my $timeStamp;
    
    my $object = OBM::Entities::obmContact->new( 0, 1, 0, $domain->{'domain_id'} );
    if( $ldapEngine->getObjectParentDN( $object, \$parentDn ) ) {
        # Pas de branche LDAP définie pour les objets contacts
        next;
    }
    

    if( $parameters{'incremental'} ) {
        # Obtention du timestamp pour la synchronisation des contacts publics
        &OBM::toolBox::write_log( 'Obtention de la date de derniere synchronisation pour le domaine \''.$domain->{'domain_label'}.'\'', 'W' );
        my $query = 'SELECT domainpropertyvalue_value
                        FROM DomainPropertyValue
                        WHERE domainpropertyvalue_domain_id=\''.$domain->{'domain_id'}.'\'
                        AND domainpropertyvalue_property_key = \'last_public_contact_export\'
                        LIMIT 1';
        my $queryResult;
        if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
            exit 1;
        }

        $timeStamp = $queryResult->fetchrow_array();
        if( !defined($timeStamp) ) {
            if( exists($parameters{'incremental'}) ) {
                &OBM::toolBox::write_log( 'Date de derniere synchronisation inconnue, execution en mode global', 'W' );
                $parameters{'global'} = 1;
                $parameters{'incremental'} = 0;
            }
        }
    }

    if( $parameters{'global'} ) {
        &OBM::toolBox::write_log( 'Synchronisation complete des contacts publics du domaine \''.$domain->{'domain_id'}.'\'', 'W' );
        $timeStamp = 0;
    }else {
        &OBM::toolBox::write_log( 'Synchronisation des contacts publics du domaine \''.$domain->{'domain_id'}.'\', depuis le \''.$timeStamp.'\'', 'W' );
    }
    
    
    &OBM::toolBox::write_log( 'Suppression des contacts publics ou prives du domaine \''. $domain->{'domain_label'}.'\'', 'W' );	
    
    my $query = '(SELECT contact_id
                    FROM Contact
                    WHERE (contact_privacy=\'1\' OR contact_archive=\'1\')
                    AND contact_domain_id=\''.$domain->{'domain_id'}.'\'
                    AND UNIX_TIMESTAMP(contact_timeupdate) > \''.$timeStamp.'\')
                UNION
                (SELECT deletedcontact_contact_id AS contact_id
                    FROM DeletedContact
                    WHERE UNIX_TIMESTAMP(deletedcontact_timestamp) > \''.$timeStamp.'\')';

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        exit 1;
    }
    
    while( my( $contactId ) = $queryResult->fetchrow_array() ) {
        my $object = OBM::Entities::obmContact->new( 0, 1, $contactId, $domain->{'domain_id'} );

        my $entryDn = $object->getLdapDnPrefix();
        if( !defined( $entryDn ) ) {
            &OBM::toolBox::write_log( 'Erreur: description LDAP de l\'objet invalide : '.$object->getEntityDescription(), 'W', 1 );

        }else {
            &OBM::toolBox::write_log( 'Suppression de l\'objet : '.$object->getEntityDescription(), 'W', 1 );

            my $ldapEntry = $ldapEngine->findDn($entryDn.','.$parentDn);
            if( defined( $ldapEntry ) ) {
                if( !$ldapEngine->deleteLdapEntity( $ldapEntry ) ) {
                    &OBM::toolBox::write_log( 'Erreur: a la suppression de l\'objet : '.$object->getEntityDescription(), 'W', 1 );
                }

            }else {
                &OBM::toolBox::write_log( 'Objet non present dans l\'annuaire : '.$object->getEntityDescription(), 'W', 2 );
            }
        }
    }

   
    &OBM::toolBox::write_log( 'Ajout ou mise a jour des contacts publics du domaine \''. $domain->{'domain_label'}.'\'', 'W' );
    
    $query = 'SELECT contact_id
                    FROM Contact
                    WHERE contact_privacy=\'0\'
                    AND contact_archive=\'0\'
                    AND contact_domain_id=\''.$domain->{'domain_id'}.'\'
                    AND UNIX_TIMESTAMP(contact_timeupdate) > \''.$timeStamp.'\'';
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        exit 1;
    }
    
    while( my( $contactId ) = $queryResult->fetchrow_array() ) {
        my $object = OBM::Entities::obmContact->new( 0, 0, $contactId );
        &OBM::toolBox::write_log( 'Ajout de l\'objet : '.$object->getEntityDescription(), 'W', 2 );

        if( !$object->getEntity( $domain ) ) {
            &OBM::toolBox::write_log( 'Erreur: description LDAP de l\'objet invalide : '.$object->getEntityDescription(), 'W', 1 );
            next;
        }
	
        if( !$ldapEngine->update( $object ) ) {
            &OBM::toolBox::write_log( 'Erreur: a l\'ajout de l\'objet : '.$object->getEntityDescription(), 'W', 1 );
        }
    }


    $timeStamp = time();
    &OBM::toolBox::write_log( 'Synchronisation terminee avec succes, mise a jour de la date de derniere synchronisation', 'W', 2 );
    $query = 'UPDATE DomainPropertyValue SET
                domainpropertyvalue_value=\''.$timeStamp.'\'
              WHERE
                domainpropertyvalue_domain_id='.$domain->{'domain_id'}.' AND
                domainpropertyvalue_property_key=\'last_public_contact_export\'';

    my $result = $dbHandler->execQuery( $query, \$queryResult );
    if( !defined($result) ) {
        exit 1;
    }elsif( $result == 0 ) {
        $query = 'INSERT INTO DomainPropertyValue
                    (domainpropertyvalue_domain_id, domainpropertyvalue_property_key, domainpropertyvalue_value)
                  VALUES
                    ('.$domain->{'domain_id'}.', \'last_public_contact_export\', \''.$timeStamp.'\')';

        if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
            exit 1;
        }
    }
}

# On arrête le moteur LDAP
$ldapEngine->destroy();

# On referme la connexion à la base
$dbHandler->destroy();

# On ferme le log
&OBM::toolBox::write_log( 'Fin du traitement', 'W' );
&OBM::toolBox::write_log( '', 'C' );

exit 0;

# Perldoc

=head1 NAME

ldapContacts.pl - OBM administration to publish public contact in LDAP

=head1 SYNOPSIS

    # Publish all public contacts for all OBM domains
    $ ldapContacts.pl --global

    # Incrmental LDAP public contacts update for last script execution
    $ ldapContacts.pl --incremental

=head1 COMMANDS

=over 4

=item C<global> : global public contacts update

=item C<incremental> : incremental public contacts update

=item C<help> : display help

=back

This script must be run by system cron.

This script will do nothing if 'obm-contact' option, from 'obm_conf.ini', is false.

This script generate log via syslog.
