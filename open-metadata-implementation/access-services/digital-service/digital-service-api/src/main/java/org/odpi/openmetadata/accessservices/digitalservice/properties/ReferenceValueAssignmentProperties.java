/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.accessservices.digitalservice.properties;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

/**
 * ReferenceValueAssignmentProperties is a java bean used to create a link between a valid value and a referenceable item
 * to enable the valid value to be used as a semi-formal tag/classifier.
 */
@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class ReferenceValueAssignmentProperties implements Serializable
{
    private static final long     serialVersionUID = 1L;

    private int    confidence             = 0;
    private String steward                = null;
    private String notes                  = null;


    /**
     * Default constructor
     */
    public ReferenceValueAssignmentProperties()
    {
    }


    /**
     * Copy/clone constructor.  Note, this is a deep copy
     *
     * @param template object to copy
     */
    public ReferenceValueAssignmentProperties(ReferenceValueAssignmentProperties template)
    {
        if (template != null)
        {
            confidence             = template.getConfidence();
            steward                = template.getSteward();
            notes                  = template.getNotes();
        }
    }


    /**
     * Return the confidence level (0-100) that the mapping is correct.
     *
     * @return int
     */
    public int getConfidence()
    {
        return confidence;
    }


    /**
     * Set up the confidence level (0-100) that the mapping is correct.
     *
     * @param confidence int
     */
    public void setConfidence(int confidence)
    {
        this.confidence = confidence;
    }


    /**
     * Returns the id of the steward responsible for the mapping.
     *
     * @return String id
     */
    public String getSteward()
    {
        return steward;
    }


    /**
     * Set up the id of the steward responsible for the mapping.
     *
     * @param steward String id
     */
    public void setSteward(String steward)
    {
        this.steward = steward;
    }



    /**
     * Return the additional values associated with the symbolic name.
     *
     * @return string text
     */
    public String getNotes()
    {
        return notes;
    }


    /**
     * Set up the additional values associated with the symbolic name.
     *
     * @param notes string text
     */
    public void setNotes(String notes)
    {
        this.notes = notes;
    }


    /**
     * Standard toString method.
     *
     * @return print out of variables in a JSON-style
     */
    @Override
    public String toString()
    {
        return "ReferenceValueAssignmentProperties{" +
                ", confidence=" + confidence +
                ", steward='" + steward + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }


    /**
     * Compare the values of the supplied object with those stored in the current object.
     *
     * @param objectToCompare supplied object
     * @return boolean result of comparison
     */
    @Override
    public boolean equals(Object objectToCompare)
    {
        if (this == objectToCompare)
        {
            return true;
        }
        if (objectToCompare == null || getClass() != objectToCompare.getClass())
        {
            return false;
        }
        ReferenceValueAssignmentProperties that = (ReferenceValueAssignmentProperties) objectToCompare;
        return confidence == that.confidence &&
                Objects.equals(steward, that.steward) &&
                Objects.equals(notes, that.notes);
    }


    /**
     * Return has code based on properties.
     *
     * @return int
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(confidence, steward, notes);
    }
}
