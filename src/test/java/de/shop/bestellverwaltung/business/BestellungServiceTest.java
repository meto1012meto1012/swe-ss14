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
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.business.KundeService;
import de.shop.kundenverwaltung.business.KundeService.FetchType;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.AbstractServiceTest;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.PASSWORD;
import static de.shop.util.TestConstants.USERNAME;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class BestellungServiceTest extends AbstractServiceTest {
	private static final Long ARTIKEL_1_ID = Long.valueOf(301);
	private static final int ARTIKEL_1_ANZAHL = 1;
	private static final Long ARTIKEL_2_ID = Long.valueOf(302);
	private static final int ARTIKEL_2_ANZAHL = 2;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private BestellungService bs;
	
	@Inject
	private KundeService ks;
	
	@Inject
	private ArtikelService as;
	
	@Test
	@InSequence(1)
	public void createBestellung() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	                                      SystemException, NotSupportedException {
		LOGGER.finer("createBestellung " + BEGINN);
		
		// Given
		final String username = USERNAME;
		final String password = PASSWORD;
		final Long artikel1Id = ARTIKEL_1_ID;
		final int artikel1Anzahl = ARTIKEL_1_ANZAHL;
		final Long artikel2Id = ARTIKEL_2_ID;
		final int artikel2Anzahl = ARTIKEL_2_ANZAHL;
		
		// When
		login(username, password);
		
		// Test: An der Web-Oberflaeche wird eine Bestellung in mehrerem Benutzerintaraktionen
		//       und Transaktionen komponiert
		
		Artikel artikel = as.findArtikelById(artikel1Id);

		Bestellung bestellung = new Bestellung();
		
		Bestellposition bpos = new Bestellposition(artikel, artikel1Anzahl);
		bestellung.addBestellposition(bpos);
		
		artikel = as.findArtikelById(artikel2Id);
		
		bpos = new Bestellposition(artikel, artikel2Anzahl);
		bestellung.addBestellposition(bpos);
		
		AbstractKunde kunde = ks.findKundeByUserName(username, FetchType.MIT_BESTELLUNGEN);
		
		final UserTransaction trans = getUserTransaction();
		trans.begin();
		bestellung = bs.createBestellung(bestellung, kunde);
		trans.commit();
		
		// Then
		assertThat(bestellung, notNullValue());
		
		kunde = bestellung.getKunde();
		assertThat(kunde, notNullValue());
		assertThat(bestellung.getKunde().getId(), is(kunde.getId()));
		assertThat(kunde.getBestellungen(), hasItem(bestellung));

		logout();
		LOGGER.finer("createBestellung " + ENDE);
	}
}
