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

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public enum MimeType {
	JPEG("image/jpeg"),
	PJPEG("image/pjpeg"),
	PNG("image/png"),
	MP4("video/mp4"),
	WAV("audio/wav");
	
	private final String value;

	private MimeType(String value) {
		this.value = value;
	}
	
	public static MimeType build(String value) {
		if (value == null) {
			return null;
		}
		
		switch (value.toUpperCase()) {
			case "image/jpeg":
			case "IMAGE/JPEG":
				return JPEG;
			case "image/pjpeg":
			case "IMAGE/PJPEG":
				return PJPEG;
			case "image/png":
			case "IMAGE/PNG":
				return PNG;
			case "video/mp4":
			case "VIDEO/MP4":
				return MP4;
			case "audio/wav":
			case "AUDIO/WAV":
				return WAV;
			
			default:			return null;
		}
	}
	
	public String getExtension() {
		switch (this) {
			case JPEG:	return "jpeg";
			case PJPEG:	return "jpeg";
			case PNG:	return "png";
			case MP4:	return "mp4";
			default:	throw new RuntimeException("Der MIME-Type " + this + " wird nicht unterstuetzt");
		}
	}
	
	public static MimeType buildFromExtension(String extension) {
		switch (extension) {
			case "jpeg":	return JPEG;
			case "png":		return PNG;
			case "mp4":		return MP4;
			default:	 	throw new RuntimeException("Die Extension " + extension + " wird nicht unterstuetzt");
		}
	}
	
	public MultimediaType getMultimediaType() {
		if (value.startsWith("image/")) {
			return MultimediaType.IMAGE;
		}
		if (value.startsWith("video/")) {
			return MultimediaType.VIDEO;
		}
		if (value.startsWith("audio/")) {
			return MultimediaType.AUDIO;
		}
		
		throw new RuntimeException("Der MultimediaType " + this + " wird nicht unterstuetzt");
	}
	
	@Override
	public String toString() {
		return value;
	}
}
