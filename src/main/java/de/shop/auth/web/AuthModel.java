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

package de.shop.auth.web;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import de.shop.auth.business.AuthService;
import de.shop.auth.domain.RolleType;
import de.shop.kundenverwaltung.business.KundeService;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.web.Client;
import de.shop.util.web.Messages;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static de.shop.kundenverwaltung.business.KundeService.FetchType.MIT_ROLLEN;
import static de.shop.kundenverwaltung.business.KundeService.FetchType.NUR_KUNDE;
import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;
import static java.util.logging.Level.FINEST;


/**
 * Dialogsteuerung f&uuml;r Authentifizierung (Login und Logout) und Authorisierung (rollenbasierte Berechtigungen).
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@SessionScoped
@Stateful
public class AuthModel implements Serializable {
	private static final long serialVersionUID = -8604525347843804815L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final String MSG_KEY_LOGIN_ERROR = "auth.login.error";
	private static final String CLIENT_ID_USERNAME = "usernameHeader";
	private static final String MSG_KEY_UPDATE_ROLLEN_KEIN_USER = "kunde.notFound.username";
	private static final String CLIENT_ID_USERNAME_INPUT = "usernameInput";
	
	private String username;
	private String password;
	
	private AbstractKunde user;
	
	private String usernameUpdateRollen;
	private Long kundeId;
	
	private List<RolleType> ausgewaehlteRollen;
	private List<RolleType> ausgewaehlteRollenOrig;
	private List<RolleType> verfuegbareRollen;

	@Inject
	private Principal principal;
	
	@Inject
	private KundeService ks;
	
	@Inject
	private AuthService authService;
	
	@Inject
	@Client
	private Locale locale;
	
	@Inject
	private Messages messages;
	
	@Inject
	private transient HttpServletRequest request;
	
	@Inject
	private transient HttpSession session;
	
	@Override
	public String toString() {
		return "AuthModel [username=" + username + ", password=" + password + ", user=" + user
			   + ", ausgewaehlteRollenOrig=" + ausgewaehlteRollenOrig + ", ausgewaehlteRollen="
			   + ausgewaehlteRollen + ", verfuegbareRollen=" + verfuegbareRollen + "]";
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Produces
	@SessionScoped
	@KundeLoggedIn
	public AbstractKunde getUser() {
		return user;
	}
	public String getUsernameUpdateRollen() {
		return usernameUpdateRollen;
	}

	public void setUsernameUpdateRollen(String usernameUpdateRollen) {
		this.usernameUpdateRollen = usernameUpdateRollen;
	}

	public List<RolleType> getAusgewaehlteRollen() {
		return ausgewaehlteRollen;
	}

	public void setAusgewaehlteRollen(List<RolleType> ausgewaehlteRollen) {
		this.ausgewaehlteRollen = ausgewaehlteRollen;
	}

	public List<RolleType> getVerfuegbareRollen() {
		return verfuegbareRollen;
	}

	public void setVerfuegbareRollen(List<RolleType> verfuegbareRollen) {
		this.verfuegbareRollen = verfuegbareRollen;
	}

	/**
	 * Einloggen eines registrierten Kunden mit Benutzername und Password.
	 * @return Pfad zur aktuellen Seite fuer Refresh
	 */
	public String login() {
		if (Strings.isNullOrEmpty(username)) {
			reset();
			messages.error(MSG_KEY_LOGIN_ERROR, locale, CLIENT_ID_USERNAME);
			return null;   // Gleiche Seite nochmals aufrufen: mit den fehlerhaften Werten
		}
		
		try {
			request.login(username, password);
		}
		catch (ServletException e) {
			if (LOGGER.isLoggable(FINEST)) {
				LOGGER.finest("username=" + username + ", password=" + password);
			}
			reset();
			messages.error(MSG_KEY_LOGIN_ERROR, locale, CLIENT_ID_USERNAME);
			return null;   // Gleiche Seite nochmals aufrufen: mit den fehlerhaften Werten
		}
		
		user = ks.findKundeByUserName(username, NUR_KUNDE);
		if (user == null) {
			logout();
			throw new RuntimeException("Kein Kunde mit dem Loginnamen \"" + username + "\" gefunden");
		}
		
		return null;
	}

	/**
	 * Einloggen eines gerade registrierten Kunden mit Benutzername und Password.
	 * @param username Benutzername
	 * @param password Passwort
	 */
	public void login(String username, String password) {
		this.username = username;
		this.password = password;
		login();
	}
	
	/**
	 * Nachtraegliche Einloggen eines registrierten Kunden mit Benutzername und Password.
	 */
	public void preserveLogin() {
		if (username != null && user != null) {
			return;
		}
		
		// Benutzername beim Login ermitteln
		username = principal.getName();

		user = ks.findKundeByUserName(username, NUR_KUNDE);
		if (user == null) {
			// Darf nicht passieren, wenn unmittelbar zuvor das Login erfolgreich war
			logout();
			throw new RuntimeException("Kein Kunde mit dem Loginnamen \"" + username + "\" gefunden");
		}
	}


	/**
	 */
	private void reset() {
		username = null;
		password = null;
		user = null;
	}

	
	/**
	 * Ausloggen und L&ouml;schen der gesamten Session-Daten.
	 * @return Pfad zur Startseite einschliesslich Redirect
	 */
	public String logout() {
		try {
			request.logout();  // Der Loginname wird zurueckgesetzt
		}
		catch (ServletException ignore) {
			return null;
		}
		
		reset();
		session.invalidate();
		//FacesContext.getCurrentInstance().getExternalContext().getSessionMap().clear();
		
		// redirect bewirkt neuen Request, der *NACH* der Session ist
		return JSF_INDEX + JSF_REDIRECT_SUFFIX;
	}

	/**
	 * &Uuml;berpr&uuml;fen, ob Login-Informationen vorhanden sind.
	 * @return true, falls man eingeloggt ist.
	 */
	public boolean isLoggedIn() {
		return user != null;
	}
	
	public List<String> findUsernameListByUsernamePrefix(String usernamePrefix) {
		final List<String> usernameList = authService.findUsernameListByUsernamePrefix(usernamePrefix);
		return usernameList;
	}
	
	public String findRollenByUsername() {
		final AbstractKunde kunde = ks.findKundeByUserName(usernameUpdateRollen, MIT_ROLLEN);
		ausgewaehlteRollenOrig = Lists.newArrayList(kunde.getRollen());
		ausgewaehlteRollen = Lists.newArrayList(kunde.getRollen());
		kundeId = kunde.getId();
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest("Rollen von " + usernameUpdateRollen + ": " + ausgewaehlteRollen);
		}

		if (verfuegbareRollen == null) {
			verfuegbareRollen = Arrays.asList(RolleType.values());
		}
		
		return null;
	}
	
	public String updateRollen() {
		// Zusaetzliche Rollen, die urspruengl. noch nicht vorhandenen waren
		final List<RolleType> zusaetzlicheRollen = ausgewaehlteRollen.stream()
		                                                             .filter(rolle -> !ausgewaehlteRollenOrig.contains(rolle))
		                                                             .collect(Collectors.toList());
		authService.addRollen(kundeId, zusaetzlicheRollen);
		
		// Zu entfernende Rollen, die nicht mehr ausgewaehlt sind
		final List<RolleType> zuEntfernendeRollen = ausgewaehlteRollenOrig.stream()
		                                                                  .filter(rolle -> !ausgewaehlteRollen.contains(rolle))
		                                                                  .collect(Collectors.toList());
		authService.removeRollen(kundeId, zuEntfernendeRollen);
		
		// zuruecksetzen
		usernameUpdateRollen = null;
		ausgewaehlteRollenOrig = null;
		ausgewaehlteRollen = null;
		kundeId = null;

		return JSF_INDEX + JSF_REDIRECT_SUFFIX;
	}
}
