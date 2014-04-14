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

package de.shop.util;

import de.shop.artikelverwaltung.rest.ArtikelResource;
import de.shop.kundenverwaltung.rest.KundeResource;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.shop.util.Constants.REST_PATH;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public final class TestConstants {
	public static final String WEB_PROJEKT = "shop";
	
	public static final String BEGINN = "BEGINN";
	public static final String ENDE = "ENDE";
	
	// https
	public static final String HTTPS = "https";
	public static final String HOST = "localhost";
	public static final int PORT = 8443;
	public static final String TLS12 = "TLSv1.2";
	public static final String KEYSTORE_TYPE = "JKS";
	public static final Path TRUSTSTORE_PATH = Paths.get(System.getenv("JBOSS_HOME"), "standalone", "configuration",
			                                             "https", "client.truststore");
	public static final char[] TRUSTSTORE_PASSWORD = "zimmermann".toCharArray();

	// Basis-URI
	private static final String BASE_URI = HTTPS + "://" + HOST + ":" + PORT + "/" + WEB_PROJEKT + REST_PATH;
	
	// Pfade und Pfad-Parameter
	public static final String KUNDEN_URI = BASE_URI + "/kunden";
	public static final String KUNDEN_PRIVAT_URI = KUNDEN_URI + "/privat";
	public static final String KUNDEN_PRIVAT_UPDATE_URI = KUNDEN_URI + "/privat/update";
	public static final String KUNDEN_ID_URI = KUNDEN_URI + "/{" + KundeResource.KUNDEN_ID_PATH_PARAM + "}";
	public static final String KUNDEN_IMAGE_URI = KUNDEN_URI + "/image";
	public static final String KUNDEN_IMAGE_ID_URI = KUNDEN_IMAGE_URI + "/{" + KundeResource.KUNDEN_ID_PATH_PARAM + "}";
	
	public static final String BESTELLUNGEN_URI = BASE_URI + "/bestellungen";
	public static final String BESTELLUNGEN_ID_PATH_PARAM = "bestellungId";
	public static final String BESTELLUNGEN_ID_URI = BESTELLUNGEN_URI + "/{" + BESTELLUNGEN_ID_PATH_PARAM + "}";
	public static final String BESTELLUNGEN_ID_KUNDE_URI = BESTELLUNGEN_ID_URI + "/kunde";
	public static final String WARENKORBPOSITION_URI = BESTELLUNGEN_URI + "/warenkorbposition";
	
	public static final String ARTIKEL_URI = BASE_URI + "/artikel";
	public static final String ARTIKEL_ID_URI = ARTIKEL_URI + "/{" + ArtikelResource.ARTIKEL_ID_PATH_PARAM + "}";
	
	public static final String NOT_EXISTING_URI = BASE_URI + "/NOT_EXISTING";
	
	// Username und Password
	public static final String USERNAME_ADMIN = "101";
	public static final String PASSWORD_ADMIN = "101";
	public static final String USERNAME = "102";
	public static final String PASSWORD = "102";
	public static final String USERNAME_KUNDE = "104";
	public static final String PASSWORD_KUNDE = "104";
	public static final String PASSWORD_FALSCH = "falsch";
	
	// Index-Seite fuer Web-Tests
	public static final String INDEX_PAGE = HTTPS + "://" + HOST + ":" + PORT + "/" + WEB_PROJEKT + "/rf/index.jsf";
	
	private TestConstants() {
	}
}
