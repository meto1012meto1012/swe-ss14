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

import de.shop.auth.business.AuthService;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Warenkorbposition;
import de.shop.bestellverwaltung.domain.Warenkorbposition_;
import de.shop.kundenverwaltung.business.KundeService;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBAccessException;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.jboss.ejb3.annotation.SecurityDomain;

import static de.shop.auth.domain.RolleType.ADMIN_STRING;
import static de.shop.auth.domain.RolleType.KUNDE_STRING;
import static de.shop.auth.domain.RolleType.MITARBEITER_STRING;
import static de.shop.kundenverwaltung.business.KundeService.FetchType.MIT_BESTELLUNGEN;
import static de.shop.util.Constants.LOADGRAPH;
import static de.shop.util.Constants.SHOP_DOMAIN;
import static java.math.BigDecimal.ZERO;
import static java.util.logging.Level.FINEST;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Stateless
@RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING, KUNDE_STRING })
@SecurityDomain(SHOP_DOMAIN)
public class BestellungService implements Serializable {
	private static final long serialVersionUID = 3365404106904200415L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	public enum FetchType {
		NUR_BESTELLUNG,
		MIT_LIEFERUNGEN
	}
	
	@Inject
	private transient EntityManager em;
	
	@Inject
	private KundeService ks;
	
	@Inject
	private AuthService as;
	
	@Inject
	@NeueBestellung
	private transient Event<Bestellung> event;
	
	/**
	 * Bestellung zu gegebener ID suchen.
	 * ConstraintViolationException zu @NotNull wird geworfen, falls keine Bestellung gefunden wurde
	 * @param id Gegebene ID
	 * @param fetch Welche Objekte sollen mitgeladen werden, z.B. Lieferungen
	 * @return Die gefundene Bestellung oder null
	 */
	@NotNull(message = "{bestellung.notFound.id}")
	@SuppressWarnings("null")
	public Bestellung findBestellungById(Long id, FetchType fetch) {
		if (id == null) {
			return null;
		}
		
		Bestellung bestellung;
		EntityGraph<?> entityGraph;
		Map<String, Object> props;
		switch (fetch) {
			case NUR_BESTELLUNG:
				bestellung = em.find(Bestellung.class, id);
				break;
				
			case MIT_LIEFERUNGEN:
				entityGraph = em.getEntityGraph(Bestellung.GRAPH_LIEFERUNGEN);
				props = Collections.singletonMap(LOADGRAPH, entityGraph);
				bestellung = em.find(Bestellung.class, id, props);
				break;
				
			default:
				bestellung = em.find(Bestellung.class, id);
				break;
		}
		
		as.checkSameUser(bestellung.getKunde().getId());
		
		return bestellung;
	}

	/**
	 * Bestellungen zu einem gegebenen Kunden suchen.
	 * ConstraintViolationException zu @Size wird geworfen, falls die Liste leer ist
	 * @param kunde Der gegebene Kunde
	 * @param fetch Welche Objekte sollen mitgeladen werden, z.B. Lieferungen
	 * @return Die gefundenen Bestellungen
	 */
	@Size(min = 1, message = "{bestellung.notFound.kunde}")
	public List<Bestellung> findBestellungenByKunde(AbstractKunde kunde, FetchType fetch) {
		if (kunde == null) {
			return Collections.emptyList();
		}
		as.checkSameUser(kunde.getId().toString());
		
		final TypedQuery<Bestellung> query = em.createNamedQuery(Bestellung.FIND_BESTELLUNGEN_BY_KUNDEID,
				                                                 Bestellung.class)
				                               .setParameter(Bestellung.PARAM_KUNDEID, kunde.getId());
				
		switch (fetch) {
			case NUR_BESTELLUNG:
				break;
				
			case MIT_LIEFERUNGEN:
				final EntityGraph<?> entityGraph = em.getEntityGraph(Bestellung.GRAPH_LIEFERUNGEN);
				query.setHint(LOADGRAPH, entityGraph);
				break;
				
			default:
				break;
		}
		
		return query.getResultList();
	}

	/**
	 * Den Kunden zu einer gegebenen Bestellung-ID suchen.
	 * ConstraintViolationException zu @NotNull wird geworfen, falls kein Kunde gefunden wurde
	 * @param id Bestellung-ID
	 * @return Der gefundene Kunde
	 */
	@NotNull(message = "{bestellung.kunde.notFound.id}")
	@SuppressWarnings("null")
	public AbstractKunde findKundeById(Long id) {
		AbstractKunde kunde;
		try {
			kunde = em.createNamedQuery(Bestellung.FIND_KUNDE_BY_ID, AbstractKunde.class)
					  .setParameter(Bestellung.PARAM_ID, id)
					  .getSingleResult();
		}
		catch (NoResultException e) {
			throw new EJBAccessException();
		}
		
		as.checkSameUser(kunde.getId());
		return kunde;
	}


	/**
	 * Zuordnung einer neuen, transienten Bestellung zu einem existierenden, persistenten Kunden,
	 * identifiziert durch den Username.
	 * @param bestellung Die neue Bestellung
	 * @param username Der Benutzername des zuzuordnenden Kunden
	 * @return Die neue Bestellung einschliesslich generierter ID
	 */
	@RolesAllowed(KUNDE_STRING)
	public Bestellung createBestellung(Bestellung bestellung, String username) {
		if (bestellung == null) {
			return null;
		}

		// Den persistenten Kunden mit der transienten Bestellung verknuepfen
		final AbstractKunde kunde = ks.findKundeByUserName(username, MIT_BESTELLUNGEN);
		return createBestellung(bestellung, kunde);
	}
	
	/**
	 * Neue Bestellung aus dem persistenten Warenkorb zu einem existierenden, persistenten Kunden,
	 * identifiziert durch den Username.
	 * @param username Der Benutzername des zuzuordnenden Kunden
	 * @return Die neue Bestellung einschliesslich generierter ID
	 */
	@RolesAllowed(KUNDE_STRING)
	public Bestellung createBestellung(String username) {
		// Den persistenten Kunden mit der transienten Bestellung verknuepfen
		final AbstractKunde kunde = ks.findKundeByUserName(username, MIT_BESTELLUNGEN);
		final List<Warenkorbposition> positionen = em.createNamedQuery(Warenkorbposition.FIND_POSITIONEN_BY_KUNDE,
				                                                       Warenkorbposition.class)
				                                     .setParameter(Warenkorbposition.PARAM_KUNDE, kunde)
				                                     .getResultList();
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest(positionen.toString());
		}
		if (positionen.isEmpty()) {
			return null;
		}
		final List<Bestellposition> bpList = positionen.parallelStream()
				                                       .map(wp -> new Bestellposition(wp.getArtikel(), wp.getAnzahl()))
				                                       .collect(Collectors.toList());
		Bestellung bestellung = new Bestellung(bpList);
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest(bestellung.toString());
		}
		bestellung = createBestellung(bestellung, kunde);
		
		// Warenkorbpositionen des Kunden loeschen
		//    DELETE
		//    FROM   Warenkorbposition wp
		//    WHERE  wp.kunde = ?	
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaDelete<Warenkorbposition> criteriaDelete = builder.createCriteriaDelete(Warenkorbposition.class);
		final Root<Warenkorbposition> wpRoot = criteriaDelete.from(Warenkorbposition.class);
		final Path<? extends AbstractKunde> kundePath = wpRoot.get(Warenkorbposition_.kunde);
		final Predicate pred = builder.equal(kundePath, kunde);
		criteriaDelete.where(pred);
		em.createQuery(criteriaDelete).executeUpdate();
		
		return bestellung;
	}
	
	/**
	 * Zuordnung einer neuen, transienten Bestellung zu einem existierenden, persistenten Kunden.
	 * Der Kunde ist fuer den EntityManager bekannt, die Bestellung dagegen nicht. Das Zusammenbauen
	 * wird sowohl fuer einen Web Service aus auch fuer eine Webanwendung benoetigt.
	 * @param bestellung Die neue Bestellung
	 * @param kunde Der existierende Kunde
	 * @return Die neue Bestellung einschliesslich generierter ID
	 */
	@RolesAllowed(KUNDE_STRING)
	public Bestellung createBestellung(Bestellung bestellung, AbstractKunde kunde) {
		if (bestellung == null || kunde == null) {
			return null;
		}

		// Den persistenten Kunden mit der transienten Bestellung verknuepfen
		if (!em.contains(kunde)) {
			kunde = ks.findKundeById(kunde.getId(), MIT_BESTELLUNGEN);
		}
		bestellung.setKunde(kunde);
		kunde.addBestellung(bestellung);
		
		final BigDecimal gesamtbetrag = bestellung.getBestellpositionen()
				                                  .parallelStream()
				                                  .map(bp -> bp.getArtikel()
														       .getPreis()
														       .multiply(new BigDecimal(bp.getAnzahl())))
				                                  .reduce(ZERO, BestellungService::sum);
		bestellung.setGesamtbetrag(gesamtbetrag);
		
		em.persist(bestellung);
		event.fire(bestellung);
		
		return bestellung;
	}
	
	private static BigDecimal sum(BigDecimal i, BigDecimal j) {
		if (i == null || BigDecimal.ZERO.equals(i)) {
			return j;
		}
		if (j == null || BigDecimal.ZERO.equals(j)) {
			return i;
		}
		return i.add(j);
	
	}
	
	@RolesAllowed(KUNDE_STRING)
	public Warenkorbposition createWarenkorbposition(Warenkorbposition warenkorbposition) {
		if (warenkorbposition == null) {
			return null;
		}
		
		em.persist(warenkorbposition);
		return warenkorbposition;
	}
}
