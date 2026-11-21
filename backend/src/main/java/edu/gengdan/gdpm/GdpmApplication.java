package edu.gengdan.gdpm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("edu.gengdan.gdpm.mapper")
public class GdpmApplication {

    /** 启动毕业设计管理服务。 */
    public static void main(String[] args) {
        SpringApplication.run(GdpmApplication.class, args);
    }
}
