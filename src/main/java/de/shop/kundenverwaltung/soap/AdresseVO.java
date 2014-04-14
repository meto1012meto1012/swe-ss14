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

import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.util.persistence.AbstractVersionedAuditable;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Value Object (VO) fuer die Domain-Klasse Adresse
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class AdresseVO implements Serializable {
	private static final long serialVersionUID = -8723846420081337740L;
	
	private Long id;
	private int version;
	private String plz;
	private String ort;
	private String strasse;
	private String hausnr;
	
	public AdresseVO() {
		super();
	}
	
	public AdresseVO(Adresse adresse) {
		super();
		
		id = adresse.getId();
		version = adresse.getVersion();
		plz = adresse.getPlz();
		ort = adresse.getOrt();
		strasse = adresse.getStrasse();
		hausnr = adresse.getHausnr();
	}
	
	public Adresse toAdresse() {
		final Adresse adresse = new Adresse(plz, ort, strasse, hausnr);
		
		// Die private Attribute "id" und "version" setzen, ohne dass es eine set-Methode gibt
		try {
			if (id != null) {
				final Field idField = Adresse.class.getDeclaredField("id");
				idField.setAccessible(true);
				idField.set(adresse, id);
			}
			final Field versionField = AbstractVersionedAuditable.class.getDeclaredField("version");
			versionField.setAccessible(true);
			versionField.setInt(adresse, version);
		}
		catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
		
		return adresse;
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
		final AdresseVO other = (AdresseVO) obj;
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
		return "AdresseVO [id=" + id + ", version=" + version + ", plz=" + plz
				+ ", ort=" + ort + ", strasse=" + strasse + ", hausnr="
				+ hausnr + "]";
	}
}
