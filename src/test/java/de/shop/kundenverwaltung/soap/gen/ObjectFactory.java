
package de.shop.kundenverwaltung.soap.gen;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.shop.kundenverwaltung.soap.gen package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _FindPrivatkundenByNachname_QNAME = new QName("urn:shop:soap:kunde", "findPrivatkundenByNachname");
    private final static QName _CreatePrivatkundeResponse_QNAME = new QName("urn:shop:soap:kunde", "createPrivatkundeResponse");
    private final static QName _GetVersionResponse_QNAME = new QName("urn:shop:soap:kunde", "getVersionResponse");
    private final static QName _FindPrivatkundenByNachnameResponse_QNAME = new QName("urn:shop:soap:kunde", "findPrivatkundenByNachnameResponse");
    private final static QName _FindPrivatkundeByIdResponse_QNAME = new QName("urn:shop:soap:kunde", "findPrivatkundeByIdResponse");
    private final static QName _GetVersion_QNAME = new QName("urn:shop:soap:kunde", "getVersion");
    private final static QName _CreatePrivatkunde_QNAME = new QName("urn:shop:soap:kunde", "createPrivatkunde");
    private final static QName _FindPrivatkundeById_QNAME = new QName("urn:shop:soap:kunde", "findPrivatkundeById");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.shop.kundenverwaltung.soap.gen
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetVersion }
     * 
     */
    public GetVersion createGetVersion() {
        return new GetVersion();
    }

    /**
     * Create an instance of {@link CreatePrivatkunde }
     * 
     */
    public CreatePrivatkunde createCreatePrivatkunde() {
        return new CreatePrivatkunde();
    }

    /**
     * Create an instance of {@link FindPrivatkundeById }
     * 
     */
    public FindPrivatkundeById createFindPrivatkundeById() {
        return new FindPrivatkundeById();
    }

    /**
     * Create an instance of {@link CreatePrivatkundeResponse }
     * 
     */
    public CreatePrivatkundeResponse createCreatePrivatkundeResponse() {
        return new CreatePrivatkundeResponse();
    }

    /**
     * Create an instance of {@link GetVersionResponse }
     * 
     */
    public GetVersionResponse createGetVersionResponse() {
        return new GetVersionResponse();
    }

    /**
     * Create an instance of {@link FindPrivatkundenByNachname }
     * 
     */
    public FindPrivatkundenByNachname createFindPrivatkundenByNachname() {
        return new FindPrivatkundenByNachname();
    }

    /**
     * Create an instance of {@link FindPrivatkundenByNachnameResponse }
     * 
     */
    public FindPrivatkundenByNachnameResponse createFindPrivatkundenByNachnameResponse() {
        return new FindPrivatkundenByNachnameResponse();
    }

    /**
     * Create an instance of {@link FindPrivatkundeByIdResponse }
     * 
     */
    public FindPrivatkundeByIdResponse createFindPrivatkundeByIdResponse() {
        return new FindPrivatkundeByIdResponse();
    }

    /**
     * Create an instance of {@link AdresseVO }
     * 
     */
    public AdresseVO createAdresseVO() {
        return new AdresseVO();
    }

    /**
     * Create an instance of {@link PrivatkundeVO }
     * 
     */
    public PrivatkundeVO createPrivatkundeVO() {
        return new PrivatkundeVO();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindPrivatkundenByNachname }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:shop:soap:kunde", name = "findPrivatkundenByNachname")
    public JAXBElement<FindPrivatkundenByNachname> createFindPrivatkundenByNachname(FindPrivatkundenByNachname value) {
        return new JAXBElement<FindPrivatkundenByNachname>(_FindPrivatkundenByNachname_QNAME, FindPrivatkundenByNachname.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreatePrivatkundeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:shop:soap:kunde", name = "createPrivatkundeResponse")
    public JAXBElement<CreatePrivatkundeResponse> createCreatePrivatkundeResponse(CreatePrivatkundeResponse value) {
        return new JAXBElement<CreatePrivatkundeResponse>(_CreatePrivatkundeResponse_QNAME, CreatePrivatkundeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetVersionResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:shop:soap:kunde", name = "getVersionResponse")
    public JAXBElement<GetVersionResponse> createGetVersionResponse(GetVersionResponse value) {
        return new JAXBElement<GetVersionResponse>(_GetVersionResponse_QNAME, GetVersionResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindPrivatkundenByNachnameResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:shop:soap:kunde", name = "findPrivatkundenByNachnameResponse")
    public JAXBElement<FindPrivatkundenByNachnameResponse> createFindPrivatkundenByNachnameResponse(FindPrivatkundenByNachnameResponse value) {
        return new JAXBElement<FindPrivatkundenByNachnameResponse>(_FindPrivatkundenByNachnameResponse_QNAME, FindPrivatkundenByNachnameResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindPrivatkundeByIdResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:shop:soap:kunde", name = "findPrivatkundeByIdResponse")
    public JAXBElement<FindPrivatkundeByIdResponse> createFindPrivatkundeByIdResponse(FindPrivatkundeByIdResponse value) {
        return new JAXBElement<FindPrivatkundeByIdResponse>(_FindPrivatkundeByIdResponse_QNAME, FindPrivatkundeByIdResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetVersion }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:shop:soap:kunde", name = "getVersion")
    public JAXBElement<GetVersion> createGetVersion(GetVersion value) {
        return new JAXBElement<GetVersion>(_GetVersion_QNAME, GetVersion.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreatePrivatkunde }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:shop:soap:kunde", name = "createPrivatkunde")
    public JAXBElement<CreatePrivatkunde> createCreatePrivatkunde(CreatePrivatkunde value) {
        return new JAXBElement<CreatePrivatkunde>(_CreatePrivatkunde_QNAME, CreatePrivatkunde.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindPrivatkundeById }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:shop:soap:kunde", name = "findPrivatkundeById")
    public JAXBElement<FindPrivatkundeById> createFindPrivatkundeById(FindPrivatkundeById value) {
        return new JAXBElement<FindPrivatkundeById>(_FindPrivatkundeById_QNAME, FindPrivatkundeById.class, null, value);
    }

}
