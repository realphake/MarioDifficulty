#ifndef _BAYESOPT_IMPL_H
#define _BAYESOPT_IMPL_H

#ifdef __cplusplus
        extern "C" {
#endif

         void updateModel(const char* rew);
         const char* nextParameters();
	 void init(const char* training);

#ifdef __cplusplus
        }
#endif

#endif