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

import de.shop.artikelverwaltung.web.ListArtikel;
import de.shop.artikelverwaltung.web.SelectArtikel;
import de.shop.util.AbstractWebTest;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.PASSWORD;
import static de.shop.util.TestConstants.USERNAME;
import static org.hamcrest.CoreMatchers.is;
import static org.jboss.arquillian.graphene.Graphene.waitAjax;
import static org.jboss.arquillian.graphene.Graphene.waitModel;
import static org.junit.Assert.assertThat;

/**
* @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
*/
@RunWith(Arquillian.class)
public class BestellungWebTest extends AbstractWebTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Page
	private ListArtikel listArtikel;
	
	@Page
	private SelectArtikel selectArtikel;
	
	@Page
	private WarenkorbPage warenkorb;
	
	@Page
	private ConfirmBestellung confirmBestellung;
	
	@Page
	private ViewBestellung viewBestellung;

	@Test
	@InSequence(1)
	public void bestellen() {
		LOGGER.finer(BEGINN);
		
		// GIVEN : als Mitarbeiter einloggen
		final String username = USERNAME;
		final String password = PASSWORD;
		indexPage.login(username, password);
		waitAjax().until().element(indexPage.getLinkSucheArtikel()).is().visible();
	
		// WHEN
		
		// Link >>Suche Artikel<< wird angeklickt, alle Artikel suchen und den obersten Artikel in den Warenkorb
		indexPage.clickSucheArtikel();
		waitModel().until().element(listArtikel.getBezeichnungFeld()).is().visible();
		listArtikel.suchen(null);
		waitAjax().until().element(listArtikel.getArtikelTabelle()).is().visible();
		listArtikel.inDenWarenkorb(0);
		
		// Link >>Suche Artikel<< wird angeklickt, alle Artikel suchen und den naechsten Artikel in den Warenkorb
		indexPage.clickSucheArtikel();
		waitModel().until().element(listArtikel.getBezeichnungFeld()).is().visible();
		listArtikel.suchen(null);
		waitAjax().until().element(listArtikel.getArtikelTabelle()).is().visible();
		listArtikel.inDenWarenkorb(1);
		
		// bestellen
		waitModel().until().element(warenkorb.getBestellenButton()).is().visible();
		final int zeile = 0;
		final int anzahlKlicks = 4;
		warenkorb.setAnzahl(zeile, anzahlKlicks)
				 .bestellen();
		waitModel().until().element(confirmBestellung.getBestellenButton()).is().visible();
		assertThat(confirmBestellung.getAnzahl(zeile), is(1 + anzahlKlicks));
		confirmBestellung.bestellen();
		
		// THEN
		// FIXME Warum erscheint nach dem Anklicken von "Bestellen" eine leere Seite?
//		waitModel().until().element(viewBestellung.getPanelHeader()).is().visible();
//		assertThat(viewBestellung.getPanelHeader().getText(), startsWith("Bestellung"));
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(2)
	@Ignore("Klick auf Artikelauswahl funktioniert nicht mit Selenium")
	public void bestellenSelect() {
		LOGGER.finer(BEGINN);
		
		// GIVEN : als Mitarbeiter einloggen
		final String username = USERNAME;
		final String password = PASSWORD;
		indexPage.login(username, password);
	
		// WHEN
		
		// Link >>Artikel Auswahl<< wird angeklickt, alle Artikel suchen und den obersten Artikel in den Warenkorb
		waitAjax().until().element(indexPage.getLinkArtikelAuswahl()).is().visible();
		indexPage.clickArtikelAuswahl();
		waitModel().until().element(selectArtikel.getSelectArrow()).is().visible();
		selectArtikel.select(0);
		
		// Link >>Artikel Auswahl<< wird angeklickt, alle Artikel suchen und den naechsten Artikel in den Warenkorb
		indexPage.clickArtikelAuswahl();
		waitModel().until().element(selectArtikel.getSelectArrow()).is().visible();
		selectArtikel.select(1);
		
		// bestellen
		waitModel().until().element(warenkorb.getBestellenButton()).is().visible();
		final int zeile = 0;
		final int anzahlKlicks = 3;
		warenkorb.setAnzahl(zeile, anzahlKlicks)
				 .bestellen();
		waitModel().until().element(confirmBestellung.getBestellenButton()).is().visible();
		assertThat(confirmBestellung.getAnzahl(zeile), is(1 + anzahlKlicks));
		confirmBestellung.bestellen();
		
		// THEN
		// FIXME Warum erscheint nach dem Anklicken von "Bestellen" eine leere Seite?
//		waitModel().until().element(viewBestellung.getPanelHeader()).is().visible();
//		assertThat(viewBestellung.getPanelHeader().getText(), startsWith("Bestellung"));
		
		LOGGER.finer(ENDE);
	}
}
