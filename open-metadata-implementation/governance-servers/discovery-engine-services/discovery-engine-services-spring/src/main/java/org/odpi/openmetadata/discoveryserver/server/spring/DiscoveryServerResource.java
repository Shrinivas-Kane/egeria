/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.discoveryserver.server.spring;

import org.odpi.openmetadata.commonservices.ffdc.rest.GUIDResponse;
import org.odpi.openmetadata.commonservices.odf.metadatamanagement.rest.*;
import org.odpi.openmetadata.discoveryserver.server.DiscoveryServerRESTServices;
import org.springframework.web.bind.annotation.*;


/**
 * DiscoveryServerResource provides the server-side catcher for REST calls using Spring
 */
@RestController
@RequestMapping("/servers/{serverName}/open-metadata/discovery-server/users/{userId}/discovery-engine/{discoveryEngineGUID}")
public class DiscoveryServerResource
{
    private DiscoveryServerRESTServices restAPI = new DiscoveryServerRESTServices();


    /**
     * Request the execution of a discovery service to explore a specific asset.
     *
     * @param serverName name of the discovery server.
     * @param discoveryEngineGUID unique identifier of the discovery engine.
     * @param userId identifier of calling user
     * @param assetGUID identifier of the asset to analyze.
     * @param assetType identifier of the type of asset to analyze - this determines which discovery service to run.
     * @param requestBody containing analysisParameters and annotationTypes
     *
     * @return unique id for the discovery request or
     *
     *  InvalidParameterException one of the parameters is null or invalid or
     *  UserNotAuthorizedException user not authorized to issue this request or
     *  DiscoveryEngineException there was a problem detected by the discovery engine.
     */
    @RequestMapping(method = RequestMethod.POST, path = "/asset-types/{assetType}/assets/{assetGUID}")

    public  GUIDResponse discoverAsset(@PathVariable String                       serverName,
                                       @PathVariable String                       discoveryEngineGUID,
                                       @PathVariable String                       userId,
                                       @PathVariable String                       assetGUID,
                                       @PathVariable String                       assetType,
                                       @RequestBody  DiscoveryRequestRequestBody  requestBody)
    {
        return restAPI.discoverAsset(serverName,
                                     discoveryEngineGUID,
                                     userId,
                                     assetGUID,
                                     assetType,
                                     requestBody);
    }


    /**
     * Request the discovery report for a discovery request that has completed.
     *
     * @param serverName name of the discovery server.
     * @param discoveryEngineGUID unique identifier of the discovery engine.
     * @param userId calling user
     * @param discoveryRequestGUID identifier of the discovery request.
     *
     * @return discovery report or
     *
     *  DiscoveryEngineException there was a problem detected by the discovery engine.
     */
    @RequestMapping(method = RequestMethod.GET, path = "/discovery-analysis-reports/{discoveryRequestGUID}")

    public DiscoveryAnalysisReportResponse getDiscoveryReport(@PathVariable String   serverName,
                                                              @PathVariable String   discoveryEngineGUID,
                                                              @PathVariable String   userId,
                                                              @PathVariable String   discoveryRequestGUID)
    {
        return restAPI.getDiscoveryReport(serverName, discoveryEngineGUID, userId, discoveryRequestGUID);
    }


    /**
     * Return the annotations linked direction to the report.
     *
     * @param serverName name of the discovery server.
     * @param discoveryEngineGUID unique identifier of the discovery engine.
     * @param userId calling user
     * @param discoveryRequestGUID identifier of the discovery request.
     * @param startingFrom initial position in the stored list.
     * @param maximumResults maximum number of definitions to return on this call.
     *
     * @return list of annotations or
     *
     *  DiscoveryEngineException there was a problem detected by the discovery engine.
     */
    @RequestMapping(method = RequestMethod.GET, path = "/discovery-analysis-reports/{discoveryRequestGUID}/annotations")

    public AnnotationListResponse getDiscoveryReportAnnotations(@PathVariable String   serverName,
                                                                @PathVariable String   discoveryEngineGUID,
                                                                @PathVariable String   userId,
                                                                @PathVariable String   discoveryRequestGUID,
                                                                @RequestParam int      startingFrom,
                                                                @RequestParam int      maximumResults)
    {
        return restAPI.getDiscoveryReportAnnotations(serverName,
                                                     discoveryEngineGUID,
                                                     userId,
                                                     discoveryRequestGUID,
                                                     startingFrom,
                                                     maximumResults);
    }


    /**
     * Return any annotations attached to this annotation.
     *
     * @param serverName name of the discovery server.
     * @param discoveryEngineGUID unique identifier of the discovery engine.
     * @param userId calling user
     * @param annotationGUID anchor annotation
     * @param startingFrom starting position in the list
     * @param maximumResults maximum number of annotations that can be returned.
     *
     * @return list of Annotation objects or
     *
     *  DiscoveryEngineException there was a problem detected by the discovery engine.
     */
    @RequestMapping(method = RequestMethod.GET, path = "/annotations/{annotationGUID}/extended-annotations")

    public AnnotationListResponse getExtendedAnnotations(@PathVariable String   serverName,
                                                         @PathVariable String   discoveryEngineGUID,
                                                         @PathVariable String   userId,
                                                         @PathVariable String   annotationGUID,
                                                         @RequestParam int      startingFrom,
                                                         @RequestParam int      maximumResults)
    {
        return restAPI.getExtendedAnnotations(serverName,
                                              discoveryEngineGUID,
                                              userId,
                                              annotationGUID,
                                              startingFrom,
                                              maximumResults);
    }


    /**
     * Retrieve a single annotation by unique identifier.  This call is typically used to retrieve the latest values
     * for an annotation.
     *
     * @param serverName name of the discovery server.
     * @param discoveryEngineGUID unique identifier of the discovery engine.
     * @param userId calling user
     * @param annotationGUID unique identifier of the annotation
     *
     * @return Annotation object or
     *
     *  DiscoveryEngineException there was a problem detected by the discovery engine.
     */
    @RequestMapping(method = RequestMethod.GET, path = "/annotations/{annotationGUID}")

    public AnnotationResponse getAnnotation(@PathVariable String   serverName,
                                            @PathVariable String   discoveryEngineGUID,
                                            @PathVariable String   userId,
                                            @PathVariable String   annotationGUID)
    {
        return restAPI.getAnnotation(serverName, discoveryEngineGUID, userId, annotationGUID);
    }
}
