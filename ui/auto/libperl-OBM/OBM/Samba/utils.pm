package OBM::Samba::utils;

require Exporter;

require OBM::Parameters::common;
use strict;


sub getUserSID {
    my( $domainSid, $userUID ) = @_;

    if( !$domainSid || !defined($userUID) ) {
        return undef;
    }

    my $userSID;

    SWITCH: {
        # Si nouvelle génération du SID et UID=0
        if( !$OBM::Parameters::common::sambaOldSidMapping && ($userUID == 0) ) {
            $userSID = $domainSid."-500";
            last SWITCH;
        }

        # Si nouvelle génération du SID
        if( !$OBM::Parameters::common::sambaOldSidMapping ) {
            $userSID = $domainSid."-".$userUID;
            last SWITCH;
        }

        # Si ancienne génération du SID et UID=0
        if( $OBM::Parameters::common::sambaOldSidMapping && ($userUID == 0) ) {
            $userSID = $domainSid."-2996";
            last SWITCH;
        }

        # Si ancienne génération du SID
        if( $OBM::Parameters::common::sambaOldSidMapping ) {
            $userSID = $domainSid."-".(2*$userUID+1000);
            last SWITCH;
        }

        $userSID = undef;
    }

    return $userSID;
}


sub getGroupSID {
    my( $domainSid, $groupGID ) = @_;

    if( !$domainSid || !defined($groupGID) ) {
        return undef;
    }

    my $groupSID = "";

    SWITCH: {
        # Groupe des administrateurs
        if( $groupGID == 512 ) {
            $groupSID = $domainSid."-512";
            last SWITCH;
        }

        # Groupe des utilisateurs
        if( $groupGID == 513 ) {
            $groupSID = $domainSid."-513";
            last SWITCH;
        }

        # Groupe des invites
        if( $groupGID == 514 ) {
            $groupSID = $domainSid."-514";
            last SWITCH;
        }

        # Si nouvelle génération du SID
        if( !$OBM::Parameters::common::sambaOldSidMapping ) {
            $groupSID = $domainSid."-".$groupGID;
        }

        # Si ancienne génération du SID
        if( $OBM::Parameters::common::sambaOldSidMapping ) {
            $groupSID = $domainSid."-".(2*$groupGID+1001);
        }

        $groupSID = undef;
    }

    return $groupSID;
}
