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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static java.util.logging.Level.WARNING;
import static org.jboss.arquillian.graphene.Graphene.waitAjax;
import static org.jboss.arquillian.graphene.Graphene.waitModel;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;


/**
* @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
*/
public abstract class AbstractPage {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private boolean loggedIn = false;

	//=========================================================================
	// Navigationsleiste
	//=========================================================================
	
	/**
	 * <pre>
	 * JSF:
	 *   <a jsf:id="artikelverwaltungListArtikel" ...>Suche Artikel
	 * HTML:
	 *   <a id="artikelverwaltungListArtikel" name="..." href="..."> Suche Artikel </a>
	 * </pre>
	 */
	@FindBy(id = "artikelverwaltungListArtikel")
	private WebElement linkSucheArtikel;
	
	/**
	 * <pre>
	 * JSF:
	 *   <a jsf:id="artikelverwaltungSelectArtikel" ...>Artikel Auswahl
	 * HTML:
	 *   <a id=artikelverwaltungSelectArtikel" name="..." href="..."> Artikel Auswahl </a>
	 * </pre>
	 */
	@FindBy(id = "artikelverwaltungSelectArtikel")
	private WebElement linkArtikelAuswahl;
	
	/**
	 * <pre>
	 * JSF:
	 *   <a jsf:id="kundenverwaltungViewKunde" ...>Suche mit Kundennr.
	 * HTML:
	 *   <a id="kundenverwaltungViewKunde" name="..." href="..."> Suche mit Kundennr. </a>
	 * </pre>
	 */
	@FindBy(id = "kundenverwaltungViewKunde")
	private WebElement linkSucheMitKundennr;
	
	/**
	 * <pre>
	 * JSF:
	 *   <a jsf:id="kundenverwaltungListKunden" ...>Suche mit Nachname
	 * HTML:
	 *   <a id="kundenverwaltungListKunden" name="..." href="..."> Suche mit Nachname </a>
	 * </pre>
	 */
	@FindBy(id = "kundenverwaltungListKunden")
	private WebElement linkSucheMitNachname;
	

	//=========================================================================
	// Kopfleiste
	//=========================================================================

	/**
	 * <pre>
	 * JSF:
	 *   <a jsf:id="homeLink" ...
	 *      <img jsf:id="homeLogo" ...
	 * HTML:
	 *   <img id="hsLogo" ...
	 * </pre>
	 */
	@FindBy(id = "homeLogo")
	private WebElement home;
	
	/**
	 * <pre>
	 * RichFaces:
	 *   <form id="loginFormHeader" prependId="false">
	 *      <input jsf:id="usernameHeader" type="text" ...>
	 * HTML:
	 *   <form id="loginFormHeader" ...>
	 *      <div id="usernameHeader">
	 * </pre>
	 */
	@FindBy(id = "loginFormHeader")
	private WebElement loginForm;
	
	@FindBy(id = "usernameHeader")
	private WebElement usernameFeld;
	
	@FindBy(id = "passwordHeader")
	private WebElement passwordFeld;
	
	@FindBy(id = "loginButtonHeader")
	private WebElement loginButton;
	
	/**
	 * <pre>
	 * JSF:
	 *   <a jsf:id="registrieren" ...>Registrieren
	 * HTML:
	 *   <a id="registrieren" name="..." href="..."> Registrieren </a>
	 * </pre>
	 */
	@FindBy(id = "registrieren")
	private WebElement linkRegistrieren;
	
	@FindBy(tagName = "body")
	protected WebElement body;

	public WebElement getLinkSucheArtikel() {
		return linkSucheArtikel;
	}
	
	public WebElement getLinkArtikelAuswahl() {
		return linkArtikelAuswahl;
	}
	
	public void clickSucheArtikel() {
		linkSucheArtikel.click();
	}
	
	public void clickArtikelAuswahl() {
		linkArtikelAuswahl.click();
	}
	
	public WebElement getLinkSucheMitKundennr() {
		return linkSucheMitKundennr;
	}

	public void setLinkSucheMitKundennr(WebElement linkSucheMitKundennr) {
		this.linkSucheMitKundennr = linkSucheMitKundennr;
	}

	public void clickSucheMitKundennr() {
		linkSucheMitKundennr.click();
	}

	public WebElement getLinkSucheMitNachname() {
		return linkSucheMitNachname;
	}

	public void setLinkSucheMitNachname(WebElement linkSucheMitNachname) {
		this.linkSucheMitNachname = linkSucheMitNachname;
	}
	
	public void clickSucheMitNachname() {
		linkSucheMitNachname.click();
	}
	
	public void clickRegistrieren() {
		linkRegistrieren.click();
	}

	public WebElement getUsernameFeld() {
		return usernameFeld;
	}
	
	public void login(String username, String password) {
		usernameFeld.clear();
		usernameFeld.sendKeys(username);
		passwordFeld.clear();
		passwordFeld.sendKeys(password);
		loginButton.click();
		
		// "Mein Konto" ist bei erfolgreichem Login sichtbar
		// RichFaces:
		//   <form jsf:id="kontoForm" ...>
		//      <r:dropDownMenu id="kontoMenu">
		//          <f:facet name="label">
		// HTML:
		//   <form id="kontoForm"
		//      <div id="kontoMenu" ...>
		//         <div id="kontoMenu_label"
		try {
			waitAjax().until().element(body.findElement(id("kontoMenu_label"))).is().visible();
		}
		catch (NoSuchElementException e) {
			// Login ist fehlgeschlagen, z.B. wegen eines falschen Passworts
			LOGGER.finer("Login ist fehlgeschlagen: " + e.getMessage());
			return;
		}
		
		loggedIn = true;
	}
	
	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	public WebElement getFehlermeldungLogin() {
		// RichFaces:
		//   <form id="loginFormHeader" prependId="false">
		//      <input jsf:id="usernameHeader" type="text" ...>
		// HTML:
		//   <form id="loginFormHeader" ...>
		//      <span id="fehlermeldungLogin"...>
		//         <span id="fehlermeldungLogin:usernameHeader" ...>
        //            <span class="rf-msg-det">
		waitAjax().until().element(loginForm.findElement(id("fehlermeldungLogin"))).is().visible();
		return loginForm.findElement(id("fehlermeldungLogin:usernameHeader"))
				        .findElement(className("rf-msg-det"));
	}
	
	public void logout(WebDriver browser) {
		if (!loggedIn) {
			return;
		}
		
		// RichFaces:
		//   <form jsf:id="kontoForm" ...>
		//      <r:dropDownMenu id="kontoMenu">
		//          <f:facet name="label">
		// HTML:
		//   <form id="kontoForm"
		//      <div id="kontoMenu" ...>
		//         <div id="kontoMenu_label"
		WebElement kontoMenu;
		try {
			kontoMenu = body.findElement(id("kontoMenu_label"));
		}
		catch (NoSuchElementException e) {
			LOGGER.finest("Kein Logout-Menue: " + e.getMessage());
			return;
		}
		new Actions(browser).moveToElement(kontoMenu)
							.build()
							.perform();

		waitModel().until().element(body.findElement(id("logout"))).is().visible();
		body.findElement(id("logout"))
			.findElement(className("rf-ddm-itm-lbl"))
			.click();
		loggedIn = false;

		// Vorsichtsmassnahme, damit beim Testen keine Drop-Down-Menues herunterklappen
		// und evtl. andere Menuepunkte verdecken
		home.click();
	}
	
	/**
	 * Hilfsmethode: irgendwo hinclicken (z.B. body), um das Blur-Ereignis auszuloesen
	 */
	public void clickBody() {
		body.click();
	}

	public static void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		}
		catch (InterruptedException e) {
			LOGGER.log(WARNING, e.getMessage());
		}
	}
}
