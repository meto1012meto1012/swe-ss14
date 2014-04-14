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

package de.shop.util.web;

import de.shop.auth.business.AuthService;
import de.shop.util.interceptor.Log;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;

import static java.util.logging.Level.FINEST;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@SessionScoped
@Log
public class Captcha implements Serializable {
	private static final long serialVersionUID = -2422584806291795656L;
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static final Path CAPTCHA_FILE_PATH = Paths.get(System.getProperty("java.io.tmpdir"), "capchta.txt");
	
	private static final int WIDTH = 110;
	private static final int HEIGHT = 50;
	private static final int X_COORD = 20;
	private static final int Y_COORD = 35;
	private static final Color BACKGROUND = new Color(190, 214, 248);
	private static final Color DRAW_COLOR = new Color(0, 0, 0);
	private static final Font FONT = new Font(Font.SERIF, Font.TRUETYPE_FONT, 30);
	private static final int CAPTCHA_LENGTH = 4;
	
	private String value;
	
	private Random generator;
	
	@Inject
	private AuthService authService;
	
	@PostConstruct
	private void postConstruct() {
		generator = new Random(System.currentTimeMillis());
	}

	public String getValue() {
		return value;
	}
	
	public void paint(OutputStream stream, Object unused) throws IOException {
		value = generateString();
		
		final BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		final Graphics2D graphics2D = img.createGraphics();
		graphics2D.setBackground(BACKGROUND);
		graphics2D.setColor(DRAW_COLOR);
		graphics2D.clearRect(0, 0, WIDTH, HEIGHT); // x, y, Breite, Hoehe
		graphics2D.setFont(FONT);
		graphics2D.drawString(value, X_COORD, Y_COORD); // String zeichnen an x-/y-Koordinaten
		
		ImageIO.write(img, "png", stream);  // PNG-Bild erzeugen
	}
	
	private String generateString() throws IOException {
		// Zufallszahl generieren
		final int randomNumber = generator.nextInt(100);
		
		// Zufallszahl wie ein Passwort verschluesseln und dadurch einen Base64-String produzieren
		final char[] hashValue = authService.verschluesseln(String.valueOf(randomNumber)).toCharArray();
		
		// die ersten 4 Buchstaben und Ziffern aus dem Base64-String extrahieren
		final StringBuilder sb = new StringBuilder();
		for (char c : hashValue) {
			if (Character.isLetterOrDigit(c)) {
				sb.append(c);
			}
			if (sb.length() == CAPTCHA_LENGTH) {
				break;
			}
		}
		
		if (sb.length() < CAPTCHA_LENGTH) {
			// Der Base64-String enthaelt weniger als 4 Buchstaben und Ziffern, d.h. fast nur + und /
			return generateString();
		}
		
		final String result = sb.toString();
		if (LOGGER.isLoggable(FINEST)) {
			Files.write(CAPTCHA_FILE_PATH, result.getBytes());
		}
		
		return result;
	}
}
