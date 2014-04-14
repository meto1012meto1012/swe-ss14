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

package de.shop.bestellverwaltung.web;

import de.shop.auth.web.AuthModel;
import de.shop.auth.web.KundeLoggedIn;
import de.shop.bestellverwaltung.business.BestellungService;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.business.KundeService;
import de.shop.kundenverwaltung.business.KundeService.FetchType;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import static de.shop.util.Constants.JSF_DEFAULT_ERROR;
import static java.util.logging.Level.FINEST;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
//FIXME @Model funktioniert nicht mit bean-discovery-mode="annotated" https://issues.jboss.org/browse/CDI-406
//@Model
@Named
@RequestScoped
@Stateful
public class BestellungModel implements Serializable {
	private static final long serialVersionUID = -1790295502719370565L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private static final String JSF_VIEW_BESTELLUNG = "/rf/bestellverwaltung/viewBestellung";
	
	private Bestellung bestellung;
	
	@Inject
	private Warenkorb warenkorb;
	
	@Inject
	private BestellungService bs;
	
	@Inject
	private KundeService ks;
	
	@Inject
	private AuthModel auth;
	
	@Inject
	@KundeLoggedIn
	private AbstractKunde kunde;
	
	@Inject
	private transient HttpServletRequest request;
	

	public Bestellung getBestellung() {
		return bestellung;
	}


	public String bestellen() {
		auth.preserveLogin();
		
		if (warenkorb == null || warenkorb.getPositionen() == null || warenkorb.getPositionen().isEmpty()) {
			// Darf nicht passieren, wenn der Button zum Bestellen verfuegbar ist
			return JSF_DEFAULT_ERROR;
		}
		
		// Den eingeloggten Kunden mit seinen Bestellungen ermitteln, und dann die neue Bestellung zu ergaenzen
		kunde = ks.findKundeById(kunde.getId(), FetchType.MIT_BESTELLUNGEN);
		
		// Aus dem Warenkorb nur Positionen mit Anzahl > 0
		final List<Bestellposition> neuePositionen = warenkorb.getPositionen()
				                                              .stream()
		                                                      .filter(bp -> bp.getAnzahl() > 0)
		                                                      .collect(Collectors.toList());
		
		// Warenkorb zuruecksetzen
		warenkorb.endConversation();
		
		// Neue Bestellung mit neuen Bestellpositionen erstellen
		bestellung = new Bestellung(neuePositionen);
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest("Neue Bestellung: %"+ bestellung
					      + "\nBestellpositionen: " +  bestellung.getBestellpositionen());
		}
		
		// Bestellung mit VORHANDENEM Kunden verknuepfen:
		// dessen Bestellungen muessen geladen sein, weil es eine bidirektionale Beziehung ist
		bestellung = bs.createBestellung(bestellung, kunde);
		request.setAttribute("bestellung", bestellung);
		
		// Redirect nicht notwendig, da der Warenkorb mittlerweile geleert ist
		return JSF_VIEW_BESTELLUNG;
	}
}
