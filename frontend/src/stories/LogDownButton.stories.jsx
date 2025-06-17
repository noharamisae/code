import React from 'react';
import {LogDownButton} from '../LogDownButton';

export default {    
  title: 'BUTTON/DownloadButton',
  component: LogDownButton,
  argTypes: {
    label: { control: 'text' },
    content: { control: 'text' },
    fileName: { control: 'text' },
    mimeType: { control: 'text' },
  },
};

//ベースとなるテンプレート定義
const Template = (args) => <LogDownButton {...args} />;

//通常バタン（ダウンロード）
export const Default = Template.bind({});
Default.args = {
  label: '保存',
  content: 'これはログの内容です。\n2行目のテキスト。',
  fileName: 'log.txt',
  mimeType: 'text/plain',
};