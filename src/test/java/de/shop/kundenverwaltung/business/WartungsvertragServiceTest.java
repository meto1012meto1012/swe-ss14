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

package de.shop.kundenverwaltung.business;

import de.shop.kundenverwaltung.business.KundeService.FetchType;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Wartungsvertrag;
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
public class WartungsvertragServiceTest extends AbstractServiceTest {
	private static final Long KUNDE_ID_MIT_WARTUNGSVERTRAG = Long.valueOf(101);
	private static final Long KUNDE_ID_OHNE_WARTUNGSVERTRAG = Long.valueOf(104);

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private WartungsvertragService ws;

	@Inject
	private KundeService ks;

	
	@Test
	@InSequence(1)
	public void findWartungsvertraegeVorhanden() {
		LOGGER.finer("findWartungsvertraegeVorhanden " + BEGINN);

		// Given
		final Long kundeId = KUNDE_ID_MIT_WARTUNGSVERTRAG;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		assertThat(kundeId.toString(), is(username));
		
		// When
		login(username, password);
		final Collection<Wartungsvertrag> wartungsvertraege = ws.findWartungsvertraege(kundeId);
		
		// Then
		assertThat(!wartungsvertraege.isEmpty(), is(true));
		
		final AbstractKunde kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		assertThat(kunde, notNullValue());
		
		wartungsvertraege.parallelStream()
		                 .map(Wartungsvertrag::getKunde)
		                 .forEach(k -> assertThat(k, is(kunde)));
		
		logout();
		LOGGER.finer("findWartungsvertraegeVorhanden " + ENDE);
	}
	
	@Test
	@InSequence(2)
	public void findWartungsvertraegeNichtVorhanden() {
		LOGGER.finer("findWartungsvertraegeNichtVorhanden " + BEGINN);

		// Given
		final Long kundeId = KUNDE_ID_OHNE_WARTUNGSVERTRAG;
		
		// When
		thrown.expect(ConstraintViolationException.class);
		ws.findWartungsvertraege(kundeId);
	}
}
