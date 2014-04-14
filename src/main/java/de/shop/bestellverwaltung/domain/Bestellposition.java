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

package de.shop.bestellverwaltung.domain;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.util.persistence.AbstractVersionedAuditable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Objects;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostPersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlTransient;

import static java.util.logging.Level.FINER;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Entity
@Table(indexes = {
	@Index(columnList = "bestellung_fk"),
	@Index(columnList = "artikel_fk")
})
@Cacheable
public class Bestellposition extends AbstractVersionedAuditable {
	private static final long serialVersionUID = 1031749849939138054L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final int ANZAHL_MIN = 1;

	@Id
	@GeneratedValue
	@Basic(optional = false)
	private Long id;
	
	@Min(value = ANZAHL_MIN, message = "{bestellposition.anzahl.min}")
	@Basic(optional = false)
	private int anzahl;
	
	@ManyToOne
	@JoinColumn(name = "artikel_fk", nullable = false)
	//NICHT @NotNull, weil beim Anlegen der Artikel bei REST durch seine URI uebergeben wird
	//NICHT @Valid, weil der Artikel vor der Bestellposition existiert und nicht mehr validiert werden muss
	@XmlTransient
	private Artikel artikel;
	
	@Transient
	private URI artikelUri;
	
	public Bestellposition() {
		super();
	}
	
	public Bestellposition(Artikel artikel) {
		super();
		this.artikel = artikel;
		this.anzahl = 1;
	}
	
	public Bestellposition(Artikel artikel, int anzahl) {
		super();
		this.artikel = artikel;
		this.anzahl = anzahl;
	}
	
	@PostPersist
	private void postPersist() {
		if (LOGGER.isLoggable(FINER)) {
			LOGGER.finer("Neue Bestellposition mit ID=" + id);
		}
	}
	
	public Long getId() {
		return id;
	}

	public int getAnzahl() {
		return anzahl;
	}
	public void setAnzahl(int anzahl) {
		this.anzahl = anzahl;
	}
	
	public Artikel getArtikel() {
		return artikel;
	}
	public void setArtikel(Artikel artikel) {
		this.artikel = artikel;
	}

	public URI getArtikelUri() {
		return artikelUri;
	}
	
	public void setArtikelUri(URI artikelUri) {
		this.artikelUri = artikelUri;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + anzahl;
		result = prime * result + Objects.hashCode(artikel);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Bestellposition other = (Bestellposition) obj;
		if (anzahl != other.anzahl) {
			return false;
		}
		return Objects.equals(artikel, other.artikel);
	}

	@Override
	public String toString() {
		if (artikel == null) {
			return "Bestellposition [id=" + id + ", artikelUri=" + artikelUri + ", anzahl=" + anzahl + ", "
		           + super.toString() + "]";
		}

		return "Bestellposition [id=" + id + ", artikel.id=" + artikel.getId() + ", artikelUri=" + artikelUri
			   + ", anzahl=" + anzahl + ", " + super.toString() + "]";
	}

}
