import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { setCreateRoot } from '@arco-design/web-react/es/_util/react-dom'
import App from './App'
import '@arco-design/web-react/dist/css/arco.css'
import './styles/theme.css'

// 解决 Arco Design Message/Notification 在 React 18 下的兼容问题
// Arco 内部从 react-dom 导入，React 18 的 createRoot 在 react-dom/client 中
// 通过 Arco 官方提供的 setCreateRoot 方法注入
setCreateRoot(createRoot)

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
)
