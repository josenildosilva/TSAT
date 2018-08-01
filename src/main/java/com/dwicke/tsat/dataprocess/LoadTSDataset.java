package com.dwicke.tsat.dataprocess;

import com.dwicke.tsat.cli.RPM.TSATRPM;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import net.seninp.util.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.RMIClassLoader;
import java.text.ParseException;
import java.util.*;
import java.util.zip.DataFormatException;

public class LoadTSDataset {

    public static final int singleTS = 0;
    public static final int colRPM = 1;
    public static final int rowRPM = 2;
    public static final int ARFF = 3;
    private static final int JSON = 4;
    public static final int JSONRPM = 5; // with classes means that there are labels or ? labels
    public static final int JSONSINGLE = 6; // means doesn't have class labels or ? labels

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadTSDataset.class);

    final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;


    private static int getFormat(String fileName) {

        // first check the filetype arff and json
        if (fileName.contains("arff")) {
            return ARFF;
        } else if (fileName.contains("json")) {
            return JSON;
        }


        int formatStyle = singleTS;
        Path path = Paths.get(fileName);

        // lets go
        try {
            // open the reader
            BufferedReader reader = Files.newBufferedReader(path, DEFAULT_CHARSET);

            String line = reader.readLine();
            String[] lineSplit = line.trim().split("\\s+");
            if (lineSplit[0].compareTo("#") == 0) {
                if (lineSplit.length == 1) {
                    formatStyle = rowRPM;
                } else {
                    formatStyle = colRPM;
                }
            }
            reader.close();
        }catch(IOException e) {

        }
        return formatStyle;
    }




    public static Object[] loadData(String limitStr, String fileName, boolean isTestDataset) throws ParseException, FileNotFoundException {
        // check if everything is ready
        if ((null == fileName) || fileName.isEmpty()) {
//            log("unable to load data - no data source selected yet");
//            return null;
            throw new FileNotFoundException("Unable to load data - no data source selected yet.");
        }


        // make sure the path exists
        Path path = Paths.get(fileName);
        if (!(Files.exists(path))) {
//            log("file " + fileName + " doesn't exist.");
//            return null;
            throw new FileNotFoundException("file " + fileName + " doesn't exist.");
        }

        int formatStyle = getFormat(fileName);

        if (formatStyle == singleTS || formatStyle == colRPM) {
            return new Object[]{formatStyle, loadDataColumnWise(limitStr,fileName, isTestDataset)};
        }else if (formatStyle == rowRPM) {
            return new Object[]{formatStyle, loadRowTS(fileName, isTestDataset)};
        }
        else if (formatStyle == ARFF) {
            return new Object[] {formatStyle, loadARFF(fileName, isTestDataset)};
        } else if (formatStyle == JSON) {
            // now I need to check if it is for RPM or not
            // I do so by loading the json and checking if it contains class labels
            return new Object[] {loadJSON(fileName), fileName, isTestDataset};
        }

        return null;
    }

    private class MultiVariateTimeSeries {

        public TimeSeries[] timeSeries;
        public String label = null;
    }


    private class TimeSeries {

        protected double[] data = null;

    }

    public MultiVariateTimeSeries[] getSampleTS() {
        MultiVariateTimeSeries a[] = new MultiVariateTimeSeries[1];
        a[0] = new MultiVariateTimeSeries();
        a[0].timeSeries = new TimeSeries[2];

        a[0].timeSeries[0] = new TimeSeries();
        a[0].timeSeries[0].data = new double[]{2.0, 100.3, 10.4, 11.4};

        a[0].timeSeries[1] = new TimeSeries();
        a[0].timeSeries[1].data = new double[]{12.0, 90.3, 70.4, 31.4};
        return a;
    }


    public MultiVariateTimeSeries[] getSampleMTS() {
        MultiVariateTimeSeries a[] = new MultiVariateTimeSeries[2];
        a[0] = new MultiVariateTimeSeries();
        a[0].label = "1";
        a[0].timeSeries = new TimeSeries[2];

        a[0].timeSeries[0] = new TimeSeries();
        a[0].timeSeries[0].data = new double[]{2.0, 100.3, 10.4, 11.4};


        a[0].timeSeries[1] = new TimeSeries();
        a[0].timeSeries[1].data = new double[]{12.0, 90.3, 70.4, 31.4};

        a[1] = new MultiVariateTimeSeries();
        a[1].label = "1";
        a[1].timeSeries = new TimeSeries[2];

        a[1].timeSeries[0] = new TimeSeries();
        a[1].timeSeries[0].data = new double[]{20.0, 10.5, 109.4, 14.6};

        a[1].timeSeries[1] = new TimeSeries();
        a[1].timeSeries[1].data = new double[]{112.8, 23.1, 64.1, 32.7};
        return a;
    }

    public static void main(String args[]) throws IOException {


        LoadTSDataset d = new LoadTSDataset();
        String jsondata = new Gson().toJson(d.getSampleMTS());

        BufferedWriter writer = new BufferedWriter(new FileWriter("/home/drew/Desktop/TSATtutorial/mtsECG/sampleMultiLabel.json"));
        writer.write(jsondata);

        writer.close();


        int typeofJSON = -2;
        try {
            typeofJSON = loadJSON("/home/drew/Desktop/TSATtutorial/mtsECG/sampleMultiLabel.json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.err.println(typeofJSON);
    }

    public static int loadJSON(String filename) throws ParseException, FileNotFoundException{


            MultiVariateTimeSeries[] mvTS = new Gson().fromJson(new JsonReader(new FileReader(filename)), MultiVariateTimeSeries[].class);

            if (mvTS == null) {
                // then gson was unable to parse the file
                throw new ParseException("Error when parsing json file.  Not a valid format.",0);
            }

            for (int i = 0; i < mvTS.length; i++) {
                if (mvTS[i].label == null && (mvTS[i].timeSeries == null || mvTS[i].timeSeries.length == 0)) {
                    throw new ParseException("Error when parsing json time series.  Found an unlabeled and empty time series at index: " + i, i);
                }
                for (int j = 0; j < mvTS[i].timeSeries.length; j++) {
                    if (mvTS[i].timeSeries[j].data == null || mvTS[i].timeSeries[j].data.length == 0) {
                        throw new ParseException("Error when parsing json time series.  Invalid/empty or missing timeseries data within timeseries " + i, i);
                    }
                }
            }

            if (mvTS[0].label == null) {
                // no labels are present
                return JSONSINGLE;
            }else {
                // labels were present so go ahead and say it is RPM compatible
                return JSONRPM;
            }

    }



    public static Object[] loadARFF(String fileName, boolean isTestDataset) throws ParseException{
        Path path = Paths.get(fileName);

        try {
            BufferedReader reader =
                    new BufferedReader(new FileReader(path.toString()));
            ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
            Instances data = arff.getData();
            data.setClassIndex(data.numAttributes() - 1);

            double dataset[][] = new double[data.numInstances()][];
            String[] RPMLabels = new String[data.numInstances()];

            int i = 0;
            for(Instance instance : data) {
                double[] original = instance.toDoubleArray();
                dataset[i] = Arrays.copyOf(original, original.length-1);
                RPMLabels[i] = instance.toString(instance.classIndex());
                i++;
            }

            return new Object[] {dataset, RPMLabels};

        } catch(IOException e) {
            throw new ParseException("Error loading the arff file " + fileName, 0);
        }
    }

    public static Object[] loadRowTS(String fileName, boolean isTestDataset) throws ParseException{
        try {
            Map<String, List<double[]>> data =  UCRUtils.readUCRData(fileName);
            int numEntries = 0;
            for (Map.Entry<String, List<double[]>> en : data.entrySet())
            {
                numEntries += en.getValue().size();
            }

            if (!isTestDataset && data.keySet().size() == 1 && !data.keySet().toArray(new String[data.keySet().size()])[0].equals("-1")) {
                throw new DataFormatException("There needs to be more than one example for each class during training");
            }
            double dataset[][] = new double[numEntries][];
            String[] RPMLabels = new String[numEntries];
            int index = 0;
            System.err.println("creating the dataset");
            for (Map.Entry<String, List<double[]>> en : data.entrySet())
            {
                for (double[] lis : en.getValue()) {
                    RPMLabels[index] = en.getKey();
                    dataset[index] = lis;
                    index++;
                }
            }

            return new Object[] {dataset, RPMLabels};

        }catch(Exception e) {
            String stackTrace = StackTrace.toString(e);
            //log("error while trying to read data from " + fileName + ":\n " + e.getMessage() + " \n" + stackTrace);
            throw new ParseException("error while trying to read data from " + fileName + ":\n " + e.getMessage() + " \n" + stackTrace, 0);
        }
    }

    public static Object[] loadDataColumnWise(String limitStr, String fileName, boolean isTestDataset) throws ParseException, FileNotFoundException{
        if ((null == fileName) || fileName.isEmpty()) {
//            log("unable to load data - no data source selected yet");
//            return null;
            throw new FileNotFoundException("unable to load data - no data source selected yet");
        }

        // make sure the path exists
        Path path = Paths.get(fileName);
        if (!(Files.exists(path))) {
//            log("file " + fileName + " doesn't exist.");
//            return null;
            throw new FileNotFoundException("file " + fileName + " doesn't exist.");
        }

        // read the input
        //
        // init the data araay
        ArrayList<ArrayList<Double>> data = new ArrayList<>();
        String[] RPMLabels = null;
        // lets go
        try {

            // set the lines limit
            long loadLimit = 0l;
            if (!(null == limitStr) && !(limitStr.isEmpty())) {
                loadLimit = Long.parseLong(limitStr);
            }

            // open the reader
            BufferedReader reader = Files.newBufferedReader(path, DEFAULT_CHARSET);

            // read by the line in the loop from reader
            String line;
            long lineCounter = 0;

            while ((line = reader.readLine()) != null) {

                String[] lineSplit = line.trim().split("\\s+");

                if(0 == lineCounter)
                {
                    if(lineSplit[0].compareTo("#") == 0) {
                        log("Found RPM Data");
                        ArrayList<String> labels = new ArrayList<String>();

                        for(int i = 1; i < lineSplit.length; i++) {
                            labels.add(lineSplit[i]);
                        }

                        if (!isTestDataset) {
                            HashSet<String> setlabels = new HashSet<>(labels);

                            if (setlabels.size() == 1) {
                                throw new DataFormatException("There needs to be more than one class");
                            }
                            for (String curlabel : setlabels) {
                                if (Collections.frequency(labels, curlabel) == 1) {
                                    throw new DataFormatException("There needs to be more than one example for each class during training");
                                }
                            }
                        }
                        RPMLabels = labels.toArray(new String[labels.size()]);
                        continue;
                    }
                    data = new ArrayList<>();
                    for (int i = 0; i < lineSplit.length; i++) {
                        data.add(new ArrayList<>());
                    }
                }

                if (lineSplit.length < data.size()) {
                    log("line " + (lineCounter+1) + " of file " + fileName + " contains too few data points.");
                }

                for (int i = 0; i < lineSplit.length; i++) {
                    double value = new BigDecimal(lineSplit[i]).doubleValue();
                    data.get(i).add(value);
                }

                lineCounter++;

                // break the load if needed
                if ((loadLimit > 0) && (lineCounter > loadLimit)) {
                    break;
                }
            }
            reader.close();
        }
        catch (Exception e) {
            String stackTrace = StackTrace.toString(e);
//            log("error while trying to read data from " + fileName + ":\n" + stackTrace);
//            return null;
            throw new ParseException("error while trying to read data from " + fileName + ":\n" + stackTrace, 0);
        }

        double[][] output = null;
        // convert to simple doubles array and clean the variable
        if (!(data.isEmpty())) {
            output = new double[data.size()][data.get(0).size()];

            for (int i = 0; i < data.size(); i++) {
                for (int j = 0; j < data.get(0).size(); j++) {
                    output[i][j] = data.get(i).get(j);
                }
            }
        }

        return new Object[]{output, RPMLabels};
    }




    /**
     * Logging function.
     *
     * @param message the message to be logged.
     */
    public static void log(String message) {
        LOGGER.debug(message);
    }
}
