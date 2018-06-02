import java.io.*;
import java.net.*;

class ETUser {
  private Socket socket; // ソケット
  private String name; // ユーザーネーム
  private ETServer server = ETServer.getServerInstance(); // サーバー

  public ETUser(Socket clientSock, String name) {
    this.socket = clientSock;
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
