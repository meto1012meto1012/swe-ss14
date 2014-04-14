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

package de.shop.artikelverwaltung.web;

import de.shop.artikelverwaltung.business.ArtikelService;
import de.shop.artikelverwaltung.domain.Artikel;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static de.shop.util.Constants.BESTELLVORGANG_TIMEOUT;
import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.TIMEOUT_UNIT;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;


/**
 * Dialogsteuerung fuer ArtikelService
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@ConversationScoped
@Stateful
public class ArtikelModel implements Serializable {
	private static final long serialVersionUID = 1564024850446471639L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final String JSF_LIST_ARTIKEL = "/rf/artikelverwaltung/listArtikel";
	private static final String JSF_SELECT_ARTIKEL = "/rf/artikelverwaltung/selectArtikel";
	
	private String bezeichnung;
	
	private List<Artikel> artikel;
	
	private List<Artikel> verfuegbareArtikel;
	
	private Artikel neuerArtikel;

	@Inject
	private ArtikelService as;
	
	@Inject
	private transient Conversation conversation;

	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	public List<Artikel> getArtikel() {
		return artikel;
	}

	public List<Artikel> getVerfuegbareArtikel() {
		return verfuegbareArtikel;
	}

	public Artikel getNeuerArtikel() {
		return neuerArtikel;
	}

	private void beginConversation() {
		if (!conversation.isTransient()) {
			if (LOGGER.isLoggable(FINER)) {
				LOGGER.finer("Die Conversation ist bereits gestartet");
			}
			return;
		}
		
		if (LOGGER.isLoggable(FINER)) {
			LOGGER.finer("Neue Conversation wird gestartet");
		}
		conversation.begin();
		conversation.setTimeout(TIMEOUT_UNIT.toMillis(BESTELLVORGANG_TIMEOUT));
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest("Neue Conversation beginnt");
		}
	}
	
	public String findArtikelByBezeichnung() {
		beginConversation();
		artikel = as.findArtikelByBezeichnung(bezeichnung);
		return JSF_LIST_ARTIKEL;
	}
	
	public String selectArtikel() {
		beginConversation();
		if (verfuegbareArtikel == null) {
			verfuegbareArtikel = as.findVerfuegbareArtikel();
		}
		
		return JSF_SELECT_ARTIKEL;
	}

	public void createEmptyArtikel() {
		beginConversation();
		if (neuerArtikel != null) {
			return;
		}
		
		neuerArtikel = new Artikel();
	}
	
	public String createArtikel() {
		as.createArtikel(neuerArtikel);
		neuerArtikel = null;
		conversation.end();
		return JSF_INDEX;
	}
	
	@Override
	public String toString() {
		return "ArtikelModel [bezeichnung=" + bezeichnung + ", artikel=" + artikel
				+ ", verfuegbareArtikel=" + verfuegbareArtikel + "]";
	}
}
