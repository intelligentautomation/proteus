#!/bin/bash

##########################################
# CONFIGURE THE FOLLOWING
##########################################

# NOTE ! No trailing slashes ! 

# Root of Git repository 
# E.g. /Users/userid/dev/git/proteus
PROTEUS_SRC_ROOT=<edit me>
# Root of Eclipse installation (with Delta pack)
# E.g. /Users/userid/eclipse-rcp-indigo
ECLIPSE_TARGET_ROOT=<edit me>

##########################################
# OPTIONAL
##########################################

# Directory where the build is controlled 
PDE_BUILDER_DIR=$PROTEUS_SRC_ROOT/utilities/builder
# The output directory of the build 
BUILD_TARGET=~/dev/git/proteus/utilities/target

##########################################
# DO NOT EDIT BELOW
##########################################

echo "Preparing to build..."
rm -fr $BUILD_TARGET
mkdir $BUILD_TARGET
cp -r $PROTEUS_SRC_ROOT/plugins $BUILD_TARGET
cp -r $PROTEUS_SRC_ROOT/features $BUILD_TARGET

echo "Building..."
java -jar $ECLIPSE_TARGET_ROOT/plugins/org.eclipse.equinox.launcher_*.jar -application org.eclipse.ant.core.antRunner -buildfile $ECLIPSE_TARGET_ROOT/plugins/org.eclipse.pde.build_*/scripts/productBuild/productBuild.xml -Dbuilder=$PDE_BUILDER_DIR -DbaseLocation=$ECLIPSE_TARGET_ROOT -DbuildDirectory=$BUILD_TARGET
