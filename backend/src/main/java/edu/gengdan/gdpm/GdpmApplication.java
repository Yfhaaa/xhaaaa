package edu.gengdan.gdpm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 启动毕业设计全过程管理，注册持久化映射和每日截止提醒。 */
@SpringBootApplication
@EnableScheduling
@MapperScan("edu.gengdan.gdpm.mapper")
public class GdpmApplication {

    /** 启动毕设服务、鉴权、数据库迁移与每日截止提醒任务。 */
    public static void main(String[] args) {
        SpringApplication.run(GdpmApplication.class, args);
    }
}
