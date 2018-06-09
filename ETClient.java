import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class ETClient extends JFrame implements Runnable {
  private static final String APPNAME = "EdiTogether!"; // App名
  private static final int PORT = 8001; // 接続先ポート番号
  private Socket socket; // 接続に用いるソケット
  private Thread thread; // 編集監視用スレッド
  private String myName; // ユーザーネーム
  private int myNumber; // ユーザー番号

  // Swingコンポーネント
  private JTextArea fileEditArea; // ファイルを編集するテキストエリア
  private JList userList; // クライアント(ユーザー)のリスト
  private JLabel filenameLabel; // ファイル名のラベル
  private JButton fileSelectShareButton; // ファイル選択or共有ボタン
  private JButton fileSaveButton; // ファイル保存ボタン
  private JButton fileEditButton; // ファイル編集ボタン
  private String filename; // 編集しているファイル名

  public static void main(String[] args) {
    ETClient window = new ETClient();
    window.setSize(800, 600);
    window.setVisible(true);
  }

  public ETClient() {
    super(APPNAME);

    connectServer();

    JPanel filePanel = new JPanel();
    JPanel userPanel = new JPanel();

    fileEditArea = new JTextArea();
    fileEditArea.getDocument().addDocumentListener(new ETListener(this, socket));
    userList = new JList();
    fileSelectShareButton = new JButton("Select file");
    fileSaveButton = new JButton("Save file");
    fileEditButton = new JButton("Edit");

    fileSelectShareButton.addActionListener(new ETListener(this, socket));
    fileSelectShareButton.setActionCommand("select");

    fileSaveButton.addActionListener(new ETListener(this, socket));
    fileSaveButton.setActionCommand("save");
    fileSaveButton.setEnabled(false);

    fileEditButton.addActionListener(new ETListener(this, socket));
    fileEditButton.setActionCommand("edit");

    filePanel.setLayout(new FlowLayout());
    filenameLabel = new JLabel("Let's select file");
    filePanel.add(filenameLabel);
    filePanel.add(fileSelectShareButton);
    filePanel.add(fileSaveButton);
    filePanel.add(fileEditButton);

    userPanel.setLayout(new BorderLayout());
    userPanel.add(new JLabel("Edit Friends"), BorderLayout.NORTH);
    userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

    if(myName != null) {
      setTitle(APPNAME + "  " + myName);
    } else {
      setTitle(APPNAME);
    }

    this.getContentPane().add(new JScrollPane(fileEditArea), BorderLayout.CENTER);
    this.getContentPane().add(filePanel, BorderLayout.NORTH);
    this.getContentPane().add(userPanel, BorderLayout.WEST);

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
    try {
      InetAddress addr = null;
      while(true) {
        System.out.print("Input IPAdress: ");
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
        OutputStream outStream = socket.getOutputStream();
        InputStream inStream = socket.getInputStream();
        PrintWriter out =
          new PrintWriter(
              new BufferedWriter(
                new OutputStreamWriter(outStream)), true);
        DataInputStream dis =
          new DataInputStream(inStream);
        System.out.print("Input USERNAME: ");
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
    try {
      InputStream inStream = socket.getInputStream();
      BufferedReader in =
        new BufferedReader(
            new InputStreamReader(inStream));
      while(!socket.isClosed()) {
        String msg = in.readLine();
        switch(msg) {
          case "CLOSE_SOCKET":
            if(socket != null) {
              socket.close();
              System.exit(0);
            }
            break;
        }
      }
    } catch(IOException ioe) {
      ioe.printStackTrace();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  void closeSocket(int userNumber) {
    try {
      OutputStream outStream = socket.getOutputStream();
      PrintWriter out =
        new PrintWriter(
            new BufferedWriter(
              new OutputStreamWriter(outStream)), true);
      out.println("REMOVE_USER");
      out.println(String.valueOf(userNumber));
    } catch(IOException ioe) {
      ioe.printStackTrace();
      System.exit(0);
    } catch(Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
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

  JButton getSelectShareButton() {
    return this.fileSelectShareButton;
  }

  JButton getSaveButton() {
    return this.fileSaveButton;
  }

  String getFileName() {
    return this.filename;
  }

  void setFileName(String filename) {
    this.filename = filename;
  }
}
