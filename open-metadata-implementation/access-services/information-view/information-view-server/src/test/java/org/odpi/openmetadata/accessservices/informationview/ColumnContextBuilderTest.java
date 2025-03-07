/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.informationview;


import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.odpi.openmetadata.accessservices.informationview.context.ColumnContextBuilder;
import org.odpi.openmetadata.accessservices.informationview.events.TableContextEvent;
import org.odpi.openmetadata.accessservices.informationview.utils.Constants;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.OMRSMetadataCollection;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.EntityDetail;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.InstanceProperties;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.typedefs.TypeDef;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryConnector;
import org.odpi.openmetadata.repositoryservices.localrepository.repositorycontentmanager.OMRSRepositoryContentHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.odpi.openmetadata.accessservices.informationview.TestDataHelper.*;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ColumnContextBuilderTest {

    @Mock
    private OMRSRepositoryConnector omrsRepositoryConnector;
    @Mock
    private OMRSMetadataCollection omrsMetadataCollection;
    @Mock
    private OMRSRepositoryContentHelper omrsRepositoryHelper;

    private ColumnContextBuilder builder;

    private TestDataHelper helper;

    @BeforeMethod
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        helper = new TestDataHelper();
        when(omrsRepositoryConnector.getMetadataCollection()).thenReturn(omrsMetadataCollection);
        when(omrsRepositoryConnector.getRepositoryHelper()).thenReturn(omrsRepositoryHelper);
        when(omrsRepositoryHelper.getStringProperty(eq(Constants.INFORMATION_VIEW_OMAS_NAME),
                                                    any(String.class),
                                                    any(InstanceProperties.class),
                                                    any(String.class))).thenCallRealMethod();
        when(omrsRepositoryHelper.getBooleanProperty(eq(Constants.INFORMATION_VIEW_OMAS_NAME),
                                                    any(String.class),
                                                    any(InstanceProperties.class),
                                                    any(String.class))).thenCallRealMethod();
        when(omrsRepositoryHelper.getIntProperty(eq(Constants.INFORMATION_VIEW_OMAS_NAME),
                                                    any(String.class),
                                                    any(InstanceProperties.class),
                                                    any(String.class))).thenCallRealMethod();
        when(omrsRepositoryHelper.getStringArrayProperty(eq(Constants.INFORMATION_VIEW_OMAS_NAME),
                                                    any(String.class),
                                                    any(InstanceProperties.class),
                                                    any(String.class))).thenCallRealMethod();
        buildEntitiesAndRelationships();
        buildRelationshipsTypes();
        builder = new ColumnContextBuilder(omrsRepositoryConnector);
    }

    private void buildEntitiesAndRelationships() throws Exception {
        EntityDetail columnEntityDetail = helper.createColumnEntity();
        EntityDetail columnEntityTypeEntity = helper.createColumnTypeEntity();
        EntityDetail tableEntityDetail = helper.createTableEntity();
        EntityDetail tableTypeEntityDetail = helper.createTableTypeEntity();
        EntityDetail dbSchemaTypeEntityDetail = helper.createRelationalDBSchemaTypeEntity();
        EntityDetail deployedDatabaseSchemaEntityDetail = helper.createDeployedDatabaseSchemaEntity();
        EntityDetail databaseEntityDetail = helper.createDatabaseEntity();
        EntityDetail endpointEntityDetail = helper.createEndpointEntity();
        EntityDetail connectionEntity = helper.createConnectionEntity();
        EntityDetail connectorTypeEntity = helper.createConnectorTypeEntity();


        when(omrsMetadataCollection.getEntityDetail(eq(Constants.INFORMATION_VIEW_USER_ID), eq(columnEntityDetail.getGUID()))).thenReturn(columnEntityDetail);
        when(omrsMetadataCollection.getEntityDetail(eq(Constants.INFORMATION_VIEW_USER_ID), eq(columnEntityTypeEntity.getGUID()))).thenReturn(columnEntityTypeEntity);
        when(omrsMetadataCollection.getEntityDetail(eq(Constants.INFORMATION_VIEW_USER_ID), eq(tableEntityDetail.getGUID()))).thenReturn(tableEntityDetail);
        when(omrsMetadataCollection.getEntityDetail(eq(Constants.INFORMATION_VIEW_USER_ID), eq(tableTypeEntityDetail.getGUID()))).thenReturn(tableTypeEntityDetail);
        when(omrsMetadataCollection.getEntityDetail(eq(Constants.INFORMATION_VIEW_USER_ID), eq(dbSchemaTypeEntityDetail.getGUID()))).thenReturn(dbSchemaTypeEntityDetail);
        when(omrsMetadataCollection.getEntityDetail(eq(Constants.INFORMATION_VIEW_USER_ID), eq(deployedDatabaseSchemaEntityDetail.getGUID()))).thenReturn(deployedDatabaseSchemaEntityDetail);
        when(omrsMetadataCollection.getEntityDetail(eq(Constants.INFORMATION_VIEW_USER_ID), eq(databaseEntityDetail.getGUID()))).thenReturn(databaseEntityDetail);
        when(omrsMetadataCollection.getEntityDetail(eq(Constants.INFORMATION_VIEW_USER_ID), eq(endpointEntityDetail.getGUID()))).thenReturn(endpointEntityDetail);
        when(omrsMetadataCollection.getEntityDetail(eq(Constants.INFORMATION_VIEW_USER_ID), eq(connectionEntity.getGUID()))).thenReturn(connectionEntity);
        when(omrsMetadataCollection.getEntityDetail(eq(Constants.INFORMATION_VIEW_USER_ID), eq(connectorTypeEntity.getGUID()))).thenReturn(connectorTypeEntity);
        when(omrsMetadataCollection.getRelationshipsForEntity(eq(Constants.INFORMATION_VIEW_USER_ID), or(eq(columnEntityDetail.getGUID()),eq(tableTypeEntityDetail.getGUID())), eq(ATTRIBUTE_FOR_SCHEMA_REL_TYPE_GUID), eq(0), eq(null), eq(null), eq(null), eq(null), any(Integer.class))).thenReturn(Collections.singletonList(helper.createRelationshipToParentSchemaType(columnEntityDetail.getGUID(), tableTypeEntityDetail.getGUID())));
        when(omrsMetadataCollection.getRelationshipsForEntity(eq(Constants.INFORMATION_VIEW_USER_ID), or(eq(columnEntityDetail.getGUID()), eq(columnEntityTypeEntity.getGUID())), eq(SCHEMA_ATTRIBUTE_TYPE_REL_TYPE_GUID), eq(0), eq(null), eq(null), eq(null), eq(null), any(Integer.class))).thenReturn(Collections.singletonList(helper.createRelationshipToSchemaType(columnEntityDetail.getGUID(), columnEntityTypeEntity.getGUID())));
        when(omrsMetadataCollection.getRelationshipsForEntity(eq(Constants.INFORMATION_VIEW_USER_ID), or(eq(tableEntityDetail.getGUID()), eq(dbSchemaTypeEntityDetail.getGUID())), eq(ATTRIBUTE_FOR_SCHEMA_REL_TYPE_GUID), eq(0), eq(null), eq(null), eq(null), eq(null), any(Integer.class))).thenReturn(Collections.singletonList(helper.createRelationshipToParentSchemaType(tableEntityDetail.getGUID(), dbSchemaTypeEntityDetail.getGUID())));
        when(omrsMetadataCollection.getRelationshipsForEntity(eq(Constants.INFORMATION_VIEW_USER_ID), or(eq(tableEntityDetail.getGUID()), eq(tableTypeEntityDetail.getGUID())), eq(SCHEMA_ATTRIBUTE_TYPE_REL_TYPE_GUID), eq(0), eq(null), eq(null), eq(null), eq(null), any(Integer.class))).thenReturn(Collections.singletonList(helper.createRelationshipToSchemaType(tableEntityDetail.getGUID(), tableTypeEntityDetail.getGUID())));
        when(omrsMetadataCollection.getRelationshipsForEntity(eq(Constants.INFORMATION_VIEW_USER_ID), or(eq(dbSchemaTypeEntityDetail.getGUID()), eq(deployedDatabaseSchemaEntityDetail.getGUID())), eq(ASSET_SCHEMA_REL_TYPE_GUID), eq(0), eq(null), eq(null), eq(null), eq(null), any(Integer.class))).thenReturn(Collections.singletonList(helper.createRelationshipAssetSchemaType(GUID_DB_SCHEMA_TYPE, GUID_DEPLOYED_DATABASE_SCHEMA)));
        when(omrsMetadataCollection.getRelationshipsForEntity(eq(Constants.INFORMATION_VIEW_USER_ID), or(eq(deployedDatabaseSchemaEntityDetail.getGUID()),eq(databaseEntityDetail.getGUID())), eq(DATA_CONTENT_DATASET_REL_TYPE_GUID), eq(0), eq(null), eq(null), eq(null), eq(null), any(Integer.class))).thenReturn(Collections.singletonList(helper.createRelationshipDataContentForDataSet(GUID_DEPLOYED_DATABASE_SCHEMA, GUID_DATABASE)));
        when(omrsMetadataCollection.getRelationshipsForEntity(eq(Constants.INFORMATION_VIEW_USER_ID), or(eq(databaseEntityDetail.getGUID()), eq(connectionEntity.getGUID())), eq(CONNECTION_ASSET_REL_TYPE_GUID), eq(0), eq(null), eq(null), eq(null), eq(null), any(Integer.class))).thenReturn(Collections.singletonList(helper.createRelationshipConnectionToAsset(GUID_DATABASE, GUID_CONNECTION)));
        when(omrsMetadataCollection.getRelationshipsForEntity(eq(Constants.INFORMATION_VIEW_USER_ID), eq(endpointEntityDetail.getGUID()), any(String.class), eq(0), eq(null), eq(null), eq(null), eq(null), any(Integer.class))).thenReturn(new ArrayList<>());
        when(omrsMetadataCollection.getRelationshipsForEntity(eq(Constants.INFORMATION_VIEW_USER_ID), or(eq(connectionEntity.getGUID()), eq(deployedDatabaseSchemaEntityDetail.getGUID())), eq(CONNECTION_ASSET_REL_TYPE_GUID), eq(0), eq(null), eq(null), eq(null), eq(null), any(Integer.class))).thenReturn(Collections.singletonList(helper.createRelationship(Constants.CONNECTION_TO_ASSET, GUID_CONNECTION, GUID_DEPLOYED_DATABASE_SCHEMA)));
        when(omrsMetadataCollection.getRelationshipsForEntity(eq(Constants.INFORMATION_VIEW_USER_ID), or(eq(connectionEntity.getGUID()), eq(endpointEntityDetail.getGUID())), eq(CONNECTION_ENDPOINT_REL_TYPE_GUID), eq(0), eq(null), eq(null), eq(null), eq(null), any(Integer.class))).thenReturn(Collections.singletonList(helper.createRelationship(Constants.CONNECTION_TO_ENDPOINT, GUID_ENDPOINT, GUID_CONNECTION)));
        when(omrsMetadataCollection.getRelationshipsForEntity(eq(Constants.INFORMATION_VIEW_USER_ID), or(eq(connectionEntity.getGUID()), eq(connectorTypeEntity.getGUID())), eq(CONNECTION_CONNECTOR_REL_TYPE_GUID), eq(0), eq(null), eq(null), eq(null), eq(null), any(Integer.class))).thenReturn(Collections.singletonList(helper.createRelationship(Constants.CONNECTION_CONNECTOR_TYPE, GUID_CONNECTION, GUID_CONNECTOR_TYPE)));
    }


    private void buildRelationshipsTypes()  {

        TypeDef typeDef = helper.buildRelationshipType(Constants.CONNECTION_TO_ENDPOINT, CONNECTION_ENDPOINT_REL_TYPE_GUID);
        when(omrsRepositoryHelper.getTypeDefByName(Constants.INFORMATION_VIEW_USER_ID, typeDef.getName())).thenReturn(typeDef);
        typeDef = helper.buildRelationshipType(Constants.CONNECTION_CONNECTOR_TYPE, CONNECTION_CONNECTOR_REL_TYPE_GUID);
        when(omrsRepositoryHelper.getTypeDefByName(Constants.INFORMATION_VIEW_USER_ID, typeDef.getName())).thenReturn(typeDef);
        typeDef = helper.buildRelationshipType(Constants.CONNECTION_TO_ASSET, CONNECTION_ASSET_REL_TYPE_GUID);
        when(omrsRepositoryHelper.getTypeDefByName(Constants.INFORMATION_VIEW_USER_ID, typeDef.getName())).thenReturn(typeDef);
        typeDef = helper.buildRelationshipType(Constants.DATA_CONTENT_FOR_DATASET, DATA_CONTENT_DATASET_REL_TYPE_GUID);
        when(omrsRepositoryHelper.getTypeDefByName(Constants.INFORMATION_VIEW_USER_ID, typeDef.getName())).thenReturn(typeDef);
        typeDef = helper.buildRelationshipType(Constants.ASSET_SCHEMA_TYPE, ASSET_SCHEMA_REL_TYPE_GUID);
        when(omrsRepositoryHelper.getTypeDefByName(Constants.INFORMATION_VIEW_USER_ID, typeDef.getName())).thenReturn(typeDef);
        typeDef = helper.buildRelationshipType(Constants.SCHEMA_ATTRIBUTE_TYPE, SCHEMA_ATTRIBUTE_TYPE_REL_TYPE_GUID);
        when(omrsRepositoryHelper.getTypeDefByName(Constants.INFORMATION_VIEW_USER_ID, typeDef.getName())).thenReturn(typeDef);
        typeDef = helper.buildRelationshipType(Constants.ATTRIBUTE_FOR_SCHEMA, ATTRIBUTE_FOR_SCHEMA_REL_TYPE_GUID);
        when(omrsRepositoryHelper.getTypeDefByName(Constants.INFORMATION_VIEW_USER_ID, typeDef.getName())).thenReturn(typeDef);
        typeDef = helper.buildRelationshipType(Constants.SCHEMA_QUERY_IMPLEMENTATION, SCHEMA_QUERY_IMPLEMENTATION_REL_TYPE_GUID);
        when(omrsRepositoryHelper.getTypeDefByName(Constants.INFORMATION_VIEW_USER_ID, typeDef.getName())).thenReturn(typeDef);
        typeDef = helper.buildRelationshipType(Constants.SEMANTIC_ASSIGNMENT, SEMANTIC_ASSIGNMENT_REL_TYPE_GUID);
        when(omrsRepositoryHelper.getTypeDefByName(Constants.INFORMATION_VIEW_USER_ID, typeDef.getName())).thenReturn(typeDef);
        typeDef = helper.buildRelationshipType(Constants.FOREIGN_KEY, FOREIGN_KEY_REL_TYPE_GUID);
        when(omrsRepositoryHelper.getTypeDefByName(Constants.INFORMATION_VIEW_USER_ID, typeDef.getName())).thenReturn(typeDef);
    }

    @Test
    public void testColumnContext() throws Exception {
        List<TableContextEvent> events = builder.buildContexts(GUID_COLUMN);
        assertNotNull(events);
        assertEquals(events.size(), 1);
        TableContextEvent event = events.get(0);
        assertEquals(event.getTableSource().getName(), TABLE_NAME);
        assertEquals(event.getTableSource().getSchemaName(), RELATIONAL_DB_SCHEMA_NAME);
        assertEquals(event.getTableSource().getDatabaseSource().getEndpointSource().getNetworkAddress(), HOSTNAME_VALUE + ":" + PORT_VALUE);
        assertEquals(event.getTableSource().getDatabaseSource().getEndpointSource().getProtocol(), PROTOCOL_VALUE);
        assertEquals(event.getTableColumns().get(0).getName(), COLUMN_NAME);


    }
}
