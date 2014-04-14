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

package de.shop.bestellverwaltung.rest;

import de.shop.artikelverwaltung.business.ArtikelService;
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.rest.ArtikelResource;
import de.shop.bestellverwaltung.business.BestellungService;
import de.shop.bestellverwaltung.business.BestellungService.FetchType;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Warenkorbposition;
import de.shop.kundenverwaltung.business.KundeService;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.rest.KundeResource;
import de.shop.util.rest.UriHelper;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static de.shop.util.Constants.ADD_LINK;
import static de.shop.util.Constants.SELF_LINK;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Path("/bestellungen")
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75", TEXT_XML + ";qs=0.5" })
@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Stateless
public class BestellungResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Context
	private UriInfo uriInfo;
	
	@Context
	private HttpHeaders httpHeaders;
	
	@Inject
	private BestellungService bs;
	
	@Inject
	private ArtikelService as;
	
	@Inject
	private KundeService ks;
	
	@Inject
	private Principal principal;
	
	@EJB  // NICHT @Inject wegen zyklischer Abhaengigkeit
	private KundeResource kundeResource;
	
	@Inject
	private ArtikelResource artikelResource;
	
	@Inject
	private UriHelper uriHelper;

	/**
	 * Mit der URL /bestellungen/{id} eine Bestellung ermitteln
	 * @param id ID der Bestellung
	 * @return Objekt mit Bestelldaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("{id:[1-9][0-9]*}")
	public Response findBestellungById(@PathParam("id") Long id) {
		final Bestellung bestellung = bs.findBestellungById(id, FetchType.NUR_BESTELLUNG);

		// URIs innerhalb der gefundenen Bestellung anpassen
		setStructuralLinks(bestellung, uriInfo);
		
		// Link-Header setzen
		return Response.ok(bestellung)
                       .links(getTransitionalLinks(bestellung, uriInfo))
                       .build();
	}
	
	/**
	 * Mit der URL /bestellungen/{id}/kunde den Kunden einer Bestellung ermitteln
	 * @param id ID der Bestellung
	 * @return Objekt mit Kundendaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("{id:[1-9][0-9]*}/kunde")
	public Response findKundeByBestellungId(@PathParam("id") Long id) {
		final AbstractKunde kunde = bs.findKundeById(id);
		kundeResource.setStructuralLinks(kunde, uriInfo);

		// Link Header setzen
		return Response.ok(kunde)
                       .links(kundeResource.getTransitionalLinks(kunde, uriInfo))
                       .build();
	}
	
	/**
	 * Mit der URL /bestellungen eine neue Bestellung anlegen
	 * @param bestellung die neue Bestellung
	 * @return Response mit Location
	 */
	@POST
	public Response bestellen(@Valid Bestellung bestellung) {
		// Username aus dem Principal ermitteln
		final String username = principal.getName();
		
		// IDs der (persistenten) Artikel ermitteln
		final List<Long> artikelIds = new ArrayList<>();
		bestellung.getBestellpositionen()
				  .stream()
				  .map(bp -> bp.getArtikelUri())
				  .filter(Objects::nonNull)
				  .map(artikelUri -> artikelUri.toString())
				  .forEach(artikelUriStr -> {
			final int startPos = artikelUriStr.lastIndexOf('/') + 1;
			final String artikelIdStr = artikelUriStr.substring(startPos);
			try {
				final Long artikelId = Long.valueOf(artikelIdStr);
				artikelIds.add(artikelId);
			}
			catch (NumberFormatException ignore) {
				// Ungueltige Artikel-ID: wird nicht beruecksichtigt
				if (LOGGER.isLoggable(FINER)) {
					LOGGER.finer("Keine gueltige Artikel-Nr.: " + artikelIdStr);
				}
			}
		});
		
		if (artikelIds.isEmpty()) {
			// keine einzige Artikel-ID als gueltige Zahl
			throw new InvalidArtikelIdException();
		}
		
		final List<Artikel> gefundeneArtikel = as.findArtikelByIds(artikelIds);
		
		// Bestellpositionen haben URIs fuer persistente Artikel.
		// Diese persistenten Artikel wurden in einem DB-Zugriff ermittelt (s.o.)
		// Fuer jede Bestellposition wird der Artikel passend zur Artikel-URL bzw. Artikel-ID gesetzt.
		// Bestellpositionen mit nicht-gefundene Artikel werden eliminiert.
		int i = 0;
		final List<Bestellposition> neueBestellpositionen = new ArrayList<>();
		for (Bestellposition bp : bestellung.getBestellpositionen()) {
			// Artikel-ID der aktuellen Bestellposition (s.o.):
			// artikelIds haben gleiche Reihenfolge wie bestellpositionen
			final long artikelId = artikelIds.get(i++); // i ist nicht final: kein Lambda, sondern for-Schleife
			
			// Wurde der Artikel beim DB-Zugriff gefunden?
			final Optional<Artikel> artikel = gefundeneArtikel.stream()
			                                                  .filter(a -> a.getId().longValue() == artikelId)
			                                                  .findAny();
			if (artikel.isPresent()) {
				bp.setArtikel(artikel.get());
				neueBestellpositionen.add(bp);				
			}
		}
		bestellung.setBestellpositionen(neueBestellpositionen);
		
		// Kunde mit den vorhandenen ("alten") Bestellungen ermitteln
		bestellung = bs.createBestellung(bestellung, username);
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest(bestellung.toString());
		}

		return Response.created(getUriBestellung(bestellung, uriInfo))
				       .build();
	}
	
	/**
	 * Mit der URL /bestellungen eine neue Bestellung aus dem persistenten Warenkorb anlegen<br>
	 * Header: content-type application/x-www-form-urlencoded<br>
	 * @return Response mit Location
	 */
	@POST
	@Consumes(APPLICATION_FORM_URLENCODED)
	public Response bestellenWarenkorb() {
		// Username aus dem Principal ermitteln
		final String username = principal.getName();

		// Bestellung aus dem persistenten Warenkorb
		final Bestellung neueBestellung = bs.createBestellung(username);
		return neueBestellung == null
			   ? Response.status(BAD_REQUEST).build()
			   : Response.created(getUriBestellung(neueBestellung, uriInfo)).build();
	}
	
	/**
	 * Mit der URL /bestellungen/warenkorbposition eine neue Position im persistenten Warenkorb anlegen<br>
	 * Header: content-type application/x-www-form-urlencoded<br>
	 * Beispiel: anzahl=1&amp;artikelUri=http://localhost:8443/shop/rest/artikel/301<br>
	 * @param warenkorbposition die neue Position im Warenkorb
	 * @return Response ohne Location
	 */
	@POST
	@Path("/warenkorbposition")
	@Consumes(APPLICATION_FORM_URLENCODED)
	public Response neueWarenkorbPosition(@BeanParam @Valid Warenkorbposition warenkorbposition) {
		// Artikel zur uebergebenen URI ermitteln
		final String artikelUri = warenkorbposition.getArtikelUri().toString();
		final int startPos = artikelUri.lastIndexOf('/') + 1;
		final String artikelIdStr = artikelUri.substring(startPos);
		Long artikelId;
		try {
			artikelId = Long.valueOf(artikelIdStr);
		}
		catch (NumberFormatException ignore) {
			// Ungueltige Artikel-ID: wird nicht beruecksichtigt
			if (LOGGER.isLoggable(FINER)) {
				LOGGER.finer("Keine gueltige Artikel-Nr.: " + artikelIdStr);
			}
			return Response.status(BAD_REQUEST).build();
		}
		final Artikel artikel = as.findArtikelById(artikelId);
		warenkorbposition.setArtikel(artikel);
		
		// Kunde zum Username ermitteln
		final String username = principal.getName();
		final AbstractKunde kunde = ks.findKundeByUserName(username, KundeService.FetchType.MIT_BESTELLUNGEN);
		warenkorbposition.setKunde(kunde);

		// Ermittelten Artikel mit zugehoeriger Anzahl
		bs.createWarenkorbposition(warenkorbposition);
		return Response.created(null).build();  // Header ohne "Location"
	}
	
	
	//--------------------------------------------------------------------------
	// Methoden fuer URIs und Links
	//--------------------------------------------------------------------------
	
	public URI getUriBestellung(Bestellung bestellung, UriInfo uriInfo) {
		return uriHelper.getUri(BestellungResource.class, "findBestellungById", bestellung.getId(), uriInfo);
	}
	
	public void setStructuralLinks(Bestellung bestellung, UriInfo uriInfo) {
		// URI fuer Kunde setzen
		final AbstractKunde kunde = bestellung.getKunde();
		if (kunde != null) {
			final URI kundeUri = kundeResource.getUriKunde(bestellung.getKunde(), uriInfo);
			bestellung.setKundeUri(kundeUri);
		}
				
		// URI fuer Artikel in den Bestellpositionen setzen
		final Collection<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
		if (bestellpositionen != null) {
			bestellpositionen.forEach(bp -> {
				final URI artikelUri = artikelResource.getUriArtikel(bp.getArtikel(), uriInfo);
				bp.setArtikelUri(artikelUri);
			});
		}
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest(bestellung.toString());
		}
	}

	private Link[] getTransitionalLinks(Bestellung bestellung, UriInfo uriInfo) {
		final Link self = Link.fromUri(getUriBestellung(bestellung, uriInfo))
                              .rel(SELF_LINK)
                              .build();
		final Link add = Link.fromUri(uriHelper.getUri(BestellungResource.class, uriInfo))
                              .rel(ADD_LINK)
                              .build();

		return new Link[] { self, add };
	}
}
