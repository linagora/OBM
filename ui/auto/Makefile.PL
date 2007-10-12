# Perl #

use ExtUtils::MakeMaker qw(WriteMakefile);
use ExtUtils::Manifest qw(mkmanifest);

mkmanifest();


WriteMakefile(
			NAME      => "OBM",
         VERSION => "0.1",
			PMLIBDIRS => (["OBM"]),
);











