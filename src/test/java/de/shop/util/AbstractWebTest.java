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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;

import static de.shop.util.AbstractPage.sleep;
import static org.jboss.arquillian.graphene.Graphene.goTo;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public abstract class AbstractWebTest {
	private static final int DELAY = 1;    // Browser soll noch 1 Sek nach einer Testmethode erhalten bleiben
	
	@Drone
	protected WebDriver browser;           // Treiber fuer Webbrowser: Firefox, Chrome, ...
	
	@Page
	protected IndexPage indexPage;
	
	@Deployment(name = ArchiveBuilder.TEST_WAR, testable = false) // Tests laufen nicht im Container
	@OverProtocol(value = "Servlet 3.0")  // https://docs.jboss.org/author/display/ARQ/Servlet+3.0
	protected static Archive<?> deployment() {
		return ArchiveBuilder.getInstance().getArchive();
	}
	
	@Before
	public void before() {
		browser.manage().deleteAllCookies();  // alle Cookies loeschen
		goTo(IndexPage.class);
	}
	
	@After
	public void after() {
		sleep(DELAY);
		
		indexPage.logout(browser);
	}
}
