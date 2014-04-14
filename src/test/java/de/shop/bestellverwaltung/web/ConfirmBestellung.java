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

import static org.openqa.selenium.By.id;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ConfirmBestellung extends AbstractPage {
	private static final String POSITIONENTABELLE = "positionenTabelle";
	/**
	 * <pre>
	 * RichFaces
	 *   <form jsf:id="bestellungForm" jsf:prependId="false"
	 *      <r:dataTable id="positionenTabelle"
	 * HTML:
	 *   <form id="bestellungForm" ...>
	 *      <table id="positionenTabelle" ...>
	 * </pre>
	 */
	@FindBy(id = POSITIONENTABELLE)
	private WebElement positionenTabelle;

	/**
	 * <pre>
	 * RichFaces
	 *   <form jsf:id="bestellungForm" jsf:prependId="false"
	 *      <r:dataTable id="positionenTabelle"
	 *         ...
	 *         <f:facet name="footer">
	 *            <span id="buttons">
	 *               <input jsf:id="bestellenButton" type="submit"
	 * HTML:
	 *   <form id="bestellungForm" ...>
	 *      <table id="positionenTabelle" ...>
	 *         ...
	 *         <tfoot id="positionenTabelle:tf" ...>
	 *            <tr id="positionenTabelle:f" ...>
	 *               <td ...>
	 *                  <span id="buttons">
	 *                     <input id="positionenTabelle:bestellenButton" type="submit" ...>
	 * </pre>
	 */
	@FindBy(id = "positionenTabelle:bestellenButton")
	private WebElement bestellenButton;
	
	public int getAnzahl(int zeile) {
		// RichFaces
		//   <form jsf:id="bestellungForm" jsf:prependId="false"
		//      <r:dataTable id="positionenTabelle"
		//         <r:column id="anzahlSpalte">
		// HTML:
		//   <form id="bestellungForm" ...>
		//      <table id="positionenTabelle" ...>
		//         <tbody id="positionenTabelle:tb" ...>
		//            <td id="positionenTabelle:0:anzahlSpalte" ...>
		final String anzahlStr = positionenTabelle.findElement(id(POSITIONENTABELLE + ":" + zeile + ":anzahlSpalte"))
				                                  .getText();
		return Integer.parseInt(anzahlStr);
	}
	
	public WebElement getBestellenButton() {
		return bestellenButton;
	}
	
	public void bestellen() {
		bestellenButton.click();
	}
}
