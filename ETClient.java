import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.Document;

public class ETClient extends JFrame implements Runnable {
  private static final String APPNAME = "EdiTogether!"; // App名
  private static final int PORT = 8001; // 接続先ポート番号
  private final String FILESEP = "::!::"; // ファイル区切り文字列
  private final String SENDPHRASE = "Send message >> "; // 送信句
  private final String ARRIVEDPHRASE = "Arrived message << "; // 受信句
  private Socket socket; // 接続に用いるソケット
  private ETListener listener; // リスナ
  private String myName; // ユーザーネーム
  private int myNumber; // ユーザー番号
  private String filename; // 編集しているファイル名
  private Thread thread; // サーバーからの情報監視用スレッド

  // Swingコンポーネント
  private JTextArea fileEditArea; // ファイルを編集するテキストエリア
  private JList userList; // クライアント(ユーザー)のリスト
  private JLabel filenameLabel; // ファイル名のラベル
  private JLabel editorLabel; // 現在編集しているユーザのラベル
  private JButton fileSelectButton; // ファイル選択ボタン
  private JButton fileSaveButton; // ファイル保存ボタン
  private JButton fileEditButton; // ファイル編集ボタン

  public static void main(String[] args) {
    ETClient window = new ETClient();
    window.setSize(800, 800);
    window.setVisible(true);
  }

  public ETClient() {
    super(APPNAME);

    connectServer();

    JPanel filePanel = new JPanel();
    JPanel userPanel = new JPanel();
    userPanel.setPreferredSize(new Dimension(150, 800));

    listener = new ETListener(this, socket);
    fileEditArea = new JTextArea();
    fileEditArea.setEditable(false);
    enableDocumentListener(fileEditArea.getDocument());
    userList = new JList();
    fileSelectButton = new JButton("Select file");
    fileSaveButton = new JButton("Save file");
    fileEditButton = new JButton("Begin Edit");
    UIManager.put("FileChooser.saveButtonText", "Save");
    UIManager.put("FileChooser.saveButtonToolTipText", "Save the file");
    UIManager.put("FileChooser.openButtonText", "Open");
    UIManager.put("FileChooser.cancelButtonText", "Cancel");

    fileSelectButton.addActionListener(listener);
    fileSelectButton.setActionCommand("select");

    fileSaveButton.addActionListener(listener);
    fileSaveButton.setActionCommand("save");
    fileSaveButton.setEnabled(false);

    fileEditButton.addActionListener(listener);
    fileEditButton.setActionCommand("begin_edit");

    filePanel.setLayout(new FlowLayout());
    filenameLabel = new JLabel("Let's select file");
    editorLabel = new JLabel("Nobody is editing.");
    filePanel.add(filenameLabel);
    filePanel.add(fileSelectButton);
    filePanel.add(fileSaveButton);
    filePanel.add(fileEditButton);
    filePanel.add(editorLabel);

    userPanel.setLayout(new BorderLayout());
    userPanel.add(new JLabel("Edit Friends"), BorderLayout.NORTH);
    userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

    this.getContentPane().add(new JScrollPane(fileEditArea), BorderLayout.CENTER);
    this.getContentPane().add(filePanel, BorderLayout.NORTH);
    this.getContentPane().add(userPanel, BorderLayout.WEST);

    if(myName != null) {
      setTitle(APPNAME + "  " + myName);
    } else {
      setTitle(APPNAME);
    }

    sendData("PULL_CURRENT" + " " + (String.valueOf(myNumber)), true);

    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        closeSocket(myNumber);
      }
    });

    thread = new Thread(this);
    thread.start();
  }

  public void connectServer() {
    Scanner sc = new Scanner(System.in);
    OutputStream outStream = null;
    InputStream inStream = null;
    PrintWriter out = null;
    DataInputStream dis = null;
    try {
      InetAddress addr = null;
      while(true) {
        System.out.print("Input IPAdress > ");
        try {
          addr = InetAddress.getByName(sc.next()); // IPアドレスの変換
          System.out.println("IP Adress: " + addr);
          break;
        } catch(UnknownHostException uhe) {
          System.out.println("IP Adress not found.");
        } catch(SecurityException se) {
          se.printStackTrace();
          System.exit(0);
        } catch(Exception e) {
          e.printStackTrace();
          System.exit(0);
        }
      }
      socket = new Socket(addr, PORT); // ソケット生成
      System.out.println("Socket: " + socket);
      if(socket.isBound()) {
        outStream = socket.getOutputStream();
        inStream = socket.getInputStream();
        out = new PrintWriter(
                new BufferedWriter(
                  new OutputStreamWriter(outStream)), true);
        dis = new DataInputStream(inStream);
        System.out.print("Input USERNAME > ");
        myName = sc.next();
        out.println(myName);
        myNumber = dis.readInt();
      }
    } catch(IOException ioe) {
      ioe.printStackTrace();
      System.exit(0);
    } catch(Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

  @Override
  public void run() { // サーバーからの情報に対して処理
    InputStream inStream = null;
    BufferedReader in = null;
    try {
      inStream = socket.getInputStream();
      in = new BufferedReader(
            new InputStreamReader(inStream));
      while(!socket.isClosed()) {
        String words = in.readLine();
        String[] data = words.split(" ", 2);
        String msg = data[0];
        String value = (data.length <= 1 ? "" : data[1]);
        arrivedData(msg, value);
      }
    } catch(IOException ioe) {
      ioe.printStackTrace();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  void arrivedData(String msg, String value) throws IOException {
    String data;
    switch(msg) {
      case "ENABLE_EDITAREA":
        System.out.println(ARRIVEDPHRASE + msg);
        System.out.println();
        fileEditArea.setEditable(true);
        break;
      case "DISABLE_EDITAREA":
        System.out.println(ARRIVEDPHRASE + msg);
        System.out.println();
        fileEditArea.setEditable(false);
        break;
      case "GIVE_CONTENT":
        System.out.println(ARRIVEDPHRASE + msg);
        System.out.println();
        data = "SET_CONTENT" + " " + value + " "  + getContent();
        sendData(data, false);
        break;
      case "GIVE_FILENAME":
        System.out.println(ARRIVEDPHRASE + msg);
        System.out.println();
        data = "SET_FILENAME" + " " + value + " " + getFileName();
        sendData(data, true);
        break;
      case "CLOSE_SOCKET":
        System.out.println(ARRIVEDPHRASE + msg);
        System.out.println();
        if(socket != null) {
          socket.close();
          System.exit(0);
        }
        break;
      case "SET_USER_LIST":
        System.out.println(ARRIVEDPHRASE + msg);
        System.out.println();
        if(value.equals("")) {
          userList.setModel(new DefaultListModel());
        } else {
          String[] usernameList = value.split(" ");
          userList.setListData(usernameList);
        }
        break;
      case "SET_CONTENT":
        System.out.println(ARRIVEDPHRASE + msg);
        System.out.println();
        disableDocumentListener(fileEditArea.getDocument());
        fileEditArea.setText(value.replaceAll(FILESEP, "\n"));
        enableDocumentListener(fileEditArea.getDocument());
        break;
      case "SET_FILE_CONTENT":
        System.out.println(ARRIVEDPHRASE + msg);
        System.out.println();
        disableDocumentListener(fileEditArea.getDocument());
        fileEditArea.setText("");
        String[] lines = value.split(FILESEP, 0);
        for(String line : lines) {
          fileEditArea.append(line + "\n");
        }
        int fileLen = fileEditArea.getText().length();
        fileEditArea.replaceRange("", fileLen-1, fileLen);
        enableDocumentListener(fileEditArea.getDocument());
        fileSaveButton.setEnabled(true);
        break;
      case "SET_FILE_INFO":
        System.out.println(ARRIVEDPHRASE + msg + " " + value);
        System.out.println();
        setFileName(value);
        fileSaveButton.setEnabled(true);
        break;
      case "INSERT_PARTIAL":
        disableDocumentListener(fileEditArea.getDocument());
        System.out.println(ARRIVEDPHRASE + msg + " " + value);
        System.out.println();
        String[] insertData = value.split(" ", 0);
        String insertStr = insertData[0];
        int pos = Integer.parseInt(insertData[1]);
        switch(insertStr) {
          case "SPACE":
            fileEditArea.insert(" ", pos);
            break;
          case "TAB":
            fileEditArea.insert("\t", pos);
            break;
          case "NEWLINE":
            fileEditArea.insert("\n", pos);
            break;
          default:
            fileEditArea.insert(insertStr, pos);
        }
        enableDocumentListener(fileEditArea.getDocument());
        break;
      case "REMOVE_PARTIAL":
        disableDocumentListener(fileEditArea.getDocument());
        System.out.println(ARRIVEDPHRASE + msg + " " + value);
        System.out.println();
        String[] removeData = value.split(" ", 0);
        int removeLength = Integer.parseInt(removeData[0]);
        int removePos = Integer.parseInt(removeData[1]);
        int start = removePos - removeLength + 1;
        int end = removePos + 1;
        fileEditArea.replaceRange("", start, end);
        enableDocumentListener(fileEditArea.getDocument());
        break;
      case "DISABLE_EDIT_BUTTON":
        fileEditButton.setEnabled(false);
        editorLabel.setText(value + " is editing.");
        break;
      case "ENABLE_EDIT_BUTTON":
        fileEditButton.setEnabled(true);
        editorLabel.setText("Nobody is editing.");
        break;
      default:
        System.out.println("DON'T MATCH");
    }
  }

  void sendData(String data, boolean detail) {
    OutputStream outStream = null;
    PrintWriter out = null;
    if(detail) System.out.println(SENDPHRASE + data + "\n");
    else System.out.println(SENDPHRASE + data.split(" ", 2)[0] + "\n");
    try {
      outStream = socket.getOutputStream();
      out = new PrintWriter(
              new BufferedWriter(
                new OutputStreamWriter(outStream)), true);
      out.println(data);
    } catch(IOException ioe) {
      ioe.printStackTrace();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  void closeSocket(int userNumber) {
    String data = "REMOVE_USER" + " " + (String.valueOf(userNumber));
    sendData(data, true);
  }

  void enableDocumentListener(Document doc) {
    doc.addDocumentListener(listener);
  }

  void disableDocumentListener(Document doc) {
    doc.removeDocumentListener(listener);
  }

  Socket getSocket() {
    return this.socket;
  }

  JTextArea getTextArea() {
    return this.fileEditArea;
  }

  JLabel getFileNameLabel() {
    return this.filenameLabel;
  }

  JLabel getEditorLabel() {
    return this.editorLabel;
  }

  JButton getSelectButton() {
    return this.fileSelectButton;
  }

  JButton getSaveButton() {
    return this.fileSaveButton;
  }

  JButton getEditButton() {
    return this.fileEditButton;
  }

  String getContent() {
    String fileTxt = fileEditArea.getText();
    int fileLen = fileTxt.length();
    if(fileTxt.equals("")) {
      String[] lines = fileTxt.split("\n");
      return String.join(FILESEP, lines);
    } else {
      if(fileTxt.charAt(fileLen-1) == '\n') {
        String fileSeps = "";
        int i = fileLen-1;
        while(fileTxt.charAt(i) == '\n') {
          fileSeps += FILESEP;
          i--;
        }
        String[] lines = fileTxt.split("\n");
        return String.join(FILESEP, lines) + fileSeps;
      } else {
        String[] lines = fileTxt.split("\n");
        return String.join(FILESEP, lines);
      }
    }
  }

  String getMyName() {
    return this.myName;
  }

  String getFileName() {
    return this.filename;
  }

  void setFileName(String filename) {
    this.filename = filename;
    this.filenameLabel.setText(filename);
  }
}
