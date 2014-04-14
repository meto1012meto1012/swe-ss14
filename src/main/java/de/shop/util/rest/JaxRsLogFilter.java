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

package de.shop.util.rest;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.Principal;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import static java.util.logging.Level.FINER;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Provider
@ApplicationScoped
public class JaxRsLogFilter implements ContainerRequestFilter, ContainerResponseFilter {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Override
	public void filter(ContainerRequestContext requestCtx) throws IOException {
		if (!LOGGER.isLoggable(FINER)) {
			return;
		}
		LOGGER.finer("<Request> URI: " + requestCtx.getUriInfo().getAbsolutePath());
		LOGGER.finer("<Request> Method: " + requestCtx.getMethod());
		LOGGER.finer("<Request> Acceptable Media Types: " + requestCtx.getAcceptableMediaTypes());
		LOGGER.finer("<Request> Content Type: " + requestCtx.getHeaderString("content-type"));
		final SecurityContext securityCtx = requestCtx.getSecurityContext();
		if (securityCtx == null) {
			LOGGER.finer("<Request> Security Context: null");
		}
		else {
			LOGGER.finer("<Request> Authentication Scheme: " + securityCtx.getAuthenticationScheme());
			final Principal principal = securityCtx.getUserPrincipal();
			final String principalName = principal == null ? null : principal.getName();
			LOGGER.finer("<Request> Principal: " + principalName);
		}
		LOGGER.finer("<Request> Authorization: " + requestCtx.getHeaderString("authorization"));
		LOGGER.finer("<Request> Acceptable Languages: " + requestCtx.getAcceptableLanguages());
	}

	@Override
	public void filter(ContainerRequestContext requestCtx, ContainerResponseContext responseCtx) throws IOException {
		if (!LOGGER.isLoggable(FINER)) {
			return;
		}
		LOGGER.finer("<Response> Status Info: " + responseCtx.getStatus() + " " + responseCtx.getStatusInfo());
		LOGGER.finer("<Response> Location: " + responseCtx.getLocation());		
	}
}
