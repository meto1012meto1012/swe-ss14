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

package de.shop.kundenverwaltung.business;

import de.shop.kundenverwaltung.business.KundeService.FetchType;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.inject.Inject;
import javax.security.auth.login.LoginException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;

import static de.shop.util.TestConstants.PASSWORD_ADMIN;
import static de.shop.util.TestConstants.USERNAME_ADMIN;
import static javax.ejb.TransactionManagementType.BEAN;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Stateful
@TransactionManagement(BEAN)
public class KundeServiceConcurrencyHelper {
	@Inject
	private KundeService ks;
	
	@Inject
	private UserTransaction trans;
	
	private SecurityClient securityClient;
	
	@PostConstruct
	private void postConstruct() {
		try {
			securityClient = SecurityClientFactory.getSecurityClient();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Asynchronous
	public Future<AbstractKunde> update(Long kundeId) throws NotSupportedException, SystemException,
	                                                         HeuristicRollbackException, HeuristicMixedException,
	                                                         RollbackException {
		
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		login(username, password);
		
		trans.begin();
		AbstractKunde kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		kunde.setVorname(kunde.getVorname() + "concurrent");
		ks.updateKunde(kunde, false);
		trans.commit();
		
		// Kunde erneut lesen, um die erhoehte Versionsnummer zu ermitteln
		trans.begin();
		kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		trans.commit();
		
		logout();
		return new AsyncResult<>(kunde);
	}
	
	@Asynchronous
	public Future<Boolean> delete(Long kundeId) throws NotSupportedException, SystemException,
			                                           HeuristicRollbackException, HeuristicMixedException,
													   RollbackException {
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		login(username, password);

		trans.begin();
		ks.deleteKundeById(kundeId);
		trans.commit();
		
		logout();
		return new AsyncResult<>(Boolean.TRUE);
	}
	
	private void login(String username, String password) {
		securityClient.setSimple(username, password);
		try {
			securityClient.login();
		}
		catch (LoginException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void logout() {
		securityClient.logout();
	}
}
