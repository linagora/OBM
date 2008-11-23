package OBM::Tools::passwd;


$debug = 1;


use 5.006_001;
use strict;
use vars qw( @EXPORT_OK $VERSION );
use base qw(Exporter);

use MIME::Base64;
use Digest::SHA1;
use Digest::MD5;
use Crypt::SmbHash;

$VERSION = "1.0";
@EXPORT_OK = qw(    _md5sumToMd5
                    _plainToMd5sum
                    _toMd5
                    _toSsha
                    _convertPasswd
                    _getNTLMPasswd
               );



sub _md5sumToMd5 {
    my $self = shift;
    my( $passwdMd5sum ) = @_;

    return encode_base64( pack( "H*", $passwdMd5sum ) );
}


sub _plainToMd5sum {
    my $self = shift;
    my( $passwdPlain ) = @_;

    my $md5 = Digest::MD5->new;
    $md5->add( $passwdPlain );

    return $md5->hexdigest;
}


sub _toMd5 {
    my $self = shift;
    my( $passwdPlain ) = @_;

    my $cryptPass = Digest::MD5->new;
    $cryptPass->add($passwdPlain);

    return encode_base64($cryptPass->digest,'');
}


sub _toSsha {
    my $self = shift;
    my( $passwdPlain ) = @_;

    my $salt = join '', ('a'..'z')[rand 26,rand 26,rand 26,rand 26,rand 26,rand 26,rand 26,rand 26];
    my $cryptPass = Digest::SHA1->new;

    $cryptPass->add( $passwdPlain );
    $cryptPass->add( $salt );

    return encode_base64($cryptPass->digest . $salt,'');
}


sub _convertPasswd {
    my $self = shift;
    my( $passwdType, $passwd ) = @_;
    my $userPasswd = undef;

    if( !defined($passwdType) || !defined($passwd) ) {
        return undef;
    }

    SWITCH: {
        if( uc($passwdType) eq 'PLAIN' ) {
            $userPasswd = '{SSHA}'.$self->_toSsha( $passwd );
            last SWITCH;
        }

        if( uc($passwdType) eq 'MD5SUM' ) {
            $userPasswd = '{MD5}'.$self->_md5sumToMd5( $passwd );
            last SWITCH;
        }

        if( uc($passwdType) eq 'CRYPT' ) {
            $userPasswd = '{CRYPT}'.$passwd;
            last SWITCH;
        }
    }

    return $userPasswd;
}


sub _getNTLMPasswd {
    my $self = shift;
    my( $plainPasswd, $lmPasswd, $ntPasswd ) = @_;

    if( !defined($plainPasswd) ) {
        return 1;
    }

    ( $$lmPasswd, $$ntPasswd ) = ntlmgen( $plainPasswd );

    return 0;
}
