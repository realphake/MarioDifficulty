/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Statistics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author Norbert Heijne
 */
// Code inspired by
// http://stackoverflow.com/questions/20017957/how-to-reuse-saved-classifier-created-from-explorerin-weka-in-eclipse-java
public class WekaFunctions {

    Instances train;
    Instances trainParameters;
    Instances RF_testInstances;
    Instances predict;
    RandomForest model = new RandomForest();
    Remove removefilter = new Remove();
    FilteredClassifier fc = new FilteredClassifier();
    public double[] distributions;
    public WekaFunctions() {
        // do nothing
    }

    static String readFile(String path) {

        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(path));
            return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        } catch (IOException ex) {
            Logger.getLogger(WekaFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Could not read file " + path);
        return "";
    }

    public void loadModel(String modelPath) {
        //load model
        try {
            model = (RandomForest) weka.core.SerializationHelper.read(modelPath);
        } catch (Exception ex) {
            Logger.getLogger(WekaFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadTrainInstance(String instances) {
        System.out.println("Load instance starting");
        System.out.println(instances);
        try {
            // read the instances from the ARFF file
            train = new Instances(new BufferedReader(new StringReader(instances)));
            // Set the to be predicted class
            train.setClassIndex(train.numAttributes() - 2);
            // weka uses a 1-based index parameters are then 61-66 and challenge 69
            removefilter.setOptions(
                    weka.core.Utils.splitOptions(
                    "-R 1-65,69 -V"));  // set options

            // inform filter about dataset **AFTER** setting options
            removefilter.setInputFormat(train);
            // apply filter
            trainParameters = Filter.useFilter(train, removefilter);
            trainParameters.setClassIndex(trainParameters.numAttributes() - 1);
            predict = new Instances(trainParameters, 0);
            predict.setClassIndex(predict.numAttributes() - 1);
            System.out.println(trainParameters);
        } catch (IOException ex) {
            Logger.getLogger(WekaFunctions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(WekaFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Load instance succesful");
    }

    public void loadTestInstances(boolean verbose,String instances) {
        try {
            //Load test instances into data
            //System.out.println("");
            //System.out.println("Loading test instances...");
           
            Instances data = new Instances(new BufferedReader(new StringReader(instances)));
           
            // setting class attribute
            data.setClassIndex(data.numAttributes() - 2);        //2nd to last attribute is used for classification (last is timestamp)
            removefilter.setOptions(
                    weka.core.Utils.splitOptions(
                    "-R 1-65,69 -V"));  // set options

            // inform filter about dataset **AFTER** setting options
            removefilter.setInputFormat(data);
            // apply filter
            RF_testInstances = Filter.useFilter(data, removefilter);
            RF_testInstances.setClassIndex(RF_testInstances.numAttributes() - 1);

            System.out.println("-done loading " + RF_testInstances.numInstances() + " test instance(s)");


// create copy
            Instances labeled = new Instances(RF_testInstances);
            // label instances

//// label instances
// for (int i = 0; i < RF_testInstances.numInstances(); i++) {
//   double[] clsLabel = classifyInstance(RF_testInstances.instance(i),true);
//   break;
// }

            this.distributions = classifyInstance(selectTestInstance(), true);
            
        } catch (Exception e) {
            //Error reading file
            System.out.println("ERROR!!! - In function loadTestInstances()...");
            System.out.println("-" + e);
        }
    }

    public double[] classifyInstance(Instance testInstance, boolean verbose) {
        try {
            //Classify one particular instance from loaded set of Test Instances

            // Specify that the instance belong to the training set 
            // in order to inherit from the set description                                
            //testInstance.setDataset(RF_trainingInstances);

            // Get the likelihood of each class
            double[] fDistribution = model.distributionForInstance(testInstance);
            verbose = true;
            if (verbose) {
                System.out.println("");
                System.out.println("Classifying selected test instance...");
                System.out.println("-probability of instance being class 1: " + fDistribution[0]);
                System.out.println("-probability of instance being class 2: " + fDistribution[1]);
                System.out.println("-probability of instance being class 3: " + fDistribution[2]);
                System.out.println("-probability of instance being class 4: " + fDistribution[3]);
                System.out.println("-probability of instance being class 5: " + fDistribution[4]);
            }
            return fDistribution;
        } catch (Exception e) {
            //Error reading file
            System.out.println("ERROR!!! - In function classifyInstance()...");
            System.out.println("-" + e);
            return new double[]{0.0, 0.0, 0.0, 0.0, 0.0}; //dummy values
        }
    }

    public Instance selectTestInstance() {
        //Select last instance from loaded set of Test Instances

        //Create test instance
        //Instance testInstance = new Instance(newDataTest.firstInstance());
        //Instance testInstance = new Instance(newDataTest.instance(0));
        Instance testInstance = new Instance(RF_testInstances.lastInstance());
        //System.out.println("-selecting last instance in test set RF_testInstances, done");

        // Specify that the instance belong to the training set 
        // in order to inherit from the set description                                
        testInstance.setDataset(trainParameters);
        System.out.println("-selected last instance in test set: " + testInstance.toString());

        return testInstance;
    }

    public void buildLRcls() {
        try {
            System.out.println("building classifier");
            model.setOptions(
                    weka.core.Utils.splitOptions(
                    "-S 0 -D"));  // set options
            model.buildClassifier(trainParameters);

            // evaluate classifier and print some statistics 
            Evaluation eval = new Evaluation(trainParameters);
            eval.evaluateModel(model, trainParameters);
            System.out.println(eval.toSummaryString());



        } catch (Exception ex) {
            Logger.getLogger(WekaFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public double predict(double[] parameterValues) {
        //produce proper instance for predict Instances
        double[] inputvalues = new double[7];
        for (int i = 0; i < parameterValues.length; i++) {
            inputvalues[i] = parameterValues[i];
        }
        predict.add(new Instance(1, inputvalues));
        double predictedValue = 0;
        try {
            predictedValue = model.classifyInstance(predict.lastInstance());
        } catch (Exception ex) {
            Logger.getLogger(WekaFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        predict.delete(0);

        System.out.println("The predicted Challenge for parameter values "
                + parameterValues.toString()
                + ": " + predictedValue);
        return predictedValue;
    }

    public static void main(String[] args) {
        WekaFunctions wf = new WekaFunctions();
        //wf.loadModel("../../MAINOOR/traindata/LinRegressionModel.model");
        //or
        //wf.loadTrainInstance(wf.readFile("../../MAINOOR/MarioPOMDP-testinstances.arff"))
        wf.predict(new double[]{4, 4, 4, 4, 4, 4});
    }
}
