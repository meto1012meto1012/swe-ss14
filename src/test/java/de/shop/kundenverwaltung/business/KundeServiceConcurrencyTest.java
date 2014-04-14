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
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.util.AbstractServiceTest;
import de.shop.util.persistence.ConcurrentDeletedException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolationException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.PASSWORD_ADMIN;
import static de.shop.util.TestConstants.USERNAME_ADMIN;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class KundeServiceConcurrencyTest extends AbstractServiceTest {
	private static final long TIMEOUT = 5;  // Sekunden
	
	private static final String NACHNAME_NEU_UPDATEUPDATE = "Updateupdate";
	private static final String NACHNAME_NEU_UPDATEDELETE = "Updatedelete";
	private static final String NACHNAME_NEU_DELETEUPDATE = "Deleteupdate";
	private static final String VORNAME_NEU = "Vorname";
	private static final String EMAIL_NEU_UPDATEUPDATE = "Updateupdate@Updateupdate.de";
	private static final String EMAIL_NEU_UPDATEDELETE = "Updatedelete@Updatedelete.de";
	private static final String EMAIL_NEU_DELETEUPDATE = "Deleteupdate@Deleteupdate.de";
	private static final int KATEGORIE_NEU = 1;
	private static final BigDecimal RABATT_NEU = new BigDecimal("0.15");
	private static final BigDecimal UMSATZ_NEU = new BigDecimal("10000000");
	private static final int TAG = 1;
	private static final int MONAT = Calendar.FEBRUARY;
	private static final int JAHR = 2014;
	private static final Date SEIT_NEU = new GregorianCalendar(JAHR, MONAT, TAG).getTime();
	private static final String PLZ_NEU = "76133";
	private static final String ORT_NEU = "Karlsruhe";
	private static final String STRASSE_NEU = "Moltkestra\u00DFe";
	private static final String HAUSNR_NEU = "40";
	private static final boolean AGB_AKZEPTIERT = true;
	private static final String PASSWORD_NEU = "password";
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private KundeService ks;
	
	@Inject
	private KundeServiceConcurrencyHelper concurrencyHelper;

	@Inject
	private EntityManager em;


	@Test
	@InSequence(1)
	public void updateUpdateKunde() throws InterruptedException, 
	            RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException,
	            ExecutionException, NotSupportedException, TimeoutException  {
		LOGGER.finer("updateUpdateKunde " + BEGINN);

		// Given
		final String nachname = NACHNAME_NEU_UPDATEUPDATE;
		final String vorname = VORNAME_NEU;
		final String vornameSuffixNeu = "updated";
		final String email = EMAIL_NEU_UPDATEUPDATE;
		final int kategorie = KATEGORIE_NEU;
		final BigDecimal rabatt = RABATT_NEU;
		final BigDecimal umsatz = UMSATZ_NEU;
		final Date seit = SEIT_NEU;
		final String plz = PLZ_NEU;
		final String ort = ORT_NEU;
		final String strasse = STRASSE_NEU;
		final String hausnr = HAUSNR_NEU;
		final boolean agbAKzeptiert = AGB_AKZEPTIERT;
		final String password = PASSWORD_NEU;
		final String usernameLogin = USERNAME_ADMIN;
		final String passwordLogin = PASSWORD_ADMIN;
		
		// When
		final AbstractKunde tmpKunde = new Privatkunde();
		tmpKunde.setNachname(nachname);
		tmpKunde.setVorname(vorname);
		tmpKunde.setEmail(email);	
		tmpKunde.setKategorie(kategorie);
		tmpKunde.setRabatt(rabatt);
		tmpKunde.setUmsatz(umsatz);
		tmpKunde.setSeit(seit);
		final Adresse adresse = new Adresse(plz, ort, strasse, hausnr);
		tmpKunde.setAdresse(adresse);
		adresse.setKunde(tmpKunde);
		tmpKunde.setAgbAkzeptiert(agbAKzeptiert);
		tmpKunde.setPassword(password);
		tmpKunde.setPasswordWdh(password);
		
		final UserTransaction trans = getUserTransaction();
		trans.begin();
		final AbstractKunde neuerKunde = ks.createKunde(tmpKunde);
		trans.commit();
		
		assertThat(neuerKunde.getId().longValue() > 0, is(true));
		
		final Future<AbstractKunde> future = concurrencyHelper.update(neuerKunde.getId());
		// Warten bis der "parallele" Thread fertig ist
		final AbstractKunde aktualisierterKunde = future.get(TIMEOUT, SECONDS);
		assertThat(aktualisierterKunde.getVersion() > neuerKunde.getVersion(), is(true));
		
		neuerKunde.setVorname(neuerKunde.getVorname() + vornameSuffixNeu);
		
		login(usernameLogin, passwordLogin);
		trans.begin();
		try {
			ks.updateKunde(neuerKunde, false);
			fail("ConcurrentUpdatedException wurde nicht geworfen");
		}
		catch (OptimisticLockException e) {
			assertThat(e.getMessage(), containsString(neuerKunde.getId().toString()));
			final Long id = (Long) em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(neuerKunde);
			assertThat(id, is(neuerKunde.getId()));
			assertThat(e.getEntity(), is((Object) neuerKunde));
		}

		// Then
		trans.rollback();

		trans.begin();
		ks.deleteKundeById(neuerKunde.getId());
		trans.commit();
		
		logout();
		LOGGER.finer("updateUpdateKunde " + ENDE);
	}
	
	@Test
	@InSequence(2)
	public void updateDeleteKunde() throws InterruptedException, RollbackException, HeuristicMixedException,
	                                       HeuristicRollbackException, SystemException, ExecutionException,
	                                       NotSupportedException, TimeoutException {
		LOGGER.finer("updateDeleteKunde " + BEGINN);

		// Given
		final String nachname = NACHNAME_NEU_UPDATEDELETE;
		final String vorname = VORNAME_NEU;
		final String vornameSuffixNeu = "updated";
		final String email = EMAIL_NEU_UPDATEDELETE;
		final int kategorie = KATEGORIE_NEU;
		final BigDecimal rabatt = RABATT_NEU;
		final BigDecimal umsatz = UMSATZ_NEU;
		final Date seit = SEIT_NEU;
		final String plz = PLZ_NEU;
		final String ort = ORT_NEU;
		final String strasse = STRASSE_NEU;
		final String hausnr = HAUSNR_NEU;
		final boolean agbAKzeptiert = AGB_AKZEPTIERT;
		final String password = PASSWORD_NEU;
		final String usernameLogin = USERNAME_ADMIN;
		final String passwordLogin = PASSWORD_ADMIN;
		
		// When
		final AbstractKunde tmpKunde = new Privatkunde();
		tmpKunde.setNachname(nachname);
		tmpKunde.setVorname(vorname);
		tmpKunde.setEmail(email);
		tmpKunde.setKategorie(kategorie);
		tmpKunde.setRabatt(rabatt);
		tmpKunde.setUmsatz(umsatz);
		tmpKunde.setSeit(seit);
		final Adresse adresse = new Adresse(plz, ort, strasse, hausnr);
		tmpKunde.setAdresse(adresse);
		adresse.setKunde(tmpKunde);
		tmpKunde.setAgbAkzeptiert(agbAKzeptiert);
		tmpKunde.setPassword(password);
		tmpKunde.setPasswordWdh(password);
		
		final UserTransaction trans = getUserTransaction();
		trans.begin();
		final AbstractKunde neuerKunde = ks.createKunde(tmpKunde);
		trans.commit();

		assertThat(neuerKunde.getId().longValue() > 0, is(true));
		
		final Future<?> future = concurrencyHelper.delete(neuerKunde.getId());
		future.get(TIMEOUT, SECONDS);   // Warten bis der "parallele" Thread fertig ist
		
		neuerKunde.setVorname(neuerKunde.getVorname() + vornameSuffixNeu);
		
		// Then
		login(usernameLogin, passwordLogin);
		trans.begin();
		thrown.expect(ConcurrentDeletedException.class);
		ks.updateKunde(neuerKunde, false);
		trans.rollback();

		LOGGER.finer("updateDeleteKunde " + ENDE);
	}

	@Test
	@InSequence(3)
	public void deleteUpdateKunde() throws InterruptedException, RollbackException, HeuristicMixedException,
	                                       HeuristicRollbackException, SystemException, ExecutionException,
	                                       NotSupportedException, TimeoutException {
		LOGGER.finer("deleteUpdateKunde " + BEGINN);

		// Given
		final String nachname = NACHNAME_NEU_DELETEUPDATE;
		final String vorname = VORNAME_NEU;
		final String email = EMAIL_NEU_DELETEUPDATE;
		final int kategorie = KATEGORIE_NEU;
		final BigDecimal rabatt = RABATT_NEU;
		final BigDecimal umsatz = UMSATZ_NEU;
		final Date seit = SEIT_NEU;
		final String plz = PLZ_NEU;
		final String ort = ORT_NEU;
		final String strasse = STRASSE_NEU;
		final String hausnr = HAUSNR_NEU;
		final boolean agbAKzeptiert = AGB_AKZEPTIERT;
		final String password = PASSWORD_NEU;
		final String usernameLogin = USERNAME_ADMIN;
		final String passwordLogin = PASSWORD_ADMIN;
		
		// When
		final AbstractKunde tmpKunde = new Privatkunde();
		tmpKunde.setNachname(nachname);
		tmpKunde.setVorname(vorname);
		tmpKunde.setEmail(email);
		tmpKunde.setKategorie(kategorie);
		tmpKunde.setRabatt(rabatt);
		tmpKunde.setUmsatz(umsatz);
		tmpKunde.setSeit(seit);
		final Adresse adresse = new Adresse(plz, ort, strasse, hausnr);
		tmpKunde.setAdresse(adresse);
		adresse.setKunde(tmpKunde);
		tmpKunde.setAgbAkzeptiert(agbAKzeptiert);
		tmpKunde.setPassword(password);
		tmpKunde.setPasswordWdh(password);
		
		final UserTransaction trans = getUserTransaction();
		trans.begin();
		final AbstractKunde neuerKunde = ks.createKunde(tmpKunde);
		trans.commit();
		assertThat(neuerKunde.getId().longValue() > 0 , is(true));

		final Future<AbstractKunde> future = concurrencyHelper.update(neuerKunde.getId());
		// Warten bis der "parallele" Thread fertig ist
		final AbstractKunde aktualisierterKunde = future.get(TIMEOUT, SECONDS);
		assertThat(aktualisierterKunde.getVersion() > neuerKunde.getVersion(), is(true));
		
		login(usernameLogin, passwordLogin);
		trans.begin();
		ks.deleteKunde(neuerKunde);
		trans.commit();

		// Then
		trans.begin();
		thrown.expect(ConstraintViolationException.class);
		ks.findKundeById(neuerKunde.getId(), FetchType.NUR_KUNDE);
		trans.rollback();
		logout();
	}
}
