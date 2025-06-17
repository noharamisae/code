package com.example.remotessh.service;

import com.example.remotessh.dto.SshRequest;

import com.jcraft.jsch.*;

import org.springframework.stereotype.Service;

import java.io.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List; 

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors; // Stream用の収集用のユーティリティをインポート
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException; 
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.util.Base64;

@Service
public class SshSercive {
    //SSH接続時に使用する（監視サーバー）
    private static final String USERNAME = "root"; 
    private static final String PASSWORD = "web2100";

    //接続可能性のあるsshポート番号の候補リスト（優先順仁試す）
    private static final int[] PORT_CANDIDATES = { 22, 2222, 8022 }; 

    //監視サーバーしたの作業ディレクトリパス
    private static final String BASE_PATH = "/ts2100/share/SA3000/retail_tool/";

    //ssps画像送信コマンド
    public Map<String, String> executeSspsImageSend(String ip, String storeCode, String allStreCodeFile) { 
        String command = "./ImageFtpExec \"" + storeCode + "\" \"" + allStreCodeFile + "\"";
        return executeRemote(ip, command, true); 
    }

    //sspsスクリプト送信コマンド
    public Map<String, String> executeSspsScriptSend(String ip, String scriptName, String storeCode,
            String allStreCodeFile) { 
        String command = "./ShFtpExec \"" + storeCode + "\" \"" + scriptName + "\" \"" + allStreCodeFile + "\""; 
        return executeRemote(ip, command, false); 
    }

    //sspsチェック実行コマンド
    public Map<String, String> runSspsCheckSend(String ip, String storeCode, String allStreCodeFile) {
    String command = "./ChkImageExec \"" + storeCode + "\" \"" + allStreCodeFile + "\""; 
    Map<String, String> runSspsCheckSendresult = executeRemote(ip, command, true); 
    //元スクリプト出力
    String scriptOutput = runSspsCheckSendresult.get("scriptOutput");
    // System.out.println("scriptOutput: " + scriptOutput + "終わり"); // 出力元スクリプト出力

        // 統計情報の抽出
        if (scriptOutput != null && !scriptOutput.isEmpty()) {
        String summaryOutput = extractSummary(scriptOutput); 

        // 統計情報を結果集に追加
        runSspsCheckSendresult.put("summaryOutput", summaryOutput); 
    }
    return runSspsCheckSendresult;
}


    //rpm送信コマンド
        public Map<String, String> executeRpmSend(String ip, String storeCode, String allStreCodeFile) { 
        String command = "./RpmFtpExec \"" + storeCode + "\" \"" + allStreCodeFile + "\"";
        System.out.println("executeRpmSend: " + command);
         Map<String, String> runeRpmSendresult = executeRemote(ip, command, true); 
    //元スクリプト出力
    String scriptOutput = runeRpmSendresult.get("scriptOutput");

        // 統計情報の抽出
        if (scriptOutput != null && !scriptOutput.isEmpty()) {
        String summaryOutput = extractSummary(scriptOutput); 
          String okOutput = getOnlyOKStores(scriptOutput); 
         
          String ngOutput = getOnlyNGStores(scriptOutput); 
           System.err.println(    "executeRpmSend: okOutput: " + okOutput + "ngOutput: " + ngOutput);
        // 統計情報を結果集に追加
        runeRpmSendresult.put("summaryOutput", summaryOutput); 
        runeRpmSendresult.put("okOutput", okOutput); 
        runeRpmSendresult.put("ngOutput", ngOutput); 
    }
    return runeRpmSendresult;
    }

    //rpmチェック用スクリプト送信コマンド
            public Map<String, String> executeRpmScriptSend(String ip, String scriptName, String storeCode,
            String allStreCodeFile) { 
        String command = "./ShFtpExec \"" + storeCode + "\" \"" + scriptName + "\" \"" + allStreCodeFile + "\""; 
  Map<String, String> executeRpmResult = executeRemote(ip, command, false); 
    //元スクリプト出力
    String scriptOutput = executeRpmResult.get("scriptOutput");

      
        // 統計情報の抽出
        if (scriptOutput != null && !scriptOutput.isEmpty()) {
        String summaryOutput = extractRpmScriptSummary(scriptOutput); 
          String okOutput = getRpmScriptOKStores(scriptOutput); 
         
          String ngOutput = getRpmScriptOnlyNGStores(scriptOutput); 
           System.err.println(    "executeRpmSend: okOutput: " + okOutput + "ngOutput: " + ngOutput);
        // 統計情報を結果集に追加
        executeRpmResult.put("summaryOutput", summaryOutput); 
        executeRpmResult.put("okOutput", okOutput); 
        executeRpmResult.put("ngOutput", ngOutput); 
    }
    return executeRpmResult;
    }
        //sspsチェック実行コマンド
    public Map<String, String> runRpmCheckSend(String ip, String storeCode, String allStreCodeFile) {
    String command = "./ChkVerExec \"" + storeCode + "\" \"" + allStreCodeFile + "\""; 
    Map<String, String> runSspsCheckSendresult = executeRemote(ip, command, true); 
    //元スクリプト出力
    String scriptOutput = runSspsCheckSendresult.get("scriptOutput");
    // System.out.println("scriptOutput: " + scriptOutput + "終わり"); // 出力元スクリプト出力

        // 統計情報の抽出
        if (scriptOutput != null && !scriptOutput.isEmpty()) {
        String summaryOutput = extractSummary(scriptOutput); 


        // 統計情報を結果集に追加
        runSspsCheckSendresult.put("summaryOutput", summaryOutput); 
        String okOutput = getOnlyOKStores(scriptOutput);
        String ngOutput = getOnlyNGStores(scriptOutput);
        runSspsCheckSendresult.put("okOutput", okOutput);
        runSspsCheckSendresult.put("ngOutput", ngOutput);
 
    
    }
    return runSspsCheckSendresult;
}

// scriptOutputから送信数／OK数／NG店舗番号などを要約抽出する
private String extractSummary(String log) {
    int ok = 0;
    int ng = 0;
    int skip = 0;

    //NG店舗の情報を格納するリストを初期化する
    List<String> ngList = new ArrayList<>();

    //結果出力用のStringBuilder
    StringBuilder sb = new StringBuilder();

    //ログを一行ずつ処理
    for (String line : log.split("\n")) {
    line = line.trim();

    //　OK店舗数を取得
    if (line.contains("送信 OK 店舗数[")) {
        //文字列から数字以外の文字（[^0-9）をすべて空文字に置き換える
        ok = Integer.parseInt(line.replaceAll("[^0-9]", ""));
    }
    // NG店舗数を取得
     else if (line.contains("送信 NG 店舗数[")) {
        ng = Integer.parseInt(line.replaceAll("[^0-9]", ""));
    }
    // SKIP店舗数を取得
    else if (line.contains("送信SKIP店舗数[")) {
        skip = Integer.parseInt(line.replaceAll("[^0-9]", ""));
    }

    //　SHELL/FTP/PING　エラーを含む行をNGとみなし、NGリストへ追加
    if (
        line.contains("SHELL ERROR") ||
        line.contains("FTP ERROR") ||
        line.contains("PING ERROR")
    ) {

        //行を空白で分割する
        String[] parts = line.split("\\s+");
       //少なくともとも店舗番号と店舗名があることを確認
        if (parts.length >= 2) {
            //店舗情報を取り出す
            String code = parts[0];     
            String name = parts[1];   

            //フォーマットとして番号と名前を結合する
            String store = code + "　　" + name;
            //すでにリストに含まれていない場合、NGリストに追加
if (!ngList.contains(store)) {
    ngList.add(store);
}
        }
    }
     }

     //統計情報を出力用StringBuilderに追加
     int total = ok + ng;

     sb.append(String.format("%-10s: %-5d\n", "送信店舗数", total));
sb.append(String.format("%-12s: %-5d\n", "OK店舗数", ok));
sb.append(String.format("%-12s: %-5d\n", "NG店舗数", ng));


    //NG店舗番号のカウント用変数countを1で初期化します
    int count = 1;
    //NGリストngListの各行について繰り返し処理を行います
    for (String ngLine : ngList) {
sb.append(String.format("NG店舗No%-2d  %-20s\n", count++, ngLine));
    }

    return sb.toString();
}

// scriptOutputから送信数／OK数／NG店舗番号などを要約抽出する
private String extractRpmScriptSummary(String log) {
    int ok = 0;
    int ng = 0;
    int skip = 0;

    List<String> ngList = new ArrayList<>();
    List<String> okList = new ArrayList<>();

    StringBuilder sb = new StringBuilder();

    // 店舗ごとのブロックに分割 / 每个店铺为一个块
    String[] blocks = log.split("-{5,}");

    Pattern storeInfoPattern = Pattern.compile(
        "^(\\d{4})店目\\s+開始:(\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2})\\s+\\[(\\d{4})]\\s+\\[(.+?)]"
    );

    for (String block : blocks) {
        block = block.trim();
        if (block.isEmpty()) continue;

        String storeCode = "";
        String storeName = "";
        boolean hasError = false;
        boolean hasStart = false;
        boolean hasEnd = false;

        String[] lines = block.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.contains("開始:")) {
                hasStart = true;
                Matcher m = storeInfoPattern.matcher(line);
                if (m.find()) {
                    storeCode = m.group(3);
                    storeName = m.group(4);
                }
            }

            if (line.contains("終了:")) {
                hasEnd = true;
            }

            if (line.toUpperCase().contains("ERROR") || line.toUpperCase().endsWith("NG")) {
                hasError = true;
            }

            if (line.contains("スキップします")) {
                skip++;
                break;
            }
        }

        if (hasStart && hasEnd && hasError) {
            ng++;
            ngList.add(storeCode + "　　" + storeName);
        } else if (hasStart && hasEnd && !hasError) {
            ok++;
            okList.add(storeCode + "　　" + storeName);
        }
    }

    int total = ok + ng;
    sb.append(String.format("%-10s: %-5d\n", "送信店舗数", total));
    sb.append(String.format("%-12s: %-5d\n", "OK店舗数", ok));
    sb.append(String.format("%-12s: %-5d\n", "NG店舗数", ng));

    int count = 1;
    for (String ngLine : ngList) {
        sb.append(String.format("NG店舗No%-2d  %-20s\n", count++, ngLine));
    }

    return sb.toString();
}


    //SSH接続して任意のコマンドを実行し、標準出力を取得する
private Map<String, String> executeRemote(String ip, String command, boolean autoYes) {
    Map<String, String> resultMap = new HashMap<>();

    // 有効なポートを探す（複数候補を順に試す）  
    // 检测有效的 SSH 端口（多个候选）
    int sshPort = detectSshPort(ip);
    if (sshPort == -1) {
        resultMap.put("error", "SSHポートが見つかりませんでした");
        return resultMap;
    }

    try {
        // セッション準備（准备 SSH 会话）
        Session session = setupSession(ip, sshPort);
        session.connect();

        // 作業ディレクトリを移動した上でコマンドを実行（cd 后执行主命令）
        String fullCommand = "cd " + BASE_PATH + " && " + command;
        System.err.println("実行コマンド: " + fullCommand);

        // コマンドを実行し、結果を取得（执行脚本主命令）
        String scriptOutput = execRemoteCommand(session, fullCommand, autoYes);
        System.out.println("scriptOutput: " + scriptOutput + "終わり");

        resultMap.put("scriptOutput", scriptOutput); // 主输出内容

        // 🔽 StreCode.txtの内容をShift_JISで取得する（额外取得 StreCode.txt）
        String streCodePath = BASE_PATH + "/StreCode.txt";
        String base64 = execRemoteCommand(session, "base64 " + streCodePath, false);

        // 不要な空白や改行を除去（去掉空格/换行）
        String cleaned = base64.replaceAll("\\s+", "");

        // base64デコード → Shift_JIS に復元（解码 + 编码转换）
        byte[] bytes = Base64.getDecoder().decode(cleaned);
        String decodedText = new String(bytes, Charset.forName("Shift_JIS"));

        System.out.println("✅ StreCode.txt内容:\n" + decodedText.trim());
        resultMap.put("streCodeContent", decodedText.trim());

        // セッションを閉じる
        session.disconnect();

    } catch (Exception e) {
        System.err.println("例外発生: " + e.getMessage());
        e.printStackTrace();
        resultMap.put("error", e.getMessage());
    }

    return resultMap;
}


    // SSH接続可能なポートを探す
    private int detectSshPort(String ip) { 

        // 候補ポートを順に試す
        for (int port : PORT_CANDIDATES) { 

            //ソケットで対象IPとポート接続を試みる（三秒タイムアウト）
            try (Socket socket = new Socket()) { 
                socket.connect(new InetSocketAddress(ip, port), 3000); 
                //接続成功ならこのポートを返す
                return port; 
                //接続失敗時何もしない（次のポートへ）
            } catch (IOException ignored) { 
            }
        }
        //すべて失敗した場合は-1を返す
        return -1; 
    }

    // SSH接続セッションをセットアップする（clintと監視サーバー間のSSH接続）
    private Session setupSession(String ip, int port) throws JSchException { 

        // JSchを使ってSSH接続セッションをセットアップ
        JSch jsch = new JSch(); 

        // 指定したIP ポートでセッションを生成
        Session session = jsch.getSession(USERNAME, ip, port); 

        //パスワード認定設定
        session.setPassword(PASSWORD);

        //ホスト鍵チェックを無効
        session.setConfig("StrictHostKeyChecking", "no");

        //準備完了したセッションを返す
        return session; 
    }

    // SSH接続セッションを使ってコマンドを実行し、標準出力を取得する
    private String execRemoteCommand(Session session, String command, boolean autoYes) throws Exception {

        // 実行通道を開く
        ChannelExec channel = (ChannelExec) session.openChannel("exec"); 

        // 実行コマンドを設定
        channel.setCommand(command); 

        // 標準入力を無効化
        channel.setInputStream(null); 

        // リモートコマンドをの出力を読取ための入力ストリーム
        InputStream in = channel.getInputStream(); 

        //応答送信用のストリーム
        OutputStream out = channel.getOutputStream(); 

        //実行開始
        channel.connect();
        if (autoYes) { 
            //応答待ちのため1秒待機
            Thread.sleep(1000); 
            //自動で[yes]と入力
            out.write("yes\n".getBytes());
            //即時送信
            out.flush();
        }

        // EUC-JPで出力内容を読み取る（日本語対応）
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("EUC-JP")));

        //BufferedReader から1行ずつ読み込んで、Stream<String> 生成し、そしてすべての行を改行で結合して。一人ずつの文字列にします。
        String result = reader.lines().collect(Collectors.joining("\n")); 
        channel.disconnect(); 
        return result; 
    }

    //アドレスとディレクトリパスを受け取り、ファイルの名のリストを返すメソッドです。
    public List<String> getRemoteFileList(String ip, String directory) {
        //監視サーバーログ
        String username = "root";
        String password = "web2100";
        int[] ports = { 22, 2222, 8022 }; 

        //取得したファイルを格納するリスト
        List<String> fileList = new ArrayList<>();

        //ポートを一つずつ試します
        for (int port : ports) {
            try {
                JSch jsch = new JSch();
                //JSchでSSHセッションを初期化
                Session session = jsch.getSession(username, ip, port);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect(3000); 
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand("ls " + directory); 
                channel.setInputStream(null);
                InputStream input = channel.getInputStream();
                channel.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line;
                while ((line = reader.readLine()) != null) {
                    fileList.add(line.trim());
                }
                channel.disconnect();
                session.disconnect();
                break; 

            } catch (Exception e) {
            }
        }

        return fileList;
    }

// RPM送信 OK 店舗一覧の抽出メソッド（String 形式で返す）
public String getOnlyOKStores(String log) {
    StringBuilder sb = new StringBuilder();
    int okCount = 0;
    List<String> okLines = new ArrayList<>();

    for (String line : log.split("\n")) {
        line = line.trim();

        // 统计 OK 数量
        if (line.contains("送信 OK 店舗数[")) {
            okCount = Integer.parseInt(line.replaceAll("[^0-9]", ""));
        }

        // 提取每一行实际含 OK 的门店
        if (line.endsWith("OK")) {
            String[] parts = line.split("\\s+");
            if (parts.length >= 2) {
                String code = parts[0];     // 门店编号
                String name = parts[1];     // 店名（简化为第2列）
                okLines.add(code + "\t" + name + "\tOK");
            }
        }
    }

    sb.append("OK店舗数　　　　　　　").append(okCount).append("\n");
    int index = 1;
    for (String store : okLines) {
        sb.append("No.").append(index++).append("\t").append(store).append("\n");
    }

    System.err.println("OK店舗一覧: " + sb.toString());
    return sb.toString();
}

// RPM送信 OK 店舗一覧の抽出メソッド（String 形式で返す）
public String getRpmScriptOKStores(String log) {
    StringBuilder sb = new StringBuilder();
    int okCount = 0;
    List<String> okLines = new ArrayList<>();

    // ✅ 正規表現：完整に1行に開始〜終了＋店番＋店名があるものを抽出
    Pattern pattern = Pattern.compile(
        "^(\\d{4})店目\\s+開始:(\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2})\\s+\\[(\\d{4})]\\s+\\[(.+?)]\\s+終了:(\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2})"
    );

    for (String line : log.split("\n")) {
        line = line.trim();

        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            okCount++;  // ✅ マッチすれば OK 店舗としてカウント

            String storeNo = matcher.group(1);
            String startTime = matcher.group(2);
            String storeCode = matcher.group(3);
            String storeName = matcher.group(4);
            String endTime = matcher.group(5);

            // ✅ 店舗ごとの情報をリストに追加
            okLines.add(storeCode + "\t" + storeName + "\tOK");
        }
    }

    // ✅ 出力の組み立て
    sb.append("OK店舗数　　　　　　　").append(okCount).append("\n");
    int index = 1;
    for (String store : okLines) {
        sb.append("No.").append(index++).append("\t").append(store).append("\n");
    }

    System.err.println("OK店舗一覧: " + sb.toString());
    return sb.toString();
}



 // NG 店舗一覧ログファイル出力 / 生成 NG 店铺列表日志文本
public String getRpmScriptOnlyNGStores(String log) {
    StringBuilder sb = new StringBuilder();
    List<String> ngLines = new ArrayList<>();
    int index = 1;

    // 1店舗ごとのブロックで分割 / 按照门店块分割
    String[] blocks = log.split("-{10,}");

    for (String block : blocks) {
        block = block.trim();
        if (block.isEmpty()) continue;

        // エラーがあるブロックのみ対象 / 只处理含 ERROR 的块
        if (!(block.contains("ERROR") || block.contains("Error") || block.contains("NG"))) {
            continue;
        }

        String[] lines = block.split("\n");
        String storeCode = "", storeName = "", startTime = "", endTime = "";
        List<String> reasons = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();

            if (line.contains("開始:")) {
                Pattern p = Pattern.compile(
                    "^(\\d{4})店目\\s+開始:(\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2})\\s+\\[(\\d{4})]\\s+\\[(.+?)]"
                );
                Matcher m = p.matcher(line);
                if (m.find()) {
                    startTime = m.group(2);
                    storeCode = m.group(3);
                    storeName = m.group(4);
                }
            }

            if (line.startsWith("終了:") || line.contains("終了:")) {
                endTime = line.replace("終了:", "").trim();
            }

            if (line.toUpperCase().contains("ERROR")) {
                reasons.add(line);
            }
        }

        // 出力形式に合わせる / 输出格式
        if (!reasons.isEmpty()) {
            sb.append(String.format(
                "NG店舗No%d　店舗番号:%s　店舗名:%s　実行時間: %s ～ %s　理由: %s\n\n",
                index++, storeCode, storeName, startTime, endTime,
                String.join(" ", reasons)
            ));
        }
    }

    sb.insert(0, String.format("NG店舗数　　　: %d\n\n", index - 1));
    return sb.toString();
}

// NG 店舗一覧ログファイル出力 / 生成 NG 店铺列表日志文本
public String getOnlyNGStores(String log) {
    StringBuilder sb = new StringBuilder(); // 出力バッファ / 输出缓存
    int ngCount = 0;
    List<String> ngLines = new ArrayList<>();
    int index = 1;

    for (String line : log.split("\n")) {
        line = line.trim();
        // NG 店舗数の取得 / 获取 NG 店铺总数
        if (line.contains("送信 NG 店舗数[")) {
            ngCount = Integer.parseInt(line.replaceAll("[^0-9]", ""));
        }

        // NG の行の判定 / 判断是 NG 行
        if (line.contains("SHELL ERROR") || line.contains("FTP ERROR") ||
            line.contains("PING ERROR") || line.endsWith("NG")) {

            int timeEndIdx = line.indexOf("]");
            if (timeEndIdx != -1 && timeEndIdx + 1 < line.length()) {
                String before = line.substring(0, timeEndIdx + 1).trim();  // 所要時間まで
                String after = line.substring(timeEndIdx + 1).trim();      // 以降がNG理由

                // NG 情報のパース元は before 部分
                String[] parts = before.split("\\s+");

                if (parts.length >= 7) {
                    String storeCode = parts[0];          // 店舗番号 / 门店编号
                    String storeName = parts[1];          // 店舗名（略）/ 门店名称
                    String startTime = parts[4];          // 開始時間
                    String endTime = parts[5];            // 終了時間
                    String duration = parts[6];           // 所要時間
                    String reason = after;                // NG理由

                    // NG行フォーマット出力 / 输出格式化 NG 行
                    ngLines.add(String.format(
                        "NG店舗No%-2d  店舗番号:%-5s  店舗名:%-10s  実行時間: %s ～ %s %s  理由: %s",
                        index++, storeCode, storeName, startTime, endTime,
                        duration.replaceAll("[\\[\\]]", ""), reason
                    ));
                }
            }
        }
    }

    // NG数とNG行まとめて出力 / 汇总 NG 店铺数与详细记录
    sb.append(String.format("NG店舗数       : %d\n\n", ngCount));
    for (String line : ngLines) {
        sb.append(line).append("\n\n");
    }

    return sb.toString();
}

    // SKIP 店铺列表
    public String getOnlySkipStores(String log) {
        StringBuilder sb = new StringBuilder();
        int skipCount = 0;
        List<String> skipLines = new ArrayList<>();

        for (String line : log.split("\n")) {
            line = line.trim();

            if (line.contains("送信SKIP店舗数[")) {
                skipCount = Integer.parseInt(line.replaceAll("[^0-9]", ""));
            }

            if (line.endsWith("SKIP")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    skipLines.add(parts[0] + "\t" + parts[1] + "\tSKIP");
                }
            }
        }

        sb.append("SKIP店舗数　　　　　　").append(skipCount).append("\n");
        int index = 1;
        for (String store : skipLines) {
            sb.append("No.").append(index++).append("\t").append(store).append("\n");
        }

        return sb.toString();
    }





}
