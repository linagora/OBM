package OBM::DbUpdater::userUpdater;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);


sub new {
    my $class = shift;

    my $self = bless { }, $class;

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub update {
    my $self = shift;
    my( $entity ) = @_;

    if( ref($entity) ne 'OBM::Entities::obmUser' ) {
        $self->_log( 'entité incorrecte, traitement impossible', 3 );
        return 1;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler;
    my $sth;
    if( !($dbHandler = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 3 );
        return 1;
    }

    if( $self->_delete($entity) ) {
        $self->_log( 'problème à la mise à jour BD du groupe '.$entity->getDescription(), 2 );
        return 1;
    }


    my $query;
    if( !$entity->getDelete() && $entity->getUpdateEntity() ) {
        $query = 'INSERT INTO P_UserObm
                    (   userobm_id,
                        userobm_domain_id,
                        userobm_timecreate,
                        userobm_userupdate,
                        userobm_usercreate,
                        userobm_local,
                        userobm_ext_id,
                        userobm_system,
                        userobm_archive,
                        userobm_timelastaccess,
                        userobm_login,
                        userobm_nb_login_failed,
                        userobm_password_type,
                        userobm_password,
                        userobm_password_dateexp,
                        userobm_account_dateexp,
                        userobm_perms,
                        userobm_delegation_target,
                        userobm_delegation,
                        userobm_calendar_version,
                        userobm_uid,
                        userobm_gid,
                        userobm_datebegin,
                        userobm_hidden,
                        userobm_kind,
                        userobm_lastname,
                        userobm_firstname,
                        userobm_title,
                        userobm_sound,
                        userobm_company,
                        userobm_direction,
                        userobm_service,
                        userobm_address1,
                        userobm_address2,
                        userobm_address3,
                        userobm_zipcode,
                        userobm_town,
                        userobm_expresspostal,
                        userobm_country_iso3166,
                        userobm_phone,
                        userobm_phone2,
                        userobm_mobile,
                        userobm_fax,
                        userobm_fax2,
                        userobm_web_perms,
                        userobm_web_list,
                        userobm_web_all,
                        userobm_mail_perms,
                        userobm_mail_ext_perms,
                        userobm_email,
                        userobm_mail_server_id,
                        userobm_mail_quota,
                        userobm_mail_quota_use,
                        userobm_mail_login_date,
                        userobm_nomade_perms,
                        userobm_nomade_enable,
                        userobm_nomade_local_copy,
                        userobm_nomade_datebegin,
                        userobm_nomade_dateend,
                        userobm_email_nomade,
                        userobm_vacation_enable,
                        userobm_vacation_datebegin,
                        userobm_vacation_dateend,
                        userobm_vacation_message,
                        userobm_samba_perms,
                        userobm_samba_home,
                        userobm_samba_home_drive,
                        userobm_samba_logon_script,
                        userobm_host_id,
                        userobm_description,
                        userobm_location,
                        userobm_education,
                        userobm_photo_id
                    ) SELECT    userobm_id,
                                userobm_domain_id,
                                userobm_timecreate,
                                userobm_userupdate,
                                userobm_usercreate,
                                userobm_local,
                                userobm_ext_id,
                                userobm_system,
                                userobm_archive,
                                userobm_timelastaccess,
                                userobm_login,
                                userobm_nb_login_failed,
                                userobm_password_type,
                                userobm_password,
                                userobm_password_dateexp,
                                userobm_account_dateexp,
                                userobm_perms,
                                userobm_delegation_target,
                                userobm_delegation,
                                userobm_calendar_version,
                                userobm_uid,
                                userobm_gid,
                                userobm_datebegin,
                                userobm_hidden,
                                userobm_kind,
                                userobm_lastname,
                                userobm_firstname,
                                userobm_title,
                                userobm_sound,
                                userobm_company,
                                userobm_direction,
                                userobm_service,
                                userobm_address1,
                                userobm_address2,
                                userobm_address3,
                                userobm_zipcode,
                                userobm_town,
                                userobm_expresspostal,
                                userobm_country_iso3166,
                                userobm_phone,
                                userobm_phone2,
                                userobm_mobile,
                                userobm_fax,
                                userobm_fax2,
                                userobm_web_perms,
                                userobm_web_list,
                                userobm_web_all,
                                userobm_mail_perms,
                                userobm_mail_ext_perms,
                                userobm_email,
                                userobm_mail_server_id,
                                userobm_mail_quota,
                                userobm_mail_quota_use,
                                userobm_mail_login_date,
                                userobm_nomade_perms,
                                userobm_nomade_enable,
                                userobm_nomade_local_copy,
                                userobm_nomade_datebegin,
                                userobm_nomade_dateend,
                                userobm_email_nomade,
                                userobm_vacation_enable,
                                userobm_vacation_datebegin,
                                userobm_vacation_dateend,
                                userobm_vacation_message,
                                userobm_samba_perms,
                                userobm_samba_home,
                                userobm_samba_home_drive,
                                userobm_samba_logon_script,
                                userobm_host_id,
                                userobm_description,
                                userobm_location,
                                userobm_education,
                                userobm_photo_id
                      FROM UserObm
                      WHERE userobm_id='.$entity->getId();
    
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 2 );
            return 1;
        }
    }

    if( !$entity->getDelete() && $entity->getUpdateLinks() ) {
        $query = 'INSERT INTO P_EntityRight
                    (   entityright_entity,
                        entityright_entity_id,
                        entityright_consumer,
                        entityright_consumer_id,
                        entityright_read,
                        entityright_write,
                        entityright_admin,
                        entityright_access
                    ) SELECT    entityright_entity,
                                entityright_entity_id,
                                entityright_consumer,
                                entityright_consumer_id,
                                entityright_read,
                                entityright_write,
                                entityright_admin,
                                entityright_access
                      FROM EntityRight
                      WHERE entityright_entity=\'MailShare\' AND entityright_entity_id='.$entity->getId();

        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour des liens '.$entity->getDescription() );
            return 1;
        }
    }


    return 0;
}


sub delete {
    my $self = shift;
    my( $entity ) = @_;

    if( !$entity->getDelete() ) {
        $self->_log( 'l\'entité '.$entity->getDescription().' n\'est pas à supprimer. Suppression annulée', 3 );
        return 0;
    }

    return $self->_delete( $entity );
}


sub _delete {
    my $self = shift;
    my( $entity ) = @_;

    if( ref($entity) ne 'OBM::Entities::obmUser' ) {
        $self->_log( 'entité incorrecte, traitement impossible', 3 );
        return 1;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler;
    my $sth;
    if( !($dbHandler = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 3 );
        return 1;
    }


    my $query;
    if( $entity->getUpdateEntity() ) {
        $query = 'DELETE FROM P_UserObm WHERE userobm_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 2 );
            return 1;
        }
    }

    if( $entity->getUpdateLinks() ) {
         $query = 'DELETE FROM P_EntityRight WHERE entityright_entity=\'MailShare\' AND entityright_entity_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD de liens '.$entity->getDescription(), 2 );
            return 1;
        }
    }


    return 0;
}
