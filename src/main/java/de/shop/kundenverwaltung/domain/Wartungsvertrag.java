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
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PostPersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import static java.util.logging.Level.FINER;
import static javax.persistence.TemporalType.DATE;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@XmlRootElement
@Entity
@Table(indexes = @Index(columnList = "kunde_fk"))
@NamedQueries({
	@NamedQuery(name  = Wartungsvertrag.FIND_WARTUNGSVERTRAEGE_BY_KUNDE_ID,
                query = "SELECT w"
				        + " FROM   Wartungsvertrag w"
			            + " WHERE  w.kunde.id = :" + Wartungsvertrag.PARAM_KUNDE_ID)
})
@IdClass(WartungsvertragId.class)
public class Wartungsvertrag  extends AbstractVersionedAuditable {
	private static final long serialVersionUID = -5955263122430830600L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	public static final String PREFIX = "Wartungsvertrag.";
	public static final String FIND_WARTUNGSVERTRAEGE_BY_KUNDE_ID =
		                       PREFIX + "findWartungsvertraegeByKundeId";
	public static final String PARAM_KUNDE_ID = "kundeId";

	@Id
	@Basic(optional = false)
	private long nr;

	@Id
	@NotNull(message = "{wartungsvertrag.datum.notNull}")
	@Temporal(DATE)
	private Date datum;
	
	private String inhalt;

	@NotNull(message = "{wartungsvertrag.kunde.notNull}")
	@ManyToOne(optional = false)
	@JoinColumn(name = "kunde_fk", nullable = false, insertable = false, updatable = false)
	@XmlTransient
	private AbstractKunde kunde;
	
	public Wartungsvertrag() {
		super();
	}
	
	public Wartungsvertrag(long nr, Date datum) {
		super();
		this.nr = nr;
		this.datum = datum == null ? null : (Date) datum.clone();
	}
	
	@PostPersist
	private void postPersist() {
		if (LOGGER.isLoggable(FINER)) {
			LOGGER.finer("Neuer Wartungsvertrag mit Nr=" + nr + "/Datum=" + datum);
		}
	}

	public long getNr() {
		return nr;
	}

	public void setNr(long nr) {
		this.nr = nr;
	}

	public Date getDatum() {
		return datum == null ? null : (Date) datum.clone();
	}

	public void setDatum(Date datum) {
		this.datum = datum == null ? null : (Date) datum.clone();
	}

	public String getInhalt() {
		return inhalt;
	}

	public void setInhalt(String inhalt) {
		this.inhalt = inhalt;
	}

	public AbstractKunde getKunde() {
		return kunde;
	}

	public void setKunde(AbstractKunde kunde) {
		this.kunde = kunde;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(datum);
		result = prime * result + (int) (nr ^ (nr >>> 32));
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
		final Wartungsvertrag other = (Wartungsvertrag) obj;
		if (!Objects.equals(datum, other.datum)) {
			return false;
		}
		return nr != other.nr;
	}

	@Override
	public String toString() {
		return "Wartungsvertrag [nr=" + nr + ", datum=" + datum + ", inhalt=" + inhalt + ", " + super.toString() + ']';
	}
}
