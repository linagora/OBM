# Perl #

use ExtUtils::MakeMaker qw(WriteMakefile);
use ExtUtils::Manifest qw(mkmanifest);


mkmanifest();


WriteMakefile(
			NAME      => "ObmSatellite",
         VERSION => "0.1",
			PMLIBDIRS => (["ObmSatellite"]),
);











