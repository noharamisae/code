import React from 'react';
import Encoding from "encoding-japanese";

type LogDownButtonProps = {
  label?: string;
  content?: string;
  fileName?: string;
  prefix?: string;
  suffix?: string;
  mimeType?: string;
  onAfterDownload?: () => void;
  className?: string;
  operation?: string; 
  multiContent?: {
    content: string | Blob;
    prefix: string;
    suffix: string;
    mimeType: string;
  }[];
  showConfirm?: boolean; // ✅ 追加：保存前に確認するかどうか（是否显示确认弹窗）
};

export const LogDownButton = ({
  label = '保存',
  content = '',
  fileName = '',
  prefix = 'Download_',
  suffix = '.txt',
  mimeType = 'text/plain',
  onAfterDownload = () => {},
  className = '',
  multiContent,
  showConfirm = false, // ✅ デフォルトはfalse（默认不开启确认）
}: LogDownButtonProps) => {

  // 👉 日時の文字列（格式化时间戳）
  const now = new Date();
  const pad = (n: number) => n.toString().padStart(2, '0');
  const formattedTime = `${now.getFullYear().toString().slice(2)}${pad(now.getMonth() + 1)}${pad(now.getDate())}${pad(now.getHours())}${pad(now.getMinutes())}`;
  const finalFileName = fileName || `${prefix}${formattedTime}${suffix}`;



  const handleClick = async () => {
   if (showConfirm) {
    const fileNames = multiContent?.map(item =>
      `${item.prefix}${formattedTime}${item.suffix}`
    ).join('\n');

    const confirmResult = window.confirm(
      `以下の${multiContent?.length || 4}ファイルを保存しますか？\n\n${fileNames}\n\n保存を続けますか？`
    );

    if (!confirmResult) {
      // ❌ キャンセルされた場合は中断（点击取消就不执行后续）
      return;
    }
  }

    // ✅ 複数ファイル保存処理
    if (multiContent && Array.isArray(multiContent)) {
      for (const item of multiContent) {
       if (typeof item.content === 'string' && !item.content.trim()) continue;
const blob = (globalThis.Blob && item.content instanceof Blob)
  ? item.content
  : new Blob([item.content], { type: item.mimeType });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${item.prefix}${formattedTime}${item.suffix}`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
      }

      onAfterDownload?.();
      return;
    }

    // ✅ 単一ファイル保存処理
    try {
      if ('showSaveFilePicker' in window) {
        const handle = await (window as any).showSaveFilePicker({
          suggestedName: finalFileName,
          types: [
            {
              description: 'テキストファイル',
              accept: { 'text/plain': ['.txt'] },
            },
          ],
        });
        const writable = await handle.createWritable();
        await writable.write(content);
        await writable.close();
      } else {
        const blob = new Blob([content], { type: mimeType });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = finalFileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
      }

      onAfterDownload?.();
    } catch (err) {
      console.error('❌ 保存失敗またはキャンセル:', err);
    }
  };

  return (
    <button onClick={handleClick} className={className}>
      {label}
    </button>
  );
};
