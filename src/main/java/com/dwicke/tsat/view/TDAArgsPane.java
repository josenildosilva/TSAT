package com.dwicke.tsat.view;

import com.dwicke.tsat.dataprocess.TDAProcessing;
import com.dwicke.tsat.model.UserSession;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.stream.DoubleStream;

/**
 * Implements the parameter panel for GrammarViz.
 * 
 * @author psenin
 * 
 */
public class TDAArgsPane extends JPanel {

  private static final long serialVersionUID = -941188995659753923L;

  // The labels
  //
  private static final JLabel WINDOW_LABEL = new JLabel("Window to compute persistence on:");
  private static final JLabel DT_LABEL = new JLabel("Number of samples to skip between points:");
  private static final JLabel P_LABEL = new JLabel("Integer value for p in L^p norm to compute:");
  private static final JLabel MAXRAD_LABEL = new JLabel("max distance between pairwise points to consider for the Rips complex:");
  private static final JLabel CONSOLIDATE_LABEL = new JLabel("Should consolidate:");
  private static final JLabel LOGDIV_LABEL = new JLabel("Should log divide time series data:");

  // and their UI widgets
  //

  private static final JTextField windowField = new JFormattedTextField(integerNumberFormatter());
  private static final JTextField dtField = new JFormattedTextField(integerNumberFormatter());
  private static final JTextField pField = new JFormattedTextField(integerNumberFormatter());

  private static final JTextField maxRadField = new JFormattedTextField(
          new DecimalFormat("0.00"));


  private static final JCheckBox consolidateField = new JCheckBox("check if true");
  private static final JCheckBox logdivField = new JCheckBox("check if true");


  /**
   * Constructor.
   *
   * @param userSession
   */
  public TDAArgsPane(TDAProcessing userSession) {

    super(new MigLayout("fill", "[][fill,grow][fill,grow][fill,grow]", "[grow]"));

    this.add(WINDOW_LABEL, "span 2");
    this.add(windowField, "wrap");

    this.add(DT_LABEL, "span 2");
    this.add(dtField, "wrap");

    this.add(P_LABEL, "span 2");
    this.add(pField, "wrap");


    this.add(MAXRAD_LABEL, "span 2");
    this.add(maxRadField, "wrap");

    this.add(CONSOLIDATE_LABEL, "span 2");
    this.add(consolidateField, "wrap");

    this.add(LOGDIV_LABEL, "span 2");
    logdivField.setSelected(true);
    this.add(logdivField);


    setValues(userSession);

  }

  private void setValues(TDAProcessing userSession) {

    windowField.setText(Integer.valueOf(userSession.getWindow()).toString());
    dtField.setText(Integer.valueOf(userSession.getDt()).toString());
    pField.setText(Integer.valueOf(userSession.getP()).toString());
    maxRadField.setText(Double.valueOf(userSession.getMaxRad()).toString());
    consolidateField.setSelected(userSession.isShouldConsolidate());
    logdivField.setSelected(userSession.isShouldLogDivide());

  }

  public void updateSession(TDAProcessing userSession) {


    userSession.setWindow(Integer.valueOf(windowField.getText()));
    userSession.setDt(Integer.valueOf(dtField.getText()));
    userSession.setP(Integer.valueOf(pField.getText()));
    userSession.setMaxRad(Double.valueOf(maxRadField.getText()));
    userSession.setShouldConsolidate(consolidateField.isSelected());
    userSession.setLogDivide(logdivField.isSelected());

  }

  /**
   * Provides a convenient integer formatter.
   * 
   * @return a formatter instance.
   */
  private static NumberFormatter integerNumberFormatter() {
    NumberFormat format = NumberFormat.getInstance();
    NumberFormatter formatter = new NumberFormatter(format);
    formatter.setValueClass(Integer.class);
    formatter.setMinimum(0);
    formatter.setMaximum(Integer.MAX_VALUE);
    // If you want the value to be committed on each keystroke instead of focus lost
    formatter.setCommitsOnValidEdit(true);
    return formatter;
  }

}
