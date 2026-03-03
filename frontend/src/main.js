import { createApp } from 'vue'
import App from './App.vue'//根组件
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

//import './assets/main.css'//全局的css

createApp(App).use(ElementPlus).mount('#app')
