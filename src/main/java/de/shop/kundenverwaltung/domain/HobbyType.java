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
public enum HobbyType {
	SPORT("S"),
	LESEN("L"),
	REISEN("R");
	
	private static final Locale LOCALE_DEFAULT = Locale.getDefault();
	
	private final String value;
	
	private HobbyType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public static HobbyType build(String value) {
		if (value == null) {
			return null;
		}
		
		switch (value.toUpperCase(LOCALE_DEFAULT)) {
			case "S":
			case "SPORT":
				return SPORT;
			case "L":
			case "LESEN":
				return LESEN;
			case "R":
			case "REISEN":
				return REISEN;
			default:
				throw new RuntimeException(value + " ist kein gueltiger Wert fuer HobbyType");
		}
	}
}
