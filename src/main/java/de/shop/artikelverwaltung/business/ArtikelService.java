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

package de.shop.artikelverwaltung.business;

import com.google.common.base.Strings;
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.domain.Artikel_;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.jboss.ejb3.annotation.SecurityDomain;

import static de.shop.auth.domain.RolleType.ADMIN_STRING;
import static de.shop.auth.domain.RolleType.MITARBEITER_STRING;
import static de.shop.util.Constants.SHOP_DOMAIN;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Stateless
@RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
@SecurityDomain(SHOP_DOMAIN)
public class ArtikelService implements Serializable {
	private static final long serialVersionUID = 5292529185811096603L;
	
	@Inject
	private transient EntityManager em;
	
	/**
	 * Verfuegbare Artikel ermitteln
	 * @return Liste der verfuegbaren Artikel
	 */
	@PermitAll
	public List<Artikel> findVerfuegbareArtikel() {
		return em.createNamedQuery(Artikel.FIND_VERFUEGBARE_ARTIKEL, Artikel.class)
				 .getResultList();
	}

	
	/**
	 * Einen Artikel zu gegebener ID suchen und ggf. ConstraintViolationException zu @NotNull werfen,
	 * falls kein Artikel gefunden wurde
	 * @param id ID des gesuchten Artikels
	 * @return Der Artikel zur gegebenen ID.
	 */
	@NotNull(message = "{artikel.notFound.id}")
	@PermitAll
	public Artikel findArtikelById(Long id) {
		return em.find(Artikel.class, id);
	}
	
	/**
	 * Liste mit Artikeln mit gleicher Bezeichnung suchen und ggf. ConstraintViolationException zu @Size werfen,
	 * falls die Liste leer ist
	 * @param bezeichnung Die Bezeichnung der gesuchten Artikel suchen
	 * @return Liste der gefundenen Artikel suchen
	 */
	@Size(min = 1, message = "{artikel.notFound.bezeichnung}")
	@PermitAll
	public List<Artikel> findArtikelByBezeichnung(String bezeichnung) {
		if (Strings.isNullOrEmpty(bezeichnung)) {
			return findVerfuegbareArtikel();
		}
		
		return em.createNamedQuery(Artikel.FIND_ARTIKEL_BY_BEZ, Artikel.class)
				 .setParameter(Artikel.PARAM_BEZEICHNUNG, "%" + bezeichnung + "%")
				 .getResultList();
		
	}
	
	/**
	 * Artikel zu gegebenen IDs suchen und ggf. ConstraintViolationException zu @Size werfen, falls die Liste leer ist
	 * @param ids Liste der IDs
	 * @return Liste der gefundenen Artikel
	 */
	@Size(min = 1, message = "{artikel.notFound.ids}")
	@PermitAll
	public List<Artikel> findArtikelByIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}
		
		// SELECT a
		// FROM   Artikel a
		// WHERE  a.id = ? OR a.id = ? OR ...
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<Artikel> criteriaQuery = builder.createQuery(Artikel.class);
		final Root<Artikel> a = criteriaQuery.from(Artikel.class);

		final Path<Long> idPath = a.get(Artikel_.id);
			
		Predicate pred;
		if (ids.size() == 1) {
			// Genau 1 id: kein OR notwendig
			pred = builder.equal(idPath, ids.get(0));
		}
		else {
			// Mind. 2x id, durch OR verknuepft
			final Predicate[] equals = ids.stream()
					                      .map(id -> builder.equal(idPath, id))
					                      .collect(Collectors.toList())
					                      .toArray(new Predicate[ids.size()]);
			pred = builder.or(equals);
		}
		criteriaQuery.where(pred);
			
		return em.createQuery(criteriaQuery)
				 .getResultList();
	}
	
	/**
	 * Liste der wenig bestellten Artikel ermitteln
	 * @param anzahl Obergrenze fuer die maximale Anzahl der Bestellungen
	 * @return Liste der gefundenen Artikel
	 */
	@PermitAll
	public List<Artikel> ladenhueter(int anzahl) {
		return em.createNamedQuery(Artikel.FIND_LADENHUETER, Artikel.class)
				 .setMaxResults(anzahl)
				 .getResultList();
	}
	
	/**
	 * Einen neuen Artikel anlegen
	 * @param artikel Der neue Artikel
	 * @return Der neue Artikel einschliesslich generierter ID
	 */
	public Artikel createArtikel(Artikel artikel) {
		if (artikel == null) {
			return artikel;
		}
	
		em.persist(artikel);
		return artikel;
	}
	
	/**
	 * Einen vorhandenen Artikel aktualisieren
	 * @param artikel Der aktualisierte Artikel
	 * @return Der aktualisierte Artikel
	 */
	public Artikel updateArtikel(Artikel artikel) {
		if (artikel == null) {
			return artikel;
		}
	
		em.merge(artikel);
		return artikel;
	}
}
