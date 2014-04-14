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

import de.shop.bestellverwaltung.domain.Lieferung;
import java.io.Serializable;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.validation.constraints.Size;

import static de.shop.util.Constants.LOADGRAPH;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Stateless
public class LieferungService implements Serializable {
	private static final long serialVersionUID = 1500121148732394118L;
	
	@Inject
	private transient EntityManager em;

	/**
	 * Lieferungen zu gegebenem Praefix der Liefernummer suchen.
	 * ConstraintViolationException zu @Size wird geworfen, falls die Liste leer ist
	 * @param nr Praefix der Liefernummer
	 * @return Liste der Lieferungen
	 */
	@Size(min = 1, message = "{lieferung.notFound.nr}")
	public List<Lieferung> findLieferungen(String nr) {
		final EntityGraph<?> entityGraph = em.getEntityGraph(Lieferung.GRAPH_BESTELLUNGEN);
		return em.createNamedQuery(Lieferung.FIND_LIEFERUNGEN_BY_LIEFERNR, Lieferung.class)
                 .setParameter(Lieferung.PARAM_LIEFER_NR, nr)
                 .setHint(LOADGRAPH, entityGraph)
                 .getResultList();
	}
	
	/**
	 * Eine neue Lieferung in der DB anlegen
	 * @param lieferung Die neue Lieferung
	 * @return Die neue Lieferung einschliesslich generierter ID
	 */
	public Lieferung createLieferung(Lieferung lieferung) {
		if (lieferung == null) {
			return null;
		}
		
		em.persist(lieferung);
		return lieferung;
	}
}
