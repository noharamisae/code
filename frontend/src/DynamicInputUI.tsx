import { useState, useEffect, useRef } from 'react';
import { LogDownButton } from './LogDownButton';
import styles from './styles/styles.module.css';
import  ConfirmModal from './ConfirmModal';
import Encoding from "encoding-japanese"; 

/**
 * 操作テンプレート
 */
const operationTemplates: Record<string, {
  label: string;
  endpoint: string;
  method: 'GET' | 'POST';
  inputs: { key: string; label: string; placeholder?: string }[];
}> = {
  b1: {
    label: 'SSPS画像データ送信(ImageFtpExec)',
    endpoint: 'http://192.168.106.189:8080/api/ssps-image-send',
    method: 'POST',
    inputs: [
      { key: 'storeCode', label: '店舗番号',  placeholder: '指定StreCodeの全店舗送信の場合は「0」を入力' },
      { key: 'allStreCodeFile', label: 'StreCode.txtファイル', placeholder: 'ファイル名を入力するか、または選択してください' },
  
    ],
  },
  b2: {
    label: 'SSPS画像データ確認用スクリプト送信(ShFtpExec)',
    endpoint: 'http://192.168.106.189:8080/api/ssps-script-send',
    method: 'POST',
    inputs: [
      { key: 'scriptName', label: '送信スクリプト（ToolChkImage.sh）',placeholder: 'ファイル名を入力するか、または選択してください'},
      { key: 'storeCode', label: '店舗番号',  placeholder: '指定StreCodeの全店舗送信の場合は「0」を入力'  },
      { key: 'allStreCodeFile', label: 'StreCode.txtファイル', placeholder: 'ファイル名を入力するか、または選択してください' },
    ],
  },
  b3: {
    label: 'SSPS画像データ送信結果確認(ChkImageExec)',
    endpoint: 'http://192.168.106.189:8080/api/ssps-check-send',
    method: 'POST',
    inputs: [
      { key: 'storeCode', label: '店舗番号',  placeholder: '指定StreCodeの全店舗送信の場合は「0」を入力' },
      { key: 'allStreCodeFile', label: 'StreCode.txtファイル' , placeholder: 'ファイル名を入力するか、または選択してください' },
    ],
  },
  c1: {
    label: 'アプリのRPMデータ送信(RpmFtpExec)',
    endpoint: 'http://192.168.106.189:8080/api/rpm-send',
    method: 'POST',
    inputs: [
      { key: 'storeCode', label: '店舗番号',  placeholder: '指定StreCodeの全店舗送信の場合は「0」を入力' },
      { key: 'allStreCodeFile', label: 'StreCode.txtファイル' , placeholder: 'ファイル名を入力するか、または選択してください' },
    ],
  },
  c2: {
    label: 'アプリのRPM確認用スクリプト送信(ShFtpExec)',
    endpoint: 'http://192.168.106.189:8080/api/rpm-script-send',
    method: 'POST',
    inputs: [
      { key: 'scriptName', label: '送信スクリプト（ToolChkRpm.sh）',  placeholder: 'ファイル名を入力するか、または選択してください'},
      { key: 'storeCode', label: '店舗番号',  placeholder: '指定StreCodeの全店舗送信の場合は「0」を入力' },
      { key: 'allStreCodeFile', label: 'StreCode.txtファイル' , placeholder: 'ファイル名を入力するか、または選択してください' },
    ],
  },
  c3: {
    label: 'アプリのRPMデータ送信結果確認(ChkVerExec)',
    endpoint: 'http://192.168.106.189:8080/api/rpm-check-send',
    method: 'POST',
    inputs: [
      { key: 'storeCode', label: '店舗番号',  placeholder: '指定StreCodeの全店舗送信の場合は「0」を入力' },
      { key: 'allStreCodeFile', label: 'StreCode.txtファイル' , placeholder: 'ファイル名を入力するか、または選択してください'  },
    ],
  },
};

/**
 * React の関数型コンポーネントの定義です。
 */
export default function LogViewerApp() {
  const [operation, setOperation] = useState('b1'); // 現在選択されている操作のキーを保持
  const [formValues, setFormValues] = useState<Record<string, string>>({}); // 入力フォームの値をkey-value形式として保存
  const [scriptOutput, setScriptOutput] = useState(''); // スクリプト実行結果
  const [summaryOutput, setSummaryOutput] = useState(''); // 実行結果概要
  const [rpmScicptSendSummary, setRpmScicptSendSummary] = useState<any>(null); // スクリプト実行結果の概要
  const [okOutput, setOkOutput] = useState('');// 実行成功した店舗一覧
  const [ngOutput, setNgOutput] = useState('');// 実行失敗した店舗一覧
  const [result, setResult] = useState('');
  const outputRef = useRef<HTMLDivElement>(null); // useRef を使えば、値を変えても画面はそのままです。
  const [fileList, setFileList] = useState<string[]>([]);
  const [showFileDialog, setShowFileDialog] = useState(false);// ファイル選択ダイアログを表示するかどうかのフラグ
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);// 確認ダイアログを表示するかどうかのフラグ
  const [showStreCodeDownloadButton, setStreCodeDownloadButton] = useState(false);// StreCode.txtダウンロードボタンを表示するかどうかのフラグ
  const [streCodeContent, setStreCodeContent] = useState(''); // StreCode.txtの内容を保持するためのstate


  // ✅ Shift_JIS に変換する関数
// Shift_JISエンコード＋BOM付きBlobを作成（Shift_JIS编码并加上BOM）
const getSjisBlob = (text: string): Blob => {
  const sjisArray = Encoding.convert(Encoding.stringToCode(text), {
    to: ' Shift_JIS ',
    from: 'UNICODE',
    type: 'arraybuffer',
  });

  // Add BOM（加入 Shift_JIS BOM）
  const bom = [0xEF, 0xBB, 0xBF];
  const finalArray = new Uint8Array([...bom, ...sjisArray]);

  return new Blob([finalArray], { type: "application/octet-stream" });
};

  // ファイル名のプレフィックスを取得する関数
const getPrefixByType = (operation: string, type: "main" | "ok" | "ng") => {
  let base = "";
  let base2 = "";
  switch (operation) {
    case "b1":
      base = "ImageFtpExec";
      base2 = "Image";
      break;
    case "b2":
      base = "ShFtpExec";
      base2 = "ToolChkImage";
      break;
    case "ChkImageExec":
      base = "ChkImageExec";
      base2 = "ChkImage";
      break;
    case "c1":
      base = "RpmFtpExec";
      base2 = "Rpm";
      break;
    case "c2":
      base = "ShFtpExec";
      base2 = "ToolChkRpm";
      break;
    case "c3":
      base = "ChkVerExec";
      base2 = "ChkRpm";
      break;  
  }

  // ファイル種類別に prefix を返す
  if (type === "main") return `${base}_Output`;
  if (type === "ok") return `${base2}send_`;
  if (type === "ng") return `${base2}send_`;

  return "";
};
  useEffect(() => {
    if (outputRef.current) {
      outputRef.current.scrollTop = outputRef.current.scrollHeight; // これは新しい出力がでる度に、outputRefのDIVを一番したまでスクロールするための処理
    }
  }, [scriptOutput, summaryOutput, rpmScicptSendSummary]); // scriptOutput の中身が変わる度に、useEffectのscriptOutputが反応して、スクロールを最下部まで動かしています

  const handleInputChange = (key: string, value: string) => {
    // このprevは、setFormValues に渡される前のの状態を指します、前の状態をコピーしつつ、指定されたkeyの値を更新します
    setFormValues(prev => ({ ...prev, [key]: value })); // 複数の入力欄がある時、それぞれの値を一つのオブジェクトにまとめて
  };

  const handleFileListOpen = async () => { // 非同期関数
    try {
      const res = await fetch('http://192.168.106.189:8080/api/browse-files');

      const data = await res.json();
      // サーバーから取得ししたファイルをfileList に保存、もしdata.filesがundefined やnull なら、空の配列を代わりにセット
      setFileList(data.files || []);
      // ファイル選択ダイアログを画面に表示するために、表示フラグをtrue
      setShowFileDialog(true);
    } catch (err) {
      alert('ファイル一覧の取得に失敗しました');
    }
  };

  const handleSelectFile = (filename: string) => {
    setFormValues(prev => ({ ...prev, allStreCodeFile: filename }));
    setShowFileDialog(false);
  };


  const handleSubmit = async () => {
    const { inputs, endpoint, method } = operationTemplates[operation];

    const params = inputs.reduce((acc, { key, label }) => {
      if (key === 'allStreCodeFile') {
        acc[key] = formValues[key] || 'StreCode.txt';
      } else {
        if (!formValues[key]) {
          alert(`${label} を入力してください`);
          throw new Error('未入力の項目があります');
        }
        acc[key] = formValues[key];
      }
      return acc;
    }, {} as Record<string, string>);

    setResult('実行中...');
    setScriptOutput('');
    setSummaryOutput('');
    setRpmScicptSendSummary('');

    try {
      const headers: Record<string, string> = { 'Content-Type': 'application/json' };
      const response = await fetch(endpoint, {
        method,
        headers,
        body: JSON.stringify({ operation, ...params }),
      });
      const data = await response.json();
      setScriptOutput(data.scriptOutput || '');
      setSummaryOutput(data.summaryOutput || '');
      setRpmScicptSendSummary(data.rpmScriptAnalyzeLogAsJson || '');
      console.log('RpmScicptSendSummary:', data.rpmScriptAnalyzeLogAsJson);
      setStreCodeContent(data.streCodeContent || '');
      setOkOutput(data.okOutput || '');
      setNgOutput(data.ngOutput || '');
      setResult('✅ 実行完了');
      setStreCodeDownloadButton(true)
    } catch (err: any) {
      setResult('実行失敗：' + err.message);
    }
  };

  const currentInputs = operationTemplates[operation].inputs;



return (
  <div className={styles.container}>
    <h1 className={styles.title}>コマンドツール</h1>

    <div className={styles.section}>
      <div className={styles.leftPane}>
        <div>
          <label className={styles.label}>操作を選択：</label>
          <select
            className={styles.selectBox}
            value={operation}
            onChange={(e) => {
              setOperation(e.target.value);
              setFormValues({});
              setResult("");
              setScriptOutput("");
              setSummaryOutput("");
              setRpmScicptSendSummary("");
              setOkOutput("");
              setNgOutput("");
              setStreCodeContent("");
              setStreCodeDownloadButton(false);
            }}
          >
            {Object.entries(operationTemplates).map(([key, { label }]) => (
              <option key={key} value={key}>
                {label}
              </option>
            ))}
          </select>
        </div>
        <div className={styles.allInput}>
          {currentInputs.map(({ key, label, placeholder }) => (
            <div key={key}>
              <label className={styles.inputLabel}>{label}</label>
              <div className={styles.inputRow}>
                <input
                  className={styles.inputField}
                  value={formValues[key] || ""}
                  placeholder={placeholder || ""}
                  onChange={(e) => handleInputChange(key, e.target.value)}
                />

                {key === "allStreCodeFile" && showStreCodeDownloadButton && (
                  // <LogDownButton
                  //   label="StreCode.txt保存"
                  //   content={streCodeContent}
                  //   prefix="Rpmsend_"
                  //   suffix="_StreCode.txt"
                  //   mimeType="text/plain"
                  //   onAfterDownload={() =>
                  //     alert("StreCode.txtのダウンロードが完了しました。")
                  //   }
                  //   className={styles.fileSelectButton}
                  // />

<LogDownButton
  label="StreCode.txt保存"
  content={streCodeContent} // ✅ 任意保留原内容（如按钮预览）
  showConfirm={true}
  multiContent={[
    {
   content: getSjisBlob(streCodeContent),
      prefix: getPrefixByType(operation, "ok"),
      suffix: "_StreCode.txt",
      mimeType: "application/octet-stream",
    }
  ]}
  onAfterDownload={() => alert("StreCode.txtのダウンロードが完了しました。")}
  className={styles.fileSelectButton}
/>
                  






                  
                )}
                {key === "allStreCodeFile" && (
                  <button
                    className={styles.fileSelectButton}
                    onClick={() => handleFileListOpen()}
                  >
                    ファイル選択
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>

        <button
          onClick={() => setShowConfirmDialog(true)}
          className={styles.blueButton}
        >
          実行
        </button>

        {showConfirmDialog && (
          <ConfirmModal
            message={`指定店舗に${operationTemplates[operation].label} を実行します。よろしいでしょうか？`}
            onConfirm={() => {
              handleSubmit();
              setShowConfirmDialog(false);
            }}
            onCancel={() => setShowConfirmDialog(false)}
          />
        )}

        {result && <div className={styles.result}>{result}</div>}

        {(operation === "c1" || operation === "c2") && summaryOutput && (
          <div
            ref={outputRef}
            className={styles.summaryBlock}
            style={{ position: "relative" }}
          >
            {/* 🔘 右上のボタンエリア */}
            <div
              style={{
                position: "absolute",
                top: "0.75rem",
                right: "0.75rem",
                display: "flex",
                gap: "0.5rem",
              }}
            ></div>

            {/* 📄 実行統計タイトルと内容 */}
            <h2 className={styles.outputTitle}>📄 実行統計</h2>
            <pre
              className={styles.outputPre}
              dangerouslySetInnerHTML={{
                __html: summaryOutput.replace(
                  /(NG店舗数.*?:\s*)(\d+)/,
                  '$1<strong style="color: red;">$2</strong>',
                ),
              }}
            />
          </div>
        )}
      </div>

      {(operation === "b1" ||
        operation === "b2" ||
        operation === "b3" ||
        operation === "c1" ||
        operation === "c2" ||
        operation === "c3") &&
        scriptOutput && (
          <div ref={outputRef} className={styles.scriptBlock}>
            <h2 className={styles.outputTitle}>📄 スクリプト出力</h2>
            <pre className={styles.outputPre}>{scriptOutput}</pre>
            <LogDownButton
              label="保存"
              content={scriptOutput}
              showConfirm={true}
              multiContent={[
                {
                  content: scriptOutput,
                  prefix: getPrefixByType(operation, "main"),
                  suffix: ".log",
                  mimeType: "text/plain",
                },
                {
                  content: okOutput,
                prefix: getPrefixByType(operation, "ok"),
                  suffix: "_OK.txt",
                  mimeType: "text/plain",
                },
                {
                  content: ngOutput,
                  prefix: getPrefixByType(operation, "ok"),
                  suffix: "_NG.txt",
                  mimeType: "text/plain",
                },
              ]}
              onAfterDownload={() => alert("スクリプトの保存が完了しました。")}
              className={`${styles.fileSelectButton} absolute bottom-40 right-4`}
            />
          </div>
        )}
    </div>
    {showFileDialog && (
      <div className={styles.fileDialog}>
        <div className={styles.fileDialogContent}>
          <h2 className={styles.dialogTitle}>📂 ファイルを選択してください</h2>

          {fileList.length > 0 ? (
            <ul className={styles.fileList}>
              {fileList.map((file) => (
                <li key={file} className={styles.fileItem}>
                  <button
                    className={styles.fileButton}
                    onClick={() => handleSelectFile(file)}
                  >
                    {file}
                  </button>
                </li>
              ))}
            </ul>
          ) : (
            <p className={styles.noFile}>ファイルが見つかりませんでした。</p>
          )}

          <div className={styles.dialogFooter}>
            <button
              className={styles.dialogCloseButton}
              onClick={() => setShowFileDialog(false)}
            >
              閉じる
            </button>
          </div>
        </div>
      </div>
    )}
  </div>
);

}
