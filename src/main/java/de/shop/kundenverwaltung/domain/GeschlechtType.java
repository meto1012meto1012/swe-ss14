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

import java.util.Locale;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public enum GeschlechtType {
	MAENNLICH("M"),
	WEIBLICH("W");
	
	private static final Locale LOCALE_DEFAULT = Locale.getDefault();
	
	private final String value;
	
	private GeschlechtType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public static GeschlechtType build(String value) {
		if (value == null) {
			return null;
		}
		
		switch (value.toUpperCase(LOCALE_DEFAULT)) {
			case "M":
			case "MAENNLICH":
				return MAENNLICH;
			case "W":
			case "WEIBLICH":
				return WEIBLICH;
			default:
				throw new RuntimeException(value + " ist kein gueltiger Wert fuer GeschlechtType");
		}
	}
}
