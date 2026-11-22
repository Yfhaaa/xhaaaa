<script setup>
import VersionDetails from "./VersionDetails.vue";
defineProps({ nodes: { type: Array, default: () => [] } });
</script>
<template>
  <el-timeline
    ><el-timeline-item
      v-for="node in nodes"
      :key="node.code"
      :type="['已通过', '已完成'].includes(node.status) ? 'success' : 'primary'"
      :timestamp="node.name"
      placement="top"
    >
      <div class="timeline-card">
        <StatusTag :status="node.status" />
        <VersionDetails v-if="node.versions.length" :version="node.versions[0]" :stage="node.name" />
        <p v-else class="subtle">尚无提交记录</p>
        <el-collapse v-if="node.versions.length > 1">
          <el-collapse-item :title="'查看历史版本（' + (node.versions.length - 1) + '）'">
            <VersionDetails
              v-for="(v, index) in node.versions.slice(1)"
              :key="index"
              :version="v"
              :stage="node.name"
            /> </el-collapse-item
        ></el-collapse></div></el-timeline-item
  ></el-timeline>
</template>
