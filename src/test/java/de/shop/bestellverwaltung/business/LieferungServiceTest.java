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

import de.shop.artikelverwaltung.business.ArtikelService;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.kundenverwaltung.business.KundeService;
import de.shop.util.AbstractServiceTest;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.PASSWORD_ADMIN;
import static de.shop.util.TestConstants.USERNAME_ADMIN;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class LieferungServiceTest extends AbstractServiceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final String LIEFERNR_VORHANDEN = "201402%";
	private static final String LIEFERNR_NICHT_VORHANDEN = "1888%";
	
	@Inject
	private LieferungService ls;
	@Inject
	private KundeService ks;
	@Inject
	private ArtikelService as;
	
	@Test
	@InSequence(1)
	public void findLieferungVorhanden() {
		LOGGER.finer("findLieferungVorhanden " + BEGINN);

		// Given
		final String lieferNr = LIEFERNR_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		login(username, password);
		final Collection<Lieferung> lieferungen = ls.findLieferungen(lieferNr);
		
		// Then
		assertThat(!lieferungen.isEmpty(), is(true));

		final String lieferNrPraefix = lieferNr.substring(0, lieferNr.length() - 2);  // '%' ausblenden
		lieferungen.forEach(l -> {
			assertThat(l.getLiefernr().startsWith(lieferNrPraefix), is(true));

			final Collection<Bestellung> bestellungen = l.getBestellungen();
			assertThat(!bestellungen.isEmpty(), is(true));
			bestellungen.stream()
			            .map(Bestellung::getKunde)
			            .forEach(k -> assertThat(k, notNullValue()));
		});

		logout();
		LOGGER.finer("findLieferungVorhanden " + ENDE);
	}

	@Test
	@InSequence(2)
	public void findLieferungNichtVorhanden() {
		LOGGER.finer("findLieferungNichtVorhanden " + BEGINN);

		// Given
		final String lieferNr = LIEFERNR_NICHT_VORHANDEN;
		
		// When
		thrown.expect(ConstraintViolationException.class);
		ls.findLieferungen(lieferNr);
	}
}
