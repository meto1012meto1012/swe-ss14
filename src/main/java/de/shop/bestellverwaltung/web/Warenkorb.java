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

import de.shop.artikelverwaltung.business.ArtikelService;
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.util.interceptor.Log;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static java.util.logging.Level.FINEST;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@ConversationScoped
@Log
public class Warenkorb implements Serializable {
	private static final long serialVersionUID = -1981070683990640854L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final String JSF_VIEW_WARENKORB = "/rf/bestellverwaltung/viewWarenkorb?init=true";
	
	private List<Bestellposition> positionen;
	private Long artikelId;  // fuer selectArtikel.xhtml
	
	@Inject
	private transient Conversation conversation;
	
	@Inject
	private ArtikelService as;

	@PostConstruct
	private void postConstruct() {
		positionen = new ArrayList<>();
	}
	
	public List<Bestellposition> getPositionen() {
		return positionen;
	}
		
	public void setArtikelId(Long artikelId) {
		this.artikelId = artikelId;
	}

	public Long getArtikelId() {
		return artikelId;
	}

	@Override
	public String toString() {
		return "Warenkorb " + positionen;
	}
	
	/**
	 * Den selektierten Artikel zum Warenkorb hinzufuergen
	 * @param artikel Der selektierte Artikel
	 * @return Pfad zur Anzeige des aktuellen Warenkorbs
	 */
	public String add(Artikel artikel) {
		for (Bestellposition bp : positionen) {
			if (bp.getArtikel().equals(artikel)) {
				// bereits im Warenkorb
				final int vorhandeneAnzahl = bp.getAnzahl();
				bp.setAnzahl(vorhandeneAnzahl + 1);
				return JSF_VIEW_WARENKORB;  // kein Lambda-Ausdruck moeglich
			}
		}
		
		final Bestellposition neu = new Bestellposition(artikel);
		positionen.add(neu);
		return JSF_VIEW_WARENKORB;
	}
	
	/**
	 * Den selektierten Artikel zum Warenkorb hinzufuergen
	 * @return Pfad zur Anzeige des aktuellen Warenkorbs
	 */
	public String add() {
		final Artikel artikel = as.findArtikelById(artikelId);
		final String outcome = add(artikel);
		artikelId = null;
		return outcome;
	}
	
	public void endConversation() {
		conversation.end();
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest("Conversation beendet");
		}
	}
	
	/**
	 * Eine potenzielle Bestellposition entfernen
	 * @param bestellposition Die zu entfernende Bestellposition
	 */
	public void remove(Bestellposition bestellposition) {
		positionen.remove(bestellposition);
		if (positionen.isEmpty()) {
			endConversation();
		}
	}
}
