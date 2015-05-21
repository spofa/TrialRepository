//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.20 at 04:53:49 PM IST 
//


package com.energizer.core.jaxb.xsd.objects;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="I_SOHEAD" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ZSD_ISOHEAD_fa2309" minOccurs="0"/>
 *         &lt;element name="MESSAGETABLE" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ArrayOfBAPIRET2_fa2309" minOccurs="0"/>
 *         &lt;element name="ORDER_INCOMPLETE" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ArrayOfBAPIINCOMP_fa2309" minOccurs="0"/>
 *         &lt;element name="T_SOITEM" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ArrayOfZSD_TSOITEM_fa2309" minOccurs="0"/>
 *         &lt;element name="T_SOPARTNER" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ArrayOfZSD_TSOPART_fa2309" minOccurs="0"/>
 *         &lt;element name="T_TSOCONDITIONS" type="{http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/}ArrayOfZSD_TSOCONDITIONS_fa2309" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "iSOHEAD",
    "messagetable",
    "orderINCOMPLETE",
    "tSOITEM",
    "tSOPARTNER",
    "tTSOCONDITIONS"
})
@XmlRootElement(name = "ZSD_BAPI_SALESORDER_SIMULATE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/")
public class ZSD_BAPI_SALESORDER_SIMULATE {

    @XmlElementRef(name = "I_SOHEAD", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<ZSD_ISOHEAD_Fa2309> iSOHEAD;
    @XmlElementRef(name = "MESSAGETABLE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfBAPIRET2_Fa2309> messagetable;
    @XmlElementRef(name = "ORDER_INCOMPLETE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfBAPIINCOMP_Fa2309> orderINCOMPLETE;
    @XmlElementRef(name = "T_SOITEM", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfZSD_TSOITEM_Fa2309> tSOITEM;
    @XmlElementRef(name = "T_SOPARTNER", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfZSD_TSOPART_Fa2309> tSOPARTNER;
    @XmlElementRef(name = "T_TSOCONDITIONS", namespace = "http://Microsoft.LobServices.Sap/2007/03/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfZSD_TSOCONDITIONS_Fa2309> tTSOCONDITIONS;

    /**
     * Gets the value of the i_SOHEAD property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ZSD_ISOHEAD_Fa2309 }{@code >}
     *     
     */
    public JAXBElement<ZSD_ISOHEAD_Fa2309> getI_SOHEAD() {
        return iSOHEAD;
    }

    /**
     * Sets the value of the i_SOHEAD property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ZSD_ISOHEAD_Fa2309 }{@code >}
     *     
     */
    public void setI_SOHEAD(JAXBElement<ZSD_ISOHEAD_Fa2309> value) {
        this.iSOHEAD = value;
    }

    /**
     * Gets the value of the messagetable property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfBAPIRET2_Fa2309 }{@code >}
     *     
     */
    public JAXBElement<ArrayOfBAPIRET2_Fa2309> getMESSAGETABLE() {
        return messagetable;
    }

    /**
     * Sets the value of the messagetable property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfBAPIRET2_Fa2309 }{@code >}
     *     
     */
    public void setMESSAGETABLE(JAXBElement<ArrayOfBAPIRET2_Fa2309> value) {
        this.messagetable = value;
    }

    /**
     * Gets the value of the order_INCOMPLETE property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfBAPIINCOMP_Fa2309 }{@code >}
     *     
     */
    public JAXBElement<ArrayOfBAPIINCOMP_Fa2309> getORDER_INCOMPLETE() {
        return orderINCOMPLETE;
    }

    /**
     * Sets the value of the order_INCOMPLETE property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfBAPIINCOMP_Fa2309 }{@code >}
     *     
     */
    public void setORDER_INCOMPLETE(JAXBElement<ArrayOfBAPIINCOMP_Fa2309> value) {
        this.orderINCOMPLETE = value;
    }

    /**
     * Gets the value of the t_SOITEM property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfZSD_TSOITEM_Fa2309 }{@code >}
     *     
     */
    public JAXBElement<ArrayOfZSD_TSOITEM_Fa2309> getT_SOITEM() {
        return tSOITEM;
    }

    /**
     * Sets the value of the t_SOITEM property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfZSD_TSOITEM_Fa2309 }{@code >}
     *     
     */
    public void setT_SOITEM(JAXBElement<ArrayOfZSD_TSOITEM_Fa2309> value) {
        this.tSOITEM = value;
    }

    /**
     * Gets the value of the t_SOPARTNER property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfZSD_TSOPART_Fa2309 }{@code >}
     *     
     */
    public JAXBElement<ArrayOfZSD_TSOPART_Fa2309> getT_SOPARTNER() {
        return tSOPARTNER;
    }

    /**
     * Sets the value of the t_SOPARTNER property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfZSD_TSOPART_Fa2309 }{@code >}
     *     
     */
    public void setT_SOPARTNER(JAXBElement<ArrayOfZSD_TSOPART_Fa2309> value) {
        this.tSOPARTNER = value;
    }

    /**
     * Gets the value of the t_TSOCONDITIONS property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfZSD_TSOCONDITIONS_Fa2309 }{@code >}
     *     
     */
    public JAXBElement<ArrayOfZSD_TSOCONDITIONS_Fa2309> getT_TSOCONDITIONS() {
        return tTSOCONDITIONS;
    }

    /**
     * Sets the value of the t_TSOCONDITIONS property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfZSD_TSOCONDITIONS_Fa2309 }{@code >}
     *     
     */
    public void setT_TSOCONDITIONS(JAXBElement<ArrayOfZSD_TSOCONDITIONS_Fa2309> value) {
        this.tTSOCONDITIONS = value;
    }

}
