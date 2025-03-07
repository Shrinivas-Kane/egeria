/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
/**
 * This is the interface for the generic operations on data virtualization solutions
 */
package org.odpi.openmetadata.openconnectors.governancedaemonconnectors.viewgenerator.derby;

import org.odpi.openmetadata.accessservices.informationview.events.TableContextEvent;
import org.odpi.openmetadata.frameworks.connectors.properties.AdditionalProperties;
import org.odpi.openmetadata.frameworks.connectors.properties.ConnectionProperties;
import org.odpi.openmetadata.frameworks.connectors.properties.EndpointProperties;
import org.odpi.openmetadata.openconnectors.governancedaemonconnectors.viewgenerator.ViewGeneratorConnectorBase;
import org.odpi.openmetadata.openconnectors.governancedaemonconnectors.viewgenerator.derby.auditlog.DerbyConnectorAuditCode;
import org.odpi.openmetadata.openconnectors.governancedaemonconnectors.viewgenerator.model.LogicTable;

import org.odpi.openmetadata.openconnectors.governancedaemonconnectors.viewgenerator.model.MappedColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

import org.odpi.openmetadata.openconnectors.governancedaemonconnectors.viewgenerator.utils.ConnectorUtils;

import static org.odpi.openmetadata.openconnectors.governancedaemonconnectors.viewgenerator.utils.ConnectorUtils.BUSINESS_PREFIX;
import static org.odpi.openmetadata.openconnectors.governancedaemonconnectors.viewgenerator.utils.ConnectorUtils.TECHNICAL_PREFIX;


public class DerbyConnector extends ViewGeneratorConnectorBase {

    private static final Logger log = LoggerFactory.getLogger(DerbyConnector.class);
    private static String databaseUrl;
    private static java.sql.Connection derbyConnection;
    private static java.sql.Statement derbyStatement;
    private static DerbyConnectorAuditCode auditCode;
    /*
     * Variables fpr building the connection to the Derby database
     */
    private String serverAddress;
    private String username;
    private String password;
    private String isCreate;
    private String databaseName;
    private int timeoutInSecond;
    private String gdbNode;
    private String logicTableName;
    private String logicTableDefinition;
    private String getLogicTablesQuery;
    private String gaianFrontendName;

    /**
     * Initialize the connector.
     *
     * @param connectorInstanceId  - unique id for the connector instance - useful for messages etc
     * @param connectionProperties - POJO for the configuration used to create the connector.
     */
    @Override
    public void initialize(String connectorInstanceId, ConnectionProperties connectionProperties) {

        final String actionDescription = "initialize";

        super.initialize(connectorInstanceId, connectionProperties);

        this.connectorInstanceId = connectorInstanceId;
        this.connectionProperties = connectionProperties;

        AdditionalProperties additionalProperties = connectionProperties.getAdditionalProperties();
        EndpointProperties endpoint = connectionProperties.getEndpoint();

        if (omrsAuditLog != null) {
            auditCode = DerbyConnectorAuditCode.CONNECTOR_INITIALIZING;
            omrsAuditLog.logRecord(actionDescription,
                    auditCode.getLogMessageId(),
                    auditCode.getSeverity(),
                    auditCode.getFormattedLogMessage(),
                    null,
                    auditCode.getSystemAction(),
                    auditCode.getUserAction());
        }

        if (endpoint != null) {
            serverAddress = endpoint.getAddress();
            AdditionalProperties serverProperty = endpoint.getAdditionalProperties();
            if (serverAddress != null && serverProperty != null) {
                username = connectionProperties.getUserId();
                password = connectionProperties.getClearPassword();
                isCreate = serverProperty.getProperty("create");
                databaseName = additionalProperties.getProperty("databaseName");
                timeoutInSecond = Integer.parseInt(serverProperty.getProperty("timeoutInSecond"));
                databaseUrl = serverAddress + "/" + databaseName +
                        ";create=" + isCreate +
                        ";user=" + username +
                        ";password=" + password +
                        ";proxy-user=" + username +
                        ";proxy-pwd=" + password;
            } else {
                log.error("Errors in the server configuration. The address of the server cannot be extracted");
                if (omrsAuditLog != null) {
                    auditCode = DerbyConnectorAuditCode.CONNERCTOR_SERVER_CONFIGURATION_ERROR;
                    omrsAuditLog.logRecord(actionDescription,
                            auditCode.getLogMessageId(),
                            auditCode.getSeverity(),
                            auditCode.getFormattedLogMessage(),
                            null,
                            auditCode.getSystemAction(),
                            auditCode.getUserAction());
                }
            }
        } else {
            log.error("Errors in server address. The endpoint containing the server address is invalid!");
            if (omrsAuditLog != null) {
                auditCode = DerbyConnectorAuditCode.CONNERCTOR_SERVER_ADDRESS_ERROR;
                omrsAuditLog.logRecord(actionDescription,
                        auditCode.getLogMessageId(),
                        auditCode.getSeverity(),
                        auditCode.getFormattedLogMessage(),
                        null,
                        auditCode.getSystemAction(),
                        auditCode.getUserAction());
            }
        }

        if (additionalProperties != null) {
            logicTableName = additionalProperties.getProperty("logicTableName");
            logicTableDefinition = additionalProperties.getProperty("logicTableDefinition");
            gdbNode = additionalProperties.getProperty("gdbNode");
            getLogicTablesQuery = additionalProperties.getProperty("getLogicTables");
            gaianFrontendName = additionalProperties.getProperty("frontendName");
        } else {
            log.error("Errors in settings of the GaianDB");
            if (omrsAuditLog != null) {
                auditCode = DerbyConnectorAuditCode.CONNERCTOR_LOGICAL_TABLE_ERROR;
                omrsAuditLog.logRecord(actionDescription,
                        auditCode.getLogMessageId(),
                        auditCode.getSeverity(),
                        auditCode.getFormattedLogMessage(),
                        null,
                        auditCode.getSystemAction(),
                        auditCode.getUserAction());
            }
        }

        createDerbyConnection();

        if (derbyConnection != null && omrsAuditLog != null) {
            auditCode = DerbyConnectorAuditCode.CONNECTOR_INITIALIZED;
            omrsAuditLog.logRecord(actionDescription,
                    auditCode.getLogMessageId(),
                    auditCode.getSeverity(),
                    auditCode.getFormattedLogMessage(),
                    null,
                    auditCode.getSystemAction(),
                    auditCode.getUserAction());
        }
    }

    /**
     * Delete a table
     *
     * @param tableName table name
     * @return boolean whether the table is deleted successfully
     */
    @Override
    public boolean deleteLogicalTable(String tableName) {
        final String actionDescription = "deleteLogicalTable";

        try {
            derbyStatement = derbyConnection.createStatement();
            derbyStatement.setQueryTimeout(timeoutInSecond);
            derbyStatement.executeUpdate("call removelt('" + tableName + "')");

            return true;

        } catch (SQLException e) {
            log.error("Error deleting table", e);
            if (omrsAuditLog != null) {
                auditCode = DerbyConnectorAuditCode.CONNERCTOR_QUERY_ERROR;
                omrsAuditLog.logRecord(actionDescription,
                        auditCode.getLogMessageId(),
                        auditCode.getSeverity(),
                        auditCode.getFormattedLogMessage(),
                        null,
                        auditCode.getSystemAction(),
                        auditCode.getUserAction());
            }
            return false;
        }
    }

    @Override
    public List<LogicTable> getAllLogicTables() {
        final String actionDescription = "getAllLogicTables";

        List logicTableList = new ArrayList();

        try {
            derbyStatement = derbyConnection.createStatement();
            derbyStatement.setQueryTimeout(timeoutInSecond);
            ResultSet resultSet = derbyStatement.executeQuery(getLogicTablesQuery);
            while (resultSet.next()) {
                logicTableList.add(extractLogicTableDefinition(resultSet));
            }

        } catch (SQLException e) {
            log.error("Error in getting all the logic tables: ", e);
            if (omrsAuditLog != null) {
                auditCode = DerbyConnectorAuditCode.CONNERCTOR_QUERY_ERROR;
                omrsAuditLog.logRecord(actionDescription,
                        auditCode.getLogMessageId(),
                        auditCode.getSeverity(),
                        auditCode.getFormattedLogMessage(),
                        null,
                        auditCode.getSystemAction(),
                        auditCode.getUserAction());
            }
        }
        return logicTableList;
    }


    public boolean executeCustomizedUpdate(String update) {

        final String actionDescription = "executeCustomizedUpdate: " + update;

        try {
            derbyStatement = derbyConnection.createStatement();
            derbyStatement.setQueryTimeout(timeoutInSecond);
            derbyStatement.executeUpdate(update);
            return true;
        } catch (SQLException e) {
            log.error("Error in executing a customized update!", e);
            if (omrsAuditLog != null) {
                auditCode = DerbyConnectorAuditCode.CONNERCTOR_QUERY_ERROR;
                omrsAuditLog.logRecord(actionDescription,
                        auditCode.getLogMessageId(),
                        auditCode.getSeverity(),
                        auditCode.getFormattedLogMessage(),
                        null,
                        auditCode.getSystemAction(),
                        auditCode.getUserAction());
            }
            return false;
        }
    }

    /**
     * Process the serialized  information view event
     *
     * @param tableContextEvent event
     * @return the table sent to Gaian
     */
    @Override
    public Map<String, String> processInformationViewEvent(TableContextEvent tableContextEvent) {
        /*Do Nothing*/
        final String actionDescription = "processInformationViewTopic";
        if (tableContextEvent == null) {
            log.debug("Object TableContextEvent is null");
            if (omrsAuditLog != null) {
                auditCode = DerbyConnectorAuditCode.CONNERCTOR_IV_EVENT_ERROR;
                omrsAuditLog.logRecord(actionDescription,
                        auditCode.getLogMessageId(),
                        auditCode.getSeverity(),
                        auditCode.getFormattedLogMessage(),
                        null,
                        auditCode.getSystemAction(),
                        auditCode.getUserAction());
                return Collections.emptyMap();
            }
        }

        try {
            String gaianNodeName = tableContextEvent.getTableSource().getDatabaseSource().getEndpointSource().getNetworkAddress().replace(".", "").toLowerCase();
            String technicalTableName = ConnectorUtils.getLogicTableName(TECHNICAL_PREFIX, tableContextEvent, gaianNodeName);
            String businessTableName = ConnectorUtils.getLogicTableName(BUSINESS_PREFIX, tableContextEvent, gaianNodeName);
            String logicalTableName = ConnectorUtils.getLogicTableName(ConnectorUtils.GENERAL, tableContextEvent, gaianNodeName);
            List<MappedColumn> mappedColumns = ConnectorUtils.getMappedColumns(tableContextEvent);

            if (mappedColumns == null || mappedColumns.isEmpty()){
                log.info("There are no business term associations to columns in the received event, removing existing definitions");
                if (getMatchingTables(gaianNodeName, Arrays.asList(businessTableName, technicalTableName)) != null){
                    deleteLogicalTable(businessTableName);
                    deleteLogicalTable(technicalTableName);
                }
            }
            else {
                return createTableDefinitions(tableContextEvent, gaianNodeName, technicalTableName, businessTableName, logicalTableName, mappedColumns);
            }
        } catch (Exception e){
            log.error("Unable to process the event.");
        }


        return null;
    }


    /**
     * Establish the connection to database
     */
    private void createDerbyConnection() {

        final String actionDescription = "createDerbyConnection";

        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            //Get a connection
            derbyConnection = DriverManager.getConnection(databaseUrl);
            log.info("The connection to database is successfully established!");
        } catch (Exception e) {
            log.error("Error in creating the connection to derby: ", e);
            if (omrsAuditLog != null) {
                auditCode = DerbyConnectorAuditCode.CONNERCTOR_SERVER_CONNECTION_ERROR;
                omrsAuditLog.logRecord(actionDescription,
                        auditCode.getLogMessageId(),
                        auditCode.getSeverity(),
                        auditCode.getFormattedLogMessage(),
                        null,
                        auditCode.getSystemAction(),
                        auditCode.getUserAction());
            }
        }
    }


    private LogicTable getMatchingTables(String gaianNodeName, List<String> tables) {
        log.debug("gaianNodeName: " + gaianNodeName);
        log.debug("tables to match in gaian: " + tables);
        List<LogicTable> logicTableList = new ArrayList<>();

        logicTableList = getAllLogicTables();

        if (logicTableList != null && !logicTableList.isEmpty()) {
            return logicTableList.stream().filter(e -> (e.getGaianNode().equals(gaianNodeName) && tables.contains(e.getLogicalTableName()))).findFirst().orElse(null);
        }
        return null;
    }


    private LogicTable extractLogicTableDefinition(ResultSet sqlResults) throws SQLException {
        LogicTable logicTable = new LogicTable();
        Map<String, String> defLists = new HashMap<>();
        logicTable.setGaianNode(sqlResults.getString(gdbNode));
        logicTable.setLogicalTableName(sqlResults.getString(logicTableName));
        String def = sqlResults.getString(logicTableDefinition);
        String[] defs = def.split(", ");//here we need to split with ,+space, it is showed in Gaian LTDEF
        for (String column : defs) {
            String[] temp = column.split(" ");
            defLists.put(temp[0], temp[1]);
        }
        logicTable.setLogicalTableDefinition(defLists);
        return logicTable;
    }


    private Map<String, String> createTableDefinitions(TableContextEvent tableContextEvent, String gaianNodeName, String technicalTableName, String businessTableName, String logicalTableName, List<MappedColumn> mappedColumns){
        String methodName = "createTableDefinitions";
        Map<String, String> createdTables = new HashMap();
        LogicTable backendTable = getMatchingTables(gaianNodeName, Collections.singletonList(logicalTableName));
        if (backendTable != null) {
            if (!backendTable.getGaianNode().equals(gaianFrontendName)) {
                createMirroringLogicalTable(logicalTableName, gaianNodeName);
            }
            ConnectorUtils.updateColumnDataType(mappedColumns, backendTable);

            String updatedTable = createTableDefinition(tableContextEvent.getTableSource().getDatabaseSource().getName(), businessTableName, (c -> c.getBusinessName()), mappedColumns, gaianNodeName, logicalTableName);
            if (updatedTable != null) {
                createdTables.put(ConnectorUtils.BUSINESS_PREFIX, updatedTable);
            }

            updatedTable = createTableDefinition(tableContextEvent.getTableSource().getDatabaseSource().getName(), technicalTableName, (c -> c.getTechnicalName()), mappedColumns, gaianNodeName, logicalTableName);
            if (updatedTable != null) {
                createdTables.put(ConnectorUtils.TECHNICAL_PREFIX, updatedTable);
            }

            if (!backendTable.getGaianNode().equals(gaianFrontendName)) {
                log.info("Remove mirrored logical table: {}", logicalTableName);
                deleteLogicalTable(logicalTableName);
            }
            return createdTables;
        } else {
            log.error("error");
            return null;
        }
    }


    /**
     * Set Logical Table mirroring the definition for the given Logical Table Name on another GaianDB node, so its data can be queried remotely.
     *
     * @param logicalTableName
     * @param gaianNodeName
     */
    private void createMirroringLogicalTable(String logicalTableName, String gaianNodeName) {
        log.debug("Set up Logical Table for Gaian node");
        String setLogicalTableForNode = "call setltfornode('" +
                logicalTableName + "','" +
                gaianNodeName + "')";

        executeCustomizedUpdate(setLogicalTableForNode);
    }

    private String createTableDefinition(String databaseName, String tableName, Function<MappedColumn, String> function, List<MappedColumn> mappedColumns, String gaianNodeName, String logicalTableName) {
        String businessTableCreateStatement = buildTableCreateStatement(tableName, mappedColumns, function);
        String setBusinessTableDataSource = buildCreateTableDataSourceStatement(databaseName, tableName, gaianNodeName, mappedColumns, logicalTableName);

        boolean queryStatus = executeCustomizedUpdate(businessTableCreateStatement);
        if (queryStatus){
            queryStatus = executeCustomizedUpdate(setBusinessTableDataSource);
        }

        if (queryStatus) {
            log.debug("Successfully created table {}", tableName);
            return tableName;
        } else {
            log.error("Failed to create table {}" ,tableName);
        }
        return null;
    }

    /**
     *
     * @param tableName name of the table to be created
     * @param mappedColumns columns to be added to table definition
     * @param function to retrieve the value to be used as column name; it is either technical or business name
     * @return
     */
    private String buildTableCreateStatement(String tableName, List<MappedColumn> mappedColumns, Function<MappedColumn, String> function) {
        StringBuilder statement = new StringBuilder("call setlt('" +
                tableName +
                "','");
        for (MappedColumn mappedColumn : mappedColumns) {
            statement.append(function.apply(mappedColumn)).append(" ").append(mappedColumn.getType()).append(",");
        }
        statement = new StringBuilder(statement.substring(0, (statement.length() - 1)));
        statement.append("','')");
        return statement.toString();
    }


    private String buildCreateTableDataSourceStatement(String databaseName, String tableName, String gaianNodeName, List<MappedColumn> mappedColumns, String logicalTableName) {
        String connectionName = gaianNodeName.toUpperCase();
        String statementForCreatingDataSource = "call setdsrdbtable('" +
                tableName +
                "', '', '" +
                connectionName +
                "', '" +
                logicalTableName +
                "','', '";

        for (MappedColumn mappedColumn : mappedColumns) {
            statementForCreatingDataSource += mappedColumn.getTechnicalName() + ",";
        }
        statementForCreatingDataSource = statementForCreatingDataSource.substring(0, (statementForCreatingDataSource.length() - 1));
        statementForCreatingDataSource += "')";
        return statementForCreatingDataSource;
    }

}
