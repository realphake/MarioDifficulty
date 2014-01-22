/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Statistics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.meta.FilteredClassifier;
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
    Instances predict;
    LinearRegression model = new LinearRegression();
    Remove removefilter = new Remove();;
    FilteredClassifier fc = new FilteredClassifier();
            
    public WekaFunctions(){
        loadTrainInstance("../../MAINOOR/MarioPOMDP-testinstances.arff");
        buildLRcls();
    }


    public void loadModel(String modelPath) {
        //load model
        try {
            model = (LinearRegression) weka.core.SerializationHelper.read(modelPath);
        } catch (Exception ex) {
            Logger.getLogger(WekaFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    public void loadTrainInstance(String instancePath){
        System.out.println("Load instance starting");
        try {
            // read the instances from the ARFF file
            train = new Instances(new BufferedReader(new FileReader(instancePath)));
            // Set the to be predicted class
            train.setClassIndex(train.numAttributes()-2);
            removefilter.setOptions(
                    weka.core.Utils.splitOptions(
                            "-R 46,47,48,49,50,51,54 -V"));  // set options
            // inform filter about dataset **AFTER** setting options
            removefilter.setInputFormat(train);
            // apply filter
            trainParameters = Filter.useFilter(train, removefilter); 
            trainParameters.setClassIndex(trainParameters.numAttributes()-1);
            predict = new Instances(trainParameters, 0);
            predict.setClassIndex(predict.numAttributes()-1);
            System.out.println(trainParameters);
        } catch (IOException ex) {
            Logger.getLogger(WekaFunctions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(WekaFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Load instance succesful");
    }
    
    public void buildLRcls(){
        try {
            System.out.println("building classifier");
            model.setOptions(
                    weka.core.Utils.splitOptions(
                            "-S 0 -C -D"));  // set options
            model.buildClassifier(trainParameters);
        } catch (Exception ex) {
            Logger.getLogger(WekaFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public double predict(double[] parameterValues){
        //produce proper instance for predict Instances
        double[] inputvalues = new double[7];
        for (int i=0;i<parameterValues.length;i++){
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
    
    public static void main(String[] args){
        WekaFunctions wf = new WekaFunctions();
        //wf.loadModel("../../MAINOOR/traindata/LinRegressionModel.model");
        wf.predict(new double[]{4, 4, 4, 4, 4, 4});
    }
}
