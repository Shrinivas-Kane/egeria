/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.dataengine.server.admin;

import org.odpi.openmetadata.accessservices.dataengine.ffdc.DataEngineErrorCode;
import org.odpi.openmetadata.accessservices.dataengine.server.handlers.DataEngineSchemaTypeHandler;
import org.odpi.openmetadata.accessservices.dataengine.server.handlers.PortHandler;
import org.odpi.openmetadata.accessservices.dataengine.server.handlers.ProcessHandler;
import org.odpi.openmetadata.accessservices.dataengine.server.handlers.SoftwareServerRegistrationHandler;
import org.odpi.openmetadata.adminservices.configuration.registration.AccessServiceDescription;
import org.odpi.openmetadata.commonservices.multitenant.OCFOMASServiceInstance;
import org.odpi.openmetadata.commonservices.multitenant.ffdc.exceptions.NewInstanceException;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditLog;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryConnector;

import java.util.List;

/**
 * DataEngineServicesInstance caches references to OMRS objects for a specific server.
 * It is also responsible for registering itself in the instance map.
 */
class DataEngineServicesInstance extends OCFOMASServiceInstance {
    private static final AccessServiceDescription description = AccessServiceDescription.DATA_ENGINE_OMAS;

    private ProcessHandler processHandler;
    private SoftwareServerRegistrationHandler softwareServerRegistrationHandler;
    private DataEngineSchemaTypeHandler dataEngineSchemaTypeHandler;
    private PortHandler portHandler;

    /**
     * Set up the local repository connector that will service the REST Calls.
     *
     * @param repositoryConnector link to the repository responsible for servicing the REST calls.
     *
     * @throws NewInstanceException a problem occurred during initialization
     */
    DataEngineServicesInstance(OMRSRepositoryConnector repositoryConnector, List<String> supportedZones,
                               OMRSAuditLog auditLog) throws NewInstanceException {
        super(description.getAccessServiceName(), repositoryConnector, auditLog);
        super.supportedZones = supportedZones;

        if (repositoryHandler != null) {
            processHandler = new ProcessHandler(serviceName, serverName, invalidParameterHandler, repositoryHandler,
                    repositoryHelper);
            softwareServerRegistrationHandler = new SoftwareServerRegistrationHandler(serviceName, serverName,
                    invalidParameterHandler, repositoryHandler, repositoryHelper);
            dataEngineSchemaTypeHandler = new DataEngineSchemaTypeHandler(serviceName, serverName,
                    invalidParameterHandler, repositoryHandler, repositoryHelper);
            portHandler = new PortHandler(serviceName, serverName, invalidParameterHandler, repositoryHandler,
                    repositoryHelper);

        } else {
            final String methodName = "new ServiceInstance";

            DataEngineErrorCode errorCode = DataEngineErrorCode.OMRS_NOT_INITIALIZED;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName);

            throw new NewInstanceException(errorCode.getHttpErrorCode(), this.getClass().getName(), methodName,
                    errorMessage, errorCode.getSystemAction(), errorCode.getUserAction());
        }
    }

    /**
     * Return the handler for process requests
     *
     * @return handler object
     */
    ProcessHandler getProcessHandler() {
        return processHandler;
    }

    /**
     * Return the handler for registration requests
     *
     * @return handler object
     */
    SoftwareServerRegistrationHandler getSoftwareServerRegistrationHandler() {
        return softwareServerRegistrationHandler;
    }

    /**
     * Return the handler for schema types requests
     *
     * @return handler object
     */
    DataEngineSchemaTypeHandler getDataEngineSchemaTypeHandler() {
        return dataEngineSchemaTypeHandler;
    }

    /**
     * Return the handler for port requests
     *
     * @return handler object
     */
    PortHandler getPortHandler() {
        return portHandler;
    }
}
