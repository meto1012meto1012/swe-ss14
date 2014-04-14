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

package de.shop.kundenverwaltung.soap;

import de.shop.kundenverwaltung.soap.gen.AdresseVO;
import de.shop.kundenverwaltung.soap.gen.KundeSOAP;
import de.shop.kundenverwaltung.soap.gen.KundeSOAPService;
import de.shop.kundenverwaltung.soap.gen.PrivatkundeVO;
import de.shop.util.AbstractSoapTest;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.WebServiceException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class KundeSoapTest extends AbstractSoapTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final long PRIVATKUNDE_ID_VORHANDEN = 101;
	private static final long PRIVATKUNDE_ID_NICHT_VORHANDEN = 9999;
	private static final String NACHNAME_VORHANDEN = "Alpha";
	private static final String NACHNAME_NICHT_VORHANDEN = "Nichtvorhanden";
	
	private static final String NACHNAME_NEU = "Neuernachname";
	private static final String VORNAME_NEU = "Neuervorname";
	private static final String EMAIL_NEU = "neu@neu.de";
	private static final int JAHR_NEU = 2014;
	private static final int MONAT_NEU = DatatypeConstants.FEBRUARY;
	private static final int TAG_NEU = 1;
	private static final XMLGregorianCalendar SEIT_NEU;
	private static final String PLZ_NEU = "12345";
	private static final String ORT_NEU = "Testort";
	private static final String STRASSE_NEU = "Testweg";
	private static final String HAUSNR_NEU = "1";
	private static final String PASSWORD_NEU = "password";
	
	private static final String USERNAME = "102";
	private static final String PASSWORD = "102";
	private static final String PASSWORD_FALSCH = "falsch";
	private static final String USERNAME_NOT_ALLOWED = "105";
	private static final String PASSWORD_NOT_ALLOWED = "105";
	
	static {
		try {
			SEIT_NEU = DatatypeFactory.newInstance()
			                          .newXMLGregorianCalendar(JAHR_NEU, MONAT_NEU, TAG_NEU, 0, 0, 0, 0, DatatypeConstants.FIELD_UNDEFINED);
		}
		catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static KundeSOAP proxy;
	
	@BeforeClass
	public static void setupProxy() {
		proxy = new KundeSOAPService().getKundeSOAPPort();
		assertThat(proxy, notNullValue());
	}
	
	@Test   // langer Timeout wg. 1. Test
	@InSequence(1)
	public void getVersion() {
		LOGGER.finer(BEGINN);
		
		// Given
		final String username = USERNAME;
		final String password = PASSWORD;
		
		// When
		login(username, password);
		final String version =  proxy.getVersion();
		
		// Then
		assertThat(version, is("1.0"));
		
		LOGGER.finer(ENDE);
	}

	@Test
	@InSequence(10)
	public void findPrivatkundeById() {
		LOGGER.finer(BEGINN);
		
		// Given
		final long kundeId = PRIVATKUNDE_ID_VORHANDEN;
		final String username = USERNAME;
		final String password = PASSWORD;
		
		// When
		login(username, password);
		final PrivatkundeVO privatkunde = proxy.findPrivatkundeById(kundeId);
		
		// Then
		assertThat(privatkunde.getId(), is(kundeId));
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(11)
	public void findPrivatkundeByIdNichtVorhanden() {
		LOGGER.finer(BEGINN);
		
		// Given
		final long kundeId = PRIVATKUNDE_ID_NICHT_VORHANDEN;
		final String username = USERNAME;
		final String password = PASSWORD;
		
		// When
		login(username, password);
		final PrivatkundeVO privatkunde = proxy.findPrivatkundeById(kundeId);
		
		// Then
		assertThat(privatkunde, nullValue());
		
		LOGGER.finer(ENDE);
	}
	
	@Test
	@InSequence(12)
	public void findPrivatkundeByIdFalschesPassword() {
		LOGGER.finer(BEGINN);
		
		// Given
		final long kundeId = PRIVATKUNDE_ID_VORHANDEN;
		final String username = USERNAME;
		final String password = PASSWORD_FALSCH;
		
		// When
		login(username, password);
		try {
			proxy.findPrivatkundeById(kundeId);
			fail("WebServiceException wurde nicht geworfen");
		}
		catch (WebServiceException e) {
			assertThat(e.getMessage(), containsString("401"));
			assertThat(e.getMessage(), containsString("Unauthorized"));
		}
		
		LOGGER.finer(ENDE);
	}

	@Test
	@InSequence(13)
	public void findPrivatkundeByIdNotAllowed() {
		LOGGER.finer(BEGINN);
		
		// Given
		final long kundeId = PRIVATKUNDE_ID_VORHANDEN;
		final String username = USERNAME_NOT_ALLOWED;
		final String password = PASSWORD_NOT_ALLOWED;
		
		// When
		login(username, password);
		try {
			proxy.findPrivatkundeById(kundeId);
			fail("WebServiceException wurde nicht geworfen");
		}
		catch (WebServiceException e) {
			assertThat(e.getMessage(), containsString("403"));
			assertThat(e.getMessage(), containsString("Forbidden"));
		}
		
		LOGGER.finer(ENDE);
	}

	@Test
	@InSequence(20)
	public void findKundenByNachname() {
		LOGGER.finer(BEGINN);
		
		// Given
		final String nachname = NACHNAME_VORHANDEN;
		final String username = USERNAME;
		final String password = PASSWORD;
		
		// When
		login(username, password);
		final List<PrivatkundeVO> privatkunden = proxy.findPrivatkundenByNachname(nachname);
		
		// Then
		assertThat(!privatkunden.isEmpty(), is(true));
		privatkunden.parallelStream()
		            .map(PrivatkundeVO::getNachname)
		            .forEach(n -> assertThat(n, is(nachname)));
	}
	
	@Test
	@InSequence(21)
	public void findKundenByNachnameNichtVorhanden() {
		LOGGER.finer(BEGINN);
		
		// Given
		final String nachname = NACHNAME_NICHT_VORHANDEN;
		final String username = USERNAME;
		final String password = PASSWORD;
		
		// When
		login(username, password);
		final List<PrivatkundeVO> privatkunden = proxy.findPrivatkundenByNachname(nachname);
		
		// Then
		assertThat(privatkunden.isEmpty(), is(true));
		
		LOGGER.finer(ENDE);
	}

	@Test
	@InSequence(30)
	public void createPrivatkunde() {
		LOGGER.finer(BEGINN);
		
		// Given
		final String nachname = NACHNAME_NEU;
		final String vorname = VORNAME_NEU;
		final String email = EMAIL_NEU;
		final XMLGregorianCalendar seit = SEIT_NEU;
		final String password = PASSWORD_NEU;

		final PrivatkundeVO privatkunde = new PrivatkundeVO();
		privatkunde.setNachname(nachname);
		privatkunde.setVorname(vorname);
		privatkunde.setEmail(email);
		privatkunde.setSeit(seit);
		privatkunde.setAgbAkzeptiert(true);
		privatkunde.setPassword(password);
		
		final String plz = PLZ_NEU;
		final String ort = ORT_NEU;
		final String strasse = STRASSE_NEU;
		final String hausnr = HAUSNR_NEU;
		
		final AdresseVO adresse = new AdresseVO();
		adresse.setPlz(plz);
		adresse.setOrt(ort);
		adresse.setStrasse(strasse);
		adresse.setHausnr(hausnr);
		privatkunde.setAdresse(adresse);
		
		// When
		final Long id = proxy.createPrivatkunde(privatkunde);
		
		// Then
		assertThat(id > 0, is(true));
		
		LOGGER.finer(ENDE);
	}
	
	private void login(String username, String password) {
		login(username, password, proxy);
	}
}
