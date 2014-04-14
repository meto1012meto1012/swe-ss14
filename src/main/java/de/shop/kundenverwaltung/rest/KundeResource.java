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

package de.shop.kundenverwaltung.rest;

import com.google.common.base.Strings;
import de.shop.bestellverwaltung.business.BestellungService;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.rest.BestellungResource;
import de.shop.kundenverwaltung.business.KundeService;
import de.shop.kundenverwaltung.business.KundeService.FetchType;
import de.shop.kundenverwaltung.business.KundeService.OrderByType;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.GeschlechtType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.util.persistence.File;
import de.shop.util.rest.UriHelper;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static de.shop.util.Constants.ADD_LINK;
import static de.shop.util.Constants.EMAIL_PATTERN;
import static de.shop.util.Constants.FIRST_LINK;
import static de.shop.util.Constants.LAST_LINK;
import static de.shop.util.Constants.LIST_LINK;
import static de.shop.util.Constants.REMOVE_LINK;
import static de.shop.util.Constants.SELF_LINK;
import static de.shop.util.Constants.UPDATE_LINK;
import static java.util.logging.Level.FINEST;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_XML;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Path("/kunden")
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75", TEXT_XML + ";qs=0.5" })
@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Stateless
// Falsche Warning bei NetBeans: "If a managed bean has a public field, it must have scope @Dependent"
// weil in der Spez. von CDI 1.0 bzw. Java EE 6 "static" vergessen wurde
public class KundeResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	// public fuer Testklassen
	public static final String KUNDEN_ID_PATH_PARAM = "kundeId";
	public static final String KUNDEN_NACHNAME_QUERY_PARAM = "nachname";
	public static final String KUNDEN_PLZ_QUERY_PARAM = "plz";
	public static final String KUNDEN_EMAIL_QUERY_PARAM = "email";
	public static final String KUNDEN_SEIT_QUERY_PARAM = "seit";
	public static final String KUNDEN_GESCHLECHT_QUERY_PARAM = "geschlecht";
	public static final String KUNDEN_MINBESTMENGE_QUERY_PARAM = "minBestMenge";
	
	@Context  // DI durch JAX-RS bei jedem Request, weshalb Producer-Klasse mit CDI fuer spaeteres @Inject nicht funktioniert
	private UriInfo uriInfo;   // funktioniert nicht innerhalb von parallelStream() von Java 8
	
	@Inject
	private KundeService ks;
	
	@Inject
	private BestellungService bs;
	
	@EJB    // NICHT @Inject wegen zyklischer Abhaengigkeit
	private BestellungResource bestellungResource;
	
	@Inject
	private UriHelper uriHelper;

	
	/**
	 * Mit der URI /kunden/{id} einen Kunden ermitteln
	 * @param id ID des Kunden
	 * @return Objekt mit Kundendaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("{" + KUNDEN_ID_PATH_PARAM + ":[1-9][0-9]*}")
	public Response findKundeById(@PathParam(KUNDEN_ID_PATH_PARAM) Long id) {
		final AbstractKunde kunde = ks.findKundeById(id, FetchType.NUR_KUNDE);
		setStructuralLinks(kunde, uriInfo);
		
		return Response.ok(kunde)
				       .links(getTransitionalLinks(kunde, uriInfo))
				       .build();
	}
	
	/**
	 * Kunden-IDs zu gegebenem Praefix suchen
	 * @param idPrefix Praefix zu gesuchten IDs
	 * @return Collection mit IDs zu gegebenem Praefix
	 */
	@GET
	@Path("/prefix/id/{id:[1-9][0-9]*}")
	@Produces({ APPLICATION_JSON, TEXT_PLAIN })
	public Collection<Long> findIdsByPrefix(@PathParam("id") String idPrefix) {
		return ks.findIdsByPrefix(idPrefix);
	}
	

	/**
	 * Mit der URI /kunden werden alle Kunden ermittelt oder
	 * mit kunden?nachname=... diejenigen mit einem bestimmten Nachnamen.
	 * @param email Email-Adresse
	 * @param nachname Der gemeinsame Nachname der gesuchten Kunden
	 * @param plz Postleitzahl
	 * @param seit Datum seit wann
	 * @param geschlecht Geschlecht
	 * @param minBestMenge Mindestbestellmenge
	 * @return Collection mit den gefundenen Kundendaten
	 */
	@GET
	public Response findKunden(@QueryParam(KUNDEN_EMAIL_QUERY_PARAM)
                               @Pattern(regexp = EMAIL_PATTERN, message = "{kunde.email}")
                               String email,
                               @QueryParam(KUNDEN_NACHNAME_QUERY_PARAM)
                               @Pattern(regexp = AbstractKunde.NACHNAME_PATTERN, message = "{kunde.nachname.pattern}")
	                           String nachname,
	                           @QueryParam(KUNDEN_PLZ_QUERY_PARAM)
	                           @Pattern(regexp = "\\d{5}", message = "{adresse.plz}")
                               String plz,
                               @QueryParam(KUNDEN_SEIT_QUERY_PARAM)  // Default-Format, z.B. 31 Oct 2001
                               Date seit,
                               @QueryParam(KUNDEN_GESCHLECHT_QUERY_PARAM)
	                           GeschlechtType geschlecht,
	                           @QueryParam(KUNDEN_MINBESTMENGE_QUERY_PARAM)
                               Integer minBestMenge) {
		List<? extends AbstractKunde> kunden = null;
		AbstractKunde kunde = null;
		// Kein Query-Parameter
		if (Strings.isNullOrEmpty(email) && Strings.isNullOrEmpty(nachname) && Strings.isNullOrEmpty(plz)
		    && seit == null && geschlecht == null && minBestMenge == null) {
			kunden = ks.findAllKunden(FetchType.NUR_KUNDE, OrderByType.ID);
		}
		// Genau Ein Query-Parameter
		else if (!Strings.isNullOrEmpty(email) && Strings.isNullOrEmpty(nachname) && Strings.isNullOrEmpty(plz)
			     && seit == null && geschlecht == null && minBestMenge == null) {
			kunde = ks.findKundeByEmail(email);
		}
		else if (!Strings.isNullOrEmpty(nachname) && Strings.isNullOrEmpty(email) && Strings.isNullOrEmpty(plz)
				 && seit == null && geschlecht == null && minBestMenge == null) {
			kunden = ks.findKundenByNachname(nachname, FetchType.NUR_KUNDE);
		}
		else if (!Strings.isNullOrEmpty(plz) && Strings.isNullOrEmpty(email) && Strings.isNullOrEmpty(nachname)
				 && seit == null && geschlecht == null && minBestMenge == null) {
			kunden = ks.findKundenByPLZ(plz);
		}
		else if (seit != null && Strings.isNullOrEmpty(email) && Strings.isNullOrEmpty(nachname)
				 && Strings.isNullOrEmpty(plz) && geschlecht == null && minBestMenge == null) {
			kunden = ks.findKundenBySeit(seit);
		}
		else if (geschlecht != null && Strings.isNullOrEmpty(email) && Strings.isNullOrEmpty(nachname)
				 && Strings.isNullOrEmpty(plz) && seit == null && minBestMenge == null) {
			kunden = ks.findKundenByGeschlecht(geschlecht);
		}
		else if (minBestMenge != null && Strings.isNullOrEmpty(email) && Strings.isNullOrEmpty(nachname)
				&& Strings.isNullOrEmpty(plz) && seit == null && geschlecht == null) {
			kunden = ks.findKundenByMinBestMenge(minBestMenge.intValue());
		}
		// Mehrere Query-Parameter
		else {
			kunden = ks.findKundenByCriteria(email, nachname, plz, seit, geschlecht, minBestMenge);
		}
		
		Object entity = null;
		Link[] links = null;
		if (kunden != null) {
			kunden.forEach(k -> setStructuralLinks(k, uriInfo));
			entity = new GenericEntity<List<? extends AbstractKunde>>(kunden){};
			links = getTransitionalLinksKunden(kunden, uriInfo);
		}
		else if (kunde != null) {
			entity = kunde;
			links = getTransitionalLinks(kunde, uriInfo);
		}
		
		return Response.ok(entity)
                       .links(links)
                       .build();
	}
	
	/**
	 * Nachnamen zu gegebenem Praefix suchen
	 * @param nachnamePrefix Praefix zu gesuchten Nachnamen
	 * @return Collection mit Nachnamen zu gegebenem Praefix
	 */
	@GET
	@Path("/prefix/nachname/{nachname}")
	@Produces({ APPLICATION_JSON, TEXT_PLAIN })
	public Collection<String> findNachnamenByPrefix(@PathParam("nachname") String nachnamePrefix) {
		return ks.findNachnamenByPrefix(nachnamePrefix);
	}
	
	
	/**
	 * Mit der URI kunden/{id}/bestellungen die Bestellungen zu eine Kunden ermitteln
	 * @param id ID des Kunden
	 * @return Objekt mit Bestellungsdaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("{id:[1-9][0-9]*}/bestellungen")
	public Response findBestellungenByKundeId(@PathParam("id") Long id) {
		final AbstractKunde kunde = ks.findKundeById(id, FetchType.NUR_KUNDE);
		final List<Bestellung> bestellungen = bs.findBestellungenByKunde(kunde,
				                                                         BestellungService.FetchType.NUR_BESTELLUNG);
		
		// URIs innerhalb der gefundenen Bestellungen anpassen
		if (bestellungen != null) {
			bestellungen.forEach(bestellung -> bestellungResource.setStructuralLinks(bestellung, uriInfo));
		}
		
		return Response.ok(new GenericEntity<List<Bestellung>>(bestellungen) {})
                       .links(getTransitionalLinksBestellungen(bestellungen, kunde, uriInfo))
                       .build();
	}
		
	/**
	 * IDs der Bestellungen zu einem Kunden mit gegebener ID suchen
	 * @param kundeId ID des Kunden
	 * @return Liste der Bestellungen-IDs
	 */
	@GET
	@Path("{id:[1-9][0-9]*}/bestellungenIds")
	public Response findBestellungenIdsByKundeId(@PathParam("id") Long kundeId) {
		final AbstractKunde kunde = ks.findKundeById(kundeId, FetchType.MIT_BESTELLUNGEN);
		final Collection<Bestellung> bestellungen =
				                     bs.findBestellungenByKunde(kunde, BestellungService.FetchType.NUR_BESTELLUNG);
		
		final List<Long> bestellungenIds = bestellungen.parallelStream()
		                                               .map(Bestellung::getId)
		                                               .collect(Collectors.toList());
		
		return Response.ok(new GenericEntity<Collection<Long>>(bestellungenIds) {})
			           .build();
	}
	
	/**
	 * Mit der URI /kunden einen Kunden per POST anlegen.
	 * @param kunde neuer Kunde
	 * @return Response-Objekt mit URI des neuen Kunden
	 */
	@POST
	public Response registrieren(@Valid AbstractKunde kunde) {
		final Adresse adresse = kunde.getAdresse();
		if (adresse != null) {
			adresse.setKunde(kunde);
		}
		if (Strings.isNullOrEmpty(kunde.getPasswordWdh())) {
			// ein IT-System als REST-Client muss das Password ggf. nur 1x uebertragen
			kunde.setPasswordWdh(kunde.getPassword());
		}
		
		kunde = ks.createKunde(kunde);
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest(kunde.toString());
		}
		
		return Response.created(getUriKunde(kunde, uriInfo))
				       .build();
	}
	
	/**
	 * Mit der URI /kunden/form/privat einen Privatkunden per POST durch FORM-Parameter anlegen.
	 * @param kunde neuer Kunde
	 * @return Response-Objekt mit er URI des neuen Kunden
	 */
	@Path("/privat")
	@POST
	@Consumes(APPLICATION_FORM_URLENCODED)
	public Response registrierenPrivatkundeForm(@BeanParam @Valid Privatkunde kunde) {
		return registrieren(kunde);
	}
	
	/**
	 * Mit der URI /kunden/privat/update einen Privatkunden per PUT durch FORM-Parameter aktualisieren.
	 * @param kunde zu aendernder Kunde
	 * @return Response-Objekt
	 */
	@Path("/privat/update")  // NICHT /privat, um Security-Constraints setzen zu koennen
	@PUT
	@Consumes(APPLICATION_FORM_URLENCODED)
	public Response updatePrivatkundeForm(@BeanParam @Valid Privatkunde kunde) {
		return updateKunde(kunde);
	}

	/**
	 * Mit der URI /kunden einen Kunden per PUT aktualisieren
	 * @param kunde zu aktualisierende Daten des Kunden
	 * @return Aktualisierter Kunde
	 */
	@PUT
	public Response updateKunde(@Valid AbstractKunde kunde) {
		// Vorhandenen Kunden ermitteln
		final AbstractKunde origKunde = ks.findKundeById(kunde.getId(), FetchType.MIT_ROLLEN);
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest("Kunde vorher = " + origKunde);
			LOGGER.finest("Neue Werte durch den PUT-Request = " + kunde);
		}
	
		// Daten des vorhandenen Kunden ueberschreiben
		origKunde.setValues(kunde);
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest("Kunde nachher = " + origKunde);
		}
		
		// Update durchfuehren
		kunde = ks.updateKunde(origKunde, false);
		setStructuralLinks(kunde, uriInfo);
		
		return Response.ok(kunde)
				       .links(getTransitionalLinks(kunde, uriInfo))
				       .build();
	}


	/**
	 * Mit der URI /kunden{id} einen Kunden per DELETE l&ouml;schen
	 * @param kundeId des zu l&ouml;schenden Kunden
	 *         gel&ouml;scht wurde, weil es zur gegebenen id keinen Kunden gibt
	 */
	@Path("{id:[1-9][0-9]*}")
	@DELETE
	public void deleteKunde(@PathParam("id") long kundeId) {
		ks.deleteKundeById(kundeId);
	}

	// <security-constraint> in web.xml: Wildcards in <url-pattern> nur am Ende
	@Path("image/{id:[1-9][0-9]*}")
	@POST
	@Consumes({ "image/jpeg", "image/pjpeg", "image/png" })  // RESTEasy unterstuetzt nicht video/mp4
	public Response uploadImage(@PathParam("id") Long kundeId, byte[] bytes) {
		ks.setFile(kundeId, bytes);
		return Response.created(uriHelper.getUri(KundeResource.class, "downloadImage", kundeId, uriInfo))
				       .build();
	}
	
	/**
	 * Bild zu einem Kunden mit gegebener ID herunterladen
	 * @param kundeId ID des Kunden
	 * @return Byte[] mit dem Bild
	 */
	@Path("image/{id:[1-9][0-9]*}")
	@GET
	@Produces({ "image/jpeg", "image/pjpeg", "image/png" })
	public byte[] downloadImage(@PathParam("id") Long kundeId) {
		final AbstractKunde kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		final File file = kunde.getFile();
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest(file.toString());
		}
		
		return file.getBytes();
	}
	
	@Path("base64/{id:[1-9][0-9]*}")
	@POST
	@Consumes({ TEXT_PLAIN })
	public Response uploadBase64(@PathParam("id") Long kundeId, String base64) {
		final byte[] bytes = Base64.getDecoder().decode(base64);
		ks.setFile(kundeId, bytes);
		return Response.created(uriHelper.getUri(KundeResource.class, "downloadBase64", kundeId, uriInfo))
				       .build();
	}
	
	/**
	 * Multimedia-Datei (mit Base64-Codierung) zu einem Kunden mit gegebener ID herunterladen
	 * @param kundeId ID des Kunden
	 * @return String mit Base64-Codierung einer Multimedia-Datei
	 */
	@Path("base64/{id:[1-9][0-9]*}")
	@GET
	@Produces({ TEXT_PLAIN })
	public String downloadBase64(@PathParam("id") Long kundeId) {
		final AbstractKunde kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		final File file = kunde.getFile();
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest(file.toString());
		}
		
		return Base64.getEncoder().encodeToString(file.getBytes());
	}
	
	//--------------------------------------------------------------------------
	// Methoden fuer URIs und Links
	//--------------------------------------------------------------------------
	
	public URI getUriKunde(AbstractKunde kunde, UriInfo uriInfo) {
		return uriHelper.getUri(KundeResource.class, "findKundeById", kunde.getId(), uriInfo);
	}

	private URI getUriBestellungen(AbstractKunde kunde, UriInfo uriInfo) {
		return uriHelper.getUri(KundeResource.class, "findBestellungenByKundeId", kunde.getId(), uriInfo);
	}
	
	public void setStructuralLinks(AbstractKunde kunde, UriInfo uriInfo) {
		// URI fuer Bestellungen setzen
		final URI uri = getUriBestellungen(kunde, uriInfo);
		kunde.setBestellungenUri(uri);
		
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest(kunde.toString());
		}
	}
	
	public Link[] getTransitionalLinks(AbstractKunde kunde, UriInfo uriInfo) {
		final Link self = Link.fromUri(getUriKunde(kunde, uriInfo))
	                          .rel(SELF_LINK)
	                          .build();

		final Link list = Link.fromUri(uriHelper.getUri(KundeResource.class, uriInfo))
                              .rel(LIST_LINK)
                              .build();
		
		final Link add = Link.fromUri(uriHelper.getUri(KundeResource.class, uriInfo))
                             .rel(ADD_LINK)
                             .build();

		final Link update = Link.fromUri(uriHelper.getUri(KundeResource.class, uriInfo))
				                .rel(UPDATE_LINK)
				                .build();

		final Link remove = Link.fromUri(uriHelper.getUri(KundeResource.class, "deleteKunde", kunde.getId(), uriInfo))
                                .rel(REMOVE_LINK)
                                .build();

		return new Link[] { self, list, add, update, remove };
	}
	
	private Link[] getTransitionalLinksKunden(List<? extends AbstractKunde> kunden, UriInfo uriInfo) {
		if (kunden == null || kunden.isEmpty()) {
			return null;
		}
		
		final Link first = Link.fromUri(getUriKunde(kunden.get(0), uriInfo))
	                           .rel(FIRST_LINK)
	                           .build();
		final int lastPos = kunden.size() - 1;
		final Link last = Link.fromUri(getUriKunde(kunden.get(lastPos), uriInfo))
                              .rel(LAST_LINK)
                              .build();
		
		return new Link[] { first, last };
	}
	
	private Link[] getTransitionalLinksBestellungen(List<Bestellung> bestellungen,
			                                        AbstractKunde kunde,
			                                        UriInfo uriInfo) {
		if (bestellungen == null || bestellungen.isEmpty()) {
			return new Link[0];
		}
		
		final Link self = Link.fromUri(getUriBestellungen(kunde, uriInfo))
                              .rel(SELF_LINK)
                              .build();
		
		final Link first = Link.fromUri(bestellungResource.getUriBestellung(bestellungen.get(0), uriInfo))
	                           .rel(FIRST_LINK)
	                           .build();
		
		final int lastPos = bestellungen.size() - 1;
		final Link last = Link.fromUri(bestellungResource.getUriBestellung(bestellungen.get(lastPos), uriInfo))
                              .rel(LAST_LINK)
                              .build();
		
		return new Link[] { self, first, last };
	}
}
