package edu.gengdan.gdpm.config;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import edu.gengdan.gdpm.entity.FileRecord;
import edu.gengdan.gdpm.mapper.FileRecordMapper;
import edu.gengdan.gdpm.mapper.UserMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

/** 仅空库导入演示 SQL，正常启动与自测不覆盖既有数据。 */
@Component
public class DemoDataInitializer implements ApplicationRunner {

    private final UserMapper users;
    private final FileRecordMapper files;
    private final DataSource source;

    @Value("${app.upload-dir}")
    private String dir;

    public DemoDataInitializer(UserMapper users, FileRecordMapper files, DataSource source) {
        this.users = users;
        this.files = files;
        this.source = source;
    }

    /** 初始化空库演示数据与材料文件。 */
    public void run(ApplicationArguments args) throws Exception {
        if (users.selectCount(null) == 0) {
            try (Connection connection = source.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    ScriptUtils.executeSqlScript(
                        connection,
                        new org.springframework.core.io.support.EncodedResource(
                            new ClassPathResource("db/data.sql"),
                            StandardCharsets.UTF_8
                        )
                    );
                    connection.commit();
                } catch (Exception e) {
                    connection.rollback();
                    throw e;
                }
            }
        }
        Path root = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(root);
        for (int id = 5; id <= 7; id++) {
            Path path = root.resolve("demo-" + id + ".txt");
            if (!Files.exists(path)) {
                Files.writeString(
                    path,
                    "毕业设计全过程管理系统\n演示过程材料：任务书、开题、中检、论文。\n",
                    StandardCharsets.UTF_8
                );
            }
            files.update(
                null,
                new LambdaUpdateWrapper<FileRecord>()
                    .eq(FileRecord::getStoredName, path.getFileName().toString())
                    .set(FileRecord::getSizeBytes, Files.size(path))
            );
        }
    }
}
