#########################################################################
# OBM           - File : OBM::passwd.pm (Perl Module)                   #
#               - Desc : Librairie Perl pour aliamin                    #
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
require Exporter;

@ISA = qw(Exporter);
@EXPORT_const = qw();
@EXPORT_function = qw( md5sumToMd5 toMd5 toSsha plainToMd5sum );
@EXPORT = (@EXPORT_function, @EXPORT_const);
@EXPORT_OK = qw();

# Necessaire pour le bon fonctionnement du package
$debug=1;

    
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
    $cryptPass->add($pass);

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
