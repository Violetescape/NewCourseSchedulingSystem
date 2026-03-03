<template>
  <div class="home-page">
    <!-- 顶部欢迎区 -->
    <section class="hero">
      <div class="hero-text">
        <h1>欢迎使用排课效能分析系统</h1>
        <p>
          快速掌握班级、教师、教室与课程资源配置情况，辅助自动排课与效能评估。
        </p>
      </div>
    </section>

    <!-- 统计卡片区 -->
    <section class="stats-grid">
      <el-row :gutter="16">
        <el-col
          v-for="card in statCards"
          :key="card.key"
          :xs="12"
          :sm="12"
          :md="6"
          :lg="6"
        >
          <el-card class="stat-card" shadow="hover">
            <div class="stat-card-header">
              <span class="stat-label">{{ card.label }}</span>
            </div>
            <div class="stat-value-row">
              <span class="stat-value">{{ card.value }}</span>
              <span
                class="stat-trend"
                :class="card.trend === 'up' ? 'up' : 'down'"
              >
                {{ card.trend === 'up' ? '+' : '-' }}{{ card.percent }}%
              </span>
            </div>
            <div class="stat-subtext">
              {{ card.subtext }}
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 占位区：近期排课提示/公告等 -->
    <section class="placeholder-section">
      <el-card shadow="never" class="placeholder-card">
        <template #header>
          <div class="placeholder-header">
            <span>近期排课概览</span>
          </div>
        </template>
        <p class="placeholder-text">
          这里可以展示未来 7 天的排课安排、冲突提醒与资源利用率变化趋势。
          后续接入实际数据后，将以图表与列表的形式呈现。
        </p>
      </el-card>
    </section>
  </div>
</template>

<script setup>
const statCards = [
  {
    key: 'classes',
    label: '班级总数',
    value: 32,
    percent: 8.4,
    trend: 'up',
    subtext: '相比上学期增加 8.4%'
  },
  {
    key: 'teachers',
    label: '可排课教师',
    value: 58,
    percent: 2.1,
    trend: 'up',
    subtext: '教师排课可用率提升 2.1%'
  },
  {
    key: 'rooms',
    label: '教室利用率',
    value: '76%',
    percent: 3.7,
    trend: 'up',
    subtext: '高峰时段利用率稳步上升'
  },
  {
    key: 'conflicts',
    label: '排课冲突数',
    value: 4,
    percent: 5.2,
    trend: 'down',
    subtext: '冲突数较上周下降 5.2%'
  }
]
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 顶部欢迎区 */
.hero {
  padding: 18px 20px;
  border-radius: 18px;
  background: radial-gradient(circle at top left, #1d4ed8 0, #020617 55%);
  color: #e5e7eb;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.6);
}

.hero-text h1 {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
  color: #f9fafb;
}

.hero-text p {
  margin: 0;
  font-size: 14px;
  color: #e5e7eb;
  opacity: 0.9;
}

/* 统计卡片区 */
.stats-grid {
  margin-top: 4px;
}

.stat-card {
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.35);
  color: #e5e7eb;
  padding: 14px 16px;
}

.stat-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 13px;
  color: #9ca3af;
}

.stat-value-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #f9fafb;
}

.stat-trend {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
}

.stat-trend.up {
  background-color: rgba(22, 163, 74, 0.15);
  color: #4ade80;
}

.stat-trend.down {
  background-color: rgba(239, 68, 68, 0.15);
  color: #fca5a5;
}

.stat-subtext {
  font-size: 12px;
  color: #9ca3af;
}

/* 占位卡片 */
.placeholder-section {
  margin-top: 4px;
}

.placeholder-card {
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.35);
  color: #e5e7eb;
}

.placeholder-header {
  font-size: 14px;
  font-weight: 500;
}

.placeholder-text {
  font-size: 13px;
  color: #9ca3af;
  margin: 0;
}

/* 响应式微调 */
@media (max-width: 768px) {
  .hero {
    padding: 14px 16px;
  }

  .hero-text h1 {
    font-size: 18px;
  }
}
</style>

