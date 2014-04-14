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

package de.shop.auth.business;

import com.google.common.base.Strings;
import de.shop.auth.domain.RolleType;
import de.shop.kundenverwaltung.business.KundeService;
import de.shop.kundenverwaltung.business.KundeService.FetchType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.security.SimpleGroup;

import static de.shop.auth.domain.RolleType.ADMIN_STRING;
import static de.shop.auth.domain.RolleType.KUNDE_STRING;
import static de.shop.auth.domain.RolleType.MITARBEITER_STRING;
import static de.shop.kundenverwaltung.domain.AbstractKunde.FIND_USERNAME_BY_USERNAME_PREFIX;
import static de.shop.kundenverwaltung.domain.AbstractKunde.PARAM_USERNAME_PREFIX;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Stateless
public class AuthService implements Serializable {
	private static final long serialVersionUID = -2736040689592627172L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static final String HASH_ALGORITHM = "SHA-256";
	
	private static final String SECURITY_DOMAIN = "shop";
	private static final String LOCALHOST = "localhost";
	private static final int MANAGEMENT_PORT = 9990;  // JBossAS hatte den Management-Port 9999
	
	@Inject
	private Principal principal;
	
	@Resource
	private SessionContext ctx;
	
	@Inject
	private transient EntityManager em;
	
	@EJB    // KundeService hat @Inject mit AuthService
	private KundeService ks;


	/**
	 * @param password zu verschluesselndes Password
	 * @return Verschluesseltes Password, d.h. der berechnete Hashwert
	 */
	public String verschluesseln(String password) {
		if (password == null) {
			return null;
		}

		final MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		messageDigest.update(password.getBytes());
		final byte[] passwordHash = messageDigest.digest();

		return Base64.getEncoder().encodeToString(passwordHash);
		
		// Alternativ mit JAXB
		//return DatatypeConverter.printBase64Binary(passwordHash);   // HEX durch DatatypeConverter.printHexBinary()
		
		// Alternativ mit WildFly: org.jboss.security.auth.spi.Util
		// return createPasswordHash(HASH_ALGORITHM, "base64", "UTF-8", null, password);
	}
	
	/**
	 * Zu einem Kunden neue Rollen hinzufuegen
	 * @param kundeId ID des betroffenen Kunden
	 * @param rollen Neue Rollen
	 */
	public void addRollen(Long kundeId, Collection<RolleType> rollen) {
		if (rollen == null || rollen.isEmpty()) {
			return;
		}

		ks.findKundeById(kundeId, FetchType.NUR_KUNDE)
		  .addRollen(rollen);
		flushSecurityCache(kundeId.toString());
	}

	/**
	 * Von einem Kunden Rollen wegnehmen
	 * @param kundeId ID des betroffenen Kunden
	 * @param rollen Die wegzunehmenden Rollen
	 */
	public void removeRollen(Long kundeId, Collection<RolleType> rollen) {
		if (rollen == null || rollen.isEmpty()) {
			return;
		}

		ks.findKundeById(kundeId, FetchType.NUR_KUNDE)
		  .removeRollen(rollen);
		flushSecurityCache(kundeId.toString());
	}
	
	/*
	 * siehe http://community.jboss.org/thread/169263
	 * siehe https://docs.jboss.org/author/display/AS7/Management+Clients
	 * siehe https://github.com/jbossas/jboss-as/blob/master/controller-client/src/main/java/org/jboss/as/controller/client/ModelControllerClient.java
	 * siehe http://community.jboss.org/wiki/FormatOfADetypedOperationRequest
	 * siehe http://community.jboss.org/wiki/DetypedDescriptionOfTheAS7ManagementModel
	 * 
	 * Gleicher Ablauf mit CLI (= command line interface):
	 * cd %JBOSS_HOME%\bin
	 * jboss-cli.bat -c --command=/subsystem=security/security-domain=shop:flush-cache(principal=myUserName)
	 */
	private static void flushSecurityCache(String username) {
		// ModelControllerClient ist abgeleitet vom Interface Autoclosable
		try (final ModelControllerClient client = ModelControllerClient.Factory.create(LOCALHOST, MANAGEMENT_PORT)) {
			final ModelNode address = new ModelNode();
			address.add("subsystem", "security");
			address.add("security-domain", SECURITY_DOMAIN);

			final ModelNode operation = new ModelNode();
			operation.get("address").set(address);
			operation.get("operation").set("flush-cache");
			operation.get("principal").set(username);

			final ModelNode result = client.execute(operation);
			final String resultString = result.get("outcome").asString();
			if (!"success".equals(resultString)) {
				throw new RuntimeException("FEHLER bei der Operation \"flush-cache\" fuer den Security-Cache: "
						                   + resultString);
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void checkSameUser(String usernameRequired) {
		// Falls nur die Rolle "kunde" vorhanden ist, dann darf nur nach dem eigenen Usernamen gesucht werden
		
		if (ctx.isCallerInRole(ADMIN_STRING) || ctx.isCallerInRole(MITARBEITER_STRING)) {
			if (LOGGER.isLoggable(FINEST)) {
				LOGGER.finest("Eingeloggt in der Rolle " + ADMIN_STRING + " oder " + MITARBEITER_STRING);
			}
			return;
		}

		if (ctx.isCallerInRole(KUNDE_STRING)) {
			if (LOGGER.isLoggable(FINEST)) {
				LOGGER.finest("Eingeloggt in der Rolle " + KUNDE_STRING);
			}
			final String username = principal.getName();
			if (LOGGER.isLoggable(FINEST)) {
				LOGGER.finest("Username=" + username);
			}
			if (!Objects.equals(username, usernameRequired)) {
				throw new EJBAccessException();
			}
		}
	}
	
	public void checkSameUser(Long id) {
		// Falls nur die Rolle "kunde" vorhanden ist, dann darf nur nach dem eigenen Usernamen gesucht werden
		if (id == null) {
			throw new EJBAccessException();
		}
		
		checkSameUser(id.toString());
	}

	
	public void checkAdminMitarbeiter() {
		if (ctx.isCallerInRole(ADMIN_STRING) || ctx.isCallerInRole(MITARBEITER_STRING)) {
			return;
		}
		
		throw new EJBAccessException();
	}

	/**
	 * Zu einem Praefix alle passenden Usernamen ermitteln
	 * @param usernamePrefix Gemeinsamer Praefix fuer potenzielle Usernamen 
	 * @return Liste der potenziellen Usernamen
	 */
	public List<String> findUsernameListByUsernamePrefix(String usernamePrefix) {
		return em.createNamedQuery(FIND_USERNAME_BY_USERNAME_PREFIX, String.class)
				 .setParameter(PARAM_USERNAME_PREFIX, usernamePrefix + '%')
				 .getResultList();
	}
	

	public static void main(String[] args) throws IOException {
		if (args.length > 1) {
			System.out.println("Usage: java " + AuthService.class.getName() + " [password]");
			System.exit(-1);
		}
		
		if (args.length == 1) {
			final String passwordHash = new AuthService().verschluesseln(args[0]);
			System.out.println("Verschluesselt: " + passwordHash);
			return;
		}
		
		try (final BufferedReader reader = new BufferedReader(
					                           new InputStreamReader(System.in, Charset.defaultCharset()));) {
			for (;;) {
				System.out.print("Password (Abbruch durch <Return>): ");

				final String password = reader.readLine();
				if (Strings.isNullOrEmpty(password)) {
					break;
				}
				final String passwordHash = new AuthService().verschluesseln(password);
				System.out.println("Verschluesselt: " + passwordHash + System.getProperty("line.separator"));
			}
		}
		
		System.out.println("FERTIG");
	}
	
	/**
	 * Rollen zum eingeloggten User ermitteln
	 * @return Liste der Rollen des eingeloggten Users
	 */
	public List<RolleType> getEigeneRollen() {
		// Authentifiziertes Subject mittels JACC ermitteln
		
		// Caveat: bei Oracle WebLogic und IBM WebSphere ist JACC defaultmaessig AUSGESCHALTET
		//         Ausserdem enthaelt IBM WebSphere *keinen* JACC Provider: man muss Tivoli Access Manager installieren
		Subject subject = null;
		try {
			subject = Subject.class.cast(PolicyContext.getContext("javax.security.auth.Subject.container"));
			//IBM WebSPhere ohne Tivoli Access Manager:
			//subject = com.ibm.websphere.security.auth.WSSubject.getCallerSubject();
		}
		catch (PolicyContextException e) {
			LOGGER.log(SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
		if (subject == null) {
			return null;
		}

		// WildFly: Es gibt 2 Arten von Principals:
		//          1) Principals mit Namen "CallerPrincipal", d.h. eingeloggte User
		//          2) Principals mit Namen "Roles", d.h. Rollen fuer die eingeloggten User
		// In beiden Faellen implementiert die Klasse 'SimpleGroup' das Interface 'Principal'
		// Die Klasse 'SimpleGroup' hat die Methode members(), welche eine Enumeration mit Principals
		// von der Klasse 'SimplePrincipal' liefert.
		// Diese haben entweder den Namen eines eingeloggten Users oder den Namen einer zugeordneten Rolle:
		//   Principals
		//      org.jboss.security.SimpleGroup (name = "CallerPrincipal")
		//         members
		//            org.jboss.security.SimplePrincipal (name = "joe.doe")
		//      org.jboss.security.SimpleGroup (name  = "Roles")
		//         members
		//            org.jboss.security.SimplePrincipal (name = "kunde")
		final List<RolleType> rollen = new LinkedList<>();
		subject.getPrincipals()
			   .stream()
			   .filter(p -> p instanceof SimpleGroup && "Roles".equals(p.getName()))
			   .forEach(sg -> {
					// Rollen als String ermitteln und in RolleType konvertieren
					Collections.list(((SimpleGroup) sg).members())
							   .stream()
							   .map(Principal::getName)
							   .map(RolleType::build)
							   .forEach(rolle -> {
						rollen.add(rolle);
					});
		});
		
		// Oracle WebLogic: zum Interface 'Principal' gibt es die beiden Klassen 'WLSUserImpl' und 'WLSGroupImpl'
		// IBM WebSphere: zum Interface 'Principal' gibt es die die Klasse 'WSPrincipalImpl' und
		//                zum Interface 'PublicCredential' gibt es weitere Klassen ...
		
		return rollen;
	}
}
