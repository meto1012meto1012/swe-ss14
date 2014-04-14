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

package de.shop.artikelverwaltung.domain;

import de.shop.util.persistence.AbstractVersionedAuditable;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PostPersist;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlRootElement;

import static java.util.logging.Level.FINEST;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@XmlRootElement
@Entity
@Table(indexes = @Index(columnList = "bezeichnung"))
@NamedQueries({
	@NamedQuery(name  = Artikel.FIND_VERFUEGBARE_ARTIKEL,
                query = "SELECT      a"
				        + " FROM     Artikel a"
                        + " WHERE    a.ausgesondert = FALSE"
				        + " ORDER BY a.id ASC"),
	@NamedQuery(name  = Artikel.FIND_ARTIKEL_BY_BEZ,
				query = "SELECT      a"
				        + " FROM     Artikel a"
						+ " WHERE    a.bezeichnung LIKE :" + Artikel.PARAM_BEZEICHNUNG
						+ "          AND a.ausgesondert = FALSE"
						+ " ORDER BY a.id ASC"),
	// Falsche Warning durch NetBeans
    @NamedQuery(name  = Artikel.FIND_LADENHUETER,
   	            query = "SELECT    a"
   	            	    + " FROM   Artikel a"
   	            	    + " WHERE  a NOT IN (SELECT bp.artikel FROM Bestellposition bp)")
})
@Cacheable
public class Artikel extends AbstractVersionedAuditable {
	private static final long serialVersionUID = -3700579190995722151L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final int BEZEICHNUNG_LENGTH_MAX = 32;
	
	private static final String PREFIX = "Artikel."; 
	public static final String FIND_VERFUEGBARE_ARTIKEL = PREFIX + "findVerfuegbareArtikel";
	public static final String FIND_ARTIKEL_BY_BEZ = PREFIX + "findArtikelByBez";
	public static final String FIND_LADENHUETER = PREFIX + "findLadenhueter";
	
	public static final String PARAM_BEZEICHNUNG = "bezeichnung";

	@Id
	@GeneratedValue
	@Basic(optional = false)
	@FormParam(value = "id")
	private Long id;
	
	@NotNull(message = "{artikel.bezeichnung.notNull}")
	@Size(max = BEZEICHNUNG_LENGTH_MAX, message = "{artikel.bezeichnung.length}")
	@FormParam(value = "bezeichnung")
	private String bezeichnung;
	
	@NotNull(message = "{artikel.preis.notNull}")
	@Digits(integer = 5, fraction = 2, message = "{artikel.preis.digits}")
	@FormParam(value = "preis")
	private BigDecimal preis;
	
	@Basic(optional = false)
	@FormParam(value = "ausgesondert")
	private boolean ausgesondert;

	@PostPersist
	private void postPersist() {
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest("Neuer Artikel mit ID=" + id);
		}
	}
	
	public Artikel() {
		super();
	}

	public Artikel(String bezeichnung, BigDecimal preis) {
		super();
		this.bezeichnung = bezeichnung;
		this.preis = preis;
	}

	public Long getId() {
		return id;
	}

	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	public BigDecimal getPreis() {
		return preis;
	}

	public void setPreis(BigDecimal preis) {
		this.preis = preis;
	}

	public boolean isAusgesondert() {
		return ausgesondert;
	}

	public void setAusgesondert(boolean ausgesondert) {
		this.ausgesondert = ausgesondert;
	}
	
	@Override
	public void setValues(AbstractVersionedAuditable newValues) {
		if (!(newValues instanceof Artikel)) {
			return;
		}
		
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest("Artikel vorher: " + this);
			LOGGER.finest("Artikel neu: " + newValues);
		}
		
		super.setValues(newValues);
		
		final Artikel newArtikel = (Artikel) newValues;
		bezeichnung = newArtikel.bezeichnung;
		preis = newArtikel.preis;
		ausgesondert = newArtikel.ausgesondert;
		
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest("Artikel nachher: " + this);
		}
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 17 * hash + Objects.hashCode(bezeichnung);
		hash = 17 * hash + Objects.hashCode(preis);
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
		final Artikel other = (Artikel) obj;
		if (!Objects.equals(bezeichnung, other.bezeichnung)) {
			return false;
		}
		if (!Objects.equals(preis, other.preis)) {
			return false;
		}
		return true;
	}



	@Override
	public String toString() {
		return "Artikel [id=" + id + ", bezeichnung=" + bezeichnung	+ ", preis=" + preis
			   + ", ausgesondert=" + ausgesondert + ", " + super.toString() + "]";
	}
}
