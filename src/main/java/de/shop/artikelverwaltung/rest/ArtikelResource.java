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

package de.shop.artikelverwaltung.rest;

import de.shop.artikelverwaltung.business.ArtikelService;
import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.util.rest.UriHelper;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static de.shop.util.Constants.SELF_LINK;
import static java.util.logging.Level.FINEST;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Path("/artikel")
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75", TEXT_XML + ";qs=0.5" })
@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Stateless
public class ArtikelResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	public static final String ARTIKEL_ID_PATH_PARAM = "id";
	
	@Context
	private UriInfo uriInfo;
	
	@Inject
	private ArtikelService as;
	
	@Inject
	private UriHelper uriHelper;
	
	@Inject
	private HttpServletRequest request;
	
	/**
	 * Mit der URL /artikel/{id} einen Artikel ermitteln
	 * @param id ID des Artikels
	 * @return Objekt mit Artikeldaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("{" + ARTIKEL_ID_PATH_PARAM + ":[1-9][0-9]*}")
	public Response findArtikelById(@PathParam("id") Long id) {
		final Artikel artikel = as.findArtikelById(id);
		return Response.ok(artikel)
	                   .links(getTransitionalLinks(artikel, uriInfo))
	                   .build();
	}
	
	/**
	 * Mit der URI /artikel einen Artikel per POST anlegen.
	 * @param artikel neuer Artikel
	 * @return Response-Objekt mit URI des neuen Artikels
	 */
	@POST
	public Response createArtikel(@Valid Artikel artikel) {
		artikel = as.createArtikel(artikel);
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest(artikel.toString());
		}
		
		return Response.created(getUriArtikel(artikel, uriInfo))
				       .build();
	}
	
	/**
	 * Mit der URI /artikel/form einen Artikel per POST durch FORM-Parameter anlegen oder aktualisieren.
	 * @param artikel neuer oder zu aktualisierender Artikel
	 * @return Response-Objekt mit ggf. der URI des neuen Artikels
	 */
	@POST
	@PUT
	@Consumes(APPLICATION_FORM_URLENCODED)
	public Response createArtikelForm(@BeanParam @Valid Artikel artikel) {
		if (artikel.getId() == null && "POST".equals(request.getMethod())) {
			return createArtikel(artikel);
		}
		if (artikel.getId() != null && "PUT".equals(request.getMethod())) {
			return updateArtikel(artikel);
		}
		return Response.status(BAD_REQUEST).build();
	}

	/**
	 * Mit der URI /artikel einen Artikel per PUT aktualisieren
	 * @param artikel zu aktualisierende Daten des Artikels
	 * @return Aktualisierter Artikel
	 */
	@PUT
	public Response updateArtikel(@Valid Artikel artikel) {
		// Vorhandenen Artikel ermitteln
		final Artikel origArtikel = as.findArtikelById(artikel.getId());
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest("Artikel vorher = " + origArtikel);
		}
	
		// Daten des vorhandenen Artikels ueberschreiben
		origArtikel.setValues(artikel);
		if (LOGGER.isLoggable(FINEST)) {
			LOGGER.finest("Artikel nachher = " + origArtikel);
		}
		
		// Update durchfuehren
		artikel = as.updateArtikel(origArtikel);		
		return Response.ok(artikel)
				       .links(getTransitionalLinks(artikel, uriInfo))
				       .build();
	}
	
	//--------------------------------------------------------------------------
	// Methoden fuer URIs und Links
	//--------------------------------------------------------------------------
	
	public URI getUriArtikel(Artikel artikel, UriInfo uriInfo) {
		return uriHelper.getUri(ArtikelResource.class, "findArtikelById", artikel.getId(), uriInfo);
	}
	
	private Link[] getTransitionalLinks(Artikel artikel, UriInfo uriInfo) {
		final Link self = Link.fromUri(getUriArtikel(artikel, uriInfo))
                              .rel(SELF_LINK)
                              .build();

		return new Link[] { self };
	}
}
