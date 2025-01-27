package com.example.godotrl.cst.behavior;
import br.unicamp.cst.core.entities.Codelet;


import br.unicamp.cst.core.entities.MemoryObject;
import com.example.godotrl.util.Action;
import com.example.godotrl.util.State;
import com.example.godotrl.util.Updater;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class LearnerCodelet extends Codelet {
    Boolean isTraining;
    Boolean isTabular;
    Double epsilon;
    Double epsilonInitial;
    Double epsilonFinal;
    Double epsilonDecay;
    Tabular qLearning;
    LFA lfa;
    Long numEpisodes;
    // actionSpace[3] = {isRealDomain(0 or 1), minValue, maxValue-1}
    Domain<Double>[] actionSpace;
    FrogEnv env;
    Long currEpisode = 0L;
    Integer currStep = 0;
    private CSV csvRewardRecord;
    // Serialization of q-table or weights. e.g. "q_learning.ser", "lfa_weights.ser"
    String learningFileName;
    // csv format. E.g. "cumulative_learning_lfa.csv"
    String cumRewardFileName;
    // for both learning and cumulative reward
    String localPathToCheckpoint;
    Boolean episodeIsDone = false;
    Domain reward = new Domain(0.0);
    Long checkpointEachNEpisodes;
    private Double greatestCheckpointReward = Double.MIN_VALUE;
    private MemoryObject updateMO;
    private MemoryObject motorMO;
    private MemoryObject stateMO;
    private MemoryObject lastStateMO;

    private boolean doneRunning = false;
    private boolean hasWon = false;
    private boolean hasLost = false;
    private Integer nMaxSteps;
    private ArrayList lastObs;


    public LearnerCodelet(
            Double epsilonInitial, Double epsilonFinal,
            Long numEpisodes, Boolean isTraining, Boolean isTabular,
            ValueBasedRL learning, Domain<Double>[] actionSpace,
            String localPathToCheckpoint, String learningFileName,
            String cumRewardFileName, Long checkpointEachNEpisodes, Integer nMaxSteps) {
        this.epsilon = epsilonInitial;
        this.epsilonInitial = epsilonInitial;
        this.epsilonFinal = epsilonFinal;
        this.numEpisodes = numEpisodes;
        this.epsilonDecay = (epsilonInitial - epsilonFinal) / numEpisodes;
        this.isTraining = isTraining;
        this.isTabular = isTabular;
        this.actionSpace = actionSpace;
        this.env = new FrogEnv( stateMO, actionSpace );
        this.localPathToCheckpoint = localPathToCheckpoint;
        this.learningFileName = learningFileName;
        this.cumRewardFileName = cumRewardFileName;
        this.checkpointEachNEpisodes = checkpointEachNEpisodes;
        this.nMaxSteps = nMaxSteps;
        setLearningType( learning );
        this.csvRewardRecord = new CSV(localPathToCheckpoint, cumRewardFileName, false, true);
    }

    private void setLearningType( ValueBasedRL learning ) {
        if (this.isTabular) {
            this.qLearning = (Tabular) learning;
            this.qLearning.initQTable();
        // new Tabular( this.alpha, this.gamma, this.actionSpace[2].intValue(), this.localPathToCheckpoint );
        } else {
            FeaturesExtractor fe = new FeaturesExtractor();
            this.lfa = (LFA) learning;
            // new LFA( this.alpha, this.gamma, this.actionSpace[2].intValue(), this.localPathToCheckpoint, fe);
        }
    }

    @Override
    public void proc() {
        if (((Updater) this.updateMO.getI()).updateLearner() && !doneRunning) {
        /*
            Old code below, should be reimplemented above
        */

            hasLost = hasLost || currStep >= nMaxSteps;
            episodeIsDone = hasWon || hasLost;

            if (this.currEpisode < this.numEpisodes) {

                if (this.currEpisode == 0 && this.currStep == 0) {
                    if (Files.exists(
                            Path.of(this.localPathToCheckpoint + this.learningFileName))) {
                        this.deserializeLearning();
                    }
                }

                // Gets s_n, s_(n-1) and a_(n-1). On first call, s_(-1) = null and a_(-1) = Action.INVALID
                State lastState = (State) lastStateMO.getI();
                Action lastAction = (Action) motorMO.getI();
                State state = (State) stateMO.getI();

                /* Q LEARNING ALGORITHM */
                ArrayList step = env.step(state, lastAction, episodeIsDone, hasWon);

                Double currReward = ((Double) step.get(1));
                this.reward = new Domain<Double>(this.reward.doubleValue() + currReward);

                // ArrayList lastObs = env.getObservationSpace( lastState, lastAction );
                ArrayList obs = ((ArrayList<Domain>) step.get(0));
                Domain idAction = env.getActionID(lastAction);

                if (!this.episodeIsDone && currStep > 0) {

                    if (isTabular) {
                        qLearning.update(lastObs, obs, idAction, new Domain<Double>(currReward));
                    } else {
                        lfa.update(lastObs, obs, idAction, new Domain<Double>(currReward));
                    }
                }

                /* CHOOSE ACTION */
                Action action;

                if ( currStep < nMaxSteps ) {
                    if (isTabular)
                        idAction = qLearning.epsilonGreedyPolicy(this.epsilon, obs);
                    else
                        idAction = lfa.epsilonGreedyPolicy(this.epsilon, obs);

                    // Action should be decided through the Q-Learning algorithm. Should be an element of the enum Action
                    action = env.convertIdToAction(idAction);
                }
                else
                    action = Action.RESET;

                motorMO.setI(action);
                lastObs = obs;

                if (episodeIsDone) {
                    if ((this.currEpisode + 1) % this.checkpointEachNEpisodes == 0) {
                        if (this.reward.doubleValue() > this.greatestCheckpointReward) {
                            this.greatestCheckpointReward = this.reward.doubleValue();
                            serializeLearning();
                        }
                    }

                    csvRewardRecord.recordNewEpisode(
                            currEpisode, reward, epsilon);

                    reward = new Domain<Double>(0.0);
                    currStep = 0;
                    currEpisode++;

                    this.epsilon = Math.max(
                            this.epsilon - this.epsilonDecay,
                            this.epsilonFinal);
                }
                else {
                    currStep++;
                }
            }
            else {
                doneRunning = true;
                serializeLearning( localPathToCheckpoint + "final_" + learningFileName );
            }
        }
    }

    private void serializeLearning() {
        if (this.isTabular) {
            qLearning.serializeLearning(this.localPathToCheckpoint + this.learningFileName);
        }
        else {
            lfa.serializeLearning(this.localPathToCheckpoint + this.learningFileName);
        }
    }

    private void serializeLearning(String path) {
        if (this.isTabular) {
            qLearning.serializeLearning(path);
        }
        else {
            lfa.serializeLearning(path);
        }
    }

    private void deserializeLearning() {
        if (this.isTabular) {
            qLearning.deserializeLearning( this.learningFileName );
        }
        else {
            lfa.deserializeLearning( this.learningFileName );
        }
    }

    public void lose() {
        hasLost = true;
    }

    public void win() {
        hasWon = true;
    }

    public void resetWinState() {
        hasWon = false;
        hasLost = false;

        currStep = 0;
    }

    public boolean canEnd() {
        return episodeIsDone;
    }

    @Override
    public void accessMemoryObjects () {
        stateMO = (MemoryObject) getInput("STATE");
        lastStateMO = (MemoryObject) getInput("LAST_STATE");
        updateMO = (MemoryObject) getOutput("UPDATE");
        motorMO = (MemoryObject) getOutput("MOTOR");
    }

    @Override
    public void calculateActivation () {
        return;
    }
}