/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.discoveryserver.server;

import org.odpi.openmetadata.commonservices.multitenant.GovernanceServerServiceInstance;
import org.odpi.openmetadata.discoveryserver.ffdc.DiscoveryServerErrorCode;
import org.odpi.openmetadata.discoveryserver.handlers.DiscoveryEngineHandler;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditLog;

import java.util.Map;

/**
 * DiscoveryServerInstance maintains the instance information needed to execute requests on behalf of
 * a discovery server.
 */
public class DiscoveryServerInstance extends GovernanceServerServiceInstance
{
    private Map<String, DiscoveryEngineHandler> discoveryEngineInstances;


    /**
     * Constructor where REST Services used.
     *
     * @param serverName name of this server
     * @param serviceName name of this service
     * @param auditLog link to the repository responsible for servicing the REST calls.
     * @param accessServiceRootURL URL root for server platform where the access service is running.
     * @param accessServiceServerName name of the server where the access service is running.
     * @param discoveryEngineInstances active discovery engines in this server.
     */
    public DiscoveryServerInstance(String                              serverName,
                                   String                              serviceName,
                                   OMRSAuditLog                        auditLog,
                                   String                              accessServiceRootURL,
                                   String                              accessServiceServerName,
                                   Map<String, DiscoveryEngineHandler> discoveryEngineInstances)
    {
        super(serverName, serviceName, auditLog, accessServiceRootURL, accessServiceServerName);

        this.discoveryEngineInstances = discoveryEngineInstances;
    }



    /**
     * Return the discovery engine instance requested on an discovery engine services request.
     *
     * @param discoveryEngineGUID unique identifier of the discovery engine
     * @return discovery engine instance.
     * @throws InvalidParameterException the discovery engine guid is not recognized
     */
    synchronized DiscoveryEngineHandler getDiscoveryEngine(String   discoveryEngineGUID) throws InvalidParameterException
    {
        final String  methodName        = "getDiscoveryEngine";
        final String  guidParameterName = "discoveryEngineGUID";

        DiscoveryEngineHandler instance = discoveryEngineInstances.get(discoveryEngineGUID);

        if (instance == null)
        {
            DiscoveryServerErrorCode errorCode    = DiscoveryServerErrorCode.UNKNOWN_DISCOVERY_ENGINE;
            String                   errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(serverName,
                                                                                                                       discoveryEngineGUID);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                methodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction(),
                                                guidParameterName);
        }

        return instance;
    }


    /**
     * Shutdown the engines
     */
    @Override
    public void shutdown()
    {
        if (discoveryEngineInstances != null)
        {
            for (DiscoveryEngineHandler  handler : discoveryEngineInstances.values())
            {
                if (handler != null)
                {
                    handler.terminate();
                }
            }
        }

        super.shutdown();
    }
}
