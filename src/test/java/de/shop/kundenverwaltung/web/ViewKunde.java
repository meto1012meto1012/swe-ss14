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

import com.google.common.base.Strings;
import de.shop.util.AbstractPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.arquillian.graphene.Graphene.waitAjax;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.tagName;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ViewKunde extends AbstractPage {
	//=========================================================================
	// Suchformular
	//=========================================================================

	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      <h:panelGrid id="suchePanelGrid" ...>
	 *         <r:autocomplete id="kundeIdSuche" ...>
	 * HTML:
	 *   ...
	 *      <table id="suchePanelGrid">
	 *         <tbody>
	 *            <tr>
	 *               <td>
	 *                  <span id="kundeIdSuche">
	 *                     <input id="kundeIdSucheInput" type="text" ...>
	 * </pre>
	 */
	@FindBy(id = "kundeIdSucheInput")
	private WebElement kundeIdFeld;
	
	/**
	 * <pre>
	 * RichFaces
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      ...
	 *       <r:commandButton id="findButton" ...>
	 * HTML:
	 *   <input id="findButton" type="submit" ...>
	 * </pre>
	 */
	@FindBy(id = "findButton")
	private WebElement findButton;
	

	//=========================================================================
	// Gefundener Kunde
	//=========================================================================

	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      <r:tabPanel id="tabPanel" ...>
	 * HTML:
	 *   ...
	 *   <div id="tabPanel"
	 * </pre>
	 */
	@FindBy(id = "tabPanel")
	private WebElement tabPanel;

	@FindBy(id = "kundeId")
	private WebElement kundeId;
	
	@FindBy(id = "nachname")
	private WebElement nachname;
	
	@FindBy(id = "vorname")
	private WebElement vorname;

	@FindBy(id = "kategorie")
	private WebElement kategorie;

	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      <r:tabPanel id="tabPanel" ...>
	 *         <r:tab id="bestellungenTab" ...>
	 *            <f:facet name="label">
	 *               <img jsf:id="bestellungenGif"
	 * HTML:
	 *   ...
	 *   <div id="tabPanel"
	 *      <table>
	 *         <tbody>
	 *            <tr>
	 *               <td id="bestellungenTab:header:active"
	 *                  <img jsf:id="bestellungenGif"
	 * </pre>
	 */
	@FindBy(id = "bestellungenGif")
	private WebElement bestellungenTab;
	
	
	
	public WebElement getKundeIdFeld() {
		return kundeIdFeld;
	}
	
	public ViewKunde suchen(String prefix, String kundeId) {
		if (kundeId == null) {
			throw new IllegalArgumentException("kundeId ist null");
		}
		
		kundeIdFeld.clear();    // evtl. Vorbelegung loeschen
		
		if (Strings.isNullOrEmpty(prefix)) {
			kundeIdFeld.sendKeys(prefix);
			waitAjax().until().element(body.findElement(className("ui-autocomplete"))).is().visible();
			
			// RichFaces
			// <form jsf:id="..." jsf:prependId="false">
			//    <r:autocomplete id="kundeIdSuche" ...>
			// JSF:
			//    <ul id="..." class="ui-autocomplete ..." ...>
			//       <li class="ui-menu-item" role="presentation">
			//          <a id="ui-id-7" class="ui-corner-all" tabindex="-1">
			//             102
			body.findElement(className("ui-autocomplete"))
			    .findElements(className("ui-menu-item"))
			    .stream()
			    .map(elem -> elem.findElement(tagName("a")))
			    .filter(elem -> kundeId.equals(elem.getText()))
			    .findFirst()
			    .get()
			    .click();
		}
		else {
			kundeIdFeld.sendKeys(kundeId);
		}
		
		findButton.click();
		return this;
	}
	
	public WebElement getTabPanel() {
		return tabPanel;
	}

	public String getKundeId() {
		return kundeId.getText();
	}

	public WebElement getNachnameFeld() {
		return nachname;
	}
	
	public String getNachname() {
		return nachname.getText();
	}
	
	public String getVorname() {
		return vorname.getText();
	}
	
	public String getKategorie() {
		return kategorie.getText();
	}
	
	public ViewKunde clickBestellungenTab() {
		bestellungenTab.click();
		return this;
	}
}
