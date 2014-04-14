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

import javax.xml.ws.BindingProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.BeforeClass;

import static de.shop.util.TestConstants.KEYSTORE_TYPE;
import static de.shop.util.TestConstants.TRUSTSTORE_PATH;
import static de.shop.util.TestConstants.TRUSTSTORE_PASSWORD;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public abstract class AbstractSoapTest {
	@Deployment(name = ArchiveBuilder.TEST_WAR, testable = false)  // Die Tests laufen nicht im Container
	@OverProtocol(value = "Servlet 3.0")  // https://docs.jboss.org/author/display/ARQ/Servlet+3.0
	protected static Archive<?> deployment() {
		return ArchiveBuilder.getInstance().getArchive();
	}
	
	protected void login(String username, String password, Object proxy) {
		final BindingProvider bp = BindingProvider.class.cast(proxy);
		bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
		bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
	}
	
	@BeforeClass
	public static void init() {
		System.setProperty("javax.net.ssl.trustStore", TRUSTSTORE_PATH.toString());
		System.setProperty("javax.net.ssl.trustStorePassword", String.valueOf(TRUSTSTORE_PASSWORD));
		System.setProperty("javax.net.ssl.trustStoreType", KEYSTORE_TYPE);
	}
}
