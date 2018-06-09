import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

class ETListener implements ActionListener, EditAreaListener {
  private ETClient window;
  private Socket socket; // 接続に用いるソケット

  public ETListener(ETClient window, Socket socket) {
    this.window = window;
    this.socket = socket;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    Container container = window.getContentPane();
    JFileChooser fc = new JFileChooser();
    int btn_status;

    if(cmd.equals("select")) {
      fc.setApproveButtonText("みんなで編集");
      fc.setApproveButtonToolTipText("選択したファイルをユーザーと共有します");
      btn_status = fc.showOpenDialog(container);
    } else if(cmd.equals("save")) {
      fc.setSelectedFile(new File(window.getFileName()));
      btn_status = fc.showSaveDialog(container);
    } else {
      return;
    }

    if(btn_status != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File file = fc.getSelectedFile();
    JTextArea fileEditArea = window.getTextArea();
    JButton fileSelectButton = window.getSelectButton();
    if(cmd.equals("select")) {
      JLabel filenameLabel = window.getFileNameLabel();
      filenameLabel.setText(fc.getName(file));
      window.setFileName(fc.getName(file));
      BufferedReader br = null;
      try {
        br =
          new BufferedReader(
              new FileReader(file));
        fileEditArea.read(br, null);
        window.getSaveButton().setEnabled(true);
      } catch(FileNotFoundException fnfe) {
        fnfe.printStackTrace();
      } catch(IOException ioe) {
        ioe.printStackTrace();
      } finally {
        if(br != null) {
          try {
            br.close();
          } catch(IOException ioe) {
            ioe.printStackTrace();
          }
        }
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
      } finally {
        if(bw != null) {
          try {
            bw.close();
          } catch(IOException ioe) {
            ioe.printStackTrace();
          }
        }
      }
    }
  }

  @Override
  public void handleShareInsert(DocumentEvent e) {
    int editLength = e.getLength();
    int offset = e.getOffset();
    try {
      String insertStr = e.getDocument().getText(0, editLength);
      // server.shareInsertStr(window.getSelf(), offset, insertStr);
    } catch(BadLocationException ble) {
      ble.printStackTrace();
    }
  }

  @Override
  public void handleShareRemove(DocumentEvent e) {

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
