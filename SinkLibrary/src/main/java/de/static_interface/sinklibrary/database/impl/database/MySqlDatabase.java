/*
 * Copyright (c) 2013 - 2015 http://static-interface.de and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.static_interface.sinklibrary.database.impl.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.static_interface.sinklibrary.database.Database;
import de.static_interface.sinklibrary.database.DatabaseConfiguration;
import org.bukkit.plugin.Plugin;
import org.jooq.SQLDialect;

import java.sql.SQLException;

public class MySqlDatabase extends Database {

    public MySqlDatabase(DatabaseConfiguration config, Plugin plugin) {
        super(config, plugin, SQLDialect.MYSQL, '`');
    }

    @Override
    public void setupConfig() {
        HikariConfig hConfig = new HikariConfig();
        hConfig.setMaximumPoolSize(10);
        /*
                We use the MariaDB driver for MySQL connections, because bukkit itself and some other plugins use older versions of the MySQL JDBC driver
            which result in MethodNotFoundExceptions or AbstractMethodErrors. Updating the driver itself is hard and may not work on all servers, so we use this simple solution
            to get the latest and up-to-date driver.
                There should be no problems, since MariaDB is a compatible fork of MySQL

                Description on the driver homepage: "MariaDB Connector/J is a Type 4 JDBC driver. It was developed specifically as a lightweight JDBC connector
            for use with MySQL and MariaDB database servers. It's originally based on the Drizzle JDBC code, and with a lot of additions and bug fixes."
         */
        hConfig.setDataSourceClassName("org.mariadb.jdbc.MySQLDataSource");
        hConfig.addDataSourceProperty("serverName", getConfig().getAddress());
        hConfig.addDataSourceProperty("port", getConfig().getPort());
        hConfig.addDataSourceProperty("databaseName", getConfig().getDatabaseName());
        hConfig.addDataSourceProperty("user", getConfig().getUsername());
        hConfig.addDataSourceProperty("password", getConfig().getPassword());
        hConfig.setConnectionTimeout(5000);
        dataSource = new HikariDataSource(hConfig);
    }

    @Override
    public void connect() throws SQLException {
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            dataSource.close();
            throw e;
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }

        if (dataSource != null) {
            dataSource.close();
        }
    }
}