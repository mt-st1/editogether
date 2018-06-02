import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;
// import javax.swing.filechooser.FileFilter;

class ETListener implements ActionListener {
  private ETClient window;
  public ETListener(ETClient window) {
    this.window = window;
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    Container container = window.getContentPane();
    JFileChooser fc = new JFileChooser();
    int btn_status;

    if(cmd.equals("select")) {
      btn_status = fc.showOpenDialog(container);
    } else if(cmd.equals("save")) {
      btn_status = fc.showSaveDialog(container);
    } else {
      return;
    }

    if(btn_status != JFileChooser.APPROVE_OPTION) {
      return;
    }
    File file = fc.getSelectedFile();
    JTextArea fileEditArea = window.getTextArea();
    if(cmd.equals("select")) {
      // window.setFileSelectStatus(true);
      JLabel filenameLabel = window.getLabel();
      filenameLabel.setText(fc.getName(file));
      BufferedReader br = null;
      try {
        br =
          new BufferedReader(
              new FileReader(file));
        fileEditArea.read(br, null);
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
}
