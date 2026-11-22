package edu.gengdan.gdpm.service;

import edu.gengdan.gdpm.vo.ImportResultVO;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理员下载含可删除示例行的固定七列表头用户模板。 管理员逐行导入合规用户，失败行隔离，不回传密码，最多两千行二十兆。 持 stats:read 权限导出与查询相同范围的成绩、进度或缺失材料。
 * 范围内用户下载齐全项目的五阶段材料和成绩；管理员可按班级打包，缺文件时失败。
 */
public interface ExchangeService {
    /** 管理员下载含可删除示例行的固定七列表头用户模板。 */
    byte[] template();

    /** 管理员逐行导入合规用户，失败行隔离，不回传密码，最多两千行二十兆。 */
    ImportResultVO importUsers(MultipartFile file) throws Exception;

    /** 持 stats:read 权限导出与查询相同范围的成绩、进度或缺失材料。 */
    byte[] excel(String type, Map<String, String> filters);

    /** 范围内用户下载齐全项目的五阶段材料和成绩；管理员可按班级打包，缺文件时失败。 */
    byte[] archive(Long studentId, Long classId) throws Exception;
}
