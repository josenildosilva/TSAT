package com.dwicke.tsat.view;

import com.dwicke.tsat.dataprocess.TDAProcessing;
import com.dwicke.tsat.model.UserSession;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/* 1.4 example used by DialogDemo.java. */
class TDAArgsDialog extends JDialog implements ActionListener {

  private static final long serialVersionUID = 8146102612457794550L;

  private static final String OK_BUTTON_TEXT = "OK";
  private static final String CANCEL_BUTTON_TEXT = "Cancel";

  private TDAProcessing session;

  private TDAArgsPane guesserPane;

  protected volatile boolean wasCancelled;

  /** Creates the reusable dialog. */
  public TDAArgsDialog(JFrame topFrame, JPanel guesserPane, TDAProcessing session) {

    super(topFrame, true);

    if (topFrame != null) {
      Dimension parentSize = topFrame.getSize();
      Point p = topFrame.getLocation();
      setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
    }

    this.setTitle("Topological Data Analysis Arguments Input Dialog");

     this.session = session;

    this.guesserPane = (TDAArgsPane) guesserPane;

    MigLayout mainFrameLayout = new MigLayout("fill", "[grow,center]", "[grow]5[]");

    getContentPane().setLayout(mainFrameLayout);

    getContentPane().add(this.guesserPane, "h 200:300:,w 600:650:,growx,growy,wrap");

    JPanel buttonPane = new JPanel();
    JButton okButton = new JButton(OK_BUTTON_TEXT);
    JButton cancelButton = new JButton(CANCEL_BUTTON_TEXT);
    buttonPane.add(okButton);
    buttonPane.add(cancelButton);
    okButton.addActionListener(this);
    cancelButton.addActionListener(this);

    getContentPane().add(buttonPane, "wrap");

    pack();
  }

  //
  // Handles events for the text field.
  //
  @Override
  public void actionPerformed(ActionEvent e) {
    if (OK_BUTTON_TEXT.equalsIgnoreCase(e.getActionCommand())) {

      // set params
      this.wasCancelled = false;
      this.guesserPane.updateSession(session);

    }
    else if (CANCEL_BUTTON_TEXT.equalsIgnoreCase(e.getActionCommand())) {
      this.wasCancelled = true;
    }

    this.dispose();
  }

  /**
   * Clears the dialog and hides it.
   */
  public void clearAndHide() {
    setVisible(false);
  }
}