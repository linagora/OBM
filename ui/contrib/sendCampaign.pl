#!/usr/bin/perl -w
#####################################################################
# OBM               - File : sendCampaign.pl                        #
#                   - Desc : Script permettant d'envoyer une        #
#                   campagne de mailing                             #
#####################################################################

# depends on libproc-pid-file-perl
# and libproc-daemon-perl

# TODO : mettre ce script en tant que demon
# TODO : faire la procedure qui poll
# TODO : reagir au sighup pour forcer un nouveau poll

# TODO : modification du modele de donnnes : campaignmailcontent_content doit etre un mediumblob
# pour accepter des fichiers d'une taille superieur a 64 ko (et inferieur a 16 Mo)
# => SQL: alter table CampaignMailContent modify campaignmailcontent_content mediumblob;

package sendCampaign;

require OBM::Tools::obmDbHandler;

use strict;
use OBM::Tools::commonMethods qw(_log dump);
use OBM::Parameters::common;
use Getopt::Long;
use POSIX;
use Proc::PID::File;
use Proc::Daemon;

my $command = Cwd::realpath($0);
delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

# Variables globales
my $interval = 10; # toutes les 10 minutes
my $fromdos = 'fromdos';
my $dbHandler;
my $documents_path;
my %campaign_status = (
    'created'           => 0,
    'ready'             => 1,
    'running'           => 2,
    'planified'         => 3,
    'finished'          => 4,
    'archived'          => 5,
    'error_mail_format' => 6,
    'error_target'      => 7,
    'error'             => 8
);
my %bads;
my %goods;
my %doublons;

my %parameters;
my $return = GetOptions( \%parameters, 'daemon', 'test=s', 'import=s', 'testaddress=s', 'extfile=s' ,'help' );

if( !$return ) {
    %parameters = undef;
}

my $keep_going = 1;

exit sendCampaign->run(\%parameters);

$|=1;


sub getParameter {
    my $self = shift;
    my( $parameters ) = @_;

    # TODO : tester l'incompatbilite des 3 mots cle daemon, import et test
    if( exists($parameters->{'import'}) && exists($parameters->{'test'}) ) {
        print STDERR "ERREUR: Parametres '--import' et '--test' incompatibles\n\n";
        $parameters->{'help'} = 1;

    }elsif( exists($parameters->{'daemon'}) ) {
        $self->_log( "Demarre en tant que daemon", 3 );

    }elsif( exists($parameters->{'import'}) ) {
        $self->_log( "Import seulement", 3 );

    }elsif( exists($parameters->{'test'}) ) {
        if( ! exists($parameters->{'testaddress'}) ) {
            print STDERR "ERREUR: Parametres : il faut fournir une adresse destinataire pour le test\n\n";
            $parameters->{'help'} = 1;
        }
        $self->_log( 'Test', 3 );
    }
    if( exists( $parameters->{'help'} ) ) {
        $self->_log( 'Affichage de l\'aide', 3 );
        print "Script qui importe et envoi les campagnes de mailings.\n\n";

        print "Syntaxes: $0 [ --daemon] \n";
        print "          $0 --import id [ --extfile fichier_adresses]\n";
        print "          $0 --test id --testaddress adresse \n";
        print "\t--daemon : Demarre en tant que demon (pas encore implémenté)\n";
        print "\t--import : Fait uniquement un import des adresses et pas d'envoi\n";
        print "\t--test : Envoi un mail de test\n";

        exit 0;
    }
}

# Recherche le chemin de stockage du document dans la configuration
sub getDocumentsPath {
    my $self = shift;

    my $path = $documentRoot.$documentDefaultPath;

    print "Documents path is $path\n";
    return $path;
}

sub getCampaignToStart {
    my $self = shift;
    my $query = "SELECT campaign_id, campaign_name FROM Campaign ";
    $query .= "WHERE campaign_start_date <= NOW() AND campaign_end_date+1 >= NOW() AND campaign_status = ".$campaign_status{'planified'};
    $query .= "ORDER BY campaign_end_date LIMIT 1";
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        exit 1;
    }
    return $queryResult->fetchrow_array(); # return ( $campaignId, $campaignName )
}

sub loadMailContent {
    my $self = shift;
    my $campaignId = shift;
    my $mailContentId = shift;
    my $query = "SELECT campaign_email
    FROM Campaign
    WHERE campaign_id=$campaignId";
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        exit 1;
    }

    my( $campaignEmail ) = $queryResult->fetchrow_array();

    my $doc_path = $documents_path.'/'.($campaignEmail%10).'/'.$campaignEmail;

    $self->_log( "Chargement du document $doc_path", 3 );
    print "CHARGEMENT du document $doc_path...\n";

    $self->_log("execute the $fromdos $doc_path", 3);
    (system("$fromdos $doc_path") == 0) or $self->_log("Can't exec the $fromdos command, could be a serious problem !", 1);
    open(F,$doc_path);
    my $headerzone=1;

    my $concat = "campaignmailcontent_content";
    if( $dbHandler->{'dbType'} eq 'mysql' ) {
        $concat = "concat(campaignmailcontent_content,?)";
    } elsif( $dbHandler->{'dbType'} eq 'pgsql' ) {
        $concat = "campaignmailcontent_content || ?";
    }

    my $sth = $dbHandler->{'dbHandler'}->prepare("update CampaignMailContent set campaignmailcontent_content=$concat where campaignmailcontent_refext_id='$campaignId'");
    if( !$sth ) {
        exit 1;
    }

    my $header="";
    while(<F>) {
        if($headerzone) {
            if(/^[ \t]+/ || /^([A-Za-z0-9\\-]+): .*$/) {
                # Debut d'entete normale
                $header = $1 if($1);
                # sinon Entete sur plusieurs lignes

                # entetes a enlever
                next if($header eq 'Received' || $header eq 'Date' || $header eq 'Message-ID' || $header eq 'X-Sieve' || $header eq 'CC' || $header eq 'Return-Path');

                $_ = "To: {recipient_address}\n" if $header eq 'To';
                print uc($header)." => ".$_;

            } elsif($_ eq "\n") {
                # Fin du bloc d'entetes
                $headerzone=0;
                # Entêtes du moteur a ajouter la fin des entêtes
                $sth->execute("X-OBM-Push-Version: 1.0\n");
                $sth->execute("X-OBM-Push-Reference: c$campaignId/pt{recipient_pushtarget_id}/re{recipient_refext_id}\n");

            } else {
                # Entete invalide
                $self->_log( "Erreur lors du parsage d'une entete du mail", 3 );
                print "Erreur lors du parsage d'une entete du mail : '$_'\n";
                exit 1;
            }
        }
        $sth->execute($_);
    }
    close(F);
}

sub getMailContent {
    my $self = shift;
    my $campaignId = shift;
    my $bufMail = shift;

    my $size;
    my $queryResult;
    my $query = "select campaignmailcontent_content from CampaignMailContent where campaignmailcontent_refext_id='$campaignId'";

    if( !defined($dbHandler->execQuery( $query, \$queryResult ) )) {
        exit 1;
    }

    my ($datas) = $queryResult->fetchrow_array();

    # TODO : et s'il y a des \r ?
    # TODO : a verifier pour clients sous windows ? car conversion
    @{$bufMail} = split(/[\n\r]/,$datas);

    return length($datas);
}

sub checkEmailAddress {
    my $self = shift;
    my ($e) = @_;
    return (  $e =~ /^[0-9a-z_\-\.]+@[0-9a-z_\-\.]+$/ )
}


sub updateCampaignStatus {
    my $self = shift;
    my $campaignId = shift;
    my $status = shift;
    my $query = "update Campaign set campaign_status=".$campaign_status{$status};
    $query.=" where campaign_id=$campaignId";

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult ) ) ) {
        exit 1;
    }
}

sub campaignImport {
    my $self = shift;
    my $campaignId = shift;
    my $campaignName = shift;
    my ($query,$queryResult);

    $self->_log( "Importation de la campagne $campaignName", 3 );
    print "Import campagne '$campaignName'\n";

    # Importation du contenu dans CampaignMailContent
    # Vidage de CampaignPushTarget cas de 2 eme import
    $query = "delete from CampaignPushTarget where campaignpushtarget_mailcontent_id in (select campaignmailcontent_id from CampaignMailContent where campaignmailcontent_refext_id='$campaignId')";
    if( !defined($dbHandler->execQuery( $query, \$queryResult ) ) ) {
        exit 1;
    }

    # Vidage de CampaignMailContent en cas de 2eme import
    $query = "delete from CampaignMailContent where campaignmailcontent_refext_id='$campaignId'";
    if( !defined($dbHandler->execQuery( $query, \$queryResult ) ) ) {
        exit 1;
    }

    # Instanciation d'un nouveau mailcontent
    $query = "insert into CampaignMailContent (campaignmailcontent_refext_id,campaignmailcontent_content) values ('$campaignId','')";
    if( !defined($dbHandler->execQuery( $query, \$queryResult ) ) ) {
        exit 1;
    }

    $query = "";
    if( $dbHandler->{'dbType'} eq 'mysql' ) {
        $query = "select last_insert_id()";
    } elsif( $dbHandler->{'dbType'} eq 'pgsql' ) {
        $query = "select currval(pg_get_serial_sequence('CampaignMailContent','campaignmailcontent_id'))";
    }
    if( !defined($dbHandler->execQuery( $query, \$queryResult ) ) ) {
        exit 1;
    }
    my ($mailContentId) = $queryResult->fetchrow_array();
    print "new mailContentId is $mailContentId\n";

    # Chargement du contenu du mail
    $self->loadMailContent($campaignId,$mailContentId);

    # Importation des addresses dans CampaignPushTarget
    $query = "select distinct campaigntarget_entity_id from CampaignTarget INNER JOIN ListEntity ON listentity_entity_id = campaigntarget_entity_id where campaigntarget_campaign_id=$campaignId";
    if( !defined($dbHandler->execQuery( $query, \$queryResult ) ) ) {
        exit 1;
    }

    my $sth = $dbHandler->{'dbHandler'}->prepare("insert into CampaignPushTarget (campaignpushtarget_mailcontent_id,campaignpushtarget_refext_id,campaignpushtarget_email_address,campaignpushtarget_start_time,campaignpushtarget_retries) values ('$mailContentId',?,?,now(),0)");


    while ( my ( $listId ) = $queryResult->fetchrow_array() ) {
        $self->importAddressesList($listId,$mailContentId,$sth);
    }

    if($parameters{'extfile'}) {
        print "Chargement du fichier d'adresses ".$parameters{'extfile'}."\n";
        $self->importAddressesFile($parameters{'extfile'},$mailContentId,$sth);
    }

    $self->_log( "Nombre d'adresses importees : ".keys(%goods)." bonnes, "
        .keys(%bads)." mauvaises, ".keys(%doublons)." doublons", 3 );

    # Passage de la campagne dans l'etat "En cours"
    $self->updateCampaignStatus($campaignId,'running');
}

sub importAddress {
    my $self = shift;
    my $address = shift;
    my $refext_id = shift;
    my $sth = shift;

    # Filtre les eventuelles caracteres parasites avant et apres
    $address =~ s/^[ ,\t\r\n"]+//;
    $address =~ s/[ ,\t\r\n"]+$//;

    return if $address eq '';

    print "$address : ";

    if( $self->checkEmailAddress($address)) {
        if(defined($goods{$address})) {
            #C'est un doublon
            $doublons{$address}=1;
            print "D";

        } else {
            $goods{$address}=1;
            $sth->execute($refext_id,$address);
            print "G";
        }

    } else {
        $bads{$address} = 1;
        print "B";
    }
    print "\n";
}

sub importAddressesFile {
    my $self = shift;
    my $filename = shift;
    my $mailContentId = shift;
    my $sth = shift;

    $self->_log( "Importation des adresses : fichier $filename", 3 );

    if( ! open(F,$filename) )  {
        $self->_log( "Probleme lors de l'ouverture du fichier $filename", 3 );
        print "Probleme lors de l'ouverture du fichier $filename";
        exit 1;
    }

    my $address;
    while (<F>) {
        chomp;
        $address = lc($_);
        $self->importAddress($address,"",$sth);
    }
    close(F);
}

sub importAddressesList {
    my $self = shift;
    my $listId = shift;
    my $mailContentId = shift;
    my $sth = shift;

    my $query = "select list_query from List inner join ListEntity on listentity_list_id=list_id where listentity_entity_id=$listId";
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult ) ) ) {
        exit 1;
    }

    my ($dyn_query) = $queryResult->fetchrow_array();
    if(!defined($dyn_query)) {
        $self->_log( "L'entité $listId n'est pas une liste ou elle n'a pas été trouvée", 3 );
        return 1;
    }
    my $static_query = "SELECT distinct
      contactlist_contact_id as id,
      contactlist_contact_id as contact_id,
      contact_lastname,
      contact_firstname,
      '' as kind_minilabel,
      '' as kind_header,
      '' as kind_lang,
      '' as contactfunction_label,
      contact_title,
      contact_service,
      '' as contact_street,
      '' as contact_zipcode,
      '' as contact_town,
      '' as contact_expresspostal,
      '' as contact_state,
      '' as contact_country,
      '' as \"WorkPhone.phone_number\",
      '' as \"MobilePhone.phone_number\",
      '' as \"FaxPhone.phone_number\",
      '' as \"HomePhone.phone_number\",
      email_address,
      contact_archive,
      contact_company,
      0 AS company_id,
      '' AS company_name,
      '' AS company_street,
      '' AS company_zipcode,
      '' AS company_town,
      '' AS company_expresspostal,
      '' AS company_state,
      '' AS company_country
    FROM ContactList
      INNER JOIN Contact ON contactlist_contact_id=contact_id
      INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
      INNER JOIN Email ON contactentity_entity_id=email_entity_id AND email_label = 'INTERNET;X-OBM-Ref1'
      INNER JOIN ListEntity ON listentity_list_id = contactlist_list_id
    WHERE listentity_entity_id = $listId";
    chomp($dyn_query);
    if (!$dyn_query) {
      $query = $static_query;
    } else {
      $query = "$static_query UNION $dyn_query";
    }
    $self->_log( "Importation des adresses : requete \"$query\"", 3 );

    # Execution réel de la requete
    if( !defined($dbHandler->execQuery( $query, \$queryResult ) ) ) {
        exit 1;
    }

    my ($row,$address);
    while ($row = $queryResult->fetchrow_hashref()) {
        $address = lc($row->{'email_address'});
        $self->importAddress($address,$row->{'contact_id'},$sth);
    }
}


sub sendReport {
    my $self = shift;
    my ($state,$campaignId,$sender,$campaignName,$waittime,$size,$nb_inqueue) = @_;
    print "Envoi du rapport de $state\n";

    my $reportAddr = $OBM::Parameters::common::cfgFile->val( 'campaign' , 'reportAddr');

    open(T,"|/usr/bin/mail $reportAddr -s '[Mailing] Envoi de la campagne $campaignId : $state'") || die "could not execute mail command";
    print T "Envoi d'une campagne de mails $state.\n";
    print T "  ID: $campaignId\n";
    print T "  Expediteur: $sender\n";
    print T "  Titre: $campaignName\n";
    print T "  Taille du message: $size octets\n";
    print T "  Debit: 1 mail toutes les $waittime secondes\n";

    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) =
    localtime();
    if($state eq "debut") {
        print T "  Nombre de messages: $nb_inqueue\n";
        my $val = $nb_inqueue*$waittime;
        my $h = POSIX::floor($val/3600);
        my $m = POSIX::floor(($val-$h*3600)/60);
        my $s = $val%60;
        print T "  Duree estimee: $h heures, $m minutes\n";

        print T "  Debut : ". strftime("%A %e %b %Y %H:%M:%S",$sec, $min, $hour, $mday, $mon, $year). "\n";
        my $t = mktime($sec,$min,$hour,$mday,$mon,$year);
        $t+=$h*3600+$m*60+$s;
        print T "  Fin estimee: ".strftime("%A %e %b %Y %H:%M:%S", localtime($t) ). "\n";
    }
    close(T);
}

sub sendMail {
    my $self = shift;
    my ($address,$bufMail,$id,$refext_id) = @_;
    print "Envoi a $address\n";

    my $bounceAddr = $OBM::Parameters::common::cfgFile->val( 'campaign' , 'bounceAddr');

    my $lcop;
    open(P,"|/usr/sbin/sendmail -f $bounceAddr $address") || die "could not execute sendmail command!";
    foreach my $l (@{$bufMail}) {
        $lcop = $l;
        $lcop =~ s/{recipient_address}/$address/;
        $lcop =~ s/{recipient_pushtarget_id}/$id/;
        $lcop =~ s/{recipient_refext_id}/$refext_id/;
#		print "> $lcop";
        print P $lcop."\n";
    }
    close(P);
}

sub sendTestMail {
    my $self = shift;
    my $campaignId = shift;
    my $address = shift;
    my @bufMail;
    $self->_log( "Envoi d'un mail de test a $address",3);
    print "envoi d'un mail de test a l'adresse $address\n";

    # Recuperation des données du mail
    $self->getMailContent($campaignId,\@bufMail);
    $self->sendMail($address,\@bufMail,0,'Test');
}

sub campaignSendMails {
    my $self = shift;
    my $campaignId = shift;
    my $campaignName = shift;
    my $sender = shift;
    my $size;
    my @bufMail;

    $self->_log( "Envoi de la campaigne $campaignName",2);

    # Recuperation des données du mail
    $size = $self->getMailContent($campaignId,\@bufMail);

    # Parametre de configuration maxBandwidthAverage : bande passante de la connexion
    # sortante a ne pas depasser (en moyenne). Exprimé en kbit/s.
    my $maxBandwidthAverage = $OBM::Parameters::common::cfgFile->val( 'campaign' , 'maxBandwidthAverage');

    # Calcul de l'interval entre 2 messages (waittime) en fonction du maxBandwidthAverage
    my $waittime = POSIX::ceil(($size*8)/($maxBandwidthAverage*1000))+3;

    $self->_log( "Debit de l'envoi : 1 mail toutes les $waittime secondes",3);
    print "Waittime is $waittime\n";

    # Calcul des nombres de messages et preparation des requetes
    my $query = "select campaignpushtarget_id,campaignpushtarget_email_address,campaignpushtarget_refext_id from CampaignPushTarget INNER JOIN CampaignMailContent on campaignpushtarget_mailcontent_id=campaignmailcontent_id where campaignmailcontent_refext_id='$campaignId' and campaignpushtarget_status=1";
    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult ) ) ) {
        exit 1;
    }

    my $nb_total=$queryResult->rows;
    my $nb_sent=0;
    my $nb_inqueue=$nb_total;

    $query = "update Campaign set campaign_nb_sent=?,campaign_nb_inqueue=?,campaign_progress=? where campaign_id=$campaignId";
    my $queryResultProgress = $dbHandler->{'dbHandler'}->prepare($query);
    $queryResultProgress->execute(0,$nb_inqueue,0);

    $query = "update CampaignPushTarget set campaignpushtarget_status=2,campaignpushtarget_sent_time=now() where campaignpushtarget_id=?";
    my $queryResultPushTarget = $dbHandler->{'dbHandler'}->prepare($query);

    # Envoi du rapport de debut
    $self->sendReport("debut",$campaignId,$sender,$campaignName,$waittime,$size,$nb_inqueue);

    # Envoi effectif des mails
    my $stat;
    while($keep_going and my( $id,$address,$refext_id ) = $queryResult->fetchrow_array() ) {
        $stat = sprintf("%5i",$nb_sent+1)."/".$nb_total." ".sprintf("%3i",(100*($nb_sent+1))/$nb_total)."%";
        $self->_log( "($stat) Envoi a $address / $refext_id",3);
        $self->sendMail($address,\@bufMail,$id,$refext_id);

        $queryResultPushTarget->execute($id);

        $nb_sent++; $nb_inqueue--;
        $queryResultProgress->execute($nb_sent,$nb_inqueue,int((100*$nb_sent)/$nb_total));
        sleep $waittime;
    }

    if ($keep_going) {
        # Passage de la campagne dans l'etat "terminée"
        $self->updateCampaignStatus($campaignId,'finished');

        # Envoi du rapport de fin
        $self->sendReport("fin",$campaignId,$sender,$campaignName,$waittime,$size);
    }
}

sub doProcess {
    my $self = shift;
    my $query;
    my $queryResult;

    if( exists( $parameters{'import'} )) {
        $self->_log( "Recherche de campagnes a importer", 3 );
        $query = "SELECT campaign_id,campaign_name FROM Campaign
            WHERE campaign_id=".$parameters{'import'};
        if( !defined($dbHandler->execQuery( $query, \$queryResult ) ) ) {
            exit 1;
        }

        while( my( $campaignId,$campaignName ) = $queryResult->fetchrow_array() ) {
            $self->campaignImport($campaignId,$campaignName);
        }

    } else {
        $self->_log( "Recherche de campagnes a envoyer", 3 );
        my $sel_manager = "a.userobm_lastname as manager";
        my $sel_creator = "b.userobm_lastname as creator";

        if( $dbHandler->{'dbType'} eq 'mysql' ) {
            $sel_manager = "concat(a.userobm_firstname,' ',a.userobm_lastname) as manager";
            $sel_creator = "concat(b.userobm_firstname,' ',b.userobm_lastname) as creator";
        } elsif( $dbHandler->{'dbType'} eq 'pgsql' ) {
            $sel_manager = "a.userobm_firstname || ' ' || a.userobm_lastname as manager";
            $sel_creator = "b.userobm_firstname || ' ' || b.userobm_lastname as creator";
        }

        # Recherche des campagnes dans l'etat "en cours" à envoyer
        $query = "SELECT campaign_id,campaign_name,
        $sel_manager,
        $sel_creator
        FROM Campaign
        LEFT JOIN UserObm a on campaign_manager_id=a.userobm_id
        LEFT JOIN UserObm b on campaign_usercreate=b.userobm_id
        WHERE campaign_start_date<=current_date AND campaign_end_date>=current_date
        AND campaign_status=".$campaign_status{'running'};
        if( !defined($dbHandler->execQuery( $query, \$queryResult ) ) ) {
            exit 1;
        }

        while($keep_going and my( $campaignId,$campaignName,$manager,$creator) = $queryResult->fetchrow_array() ) {
            print "Envoi campagne '$campaignName' (createur : $creator".(defined($manager)?", responsable : $manager":"").")\n";
            $self->campaignSendMails($campaignId,$campaignName,defined($manager)?$manager:$creator);
        }
    }
}

sub doFinish {
    my $self = shift;
    # On ferme le log
    $self->_log( 'Fin du traitement', 2 );

    exit 0;
}

# Debut du programme principal
sub run {
    my $self = shift;
    my( $parameters ) = @_;

    if( !defined($parameters) ) {
        $parameters->{'help'} = 1;
    }

    # Traitement des paramètres
    $self->_log( 'Analyse des parametres du script', 3 );
    $self->getParameter( $parameters );

    # On prepare le log
    my ($scriptname) = ($0=~'.*/([^/]+)');
    $self->_log( "$scriptname: ", "O" );

    # Recherche de chemin où sont stockés les documents
    $documents_path = $self->getDocumentsPath();

    # On se connecte a la base
    $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->dienice("connection à la base de données impossible !");
    }

    if( exists($parameters{"test"}) ) {
        $self->sendTestMail($parameters->{"test"},$parameters->{"testaddress"});
        $self->doFinish;

    } else {        
        $SIG{HUP}  = sub { $self->_log('Caught SIGHUP: exiting', 0); $keep_going = 0; };
        $SIG{INT}  = sub { $self->_log('Caught SIGINT: exiting', 0); $keep_going = 0; };
        $SIG{QUIT} = sub { $self->_log('Caught SIGQUIT: exiting', 0); $keep_going = 0; };
        $SIG{TERM} = sub { $self->_log('Caught SIGTERM: exiting', 0); $keep_going = 0; };

        if( exists($parameters->{"daemon"}) ) {
            $SIG{CHLD} = 'IGNORE';
            $self->startDaemon();
        } else {
            $self->doProcess;
            $self->doFinish;
        }
    }
}

sub startChildren {
    my $self = shift;
    my $pid;
    if ($pid=fork) {
        return $pid;
    } elsif (defined $pid) {
        exec("$command") or $self->dienice("Unable to exec the $command command into the child process");
        exit;
    }
    #else
    $self->dienice("Unable to start the child process");
}

sub startDaemon {
    my $self = shift;
    my $pid;
    # Fork and detach from the parent process
    eval { Proc::Daemon::Init; };
    if ($@) {
        $self->dienice("Unable to start daemon: $@");
    }

    # Get a PID file
    $self->dienice("Already running!") if Proc::PID::File->running();

    while ($keep_going) {

        # Check the beginning of a planified campaign
        my( $campaignId, $campaignName ) = $self->getCampaignToStart();
        if (defined($campaignId)) {
            #import the planified campaign
            $self->campaignImport($campaignId,$campaignName);
        }

        # check that children is still alive
        if ($pid) {
            my $nb = kill(0, $pid);
            if ($nb == 0) {
                $pid = undef;
            }
        }
        # start the children if none is already running
        if (not $pid) {
            $self->startChildren();
        }

        if ($keep_going) {
            sleep( $interval * 60 );
        }
    }

    # kill the children in case of exit
    if ($pid) {
        my $nb = kill(3, $pid);
    }
}

# dienice
# write die messages to the log before die'ing
sub dienice ($) {
    my $self = shift;
    my ( $package, $filename, $line ) = caller;
    $self->_log("$_[0] at line $line in $filename", 0);
    die $_[0];
}
