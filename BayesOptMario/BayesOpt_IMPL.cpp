/*
-------------------------------------------------------------------------
   This file is part of BayesOpt, an efficient C++ library for 
   Bayesian optimization.

   Copyright (C) 2011-2013 Ruben Martinez-Cantin <rmcantin@unizar.es>
 
   BayesOpt is free software: you can redistribute it and/or modify it 
   under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   BayesOpt is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with BayesOpt.  If not, see <http://www.gnu.org/licenses/>.
------------------------------------------------------------------------
*/

#include <ctime>
#include "bayesoptwpr.h"                 // For the C API
#include "bayesoptcont.hpp"
#include <cstdlib>
#include "bayesoptwpr.h"               // For the C API
#include "bayesoptdisc.hpp"            // For the C++ API
#include "lhs.hpp"
#include <stdio.h>
#include <fstream>
#include <unistd.h>
#include <string.h>
#include <assert.h>
#include <stdlib.h>
#include <BayesOpt_IMPL.h>
using namespace std;

string int_array_to_string(double int_array[], int size_of_array){
ostringstream oss("");
 for (int temp = 0; temp < size_of_array; temp++)
 {
     
    oss <<round(int_array[temp]*4)+1<<" ";
 }
 return oss.str();
}

/* Class to be used for C++-API testing */
class marioBayesOpt: public bayesopt::ContinuousModel
{
 public:
  string Training;
  vectord next;
  int updateNumber;
  char* reward;
  

  marioBayesOpt(size_t dim,bopt_params param,string training):
    ContinuousModel(dim,param) {
    Training = training;
    updateNumber = 0;
  }
  /*
  void stepOptimization1()
  {
    this->nextPoint(next); // Find what is the next point.
  }
  
  void stepOptimization2()
  {
    double yNext = this->evaluateSampleInternal(next);
    updateNumber++;
    // Update surrogate model
    ContinuousModel::getSurrogateModel()->updateSurrogateModel(next,yNext); 
  }
   */
  double evaluateSample( const vectord &Xi ) 
  {
      
    printf ("Received Reward\n");
    int value = atoi(reward); //value = 45 
    return value;
     // return 10;
  };

  int sampleInitialPoints()
  {
    
    string line;
    float n_value;
    string value;
    int kStart = 45;
    int kEnd = 50;
    int kvalue = 53;
    int k = 0;
    int m = 0;
    ifstream myfile(Training.c_str());
    size_t nSamples = ContinuousModel::getParameters()->n_init_samples;
    
    matrixd initPoints(nSamples,6);
    vectord initValues(nSamples);
    
    if (myfile.is_open()){
        while(getline(myfile,line)){
            if(line[0]!='@'){
                k = 0;
                istringstream iss(line);
                while ( getline(iss,value,',') ){
                    n_value = atof(value.c_str());
                    if(k>=kStart && k<=kEnd)
                    {
                         initPoints(m,k-kStart) = n_value/5.0;
                         
                         
                    }
                    if(k==kvalue)
                    {
                        initValues(m)=n_value;
                        cout<<n_value<<endl;
                    }
                    k++;
                }
                m++;
            }
        }
    }
    
    
    double yPoint;
    vectord xPoint(6);

    for(size_t i = 0; i < nSamples; i++)
    {
      xPoint = row(initPoints,i);
      yPoint = -initValues(i);
      ContinuousModel::getSurrogateModel()->addSample(xPoint,yPoint);
    }

    ContinuousModel::getSurrogateModel()->fitInitialSurrogate(true);
    return 0;
  }


  bool checkReachability( const vectord &query )
  { return true; };
 
};

marioBayesOpt *opt;

void updateModel(const char* rew)
{
	strcpy(opt->reward , rew);
	opt->stepOptimization2(opt->next);
}


const char* nextParameters(){
    opt->next = opt->stepOptimization1();
    double x[100];
    for (size_t i = 0; i < opt->next.size(); ++i)
	x[i] = opt->next(i);
        
    return int_array_to_string(x,6).c_str();
}

void init(const char* train){
    string training = string(train);
    //Parameter Initialisation
    bopt_params par = initialize_parameters_to_default();

    //General Parameters
    int n = 6;                   // Number of dimensions
    par.n_iterations = 1000;       // Number of iterations

    cout<<training<<endl;
    //For init samples open file and count
    ifstream myfile(training.c_str());
    int m = 0; 
    string line;
    if (myfile.is_open()){
          while(getline(myfile,line)){
              if(line[0]!='@'){
                  m++;
              }
          }
    }
    par.n_init_samples = m;
    par.verbose_level = 2;

    //Model Parameters
    par.surr_name = "sGaussianProcessML";

    //Kernel Parameters
    par.kernel.name = "kSEARD";
    par.kernel.n_hp = n;
    for(int i = 0 ; i<n ; i++)
    {
      par.kernel.hp_mean[i] = 1.0;
      par.kernel.hp_std[i] = 1.0;
    }
    //Exploration Parameters
    par.epsilon = 0;
    par.crit_name = "cEI";

    std::cout << "Running C++ interface" << std::endl;
    marioBayesOpt opt1(n,par,training);
    opt = &opt1;
    opt->initializeOptimization();
    std::cout << "Initialized." << std::endl;
}

