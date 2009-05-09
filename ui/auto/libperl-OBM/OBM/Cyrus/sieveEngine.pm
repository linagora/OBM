package OBM::Cyrus::sieveEngine;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


use OBM::Tools::commonMethods qw(
        _log
        dump
        );
use OBM::Ldap::utils qw(
        _modifyAttr
        );
use Cyrus::SIEVE::managesieve;


sub new {
    my $class = shift;

    my $self = bless { }, $class;

    require OBM::Parameters::common;
    if( !$OBM::Parameters::common::obmModules->{'mail'} ) {
        $self->_log( 'module OBM-MAIL désactivé, moteur non démarré', 3 );
        return '0 but true';
    }

    require OBM::Cyrus::cyrusServers;
    if( !($self->{'sieveServers'} = OBM::Cyrus::cyrusServers->instance()) ) {
        $self->_log( 'initialisation du gestionnaire de serveur Cyrus impossible', 3 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub _doWork {
    my $self = shift;

    # Checking entity...
    my $entity = $self->{'currentEntity'};
    if( !defined($self->{'currentEntity'}) ) {
        $self->_log( 'entite à mettre à jour non défini', 3 );
        return 1;
    }

    my $entityMailboxName = $self->{'currentEntity'}->getMailboxName( 'new' );
    if( !defined($entityMailboxName) ) {
        $self->_log( 'l\'entité '.$self->{'currentEntity'}->getDescription().' n\'a pas de nom de boîte à lettres', 3 );
        return 1;
    }

    # Checking Sieve server...
    if( !defined($self->{'currentSieveSrv'}) ) {
        $self->_log( 'pas de serveur sieve associé à l\'entité '.$self->{'currentEntity'}->getDescription(), 3 );
        return 1;
    }

    my $sieveSrvConn = $self->{'currentSieveSrv'}->getSieveServerConn( $entity->getDomainId(), $entity->getMailboxName( 'new' ) );
    if( !defined($sieveSrvConn) ) {
        $self->_log( 'impossible de se sonnecter au serveur Sieve '.$self->{'currentSieveSrv'}->getDescription(), 2 );
        return 1;
    }


    # Getting active script name
    my $sieveScriptName;
    sieve_list( $sieveSrvConn, sub{ my( $scriptName, $state) = @_; $sieveScriptName = $scriptName if ($state); } );

    # If no active script, create new
    if( !$sieveScriptName ) {
        $sieveScriptName = $entityMailboxName.'.sieve';
        $sieveScriptName =~ s/@/-/g;
    }


    my $currentScriptString = '';
    # Get server sieveScriptName sieve script content if exists
    sieve_get( $sieveSrvConn, $sieveScriptName, $currentScriptString );
    my @oldSieveScript;
    if( defined($currentScriptString) ) {
        @oldSieveScript = split( /\n/, $currentScriptString );
    }


    # Create new sieve script
    $self->_log( 'création du nouveau script Sieve \''.$sieveScriptName.'\' de '.$self->{'currentEntity'}->getDescription(), 2 );
    my @newSieveScript;
    if( $self->_updateSieveScript( \@oldSieveScript, \@newSieveScript ) ) {
        $self->_log( 'problème à la création du script sieve de '.$self->{'currentEntity'}->getDescription(), 2 );
        sieve_logout( $sieveSrvConn );
        return 1;
    }


    $self->_log( 'suppression du script Sieve \''.$sieveScriptName.'\' de '.$self->{'currentEntity'}->getDescription(), 2 );
    # Disable old Sieve script
    sieve_activate( $sieveSrvConn, '' );
    # Delete old Sieve script
    sieve_delete( $sieveSrvConn, $sieveScriptName );

    if( $#newSieveScript >= 0 ) {
        $self->_log( 'mise a jour du script Sieve \''.$sieveScriptName.'\' de '.$self->{'currentEntity'}->getDescription(), 2 );

        if( sieve_put( $sieveSrvConn, $sieveScriptName, join("\n", @newSieveScript) ) ) {
            my $errstr = sieve_get_error( $sieveSrvConn );
            $errstr = 'Sieve - erreur inconnue.' if(!defined($errstr));
            $self->_log( 'erreur lors de la mise à jour du script Sieve : '.$errstr , 2 );
            sieve_logout( $sieveSrvConn );
            return 1;
        }

        $self->_log( 'activation du script Sieve \''.$sieveScriptName.'\' de '.$self->{'currentEntity'}->getDescription(), 2 );

        # On active le nouveau script
        if( sieve_activate( $sieveSrvConn, $sieveScriptName ) ) {
            my $errstr = sieve_get_error( $sieveSrvConn );
            $errstr = 'Sieve - erreur inconnue.' if(!defined($errstr));
            $self->_log( 'probleme lors de l\'activation du script Sieve : '.$errstr, 2 );
            sieve_logout( $sieveSrvConn );
            return 1;
        }
    }

    sieve_logout( $sieveSrvConn );
    return 0;
}


sub _updateSieveScript {
    my $self = shift;
    my( $oldSieveScript, $newSieveScript ) = @_;

    # Recuperation des en-tetes 'require' de l'ancien script
    my @headers;
    $self->_sieveGetHeaders( $oldSieveScript, \@headers );

    my @vacation;
    if( $self->_updateSieveVacation( \@headers, $oldSieveScript, \@vacation ) ) {
        return 1;
    }

    my @nomade;
    if( $self->_updateSieveNomade( \@headers, $oldSieveScript, \@nomade ) ) {
        return 1;
    }

    splice( @{$newSieveScript}, 0 );

    if( ( $#vacation < 0 ) && ( $#nomade < 0 ) && ($#{$oldSieveScript} < 0) ) {
        return 0;
    }

    push( @{$newSieveScript}, @headers );
    push( @{$newSieveScript}, @vacation );
    push( @{$newSieveScript}, @nomade );
    push( @{$newSieveScript}, @{$oldSieveScript} );

    return 0;
}


sub _updateSieveVacation {
    my $self = shift;
    my( $headers, $oldSieveScript, $newSieveScript ) = @_;
    my $vacationMark = "# OBM2 - Vacation";

    if( !defined($self->{'currentEntity'}) ) {
        $self->_log( 'entite à mettre à jour non défini', 3 );
        return 1;
    }


    if( my $vacationMsg = $self->{'currentEntity'}->getSieveVacation() ) {
        # Checking sieve script headers
        my $i=0;
        while( ( $i<=$#{$headers} ) && ( $headers->[$i] !~ /[^#]*require \"vacation\";/) ) {
            $i++;
        }

        if( $i > $#{$headers} ) {
            unshift( @{$headers}, 'require "vacation";' );
        }

        $self->_log( 'gestion du message d\'absence de '.$self->{'currentEntity'}->getDescription(), 2 );

        push( @{$newSieveScript}, $vacationMark );
        push( @{$newSieveScript}, $vacationMsg );
        push( @{$newSieveScript}, $vacationMark );
    }

    # Delete vacation part from old script
    $self->_sieveDeleteMark( $oldSieveScript, $vacationMark );

    return 0;
}


sub _updateSieveNomade {
    my $self = shift;
    my( $headers, $oldSieveScript, $newSieveScript ) = @_;
    my $nomadeMark = "# OBM2 - Nomade";

    if( !defined($self->{'currentEntity'}) ) {
        $self->_log( 'entite à mettre à jour non défini', 3 );
        return 1;
    }

    if( my $nomadeMsg = $self->{'currentEntity'}->getSieveNomade() ) {
        $self->_log( 'gestion de la redirection de '.$self->{'currentEntity'}->getDescription(), 2 );

        push( @{$newSieveScript}, $nomadeMark );
        push( @{$newSieveScript}, $nomadeMsg );
        push( @{$newSieveScript}, $nomadeMark );
    }

    # Delete redirection part from old script
    $self->_sieveDeleteMark( $oldSieveScript, $nomadeMark );

    return 0;
}


sub _sieveGetHeaders {
    my $self = shift;
    my( $oldSieveScript, $headers ) = @_;

    while( ( $#{$oldSieveScript}>=0 ) && ( $oldSieveScript->[0] =~ /^(require|#|\s+|$)/ ) ) {
        if( $oldSieveScript->[0] !~ /OBM2/ ) {
            push( @{$headers}, $oldSieveScript->[0] );
        }else {
            last;
        }
        splice( @{$oldSieveScript}, 0, 1 );
    }

    return 0;
}


sub _sieveDeleteMark {
    my $self = shift;
    my( $sieveScript, $mark ) = @_;

    my $i=0;
    while( ( $i<=$#{$sieveScript} ) && ( $sieveScript->[$i] !~ /^$mark$/ ) ) {
        $i++
    }

    if( $i<=$#{$sieveScript} ) {
        splice( @{$sieveScript}, $i, 1 );

        while( ( $i<=$#{$sieveScript} ) && ( $sieveScript->[$i] !~ /^$mark$/ ) ) {
            splice( @{$sieveScript}, $i, 1 );
        }
        splice( @{$sieveScript}, $i, 1 );
    }

    return 0;
}


sub update {
    my $self = shift;
    my( $entity ) = @_;

    if( !defined($entity) ) {
        $self->_log( 'entité non définie', 3 );
        return 1;
    }elsif( !ref($entity) ) {
        $self->_log( 'entité incorrecte', 3 );
        return 1;
    }
    $self->{'currentEntity'} = $entity;

    # If entity don't have Sieve dependancy, we do nothing and it's not an error
    if( !$entity->isSieveAvailable() ) {
        $self->_log( 'entité '.$entity->getDescription().' n\'a aucune représentation Sieve', 3 );
        return 0;
    }

    # If entity is deleting, we do nothing and it's not an error
    if( $entity->getDelete() ) {
        $self->_log( 'suppression de l\'entité '.$entity->getDescription(), 3 );
        return 0;
    }

    # If entity is archiving, we do nothing and it's not an error
    if( $entity->getArchive() ) {
        $self->_log( 'pas de gestion de SIEVE pour l\'entité archivée '.$entity->getDescription(), 2 );
        return 0;
    }

    # If entity don't have mail right, we do nothing and it's not an error
    if( !$entity->isMailActive() ) {
        $self->_log( 'droit mail désactivé pour l\'objet : '.$entity->getDescription().', pas de gestion Sieve', 2 );
        return 0;
    }

    # If entity haven't be update, we do nothing and it's no an error
    if( !$entity->getUpdateEntity() ) {
        $self->_log( 'l\'entité '.$entity->getDescription().' n\'a pas été mise à jour, Sieve n\'a pas besoin d\'être mis à jour', 3 );
        return 0;
    }


    # Get user BAL server object
    my $mailServerId = $entity->getMailServerId();
    if( !defined($mailServerId) && $entity->isMailActive() && !$entity->getArchive() ) {
        $self->_log( 'serveur de courrier IMAP non defini et droit mail actif - erreur', 0 );
        return 1;
    }elsif( !defined($mailServerId) && (!$entity->isMailActive() || $entity->getArchive()) ) {
        $self->_log( 'serveur de courrier IMAP non defini et droit mail inactif - succés', 0 );
        return 0;
    }elsif( !defined($mailServerId) ) {
        $self->_log( 'serveur de courrier IMAP non defini - erreur', 0 );
        return 1;
    }

    # Get Sieve server connection
    $self->{'currentSieveSrv'} = $self->{'sieveServers'}->getEntityCyrusServer( $entity );
    if( !defined($self->{'currentSieveSrv'}) ) {
        $self->_log( 'serveur de courrier Sieve d\'identifiant \''.$entity->getMailServerId().'\' inconnu - Operation annulée !', 0 );
        return 1;
    }


    # Do stuff...
    if( $self->_doWork() ) {
        $self->_log( 'problème de traitement de '.$entity->getDescription().' - Operation annulee !', 0 );
        return 1;
    }

    return 0;
}
