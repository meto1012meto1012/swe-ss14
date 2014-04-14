
package de.shop.kundenverwaltung.soap.gen;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für findPrivatkundenByNachnameResponse complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="findPrivatkundenByNachnameResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="kunden" type="{urn:shop:soap:kunde}privatkundeVO" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "findPrivatkundenByNachnameResponse", propOrder = {
    "kunden"
})
public class FindPrivatkundenByNachnameResponse {

    protected List<PrivatkundeVO> kunden;

    /**
     * Gets the value of the kunden property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the kunden property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKunden().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PrivatkundeVO }
     * 
     * 
     */
    public List<PrivatkundeVO> getKunden() {
        if (kunden == null) {
            kunden = new ArrayList<PrivatkundeVO>();
        }
        return this.kunden;
    }

}
