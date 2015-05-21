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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BAPIINCOMP_fa2309 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BAPIINCOMP_fa2309">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DOC_NUMBER" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="10"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="ITM_NUMBER" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *               &lt;totalDigits value="6"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="SCHED_LINE" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *               &lt;totalDigits value="4"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="PARTN_ROLE" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="TABLE_NAME" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="30"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="FIELD_NAME" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="30"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="FIELD_TEXT" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="40"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BAPIINCOMP_fa2309", propOrder = {
    "docNUMBER",
    "itmNUMBER",
    "schedLINE",
    "partnROLE",
    "tableNAME",
    "fieldNAME",
    "fieldTEXT"
})
public class BAPIINCOMP_Fa2309 {

    @XmlElementRef(name = "DOC_NUMBER", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> docNUMBER;
    @XmlElementRef(name = "ITM_NUMBER", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> itmNUMBER;
    @XmlElementRef(name = "SCHED_LINE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> schedLINE;
    @XmlElementRef(name = "PARTN_ROLE", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> partnROLE;
    @XmlElementRef(name = "TABLE_NAME", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> tableNAME;
    @XmlElementRef(name = "FIELD_NAME", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> fieldNAME;
    @XmlElementRef(name = "FIELD_TEXT", namespace = "http://Microsoft.LobServices.Sap/2007/03/Types/Rfc/", type = JAXBElement.class, required = false)
    protected JAXBElement<String> fieldTEXT;

    /**
     * Gets the value of the doc_NUMBER property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDOC_NUMBER() {
        return docNUMBER;
    }

    /**
     * Sets the value of the doc_NUMBER property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDOC_NUMBER(JAXBElement<String> value) {
        this.docNUMBER = value;
    }

    /**
     * Gets the value of the itm_NUMBER property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getITM_NUMBER() {
        return itmNUMBER;
    }

    /**
     * Sets the value of the itm_NUMBER property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setITM_NUMBER(JAXBElement<Integer> value) {
        this.itmNUMBER = value;
    }

    /**
     * Gets the value of the sched_LINE property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getSCHED_LINE() {
        return schedLINE;
    }

    /**
     * Sets the value of the sched_LINE property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setSCHED_LINE(JAXBElement<Integer> value) {
        this.schedLINE = value;
    }

    /**
     * Gets the value of the partn_ROLE property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPARTN_ROLE() {
        return partnROLE;
    }

    /**
     * Sets the value of the partn_ROLE property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPARTN_ROLE(JAXBElement<String> value) {
        this.partnROLE = value;
    }

    /**
     * Gets the value of the table_NAME property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTABLE_NAME() {
        return tableNAME;
    }

    /**
     * Sets the value of the table_NAME property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTABLE_NAME(JAXBElement<String> value) {
        this.tableNAME = value;
    }

    /**
     * Gets the value of the field_NAME property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFIELD_NAME() {
        return fieldNAME;
    }

    /**
     * Sets the value of the field_NAME property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFIELD_NAME(JAXBElement<String> value) {
        this.fieldNAME = value;
    }

    /**
     * Gets the value of the field_TEXT property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFIELD_TEXT() {
        return fieldTEXT;
    }

    /**
     * Sets the value of the field_TEXT property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFIELD_TEXT(JAXBElement<String> value) {
        this.fieldTEXT = value;
    }

}
