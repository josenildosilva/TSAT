package com.dwicke.tsat.dataprocess;


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
