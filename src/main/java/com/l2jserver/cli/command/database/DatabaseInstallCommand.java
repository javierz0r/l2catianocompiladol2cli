/*
 * Copyright © 2019 L2J Server
 *
 * This file is part of L2J Server.
 *
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.cli.command.database;

import java.io.File;
import java.util.Scanner;

import org.aeonbits.owner.Mutable;

import com.l2jserver.cli.command.AbstractCommand;
import com.l2jserver.cli.config.Configuration;
import com.l2jserver.cli.dao.AbstractDatabaseDAO;
import com.l2jserver.cli.dao.GameServerDatabaseDAO;
import com.l2jserver.cli.dao.LoginServerDatabaseDAO;
import com.l2jserver.cli.model.DatabaseInstallType;
import com.l2jserver.cli.model.ServerType;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Database install command.
 * @author Zoey76
 * @version 1.0.0
 */
@Command(name = "install")
public class DatabaseInstallCommand extends AbstractCommand {
	
	@Option(names = {
		"-l",
		"--sql-location"
	}, required = true, description = "Files location")
	private String path;
	
	@Option(names = {
		"-dv",
		"--database-driver"
	}, description = "Database Driver")
	private String driver;
	
	@Option(names = {
		"-url",
		"--database-url"
	}, description = "Database URL")
	private String url;
	
	@Option(names = {
		"-u",
		"--database-user"
	}, description = "Database User")
	private String user;
	
	@Option(names = {
		"-pw",
		"--database-password"
	}, description = "Database Password")
	private String password;
	
	@Option(names = {
		"-pool",
		"--connection-pool"
	}, description = "Connection Pool")
	private String connectionPool;
	
	@Option(names = {
		"-m",
		"--mode"
	}, required = true, description = "Database installation mode")
	private DatabaseInstallType mode;
	
	@Option(names = {
		"-t",
		"--type"
	}, required = true, description = "Server Type")
	private ServerType serverType;
	
	@Override
	public void run() {
		// Validate files exists
		final var sqlPath = new File(path);
		if (!sqlPath.exists()) {
			System.err.println("The path does not exist!");
			return;
		}
		
		final AbstractDatabaseDAO databaseDAO = databaseDAO();
		
		databaseDAO.createDump();
		
		databaseDAO.updates(mode, sqlPath);
		
		databaseDAO.basic(sqlPath);
		
		System.out.print("Install custom tables? (y/N): ");
		try (var s = new Scanner(FILTER_INPUT_STREAM)) {
			if (YES.equalsIgnoreCase(s.next())) {
				databaseDAO.custom(sqlPath);
			}
		}
		
		System.out.print("Install mod tables? (y/N): ");
		try (var s = new Scanner(FILTER_INPUT_STREAM)) {
			if (YES.equalsIgnoreCase(s.next())) {
				databaseDAO.mods(sqlPath);
			}
		}
		
		System.out.println("Database installation complete.");
	}
	
	private AbstractDatabaseDAO databaseDAO() {
		switch (serverType) {
			case GAME: {
				overrideConfigs(Configuration.gameServer());
				return new GameServerDatabaseDAO();
			}
			default:
			case LOGIN: {
				overrideConfigs(Configuration.loginServer());
				return new LoginServerDatabaseDAO();
			}
		}
	}
	
	private void overrideConfigs(Mutable databaseConfiguration) {
		if (driver != null) {
			databaseConfiguration.setProperty("DatabaseDriver", driver);
		}
		
		if (url != null) {
			databaseConfiguration.setProperty("DatabaseURL", url);
		}
		
		if (user != null) {
			databaseConfiguration.setProperty("DatabaseUser", user);
		}
		
		if (password != null) {
			databaseConfiguration.setProperty("DatabasePassword", password);
		}
		
		if (connectionPool != null) {
			databaseConfiguration.setProperty("DatabaseConnectionPool", connectionPool);
		}
	}
}
