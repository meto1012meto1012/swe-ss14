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

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

import static de.shop.util.TestConstants.HTTPS;
import static de.shop.util.TestConstants.PORT;
import static java.util.logging.Level.WARNING;



/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class HttpsConcurrencyHelper {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private HttpClientConnectionManager httpClientConnectionManager;

	public Client getHttpsClient() {
		return getHttpsClient(null, null);
	}
	
	public Client getHttpsClient(String username, String password) {
		shutdownHttpClient();  // falls noch eine offene HTTP-Verbindung existiert, diese zuerst schliessen
		
		// Nur fuer genau 1 HTTP-Verbindung geeignet (und nicht fuer mehrere)
		final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
																		  .register(HTTPS, AbstractResourceTest.getSocketFactory())
																		  .build();
		httpClientConnectionManager = new BasicHttpClientConnectionManager(registry);
		// Lambda-Expression als anonyme Methode fuer das Interface SchemePortResolver
		final SchemePortResolver schemePortResolver = (HttpHost host) -> {
			if (PORT == host.getPort()) {
				return PORT;
			}
			throw new RuntimeException("Falscher HttpHost: " + host);
		};

		final HttpClientBuilder clientBuilder = HttpClients.custom()
														   .setConnectionManager(httpClientConnectionManager)
														   .setSSLSocketFactory(AbstractResourceTest.getSocketFactory())
														   .setSchemePortResolver(schemePortResolver);

		if (username != null) {
			final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			final Credentials credentials = new UsernamePasswordCredentials(username, password);
			credentialsProvider.setCredentials(AuthScope.ANY, credentials);
			clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
		}

		final HttpClient httpClient = clientBuilder.build();
		final ClientHttpEngine engine = new ApacheHttpClient4Engine(httpClient);
		return AbstractResourceTest.getResteasyClientBuilder()
				                   .httpEngine(engine)
				                   .build();
	}

	/**
	 * Wenn ein Objekt der Hilfsklasse nicht mehr benoetigt wird, dann wird auch die HTTP-Verbindung geschlossen
	 * @throws Throwable Eine potenzielle Exception
	 */
	@Override
	@SuppressWarnings("FinalizeDeclaration")
	protected void finalize() throws Throwable {
		shutdownHttpClient();
		super.finalize();
	}
	
	private void shutdownHttpClient() {
		if (httpClientConnectionManager != null) {
			try {
				httpClientConnectionManager.shutdown();
			}
			catch (IllegalStateException e) {
				LOGGER.log(WARNING, e.getMessage(), e);
			}
			httpClientConnectionManager = null;
		}
	}
}
