#################################################################################
# Copyright (C) 2011-2012 Linagora
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version, provided you comply with the Additional Terms applicable for OBM
# software by Linagora pursuant to Section 7 of the GNU Affero General Public
# License, subsections (b), (c), and (e), pursuant to which you must notably (i)
# retain the displaying by the interactive user interfaces of the “OBM, Free
# Communication by Linagora” Logo with the “You are using the Open Source and
# free version of OBM developed and supported by Linagora. Contribute to OBM R&D
# by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
# links between OBM and obm.org, between Linagora and linagora.com, as well as
# between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
# from infringing Linagora intellectual property rights over its trademarks and
# commercial brands. Other Additional Terms apply, see
# <http://www.linagora.com/licenses/> for more details.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License and
# its applicable Additional Terms for OBM along with this program. If not, see
# <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
# version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
# applicable to the OBM software.
#################################################################################


package OBM::Cyrus::sieveEngine;

$VERSION = '1.0';

use OBM::Log::log;
@ISA = ('OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use Cyrus::SIEVE::managesieve;

use constant HEADER => '# rule';
use constant VACATION_HEADER => '# rule:[OBM Vacation]';
use constant NOMADE_HEADER => '# rule:[OBM Nomade]';


sub new {
    my $class = shift;

    my $self = bless { }, $class;

    require OBM::Parameters::common;
    if( !$OBM::Parameters::common::obmModules->{'mail'} ) {
        $self->_log( 'module OBM-MAIL désactivé, moteur non démarré', 2 );
        return '0 but true';
    }

    require OBM::Cyrus::cyrusServers;
    if( !($self->{'sieveServers'} = OBM::Cyrus::cyrusServers->instance()) ) {
        $self->_log( 'initialisation du gestionnaire de serveur Cyrus impossible', 0 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );
}


sub _doWork {
    my $self = shift;

    # Checking entity...
    my $entity = $self->{'currentEntity'};
    if( !defined($self->{'currentEntity'}) ) {
        $self->_log( 'entite à mettre à jour non défini', 1 );
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
        $self->_log( 'impossible de se sonnecter au serveur Sieve '.$self->{'currentSieveSrv'}->getDescription(), 1 );
        return 1;
    }

    # Getting active script name
    my @sieveScripts;
    sieve_list( $sieveSrvConn, sub{ my( $scriptName, $state) = @_; push @sieveScripts, [$scriptName, $state]; } );

    my @activeScripts = grep { $_->[1] } @sieveScripts;
    my $activeSieveScriptName;
    my $sieveScriptExists;
    my $sieveScriptName;
    if (@activeScripts == 1) {
        $sieveScriptName = $activeScripts[0]->[0];
        $sieveScriptExists = 1;
    }
    else {
        my $defaultSieveScriptName = $entityMailboxName.'.sieve';
        $defaultSieveScriptName =~ s/@/-/g;

        $sieveScriptName = $defaultSieveScriptName;
        $sieveScriptExists =  (grep { $_->[0] eq $sieveScriptName } @sieveScripts) > 0;
    }

    my @oldSieveScript;
    my $currentScriptString;
    if ($sieveScriptExists) {
        $self->_log("Sieve script $sieveScriptName retrieved", 3);
        # Get server sieveScriptName sieve script content if exists
        sieve_get( $sieveSrvConn, $sieveScriptName, $currentScriptString );
        if( defined($currentScriptString) ) {
            @oldSieveScript = split( /\n/, $currentScriptString );
        }
    }
    else {
        $self->_log("No active or default sieve script found, a fresh one ".
            "will be generated", 3);
    }


    # Create new sieve script
    $self->_log( 'création du nouveau script Sieve \''.$sieveScriptName.'\' de '.$self->{'currentEntity'}->getDescription(), 3 );
    my @newSieveScript;
    if( $self->_updateSieveScript( \@oldSieveScript, \@newSieveScript ) ) {
        $self->_log( 'problème à la création du script sieve de '.$self->{'currentEntity'}->getDescription(), 1 );
        sieve_logout( $sieveSrvConn );
        return 1;
    }


    $self->_log( 'suppression du script Sieve \''.$sieveScriptName.'\' de '.$self->{'currentEntity'}->getDescription(), 4 );
    # Disable old Sieve script
    sieve_activate( $sieveSrvConn, '' );
    # Delete old Sieve script
    sieve_delete( $sieveSrvConn, $sieveScriptName );

    if( $#newSieveScript >= 0 ) {
        $self->_log( 'mise a jour du script Sieve \''.$sieveScriptName.'\' de '.$self->{'currentEntity'}->getDescription(), 4 );

        foreach my $script (@newSieveScript) {
            $self->_log($script, 5);
        }

        if( sieve_put( $sieveSrvConn, $sieveScriptName, join("\n", @newSieveScript) ) ) {
            my $errstr = sieve_get_error( $sieveSrvConn );
            $errstr = 'Sieve - erreur inconnue.' if(!defined($errstr));
            $self->_log( 'erreur lors de la mise à jour du script Sieve : '.$errstr , 1 );
            sieve_logout( $sieveSrvConn );
            return 1;
        }

        $self->_log( 'activation du script Sieve \''.$sieveScriptName.'\' de '.$self->{'currentEntity'}->getDescription(), 3 );

        # On active le nouveau script
        if( sieve_activate( $sieveSrvConn, $sieveScriptName ) ) {
            my $errstr = sieve_get_error( $sieveSrvConn );
            $errstr = 'Sieve - erreur inconnue.' if(!defined($errstr));
            $self->_log( 'probleme lors de l\'activation du script Sieve : '.$errstr, 1 );
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

    if ($#headers >= 0) {
        push( @{$newSieveScript}, 'require [' . join(',', @headers) . '];' );
    }

    push( @{$newSieveScript}, @vacation );
    push( @{$newSieveScript}, @nomade );
    push( @{$newSieveScript}, @{$oldSieveScript} );

    return 0;
}


sub _updateSieveVacation {
    my $self = shift;
    my( $headers, $oldSieveScript, $newSieveScript ) = @_;
    my $vacationMark = VACATION_HEADER;

    if( !defined($self->{'currentEntity'}) ) {
        $self->_log( 'entite à mettre à jour non défini', 1 );
        return 1;
    }

    if( my $vacationMsg = $self->{'currentEntity'}->getSieveVacation() ) {
        if (!grep(/\"vacation\"/, @{$headers})) {
            push(@{$headers}, "\"vacation\"");
        }

        $self->_log( 'gestion du message d\'absence de '.$self->{'currentEntity'}->getDescription(), 3 );

        push( @{$newSieveScript}, $vacationMark );
        push( @{$newSieveScript}, $vacationMsg );
    }

    # Delete vacation part from old script
    $self->_sieveDeleteMark( $oldSieveScript, $vacationMark );
    $self->_sieveDeleteOldSchoolMark( $oldSieveScript, "# OBM2 - Vacation");

    return 0;
}


sub _updateSieveNomade {
    my $self = shift;
    my( $headers, $oldSieveScript, $newSieveScript ) = @_;
    my $nomadeMark = NOMADE_HEADER;

    if( !defined($self->{'currentEntity'}) ) {
        $self->_log( 'entite à mettre à jour non défini', 1 );
        return 1;
    }

    if( my $nomadeMsg = $self->{'currentEntity'}->getSieveNomade() ) {
        $self->_log( 'gestion de la redirection de '.$self->{'currentEntity'}->getDescription(), 4 );

        push( @{$newSieveScript}, $nomadeMark );
        push( @{$newSieveScript}, $nomadeMsg );
    }

    # Delete redirection part from old script
    $self->_sieveDeleteMark( $oldSieveScript, $nomadeMark );
    $self->_sieveDeleteOldSchoolMark( $oldSieveScript, "# OBM2 - Nomade");

    return 0;
}


sub _sieveGetHeaders {
    my $self = shift;
    my( $oldSieveScript, $headers ) = @_;

    while( ( $#{$oldSieveScript} >= 0 ) && ( $oldSieveScript->[0] =~ /^require\s+\[(.+)\];$/ ) ) {
        my @requires = split(',', $1);

        push(@{$headers}, @requires);
        shift(@{$oldSieveScript});
    }

    return 0;
}

sub indexOfMarks {
    my $start = 0;
    my $self = shift;
    my ($script, $mark, $endMark) = @_;

    while ($start <= $#{$script} && $script->[$start] !~ /^\Q$mark\E/) {
        $start++
    }
    
    if ($start <= $#{$script}) {
        my $end = $start + 1;

        while ($end <= $#{$script} && $script->[$end] !~ /^\Q$endMark\E/) {
            $end++
        }

        return ($start, $end);
    }

    return (-1, -1);
}

sub _sieveDeleteMark {
    my $self = shift;
    my ($sieveScript, $mark) = @_;
    my ($start, $end) = $self->indexOfMarks($sieveScript, $mark, HEADER);

    if ($start > -1) {
        splice(@{$sieveScript}, $start, $end - $start);
    }

    return 0;
}

sub _sieveDeleteOldSchoolMark {
    my $self = shift;
    my ($sieveScript, $mark) = @_;
    my ($start, $end) = $self->indexOfMarks($sieveScript, $mark, $mark);

    if ($start > -1) {
        splice(@{$sieveScript}, $start, $end - $start + 1);
    }

    return 0;
}


sub update {
    my $self = shift;
    my( $entity ) = @_;

    if( !defined($entity) ) {
        $self->_log( 'entité non définie', 1 );
        return 1;
    }elsif( !ref($entity) ) {
        $self->_log( 'entité incorrecte', 1 );
        return 1;
    }
    $self->{'currentEntity'} = $entity;

    # If entity don't have Sieve dependancy, we do nothing and it's not an error
    if( !$entity->isSieveAvailable() ) {
        $self->_log( 'entité '.$entity->getDescription().' n\'a aucune représentation Sieve', 4 );
        return 0;
    }

    # If entity is deleting, we do nothing and it's not an error
    if( $entity->getDelete() ) {
        $self->_log( 'suppression de l\'entité '.$entity->getDescription(), 3 );
        return 0;
    }

    # If entity is archiving, we do nothing and it's not an error
    if( $entity->getArchive() ) {
        $self->_log( 'pas de gestion de SIEVE pour l\'entité archivée '.$entity->getDescription(), 3 );
        return 0;
    }

    # If entity don't have mail right, we do nothing and it's not an error
    if( !$entity->isMailActive() ) {
        $self->_log( 'droit mail désactivé pour l\'objet : '.$entity->getDescription().', pas de gestion Sieve', 3 );
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
        $self->_log( 'serveur de courrier IMAP non defini et droit mail actif - erreur', 1 );
        return 1;
    }elsif( !defined($mailServerId) && (!$entity->isMailActive() || $entity->getArchive()) ) {
        $self->_log( 'serveur de courrier IMAP non defini et droit mail inactif - succés', 3 );
        return 0;
    }elsif( !defined($mailServerId) ) {
        $self->_log( 'serveur de courrier IMAP non defini - erreur', 1 );
        return 1;
    }

    # Get Sieve server connection
    $self->{'currentSieveSrv'} = $self->{'sieveServers'}->getEntityCyrusServer( $entity );
    if( !defined($self->{'currentSieveSrv'}) ) {
        $self->_log( 'serveur de courrier Sieve d\'identifiant \''.$entity->getMailServerId().'\' inconnu - Operation annulée !', 1 );
        return 1;
    }


    # Do stuff...
    if( $self->_doWork() ) {
        $self->_log( 'problème de traitement de '.$entity->getDescription().' - Operation annulee !', 1 );
        return 1;
    }

    return 0;
}
