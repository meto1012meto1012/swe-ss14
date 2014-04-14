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

import java.util.Locale;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public enum TransportType {
	STRASSE("ST"),
	SCHIENE("SCH"),
	LUFT("L"),
	WASSER("W");
	
	private static final Locale LOCALE_DEFAULT = Locale.getDefault();
	
	private final String value;
	
	private TransportType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public static TransportType build(String value) {
		switch (value.toUpperCase(LOCALE_DEFAULT)) {
			case "ST":
			case "STRASSE":
				return STRASSE;
			case "SCH":
			case "SCHIENE":
				return SCHIENE;
			case "L":
			case "LUFT":
				return LUFT;
			case "W":
			case "WASSER":
				return WASSER;
			default:
				throw new RuntimeException(value + " ist kein gueltiger Wert fuer TransportType");
		}
	}
}
