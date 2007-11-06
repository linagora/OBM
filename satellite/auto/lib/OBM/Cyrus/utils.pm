package OBM::Cyrus::utils;

require Exporter;

use strict;


sub sieveGetHeaders {
    my( $oldSieveScript, $headers ) = @_;

    while( ( $#{$oldSieveScript}>=0 ) && ( $oldSieveScript->[0] =~ /^(require|#|\s+|$)/ ) ) {
        if( $oldSieveScript->[0] !~ /OBM2/ ) {
            push( @{$headers}, $oldSieveScript->[0]."\n" );
        }else {
            last;
        }
        splice( @{$oldSieveScript}, 0, 1 );
    }

    return 0;
}


sub sieveDeleteMark {
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

