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

package de.shop.auth.domain;

import java.util.Locale;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public enum RolleType {
	ADMIN(RolleType.ADMIN_STRING),
	MITARBEITER(RolleType.MITARBEITER_STRING),
	ABTEILUNGSLEITER(RolleType.ABTEILUNGSLEITER_STRING),
	KUNDE(RolleType.KUNDE_STRING);
	
	public static final String ADMIN_STRING = "admin";
	public static final String MITARBEITER_STRING = "mitarbeiter";
	public static final String ABTEILUNGSLEITER_STRING = "abteilungsleiter";
	public static final String KUNDE_STRING = "kunde";
	
	private static final Locale LOCALE_DEFAULT = Locale.getDefault();
	
	private final String value;
	
	private RolleType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public static RolleType build(String value) {
		if (value == null) {
			return null;
		}
		
		return RolleType.valueOf(value.toUpperCase(LOCALE_DEFAULT));
	}
}
