/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.dataplatform.admin;

import org.odpi.openmetadata.accessservices.dataplatform.auditlog.DataPlatformAuditCode;
import org.odpi.openmetadata.accessservices.dataplatform.contentmanager.OMEntityDao;
import org.odpi.openmetadata.accessservices.dataplatform.eventprocessor.EventPublisher;
import org.odpi.openmetadata.accessservices.dataplatform.ffdc.DataPlatformErrorCode;
import org.odpi.openmetadata.accessservices.dataplatform.listeners.DataPlatformInTopicListener;
import org.odpi.openmetadata.accessservices.dataplatform.server.DataPlatformServicesInstance;
import org.odpi.openmetadata.accessservices.dataplatform.listeners.DataPlatformEnterpriseOmrsEventListener;

import org.odpi.openmetadata.adminservices.configuration.properties.AccessServiceConfig;
import org.odpi.openmetadata.adminservices.configuration.registration.AccessServiceAdmin;
import org.odpi.openmetadata.adminservices.ffdc.exception.OMAGConfigurationErrorException;
import org.odpi.openmetadata.frameworks.connectors.ConnectorBroker;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.Connection;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditLog;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditingComponent;
import org.odpi.openmetadata.repositoryservices.connectors.omrstopic.OMRSTopicConnector;
import org.odpi.openmetadata.repositoryservices.connectors.openmetadatatopic.OpenMetadataTopicConnector;
import org.odpi.openmetadata.repositoryservices.connectors.openmetadatatopic.OpenMetadataTopicListener;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryConnector;
import org.odpi.openmetadata.repositoryservices.ffdc.OMRSErrorCode;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.OMRSConfigErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class DataPlatformAdmin extends AccessServiceAdmin
{

    private static final Logger log = LoggerFactory.getLogger(DataPlatformAdmin.class);
    private OpenMetadataTopicConnector dataPlatformInTopicConnector;
    private OpenMetadataTopicConnector dataPlatformOutTopicConnector;
    private OMRSAuditLog auditLog;
    private String serverName = null;
    private DataPlatformServicesInstance instance = null;

    /**
     * Initialize the access service.
     *
     * @param accessServiceConfigurationProperties specific configuration properties for this access service.
     * @param enterpriseOMRSTopicConnector         connector for receiving OMRS Events from the cohorts
     * @param enterpriseConnector                  connector for querying the cohort repositories
     * @param auditLog                             audit log component for logging messages.
     * @param serverUserName                       user id to use on OMRS calls where there is no end user.
     * @throws OMAGConfigurationErrorException invalid parameters in the configuration properties.
     */
    @Override
    public void initialize(AccessServiceConfig accessServiceConfigurationProperties, OMRSTopicConnector enterpriseOMRSTopicConnector, OMRSRepositoryConnector enterpriseConnector, OMRSAuditLog auditLog, String serverUserName) throws OMAGConfigurationErrorException {
        final String actionDescription = "initialize";
        DataPlatformAuditCode auditCode;

        auditCode = DataPlatformAuditCode.SERVICE_INITIALIZING;
        auditLog.logRecord(actionDescription,
                auditCode.getLogMessageId(),
                auditCode.getSeverity(),
                auditCode.getFormattedLogMessage(),
                null,
                auditCode.getSystemAction(),
                auditCode.getUserAction());


        this.auditLog = auditLog;

        if (enterpriseConnector != null) {
            serverName = enterpriseConnector.getServerName();
        }
        String inTopicName = getTopicName(accessServiceConfigurationProperties.getAccessServiceInTopic());
        String outTopicName = getTopicName(accessServiceConfigurationProperties.getAccessServiceOutTopic());
        dataPlatformInTopicConnector = initializeDataPlatformTopicConnector(accessServiceConfigurationProperties.getAccessServiceInTopic());
        dataPlatformOutTopicConnector = initializeDataPlatformTopicConnector(accessServiceConfigurationProperties.getAccessServiceOutTopic());

        List<String> supportedZones = this.extractSupportedZones(accessServiceConfigurationProperties.getAccessServiceOptions());
        OMEntityDao omEntityDao = new OMEntityDao(enterpriseConnector, supportedZones, auditLog);
        EventPublisher eventPublisher = null;

        if (enterpriseOMRSTopicConnector != null) {
            auditCode = DataPlatformAuditCode.SERVICE_REGISTERED_WITH_ENTERPRISE_TOPIC;
            auditLog.logRecord(actionDescription,
                    auditCode.getLogMessageId(),
                    auditCode.getSeverity(),
                    auditCode.getFormattedLogMessage(serverName),
                    null,
                    auditCode.getSystemAction(),
                    auditCode.getUserAction());

            eventPublisher = new EventPublisher(dataPlatformOutTopicConnector, auditLog);
            DataPlatformEnterpriseOmrsEventListener dataPlatformEnterpriseOmrsEventListener = new DataPlatformEnterpriseOmrsEventListener(eventPublisher, auditLog);
            enterpriseOMRSTopicConnector.registerListener(dataPlatformEnterpriseOmrsEventListener);
        }


        if (dataPlatformInTopicConnector != null) {
            OpenMetadataTopicListener dataPlatformInTopicListener = new DataPlatformInTopicListener(omEntityDao, auditLog, eventPublisher, enterpriseConnector.getRepositoryHelper());
            this.dataPlatformInTopicConnector.registerListener(dataPlatformInTopicListener);
            startConnector(DataPlatformAuditCode.SERVICE_REGISTERED_WITH_DP_IN_TOPIC, actionDescription, inTopicName, dataPlatformInTopicConnector);
        }

        if (dataPlatformOutTopicConnector != null) {
            startConnector(DataPlatformAuditCode.SERVICE_REGISTERED_WITH_DP_OUT_TOPIC, actionDescription, outTopicName, dataPlatformOutTopicConnector);
        }

        auditCode = DataPlatformAuditCode.SERVICE_INITIALIZED;
        auditLog.logRecord(actionDescription,
                auditCode.getLogMessageId(),
                auditCode.getSeverity(),
                auditCode.getFormattedLogMessage(serverName),
                null,
                auditCode.getSystemAction(),
                auditCode.getUserAction());
    }

    private void startConnector(DataPlatformAuditCode auditCode, String actionDescription, String topicName, OpenMetadataTopicConnector topicConnector) throws OMAGConfigurationErrorException {

        auditLog.logRecord(actionDescription,
                auditCode.getLogMessageId(),
                auditCode.getSeverity(),
                auditCode.getFormattedLogMessage(topicName),
                null,
                auditCode.getSystemAction(),
                auditCode.getUserAction());


        try {
            topicConnector.start();
        } catch (ConnectorCheckedException e) {
            auditCode = DataPlatformAuditCode.ERROR_INITIALIZING_DATA_PLATFORM_TOPIC_CONNECTION;
            auditLog.logRecord(actionDescription,
                    auditCode.getLogMessageId(),
                    auditCode.getSeverity(),
                    auditCode.getFormattedLogMessage(topicName, serverName),
                    null,
                    auditCode.getSystemAction(),
                    auditCode.getUserAction());
            throw new OMAGConfigurationErrorException(400,
                    DataPlatformAdmin.class.getSimpleName(),
                    actionDescription,
                    auditCode.getFormattedLogMessage(),
                    auditCode.getSystemAction(),
                    auditCode.getUserAction()
            );
        }
    }


    /**
     * Returns the topic created based on connection properties
     *
     * @param topicConnection properties of the topic
     * @return the topic created based on the connection properties
     */
    private OpenMetadataTopicConnector initializeDataPlatformTopicConnector(Connection topicConnection) {
        final String actionDescription = "initialize";
        if (topicConnection != null) {
            try {
                return getTopicConnector(topicConnection);
            } catch (Exception e) {
                DataPlatformAuditCode auditCode = DataPlatformAuditCode.ERROR_INITIALIZING_CONNECTION;
                auditLog.logRecord(actionDescription,
                        auditCode.getLogMessageId(),
                        auditCode.getSeverity(),
                        auditCode.getFormattedLogMessage(topicConnection.toString(), serverName, e.getMessage()),
                        null,
                        auditCode.getSystemAction(),
                        auditCode.getUserAction());
                throw e;
            }

        }
        return null;

    }


    /**
     * Returns the connector created from topic connection properties
     *
     * @param topicConnection properties of the topic connection
     * @return the connector created based on the topic connection properties
     */
    private OpenMetadataTopicConnector getTopicConnector(Connection topicConnection) {
        try {
            ConnectorBroker connectorBroker = new ConnectorBroker();

            OpenMetadataTopicConnector topicConnector = (OpenMetadataTopicConnector) connectorBroker.getConnector(topicConnection);

            topicConnector.setAuditLog(auditLog.createNewAuditLog(OMRSAuditingComponent.OPEN_METADATA_TOPIC_CONNECTOR));

            return topicConnector;
        } catch (Throwable error) {
            String methodName = "getTopicConnector";

            if (log.isDebugEnabled()) {
                log.debug("Unable to create topic connector: " + error.toString());
            }

            OMRSErrorCode errorCode = OMRSErrorCode.NULL_TOPIC_CONNECTOR;
            String errorMessage = errorCode.getErrorMessageId()
                    + errorCode.getFormattedErrorMessage("getTopicConnector");

            throw new OMRSConfigErrorException(errorCode.getHTTPErrorCode(),
                    this.getClass().getName(),
                    methodName,
                    errorMessage,
                    errorCode.getSystemAction(),
                    errorCode.getUserAction(),
                    error);

        }
    }


    private List<String> extractSupportedZones(Map<String, Object> accessServiceOptions) throws OMAGConfigurationErrorException {
        final String methodName = "extractSupportedZones";
        DataPlatformAuditCode auditCode;

        if (accessServiceOptions == null) {
            return null;
        } else {
            Object zoneListObject = accessServiceOptions.get(supportedZonesPropertyName);

            if (zoneListObject == null) {
                auditCode = DataPlatformAuditCode.ALL_ZONES;
                auditLog.logRecord(methodName,
                        auditCode.getLogMessageId(),
                        auditCode.getSeverity(),
                        auditCode.getFormattedLogMessage(),
                        null,
                        auditCode.getSystemAction(),
                        auditCode.getUserAction());
                return null;
            } else {
                try {
                    List<String> zoneList = (List<String>) zoneListObject;

                    auditCode = DataPlatformAuditCode.SUPPORTED_ZONES;
                    auditLog.logRecord(methodName,
                            auditCode.getLogMessageId(),
                            auditCode.getSeverity(),
                            auditCode.getFormattedLogMessage(zoneList.toString()),
                            null,
                            auditCode.getSystemAction(),
                            auditCode.getUserAction());

                    return zoneList;
                } catch (Throwable error) {
                    auditCode = DataPlatformAuditCode.BAD_CONFIG;
                    auditLog.logRecord(methodName,
                            auditCode.getLogMessageId(),
                            auditCode.getSeverity(),
                            auditCode.getFormattedLogMessage(zoneListObject.toString(), supportedZonesPropertyName),
                            null,
                            auditCode.getSystemAction(),
                            auditCode.getUserAction());

                    DataPlatformErrorCode errorCode = DataPlatformErrorCode.BAD_CONFIG;

                    String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(zoneListObject.toString(),
                            supportedZonesPropertyName,
                            error.getClass().getName(),
                            error.getMessage());

                    throw new OMAGConfigurationErrorException(errorCode.getHTTPErrorCode(),
                            this.getClass().getName(),
                            methodName,
                            errorMessage,
                            errorCode.getSystemAction(),
                            errorCode.getUserAction(),
                            error);
                }
            }
        }
    }

    /**
     * Shutdown the access service.
     */
    public void shutdown() {
        try {
            dataPlatformInTopicConnector.disconnect();
        } catch (ConnectorCheckedException e) {
            log.error("Error disconnecting data platform topic connector");
        }

        if (instance != null) {
            instance.shutdown();
        }

        final String actionDescription = "shutdown";
        DataPlatformAuditCode auditCode;

        auditCode = DataPlatformAuditCode.SERVICE_SHUTDOWN;
        auditLog.logRecord(actionDescription,
                auditCode.getLogMessageId(),
                auditCode.getSeverity(),
                auditCode.getFormattedLogMessage(serverName),
                null,
                auditCode.getSystemAction(),
                auditCode.getUserAction());
    }
}

