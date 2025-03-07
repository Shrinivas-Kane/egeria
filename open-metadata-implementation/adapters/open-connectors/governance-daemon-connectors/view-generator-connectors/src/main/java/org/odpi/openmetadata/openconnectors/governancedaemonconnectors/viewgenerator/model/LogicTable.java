/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.openconnectors.governancedaemonconnectors.viewgenerator.model;

import java.util.Map;
import java.util.Objects;

/**
 * LogicTable is a POJO which stores Logical Table information in Gaian
 */
public class LogicTable {

    private String logicalTableName;
    private String gaianNode;
    private Map<String, String> logicalTableDefinition;

    /**
     * Provide the content of the logical table
     * @return content of logical table
     */
    public Map<String, String> getLogicalTableDefinition() {
        return logicalTableDefinition;
    }

    /**
     *  Set the content of the logical table
     * @param logicalTableDefinition Map<String, String>
     */
    public void setLogicalTableDefinition(Map<String, String> logicalTableDefinition) {
        this.logicalTableDefinition = logicalTableDefinition;
    }

    /**
     * Provide the node name
     * @return Node name
     */
    public String getGaianNode() {
        return gaianNode;
    }

    /**
     * Set the node name
     * @param gaianNode String
     */
    public void setGaianNode(String gaianNode) {
        this.gaianNode = gaianNode;
    }


    /**
     * Provide the logical table name
     * @return logical table name
     */
    public String getLogicalTableName() {
        return logicalTableName;
    }

    /**
     * Set the logical table name
     * @param logicalTableName String
     */
    public void setLogicalTableName(String logicalTableName) {
        this.logicalTableName = logicalTableName;
    }

    /**
     * Compare object
     * @param o object to compare
     * @return boolean if the objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogicTable)) return false;
        LogicTable that = (LogicTable) o;
        return Objects.equals(getLogicalTableName(), that.getLogicalTableName()) &&
                Objects.equals(getGaianNode(), that.getGaianNode()) &&
                Objects.equals(getLogicalTableDefinition(), that.getLogicalTableDefinition());
    }

    /**
     * Hash
     * @return hash value of the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(getLogicalTableName(), getGaianNode(), getLogicalTableDefinition());
    }
}
