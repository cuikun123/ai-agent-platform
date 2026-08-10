import { Select } from '@arco-design/web-react'

const Option = Select.Option

/** 可用模型列表 */
const MODELS = [
  { value: 'deepseek-chat', label: 'DeepSeek Chat' },
  { value: 'deepseek-coder', label: 'DeepSeek Coder' },
]

interface ModelSelectorProps {
  value: string
  onChange: (model: string) => void
}

/** 模型选择下拉框 */
function ModelSelector({ value, onChange }: ModelSelectorProps) {
  return (
    <Select
      value={value}
      onChange={onChange}
      style={{ width: 180 }}
      size="small"
      bordered={false}
      triggerProps={{
        autoAlignPopupWidth: false,
      }}
    >
      {MODELS.map((m) => (
        <Option key={m.value} value={m.value}>
          {m.label}
        </Option>
      ))}
    </Select>
  )
}

export default ModelSelector
