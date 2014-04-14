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
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PostPersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlTransient;

import static java.util.logging.Level.FINER;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Entity
@Table(indexes = @Index(columnList = "plz"))  // Zu kunde_fk wird unten ein UNIQUE Index definiert
public class Adresse  extends AbstractVersionedAuditable {
	private static final long serialVersionUID = 4618817696314640065L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private static final int PLZ_LENGTH = 5;
	private static final int ORT_LENGTH_MIN = 2;
	private static final int ORT_LENGTH_MAX = 32;
	private static final int STRASSE_LENGTH_MIN = 2;
	private static final int STRASSE_LENGTH_MAX = 32;
	private static final int HAUSNR_LENGTH_MAX = 4;
	
	@Id
	@GeneratedValue
	@Basic(optional = false)
	@FormParam(value = "id")
	private Long id;
	
	@NotNull(message = "{adresse.plz.notNull}")
	@Pattern(regexp = "\\d{" + PLZ_LENGTH + "}", message = "{adresse.plz}")
	@Column(length = PLZ_LENGTH)
	@FormParam(value = "plz")
	private String plz;

	@NotNull(message = "{adresse.ort.notNull}")
	@Size(min = ORT_LENGTH_MIN, max = ORT_LENGTH_MAX, message = "{adresse.ort.length}")
	@FormParam(value = "ort")
	private String ort;

	@NotNull(message = "{adresse.strasse.notNull}")
	@Size(min = STRASSE_LENGTH_MIN, max = STRASSE_LENGTH_MAX, message = "{adresse.strasse.length}")
	@FormParam(value = "strasse")
	private String strasse;

	@Size(max = HAUSNR_LENGTH_MAX, message = "{adresse.hausnr.length}")
	@FormParam(value = "hausnr")
	private String hausnr;

	@OneToOne
	//NICHT @NotNull, weil beim Anlegen ueber REST der Rueckwaertsverweis noch nicht existiert
	@JoinColumn(name = "kunde_fk", nullable = false, unique = true)
	@XmlTransient
	private AbstractKunde kunde;

	public Adresse() {
		super();
	}

	public Adresse(String plz, String ort, String strasse, String hausnr) {
		super();
		this.plz = plz;
		this.ort = ort;
		this.strasse = strasse;
		this.hausnr = hausnr;
	}
	
	@PostPersist
	private void postPersist() {
		if (LOGGER.isLoggable(FINER)) {
			LOGGER.finer("Neue Adresse mit ID=" + id);
		}
	}
	
	/**
	 * {inheritDoc}
	 */
	@Override
	public void setValues(AbstractVersionedAuditable other) {
		if (!(other instanceof Adresse)) {
			return;
		}

		if (LOGGER.isLoggable(FINER)) {
			LOGGER.finer("Original-Adresse VOR setValues: " + this);
			LOGGER.finer("Zu setzende Werte VOR setValues: " + other);
		}
		super.setValues(other);
		
		final Adresse a = (Adresse) other;
		plz = a.plz;
		ort = a.ort;
		strasse = a.strasse;
		hausnr = a.hausnr;
		
		if (LOGGER.isLoggable(FINER)) {
			LOGGER.finer("Adresse NACH setValues: " + this);
		}
	}
	
	public Long getId() {
		return id;
	}

	public String getPlz() {
		return plz;
	}
	public void setPlz(String plz) {
		this.plz = plz;
	}

	public String getOrt() {
		return ort;
	}
	public void setOrt(String ort) {
		this.ort = ort;
	}

	public String getStrasse() {
		return strasse;
	}

	public void setStrasse(String strasse) {
		this.strasse = strasse;
	}

	public String getHausnr() {
		return hausnr;
	}
	public void setHausnr(String hausnr) {
		this.hausnr = hausnr;
	}

	public AbstractKunde getKunde() {
		return kunde;
	}

	public void setKunde(AbstractKunde kunde) {
		this.kunde = kunde;
	}

	@Override
	public String toString() {
		return "Adresse [id=" + id + ", plz=" + plz + ", ort=" + ort + ", strasse=" + strasse
		       + ", hausnr=" + hausnr + ", " + super.toString() + ']';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(hausnr);
		result = prime * result + Objects.hashCode(ort);
		result = prime * result + Objects.hashCode(plz);
		result = prime * result + Objects.hashCode(strasse);
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
		final Adresse other = (Adresse) obj;
		if (!Objects.equals(hausnr, other.hausnr)) {
			return false;
		}
		if (!Objects.equals(ort, other.ort)) {
			return false;
		}
		if (!Objects.equals(plz, other.plz)) {
			return false;
		}
		return Objects.equals(strasse, other.strasse);
	}
}
