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
import java.io.Serializable;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;  // sonst gibt es einen Fehler bei javadoc
import javax.validation.constraints.Size;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Stateless
public class WartungsvertragService implements Serializable {
	private static final long serialVersionUID = -7596909915199113773L;
	
	@Inject
	private transient EntityManager em;
	
	@Inject
	private KundeService ks;
	
	/**
	 * Die Wartungsvertraege eines Kunden ermitteln
	 * @param kundeId Die ID des Kunden
	 * @return Die Liste mit seinen Wartungsvertraegen
	 * @exception ConstraintViolationException zu @Size, falls die Liste leer ist
	 */
	@Size(min = 1, message = "{wartungsvertrag.notFound.kundeId}")
	public List<Wartungsvertrag> findWartungsvertraege(Long kundeId) {
		return em.createNamedQuery(Wartungsvertrag.FIND_WARTUNGSVERTRAEGE_BY_KUNDE_ID, Wartungsvertrag.class)
				 .setParameter(Wartungsvertrag.PARAM_KUNDE_ID, kundeId)
				 .getResultList();
	}

	
	/**
	 * Einen neuen Wartungsvertrag anlegen
	 * @param wartungsvertrag Der neue Wartungsvertrag
	 * @param kunde Der zugehoerige Kunde
	 * @return Der neue Wartungsvertrag einschliesslich ggf. generierter ID
	 */
	public Wartungsvertrag createWartungsvertrag(Wartungsvertrag wartungsvertrag, AbstractKunde kunde) {
		if (wartungsvertrag == null) {
			return null;
		}
		
		ks.findKundeById(kunde.getId(), FetchType.NUR_KUNDE);
		em.persist(wartungsvertrag);
		return wartungsvertrag;
	}
}
