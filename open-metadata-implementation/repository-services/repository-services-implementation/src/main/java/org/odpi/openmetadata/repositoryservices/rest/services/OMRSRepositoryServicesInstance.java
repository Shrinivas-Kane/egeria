/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.repositoryservices.rest.services;

import org.odpi.openmetadata.commonservices.multitenant.OMAGServerServiceInstance;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditLog;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.OMRSMetadataCollection;
import org.odpi.openmetadata.repositoryservices.localrepository.repositoryconnector.LocalOMRSRepositoryConnector;


/**
 * OMRSRepositoryServicesInstance caches references to OMRS objects for a specific server
 */
public class OMRSRepositoryServicesInstance extends OMAGServerServiceInstance
{
    private OMRSMetadataCollection localMetadataCollection;
    private String                 localServerURL;
    private OMRSAuditLog           auditLog;


    /**
     * Set up the local repository connector that will service the REST Calls.
     *
     * @param localServerName name of this server
     * @param localRepositoryConnector link to the local repository responsible for servicing the REST calls.
     *                                 If localRepositoryConnector is null when a REST calls is received, the request
     *                                 is rejected.
     * @param localServerURL URL of the local server
     * @param serviceName name of this service
     * @param auditLog logging destination
     */
    public OMRSRepositoryServicesInstance(String                       localServerName,
                                          LocalOMRSRepositoryConnector localRepositoryConnector,
                                          String                       localServerURL,
                                          String                       serviceName,
                                          OMRSAuditLog                 auditLog)
    {
        super(localServerName, serviceName);

        this.auditLog = auditLog;
        this.localServerURL = localServerURL;

        try
        {
            this.localMetadataCollection = localRepositoryConnector.getMetadataCollection();
        }
        catch (Throwable error)
        {
            this.localMetadataCollection = null;
        }
    }


    /**
     * Return the local metadata collection for this server.
     *
     * @return OMRSMetadataCollection object
     */
    public OMRSMetadataCollection getLocalMetadataCollection()
    {
        return localMetadataCollection;
    }


    /**
     * Return the URL root for this server.
     *
     * @return URL
     */
    public String getLocalServerURL()
    {
        return localServerURL;
    }


    /**
     * Return the audit log destination for this server.
     *
     * @return auditLog
     */
    public OMRSAuditLog getAuditLog()
    {
        return auditLog;
    }
}
