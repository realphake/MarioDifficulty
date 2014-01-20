/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Architect;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Norrie
 */
public class BayesOpt {
    
    /*************************************************************/
    /*** Default values                                        ***/
    /*************************************************************/

    /* Nonparametric process "parameters" */
    static final double KERNEL_THETA    = 1.0;
    static final double KERNEL_SIGMA    = 100.0;
    static final double MEAN_MU         = 1.0;
    static final double MEAN_SIGMA      = 1000.0;
    static final double PRIOR_ALPHA     = 1.0;
    static final double PRIOR_BETA      = 1.0;
    static final double DEFAULT_SIGMA   = 1.0;
    static final double DEFAULT_NOISE   = 1e-4;

    /* Algorithm parameters */
    static final int DEFAULT_ITERATIONS  = 1000;
    static final int DEFAULT_SAMPLES     = 30;
    static final int DEFAULT_VERBOSE     = 1;

    /* Algorithm limits */
    static final int MAX_ITERATIONS  = 1000;        /**< Used if n_iterations <0 */
    /*  const size_t MAX_DIM         = 40;         Not used */

    /* INNER Optimizer default values */
    static final int MAX_INNER_EVALUATIONS = 500;   /**< Used per dimmension */
    
    public String training_file;
    public Params par;
    public Vector results;
    public int dims;
    
    public BayesOpt()
    {
        
        training_file = "/home/stathis/NetBeansProjects/MAINOOR/MarioPOMDP-testinstances.arff";
        par = new Params();
        // setting the mean and std of the kernel
        dims = 6; // dimensions of the inputvector
        par.n_hp = dims;
        for(int i = 0 ; i<dims ; i++)
        {
            par.hp_mean[i] = 1.0;
            par.hp_std[i] = 1.0;
        }
        // Other parameters
        par.verbose_level = 2;
        
        // reading out the file to see how many samples we have
        try 
        {
            BufferedReader br = new BufferedReader(new FileReader(training_file));
            String line;
            int m = 0;
            while ((line = br.readLine()) != null) 
            {
                if(!line.startsWith("@"))
                {
                    m++;
                }
            }
            br.close();
            par.n_init_samples = m;
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(BayesOpt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    class Params {
        // Mean Params
        String mean_name;                  /**< Name of the mean function */
        double[] coef_mean;       /**< Basis function coefficients (mean)[128]  */
        double[] coef_std;        /**< Basis function coefficients (std)[128]  */
        int n_coef;               /**< Number of mean funct. hyperparameters */
        
        // Kernel Params
        String  kernel_name;                 /**< Name of the kernel function */
        double[] hp_mean;         /**< Kernel hyperparameters prior (mean) [128] */
        double[] hp_std;          /**< Kernel hyperparameters prior (st dev) [128] */
        int n_hp;                 /**< Number of kernel hyperparameters */
        
        // Function Params
        public int n_iterations;         /**< Maximum BayesOpt evaluations (budget) */
        public int n_inner_iterations;   /**< Maximum inner optimizer evaluations */
        public int n_init_samples;       /**< Number of samples before optimization */
        public int n_iter_relearn;       /**< Number of samples before relearn kernel */
        public int init_method;   /**< Sampling method for initial set 1-LHS, 2-Sobol (if available), other uniform */

        public int verbose_level;        /**< 1-Error,2-Warning,3-Info. 4-6 log file*/
        public String log_filename;          /**< Log file path (if applicable) */

        public String surr_name;             /**< Name of the surrogate function */
        public double sigma_s;              /**< Signal variance (if known) */
        public double noise;                /**< Observation noise (and nugget) */
        public double alpha;                /**< Inverse Gamma prior for signal var */
        public double beta;                 /**< Inverse Gamma prior for signal var*/
        public String l_type;        /**< Type of learning for the kernel params*/
        public double epsilon;              /**< For epsilon-greedy exploration */

        public String crit_name;             /**< Name of the criterion */
        public double crit_params;     /**< Criterion hyperparameters (if needed)[128] */
        public int n_crit_params;        /**< Number of criterion hyperparameters */
        
        public Params()
        {
            // kernel parameters 
            kernel_name = "kSEARD";;
            hp_mean = new double[128];
            hp_mean[0] = KERNEL_THETA;
            hp_std = new double[128];
            hp_std[0] = KERNEL_SIGMA;
            
            n_hp = 1;

            // mean parameters
            mean_name = "mConst";
            coef_mean[0] = MEAN_MU;
            coef_std[0] = MEAN_SIGMA;
            n_coef = 1;

            // function parameters
            n_iterations =   DEFAULT_ITERATIONS;
            n_inner_iterations = MAX_INNER_EVALUATIONS;
            n_init_samples = DEFAULT_SAMPLES;
            n_iter_relearn = 0;
            init_method = 1;

            verbose_level = DEFAULT_VERBOSE;
            log_filename = "bayesopt.log";

            surr_name = "sGaussianProcessML";

            sigma_s = DEFAULT_SIGMA;
            noise = DEFAULT_NOISE;
            alpha = PRIOR_ALPHA;
            beta = PRIOR_BETA;
            l_type = "L_MAP";
            epsilon = 0.0;

            crit_name = "cEI";
            n_crit_params = 0;

        }
    }
    
    void setSurrogateModel()
    {
        
    }
    
    public Vector optimize()
    {
        
        return new Vector();
    }
    
////// FROM HERE FOLLOWS THE BAYESOPT ALGORITHM
    
    // We want to calculate the bestPoint before every new chunk by calling 
    // optimize for one step
    /*
    int BayesOptBase::optimize(vectord &bestPoint)
    {
      initializeOptimization();
        int ContinuousModel::initializeOptimization()
        {
          if (mBB == NULL) //mBB is null because no bounds were given
            {
              vectord lowerBound = zvectord(mDims);
              vectord upperBound = svectord(mDims,1.0);
              mBB = new utils::BoundingBox<vectord>(lowerBound,upperBound);
            }
          sampleInitialPoints(); // use from main.ccp
          return 0;
        }
      
      assert(mDims == bestPoint.size());// not relevant

      for (size_t ii = 0; ii < mParameters.n_iterations; ++ii)
        {      
          stepOptimization(ii); // main process
        }

      bestPoint = getFinalResult(); // not relevant since we will return the new
                                    // parameters after one step of optimization

      return 0;
    } // optimize
    
    int BayesOptBase::stepOptimization(size_t ii)
    {
      vectord xNext(mDims);
      nextPoint(xNext); // Find what is the next point. Hardest Part!

      double yNext = evaluateSampleInternal(xNext); 
            inline double evaluateSampleInternal( const vectord &query )
            { 
              vectord unnormalizedQuery = mBB->unnormalizeVector(query);
              return evaluateSample(unnormalizedQuery); // use main.ccp for this 
            }; // evaluateSampleInternal
    
        // Create 3 function 
        // initilization, 
        // get next parameter set, 
        // update model with rewards

      // Update surrogate model
      if ((mParameters.n_iter_relearn > 0) && 
          ((ii + 1) % mParameters.n_iter_relearn == 0))
        mGP->fullUpdateSurrogateModel(xNext,yNext); 
      else
        mGP->updateSurrogateModel(xNext,yNext); // update part relevant after we
                                                // receive rewards

      plotStepData(ii,xNext,yNext);// not relevant
      return 0;
    }
    
    int BayesOptBase::nextPoint(vectord &Xnext)
    {
      int error = 0;

      //Epsilon-Greedy exploration (see Bull 2011) // epsilon set to 0.0 so not relevant
      if ((mParameters.epsilon > 0.0) && (mParameters.epsilon < 1.0))
        {
          randFloat drawSample(mEngine,realUniformDist(0,1));
          double result = drawSample();
          FILE_LOG(logINFO) << "Trying random jump with prob:" << result;
          if (mParameters.epsilon > result)
            {
              for (size_t i = 0; i <Xnext.size(); ++i)
                {
                   Xnext(i) = drawSample();
                } 
              FILE_LOG(logINFO) << "Epsilon-greedy random query!";
              return 0;
            }
        }

      if (mCrit->requireComparison()) // what is this can't find it...
        {
          bool check = false;
          std::string name;

          mCrit->reset();
          while (!check)
            {
              findOptimal(Xnext);
              check = mCrit->checkIfBest(Xnext,name,error);
            }
          FILE_LOG(logINFO) << name << " was selected.";
        }
      else
        {
          findOptimal(Xnext);
            inline int findOptimal(vectord &xOpt) // This is where Nlopt starts :(
            { return innerOptimize(xOpt); }; 
        }
      return error;
    }
    
    int InnerOptimization::innerOptimize(vectord &Xnext)
    {   
      void *objPointer = static_cast<void *>(this);
      int n = static_cast<int>(Xnext.size());
      int error;

      assert(objPointer != NULL);
      error = innerOptimize(&Xnext(0), n, objPointer);

      return error;
    } // innerOptimize (uBlas)

    int InnerOptimization::innerOptimize(double* x, int n, void* objPointer)
    {
      double u[128], l[128];
      double fmin = 1;
      int maxf = MAX_INNER_EVALUATIONS*n;    
      int ierror;

      for (int i = 0; i < n; ++i) {
        l[i] = mDown;	u[i] = mUp;
        // What if x is undefined?
        if (x[i] < l[i] || x[i] > u[i])
          x[i]=(l[i]+u[i])/2.0;
      }

      nlopt_opt opt;
      double (*fpointer)(unsigned int, const double *, double *, void *);
      double coef = 0.8;  //Percentaje of resources used in local optimization

        // Learning type is COMBINED
      // algorithm and dims
      if (alg == LBFGS)                                     //Require gradient
        fpointer = &(NLOPT_WPR::evaluate_nlopt_grad);
      else                                           //Do not require gradient
        fpointer = &(NLOPT_WPR::evaluate_nlopt);

      if (alg == COMBINED)  coef = 0.8;

      switch(alg)
        {
        case DIRECT:      // same as combined 
        case COMBINED: 	opt = nlopt_create(NLOPT_GN_DIRECT_L, n); break;
            //sets opt to default values
        case BOBYQA: 	opt = nlopt_create(NLOPT_LN_BOBYQA, n); break;
        case LBFGS:       opt = nlopt_create(NLOPT_LD_LBFGS, n); break;
        default: FILE_LOG(logERROR) << "Algorithm not supported"; return -1;
        }

      nlopt_set_lower_bounds(opt, l);
        opt->lb
      nlopt_set_upper_bounds(opt, u);
        opt->ub
      nlopt_set_min_objective(opt, fpointer, objPointer);
        nlopt_result NLOPT_STDCALL nlopt_set_min_objective(nlopt_opt opt,
						   nlopt_func f, void *f_data)
        {
             if (opt) {
                  if (opt->munge_on_destroy) opt->munge_on_destroy(opt->f_data);
                  opt->f = f; opt->f_data = f_data;
                  opt->maximize = 0;
                  if (nlopt_isinf(opt->stopval) && opt->stopval > 0)
                       opt->stopval = -HUGE_VAL; // switch default from max to min 
                  return NLOPT_SUCCESS;
             }
             return NLOPT_INVALID_ARGS;
        }
      int nfeval = static_cast<int>(static_cast<double>(maxf)*coef);
      nlopt_set_maxeval(opt, nfeval) ;


      nlopt_result errortype = nlopt_optimize(opt, x, &fmin);
      checkNLOPTerror(errortype);

      // Local refinement
      if ((alg == COMBINED) && (coef < 1)) 
        {
          nlopt_destroy(opt);  // Destroy previous one
          opt = nlopt_create(NLOPT_LN_SBPLX, n); // algorithm and dims 
            //sets opt to default values
          nlopt_set_lower_bounds(opt, l);
            opt->lb
          nlopt_set_upper_bounds(opt, u);
            opt->ub
          nlopt_set_min_objective(opt, fpointer, objPointer);
            
          nlopt_set_maxeval(opt, maxf-nfeval);

          errortype = nlopt_optimize(opt, x, &fmin);
          checkNLOPTerror(errortype);
        }

      nlopt_destroy(opt);  // Destroy opt

      ierror = static_cast<int>(errortype);
      return ierror;

    } // innerOptimize (C array)
    
    
    struct nlopt_opt_s {
        nlopt_algorithm algorithm; /* the optimization algorithm (immutable) 
        unsigned n; /* the dimension of the problem (immutable) 

        nlopt_func f; void *f_data; /* objective function to minimize 
        int maximize; /* nonzero if we are maximizing, not minimizing 

        double *lb, *ub; /* lower and upper bounds (length n) 

        unsigned m; /* number of inequality constraints 
        unsigned m_alloc; /* number of inequality constraints allocated 
        nlopt_constraint *fc; /* inequality constraints, length m_alloc 

        unsigned p; /* number of equality constraints 
        unsigned p_alloc; /* number of inequality constraints allocated 
        nlopt_constraint *h; /* equality constraints, length p_alloc 

        nlopt_munge munge_on_destroy, munge_on_copy; /* hack for wrappers 

        /* stopping criteria 
        double stopval; /* stop when f reaches stopval or better 
        double ftol_rel, ftol_abs; /* relative/absolute f tolerances 
        double xtol_rel, *xtol_abs; /* rel/abs x tolerances 
        int maxeval; /* max # evaluations 
        double maxtime; /* max time (seconds) 

        int force_stop; /* if nonzero, force a halt the next time we
                           try to evaluate the objective during optimization 
        /* when local optimization is used, we need a force_stop in the
           parent object to force a stop in child optimizations 
        struct nlopt_opt_s *force_stop_child;

        /* algorithm-specific parameters 
        nlopt_opt local_opt; /* local optimizer 
        unsigned stochastic_population; /* population size for stochastic algs 
        double *dx; /* initial step sizes (length n) for nonderivative algs 
        unsigned vector_storage; /* max subspace dimension (0 for default) 

        double *work; /* algorithm-specific workspace during optimization 
   };
    
    nlopt_result 
    NLOPT_STDCALL nlopt_optimize(nlopt_opt opt, double *x, double *opt_f)
    {
         nlopt_func f; void *f_data;
         f_max_data fmd;
         int maximize;
         nlopt_result ret;

         if (!opt || !opt_f || !opt->f) return NLOPT_INVALID_ARGS;
         f = opt->f; f_data = opt->f_data;

         /* for maximizing, just minimize the f_max wrapper, which 
            flips the sign of everything 
         if ((maximize = opt->maximize)) {
              fmd.f = f; fmd.f_data = f_data;
              opt->f = f_max; opt->f_data = &fmd;
              opt->stopval = -opt->stopval;
              opt->maximize = 0;
         }

         { /* possibly eliminate lb == ub dimensions for some algorithms 
              nlopt_opt elim_opt = opt;
              if (elimdim_wrapcheck(opt)) {
                   elim_opt = elimdim_create(opt);
                   if (!elim_opt) { ret = NLOPT_OUT_OF_MEMORY; goto done; }
                   elimdim_shrink(opt->n, x, opt->lb, opt->ub);
              }

              ret = nlopt_optimize_(elim_opt, x, opt_f);
                // Case NLOPT_GN_DIRECT_L
                return cdirect(ni, f, f_data, 
			     lb, ub, x, minf, &stop, 0.0, 13);

                // Case NLOPT_LN_SBPLX: 
                {
                     nlopt_result ret;
                     int freedx = 0;
                     if (!opt->dx) {
                          freedx = 1;
                          if (nlopt_set_default_initial_step(opt, x) != NLOPT_SUCCESS) 
                                    //dx should probably be assigned [1]*n
                               return NLOPT_OUT_OF_MEMORY;
                     }
                     if (algorithm == NLOPT_LN_NELDERMEAD)
                          ret= nldrmd_minimize(ni,f,f_data,lb,ub,x,minf,opt->dx,&stop);
                     else
                          ret= sbplx_minimize(ni,f,f_data,lb,ub,x,minf,opt->dx,&stop);
                     if (freedx) { free(opt->dx); opt->dx = NULL; }
                     return ret;

              if (elim_opt != opt) {
                   elimdim_destroy(elim_opt);
                   elimdim_expand(opt->n, x, opt->lb, opt->ub);
              }
         }

    done:
         if (maximize) { /* restore original signs 
              opt->maximize = maximize;
              opt->stopval = -opt->stopval;
              opt->f = f; opt->f_data = f_data;
              *opt_f = -*opt_f;
         }

         return ret;
    }
    
    
    Find sbplx_minimize and cdirect: Done
    cdirect.cdirect.c // Some kind of rectangular space division
    cdirect.cdirect.h
    dependencies:
        util.redblack.h // Tree structure lib
        util.redblack.c
    
    neldermead.sbplx.c
    neldermead.sbplx.h
    
    
    */
}
