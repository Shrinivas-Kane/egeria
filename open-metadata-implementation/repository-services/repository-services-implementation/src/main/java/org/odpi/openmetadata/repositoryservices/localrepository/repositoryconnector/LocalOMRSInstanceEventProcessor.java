/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.repositoryservices.localrepository.repositoryconnector;

import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.*;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.typedefs.TypeDefCategory;
import org.odpi.openmetadata.repositoryservices.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditCode;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditLog;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.OMRSLogicErrorException;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.OMRSMetadataCollection;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.typedefs.TypeDefSummary;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryConnector;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryHelper;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryValidator;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryeventmapper.OMRSRepositoryEventProcessor;
import org.odpi.openmetadata.repositoryservices.eventmanagement.*;
import org.odpi.openmetadata.repositoryservices.ffdc.OMRSErrorCode;

import java.util.UUID;


/**
 * LocalOMRSInstanceEventProcessor processes incoming metadata instance events that describe changes to the
 * entities and relationships in other repositories in the connected cohorts.
 * It uses the save exchange rule to decide which events to process and which to ignore.
 * Events that are to be processed are converted into reference copies of their respective entities and
 * relationships and stored in the local repository.
 */
public class LocalOMRSInstanceEventProcessor extends OMRSInstanceEventProcessor implements OMRSInstanceRetrievalEventProcessor
{
    private String                          localMetadataCollectionId;
    private String                          localServerName;
    private OMRSRepositoryConnector         localRepositoryConnector;
    private OMRSRepositoryHelper            repositoryHelper;
    private OMRSRepositoryValidator         repositoryValidator;
    private OMRSRepositoryEventExchangeRule saveExchangeRule;
    private OMRSMetadataCollection          localMetadataCollection = null;
    private OMRSRepositoryEventProcessor    outboundRepositoryEventProcessor;

    /*
     * The audit log provides a verifiable record of the open metadata archives that have been loaded into
     * the open metadata repository.  The Logger is for standard debug.
     */
    private OMRSAuditLog auditLog;

    private static final Logger       log      = LoggerFactory.getLogger(LocalOMRSInstanceEventProcessor.class);


    /**
     * Constructor saves all of the information necessary to process incoming instance events.  It is intolerant
     * of nulls in any of its parameters and will throw a logic error exception is it finds any.
     *
     * @param localMetadataCollectionId        local metadata collection identifier
     * @param localServerName                  name of the local server for logging
     * @param localConnector                   connector to the  local repository
     * @param repositoryHelper                 helper class for building instances
     * @param repositoryValidator              helper class for validating instances
     * @param saveExchangeRule                 rule that determines which events to process.
     * @param outboundRepositoryEventProcessor event processor
     * @param auditLog                         audit log for this component.
     */
    LocalOMRSInstanceEventProcessor(String                          localMetadataCollectionId,
                                    String                          localServerName,
                                    OMRSRepositoryConnector         localConnector,
                                    OMRSRepositoryHelper            repositoryHelper,
                                    OMRSRepositoryValidator         repositoryValidator,
                                    OMRSRepositoryEventExchangeRule saveExchangeRule,
                                    OMRSRepositoryEventProcessor    outboundRepositoryEventProcessor,
                                    OMRSAuditLog                    auditLog)
    {
        final String methodName = "LocalOMRSInstanceEventProcessor constructor";

        this.localMetadataCollectionId = localMetadataCollectionId;
        this.localServerName = localServerName;
        this.localRepositoryConnector = localConnector;
        this.repositoryHelper = repositoryHelper;
        this.repositoryValidator = repositoryValidator;
        this.saveExchangeRule = saveExchangeRule;
        this.outboundRepositoryEventProcessor = outboundRepositoryEventProcessor;
        this.auditLog = auditLog;

        if (localConnector != null)
        {
            try
            {
                this.localMetadataCollection = localConnector.getMetadataCollection();
            }
            catch (Throwable  error)
            {
                /*
                 * Nothing to do, error will be logged in verifyEventProcessor
                 */
                this.localMetadataCollection = null;
            }
        }

        this.verifyEventProcessor(methodName);
    }


    /*
     * ====================================
     * OMRSInstanceEventProcessor
     */


    /**
     * Unpack and process the incoming event
     *
     * @param cohortName source of the event
     * @param instanceEvent the event to process
     */
    public void   sendInstanceEvent(String            cohortName,
                                    OMRSInstanceEvent instanceEvent)
    {
        final String methodName = "sendInstanceEvent";

        OMRSInstanceEventType instanceEventType       = instanceEvent.getInstanceEventType();
        OMRSEventOriginator   instanceEventOriginator = instanceEvent.getEventOriginator();


        if ((instanceEventType != null) && (instanceEventOriginator != null))
        {
            OMRSAuditCode auditCode = OMRSAuditCode.PROCESS_INCOMING_EVENT;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(instanceEventType.getName(),
                                                                instanceEventOriginator.toString()),
                               instanceEvent.toString(),
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());

            switch (instanceEventType)
            {
                case NEW_ENTITY_EVENT:
                    this.processNewEntityEvent(cohortName,
                                               instanceEventOriginator.getMetadataCollectionId(),
                                               instanceEventOriginator.getServerName(),
                                               instanceEventOriginator.getServerType(),
                                               instanceEventOriginator.getOrganizationName(),
                                               instanceEvent.getEntity());
                    break;

                case UPDATED_ENTITY_EVENT:
                    this.processUpdatedEntityEvent(cohortName,
                                                   instanceEventOriginator.getMetadataCollectionId(),
                                                   instanceEventOriginator.getServerName(),
                                                   instanceEventOriginator.getServerType(),
                                                   instanceEventOriginator.getOrganizationName(),
                                                   instanceEvent.getOriginalEntity(),
                                                   instanceEvent.getEntity());
                    break;

                case CLASSIFIED_ENTITY_EVENT:
                    this.processClassifiedEntityEvent(cohortName,
                                                      instanceEventOriginator.getMetadataCollectionId(),
                                                      instanceEventOriginator.getServerName(),
                                                      instanceEventOriginator.getServerType(),
                                                      instanceEventOriginator.getOrganizationName(),
                                                      instanceEvent.getEntity());
                    break;

                case RECLASSIFIED_ENTITY_EVENT:
                    this.processReclassifiedEntityEvent(cohortName,
                                                        instanceEventOriginator.getMetadataCollectionId(),
                                                        instanceEventOriginator.getServerName(),
                                                        instanceEventOriginator.getServerType(),
                                                        instanceEventOriginator.getOrganizationName(),
                                                        instanceEvent.getEntity());
                    break;

                case DECLASSIFIED_ENTITY_EVENT:
                    this.processDeclassifiedEntityEvent(cohortName,
                                                        instanceEventOriginator.getMetadataCollectionId(),
                                                        instanceEventOriginator.getServerName(),
                                                        instanceEventOriginator.getServerType(),
                                                        instanceEventOriginator.getOrganizationName(),
                                                        instanceEvent.getEntity());
                    break;

                case DELETED_ENTITY_EVENT:
                    this.processDeletedEntityEvent(cohortName,
                                                   instanceEventOriginator.getMetadataCollectionId(),
                                                   instanceEventOriginator.getServerName(),
                                                   instanceEventOriginator.getServerType(),
                                                   instanceEventOriginator.getOrganizationName(),
                                                   instanceEvent.getEntity());
                    break;

                case PURGED_ENTITY_EVENT:
                    this.processPurgedEntityEvent(cohortName,
                                                  instanceEventOriginator.getMetadataCollectionId(),
                                                  instanceEventOriginator.getServerName(),
                                                  instanceEventOriginator.getServerType(),
                                                  instanceEventOriginator.getOrganizationName(),
                                                  instanceEvent.getTypeDefGUID(),
                                                  instanceEvent.getTypeDefName(),
                                                  instanceEvent.getInstanceGUID());
                    break;

                case DELETE_PURGED_ENTITY_EVENT:
                    this.processDeletePurgedEntityEvent(cohortName,
                                                        instanceEventOriginator.getMetadataCollectionId(),
                                                        instanceEventOriginator.getServerName(),
                                                        instanceEventOriginator.getServerType(),
                                                        instanceEventOriginator.getOrganizationName(),
                                                        instanceEvent.getEntity());
                    break;

                case UNDONE_ENTITY_EVENT:
                    this.processUndoneEntityEvent(cohortName,
                                                  instanceEventOriginator.getMetadataCollectionId(),
                                                  instanceEventOriginator.getServerName(),
                                                  instanceEventOriginator.getServerType(),
                                                  instanceEventOriginator.getOrganizationName(),
                                                  instanceEvent.getEntity());
                    break;

                case RESTORED_ENTITY_EVENT:
                    this.processRestoredEntityEvent(cohortName,
                                                    instanceEventOriginator.getMetadataCollectionId(),
                                                    instanceEventOriginator.getServerName(),
                                                    instanceEventOriginator.getServerType(),
                                                    instanceEventOriginator.getOrganizationName(),
                                                    instanceEvent.getEntity());
                    break;

                case REFRESH_ENTITY_REQUEST:
                    this.processRefreshEntityRequested(cohortName,
                                                       instanceEventOriginator.getMetadataCollectionId(),
                                                       instanceEventOriginator.getServerName(),
                                                       instanceEventOriginator.getServerType(),
                                                       instanceEventOriginator.getOrganizationName(),
                                                       instanceEvent.getTypeDefGUID(),
                                                       instanceEvent.getTypeDefName(),
                                                       instanceEvent.getInstanceGUID(),
                                                       instanceEvent.getHomeMetadataCollectionId());
                    break;

                case REFRESHED_ENTITY_EVENT:
                    this.processRefreshEntityEvent(cohortName,
                                                   instanceEventOriginator.getMetadataCollectionId(),
                                                   instanceEventOriginator.getServerName(),
                                                   instanceEventOriginator.getServerType(),
                                                   instanceEventOriginator.getOrganizationName(),
                                                   instanceEvent.getEntity());
                    break;

                case RE_HOMED_ENTITY_EVENT:
                    this.processReHomedEntityEvent(cohortName,
                                                   instanceEventOriginator.getMetadataCollectionId(),
                                                   instanceEventOriginator.getServerName(),
                                                   instanceEventOriginator.getServerType(),
                                                   instanceEventOriginator.getOrganizationName(),
                                                   instanceEvent.getOriginalHomeMetadataCollectionId(),
                                                   instanceEvent.getEntity());
                    break;

                case RETYPED_ENTITY_EVENT:
                    this.processReTypedEntityEvent(cohortName,
                                                   instanceEventOriginator.getMetadataCollectionId(),
                                                   instanceEventOriginator.getServerName(),
                                                   instanceEventOriginator.getServerType(),
                                                   instanceEventOriginator.getOrganizationName(),
                                                   instanceEvent.getOriginalTypeDefSummary(),
                                                   instanceEvent.getEntity());
                    break;

                case RE_IDENTIFIED_ENTITY_EVENT:
                    this.processReIdentifiedEntityEvent(cohortName,
                                                        instanceEventOriginator.getMetadataCollectionId(),
                                                        instanceEventOriginator.getServerName(),
                                                        instanceEventOriginator.getServerType(),
                                                        instanceEventOriginator.getOrganizationName(),
                                                        instanceEvent.getOriginalInstanceGUID(),
                                                        instanceEvent.getEntity());
                    break;

                case NEW_RELATIONSHIP_EVENT:
                    this.processNewRelationshipEvent(cohortName,
                                                     instanceEventOriginator.getMetadataCollectionId(),
                                                     instanceEventOriginator.getServerName(),
                                                     instanceEventOriginator.getServerType(),
                                                     instanceEventOriginator.getOrganizationName(),
                                                     instanceEvent.getRelationship());
                    break;

                case UPDATED_RELATIONSHIP_EVENT:
                    this.processUpdatedRelationshipEvent(cohortName,
                                                         instanceEventOriginator.getMetadataCollectionId(),
                                                         instanceEventOriginator.getServerName(),
                                                         instanceEventOriginator.getServerType(),
                                                         instanceEventOriginator.getOrganizationName(),
                                                         instanceEvent.getOriginalRelationship(),
                                                         instanceEvent.getRelationship());
                    break;

                case UNDONE_RELATIONSHIP_EVENT:
                    this.processUndoneRelationshipEvent(cohortName,
                                                        instanceEventOriginator.getMetadataCollectionId(),
                                                        instanceEventOriginator.getServerName(),
                                                        instanceEventOriginator.getServerType(),
                                                        instanceEventOriginator.getOrganizationName(),
                                                        instanceEvent.getRelationship());
                    break;

                case DELETED_RELATIONSHIP_EVENT:
                    this.processDeletedRelationshipEvent(cohortName,
                                                         instanceEventOriginator.getMetadataCollectionId(),
                                                         instanceEventOriginator.getServerName(),
                                                         instanceEventOriginator.getServerType(),
                                                         instanceEventOriginator.getOrganizationName(),
                                                         instanceEvent.getRelationship());
                    break;

                case PURGED_RELATIONSHIP_EVENT:
                    this.processPurgedRelationshipEvent(cohortName,
                                                        instanceEventOriginator.getMetadataCollectionId(),
                                                        instanceEventOriginator.getServerName(),
                                                        instanceEventOriginator.getServerType(),
                                                        instanceEventOriginator.getOrganizationName(),
                                                        instanceEvent.getTypeDefGUID(),
                                                        instanceEvent.getTypeDefName(),
                                                        instanceEvent.getInstanceGUID());
                    break;

                case DELETE_PURGED_RELATIONSHIP_EVENT:
                    this.processDeletePurgedRelationshipEvent(cohortName,
                                                              instanceEventOriginator.getMetadataCollectionId(),
                                                              instanceEventOriginator.getServerName(),
                                                              instanceEventOriginator.getServerType(),
                                                              instanceEventOriginator.getOrganizationName(),
                                                              instanceEvent.getRelationship());
                    break;

                case RESTORED_RELATIONSHIP_EVENT:
                    this.processRestoredRelationshipEvent(cohortName,
                                                          instanceEventOriginator.getMetadataCollectionId(),
                                                          instanceEventOriginator.getServerName(),
                                                          instanceEventOriginator.getServerType(),
                                                          instanceEventOriginator.getOrganizationName(),
                                                          instanceEvent.getRelationship());
                    break;

                case REFRESH_RELATIONSHIP_REQUEST:
                    this.processRefreshRelationshipRequest(cohortName,
                                                           instanceEventOriginator.getMetadataCollectionId(),
                                                           instanceEventOriginator.getServerName(),
                                                           instanceEventOriginator.getServerType(),
                                                           instanceEventOriginator.getOrganizationName(),
                                                           instanceEvent.getTypeDefGUID(),
                                                           instanceEvent.getTypeDefName(),
                                                           instanceEvent.getInstanceGUID(),
                                                           instanceEvent.getHomeMetadataCollectionId());
                    break;

                case REFRESHED_RELATIONSHIP_EVENT:
                    this.processRefreshRelationshipEvent(cohortName,
                                                         instanceEventOriginator.getMetadataCollectionId(),
                                                         instanceEventOriginator.getServerName(),
                                                         instanceEventOriginator.getServerType(),
                                                         instanceEventOriginator.getOrganizationName(),
                                                         instanceEvent.getRelationship());
                    break;

                case RE_IDENTIFIED_RELATIONSHIP_EVENT:
                    this.processReIdentifiedRelationshipEvent(cohortName,
                                                              instanceEventOriginator.getMetadataCollectionId(),
                                                              instanceEventOriginator.getServerName(),
                                                              instanceEventOriginator.getServerType(),
                                                              instanceEventOriginator.getOrganizationName(),
                                                              instanceEvent.getOriginalInstanceGUID(),
                                                              instanceEvent.getRelationship());
                    break;

                case RE_HOMED_RELATIONSHIP_EVENT:
                    this.processReHomedRelationshipEvent(cohortName,
                                                         instanceEventOriginator.getMetadataCollectionId(),
                                                         instanceEventOriginator.getServerName(),
                                                         instanceEventOriginator.getServerType(),
                                                         instanceEventOriginator.getOrganizationName(),
                                                         instanceEvent.getOriginalHomeMetadataCollectionId(),
                                                         instanceEvent.getRelationship());
                    break;

                case RETYPED_RELATIONSHIP_EVENT:
                    this.processReTypedRelationshipEvent(cohortName,
                                                         instanceEventOriginator.getMetadataCollectionId(),
                                                         instanceEventOriginator.getServerName(),
                                                         instanceEventOriginator.getServerType(),
                                                         instanceEventOriginator.getOrganizationName(),
                                                         instanceEvent.getOriginalTypeDefSummary(),
                                                         instanceEvent.getRelationship());
                    break;
                case BATCH_INSTANCES_EVENT:
                	this.processInstanceBatchEvent(cohortName,
                            instanceEventOriginator.getMetadataCollectionId(),
                            instanceEventOriginator.getServerName(),
                            instanceEventOriginator.getServerType(),
                            instanceEventOriginator.getOrganizationName(),
                            instanceEvent.getInstanceBatch());
                	break;
                case INSTANCE_ERROR_EVENT:
                    OMRSInstanceEventErrorCode errorCode = instanceEvent.getErrorCode();

                    if (errorCode != null)
                    {
                        switch(errorCode)
                        {
                            case CONFLICTING_INSTANCES:
                                this.processConflictingInstancesEvent(cohortName,
                                                                      instanceEventOriginator.getMetadataCollectionId(),
                                                                      instanceEventOriginator.getServerName(),
                                                                      instanceEventOriginator.getServerType(),
                                                                      instanceEventOriginator.getOrganizationName(),
                                                                      instanceEvent.getTargetMetadataCollectionId(),
                                                                      instanceEvent.getTargetTypeDefSummary(),
                                                                      instanceEvent.getTargetInstanceGUID(),
                                                                      instanceEvent.getOtherMetadataCollectionId(),
                                                                      instanceEvent.getOtherOrigin(),
                                                                      instanceEvent.getOtherTypeDefSummary(),
                                                                      instanceEvent.getOtherInstanceGUID(),
                                                                      instanceEvent.getErrorMessage());
                                break;

                            case CONFLICTING_TYPE:
                                this.processConflictingTypeEvent(cohortName,
                                                                 instanceEventOriginator.getMetadataCollectionId(),
                                                                 instanceEventOriginator.getServerName(),
                                                                 instanceEventOriginator.getServerType(),
                                                                 instanceEventOriginator.getOrganizationName(),
                                                                 instanceEvent.getTargetMetadataCollectionId(),
                                                                 instanceEvent.getTargetTypeDefSummary(),
                                                                 instanceEvent.getTargetInstanceGUID(),
                                                                 instanceEvent.getOtherTypeDefSummary(),
                                                                 instanceEvent.getErrorMessage());
                                break;

                            default:
                                log.debug("Unknown instance event error code, ignoring event");
                                break;
                        }
                    }
                    else
                    {
                        log.debug("Ignored Instance event, null error code");
                    }
                    break;

                default:
                    log.debug("Ignored Instance event, unknown type");
                    break;
            }
        }
        else
        {
            log.debug("Ignored instance event, null type");
        }
    }


    /**
     * A new entity has been created.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param entity                         details of the new entity
     */
    public void processNewEntityEvent(String       sourceName,
                                      String       originatorMetadataCollectionId,
                                      String       originatorServerName,
                                      String       originatorServerType,
                                      String       originatorOrganizationName,
                                      EntityDetail entity)
    {
        final String methodName = "processNewEntityEvent";

        updateReferenceEntity(sourceName,
                              methodName,
                              originatorMetadataCollectionId,
                              originatorServerName,
                              entity);
    }


    /**
     * An existing entity has been updated.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param oldEntity                      original values for the entity.
     * @param newEntity                      details of the new version of the entity.
     */
    public void processUpdatedEntityEvent(String       sourceName,
                                          String       originatorMetadataCollectionId,
                                          String       originatorServerName,
                                          String       originatorServerType,
                                          String       originatorOrganizationName,
                                          EntityDetail oldEntity,
                                          EntityDetail newEntity)
    {
        final String methodName = "processUpdatedEntityEvent";

        updateReferenceEntity(sourceName,
                              methodName,
                              originatorMetadataCollectionId,
                              originatorServerName,
                              newEntity);
    }


    /**
     * An update to an entity has been undone.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param entity                         details of the version of the entity that has been restored.
     */
    public void processUndoneEntityEvent(String       sourceName,
                                         String       originatorMetadataCollectionId,
                                         String       originatorServerName,
                                         String       originatorServerType,
                                         String       originatorOrganizationName,
                                         EntityDetail entity)
    {
        final String methodName = "processUndoneEntityEvent";

        updateReferenceEntity(sourceName,
                              methodName,
                              originatorMetadataCollectionId,
                              originatorServerName,
                              entity);
    }


    /**
     * A new classification has been added to an entity.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param entity                         details of the entity with the new classification added.
     */
    public void processClassifiedEntityEvent(String sourceName,
                                             String originatorMetadataCollectionId,
                                             String originatorServerName,
                                             String originatorServerType,
                                             String originatorOrganizationName,
                                             EntityDetail entity)
    {
        final String methodName = "processClassifiedEntityEvent";

        updateReferenceEntity(sourceName,
                              methodName,
                              originatorMetadataCollectionId,
                              originatorServerName,
                              entity);
    }


    /**
     * A classification has been removed from an entity.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param entity                         details of the entity after the classification has been removed.
     */
    public void processDeclassifiedEntityEvent(String sourceName,
                                               String originatorMetadataCollectionId,
                                               String originatorServerName,
                                               String originatorServerType,
                                               String originatorOrganizationName,
                                               EntityDetail entity)
    {
        final String methodName = "processDeclassifiedEntityEvent";

        updateReferenceEntity(sourceName,
                              methodName,
                              originatorMetadataCollectionId,
                              originatorServerName,
                              entity);
    }


    /**
     * An existing classification has been changed on an entity.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param entity                         details of the entity after the classification has been changed.
     */
    public void processReclassifiedEntityEvent(String       sourceName,
                                               String       originatorMetadataCollectionId,
                                               String       originatorServerName,
                                               String       originatorServerType,
                                               String       originatorOrganizationName,
                                               EntityDetail entity)
    {
        final String methodName = "processReclassifiedEntityEvent";

        updateReferenceEntity(sourceName,
                              methodName,
                              originatorMetadataCollectionId,
                              originatorServerName,
                              entity);
    }


    /**
     * An existing entity has been deleted.  This is a soft delete. This means it is still in the repository
     * but it is no longer returned on queries.
     * <p>
     * All relationships to the entity are also soft-deleted and will no longer be usable.  These deleted relationships
     * will be notified through separate events.
     * <p>
     * Details of the TypeDef are included with the entity's unique id (guid) to ensure the right entity is deleted in
     * the remote repositories.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param entity                         deleted entity
     */
    public void processDeletedEntityEvent(String       sourceName,
                                          String       originatorMetadataCollectionId,
                                          String       originatorServerName,
                                          String       originatorServerType,
                                          String       originatorOrganizationName,
                                          EntityDetail entity)
    {
        final String methodName = "processDeletedEntityEvent";

        updateReferenceEntity(sourceName,
                              methodName,
                              originatorMetadataCollectionId,
                              originatorServerName,
                              entity);
    }


    /**
     * A deleted entity has been permanently removed from the repository.  This request can not be undone.
     * <p>
     * Details of the TypeDef are included with the entity's unique id (guid) to ensure the right entity is purged in
     * the remote repositories.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param typeDefGUID                    unique identifier for this entity's TypeDef
     * @param typeDefName                    name of this entity's TypeDef
     * @param instanceGUID                   unique identifier for the entity
     */
    public void processPurgedEntityEvent(String sourceName,
                                         String originatorMetadataCollectionId,
                                         String originatorServerName,
                                         String originatorServerType,
                                         String originatorOrganizationName,
                                         String typeDefGUID,
                                         String typeDefName,
                                         String instanceGUID)
    {
        final String methodName = "processPurgedEntityEvent";

        try
        {
            verifyEventProcessor(methodName);

            localMetadataCollection.purgeEntityReferenceCopy(localRepositoryConnector.getServerUserId(),
                                                             instanceGUID,
                                                             typeDefGUID,
                                                             typeDefName,
                                                             originatorMetadataCollectionId);

        }
        catch (Throwable error)
        {
            handleUnexpectedErrorFromEvent(error,
                                           methodName,
                                           originatorServerName,
                                           originatorMetadataCollectionId);
        }
    }


    /**
     * A deleted entity has been restored to the state it was before it was deleted.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param entity                         details of the version of the entity that has been restored.
     */
    public void processRestoredEntityEvent(String       sourceName,
                                           String       originatorMetadataCollectionId,
                                           String       originatorServerName,
                                           String       originatorServerType,
                                           String       originatorOrganizationName,
                                           EntityDetail entity)
    {
        final String methodName = "processRestoredEntityEvent";

        updateReferenceEntity(sourceName,
                              methodName,
                              originatorMetadataCollectionId,
                              originatorServerName,
                              entity);
    }


    /**
     * The guid of an existing entity has been changed to a new value.  This is used if two different
     * entities are discovered to have the same guid.  This is extremely unlikely but not impossible so
     * the open metadata protocol has provision for this.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param originalEntityGUID             the existing identifier for the entity.
     * @param entity                         new values for this entity, including the new guid.
     */
    public void processReIdentifiedEntityEvent(String       sourceName,
                                               String       originatorMetadataCollectionId,
                                               String       originatorServerName,
                                               String       originatorServerType,
                                               String       originatorOrganizationName,
                                               String       originalEntityGUID,
                                               EntityDetail entity)
    {
        final String methodName = "processReIdentifiedEntityEvent";

        updateReferenceEntity(sourceName,
                              methodName,
                              originatorMetadataCollectionId,
                              originatorServerName,
                              entity);
    }


    /**
     * An existing entity has had its type changed.  Typically this action is taken to move an entity's
     * type to either a super type (so the subtype can be deleted) or a new subtype (so additional properties can be
     * added.)  However, the type can be changed to any compatible type.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param originalTypeDefSummary         original details of this entity's TypeDef.
     * @param entity                         new values for this entity, including the new type information.
     */
    public void processReTypedEntityEvent(String         sourceName,
                                          String         originatorMetadataCollectionId,
                                          String         originatorServerName,
                                          String         originatorServerType,
                                          String         originatorOrganizationName,
                                          TypeDefSummary originalTypeDefSummary,
                                          EntityDetail   entity)
    {
        final String methodName = "processReTypedEntityEvent";

        updateReferenceEntity(sourceName,
                              methodName,
                              originatorMetadataCollectionId,
                              originatorServerName,
                              entity);
    }


    /**
     * An existing entity has changed home repository.  This action is taken for example, if a repository
     * becomes permanently unavailable, or if the user community updating this entity move to working
     * from a different repository in the open metadata repository cluster.
     *
     * @param sourceName                       name of the source of the event.  It may be the cohort name for incoming events or the
     *                                         local repository, or event mapper name.
     * @param originatorMetadataCollectionId   unique identifier for the metadata collection hosted by the server that
     *                                         sent the event.
     * @param originatorServerName             name of the server that the event came from.
     * @param originatorServerType             type of server that the event came from.
     * @param originatorOrganizationName       name of the organization that owns the server that sent the event.
     * @param originalHomeMetadataCollectionId unique identifier for the original home repository.
     * @param entity                           new values for this entity, including the new home information.
     */
    public void processReHomedEntityEvent(String       sourceName,
                                          String       originatorMetadataCollectionId,
                                          String       originatorServerName,
                                          String       originatorServerType,
                                          String       originatorOrganizationName,
                                          String       originalHomeMetadataCollectionId,
                                          EntityDetail entity)
    {
        final String methodName = "processReHomedEntityEvent";

        updateReferenceEntity(sourceName,
                              methodName,
                              originatorMetadataCollectionId,
                              originatorServerName,
                              entity);
    }


    /**
     * The remote repository is requesting that an entity from this repository's metadata collection is
     * refreshed so the remote repository can create a reference copy.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param typeDefGUID                    unique identifier for this entity's TypeDef
     * @param typeDefName                    name of this entity's TypeDef
     * @param instanceGUID                   unique identifier for the entity
     * @param homeMetadataCollectionId       metadata collection id for the home of this instance.
     */
    public void processRefreshEntityRequested(String sourceName,
                                              String originatorMetadataCollectionId,
                                              String originatorServerName,
                                              String originatorServerType,
                                              String originatorOrganizationName,
                                              String typeDefGUID,
                                              String typeDefName,
                                              String instanceGUID,
                                              String homeMetadataCollectionId)
    {
        final String  methodName = "processRefreshEntityRequested";

        try
        {
            verifyEventProcessor(methodName);

            if (localMetadataCollectionId.equals(homeMetadataCollectionId))
            {
                EntityDetail entity = localMetadataCollection.isEntityKnown(localRepositoryConnector.getServerUserId(),
                                                                            instanceGUID);

                if (entity != null)
                {
                    outboundRepositoryEventProcessor.processRefreshEntityEvent(localRepositoryConnector.getRepositoryName(),
                                                                               localMetadataCollectionId,
                                                                               localRepositoryConnector.getServerName(),
                                                                               localRepositoryConnector.getServerType(),
                                                                               localRepositoryConnector.getOrganizationName(),
                                                                               entity);
                }
            }
        }
        catch (Throwable error)
        {
            handleUnexpectedErrorFromEvent(error,
                                           methodName,
                                           originatorServerName,
                                           originatorMetadataCollectionId);
        }
    }


    /**
     * A remote repository in the cohort has sent entity details in response to a refresh request.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param entity                         details of the requested entity
     */
    public void processRefreshEntityEvent(String       sourceName,
                                          String       originatorMetadataCollectionId,
                                          String       originatorServerName,
                                          String       originatorServerType,
                                          String       originatorOrganizationName,
                                          EntityDetail entity)
    {
        final String methodName = "processReHomedEntityEvent";

        updateReferenceEntity(sourceName,
                              methodName,
                              originatorMetadataCollectionId,
                              originatorServerName,
                              entity);
    }


    /**
     * A new relationship has been created.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param relationship                   details of the new relationship
     */
    public void processNewRelationshipEvent(String       sourceName,
                                            String       originatorMetadataCollectionId,
                                            String       originatorServerName,
                                            String       originatorServerType,
                                            String       originatorOrganizationName,
                                            Relationship relationship)
    {
        final String methodName = "processNewRelationshipEvent";

        updateReferenceRelationship(sourceName,
                                    methodName,
                                    originatorMetadataCollectionId,
                                    originatorServerName,
                                    relationship);
    }


    /**
     * An existing relationship has been updated.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param oldRelationship                original details of the relationship.
     * @param newRelationship                details of the new version of the relationship.
     */
    public void processUpdatedRelationshipEvent(String       sourceName,
                                                String       originatorMetadataCollectionId,
                                                String       originatorServerName,
                                                String       originatorServerType,
                                                String       originatorOrganizationName,
                                                Relationship oldRelationship,
                                                Relationship newRelationship)
    {
        final String methodName = "processUpdatedRelationshipEvent";

        updateReferenceRelationship(sourceName,
                                    methodName,
                                    originatorMetadataCollectionId,
                                    originatorServerName,
                                    newRelationship);
    }


    /**
     * An update to a relationship has been undone.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param relationship                   details of the version of the relationship that has been restored.
     */
    public void processUndoneRelationshipEvent(String       sourceName,
                                               String       originatorMetadataCollectionId,
                                               String       originatorServerName,
                                               String       originatorServerType,
                                               String       originatorOrganizationName,
                                               Relationship relationship)
    {
        final String methodName = "processUndoneRelationshipEvent";

        updateReferenceRelationship(sourceName,
                                    methodName,
                                    originatorMetadataCollectionId,
                                    originatorServerName,
                                    relationship);
    }


    /**
     * An existing relationship has been deleted.  This is a soft delete. This means it is still in the repository
     * but it is no longer returned on queries.
     * <p>
     * Details of the TypeDef are included with the relationship's unique id (guid) to ensure the right
     * relationship is deleted in the remote repositories.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param relationship                   deleted relationship
     */
    public void processDeletedRelationshipEvent(String       sourceName,
                                                String       originatorMetadataCollectionId,
                                                String       originatorServerName,
                                                String       originatorServerType,
                                                String       originatorOrganizationName,
                                                Relationship relationship)
    {
        final String methodName = "processDeletedRelationshipEvent";

        updateReferenceRelationship(sourceName,
                                    methodName,
                                    originatorMetadataCollectionId,
                                    originatorServerName,
                                    relationship);
    }


    /**
     * A deleted relationship has been permanently removed from the repository.  This request can not be undone.
     * <p>
     * Details of the TypeDef are included with the relationship's unique id (guid) to ensure the right
     * relationship is purged in the remote repositories.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param typeDefGUID                    unique identifier for this relationship's TypeDef.
     * @param typeDefName                    name of this relationship's TypeDef.
     * @param instanceGUID                   unique identifier for the relationship.
     */
    public void processPurgedRelationshipEvent(String sourceName,
                                               String originatorMetadataCollectionId,
                                               String originatorServerName,
                                               String originatorServerType,
                                               String originatorOrganizationName,
                                               String typeDefGUID,
                                               String typeDefName,
                                               String instanceGUID)
    {
        final String methodName = "processPurgedRelationshipEvent";

        try
        {
            verifyEventProcessor(methodName);

            localMetadataCollection.purgeRelationshipReferenceCopy(localRepositoryConnector.getServerUserId(),
                                                                   instanceGUID,
                                                                   typeDefGUID,
                                                                   typeDefName,
                                                                   originatorMetadataCollectionId);

        }
        catch (Throwable error)
        {
            handleUnexpectedErrorFromEvent(error,
                                           methodName,
                                           originatorServerName,
                                           originatorMetadataCollectionId);
        }
    }


    /**
     * A deleted relationship has been restored to the state it was before it was deleted.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param relationship                   details of the version of the relationship that has been restored.
     */
    public void processRestoredRelationshipEvent(String       sourceName,
                                                 String       originatorMetadataCollectionId,
                                                 String       originatorServerName,
                                                 String       originatorServerType,
                                                 String       originatorOrganizationName,
                                                 Relationship relationship)
    {
        final String methodName = "processRestoredRelationshipEvent";

        updateReferenceRelationship(sourceName,
                                    methodName,
                                    originatorMetadataCollectionId,
                                    originatorServerName,
                                    relationship);
    }


    /**
     * The guid of an existing relationship has changed.  This is used if two different
     * relationships are discovered to have the same guid.  This is extremely unlikely but not impossible so
     * the open metadata protocol has provision for this.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param originalRelationshipGUID       the existing identifier for the relationship.
     * @param relationship                   new values for this relationship, including the new guid.
     */
    public void processReIdentifiedRelationshipEvent(String       sourceName,
                                                     String       originatorMetadataCollectionId,
                                                     String       originatorServerName,
                                                     String       originatorServerType,
                                                     String       originatorOrganizationName,
                                                     String       originalRelationshipGUID,
                                                     Relationship relationship)
    {
        final String methodName = "processReIdentifiedRelationshipEvent";

        updateReferenceRelationship(sourceName,
                                    methodName,
                                    originatorMetadataCollectionId,
                                    originatorServerName,
                                    relationship);
    }


    /**
     * An existing relationship has had its type changed.  Typically this action is taken to move a relationship's
     * type to either a super type (so the subtype can be deleted) or a new subtype (so additional properties can be
     * added.)  However, the type can be changed to any compatible type.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param originalTypeDefSummary         original details of this relationship's TypeDef.
     * @param relationship                   new values for this relationship, including the new type information.
     */
    public void processReTypedRelationshipEvent(String         sourceName,
                                                String         originatorMetadataCollectionId,
                                                String         originatorServerName,
                                                String         originatorServerType,
                                                String         originatorOrganizationName,
                                                TypeDefSummary originalTypeDefSummary,
                                                Relationship   relationship)
    {
        final String methodName = "processReTypedRelationshipEvent";

        updateReferenceRelationship(sourceName,
                                    methodName,
                                    originatorMetadataCollectionId,
                                    originatorServerName,
                                    relationship);
    }


    /**
     * An existing relationship has changed home repository.  This action is taken for example, if a repository
     * becomes permanently unavailable, or if the user community updating this relationship move to working
     * from a different repository in the open metadata repository cluster.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param originalHomeMetadataCollection unique identifier for the original home repository.
     * @param relationship                   new values for this relationship, including the new home information.
     */
    public void processReHomedRelationshipEvent(String       sourceName,
                                                String       originatorMetadataCollectionId,
                                                String       originatorServerName,
                                                String       originatorServerType,
                                                String       originatorOrganizationName,
                                                String       originalHomeMetadataCollection,
                                                Relationship relationship)
    {
        final String methodName = "processReHomedRelationshipEvent";

        updateReferenceRelationship(sourceName,
                                    methodName,
                                    originatorMetadataCollectionId,
                                    originatorServerName,
                                    relationship);
    }


    /**
     * A repository has requested the home repository of a relationship send details of the relationship so
     * the local repository can create a reference copy of the instance.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param typeDefGUID                    unique identifier for this instance's TypeDef
     * @param typeDefName                    name of this relationship's TypeDef
     * @param instanceGUID                   unique identifier for the instance
     * @param homeMetadataCollectionId       metadata collection id for the home of this instance.
     */
    public void processRefreshRelationshipRequest(String sourceName,
                                                  String originatorMetadataCollectionId,
                                                  String originatorServerName,
                                                  String originatorServerType,
                                                  String originatorOrganizationName,
                                                  String typeDefGUID,
                                                  String typeDefName,
                                                  String instanceGUID,
                                                  String homeMetadataCollectionId)
    {
        final String    methodName = "processRefreshRelationshipRequest";

        try
        {
            verifyEventProcessor(methodName);

            if (localMetadataCollectionId.equals(homeMetadataCollectionId))
            {
                Relationship relationship = localMetadataCollection.isRelationshipKnown(localRepositoryConnector.getServerUserId(),
                                                                                        instanceGUID);

                if (relationship != null)
                {
                    outboundRepositoryEventProcessor.processRefreshRelationshipEvent(localRepositoryConnector.getRepositoryName(),
                                                                                     localMetadataCollectionId,
                                                                                     localRepositoryConnector.getServerName(),
                                                                                     localRepositoryConnector.getServerType(),
                                                                                     localRepositoryConnector.getOrganizationName(),
                                                                                     relationship);
                }

            }

        }
        catch (Throwable error)
        {
            handleUnexpectedErrorFromEvent(error,
                                           methodName,
                                           originatorServerName,
                                           originatorMetadataCollectionId);
        }
    }


    /**
     * The local repository is refreshing the information about a relationship for the other
     * repositories in the cohort.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param relationship                   relationship details
     */
    public void processRefreshRelationshipEvent(String       sourceName,
                                                String       originatorMetadataCollectionId,
                                                String       originatorServerName,
                                                String       originatorServerType,
                                                String       originatorOrganizationName,
                                                Relationship relationship)
    {
        final String methodName = "processRefreshRelationshipEvent";

        updateReferenceRelationship(sourceName,
                                    methodName,
                                    originatorMetadataCollectionId,
                                    originatorServerName,
                                    relationship);
    }


    /**
     * An open metadata repository is passing information about a collection of entities and relationships
     * with the other repositories in the cohort.
     *
     * @param sourceName name of the source of the event.  It may be the cohort name for incoming events or the
     *                   local repository, or event mapper name.
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName name of the server that the event came from.
     * @param originatorServerType type of server that the event came from.
     * @param originatorOrganizationName name of the organization that owns the server that sent the event.
     * @param instances multiple entities and relationships for sharing.
     */
    public void processInstanceBatchEvent(String         sourceName,
                                          String         originatorMetadataCollectionId,
                                          String         originatorServerName,
                                          String         originatorServerType,
                                          String         originatorOrganizationName,
                                          InstanceGraph  instances)
    {
        final String methodName = "processInstanceBatchEvent";

        try
        {
            verifyEventProcessor(methodName);

            localMetadataCollection.saveInstanceReferenceCopies(localRepositoryConnector.getServerUserId(),
                                                                instances);
        }
        catch (Throwable error)
        {
            handleUnexpectedErrorFromEvent(error,
                                           methodName,
                                           originatorServerName,
                                           originatorMetadataCollectionId);
        }
    }


    /**
     * An open metadata repository has detected two metadata instances with the same identifier (guid).
     * This is a serious error because it could lead to corruption of the metadata collections within the cohort.
     * When this occurs, all repositories in the cohort delete their reference copies of the metadata instances and
     * at least one of the instances has its GUID changed in its respective home repository.  The updated instance(s)
     * are redistributed around the cohort.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId metadata collection id of the repository reporting the conflicting instance
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param targetMetadataCollectionId     metadata collection id of other repository with the conflicting instance
     * @param targetTypeDefSummary           details of the target instance's TypeDef
     * @param targetInstanceGUID             unique identifier for the source instance
     * @param otherOrigin                    origin of the other (older) metadata instance
     * @param otherMetadataCollectionId      metadata collection of the other (older) metadata instance
     * @param otherTypeDefSummary            details of the other (older) instance's TypeDef
     * @param otherInstanceGUID              unique identifier for the other (older) instance
     * @param errorMessage                   description of the error.
     */
    public void processConflictingInstancesEvent(String                 sourceName,
                                                 String                 originatorMetadataCollectionId,
                                                 String                 originatorServerName,
                                                 String                 originatorServerType,
                                                 String                 originatorOrganizationName,
                                                 String                 targetMetadataCollectionId,
                                                 TypeDefSummary         targetTypeDefSummary,
                                                 String                 targetInstanceGUID,
                                                 String                 otherMetadataCollectionId,
                                                 InstanceProvenanceType otherOrigin,
                                                 TypeDefSummary         otherTypeDefSummary,
                                                 String                 otherInstanceGUID,
                                                 String                 errorMessage)
    {
        final String methodName = "processConflictingInstancesEvent";

        OMRSAuditCode auditCode = OMRSAuditCode.DUPLICATE_INSTANCES_FOR_GUID;
        auditLog.logRecord(methodName,
                           auditCode.getLogMessageId(),
                           auditCode.getSeverity(),
                           auditCode.getFormattedLogMessage(originatorServerName,
                                                            originatorMetadataCollectionId,
                                                            otherInstanceGUID,
                                                            otherMetadataCollectionId,
                                                            otherTypeDefSummary.toString(),
                                                            otherOrigin.getName(),
                                                            targetMetadataCollectionId,
                                                            targetTypeDefSummary.toString(),
                                                            errorMessage),
                           sourceName,
                           auditCode.getSystemAction(),
                           auditCode.getUserAction());

        /*
         * This repository has been targeted to act.
         */
        if (localMetadataCollectionId.equals(targetMetadataCollectionId))
        {
            /*
             * Attempt to update the guid of the instance
             */
            try
            {
                String  newGUID = UUID.randomUUID().toString();

                if (targetTypeDefSummary.getCategory() == TypeDefCategory.ENTITY_DEF)
                {
                    localMetadataCollection.reIdentifyEntity(localRepositoryConnector.getServerUserId(),
                                                             targetTypeDefSummary.getGUID(),
                                                             targetTypeDefSummary.getName(),
                                                             targetInstanceGUID,
                                                             newGUID);
                }
                else
                {
                    localMetadataCollection.reIdentifyRelationship(localRepositoryConnector.getServerUserId(),
                                                                   targetTypeDefSummary.getGUID(),
                                                                   targetTypeDefSummary.getName(),
                                                                   targetInstanceGUID,
                                                                   newGUID);
                }

                auditCode = OMRSAuditCode.INSTANCE_SUCCESSFULLY_REIDENTIFIED;
                auditLog.logRecord(methodName,
                                   auditCode.getLogMessageId(),
                                   auditCode.getSeverity(),
                                   auditCode.getFormattedLogMessage(localServerName,
                                                                    localMetadataCollectionId,
                                                                    otherTypeDefSummary.getName(),
                                                                    targetInstanceGUID,
                                                                    newGUID),
                                   null,
                                   auditCode.getSystemAction(),
                                   auditCode.getUserAction());
            }
            catch (Throwable error)
            {
                auditCode = OMRSAuditCode.UNABLE_TO_RE_IDENTIFY_INSTANCE;
                auditLog.logRecord(methodName,
                                   auditCode.getLogMessageId(),
                                   auditCode.getSeverity(),
                                   auditCode.getFormattedLogMessage(localServerName,
                                                                    localMetadataCollectionId,
                                                                    otherTypeDefSummary.getName(),
                                                                    targetInstanceGUID,
                                                                    error.getClass().getName(),
                                                                    error.getMessage()),
                                   null,
                                   auditCode.getSystemAction(),
                                   auditCode.getUserAction());
            }
        }
        else
        {
            /*
             * Remove reference copy if stored
             */
            removeReferenceCopyOfInstance(targetTypeDefSummary,
                                          targetInstanceGUID,
                                          targetMetadataCollectionId,
                                          methodName);
        }
    }


    /**
     * An open metadata repository has detected an inconsistency in the version of the type used in an updated metadata
     * instance compared to its stored version.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param originatorMetadataCollectionId metadata collection id of the repository reporting the conflicting instance
     * @param originatorServerName           name of the server that the event came from.
     * @param originatorServerType           type of server that the event came from.
     * @param originatorOrganizationName     name of the organization that owns the server that sent the event.
     * @param targetMetadataCollectionId     metadata collection id of other repository with the conflicting instance
     * @param targetTypeDefSummary           details of the target instance's TypeDef
     * @param targetInstanceGUID             unique identifier for the source instance
     * @param otherTypeDefSummary            details of the local copy of the instance's TypeDef
     * @param errorMessage                   description of the error.
     */
    public void processConflictingTypeEvent(String         sourceName,
                                            String         originatorMetadataCollectionId,
                                            String         originatorServerName,
                                            String         originatorServerType,
                                            String         originatorOrganizationName,
                                            String         targetMetadataCollectionId,
                                            TypeDefSummary targetTypeDefSummary,
                                            String         targetInstanceGUID,
                                            TypeDefSummary otherTypeDefSummary,
                                            String         errorMessage)
    {
        final String methodName = "processConflictingTypeEvent";

        if (localMetadataCollectionId.equals(targetMetadataCollectionId))
        {
            OMRSAuditCode auditCode = OMRSAuditCode.LOCAL_INSTANCE_WITH_CONFLICTING_TYPES;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(originatorServerName,
                                                                originatorMetadataCollectionId,
                                                                targetInstanceGUID,
                                                                otherTypeDefSummary.toString(),
                                                                targetMetadataCollectionId,
                                                                targetTypeDefSummary.toString(),
                                                                errorMessage),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }
        else
        {
            OMRSAuditCode auditCode = OMRSAuditCode.INSTANCES_WITH_CONFLICTING_TYPES;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(originatorServerName,
                                                                originatorMetadataCollectionId,
                                                                targetInstanceGUID,
                                                                otherTypeDefSummary.toString(),
                                                                targetMetadataCollectionId,
                                                                targetTypeDefSummary.toString(),
                                                                errorMessage),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());

            removeReferenceCopyOfInstance(targetTypeDefSummary,
                                          targetInstanceGUID,
                                          targetMetadataCollectionId,
                                          methodName);
        }
    }


    /*
     * =======================
     * OMRSInstanceRetrievalEventProcessor
     */


    /**
     * Pass an entity that has been retrieved from a remote open metadata repository so it can be validated and
     * (if the rules permit) cached in the local repository.
     *
     * @param sourceName name of the source of this event.
     * @param metadataCollectionId unique identifier for the metadata from the remote repository
     * @param processedEntityGUID the retrieved entity's GUID.
     * @param processedEntityType the retrieved entity's Type.
     */
    private void refreshRetrievedEntity(String        sourceName,
                                        String        metadataCollectionId,
                                        String        processedEntityGUID,
                                        InstanceType  processedEntityType)
    {
        try
        {
            if (localMetadataCollection.isEntityKnown(localRepositoryConnector.getServerUserId(),
                                                      processedEntityGUID) == null)
            {
                if (processedEntityType != null)
                {
                    /*
                     * It would be possible to save the relationship directly into the repository,
                     * but it is possible that some of the properties have been suppressed for the
                     * requesting user Id.  In which case saving it now would result in other users
                     * seeing a restricted view of the relationship.
                     */
                    localMetadataCollection.refreshEntityReferenceCopy(localRepositoryConnector.getServerUserId(),
                                                                       processedEntityGUID,
                                                                       processedEntityType.getTypeDefGUID(),
                                                                       processedEntityType.getTypeDefName(),
                                                                       metadataCollectionId);
                }
            }
        }
        catch (Throwable   error)
        {
            final String methodName = "processRetrievedEntity";

            handleUnexpectedErrorFromEvent(error,
                                           methodName,
                                           sourceName,
                                           metadataCollectionId);
        }
    }


    /**
     * Pass an entity that has been retrieved from a remote open metadata repository so it can be validated and
     * (if the rules permit) cached in the local repository.
     *
     * @param sourceName name of the source of this event.
     * @param metadataCollectionId unique identifier for the metadata from the remote repository
     * @param processedEntity  the retrieved entity.
     */
    public void processRetrievedEntitySummary(String        sourceName,
                                              String        metadataCollectionId,
                                              EntitySummary processedEntity)
    {
        /*
         * Discover whether the instance should be learned.
         */
        if (verifyEventToLearn(sourceName, processedEntity))
        {
            refreshRetrievedEntity(sourceName,
                                   metadataCollectionId,
                                   processedEntity.getGUID(),
                                   processedEntity.getType());
        }
    }


    /**
     * Pass an entity that has been retrieved from a remote open metadata repository so it can be validated and
     * (if the rules permit) cached in the local repository.
     *
     * @param sourceName name of the source of this event.
     * @param metadataCollectionId unique identifier for the metadata from the remote repository
     * @param processedEntity the retrieved entity.
     */
    public void processRetrievedEntityDetail(String       sourceName,
                                             String       metadataCollectionId,
                                             EntityDetail processedEntity)
    {
        /*
         * Discover whether the instance should be learned.
         */
        if (verifyEventToLearn(sourceName, processedEntity))
        {
            refreshRetrievedEntity(sourceName,
                                   metadataCollectionId,
                                   processedEntity.getGUID(),
                                   processedEntity.getType());
        }
    }


    /**
     * Pass a relationship that has been retrieved from a remote open metadata repository so it can be validated and
     * (if the rules permit) cached in the local repository.
     *
     * @param sourceName name of the source of this event.
     * @param metadataCollectionId unique identifier for the metadata from the remote repository
     * @param processedRelationship         the retrieved relationship
     */
    public void processRetrievedRelationship(String       sourceName,
                                             String       metadataCollectionId,
                                             Relationship processedRelationship)
    {
        /*
         * Discover whether the instance should be learned.
         */
        if (verifyEventToLearn(sourceName, processedRelationship))
        {
            try
            {
                if (localMetadataCollection.isRelationshipKnown(localRepositoryConnector.getServerUserId(),
                                                                processedRelationship.getGUID()) == null)
                {
                    InstanceType type = processedRelationship.getType();

                    if (type != null)
                    {
                        /*
                         * It would be possible to save the relationship directly into the repository,
                         * but it is possible that some of the properties have been suppressed for the
                         * requesting user Id.  In which case saving it now would result in other users
                         * seeing a restricted view of the relationship.
                         */
                        localMetadataCollection.refreshRelationshipReferenceCopy(localRepositoryConnector.getServerUserId(),
                                                                                 processedRelationship.getGUID(),
                                                                                 type.getTypeDefGUID(),
                                                                                 type.getTypeDefName(),
                                                                                 metadataCollectionId);
                    }
                }
            }
            catch (Throwable   error)
            {
                final String methodName = "processRetrievedRelationship";

                handleUnexpectedErrorFromEvent(error,
                                               methodName,
                                               sourceName,
                                               metadataCollectionId);
            }
        }
    }


    /*
     * ==============================
     * Private methods
     * ==============================
     */


    /**
     * Remove the local reference copy of an instance found to be in error.
     *
     * @param typeDefSummary description of the instance's type
     * @param instanceGUID unique identifier of type
     * @param homeMetadataCollectionId home of the instance
     * @param methodName calling method
     */
    private void removeReferenceCopyOfInstance(TypeDefSummary    typeDefSummary,
                                               String            instanceGUID,
                                               String            homeMetadataCollectionId,
                                               String            methodName)
    {
        try
        {
            if (typeDefSummary.getCategory() == TypeDefCategory.ENTITY_DEF)
            {

                localMetadataCollection.purgeEntityReferenceCopy(localRepositoryConnector.getServerUserId(),
                                                                 instanceGUID,
                                                                 typeDefSummary.getGUID(),
                                                                 typeDefSummary.getName(),
                                                                 homeMetadataCollectionId);
            }
            else
            {
                localMetadataCollection.purgeEntityReferenceCopy(localRepositoryConnector.getServerUserId(),
                                                                 instanceGUID,
                                                                 typeDefSummary.getGUID(),
                                                                 typeDefSummary.getName(),
                                                                 homeMetadataCollectionId);
            }
        }
        catch (Throwable error)
        {
            OMRSAuditCode auditCode = OMRSAuditCode.UNABLE_TO_REMOVE_REFERENCE_COPY;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(localServerName,
                                                                localMetadataCollectionId,
                                                                typeDefSummary.getName(),
                                                                instanceGUID,
                                                                error.getClass().getName(),
                                                                error.getMessage()),
                               error.toString(),
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }
    }

    /**
     * Update the reference entity in the local repository if all checks permit.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param methodName                     name of the event method
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param entity                         details of the new entity
     */
    private void updateReferenceEntity(String       sourceName,
                                       String       methodName,
                                       String       originatorMetadataCollectionId,
                                       String       originatorServerName,
                                       EntityDetail entity)
    {
        try
        {
            final String entityParameterName = "entity";

            verifyEventProcessor(methodName);
            repositoryValidator.validateReferenceInstanceHeader(sourceName,
                                                                localMetadataCollectionId,
                                                                entityParameterName,
                                                                entity,
                                                                methodName);

            EntityDetail storedEntity = localMetadataCollection.isEntityKnown(localRepositoryConnector.getServerUserId(),
                                                                              entity.getGUID());

            /*
             * Verify that the incoming instance is compatible with the stored instance.
             */
            if (compareAndValidateReferenceInstance(originatorServerName,
                                                    entity,
                                                    storedEntity,
                                                    methodName))
            {
                /*
                 * Verify that the rules allow the entity to be saved
                 */
                if (verifyEventToSave(sourceName, entity))
                {
                    localMetadataCollection.saveEntityReferenceCopy(localRepositoryConnector.getServerUserId(), entity);
                }
            }
        }
        catch (Throwable error)
        {
            handleUnexpectedErrorFromEvent(error,
                                           methodName,
                                           originatorServerName,
                                           originatorMetadataCollectionId);
        }
    }


    /**
     * The incoming instance is properly formed.  Is it compatible (and a newer version) than the instance
     * that is stored?
     *
     * @param originatorServerName sender of the event being processed
     * @param incomingInstance this is the instance received
     * @param storedInstance this is the instance in the repository
     * @param methodName calling method
     * @return boolean indicating that it is ok to store the incoming instance
     */
    private boolean compareAndValidateReferenceInstance(String          originatorServerName,
                                                        InstanceHeader  incomingInstance,
                                                        InstanceHeader  storedInstance,
                                                        String          methodName)
    {
        if (storedInstance == null)
        {
            return true;
        }
        else
        {
            InstanceType storedInstanceType   = storedInstance.getType();
            InstanceType incomingInstanceType = incomingInstance.getType();

            if ((storedInstanceType != null) && (incomingInstanceType != null))
            {
                /*
                 * It is extremely unlikely but this may be a situation where there are two instances with the same guid.
                 * This situation is detected by differences in the fixed values in the instance header.
                 * Since the protocol allows some adjustment to the guid (reIdentify), the type (reType) and
                 * the home metadataCollectionId (reHome) we use the creation time to validate that this is the same instance.
                 */
                if (incomingInstance.getCreateTime().equals(storedInstance.getCreateTime()))
                {
                    /*
                     * It is probably the same instance.  Is the incoming value a later version? If it isn't, it means
                     * the events have been received in a mixed up order - not an error - just need to ignore the incoming
                     * instance.
                     */
                    if (storedInstance.getVersion() < incomingInstance.getVersion())
                    {
                        /*
                         * The final check is to ensure that the type version has not been regressed.
                         */
                        if (storedInstanceType.getTypeDefVersion() <= incomingInstanceType.getTypeDefVersion())
                        {
                            /*
                             * All checks are complete - the instance can be saved.  There are two additional actions
                             * to output audit log messages if there are differences in the home metadata collection id
                             * and type information.  This are valid if reTyping and reHoming have occurred - so the
                             * audit log message enables operator to check all is ok.
                             */

                            if (!incomingInstance.getMetadataCollectionId().equals(
                                    storedInstance.getMetadataCollectionId()))
                            {
                                OMRSAuditCode auditCode = OMRSAuditCode.NEW_HOME_INFORMATION;
                                auditLog.logRecord(methodName,
                                                   auditCode.getLogMessageId(),
                                                   auditCode.getSeverity(),
                                                   auditCode.getFormattedLogMessage(
                                                           Long.toString(incomingInstance.getVersion()),
                                                           incomingInstance.getGUID(),
                                                           originatorServerName,
                                                           incomingInstance.getMetadataCollectionId(),
                                                           storedInstance.getMetadataCollectionId()),
                                                   incomingInstance.toString(),
                                                   auditCode.getSystemAction(),
                                                   auditCode.getUserAction());
                            }

                            if (!incomingInstanceType.getTypeDefName().equals(storedInstanceType.getTypeDefName()))
                            {
                                OMRSAuditCode auditCode = OMRSAuditCode.NEW_TYPE_INFORMATION;
                                auditLog.logRecord(methodName,
                                                   auditCode.getLogMessageId(),
                                                   auditCode.getSeverity(),
                                                   auditCode.getFormattedLogMessage(
                                                           Long.toString(incomingInstance.getVersion()),
                                                           incomingInstance.getGUID(),
                                                           originatorServerName,
                                                           incomingInstance.getMetadataCollectionId(),
                                                           incomingInstanceType.getTypeDefName(),
                                                           incomingInstanceType.getTypeDefGUID(),
                                                           storedInstanceType.getTypeDefName(),
                                                           storedInstanceType.getTypeDefGUID()),
                                                   incomingInstance.toString(),
                                                   auditCode.getSystemAction(),
                                                   auditCode.getUserAction());
                            }

                            return true;
                        }
                        else
                        {
                            /*
                             * For some reason the later version of the instance is using a older version of the type.
                             * Something is not right.  There is an architected exchange for this case because types can
                             * change and it probably means the originator of the event (the home of the instance)
                             * has had its types regressed.
                             */
                            try
                            {
                                OMRSAuditCode auditCode = OMRSAuditCode.PROCESS_INSTANCE_TYPE_CONFLICT;
                                String errorMessage = auditCode.getFormattedLogMessage(storedInstance.getGUID(),
                                                                                       originatorServerName,
                                                                                       storedInstance.getMetadataCollectionId(),
                                                                                       storedInstanceType.getTypeDefName(),
                                                                                       storedInstanceType.toString(),
                                                                                       incomingInstanceType.toString());
                                outboundRepositoryEventProcessor.processConflictingTypeEvent(localServerName,
                                                                                             localMetadataCollectionId,
                                                                                             localRepositoryConnector.getServerName(),
                                                                                             localRepositoryConnector.getServerType(),
                                                                                             localRepositoryConnector.getOrganizationName(),
                                                                                             incomingInstance.getMetadataCollectionId(),
                                                                                             repositoryHelper.getTypeDef(
                                                                                                     localServerName,
                                                                                                     "incomingInstanceType",
                                                                                                     incomingInstanceType.getTypeDefGUID(),
                                                                                                     methodName),
                                                                                             incomingInstance.getGUID(),
                                                                                             repositoryHelper.getTypeDef(
                                                                                                     localServerName,
                                                                                                     "storedInstanceType",
                                                                                                     storedInstanceType.getTypeDefGUID(),
                                                                                                     methodName),
                                                                                             errorMessage);
                            }
                            catch (Throwable error)
                            {
                                /*
                                 * Don't ever expect to be here - it probably means that the getTypeDef method
                                 * threw an exception because the type name is not known - but then how did we get
                                 * this far if that were the case!
                                 */
                                final String localMethodName = "compareAndValidateReferenceInstance";

                                handleUnexpectedErrorFromEvent(error,
                                                               localMethodName,
                                                               originatorServerName,
                                                               incomingInstance.getMetadataCollectionId());
                            }
                        }
                    }
                }
                else
                {
                    try
                    {
                        OMRSAuditCode auditCode = OMRSAuditCode.PROCESS_INSTANCE_GUID_CONFLICT;
                        String errorMessage = auditCode.getFormattedLogMessage(incomingInstanceType.getTypeDefName(),
                                                                               incomingInstanceType.getTypeDefName(),
                                                                               originatorServerName,
                                                                               incomingInstance.getMetadataCollectionId(),
                                                                               storedInstance.getGUID(),
                                                                               storedInstanceType.getTypeDefName(),
                                                                               storedInstanceType.getTypeDefGUID());
                        outboundRepositoryEventProcessor.processConflictingInstancesEvent(localServerName,
                                                                                          localMetadataCollectionId,
                                                                                          localRepositoryConnector.getServerName(),
                                                                                          localRepositoryConnector.getServerType(),
                                                                                          localRepositoryConnector.getOrganizationName(),
                                                                                          incomingInstance.getMetadataCollectionId(),
                                                                                          repositoryHelper.getTypeDef(
                                                                                                  localServerName,
                                                                                                  "incomingInstanceType",
                                                                                                  incomingInstanceType.getTypeDefGUID(),
                                                                                                  methodName),
                                                                                          incomingInstance.getGUID(),
                                                                                          localMetadataCollectionId,
                                                                                          storedInstance.getInstanceProvenanceType(),
                                                                                          repositoryHelper.getTypeDef(
                                                                                                  localServerName,
                                                                                                  "storedInstanceType",
                                                                                                  storedInstanceType.getTypeDefGUID(),
                                                                                                  methodName),
                                                                                          storedInstance.getGUID(),
                                                                                          errorMessage);
                    }
                    catch (Throwable error)
                    {
                        /*
                         * Don't ever expect to be here - it probably means that the getTypeDef method
                         * threw an exception because the type name is not known - but then how did we get
                         * this far if that were the case!
                         */
                        final String localMethodName = "compareAndValidateReferenceInstance";

                        handleUnexpectedErrorFromEvent(error,
                                                       localMethodName,
                                                       originatorServerName,
                                                       incomingInstance.getMetadataCollectionId());
                    }
                }
            }
        }

        return false;
    }


    /**
     * Update the reference relationship in the local repository.
     *
     * @param sourceName                     name of the source of the event.  It may be the cohort name for incoming events or the
     *                                       local repository, or event mapper name.
     * @param methodName                     name of the event method
     * @param originatorMetadataCollectionId unique identifier for the metadata collection hosted by the server that
     *                                       sent the event.
     * @param originatorServerName           name of the server that the event came from.
     * @param relationship                   details of the relationship
     */
    private void updateReferenceRelationship(String       sourceName,
                                             String       methodName,
                                             String       originatorMetadataCollectionId,
                                             String       originatorServerName,
                                             Relationship relationship)
    {
        try
        {
            final String     relationshipParameterName = "relationship";

            verifyEventProcessor(methodName);
            repositoryValidator.validateReferenceInstanceHeader(sourceName,
                                                                localMetadataCollectionId,
                                                                relationshipParameterName,
                                                                relationship,
                                                                methodName);

            if (verifyEventToSave(sourceName, relationship))
            {
                localMetadataCollection.saveRelationshipReferenceCopy(localRepositoryConnector.getServerUserId(),
                                                                      relationship);
            }
        }
        catch (Throwable error)
        {
            handleUnexpectedErrorFromEvent(error,
                                           methodName,
                                           originatorServerName,
                                           originatorMetadataCollectionId);
        }
    }


    /**
     * Validate that this event processor is correctly initialized.
     *
     * @param methodName name of the method being called
     */
    private void verifyEventProcessor(String    methodName)
    {
        if (localMetadataCollectionId == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_LOCAL_METADATA_COLLECTION;
            String        errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage();

            throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }

        if (localRepositoryConnector == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NO_LOCAL_REPOSITORY;
            String        errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage();

            throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }

        if (repositoryHelper ==null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_REPOSITORY_HELPER;
            String        errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage();

            throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }

        if (repositoryValidator == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_REPOSITORY_VALIDATOR;
            String        errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage();

            throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }

        if (saveExchangeRule == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_EXCHANGE_RULE;
            String        errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName);

            throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }

        if (localMetadataCollection == null)
        {
            OMRSErrorCode errorCode = OMRSErrorCode.NULL_METADATA_COLLECTION;
            String        errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage();

            throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }
    }


    /**
     * Log the fact that there has been an unexpected error when processing an incoming OMRS Event.
     *
     * @param error unexpected exception
     * @param methodName calling method
     * @param originatorServerName originator server name
     * @param originatorMetadataCollectionId originators metadata collection id
     */
    private void handleUnexpectedErrorFromEvent(Throwable  error,
                                                String     methodName,
                                                String     originatorServerName,
                                                String     originatorMetadataCollectionId)
    {
        OMRSAuditCode auditCode = OMRSAuditCode.UNEXPECTED_EXCEPTION_FROM_EVENT;
        auditLog.logRecord(methodName,
                           auditCode.getLogMessageId(),
                           auditCode.getSeverity(),
                           auditCode.getFormattedLogMessage(methodName,
                                                            originatorServerName,
                                                            originatorMetadataCollectionId,
                                                            error.getMessage()),
                           error.toString(),
                           auditCode.getSystemAction(),
                           auditCode.getUserAction());
    }

    /**
     * Determine if the event should be processed.
     *
     * @param source identifier of the source of the event.
     * @param instance metadata instance in the event.
     * @return boolean flag indicating whether the event should be sent to the real repository or not.
     */
    private boolean verifyEventToSave(String             source,
                                      InstanceHeader     instance)
    {
        InstanceType   instanceType = instance.getType();

        return ((saveExchangeRule.processInstanceEvent(instance)) &&
                (repositoryValidator.isActiveType(source,
                                                  instanceType.getTypeDefGUID(),
                                                  instanceType.getTypeDefName())));
    }

    /**
     * Determine if the event should be processed.
     *
     * @param source identifier of the source of the event.
     * @param instance metadata instance in the event.
     * @return boolean flag indicating whether the event should be sent to the real repository or not.
     */
    private boolean verifyEventToLearn(String             source,
                                       InstanceHeader     instance)
    {
        InstanceType   instanceType = instance.getType();

        return ((saveExchangeRule.learnInstanceEvent(instance)) &&
                (repositoryValidator.isActiveType(source,
                                                  instanceType.getTypeDefGUID(),
                                                  instanceType.getTypeDefName())));
    }
}
