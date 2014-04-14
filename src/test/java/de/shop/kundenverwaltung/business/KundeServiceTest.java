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

import com.google.common.collect.Sets;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.business.KundeService.FetchType;
import de.shop.kundenverwaltung.business.KundeService.OrderByType;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.FamilienstandType;
import de.shop.kundenverwaltung.domain.HobbyType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.util.AbstractServiceTest;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

import static de.shop.util.TestConstants.BEGINN;
import static de.shop.util.TestConstants.ENDE;
import static de.shop.util.TestConstants.PASSWORD_ADMIN;
import static de.shop.util.TestConstants.USERNAME_ADMIN;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@RunWith(Arquillian.class)
public class KundeServiceTest extends AbstractServiceTest {
	private static final String KUNDE_NACHNAME_VORHANDEN = "Alpha";
	private static final String KUNDE_NACHNAME_NICHT_VORHANDEN = "Beta";
	private static final String KUNDE_NACHNAME_UNGUELTIG = "?!";
	private static final Long PRIVATKUNDE_ID_VORHANDEN = Long.valueOf(101);
	private static final Long KUNDE_ID_MIT_BESTELLUNGEN = Long.valueOf(102);
	private static final Long KUNDE_ID_OHNE_BESTELLUNGEN = Long.valueOf(103);
	private static final String KUNDE_EMAIL_VORHANDEN = "rainer.neumann@hs-karlsruhe.de";
	private static final String PLZ_VORHANDEN = "76133";
	private static final String PLZ_UNGUELTIG = "111";
	private static final String KUNDE_NEU_NACHNAME = "Alphaneu";
	private static final String KUNDE_NEU_EMAIL = KUNDE_NEU_NACHNAME + "@" + KUNDE_NEU_NACHNAME + ".de";
	private static final int KATEGORIE_NEU = 1;
	private static final BigDecimal RABATT_NEU = new BigDecimal("0.15");
	private static final BigDecimal UMSATZ_NEU = new BigDecimal("10000000");
	private static final int TAG = 1;
	private static final int MONAT = Calendar.FEBRUARY;
	private static final int JAHR = 2014;
	private static final Date SEIT_NEU = new GregorianCalendar(JAHR, MONAT, TAG).getTime();
	private static final boolean NEWSLETTER_NEU = true;
	private static final FamilienstandType FAMILIENSTAND_NEU = FamilienstandType.VERHEIRATET;
	private static final Set<HobbyType> HOBBIES_NEU = Sets.newHashSet(HobbyType.LESEN, HobbyType.REISEN);
	private static final boolean AGB_AKZEPTIERT_NEU = true;
	private static final String PASSWORD_NEU = "password";
	private static final String PLZ_NEU = "76133";
	private static final String ORT_NEU = "Karlsruhe";
	private static final String STRASSE_NEU = "Moltkestra\u00DFe";
	private static final String HAUSNR_NEU = "40";

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private KundeService ks;


	@Test
	@InSequence(1)
	public void findKundeMitEmailVorhanden() {
		LOGGER.finer("findKundenMitEmailVorhanden " + BEGINN);
		
		// Given
		final String email = KUNDE_EMAIL_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		login(username, password);
		final AbstractKunde kunde = ks.findKundeByEmail(email);
		
		// Then
		assertThat(kunde.getEmail(), is(email));
		logout();
		LOGGER.finer("findKundenMitEmailVorhanden " + ENDE);
	}
	
	@Test
	@InSequence(10)
	public void findKundenMitNachnameVorhanden() {
		LOGGER.finer("findKundenMitNachnameVorhanden " + BEGINN);
		
		// Given
		final String nachname = KUNDE_NACHNAME_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		login(username, password);
		final Collection<AbstractKunde> kunden = ks.findKundenByNachname(nachname, FetchType.NUR_KUNDE);
		
		// Then
		assertThat(!kunden.isEmpty(), is(true));
		kunden.parallelStream()
              .map(AbstractKunde::getNachname)
	          .forEach(n -> assertThat(n, is(nachname)));

		logout();
		LOGGER.finer("findKundenMitNachnameVorhanden " + ENDE);
	}
	
	@Test
	@InSequence(11)
	public void findKundenMitNachnameVorhandenFetch() {
		LOGGER.finer("findKundenMitNachnameVorhandenFetch " + BEGINN);
		
		// Given
		final String nachname = KUNDE_NACHNAME_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		login(username, password);
		final Collection<AbstractKunde> kunden = ks.findKundenByNachname(nachname, FetchType.MIT_BESTELLUNGEN);
		
		// Then
		kunden.forEach(k -> {
			assertThat(k.getNachname(), is(nachname));
			
			// Zugriff auf Bestellungen auch ausserhalb einer Transaktion moeglich
			final Collection<Bestellung> bestellungen = k.getBestellungen();
			if (bestellungen != null) {
				assertThat(!bestellungen.isEmpty(), anyOf(is(true), is(true)));
			}
		});

		logout();
		LOGGER.finer("findKundenMitNachnameVorhandenFetch " + ENDE);
	}

	@Test
	@InSequence(12)
	public void findKundenMitNachnameNichtVorhanden() {
		LOGGER.finer("findKundenMitNachnameNichtVorhanden " + BEGINN);

		// Given
		final String nachname = KUNDE_NACHNAME_NICHT_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		login(username, password);
		thrown.expect(ConstraintViolationException.class);
		ks.findKundenByNachname(nachname, FetchType.NUR_KUNDE);
		//logout();
	}
	
	@Test
	@InSequence(13)
	public void findKundenMitNachnameUngueltig() {
		LOGGER.finer("findKundenMitNachnameUngueltig " + BEGINN);
		
		// Given
		final String nachname = KUNDE_NACHNAME_UNGUELTIG;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;

		// When
		login(username, password);
		thrown.expect(ConstraintViolationException.class);
		ks.findKundenByNachname(nachname, FetchType.NUR_KUNDE);
		//logout();
	}
	
	@Test
	@InSequence(14)
	public void findKundenMitNachnameCriteriaVorhanden() {
		LOGGER.finer("findKundenMitNachnameCriteriaVorhanden " + BEGINN);

		// Given
		final String nachname = KUNDE_NACHNAME_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		login(username, password);
		final Collection<AbstractKunde> kunden = ks.findKundenByNachnameCriteria(nachname);
		
		// Then
		assertThat(!kunden.isEmpty(), is(true));
		kunden.parallelStream()
		      .map(AbstractKunde::getNachname)
		      .forEach(n -> assertThat(n, is(nachname)));

		logout();
		LOGGER.finer("findKundenMitNachnameCriteriaVorhanden " + ENDE);
	}
	
	@Test
	@InSequence(20)
	public void findKundenMitMinBestMenge() {
		LOGGER.finer("findKundenMitMinBestMenge " + BEGINN);
		
		// Given
		final int minMenge = 2;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		login(username, password);
		final Collection<AbstractKunde> kunden = ks.findKundenByMinBestMenge(minMenge);
		
		// Then
		kunden.stream()
			  .map(AbstractKunde::getId)
			  .forEach(id -> {
			final AbstractKunde kundeMitBestellungen = ks.findKundeById(id, FetchType.MIT_BESTELLUNGEN);
			int bestellmenge = 0;
			final Collection<Bestellung> bestellungen = kundeMitBestellungen.getBestellungen();
			for (Bestellung b : bestellungen) {
				bestellmenge += b.getBestellpositionen()
						         .stream()
						         .map(Bestellposition::getAnzahl)
						         .map(anz -> anz.intValue())
						         .reduce(0, Integer::sum);
			}
			
			assertThat(bestellmenge >= minMenge, is(true));
		});
		
		logout();
		LOGGER.finer("findKundenMitMinBestMenge " + ENDE);
	}

	
	@Test
	@InSequence(21)
	public void findKundenMitPLZVorhanden() {
		LOGGER.finer("findKundenMitPLZVorhanden " + BEGINN);

		// Given
		final String plz = PLZ_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		// When
		login(username, password);
		final Collection<AbstractKunde> kunden = ks.findKundenByPLZ(plz);
		
		// Then
		assertThat(!kunden.isEmpty(), is(true));
		kunden.parallelStream()
		      .map(AbstractKunde::getAdresse)
		      .forEach(adr -> { assertThat(adr, notNullValue());
		                        assertThat(adr.getPlz(), is(plz));
		                      }
		              );

		logout();
		LOGGER.finer("findKundenMitPLZVorhanden " + ENDE);
	}

	@Test
	@InSequence(22)
	public void findKundenMitPLZUngueltig() {
		LOGGER.finer("findKundenMitPLZUngueltig " + BEGINN);

		// Given
		final String plz = PLZ_UNGUELTIG;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;

		// When
		login(username, password);
		thrown.expect(ConstraintViolationException.class);
		ks.findKundenByPLZ(plz);
		//logout();
	}

	@Test
	@InSequence(30)
	public void createPrivatkunde() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	                                       SystemException, NotSupportedException {
		LOGGER.finer("createPrivatkunde " + BEGINN);

		// Given
		final String nachname = KUNDE_NEU_NACHNAME;
		final String email = KUNDE_NEU_EMAIL;
		final int kategorie = KATEGORIE_NEU;
		final BigDecimal rabatt = RABATT_NEU;
		final BigDecimal umsatz = UMSATZ_NEU;
		final Date seit = SEIT_NEU;
		final boolean newsletter = NEWSLETTER_NEU;
		final FamilienstandType familienstand = FAMILIENSTAND_NEU;
		final Set<HobbyType> hobbies = HOBBIES_NEU;
		final boolean agbAkzeptiert = AGB_AKZEPTIERT_NEU;
		final String password = PASSWORD_NEU;
		final String plz = PLZ_NEU;
		final String ort = ORT_NEU;
		final String strasse = STRASSE_NEU;
		final String hausnr = HAUSNR_NEU;
		final String usernameLogin = USERNAME_ADMIN;
		final String passwordLogin = PASSWORD_ADMIN;
		
		login(usernameLogin, passwordLogin);
		final Collection<AbstractKunde> kundenVorher = ks.findAllKunden(FetchType.NUR_KUNDE, OrderByType.UNORDERED);
		logout();

		// When
		final Privatkunde neuerKunde = new Privatkunde();
		neuerKunde.setNachname(nachname);
		final Adresse adresse = new Adresse(plz, ort, strasse, hausnr);
		neuerKunde.setAdresse(adresse);
		adresse.setKunde(neuerKunde);
		
		neuerKunde.setEmail(email);
		neuerKunde.setKategorie(kategorie);
		neuerKunde.setRabatt(rabatt);
		neuerKunde.setUmsatz(umsatz);
		neuerKunde.setSeit(seit);
		neuerKunde.setNewsletter(newsletter);
		neuerKunde.setFamilienstand(familienstand);
		neuerKunde.setHobbies(hobbies);
		neuerKunde.setAgbAkzeptiert(agbAkzeptiert);
		neuerKunde.setPassword(password);
		neuerKunde.setPasswordWdh(password);

		final Date datumVorher = new Date();
		
		final UserTransaction trans = getUserTransaction();
		trans.begin();
		final AbstractKunde result = ks.createKunde(neuerKunde);
		trans.commit();
		
		// Then
		assertThat(result.getId().longValue() > 0, is(true));
		assertThat(datumVorher.getTime() <= result.getErzeugt().getTime(), is(true));
		assertThat(result, instanceOf(Privatkunde.class));
		final Privatkunde resultPrivatkunde = Privatkunde.class.cast(result);
		assertThat(resultPrivatkunde.getFamilienstand(), is(familienstand));
		assertThat(resultPrivatkunde.getHobbies().size(), is(hobbies.size()));
		for (HobbyType h : resultPrivatkunde.getHobbies()) {
			switch (h) {
				case LESEN:
					continue;
				case REISEN:
					continue;
				default:
					fail("Unzulaessiger Wert fuer HobbyType: " + h);
					break;
			}
		}

		login(usernameLogin, passwordLogin);
		final Collection<AbstractKunde> kundenNachher = ks.findAllKunden(FetchType.NUR_KUNDE, OrderByType.UNORDERED);
		logout();
		assertThat(kundenNachher.size(), is(kundenVorher.size() + 1));
		
		kundenVorher.parallelStream()
		            .forEach(k -> { assertThat(k.getId() < result.getId(), is(true));
					                  assertThat(k.getErzeugt().getTime() < result.getErzeugt().getTime(), is(true));
					              }
		                    );

		LOGGER.finer("createPrivatkunde " + ENDE);
	}
	
	@Test
	@InSequence(31)
	public void createPrivatkundeEmailExists() throws NotSupportedException, SystemException {
		LOGGER.finer("createPrivatkundeEmailExists " + BEGINN);

		// Given
		final Long kundeId = PRIVATKUNDE_ID_VORHANDEN;
		final String password = PASSWORD_NEU;
		final String usernameLogin = USERNAME_ADMIN;
		final String passwordLogin = PASSWORD_ADMIN;
		
		login(usernameLogin, passwordLogin);
		final AbstractKunde k = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		logout();
		
		// When
		final AbstractKunde neuerKunde = new Privatkunde();
		neuerKunde.setNachname(k.getNachname());
		neuerKunde.setVorname(k.getVorname());
		neuerKunde.setSeit(k.getSeit());
		neuerKunde.setAdresse(k.getAdresse());
		neuerKunde.setEmail(k.getEmail());
		neuerKunde.setAgbAkzeptiert(true);
		neuerKunde.setPassword(password);
		neuerKunde.setPasswordWdh(password);
		
		// Then
		final UserTransaction trans = getUserTransaction();
		trans.begin();
		thrown.expect(EmailExistsException.class);
		thrown.expectMessage("Die Email-Adresse " + k.getEmail() + " existiert bereits");
		ks.createKunde(neuerKunde);
		trans.rollback();
	}
	
	@Test
	@InSequence(32)
	public void createKundeOhneAdresse() throws NotSupportedException, SystemException, HeuristicMixedException,
	                                            HeuristicRollbackException {
		LOGGER.finer("createKundeOhneAdresse " + BEGINN);

		// Given
		final String nachname = KUNDE_NEU_NACHNAME;
		final Adresse adresse = null;
		
		// When
		final AbstractKunde neuerKunde = new Privatkunde();
		neuerKunde.setNachname(nachname);
		neuerKunde.setAdresse(adresse);
		
		final UserTransaction trans = getUserTransaction();
		trans.begin();
		ks.createKunde(neuerKunde);

		try {
				trans.commit();
			}
		catch (RollbackException e) {
			@SuppressWarnings("ThrowableResultIgnored")
			final PersistenceException persistenceException = PersistenceException.class.cast(e.getCause());
			@SuppressWarnings("ThrowableResultIgnored")
			final ConstraintViolationException constraintViolationException =
					                           ConstraintViolationException.class
					                                                       .cast(persistenceException.getCause());
			
			final Optional<ConstraintViolation<?>> violation =
					                               constraintViolationException.getConstraintViolations()
					                                                           .stream()
					                                                           .filter(v -> "Ein Kunde muss eine Adresse haben.".equals(v.getMessage()))
					                                                           .findAny();
			assertThat(violation.isPresent(), is(true));

			LOGGER.finer("createKundeOhneAdresse " + ENDE);
			return;
		}
		
		fail("RollbackException wurde nicht geworfen");
	}

	@Test
	@InSequence(40)
	public void neuerNachnameFuerKunde() throws RollbackException, HeuristicMixedException,
	                                            HeuristicRollbackException, SystemException,
	                                            NotSupportedException {
		LOGGER.finer("neuerNachnameFuerKunde " + BEGINN);

		// Given
		final Long kundeId = PRIVATKUNDE_ID_VORHANDEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		login(username, password);
		AbstractKunde kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		assertThat(kunde, notNullValue());
		
		// When
		final String alterNachname = kunde.getNachname();
		final String neuerNachname = alterNachname + alterNachname.charAt(alterNachname.length() - 1);
		kunde.setNachname(neuerNachname);
		final UserTransaction trans = getUserTransaction();
		trans.begin();
		kunde = ks.updateKunde(kunde, false);
		trans.commit();

		// Then
		assertThat(kunde.getNachname(), is(neuerNachname));
		
		kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		assertThat(kunde.getNachname(), is(neuerNachname));

		logout();
		LOGGER.finer("neuerNachnameFuerKunde " + ENDE);
	}
	
	@Test
	@InSequence(50)
	public void deleteKundeOhneBestellungen() throws RollbackException, HeuristicMixedException,
	                                                 HeuristicRollbackException, SystemException,
	                                                 NotSupportedException {
		LOGGER.finer("deleteKundeOhneBestellungen " + BEGINN);

		// Given
		final Long kundeId = KUNDE_ID_OHNE_BESTELLUNGEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		login(username, password);
		final Collection<AbstractKunde> kundenVorher = ks.findAllKunden(FetchType.NUR_KUNDE, OrderByType.UNORDERED);
		final AbstractKunde kunde = ks.findKundeById(kundeId, FetchType.MIT_BESTELLUNGEN);
		assertThat(kunde, notNullValue());
		
		// When
		final UserTransaction trans = getUserTransaction();
		trans.begin();
		ks.deleteKunde(kunde);
		trans.commit();

		// Then
		final Collection<AbstractKunde> kundenNachher = ks.findAllKunden(FetchType.NUR_KUNDE, OrderByType.UNORDERED);
		assertThat(kundenNachher.size(), is(kundenVorher.size() - 1));

		logout();
		LOGGER.finer("deleteKundeOhneBestellungen " + ENDE);
	}
	
	@Test
	@InSequence(51)
	public void deleteKundeMitBestellungen() throws NotSupportedException, SystemException {
		LOGGER.finer("deleteKundeMitBestellungen " + BEGINN);

		// Given
		final Long kundeId = KUNDE_ID_MIT_BESTELLUNGEN;
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		login(username, password);

		final AbstractKunde kunde = ks.findKundeById(kundeId, FetchType.MIT_BESTELLUNGEN);
		assertThat(kunde, notNullValue());

		// When & Then
		final UserTransaction trans = getUserTransaction();
		trans.begin();
		thrown.expect(KundeDeleteBestellungException.class);
		thrown.expectMessage("Kunde mit ID=" + kundeId + " kann nicht geloescht werden: ");
		thrown.expectMessage("Bestellung(en)");
		ks.deleteKunde(kunde);
		trans.rollback();
		logout();
	}
}
