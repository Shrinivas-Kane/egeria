/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adminservices.spring;


import org.odpi.openmetadata.adminservices.OMAGServerAdminSecurityServices;
import org.odpi.openmetadata.adminservices.rest.ConnectionResponse;
import org.odpi.openmetadata.adminservices.rest.PlatformSecurityRequestBody;
import org.odpi.openmetadata.commonservices.ffdc.rest.VoidResponse;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.Connection;
import org.springframework.web.bind.annotation.*;

/**
 * ConfigOpenMetadataSecurityResource provides the API to configure the security connector that validates
 * platform requests that do not reference an OMAG server.  These requests are used by the
 * team that run the platform as a service.
 */
@RestController
@RequestMapping("/open-metadata/admin-services/users/{userId}/security")
public class ConfigOpenMetadataSecurityResource
{
    private static OMAGServerAdminSecurityServices adminSecurityAPI = new OMAGServerAdminSecurityServices();


    /**
     * Set up a platform security connector
     *
     * @param userId calling user.
     * @param requestBody requestBody used to create and configure the connector that performs platform security
     * @return void response
     */
    @RequestMapping(method = RequestMethod.POST, path = "/platform/connection")

    public VoidResponse setPlatformSecurityConnection(@PathVariable String                      userId,
                                                      @RequestBody  PlatformSecurityRequestBody requestBody)
    {
        return adminSecurityAPI.setPlatformSecurityConnection(userId, requestBody);
    }


    /**
     * Return the connection object for platform security connector.  Null is returned if no platform security
     * has been set up.
     *
     * @param userId calling user
     * @return connection response
     */
    @RequestMapping(method = RequestMethod.GET, path = "/platform/connection")

    public ConnectionResponse getPlatformSecurityConnection(@PathVariable String       userId)
    {
        return adminSecurityAPI.getPlatformSecurityConnection(userId);
    }


    /**
     * Clear the connection object for platform security.  This means there is no platform security set up.
     *
     * @param userId calling user
     * @return void response
     */
    @RequestMapping(method = RequestMethod.DELETE, path = "/platform/connection")

    public  VoidResponse clearPlatformSecurityConnection(@PathVariable String   userId)
    {
        return adminSecurityAPI.clearPlatformSecurityConnection(userId);
    }


    /**
     * Override the default server security connector.
     *
     * @param userId calling user.
     * @param serverName server to configure
     * @param connection connection used to create and configure the connector.
     * @return void response
     */
    @RequestMapping(method = RequestMethod.POST, path = "/servers/{serverName}/connection")

    public synchronized VoidResponse setServerSecurityConnection(@PathVariable String       userId,
                                                                 @PathVariable String       serverName,
                                                                 @RequestBody  Connection   connection)
    {
          return adminSecurityAPI.setServerSecurityConnection(userId, serverName, connection);
    }


    /**
     * Return the connection object for the server security connector.
     *
     * @param userId calling user
     * @param serverName server to retrieve configuration from
     * @return connection response
     */
    @RequestMapping(method = RequestMethod.GET, path = "/servers/{serverName}/connection")

    public synchronized ConnectionResponse getServerSecurityConnection(@PathVariable  String       userId,
                                                                       @PathVariable  String       serverName)
    {
        return adminSecurityAPI.getServerSecurityConnection(userId, serverName);
    }


    /**
     * Clear the connection object for the server security connector.
     * This sets the server security back to default.
     *
     * @param userId calling user
     * @param serverName server to configure
     * @return connection response
     */
    @RequestMapping(method = RequestMethod.DELETE, path = "/servers/{serverName}/connection")

    public synchronized VoidResponse clearServerSecurityConnection(@PathVariable  String   userId,
                                                                   @PathVariable  String   serverName)
    {
        return adminSecurityAPI.clearServerSecurityConnection(userId, serverName);
    }
}
