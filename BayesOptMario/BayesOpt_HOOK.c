#include <inttypes.h>
#include <jni.h>
#include "BayesOpt_HOOK.h"
#include "BayesOpt_IMPL.h"

/*
 * Class:     Architect_BayesOpt_HOOK
 * Method:    init
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_Architect_BayesOpt_1HOOK_init
  (JNIEnv *env, jclass obj, jstring javaString){
    const char *nativeString = env->GetStringUTFChars( javaString, 0);
    init(nativeString);
    env->ReleaseStringUTFChars( javaString, nativeString);
}

/*
 * Class:     Architect_BayesOpt_HOOK
 * Method:    nextParameters
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_Architect_BayesOpt_1HOOK_nextParameters
  (JNIEnv *env, jclass obj){
    return env->NewStringUTF(nextParameters());;
}

JNIEXPORT void JNICALL Java_Architect_BayesOpt_1HOOK_updateModel
  (JNIEnv *env, jclass obj, jstring javaString){
    const char *nativeString = env->GetStringUTFChars(javaString, 0);
	updateModel(nativeString);
    env->ReleaseStringUTFChars(javaString, nativeString);
}