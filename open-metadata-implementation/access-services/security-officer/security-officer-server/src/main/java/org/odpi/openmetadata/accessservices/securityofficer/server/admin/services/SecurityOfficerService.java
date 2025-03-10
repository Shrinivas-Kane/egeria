/*
 *  SPDX-License-Identifier: Apache-2.0
 *  Copyright Contributors to the ODPi Egeria project.
 */
package org.odpi.openmetadata.accessservices.securityofficer.server.admin.services;

import org.odpi.openmetadata.accessservices.securityofficer.api.ffdc.exceptions.PropertyServerException;
import org.odpi.openmetadata.accessservices.securityofficer.api.model.SecurityClassification;
import org.odpi.openmetadata.accessservices.securityofficer.api.model.rest.SecurityOfficerSchemaElementListResponse;
import org.odpi.openmetadata.accessservices.securityofficer.api.model.rest.SecurityOfficerSecurityTagListResponse;
import org.odpi.openmetadata.accessservices.securityofficer.api.model.rest.SecurityOfficerSecurityTagResponse;
import org.odpi.openmetadata.accessservices.securityofficer.server.admin.utils.ExceptionHandler;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.ClassificationErrorException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.EntityNotKnownException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.EntityProxyOnlyException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.FunctionNotSupportedException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.InvalidParameterException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.PagingErrorException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.PropertyErrorException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.RepositoryErrorException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.TypeDefNotKnownException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.TypeErrorException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.UserNotAuthorizedException;

/**
 * The SecurityOfficerService provides the server-side implementation of the Security Officer Open Metadata Assess Service (OMAS).
 */
public class SecurityOfficerService {

    private static SecurityOfficerInstanceHandler instanceHandler = new SecurityOfficerInstanceHandler();
    private ExceptionHandler exceptionHandler = new ExceptionHandler();

    public SecurityOfficerSecurityTagResponse getSecurityTagBySchemaElementId(String serverName, String userId, String schemaElementId) {
        SecurityOfficerSecurityTagResponse response = new SecurityOfficerSecurityTagResponse();

        try {
            response.setSecurityTag(instanceHandler.getSecurityTagBySchemaElementId(serverName, userId, schemaElementId));
        } catch (UserNotAuthorizedException | EntityNotKnownException | InvalidParameterException | EntityProxyOnlyException | RepositoryErrorException e) {
            exceptionHandler.captureOMRSException(response, e);
        } catch (PropertyServerException e) {
            exceptionHandler.captureCheckedException(response, e, e.getClass().getName());
        }

        return response;
    }

    public SecurityOfficerSecurityTagListResponse getSecurityTags(String serverName, String userId) {
        SecurityOfficerSecurityTagListResponse response = new SecurityOfficerSecurityTagListResponse();
        try {
            response.setSecurityTags(instanceHandler.getAvailableSecurityTags(serverName, userId));
        } catch (RepositoryErrorException | InvalidParameterException | PropertyErrorException | PagingErrorException | ClassificationErrorException | FunctionNotSupportedException | TypeErrorException | UserNotAuthorizedException | TypeDefNotKnownException e) {
            exceptionHandler.captureOMRSException(response, e);
        } catch (PropertyServerException e) {
            exceptionHandler.captureCheckedException(response, e, e.getClass().getName());
        }
        return response;
    }

    public SecurityOfficerSchemaElementListResponse updateSecurityTag(String serverName, String userId, String schemaElementId, SecurityClassification securityClassification) {
        SecurityOfficerSchemaElementListResponse response = new SecurityOfficerSchemaElementListResponse();

        try {
            response.setSchemaElementEntityList(instanceHandler.updateSecurityTagBySchemaElementId(serverName, userId, schemaElementId, securityClassification));
        } catch (UserNotAuthorizedException | RepositoryErrorException | ClassificationErrorException | EntityProxyOnlyException
                | PropertyErrorException | InvalidParameterException | FunctionNotSupportedException | EntityNotKnownException | TypeDefNotKnownException | TypeErrorException | PagingErrorException e) {
            exceptionHandler.captureOMRSException(response, e);
        } catch (PropertyServerException e) {
            exceptionHandler.captureCheckedException(response, e, e.getClass().getName());
        }

        return response;
    }

}