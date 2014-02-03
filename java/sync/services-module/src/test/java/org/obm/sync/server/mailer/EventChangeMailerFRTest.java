package org.obm.sync.server.mailer;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

public class EventChangeMailerFRTest extends EventChangeMailerTest {

	@Override
	protected EventChangeMailer newEventChangeMailer() {
		return getLocaleEventChangeMailer(Locale.FRENCH);
	}

	@Override
	protected Locale getLocale() {
		return Locale.FRENCH;
	}

	private List<String> getPlainMessage(String header) {
		return Lists.newArrayList(
				header,
				"du            : 8 nov. 2010 11:00",
				"au            : 8 nov. 2010 11:45",
				"fuseau horaire: Europe/Paris",
				"sujet         : Sprint planning OBM",
				"lieu          : A random location",
				"organisateur  : Raphael ROUGERON",
				"créé par      : Emmanuel SURLEAU"
		);
	}

	private List<String> getHtmlMessage(String header) {
		return Lists.newArrayList(
				header,
				"Du 8 nov. 2010 11:00",
				"Au 8 nov. 2010 11:45",
				"Fuseau horaire Europe/Paris",
				"Sujet Sprint planning OBM",
				"Lieu A random location",
				"Organisateur Raphael ROUGERON",
				"Créé par Emmanuel SURLEAU"
		);
	}

	private List<String> getRecurrentPlainMessage(String header) {
		return Lists.newArrayList(
				header,
				"du            : 8 nov. 2010",
				"au            : 23 nov. 2012",
				"heure         : 11:00 - 11:45",
				"fuseau horaire: Europe/Paris",
				"recurrence    : Toutes les 2 semaines [Lundi, Mercredi, Jeudi]",
				"sujet         : Sprint planning OBM",
				"lieu          : A random location",
				"organisateur  : Raphael ROUGERON"
		);
	}

	private List<String> getRecurrentHtmlMessage(String header) {
		return Lists.newArrayList(
				header,
				"Du 8 nov. 2010",
				"Au 23 nov. 2012",
				"Fuseau horaire Europe/Paris",
				"Sujet Sprint planning OBM",
				"Lieu A random location",
				"Organisateur Raphael ROUGERON",
				"Heure 11:00 - 11:45",
				"Type de récurrence Toutes les 2 semaines [Lundi, Mercredi, Jeudi]"
		);
	}


	@Override
	protected List<String> getInvitationPlainMessage() {
		return getPlainMessage("NOUVEAU RENDEZ-VOUS");
	}

	@Override
	protected List<String> getInvitationHtmlMessage() {
		return getHtmlMessage("Invitation à un événement");
	}

	@Override
	protected List<String> getUpdatePlainMessage() {
		return Lists.newArrayList(
				"RENDEZ-VOUS MODIFIÉ !",
				"du 8 nov. 2010 11:00",
				"au 8 nov. 2010 11:45",
				"du            : 8 nov. 2010 12:00",
				"au            : 8 nov. 2010 13:00",
				"fuseau horaire: Europe/Paris",
				"sujet         : Sprint planning OBM",
				"lieu          : A random location",
				"organisateur  : Raphael ROUGERON",
				"créé par      : Emmanuel SURLEAU"
		);
	}

	@Override
	protected List<String> getUpdateHtmlMessage() {
		return Lists.newArrayList(
				"Invitation à un évènement : mise à jour",
				"du 8 nov. 2010 11:00",
				"au 8 nov. 2010 11:45",
				"Du 8 nov. 2010 12:00",
				"Au 8 nov. 2010 13:00",
				"Fuseau horaire Europe/Paris",
				"Sujet Sprint planning OBM",
				"Lieu A random location",
				"Organisateur Raphael ROUGERON",
				"Créé par Emmanuel SURLEAU"
		);
	}

	@Override
	protected List<String> getCancelPlainMessage() {
		return getPlainMessage("RENDEZ-VOUS ANNULÉ");
	}

	@Override
	protected List<String> getCancelHtmlMessage() {
		return getHtmlMessage("Annulation d'un événement");
	}

	@Override
	protected List<String> getRecurrentInvitationPlainMessage() {
		return getRecurrentPlainMessage("NOUVEAU RENDEZ-VOUS RÉCURRENT");
	}

	@Override
	protected List<String> getRecurrentInvitationHtmlMessage() {
		return getRecurrentHtmlMessage("Invitation à un événement récurrent");
	}

	@Override
	protected List<String> getRecurrentUpdatePlainMessage() {
		return Lists.newArrayList(
				"RENDEZ-VOUS RÉCURRENT MODIFIÉ !",
				"du 8 nov. 2010",
				"au \"Sans date de fin\"",
				"du            : 8 nov. 2010",
				"au            : 23 nov. 2012",
				"de 11:00 à 11:45",
				"heure         : 12:00 - 13:00",
				"fuseau horaire: Europe/Paris",
				"recurrence    : Toutes les 2 semaines [Lundi, Mercredi, Jeudi]",
				"sujet         : Sprint planning OBM",
				"lieu          : A random location",
				"organisateur  : Raphael ROUGERON"
		);
	}

	@Override
	protected List<String> getRecurrentUpdateHtmlMessage() {
		return Lists.newArrayList(
				"Invitation à un évènement récurrent : mise à jour",
				"du 8 nov. 2010",
				"au \"Sans date de fin\"",
				"Du 8 nov. 2010",
				"Au 23 nov. 2012",
				"Fuseau horaire Europe/Paris",
				"Sujet Sprint planning OBM",
				"Lieu A random location",
				"Organisateur Raphael ROUGERON",
				"de 11:00 à 11:45",
				"Heure 12:00 - 13:00",
				"Type de récurrence Toutes les 2 semaines [Lundi, Mercredi, Jeudi]"
		);
	}
	
	@Override
	protected List<String> getNonRecurrentToRecurrentUpdatePlainMessage() {
		return Lists.newArrayList(
				"RENDEZ-VOUS RÉCURRENT MODIFIÉ !",
				"du 8 nov. 2010",
				"au 8 nov. 2010",
				"du            : 8 nov. 2010",
				"au            : 23 nov. 2012",
				"de 11:00 à 11:45",
				"heure         : 11:00 - 11:45",
				"fuseau horaire: Europe/Paris",
				"type de récurrence : Pas de récurrence",
				"recurrence    : Toutes les 2 semaines [Lundi, Mercredi, Jeudi]",
				"sujet         : Sprint planning OBM",
				"lieu          : A random location",
				"organisateur  : Raphael ROUGERON"
		);
	}

	@Override
	protected List<String> getNonRecurrentToRecurrentUpdateHtmlMessage() {
		return Lists.newArrayList(
				"Invitation à un évènement récurrent : mise à jour",
				"du 8 nov. 2010",
				"au 8 nov. 2010",
				"Du 8 nov. 2010",
				"Au 23 nov. 2012",
				"Sujet Sprint planning OBM",
				"Lieu A random location",
				"Organisateur Raphael ROUGERON",
				"de 11:00 à 11:45",
				"Heure 11:00 - 11:45",
				"Fuseau horaire Europe/Paris",
				"type de récurrence : Pas de récurrence",
				"Type de récurrence Toutes les 2 semaines [Lundi, Mercredi, Jeudi]"
		);
	}
	
	@Override
	protected List<String> getRecurrentToNonRecurrentUpdatePlainMessage() {
		return Lists.newArrayList(
				"RENDEZ-VOUS MODIFIÉ !",
				"du 8 nov. 2010 11:00",
				"au 8 nov. 2010 11:45",
				"du            : 8 nov. 2010 11:00",
				"au            : 8 nov. 2010 11:45",
				"fuseau horaire: Europe/Paris",
				"sujet         : Sprint planning OBM",
				"lieu          : A random location",
				"organisateur  : Raphael ROUGERON",
				"créé par      : Emmanuel SURLEAU"
		);
	}

	@Override
	protected List<String> getRecurrentToNonRecurrentUpdateHtmlMessage() {
		return Lists.newArrayList(
				"Invitation à un évènement : mise à jour",
				"du 8 nov. 2010 11:00",
				"au 8 nov. 2010 11:45",
				"Du 8 nov. 2010 11:00",
				"Au 8 nov. 2010 11:45",
				"Fuseau horaire Europe/Paris",
				"Sujet Sprint planning OBM",
				"Lieu A random location",
				"Organisateur Raphael ROUGERON",
				"Créé par Emmanuel SURLEAU"
		);
	}
	
	@Override
	protected List<String> getRecurrentCancelPlainMessage() {
		return getRecurrentPlainMessage("RENDEZ-VOUS RÉCURRENT ANNULÉ");
	}

	@Override
	protected List<String> getRecurrentCancelHtmlMessage() {
		return getRecurrentHtmlMessage("Annulation d'un événement récurrent");
	}

	@Override
	protected List<String> getChangeParticipationPlainMessage() {
		return Lists.newArrayList(
				"PARTICIPATION : MISE A JOUR",
				"Matthieu BAECHLER a accepté",
				"l'événement Sprint planning OBM prévu le 8 nov. 2010",
				"This is a random comment"
		);
	}
	
	@Override
	protected List<String> getChangeParticipationHtmlMessage() {
		return Lists.newArrayList(
				"Participation : mise à jour ",
				"Matthieu BAECHLER a accepté",
				"l'événement Sprint planning OBM prévu le 8 nov. 2010",
				"Commentaire This is a random comment"
		);
	}
	
	@Override
	protected String getNotice() {
		return "Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, "
				+ "vous devez synchroniser pour visualiser";
	}

	@Override
	protected String getNewEventSubject() {
		return "=?UTF-8?Q?Nouvel_=C3=A9v=C3=A9nement_de_Raphael_R?=\r\n =?UTF-8?Q?OUGERON_:_Sprint_planning_OBM";
	}

	@Override
	protected String getNewRecurrentEventSubject() {
		return "=?UTF-8?Q?Nouvel_=C3=A9v=C3=A9nement_r=C3=A9current_de_Raph?=\r\n"
				+ " =?UTF-8?Q?ael_ROUGERON_:_Sprint_planning_OBM?=";
	}

	@Override
	protected String getCancelEventSubject() {
		return "=?UTF-8?Q?Annulation_d'un_=C3=A9v=C3=A9nement_de_Raphael";
	}

	@Override
	protected String getCancelRecurrentEventSubject() {
		return "=?UTF-8?Q?Annulation_d'un_=C3=A9v=C3=A9nement_r=C3=A9current_de_Rap?=\r\n"
				+ " =?UTF-8?Q?hael_ROUGERON_sur_OBM_:_Sprint_planning_OBM?=";
	}

	@Override
	protected String getUpdateEventSubject() {
		return "=?UTF-8?Q?Mise_=C3=A0_jour_d'un_=C3=A9v=C3=A9nement_de_Raphael";
	}

	@Override
	protected String getUpdateRecurrentEventSubject() {
		return "=?UTF-8?Q?Mise_=C3=A0_jour_d'un_=C3=A9v=C3=A9nement_r=C3=A9current_de_Rap?=\r\n"
				+ " =?UTF-8?Q?hael_ROUGERON_sur_OBM_:_Sprint_planning_OBM?=";
	}
	
	@Override
	protected String getChangeParticipationSubject() {
		return "=?UTF-8?Q?Mise_=C3=A0_jour_de_participation_?=\r\n"
				+ " =?UTF-8?Q?dans_OBM_:_Sprint_planning_OBM?=";
	}
}