#!/usr/bin/env python3

## this script uses some code form https://github.com/EMResearch/EMB/blob/master/scripts/dist.py

EVOMASTER_VERSION = "1.6.2-SNAPSHOT"

import os
import shutil
import platform
from shutil import copy
from subprocess import run
from os.path import expanduser


### Environment variables ###
JAVA_HOME_8 = os.environ.get('JAVA_HOME_8', '')
JAVA_HOME_11 = os.environ.get('JAVA_HOME_11', '')


HOME = expanduser("~")
SCRIPT_LOCATION = os.path.dirname(os.path.realpath(__file__))
PROJ_LOCATION = os.path.abspath(SCRIPT_LOCATION)

DIST = os.path.join(PROJ_LOCATION, "dist")

SHELL = platform.system() == 'Windows'

def checkJavaVersions():
    if JAVA_HOME_8 == '':
        print("\nERROR: JAVA_HOME_8 environment variable is not defined")
        exit(1)

    if JAVA_HOME_11 == '':
        print("\nERROR: JAVA_HOME_11 environment variable is not defined")
        exit(1)

checkJavaVersions()

def prepareDistFolder():

    if os.path.exists(DIST):
        shutil.rmtree(DIST)

    os.mkdir(DIST)

prepareDistFolder()

def callMaven(folder, jdk_home):
    env_vars = os.environ.copy()
    env_vars["JAVA_HOME"] = jdk_home

    mvnres = run(["mvn", "clean", "install", "-DskipTests"], shell=SHELL, cwd=os.path.join(PROJ_LOCATION,folder), env=env_vars)
    mvnres = mvnres.returncode

    if mvnres != 0:
        print("\nERROR: Maven command failed")
        exit(1)

def buildEvoMaster() :

    mvnres = run(["mvn", "clean", "install", "-DskipTests"], shell=SHELL, cwd=os.path.join(PROJ_LOCATION, "EvoMaster"))
    mvnres = mvnres.returncode

    if mvnres != 0:
        print("\nERROR: Maven command failed for building EvoMaster")
        exit(1)

# build evomaster
buildEvoMaster()

def build_jdk_8_maven() :

    folder = "EMB/jdk_8_maven"
    callMaven(folder, JAVA_HOME_8)

    # Copy JAR files
    copy(folder +"/cs/rest/original/catwatch/catwatch-backend/target/catwatch-sut.jar", DIST)
    copy(folder +"/em/external/rest/catwatch/target/catwatch-evomaster-runner.jar", DIST)

    copy(folder +"/cs/rest-gui/genome-nexus/web/target/genome-nexus-sut.jar", DIST)
    copy(folder +"/em/external/rest/genome-nexus/target/genome-nexus-evomaster-runner.jar", DIST)

build_jdk_8_maven()

def build_jdk_11_maven() :

    folder = "EMB/jdk_11_maven"
    callMaven(folder, JAVA_HOME_11)

    copy(folder +"/cs/rest/cwa-verification-server/target/cwa-verification-sut.jar", DIST)
    copy(folder +"/em/external/rest/cwa-verification/target/cwa-verification-evomaster-runner.jar", DIST)

    ind1 = os.environ.get('SUT_LOCATION_IND1', '')
    if ind1 == '':
            print("\nWARN: SUT_LOCATION_IND1 env variable is not defined")
    else:
            copy(ind1, os.path.join(DIST, "ind1-sut.jar"))
            copy(folder +"/em/external/rest/ind1/target/ind1-evomaster-runner.jar", DIST)


build_jdk_11_maven()

print("\n\nSUCCESS\n\n")