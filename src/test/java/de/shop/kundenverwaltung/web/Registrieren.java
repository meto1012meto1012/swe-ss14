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
import de.shop.kundenverwaltung.domain.FamilienstandType;
import de.shop.kundenverwaltung.domain.GeschlechtType;
import de.shop.kundenverwaltung.domain.HobbyType;
import de.shop.util.AbstractPage;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.xpath;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class Registrieren extends AbstractPage {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static final Path CAPTCHA_FILE_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "capchta.txt");
	
	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
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
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <h:panelGrid columns="3" ...>
	 *	       <input jsf:id="vorname" type="text" ...>
	 * HTML:
	 *   <input id="vorname" type="text" ...>
	 * </pre>
	 */
	@FindBy(id = "vorname")
	private WebElement vornameFeld;
	
	/**
	 * * <pre>
	 * RichFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *     <r:inputNumberSlider id="kategorie" ...>
	 * HTML:
	 *   ...
	 *   <span id="kategorie" class="rf-insl">
     *      <span class="rf-insl-trc-cntr">
     *         <span class="rf-insl-trc" ...>
     *            <span class="rf-insl-hnd-cntr" ...>
     *               <span class="rf-insl-hnd">
	 */
	@FindBys({
		@FindBy(id = "kategorie"),
		@FindBy(className = "rf-insl-hnd")
	})
	private WebElement kategorieSchieberegler;
	
	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <r:calendar id="seit" ...>
	 * HTML:
	 *    ...
	 *    <span id="seit" ...>
	 *       <span id="seitPopup" ...>
	 *          <input id="seitInputDate" ...>
	 *          <img id="seitPopupButton" ...>
	 * </pre>
	 */
	@FindBy(id = "seitPopupButton")
	private WebElement kalenderButton;
	
	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="registriereForm"  jsf:prependId="false">
	 *      <r:calendar id="seit" ...>
	 * HTML:
	 *    ...
	 *    <span id="seit" ...>
	 * </pre>
	 */
	@FindBy(id = "seit")
	private WebElement kalenderElem;
	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <label id="familienstandLabel" ...>
	 * HTML:
	 *   <label id="familienstandLabel" ...>
	 * </pre>
	 */
	@FindBy(id = "familienstandLabel")
	private WebElement familienstandLabel;
	
	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <r:select id="familienstand"
	 * HTML:
	 *   <div id="familienstand" ...>
	 *      ...
	 *      <span id="familienstandButton"...>
	 *         <span class="rf-sel-btn-arrow">
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "familienstandButton"),
		@FindBy(className = "rf-sel-btn-arrow")
	})
	private WebElement familienstandArrow;

	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <r:select id="familienstand"
	 * HTML:
	 *   <div id="familienstand" ...>
	 *      ...
	 *      <span id="familienstandButton"...>
	 *         <span class="rf-sel-btn-arrow">
	 *      <div id="familienstandList" ...>
	 *         <div id="familienstandItems" ...>
	 *         
	 * </pre>
	 */
	@FindBy(id = "familienstandItems")
	private WebElement familienstandItems;
	
	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <r:select id="familienstand"
	 *         <f:selectItem id="ledig" itemLabel="ledig" ...>
	 * HTML:
	 *   <div id="familienstandItems" ...>
	 *      <div id="familienstandItem0" ...>ledig
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "familienstandItems"),
		@FindBy(xpath = "div[text()='ledig']")
	})
	private WebElement ledigOption;

	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <r:select id="familienstand"
	 *         <f:selectItem id="verheiratet" itemLabel="verheiratet" ...>
	 * HTML:
	 *   <div id="familienstandItems" ...>
	 *      <div id="familienstandItem1" ...>verheiratet
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "familienstandItems"),
		@FindBy(xpath = "div[text()='verheiratet']")
	})
	private WebElement verheiratetOption;
	
	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <r:select id="familienstand"
	 *         <f:selectItem id="geschieden" itemLabel="geschieden" ...>
	 * HTML:
	 *   <div id="familienstandItems" ...>
	 *      <div id="familienstandItem2" ...>geschieden
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "familienstandItems"),
		@FindBy(xpath = "div[text()='geschieden']")
	})
	private WebElement geschiedenOption;
	
	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <r:select id="familienstand"
	 *         <f:selectItem id="verwitwet" itemLabel="verwitwet" ...>
	 * HTML:
	 *   <div id="familienstandItems" ...>
	 *      <div id="familienstandItem3" ...>verwitwet
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "familienstandItems"),
		@FindBy(xpath = "div[text()='verwitwet']")
	})
	private WebElement verwitwetOption;
	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <h:selectOneRadio id="geschlecht" ...>
	 *         <f:selectItem id="weiblich" ...>
	 * HTML:
	 *   <table id="geschlecht" ...>
	 *      <tbody>
	 *         <tr>
	 *            <td>
	 *               <input id="geschlecht:0" type="radio"
	 * </pre>
	 */
	@FindBy(id = "geschlecht:0")
	private WebElement weiblichRadio;
	
	@FindBy(id = "geschlecht:1")
	private WebElement maennlichRadio;
	
	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <input jsf:id="newsletter" type="checkbox"
	 * HTML:
	 *   <input id="newsletter" type="checkbox"
	 * </pre>
	 */
	@FindBy(id = "newsletter")
	private WebElement newsletterCheckbox;

	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <h:panelGrid id="registriereTabelle" columns="3" ...>
	 *         <h:selectManyCheckbox layout="pageDirection" ...>
	 *            <f:selectItem itemValue="SPORT"
	 * HTML:
	 *   <table id="registriereTabelle">
	 *      <tbody>
	 *         <tr>
	 *            <td>
	 *               <table>
	 *                  <tbody>
	 *                     <tr>
	 *                        <td>
	 *                           <input id="..." type="checkbox" value="SPORT" ...>
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "registriereTabelle"),
		@FindBy(xpath = "tbody//input[@type='checkbox' and @value='SPORT']")
	})
	private WebElement sportCheckbox;
	
	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <h:panelGrid id="registriereTabelle" columns="3" ...>
	 *         <h:selectManyCheckbox layout="pageDirection" ...>
	 *            <f:selectItem itemValue="LESEN"
	 * HTML:
	 *   <table id="registriereTabelle">
	 *      <tbody>
	 *         <tr>
	 *            <td>
	 *               <table>
	 *                  <tbody>
	 *                     <tr>
	 *                        <td>
	 *                           <input id="..." type="checkbox" value="LESEN" ...>
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "registriereTabelle"),
		@FindBy(xpath = "tbody//input[@type='checkbox' and @value='LESEN']")
	})
	private WebElement lesenCheckbox;
	
	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <h:panelGrid id="registriereTabelle" ...>
	 *         <h:selectManyCheckbox layout="pageDirection" ...>
	 *            <f:selectItem itemValue="REISEN"
	 * HTML:
	 *   <table id="registriereTabelle">
	 *      <tbody>
	 *         <tr>
	 *            <td>
	 *               <table>
	 *                  <tbody>
	 *                     <tr>
	 *                        <td>
	 *                           <input id="..." type="checkbox" value="REISEN" ...>
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "registriereTabelle"),
		@FindBy(xpath = "tbody//input[@type='checkbox' and @value='REISEN']")
	})
	private WebElement reisenCheckbox;

	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <input jsf:id="email" type="text" ...>
	 * HTML:
	 *   <input id="email" type="text" ...>
	 * </pre>
	 */
	@FindBy(id = "email")
	private WebElement emailFeld;
	
	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <input jsf:id="password" type="password" ...>
	 * HTML:
	 *   <input id="password" type="password" ...>
	 * </pre>
	 */
	@FindBy(id = "password")
	private WebElement passwordFeld;
	
	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <input jsf:id="passwordWdh" type="password" ...>
	 * HTML:
	 *   <input id="passwordWdh" type="password" ...>
	 * </pre>
	 */
	@FindBy(id = "passwordWdh")
	private WebElement passwordWdhFeld;
	
	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <input jsf:id="plz" type="text" ...>
	 * HTML:
	 *   <input id="plz" type="text" ...>
	 * </pre>
	 */
	@FindBy(id = "plz")
	private WebElement plzFeld;

	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <input jsf:id="ort" type="text" ...>
	 * HTML:
	 *   <input id="ort" type="text" ...>
	 * </pre>
	 */
	@FindBy(id = "ort")
	private WebElement ortFeld;
	
	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <input jsf:id="strasse" type="text" ...>
	 * HTML:
	 *   <input id="strasse" type="text" ...>
	 * </pre>
	 */
	@FindBy(id = "strasse")
	private WebElement strasseFeld;
	
	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <input jsf:id="hausnr" type="text" ...>
	 * HTML:
	 *   <input id="hausnr" type="text" ...>
	 * </pre>
	 */
	@FindBy(id = "hausnr")
	private WebElement hausnrFeld;
	
	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <r:mediaOutput id="captcha" ...
	 *      <span jsf:id="captchaInputGroup">
	 *         <input jsf:id="captchaInput" ...
	 * HTML:
	 *   <input id="captchaInput" ...
	 * </pre>
	 */
	@FindBy(id = "captchaInput")
	private WebElement captcha;

	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <r:mediaOutput id="captcha"
	 *      <span jsf:id="captchaInputGroup">
	 *         <input jsf:id="captchaInput" type="text" jsf:value="#{kundeModel.captchaInput}"/>
	 *      </span>
	 *      <r:message id="fehlermeldungCaptcha" for="captchaInput"/>
	 * HTML:
	 *   <div id="familienstand" ...>
	 *      <span ...>
	 *         <span class="rf-sel-btn-arrow">
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "fehlermeldungCaptcha:captchaInput"),
		@FindBy(className = "rf-msg-det")
	})
	private WebElement captchaFehlermeldung;
	
	/**
	 * <pre>
	 * JSF:
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <input jsf:id="agb" type="checkbox" ...>
	 * HTML:
	 *   <input id="agb" type="checkbox" ...>
	 * </pre>
	 */
	@FindBy(id = "agb")
	private WebElement agbCheckbox;
	
	public WebElement getNachnameFeld() {
		return nachnameFeld;
	}

	/**
	 * <pre>
	 * JSF
	 *   <form jsf:id="registriereForm" jsf:prependId="false">
	 *      <p>
	 *         <input jsf:id="createSubmit" type="submit" ...>
	 * HTML:
	 *   <input type="submit" id="createSubmit" ...>
	 * </pre>
	 */
	@FindBy(id = "createSubmit")
	private WebElement anlegenButton;

	public Registrieren inputNachname(String nachname) {
		nachnameFeld.sendKeys(nachname);
		return this;
	}
	
	public Registrieren inputVorname(String vorname) {
		vornameFeld.sendKeys(vorname);
		return this;
	}
	
	public Registrieren schiebeKategorie(WebDriver browser, int offsetX) {
		new Actions(browser).clickAndHold(kategorieSchieberegler)
				            .moveByOffset(offsetX, 0)
				            .release()
							.build()
							.perform();
		return this;
	}
	
	public Registrieren clickTag(int tag) {
		kalenderButton.click();
		// RichFaces:
		//   <form jsf:id="registriereForm" jsf:prependId="false">
		//      <r:calendar id="seit" ...>
		// HTML:
		//   ...
		//   <span id="seit" ...>
		//      <table ...>
		//         <tr ...>
		//            <td ...>1</td>
		kalenderElem.findElement(xpath("//td[text()='" + tag + "']"))
		            .click();
		return this;
	}
	
	public Registrieren clickFamilienstand(FamilienstandType familienstand) {
		familienstandLabel.click();  // Workaround, damit der Pfeil der Combobox sichtbar wird
		familienstandArrow.click();
		switch (familienstand) {
			case LEDIG:
				ledigOption.click();
				break;
			case VERHEIRATET:
				verheiratetOption.click();
				break;
			case GESCHIEDEN:
				geschiedenOption.click();
				break;
			case VERWITWET:
				verwitwetOption.click();
				break;
			default:
				break;
		}
		return this;
	}
	
	public Registrieren clickGeschlecht(GeschlechtType geschlecht) {
		switch (geschlecht) {
			case WEIBLICH:
				weiblichRadio.click();
				break;
			case MAENNLICH:
				maennlichRadio.click();
				break;
			default:
				break;
		}
		return this;
	}
	

	public Registrieren clickNewsletter(boolean newsletter) {
		if (newsletter) {
			newsletterCheckbox.click();
		}
		return this;
	}
	
	public Registrieren clickHobbies(HobbyType[] hobbies) {
		for (HobbyType hobby : hobbies) {
			switch (hobby) {
				case SPORT:
					sportCheckbox.click();
					break;
				case LESEN:
					lesenCheckbox.click();
					break;
				case REISEN:
					reisenCheckbox.click();
					break;
				default:
					break;
			}
		}
		return this;
	}

	public Registrieren inputEmail(String email) {
		emailFeld.sendKeys(email);
		return this;
	}

	public Registrieren inputPassword(String password) {
		passwordFeld.sendKeys(password);
		return this;
	}

	public Registrieren inputPasswordWdh(String passwordWdh) {
		passwordWdhFeld.sendKeys(passwordWdh);
		return this;
	}

	public Registrieren inputPlz(String plz) {
		plzFeld.sendKeys(plz);
		return this;
	}
	
	public Registrieren inputOrt(String ort) {
		ortFeld.sendKeys(ort);
		return this;
	}
	
	public Registrieren inputStrasse(String strasse) {
		strasseFeld.sendKeys(strasse);
		return this;
	}
	
	public Registrieren inputHausnr(String hausnr) {
		hausnrFeld.sendKeys(hausnr);
		return this;
	}
	
	public Registrieren clickAgb(boolean agb) {
		if (agb) {
			agbCheckbox.click();
		}
		return this;
	}

	public void clickAnlegenButton() {
		anlegenButton.click();
	}

	public Registrieren inputCaptcha() {
		// Voraussetzung: fuer die serverseitige Klasse Captcha ist der Log-Level ALL eingeschaltet
		byte[] captchaStr;
		try {
			captchaStr = Files.readAllBytes(CAPTCHA_FILE_PATH);
		}
		catch (IOException e) {
			LOGGER.warning("Die Datei " + CAPTCHA_FILE_PATH.getFileName()
					       + " mit dem Captcha-String kann nicht gelesen werden: " + e.getMessage());
			return this;
		}
		captcha.sendKeys(new String(captchaStr));
		return this;
	}
	
	public WebElement getCaptchaFehlermeldung() {
		return captchaFehlermeldung;
	}
	
	public List<String> getFehlermeldungenNachname() {
		return body.findElement(id("fehlermeldungenNachname"))
				   .findElements(className("rf-msgs-sum"))
		           .stream()
				   .map(WebElement::getText)
		           .filter(txt -> !Strings.isNullOrEmpty(txt))
				   .collect(Collectors.toList());
	}
}
