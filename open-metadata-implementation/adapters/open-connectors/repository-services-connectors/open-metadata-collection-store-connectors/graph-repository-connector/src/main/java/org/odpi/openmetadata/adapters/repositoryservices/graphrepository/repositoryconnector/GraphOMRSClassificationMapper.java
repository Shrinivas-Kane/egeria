/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.adapters.repositoryservices.graphrepository.repositoryconnector;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.Classification;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.ClassificationOrigin;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.InstanceProperties;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.InstancePropertyCategory;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.InstancePropertyValue;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.InstanceProvenanceType;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.InstanceStatus;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.InstanceType;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.PrimitivePropertyValue;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.typedefs.TypeDef;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.typedefs.TypeDefAttribute;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.typedefs.TypeDefCategory;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.typedefs.TypeDefSummary;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryHelper;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.RepositoryErrorException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.TypeErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.odpi.openmetadata.adapters.repositoryservices.graphrepository.repositoryconnector.GraphOMRSConstants.*;



public class GraphOMRSClassificationMapper {

        private static final Logger log = LoggerFactory.getLogger(GraphOMRSClassificationMapper.class);

        private String               metadataCollectionId;
        private String               repositoryName;
        private OMRSRepositoryHelper repositoryHelper;

        public GraphOMRSClassificationMapper(String               metadataCollectionId,
                                             String               repositoryName,
                                             OMRSRepositoryHelper repositoryHelper)
        {

            this.metadataCollectionId   = metadataCollectionId;
            this.repositoryName         = repositoryName;
            this.repositoryHelper       = repositoryHelper;
        }



        private Object getVertexProperty(Vertex vertex, String propName)
        {
            VertexProperty vp = vertex.property(propName);
            if (vp == null || !vp.isPresent())
                return null;
            else
                return vp.value();
        }



        // Inbound methods - i.e. writing to store



        public void mapClassificationToVertex(Classification classification, Vertex vertex)
                throws RepositoryErrorException
        {
            final String methodName = "mapClassificationToVertex";

            mapInstanceAuditHeaderToVertex(classification, vertex);

            InstanceProperties classificationProperties = classification.getProperties();
            if (classificationProperties != null) {

                // First write properties as json - useful for handling collections and possibly for full text/string matching???
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString;
                try {
                    jsonString = objectMapper.writeValueAsString(classificationProperties);
                    log.debug("{} classification has serialized properties {}", methodName, jsonString);
                    vertex.property("classificationProperties", jsonString);
                } catch (Throwable exc) {
                    log.error("{} Caught exception from classification mapper", methodName);
                    GraphOMRSErrorCode errorCode = GraphOMRSErrorCode.CLASSIFICATION_PROPERTIES_ERROR;
                    String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(classification.getName(), methodName,
                            this.getClass().getName(),
                            repositoryName);
                    throw new RepositoryErrorException(errorCode.getHTTPErrorCode(),
                            this.getClass().getName(),
                            methodName,
                            errorMessage,
                            errorCode.getSystemAction(),
                            errorCode.getUserAction());
                }

                // Secondly add primitive properties to support searches

                String typeName = classification.getType().getTypeDefName();
                TypeDef typeDef = repositoryHelper.getTypeDefByName(repositoryName, typeName);

                // For the type of the entity, walk its type hierarchy and construct a map of short prop name -> qualified prop name.
                Map<String,String> qualifiedPropertyNames = GraphOMRSMapperUtils.getQualifiedPropertyNamesForTypeDef(typeDef, repositoryName, repositoryHelper);

                // This is a full property update - any defined properties that are not in the instanceProperties are cleared.

                List<TypeDefAttribute> propertiesDef = repositoryHelper.getAllPropertiesForTypeDef(repositoryName, typeDef, methodName);

                for (TypeDefAttribute propertyDef : propertiesDef) {
                    String propertyName = propertyDef.getAttributeName();
                    String qualifiedPropName = qualifiedPropertyNames.get(propertyName);
                    // Get the specified value for this property from classificationProperties - uses non-qualified name
                    InstancePropertyValue ipv = classificationProperties.getPropertyValue(propertyName);
                    if (ipv != null) {
                        // a value has been specified

                        // This uses the qualified property name - so that graph index property keys are finer grained which speeds up index enablement and
                        // will also make lookups faster.
                        InstancePropertyCategory ipvCat = ipv.getInstancePropertyCategory();
                        if (ipvCat == InstancePropertyCategory.PRIMITIVE) {
                            // Primitives are stored directly in the graph
                            PrimitivePropertyValue ppv = (PrimitivePropertyValue) ipv;
                            Object primValue = ppv.getPrimitiveValue();
                            vertex.property(qualifiedPropName, primValue);
                        }
                        else {
                            log.debug("{} non-primitive classification property {}", propertyName);
                        }

                    }
                    else {
                        // no value has been specified - remove the property from the vertex
                        VertexProperty vp = vertex.property(qualifiedPropName);
                        if (vp != null) {
                            vp.remove();
                        }
                    }
                }
            }
        }






        public void mapInstanceAuditHeaderToVertex(Classification classification, Vertex vertex)
                throws RepositoryErrorException
        {
            String methodName = "mapInstanceAuditHeaderToVertex";


            // Some properties are mandatory. If any of these are null then throw exception
            boolean missingAttribute = false;
            String  missingAttributeName = null;

            if (classification.getName() != null)
                vertex.property(PROPERTY_KEY_CLASSIFICATION_CLASSIFICATION_NAME, classification.getName());
            else {
                log.debug("{} missing attribute: guid", methodName);
                missingAttribute = true;
                missingAttributeName = "guid";
            }

            InstanceType instanceType = classification.getType();
            if (instanceType != null && instanceType.getTypeDefName() != null)                              // ** name mapping
                vertex.property(PROPERTY_KEY_CLASSIFICATION_TYPE_NAME, instanceType.getTypeDefName());
            else {
                log.debug("{} missing attribute: type name", methodName);
                missingAttribute = true;
                missingAttributeName = "type or typeName";
            }

            if (this.metadataCollectionId != null)
                vertex.property(PROPERTY_KEY_CLASSIFICATION_METADATACOLLECTION_ID, this.metadataCollectionId);
            else {
                log.debug("{} missing attribute: metadataCollectionId", methodName);
                missingAttribute = true;
                missingAttributeName = "metadataCollectionId";
            }

            if (missingAttribute) {
                log.error("{} entity is missing a core attribute {}", methodName, missingAttributeName);
                GraphOMRSErrorCode errorCode = GraphOMRSErrorCode.CLASSIFICATION_PROPERTIES_ERROR;
                String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(classification.getName(), methodName,
                        this.getClass().getName(),
                        repositoryName);
                throw new RepositoryErrorException(errorCode.getHTTPErrorCode(),
                        this.getClass().getName(),
                        methodName,
                        errorMessage,
                        errorCode.getSystemAction(),
                        errorCode.getUserAction());
            }

            // Version always accepted as-is
            vertex.property(PROPERTY_KEY_CLASSIFICATION_VERSION, classification.getVersion());

            // Other properties can be removed if set to null

            if (classification.getMetadataCollectionName() != null) {
                vertex.property(PROPERTY_KEY_CLASSIFICATION_METADATACOLLECTION_NAME, classification.getMetadataCollectionName());
            }
            else {
                VertexProperty vp = vertex.property(PROPERTY_KEY_CLASSIFICATION_METADATACOLLECTION_NAME);
                if (vp != null)
                    vp.remove();
            }


            if (classification.getCreatedBy() != null) {
                vertex.property(PROPERTY_KEY_CLASSIFICATION_CREATED_BY, classification.getCreatedBy());
            }
            else {
                VertexProperty vp = vertex.property(PROPERTY_KEY_CLASSIFICATION_CREATED_BY);
                if (vp != null)
                    vp.remove();
            }

            if (classification.getCreateTime() != null) {
                vertex.property(PROPERTY_KEY_CLASSIFICATION_CREATE_TIME, classification.getCreateTime());
            }
            else {
                VertexProperty vp = vertex.property(PROPERTY_KEY_CLASSIFICATION_CREATE_TIME);
                if (vp != null)
                    vp.remove();
            }

            if (classification.getUpdatedBy() != null) {
                vertex.property(PROPERTY_KEY_CLASSIFICATION_UPDATED_BY, classification.getUpdatedBy());
            }
            else {
                VertexProperty vp = vertex.property(PROPERTY_KEY_CLASSIFICATION_UPDATED_BY);
                if (vp != null)
                    vp.remove();
            }

            if (classification.getUpdateTime() != null) {
                vertex.property(PROPERTY_KEY_CLASSIFICATION_UPDATE_TIME, classification.getUpdateTime());
            }
            else {
                VertexProperty vp = vertex.property(PROPERTY_KEY_CLASSIFICATION_UPDATE_TIME);
                if (vp != null)
                    vp.remove();
            }

            if (classification.getInstanceProvenanceType() != null) {
                vertex.property(PROPERTY_KEY_CLASSIFICATION_PROVENANCE_TYPE, classification.getInstanceProvenanceType().getOrdinal());     // ** ordinal mapping
            }
            else {
                VertexProperty vp = vertex.property(PROPERTY_KEY_CLASSIFICATION_PROVENANCE_TYPE);
                if (vp != null)
                    vp.remove();
            }

            if (classification.getStatus() != null) {
                vertex.property(PROPERTY_KEY_CLASSIFICATION_STATUS, classification.getStatus().getOrdinal());                              // ** ordinal mapping
            }
            else {
                VertexProperty vp = vertex.property(PROPERTY_KEY_CLASSIFICATION_STATUS);
                if (vp != null)
                    vp.remove();
            }


            if (classification.getStatusOnDelete() != null) {
                vertex.property(PROPERTY_KEY_CLASSIFICATION_STATUS_ON_DELETE, classification.getStatusOnDelete().getOrdinal());                              // ** ordinal mapping
            }
            else {
                VertexProperty vp = vertex.property(PROPERTY_KEY_CLASSIFICATION_STATUS_ON_DELETE);
                if (vp != null)
                    vp.remove();
            }



            if (classification.getInstanceLicense() != null) {
                vertex.property(PROPERTY_KEY_CLASSIFICATION_INSTANCE_LICENSE, classification.getInstanceLicense());
            }
            else {
                VertexProperty vp = vertex.property(PROPERTY_KEY_CLASSIFICATION_INSTANCE_LICENSE);
                if (vp != null)
                    vp.remove();
            }

            if (classification.getClassificationOrigin() != null)                                                                    // ** ordinal mapping
                vertex.property(PROPERTY_KEY_CLASSIFICATION_CLASSIFICATION_ORIGIN, classification.getClassificationOrigin().getOrdinal());
            else {
                VertexProperty vp = vertex.property(PROPERTY_KEY_CLASSIFICATION_CLASSIFICATION_ORIGIN);
                if (vp != null)
                    vp.remove();
            }

            if (classification.getClassificationOriginGUID() != null)
                vertex.property(PROPERTY_KEY_CLASSIFICATION_CLASSIFICATION_ORIGIN_GUID, classification.getClassificationOriginGUID());
            else {
                VertexProperty vp = vertex.property(PROPERTY_KEY_CLASSIFICATION_CLASSIFICATION_ORIGIN_GUID);
                if (vp != null)
                    vp.remove();
            }


            // maintainedBy property

            List<String> maintainedByList = classification.getMaintainedBy();
            if (maintainedByList != null && !maintainedByList.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString;
                try {
                    jsonString = objectMapper.writeValueAsString(maintainedByList);
                    vertex.property(PROPERTY_KEY_CLASSIFICATION_MAINTAINED_BY, jsonString);

                } catch (Throwable exc) {
                    GraphOMRSErrorCode errorCode = GraphOMRSErrorCode.CLASSIFICATION_PROPERTIES_ERROR;

                    String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName,
                            this.getClass().getName(),
                            repositoryName);

                    throw new RepositoryErrorException(errorCode.getHTTPErrorCode(),
                            this.getClass().getName(),
                            methodName,
                            errorMessage,
                            errorCode.getSystemAction(),
                            errorCode.getUserAction());
                }

            }
            else {
                VertexProperty vp = vertex.property(PROPERTY_KEY_CLASSIFICATION_MAINTAINED_BY);
                if (vp != null)
                    vp.remove();
            }

            if (classification.getReplicatedBy() != null) {
                vertex.property(PROPERTY_KEY_CLASSIFICATION_REPLICATED_BY, classification.getReplicatedBy());
            }
            else {
                VertexProperty vp = vertex.property(PROPERTY_KEY_CLASSIFICATION_REPLICATED_BY);
                if (vp != null)
                    vp.remove();
            }


        }



        // Outbound methods - i.e. reading from store



        public void mapVertexToClassification(Vertex vertex, Classification classification)
                throws RepositoryErrorException
        {
            String methodName = "mapVertexToClassification";

            mapVertexToInstanceAuditHeader(vertex, classification);

            // properties
            String stringProps = (String) getVertexProperty(vertex, "classificationProperties");

            if (stringProps != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    InstanceProperties instanceProperties = objectMapper.readValue(stringProps, InstanceProperties.class);
                    log.debug("{} classification has deserialized properties {}", methodName, instanceProperties);
                    classification.setProperties(instanceProperties);
                } catch (Throwable exc) {
                    log.error("{} caught exception {}", methodName, exc.getMessage());
                    GraphOMRSErrorCode errorCode = GraphOMRSErrorCode.CLASSIFICATION_PROPERTIES_ERROR;
                    String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(classification.getName(), methodName,
                            this.getClass().getName(),
                            repositoryName);
                    throw new RepositoryErrorException(errorCode.getHTTPErrorCode(),
                            this.getClass().getName(),
                            methodName,
                            errorMessage,
                            errorCode.getSystemAction(),
                            errorCode.getUserAction());
                }
            }

        }



        public void mapVertexToInstanceAuditHeader(Vertex vertex, Classification classification)
                throws RepositoryErrorException
        {
            String methodName = "mapVertexToInstanceAuditHeader";


            classification.setName((String) getVertexProperty( vertex, PROPERTY_KEY_CLASSIFICATION_CLASSIFICATION_NAME));
            classification.setMetadataCollectionId((String) getVertexProperty( vertex, PROPERTY_KEY_CLASSIFICATION_METADATACOLLECTION_ID));
            classification.setMetadataCollectionName((String) getVertexProperty( vertex, PROPERTY_KEY_CLASSIFICATION_METADATACOLLECTION_NAME));
            classification.setClassificationOriginGUID((String) getVertexProperty( vertex, PROPERTY_KEY_CLASSIFICATION_CLASSIFICATION_ORIGIN_GUID));
            classification.setCreatedBy((String) getVertexProperty( vertex,  PROPERTY_KEY_CLASSIFICATION_CREATED_BY));
            classification.setCreateTime((Date)getVertexProperty( vertex, PROPERTY_KEY_CLASSIFICATION_CREATE_TIME));
            classification.setUpdatedBy((String) getVertexProperty( vertex,  PROPERTY_KEY_CLASSIFICATION_UPDATED_BY));
            classification.setUpdateTime((Date)getVertexProperty( vertex, PROPERTY_KEY_CLASSIFICATION_UPDATE_TIME));
            classification.setVersion((long) getVertexProperty( vertex, PROPERTY_KEY_CLASSIFICATION_VERSION));
            classification.setInstanceLicense((String) getVertexProperty( vertex, PROPERTY_KEY_CLASSIFICATION_INSTANCE_LICENSE));


            // Retrieve the type name from the vertex, use the RH to retrieve the type
            // then use the necessary type fields to construct a TypeDefSummary and pass
            // that to the RH to create a new InstanceType...
            try {
                String typeName = (String) getVertexProperty( vertex, PROPERTY_KEY_CLASSIFICATION_TYPE_NAME);
                TypeDef typeDef = repositoryHelper.getTypeDefByName(repositoryName, typeName);
                TypeDefSummary typeDefSummary = new TypeDefSummary(TypeDefCategory.CLASSIFICATION_DEF, typeDef.getGUID(), typeDef.getName(), typeDef.getVersion(), typeDef.getVersionName());
                InstanceType instanceType = repositoryHelper.getNewInstanceType(repositoryName, typeDefSummary);
                classification.setType(instanceType);

            } catch (TypeErrorException e) {
                log.error("{} caught exception {}", methodName, e.getMessage());
                GraphOMRSErrorCode errorCode = GraphOMRSErrorCode.CLASSIFICATION_PROPERTIES_ERROR;

                String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(classification.getName(), methodName,
                        this.getClass().getName(),
                        repositoryName);

                throw new RepositoryErrorException(errorCode.getHTTPErrorCode(),
                        this.getClass().getName(),
                        methodName,
                        errorMessage,
                        errorCode.getSystemAction(),
                        errorCode.getUserAction());
            }

            Integer provenanceOrdinal = (Integer) getVertexProperty( vertex, PROPERTY_KEY_CLASSIFICATION_PROVENANCE_TYPE);
            InstanceProvenanceType instanceProvenanceType = GraphOMRSMapperUtils.mapProvenanceOrdinalToEnum(provenanceOrdinal);
            classification.setInstanceProvenanceType(instanceProvenanceType);

            Integer statusOrdinal = (Integer) getVertexProperty( vertex, PROPERTY_KEY_CLASSIFICATION_STATUS);
            InstanceStatus instanceStatus = GraphOMRSMapperUtils.mapStatusOrdinalToEnum(statusOrdinal);
            classification.setStatus(instanceStatus);

            Integer statusOnDeleteOrdinal = (Integer) getVertexProperty( vertex, PROPERTY_KEY_CLASSIFICATION_STATUS_ON_DELETE);
            InstanceStatus instanceStatusOnDelete = GraphOMRSMapperUtils.mapStatusOrdinalToEnum(statusOnDeleteOrdinal);
            classification.setStatusOnDelete(instanceStatusOnDelete);

            Integer originOrdinal = (Integer) getVertexProperty( vertex, PROPERTY_KEY_CLASSIFICATION_CLASSIFICATION_ORIGIN);
            ClassificationOrigin classificationOrigin = GraphOMRSMapperUtils.mapClassificationOriginOrdinalToEnum(originOrdinal);
            classification.setClassificationOrigin(classificationOrigin);

            // maintainedBy
            String maintainedByString = (String) getVertexProperty(vertex, PROPERTY_KEY_CLASSIFICATION_MAINTAINED_BY);
            if (maintainedByString != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    List<String> maintainedByList = (List<String>) objectMapper.readValue(maintainedByString, List.class);
                    log.debug("{} vertex has deserialized maintainedBy list {}", methodName, maintainedByList);
                    classification.setMaintainedBy(maintainedByList);

                } catch (Throwable exc) {
                    GraphOMRSErrorCode errorCode = GraphOMRSErrorCode.CLASSIFICATION_PROPERTIES_ERROR;

                    String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(classification.getName(), methodName,
                            this.getClass().getName(),
                            repositoryName);

                    throw new RepositoryErrorException(errorCode.getHTTPErrorCode(),
                            this.getClass().getName(),
                            methodName,
                            errorMessage,
                            errorCode.getSystemAction(),
                            errorCode.getUserAction());
                }
            }

            classification.setReplicatedBy((String) getVertexProperty( vertex,  PROPERTY_KEY_CLASSIFICATION_REPLICATED_BY));

        }


}
