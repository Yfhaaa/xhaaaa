<script setup>
/**
 * 材料附件上传：拖拽或点击选择，前端校验类型与大小，上传成功后通过 v-model 回传文件编号。
 * 服务端仍会再次校验类型、大小与访问权限。
 */
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { UploadFilled, Document, CircleCloseFilled } from "@element-plus/icons-vue";
import http from "../api/http";

const props = defineProps({
  modelValue: { type: Number, default: null },
  accept: { type: String, default: ".pdf,.doc,.docx,.txt,.zip" },
  maxSizeMb: { type: Number, default: 20 },
});
const emit = defineEmits(["update:modelValue", "uploaded"]);

const busy = ref(false);
const fileInfo = ref(null);
const progress = ref(0);

const allowed = props.accept.split(",").map((ext) => ext.trim().toLowerCase());

function formatSize(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

/** el-upload 自定义上传：校验后调用统一文件接口 */
async function upload({ file }) {
  const ext = file.name.slice(file.name.lastIndexOf(".")).toLowerCase();
  if (!allowed.includes(ext)) {
    ElMessage.warning(`仅支持 ${allowed.join(" / ")} 格式`);
    return;
  }
  if (file.size > props.maxSizeMb * 1024 * 1024) {
    ElMessage.warning(`文件不能超过 ${props.maxSizeMb}MB`);
    return;
  }
  busy.value = true;
  progress.value = 0;
  try {
    const body = new FormData();
    body.append("file", file);
    const uploaded = await http.post("/files", body, {
      onUploadProgress: (e) => {
        if (e.total) progress.value = Math.round((e.loaded / e.total) * 100);
      },
    });
    fileInfo.value = { name: file.name, size: file.size };
    emit("update:modelValue", uploaded.id);
    emit("uploaded", { id: uploaded.id, name: file.name, size: file.size });
  } finally {
    busy.value = false;
  }
}

function clear() {
  fileInfo.value = null;
  emit("update:modelValue", null);
}
</script>

<template>
  <div class="gd-uploader">
    <div v-if="fileInfo && modelValue" class="gd-uploader__file">
      <el-icon :size="28" class="gd-uploader__file-icon"><Document /></el-icon>
      <div class="gd-uploader__file-meta">
        <strong>{{ fileInfo.name }}</strong>
        <span>{{ formatSize(fileInfo.size) }} · 已上传</span>
      </div>
      <el-icon class="gd-uploader__remove" title="移除" @click="clear"><CircleCloseFilled /></el-icon>
    </div>
    <el-upload
      v-else
      drag
      :accept="accept"
      :show-file-list="false"
      :http-request="upload"
      :disabled="busy"
      class="gd-uploader__drop"
    >
      <template v-if="busy">
        <el-progress type="circle" :percentage="progress" :width="64" />
        <div class="el-upload__text">正在上传…</div>
      </template>
      <template v-else>
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或 <em>点击选择</em></div>
      </template>
    </el-upload>
    <div class="gd-uploader__tip">支持 {{ allowed.join(" / ") }}，单个文件不超过 {{ maxSizeMb }}MB</div>
  </div>
</template>

<style scoped>
.gd-uploader {
  width: 100%;
}
.gd-uploader__drop :deep(.el-upload-dragger) {
  padding: 22px 16px;
  border-radius: 10px;
  background: #fafcfb;
}
.gd-uploader__file {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid #cfe0d9;
  border-radius: 10px;
  background: #f3f8f6;
}
.gd-uploader__file-icon {
  color: #175c4b;
}
.gd-uploader__file-meta {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
}
.gd-uploader__file-meta strong {
  overflow: hidden;
  color: #273d37;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.gd-uploader__file-meta span {
  color: #7d9088;
  font-size: 12px;
}
.gd-uploader__remove {
  color: #b5c2bc;
  font-size: 18px;
  cursor: pointer;
}
.gd-uploader__remove:hover {
  color: #e05a4f;
}
.gd-uploader__tip {
  margin-top: 6px;
  color: #8a9a93;
  font-size: 12px;
}
</style>
