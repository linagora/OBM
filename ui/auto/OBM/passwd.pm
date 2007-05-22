#########################################################################
# OBM           - File : OBM::passwd.pm (Perl Module)                   #
#               - Desc : Librairie Perl pour OBM                        #
#               Les fonctions implementant les différents algortihmes   #
#               de chiffrage necessaires                                #
#########################################################################
# Cree le 2004-10-06                                                    #
#########################################################################
# $Id$                  #
#########################################################################
package OBM::passwd;

use OBM::Parameters::common;
use MIME::Base64;
use Digest::SHA1;
use Digest::MD5;
use Crypt::SmbHash;

require Exporter;
use strict;


sub md5sumToMd5 {
    my( $passwdMd5sum ) = @_;

    return encode_base64( pack( "H*", $passwdMd5sum ) );
}


sub plainToMd5sum {
    my( $passwdPlain ) = @_;

    my $md5 = Digest::MD5->new;
    $md5->add( $passwdPlain );

    return $md5->hexdigest;
}


sub toMd5 {
    my( $passwdPlain ) = @_;

    my $cryptPass = Digest::MD5->new;
    $cryptPass->add($passwdPlain);

    return encode_base64($cryptPass->digest,'');
}


sub toSsha {
    my( $passwdPlain ) = @_;

    my $salt = join '', ('a'..'z')[rand 26,rand 26,rand 26,rand 26,rand 26,rand 26,rand 26,rand 26];
    my $cryptPass = Digest::SHA1->new;

    $cryptPass->add( $passwdPlain );
    $cryptPass->add( $salt );

    return encode_base64($cryptPass->digest . $salt,'');
}


sub convertPasswd {
    my( $passwdType, $passwd ) = @_;
    my $userPasswd = undef;

    if( !defined($passwdType) || !defined($passwd) ) {
        return undef;
    }

    SWITCH: {
        if( uc($passwdType) eq "PLAIN" ) {
            $userPasswd = "{SSHA}".&OBM::passwd::toSsha( $passwd );
            last SWITCH;
        }

        if( uc($passwdType) eq "MD5SUM" ) {
            $userPasswd = "{MD5}".&OBM::passwd::md5sumToMd5( $passwd );
            last SWITCH;
        }

        if( uc($passwdType) eq "CRYPT" ) {
            $userPasswd = "{CRYPT}".$passwd;
            last SWITCH;
        }
    }

    return $userPasswd;
}


sub getNTLMPasswd {
    my( $plainPasswd, $lmPasswd, $ntPasswd ) = @_;

    if( !defined($plainPasswd) ) {
        return 1;
    }

    ( $$lmPasswd, $$ntPasswd ) = ntlmgen( $plainPasswd );

    return 0;
}
