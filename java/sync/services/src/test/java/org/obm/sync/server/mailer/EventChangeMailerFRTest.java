package org.obm.sync.server.mailer;

import java.util.ArrayList;
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

	private ArrayList<String> getPlainMessage(String header) {
		return Lists.newArrayList(
				header,
				"du            : 8 nov. 2010 11:00", 
				"au            : 8 nov. 2010 11:45",
				"sujet         : Sprint planning OBM", 
				"lieu          : A random location",
				"organisateur  : Raphael ROUGERON",
				"créé par      : Emmanuel SURLEAU"
		);
	}

	private ArrayList<String> getHtmlMessage(String header) {
		return Lists.newArrayList(
				header,
				"Du 8 nov. 2010 11:00", 
				"Au 8 nov. 2010 11:45", 
				"Sujet Sprint planning OBM", 
				"Lieu A random location", 
				"Organisateur Raphael ROUGERON",
				"Créé par Emmanuel SURLEAU"
		);
	}

	private ArrayList<String> getRecurrentPlainMessage(String header) {
		return Lists.newArrayList(
				header,
				"du            : 8 nov. 2010", 
				"au            : 23 nov. 2012",
				"heure         : 11:00 - 11:45",
				"recurrence    : Toutes les 2 semaines [Lundi, Mercredi, Jeudi]",
				"sujet         : Sprint planning OBM", 
				"lieu          : A random location",
				"organisateur  : Raphael ROUGERON"
		);
	}

	private ArrayList<String> getRecurrentHtmlMessage(String header) {
		return Lists.newArrayList(
				header,
				"Du 8 nov. 2010", 
				"Au 23 nov. 2012", 
				"Sujet Sprint planning OBM", 
				"Lieu A random location",
				"Organisateur Raphael ROUGERON",
				"Heure 11:00 - 11:45",
				"Type de récurrence Toutes les 2 semaines [Lundi, Mercredi, Jeudi]"
		);
	}


	@Override
	protected ArrayList<String> getInvitationPlainMessage() {
		return getPlainMessage("NOUVEAU RENDEZ-VOUS");
	}

	@Override
	protected ArrayList<String> getInvitationHtmlMessage() {
		return getHtmlMessage("Invitation à un événement");
	}

	@Override
	protected ArrayList<String> getUpdatePlainMessage() {
		return Lists.newArrayList(
				"RENDEZ-VOUS MODIFIÉ !",
				"du 8 nov. 2010 11:00",
				"au 8 nov. 2010 11:45",
				"du            : 8 nov. 2010 12:00", 
				"au            : 8 nov. 2010 13:00",
				"sujet         : Sprint planning OBM", 
				"lieu          : A random location",
				"organisateur  : Raphael ROUGERON",
				"créé par      : Emmanuel SURLEAU"
		);
	}

	@Override
	protected ArrayList<String> getUpdateHtmlMessage() {
		return Lists.newArrayList(
				"Invitation à un évènement : mise à jour",
				"du 8 nov. 2010 11:00",
				"au 8 nov. 2010 11:45",
				"Du 8 nov. 2010 12:00", 
				"Au 8 nov. 2010 13:00", 
				"Sujet Sprint planning OBM", 
				"Lieu A random location",
				"Organisateur Raphael ROUGERON",
				"Créé par Emmanuel SURLEAU"
		);
	}

	@Override
	protected ArrayList<String> getCancelPlainMessage() {
		return getPlainMessage("RENDEZ-VOUS ANNULÉ");
	}

	@Override
	protected ArrayList<String> getCancelHtmlMessage() {
		return getHtmlMessage("Annulation d'un événement");
	}

	@Override
	protected ArrayList<String> getRecurrentInvitationPlainMessage() {
		return getRecurrentPlainMessage("NOUVEAU RENDEZ-VOUS RÉCURRENT");
	}

	@Override
	protected ArrayList<String> getRecurrentInvitationHtmlMessage() {
		return getRecurrentHtmlMessage("Invitation à un événement récurrent");
	}

	@Override
	protected ArrayList<String> getRecurrentUpdatePlainMessage() {
		return Lists.newArrayList(
				"RENDEZ-VOUS RÉCURRENT MODIFIÉ !",
				"du 8 nov. 2010", 
				"au 23 nov. 2012", 				
				"du            : 8 nov. 2010", 
				"au            : 23 nov. 2012",
				"de 11:00 à 11:45",
				"heure         : 12:00 - 13:00",
				"recurrence    : Toutes les 2 semaines [Lundi, Mercredi, Jeudi]",
				"sujet         : Sprint planning OBM", 
				"lieu          : A random location",
				"organisateur  : Raphael ROUGERON"
		);
	}

	@Override
	protected ArrayList<String> getRecurrentUpdateHtmlMessage() {
		return Lists.newArrayList(
				"Invitation à un évènement récurrent : mise à jour",
				"du 8 nov. 2010", 
				"au 23 nov. 2012", 
				"Du 8 nov. 2010", 
				"Au 23 nov. 2012", 
				"Sujet Sprint planning OBM", 
				"Lieu A random location",
				"Organisateur Raphael ROUGERON",
				"de 11:00 à 11:45",
				"Heure 12:00 - 13:00",
				"Type de récurrence Toutes les 2 semaines [Lundi, Mercredi, Jeudi]"
		);
	}
	
	@Override
	protected ArrayList<String> getNonRecurrentToRecurrentUpdatePlainMessage() {
		return Lists.newArrayList(
				"RENDEZ-VOUS RÉCURRENT MODIFIÉ !",
				"du 8 nov. 2010", 
				"au 8 nov. 2010", 				
				"du            : 8 nov. 2010", 
				"au            : 23 nov. 2012",
				"de 11:00 à 11:45",
				"heure         : 11:00 - 11:45",
				"type de récurrence : Pas de récurrence",
				"recurrence    : Toutes les 2 semaines [Lundi, Mercredi, Jeudi]",
				"sujet         : Sprint planning OBM", 
				"lieu          : A random location",
				"organisateur  : Raphael ROUGERON"
		);
	}

	@Override
	protected ArrayList<String> getNonRecurrentToRecurrentUpdateHtmlMessage() {
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
				"type de récurrence : Pas de récurrence",
				"Type de récurrence Toutes les 2 semaines [Lundi, Mercredi, Jeudi]"
		);
	}
	
	@Override
	protected ArrayList<String> getRecurrentToNonRecurrentUpdatePlainMessage() {
		return Lists.newArrayList(
				"RENDEZ-VOUS MODIFIÉ !",
				"du 8 nov. 2010 11:00",
				"au 8 nov. 2010 11:45",
				"du            : 8 nov. 2010 11:00", 
				"au            : 8 nov. 2010 11:45",
				"sujet         : Sprint planning OBM", 
				"lieu          : A random location",
				"organisateur  : Raphael ROUGERON",
				"créé par      : Emmanuel SURLEAU"
		);
	}

	@Override
	protected ArrayList<String> getRecurrentToNonRecurrentUpdateHtmlMessage() {
		return Lists.newArrayList(
				"Invitation à un évènement : mise à jour",
				"du 8 nov. 2010 11:00",
				"au 8 nov. 2010 11:45",
				"Du 8 nov. 2010 11:00", 
				"Au 8 nov. 2010 11:45", 
				"Sujet Sprint planning OBM", 
				"Lieu A random location",
				"Organisateur Raphael ROUGERON",
				"Créé par Emmanuel SURLEAU"
		);
	}
	
	@Override
	protected ArrayList<String> getRecurrentCancelPlainMessage() {
		return getRecurrentPlainMessage("RENDEZ-VOUS RÉCURRENT ANNULÉ");
	}

	@Override
	protected ArrayList<String> getRecurrentCancelHtmlMessage() {
		return getRecurrentHtmlMessage("Annulation d'un événement récurrent");
	}

	@Override
	protected ArrayList<String> getChangeParticipationPlainMessage() {
		return Lists.newArrayList(
				"PARTICIPATION : MISE A JOUR",
				"Matthieu BAECHLER a accepté",
				"l'événement Sprint planning OBM prévu le 8 nov. 2010",
				"This is a random comment"
		);
	}
	
	@Override
	protected ArrayList<String> getChangeParticipationHtmlMessage() {
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