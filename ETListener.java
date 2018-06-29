import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

class ETListener implements ActionListener, EditAreaListener {
  private final String FILESEP = "::!::"; // ファイル区切り文字列
  private final String SENDPHRASE = "Send message >> "; // 送信句
  private ETClient window; // クライアント(GUI情報)
  private Socket socket; // 接続に用いるソケット
  private enum SendType { // 送信する内容の識別子
    FILE_CONTNET,
    FILE_INFO,
    PARTIAL_INSERT,
    PARTIAL_REMOVE,
    EDIT_INFO
  }

  public ETListener(ETClient window, Socket socket) {
    this.window = window;
    this.socket = socket;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    Container container = window.getContentPane();
    JFileChooser fc = new JFileChooser();
    int btn_status = 99;

    try {
      if(cmd.equals("select")) {
        fc.setApproveButtonText("Edit Together!");
        fc.setApproveButtonToolTipText("Share the selected file with the user");
        fc.setDialogTitle("File Open");
        btn_status = fc.showOpenDialog(container);
      } else if(cmd.equals("save")) {
        fc.setSelectedFile(new File(window.getFileName()));
        fc.setDialogTitle("File Save");
        btn_status = fc.showSaveDialog(container);
      } else if(cmd.equals("begin_edit")) {
        window.getTextArea().setEditable(true);
        window.getEditorLabel().setText(window.getMyName() + " is editing.");
        sendData("BEGIN_EDIT", SendType.EDIT_INFO);
        window.getEditButton().setText("Finish Edit");
        window.getEditButton().setActionCommand("finish_edit");
      } else if(cmd.equals("finish_edit")) {
        window.getTextArea().setEditable(false);
        window.getEditorLabel().setText("Nobody is editing.");
        sendData("FINISH_EDIT", SendType.EDIT_INFO);
        window.getEditButton().setText("Begin Edit");
        window.getEditButton().setActionCommand("begin_edit");
      } else {
        return;
      }
    } catch(IOException ioe) {
      ioe.printStackTrace();
    } catch(Exception exception) {
      exception.printStackTrace();
    }

    if(btn_status != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File file = fc.getSelectedFile();
    JTextArea fileEditArea = window.getTextArea();
    JButton fileSelectButton = window.getSelectButton();
    if(cmd.equals("select")) {
      String filename = fc.getName(file);
      JLabel filenameLabel = window.getFileNameLabel();
      filenameLabel.setText(filename);
      window.setFileName(filename);
      BufferedReader br = null;
      try {
        br = new BufferedReader(
              new FileReader(file));
        window.disableDocumentListener(fileEditArea.getDocument());
        fileEditArea.read(br, null);
        int fileLen = fileEditArea.getText().length();
        fileEditArea.replaceRange("", fileLen-1, fileLen);
        window.getSaveButton().setEnabled(true);
        window.enableDocumentListener(fileEditArea.getDocument());
        String fileStr = getFileStr(file);
        sendData("SHARE_FILE_CONTENT" + " " + fileStr, SendType.FILE_CONTNET);
        sendData("SHARE_FILE_INFO" + " " + filename, SendType.FILE_INFO);
      } catch(FileNotFoundException fnfe) {
        fnfe.printStackTrace();
      } catch(IOException ioe) {
        ioe.printStackTrace();
      }
    } else if(cmd.equals("save")) {
      BufferedWriter bw = null;
      try {
        bw =
          new BufferedWriter(
              new FileWriter(file));
        fileEditArea.write(bw);
      } catch(IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  void sendData(String data, SendType type) throws IOException {
    OutputStream outStream = socket.getOutputStream();
    PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                          new OutputStreamWriter(outStream)), true);
    out.println(data);
    switch(type) {
      case FILE_CONTNET:
        System.out.println(SENDPHRASE + "SHARE_FILE_CONTENT");
        System.out.println();
        break;
      case FILE_INFO:
        System.out.println(SENDPHRASE + data);
        System.out.println();
        break;
      case PARTIAL_INSERT:
        System.out.println(SENDPHRASE + data);
        System.out.println();
        break;
      case PARTIAL_REMOVE:
        System.out.println(SENDPHRASE + data);
        System.out.println();
        break;
      case EDIT_INFO:
        System.out.println(SENDPHRASE + data);
        System.out.println();
        break;
      default:
        System.out.println("DON'T MATCH");
    }
  }

  String getFileStr(File file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    java.util.List<String> lines = new ArrayList<String>();
    String line = reader.readLine();
    while(line != null) {
      lines.add(line);
      line = reader.readLine();
    }
    return String.join(FILESEP, lines);
  }

  @Override
  public void handleShareInsert(DocumentEvent e) {
    int editLength = e.getLength();
    int offset = e.getOffset();
    try {
      String insertStr = e.getDocument().getText(offset, editLength);
      switch(insertStr) {
        case " ":
          insertStr = "SPACE";
          break;
        case "\t":
          insertStr = "TAB";
          break;
        case "\n":
          insertStr = "NEWLINE";
          break;
      }
      sendData("SHARE_PARTIAL_INSERT" + " " + insertStr + " " + String.valueOf(offset), SendType.PARTIAL_INSERT);
    } catch(BadLocationException ble) {
      ble.printStackTrace();
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  @Override
  public void handleShareRemove(DocumentEvent e) {
    int editLength = e.getLength();
    int offset = e.getOffset();
    try {
      sendData("SHARE_PARTIAL_REMOVE" + " " + String.valueOf(editLength) + " " + String.valueOf(offset), SendType.PARTIAL_REMOVE);
    } catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    handleShareInsert(e);
  }


  @Override
  public void removeUpdate(DocumentEvent e) {
    handleShareRemove(e);
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
  }
}
