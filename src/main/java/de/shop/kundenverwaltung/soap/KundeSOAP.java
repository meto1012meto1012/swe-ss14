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

package de.shop.kundenverwaltung.soap;

import de.shop.kundenverwaltung.business.KundeService;
import de.shop.kundenverwaltung.business.KundeService.FetchType;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.util.soap.ConstraintViolationInterceptor;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ws.api.annotation.WebContext;

import static de.shop.auth.domain.RolleType.ADMIN_STRING;
import static de.shop.auth.domain.RolleType.MITARBEITER_STRING;
import static de.shop.util.Constants.SHOP_DOMAIN;
import static javax.ejb.TransactionAttributeType.SUPPORTS;

/**
 * https://localhost:8443/shop/KundeSOAPService/KundeSOAP?wsdl
 * standalone\data\wsdl\shop.war\KundeSOAPService.wsdl
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@WebService(name = "KundeSOAP",
            targetNamespace = "urn:shop:soap:kunde",
            serviceName = "KundeSOAPService")
@SOAPBinding  // default: document/literal (einzige Option fuer Integration mit .NET)
@WebContext(authMethod = "BASIC",
            transportGuarantee = "CONFIDENTIAL",    // TLS durch "INTEGRAL" oder "CONFIDENTIAL"; sonst "NONE"
            secureWSDLAccess = true)
@Stateless
@Interceptors(ConstraintViolationInterceptor.class)
@RolesAllowed({ ADMIN_STRING, MITARBEITER_STRING })
@SecurityDomain(SHOP_DOMAIN)
public class KundeSOAP {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private KundeService ks;
	
	@WebResult(name = "version")
	@TransactionAttribute(SUPPORTS)
	public String getVersion() {
		return "1.0";
	}
	
	@WebResult(name = "kunde")
	public PrivatkundeVO findPrivatkundeById(@WebParam(name = "id") long id) {
		final AbstractKunde kunde = ks.findKundeById(id, FetchType.MIT_BESTELLUNGEN);
		if (!(kunde instanceof Privatkunde)) {
			return null;
		}
		
		final Privatkunde privatkunde = Privatkunde.class.cast(kunde);
		return new PrivatkundeVO(privatkunde);
	}
	
	@WebResult(name = "kunden")
	public List<PrivatkundeVO> findPrivatkundenByNachname(@WebParam(name = "nachname") String nachname) {
		final List<AbstractKunde> kunden = ks.findKundenByNachname(nachname, FetchType.MIT_BESTELLUNGEN);

		final List<PrivatkundeVO> result =  kunden.parallelStream()
			                                      .filter(kunde -> (kunde instanceof Privatkunde))
			                                      .map(kunde -> (Privatkunde) kunde)
			                                      .map(privatkunde -> new PrivatkundeVO(privatkunde))
			                                      .collect(Collectors.toList());
		
		return result;
	}
	
	@WebResult(name = "id")
	@PermitAll
	public Long createPrivatkunde(@WebParam(name = "kunde") PrivatkundeVO privatkundeVO) {
		final Privatkunde privatkunde = privatkundeVO.toPrivatkunde();
		final AbstractKunde result = ks.createKunde(privatkunde);
		return result.getId();
	}
}
