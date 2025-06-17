import { useState } from 'react'; // 从 React 中引入 useState，用于声明组件状态变量

// 定义每个操作类型的模板，包括 label 名称、接口地址、方法和所需输入字段
const operationTemplates: Record<string, {
  label: string; // 显示在下拉框中的文字
  endpoint: string; // 请求的 API 地址
  method: 'GET' | 'POST'; // HTTP 方法类型，只能是 GET 或 POST
  inputs: { key: string; label: string }[]; // 输入字段数组，包含字段名与标签
}> = {
  a: {
    label: 'レジ画面のスクリーンショット(immage.sh実行)', // 显示为“收银画面的截图（执行image.sh）”
    endpoint: 'http://localhost:8080/api/run-b', // 执行截图脚本的接口
    method: 'POST', // 使用 POST 方式
    inputs: [
      { key: 'ip', label: 'IPアドレス（例: 10.168.106.102）' } // 用户需输入 IP 地址
    ]
  },
  b: {
    label: 'SSPS送信', // 显示为“发送 SSPS”
    endpoint: 'http://localhost:8080/api/ssps-send', // 目标接口地址
    method: 'POST',
    inputs: [
      { key: 'ip', label: '監視サーバーいるIP' }, // 监视服务器的 IP
      { key: 'storeCode', label: '店舗番号（例: 7105）' }, // 店铺编号
      { key: 'StreCode.txt', label: 'StreCode.txtファイル（例: StreCode.txt）' } // 参数文件名
    ]
  },
  c: {
    label: '自分用テストレジからログ内容を取得', // 表示从测试机获取日志
    endpoint: 'http://localhost:8080/api/log/latest', // 获取日志的接口
    method: 'GET',
    inputs: [
      { key: 'ip', label: 'IPアドレス（例: 10.168.106.102）' } // 输入 IP 地址
    ]
  }
};

export default function LogViewerApp() { // 定义并导出主组件 LogViewerApp

  const [taskIdResult, setTaskId] = useState<string>(''); // 保存任务 ID 结果
  const [imageUrls, setImageUrls] = useState<string[]>([]); // 存储图片 URL 列表
  const [operation, setOperation] = useState('a'); // 当前选择的操作类型（默认为 a）
  const [formValues, setFormValues] = useState<Record<string, string>>({}); // 表单输入的值集合
  const [result, setResult] = useState<string>(''); // 请求结果显示内容
  const [log, setLog] = useState<string>(''); // 日志文本内容

  // 处理输入框变化，动态更新对应字段值
  const handleInputChange = (name: string, value: string) => {
    setFormValues(prev => ({ ...prev, [name]: value })); // 保留原字段，更新指定 name 的值
  };

  // 点击“実行”按钮时触发，提交请求逻辑
  const handleSubmit = async () => {
    const { inputs, endpoint, method } = operationTemplates[operation]; // 根据当前操作类型获取配置

    // 组装请求参数
    const params = inputs.reduce((acc, item) => {
      if (!formValues[item.key]) { // 检查每个字段是否为空
        alert(`${item.key} を入力してください`); // 提示用户输入
        throw new Error('未入力の項目があります'); // 抛出异常，阻止提交
      }
      acc[item.key] = formValues[item.key]; // 添加键值对到参数对象中
      return acc;
    }, {} as Record<string, string>);

    // 请求前 UI 初始化（显示“执行中...”）
    setResult('実行中...');
    setImageUrls([]);
    setTaskId('');
    setLog('');

    try {
      if (operation === 'c') { // GET 请求（获取日志）
        const res = await fetch(endpoint, {
          method: 'GET',
          headers: { 'X-Target-IP': params['ip'] } // 用自定义头部传递 IP
        });
        const logText = await res.text(); // 获取文本
        setLog(logText);
        setResult(logText); // 显示结果
      } else if (operation === 'a') { // POST 请求，执行截图并获取任务 ID 和图片
        const response = await fetch(endpoint, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ operation, ...params }) // 传送操作类型和参数 这里必须是json字符串 
        });

        const taskIdResult = await response.text(); // 接收任务 ID
        setTaskId(taskIdResult);
        setResult("Task ID: " + taskIdResult);

        const imageRes = await fetch(`/api/image-files?taskId=${taskIdResult}`); // 获取图片列表
        const rawImageText = await imageRes.text();
        try {
          const imageList = JSON.parse(rawImageText); // 转为数组
          setImageUrls(imageList); // 设置图片 URL
        } catch (e) {
          setResult("画像取得失敗：" + e); // 捕获解析失败
        }
      } else { // 其他 POST 请求（如 b）
        const response = await fetch(endpoint, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ operation, ...params })
        });
        const text = await response.text();
        setResult(text); // 显示文本
        setLog(text); // 同时记录日志
      }
    } catch (err: any) {
      setResult("実行失敗：" + err.message); // 异常处理
    }
  };

  return (
    <div className="p-4 max-w-5xl mx-auto bg-white rounded-xl shadow-md"> {/* 外层容器样式 */}
      <h1 className="text-xl font-bold mb-4">コマンドツール</h1> {/* 页面标题 */}

      <label className="block mb-2 font-medium">操作を選択：</label> {/* 操作选择标签 */}
      <select
        className="border p-2 mb-4 w-full"
        value={operation} // 当前选中值
        onChange={e => {
          setOperation(e.target.value); // 更新操作类型
          setFormValues({}); // 清空表单输入
          setResult('');
          setLog('');
          setImageUrls([]);
          setTaskId('');
        }}
      >
        {/* 遍历 operationTemplates 生成选项 */}
        {Object.entries(operationTemplates).map(([key, op]) => (
          <option key={key} value={key}>{op.label}</option>
        ))}
      </select>

      {/* 根据选中操作的输入字段渲染输入框 */}
      {operationTemplates[operation].inputs.map(input => (
        <div key={input.key} className="mb-4">
          <label className="block mb-1 font-medium">{input.label}</label>
          <input
            className="border p-2 w-full"
            value={formValues[input.key] || ''} // 绑定对应字段值
            onChange={e => handleInputChange(input.key, e.target.value)} // 变化时触发更新
          />
        </div>
      ))}

      {/* 提交按钮 */}
      <button
        onClick={handleSubmit}
        className="bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700"
      >
        実行
      </button>

      {/* 显示任务 ID */}
      {operation === 'a' && taskIdResult && (
        <div className="mt-4 text-sm text-green-700">{taskIdResult}</div>
      )}

      {/* 显示日志（操作 c）*/}
      {operation === 'c' && (
        <div className="mt-4 text-sm textc-green-8s00">123465{log}</div>
      )}

      {/* 显示日志（操作 b）*/}
      {operation === 'b' && (
        <div className="mt-4 max-h-96 overflow-auto p-2 text-sm text-green-700 whitespace-pre-wrap border border-gray-300 rounded bg-gray-50">{log}</div>
      )}

      {/* 显示图片区域 */}
      {operation === 'a' && imageUrls.length > 0 && (
        <div className="mtd-6">
          <h2 className="text-md font-semibold mb-2">画像ファイル一覧：</h2>
          <div className="grid grid-cols-2 gap-4">
            {imageUrls.map((url, idx) => (
              <div key={idx} className="border p-2 text-center shadow rounded">
                <a href={url} download>
                  <img src={url} alt={`img-${idx}`} className="w-24 h-24 object-cover mx-auto" />
                  <p className="text-sm mt-1">{url.split('/').pop()}</p>
                </a>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}