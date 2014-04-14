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

package de.shop.artikelverwaltung.rest;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.util.AbstractResourceTest;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.logging.Logger;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.artikelverwaltung.rest.ArtikelResource.ARTIKEL_ID_PATH_PARAM;
import static de.shop.util.TestConstants.ARTIKEL_ID_URI;
import static de.shop.util.TestConstants.ARTIKEL_URI;
import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.PASSWORD_ADMIN;
import static de.shop.util.TestConstants.USERNAME_ADMIN;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class ArtikelResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final Long ARTIKEL_ID_VORHANDEN = Long.valueOf(301);
	
	private static final String BEZEICHNUNG_NEU = "Bezeichnung neuer Artikel";
	private static final BigDecimal PREIS_NEU = new BigDecimal("100.11");
	private static final String BEZEICHNUNG_NEU_FORM = "Bezeichnung neuer Artikel (Form)";
	
	private static final Long ARTIKEL_ID_UPDATE = Long.valueOf(302);
	private static final BigDecimal PREIS_INCR = new BigDecimal("1.11");
	
	@Test
	@InSequence(1)
	public void findArtikelById() {
		LOGGER.finer(BEGINN);
		
		// Given
		final Long id = ARTIKEL_ID_VORHANDEN;
		
		// When
		final Response response = getHttpsClient()
				                  .target(ARTIKEL_ID_URI)
				                  .resolveTemplate(ARTIKEL_ID_PATH_PARAM, id)
				                  .request()
                                  .accept(APPLICATION_JSON)
                                  .get();
		
		// Then
		assertThat(response.getStatus(), is(HTTP_OK));
		final Artikel artikel = response.readEntity(Artikel.class);
		assertThat(artikel.getId(), is(id));
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(10)
	public void createArtikel() {
		LOGGER.finer(BEGINN);
		
		// Given
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		final String bezeichnung = BEZEICHNUNG_NEU;
		final BigDecimal preis = PREIS_NEU;
		Artikel artikel = new Artikel(bezeichnung, preis);
		
		// When
		Response response = getHttpsClient(username, password)
				            .target(ARTIKEL_URI)
				            .request()
				            .post(json(artikel));
		
		// Then
		assertThat(response.getStatus(), is(HTTP_CREATED));
		final String location = response.getLocation().toString();
		response.close();
		
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		final Long id = Long.valueOf(idStr);
		assertThat(id > 0, is(true));
		
		response = getHttpsClient()
				   .target(ARTIKEL_ID_URI)
				   .resolveTemplate(ARTIKEL_ID_PATH_PARAM, id)
				   .request()
                   .accept(APPLICATION_JSON)
                   .get();
		artikel = response.readEntity(Artikel.class);
		assertThat(artikel.getId(), is(id));
		assertThat(artikel.getBezeichnung(), is(bezeichnung));
		assertThat(artikel.getPreis(), is(preis));
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(11)
	public void createArtikelForm() {
		LOGGER.finer(BEGINN);
		
		// Given
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		final String bezeichnung = BEZEICHNUNG_NEU_FORM;
		final BigDecimal preis = PREIS_NEU;
		
		final Form form = new Form();
		form.param("bezeichnung", bezeichnung)
			.param("preis", String.valueOf(preis));
		
		// When
		Response response = getHttpsClient(username, password)
				            .target(ARTIKEL_URI)
				            .request()
				            .post(form(form));
		
		// Then
		assertThat(response.getStatus(), is(HTTP_CREATED));
		final String location = response.getLocation().toString();
		response.close();
		
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		final Long id = Long.valueOf(idStr);
		assertThat(id > 0, is(true));
		
		response = getHttpsClient()
				   .target(ARTIKEL_ID_URI)
				   .resolveTemplate(ARTIKEL_ID_PATH_PARAM, id)
				   .request()
                   .accept(APPLICATION_JSON)
                   .get();
		final Artikel artikel = response.readEntity(Artikel.class);
		assertThat(artikel.getId(), is(id));
		assertThat(artikel.getBezeichnung(), is(bezeichnung));
		assertThat(artikel.getPreis(), is(preis));
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(20)
	public void updateArtikel() {
		LOGGER.finer(BEGINN);
		
		// Given
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		final Long id = ARTIKEL_ID_UPDATE;
		final BigDecimal preisIncr = PREIS_INCR;
		
		Response response = getHttpsClient()
				            .target(ARTIKEL_ID_URI)
				            .resolveTemplate(ARTIKEL_ID_PATH_PARAM, id)
				            .request()
                            .accept(APPLICATION_JSON)
                            .get();
		Artikel artikel = response.readEntity(Artikel.class);
		assertThat(artikel.getId(), is(id));
		
		// When
		final BigDecimal preisAlt = artikel.getPreis();
		final BigDecimal preisNeu = preisAlt.add(preisIncr);
		artikel.setPreis(preisNeu);
		response = getHttpsClient(username, password)
				   .target(ARTIKEL_URI)
				   .request()
                   .put(json(artikel));
		
		// Then
		assertThat(response.getStatus(), is(HTTP_OK));
		artikel = response.readEntity(Artikel.class);
		assertThat(artikel.getId(), is(id));
		assertThat(artikel.getPreis(), is(preisNeu));
		
		LOGGER.finer(ENDE);		
	}
	
	@Test
	@InSequence(21)
	public void updateArtikelForm() {
		LOGGER.finer(BEGINN);
		
		// Given
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		final Long id = ARTIKEL_ID_UPDATE;
		final BigDecimal preisIncr = PREIS_INCR;
		
		Response response = getHttpsClient()
				            .target(ARTIKEL_ID_URI)
				            .resolveTemplate(ARTIKEL_ID_PATH_PARAM, id)
				            .request()
                            .accept(APPLICATION_JSON)
                            .get();
		Artikel artikel = response.readEntity(Artikel.class);
		assertThat(artikel.getId(), is(id));
		
		// When
		// Aus den gelesenen Werten ein neues Formular-Objekt mit neuem Nachnamen bauen
		final BigDecimal preisAlt = artikel.getPreis();
		final BigDecimal preisNeu = preisAlt.add(preisIncr);
		final Form form = new Form();
		form.param("id", String.valueOf(artikel.getId()))
		    .param("version", String.valueOf(artikel.getVersion()))
			.param("bezeichnung", artikel.getBezeichnung())
			.param("preis", String.valueOf(preisNeu))
			.param("ausgesondert", String.valueOf(artikel.isAusgesondert()));
		
		response = getHttpsClient(username, password)
				   .target(ARTIKEL_URI)
				   .request()
				   .put(form(form));
		
		// Then
		assertThat(response.getStatus(), is(HTTP_OK));
		response = getHttpsClient()
				            .target(ARTIKEL_ID_URI)
				            .resolveTemplate(ARTIKEL_ID_PATH_PARAM, id)
				            .request()
                            .accept(APPLICATION_JSON)
                            .get();
		assertThat(response.getStatus(), is(HTTP_OK));
		artikel = response.readEntity(Artikel.class);
		assertThat(artikel.getId(), is(id));
		assertThat(artikel.getPreis(), is(preisNeu));
	}
}
