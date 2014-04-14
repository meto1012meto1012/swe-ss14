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

package de.shop.kundenverwaltung.web;

import com.google.common.base.Strings;
import de.shop.auth.web.AuthModel;
import de.shop.kundenverwaltung.business.EmailExistsException;
import de.shop.kundenverwaltung.business.KundeDeleteBestellungException;
import de.shop.kundenverwaltung.business.KundeService;
import de.shop.kundenverwaltung.business.KundeService.FetchType;
import de.shop.kundenverwaltung.business.KundeService.OrderByType;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.HobbyType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.util.AbstractShopException;
import de.shop.util.persistence.ConcurrentDeletedException;
import de.shop.util.persistence.File;
import de.shop.util.persistence.FileHelper;
import de.shop.util.web.Captcha;
import de.shop.util.web.Client;
import de.shop.util.web.Messages;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Pattern;
import org.richfaces.model.SortOrder;
import org.richfaces.push.cdi.Push;

import static de.shop.kundenverwaltung.business.KundeService.FetchType.MIT_BESTELLUNGEN;
import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;
import static java.util.logging.Level.FINEST;
import static javax.persistence.PersistenceContextType.EXTENDED;

/**
 * Dialogsteuerung fuer die Kundenverwaltung
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@SessionScoped
@Stateful
public class KundeModel implements Serializable {
	private static final long serialVersionUID = -8817180909526894740L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final String JSF_KUNDENVERWALTUNG = "/rf/kundenverwaltung/";
	private static final String JSF_VIEW_KUNDE = JSF_KUNDENVERWALTUNG + "viewKunde";
	private static final String JSF_LIST_KUNDEN = JSF_KUNDENVERWALTUNG + "listKunden";
	private static final String JSF_UPDATE_PRIVATKUNDE = JSF_KUNDENVERWALTUNG + "updatePrivatkunde";
	private static final String JSF_UPDATE_FIRMENKUNDE = JSF_KUNDENVERWALTUNG + "updateFirmenkunde";
	private static final String JSF_DELETE_OK = JSF_KUNDENVERWALTUNG + "okDelete";
	
	private static final String REQUEST_KUNDE_ID = "kundeId";

	private static final String CLIENT_ID_KUNDEID = "kundeIdInput";
	private static final String MSG_KEY_KUNDE_NOT_FOUND_BY_ID = "kunde.notFound.id";
	
	private static final String CLIENT_ID_KUNDEN_NACHNAME = "nachname";
	private static final String MSG_KEY_KUNDEN_NOT_FOUND_BY_NACHNAME = "kunde.notFound.nachname";

	private static final String CLIENT_ID_CREATE_EMAIL = "email";
	private static final String MSG_KEY_EMAIL_EXISTS = ".kunde.emailExists";
	
	private static final String CLIENT_ID_CREATE_CAPTCHA_INPUT = "captchaInput";
	private static final String MSG_KEY_CREATE_PRIVATKUNDE_WRONG_CAPTCHA = "kunde.wrongCaptcha";
	
	private static final String CLIENT_ID_UPDATE_EMAIL = "email";
	private static final String MSG_KEY_CONCURRENT_UPDATE = "persistence.concurrentUpdate";
	private static final String MSG_KEY_CONCURRENT_DELETE = "persistence.concurrentDelete";
	
	private static final String CLIENT_ID_DELETE_BUTTON = "deleteButton";
	private static final String MSG_KEY_DELETE_KUNDE_BESTELLUNG = "kunde.deleteMitBestellung";
	
	@PersistenceContext(type = EXTENDED)
	private transient EntityManager em;
	
	@Inject
	private KundeService ks;
	
	@Inject
	private transient HttpServletRequest request;
	
	@Inject
	private AuthModel auth;
	
	@Inject
	@Client
	private Locale locale;
	
	@Inject
	private Messages messages;

	@Inject
	@Push(topic = "marketing")
	private transient Event<String> neuerKundeEvent;
	
	@Inject
	@Push(topic = "updateKunde")
	private transient Event<String> updateKundeEvent;
	
	@Inject
	private Captcha captcha;
	
	@Inject
	private FileHelper fileHelper;

	private String kundeIdStr;
	private Long kundeId;
	
	private AbstractKunde kunde;
	private List<String> hobbies;
	
	@Pattern(regexp = AbstractKunde.NACHNAME_PATTERN, message = "{kunde.nachname.pattern}")
	private String nachname;
	
	private List<AbstractKunde> kunden = Collections.emptyList();
	
	private SortOrder vornameSortOrder = SortOrder.unsorted;
	private String vornameFilter = "";
	
	private boolean geaendertKunde;    // fuer ValueChangeListener
	private Privatkunde neuerPrivatkunde;
	private String captchaInput;

	@Override
	public String toString() {
		return "KundeModel [kundeIdStr=" + kundeIdStr + ", kundeId=" + kundeId + ", nachname=" + nachname
		       + ", geaendertKunde=" + geaendertKunde + "]";
	}
	
	public String getKundeIdStr() {
		return kundeIdStr;
	}

	public void setKundeIdStr(String kundeIdStr) {
		this.kundeIdStr = kundeIdStr;
	}

	public Long getKundeId() {
		return kundeId;
	}

	public void setKundeId(Long kundeId) {
		this.kundeId = kundeId;
	}

	public AbstractKunde getKunde() {
		return kunde;
	}

	public List<String> getHobbies() {
		return hobbies;
	}
	
	public void setHobbies(List<String> hobbies) {
		this.hobbies = hobbies;
	}

	public String getNachname() {
		return nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public List<AbstractKunde> getKunden() {
		return kunden;
	}

	public SortOrder getVornameSortOrder() {
		return vornameSortOrder;
	}

	public void setVornameSortOrder(SortOrder vornameSortOrder) {
		this.vornameSortOrder = vornameSortOrder;
	}

	public void sortByVorname() {
		vornameSortOrder = vornameSortOrder.equals(SortOrder.ascending)
						   ? SortOrder.descending
						   : SortOrder.ascending;
	} 
	
	public String getVornameFilter() {
		return vornameFilter;
	}
	
	public void setVornameFilter(String vornameFilter) {
		this.vornameFilter = vornameFilter;
	}

	public Privatkunde getNeuerPrivatkunde() {
		return neuerPrivatkunde;
	}
	
	public String getCaptchaInput() {
		return captchaInput;
	}

	public void setCaptchaInput(String captchaInput) {
		this.captchaInput = captchaInput;
	}

	/**
	 * Action Methode, um einen Kunden zu gegebener ID zu suchen
	 * @return URL fuer Anzeige des gefundenen Kunden; sonst null
	 */
	public String findKundeById() {
		if (kundeIdStr == null) {
			return null;
		}
		
		try {
			kundeId = Long.valueOf(kundeIdStr);
		}
		catch (NumberFormatException e) {
			return findKundeByIdErrorMsg(kundeIdStr);
		}
		
		try {
			// ggf. Bestellungen ueber den Extended Persistence Context nachladen
			kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
			//kunde = ks.findKundeById(kundeId, FetchType.MIT_BESTELLUNGEN);
		}
		catch (ConstraintViolationException e){
			// Kein Kunde zu gegebener ID gefunden
			return findKundeByIdErrorMsg(kundeId.toString());
		}
		if (kunde.getFile() != null) {
			kunde.getFile().getId(); // nachladen
		}
		
		return JSF_VIEW_KUNDE;
	}
	

	private String findKundeByIdErrorMsg(String id) {
		messages.error(MSG_KEY_KUNDE_NOT_FOUND_BY_ID, locale, CLIENT_ID_KUNDEID, id);
		return null;
	}
	
	/**
	 * F&uuml;r rich:autocomplete
	 * @param idPrefix Praefix fuer potenzielle Kunden-IDs
	 * @return Liste der potenziellen Kunden
	 */
	public List<String> findKundenByIdPrefix(String idPrefix) {
		if (Strings.isNullOrEmpty(idPrefix)) {
			return Collections.emptyList();
		}
		
		Long id; 
		try {
			id = Long.valueOf(idPrefix);
		}
		catch (NumberFormatException e) {
			findKundeByIdErrorMsg(idPrefix);
			return Collections.emptyList();
		}
		
		final List<AbstractKunde> kundenPrefix = ks.findKundenByIdPrefix(id);
		if (kundenPrefix == null || kundenPrefix.isEmpty()) {
			// Kein Kunde zu gegebenem ID-Praefix vorhanden
			findKundeByIdErrorMsg(idPrefix);
			return Collections.emptyList();
		}
		
		final List<String> ids =  kundenPrefix.parallelStream()
		                                      .map(AbstractKunde::getId)
				                              .map(String::valueOf)
		                                      .collect(Collectors.toList());
		
		return ids;
	}
	
	@SuppressWarnings("null")
	public void loadKundeById() {
		// Request-Parameter "kundeId" fuer ID des gesuchten Kunden
		kundeIdStr = request.getParameter("kundeId");
		if (Strings.isNullOrEmpty(kundeIdStr)) {
			if (LOGGER.isLoggable(FINEST)) {
				LOGGER.finest("Der Parameter kundeId ist nicht gesetzt");
			}
//			final AbstractKunde user = auth.getUser();
//			if (user != null) {
//				kundeId = user.getId();
//				kunde = ks.findKundeById(kundeId, FetchType.MIT_BESTELLUNGEN);
//			}

			return;
		}
		try {
			kundeId = Long.valueOf(kundeIdStr);
		}
		catch (NumberFormatException e) {
			if (LOGGER.isLoggable(FINEST)) {
				LOGGER.finest("Ungueltige kundeId = " + kundeIdStr);
			}
			return;
		}
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest("kundeId = " + kundeId);
		}
		
		// Suche durch den Anwendungskern
		final AbstractKunde tmp = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		//final AbstractKunde tmp = ks.findKundeById(kundeId, FetchType.MIT_BESTELLUNGEN);
		
		if (tmp != null) {
			kunde = tmp;
		}
	}
	
	/**
	 * Action Methode, um einen Kunden zu gegebener ID zu suchen
	 * @return URL fuer Anzeige des gefundenen Kunden; sonst null
	 */
	public String findKundenByNachname() {
		if (nachname == null || nachname.isEmpty()) {
			kunden = ks.findAllKunden(FetchType.NUR_KUNDE, OrderByType.UNORDERED);
			return JSF_LIST_KUNDEN;
		}

		try {
			// Bestellungen ggf. nachladen
			kunden = ks.findKundenByNachname(nachname, FetchType.NUR_KUNDE);
		}
		catch (ConstraintViolationException e) {
			kunden = Collections.emptyList();
		}
		return JSF_LIST_KUNDEN;
	}
	
	/**
	 * F&uuml;r rich:autocomplete
	 * @param nachnamePrefix Praefix fuer gesuchte Nachnamen
	 * @return Liste der potenziellen Nachnamen
	 */
	public List<String> findNachnamenByPrefix(String nachnamePrefix) {
		if (Strings.isNullOrEmpty(nachnamePrefix)) {
			return Collections.emptyList();
		}
		// NICHT: Liste von Kunden. Sonst waeren gleiche Nachnamen mehrfach vorhanden.
		final List<String> nachnamen = ks.findNachnamenByPrefix(nachnamePrefix);
		if (nachnamen == null || nachnamen.isEmpty()) {
			messages.error(MSG_KEY_KUNDEN_NOT_FOUND_BY_NACHNAME, locale, CLIENT_ID_KUNDEN_NACHNAME, nachnamePrefix);
			return Collections.emptyList();
		}
		
		return nachnamen;
	}
	
	public String details(AbstractKunde ausgewaehlterKunde) {
		if (ausgewaehlterKunde == null) {
			return null;
		}
		
		kunde = ausgewaehlterKunde;
		kundeId = ausgewaehlterKunde.getId();
		
		return JSF_VIEW_KUNDE;
	}
	
	public String registrierePrivatkunde() {
		if (!captcha.getValue().equals(captchaInput)) {
			return registrierenErrorMsg(null);
		}

		// Liste von Strings als Set von Enums konvertieren
		final Set<HobbyType> hobbiesPrivatkunde = hobbies.stream()
		                                                 .map(HobbyType::valueOf)
		                                                 .collect(Collectors.toSet());
		neuerPrivatkunde.setHobbies(hobbiesPrivatkunde);
		final String password = neuerPrivatkunde.getPassword();  // vor Verschluesselung
		try {
			neuerPrivatkunde = ks.createKunde(neuerPrivatkunde);
		}
		catch (EmailExistsException e) {
			return registrierenErrorMsg(e);
		}

		// Push-Event fuer Webbrowser
		neuerKundeEvent.fire(String.valueOf(neuerPrivatkunde.getId()));
		
		// Einloggen des neuen, registrierten Kunden:
		// geht nicht, weil erst beim Transaktionsende der Kunde gespeichert wird
		//auth.login(neuerPrivatkunde.getId().toString(), password);
		
		// Aufbereitung fuer viewKunde.xhtml
		kundeId = neuerPrivatkunde.getId();
		kunde = neuerPrivatkunde;
		neuerPrivatkunde = null;  // zuruecksetzen
		hobbies = null;
		
		return JSF_VIEW_KUNDE + JSF_REDIRECT_SUFFIX;
	}

	private String registrierenErrorMsg(AbstractShopException e) {
		if (e == null) {
			messages.error(MSG_KEY_CREATE_PRIVATKUNDE_WRONG_CAPTCHA, locale, CLIENT_ID_CREATE_CAPTCHA_INPUT);
		}
		else {
			final Class<?> exceptionClass = e.getClass();
			if (EmailExistsException.class.equals(exceptionClass)) {
				@SuppressWarnings("ThrowableResultIgnored")
				final EmailExistsException e2 = EmailExistsException.class.cast(e);
				messages.error(MSG_KEY_EMAIL_EXISTS, locale, CLIENT_ID_CREATE_EMAIL, e2.getEmail());
			}
			else {
				throw new RuntimeException(e);
			}
		}
		
		return null;
	}

	public void createEmptyPrivatkunde() {
		captchaInput = null;

		if (neuerPrivatkunde != null) {
			return;
		}

		neuerPrivatkunde = new Privatkunde();
		final Adresse adresse = new Adresse();
		adresse.setKunde(neuerPrivatkunde);
		neuerPrivatkunde.setAdresse(adresse);
		
		final int anzahlHobbies = HobbyType.values().length;
		hobbies = new ArrayList<>(anzahlHobbies);
	}
	
	/**
	 * Hobbies bei preRenderView als Liste von Strings fuer JSF aufbereiten,
	 * wenn ein existierender Privatkunde in updatePrivatkunde.xhtml aktualisiert wird
	 */
	public void hobbyTypeToString() {
		if (!kunde.getClass().equals(Privatkunde.class)) {
			return;
		}
		
		final Privatkunde privatkunde = Privatkunde.class.cast(kunde);
		hobbies = privatkunde.getHobbies()
		                     .stream()
		                     .map(HobbyType::name)
		                     .collect(Collectors.toList());
	}
	
	/**
	 * Verwendung als ValueChangeListener bei updatePrivatkunde.xhtml und updateFirmenkunde.xhtml
	 * @param e Ereignis-Objekt mit der Aenderung in einem Eingabefeld, z.B. inputText
	 */
	public void geaendert(ValueChangeEvent e) {
		if (geaendertKunde) {
			return;
		}
		geaendertKunde = !Objects.equals(e.getNewValue(), e.getOldValue());
	}
	

	public String update() {
		auth.preserveLogin();
		
		if (!geaendertKunde || kunde == null) {
			return JSF_INDEX;
		}
		
		// Hobbies konvertieren: String -> HobbyType
		if (kunde.getClass().equals(Privatkunde.class)) {
			final Privatkunde privatkunde = Privatkunde.class.cast(kunde);
			final Set<HobbyType> hobbiesPrivatkunde = hobbies.stream()
					                                         .map(HobbyType::valueOf)
					                                         .collect(Collectors.toSet());
			privatkunde.setHobbies(hobbiesPrivatkunde);
		}
		
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest("Aktualisierter Kunde: " + kunde);
		}
		try {
			kunde = ks.updateKunde(kunde, false);
		}
		catch (EmailExistsException | ConcurrentDeletedException | OptimisticLockException e) {
			return updateErrorMsg(e, kunde.getClass());
		}

		// Push-Event fuer Webbrowser
		updateKundeEvent.fire(String.valueOf(kunde.getId()));
		return JSF_INDEX + JSF_REDIRECT_SUFFIX;
	}
	
	private String updateErrorMsg(RuntimeException e, Class<? extends AbstractKunde> kundeClass) {
		final Class<? extends RuntimeException> exceptionClass = e.getClass();
		if (EmailExistsException.class.equals(exceptionClass)) {
			@SuppressWarnings("ThrowableResultIgnored")
			final EmailExistsException e2 = EmailExistsException.class.cast(e);
			messages.error(MSG_KEY_EMAIL_EXISTS, locale, CLIENT_ID_UPDATE_EMAIL, e2.getEmail());
		}
		else if (OptimisticLockException.class.equals(exceptionClass)) {
			messages.error(MSG_KEY_CONCURRENT_UPDATE, locale, null);

		}
		else if (ConcurrentDeletedException.class.equals(exceptionClass)) {
			messages.error(MSG_KEY_CONCURRENT_DELETE, locale, null);
		}
		else {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	public String selectForUpdate(AbstractKunde ausgewaehlterKunde) {
		if (ausgewaehlterKunde == null) {
			return null;
		}
		
		kunde = ausgewaehlterKunde;
		
		return Privatkunde.class.equals(ausgewaehlterKunde.getClass())
			   ? JSF_UPDATE_PRIVATKUNDE
			   : JSF_UPDATE_FIRMENKUNDE;
	}
	
	/**
	 * Action Methode, um einen zuvor gesuchten Kunden zu l&ouml;schen
	 * @return URL fuer Startseite im Erfolgsfall, sonst wieder die gleiche Seite
	 */
	public String deleteAngezeigtenKunden() {
		if (kunde == null) {
			return null;
		}
		
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest(kunde.toString());
		}
		try {
			ks.deleteKunde(kunde);
		}
		catch (KundeDeleteBestellungException e) {
			messages.error(MSG_KEY_DELETE_KUNDE_BESTELLUNG, locale, CLIENT_ID_DELETE_BUTTON,
					       e.getKundeId(), e.getAnzahlBestellungen());
			return null;
		}
		
		// Aufbereitung fuer ok.xhtml
		request.setAttribute(REQUEST_KUNDE_ID, kunde.getId());
		return JSF_DELETE_OK;
	}

	public String delete(AbstractKunde ausgewaehlterKunde) {
		try {
			ks.deleteKunde(ausgewaehlterKunde);
		}
		catch (KundeDeleteBestellungException e) {
			messages.error(MSG_KEY_DELETE_KUNDE_BESTELLUNG, locale, null, e.getKundeId(), e.getAnzahlBestellungen());
			return null;
		}

		// Aufbereitung fuer ok.xhtml
		request.setAttribute(REQUEST_KUNDE_ID, kunde.getId());
		return JSF_DELETE_OK;
	}
	
	public String getFilename(File file) {
		if (file == null) {
			return "";
		}
		
		fileHelper.store(file);
		return file.getFilename();
	}
	
	
	public String editUser() {
		if (auth == null) {
			return null;
		}
		final AbstractKunde user = auth.getUser();
		if (user == null) {
			return null;
		}
		
		kundeId = user.getId();
		kunde = ks.findKundeById(kundeId, MIT_BESTELLUNGEN);
		
		return kunde.getClass().equals(Privatkunde.class) ? JSF_UPDATE_PRIVATKUNDE : JSF_UPDATE_FIRMENKUNDE;
	}
}
