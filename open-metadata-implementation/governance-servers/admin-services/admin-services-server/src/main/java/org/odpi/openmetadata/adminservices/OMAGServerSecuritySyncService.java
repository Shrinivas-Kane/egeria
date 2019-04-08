/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adminservices;

import org.odpi.openmetadata.adapters.repositoryservices.ConnectorConfigurationFactory;
import org.odpi.openmetadata.adminservices.configuration.properties.EventBusConfig;
import org.odpi.openmetadata.adminservices.configuration.properties.OMAGServerConfig;
import org.odpi.openmetadata.adminservices.configuration.properties.SecuritySyncConfig;
import org.odpi.openmetadata.adminservices.ffdc.exception.OMAGInvalidParameterException;
import org.odpi.openmetadata.adminservices.rest.VoidResponse;

import java.util.*;

public class OMAGServerSecuritySyncService {

    private OMAGServerAdminStoreServices configStore = new OMAGServerAdminStoreServices();
    private OMAGServerErrorHandler errorHandler = new OMAGServerErrorHandler();

    private static final String defaultOutTopicName = "OutTopic";
    private static final String defaultInTopicName = "open-metadata.access-services.GovernanceEngine.outTopic";

    private static final String outputTopic = "open-metadata.security-sync.";
    private static final String defaultOutTopic = ".outTopic";

    public VoidResponse setSecuritySyncConfig(String userId, String serverName, SecuritySyncConfig securitySyncConfig) {
        String methodName = "setSecuritySyncConfig";
        VoidResponse response = new VoidResponse();

        try {
            OMAGServerConfig serverConfig = configStore.getServerConfig(serverName, methodName);

            List<String> configAuditLog = serverConfig.getAuditTrail();

            if (configAuditLog == null) {
                configAuditLog = new ArrayList<>();
            }

            if (securitySyncConfig == null) {
                configAuditLog.add(new Date().toString() + " " + userId + " removed configuration for security sync services.");
            } else {
                configAuditLog.add(new Date().toString() + " " + userId + " updated configuration for security sync services.");
            }

            serverConfig.setAuditTrail(configAuditLog);
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
                                eventBusConfig.getAdditionalProperties()));
            }

            if(securitySyncConfig != null && securitySyncConfig.getSecurityServerType() != null) {
                securitySyncConfig.setSecuritySyncOutTopic(
                        connectorConfigurationFactory.getDefaultEventBusConnection(defaultOutTopicName,
                                eventBusConfig.getConnectorProvider(),
                                eventBusConfig.getTopicURLRoot() + ".server." + serverName,
                                getOutputTopicName(securitySyncConfig.getSecuritySyncOutTopicName()),
                                serverConfig.getLocalServerId(),
                                eventBusConfig.getAdditionalProperties()));
            }

            if(securitySyncConfig.getSecurityServerURL() != null && securitySyncConfig.getSecurityServerAuthorization() != null){
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
        } catch (OMAGInvalidParameterException e) {
            errorHandler.captureInvalidParameterException(response, e);
        }
        return response;
    }

    private String getOutputTopicName(String securityServerType) {
        return outputTopic + securityServerType + defaultOutTopic;
    }

    public VoidResponse enableSecuritySyncService(String userId, String serverName) {

        final String methodName = "enableSecuritySyncService";
        VoidResponse response = new VoidResponse();

        try {
            OMAGServerConfig serverConfig = configStore.getServerConfig(serverName, methodName);
            SecuritySyncConfig securitySyncConfig = serverConfig.getSecuritySyncConfig();
            this.setSecuritySyncConfig(userId, serverName, securitySyncConfig);
        } catch (OMAGInvalidParameterException e) {
            errorHandler.captureInvalidParameterException(response, e);
        }
        return response;
    }

}
