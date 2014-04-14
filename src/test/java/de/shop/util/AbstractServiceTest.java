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

import de.shop.bestellverwaltung.business.BestellungServiceTest;
import de.shop.kundenverwaltung.business.KundeServiceConcurrencyHelper;
import de.shop.kundenverwaltung.business.KundeServiceConcurrencyTest;
import de.shop.kundenverwaltung.business.KundeServiceTest;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.security.auth.login.LoginException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import static javax.transaction.Status.STATUS_ACTIVE;
import static javax.transaction.Status.STATUS_MARKED_ROLLBACK;
import static javax.transaction.Status.STATUS_NO_TRANSACTION;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public abstract class AbstractServiceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	// Testklassen fuer Service--Tests
	private static final Class<?>[] TEST_CLASSES = { // Service-Tests
                                                     AbstractServiceTest.class,
                                                     KundeServiceTest.class,
                                                     KundeServiceConcurrencyHelper.class,
                                                     KundeServiceConcurrencyTest.class,
                                                     BestellungServiceTest.class, };
	
	@Inject
	private UserTransaction trans;
	
	private SecurityClient securityClient;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();   // public wegen JUnit
	
	@Deployment
	@OverProtocol(value = "Servlet 3.0")  // https://docs.jboss.org/author/display/ARQ/Servlet+3.0
	protected static Archive<?> deployment() {
		return ArchiveBuilder.getInstance().getArchive(TEST_CLASSES);
	}
	
	@Before
	public void before() throws Exception {
		int status;
		try {
			status = trans.getStatus();
		}
		catch (IllegalStateException e) {
			assertThat(e.getMessage(), is("UserTransaction is not available within the scope of a bean or method annotated with @Transactional and a Transactional.TxType other than NOT_SUPPORTED or NEVER"));
			return;
		}
		assertThat(status, is(STATUS_NO_TRANSACTION));
		
		securityClient = SecurityClientFactory.getSecurityClient();
	}
	
	@After
	public void after() throws SystemException {
		int status;
		try {
			status = trans.getStatus();
		}
		catch (IllegalStateException e) {
			assertThat(e.getMessage(), is("UserTransaction is not available within the scope of a bean or method annotated with @Transactional and a Transactional.TxType other than NOT_SUPPORTED or NEVER"));
			return;
		}
		
		switch (status) {
			case STATUS_ACTIVE:
				// In einer Testmethode wurde eine Exception geworfen und dadurch vorzeitig beendet
				LOGGER.finer("after(): UserTransaction.getStatus() == STATUS_ACTIVE");
				trans.rollback();
				break;
				
			case STATUS_MARKED_ROLLBACK:
				// In einer Testmethode wurde eine Exception mit @ApplicationException fuer EJBs geworfen
				// und dadurch vorzeitig beendet
				LOGGER.finer("after(): UserTransaction.getStatus() == STATUS_MARKED_ROLLBACK");
				trans.rollback();
				break;
		}

		assertThat(trans.getStatus(), is(STATUS_NO_TRANSACTION));
	}
	
	protected UserTransaction getUserTransaction() {
		return trans;
	}
	
	protected void login(String username, String password) {
		securityClient.setSimple(username, password);
		try {
			securityClient.login();
		}
		catch (LoginException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void logout() {
		securityClient.logout();
	}
}
