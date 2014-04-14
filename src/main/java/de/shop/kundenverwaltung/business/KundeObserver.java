/*
 * Copyright (C) 2013 Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.shop.kundenverwaltung.business;

import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.util.mail.AbsenderMail;
import de.shop.util.mail.AbsenderName;
import de.shop.util.mail.EmpfaengerMail;
import de.shop.util.mail.EmpfaengerName;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Stateless
public class KundeObserver {
	private static final String NEWLINE = System.getProperty("line.separator");
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private transient Session session;
	
	@Inject
	@AbsenderMail
	private String absenderMail;
	
	@Inject
	@AbsenderName
	private String absenderName;

	@Inject
	@EmpfaengerMail
	private String empfaengerMail;
	
	@Inject
	@EmpfaengerName
	private String empfaengerName;
	
	@PostConstruct
	// Attribute mit @Inject sind initialisiert
	private void postConstruct() {
		if (absenderMail == null || empfaengerMail == null) {
			LOGGER.warning("Absender oder Empfaenger fuer Markteting-Emails sind nicht gesetzt.");
			return;
		}
		LOGGER.info("Absender fuer Markteting-Emails: " + absenderName + "<" + absenderMail + ">");
		LOGGER.info("Empfaenger fuer Markteting-Emails: " + empfaengerName + "<" + empfaengerMail + ">");
	}
	
	// Loose Kopplung durch @Observes, d.h. ohne JMS
	@Asynchronous
	public void onCreateKunde(@Observes @NeuerKunde final AbstractKunde kunde) {
		if (absenderMail == null || empfaengerMail == null || kunde == null) {
			return;
		}
		
		final MimeMessage message = new MimeMessage(session);

		try {
			// Absender setzen
			final InternetAddress absenderObj = new InternetAddress(absenderMail, absenderName);
			message.setFrom(absenderObj);

			// Empfaenger setzen
			final InternetAddress empfaenger = new InternetAddress(empfaengerMail, empfaengerName);
			message.setRecipient(RecipientType.TO, empfaenger);   // RecipientType: TO, CC, BCC

			final Adresse adr = kunde.getAdresse();

			// Subject setzen
			final String subject = adr == null
					? "Neuer Kunde ohne Adresse"
					: "Neuer Kunde in " + adr.getPlz() + " " + adr.getOrt();
			message.setSubject(subject);

			// HTML-Text setzen mit MIME Type "text/html"
			final String text = adr == null
					? "<p><b>" + kunde.getVorname() + " " + kunde.getNachname() + "</b></p>" + NEWLINE
					: "<p><b>" + kunde.getVorname() + " " + kunde.getNachname() + "</b></p>" + NEWLINE
					  + "<p>" + adr.getPlz() + " " + adr.getOrt() + "</p>" + NEWLINE
					  + "<p>" + adr.getStrasse() + " " + adr.getHausnr() + "</p>" + NEWLINE;

			message.setContent(text, "text/html");

			// Hohe Prioritaet einstellen
			//message.setHeader("Importance", "high");
			//message.setHeader("Priority", "urgent");
			//message.setHeader("X-Priority", "1");

			// HTML-Text mit einem Bild als Attachment
			Transport.send(message);
		}
		catch (MessagingException | UnsupportedEncodingException e) {
			LOGGER.severe(e.getMessage());
		}
	}
}
