/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.frameworks.discovery;

import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.frameworks.discovery.properties.Annotation;
import org.odpi.openmetadata.frameworks.discovery.properties.AnnotationStatus;

import java.util.List;

/**
 * The DiscoveryAnnotationStore provides the interface used by a discovery engine to
 * store annotations in the annotation store.  There is one instance of the annotation store
 * for each discovery request.  The userId that made the discovery request is the default user for
 * the annotation store.  This userId may be over-ridden by the discovery engine.
 */
public abstract class DiscoveryAnnotationStore
{
    protected String  userId;
    protected String  assetGUID;
    protected String  discoveryReportGUID;


    /**
     * Constructor sets up the key parameters for accessing the annotations store.
     *
     * @param userId calling user
     * @param assetGUID unique identifier of the asset that the annotations should be attached to
     * @param discoveryReportGUID unique identifier of the discovery request that is used to identifier the
     *                            discovery report.
     */
    public DiscoveryAnnotationStore(String userId, String assetGUID, String discoveryReportGUID)
    {
        this.userId = userId;
        this.assetGUID = assetGUID;
        this.discoveryReportGUID = discoveryReportGUID;
    }


    /**
     * Return the list of Annotations created for the asset by previous runs of the discovery service.
     *
     * @param startingFrom starting position in the list.
     * @param maximumResults maximum number of elements that can be returned
     * @return list of annotation (or null if none are registered)
     * @throws InvalidParameterException one of the parameters is invalid
     * @throws UserNotAuthorizedException the user id not authorized to issue this request
     * @throws PropertyServerException there was a problem retrieving annotations from the annotation store.
     */
    public abstract List<Annotation>  getPreviousAnnotationsForAsset(int       startingFrom,
                                                                     int       maximumResults) throws InvalidParameterException,
                                                                                                      UserNotAuthorizedException,
                                                                                                      PropertyServerException;


    /**
     * Return the list of annotations from previous runs of the discovery service that are set to a specific status.
     * If status is null then annotations that have been reviewed, approved and/or actioned are returned from
     * discovery reports that are not waiting or in progress.
     *
     * @param status status value to use on the query
     * @param startingFrom starting position in the list.
     * @param maximumResults maximum number of elements that can be returned
     * @return list of annotation (or null if none are registered)
     * @throws InvalidParameterException one of the parameters is invalid
     * @throws UserNotAuthorizedException the user id not authorized to issue this request
     * @throws PropertyServerException there was a problem retrieving annotations from the annotation store.
     */
    public abstract List<Annotation>  getPreviousAnnotationsForAsset(AnnotationStatus status,
                                                                     int              startingFrom,
                                                                     int              maximumResults) throws InvalidParameterException,
                                                                                                             UserNotAuthorizedException,
                                                                                                             PropertyServerException;


    /**
     * Return the current list of annotations for this discovery run.
     *
     * @param startingFrom starting position in the list.
     * @param maximumResults maximum number of elements that can be returned
     * @return list of annotation (or null if none are registered)
     * @throws InvalidParameterException one of the parameters is invalid
     * @throws UserNotAuthorizedException the user id not authorized to issue this request
     * @throws PropertyServerException there was a problem retrieving annotations from the annotation store.
     */
    public abstract List<Annotation>  getNewAnnotationsForAsset(int       startingFrom,
                                                                int       maximumResults) throws InvalidParameterException,
                                                                                                 UserNotAuthorizedException,
                                                                                                 PropertyServerException;


    /**
     * Return any annotations attached to this annotation.
     *
     * @param userId identifier of calling user
     * @param annotationGUID anchor annotation
     * @param startingFrom starting position in the list
     * @param maximumResults maximum number of annotations that can be returned.
     *
     * @return list of Annotation objects
     *
     * @throws InvalidParameterException one of the parameters is null or invalid.
     * @throws UserNotAuthorizedException user not authorized to issue this request.
     * @throws PropertyServerException there was a problem that occurred within the property server.
     */
    public abstract List<Annotation>  getExtendedAnnotations(String   userId,
                                                             String   annotationGUID,
                                                             int      startingFrom,
                                                             int      maximumResults) throws InvalidParameterException,
                                                                                             UserNotAuthorizedException,
                                                                                             PropertyServerException;


    /**
     * Return a specific annotation stored in the annotation store (previous or new).
     *
     * @param annotationGUID unique identifier of the annotation
     * @return annotation object
     * @throws InvalidParameterException one of the parameters is invalid
     * @throws UserNotAuthorizedException the user id not authorized to issue this request
     * @throws PropertyServerException there was a problem retrieving the annotation from the annotation store.
     */
    public abstract Annotation  getAnnotation(String    annotationGUID) throws InvalidParameterException,
                                                                               UserNotAuthorizedException,
                                                                               PropertyServerException;


    /**
     * Add a new annotation to the annotation store as a top level annotation linked directly off of the report.
     *
     * @param annotation annotation object
     * @return unique identifier of new annotation
     * @throws InvalidParameterException the annotation is invalid
     * @throws UserNotAuthorizedException the user id not authorized to issue this request
     * @throws PropertyServerException there was a problem retrieving adding the annotation to the annotation store.
     */
    public abstract String  addAnnotationToDiscoveryReport(Annotation annotation) throws InvalidParameterException,
                                                                                         UserNotAuthorizedException,
                                                                                         PropertyServerException;


    /**
     * Add a new annotation and link it to an existing annotation.
     *
     * @param anchorAnnotationGUID unique identifier of the annotation that this new one os to be attached to
     * @param annotation annotation object
     * @return fully filled out annotation
     * @throws InvalidParameterException one of the parameters is invalid
     * @throws UserNotAuthorizedException the user id not authorized to issue this request
     * @throws PropertyServerException there was a problem saving annotations in the annotation store.
     */
    public abstract Annotation  addAnnotationToAnnotation(String     anchorAnnotationGUID,
                                                          Annotation annotation) throws InvalidParameterException,
                                                                                        UserNotAuthorizedException,
                                                                                        PropertyServerException;


    /**
     * Link an existing annotation to another object.  The anchor object my be a Referenceable.
     *
     * @param anchorGUID unique identifier that the annotation is to be linked to
     * @param annotationGUID unique identifier of the annotation
     * @throws InvalidParameterException one of the parameters is invalid
     * @throws UserNotAuthorizedException the user id not authorized to issue this request
     * @throws PropertyServerException there was a problem updating annotations in the annotation store.
     */
    public abstract void    linkAnnotation(String anchorGUID,
                                           String annotationGUID) throws InvalidParameterException,
                                                                         UserNotAuthorizedException,
                                                                         PropertyServerException;


    /**
     * Remove the relationship between an annotation and another object.
     *
     * @param anchorGUID unique identifier that the annotation is to be unlinked from
     * @param annotationGUID unique identifier of the annotation
     * @throws InvalidParameterException one of the parameters is invalid
     * @throws UserNotAuthorizedException the user id not authorized to issue this request
     * @throws PropertyServerException there was a problem updating annotations in the annotation store.
     */
    public abstract void    unlinkAnnotation(String anchorGUID,
                                             String annotationGUID) throws InvalidParameterException,
                                                                           UserNotAuthorizedException,
                                                                           PropertyServerException;


    /**
     * Replace the current properties of an annotation.
     *
     * @param annotation new properties
     *
     * @return fully filled out annotation
     * @throws InvalidParameterException one of the parameters is invalid
     * @throws UserNotAuthorizedException the user id not authorized to issue this request
     * @throws PropertyServerException there was a problem updating the annotation in the annotation store.
     */
    public abstract Annotation  updateAnnotation(Annotation annotation) throws InvalidParameterException,
                                                                         UserNotAuthorizedException,
                                                                         PropertyServerException;


    /**
     * Remove an annotation from the annotation store.
     *
     * @param annotationGUID unique identifier of the annotation
     * @throws InvalidParameterException one of the parameters is invalid
     * @throws UserNotAuthorizedException the user id not authorized to issue this request
     * @throws PropertyServerException there was a problem deleting the annotation from the annotation store.
     */
    public abstract void  deleteAnnotation(String   annotationGUID) throws InvalidParameterException,
                                                                           UserNotAuthorizedException,
                                                                           PropertyServerException;
}
