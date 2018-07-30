package com.dwicke.tsat.dataprocess;


import com.dwicke.tsat.view.TDAArgsPane;
import com.sun.xml.internal.ws.api.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * This class acts as an interface between the TDA python tool and TSAT.
 * It will act to store the file names of the multiattribute and the single attribute ts
 * it will call
 */
public class TDAProcessing {


    String multiVarJsonFilename;
    String lineLimit;
    boolean isTestData;
    String univarTSFilename;


    int window = 40;
    int p = 2;
    int dt = 1;
    double maxRad = 1.0;

    boolean shouldConsolidate = false;


    public TDAProcessing(String limitStr, String dataFileName, boolean isTestdata) {
        this.lineLimit = limitStr;
        this.multiVarJsonFilename = dataFileName;
        this.isTestData = isTestdata;
    }


    public static void main(String args[]) {
        TDAProcessing tdap = new TDAProcessing("32", "/home/drew/Desktop/TSATtutorial/mtsECG/ECG_TRAIN3.json", false);
        tdap.univarTSFilename = "/home/drew/Desktop/TSATtutorial/mtsECG/ECG_TRAIN_UNIVAR";
        tdap.lineLimit = "1";
        tdap.multiToUniTDAProc();
    }

    public void multiToUniTDAProc() {

        String fileName = "tdatool.sh";//"tdaInterface.py";
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        System.err.println(file.getAbsolutePath());
//source /opt/anaconda3/etc/profile.d/conda.sh; conda activate base;
        //ProcessBuilder pb = new ProcessBuilder("bash", "-c", "source /home/drew/anaconda2/etc/profile.d/conda.sh;conda activate base; echo $CONDA_DEFAULT_ENV; conda execute -q /home/drew/src/TSAT/target/classes/testingPython.py ; echo hi");
        ProcessBuilder pb = new ProcessBuilder("bash",file.getAbsolutePath(),multiVarJsonFilename, univarTSFilename, lineLimit, String.valueOf(window), String.valueOf(dt), String.valueOf(p), String.valueOf(maxRad), String.valueOf(shouldConsolidate));
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectErrorStream(true);
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void setUnivariateTSLocation(String filename) {
        this.univarTSFilename = filename;
    }

    public String getUnivarTSFilename() {
        return univarTSFilename;
    }

    public void setUnivarTSFilename(String univarTSFilename) {
        this.univarTSFilename = univarTSFilename;
    }

    public int getWindow() {
        return window;
    }

    public void setWindow(int window) {
        this.window = window;
    }

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }

    public int getDt() {
        return dt;
    }

    public void setDt(int dt) {
        this.dt = dt;
    }

    public double getMaxRad() {
        return maxRad;
    }

    public void setMaxRad(double maxRad) {
        this.maxRad = maxRad;
    }

    public boolean isShouldConsolidate() {
        return shouldConsolidate;
    }

    public void setShouldConsolidate(boolean shouldConsolidate) {
        this.shouldConsolidate = shouldConsolidate;
    }
}
