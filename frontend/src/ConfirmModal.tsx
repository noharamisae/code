import React from 'react';
import styles from './styles/ConfirmModal.module.css';

type ConfirmModalProps = {
  message: string;             // 表示するメッセージ
  onConfirm: () => void;       // 「確認」ボタン押下時の処理 
  onCancel: () => void;        // 「キャンセル」ボタン押下時の処理 
};

const ConfirmModal: React.FC<ConfirmModalProps> = ({ message, onConfirm, onCancel }) => {
  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent}>
        <h2 className={styles.modalTitle}>確認</h2>
        <p className={styles.modalMessage}>{message}</p>
        <div className={styles.modalButtons}>
          <button className={styles.confirmButton} onClick={onConfirm}>
            確認 {/* 确认 */}
          </button>
          <button className={styles.cancelButton} onClick={onCancel}>
            キャンセル {/* 取消 */}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmModal;
