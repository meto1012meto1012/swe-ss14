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

package de.shop.bestellverwaltung.business;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.mail.AbsenderMail;
import de.shop.util.mail.AbsenderName;
import java.io.Serializable;
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

import static java.util.logging.Level.FINEST;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Stateless
public class BestellungObserver implements Serializable {
	private static final long serialVersionUID = -1567643645881819340L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static final String NEWLINE = System.getProperty("line.separator");
	
	@Inject
	private transient Session session;
	
	@Inject
	@AbsenderMail
	private String absenderMail;
	
	@Inject
	@AbsenderName
	private String absenderName;

	@PostConstruct
	private void postConstruct() {
		if (absenderMail == null) {
			LOGGER.warning("Der Absender fuer Bestellung-Emails ist nicht gesetzt.");
		}
	}
	
	@Asynchronous
	public void onCreateBestellung(@Observes @NeueBestellung final Bestellung bestellung) {
		if (absenderMail == null) {
			return;
		}
		final AbstractKunde kunde = bestellung.getKunde();
		final String empfaengerMail = kunde.getEmail();
		if (empfaengerMail == null) {
			return;
		}
		
		final MimeMessage message = new MimeMessage(session);
		try {
			// Absender setzen
			final InternetAddress absenderObj = new InternetAddress(absenderMail, absenderName);
			message.setFrom(absenderObj);

			// Empfaenger setzen
			final String vorname = kunde.getVorname() == null ? "" : kunde.getVorname();
			final String empfaengerName = vorname + " " + kunde.getNachname();
			final InternetAddress empfaenger = new InternetAddress(empfaengerMail, empfaengerName);
			message.setRecipient(RecipientType.TO, empfaenger);   // RecipientType: TO, CC, BCC

			// Subject setzen
			message.setSubject("Neue Bestellung Nr. " + bestellung.getId());

			// Text setzen mit MIME Type "text/plain"
			final StringBuilder sb = new StringBuilder(256);
			sb.append("<h3>Neue Bestellung Nr. <b>")
			  .append(bestellung.getId())
			  .append("</b></h3>")
			  .append(NEWLINE);

			bestellung.getBestellpositionen()				
					  .forEach(bp -> sb.append(bp.getAnzahl())
									   .append("\t")
									   .append(bp.getArtikel().getBezeichnung())
									   .append("<br/>")
									   .append(NEWLINE));
			final String text = sb.toString();
			if (LOGGER.isLoggable(FINEST)) {
				LOGGER.finest(text);
			}
			message.setContent(text, "text/html;charset=iso-8859-1");

			// Hohe Prioritaet einstellen
			//message.setHeader("Importance", "high");
			//message.setHeader("Priority", "urgent");
			//message.setHeader("X-Priority", "1");

			Transport.send(message);
		}
		catch (MessagingException | UnsupportedEncodingException e) {
			LOGGER.severe(e.getMessage());
		}
	}
}
