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

package de.shop.kundenverwaltung.rest;

import de.shop.auth.domain.RolleType;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.GeschlechtType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.util.AbstractResourceTest;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ViolationReport;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.kundenverwaltung.rest.KundeResource.KUNDEN_GESCHLECHT_QUERY_PARAM;
import static de.shop.kundenverwaltung.rest.KundeResource.KUNDEN_ID_PATH_PARAM;
import static de.shop.kundenverwaltung.rest.KundeResource.KUNDEN_NACHNAME_QUERY_PARAM;
import static de.shop.util.Constants.FIRST_LINK;
import static de.shop.util.Constants.LAST_LINK;
import static de.shop.util.Constants.SELF_LINK;
import static de.shop.util.TestConstants.ARTIKEL_URI;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.BESTELLUNGEN_URI;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.KUNDEN_ID_URI;
import static de.shop.util.TestConstants.KUNDEN_IMAGE_ID_URI;
import static de.shop.util.TestConstants.KUNDEN_IMAGE_URI;
import static de.shop.util.TestConstants.KUNDEN_PRIVAT_UPDATE_URI;
import static de.shop.util.TestConstants.KUNDEN_PRIVAT_URI;
import static de.shop.util.TestConstants.KUNDEN_URI;
import static de.shop.util.TestConstants.NOT_EXISTING_URI;
import static de.shop.util.TestConstants.PASSWORD;
import static de.shop.util.TestConstants.PASSWORD_ADMIN;
import static de.shop.util.TestConstants.PASSWORD_FALSCH;
import static de.shop.util.TestConstants.USERNAME;
import static de.shop.util.TestConstants.USERNAME_ADMIN;
import static java.math.BigDecimal.ZERO;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNSUPPORTED_TYPE;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class KundeResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final Long KUNDE_ID_VORHANDEN_MIT_BESTELLUNGEN = Long.valueOf(101);
	private static final Long KUNDE_ID_NICHT_VORHANDEN = Long.valueOf(1000);
	private static final Long KUNDE_ID_UPDATE = Long.valueOf(120);
	private static final Long KUNDE_ID_DELETE = Long.valueOf(122);
	private static final Long KUNDE_ID_DELETE_MIT_BESTELLUNGEN = Long.valueOf(101);
	private static final Long KUNDE_ID_DELETE_FORBIDDEN = Long.valueOf(101);
	private static final String NACHNAME_VORHANDEN = "Alpha";
	private static final String NACHNAME_NICHT_VORHANDEN = "Falschername";
	private static final String NACHNAME_INVALID = "Test9";
	private static final String NEUER_NACHNAME = "Nachnameneu";
	private static final String NEUER_NACHNAME_INVALID = "!";
	private static final String NEUER_VORNAME = "Vorname";
	private static final String NEUE_EMAIL = NEUER_NACHNAME + "@test.de";
	private static final String NEUE_EMAIL_INVALID = "?";
	private static final int NEUE_KATEGORIE = 1;
	private static final BigDecimal NEUER_RABATT = new BigDecimal("0.15");
	private static final BigDecimal NEUER_UMSATZ = new BigDecimal("10000000");
	private static final int TAG = 1;
	private static final int MONAT = Calendar.FEBRUARY;
	private static final int JAHR = 2014;
	private static final Date NEU_SEIT = new GregorianCalendar(JAHR, MONAT, TAG).getTime();
	private static final String NEU_SEIT_STR = "2014-02-01";
	private static final String NEUE_PLZ = "76133";
	private static final String NEUE_PLZ_FALSCH = "1234";
	private static final String NEUER_ORT = "Karlsruhe";
	private static final String NEUE_STRASSE = "Testweg";
	private static final String NEUE_HAUSNR = "1";
	private static final String NEUES_PASSWORD = "neuesPassword";
	private static final Long ARTIKEL_ID_VORHANDEN = Long.valueOf(301);
	
	private static final String IMAGE_FILENAME = "image.png";
	private static final String IMAGE_PATH_UPLOAD = "src/test/resources/rest/" + IMAGE_FILENAME;
	private static final String IMAGE_MIMETYPE = "image/png";
	private static final String IMAGE_PATH_DOWNLOAD = "target/" + IMAGE_FILENAME;
	private static final Long KUNDE_ID_UPLOAD = Long.valueOf(102);

	private static final String IMAGE_INVALID = "image.bmp";
	private static final String IMAGE_INVALID_PATH = "src/test/resources/rest/" + IMAGE_INVALID;
	private static final String IMAGE_INVALID_MIMETYPE = "image/bmp";
	
	
	@Test
	@InSequence(1)
	public void validate() {
		LOGGER.finer(BEGINN);
		
		assertThat(true, is(true));
		
		LOGGER.finer(ENDE);
	}
	
	@Ignore("Beispiel fuer Ignore")
	@Test
	@InSequence(2)
	public void beispielIgnore() {
		assertThat(true, is(false));
	}
	
	@Ignore("Beispiel fuer Fail")
	@Test
	@InSequence(3)
	public void beispielFailMitIgnore() {
		fail("Beispiel fuer fail()");
	}
	
	@Test
	@InSequence(10)
	public void findKundeMitBestellungenById() {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long kundeId = KUNDE_ID_VORHANDEN_MIT_BESTELLUNGEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		Response response = getHttpsClient(username, password)
				            .target(KUNDEN_ID_URI)
                            .resolveTemplate(KUNDEN_ID_PATH_PARAM, kundeId)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
	
		// Then
		assertThat(response.getStatus(), is(HTTP_OK));
		final AbstractKunde kunde = response.readEntity(AbstractKunde.class);
		assertThat(kunde.getId(), is(kundeId));
		assertThat(!kunde.getNachname().isEmpty(), is(true));
		assertThat(kunde.getAdresse(), notNullValue());
		assertThat(kunde.isAgbAkzeptiert(), is(true));
		
		// Link-Header fuer Bestellungen pruefen
		assertThat(!response.getLinks().isEmpty(), is(true));
		assertThat(response.getLink(SELF_LINK).getUri().toString(), containsString(String.valueOf(kundeId)));
		
		final URI bestellungenUri = kunde.getBestellungenUri();
		assertThat(bestellungenUri, notNullValue());
		
		response = getHttpsClient(username, password)
				   .target(bestellungenUri)
				   .request()
				   .accept(APPLICATION_JSON)
				   .get();
		assertThat(response.getStatus(), is(HTTP_OK));
		
		// Verweist der Link-Header der ermittelten Bestellungen auf den Kunden?
		final Collection<Bestellung> bestellungen = response.readEntity(new GenericType<Collection<Bestellung>>() { });
		
		assertThat(!bestellungen.isEmpty(), is(true));
		assertThat(bestellungen, not(hasItem((Bestellung) null)));
		final String kundeIdStr = String.valueOf(kundeId);
		bestellungen.parallelStream()
				    .map(Bestellung::getKundeUri)
				    .map(URI::toString)
				    .forEach(s -> assertThat(s, endsWith(kundeIdStr)));
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(11)
	public void findKundeByIdNichtVorhanden() {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long kundeId = KUNDE_ID_NICHT_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		final Response response = getHttpsClient(username, password)
				                  .target(KUNDEN_ID_URI)
                                  .resolveTemplate(KUNDEN_ID_PATH_PARAM, kundeId)
                                  .request()
                                  .acceptLanguage(GERMAN)
                                  .get();

    	// Then
    	assertThat(response.getStatus(), is(HTTP_NOT_FOUND));
    	final String fehlermeldung = response.readEntity(String.class);
    	assertThat(fehlermeldung, startsWith("Kein Kunde mit dieser ID"));
    	assertThat(fehlermeldung, endsWith("gefunden."));
		
		LOGGER.finer(ENDE);
	}

	@Test
	@InSequence(10)
	public void notExistingUri() {
		LOGGER.finer(BEGINN);
		
		// Given
		
		// When
		Response response = getHttpsClient().target(NOT_EXISTING_URI)
                                            .request()
                                            .get();
	
		// Then
		assertThat(response.getStatus(), is(HTTP_NOT_FOUND));
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(20)
	public void findKundenByNachnameVorhanden() {
		LOGGER.finer(BEGINN);
		
		// Given
		final String nachname = NACHNAME_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;

		// When
		Response response = getHttpsClient(username, password)
				            .target(KUNDEN_URI)
                            .queryParam(KUNDEN_NACHNAME_QUERY_PARAM, nachname)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();

		// Then
		assertThat(response.getStatus(), is(HTTP_OK));
		
		final Collection<AbstractKunde> kunden =
				                        response.readEntity(new GenericType<Collection<AbstractKunde>>() { });
		assertThat(!kunden.isEmpty(), is(true));
		assertThat(kunden, not(hasItem((AbstractKunde) null)));
		
		assertThat(!response.getLinks().isEmpty(), is(true));
		assertThat(response.getLink(FIRST_LINK), notNullValue());
		assertThat(response.getLink(LAST_LINK), notNullValue());

		kunden.forEach(k -> {  // NICHT parallelStream wegen Response.close()
			assertThat(k.getNachname(), is(nachname));
			
			final URI bestellungenUri = k.getBestellungenUri();
			assertThat(bestellungenUri, notNullValue());
			final Response r = getHttpsClient(username, password)
					           .target(bestellungenUri)
					           .request()
					           .accept(APPLICATION_JSON)
					           .get();
			assertThat(r.getStatus(), anyOf(is(HTTP_OK), is(HTTP_NOT_FOUND)));
			r.close();           // readEntity() wurde nicht aufgerufen
		});
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(21)
	public void findKundenByNachnameNichtVorhanden() {
		LOGGER.finer(BEGINN);
		
		// Given
		final String nachname = NACHNAME_NICHT_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		final Response response = getHttpsClient(username, password)
				                  .target(KUNDEN_URI)
                                  .queryParam(KUNDEN_NACHNAME_QUERY_PARAM, nachname)
                                  .request()
                                  .acceptLanguage(GERMAN)
                                  .get();
		
		// Then
		assertThat(response.getStatus(), is(HTTP_NOT_FOUND));
		final String fehlermeldung = response.readEntity(String.class);
		assertThat(fehlermeldung, is("Kein Kunde mit dem Nachnamen gefunden."));

		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(22)
	public void findKundenByNachnameInvalid() {
		LOGGER.finer(BEGINN);
		
		// Given
		final String nachname = NACHNAME_INVALID;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		final Response response = getHttpsClient(username, password)
				                  .target(KUNDEN_URI)
                                  .queryParam(KUNDEN_NACHNAME_QUERY_PARAM, nachname)
                                  .request()
                                  .accept(APPLICATION_JSON)
                                  .acceptLanguage(ENGLISH)
                                  .get();
		
		// Then
		assertThat(response.getStatus(), is(HTTP_BAD_REQUEST));
		assertThat(response.getHeaderString("validation-exception"), is("true"));
		final ViolationReport violationReport = response.readEntity(ViolationReport.class);
		final Collection<ResteasyConstraintViolation> violations = violationReport.getParameterViolations();
		assertThat(!violations.isEmpty(), is(true));
		
		boolean found = false;
		final ResteasyConstraintViolation violation =
				                          violations.stream()
		                                            .filter(v -> v.getMessage().startsWith("A lastname must start with exactly"))
		                                            .findFirst()
		                                            .get();
		assertThat(violation, notNullValue());
		assertThat(violation.getValue(), is(nachname));

		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(30)
	public void findKundenByGeschlecht() {
		LOGGER.finer(BEGINN);
		
		// Given
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		for (GeschlechtType geschlecht : GeschlechtType.values()) {
			// When
			final Response response = getHttpsClient(username, password)
					                  .target(KUNDEN_URI)
                                      .queryParam(KUNDEN_GESCHLECHT_QUERY_PARAM, geschlecht)
                                      .request()
                                      .accept(APPLICATION_JSON)
                                      .get();
			final Collection<Privatkunde> kunden = response.readEntity(new GenericType<Collection<Privatkunde>>() { });
			
			// Then
            assertThat(!kunden.isEmpty(), is(true));
            assertThat(kunden, not(hasItem((Privatkunde) null)));
            kunden.parallelStream()
                  .map(Privatkunde::getGeschlecht)
		          .forEach(g -> assertThat(g, is(geschlecht)));
		}
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(40)
	public void createPrivatkunde() throws URISyntaxException {
		LOGGER.finer(BEGINN);
		
		// Given
		final String nachname = NEUER_NACHNAME;
		final String vorname = NEUER_VORNAME;
		final String email = NEUE_EMAIL;
		final int kategorie = NEUE_KATEGORIE;
		final BigDecimal rabatt = NEUER_RABATT;
		final BigDecimal umsatz = NEUER_UMSATZ;
		final Date seit = NEU_SEIT;
		final boolean agbAkzeptiert = true;
		final String plz = NEUE_PLZ;
		final String ort = NEUER_ORT;
		final String strasse = NEUE_STRASSE;
		final String hausnr = NEUE_HAUSNR;
		final String neuesPassword = NEUES_PASSWORD;
		
		final Privatkunde kunde = new Privatkunde(nachname, vorname, email, seit);
		kunde.setVorname(vorname);
		kunde.setKategorie(kategorie);
		kunde.setRabatt(rabatt);
		kunde.setUmsatz(umsatz);
		kunde.setAgbAkzeptiert(agbAkzeptiert);
		final Adresse adresse = new Adresse(plz, ort, strasse, hausnr);
		kunde.setAdresse(adresse);
		kunde.setPassword(neuesPassword);
		kunde.setPasswordWdh(neuesPassword);
		kunde.addRollen(Arrays.asList(RolleType.KUNDE, RolleType.MITARBEITER));
		
		Response response = getHttpsClient().target(KUNDEN_URI)
                                            .request()
                                            .post(json(kunde));
			
		// Then
		assertThat(response.getStatus(), is(HTTP_CREATED));
		String location = response.getLocation().toString();
		response.close();
		
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		final Long id = Long.valueOf(idStr);
		assertThat(id > 0, is(true));
		
		// Einloggen als neuer Kunde und Bestellung aufgeben

		// Given (2)
		final Long artikelId = ARTIKEL_ID_VORHANDEN;
		final String username = idStr;

		// When (2)
		final Bestellung bestellung = new Bestellung();
		final Bestellposition bp = new Bestellposition();
		bp.setArtikelUri(new URI(ARTIKEL_URI + "/" + artikelId));
		bp.setAnzahl(1);
		bestellung.addBestellposition(bp);
		bestellung.setGesamtbetrag(ZERO);   // Dummy, damit @NotNull nicht verletzt wird
		
		// Then (2)
		response = getHttpsClient(username, neuesPassword)
				   .target(BESTELLUNGEN_URI)
                   .request()
                   .post(json(bestellung));

		assertThat(response.getStatus(), is(HTTP_CREATED));
		location = response.getLocation().toString();
		response.close();
		assertThat(!location.isEmpty(), is(true));

		LOGGER.finer(ENDE);
	}
	
	
	@Test
	@InSequence(41)
	public void createPrivatkundeForm() throws URISyntaxException {
		LOGGER.finer(BEGINN);
		
		// Given
		final String nachname = NEUER_NACHNAME + "f";
		final String vorname = NEUER_VORNAME + "f";
		final String email =  "f" + NEUE_EMAIL;
		final int kategorie = NEUE_KATEGORIE;
		final BigDecimal rabatt = NEUER_RABATT;
		final BigDecimal umsatz = NEUER_UMSATZ;
		final String seit = NEU_SEIT_STR;
		final boolean agbAkzeptiert = true;
		final String plz = NEUE_PLZ;
		final String ort = NEUER_ORT + "f";
		final String strasse = NEUE_STRASSE + "f";
		final String hausnr = NEUE_HAUSNR + "f";
		final String neuesPassword = NEUES_PASSWORD + "f";
		
		final Form form = new Form();
		form.param("nachname", nachname)
		    .param("vorname", vorname)
		    .param("email", email)
		    .param("kategorie", String.valueOf(kategorie))
		    .param("rabatt", String.valueOf(rabatt))
		    .param("umsatz", String.valueOf(umsatz))
		    .param("seit", seit)
		    .param("agbAkzeptiert", String.valueOf(agbAkzeptiert))
		    .param("plz", plz)
		    .param("ort", ort)
		    .param("strasse", strasse)
		    .param("hausnr", hausnr)
		    .param("password", neuesPassword)
		    .param("passwordWdh", neuesPassword);
		
		Response response = getHttpsClient().target(KUNDEN_PRIVAT_URI)
                                            .request()
                                            .post(form(form));
			
		// Then
		assertThat(response.getStatus(), is(HTTP_CREATED));
		String location = response.getLocation().toString();
		response.close();
		
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		final Long id = Long.valueOf(idStr);
		assertThat(id > 0, is(true));
		
		// Einloggen als neuer Kunde und Bestellung aufgeben

		// Given (2)
		final Long artikelId = ARTIKEL_ID_VORHANDEN;
		final String username = idStr;

		// When (2)
		final Bestellung bestellung = new Bestellung();
		final Bestellposition bp = new Bestellposition();
		bp.setArtikelUri(new URI(ARTIKEL_URI + "/" + artikelId));
		bp.setAnzahl(1);
		bestellung.addBestellposition(bp);
		bestellung.setGesamtbetrag(ZERO);   // Dummy, damit @NotNull nicht verletzt wird

		// Then (2)
		response = getHttpsClient(username, neuesPassword)
				   .target(BESTELLUNGEN_URI)
                   .request()
                   .post(json(bestellung));

		assertThat(response.getStatus(), is(HTTP_CREATED));
		location = response.getLocation().toString();
		response.close();
		assertThat(!location.isEmpty(), is(true));

		LOGGER.finer(ENDE);
	}

	@Test
	@InSequence(42)
	public void registrierePrivatkundeInvalid() {
		LOGGER.finer(BEGINN);
		
		// Given
		final String nachname = NEUER_NACHNAME_INVALID;
		final String vorname = NEUER_VORNAME;
		final String email = NEUE_EMAIL_INVALID;
		final Date seit = NEU_SEIT;
		final boolean agbAkzeptiert = false;
		final String password = NEUES_PASSWORD;
		final String passwordWdh = NEUES_PASSWORD + "x";
		final String plz = NEUE_PLZ_FALSCH;
		final String ort = NEUER_ORT;
		final String strasse = NEUE_STRASSE;
		final String hausnr = NEUE_HAUSNR;

		final Privatkunde kunde = new Privatkunde(nachname, vorname, email, seit);
		kunde.setVorname(vorname);
		kunde.setAgbAkzeptiert(agbAkzeptiert);
		kunde.setPassword(password);
		kunde.setPasswordWdh(passwordWdh);
		final Adresse adresse = new Adresse(plz, ort, strasse, hausnr);
		adresse.setKunde(kunde);
		kunde.setAdresse(adresse);
		
		// When
		final Response response = getHttpsClient().target(KUNDEN_URI)
                                                  .request()
                                                  .accept(APPLICATION_JSON)
                                                  // engl. Fehlermeldungen ohne Umlaute ;-)
                                                  .acceptLanguage(ENGLISH)
                                                  .post(json(kunde));
		
		// Then
		assertThat(response.getStatus(), is(HTTP_BAD_REQUEST));
		assertThat(response.getHeaderString("validation-exception").toLowerCase(), is("true"));
		final ViolationReport violationReport = response.readEntity(ViolationReport.class);
		response.close();
		
		final Collection<ResteasyConstraintViolation> violations = violationReport.getParameterViolations();
		assertThat(!violations.isEmpty(), is(true));
		
		ResteasyConstraintViolation violation =
				                    violations.stream()
		                                      .filter(v -> v.getMessage().equals("A lastname must have at least 2 and may only have up to 32 characters."))
		                                      .findFirst()
		                                      .get();
		assertThat(violation.getValue(), is(nachname));
		
		violation = violations.stream()
				              .filter(v -> v.getMessage().equals("A lastname must start with exactly one capital letter followed by at least one lower letter, and composed names with \"-\" are allowed."))
				              .findFirst()
				              .get();
		assertThat(violation.getValue(), is(nachname));

		violation = violations.stream()
				              .filter(v -> v.getMessage().equals("The email address " + email + " is invalid."))
				              .findFirst()
				              .get();
		assertThat(violation.getValue(), is(email));
		
		violation = violations.stream()
				              .filter(v -> v.getMessage().equals("Passwords are not equal."))
				              .findFirst()
				              .get();
		assertThat(violation.getValue().toLowerCase(), is("false"));
		
		violation = violations.stream()
				              .filter(v -> v.getMessage().equals("The terms were not accepted."))
				              .findFirst()
				              .get();
		assertThat(violation.getValue(), is(String.valueOf(agbAkzeptiert)));
		
		violation = violations.stream()
				              .filter(v -> v.getMessage().equals("The ZIP code " + plz + " doesn't have 5 digits."))
				              .findFirst()
				              .get();
		assertThat(violation.getValue(), is(plz));
		
		LOGGER.finer(ENDE);
	}

	
	@Test
	@InSequence(43)
	public void registrierePrivatkundeEmailExists() {
		LOGGER.finer(BEGINN);
		
		// Given
		final String nachname = NEUER_NACHNAME;
		final String vorname = NEUER_VORNAME;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		final Long kundeId = KUNDE_ID_VORHANDEN_MIT_BESTELLUNGEN;
		Response response = getHttpsClient(username, password)
				            .target(KUNDEN_ID_URI)
                            .resolveTemplate(KUNDEN_ID_PATH_PARAM, kundeId)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
		
		final AbstractKunde kunde = response.readEntity(AbstractKunde.class);
		kunde.setNachname(nachname);
		kunde.setVorname(vorname);
		
		// When
		response = getHttpsClient().target(KUNDEN_URI)
                                   .request()
                                   .accept(APPLICATION_JSON)
                                   // engl. Fehlermeldungen ohne Umlaute ;-)
                                   .acceptLanguage(ENGLISH)
                                   .post(json(kunde));
		
		// Then
		assertThat(response.getStatus(), is(HTTP_BAD_REQUEST));
		final String fehlermeldung = response.readEntity(String.class);
		response.close();
		assertThat(fehlermeldung, is("The email address \"" + kunde.getEmail() + "\" already exists."));
		
		LOGGER.finer(ENDE);
	}
	
	
	@Test
	@InSequence(50)
	public void updateKunde() {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long kundeId = KUNDE_ID_UPDATE;
		final String neuerNachname = NEUER_NACHNAME;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		Response response = getHttpsClient(username, password)
				            .target(KUNDEN_ID_URI)
                            .resolveTemplate(KUNDEN_ID_PATH_PARAM, kundeId)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
		AbstractKunde kunde = response.readEntity(AbstractKunde.class);
		assertThat(kunde.getId(), is(kundeId));
		final int origVersion = kunde.getVersion();
    	
    	// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuem Nachnamen bauen
		kunde.setNachname(neuerNachname);
    	
		response = getHttpsClient(username, password)
				   .target(KUNDEN_URI)
                   .request()
                   .accept(APPLICATION_JSON)
                   .put(json(kunde));
		// Then
		assertThat(response.getStatus(), is(HTTP_OK));
		kunde = response.readEntity(AbstractKunde.class);
		assertThat(kunde.getVersion() > origVersion, is(true));
		
		// Erneutes Update funktioniert, da die Versionsnr. aktualisiert ist
		response = getHttpsClient(username, password)
				   .target(KUNDEN_URI)
                   .request()
                   .put(json(kunde));
		assertThat(response.getStatus(), is(HTTP_OK));
		response.close();
		
		// Erneutes Update funktioniert NICHT, da die Versionsnr. NICHT aktualisiert ist
		response = getHttpsClient(username, password)
				   .target(KUNDEN_URI)
                   .request()
                   .put(json(kunde));
		assertThat(response.getStatus(), is(HTTP_CONFLICT));
		response.close();
		
		LOGGER.finer(ENDE);
   	}
	
	@Test
	@InSequence(51)
	public void updatePrivatkundeForm() {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long kundeId = KUNDE_ID_UPDATE;
		final String neuerNachname = NEUER_NACHNAME;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		Response response = getHttpsClient(username, password)
				            .target(KUNDEN_ID_URI)
                            .resolveTemplate(KUNDEN_ID_PATH_PARAM, kundeId)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
		Privatkunde kunde = response.readEntity(Privatkunde.class);
		assertThat(kunde.getId(), is(kundeId));
		final int origVersion = kunde.getVersion();
    	
    	// Aus den gelesenen Werten ein neues Formular-Objekt mit neuem Nachnamen bauen
		final Form form = new Form();
		form.param("id", String.valueOf(kunde.getId()))
		    .param("version", String.valueOf(kunde.getVersion()))
		    .param("nachname", neuerNachname)
		    .param("email", kunde.getEmail())
		    .param("kategorie", String.valueOf(kunde.getKategorie()))
		    .param("umsatz", String.valueOf(kunde.getUmsatz()))
		    .param("seit", NEU_SEIT_STR)
		    .param("agbAkzeptiert", String.valueOf(kunde.isAgbAkzeptiert()))
		    .param("familienstand", kunde.getFamilienstand().name())
		    .param("geschlecht", kunde.getGeschlecht().name())
		    .param("plz", kunde.getAdresse().getPlz())
		    .param("ort", kunde.getAdresse().getOrt())
		    .param("strasse", kunde.getAdresse().getStrasse())
		    .param("password", String.valueOf(kunde.getId()))
		    .param("passwordWdh", String.valueOf(kunde.getId()));
		
		if (kunde.getVorname() != null) {
		    form.param("vorname", String.valueOf(kunde.getVorname()));
		}
		if (kunde.getRabatt() != null) {
		    form.param("rabatt", String.valueOf(kunde.getRabatt()));
		}
		if (kunde.getAdresse().getHausnr() != null) {
		    form.param("hausnr", String.valueOf(kunde.getAdresse().getHausnr()));
		}
    	
		response = getHttpsClient(username, password)
				   .target(KUNDEN_PRIVAT_UPDATE_URI)
                   .request()
                   .accept(APPLICATION_JSON)
                   .put(form(form));        // POST fuer Update in einer Web-Anwendung
		// Then
		assertThat(response.getStatus(), is(HTTP_OK));
		kunde = response.readEntity(Privatkunde.class);
		assertThat(kunde.getVersion() > origVersion, is(true));
		
		// Erneutes Update funktioniert NICHT, da die Versionsnr. NICHT aktualisiert ist
		response = getHttpsClient(username, password)
				   .target(KUNDEN_PRIVAT_UPDATE_URI)
                   .request()
                   .put(form(form));
		assertThat(response.getStatus(), is(HTTP_CONFLICT));
		response.close();
		
		LOGGER.finer(ENDE);
   	}
	
	
	@Test
	@InSequence(52)
	public void updateKundeFalschesPassword() {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long kundeId = KUNDE_ID_UPDATE;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		final String passwordFalsch = PASSWORD_FALSCH;
		
		// When
		Response response = getHttpsClient(username, password)
				            .target(KUNDEN_ID_URI)
                            .resolveTemplate(KUNDEN_ID_PATH_PARAM, kundeId)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
		final AbstractKunde kunde = response.readEntity(AbstractKunde.class);
    	
		response = getHttpsClient(username, passwordFalsch)
				   .target(KUNDEN_URI)
                   .request()
                   .accept(APPLICATION_JSON)
                   .put(json(kunde));

		// Then
		assertThat(response.getStatus(), is(HTTP_UNAUTHORIZED));
		response.close();
		
		LOGGER.finer(ENDE);
	}

	@Test
	@InSequence(60)
	public void deleteKunde() {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long kundeId = KUNDE_ID_DELETE;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		Response response = getHttpsClient(username, password)
				            .target(KUNDEN_ID_URI)
                            .resolveTemplate(KUNDEN_ID_PATH_PARAM, kundeId)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
		assertThat(response.getStatus(), is(HTTP_OK));
		response.close();
		
		response = getHttpsClient(username, password)
				   .target(KUNDEN_ID_URI)
                   .resolveTemplate(KUNDEN_ID_PATH_PARAM, kundeId)
                   .request()
                   .delete();
		
		// Then
		assertThat(response.getStatus(), is(HTTP_NO_CONTENT));
		response.close();
		
		response = getHttpsClient(username, password)
				   .target(KUNDEN_ID_URI)
                   .resolveTemplate(KUNDEN_ID_PATH_PARAM, kundeId)
                   .request()
                   .accept(APPLICATION_JSON)
                   .get();
       	assertThat(response.getStatus(), is(HTTP_NOT_FOUND));
		response.close();
        
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(61)
	public void deleteKundeMitBestellung() {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long kundeId = KUNDE_ID_DELETE_MIT_BESTELLUNGEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		final Response response = getHttpsClient(username, password)
							      .target(KUNDEN_ID_URI)
                                  .resolveTemplate(KUNDEN_ID_PATH_PARAM, kundeId)
                                  .request()
                                  .acceptLanguage(GERMAN)
                                  .delete();
		
		// Then
		assertThat(response.getStatus(), is(HTTP_BAD_REQUEST));
		final String fehlermeldung = response.readEntity(String.class);
		assertThat(fehlermeldung, startsWith("Der Kunde mit ID"));
		assertThat(fehlermeldung, endsWith("Bestellung(en)."));
		
		LOGGER.finer(ENDE);
	}
	
	
	@Test
	@InSequence(62)
	public void deleteKundeFehlendeBerechtigung() {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long kundeId = KUNDE_ID_DELETE_FORBIDDEN;
		final String username = USERNAME;
		final String password = PASSWORD;
		
		// When
		final Response response = getHttpsClient(username, password)
				                  .target(KUNDEN_ID_URI)
                                  .resolveTemplate(KUNDEN_ID_PATH_PARAM, kundeId)
                                  .request()
                                  .delete();
		
		// Then
		assertThat(response.getStatus(), anyOf(is(HTTP_FORBIDDEN), is(HTTP_NOT_FOUND)));
		response.close();
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(70)
	public void uploadDownload() throws IOException {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long kundeId = KUNDE_ID_UPLOAD;
		final String path = IMAGE_PATH_UPLOAD;
		final String mimeType = IMAGE_MIMETYPE;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// Datei einlesen
		final byte[] uploadBytes = Files.readAllBytes(Paths.get(path));
		
		// When
		Response response = getHttpsClient(username, password)
				            .target(KUNDEN_IMAGE_ID_URI)
                            .resolveTemplate(KUNDEN_ID_PATH_PARAM, kundeId)
                            .request()
                            .post(entity(uploadBytes, mimeType));

		// Then
		assertThat(response.getStatus(), is(HTTP_CREATED));
		// id extrahieren aus http://localhost:8080/shop/rest/kunden/<id>/file
		final String location = response.getLocation().toString();
		response.close();
		
		final String idStr = location.replace(KUNDEN_IMAGE_URI + '/', "");
		assertThat(idStr, is(kundeId.toString()));
		
		// When (2)
		// Download der zuvor hochgeladenen Datei
		byte[] downloadBytes;
		
		response = getHttpsClient(username, password)
				   .target(KUNDEN_IMAGE_ID_URI)
                   .resolveTemplate(KUNDEN_ID_PATH_PARAM, kundeId)
                   .request()
                   .accept(mimeType)
                   .get();
		downloadBytes = response.readEntity(new GenericType<byte[]>() {});
		
		// Then (2)
		assertThat(uploadBytes.length, is(downloadBytes.length));
		assertThat(uploadBytes, is(downloadBytes));
		
		// Abspeichern des heruntergeladenen byte[] als Datei im Unterverz. target zur manuellen Inspektion
		Files.write(Paths.get(IMAGE_PATH_DOWNLOAD), downloadBytes);
		LOGGER.info("Heruntergeladene Datei abgespeichert: " + IMAGE_PATH_DOWNLOAD);
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(71)
	public void uploadInvalidMimeType() throws IOException {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long kundeId = KUNDE_ID_UPLOAD;
		final String path = IMAGE_INVALID_PATH;
		final String mimeType = IMAGE_INVALID_MIMETYPE;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// Datei einlesen
		final byte[] uploadBytes = Files.readAllBytes(Paths.get(path));
		
		// When
		final Response response = getHttpsClient(username, password)
				                  .target(KUNDEN_IMAGE_ID_URI)
                                  .resolveTemplate(KUNDEN_ID_PATH_PARAM, kundeId)
                                  .request()
                                  .post(entity(uploadBytes, mimeType));
		
		assertThat(response.getStatus(), is(HTTP_UNSUPPORTED_TYPE));
		response.close();
	}
}
