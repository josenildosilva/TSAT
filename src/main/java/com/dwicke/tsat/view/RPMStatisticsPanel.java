package com.dwicke.tsat.view;

import com.dwicke.tsat.model.UserSession;
import com.dwicke.tsat.rpm.util.ClassificationResults;
import weka.classifiers.Evaluation;

import javax.swing.*;

/**
 * This class displays RPM result statistics in a text area in the RPM Statistics tab.
 */
public class RPMStatisticsPanel extends JPanel {

    /** Fancy Serial */
    private static final long serialVersionUID = -6017992967964000474L;
    JTextArea textArea = new JTextArea ();

    JScrollPane scroll = new JScrollPane (textArea,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    UserSession session;

    public RPMStatisticsPanel() {
        super();
        this.add(scroll);
        this.setVisible(true);
    }


    /**
     * Resets the panel.
     */
    public void resetPanel() {
        // cleanup all the content
        this.removeAll();
        this.add(this.scroll);
        this.validate();
        this.repaint();
    }

    /**
     * Set the user session.
     *
     * @param session the user session.
     */
    public void setClassificationResults(UserSession session) {this.session = session; }


    private ClassificationResults getClassificationResults() {
        return session.rpmHandler.getTestingResults();
    }

    /**
     * Updates the table by populating it with results.
     */
    public void updateRPMStatistics() {
        if (!getClassificationResults().unlabeledData) {
            textArea.setText("");
            StringBuilder sb = new StringBuilder();
            Evaluation eval = getClassificationResults().evalResults;
            String rltString = eval.toSummaryString("\n\n======\nResults: ", false);
            sb.append(rltString + "\n");
            sb.append("F1 score:\n");


            for (int i = 0; i < getClassificationResults().numClasses; i++) {
                sb.append("class " + (i + 1) + " F1 Score: " + eval.fMeasure(i) + " AUC: " + eval.areaUnderROC(i) + " MCC = " + eval.matthewsCorrelationCoefficient(i) + "\n");
            }
            sb.append("\nWeighted F1 Score = " + eval.weightedFMeasure() + "\n");
            sb.append("Weighted AUC = " + eval.weightedAreaUnderROC() + "\n");
            sb.append("Weighted MCC = " + eval.weightedMatthewsCorrelation() + "\n");

            try {
                sb.append(eval.toMatrixString() + "\n");
            } catch (Exception e) {
                sb.append(e.getMessage());
                sb.append("error creating the confusion matrix in RPMStatisticsPanel");
            }
            textArea.setText(sb.toString());
        }
    }


}
