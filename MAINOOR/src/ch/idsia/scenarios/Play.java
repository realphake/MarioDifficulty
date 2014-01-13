package ch.idsia.scenarios;

import Architect.Architect;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.AgentsPool;
import ch.idsia.ai.agents.ai.*;
import ch.idsia.ai.agents.human.*;
import ch.idsia.ai.tasks.CoinTask;
import competition.cig.robinbaumgarten.AStarAgent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.mario.engine.DifficultyRecorder;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import competition.icegic.erek.ErekTask;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 * * Created by IntelliJ IDEA. * User: julian * Date: May 5, 2009 * Time:
 * 12:46:43 PM
 */
public class Play {

    static String taskTypeString;

    public static String taskType() {
        return Play.taskTypeString;
    }

    ; public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        Play.taskTypeString = "bullet";
        Agent controller = new ForwardAgent();
        Architect arch = new Architect();
        AgentsPool.addAgent(controller);
        EvaluationOptions options = new CmdLineOptions(new String[0]);
        options.setAgent(controller);
        Task task = new ErekTask();
        options.setMaxFPS(false);
        options.setVisualization(true);
        options.setNumberOfTrials(1);
        options.setMatlabFileName("");
        options.setLevelRandSeed((int) (Math.random() * Integer.MAX_VALUE));
        options.setLevelDifficulty(5);
        task.setOptions(options);
        task.evaluate(controller,arch);
    }
}