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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
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
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.BeforeClass;

import static de.shop.util.TestConstants.HTTPS;
import static de.shop.util.TestConstants.KEYSTORE_TYPE;
import static de.shop.util.TestConstants.PORT;
import static de.shop.util.TestConstants.TLS12;
import static de.shop.util.TestConstants.TRUSTSTORE_PASSWORD;
import static de.shop.util.TestConstants.TRUSTSTORE_PATH;
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public abstract class AbstractResourceTest {
	private static ResteasyClientBuilder resteasyClientBuilder;
	private static LayeredConnectionSocketFactory socketFactory;
	private HttpClientConnectionManager httpClientConnectionManager;

	@Deployment(name = ArchiveBuilder.TEST_WAR, testable = false) // Tests laufen nicht im Container
	@OverProtocol(value = "Servlet 3.0")  // https://docs.jboss.org/author/display/ARQ/Servlet+3.0
	protected static Archive<?> deployment() {
		return ArchiveBuilder.getInstance().getArchive();
	}
	
	@BeforeClass
	public static void init() {
		resteasyClientBuilder = new ResteasyClientBuilder();
		
		try {
			final KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE);
			try (final InputStream stream = Files.newInputStream(TRUSTSTORE_PATH)) {
				trustStore.load(stream, TRUSTSTORE_PASSWORD);
			}
			
			final SSLContext sslcontext = SSLContexts.custom()
													 .useProtocol(TLS12)
													 .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
													 .build();
			socketFactory = new SSLConnectionSocketFactory(sslcontext, STRICT_HOSTNAME_VERIFIER);
		}
		catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException 
		       | KeyManagementException e) {
			throw new RuntimeException(e);
		}
	}
	
	@After
	public void shutdownHttpClient() {
		if (httpClientConnectionManager != null) {
			httpClientConnectionManager.shutdown();
		}
	}
	
	protected Client getHttpsClient() {
		return getHttpsClient(null, null);
	}
	
	protected Client getHttpsClient(String username, String password) {
		shutdownHttpClient();  // falls noch eine offene HTTP-Verbindung existiert, diese zuerst schliessen
		
		// Nur fuer genau 1 HTTP-Verbindung geeignet (und nicht fuer mehrere)
		final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
																		  .register(HTTPS, socketFactory)
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
														   .setSSLSocketFactory(socketFactory)
														   .setSchemePortResolver(schemePortResolver);

		if (username != null) {
			final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			final Credentials credentials = new UsernamePasswordCredentials(username, password);
			credentialsProvider.setCredentials(AuthScope.ANY, credentials);
			clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
		}

		final HttpClient httpClient = clientBuilder.build();
		final ClientHttpEngine engine = new ApacheHttpClient4Engine(httpClient);
		return resteasyClientBuilder.httpEngine(engine).build();
	}
	
	static ResteasyClientBuilder getResteasyClientBuilder() {
		return resteasyClientBuilder;
	}
	
	static LayeredConnectionSocketFactory getSocketFactory() {
		return socketFactory;
	}
}
