package edu.gengdan.gdpm.config;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

/** 对既有 H2/MySQL 数据库执行幂等增量升级，保留所有业务数据。 */
@Component
@Order(-100)
public class UpgradeMigration implements ApplicationRunner {

    private final DataSource source;

    /** 装配已配置的依赖或已验证的数据，供所属业务使用。 */
    public UpgradeMigration(DataSource source) {
        this.source = source;
    }

    /** 仅创建缺少的表和字段。 */
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = source.getConnection()) {
            ScriptUtils.executeSqlScript(
                connection,
                new EncodedResource(new ClassPathResource("db/schema.sql"), StandardCharsets.UTF_8)
            );
            add(connection, "sys_user", "email", "VARCHAR(120)");
            add(connection, "sys_user", "phone", "VARCHAR(30)");
            add(connection, "workflow_step", "plagiarism_threshold", "DECIMAL(5,2) NOT NULL DEFAULT 30");
        }
    }

    /** 通过元数据判断字段存在性，兼容两套数据库。 */
    private void add(Connection connection, String table, String column, String type) throws Exception {
        try (
            ResultSet columns = connection
                .getMetaData()
                .getColumns(connection.getCatalog(), null, table, column)
        ) {
            if (columns.next()) {
                return;
            }
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
        }
    }
}
