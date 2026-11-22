<script setup>
import { dateText } from "../utils/format";
import { downloadFile } from "../api/material";
defineProps({ version: Object, stage: String });
</script>
<template>
  <div class="version-row">
    <strong>{{ version.title || version.group_name || stage }}</strong>
    <StatusTag v-if="version.status" :status="version.status" />
    <p>
      提交：{{
        dateText(version.submitted_at || version.updated_at || version.created_at || version.defense_at)
      }}
    </p>
    <p>
      审核人：{{ version.reviewer_name || version.teacher_name || "—" }} · 修改
      {{ version.revision_count || 0 }} 次
    </p>
    <p>
      意见：{{ version.review_comment || "—" }} · 文件版本：{{
        version.version ? "v" + version.version : "—"
      }}
    </p>
    <el-button v-if="version.file_id" link type="primary" @click="downloadFile(version)"
      >下载此版本</el-button
    >
  </div>
</template>
