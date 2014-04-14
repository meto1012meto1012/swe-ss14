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

import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.persistence.AbstractVersionedAuditable;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.net.URI;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PostPersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import static java.util.logging.Level.FINER;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.EAGER;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@XmlRootElement
@Entity
@Table(indexes = {
	@Index(columnList = "kunde_fk"),
	@Index(columnList = "erzeugt")
})
@NamedQueries({
	@NamedQuery(name  = Bestellung.FIND_BESTELLUNGEN_BY_KUNDEID,
                query = "SELECT b"
				        + " FROM   Bestellung b"
			            + " WHERE  b.kunde.id = :" + Bestellung.PARAM_KUNDEID),
	@NamedQuery(name  = Bestellung.FIND_KUNDE_BY_ID,
	            query = "SELECT DISTINCT b.kunde"
	                    + " FROM   Bestellung b"
			            + " WHERE  b.id = :" + Bestellung.PARAM_ID),
	@NamedQuery(name  = Bestellung.FIND_ANZ_BESTELLUNGEN_BY_PLZ_V1,
                query = "SELECT COUNT(b)"
                        + " FROM   Bestellung b"
                        + " WHERE  b.kunde.adresse.plz BETWEEN :" + Bestellung.PARAM_PLZ_MIN
                        +        " AND :" + Bestellung.PARAM_PLZ_MAX),
   	@NamedQuery(name  = Bestellung.FIND_ANZ_BESTELLUNGEN_BY_PLZ_V2,
                query = "SELECT COUNT(b)"
                        + " FROM   AbstractKunde k JOIN k.bestellungen b"
                        + " WHERE  k.adresse.plz BETWEEN :" + Bestellung.PARAM_PLZ_MIN
                        +        " AND :" + Bestellung.PARAM_PLZ_MAX)
})
@NamedEntityGraphs({
	@NamedEntityGraph(name = Bestellung.GRAPH_LIEFERUNGEN,
					  attributeNodes = @NamedAttributeNode("lieferungen"))
})
@Cacheable
public class Bestellung  extends AbstractVersionedAuditable {
	private static final long serialVersionUID = 7560752199018702446L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final String PREFIX = "Bestellung.";
	public static final String FIND_BESTELLUNGEN_BY_KUNDEID = PREFIX + "findBestellungenByKundeId";
	public static final String FIND_KUNDE_BY_ID = PREFIX + "findKundeById";
	public static final String FIND_ANZ_BESTELLUNGEN_BY_PLZ_V1 = PREFIX + "findAnzBestellungenByPlzV1";
	public static final String FIND_ANZ_BESTELLUNGEN_BY_PLZ_V2 = PREFIX + "findAnzBestellungenByPlzV2";
	
	public static final String PARAM_KUNDEID = "kundeId";
	public static final String PARAM_ID = "id";
	public static final String PARAM_PLZ_MIN = "min";
	public static final String PARAM_PLZ_MAX = "max";
	
	public static final String GRAPH_LIEFERUNGEN = PREFIX + "lieferungen";

	@Id
	@GeneratedValue
	@Basic(optional = false)
	private Long id;

	@OneToMany(fetch = EAGER, cascade = { PERSIST, REMOVE })
	@JoinColumn(name = "bestellung_fk", nullable = false)
	@OrderColumn(name = "idx")
	@NotNull(message = "{bestellung.bestellpositionen.notEmpty}")
	@Size(min = 1, message = "{bestellung.bestellpositionen.notEmpty}")
	@Valid
	private List<Bestellposition> bestellpositionen;

	@ManyToOne
	@JoinColumn(name = "kunde_fk", nullable = false, insertable = false, updatable = false)
	//NICHT @NotNull, weil der Kunde durch den Principal implizit festgelegt ist
	//NICHT @Valid, weil der Kunde bereits vor der Bestellung existiert
	@XmlTransient
	private AbstractKunde kunde;
	
	@NotNull(message = "{bestellung.gesamtbetrag.notNull}")
	@Digits(integer = 10, fraction = 2, message = "{bestellung.gesamtbetrag.digits}")
	private BigDecimal gesamtbetrag;
	
	@Transient
	private URI kundeUri;
	
	@ManyToMany
	@JoinTable(name = "bestellung_lieferung",
			   joinColumns = @JoinColumn(name = "bestellung_fk"),
			   inverseJoinColumns = @JoinColumn(name = "lieferung_fk"))
	@XmlTransient
	private Set<Lieferung> lieferungen;
	
	@XmlElement
	public Date getDatum() {
		return getErzeugt();
	}
	
	public void setDatum(Date datum) {
		setErzeugt(datum);
	}
	
	public Bestellung() {
		super();
	}
	
	public Bestellung(List<Bestellposition> bestellpositionen) {
		super();
		this.bestellpositionen = bestellpositionen;
	}
	
	@PostPersist
	private void postPersist() {
		if (LOGGER.isLoggable(FINER)) {
			LOGGER.finer("Neue Bestellung mit ID=" + id);
		}
	}

	public Long getId() {
		return id;
	}

	public String getErzeugt(String format) {
		final Format formatter = new SimpleDateFormat(format, Locale.getDefault());
		return formatter.format(getErzeugt());
	}
	
	public List<Bestellposition> getBestellpositionen() {
		if (bestellpositionen == null) {
			return null;
		}
		
		return Collections.unmodifiableList(bestellpositionen);
	}
	
	@SuppressWarnings("null")
	public void setBestellpositionen(List<Bestellposition> bestellpositionen) {
		if (this.bestellpositionen == null) {
			this.bestellpositionen = bestellpositionen;
			return;
		}
		
		// Wiederverwendung der vorhandenen Collection
		this.bestellpositionen.clear();
		if (bestellpositionen != null) {
			this.bestellpositionen.addAll(bestellpositionen);
		}
	}
	
	public Bestellung addBestellposition(Bestellposition bestellposition) {
		if (bestellpositionen == null) {
			bestellpositionen = new ArrayList<>();
		}
		bestellpositionen.add(bestellposition);
		return this;
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

	public BigDecimal getGesamtbetrag() {
		return gesamtbetrag;
	}

	public void setGesamtbetrag(BigDecimal gesamtbetrag) {
		this.gesamtbetrag = gesamtbetrag;
	}

	public Set<Lieferung> getLieferungen() {
		return lieferungen == null ? null : Collections.unmodifiableSet(lieferungen);
	}
	
	public void setLieferungen(Set<Lieferung> lieferungen) {
		if (this.lieferungen == null) {
			this.lieferungen = lieferungen;
			return;
		}
		
		// Wiederverwendung der vorhandenen Collection
		this.lieferungen.clear();
		if (lieferungen != null) {
			this.lieferungen.addAll(lieferungen);
		}
	}
	
	public void addLieferung(Lieferung lieferung) {
		if (lieferungen == null) {
			lieferungen = new HashSet<>();
		}
		lieferungen.add(lieferung);
	}
	
	@Override
	public String toString() {
		return "Bestellung [id=" + id + ", kundeUri=" + kundeUri + ", gesamtbetrag=" + gesamtbetrag
			   + ", " + super.toString() + ']';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(bestellpositionen);
		result = prime * result + getVersion();
		result = prime * result + Objects.hashCode(getErzeugt());
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
		final Bestellung other = (Bestellung) obj;
		if (!Objects.equals(bestellpositionen, other.bestellpositionen)) {
			return false;
		}
		if (getVersion() != other.getVersion()) {
			return false;
		}
		return Objects.equals(getErzeugt(), other.getErzeugt());
	}
}
