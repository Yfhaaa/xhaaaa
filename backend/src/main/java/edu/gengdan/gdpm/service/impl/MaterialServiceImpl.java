package edu.gengdan.gdpm.service.impl;

import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.common.BusinessStatus;
import edu.gengdan.gdpm.common.Constants;
import edu.gengdan.gdpm.common.EntityViews;
import edu.gengdan.gdpm.dto.MaterialSubmitRequest;
import edu.gengdan.gdpm.dto.ReviewRequest;
import edu.gengdan.gdpm.entity.FileRecord;
import edu.gengdan.gdpm.entity.ProcessMaterial;
import edu.gengdan.gdpm.mapper.FileRecordMapper;
import edu.gengdan.gdpm.service.MaterialService;
import edu.gengdan.gdpm.service.MaterialStageService;
import edu.gengdan.gdpm.service.MidtermService;
import edu.gengdan.gdpm.service.ProposalService;
import edu.gengdan.gdpm.service.TaskBookService;
import edu.gengdan.gdpm.service.ThesisService;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.SubmissionVO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 持 material:read 权限查询范围内各阶段全部历史版本。 */
@Service
public class MaterialServiceImpl
    extends DomainSupport<FileRecordMapper, FileRecord>
    implements MaterialService
{

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

    /** 学生按阶段顺序提交本人文件；任务书由指导教师下达，退回重提创建新版本。 */
    public SubmissionVO submit(String stage, MaterialSubmitRequest body) {
        validate(body);
        return stageService(stage).submit(stage, body);
    }

    /** 指导教师仅审核待审核的最新版本，定稿通过还需对应版本查重合格。 */
    public MessageVO review(String stage, long materialId, ReviewRequest body) {
        validate(body);
        return stageService(stage).review(stage, materialId, body);
    }

    /** 先校验学生数据范围，再读取指定阶段最新材料，尚未提交时返回空。 */
    public Map<String, Object> latest(long studentId, String stage) {
        auth.student(studentId);
        ProcessMaterial material = stageService(stage).latest(studentId, stage);
        return material == null ? null : EntityViews.view(material);
    }

    /** 仅最新版本状态为已通过时认定阶段完成。 */
    public boolean passed(long studentId, String stage) {
        Map<String, Object> material = latest(studentId, stage);
        return material != null && BusinessStatus.APPROVED.label().equals(material.get("status"));
    }

    /** 持 material:read 权限查询范围内各阶段全部历史版本。 */
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
