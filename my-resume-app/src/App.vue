<template>
  <div class="app-container">
    <aside class="sidebar">
      <div class="logo">简历 AI 助手</div>
      <nav>
        <div :class="['nav-item', currentTab === 'upload' ? 'active' : '']" @click="currentTab = 'upload'">📄 简历上传</div>
        <div :class="['nav-item', currentTab === 'chat' ? 'active' : '']" @click="currentTab = 'chat'">💬 AI 深度对话</div>
        <div :class="['nav-item', currentTab === 'report' ? 'active' : '']" @click="currentTab = 'report'">🛤️ 职业晋升路径</div>
        <div :class="['nav-item', currentTab === 'archive' ? 'active' : '']" @click="currentTab = 'archive'">📁 个人档案库</div>
      </nav>
      <div class="user-info">服务创新大赛项目</div>
    </aside>

    <main class="main-content">
      <header class="top-bar">
        <span>当前功能：{{ tabNames[currentTab] }}</span>
      </header>

      <section v-if="currentTab === 'upload'" class="content-body">
        <div class="welcome-text">
          <h1>你好！我是你的职业规划 AI</h1>
          <p>请上传一份简历，我将通过多维分析为你提供职业建议。</p>
        </div>
        <div class="upload-zone">
          <div class="upload-card">
            <div class="icon">📂</div>
            <h3>点击或拖拽简历到这里</h3>
            <p>支持 PDF, Word 格式</p>
            <input type="file" class="hidden-input" @change="handleUpload" />
          </div>
        </div>
      </section>

      <section v-if="currentTab === 'chat'" class="chat-body">
        <div class="chat-window" ref="chatWindow">
          <div v-for="(msg, index) in messages" :key="index" :class="['message-row', msg.role]">
            <div class="bubble">{{ msg.text }}</div>
          </div>
        </div>
        <div class="chat-input-area">
          <input v-model="userInput" type="text" placeholder="补充你的细节..." @keyup.enter="sendMessage" />
          <button @click="sendMessage" class="send-btn">发送</button>
          <button @click="startAnalysis" class="analyze-btn" v-if="messages.length > 2">生成报告</button>
        </div>

        <div v-if="isAnalyzing" class="progress-mask">
          <div class="progress-box">
            <p>AI 正在深度扫描并规划路径...</p>
            <div class="progress-bar-container">
              <div class="progress-bar" :style="{ width: analysisProgress + '%' }"></div>
            </div>
            <p>{{ analysisProgress }}%</p>
          </div>
        </div>
      </section>

      <section v-if="currentTab === 'report'" class="report-body">
        <div v-if="!isUploaded" class="empty-state">
          <h3>请先在首页上传您的简历</h3>
          <p>AI 需要读取您的经历后才能生成晋升路径规划哦。</p>
          <button @click="currentTab = 'upload'" class="nav-btn">去上传</button>
        </div>

        <div v-else class="report-content">
          <h2>职业晋升路径规划</h2>
          <div class="path-container">
            <div class="path-node">初级前端工程师</div>
            <div class="arrow">↓</div>
            <div class="path-node active">中级/高级开发</div>
            <div class="arrow">↓</div>
            <div class="path-node">前端架构师</div>
          </div>
        </div>
      </section>

      <section v-if="currentTab === 'archive'" class="content-body">
        <h2>已保存的分析记录</h2>
        <div class="archive-list">
          <div class="archive-item">2026-03-18 简历分析报告.pdf</div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

// --- 基础状态 ---
const currentTab = ref('upload')
const isUploaded = ref(false)
const userInput = ref('')
const tabNames = { upload: '简历上传', chat: 'AI 深度对话', report: '职业晋升路径', archive: '个人档案库' }

// --- 对话逻辑 ---
const chatWindow = ref(null)
const messages = ref([
  { role: 'ai', text: '你好！我已经读取了你的简历。能详细说说你做过的那个项目吗？' }
])

const sendMessage = () => {
  if (!userInput.value.trim()) return;
  messages.value.push({ role: 'user', text: userInput.value });
  const val = userInput.value;
  userInput.value = '';
  setTimeout(() => {
    messages.value.push({ role: 'ai', text: `针对你说的“${val}”，我建议你加强对 Vite 配置的学习。点击右下角按钮可以生成完整报告。` });
  }, 800);
}

// 自动滚动到底部
watch(messages, () => {
  nextTick(() => {
    if (chatWindow.value) {
      chatWindow.value.scrollTop = chatWindow.value.scrollHeight
    }
  })
}, { deep: true })

// --- 上传逻辑 ---
const handleUpload = (e) => {
  if (e.target.files[0]) {
    isUploaded.value = true
    alert('上传成功！');
    currentTab.value = 'chat';
  }
}

// --- 分析进度逻辑 ---
const isAnalyzing = ref(false)
const analysisProgress = ref(0)

const startAnalysis = () => {
  isAnalyzing.value = true
  analysisProgress.value = 0
  const timer = setInterval(() => {
    analysisProgress.value += 10
    if (analysisProgress.value >= 100) {
      clearInterval(timer)
      setTimeout(() => {
        isAnalyzing.value = false
        currentTab.value = 'report'
      }, 500)
    }
  }, 200)
}
</script>

<style scoped>
:root { --monet-blue: #b1c9e8; --monet-green: #d4e4bc; --monet-cream: #fdfcf0; }

.app-container { display: flex; height: 100vh; background: #fdfcf0; font-family: sans-serif; }
.sidebar { width: 260px; background: #b1c9e8; padding: 20px; display: flex; flex-direction: column; backdrop-filter: blur(10px); /* 模糊背景 */
  border-right: 1px solid rgba(255,255,255,0.3);}
.logo { font-size: 1.5rem; color: white; margin-bottom: 40px; font-weight: bold; }
.nav-item { padding: 15px; margin-bottom: 8px; border-radius: 10px; cursor: pointer; color: #4a5568; }
.nav-item.active { background: white; color: #7d9bbd; font-weight: bold; }

.main-content { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.top-bar { padding: 15px 30px; background: white; border-bottom: 1px solid #eee; color: #717a87; }

.content-body, .chat-body, .report-body { padding: 40px; flex: 1; display: flex; flex-direction: column; align-items: center; overflow-y: auto; }

.welcome-text h1 { color: #717a87; font-weight: 800; font-size: 2.2rem; }
.report-body h2, .content-body h2 { color: #717a87 !important; font-weight: 800; margin-bottom: 30px; }

/* 对话窗口 */
.chat-window { width: 100%; max-width: 600px; height: 400px; background: white; border-radius: 15px; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; border: 1px solid #eee; }
.message-row { margin-bottom: 10px; display: flex; }
.message-row.ai { justify-content: flex-start; }
.message-row.user { justify-content: flex-end; }
.bubble { padding: 10px 15px; border-radius: 15px; max-width: 70%; font-size: 0.9rem; line-height: 1.4; }
.ai .bubble { background: #eef2ff; color: #4a5568; }
.user .bubble { background: #d4e4bc; color: #4a5568; }

.chat-input-area { margin-top: 15px; width: 100%; max-width: 600px; display: flex; gap: 10px; }
.chat-input-area input { flex: 1; padding: 10px; border-radius: 8px; border: 1px solid #ddd; outline: none; }
.send-btn { padding: 10px 20px; background: #b1c9e8; border: none; border-radius: 8px; cursor: pointer; }
.analyze-btn { padding: 10px 20px; background: #d4e4bc; border: none; border-radius: 8px; cursor: pointer; color: #5a6e4d; font-weight: bold; }

/* 进度条弹窗 */
.progress-mask { position: fixed; inset: 0; background: rgba(255,255,255,0.8); display: flex; justify-content: center; align-items: center; z-index: 100; }
.progress-box { text-align: center; background: white; padding: 30px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }
.progress-bar-container { width: 300px; background: #edf2f7; border-radius: 10px; margin: 20px 0; }
.progress-bar { height: 10px; background: #b1c9e8; border-radius: 10px; transition: width 0.3s; }

/* 路径图 */
.path-container { display: flex; flex-direction: column; align-items: center; }
.path-node { padding: 15px 30px; background: white; border: 2px solid #b1c9e8; border-radius: 10px; margin: 10px; color: #717a87; }
.path-node.active { background: #d4e4bc; color: #5a6e4d !important; border-color: #5a6e4d; font-weight: bold; }
.arrow { font-size: 20px; color: #b1c9e8; }

/* 档案记录 */
.archive-list { width: 100%; max-width: 500px; }
.archive-item { background: white; padding: 15px; border-radius: 10px; border: 1px solid #eee; margin-bottom: 10px; }

/* 通用按钮 */
.nav-btn { margin-top: 20px; padding: 10px 30px; background: #b1c9e8; border: none; border-radius: 20px; cursor: pointer; }

/* 上传卡片 */
.upload-zone { width: 100%; max-width: 600px; margin-top: 20px; }
.upload-card { padding: 60px; border: 3px dashed #b1c9e8; background: white; border-radius: 24px; text-align: center; position: relative; }
.hidden-input { position: absolute; inset: 0; opacity: 0; cursor: pointer; }
.icon { font-size: 3rem; margin-bottom: 10px; }
/* 定义一个通用的点击放大基础样式 */
.send-btn, 
.analyze-btn, 
.nav-btn, 
.download-btn,
.upload-card {
  transition: transform 0.1s ease-in-out, box-shadow 0.2s ease; /* 让放大和阴影的变化更平滑 */
}

/* 当鼠标悬停时，稍微加一点阴影，让它看起来“浮起来” */
.send-btn:hover, 
.analyze-btn:hover, 
.nav-btn:hover, 
.download-btn:hover,
.upload-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); /* 淡淡的阴影 */
}

/* 当鼠标按下（点击）时的核心放大效果 */
.send-btn:active, 
.analyze-btn:active, 
.nav-btn:active, 
.download-btn:active {
  transform: scale(1.03); /* 放大 3%，这个幅度刚刚好 */
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05); /* 点击时阴影变小，模拟按下去的感觉 */
}

/* 特别给上传卡片也加一个点击放大（因为它是虚线框，放大效果很明显） */
.upload-card:active {
  transform: scale(1.02); /* 上传卡片比较大，放大 2% 就够了 */
  border-color: #d4e4bc !important; /* 点击时边框颜色变成莫奈绿 */
}
</style>