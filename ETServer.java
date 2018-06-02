import java.io.*;
import java.net.*;
import java.util.*;

public class ETServer {
  private static final int PORT = 8001; // 待受ポート番号
  private ServerSocket servSock; // サーバーソケット
  private ArrayList<ETUser> userList; // 接続しているクライアントユーザーの可変長配列

  // シングルトンパターン
  // ETServerインスタンスはただ1つ
  private static final ETServer servInstance = new ETServer();
  public static ETServer getServerInstance() {
    return ETServer.servInstance;
  }

  private ETServer() { // プライベートコンストラクタ
    userList = new ArrayList<ETUser>();
  }

  public static void main(String[] args) {
    ETServer server = ETServer.getServerInstance();
    try {
      server.start();
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void start() throws IOException{
    try {
      servSock = new ServerSocket(PORT);
      System.out.println("Started: " + servSock);
      while(!servSock.isClosed()) {
        Socket clientSock = servSock.accept(); // クライアントからの接続待機
        System.out.println("Connection accepted: " + clientSock);
        BufferedReader in =
          new BufferedReader(
              new InputStreamReader(
                clientSock.getInputStream()));
        String name = in.readLine();
        if(name != null) {
          ETUser user = new ETUser(clientSock, name);
          addUser(user);
        }
      }
    } catch(IllegalArgumentException iae) {
      System.out.println("Port parameter is illegal");
    }
  }

  public void addUser(ETUser user) {
    if(userList.contains(user)) return;
    userList.add(user);
    System.out.println(user.getName() + " came! " + "[" + user + "]");
  }
}
