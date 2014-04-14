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

package de.shop.bestellverwaltung.rest;

import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.rest.KundeResource;
import de.shop.util.AbstractResourceTest;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.util.TestConstants.ARTIKEL_URI;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.BESTELLUNGEN_ID_KUNDE_URI;
import static de.shop.util.TestConstants.BESTELLUNGEN_ID_PATH_PARAM;
import static de.shop.util.TestConstants.BESTELLUNGEN_ID_URI;
import static de.shop.util.TestConstants.BESTELLUNGEN_URI;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.KUNDEN_ID_URI;
import static de.shop.util.TestConstants.PASSWORD;
import static de.shop.util.TestConstants.PASSWORD_ADMIN;
import static de.shop.util.TestConstants.PASSWORD_KUNDE;
import static de.shop.util.TestConstants.USERNAME;
import static de.shop.util.TestConstants.USERNAME_ADMIN;
import static de.shop.util.TestConstants.USERNAME_KUNDE;
import static de.shop.util.TestConstants.WARENKORBPOSITION_URI;
import static java.math.BigDecimal.ZERO;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_OK;
import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class BestellungResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final Long BESTELLUNG_ID_VORHANDEN = Long.valueOf(401);
	private static final Long BESTELLUNG_ID_FORBIDDEN = Long.valueOf(401);
	private static final Long ARTIKEL_ID_VORHANDEN_1 = Long.valueOf(301);
	private static final Long ARTIKEL_ID_VORHANDEN_2 = Long.valueOf(302);
	private static final String ARTIKEL_ID_INVALID = "INVALID";
	
	@Test
	@InSequence(1)
	public void findBestellungById() {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long bestellungId = BESTELLUNG_ID_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		final Response response = getHttpsClient(username, password)
				                  .target(BESTELLUNGEN_ID_URI)
                                  .resolveTemplate(BESTELLUNGEN_ID_PATH_PARAM, bestellungId)
                                  .request()
                                  .accept(APPLICATION_JSON)
                                  .get();
		
		// Then
		assertThat(response.getStatus(), is(HTTP_OK));
		final Bestellung bestellung = response.readEntity(Bestellung.class);
		
		assertThat(bestellung.getId(), is(bestellungId));
		assertThat(!bestellung.getBestellpositionen().isEmpty(), is(true));

		LOGGER.finer(ENDE);
	}

	@Test
	@InSequence(2)
	public void findBestellungByIdForbidden() {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long bestellungId = BESTELLUNG_ID_FORBIDDEN;
		final String username = USERNAME_KUNDE;
		final String password = PASSWORD_KUNDE;
		
		// When
		final Response response = getHttpsClient(username, password)
				                  .target(BESTELLUNGEN_ID_URI)
                                  .resolveTemplate(BESTELLUNGEN_ID_PATH_PARAM, bestellungId)
                                  .request()
                                  .accept(APPLICATION_JSON)
                                  .get();
		
		// Then
		assertThat(response.getStatus(), is(HTTP_FORBIDDEN));
		response.close();

		LOGGER.finer(ENDE);
	}

	@Test
	@InSequence(3)
	public void findKundeByBestellungId() {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long bestellungId = BESTELLUNG_ID_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		Response response = getHttpsClient(username, password)
				            .target(BESTELLUNGEN_ID_KUNDE_URI)
                            .resolveTemplate(BESTELLUNGEN_ID_PATH_PARAM, bestellungId)
                            .request()
                            .accept(APPLICATION_JSON)
                            .get();
			
		// Then
		assertThat(response.getStatus(), is(HTTP_OK));
		final AbstractKunde kunde = response.readEntity(AbstractKunde.class);
		assertThat(kunde, notNullValue());
		
		response = getHttpsClient(username, password)
				   .target(KUNDEN_ID_URI)
                   .resolveTemplate(KundeResource.KUNDEN_ID_PATH_PARAM, kunde.getId())
                   .request()
                   .accept(APPLICATION_JSON)
                   .get();
		assertThat(response.getStatus(), is(HTTP_OK));
		assertThat(!response.getLinks().isEmpty(), is(true));
		response.close();    // response.readEntity() wurde nicht aufgerufen

		LOGGER.finer(ENDE);
	}

	@Test
	@InSequence(4)
	public void findKundeByBestellungForbidden() {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long bestellungId = BESTELLUNG_ID_FORBIDDEN;
		final String username = USERNAME_KUNDE;
		final String password = PASSWORD_KUNDE;
		
		// When
		final Response response = getHttpsClient(username, password)
				                  .target(BESTELLUNGEN_ID_KUNDE_URI)
                                  .resolveTemplate(BESTELLUNGEN_ID_PATH_PARAM, bestellungId)
                                  .request()
                                  .accept(APPLICATION_JSON)
                                  .get();
			
		// Then
		assertThat(response.getStatus(), is(HTTP_FORBIDDEN));
		response.close();    // response.readEntity() wurde nicht aufgerufen

		LOGGER.finer(ENDE);
	}

	@Test
	@InSequence(10)
	public void bestellen() throws URISyntaxException {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long artikelId1 = ARTIKEL_ID_VORHANDEN_1;
		final Long artikelId2 = ARTIKEL_ID_VORHANDEN_2;
		final String username = USERNAME;
		final String password = PASSWORD;
		
		// Neues, client-seitiges Bestellungsobjekt als JSON-Datensatz
		final Bestellung bestellung = new Bestellung();
		
		Bestellposition bp = new Bestellposition();
		bp.setArtikelUri(new URI(ARTIKEL_URI + "/" + artikelId1));
		bp.setAnzahl(1);
		bestellung.addBestellposition(bp);

		bp = new Bestellposition();
		bp.setArtikelUri(new URI(ARTIKEL_URI + "/" + artikelId2));
		bp.setAnzahl(1);
		bestellung.addBestellposition(bp);
		bestellung.setGesamtbetrag(ZERO);   // Dummy, damit @NotNull nicht verletzt wird
		
		// When
		Long id;
		Response response = getHttpsClient(username, password)
				            .target(BESTELLUNGEN_URI)
                            .request()
                            .post(json(bestellung));
			
		// Then
		assertThat(response.getStatus(), is(HTTP_CREATED));
		final String location = response.getLocation().toString();
		response.close();
			
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		id = Long.valueOf(idStr);
		assertThat(id > 0, is(true));
		
		// Gibt es die neue Bestellung?
		response = getHttpsClient(username, password)
				   .target(BESTELLUNGEN_ID_URI)
                   .resolveTemplate(BESTELLUNGEN_ID_PATH_PARAM, id)
                   .request()
                   .accept(APPLICATION_JSON)
                   .get();
		assertThat(response.getStatus(), is(HTTP_OK));
		response.close();
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(11)
	public void bestellenArtikelIdInvalid() throws URISyntaxException {
		LOGGER.finer(BEGINN);
		
		// Given
		final String artikelId = ARTIKEL_ID_INVALID;
		final String username = USERNAME;
		final String password = PASSWORD;

		// Neues, client-seitiges Bestellungsobjekt als JSON-Datensatz
		final Bestellung bestellung = new Bestellung();
		
		final Bestellposition bp = new Bestellposition();
		bp.setArtikelUri(new URI(ARTIKEL_URI + "/" + artikelId));
		bp.setAnzahl(1);
		bestellung.addBestellposition(bp);
		bestellung.setGesamtbetrag(ZERO);   // Dummy, damit @NotNull nicht verletzt wird

		// When
		final Response response = getHttpsClient(username, password)
				                  .target(BESTELLUNGEN_URI)
                                  .request()
                                  .post(json(bestellung));
			
		// Then
		assertThat(response.getStatus(), is(HTTP_BAD_REQUEST));
		final String fehlermeldung = response.readEntity(String.class);
    	assertThat(fehlermeldung, startsWith("Keine "));
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(20)
	public void bestellenWarenkorb() throws URISyntaxException {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long artikelId1 = ARTIKEL_ID_VORHANDEN_1;
		final Long artikelId2 = ARTIKEL_ID_VORHANDEN_2;
		final String username = USERNAME;
		final String password = PASSWORD;
		
		// Neues, client-seitiges Objekt fuer Warenkorbposition als Formular-Datensatz
		final Form form1 = new Form();
		form1.param("artikelUri", ARTIKEL_URI + "/" + artikelId1)
		     .param("anzahl", "1");
		final Form form2 = new Form();
		form2.param("artikelUri", ARTIKEL_URI + "/" + artikelId2)
		     .param("anzahl", "2");
		
		// When
		Response response = getHttpsClient(username, password)
				            .target(WARENKORBPOSITION_URI)
                            .request()
                            .post(form(form1));
		assertThat(response.getStatus(), is(HTTP_CREATED));
		response.close();
		response = getHttpsClient(username, password)
				   .target(WARENKORBPOSITION_URI)
                   .request()
                   .post(form(form2));
		assertThat(response.getStatus(), is(HTTP_CREATED));
		response.close();
		response = getHttpsClient(username, password)
				   .target(BESTELLUNGEN_URI)
                   .request()
                   .post(null);

		// Then
		assertThat(response.getStatus(), is(HTTP_CREATED));
		final String location = response.getLocation().toString();
		response.close();
			
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		final Long id = Long.valueOf(idStr);
		assertThat(id > 0, is(true));
		
		// Gibt es die neue Bestellung?
		response = getHttpsClient(username, password)
				   .target(BESTELLUNGEN_ID_URI)
                   .resolveTemplate(BESTELLUNGEN_ID_PATH_PARAM, id)
                   .request()
                   .accept(APPLICATION_JSON)
                   .get();
		assertThat(response.getStatus(), is(HTTP_OK));
		response.close();
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(21)
	public void bestellenWarenkorbInvalidArtikel() throws URISyntaxException {
		LOGGER.finer(BEGINN);
		
		// Given
		final String artikelId = ARTIKEL_ID_INVALID;
		final String username = USERNAME;
		final String password = PASSWORD;
		
		// Neues, client-seitiges Objekt fuer Warenkorbposition als Formular-Datensatz
		final Form form = new Form();
		form.param("artikelUri", ARTIKEL_URI + "/" + artikelId)
		    .param("anzahl", "1");
		
		// When
		final Response response = getHttpsClient(username, password)
				                  .target(WARENKORBPOSITION_URI)
                                  .request()
                                  .post(form(form));
		
		// Then
		assertThat(response.getStatus(), is(HTTP_BAD_REQUEST));
		response.close();
	}

	@Test
	@InSequence(22)
	public void bestellenWarenkorbLeer() throws URISyntaxException {
		LOGGER.finer(BEGINN);
		
		// Given
		final String username = USERNAME;
		final String password = PASSWORD;

		// When
		final Response response = getHttpsClient(username, password)
				                  .target(BESTELLUNGEN_URI)
                                  .request()
                                  .post(null);
		
		// Then
		assertThat(response.getStatus(), is(HTTP_BAD_REQUEST));
		response.close();
	}
}
