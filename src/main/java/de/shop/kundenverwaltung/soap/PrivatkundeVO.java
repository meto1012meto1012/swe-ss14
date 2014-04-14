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

package de.shop.kundenverwaltung.soap;

import de.shop.auth.domain.RolleType;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.FamilienstandType;
import de.shop.kundenverwaltung.domain.GeschlechtType;
import de.shop.kundenverwaltung.domain.HobbyType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.util.persistence.AbstractVersionedAuditable;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Value Object (VO) fuer die Domain-Klasse Privatkunde
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class PrivatkundeVO implements Serializable {
	private static final long serialVersionUID = 4239572011880707969L;
	
	private Long id;
	private int version;
	private String nachname;
	private String vorname;
	private int kategorie;
	private BigDecimal rabatt;
	private BigDecimal umsatz;
	private Date seit;
	private String email;
	private String password;
	private boolean agbAkzeptiert;
	private AdresseVO adresse;
	private List<Long> bestellungenIds;
	private Set<RolleType> rollen;
	private String bemerkungen;
	
	private FamilienstandType familienstand;
	private GeschlechtType geschlecht;
	private Set<HobbyType> hobbies;
	
	public PrivatkundeVO() {
		super();
	}
	
	public PrivatkundeVO(Privatkunde pk) {
		super();
		id = pk.getId();
		version = pk.getVersion();
		nachname = pk.getNachname();
		vorname = pk.getVorname();
		kategorie = pk.getKategorie();
		rabatt = pk.getRabatt();
		umsatz = pk.getUmsatz();
		seit = pk.getSeit();
		email = pk.getEmail();
		password = pk.getPassword();
		agbAkzeptiert = pk.isAgbAkzeptiert();
		adresse = new AdresseVO(pk.getAdresse());
		final List<Bestellung> bestellungen = pk.getBestellungen();
		if (bestellungen != null) {
			bestellungenIds = bestellungen.parallelStream()
			                              .map(Bestellung::getId)
			                              .collect(Collectors.toList());
		}
		rollen = pk.getRollen();
		bemerkungen = pk.getBemerkungen();
		familienstand = pk.getFamilienstand();
		geschlecht = pk.getGeschlecht();
		hobbies = pk.getHobbies();
	}
	
	public Privatkunde toPrivatkunde() {
		final Privatkunde pk = new Privatkunde(nachname, vorname, email, seit);

		// Die private Attribute "id" und "version" setzen, ohne dass es eine set-Methode gibt
		try {
			if (id != null) {
				final Field idField = AbstractKunde.class.getDeclaredField("id");
				idField.setAccessible(true);
				idField.set(pk, id);
			}
			final Field versionField = AbstractVersionedAuditable.class.getDeclaredField("version");
			versionField.setAccessible(true);
			versionField.setInt(pk, version);
		}
		catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
		
		pk.setKategorie(kategorie);
		pk.setRabatt(rabatt);
		pk.setUmsatz(umsatz);
		pk.setPassword(password);
		pk.setPasswordWdh(password);
		pk.setAgbAkzeptiert(agbAkzeptiert);
		pk.setAdresse(adresse.toAdresse());
		pk.getAdresse().setKunde(pk);
		// TODO SOAP: Bestellungen aus Bestellungen-IDs ermitteln
		pk.setRollen(rollen);
		pk.setBemerkungen(bemerkungen);
		pk.setFamilienstand(familienstand);
		pk.setGeschlecht(geschlecht);
		pk.setHobbies(hobbies);
		return pk;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getNachname() {
		return nachname;
	}
	public void setNachname(String nachname) {
		this.nachname = nachname;
	}
	public String getVorname() {
		return vorname;
	}
	public void setVorname(String vorname) {
		this.vorname = vorname;
	}
	public int getKategorie() {
		return kategorie;
	}
	public void setKategorie(int kategorie) {
		this.kategorie = kategorie;
	}
	public BigDecimal getRabatt() {
		return rabatt;
	}
	public void setRabatt(BigDecimal rabatt) {
		this.rabatt = rabatt;
	}
	public BigDecimal getUmsatz() {
		return umsatz;
	}
	public void setUmsatz(BigDecimal umsatz) {
		this.umsatz = umsatz;
	}
	public Date getSeit() {
		return seit == null ? null : (Date) seit.clone();
	}
	public void setSeit(Date seit) {
		this.seit = seit == null ? null : (Date) seit.clone();
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isAgbAkzeptiert() {
		return agbAkzeptiert;
	}
	public void setAgbAkzeptiert(boolean agbAkzeptiert) {
		this.agbAkzeptiert = agbAkzeptiert;
	}
	public AdresseVO getAdresse() {
		return adresse;
	}
	public void setAdresse(AdresseVO adresse) {
		this.adresse = adresse;
	}
	public List<Long> getBestellungenIds() {
		return bestellungenIds;
	}
	public void setBestellungenIds(List<Long> bestellungenIds) {
		this.bestellungenIds = bestellungenIds;
	}
	public Set<RolleType> getRollen() {
		return rollen;
	}
	public void setRollen(Set<RolleType> rollen) {
		this.rollen = rollen;
	}
	public String getBemerkungen() {
		return bemerkungen;
	}
	public void setBemerkungen(String bemerkungen) {
		this.bemerkungen = bemerkungen;
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
		return hobbies;
	}
	public void setHobbies(Set<HobbyType> hobbies) {
		this.hobbies = hobbies;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PrivatkundeVO other = (PrivatkundeVO) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "KundeVO [id=" + id + ", version=" + version + ", nachname="
				+ nachname + ", vorname=" + vorname + ", kategorie="
				+ kategorie + ", rabatt=" + rabatt + ", umsatz=" + umsatz
				+ ", seit=" + seit + ", email=" + email + ", password="
				+ password + ", agbAkzeptiert=" + agbAkzeptiert
				+ ", bestellungenIds=" + bestellungenIds + ", rollen=" + rollen
				+ ", bemerkungen=" + bemerkungen + ", familienstand="
				+ familienstand + ", geschlecht=" + geschlecht + ", hobbies="
				+ hobbies + "]";
	}
}
