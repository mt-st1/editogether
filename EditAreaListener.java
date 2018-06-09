import javax.swing.event.*;

interface EditAreaListener extends DocumentListener {
  public void handleShareInsert(DocumentEvent e);
  public void handleShareRemove(DocumentEvent e);
  public void insertUpdate(DocumentEvent e);
  public void removeUpdate(DocumentEvent e);
  public void changedUpdate(DocumentEvent e);
}
