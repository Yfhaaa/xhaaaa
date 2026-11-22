package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.gengdan.gdpm.common.BusinessStatus;
import edu.gengdan.gdpm.common.Constants;
import edu.gengdan.gdpm.common.EntityViews;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.dto.ArchiveRequest;
import edu.gengdan.gdpm.entity.ArchiveRecord;
import edu.gengdan.gdpm.entity.ScoreInfo;
import edu.gengdan.gdpm.mapper.ArchiveRecordMapper;
import edu.gengdan.gdpm.service.ArchiveService;
import edu.gengdan.gdpm.service.MaterialService;
import edu.gengdan.gdpm.vo.IdVO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 持 stats:read 权限查询本人或指导范围内归档快照。 管理员在材料全部通过且成绩已发布后生成唯一不可变归档快照。 */
@Service
public class ArchiveServiceImpl
    extends DomainSupport<ArchiveRecordMapper, ArchiveRecord>
    implements ArchiveService
{

    @Autowired
    private MaterialService materials;

    @Autowired
    private ObjectMapper json;

    /** 持 stats:read 权限查询本人或指导范围内归档快照。 */
    public List<Map<String, Object>> archives() {
        auth.permission("stats:read");
        return reports.archives(auth.scope());
    }

    /** 管理员在材料全部通过且成绩已发布后生成唯一不可变归档快照。 */
    @OperLog("归档项目")
    @Transactional(rollbackFor = Exception.class)
    public IdVO archive(ArchiveRequest body) throws Exception {
        validate(body);
        auth.roles("ADMIN");
        auth.permission("archive:write");
        long studentId = body.getStudentId();
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
        ArchiveRecord archive = new ArchiveRecord();
        archive.setStudentId(studentId);
        archive.setManifest(json.writeValueAsString(Map.of("materials", manifest, "score", EntityViews.view(score))));
        archive.setStatus(BusinessStatus.COMPLETED.label());
        save(archive);
        return new IdVO(archive.getId());
    }
}
