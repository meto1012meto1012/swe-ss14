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

import de.shop.kundenverwaltung.business.KundeService;
import de.shop.kundenverwaltung.business.KundeService.FetchType;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.richfaces.model.UploadedFile;
import org.richfaces.ui.input.fileUpload.FileUploadEvent;

import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;
import static java.util.logging.Level.FINER;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named("fileUploadKunde")  // es koennte aus eine FileUpload-Klasse fuer Artikel geben
@SessionScoped
@Stateful
public class FileUpload implements Serializable {
	private static final long serialVersionUID = 3377481542931338167L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	@Inject
	private KundeService ks;
	
	private Long kundeId;

	private byte[] bytes;
	private String contentType;

	@PostConstruct
	private void postConstruct() {
		// TODO Conversation starten und in upload() beenden.
		// Bug in RichFaces: der Upload-Listener beendet die Conversation, bevor die Methode upload() aufgerufen ist
	}

	@Override
	public String toString() {
		return "FileUpload [kundeId=" + kundeId + "]";
	}
	
	public Long getKundeId() {
		return kundeId;
	}

	public void setKundeId(Long kundeId) {
		this.kundeId = kundeId;
	}

	public void uploadListener(FileUploadEvent event) {
		final UploadedFile uploadedFile = event.getUploadedFile();
		contentType = uploadedFile.getContentType();
		if (LOGGER.isLoggable(FINER)) {
			LOGGER.finer("MIME-Type der hochgeladenen Datei: " + contentType);
		}
		bytes = uploadedFile.getData();
	}

	public String upload() {
		final AbstractKunde kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE);
		ks.setFile(kunde, bytes, contentType);
		return JSF_INDEX + JSF_REDIRECT_SUFFIX;
	}
	
	public String resetUpload() {
		kundeId = null;
		contentType = null;
		bytes = null;
		
		return null;
	}
}
