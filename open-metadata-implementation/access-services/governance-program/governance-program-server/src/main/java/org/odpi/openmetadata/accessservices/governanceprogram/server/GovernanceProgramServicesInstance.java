/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.governanceprogram.server;


import org.odpi.openmetadata.accessservices.governanceprogram.ffdc.GovernanceProgramErrorCode;
import org.odpi.openmetadata.accessservices.governanceprogram.handlers.ExternalReferencesHandler;
import org.odpi.openmetadata.accessservices.governanceprogram.handlers.GovernanceOfficerHandler;
import org.odpi.openmetadata.accessservices.governanceprogram.handlers.PersonalProfileHandler;
import org.odpi.openmetadata.adminservices.configuration.registration.AccessServiceDescription;
import org.odpi.openmetadata.commonservices.multitenant.OMASServiceInstance;
import org.odpi.openmetadata.commonservices.multitenant.ffdc.exceptions.NewInstanceException;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditLog;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryConnector;


/**
 * GovernanceProgramServicesInstance caches references to OMRS objects for a specific server.
 * It is also responsible for registering itself in the instance map.
 */
public class GovernanceProgramServicesInstance extends OMASServiceInstance
{
    private static AccessServiceDescription myDescription = AccessServiceDescription.GOVERNANCE_PROGRAM_OMAS;

    private GovernanceOfficerHandler  governanceOfficerHandler;
    private ExternalReferencesHandler externalReferencesHandler;
    private PersonalProfileHandler    personalProfileHandler;


    /**
     * Set up the local repository connector that will service the REST Calls.
     *
     * @param repositoryConnector link to the repository responsible for servicing the REST calls.
     * @param auditLog logging destination
     *
     * @throws NewInstanceException a problem occurred during initialization
     */
    public GovernanceProgramServicesInstance(OMRSRepositoryConnector repositoryConnector,
                                             OMRSAuditLog auditLog) throws NewInstanceException
    {
        super(myDescription.getAccessServiceName() + " OMAS",
              repositoryConnector,
              auditLog);

        final String methodName = "new ServiceInstance";

        if (repositoryHandler != null)
        {
            this.externalReferencesHandler = new ExternalReferencesHandler(serviceName,
                                                                           serverName,
                                                                           invalidParameterHandler,
                                                                           repositoryHelper,
                                                                           repositoryHandler);

            this.personalProfileHandler = new PersonalProfileHandler(serviceName,
                                                                           serverName,
                                                                           invalidParameterHandler,
                                                                           repositoryHelper,
                                                                           repositoryHandler);

            this.governanceOfficerHandler = new GovernanceOfficerHandler(serviceName,
                                                                         serverName,
                                                                         invalidParameterHandler,
                                                                         repositoryHelper,
                                                                         repositoryHandler,
                                                                         personalProfileHandler,
                                                                         externalReferencesHandler);
        }
        else
        {
            GovernanceProgramErrorCode errorCode   = GovernanceProgramErrorCode.OMRS_NOT_INITIALIZED;
            String                     errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName);

            throw new NewInstanceException(errorCode.getHTTPErrorCode(),
                                           this.getClass().getName(),
                                           methodName,
                                           errorMessage,
                                           errorCode.getSystemAction(),
                                           errorCode.getUserAction());

        }
    }


    /**
     * Return the server name.
     *
     * @return serverName name of the server for this instance
     * @throws NewInstanceException a problem occurred during initialization
     */
    public String getServerName() throws NewInstanceException
    {
        final String methodName = "getServerName";

        if (serverName != null)
        {
            return serverName;
        }
        else
        {
            GovernanceProgramErrorCode errorCode    = GovernanceProgramErrorCode.OMRS_NOT_AVAILABLE;
            String                errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName);

            throw new NewInstanceException(errorCode.getHTTPErrorCode(),
                                           this.getClass().getName(),
                                           methodName,
                                           errorMessage,
                                           errorCode.getSystemAction(),
                                           errorCode.getUserAction());
        }
    }


    /**
     * Return the governance officer handler
     *
     * @return handler
     */
    GovernanceOfficerHandler getGovernanceOfficerHandler()
    {
        return governanceOfficerHandler;
    }


    /**
     * Return the external references handler
     *
     * @return handler
     */
    ExternalReferencesHandler getExternalReferencesHandler()
    {
        return externalReferencesHandler;
    }


    /**
     * Return the persona profile handler.
     *
     * @return handler
     */
    PersonalProfileHandler getPersonalProfileHandler()
    {
        return personalProfileHandler;
    }
}
