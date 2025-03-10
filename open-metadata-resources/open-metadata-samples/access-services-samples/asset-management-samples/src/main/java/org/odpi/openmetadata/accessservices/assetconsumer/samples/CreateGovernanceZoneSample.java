/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.assetconsumer.samples;

import org.odpi.openmetadata.accessservices.assetowner.client.AssetOwner;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;


/**
 * CreateGovernanceZoneSample illustrates the use of the Asset Owner OMAS API to create governance zones
 * for Coco Pharmaceuticals.
 */
public class CreateGovernanceZoneSample
{
    private String  serverName;
    private String  serverURLRoot;
    private String  clientUserId;

    private AssetOwner client = null;

    /**
     * Set up the parameters for the sample.
     *
     * @param serverName server to call
     * @param serverURLRoot location of server
     * @param clientUserId userId to access the server
     */
    private CreateGovernanceZoneSample(String  serverName,
                                       String  serverURLRoot,
                                       String  clientUserId)
    {
        this.serverName = serverName;
        this.serverURLRoot = serverURLRoot;
        this.clientUserId = clientUserId;
    }


    /**
     * Set up a new zone
     *
     * @param zoneName qualified name
     * @param displayName display name
     * @param description longer description
     * @param criteria what types of assets are found in this zone
     * @throws InvalidParameterException bad parameters passed to client
     * @throws UserNotAuthorizedException userId is not allowed to create zones
     * @throws PropertyServerException service is not running - or is in trouble
     */
    private void createZone(String     zoneName,
                            String     displayName,
                            String     description,
                            String     criteria) throws InvalidParameterException,
                                                        UserNotAuthorizedException,
                                                        PropertyServerException
    {
        System.out.println("------------------------------------------------------------------------");
        System.out.println(zoneName);
        System.out.println("------------------------------------------------------------------------");
        System.out.println(" ==> qualifiedName: " + zoneName);
        System.out.println(" ==> displayName:   " + displayName);
        System.out.println(" ==> description:   " + description);
        System.out.println(" ==> criteria:      " + criteria);
        System.out.println(" ");

        client.createGovernanceZone(clientUserId,
                                    zoneName,
                                    displayName,
                                    description,
                                    criteria,
                                    null);
    }


    /**
     * This runs the sample
     */
    private void run()
    {
        try
        {
            client = new AssetOwner(serverName, serverURLRoot);

            GovernanceZoneSampleDefinitions[]   zoneSampleDefinitions = GovernanceZoneSampleDefinitions.values();

            for (GovernanceZoneSampleDefinitions zoneDefinition : zoneSampleDefinitions)
            {
                createZone(zoneDefinition.getZoneName(),
                           zoneDefinition.getDisplayName(),
                           zoneDefinition.getDescription(),
                           zoneDefinition.getCriteria());
            }


        }
        catch (Throwable error)
        {
            System.out.println("There was an exception when calling the asset owner client.  Error message is: " + error.getMessage());
        }
    }


    /**
     * Main program that controls the operation of the sample.  The parameters are passed space separated.
     * The file name must be passed as parameter 1.  The other parameters are used to override the
     * sample's default values.
     *
     * @param args 1. file name 2. server name, 3. URL root for the server, 4. client userId
     */
    public static void main(String[] args)
    {
        String  serverName = "cocoMDS1";
        String  serverURLRoot = "http://localhost:8080";
        String  clientUserId = "garygeeke";


        if (args.length > 1)
        {
            serverName = args[1];
        }

        if (args.length > 2)
        {
            serverURLRoot = args[2];
        }

        if (args.length > 3)
        {
            clientUserId = args[3];
        }

        System.out.println("===============================");
        System.out.println("Create Governance Zones Sample   ");
        System.out.println("===============================");
        System.out.println("Running against server: " + serverName + " at " + serverURLRoot);
        System.out.println("Using userId: " + clientUserId);
        System.out.println();

        try
        {
            CreateGovernanceZoneSample sample = new CreateGovernanceZoneSample(serverName, serverURLRoot, clientUserId);

            sample.run();
        }
        catch (Throwable  error)
        {
            System.out.println("Exception: " + error.getClass().getName() + " with message " + error.getMessage());
            System.exit(-1);
        }
    }
}
