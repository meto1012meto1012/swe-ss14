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

package de.shop.util.persistence;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.logging.Logger;
import javax.enterprise.inject.Vetoed;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import static java.util.logging.Level.FINEST;
import static javax.xml.bind.annotation.XmlAccessType.FIELD;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Entity
@Table(name = "file_tbl")   // "file" ist in Oracle kein gueltiger Tabellenname
@XmlAccessorType(FIELD)
@Vetoed
public class File  extends AbstractVersionedAuditable {
	private static final long serialVersionUID = 2632441781256478734L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	public static final int FILENAME_LENGTH_MAX = 128;
	
	@Id
	@GeneratedValue
	@Column(nullable = false, updatable = false)
	@XmlTransient
	private Long id;
	
	@Lob
	@Basic(optional = false)
	@XmlTransient
	private byte[] bytes;  // Spaltentyp OID bei PostgreSQL, BLOB bei Oracle und LONGBLOB bei MySQL
	
	@Column(length = FILENAME_LENGTH_MAX, unique = true, nullable = false)
	@XmlTransient
	private String filename;

	@Column(length = 5, nullable = false)
	@XmlTransient
	private MimeType mimeType;
	
	@Column(name = "multimedia_type", length = 1, nullable = false)
	@XmlTransient
	private MultimediaType multimediaType;
	
	public File() {
		super();
	}
	
	public File(byte[] bytes, String dateiname, MimeType mimeType) {
		super();
		set(bytes, dateiname, mimeType);
	}

	public Long getId() {
		return id;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public MimeType getMimeType() {
		return mimeType;
	}

	public void setMimeType(MimeType mimeType) {
		this.mimeType = mimeType;
	}

	public MultimediaType getMultimediaType() {
		return multimediaType;
	}

	public void setMultimediaType(MultimediaType multimediaType) {
		this.multimediaType = multimediaType;
	}

	public final void set(byte[] bytes, String filename, MimeType mimeType) {
		// Hibernate braucht den Aufruf der set-Methoden, um ein Update (Dirty-Flag!) zu erkennen
		setBytes(bytes);
		setFilename(filename);
		setMimeType(mimeType);
		setMultimediaType(mimeType.getMultimediaType());
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest("File aktualisiert: " + this.toString());
		}
	}

	@Override
	public String toString() {
		final int groesse = bytes == null ? 0 : bytes.length;
		return "File [id=" + id + ", groesse=" + groesse
			   + ", filename=" + filename + ", mimeType=" + mimeType + ", multimediaType=" + multimediaType
			   + ", " + super.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(filename);
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
		final File other = (File) obj;
		return Objects.equals(filename, other.filename);
	}
}
