package dk.itu.mario.scene;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author parismavromoustakos
 */
public class SectionOfGame {

    public int getPreviousDifficulty() {
        return previousDifficulty;
    }

    public void setPreviousDifficulty(int previousDifficulty) {
        this.previousDifficulty = previousDifficulty;
    }

    public int getNextDifficulty() {
        return nextDifficulty;
    }

    public void setNextDifficulty(int nextDifficulty) {
        this.nextDifficulty = nextDifficulty;
    }
    
    private int previousAction;
    private boolean firstPlay = true;
    private double startTime;
    private double endTime;
    private int id;
    private float[] previousEmotions;
    private boolean hasStarted;
    private boolean hasEnded;
    private double startTime2;
    private double endTime2;
    private boolean hasStarted2;
    private float[] emotions;
    private int times = 0;
    private ArrayList<float[]> allEmotions;
    private int previousDifficulty = 1;
    private int nextDifficulty;
    private int[] possibleActions = {-1,1};

    
    public int chooseRandomAction(){
        Random rn = new Random();
        //random int in [0,2]
        int choice  = rn.nextInt(2);
        return this.possibleActions[choice];
    }
    
    
    public void increaseTimes() {
        this.times++;
    }

    //function to reset the section measurements.
    //also calls calculateNextDifficulty.
    public void reset() {

        //reset all values
        this.times = 0;
        this.hasEnded = false;
        this.hasEnded2 = false;
        this.hasStarted = false;
        this.hasStarted2 = false;
        this.allEmotions = new ArrayList<float[]>();

        
        //calculate next difficulty
        this.nextDifficulty = calculateNextDifficulty();
        
        //save the previous emotions
        float[] tempPrev = this.emotions;
        this.previousEmotions = tempPrev;



        //reset emotions table
        float[] temp = {0, 0, 0, 0, 0, 0, 0};
        this.emotions = temp;
    }

    public float[] getEmotions() {
        return emotions;
    }

    public void setEmotions(float[] emotions) {
        this.emotions = emotions;
    }

    public boolean isHasStarted2() {
        return hasStarted2;
    }

    public void setHasStarted2(boolean hasStarted2) {
        this.hasStarted2 = hasStarted2;
    }

    public boolean isHasEnded2() {
        return hasEnded2;
    }

    public void setHasEnded2(boolean hasEnded2) {
        this.hasEnded2 = hasEnded2;
    }
    private boolean hasEnded2;

    public double getStartTime2() {
        return startTime2;
    }

    public void setStartTime2(double startTime2) {
        this.startTime2 = startTime2;
    }

    public double getEndTime2() {
        return endTime2;
    }

    public void setEndTime2(double endTime2) {
        this.endTime2 = endTime2;
    }

    public boolean isHasEnded() {
        return hasEnded;
    }

    public void setHasEnded(boolean hasEnded) {
        this.hasEnded = hasEnded;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isHasStarted() {
        return hasStarted;
    }

    public void setHasStarted(boolean hasStarted) {
        this.hasStarted = hasStarted;
    }

    public double calculateDuration() {
        return endTime - startTime;
    }

    public double calculateDuration2() {
        return endTime2 - startTime2;
    }

    //add values to emotions array.
    public void addEmotions(float[] emotions) {
        float[] temp = new float[7];
        for (int i = 0; i < 7; i++) {
            this.emotions[i] += emotions[i];
            temp[i] = emotions[i];
        }
        this.allEmotions.add(temp);
    }

    public void normalizeEmotions() {
        for (int i = 0; i < 7; i++) {
            this.emotions[i] /= this.times;
        }
    }

    public void printEmotions() {
        for (int i = 0; i < 7; i++) {
            System.out.println(this.emotions[i]);
        }
    }

    public int calculateNextDifficulty() {
        if (this.firstPlay) {
            int nextDifficulty = this.previousDifficulty;
           /** OLD IMPLEMENTATION 
            * if (this.emotions[0] > 0.5) {
                nextDifficulty++;
                if (nextDifficulty > 5) {
                    nextDifficulty = 5;
                }
            } else if (this.emotions[3] > 0.5) {
                nextDifficulty--;
                if (nextDifficulty < 0) {
                    nextDifficulty = 0;
                }
            }
            System.out.println("Section id:" + this.id + " next difficulty= " + nextDifficulty);
            this.previousDifficulty = nextDifficulty;
            this.firstPlay = false;*/
             
            //new implementation ("smart")
            int action = this.chooseRandomAction();
            nextDifficulty+=action;
            if(nextDifficulty>5){
                nextDifficulty=5;
            }
            else if(nextDifficulty<0){
                nextDifficulty=0;
            }
            this.previousAction = action;
            this.firstPlay=false;
            this.previousDifficulty = nextDifficulty;
            System.out.println("Section id:" + this.id + " next difficulty= " + nextDifficulty);
            return nextDifficulty;
        }
        else {
            //the user has already played a round, so we calculate referring to the previous measurements
            int nextDifficulty = this.previousDifficulty;
           /** OLD IMPLEMENTATION
            //neutral
            if (this.emotions[0] > this.previousEmotions[0]) {
                nextDifficulty++;
                if (nextDifficulty > 5) {
                    nextDifficulty = 5;
                }
                //angry
            } else if (this.emotions[3] > this.previousEmotions[3]) {
                nextDifficulty--;
                if (nextDifficulty < 0) {
                    nextDifficulty = 0;
                }
            }
            System.out.println("Section id:" + this.id + " next difficulty= " + nextDifficulty);
            this.previousDifficulty = nextDifficulty;
            return nextDifficulty; */
            
            //new "smart" implementation
            float differences[]={0,0,0};
            int nextAction =0;
            //neutral
            differences[0] = this.emotions[0] - this.previousEmotions[0];
            //happy
            differences[1] = this.emotions[1] - this.previousEmotions[1];
            //angry
            differences[2] = this.emotions[3] - this.previousEmotions[3];
            
            
            int mostImportantDiff = this.findMostImportantDiff(differences);
            //neutral
            if(mostImportantDiff==0){
                //neutral increase
                System.out.println("most important =0 ****"+differences[0]);
                if(differences[0]>0){
                    nextAction = this.previousAction*(-1);//user is bored. change strategy
                }
                else{
                    nextAction = this.previousAction;//user is showing emotions. make it harder.
                }
            }
            //happy
            else if(mostImportantDiff==1){
                System.out.println("most important =1 ****"+differences[1]);

                //happy increase
                if(differences[1]>0){
                    nextAction = this.previousAction;
                }
                else{
                    nextAction = this.previousAction*(-1);
                }
            }
            else{
                System.out.println("most important =2 ****"+differences[2]);
                //angry increase    
                if(differences[0]>0){
                    nextAction = this.previousAction*(-1);//user is angry. change strategy
                }
                else{
                    nextAction = this.previousAction;//user is showing emotions. make it harder.
                }
            }
            
            nextDifficulty+=nextAction;
            if(nextDifficulty>5){
                nextDifficulty=5;
            }
            else if(nextDifficulty<0){
                nextDifficulty=0;
            }
            
            return nextDifficulty;
        }
    }

    //initialize with id that corresponds to section type.
    public SectionOfGame(int id) {
        this.id = id;
        this.emotions = new float[7];
        this.allEmotions = new ArrayList<float[]>();
    }
    
    public int findMostImportantDiff(float[] diffs){
        int maxInd =0;
        float max =0;
        //return the index of the biggest difference in emotions
        //0: neutral 1:happy 2:angry
        for (int i=0;i<=2;i++){
            if (Math.abs(diffs[i])>max){
                max = diffs[i];
                maxInd = i;
            }
        }
        return maxInd;
    }
    
    
    public void writeSectionEmotionsToFile() {
        for (int j = 0; j < this.allEmotions.size(); j++) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < this.allEmotions.get(j).length; i++) {
                line.append(String.valueOf(allEmotions.get(j)[i]));
                line.append(" ");
                //System.out.println(allGameEmotions.get(j)[i]);
            }
            try {
                String filename = "section" + String.valueOf(this.id) + "Emotions.txt";
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
                out.println(line);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
