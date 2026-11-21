package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;
import static edu.gengdan.gdpm.common.Values.*;

import edu.gengdan.gdpm.common.*;
import edu.gengdan.gdpm.entity.*;
import edu.gengdan.gdpm.mapper.*;
import edu.gengdan.gdpm.service.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** GuidanceService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class GuidanceServiceImpl
    extends DomainSupport<GuidanceRecordMapper, GuidanceRecord>
    implements GuidanceService
{

    /** 查询或新增指导记录。 */
    public Object guidance() {
        auth.permission("guidance:read");
        return reports.guidance(auth.scope());
    }

    /** 查询或新增指导记录。 */
    @OperLog("记录指导")
    @Transactional
    public Object guidance(Map<String, Object> body) {
        auth.roles("TEACHER");
        auth.permission("guidance:write");
        long studentId = Params.number(body, "studentId");
        auth.student(studentId);
        GuidanceRecord record = entity(
            GuidanceRecord.class,
            "student_id",
            studentId,
            "teacher_id",
            auth.uid(),
            "content",
            Params.text(body, "content", 10000),
            "guidance_at",
            Params.time(body, "guidanceAt").toLocalDateTime()
        );
        save(record);
        return Map.of("id", record.getId());
    }
}
