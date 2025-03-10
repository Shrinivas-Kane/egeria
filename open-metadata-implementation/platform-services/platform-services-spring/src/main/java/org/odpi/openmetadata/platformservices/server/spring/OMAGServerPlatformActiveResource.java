/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.platformservices.server.spring;

import org.odpi.openmetadata.commonservices.ffdc.rest.BooleanResponse;
import org.odpi.openmetadata.platformservices.rest.ServerListResponse;
import org.odpi.openmetadata.platformservices.rest.ServerServicesListResponse;
import org.odpi.openmetadata.platformservices.rest.ServerStatusResponse;
import org.odpi.openmetadata.platformservices.server.OMAGServerPlatformActiveServices;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * OMAGServerPlatformActiveServices allow an external caller to determine which servers are active on the
 * platform and the services that are active within them.
 */
@RestController
@RequestMapping("/open-metadata/platform-services/users/{userId}")
public class OMAGServerPlatformActiveResource
{
    OMAGServerPlatformActiveServices  platformAPI = new OMAGServerPlatformActiveServices();

    /**
     * Return a flag to indicate if this server has ever run on this OMAG Server Platform.
     *
     * @param userId calling user
     * @param serverName server of interest
     * @return flag
     */
    @RequestMapping(method = RequestMethod.GET, path = "/server-platform/servers/{serverName}/is-known")

    public BooleanResponse isServerKnown(@PathVariable String    userId,
                                         @PathVariable String    serverName)
    {
        return platformAPI.isServerKnown(userId, serverName);
    }


    /**
     * Return the list of OMAG Servers that have run or are running in this OMAG Server Platform.
     *
     * @param userId calling user
     * @return list of OMAG server names
     */
    @RequestMapping(method = RequestMethod.GET, path = "/server-platform/servers")

    public ServerListResponse getKnownServerList(@PathVariable String userId)
    {
        return platformAPI.getKnownServerList(userId);
    }


    /**
     * Return the list of OMAG Servers that are active on this OMAG Server Platform.
     *
     * @param userId name of the user making the request
     * @return list of server names
     */
    @RequestMapping(method = RequestMethod.GET, path = "/server-platform/servers/active")

    public ServerListResponse getActiveServerList(@PathVariable String    userId)
    {
        return platformAPI.getActiveServerList(userId);
    }


    /**
     * Return information about when the server has been active.
     *
     * @param userId name of the user making the request
     * @param serverName name of the server of interest
     * @return details of the server status
     */
    @RequestMapping(method = RequestMethod.GET, path = "/server-platform/servers/{serverName}/status")

    public ServerStatusResponse getServerStatus(@PathVariable String    userId,
                                                @PathVariable String    serverName)
    {
        return platformAPI.getServerStatus(userId, serverName);
    }


    /**
     * Return the list of services that are active on a specific OMAG Server that is active on this OMAG Server Platform.
     *
     * @param userId name of the user making the request
     * @param serverName name of the server of interest
     * @return server name and list od services running within
     */
    @RequestMapping(method = RequestMethod.GET, path = "/server-platform/servers/{serverName}/services")

    public ServerServicesListResponse getActiveServiceListForServer(@PathVariable String    userId,
                                                                    @PathVariable String    serverName)
    {
        return platformAPI.getActiveServiceListForServer(userId, serverName);
    }
}
