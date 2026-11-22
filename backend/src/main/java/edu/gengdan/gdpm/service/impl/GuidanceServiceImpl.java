package edu.gengdan.gdpm.service.impl;

import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.dto.GuidanceSaveRequest;
import edu.gengdan.gdpm.entity.GuidanceRecord;
import edu.gengdan.gdpm.mapper.GuidanceRecordMapper;
import edu.gengdan.gdpm.service.GuidanceService;
import edu.gengdan.gdpm.vo.IdVO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 指导教师仅可为本人指导学生新增记录；查询按当前身份隔离。 */
@Service
public class GuidanceServiceImpl
    extends DomainSupport<GuidanceRecordMapper, GuidanceRecord>
    implements GuidanceService
{

    /** 指导教师仅可为本人指导学生新增记录；查询按当前身份隔离。 */
    public List<Map<String, Object>> guidance() {
        auth.permission("guidance:read");
        return reports.guidance(auth.scope());
    }

    /** 指导教师仅可为本人指导学生新增记录；查询按当前身份隔离。 */
    @OperLog("记录指导")
    @Transactional
    public IdVO guidance(GuidanceSaveRequest body) {
        validate(body);
        auth.roles("TEACHER");
        auth.permission("guidance:write");
        long studentId = body.getStudentId();
        auth.student(studentId);
        GuidanceRecord record = new GuidanceRecord();
        record.setStudentId(studentId);
        record.setTeacherId(auth.uid());
        record.setContent(body.getContent());
        record.setGuidanceAt(LocalDateTime.parse(body.getGuidanceAt().replace(" ", "T")));
        save(record);
        return new IdVO(record.getId());
    }
}
