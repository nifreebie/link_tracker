package backend.academy.scrapper.util;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class LiquibaseMigration {

    public static void migrate(Path changeLogPath, String username, String password, String jdbcUrl) {

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            Database database = new PostgresDatabase();
            database.setConnection(new JdbcConnection(conn));

            Path changelogDir = changeLogPath.getParent();
            if (changelogDir == null) {
                log.warn("Change log path '{}' does not have a parent, using current directory", changeLogPath);
                changelogDir = Path.of(".");
            }

            Path fileNamePath = changeLogPath.getFileName();
            if (fileNamePath == null) {
                throw new IllegalArgumentException("Change log path must have a file name: " + changeLogPath);
            }
            String changelogFileName = fileNamePath.toString();

            Map<String, Object> scopeValues = new HashMap<>();
            scopeValues.put(Scope.Attr.resourceAccessor.name(), new DirectoryResourceAccessor(changelogDir.toFile()));

            Scope.child(scopeValues, () -> {
                CommandScope updateCommand = new CommandScope("update");
                updateCommand.addArgumentValue("database", database);
                updateCommand.addArgumentValue("changelogFile", changelogFileName);
                updateCommand.execute();
            });
        } catch (Exception e) {
            log.error("Error while migrating changelog");
        }
    }
}
