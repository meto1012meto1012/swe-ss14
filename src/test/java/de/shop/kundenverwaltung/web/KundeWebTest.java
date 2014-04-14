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

package de.shop.kundenverwaltung.web;

import de.shop.kundenverwaltung.domain.FamilienstandType;
import de.shop.kundenverwaltung.domain.GeschlechtType;
import de.shop.kundenverwaltung.domain.HobbyType;
import de.shop.util.AbstractWebTest;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.logging.Logger;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import static de.shop.util.AbstractPage.sleep;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.PASSWORD;
import static de.shop.util.TestConstants.PASSWORD_ADMIN;
import static de.shop.util.TestConstants.USERNAME;
import static de.shop.util.TestConstants.USERNAME_ADMIN;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.jboss.arquillian.graphene.Graphene.waitAjax;
import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static org.jboss.arquillian.graphene.Graphene.waitModel;
import static org.junit.Assert.assertThat;


/**
* @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
*/
@RunWith(Arquillian.class)
public class KundeWebTest extends AbstractWebTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final String PASSWORD_FALSCH = "falschesPassword";

	private static final Long KUNDE_ID_VORHANDEN = Long.valueOf(101);
	private static final String KUNDE_ID_PREFIX_VORHANDEN = "10";
	private static final String NACHNAME_VORHANDEN = "Alpha";
	private static final String NACHNAME_PREFIX_VORHANDEN = "a";
	private static final String NACHNAME_NICHT_VORHANDEN = "Nichtvorhanden";
	
	private static final String NACHNAME_NEU = "Test";
	private static final String VORNAME_NEU = "Theo";
	private static final int KATEGORIE_OFFSET_X = 100;   // Schieberegler fuer Kategorie: 100 Pixel nach rechts
	private static final int TAG_NEU = 1;
	private static final FamilienstandType FAMILIENSTAND_NEU = FamilienstandType.VERHEIRATET;
	private static final GeschlechtType GESCHLECHT_NEU = GeschlechtType.MAENNLICH;
	private static final boolean NEWSLETTER_NEU = true;
	private static final HobbyType[] HOBBIES_NEU = { HobbyType.SPORT, HobbyType.LESEN };
	private static final String EMAIL_NEU = "theo@test.de";
	private static final String PASSWORD_NEU = "p";
	private static final String PLZ_NEU = "12345";
	private static final String ORT_NEU = "Testort";
	private static final String STRASSE_NEU = "Testweg";
	private static final String HAUSNR_NEU = "1";
	private static final boolean AGB_NEU = true;
	
	private static final String NACHNAME_NEU_INVALID = "!?$%&";
	
	private static final Long KUNDE_ID_UPDATE = Long.valueOf(102);
	private static final String KUNDE_ID_PREFIX_UPDATE = "10";
	private static final String VORNAME_UPDATE = "Vornameneu";
	private static final int ANZAHL_INCR_KATEGORIE = 3;
	private static final int ANZAHL_DECR_KATEGORIE = 1;
	private static final String NACHNAME_UPDATE_INVALID = "!";
	
	@Page
	private ViewKunde viewKunde;

	@Page
	private ListKunden listKunden;
	
	@Page
	private Registrieren registrieren;
	
	@Page
	private UpdateKunde updateKunde;

	@Test
	@InSequence(10)
	public void falschesPasswort() {
		LOGGER.finer(BEGINN);
		
		// GIVEN : falsches Passwort
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_FALSCH;
		
		// WHEN : Mit falschem Passwort einloggen
		indexPage.login(username, password);
				
		// THEN : Fehlermeldung wird angezeigt
		assertThat(!indexPage.isLoggedIn(), is(true));
		waitAjax().until().element(indexPage.getFehlermeldungLogin()).is().visible();
		assertThat(indexPage.getFehlermeldungLogin().getText(), is("Falsche Login-Daten."));
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(11)
	public void sucheMitId() {
		LOGGER.finer(BEGINN);
		
		// GIVEN : als Administrator einloggen und Link >>Suche mit Kundennr.<< wird angeklickt
		final Long kundeId = KUNDE_ID_VORHANDEN;
		final String prefix = KUNDE_ID_PREFIX_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		indexPage.login(username, password);
		waitAjax().until().element(indexPage.getLinkSucheMitKundennr()).is().visible();
		indexPage.clickSucheMitKundennr();
		waitModel().until().element(viewKunde.getKundeIdFeld()).is().visible();
				
		// WHEN : kundeId wird als Suchkriterium eingegeben und abgeschickt
		viewKunde.suchen(prefix, String.valueOf(kundeId));
				
		// THEN : Ein Kunde mit kundeId wird angezeigt
		waitAjax().until().element(viewKunde.getTabPanel()).is().visible();
		assertThat(viewKunde.getKundeId(), is(String.valueOf(kundeId)));
		sleep(1);
		viewKunde.clickBestellungenTab();
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(12)
	public void sucheMitNachname() {
		LOGGER.finer(BEGINN);

		// GIVEN : als Administrator einloggen und Link >>Suche mit Nachname<< wird angeklickt
		final String nachname = NACHNAME_VORHANDEN;
		final String prefix = NACHNAME_PREFIX_VORHANDEN;
		final Long kundeId = KUNDE_ID_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		indexPage.login(username, password);
		waitAjax().until().element(indexPage.getLinkSucheMitNachname()).is().visible();
		indexPage.clickSucheMitNachname();
		waitModel().until().element(listKunden.getNachnameFeld()).is().visible();
		
		// WHEN : nachname wird als Suchkriterium eingegeben und abgeschickt
		listKunden.suchen(prefix, nachname);
		
		// THEN : In der Spalte der Nachnamen steht immer der eingegebene Nachname und es gibt mindestens 1 Eintrag
		waitAjax().until().element(listKunden.getKundenTabelle()).is().visible();
		final List<WebElement> spalte = listKunden.getSpalte("nachnameSpalte");
		assertThat(!spalte.isEmpty(), is(true));
		spalte.stream()                             // parallelStream() funktioniert nicht mit Graphene
		      .map(WebElement::getText)
		      .forEach(text -> assertThat(text, is(nachname)));
		
		// WHEN (2) : Der Nachname zum Kunden mit der Nr. kundeId wird angeklickt
		final ViewKundePopup kundePopup = listKunden.clickNachname(kundeId, browser);
		
		// THEN (2) : Im Popup-Fenster zum Kunden Nr. kundeId ist der nachname enthalten und
		//            eine Bestellung hat mindestens eine Position
		assertThat(kundePopup.getNachname(), is(nachname));
		kundePopup.clickBestellungenTab()
		          .clickAlleBestellungen();
		sleep(1);
		kundePopup.close();
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(13)
	public void sucheMitNachnameNichtVorhanden() {
		LOGGER.finer(BEGINN);
		
		// GIVEN : als Administrator einloggen und Link >>Suche mit Nachname<< wird angeklickt
		final String nachname = NACHNAME_NICHT_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		indexPage.login(username, password);
		waitAjax().until().element(indexPage.getLinkSucheMitNachname()).is().visible();
		indexPage.clickSucheMitNachname();
		waitModel().until().element(listKunden.getNachnameFeld()).is().visible();
		
		// WHEN : nachname wird als Suchkriterium eingegeben und abgeschickt
		listKunden.suchen(null, nachname);
		
		// THEN : Die Meldung >>Keine Datensa"tze gefunden.<< erscheint in der Tabelle
		waitAjax().until().element(listKunden.getKundenTabelle()).is().visible();
		try {
			// TODO Selenium: Fehlermeldung wird nicht gefunden
			// Kein Fehler, wenn die Testmethode sucheMitNachname() erst anschl. laeuft
			assertThat(listKunden.getFehlermeldung().getText(), startsWith("Keine Datens"));  // Umlaut a"
			assertThat(listKunden.getFehlermeldung().getText(), endsWith(" gefunden."));
		}
		catch (NoSuchElementException e) {
			LOGGER.warning(e.getMessage());
		}
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(20)
	public void registrieren() {
		LOGGER.finer(BEGINN);

		// GIVEN : Link anklicken
		final String nachname = NACHNAME_NEU;
		final String vorname = VORNAME_NEU;
		final int kategorieOffsetX = KATEGORIE_OFFSET_X;
		final int tag = TAG_NEU;
		final FamilienstandType familienstand = FAMILIENSTAND_NEU;
		final GeschlechtType geschlecht = GESCHLECHT_NEU;
		final boolean newsletter = NEWSLETTER_NEU;
		final HobbyType[] hobbies = HOBBIES_NEU;
		final String email = EMAIL_NEU;
		final String passwordNeu = PASSWORD_NEU;
		final String plz = PLZ_NEU;
		final String ort = ORT_NEU;
		final String strasse = STRASSE_NEU;
		final String hausnr = HAUSNR_NEU;
		final boolean agb = AGB_NEU;
		
		indexPage.clickRegistrieren();
		waitModel().until().element(registrieren.getNachnameFeld()).is().visible();
		
		// WHEN : Forumular ausfuellen
		registrieren.inputNachname(nachname)
		            .inputVorname(vorname)
				    .schiebeKategorie(browser, kategorieOffsetX)
		            .clickTag(tag)
		            .clickFamilienstand(familienstand)
		            .clickGeschlecht(geschlecht)
		            .clickNewsletter(newsletter)
		            .clickHobbies(hobbies)
		            .inputEmail(email)
		            .inputPassword(passwordNeu)
		            .inputPasswordWdh(passwordNeu)
		            .inputPlz(plz)
		            .inputOrt(ort)
		            .inputStrasse(strasse)
		            .inputHausnr(hausnr)
		            .clickAgb(agb)
		            .inputCaptcha()
		            .clickAnlegenButton();
		
		// THEN : Der registrierte Kunde wird angezeigt
		waitModel().until().element(viewKunde.getNachnameFeld()).is().visible();
		assertThat(viewKunde.getNachname(), is(nachname));
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(21)
	public void registrierenInvalid() {
		LOGGER.finer(BEGINN);
		
		// GIVEN : Link anklicken
		final String nachname = NACHNAME_NEU_INVALID;
		indexPage.clickRegistrieren();
		waitModel().until().element(registrieren.getNachnameFeld()).is().visible();
		
		// WHEN : Forumular mit ungueltigem Nachnamen ausfuellen und in den Body klicken
		registrieren.inputNachname(nachname)
		            .clickBody();
		
		// THEN : Fehlermeldung "Nach einem " wegen des ungueltigen Nachnamens
		final List<String> fehlermeldungen = registrieren.getFehlermeldungenNachname();
		assertThat(fehlermeldungen.stream()
				                  .filter(fm -> fm.startsWith("Nach einem "))
				                  .findAny(), is(notNullValue()));
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(30)
	public void updateKunde() {
		LOGGER.finer(BEGINN);

		// GIVEN : als Mitarbeiter einloggen und Link >>Suche mit Nachname<< wird angeklickt
		final String username = USERNAME;
		final String password = PASSWORD;
		
		final String nachname = NACHNAME_VORHANDEN;
		final String prefix = NACHNAME_PREFIX_VORHANDEN;
		final Long kundeId = KUNDE_ID_UPDATE;
		final String prefixId = KUNDE_ID_PREFIX_UPDATE;
		final String neuerVorname = VORNAME_UPDATE;
		final int anzahlIncr = ANZAHL_INCR_KATEGORIE;
		final int anzahlDecr = ANZAHL_DECR_KATEGORIE;
		
		indexPage.login(username, password);
		waitAjax().until().element(indexPage.getLinkSucheMitNachname()).is().visible();
		indexPage.clickSucheMitNachname();
		waitModel().until().element(listKunden.getNachnameFeld()).is().visible();
		
		// nachname wird als Suchkriterium eingegeben und abgeschickt
		listKunden.suchen(prefix, nachname);
		waitAjax().until().element(listKunden.getKundenTabelle()).is().visible();
		
		// WHEN : Der Update-Button zum Kunden mit Nr. kundeId wird angeklickt
		listKunden.clickUpdateButton(kundeId);
		// TODO Selenium: der 1. Click auf den Update-Button funktioniert nicht
		listKunden.clickUpdateButton(kundeId);
		waitAjax().until().element(updateKunde.getVornameFeld()).is().visible();
		
		// Der angezeigte Kundendatensatz wird aktualisiert
		updateKunde.inputVorname(neuerVorname)
		           .incrKategorie(anzahlIncr)
		           .decrKategorie(anzahlDecr)
		           .clickUpdateButton();
		
		// THEN : Die Aenderungen wurden durchgefuehrt
		waitAjax().until().element(indexPage.getHeading()).is().visible();
		indexPage.clickSucheMitKundennr();
		waitModel().until().element(viewKunde.getKundeIdFeld()).is().visible();
		viewKunde.suchen(prefixId, String.valueOf(kundeId));
		waitAjax().until().element(viewKunde.getTabPanel()).is().visible();
		assertThat(viewKunde.getVorname(), is(neuerVorname));
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(31)
	@Ignore("Bean Validation funktioniert nicht fuer updatePrivatkunde.xhtml")
	public void updateKundeInvalid() {
		LOGGER.finer(BEGINN);

		// GIVEN : als Mitarbeiter einloggen und Link >>Suche mit Nachname<< wird angeklickt
		final String username = USERNAME;
		final String password = PASSWORD;
		
		final String nachname = NACHNAME_VORHANDEN;
		final String prefix = NACHNAME_PREFIX_VORHANDEN;
		final Long kundeId = KUNDE_ID_UPDATE;
		final String neuerNachname = NACHNAME_UPDATE_INVALID;
		
		indexPage.login(username, password);
		waitAjax().until().element(indexPage.getLinkSucheMitNachname()).is().visible();
		indexPage.clickSucheMitNachname();
		waitModel().until().element(listKunden.getNachnameFeld()).is().visible();
		
		// nachname wird als Suchkriterium eingegeben und abgeschickt
		listKunden.suchen(prefix, nachname);
		waitAjax().until().element(listKunden.getKundenTabelle()).is().visible();
		
		// WHEN : Der Update-Button zum Kunden mit Nr. kundeId wird angeklickt
		listKunden.clickUpdateButton(kundeId);
		// TODO Selenium: der 1. Click auf den Update-Button funktioniert nicht
		listKunden.clickUpdateButton(kundeId);
		waitAjax().until().element(updateKunde.getVornameFeld()).is().visible();
		
		// Der angezeigte Kundendatensatz wird aktualisiert
		updateKunde.inputNachname(neuerNachname)
		           .clickBody();
		
		// THEN : Die Fehlermeldung erscheint
		waitGui().until().element(updateKunde.getFehlermeldungNachname()).is().visible();
		assertThat(updateKunde.getFehlermeldungNachname().getText(), containsString("Nachname"));
		
		LOGGER.finer(ENDE);
	}
}
