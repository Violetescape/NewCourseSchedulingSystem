<template>
  <el-container class="app-container">
    <!-- 左侧侧边栏 -->
    <el-aside class="app-aside" width="240px">
      <div class="aside-inner">
        <!-- 系统名称（右上角） -->
        <div class="system-title">
          排课效能分析系统
        </div>

        <!-- 导航菜单 -->
        <el-menu
          class="menu"
          :default-active="activeMenu"
          background-color="transparent"
          text-color="#d1d5db"
          active-text-color="#ffffff"
          @select="handleMenuSelect"
        >
          <el-menu-item
            v-for="item in menuItems"
            :key="item.key"
            :index="item.key"
          >
            <el-icon class="menu-icon">
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-menu>
      </div>
    </el-aside>

    <!-- 右侧主体区域 -->
    <el-container class="main-container">
      <!-- 顶部 Header -->
      <el-header class="main-header" height="56px">
        <div class="header-title">
          {{ currentPageTitle }}
        </div>
      </el-header>

      <!-- 主体内容 -->
      <el-main class="main-content">
        <!-- 使用 v-if / v-else 简单演示页面切换 -->
        <Home v-if="activeMenu === 'home'" />
        <ClassManage v-else-if="activeMenu === 'classes'" />
        <TeacherManage v-else-if="activeMenu === 'teachers'" />
        <TeacherUnavailableManage
          v-else-if="activeMenu === 'teacher-unavailable'"
        />
        <ClassroomManage v-else-if="activeMenu === 'rooms'" />
        <CourseManage v-else-if="activeMenu === 'courses'" />
        <TeachingTaskManage v-else-if="activeMenu === 'teaching-tasks'" />
        <ScheduleView v-else-if="activeMenu === 'schedule-view'" />
        <AutoScheduleView v-else-if="activeMenu === 'auto-schedule'" />
        <AnalysisView v-else-if="activeMenu === 'analytics'" />
        <div v-else class="placeholder-page">
          <el-empty
            :description="`功能页面「${currentPageTitle}」占位，后续可接入实际组件或路由。`"
          />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import {
  House,
  Collection,
  User,
  OfficeBuilding,
  Notebook,
  Operation,
  DataAnalysis
} from '@element-plus/icons-vue'
import Home from './views/Home.vue'
import ClassManage from './views/ClassManage.vue'
import ClassroomManage from './views/ClassroomManage.vue'
import TeacherManage from './views/TeacherManage.vue'
import CourseManage from './views/CourseManage.vue'
import TeachingTaskManage from './views/TeachingTaskManage.vue'
import TeacherUnavailableManage from './views/TeacherUnavailableManage.vue'
import ScheduleView from './views/ScheduleView.vue'
import AutoScheduleView from './views/AutoScheduleView.vue'
import AnalysisView from './views/AnalysisView.vue'

const menuItems = [
  { key: 'home', label: '首页', icon: House },
  { key: 'classes', label: '班级管理', icon: Collection },
  { key: 'teachers', label: '教师管理', icon: User },
  { key: 'teacher-unavailable', label: '教师限排时间', icon: User },
  { key: 'rooms', label: '教室管理', icon: OfficeBuilding },
  { key: 'courses', label: '课程管理', icon: Notebook },
  { key: 'teaching-tasks', label: '教学任务管理', icon: Operation },
  { key: 'schedule-view', label: '课表可视化', icon: DataAnalysis },
  { key: 'auto-schedule', label: '自动排课', icon: Operation },
  { key: 'analytics', label: '效能分析', icon: DataAnalysis }
]

const activeMenu = ref('home')

const currentPageTitle = computed(() => {
  const matched = menuItems.find((item) => item.key === activeMenu.value)
  return matched ? matched.label : '首页'
})

const handleMenuSelect = (key) => {
  activeMenu.value = key
}
</script>

<style scoped>
.app-container {
  height: 100vh;
  background-color: #0f172a;
  color: #e5e7eb;
}

.app-aside {
  background: radial-gradient(circle at top left, #1e293b 0, #020617 60%);
  border-right: 1px solid rgba(148, 163, 184, 0.24);
  display: flex;
  flex-direction: column;
  padding: 16px 12px;
  box-sizing: border-box;
}

.aside-inner {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.system-title {
  align-self: flex-end;
  font-size: 14px;
  font-weight: 500;
  color: #e5e7eb;
  letter-spacing: 0.08em;
  text-align: right;
  margin-bottom: 24px;
  line-height: 1.4;
}

.menu {
  border-right: none;
  background-color: transparent;
}

.menu-icon {
  margin-right: 8px;
}

.main-container {
  background: linear-gradient(135deg, #020617 0%, #0b1120 35%, #020617 100%);
}

.main-header {
  display: flex;
  align-items: center;
  padding: 0 24px;
  box-sizing: border-box;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  background: linear-gradient(90deg, rgba(15, 23, 42, 0.9), rgba(2, 6, 23, 0.9));
  backdrop-filter: blur(14px);
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #f9fafb;
}

.main-content {
  padding: 20px 24px 24px;
  box-sizing: border-box;
}

.placeholder-page {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

