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

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.Response.Status.NOT_IMPLEMENTED;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Path("/wartungsvertraege")
@Produces({ APPLICATION_JSON, APPLICATION_XML + ";qs=0.75", TEXT_XML + ";qs=0.5" })
@Consumes({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Stateless
// Falsche Warning bei NetBeans: "If a managed bean has a public field, it must have scope @Dependent"
// weil in der Spez. von CDI 1.0 bzw. Java EE 6 "static" vergessen wurde
public class WartungsvertragResource {
	/**
	 * Mit der URI /kunden/{id}/wartungsvertraege die Wartungsvertraege ermitteln
	 * zu einem bestimmten Kunden ermitteln
	 * @param id ID des Kunden
	 * @return Wartungsvertraege, falls die ID vorhanden ist
	 */
	@GET
	@Path("{id:[1-9][0-9]*}/wartungsvertraege")
	public Response findWartungsvertraegeByKundeId(@PathParam("id") Long id) {
		// Diese Methode ist bewusst NICHT implementiert, um zu zeigen,
		// wie man Methodensignaturen an der Schnittstelle fuer andere
		// Teammitglieder schon mal bereitstellt, indem einfach ein "Not implemented" (501)
		// produziert wird.
		// Die Kolleg/inn/en koennen nun weiterarbeiten, waehrend man selbst
		// gerade keine Zeit hat, weil andere Aufgaben Vorrang haben.
		
		// TODO findWartungsvertraegeByKundeId noch nicht implementiert
		return Response.status(NOT_IMPLEMENTED)
				       .entity("findWartungsvertraegeByKundeId: NOT YET IMPLEMENTED")
				       .type(TEXT_PLAIN)
				       .build();
	}
}
