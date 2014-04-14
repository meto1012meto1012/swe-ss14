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

package de.shop.bestellverwaltung.web;

import de.shop.util.AbstractPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class WarenkorbPage extends AbstractPage {
	private static final String WARENKORBTABELLE = "warenkorbTabelle";
	/**
	 * <pre>
	 * RichFaces
	 *   <form jsf:id="warenkorbForm" jsf:prependId="false">
	 *      <r:dataTable id="warenkorbTabelle"
	 * HTML:
	 *   <form id="warenkorbForm" ...>
	 *      <table id="warenkorbTabelle" ...>
	 * </pre>
	 */
	@FindBy(id = WARENKORBTABELLE)
	private WebElement warenkorbTabelle;
	
	/**
	 * <pre>
	 * RichFaces
	 *   <form jsf:id="warenkorbForm" jsf:prependId="false">
	 *      <r:dataTable id="warenkorbTabelle"
	 *         ...
	 *         <f:facet name="footer">
	 *            <span jsf:id="buttons">
	 *               <input jsf:id="bestellenButton" type="submit"
	 * HTML:
	 *   <form id="warenkorbForm" ...>
	 *      <table id="warenkorbTabelle" ...>
	 *         <tfoot id="warenkorbTabelle:tf" ...>
	 *            <span id="warenkorbTabelle:buttons">
	 *               <input id="warenkorbTabelle:bestellenButton" type="submit" ...>
	 * </pre>
	 */
	@FindBy(id = "warenkorbTabelle:bestellenButton")
	private WebElement bestellenButton;
	
	
	public WarenkorbPage setAnzahl(int zeilennr, int anzahlKlicks) {
		// RichFaces
		//   <form jsf:id="warenkorbForm" jsf:prependId="false">
		//      <r:dataTable id="warenkorbTabelle"
		//         <r:column id="anzahlSpalte">
		//            <r:inputNumberSpinner id="anzahl" ...>
		// HTML:
		//   <form id="warenkorbForm" ...>
		//      <table id="warenkorbTabelle" ...>
		//         <tbody id="warenkorbTabelle:tb" ...>
		//            <tr id="warenkorbTabelle:0" ...>
		//               <td id="warenkorbTabelle:0:anzahlSpalte" ...>
		//                  <span id="warenkorbTabelle:anzahl" ...>
		//                     <span class="rf-insp-btns">
		//                        <span class="rf-insp-inc" ...>
		final WebElement incButton = warenkorbTabelle.findElement(id(WARENKORBTABELLE + ":" + zeilennr
				                                                     + ":anzahlSpalte"))
				                                     .findElement(className("rf-insp-inc"));
		for (int i = 0; i < anzahlKlicks; i++) {
			incButton.click();
		}
		return this;
	}
	
	public WebElement getBestellenButton() {
		return bestellenButton;
	}
	
	public void bestellen() {
		bestellenButton.click();
	}
}
