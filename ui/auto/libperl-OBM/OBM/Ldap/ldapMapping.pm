package OBM::Ldap::ldapMapping;

$VERSION = '1.0';

use OBM::Tools::commonMethods;
use Class::Singleton;
@ISA = ('Class::Singleton', 'OBM::Tools::commonMethods');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub _new_instance {
    my $class = shift;

    my $self = bless { }, $class;

    $self->{'xmlFile'} = '/etc/obm/automateLdapMapping.xml';

    if ( ! -f $self->{'xmlFile'} ) {
        $self->_log( 'Erreur fatale: le fichier XML de description de l\'annuaire LDAP \''.$self->{'xmlFile'}.'\' n\'est pas accessible', 0 );
        return undef;
    }

    require XML::Simple;
    my $xmlParser = XML::Simple->new( ForceArray => [ 'entity', 'objectclass', 'map' ] );
    $self->_log( 'chargement du fichier XML de description de l\'annuaire LDAP \''.$self->{'xmlFile'}.'\'', 2 );
    $self->{'xml'} = $xmlParser->XMLin( $self->{'xmlFile'} );

    if( !defined($self->{'xml'}) ) {
        $self->_log( 'Erreur fatale: échec du chargement du fichier XML de description de l\'annuaire LDAP \''.$self->{'xmlFile'}.'\'', 0 );
        return undef;
    }

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub getObjectClass {
    my $self = shift;
    my( $entity, $currentObjectClass ) = @_;
    my $entityType = ref($entity);

    if( !defined($self->{'xml'}->{'entity'}->{$entityType}) ) {
        $self->_log( 'entité de type \''.$entityType.'\' inconnue', 3 );
        return undef;
    }

    my $objectClassDesc = $self->{'xml'}->{'entity'}->{$entityType}->{'objectclass'};
    if( !defined($objectClassDesc) || (ref($objectClassDesc) ne 'HASH') ) {
        $self->_log( 'pas d\'objectclass définis pour les entités de type \''.$entityType.'\'', 3 );
        return undef;
    }

    # lower case keys
    while( my( $key, $values ) = each(%{$objectClassDesc}) ) {
        my $lcKey = lc($key);
        if( $lcKey ne $key ) {
            $objectClassDesc->{$lcKey} = $values;
            delete($objectClassDesc->{$key});
        }
    }

    # add objectclass from LDAP entry
    if( defined($currentObjectClass) && (ref($currentObjectClass) eq 'ARRAY') ) {
        for( my $i=0; $i<=$#{$currentObjectClass}; $i++ ) {
            if( !exists($objectClassDesc->{lc($currentObjectClass->[$i])}) ) {
                $objectClassDesc->{$currentObjectClass->[$i]} = undef;
            }
        }
    }

    my @objectClass = keys( %{$objectClassDesc} );
    my @deletedObjectclass;
    my %realObjectClass;
    for( my $i=0; $i<=$#objectClass; $i++ ) {
        if( defined($objectClassDesc->{$objectClass[$i]}->{'condition'}) ) {
            my $condition = $objectClassDesc->{$objectClass[$i]}->{'condition'};
            if( defined($condition) && defined($entity->getDesc($condition)) && !$entity->getDesc($condition) ) {
                push( @deletedObjectclass, $objectClass[$i] );
                next;
            }
        }

        $realObjectClass{$objectClass[$i]} = '';
    }

    while( my( $oc, $desc ) = each(%{$objectClassDesc}) ) {
        if( defined($desc->{'condition'}) && $entity->getDesc($desc->{'condition'}) ) {
            $realObjectClass{$oc} = '';
        }
    }

    return {
        'objectClass' => eval{ my @realObjectClass = keys(%realObjectClass); return \@realObjectClass},
        'deletedObjectclass' => \@deletedObjectclass
        };
}


sub getRdn {
    my $self = shift;
    my( $entity ) = @_;
    my $entityType = ref($entity);

    if( !defined($self->{'xml'}->{'entity'}->{$entityType}) ) {
        $self->_log( 'entité de type \''.$entityType.'\' inconnue', 3 );
        return undef;
    }

    my $ldapEntityDesc = $self->{'xml'}->{'entity'}->{$entityType};
    if( !defined($ldapEntityDesc->{'rdn'}) ) {
        $self->_log( 'pas de RDN défini pour l\'entité de type \''.$entityType.'\'', 0 );
        return undef;
    }

    my $attrsMapping = $ldapEntityDesc->{'map'};
    if( !defined($attrsMapping) || (ref($attrsMapping) ne 'ARRAY') ) {
        $self->_log( 'mapping des attributs LDAP non définis ou incorrect, vérifiez le fichier XML', 0 );
        return undef;
    }

    for( my $i=0; $i<=$#{$attrsMapping}; $i++ ) {
        my $currentMapping = $attrsMapping->[$i];
        if( lc($currentMapping->{'ldap'}->{'name'}) eq lc($ldapEntityDesc->{'rdn'}->{'ldap'}->{'name'}) ) {
            return $currentMapping;
        }
    }

    $self->_log( 'mapping de l\'attribut LDAP \''.$ldapEntityDesc->{'rdn'}->{'ldap'}->{'name'}.'\', constituant le RDN de l\'entité de type \''.$entityType.'\', non défini', 0 );
    return undef;
}


sub getCurrentRdn {
    my $self = shift;
    my( $entity ) = @_;
    my $entityType = ref($entity);

    if( !defined($self->{'xml'}->{'entity'}->{$entityType}) ) {
        $self->_log( 'entité de type \''.$entityType.'\' inconnue', 3 );
        return undef;
    }

    my $ldapEntityDesc = $self->{'xml'}->{'entity'}->{$entityType};
    if( !defined($ldapEntityDesc->{'rdn'}) ) {
        $self->_log( 'pas de RDN défini pour l\'entité de type \''.$entityType.'\'', 0 );
        return undef;
    }

    return $ldapEntityDesc->{'rdn'};
}


sub getAllAttrsMapping {
    my $self = shift;
    my( $entity, $exception ) = @_;
    my $entityType = ref($entity);

    if( !defined($self->{'xml'}->{'entity'}->{$entityType}) ) {
        $self->_log( 'entité de type \''.$entityType.'\' inconnue', 3 );
        return undef;
    }

    my $ldapEntityDesc = $self->{'xml'}->{'entity'}->{$entityType};

    if( ref($ldapEntityDesc->{'map'}) ne 'ARRAY' ) {
        return [];
    }

    if( ref($exception) ne 'ARRAY' ) {
        return $ldapEntityDesc->{'map'};
    }

    my @ldapMapping;
    for( my $i=0; $i<=$#{$ldapEntityDesc->{'map'}}; $i++ ) {
        my $map = $ldapEntityDesc->{'map'}->[$i];
        my $desc = lc($map->{'desc'}->{'name'});
        
        my $j = 0;
        while( ($j<=$#{$exception}) && ($desc ne lc($exception->[$j])) ) {
            $j++;
        }

        if( $j>$#{$exception} ) {
            push( @ldapMapping, $map );
        }
    }

    return \@ldapMapping;
}


sub getAttrsMapping {
    my $self = shift;
    my( $entity, $attrList ) = @_;
    my $entityType = ref($entity);

    if( !defined($self->{'xml'}->{'entity'}->{$entityType}) ) {
        $self->_log( 'entité de type \''.$entityType.'\' inconnue', 3 );
        return undef;
    }

    my $ldapEntityDesc = $self->{'xml'}->{'entity'}->{$entityType};

    if( ref($ldapEntityDesc->{'map'}) ne 'ARRAY' ) {
        return [];
    }

    if( ref($attrList) ne 'ARRAY' ) {
        return [];
    }

    my @ldapMapping;
    for( my $i=0; $i<=$#{$attrList}; $i++ ) {
        my $attr = lc($attrList->[$i]);

        my $j = 0;
        while( $j<=$#{$ldapEntityDesc->{'map'}} ) {
            my $map = $ldapEntityDesc->{'map'}->[$j];

            if( $attr eq lc($map->{'desc'}->{'name'}) ) {
                push( @ldapMapping, $map );
                last;
            }

            $j++;
        }
    }

    return \@ldapMapping;
}
