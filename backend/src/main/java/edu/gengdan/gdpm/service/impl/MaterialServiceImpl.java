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

/** MaterialService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class MaterialServiceImpl extends DomainSupport<FileRecordMapper, FileRecord> implements MaterialService {

    @Autowired
    private TaskBookService taskService;

    @Autowired
    private ProposalService proposalService;

    @Autowired
    private MidtermService midtermService;

    @Autowired
    private ThesisService thesisService;

    /** 按阶段白名单选择对应材料服务。 */
    private MaterialStageService<? extends ProcessMaterial> stageService(String stage) {
        switch (stage) {
            case "task":
                return taskService;
            case "proposal":
                return proposalService;
            case "midterm":
                return midtermService;
            case "draft":
            case "final":
                return thesisService;
            default:
                throw new ApiException(400, "材料阶段不存在");
        }
    }

    /** 校验阶段顺序后保存新材料版本。 */
    public Object submit(String stage, Map<String, Object> body) {
        return stageService(stage).submit(stage, body);
    }

    /** 审核当前待审核的最新材料。 */
    public Object review(String stage, long materialId, Map<String, Object> body) {
        return stageService(stage).review(stage, materialId, body);
    }

    /** 查询指定阶段的最新版本。 */
    public Map<String, Object> latest(long studentId, String stage) {
        auth.student(studentId);
        ProcessMaterial material = stageService(stage).latest(studentId, stage);
        return material == null ? null : EntityViews.view(material);
    }

    /** 判断最新材料是否审核通过。 */
    public boolean passed(long studentId, String stage) {
        Map<String, Object> material = latest(studentId, stage);
        return material != null && BusinessStatus.APPROVED.label().equals(material.get("status"));
    }

    /** 查询范围内全部材料版本。 */
    public List<Map<String, Object>> listMaterials(String stage) {
        auth.permission("material:read");
        List<Map<String, Object>> result = new ArrayList<>();
        for (String current : stage == null || stage.isBlank() ? Constants.STAGES : List.of(stage)) {
            stageService(current);
            switch (current) {
                case "task":
                    result.addAll(reports.materialsTask(auth.scope(), current));
                    break;
                case "proposal":
                    result.addAll(reports.materialsProposal(auth.scope(), current));
                    break;
                case "midterm":
                    result.addAll(reports.materialsMidterm(auth.scope(), current));
                    break;
                default:
                    result.addAll(reports.materialsThesis(auth.scope(), current));
            }
        }
        return result;
    }
}
