import java.io.*;
import java.net.*;
import java.util.*;

public class ETServer {
  private static final int PORT = 8001; // 待受ポート番号
  private ServerSocket servSock; // サーバーソケット
  // private ArrayList<ETUser> userMap; // 接続しているクライアントユーザーの可変長配列
  private Map<Integer, ETUser> userMap; // 接続しているクライアントユーザーのハッシュマップ
  private int userCount; // 接続したクライアントの数

  // シングルトンパターン
  // ETServerインスタンスはただ1つ
  private static ETServer servInstance = new ETServer();
  private ETServer() { // プライベートコンストラクタ
    userMap = new HashMap<Integer, ETUser>();
    userCount = 0;
  }
  public static ETServer getServerInstance() {
    return servInstance;
  }

  public static void main(String[] args) {
    ETServer server = ETServer.getServerInstance();
    try {
      server.start();
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void start() throws IOException {
    try {
      servSock = new ServerSocket(PORT);
      System.out.println("Started: " + servSock);
      System.out.println();
      while(!servSock.isClosed()) {
        Socket clientSock = servSock.accept(); // クライアントからの接続待機
        System.out.println("Connection accepted: " + clientSock);
        InputStream inStream = clientSock.getInputStream();
        OutputStream outStream = clientSock.getOutputStream();
        BufferedReader in =
          new BufferedReader(
              new InputStreamReader(inStream));
        DataOutputStream dos =
          new DataOutputStream(outStream);
        // PrintWriter out =
        //   new PrintWriter(
        //       new BufferedWriter(
        //         new OutputStreamWriter(
        //           clientSock.getOutputStream())), true);
        String name = in.readLine();
        System.out.println("name: " + name);
        if(name != null) {
          ETUser user = new ETUser(clientSock, name);
          addUser(clientSock, user);
          dos.writeInt(userCount);
        }
        // String msg = in.readLine();
        // System.out.println("Message came: " + msg);
        // switch (msg) {
        //   case "ADD_USER":
        //     String name = in.readLine();
        //     if(name != null) {
        //       ETUser user = new ETUser(clientSock, name);
        //       addUser(clientSock, user);
        //       dos.writeInt(userCount);
        //     }
        //     break;
        //   case "REMOVE_USER":
        //     System.out.println("case: REMOVE");
        //     int userNo = dis.readInt();
        //     System.out.println("remove user no: " + userNo);
        //     removeUser(userNo);
        //     dos.writeInt(1);
        //     break;
        // }
      }
    } catch(IllegalArgumentException iae) {
      System.out.println("Port parameter is illegal");
    }
  }

  void addUser(Socket socket, ETUser user) {
    if(userMap.containsValue(user)) return;
    userCount++;
    userMap.put(userCount, user);
    System.out.println(user.getName() + " came! " + "[" + user + "]");
    displayUserList();
  }

  void removeUser(int userNo) {
    ETUser user = userMap.get(userNo);
    System.out.println(user.getName() + " disappear... " + "[" + user + "]");
    userMap.remove(userNo);
    displayUserList();
  }

  void displayUserList() {
    System.out.println("<User List>");
    for(Integer key : userMap.keySet()) {
      System.out.print(userMap.get(key).getName() + ", ");
    }
    System.out.println();
    System.out.println();
  }

  void outUser(ETUser user) {

  }

  void shareInsertStr(ETUser self, int offset, String insertStr) {
    
  }

  ArrayList getUserList() {
    ArrayList<ETUser> userList = new ArrayList<ETUser>(userMap.values());
    return userList;
  }
}

class ETUser implements Runnable {
  private Socket socket; // ソケット
  private String name; // ユーザーネーム
  private ETServer server = ETServer.getServerInstance(); // サーバー
  private List<EditAreaListener> listeners; // リスナの可変長配列

  public ETUser(Socket clientSock, String name) {
    this.socket = clientSock;
    this.name = name;

    Thread thread = new Thread(this);
    thread.start();
  }

  @Override
  public void run() {
    try {
      InputStream inStream = socket.getInputStream();
      OutputStream outStream = socket.getOutputStream();
      BufferedReader in =
        new BufferedReader(
            new InputStreamReader(inStream));
      DataInputStream dis =
        new DataInputStream(inStream);
      DataOutputStream dos =
        new DataOutputStream(outStream);

      String msg = in.readLine();
      System.out.println("Message came: " + msg);
      switch(msg) {
        case "REMOVE_USER":
          System.out.println("This is REMOVE_USER case");
          int userNo = dis.readInt();
          System.out.println("remove user no: " + userNo);
          server.removeUser(userNo);
          dos.writeInt(1);
          dos.flush();
          break;
      }
    } catch(IOException ioe) {
      ioe.printStackTrace();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public String getName() {
    return this.name;
  }
}
