
package de.shop.kundenverwaltung.soap.gen;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für privatkundeVO complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="privatkundeVO">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="adresse" type="{urn:shop:soap:kunde}adresseVO" minOccurs="0"/>
 *         &lt;element name="agbAkzeptiert" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="bemerkungen" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bestellungenIds" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="email" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="familienstand" type="{urn:shop:soap:kunde}familienstandType" minOccurs="0"/>
 *         &lt;element name="geschlecht" type="{urn:shop:soap:kunde}geschlechtType" minOccurs="0"/>
 *         &lt;element name="hobbies" type="{urn:shop:soap:kunde}hobbyType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="kategorie" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="nachname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rabatt" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="rollen" type="{urn:shop:soap:kunde}rolleType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="seit" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="umsatz" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="vorname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "privatkundeVO", propOrder = {
    "adresse",
    "agbAkzeptiert",
    "bemerkungen",
    "bestellungenIds",
    "email",
    "familienstand",
    "geschlecht",
    "hobbies",
    "id",
    "kategorie",
    "nachname",
    "password",
    "rabatt",
    "rollen",
    "seit",
    "umsatz",
    "version",
    "vorname"
})
public class PrivatkundeVO {

    protected AdresseVO adresse;
    protected boolean agbAkzeptiert;
    protected String bemerkungen;
    @XmlElement(nillable = true)
    protected List<Long> bestellungenIds;
    protected String email;
    protected FamilienstandType familienstand;
    protected GeschlechtType geschlecht;
    @XmlElement(nillable = true)
    protected List<HobbyType> hobbies;
    protected Long id;
    protected int kategorie;
    protected String nachname;
    protected String password;
    protected BigDecimal rabatt;
    @XmlElement(nillable = true)
    protected List<RolleType> rollen;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar seit;
    protected BigDecimal umsatz;
    protected int version;
    protected String vorname;

    /**
     * Ruft den Wert der adresse-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AdresseVO }
     *     
     */
    public AdresseVO getAdresse() {
        return adresse;
    }

    /**
     * Legt den Wert der adresse-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AdresseVO }
     *     
     */
    public void setAdresse(AdresseVO value) {
        this.adresse = value;
    }

    /**
     * Ruft den Wert der agbAkzeptiert-Eigenschaft ab.
     * 
     */
    public boolean isAgbAkzeptiert() {
        return agbAkzeptiert;
    }

    /**
     * Legt den Wert der agbAkzeptiert-Eigenschaft fest.
     * 
     */
    public void setAgbAkzeptiert(boolean value) {
        this.agbAkzeptiert = value;
    }

    /**
     * Ruft den Wert der bemerkungen-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBemerkungen() {
        return bemerkungen;
    }

    /**
     * Legt den Wert der bemerkungen-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBemerkungen(String value) {
        this.bemerkungen = value;
    }

    /**
     * Gets the value of the bestellungenIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bestellungenIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBestellungenIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getBestellungenIds() {
        if (bestellungenIds == null) {
            bestellungenIds = new ArrayList<Long>();
        }
        return this.bestellungenIds;
    }

    /**
     * Ruft den Wert der email-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Legt den Wert der email-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Ruft den Wert der familienstand-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FamilienstandType }
     *     
     */
    public FamilienstandType getFamilienstand() {
        return familienstand;
    }

    /**
     * Legt den Wert der familienstand-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FamilienstandType }
     *     
     */
    public void setFamilienstand(FamilienstandType value) {
        this.familienstand = value;
    }

    /**
     * Ruft den Wert der geschlecht-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GeschlechtType }
     *     
     */
    public GeschlechtType getGeschlecht() {
        return geschlecht;
    }

    /**
     * Legt den Wert der geschlecht-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GeschlechtType }
     *     
     */
    public void setGeschlecht(GeschlechtType value) {
        this.geschlecht = value;
    }

    /**
     * Gets the value of the hobbies property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hobbies property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHobbies().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HobbyType }
     * 
     * 
     */
    public List<HobbyType> getHobbies() {
        if (hobbies == null) {
            hobbies = new ArrayList<HobbyType>();
        }
        return this.hobbies;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setId(Long value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der kategorie-Eigenschaft ab.
     * 
     */
    public int getKategorie() {
        return kategorie;
    }

    /**
     * Legt den Wert der kategorie-Eigenschaft fest.
     * 
     */
    public void setKategorie(int value) {
        this.kategorie = value;
    }

    /**
     * Ruft den Wert der nachname-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNachname() {
        return nachname;
    }

    /**
     * Legt den Wert der nachname-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNachname(String value) {
        this.nachname = value;
    }

    /**
     * Ruft den Wert der password-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Legt den Wert der password-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Ruft den Wert der rabatt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getRabatt() {
        return rabatt;
    }

    /**
     * Legt den Wert der rabatt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setRabatt(BigDecimal value) {
        this.rabatt = value;
    }

    /**
     * Gets the value of the rollen property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rollen property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRollen().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RolleType }
     * 
     * 
     */
    public List<RolleType> getRollen() {
        if (rollen == null) {
            rollen = new ArrayList<RolleType>();
        }
        return this.rollen;
    }

    /**
     * Ruft den Wert der seit-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSeit() {
        return seit;
    }

    /**
     * Legt den Wert der seit-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSeit(XMLGregorianCalendar value) {
        this.seit = value;
    }

    /**
     * Ruft den Wert der umsatz-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getUmsatz() {
        return umsatz;
    }

    /**
     * Legt den Wert der umsatz-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setUmsatz(BigDecimal value) {
        this.umsatz = value;
    }

    /**
     * Ruft den Wert der version-Eigenschaft ab.
     * 
     */
    public int getVersion() {
        return version;
    }

    /**
     * Legt den Wert der version-Eigenschaft fest.
     * 
     */
    public void setVersion(int value) {
        this.version = value;
    }

    /**
     * Ruft den Wert der vorname-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVorname() {
        return vorname;
    }

    /**
     * Legt den Wert der vorname-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVorname(String value) {
        this.vorname = value;
    }

}
