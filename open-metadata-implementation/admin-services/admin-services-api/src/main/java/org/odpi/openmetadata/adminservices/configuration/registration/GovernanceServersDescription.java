/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adminservices.configuration.registration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AccessServiceDescription provides a list of registered OMAS services.
 */
public enum GovernanceServersDescription implements Serializable
{
    DISCOVERY_ENGINE_SERVICES        (2000,
                                      "Discovery Engine Services",
                                      "Run automated discovery services",
                                      "https://odpi.github.io/egeria/open-metadata-implementation/governance-servers/discovery-engine-services/"),
    SECURITY_SYNC_SERVICES           (2001,
                                      "Security Sync Services",
                                      "Keep security enforcement engine up-to-date",
                                      "https://odpi.github.io/egeria/open-metadata-implementation/governance-servers/security-sync-services/"),
    STEWARDSHIP_SERVICES             (2002,
                                      "Stewardship Services",
                                      "Run automated stewardship actions",
                                      "https://odpi.github.io/egeria/open-metadata-implementation/governance-servers/stewardship-services/"),
    OPEN_LINEAGE_SERVICES            (2003,
                                      "Open Lineage Services", "Store and query asset lineage",
                                      "https://odpi.github.io/egeria/open-metadata-implementation/governance-servers/open-lineage-services/"),
    VIRTUALIZATION_SERVICES          (2004,
                                      "Virtualization Services",
                                      "Run virtualization services",
                                      "https://odpi.github.io/egeria/open-metadata-implementation/governance-servers/virtualization-services/"),
    CONFORMANCE_SUITE_SERVICES       (2005,
                                      "Conformance Suite Services",
                                      "Run automated open metadata conformance suite services",
                                      "https://odpi.github.io/egeria/open-metadata-conformance-suite/"),
    SECURITY_OFFICER_SERVICES        (2006,
                                      "Security Officer Services",
                                      "Manage security tags up-to-date",
                                      "https://odpi.github.io/egeria/open-metadata-implementation/governance-servers/security-officer-services/"),
    DATA_ENGINE_PROXY_SERVICES       (2007,
            "Data Engine Proxy Services",
            "Integrate Data Engines that are not self-capable of integrating to the Data Engine OMAS",
            "https://odpi.github.io/egeria/open-metadata-implementation/governance-servers/data-engine-proxy-services/");


    private static final long     serialVersionUID    = 1L;

    private int    serviceCode;
    private String serviceName;
    private String serviceDescription;
    private String serviceWiki;


    /**
     * Return a list containing each of the access service descriptions defined in this enum class.
     *
     * @return List of enums
     */
    public static List<GovernanceServersDescription> getGovernanceServersDescriptionList()
    {
        List<GovernanceServersDescription> serviceDescriptionList = new ArrayList<>();

        serviceDescriptionList.add(GovernanceServersDescription.DISCOVERY_ENGINE_SERVICES);
        serviceDescriptionList.add(GovernanceServersDescription.SECURITY_SYNC_SERVICES);
        serviceDescriptionList.add(GovernanceServersDescription.STEWARDSHIP_SERVICES);
        serviceDescriptionList.add(GovernanceServersDescription.OPEN_LINEAGE_SERVICES);
        serviceDescriptionList.add(GovernanceServersDescription.VIRTUALIZATION_SERVICES);
        serviceDescriptionList.add(GovernanceServersDescription.CONFORMANCE_SUITE_SERVICES);
        serviceDescriptionList.add(GovernanceServersDescription.SECURITY_OFFICER_SERVICES);
        serviceDescriptionList.add(GovernanceServersDescription.DATA_ENGINE_PROXY_SERVICES);

        return serviceDescriptionList;
    }


    /**
     * Default Constructor
     *
     * @param serviceCode ordinal for this access service
     * @param serviceName symbolic name for this access service
     * @param serviceDescription short description for this access service
     * @param serviceWiki wiki page for the access service for this access service
     */
    GovernanceServersDescription(int    serviceCode,
                                 String serviceName,
                                 String serviceDescription,
                                 String serviceWiki)
    {
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.serviceWiki = serviceWiki;
    }


    /**
     * Return the code for this enum instance
     *
     * @return int type code
     */
    public int getServiceCode()
    {
        return serviceCode;
    }


    /**
     * Return the default name for this enum instance.
     *
     * @return String default name
     */
    public String getServiceName()
    {
        return serviceName;
    }


    /**
     * Return the default description for the type for this enum instance.
     *
     * @return String default description
     */
    public String getServiceDescription()
    {
        return serviceDescription;
    }


    /**
     * Return the URL for the wiki page describing this access service.
     *
     * @return String URL name for the wiki page
     */
    public String getServiceWiki()
    {
        return serviceWiki;
    }
}
