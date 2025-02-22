#include <string.h>

#include <sys/select.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

#include <jni.h>
#include <android/log.h>

#include "byedpi/error.h"
#include "byedpi/proxy.h"
#include "utils.h"

static int g_proxy_fd = -1;

JNIEXPORT jint JNI_OnLoad(
        __attribute__((unused)) JavaVM *vm,
        __attribute__((unused)) void *reserved) {
    default_params = params;
    return JNI_VERSION_1_6;
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniCreateSocket(
        JNIEnv *env,
        __attribute__((unused)) jobject thiz,
        jobjectArray args) {

    if (g_proxy_fd != -1) {
        LOG(LOG_S, "proxy already running, fd: %d", g_proxy_fd);
        return -1;
    }

    int argc = (*env)->GetArrayLength(env, args);
    char *argv[argc];
    for (int i = 0; i < argc; i++) {
        jstring arg = (jstring) (*env)->GetObjectArrayElement(env, args, i);
        const char *arg_str = (*env)->GetStringUTFChars(env, arg, 0);
        argv[i] = strdup(arg_str);
        (*env)->ReleaseStringUTFChars(env, arg, arg_str);
    }

    int res = parse_args(argc, argv);

    if (res < 0) {
        uniperror("parse_args");
        return -1;
    }

    int fd = listen_socket((union sockaddr_u *)&params.laddr);

    if (fd < 0) {
        uniperror("listen_socket");
        return -1;
    }

    g_proxy_fd = fd;
    LOG(LOG_S, "listen_socket, fd: %d", fd);
    return fd;
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniStartProxy(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz) {

    LOG(LOG_S, "start_proxy, fd: %d", g_proxy_fd);

    if (start_event_loop(g_proxy_fd) < 0) {
        uniperror("event_loop");
        return get_e();
    }

    return 0;
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniStopProxy(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz) {

    LOG(LOG_S, "stop_proxy, fd: %d", g_proxy_fd);

    if (g_proxy_fd < 0) {
        LOG(LOG_S, "proxy is not running, fd: %d", g_proxy_fd);
        return 0;
    }

    reset_params();
    int res = shutdown(g_proxy_fd, SHUT_RDWR);
    g_proxy_fd = -1;

    if (res < 0) {
        uniperror("shutdown");
        return get_e();
    }

    return 0;
}

JNIEXPORT jint JNICALL
Java_io_github_dovecoteescapee_byedpi_core_ByeDpiProxy_jniCheckProxy(
        __attribute__((unused)) JNIEnv *env,
        __attribute__((unused)) jobject thiz) {

    LOG(LOG_S, "check_proxy, fd: %d", g_proxy_fd);

    if (g_proxy_fd < 0) {
        LOG(LOG_S, "proxy is not running, fd: %d", g_proxy_fd);
        return 0;
    }

    int val = 0;
    socklen_t len = sizeof(val);

    if (getsockopt(g_proxy_fd, SOL_SOCKET, SO_ACCEPTCONN, &val, &len) == -1) {
        perror("getsockopt SO_ACCEPTCONN");
        return 0;
    }

    if (val) {
        LOG(LOG_S, "proxy is listening on fd: %d", g_proxy_fd);
        return 1;
    } else {
        LOG(LOG_S, "proxy fd is open but not listening: %d", g_proxy_fd);
        return 0;
    }

    return 0;
}
