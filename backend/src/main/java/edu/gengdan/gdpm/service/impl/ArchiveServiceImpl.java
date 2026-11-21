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

/** ArchiveService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class ArchiveServiceImpl extends DomainSupport<ArchiveRecordMapper, ArchiveRecord> implements ArchiveService {

    @Autowired
    private MaterialService materials;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper json;

    /** 查询范围内的归档快照。 */
    public Object archives() {
        auth.permission("stats:read");
        return reports.archives(auth.scope());
    }

    /** 在同一事务中检查全部材料和已发布成绩并保存不可变快照。 */
    @OperLog("归档项目")
    @Transactional(rollbackFor = Exception.class)
    public Object archive(Map<String, Object> body) throws Exception {
        auth.roles("ADMIN");
        auth.permission("archive:write");
        long studentId = Params.number(body, "studentId");
        lockStudent(studentId);
        require(lambdaQuery().eq(ArchiveRecord::getStudentId, studentId).count() == 0, "该项目已经归档");
        List<Map<String, Object>> manifest = new ArrayList<>();
        for (String stage : Constants.STAGES) {
            require(materials.passed(studentId, stage), "材料未齐全：" + stage);
            manifest.add(materials.latest(studentId, stage));
        }
        ScoreInfo score = scores.selectOne(
            query(ScoreInfo.class).eq(ScoreInfo::getStudentId, studentId).eq(ScoreInfo::getPublished, 1)
        );
        require(score != null, "成绩发布后才能归档");
        ArchiveRecord archive = entity(
            ArchiveRecord.class,
            "student_id",
            studentId,
            "manifest",
            json.writeValueAsString(Map.of("materials", manifest, "score", EntityViews.view(score))),
            "status",
            BusinessStatus.COMPLETED.label()
        );
        save(archive);
        return Map.of("id", archive.getId());
    }
}
