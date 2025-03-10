/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.repositoryservices.rest.properties;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

/**
 * OMRSRESTAPIResponse provides a common header for OMRS managed rest to the OMRS REST API.   It manages
 * information about OMRS exceptions.  If no exception has been raised exceptionClassName is null.
 */
@JsonAutoDetect(getterVisibility=PUBLIC_ONLY, setterVisibility=PUBLIC_ONLY, fieldVisibility=NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "class")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = OMRSAPIPagedResponse.class, name = "OMRSRESTAPIPagedResponse"),
                @JsonSubTypes.Type(value = AttributeTypeDefListResponse.class, name = "AttributeTypeDefListResponse"),
                @JsonSubTypes.Type(value = AttributeTypeDefResponse.class, name = "AttributeTypeDefResponse"),
                @JsonSubTypes.Type(value = BooleanResponse.class, name = "BooleanResponse"),
                @JsonSubTypes.Type(value = EntityDetailResponse.class, name = "EntityDetailResponse"),
                @JsonSubTypes.Type(value = EntitySummaryResponse.class, name = "EntitySummaryResponse"),
                @JsonSubTypes.Type(value = InstanceGraphResponse.class, name = "InstanceGraphResponse"),
                @JsonSubTypes.Type(value = RelationshipListResponse.class, name = "RelationshipListResponse"),
                @JsonSubTypes.Type(value = RelationshipResponse.class, name = "RelationshipResponse"),
                @JsonSubTypes.Type(value = TypeDefGalleryResponse.class, name = "TypeDefGalleryResponse"),
                @JsonSubTypes.Type(value = TypeDefListResponse.class, name = "TypeDefListResponse"),
                @JsonSubTypes.Type(value = TypeDefResponse.class, name = "TypeDefResponse"),
                @JsonSubTypes.Type(value = VoidResponse.class, name = "VoidResponse")
        })
public abstract class OMRSAPIResponse implements Serializable
{
    protected int                   relatedHTTPCode = 200;
    protected String                exceptionClassName = null;
    protected String                exceptionErrorMessage = null;
    protected String                exceptionSystemAction = null;
    protected String                exceptionUserAction = null;
    protected Map<String, Object>   exceptionProperties = null;

    private static final long       serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public OMRSAPIResponse()
    {
    }


    /**
     * Copy/clone constructor
     *
     * @param template object to copy
     */
    public OMRSAPIResponse(OMRSAPIResponse template)
    {
        if (template != null)
        {
            relatedHTTPCode = template.getRelatedHTTPCode();
            exceptionClassName = template.getExceptionClassName();
            exceptionErrorMessage = template.getExceptionErrorMessage();
            exceptionSystemAction = template.getExceptionSystemAction();
            exceptionUserAction = template.getExceptionUserAction();
            exceptionProperties = template.getExceptionProperties();
        }
    }


    /**
     * Return the HTTP Code to use if forwarding response to HTTP client.
     *
     * @return integer HTTP status code
     */
    public int getRelatedHTTPCode()
    {
        return relatedHTTPCode;
    }


    /**
     * Set up the HTTP Code to use if forwarding response to HTTP client.
     *
     * @param relatedHTTPCode integer HTTP status code
     */
    public void setRelatedHTTPCode(int relatedHTTPCode)
    {
        this.relatedHTTPCode = relatedHTTPCode;
    }


    /**
     * Return the name of the Java class name to use to recreate the exception.
     *
     * @return String name of the fully-qualified java class name
     */
    public String getExceptionClassName()
    {
        return exceptionClassName;
    }


    /**
     * Set up the name of the Java class name to use to recreate the exception.
     *
     * @param exceptionClassName String name of the fully-qualified java class name
     */
    public void setExceptionClassName(String exceptionClassName)
    {
        this.exceptionClassName = exceptionClassName;
    }


    /**
     * Return the error message associated with the exception.
     *
     * @return string error message
     */
    public String getExceptionErrorMessage()
    {
        return exceptionErrorMessage;
    }


    /**
     * Set up the error message associated with the exception.
     *
     * @param exceptionErrorMessage string error message
     */
    public void setExceptionErrorMessage(String exceptionErrorMessage)
    {
        this.exceptionErrorMessage = exceptionErrorMessage;
    }


    /**
     * Return the description of the action taken by the system as a result of the exception.
     *
     * @return string description of the action taken
     */
    public String getExceptionSystemAction()
    {
        return exceptionSystemAction;
    }


    /**
     * Set up the description of the action taken by the system as a result of the exception.
     *
     * @param exceptionSystemAction string description of the action taken
     */
    public void setExceptionSystemAction(String exceptionSystemAction)
    {
        this.exceptionSystemAction = exceptionSystemAction;
    }


    /**
     * Return the action that a user should take to resolve the problem.
     *
     * @return string instructions
     */
    public String getExceptionUserAction()
    {
        return exceptionUserAction;
    }


    /**
     * Set up the action that a user should take to resolve the problem.
     *
     * @param exceptionUserAction string instructions
     */
    public void setExceptionUserAction(String exceptionUserAction)
    {
        this.exceptionUserAction = exceptionUserAction;
    }


    /**
     * Return any additional properties associated with the exception.
     *
     * @return map of properties
     */
    public Map<String, Object> getExceptionProperties()
    {
        if (exceptionProperties == null)
        {
            return null;
        }
        else if (exceptionProperties.isEmpty())
        {
            return null;
        }
        else
        {
            return new HashMap<>(exceptionProperties);
        }
    }


    /**
     * Set up any additional properties associated with the exception.
     *
     * @param exceptionProperties map of name value pairs
     */
    public void setExceptionProperties(Map<String, Object> exceptionProperties)
    {
        this.exceptionProperties = exceptionProperties;
    }


    /**
     * Standard toString method.
     *
     * @return print out of variables in a JSON-style
     */
    @Override
    public String toString()
    {
        return "OMRSRESTAPIResponse{" +
                "relatedHTTPCode=" + relatedHTTPCode +
                ", exceptionClassName='" + exceptionClassName + '\'' +
                ", exceptionErrorMessage='" + exceptionErrorMessage + '\'' +
                ", exceptionSystemAction='" + exceptionSystemAction + '\'' +
                ", exceptionUserAction='" + exceptionUserAction + '\'' +
                ", exceptionProperties=" + exceptionProperties +
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
        if (!(objectToCompare instanceof OMRSAPIResponse))
        {
            return false;
        }
        OMRSAPIResponse
                that = (OMRSAPIResponse) objectToCompare;
        return getRelatedHTTPCode() == that.getRelatedHTTPCode() &&
                Objects.equals(getExceptionClassName(), that.getExceptionClassName()) &&
                Objects.equals(getExceptionErrorMessage(), that.getExceptionErrorMessage()) &&
                Objects.equals(getExceptionSystemAction(), that.getExceptionSystemAction()) &&
                Objects.equals(getExceptionUserAction(), that.getExceptionUserAction()) &&
                Objects.equals(getExceptionProperties(), that.getExceptionProperties());
    }


    /**
     * Create a hash code for this element type.
     *
     * @return int hash code
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(getRelatedHTTPCode(),
                            getExceptionClassName(),
                            getExceptionErrorMessage(),
                            getExceptionSystemAction(),
                            getExceptionUserAction(),
                            getExceptionProperties());
    }
}
