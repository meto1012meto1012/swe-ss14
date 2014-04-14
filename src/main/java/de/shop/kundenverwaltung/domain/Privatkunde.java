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

package de.shop.kundenverwaltung.domain;

import de.shop.util.persistence.AbstractVersionedAuditable;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.UniqueConstraint;
import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlRootElement;

import static de.shop.kundenverwaltung.domain.AbstractKunde.PRIVATKUNDE;
import static javax.persistence.FetchType.EAGER;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@XmlRootElement
//MOXy statt Jackson
//@XmlDiscriminatorValue(AbstractKunde.PRIVATKUNDE)
@Entity
@Inheritance
@DiscriminatorValue(PRIVATKUNDE)
@NamedQueries({
	@NamedQuery(name  = Privatkunde.FIND_BY_GESCHLECHT,
                query = "SELECT  k"
				        + " FROM Privatkunde k"
                		+ " WHERE k.geschlecht = :" + Privatkunde.PARAM_GESCHLECHT)
})
@Cacheable
public class Privatkunde extends AbstractKunde {
	private static final long serialVersionUID = -1783340753647408724L;
	
	private static final String PREFIX = "Privatkunde.";
	public static final String FIND_BY_GESCHLECHT = PREFIX + "findByGeschlecht";
	public static final String PARAM_GESCHLECHT = "geschlecht";
	
	@Column(length = 2)
	@FormParam(value = "familienstand")
	private FamilienstandType familienstand = FamilienstandType.VERHEIRATET;
	
	@Column(length = 1)
	@FormParam(value = "geschlecht")
	private GeschlechtType geschlecht = GeschlechtType.WEIBLICH;
	
	@ElementCollection(fetch = EAGER)
	@CollectionTable(name = "kunde_hobby",
	                 joinColumns = @JoinColumn(name = "kunde_fk", nullable = false),
                     uniqueConstraints =  @UniqueConstraint(columnNames = { "kunde_fk", "hobby" }))
	@Column(table = "kunde_hobby", name = "hobby", length = 2, nullable = false)
	@FormParam(value = "hobbies")
	private Set<HobbyType> hobbies;
	
	public Privatkunde() {
		super();
	}
	
	public Privatkunde(String nachname, String vorname, String email, Date seit) {
		super(nachname, vorname, email, seit);
	}
	
	/**
	 * {inheritDoc}
	 */
	@Override
	public void setValues(AbstractVersionedAuditable other) {
		super.setValues(other);
		
		if (!other.getClass().equals(Privatkunde.class)) {
			return;
		}
		
		final Privatkunde pk = (Privatkunde) other;
		familienstand = pk.familienstand;
		geschlecht = pk.geschlecht;
		hobbies = pk.hobbies;
	}
	
	public FamilienstandType getFamilienstand() {
		return familienstand;
	}
	
	public void setFamilienstand(FamilienstandType familienstand) {
		this.familienstand = familienstand;
	}

	public GeschlechtType getGeschlecht() {
		return geschlecht;
	}
	public void setGeschlecht(GeschlechtType geschlecht) {
		this.geschlecht = geschlecht;
	}
	public Set<HobbyType> getHobbies() {
		if (hobbies == null) {
			return null;
		}
		return Collections.unmodifiableSet(hobbies);
	}
	
	public void setHobbies(Set<HobbyType> hobbies) {
		if (this.hobbies == null) {
			this.hobbies = hobbies;
			return;
		}
		
		// Wiederverwendung der vorhandenen Collection
		this.hobbies.clear();
		if (hobbies != null) {
			this.hobbies.addAll(hobbies);
		}
	}

	@Override
	public String toString() {
		return "Privatkunde [" + super.toString() + ", familienstand=" + familienstand
		       + ", geschlecht=" + geschlecht + ", hobbies=" + hobbies + "]";
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		// Fuer Validierung an der Benutzeroberflaeche
		final Privatkunde neuesObjekt = Privatkunde.class.cast(super.clone());
		
		neuesObjekt.familienstand = familienstand;
		neuesObjekt.geschlecht = geschlecht;
		neuesObjekt.hobbies = hobbies;
		
		return neuesObjekt;
	}
}
