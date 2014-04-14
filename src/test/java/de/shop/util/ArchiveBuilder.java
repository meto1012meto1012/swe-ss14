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

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

import static de.shop.util.TestConstants.WEB_PROJEKT;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public enum ArchiveBuilder {
	INSTANCE;
	
	static final String TEST_WAR = WEB_PROJEKT + ".war";
	
	private static final String CLASSES_DIR = "target/classes";
	private static final String WEBAPP_DIR = "src/main/webapp";
	
	private final WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_WAR);

	/**
	 */
	private ArchiveBuilder() {
		addManifestMf();
		addWebInfWebseiten();		
		addJars();
		addKlassen();
	}

	private void addManifestMf() {
		// Zeilen in META-INF\Manifest.mf duerfen laut Java-Spezifikation nur 69 Zeichen breit sein
		// Umgebrochene Zeilen muessen mit einem Leerzeichen beginnen
		archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                                           + "Dependencies: org.jboss.jts services export,org.jboss.as.controller-cl\n"
				                           + " ient,org.jboss.dmr\n"));
		
	}
	
	private void addWebInfWebseiten() {
		// XML-Konfigurationsdateien und Webseiten als JAR einlesen
		final GenericArchive tmp = ShrinkWrap.create(GenericArchive.class)
				                             .as(ExplodedImporter.class)
				                             .importDirectory(WEBAPP_DIR)
				                             .as(GenericArchive.class);
		archive.merge(tmp, "/");
	}
	
	private void addJars() {
		// http://exitcondition.alrubinger.com/2012/09/13/shrinkwrap-resolver-new-api
		final PomEquippedResolveStage pomResolver = Maven.resolver().offline().loadPomFromFile("pom.xml");
		archive.addAsLibraries(pomResolver.resolve("org.richfaces:richfaces")
				                          .withTransitivity()
				                          .asFile());
	}
	
	private void addKlassen() {
		final GenericArchive tmp = ShrinkWrap.create(GenericArchive.class)
				                             .as(ExplodedImporter.class)
				                             .importDirectory(CLASSES_DIR)
				                             .as(GenericArchive.class);
		archive.merge(tmp, "WEB-INF/classes");
	}
	
	public static ArchiveBuilder getInstance() {
		return INSTANCE;
	}
	
	public Archive<?> getArchive(Class<?>... testklassen) {
		archive.addClasses(testklassen);
		return archive;
	}
}
