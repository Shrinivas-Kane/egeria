/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adminservices;

import org.odpi.openmetadata.adapters.repositoryservices.ConnectorConfigurationFactory;
import org.odpi.openmetadata.adminservices.configuration.properties.EventBusConfig;
import org.odpi.openmetadata.adminservices.configuration.properties.OMAGServerConfig;
import org.odpi.openmetadata.adminservices.configuration.properties.SecuritySyncConfig;
import org.odpi.openmetadata.adminservices.ffdc.exception.OMAGInvalidParameterException;
import org.odpi.openmetadata.commonservices.ffdc.rest.VoidResponse;

import java.util.*;

public class OMAGServerSecuritySyncService
{
    private OMAGServerAdminStoreServices configStore = new OMAGServerAdminStoreServices();
    private OMAGServerErrorHandler       errorHandler = new OMAGServerErrorHandler();
    private OMAGServerExceptionHandler   exceptionHandler = new OMAGServerExceptionHandler();

    private static final String defaultOutTopicName = "OutTopic";
    private static final String defaultInTopicName = "open-metadata.access-services.GovernanceEngine.outTopic";

    private static final String outputTopic = "open-metadata.security-sync.";
    private static final String defaultOutTopic = ".outTopic";

    public VoidResponse setSecuritySyncConfig(String userId, String serverName, SecuritySyncConfig securitySyncConfig)
    {
        String methodName = "setSecuritySyncConfig";
        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            List<String> configAuditTrail = serverConfig.getAuditTrail();

            if (configAuditTrail == null) {
                configAuditTrail = new ArrayList<>();
            }

            if (securitySyncConfig == null) {
                configAuditTrail.add(new Date().toString() + " " + userId + " removed configuration for security sync services.");
            } else {
                configAuditTrail.add(new Date().toString() + " " + userId + " updated configuration for security sync services.");
            }

            serverConfig.setAuditTrail(configAuditTrail);
            ConnectorConfigurationFactory connectorConfigurationFactory = new ConnectorConfigurationFactory();

            EventBusConfig eventBusConfig = serverConfig.getEventBusConfig();
            if(securitySyncConfig != null && securitySyncConfig.getSecuritySyncInTopicName() != null) {
                securitySyncConfig.setSecuritySyncInTopic(
                        connectorConfigurationFactory.getDefaultEventBusConnection(
                                defaultInTopicName,
                                eventBusConfig.getConnectorProvider(),
                                eventBusConfig.getTopicURLRoot() + ".server." + serverName,
                                securitySyncConfig.getSecuritySyncInTopicName(),
                                UUID.randomUUID().toString(),
                                eventBusConfig.getConfigurationProperties()));
            }

            if(securitySyncConfig != null && securitySyncConfig.getSecurityServerType() != null) {
                securitySyncConfig.setSecuritySyncOutTopic(
                        connectorConfigurationFactory.getDefaultEventBusConnection(defaultOutTopicName,
                                eventBusConfig.getConnectorProvider(),
                                eventBusConfig.getTopicURLRoot() + ".server." + serverName,
                                getOutputTopicName(securitySyncConfig.getSecuritySyncOutTopicName()),
                                serverConfig.getLocalServerId(),
                                eventBusConfig.getConfigurationProperties()));
            }

            if(securitySyncConfig != null && securitySyncConfig.getSecurityServerURL() != null && securitySyncConfig.getSecurityServerAuthorization() != null){
                Map<String, Object> additionalProperties = new HashMap<>();
                additionalProperties.put("securityServerAuthorization", securitySyncConfig.getSecurityServerAuthorization());
                additionalProperties.put("tagServiceName", securitySyncConfig.getTagServiceName());

                securitySyncConfig.setSecurityServerConnection(
                        connectorConfigurationFactory.getSecuritySyncServerConnection(serverName,
                                securitySyncConfig.getSecurityServerURL(),
                                additionalProperties));
            }

            serverConfig.setSecuritySyncConfig(securitySyncConfig);

            configStore.saveServerConfig(serverName, methodName, serverConfig);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }
        return response;
    }

    private String getOutputTopicName(String securityServerType)
    {
        return outputTopic + securityServerType + defaultOutTopic;
    }

    public VoidResponse enableSecuritySyncService(String userId, String serverName)
    {

        final String methodName = "enableSecuritySyncService";
        VoidResponse response = new VoidResponse();

        try
        {
            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);
            SecuritySyncConfig securitySyncConfig = serverConfig.getSecuritySyncConfig();
            this.setSecuritySyncConfig(userId, serverName, securitySyncConfig);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }

}
