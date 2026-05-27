package com.wps.yundoc.database;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaPolicyTest {

    private static final Pattern INDEX_DEFINITION = Pattern.compile(
            "(?im)^\\s*(?:unique\\s+)?(?:key|index)\\s+[`\\w]+\\s*\\(([^)]*)\\)");

    @Test
    void migrationsDoNotUseAutoIncrementOrLargeFields() throws IOException {
        List<Path> migrations = migrationFiles();

        assertThat(migrations).isNotEmpty();
        for (Path migration : migrations) {
            String sql = readSql(migration);
            assertThat(sql).doesNotContain("auto_increment");
            assertThat(sql).doesNotContain(" text");
            assertThat(sql).doesNotContain(" blob");
            assertThat(sql).doesNotContain(" mediumtext");
            assertThat(sql).doesNotContain(" mediumblob");
            assertThat(sql).doesNotContain(" json");
            assertThat(sql).doesNotContain("auth_mode");
            assertThat(sql).doesNotContain("access_token");
            assertThat(sql).doesNotContain("refresh_token");
        }
    }

    @Test
    void migrationIndexesUseAtMostFiveColumns() throws IOException {
        List<Path> migrations = migrationFiles();

        for (Path migration : migrations) {
            Matcher matcher = INDEX_DEFINITION.matcher(readSql(migration));
            while (matcher.find()) {
                int columns = matcher.group(1).split(",").length;
                assertThat(columns)
                        .as("index column count in %s", migration)
                        .isLessThanOrEqualTo(5);
            }
        }
    }

    @Test
    void bizSystemMigrationContainsOnlyMvpTables() throws IOException {
        String sql = migrationFiles().stream()
                .map(SchemaPolicyTest::readSqlUnchecked)
                .collect(Collectors.joining("\n"));

        assertThat(sql).contains(
                "create table biz_system",
                "create table biz_system_api_permission",
                "primary key (business_system_id)",
                "unique key uk_biz_system_client (client_id)",
                "primary key (business_system_id, api_code)");
    }

    private static List<Path> migrationFiles() throws IOException {
        Path migrationDir = Paths.get("src", "main", "resources", "db", "migration");
        if (!Files.exists(migrationDir)) {
            return java.util.Collections.emptyList();
        }
        try (java.util.stream.Stream<Path> paths = Files.list(migrationDir)) {
            return paths
                    .filter(path -> path.getFileName().toString().endsWith(".sql"))
                    .collect(Collectors.toList());
        }
    }

    private static String readSql(Path migration) throws IOException {
        return new String(Files.readAllBytes(migration), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
    }

    private static String readSqlUnchecked(Path migration) {
        try {
            return readSql(migration);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read migration " + migration, ex);
        }
    }
}
