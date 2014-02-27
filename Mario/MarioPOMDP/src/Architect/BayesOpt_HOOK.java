/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Architect;

/**
 *
 * @author Norbert Heijne
 */
public class BayesOpt_HOOK {
    static {
            System.loadLibrary("BayesOptMario");
    }
    public native void updateModel(String reward);
    public native String nextParameters();
    public native void init(String trainFilePath);
}
