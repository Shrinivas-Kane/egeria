/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adminservices;

import org.odpi.openmetadata.adapters.repositoryservices.ConnectorConfigurationFactory;
import org.odpi.openmetadata.adminservices.configuration.properties.*;
import org.odpi.openmetadata.adminservices.ffdc.OMAGAdminErrorCode;
import org.odpi.openmetadata.adminservices.rest.OMAGServerConfigResponse;
import org.odpi.openmetadata.commonservices.ffdc.rest.VoidResponse;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.Connection;
import org.odpi.openmetadata.adminservices.ffdc.exception.OMAGConfigurationErrorException;
import org.odpi.openmetadata.adminservices.ffdc.exception.OMAGInvalidParameterException;
import org.odpi.openmetadata.adminservices.ffdc.exception.OMAGNotAuthorizedException;
import org.odpi.openmetadata.repositoryservices.admin.OMRSConfigurationFactory;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * OMAGServerAdminServices provides part of the server-side implementation of the administrative interface for
 * an Open Metadata and Governance (OMAG) Server.  In particular, this class supports the configuration
 * of the server name, server type and organization name.  It also supports the setting up of the
 * Open Metadata Repository Services' local repository and cohort.
 */
public class OMAGServerAdminServices
{
    private OMAGServerAdminStoreServices   configStore = new OMAGServerAdminStoreServices();
    private OMAGServerErrorHandler         errorHandler = new OMAGServerErrorHandler();
    private OMAGServerExceptionHandler     exceptionHandler = new OMAGServerExceptionHandler();
    /*
     * =============================================================
     * Configure server - basic options using defaults
     */


    /**
     * Set up the descriptive type of the server.  This value is added to distributed events to
     * make it easier to understand the source of events.  The default value is "Open Metadata and Governance Server".
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param typeName  short description for the type of server.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or serverType parameter.
     */
    public VoidResponse setServerType(String userId,
                                      String serverName,
                                      String typeName)
    {
        final String methodName = "setServerType";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            List<String>  configAuditTrail          = serverConfig.getAuditTrail();

            if (configAuditTrail == null)
            {
                configAuditTrail = new ArrayList<>();
            }

            if ("".equals(typeName))
            {
                typeName = null;
            }

            if (typeName == null)
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " removed configuration for local server type name.");
            }
            else
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " updated configuration for local server type name to " + typeName + ".");
            }

            serverConfig.setAuditTrail(configAuditTrail);
            serverConfig.setLocalServerType(typeName);

            configStore.saveServerConfig(serverName, methodName, serverConfig);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set up the name of the organization that is running this server.  This value is added to distributed events to
     * make it easier to understand the source of events.  The default value is null.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param name  String name of the organization.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or organizationName parameter.
     */
    public VoidResponse setOrganizationName(String userId,
                                            String serverName,
                                            String name)
    {
        final String methodName = "setOrganizationName";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            List<String>  configAuditTrail          = serverConfig.getAuditTrail();

            if (configAuditTrail == null)
            {
                configAuditTrail = new ArrayList<>();
            }

            if ("".equals(name))
            {
                name = null;
            }

            if (name == null)
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " removed configuration for local server's owning organization's name.");
            }
            else
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " updated configuration for local server's owning organization's name to " + name + ".");
            }

            serverConfig.setAuditTrail(configAuditTrail);
            serverConfig.setOrganizationName(name);

            configStore.saveServerConfig(serverName, methodName, serverConfig);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set up the user id to use when there is no external user driving the work (for example when processing events
     * from another server).
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param serverUserId  String user is for the server.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or serverURLRoot parameter.
     */
    public VoidResponse setServerUserId(String userId,
                                        String serverName,
                                        String serverUserId)
    {
        final String methodName = "setServerUserId";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateUserId(userId, serverName, methodName);
            errorHandler.validateServerName(serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            List<String>  configAuditTrail          = serverConfig.getAuditTrail();

            if (configAuditTrail == null)
            {
                configAuditTrail = new ArrayList<>();
            }

            if ("".equals(serverUserId))
            {
                serverUserId = null;
            }

            if (serverUserId == null)
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " removed configuration for local server's userId.");
            }
            else
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " updated configuration for local server's userId to " + serverUserId + ".");
            }

            serverConfig.setAuditTrail(configAuditTrail);
            serverConfig.setLocalServerUserId(serverUserId);

            configStore.saveServerConfig(serverName, methodName, serverConfig);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set up the user id to use when there is no external user driving the work (for example when processing events
     * from another server).
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param serverPassword  String password for the server.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or serverURLRoot parameter.
     */
    public VoidResponse setServerPassword(String userId,
                                          String serverName,
                                          String serverPassword)
    {
        final String methodName = "setServerPassword";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateUserId(userId, serverName, methodName);
            errorHandler.validateServerName(serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            List<String>  configAuditTrail          = serverConfig.getAuditTrail();

            if (configAuditTrail == null)
            {
                configAuditTrail = new ArrayList<>();
            }

            if ("".equals(serverPassword))
            {
                serverPassword = null;
            }

            if (serverPassword == null)
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " removed configuration for local server's password.");
            }
            else
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " updated configuration for local server's password to " + serverPassword + ".");
            }

            serverConfig.setAuditTrail(configAuditTrail);
            serverConfig.setLocalServerPassword(serverPassword);

            configStore.saveServerConfig(serverName, methodName, serverConfig);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set an upper limit in the page size that can be requested on a REST call to the server.  The default
     * value is 1000.
     *
     * @param userId - user that is issuing the request.
     * @param serverName - local server name.
     * @param maxPageSize - max number of elements that can be returned on a request.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or maxPageSize parameter.
     */
    public VoidResponse setMaxPageSize(String  userId,
                                       String  serverName,
                                       int     maxPageSize)
    {
        final String methodName = "setMaxPageSize";

        VoidResponse response = new VoidResponse();

        try
        {
            /*
             * Validate and set up the userName and server name.
             */
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            if (maxPageSize > 0)
            {
                OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

                List<String>  configAuditTrail          = serverConfig.getAuditTrail();

                if (configAuditTrail == null)
                {
                    configAuditTrail = new ArrayList<>();
                }

                configAuditTrail.add(new Date().toString() + " " + userId + " updated configuration for maximum page size to " + maxPageSize + ".");

                serverConfig.setAuditTrail(configAuditTrail);
                serverConfig.setMaxPageSize(maxPageSize);

                configStore.saveServerConfig(serverName, methodName, serverConfig);
            }
            else
            {
                OMAGAdminErrorCode errorCode = OMAGAdminErrorCode.BAD_MAX_PAGE_SIZE;
                String        errorMessage = errorCode.getErrorMessageId()
                                           + errorCode.getFormattedErrorMessage(serverName, Integer.toString(maxPageSize));

                throw new OMAGInvalidParameterException(errorCode.getHTTPErrorCode(),
                                                        this.getClass().getName(),
                                                        methodName,
                                                        errorMessage,
                                                        errorCode.getSystemAction(),
                                                        errorCode.getUserAction());
            }
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set up the default event bus for embedding in event-driven connector.   The resulting connector will
     * be used in the OMRS Topic Connector for each cohort, the in and out topics for each Access Service and
     * the local repositories event mapper.
     *
     * When the event bus is configured, it is used only on future configuration.  It does not effect
     * existing configuration.
     *
     * @param userId  user that is issuing the request.
     * @param serverName local server name.
     * @param connectorProvider  connector provider for the event bus (if it is null then Kafka is assumed).
     * @param topicURLRoot the common root of the topics used by the open metadata server.
     * @param configurationProperties  property name/value pairs used to configure the connection to the event bus connector
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGConfigurationErrorException it is too late to configure the event bus - other configuration already exists or
     * OMAGInvalidParameterException invalid serverName parameter.
     */
    public VoidResponse setEventBus(String              userId,
                                    String              serverName,
                                    String              connectorProvider,
                                    String              topicURLRoot,
                                    Map<String, Object> configurationProperties)
    {
        final String methodName = "setEventBus";

        VoidResponse response = new VoidResponse();

        try
        {
            /*
             * Validate and set up the userName and server name.
             */
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            /*
             * Retrieve the existing configuration.
             */
            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            EventBusConfig   eventBusConfig = new EventBusConfig();

            eventBusConfig.setConnectorProvider(connectorProvider);
            eventBusConfig.setTopicURLRoot(topicURLRoot);
            eventBusConfig.setConfigurationProperties(configurationProperties);

            List<String>  configAuditTrail          = serverConfig.getAuditTrail();

            if (configAuditTrail == null)
            {
                configAuditTrail = new ArrayList<>();
            }

            configAuditTrail.add(new Date().toString() + " " + userId + " updated configuration for default event bus.");

            serverConfig.setAuditTrail(configAuditTrail);
            serverConfig.setEventBusConfig(eventBusConfig);

            /*
             * Save the config away
             */
            configStore.saveServerConfig(serverName, methodName, serverConfig);

        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set up the root URL for this server that is used to construct full URL paths to calls for
     * this server's REST interfaces that is used by other members of the cohorts that this server
     * connects to.
     *
     * The default value is "localhost:8080".
     *
     * ServerURLRoot is used during the configuration of the local repository.  If called
     * after the local repository is configured, it has no effect.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param url  String url.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or serverURLRoot parameter.
     */
    public VoidResponse setServerURLRoot(String userId,
                                         String serverName,
                                         String url)
    {
        final String methodName = "setServerURLRoot";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateUserId(userId, serverName, methodName);
            errorHandler.validateServerName(serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            List<String>  configAuditTrail          = serverConfig.getAuditTrail();

            if (configAuditTrail == null)
            {
                configAuditTrail = new ArrayList<>();
            }

            if ("".equals(url))
            {
                url = null;
            }

            if (url == null)
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " removed configuration for local server's URL root.");
            }
            else
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " updated configuration for local server's URL root to " + url + ".");
            }

            serverConfig.setAuditTrail(configAuditTrail);
            serverConfig.setLocalServerURL(url);

            configStore.saveServerConfig(serverName, methodName, serverConfig);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set up the default audit log for the server.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryMode parameter.
     */
    public VoidResponse setDefaultAuditLog(String userId,
                                           String serverName)
    {
        final String methodName = "setDefaultAuditLog";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            ConnectorConfigurationFactory configurationFactory     = new ConnectorConfigurationFactory();

            List<Connection>  auditLogConnections = new ArrayList<>();

            auditLogConnections.add(configurationFactory.getDefaultAuditLogConnection(serverName));

            this.setAuditLogDestinations(userId, serverName, auditLogConnections);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Add a new open metadata archive to load at startup.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param fileName name of the open metadata archive file.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or fileName parameter.
     */
    public VoidResponse addStartUpOpenMetadataArchiveFile(String userId,
                                                          String serverName,
                                                          String fileName)
    {
        final String methodName = "addStartUpOpenMetadataArchiveFile";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);
            errorHandler.validateFileName(fileName, serverName, methodName);

            ConnectorConfigurationFactory configurationFactory   = new ConnectorConfigurationFactory();
            Connection                    newOpenMetadataArchive = configurationFactory.getOpenMetadataArchiveFileConnection(fileName);

            List<Connection>              openMetadataArchiveConnections = null;

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);
            RepositoryServicesConfig  repositoryServicesConfig = serverConfig.getRepositoryServicesConfig();

            if (repositoryServicesConfig != null)
            {
                openMetadataArchiveConnections = repositoryServicesConfig.getOpenMetadataArchiveConnections();
            }

            if (openMetadataArchiveConnections == null)
            {
                openMetadataArchiveConnections = new ArrayList<>();
            }

            openMetadataArchiveConnections.add(newOpenMetadataArchive);

            this.setOpenMetadataArchives(userId, serverName, openMetadataArchiveConnections);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set up an in memory local repository.  This repository uses hashmaps to store content.  It is useful
     * for demos, testing and POCs.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryMode parameter.
     */
    public VoidResponse setInMemLocalRepository(String userId,
                                                String serverName)
    {
        final String methodName = "setInMemLocalRepository";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            OMRSConfigurationFactory configurationFactory     = new OMRSConfigurationFactory();


            this.setLocalRepositoryConfig(userId,
                                          serverName,
                                          configurationFactory.getInMemoryLocalRepositoryConfig(serverConfig.getLocalServerName(),
                                                                                                serverConfig.getLocalServerURL()));
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set up a graph store as the local repository.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGConfigurationErrorException the event bus has not been configured or
     * OMAGInvalidParameterException invalid serverName or localRepositoryMode parameter.
     */
    public VoidResponse setGraphLocalRepository(String              userId,
                                                String              serverName)
    {
        final String methodName = "setGraphLocalRepository";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            OMRSConfigurationFactory configurationFactory     = new OMRSConfigurationFactory();


            this.setLocalRepositoryConfig(userId,
                                          serverName,
                                          configurationFactory.getLocalGraphLocalRepositoryConfig(serverConfig.getLocalServerName(),
                                                                                                  serverConfig.getLocalServerURL()));
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Remove all configuration for a local repository.  The default is no local repository.  This call
     * can be used to remove subsequent local repository configuration.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryMode parameter.
     */
    public VoidResponse setNoRepositoryMode(String userId,
                                            String serverName)
    {
        final String methodName = "setNoRepositoryMode";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            this.setLocalRepositoryConfig(userId, serverName, null);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Provide the connection to the local repository - used when the local repository mode is set to repository proxy.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param connection  connection to the OMRS repository connector.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or repositoryProxyConnection parameter or
     */
    public VoidResponse setRepositoryProxyConnection(String     userId,
                                                     String     serverName,
                                                     Connection connection)
    {
        final String methodName = "setRepositoryProxyConnection";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            OMRSConfigurationFactory configurationFactory     = new OMRSConfigurationFactory();
            LocalRepositoryConfig localRepositoryConfig
                    = configurationFactory.getRepositoryProxyLocalRepositoryConfig(serverConfig.getLocalServerName(),
                                                                                   serverConfig.getLocalServerURL());

            /*
             * Set up the repository proxy connection in the local repository config
             */
            localRepositoryConfig.setLocalRepositoryLocalConnection(connection);

            this.setLocalRepositoryConfig(userId, serverName, localRepositoryConfig);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Provide the connection to the local repository.
     * This is used when the local repository mode is set to repository proxy.
     *
     * @param userId                    user that is issuing the request.
     * @param serverName                local server name.
     * @param connectorProvider         connector provider class name to the OMRS repository connector.
     * @param additionalProperties      additional parameters to pass to the repository connector
     * @return void response or
     * OMAGNotAuthorizedException  the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or repositoryProxyConnection parameter or
     * OMAGConfigurationErrorException the local repository mode has not been set.
     */
    public VoidResponse setRepositoryProxyConnection(String               userId,
                                                     String               serverName,
                                                     String               connectorProvider,
                                                     Map<String, Object>  additionalProperties)
    {
        final String methodName  = "setRepositoryProxyConnection";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            ConnectorConfigurationFactory connectorConfigurationFactory = new ConnectorConfigurationFactory();

            this.setRepositoryProxyConnection(userId,
                                              serverName,
                                              connectorConfigurationFactory.getRepositoryProxyConnection(serverName,
                                                                                                         connectorProvider,
                                                                                                         serverConfig.getLocalServerURL(),
                                                                                                         additionalProperties));
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Provide the connection to the local repository's event mapper if needed.  The default value is null which
     * means no event mapper.  An event mapper is needed if the local repository has additional APIs that can change
     * the metadata in the repository without going through the open metadata and governance services.
     *
     * @param userId user that is issuing the request.
     * @param serverName local server name.
     * @param connection connection to the OMRS repository event mapper.
     * @return void response
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryEventMapper parameter or
     * OMAGConfigurationErrorException the local repository mode has not been set
     */
    public VoidResponse setLocalRepositoryEventMapper(String     userId,
                                                      String     serverName,
                                                      Connection connection)
    {
        final String methodName = "setLocalRepositoryEventMapper";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            RepositoryServicesConfig repositoryServicesConfig = serverConfig.getRepositoryServicesConfig();
            LocalRepositoryConfig    localRepositoryConfig    = null;

            /*
             * Extract any existing local repository configuration
             */
            if (repositoryServicesConfig != null)
            {
                localRepositoryConfig = repositoryServicesConfig.getLocalRepositoryConfig();
            }

            /*
             * The local repository should be partially configured already by setLocalRepositoryMode()
             */
            if (localRepositoryConfig == null)
            {
                OMAGAdminErrorCode errorCode    = OMAGAdminErrorCode.LOCAL_REPOSITORY_MODE_NOT_SET;
                String             errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(serverName);

                throw new OMAGConfigurationErrorException(errorCode.getHTTPErrorCode(),
                                                          this.getClass().getName(),
                                                          methodName,
                                                          errorMessage,
                                                          errorCode.getSystemAction(),
                                                          errorCode.getUserAction());
            }

            /*
             * Set up the event mapper connection in the local repository config
             */
            localRepositoryConfig.setEventMapperConnection(connection);

            this.setLocalRepositoryConfig(userId, serverName, localRepositoryConfig);
        }
        catch (OMAGConfigurationErrorException  error)
        {
            exceptionHandler.captureConfigurationErrorException(response, error);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Provide the connection to the local repository's event mapper if needed.  The default value is null which
     * means no event mapper.  An event mapper is needed if the local repository has additional APIs that can change
     * the metadata in the repository without going through the open metadata and governance services.
     *
     * @param userId                      user that is issuing the request.
     * @param serverName                  local server name.
     * @param connectorProvider           Java class name of the connector provider for the OMRS repository event mapper.
     * @param eventSource                 topic name or URL to the native event source.
     * @param additionalProperties        additional properties for the event mapper connection
     * @return void response or
     * OMAGNotAuthorizedException  the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryEventMapper parameter or
     * OMAGConfigurationErrorException the event bus is not set.
     */
    public VoidResponse setLocalRepositoryEventMapper(String              userId,
                                                      String              serverName,
                                                      String              connectorProvider,
                                                      String              eventSource,
                                                      Map<String, Object> additionalProperties)
    {
        final String methodName = "setLocalRepositoryEventMapper";

        VoidResponse response = new VoidResponse();

        try
        {
            ConnectorConfigurationFactory connectorConfigurationFactory = new ConnectorConfigurationFactory();

            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            this.setLocalRepositoryEventMapper(userId,
                                               serverName,
                                               connectorConfigurationFactory.getRepositoryEventMapperConnection(serverName,
                                                                                                                connectorProvider,
                                                                                                                additionalProperties,
                                                                                                                eventSource));
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set up the local metadata collection name.  If this is not set then the default value is the
     * local server name.
     *
     * @param userId                      user that is issuing the request.
     * @param serverName                  local server name.
     * @param localMetadataCollectionName metadata collection name
     * @return void response or
     * OMAGNotAuthorizedException  the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or name parameter or
     * OMAGConfigurationErrorException the event bus is not set.
     */
    public VoidResponse setLocalMetadataCollectionName(String               userId,
                                                       String               serverName,
                                                       String               localMetadataCollectionName)
    {
        final String methodName = "setLocalMetadataCollectionName";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);
            errorHandler.validateMetadataCollectionName(localMetadataCollectionName, serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            RepositoryServicesConfig repositoryServicesConfig = serverConfig.getRepositoryServicesConfig();
            LocalRepositoryConfig    localRepositoryConfig    = null;

            /*
             * Extract any existing local repository configuration
             */
            if (repositoryServicesConfig != null)
            {
                localRepositoryConfig = repositoryServicesConfig.getLocalRepositoryConfig();
            }

            /*
             * The local repository should be partially configured already by setLocalRepositoryMode()
             */
            if (localRepositoryConfig == null)
            {
                OMAGAdminErrorCode errorCode    = OMAGAdminErrorCode.LOCAL_REPOSITORY_MODE_NOT_SET;
                String             errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(serverName);

                throw new OMAGConfigurationErrorException(errorCode.getHTTPErrorCode(),
                                                          this.getClass().getName(),
                                                          methodName,
                                                          errorMessage,
                                                          errorCode.getSystemAction(),
                                                          errorCode.getUserAction());
            }

            /*
             * Set up the metadata collection name in the local repository config and save.
             */
            localRepositoryConfig.setMetadataCollectionName(localMetadataCollectionName);

            this.setLocalRepositoryConfig(userId, serverName, localRepositoryConfig);
        }
        catch (OMAGConfigurationErrorException  error)
        {
            exceptionHandler.captureConfigurationErrorException(response, error);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Enable registration of server to an open metadata repository cohort.  This is a group of open metadata
     * repositories that are sharing metadata.  An OMAG server can connect to zero, one or more cohorts.
     * Each cohort needs a unique name.  The members of the cohort use a shared topic to exchange registration
     * information and events related to changes in their supported metadata types and instances.
     * They are also able to query each other's metadata directly through REST calls.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param cohortName  name of the cohort.
     * @param configurationProperties additional properties for the cohort
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName, cohortName or serviceMode parameter.
     * OMAGConfigurationErrorException the event bus is not set.
     */
    public VoidResponse enableCohortRegistration(String               userId,
                                                 String               serverName,
                                                 String               cohortName,
                                                 Map<String, Object>  configurationProperties)
    {
        final String methodName = "enableCohortRegistration";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);
            errorHandler.validateCohortName(cohortName, serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            EventBusConfig   eventBusConfig  = errorHandler.validateEventBusIsSet(serverName, serverConfig, methodName);

            /*
             * Set up a new cohort
             */
            OMRSConfigurationFactory configurationFactory = new OMRSConfigurationFactory();

            CohortConfig newCohortConfig = configurationFactory.getDefaultCohortConfig(serverConfig.getLocalServerName(),
                                                                                       cohortName,
                                                                                       configurationProperties,
                                                                                       eventBusConfig.getConnectorProvider(),
                                                                                       eventBusConfig.getTopicURLRoot(),
                                                                                       serverConfig.getLocalServerId(),
                                                                                       eventBusConfig.getConfigurationProperties());


            this.setCohortConfig(userId, serverName, cohortName, newCohortConfig);
        }
        catch (OMAGConfigurationErrorException  error)
        {
            exceptionHandler.captureConfigurationErrorException(response, error);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set up the mode for an open metadata repository cohort.  This is a group of open metadata repositories that
     * are sharing metadata.  An OMAG server can connect to zero, one or more cohorts.  Each cohort needs
     * a unique name.  The members of the cohort use a shared topic to exchange registration information and
     * events related to changes in their supported metadata types and instances.  They are also able to
     * query each other's metadata directly through REST calls.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param cohortName  name of the cohort.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName, cohortName or serviceMode parameter.
     */
    public VoidResponse disableCohortRegistration(String          userId,
                                                  String          serverName,
                                                  String          cohortName)
    {
        final String methodName = "setCohortMode";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);
            errorHandler.validateCohortName(cohortName, serverName, methodName);

            this.setCohortConfig(userId, serverName, cohortName, null);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /*
     * =============================================================
     * Configure server - advanced options overriding defaults
     */


    /**
     * Set up the list of audit log destinations.  These destinations are expressed as Connection objects
     * to the connectors that will handle the audit log records.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param auditLogDestinations list of connection objects
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName.
     */
    public VoidResponse setAuditLogDestinations(String                userId,
                                                String                serverName,
                                                List<Connection>      auditLogDestinations)
    {
        final String methodName = "setAuditLogDestinations";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);
            List<String>  configAuditTrail  = serverConfig.getAuditTrail();

            if (configAuditTrail == null)
            {
                configAuditTrail = new ArrayList<>();
            }

            if (auditLogDestinations == null)
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " setting up no audit log destinations.");
            }
            else
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " updated list of audit log destinations.");
            }

            serverConfig.setAuditTrail(configAuditTrail);

            RepositoryServicesConfig repositoryServicesConfig = serverConfig.getRepositoryServicesConfig();

            /*
             * Set up the local repository config in the open metadata repository services config.
             */
            if (repositoryServicesConfig != null)
            {
                repositoryServicesConfig.setAuditLogConnections(auditLogDestinations);
            }
            else if (auditLogDestinations != null)
            {
                OMRSConfigurationFactory configurationFactory     = new OMRSConfigurationFactory();

                repositoryServicesConfig = configurationFactory.getDefaultRepositoryServicesConfig(serverConfig.getLocalServerName());
                repositoryServicesConfig.setAuditLogConnections(auditLogDestinations);
            }

            /*
             * Save the open metadata repository services config in the server's config
             */
            serverConfig.setRepositoryServicesConfig(repositoryServicesConfig);
            configStore.saveServerConfig(serverName, methodName, serverConfig);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set up the list of open metadata archives.  These are open metadata types and instances that are loaded at
     * repository start up.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param openMetadataArchives list of connection objects
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName.
     */
    public VoidResponse setOpenMetadataArchives(String                userId,
                                                String                serverName,
                                                List<Connection>      openMetadataArchives)
    {
        final String methodName = "setOpenMetadataArchives";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);
            List<String>  configAuditTrail  = serverConfig.getAuditTrail();

            if (configAuditTrail == null)
            {
                configAuditTrail = new ArrayList<>();
            }

            if (openMetadataArchives == null)
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " clearing open metadata archives.");
            }
            else
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " updated list of open metadata archives loaded at server start up.");
            }

            serverConfig.setAuditTrail(configAuditTrail);

            RepositoryServicesConfig repositoryServicesConfig = serverConfig.getRepositoryServicesConfig();

            /*
             * Set up the local repository config in the open metadata repository services config.
             */
            if (repositoryServicesConfig != null)
            {
                repositoryServicesConfig.setOpenMetadataArchiveConnections(openMetadataArchives);
            }
            else if (openMetadataArchives != null)
            {
                OMRSConfigurationFactory configurationFactory     = new OMRSConfigurationFactory();

                repositoryServicesConfig = configurationFactory.getDefaultRepositoryServicesConfig(serverConfig.getLocalServerName());
                repositoryServicesConfig.setOpenMetadataArchiveConnections(openMetadataArchives);
            }

            /*
             * Save the open metadata repository services config in the server's config
             */
            serverConfig.setRepositoryServicesConfig(repositoryServicesConfig);
            configStore.saveServerConfig(serverName, methodName, serverConfig);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set up the configuration for the local repository.  This overrides the current values.
     *
     * @param userId  user that is issuing the request.
     * @param serverName  local server name.
     * @param localRepositoryConfig  configuration properties for the local repository.
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or localRepositoryConfig parameter.
     */
    public VoidResponse setLocalRepositoryConfig(String                userId,
                                                 String                serverName,
                                                 LocalRepositoryConfig localRepositoryConfig)
    {
        final String methodName = "setLocalRepositoryConfig";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);
            List<String>  configAuditTrail          = serverConfig.getAuditTrail();

            if (configAuditTrail == null)
            {
                configAuditTrail = new ArrayList<>();
            }

            if (localRepositoryConfig == null)
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " setting up a null local repository.");
            }
            else
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " updated configuration for the local repository.");
            }

            serverConfig.setAuditTrail(configAuditTrail);

            RepositoryServicesConfig repositoryServicesConfig = serverConfig.getRepositoryServicesConfig();

            /*
             * Set up the local repository config in the open metadata repository services config.
             */
            if (repositoryServicesConfig != null)
            {
                repositoryServicesConfig.setLocalRepositoryConfig(localRepositoryConfig);
            }
            else if (localRepositoryConfig != null)
            {
                OMRSConfigurationFactory configurationFactory     = new OMRSConfigurationFactory();

                repositoryServicesConfig = configurationFactory.getDefaultRepositoryServicesConfig(serverConfig.getLocalServerName());
                repositoryServicesConfig.setLocalRepositoryConfig(localRepositoryConfig);
            }

            /*
             * Save the open metadata repository services config in the server's config
             */
            serverConfig.setRepositoryServicesConfig(repositoryServicesConfig);
            configStore.saveServerConfig(serverName, methodName, serverConfig);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set up the configuration properties for a cohort.  This may reconfigure an existing cohort or create a
     * cohort.  Use setCohortMode to delete a cohort.
     *
     * @param userId  user that is issuing the request
     * @param serverName  local server name
     * @param cohortName  name of the cohort
     * @param cohortConfig  configuration for the cohort
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName, cohortName or cohortConfig parameter.
     */
    public VoidResponse setCohortConfig(String       userId,
                                        String       serverName,
                                        String       cohortName,
                                        CohortConfig cohortConfig)
    {
        final String methodName = "setCohortConfig";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);
            errorHandler.validateCohortName(cohortName, serverName, methodName);

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);
            OMRSConfigurationFactory configurationFactory = new OMRSConfigurationFactory();
            RepositoryServicesConfig repositoryServicesConfig = serverConfig.getRepositoryServicesConfig();
            List<CohortConfig>       existingCohortConfigs = null;
            List<CohortConfig>       newCohortConfigs = new ArrayList<>();

            List<String>  configAuditTrail          = serverConfig.getAuditTrail();

            if (configAuditTrail == null)
            {
                configAuditTrail = new ArrayList<>();
            }

            if (cohortConfig == null)
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " removed configuration for cohort " + cohortName + ".");
            }
            else
            {
                configAuditTrail.add(new Date().toString() + " " + userId + " updated configuration for cohort " + cohortName + ".");
            }

            serverConfig.setAuditTrail(configAuditTrail);

            /*
             * Extract any existing local repository configuration
             */
            if (repositoryServicesConfig != null)
            {
                existingCohortConfigs = repositoryServicesConfig.getCohortConfigList();
            }

            /*
             * Transfer the cohort configurations of all other cohorts into the new cohort list
             */
            if (existingCohortConfigs != null)
            {
                /*
                 * If there is already a cohort of the same name then effectively remove it.
                 */
                for (CohortConfig existingCohort : existingCohortConfigs)
                {
                    if (existingCohort != null)
                    {
                        String existingCohortName = existingCohort.getCohortName();

                        if (! cohortName.equals(existingCohortName))
                        {
                            newCohortConfigs.add(existingCohort);
                        }
                    }
                }
            }

            /*
             * Add the new cohort to the list of cohorts
             */
            if (cohortConfig != null)
            {
                newCohortConfigs.add(cohortConfig);
            }

            /*
             * If there are no cohorts to save then remove the array list.
             */
            if (newCohortConfigs.isEmpty())
            {
                newCohortConfigs = null;
            }

            /*
             * Add the cohort list to the open metadata repository services config
             */
            if (repositoryServicesConfig != null)
            {
                repositoryServicesConfig.setCohortConfigList(newCohortConfigs);
            }
            else if (newCohortConfigs != null)
            {
                repositoryServicesConfig = configurationFactory.getDefaultRepositoryServicesConfig(serverConfig.getLocalServerName());

                repositoryServicesConfig.setCohortConfigList(newCohortConfigs);
            }

            serverConfig.setRepositoryServicesConfig(repositoryServicesConfig);
            configStore.saveServerConfig(serverName, methodName, serverConfig);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /**
     * Set up the configuration properties for an OMAG Server in a single command.
     *
     * @param userId  user that is issuing the request
     * @param serverName  local server name
     * @param omagServerConfig  configuration for the server
     * @return void response or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName or OMAGServerConfig parameter.
     */
    public VoidResponse setOMAGServerConfig(String           userId,
                                            String           serverName,
                                            OMAGServerConfig omagServerConfig)
    {
        final String methodName = "setOMAGServerConfig";

        VoidResponse response = new VoidResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            if (omagServerConfig == null)
            {
                OMAGAdminErrorCode errorCode    = OMAGAdminErrorCode.NULL_SERVER_CONFIG;
                String             errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(serverName);

                throw new OMAGInvalidParameterException(errorCode.getHTTPErrorCode(),
                                                        this.getClass().getName(),
                                                        methodName,
                                                        errorMessage,
                                                        errorCode.getSystemAction(),
                                                        errorCode.getUserAction());
            }

            OMAGServerConfig serverConfig = configStore.getServerConfig(userId, serverName, methodName);

            List<String>  configAuditTrail          = serverConfig.getAuditTrail();

            if (configAuditTrail == null)
            {
                configAuditTrail = new ArrayList<>();
            }

            configAuditTrail.add(new Date().toString() + " " + userId + " replaced configuration for server.");

            omagServerConfig.setAuditTrail(configAuditTrail);

            configStore.saveServerConfig(serverName, methodName, omagServerConfig);
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }


    /*
     * =============================================================
     * Query current configuration
     */

    /**
     * Return the complete set of configuration properties in use by the server.
     *
     * @param userId  user that is issuing the request
     * @param serverName  local server name
     * @return OMAGServerConfig properties or
     * OMAGNotAuthorizedException the supplied userId is not authorized to issue this command or
     * OMAGInvalidParameterException invalid serverName parameter.
     */
    public OMAGServerConfigResponse getStoredConfiguration(String userId,
                                                           String serverName)
    {
        final String methodName = "getStoredConfiguration";

        OMAGServerConfigResponse response = new OMAGServerConfigResponse();

        try
        {
            errorHandler.validateServerName(serverName, methodName);
            errorHandler.validateUserId(userId, serverName, methodName);

            response.setOMAGServerConfig(configStore.getServerConfig(userId, serverName, methodName));
        }
        catch (OMAGInvalidParameterException error)
        {
            exceptionHandler.captureInvalidParameterException(response, error);
        }
        catch (OMAGNotAuthorizedException error)
        {
            exceptionHandler.captureNotAuthorizedException(response, error);
        }
        catch (Throwable  error)
        {
            exceptionHandler.captureRuntimeException(serverName, methodName, response, error);
        }

        return response;
    }
}
