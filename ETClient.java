import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class ETClient extends JFrame {
  private static final String APPNAME = "EdiTogether!"; // App名
  private static String hostname; // ホスト名
  private static final int PORT = 8001; // 接続先ポート番号
  private Socket socket; // 接続に用いるソケット
  private Thread thread; // 編集監視用スレッド

  // Swingコンポーネント
  private JTextArea fileEditArea; // ファイルを編集するテキストエリア
  private JList userList; // クライアント(ユーザー)のリスト
  private JLabel filenameLabel; // ファイル名のラベル
  private JButton fileSelectButton; // ファイル選択ボタン
  private JButton fileSaveButton; // ファイル保存ボタン
  // private boolean isFileSelected; // ファイルが選択されているかどうか
  private String filename; // 編集しているファイル名

  public static void main(String[] args) {
    ETClient window = new ETClient();
    window.setSize(800, 600);
    window.setVisible(true);
  }

  public ETClient() {
    super(APPNAME);
    // isFileSelected = false;

    connectServer();

    JPanel filePanel = new JPanel();
    JPanel userPanel = new JPanel();

    fileEditArea = new JTextArea();
    userList = new JList();
    fileSelectButton = new JButton("Select file");
    fileSaveButton = new JButton("Save file");

    fileSelectButton.addActionListener(new ETListener(this));
    fileSelectButton.setActionCommand("select");

    fileSaveButton.addActionListener(new ETListener(this));
    fileSaveButton.setActionCommand("save");

    filePanel.setLayout(new FlowLayout());
    filenameLabel = new JLabel("Let's select file");
    filePanel.add(filenameLabel);
    filePanel.add(fileSelectButton);
    filePanel.add(fileSaveButton);

    userPanel.setLayout(new BorderLayout());
    userPanel.add(new JLabel("Edit Friends"), BorderLayout.NORTH);
    userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

    setTitle(APPNAME + " ");

    this.getContentPane().add(new JScrollPane(fileEditArea), BorderLayout.CENTER);
    this.getContentPane().add(filePanel, BorderLayout.NORTH);
    this.getContentPane().add(userPanel, BorderLayout.WEST);

    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        try {
          if(socket != null) {
            socket.close();
          }
        } catch(IOException ioe) {
          ioe.printStackTrace();
        } catch(Exception exception) {
          exception.printStackTrace();
        }
      }
    });
  }

  public void connectServer() {
    Scanner sc = new Scanner(System.in);
    try {
      System.out.print("Input hostname: ");
      hostname = sc.next();
      InetAddress addr = InetAddress.getByName(hostname); // IPアドレスの変換
      System.out.println("IP Adress: " + addr);
      Socket socket = new Socket(addr, PORT); // ソケット生成
      System.out.println("Socket: " + socket);
      if(socket.isBound()) {
        PrintWriter out =
          new PrintWriter(
              new BufferedWriter(
                new OutputStreamWriter(
                  socket.getOutputStream())), true);
        System.out.print("Input USERNAME: ");
        String name = sc.next();
        out.println(name);
      }
    } catch(UnknownHostException uhe) {
      uhe.printStackTrace();
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  JTextArea getTextArea() {
    return this.fileEditArea;
  }

  JLabel getLabel() {
    return this.filenameLabel;
  }

  // void setFileSelectStatus(boolean status) {
  //   this.isFileSelected = status;
  // }
}
