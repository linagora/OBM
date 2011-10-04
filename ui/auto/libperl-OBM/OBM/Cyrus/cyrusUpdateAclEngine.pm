package OBM::Cyrus::cyrusUpdateAclEngine;

$VERSION = '1.0';

use OBM::Cyrus::cyrusEngine;
use OBM::Log::log;
@ISA = ('OBM::Cyrus::cyrusEngine', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub _doWork {
    my $self = shift;

    my $entity = $self->{'currentEntity'};
    if( !defined($entity) ) {
        return 1;
    }


    my %srvBalDesc;
    if( !$self->isMailboxExist( \%srvBalDesc, $entity->getMailboxPrefix(), $entity->getMailboxName('current') ) ) {
        $self->_log( 'la BAL '.$entity->getDescription().' n\'existe pas', 0 );
        return 1;
    }


    if( $entity->getUpdateLinks() && $self->_imapSetMailboxAcls() ) {
        return 1;
    }

    return 0;
}

