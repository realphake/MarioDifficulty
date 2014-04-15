package dk.itu.mario.scene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

    private float previousNeutral = 0;

    public float getPreviousNeutral() {
        return previousNeutral;
    }

    public void setPreviousNeutral(float previousNeutral) {
        this.previousNeutral = previousNeutral;
    }

    public int getPreviousAction() {
        return previousAction;
    }

    public void setPreviousAction(int previousAction) {
        this.previousAction = previousAction;
    }

    public float[] getPreviousEmotions() {
        return previousEmotions;
    }

    public void setPreviousEmotions(float[] previousEmotions) {
        this.previousEmotions = previousEmotions;
    }

    public int[] getPossibleActions() {
        return possibleActions;
    }

    public void setPossibleActions(int[] possibleActions) {
        this.possibleActions = possibleActions;
    }
    
   
    private double deathTime;
    private boolean diedHere;

    public double getDeathTime() {
        return deathTime;
    }

    public void setDeathTime(double deathTime) {
        this.deathTime = deathTime;
    }

    public boolean getDiedHere() {
        return diedHere;
    }

    public void setDiedHere(boolean diedHere) {
        this.diedHere = diedHere;
    }

    private float[] deathEmotions = {0, 0, 0, 0, 0, 0, 0};
    private float[] emotionVariance = {0, 0, 0, 0, 0, 0, 0};
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

    public ArrayList<float[]> getAllEmotions() {
        return allEmotions;
    }

    public void setAllEmotions(ArrayList<float[]> allEmotions) {
        this.allEmotions = allEmotions;
    }
    private int previousDifficulty = 1;
    private int nextDifficulty;
    private int[] possibleActions = {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5};

    public int chooseRandomAction() {
        Random rn = new Random();
        //random int in [0,2]
        int choice = rn.nextInt(11);
        return this.possibleActions[choice];
    }

    public void increaseTimes() {
        this.times++;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public void resetAtDeath(){
        System.out.println("resetting because of death.");
        //reset all values
        if(this.diedHere==true){
            this.times = 1;
        }else{
            this.times=0;
        }
        this.diedHere = false;
        this.hasEnded = false;
        this.hasEnded2 = false;
        this.hasStarted = false;
        this.hasStarted2 = false;
        this.allEmotions = new ArrayList<float[]>();
    }
    
    
    
    //function to reset the section measurements.
    //also calls calculateNextDifficulty.
    public void reset(float alphaFactor) {

        //reset all values
        this.diedHere = false;
        this.times = 0;
        this.hasEnded = false;
        this.hasEnded2 = false;
        this.hasStarted = false;
        this.hasStarted2 = false;
        this.allEmotions = new ArrayList<float[]>();

        //calculate next difficulty
        this.nextDifficulty = calculateNextDifficulty(alphaFactor);

        //save the previous emotions
        float[] tempPrev = this.emotions;
        this.previousNeutral = tempPrev[0];
        this.previousEmotions = tempPrev;

        //reset emotions table & emotion Variances.
        float[] temp = {0, 0, 0, 0, 0, 0, 0};
        this.emotionVariance = temp;
        this.emotions = temp;
    }

    public void printVariances() {
        for (int i = 0; i < 7; i++) {
            System.out.println(this.emotionVariance[i]);
        }
    }

    public float[] getEmotionVariance() {
        return emotionVariance;
    }

    public void setEmotionVariance(float[] emotionVariance) {
        this.emotionVariance = emotionVariance;
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

    public void printAllEmotions() {
        for (int i = 0; i < this.allEmotions.size(); i++) {
            for (int j = 0; j < 7; j++) {
                if (j == 0) {
                    System.out.println("----");
                }
                System.out.println(this.allEmotions.get(i)[j]);
            }
        }
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

    public void calculateDeathEmotions(double resetTime) {
        //System.out.println("in calcdeathEmotions");
        //System.out.println("reset time: "+ resetTime + "death time : "+this.deathTime);

        float[] emotions = {0, 0, 0, 0, 0, 0, 0};
        int counter = 0;
        int times = 0;

        try {
            //read the file generated by inSight line by line.
            String currentLine;
            int a = this.id;
            boolean valid = false;
            //System.out.println(System.getProperty("user.dir"));
            BufferedReader br = new BufferedReader(new FileReader("../../../output/emotions.txt"));
            try {
                while ((currentLine = br.readLine()) != null) {

                    if (currentLine.startsWith("-")) {
                        //read timestamp
                        String timeStampS = currentLine.substring(7, currentLine.length());
                        Double temp = Double.valueOf(timeStampS);
                        double timeStamp = temp.doubleValue();
                        if (timeStamp >= this.deathTime && timeStamp <= resetTime) {
                            //System.out.println("found valid timestamp!");
                            //System.out.println(timeStamp);
                            valid = true;
                        } else {
                            valid = false;
                        }

                    } else if (valid == true) {
                        //convert string to float and add it to the correct position.
                        emotions[counter] += Float.parseFloat(currentLine);
                        counter++;
                        //if counter exceeds array dimensions
                        if (counter > 6) {
                            if (a != -1) {
                                //sections.get(a).addEmotions(emotions);
                                //sections.get(a).increaseTimes();

                                float[] temp = new float[7];
                                for (int p = 0; p < emotions.length; p++) {
                                    temp[p] = emotions[p];
                                    this.deathEmotions[p] += temp[p];
                                }
                                //add emotion to the total list
                                //allGameEmotions.add(temp);
                            }

                            //reset the emotions table.
                            for (int k = 0; k < 7; k++) {
                                emotions[k] = 0;
                            }
                            times++;
                            counter = 0;
                        }
                    }

                }
                for (int q = 0; q < 7; q++) {
                    //save death emotions
                    this.deathEmotions[q] /= times;
                    //save this.emotions to include deathEmotions
                    this.emotions[q] = this.deathEmotions[q];
                }

                //System.out.println(System.getProperty("user.dir"));// get pwd.
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        for (int qq = 0; qq < 7; qq++) {
            System.out.println(this.deathEmotions[qq]);
        }

    }

    public float[] getDeathEmotions() {
        return deathEmotions;
    }

    public void setDeathEmotions(float[] deathEmotions) {
        this.deathEmotions = deathEmotions;
    }

    public int calculateNextDifficulty(float alphaFactor) {
        if (this.firstPlay) {
            int nextDifficulty = this.previousDifficulty;
            /**
             * OLD IMPLEMENTATION if (this.emotions[0] > 0.5) {
             * nextDifficulty++; if (nextDifficulty > 5) { nextDifficulty = 5; }
             * } else if (this.emotions[3] > 0.5) { nextDifficulty--; if
             * (nextDifficulty < 0) { nextDifficulty = 0; } }
             * System.out.println("Section id:" + this.id + " next difficulty= "
             * + nextDifficulty); this.previousDifficulty = nextDifficulty;
             * this.firstPlay = false;
             */

            //new implementation ("smart")
            //int action = this.chooseRandomAction();
            int action = 0;
            nextDifficulty += action;
            if (nextDifficulty > 5) {
                nextDifficulty = 5;
            } else if (nextDifficulty < 0) {
                nextDifficulty = 0;
            }
            this.previousAction = action;
            this.firstPlay = false;
            this.previousDifficulty = nextDifficulty;
            System.out.println("Section id:" + this.id + " next difficulty= " + nextDifficulty);
            return nextDifficulty;
        } else {
            //initialize nextAction
            int nextAction = 0;

            //if user is always neutral, the game will not "progress" so, make it a bit harder.
            if (this.emotions[0] > 0.75*alphaFactor) {
                System.out.println("User >.75 neutral");
                nextAction = 1;
            } else {

                //the user has already played a round, so we calculate referring to the previous measurements
                int nextDifficulty = this.previousDifficulty;
                /**
                 * OLD IMPLEMENTATION //neutral if (this.emotions[0] >
                 * this.previousEmotions[0]) { nextDifficulty++; if
                 * (nextDifficulty > 5) { nextDifficulty = 5; } //angry } else
                 * if (this.emotions[3] > this.previousEmotions[3]) {
                 * nextDifficulty--; if (nextDifficulty < 0) { nextDifficulty =
                 * 0; } } System.out.println("Section id:" + this.id + " next
                 * difficulty= " + nextDifficulty); this.previousDifficulty =
                 * nextDifficulty; return nextDifficulty;
                 */

                //new "smart" implementation
                float differences[] = {0, 0, 0};
                //neutral
                differences[0] = this.emotions[0] - this.previousEmotions[0];
                //happy
                differences[1] = this.emotions[1] - this.previousEmotions[1];
                //angry
                differences[2] = this.emotions[3] - this.previousEmotions[3];

                int mostImportantDiff = this.findMostImportantDiff(differences);
                //neutral
                if (mostImportantDiff == 0) {

                    //neutral increase
                    System.out.println("most important =0 ****" + differences[0]);
                    if (differences[0] > 0) {
                        nextAction = Math.round(5 * differences[0] * (-1) * alphaFactor);
                    } else {
                        nextAction = Math.round(5 * differences[0] * alphaFactor);
                    }
                } //happy
                else if (mostImportantDiff == 1) {
                    System.out.println("most important =1 ****" + differences[1]);

                    //happy increase
                    if (differences[1] > 0) {
                        nextAction = Math.round(5 * differences[1] * alphaFactor);
                    } else {
                        nextAction = Math.round(5 * differences[1] * (-1) * alphaFactor);
                    }
                } else {
                    System.out.println("most important =2 ****" + differences[2]);
                    //angry increase    
                    if (differences[2] > 0) {
                        nextAction = Math.round(5 * differences[2] * (-1) * alphaFactor);
                    } else {
                        nextAction = Math.round(5 * differences[2] * alphaFactor);
                    }
                }
            }
            nextDifficulty += nextAction;
            if (nextDifficulty > 5) {
                nextDifficulty = 5;
            } else if (nextDifficulty < 0) {
                nextDifficulty = 0;
            }
            this.previousAction = nextAction;
            System.out.println(this.id + " new diff = " + this.previousDifficulty + "+" + nextAction + " = " + nextDifficulty);

            this.previousDifficulty = nextDifficulty;

            return nextDifficulty;
        }
    }

    //initialize with id that corresponds to section type.
    public SectionOfGame(int id) {
        this.id = id;
        this.emotions = new float[7];
        this.allEmotions = new ArrayList<float[]>();
    }

    public int findMostImportantDiff(float[] diffs) {
        int maxInd = 0;
        float max = 0;
        //return the index of the biggest difference in emotions
        //0: neutral 1:happy 2:angry
        for (int i = 0; i <= 2; i++) {
            if (Math.abs(diffs[i]) > max) {
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
