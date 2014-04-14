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

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import de.shop.auth.business.AuthService;
import de.shop.auth.domain.RolleType;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellposition_;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Bestellung_;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.AbstractKunde_;
import de.shop.kundenverwaltung.domain.Adresse_;
import de.shop.kundenverwaltung.domain.GeschlechtType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.kundenverwaltung.domain.Privatkunde_;
import de.shop.util.NoMimeTypeException;
import de.shop.util.persistence.ConcurrentDeletedException;
import de.shop.util.persistence.File;
import de.shop.util.persistence.FileHelper;
import de.shop.util.persistence.MimeType;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.jboss.ejb3.annotation.SecurityDomain;

import static de.shop.auth.domain.RolleType.ADMIN_STRING;
import static de.shop.auth.domain.RolleType.KUNDE_STRING;
import static de.shop.auth.domain.RolleType.MITARBEITER_STRING;
import static de.shop.util.Constants.LOADGRAPH;
import static de.shop.util.Constants.MAX_AUTOCOMPLETE;
import static de.shop.util.Constants.SHOP_DOMAIN;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Stateless
@RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
@SecurityDomain(SHOP_DOMAIN)
public class KundeService implements Serializable {
	private static final long serialVersionUID = 5654417703891549367L;
	
	public enum FetchType {
		NUR_KUNDE,
		MIT_BESTELLUNGEN,
		MIT_ROLLEN,
		MIT_WARTUNGSVERTRAEGEN
	}
	
	public enum OrderByType {
		UNORDERED,
		ID
	}
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private transient EntityManager em;
	
	@EJB
	private AuthService authService;
	
	@Inject
	private Principal principal;
	
	@Inject
	private FileHelper fileHelper;
	
	@Inject
	private transient ManagedExecutorService managedExecutorService;

	@Inject
	@NeuerKunde
	private transient Event<AbstractKunde> event;

	/**
	 * Suche nach einem Kunden anhand der ID
	 * @param id ID des gesuchten Kunden
	 * @param fetch Angabe, welche Objekte mitgeladen werden sollen
	 * @return Der gesuchte Kunde
	 * @exception ConstraintViolationException zu @NotNull, falls kein Kunde gefunden wurde
	 */
	@NotNull(message = "{kunde.notFound.id}")
	@RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
	@SuppressWarnings("null")
	public AbstractKunde findKundeById(Long id, FetchType fetch) {
		if (id == null) {
			return null;
		}
		
		// Falls nur die Rolle "kunde" vorhanden ist, dann darf nur nach der eigenen ID gesucht werden
		authService.checkSameUser(id);
		
		AbstractKunde kunde;
		EntityGraph<?> entityGraph;
		Map<String, Object> props;
		switch (fetch) {
			case NUR_KUNDE:
				kunde = em.find(AbstractKunde.class, id);
				break;
			
			case MIT_BESTELLUNGEN:
				entityGraph = em.getEntityGraph(AbstractKunde.GRAPH_BESTELLUNGEN);
				props = Collections.singletonMap(LOADGRAPH, entityGraph);
				kunde = em.find(AbstractKunde.class, id, props);
				break;
				
			case MIT_ROLLEN:
				entityGraph = em.getEntityGraph(AbstractKunde.GRAPH_ROLLEN);
				props = Collections.singletonMap(LOADGRAPH, entityGraph);
				kunde = em.find(AbstractKunde.class, id, props);
				break;
				
			case MIT_WARTUNGSVERTRAEGEN:
				entityGraph = em.getEntityGraph(AbstractKunde.GRAPH_WARTUNGSVERTRAEGE);
				props = Collections.singletonMap(LOADGRAPH, entityGraph);
				kunde = em.find(AbstractKunde.class, id, props);
				break;

			default:
				kunde = em.find(AbstractKunde.class, id);
				break;
		}
		
		return kunde;
	}
	
	/**
	 * Potenzielle IDs zu einem gegebenen ID-Praefix suchen
	 * @param idPrefix der Praefix zu potenziellen IDs als String
	 * @return Liste der passenden Praefixe
	 */
	public List<Long> findIdsByPrefix(String idPrefix) {
		if (Strings.isNullOrEmpty(idPrefix)) {
			return Collections.emptyList();
		}
		return em.createNamedQuery(AbstractKunde.FIND_IDS_BY_PREFIX, Long.class)
				 .setParameter(AbstractKunde.PARAM_KUNDE_ID_PREFIX, idPrefix + '%')
				 .getResultList();
	}
	
	/**
	 * Kunden suchen, deren ID den gleiche Praefix hat.
	 * @param id Praefix der ID
	 * @return Liste mit Kunden mit passender ID
	 */
	public List<AbstractKunde> findKundenByIdPrefix(Long id) {
		if (id == null) {
			return Collections.emptyList();
		}
		
		return em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_ID_PREFIX, AbstractKunde.class)
				 .setParameter(AbstractKunde.PARAM_KUNDE_ID_PREFIX, id.toString() + '%')
				 .setMaxResults(MAX_AUTOCOMPLETE)
				 .getResultList();
	}
	
	/**
	 * Den Kunden zu einer gegebenen Emailadresse suchen.
	 * @param email Die gegebene Emailadresse
	 * @return Der gefundene Kunde
	 * @exception ConstraintViolationException zu @NotNull, falls kein Kunde gefunden wurde
	 */
	@NotNull(message = "{kunde.notFound.email}")
	@RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
	@SuppressWarnings("null")
	public AbstractKunde findKundeByEmail(String email) {
		AbstractKunde kunde;
		try {
			kunde = em.createNamedQuery(AbstractKunde.FIND_KUNDE_BY_EMAIL, AbstractKunde.class)
					  .setParameter(AbstractKunde.PARAM_KUNDE_EMAIL, email)
					  .getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
		
		// Falls nur die Rolle "kunde" vorhanden ist, dann darf nur nach der eigenen Email-Adresse gesucht werden
		authService.checkSameUser(kunde.getId());
		
		return kunde;
	}

	/**
	 * Den Kunden zu einem Benutzernamen suchen.
	 * @param userName Der Benutzername, zu dem der passende Kunde gesucht wird.
	 * @param fetch Angabe, welche referenzierten Objekte mitgeladen werden sollen
	 * @return Der gefundene Kunde
	 * @exception ConstraintViolationException zu @NotNull, falls kein Kunde gefunden wurde
	 */
	@NotNull(message = "{kunde.notFound.username}")
	@RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
	@SuppressWarnings("null")
	public AbstractKunde findKundeByUserName(String userName, FetchType fetch) {
		// Falls nur die Rolle "kunde" vorhanden ist, dann darf nur nach dem eigenen Usernamen gesucht werden
		authService.checkSameUser(userName);
		
		final TypedQuery<AbstractKunde> query = em.createNamedQuery(AbstractKunde.FIND_KUNDE_BY_USERNAME,
				                                                    AbstractKunde.class)
					                              .setParameter(AbstractKunde.PARAM_KUNDE_USERNAME, userName);

		EntityGraph<?> entityGraph;
		switch (fetch) {
			case NUR_KUNDE:
				break;
				
			case MIT_BESTELLUNGEN:
				entityGraph = em.getEntityGraph(AbstractKunde.GRAPH_BESTELLUNGEN);
				query.setHint(LOADGRAPH, entityGraph);
				break;
				
			case MIT_ROLLEN:
				entityGraph = em.getEntityGraph(AbstractKunde.GRAPH_ROLLEN);
				query.setHint(LOADGRAPH, entityGraph);
				break;
				
			case MIT_WARTUNGSVERTRAEGEN:
				entityGraph = em.getEntityGraph(AbstractKunde.GRAPH_WARTUNGSVERTRAEGE);
				query.setHint(LOADGRAPH, entityGraph);
				break;
				
			default:
				break;
		}
				
		try {
			return query.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 * Alle Kunden in einer bestimmten Reihenfolge ermitteln
	 * @param fetch Angabe, welche Objekte mitgeladen werden sollen, z.B. Bestellungen.
	 * @param order Sortierreihenfolge, z.B. nach aufsteigenden IDs.
	 * @return Liste der Kunden
	 */
	public List<AbstractKunde> findAllKunden(FetchType fetch, OrderByType order) {
		final TypedQuery<AbstractKunde> query = OrderByType.ID.equals(order)
				                        ? em.createNamedQuery(AbstractKunde.FIND_KUNDEN_ORDER_BY_ID,
										                      AbstractKunde.class)
				                        : em.createNamedQuery(AbstractKunde.FIND_KUNDEN, AbstractKunde.class);
		EntityGraph<?> entityGraph;
		switch (fetch) {
			case NUR_KUNDE:
				break;
				
			case MIT_BESTELLUNGEN:
				entityGraph = em.getEntityGraph(AbstractKunde.GRAPH_BESTELLUNGEN);
				query.setHint(LOADGRAPH, entityGraph);
				break;
				
			case MIT_ROLLEN:
				entityGraph = em.getEntityGraph(AbstractKunde.GRAPH_ROLLEN);
				query.setHint(LOADGRAPH, entityGraph);
				break;
				
			case MIT_WARTUNGSVERTRAEGEN:
				entityGraph = em.getEntityGraph(AbstractKunde.GRAPH_WARTUNGSVERTRAEGE);
				query.setHint(LOADGRAPH, entityGraph);
				break;
				
			default:
				break;
		}
		
		return query.getResultList();
	}
	

	/**
	 * Kunden mit gleichem Nachnamen suchen.
	 * @param nachname Der gemeinsame Nachname der gesuchten Kunden
	 * @param fetch Angabe, welche Objekte mitgeladen werden sollen, z.B. Bestellungen
	 * @return Liste der gefundenen Kunden
	 * @exception ConstraintViolationException zu @Size, falls die Liste leer ist
	 */
	@Size(min = 1, message = "{kunde.notFound.nachname}")
	public List<AbstractKunde> findKundenByNachname(String nachname, FetchType fetch) {
		final TypedQuery<AbstractKunde> query = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_NACHNAME,
		                                                            AbstractKunde.class)
						                          .setParameter(AbstractKunde.PARAM_KUNDE_NACHNAME, nachname);
		EntityGraph<?> entityGraph;
		switch (fetch) {
			case NUR_KUNDE:
				break;
				
			case MIT_BESTELLUNGEN:
				entityGraph = em.getEntityGraph(AbstractKunde.GRAPH_BESTELLUNGEN);
				query.setHint(LOADGRAPH, entityGraph);
				break;
				
			case MIT_ROLLEN:
				entityGraph = em.getEntityGraph(AbstractKunde.GRAPH_ROLLEN);
				query.setHint(LOADGRAPH, entityGraph);
				break;
				
			case MIT_WARTUNGSVERTRAEGEN:
				entityGraph = em.getEntityGraph(AbstractKunde.GRAPH_WARTUNGSVERTRAEGE);
				query.setHint(LOADGRAPH, entityGraph);
				break;
				
			default:
				break;
		}
		
		return query.getResultList();
	}

	
	/**
	 * Suche nach Nachnamen mit dem gleichen Praefix
	 * @param nachnamePrefix der gemeinsame Praefix fuer die potenziellen Nachnamen 
	 * @return Liste der Nachnamen mit gleichem Praefix
	 */
	public List<String> findNachnamenByPrefix(String nachnamePrefix) {
		return em.createNamedQuery(AbstractKunde.FIND_NACHNAMEN_BY_PREFIX, String.class)
				 .setParameter(AbstractKunde.PARAM_KUNDE_NACHNAME_PREFIX, nachnamePrefix + '%')
				 .setMaxResults(MAX_AUTOCOMPLETE)
				 .getResultList();
	}

	/**
	 * Kunden mit gleicher Postleitzahl suchen
	 * @param plz Die gegebene Postleitzahl
	 * @return Liste der gefundenen Kunden
	 * @exception ConstraintViolationException zu @Size, falls die Liste leer ist
	 */
	@Size(min = 1, message = "{kunde.notFound.plz}")
	public List<AbstractKunde> findKundenByPLZ(String plz) {
		return em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_PLZ, AbstractKunde.class)
				 .setParameter(AbstractKunde.PARAM_KUNDE_ADRESSE_PLZ, plz)
				 .getResultList();
	}
	
	/**
	 * Kunden suchen, die seit einem bestimmten Datum Kunde sind.
	 * @param seit Das Datum
	 * @return Liste der gefundenen Kunden
	 * @exception ConstraintViolationException zu @Size, falls die Liste leer ist
	 */
	@Size(min = 1, message = "{kunde.notFound.seit}")
	public List<AbstractKunde> findKundenBySeit(Date seit) {
		return em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_DATE, AbstractKunde.class)
				 .setParameter(AbstractKunde.PARAM_KUNDE_SEIT, seit)
				 .getResultList();
	}
	
	/**
	 * Kunden mit gleichem Geschlecht suchen.
	 * @param geschlecht Das Geschlecht
	 * @return Liste der gefundenen Kunden
	 * @exception ConstraintViolationException zu @Size, falls die Liste leer ist
	 */
	@Size(min = 1, message = "{kunde.notFound.geschlecht}")
	public List<Privatkunde> findKundenByGeschlecht(GeschlechtType geschlecht) {
		return em.createNamedQuery(Privatkunde.FIND_BY_GESCHLECHT, Privatkunde.class)
				 .setParameter(Privatkunde.PARAM_GESCHLECHT, geschlecht)
				 .getResultList();
	}
	
	/**
	 * Alle Privat- und Firmenkunden suchen.
	 * @return Liste der gefundenen Kunden
	 */
	public List<AbstractKunde> findPrivatkundenFirmenkunden() {
		return em.createNamedQuery(AbstractKunde.FIND_PRIVATKUNDEN_FIRMENKUNDEN, AbstractKunde.class)
				 .getResultList();
	}
	
	/**
	 * Kunden mit gleichem Nachnamen suchen und dabei eine Criteria-Query verwenden.
	 * @param nachname Der Nachname der gesuchten Kunden
	 * @return Liste der gefundenen Kunden
	 * @exception ConstraintViolationException zu @Size, falls die Liste leer ist
	 */
	@Size(min = 1, message = "{kunde.notFound.nachname}")
	public List<AbstractKunde> findKundenByNachnameCriteria(String nachname) {
		// SELECT k
		// FROM   AbstractKunde k
		// WHERE  k.nachname = ?
				
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<AbstractKunde> criteriaQuery = builder.createQuery(AbstractKunde.class);
		final Root<AbstractKunde> k = criteriaQuery.from(AbstractKunde.class);

		final Path<String> nachnamePath = k.get(AbstractKunde_.nachname);
		
		final Predicate pred = builder.equal(nachnamePath, nachname);
		criteriaQuery.where(pred);

		return em.createQuery(criteriaQuery).getResultList();
	}

	/**
	 * Kunden mit einer Mindestbestellmenge suchen
	 * @param minMenge Die minimale Anzahl bestellter Artikel
	 * @return Die gefundenen Kunden
	 * @exception ConstraintViolationException zu @Size, falls die Liste leer ist
	 */
	@Size(min = 1, message = "{kunde.notFound.minBestMenge}")
	public List<AbstractKunde> findKundenByMinBestMenge(int minMenge) {
		// SELECT DISTINCT k
		// FROM   AbstractKunde k
		//        JOIN k.bestellungen b
		//        JOIN b.bestellpositionen bp
		// WHERE  bp.anzahl >= ?
		
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<AbstractKunde> criteriaQuery  = builder.createQuery(AbstractKunde.class);
		final Root<AbstractKunde> k = criteriaQuery.from(AbstractKunde.class);

		final Join<AbstractKunde, Bestellung> b = k.join(AbstractKunde_.bestellungen);
		final Join<Bestellung, Bestellposition> bp = b.join(Bestellung_.bestellpositionen);
		criteriaQuery.where(builder.gt(bp.<Integer>get(Bestellposition_.anzahl), minMenge))
		             .distinct(true);
		
		return em.createQuery(criteriaQuery).getResultList();
	}
	
	/**
	 * Kunden zu den Suchkriterien suchen
	 * @param email Email-Adresse
	 * @param nachname Nachname
	 * @param plz Postleitzahl
	 * @param seit Datum seit
	 * @param geschlecht Geschlecht
	 * @param minBestMenge Mindestbestellmenge
	 * @return Die gefundenen Kunden
	 * @exception ConstraintViolationException zu @Size, falls die Liste leer ist
	 */
	@Size(min = 1, message = "{kunde.notFound.criteria}")
	@RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
	public List<AbstractKunde> findKundenByCriteria(String email, String nachname, String plz, Date seit,
			                                        GeschlechtType geschlecht, Integer minBestMenge) {
		// SELECT DISTINCT k
		// FROM   AbstractKunde [Privatkunde] k
		// WHERE  email = ? AND nachname = ? AND k.adresse.plz = ? and seit = ? and geschlecht = ?
		
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<AbstractKunde> criteriaQuery = builder.createQuery(AbstractKunde.class);
		final Root<AbstractKunde> k = criteriaQuery.from(AbstractKunde.class);
		
		Predicate pred = null;

		if (email != null) {
			final Path<String> emailPath = k.get(AbstractKunde_.email);
			pred = builder.equal(builder.upper(emailPath), builder.upper(builder.literal(email)));
		}
		if (nachname != null) {
			final Path<String> nachnamePath = k.get(AbstractKunde_.nachname);
			final Predicate tmpPred = builder.equal(builder.upper(nachnamePath), builder.upper(builder.literal(nachname)));
			pred = pred == null ? tmpPred : builder.and(pred, tmpPred);
		}
		if (plz != null) {
			final Path<String> plzPath = k.get(AbstractKunde_.adresse)
                                          .get(Adresse_.plz);
			final Predicate tmpPred = builder.equal(plzPath, plz);
			pred = pred == null ? tmpPred : builder.and(pred, tmpPred);
		}
		if (seit != null) {
			final Path<Date> seitPath = k.get(AbstractKunde_.seit);
			final Predicate tmpPred = builder.equal(seitPath, seit);
			pred = pred == null ? tmpPred : builder.and(pred, tmpPred);
		}
		if (minBestMenge != null) {
			final Path<Integer> anzahlPath = k.join(AbstractKunde_.bestellungen)
                                              .join(Bestellung_.bestellpositionen)
                                              .get(Bestellposition_.anzahl);
			final Predicate tmpPred = builder.gt(anzahlPath, minBestMenge);
			pred = pred == null ? tmpPred : builder.and(pred, tmpPred);
		}
		
		if (geschlecht != null) {
			// Geschlecht gibt es nur bei der abgeleiteten Klasse Privatkunde
			final Root<Privatkunde> pk = builder.treat(k, Privatkunde.class);
			final Path<GeschlechtType> geschlechtPath = pk.get(Privatkunde_.geschlecht);
			final Predicate tmpPred = builder.equal(geschlechtPath, geschlecht);
			pred = pred == null ? tmpPred : builder.and(pred, tmpPred);
		}
		
		criteriaQuery.where(pred)
		             .distinct(true);
		
		final List<AbstractKunde> kunden = em.createQuery(criteriaQuery).getResultList();
		
		// Falls man nur "kunde" ist, darf man nur seine eigenen Daten lesen
		if (kunden.size() == 1) {
			authService.checkSameUser(kunden.get(0).getId());			
		}
		else {
			authService.checkAdminMitarbeiter();
		}
		
		return kunden;
	}
	
	/**
	 * Einen neuen Kunden anlegen
	 * @param kunde Der neue Kunde
	 * @param <K> Privatkunde oder Firmenkunde
	 * @return Der neue Kunde einschliesslich generierter ID
	 */
	@PermitAll
	@SuppressWarnings("null")
	public <K extends AbstractKunde> K createKunde(K kunde) {
		if (kunde == null) {
			return kunde;
		}
	
		// Pruefung, ob ein solcher Kunde schon existiert
		final AbstractKunde tmp = findKundeByEmail(kunde.getEmail());  // Kein Aufruf als Business-Methode
		if (tmp != null) {
			// Ein Kunde mit der gleichen Email-Adresse existiert bereits
			throw new EmailExistsException(kunde.getEmail());
		}
		
		// Password verschluesseln
		passwordVerschluesseln(kunde);
		
		// Rolle setzen
		kunde.addRollen(Sets.newHashSet(RolleType.KUNDE));
	
		em.persist(kunde);
		event.fire(kunde);
		
		return kunde;
	}
	
	/**
	 * Einen vorhandenen Kunden aktualisieren
	 * @param kunde Der aktualisierte Kunde
	 * @param geaendertPassword Wurde das Passwort aktualisiert und muss es deshalb verschluesselt werden?
	 * @param <K> Privatkunde oder Firmenkunde
	 * @return Der aktualisierte Kunde
	 */
	@SuppressWarnings("null")
	@RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
	public <K extends AbstractKunde> K updateKunde(K kunde, boolean geaendertPassword) {
		if (kunde == null) {
			return null;
		}
		
		// Falls nur die Rolle "kunde" vorhanden ist, dann duerfen nur die eigenen Daten aktualisiert werden
		authService.checkSameUser(kunde.getId());
		
		// kunde vom EntityManager trennen, weil anschliessend z.B. nach Id und Email gesucht wird
		em.detach(kunde);
		
		// Wurde das Objekt konkurrierend geloescht?
		AbstractKunde tmp = findKundeById(kunde.getId(), FetchType.NUR_KUNDE);  // Kein Aufruf als Business-Methode
		if (tmp == null) {
			throw new ConcurrentDeletedException(kunde.getId());
		}
		em.detach(tmp);
		
		// Gibt es ein anderes Objekt mit gleicher Email-Adresse?
		tmp = findKundeByEmail(kunde.getEmail());  // Kein Aufruf als Business-Methode
		if (tmp != null) {
			em.detach(tmp);
			if (tmp.getId().longValue() != kunde.getId().longValue()) {
				// anderes Objekt mit gleichem Attributwert fuer email
				throw new EmailExistsException(kunde.getEmail());
			}
		}
		
		// Password verschluesseln
		if (geaendertPassword) {
			passwordVerschluesseln(kunde);
		}

		kunde = em.merge(kunde);   // OptimisticLockException
		kunde.setPasswordWdh(kunde.getPassword());
		
		return kunde;
	}

	/**
	 * Einen Kunden in der DB loeschen.
	 * @param kunde Der zu loeschende Kunde
	 */
	@RolesAllowed(ADMIN_STRING)
	public void deleteKunde(AbstractKunde kunde) {
		if (kunde == null) {
			return;
		}

		deleteKundeById(kunde.getId());
	}

	/**
	 * Einen Kunden zu gegebener ID loeschen
	 * @param kundeId Die ID des zu loeschenden Kunden
	 */
	@RolesAllowed(ADMIN_STRING)
	@SuppressWarnings("null")
	public void deleteKundeById(Long kundeId) {
		final AbstractKunde kunde = findKundeById(kundeId, FetchType.MIT_BESTELLUNGEN);  // Kein Aufruf als Business-M.
		if (kunde == null) {
			// Der Kunde existiert nicht oder ist bereits geloescht
			return;
		}

		final boolean hasBestellungen = hasBestellungen(kunde);
		if (hasBestellungen) {
			throw new KundeDeleteBestellungException(kunde);
		}

		// Kundendaten loeschen
		em.remove(kunde);
	}

	
	/**
	 * Einem Kunden eine hochgeladene Datei ohne MIME Type (bei RESTful WS) zuordnen
	 * @param kundeId Die ID des Kunden
	 * @param bytes Das Byte-Array der hochgeladenen Datei
	 * @return Das Kundenobjekt
	 */
	@RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
	@SuppressWarnings("null")
	public AbstractKunde setFile(Long kundeId, byte[] bytes) {
		// Falls nur die Rolle "kunde" vorhanden ist, dann darf nur die eigene Datei aktualisiert werden
		authService.checkSameUser(kundeId);
		
		final AbstractKunde kunde = findKundeById(kundeId, FetchType.NUR_KUNDE);  // Kein Aufruf als Business-M.
		if (kunde == null) {
			return null;
		}
		final MimeType mimeType = fileHelper.getMimeType(bytes);
		setFile(kunde, bytes, mimeType);
		return kunde;
	}
	
	/**
	 * Einem Kunden eine hochgeladene Datei zuordnen
	 * @param kunde Der betroffene Kunde
	 * @param bytes Das Byte-Array der hochgeladenen Datei
	 * @param mimeTypeStr Der MIME-Type als String
	 * @return Das Kundenobjekt
	 */
	public AbstractKunde setFile(AbstractKunde kunde, byte[] bytes, String mimeTypeStr) {
		final MimeType mimeType = MimeType.build(mimeTypeStr);
		setFile(kunde, bytes, mimeType);
		return kunde;
	}
	
	private void setFile(AbstractKunde kunde, byte[] bytes, MimeType mimeType) {
		if (mimeType == null) {
			throw new NoMimeTypeException();
		}
		
		final String filename = fileHelper.getFilename(kunde.getClass(), kunde.getId(), mimeType);
		
		// Gibt es noch kein (Multimedia-) File
		File file = kunde.getFile();
		if (kunde.getFile() == null) {
			file = new File(bytes, filename, mimeType);
			if (LOGGER.isLoggable(FINEST)) {
				LOGGER.finest("Neue Datei " + file);
			}
			kunde.setFile(file);
			em.persist(file);
		}
		else {
			file.set(bytes, filename, mimeType);
			if (LOGGER.isLoggable(FINEST)) {
				LOGGER.finest("Ueberschreiben der Datei " + file);
			}
			em.merge(file);
		}

		// Hochgeladenes Bild/Video/Audio in einem parallelen Thread als Datei fuer die Web-Anwendung abspeichern
		final File newFile = kunde.getFile();
		final Runnable storeFile = () -> {
			fileHelper.store(newFile);
		};
		managedExecutorService.execute(storeFile);
	}

	private static boolean hasBestellungen(AbstractKunde kunde) {
		if (LOGGER.isLoggable(FINER)) {
			LOGGER.finer("hasBestellungen BEGINN: " + kunde);
		}
		
		boolean result = false;
		
		// Gibt es den Kunden und hat er mehr als eine Bestellung?
		// Bestellungen nachladen wegen Hibernate-Caching
		if (kunde != null && kunde.getBestellungen() != null && !kunde.getBestellungen().isEmpty()) {
			result = true;
		}
		
		if (LOGGER.isLoggable(FINER)) {
			LOGGER.finer("hasBestellungen ENDE: " + result);
		}
		return result;
	}

	
	private void passwordVerschluesseln(AbstractKunde kunde) {
		if (LOGGER.isLoggable(FINER)) {
			LOGGER.finer("passwordVerschluesseln BEGINN: " + kunde);
		}

		final String unverschluesselt = kunde.getPassword();
		final String verschluesselt = authService.verschluesseln(unverschluesselt);
		kunde.setPassword(verschluesselt);
		kunde.setPasswordWdh(verschluesselt);

		if (LOGGER.isLoggable(FINER)) {
			LOGGER.finer("passwordVerschluesseln ENDE: " + verschluesselt);
		}
	}
}
