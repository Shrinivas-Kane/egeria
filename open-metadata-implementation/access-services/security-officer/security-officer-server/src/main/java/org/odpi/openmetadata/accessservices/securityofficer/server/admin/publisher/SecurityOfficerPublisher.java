/*
 *  SPDX-License-Identifier: Apache-2.0
 *  Copyright Contributors to the ODPi Egeria project.
 */
package org.odpi.openmetadata.accessservices.securityofficer.server.admin.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.odpi.openmetadata.accessservices.securityofficer.api.events.SecurityOfficerEvent;
import org.odpi.openmetadata.accessservices.securityofficer.api.ffdc.errorcode.SecurityOfficerErrorCode;
import org.odpi.openmetadata.accessservices.securityofficer.server.admin.processor.SecurityOfficerEventProcessor;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditLog;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditLogRecordSeverity;
import org.odpi.openmetadata.repositoryservices.connectors.openmetadatatopic.OpenMetadataTopicConnector;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.EntityDetail;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.InstanceGraph;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.InstanceProvenanceType;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.InstanceType;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.Relationship;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.typedefs.TypeDefSummary;
import org.odpi.openmetadata.repositoryservices.events.OMRSInstanceEvent;
import org.odpi.openmetadata.repositoryservices.events.OMRSInstanceEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityOfficerPublisher extends OMRSInstanceEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(SecurityOfficerPublisher.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private SecurityOfficerEventProcessor securityOfficerEventProcessor;
    private OpenMetadataTopicConnector openMetadataTopicConnector;
    private OMRSAuditLog auditLog;

    public SecurityOfficerPublisher(SecurityOfficerEventProcessor securityOfficerEventProcessor, OpenMetadataTopicConnector openMetadataTopicConnector, OMRSAuditLog auditLog) {
        this.securityOfficerEventProcessor = securityOfficerEventProcessor;
        this.openMetadataTopicConnector = openMetadataTopicConnector;
        this.auditLog = auditLog;
    }

    @Override
    public void sendInstanceEvent(String sourceName, OMRSInstanceEvent instanceEvent) {
    }

    @Override
    public void processNewEntityEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, EntityDetail entity) {
    }

    @Override
    public void processUpdatedEntityEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, EntityDetail oldEntity, EntityDetail newEntity) {
    }

    @Override
    public void processUndoneEntityEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, EntityDetail entity) {
    }

    @Override
    public void processClassifiedEntityEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, EntityDetail entity) {
    }

    @Override
    public void processDeclassifiedEntityEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, EntityDetail entity) {
    }

    @Override
    public void processReclassifiedEntityEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, EntityDetail entity) {
    }

    @Override
    public void processDeletedEntityEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, EntityDetail entity) {
    }

    @Override
    public void processRestoredEntityEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, EntityDetail entity) {
    }

    @Override
    public void processPurgedEntityEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, String typeDefGUID, String typeDefName, String instanceGUID) {
    }

    @Override
    public void processReTypedEntityEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, TypeDefSummary originalTypeDef, EntityDetail entity) {
    }

    @Override
    public void processReHomedEntityEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, String originalHomeMetadataCollectionId, EntityDetail entity) {
    }

    @Override
    public void processReIdentifiedEntityEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, String originalEntityGUID, EntityDetail entity) {
    }

    @Override
    public void processRefreshEntityRequested(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, String typeDefGUID, String typeDefName, String instanceGUID, String homeMetadataCollectionId) {
    }

    @Override
    public void processRefreshEntityEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, EntityDetail entity) {
    }

    @Override
    public void processNewRelationshipEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, Relationship relationship) {
        if (relationship.getType().getTypeDefName().equals("SemanticAssignment") && isSchemaElement(relationship.getEntityOneProxy().getType())) {
            SecurityOfficerEvent securityOfficerEvent = securityOfficerEventProcessor.processSemanticAssignmentForSchemaElement(relationship);

            try {
                sendEvent(securityOfficerEvent);
            } catch (JsonProcessingException e) {
                log.debug("Unable to build the event");
            }
        } else {
            log.debug("No processing needed");
        }
    }

    private boolean isSchemaElement(InstanceType type) {
        return type.getTypeDefSuperTypes().stream().anyMatch(typeDefLink -> typeDefLink.getName().equals("SchemaElement"));
    }

    @Override
    public void processUpdatedRelationshipEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, Relationship oldRelationship, Relationship newRelationship) {
    }

    @Override
    public void processUndoneRelationshipEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, Relationship relationship) {
    }

    @Override
    public void processDeletedRelationshipEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, Relationship relationship) {
    }

    @Override
    public void processRestoredRelationshipEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, Relationship relationship) {
    }

    @Override
    public void processPurgedRelationshipEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, String typeDefGUID, String typeDefName, String instanceGUID) {
    }

    @Override
    public void processReTypedRelationshipEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, TypeDefSummary originalTypeDef, Relationship relationship) {
    }

    @Override
    public void processReHomedRelationshipEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, String originalHomeMetadataCollectionId, Relationship relationship) {
    }

    @Override
    public void processReIdentifiedRelationshipEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, String originalRelationshipGUID, Relationship relationship) {
    }

    @Override
    public void processRefreshRelationshipRequest(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, String typeDefGUID, String typeDefName, String instanceGUID, String homeMetadataCollectionId) {
    }

    @Override
    public void processRefreshRelationshipEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, Relationship relationship) {
    }

    @Override
    public void processInstanceBatchEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, InstanceGraph instances) {
    }

    @Override
    public void processConflictingInstancesEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, String targetMetadataCollectionId, TypeDefSummary targetTypeDef, String targetInstanceGUID, String otherMetadataCollectionId, InstanceProvenanceType otherOrigin, TypeDefSummary otherTypeDef, String otherInstanceGUID, String errorMessage) {
    }

    @Override
    public void processConflictingTypeEvent(String sourceName, String originatorMetadataCollectionId, String originatorServerName, String originatorServerType, String originatorOrganizationName, String targetMetadataCollectionId, TypeDefSummary targetTypeDef, String targetInstanceGUID, TypeDefSummary otherTypeDef, String errorMessage) {
    }

    public void sendEvent(SecurityOfficerEvent securityOfficerEvent) throws JsonProcessingException {
        String event = OBJECT_MAPPER.writeValueAsString(securityOfficerEvent);

        try {
            openMetadataTopicConnector.sendEvent(event);
        } catch (ConnectorCheckedException e) {
            log.error("Exception publishing event", e);
            SecurityOfficerErrorCode auditCode = SecurityOfficerErrorCode.PUBLISH_EVENT_EXCEPTION;

            auditLog.logException("send event",
                    auditCode.getErrorMessageId(),
                    OMRSAuditLogRecordSeverity.EXCEPTION,
                    auditCode.getFormattedErrorMessage(securityOfficerEvent.getClass().getName(), e.getMessage()),
                    "event {" + securityOfficerEvent + "}",
                    auditCode.getSystemAction(),
                    auditCode.getUserAction(),
                    e);
        }
    }
}