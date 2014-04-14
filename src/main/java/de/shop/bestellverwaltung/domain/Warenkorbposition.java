/*
 * Copyright (C) 2014 Hochschule Karlsruhe
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
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.persistence.AbstractVersionedAuditable;
import java.net.URI;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@XmlRootElement
@Entity
@Table(indexes = {
	@Index(columnList = "artikel_fk"),
	@Index(columnList = "kunde_fk")
})
@NamedQueries({
	@NamedQuery(name = Warenkorbposition.FIND_POSITIONEN_BY_KUNDE,
	            query = "SELECT wp FROM Warenkorbposition wp WHERE wp.kunde = :" + Warenkorbposition.PARAM_KUNDE)
})
public class Warenkorbposition extends AbstractVersionedAuditable {
	private static final long serialVersionUID = -1598611773106787288L;

	private static final String PREFIX = "Warenkorbposition.";
	public static final String FIND_POSITIONEN_BY_KUNDE = PREFIX + "findPositionenByKunde";
	public static final String PARAM_KUNDE = "kunde";
	
	@Id
	@GeneratedValue
	@Basic(optional = false)
	private Long id;
	
	@Min(value = 1, message = "{warenkorbposition.anzahl.min}")
	@FormParam(value = "anzahl")
	private int anzahl;
	
	@ManyToOne
	@JoinColumn(name = "artikel_fk", nullable = false)
	@XmlTransient
	private Artikel artikel;
	
	@FormParam(value = "artikelUri")
	@Transient
	private URI artikelUri;
	
	@ManyToOne
	@JoinColumn(name = "kunde_fk", nullable = false)
	@XmlTransient
	private AbstractKunde kunde;
	
	@Transient
	private URI kundeUri;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public AbstractKunde getKunde() {
		return kunde;
	}

	public void setKunde(AbstractKunde kunde) {
		this.kunde = kunde;
	}

	public URI getKundeUri() {
		return kundeUri;
	}

	public void setKundeUri(URI kundeUri) {
		this.kundeUri = kundeUri;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 23 * hash + this.anzahl;
		hash = 23 * hash + Objects.hashCode(artikel);
		hash = 23 * hash + Objects.hashCode(artikelUri);
		hash = 23 * hash + Objects.hashCode(kunde);
		hash = 23 * hash + Objects.hashCode(kundeUri);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Warenkorbposition other = (Warenkorbposition) obj;
		if (anzahl != other.anzahl) {
			return false;
		}
		if (!Objects.equals(artikel, other.artikel)) {
			return false;
		}
		if (!Objects.equals(artikelUri, other.artikelUri)) {
			return false;
		}
		if (!Objects.equals(kunde, other.kunde)) {
			return false;
		}
		return Objects.equals(kundeUri, other.kundeUri);
	}

	@Override
	public String toString() {
		final Long artikelId = artikel == null ? null : artikel.getId();
		final Long kundeId = kunde == null ? null : kunde.getId();
		return "Warenkorbposition{" + "id=" + id + ", anzahl=" + anzahl + ", artikelId=" + artikelId + ", artikelUri="
			   + artikelUri + ", kundeId=" + kundeId + ", kundeUri=" + kundeUri + '}';
	}
	
}
