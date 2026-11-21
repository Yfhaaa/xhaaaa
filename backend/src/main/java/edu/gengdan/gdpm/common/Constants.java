package edu.gengdan.gdpm.common;

import java.util.List;

/** 角色、材料阶段和上传限制。 */
public final class Constants {

    public static final String ADMIN = "ADMIN";
    public static final String TEACHER = "TEACHER";
    public static final String STUDENT = "STUDENT";
    public static final List<String> STAGES = List.of("task", "proposal", "midterm", "draft", "final");
    public static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    private Constants() {}
}
