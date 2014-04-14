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

package de.shop.kundenverwaltung.web;

import de.shop.util.AbstractPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class UpdateKunde extends AbstractPage {
	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="updateKundeForm" jsf:prependId="false">
	 *      <h:panelGrid columns="3" ...>
	 *	       <input jsf:id="nachname" type="text" ...>
	 * HTML:
	 *   <input id="nachname" type="text" ...>
	 * </pre>
	 */
	@FindBy(id = "nachname")
	private WebElement nachnameFeld;
	
	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="updateKundeForm" jsf:prependId="false">
	 *      <h:panelGrid columns="3" ...>
	 *	       <input jsf:id="vorname" type="text" ...>
	 * HTML:
	 *   <input id="vorname" ...>
	 * </pre>
	 */
	@FindBy(id = "vorname")
	private WebElement vornameFeld;

	/** RichFaces:
	 *  <form jsf:id="updateKundeForm" jsf:prependId="false">
	 *    <h:panelGrid ...>
	 *      <r:inputNumberSpinner id="kategorie" ...>
	 * HTML:
	 * <span id="kategorie" ...>
	 *    <input ... type="text" ...>
	 *       <span ...>
	 *          <span class="rf-insp-inc" ...>
	 */
	@FindBys({
		@FindBy(id = "kategorie"),
		@FindBy(className = "rf-insp-inc")
	})
	private WebElement kategorieArrowIncr;

	/** RichFaces:
	 *  <form jsf:id="updateKundeForm" jsf:prependId="false">
	 *    <h:panelGrid ...>
	 *      <r:inputNumberSpinner id="kategorie" ...>
	 * HTML:
	 * <span id="kategorie" ...>
	 *    <input ... type="text" ...>
	 *       <span ...>
	 *          <span class="rf-insp-inc" ...>
	 *          <span class="rf-insp-dec" ...>
	 */
	@FindBys({
		@FindBy(id = "kategorie"),
		@FindBy(className = "rf-insp-dec")
	})
	private WebElement kategorieArrowDecr;

	/**
	 * <pre>
	 * JSF
	 *   <form jsf:id="updateKundeForm" jsf:prependId="false">
	 *      <input jsf:id="updateButton" type="submit" ...>
	 * HTML:
	 *   <input id="updateButton" type="submit" ...>
	 * </pre>
	 */
	@FindBy(id = "updateButton")
	private WebElement updateButton;
	
	/**
	 * <pre>
	 * RichFaces
	 *   <form jsf:id="updateKundeForm" jsf:prependId="false">
	 *      <r:messages id="fehlermeldungenNachname" ...>
	 * HTML
	 *   <span id="fehlermeldungenNachname
	 *      <span class="rf-msgs-sum">Ein Nachname...
	 */
	@FindBys({
		@FindBy(id = "fehlermeldungenNachname"),
		@FindBy(className = "rf-msgs-sum")
	})
	private WebElement fehlermeldungNachname;

	public WebElement getNachnameFeld() {
		return nachnameFeld;
	}
	
	public WebElement getVornameFeld() {
		return vornameFeld;
	}

	public UpdateKunde inputNachname(String nachname) {
		nachnameFeld.clear();
		nachnameFeld.sendKeys(nachname);
		return this;
	}
	
	public UpdateKunde inputVorname(String vorname) {
		vornameFeld.clear();
		vornameFeld.sendKeys(vorname);
		return this;
	}
	
	public UpdateKunde incrKategorie(int anzahl) {
		for (int i = 0; i < anzahl; i++) {
			kategorieArrowIncr.click();
		}
		return this;
	}

	public UpdateKunde decrKategorie(int anzahl) {
		for (int i = 0; i < anzahl; i++) {
			kategorieArrowDecr.click();
		}
		return this;
	}

	public void clickUpdateButton() {
		updateButton.click();
	}

	public WebElement getFehlermeldungNachname() {
		return fehlermeldungNachname;
	}
}
