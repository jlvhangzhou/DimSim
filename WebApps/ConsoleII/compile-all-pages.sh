#!/bin/bash
rm -rf www
DIR=`dirname $0`;
echo $DIR
java -Xms512M -Xmx512M -cp "src/:../../ThirdPartyPackages/GWT15/gwt-user.jar:../../ThirdPartyPackages/GWT15/gwt-dev-linux.jar:../../ThirdPartyPackages/GWT15/gwt-widgets-0.2.0.jar:../../ThirdPartyPackages/GWT15/gwttk-SNAPSHOT.jar:../../ThirdPartyPackages/GWT15/gwt2swf.jar:../../ThirdPartyPackages/UICommon/dimdim_ui_common.jar" com.google.gwt.dev.GWTCompiler -out www com.dimdim.conference.ui.workspacepopout.WorkspacePopout

java -Xms512M -Xmx512M -cp "src:../../ThirdPartyPackages/GWT15/gwt-user.jar:../../ThirdPartyPackages/GWT15/gwt-dev-linux.jar:../../ThirdPartyPackages/GWT15/gwt-widgets-0.2.0.jar:../../ThirdPartyPackages/GWT15/gwttk-SNAPSHOT.jar:../../ThirdPartyPackages/UICommon/dimdim_ui_common.jar" com.google.gwt.dev.GWTCompiler -out www com.dimdim.conference.ui.envcheck.EnvCheck

java -Xms512M -Xmx512M -cp "src:../../ThirdPartyPackages/GWT15/gwt-user.jar:../../ThirdPartyPackages/GWT15/gwt-dev-linux.jar:../../ThirdPartyPackages/GWT15/gwt-widgets-0.2.0.jar:../../ThirdPartyPackages/GWT15/gwttk-SNAPSHOT.jar:../../ThirdPartyPackages/GWT15/gwt2swf.jar:../../ThirdPartyPackages/UICommon/dimdim_ui_common.jar" com.google.gwt.dev.GWTCompiler -out www com.dimdim.conference.ui.layout2.Layout2
