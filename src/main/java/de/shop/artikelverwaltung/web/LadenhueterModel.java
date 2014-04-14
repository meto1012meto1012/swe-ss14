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

package de.shop.artikelverwaltung.web;

import de.shop.artikelverwaltung.business.ArtikelService;
import de.shop.artikelverwaltung.domain.Artikel;
import java.io.Serializable;
import java.util.List;
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;



/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@RequestScoped
@Stateful
public class LadenhueterModel implements Serializable {	
	private static final long serialVersionUID = -2889271008433687088L;
	private static final int ANZAHL_LADENHUETER = 5;

	@Inject
	private ArtikelService as;
	
	@Inject
	private transient HttpSession session;

	public void load() {
		@SuppressWarnings("unchecked")
		List<Artikel> ladenhueter = (List<Artikel>) session.getAttribute("ladenhueter");
		
		if (ladenhueter == null) {
			ladenhueter = as.ladenhueter(ANZAHL_LADENHUETER);
			session.setAttribute("ladenhueter", ladenhueter);
		}
	}
}
