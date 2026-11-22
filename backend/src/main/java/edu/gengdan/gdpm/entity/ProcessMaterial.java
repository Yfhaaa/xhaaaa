package edu.gengdan.gdpm.entity;

import java.time.LocalDateTime;

/** 四类过程材料实体共享的版本和审核字段契约。 */
public interface ProcessMaterial {
    Long getId();
    void setId(Long value);
    Long getStudentId();
    void setStudentId(Long value);
    Long getTeacherId();
    void setTeacherId(Long value);
    String getStage();
    void setStage(String value);
    String getTitle();
    void setTitle(String value);
    String getContent();
    void setContent(String value);
    Long getFileId();
    void setFileId(Long value);
    Integer getVersion();
    void setVersion(Integer value);
    String getStatus();
    void setStatus(String value);
    LocalDateTime getSubmittedAt();
    void setSubmittedAt(LocalDateTime value);
    Long getReviewerId();
    void setReviewerId(Long value);
    String getReviewComment();
    void setReviewComment(String value);
    LocalDateTime getReviewedAt();
    void setReviewedAt(LocalDateTime value);
    Integer getRevisionCount();
    void setRevisionCount(Integer value);
}
