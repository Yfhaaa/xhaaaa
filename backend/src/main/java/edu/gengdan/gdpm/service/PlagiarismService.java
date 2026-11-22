package edu.gengdan.gdpm.service;

import edu.gengdan.gdpm.dto.PlagiarismRegisterRequest;
import edu.gengdan.gdpm.vo.IdVO;
import java.util.List;
import java.util.Map;

/** 持 material:read 权限查询授权学生的查重记录及对应定稿版本。 指导教师或管理员为尚未通过的最新定稿登记本人报告，新版本不能复用旧结论。 */
public interface PlagiarismService {
    /** 持 material:read 权限查询授权学生的查重记录及对应定稿版本。 */
    List<Map<String, Object>> records();

    /** 指导教师或管理员为尚未通过的最新定稿登记本人报告，新版本不能复用旧结论。 */
    IdVO register(PlagiarismRegisterRequest body);
}
