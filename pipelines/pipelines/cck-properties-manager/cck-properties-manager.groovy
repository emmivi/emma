@Library('cck-jenkins-utils') _
import groovy.io.FileType;
import fr.bnpp.cck.jenkins.util.ChangeLog;
import fr.bnpp.cck.jenkins.util.PropertiesManagerUtil;


node() {

	env.WORKSPACE = pwd();
	def dateStr = new Date().format("yyyyMMdd-hhmm");
	def PROPS_URL = "http://svn-mx-pfc.rb.echonet/svn/cckfr/cckproperties-apps";

	def STABLE_PATH = "${env.WORKSPACE}/stable";
	def STABLE_BACKEND_PROPS = "${STABLE_PATH}/backend";
	def STABLE_FRONTEND_PROPS = "${STABLE_PATH}/frontend";
	def STABLE_BATCHES_PROPS = "${STABLE_PATH}/batches";

	def DEVELOPMENT_PATH = "${env.WORKSPACE}/development";
	def DEVELOPMENT_BACKEND_PROPS = "${DEVELOPMENT_PATH}/backend";
	def DEVELOPMENT_FRONTEND_PROPS = "${DEVELOPMENT_PATH}/frontend";
	def DEVELOPMENT_BATCHES_PROPS = "${DEVELOPMENT_PATH}/batches";	

	def CANDIDATE_PATH = "${env.WORKSPACE}/candidate";
	def CANDIDATE_BACKEND_PROPS = "${CANDIDATE_PATH}/backend";
	def CANDIDATE_FRONTEND_PROPS = "${CANDIDATE_PATH}/frontend";
	def CANDIDATE_BATCHES_PROPS = "${CANDIDATE_PATH}/batches";	

	def CHANGES_PATH = "${env.WORKSPACE}/changes/${dateStr}";
	def CHANGES_BACKEND_PROPS = "${CHANGES_PATH}/backend";
	def CHANGES_FRONTEND_PROPS = "${CHANGES_PATH}/frontend";
	def CHANGES_BATCHES_PROPS = "${CHANGES_PATH}/batches";		

	def changeLog = new ChangeLog();
	def propertiesMU = new PropertiesManagerUtil(this);

	stage("Prepare") {
		//deleteDir();
		propertiesMU.setChangeLog(changeLog);
		println "WORKSPACE ${env.WORKSPACE}";
		println "PROPS_URL :: ${PROPS_URL}";
		println "STABLE_BACKEND_PROPS :: ${STABLE_BACKEND_PROPS}";
		println "STABLE_FRONTEND_PROPS :: ${STABLE_FRONTEND_PROPS}";
	}

	stage("Checkout"){
		svn PROPS_URL;
	}

	stage("Merging"){
		try{
			stage("Backend"){
				propertiesMU.initMerge(DEVELOPMENT_BACKEND_PROPS, STABLE_BACKEND_PROPS, CANDIDATE_BACKEND_PROPS, CHANGES_BACKEND_PROPS);
			}
			stage("Frontend"){
				propertiesMU.initMerge(DEVELOPMENT_FRONTEND_PROPS, STABLE_FRONTEND_PROPS, CANDIDATE_FRONTEND_PROPS, CHANGES_FRONTEND_PROPS);
			}
			stage("Batches"){
				//propertiesMU.initMerge(DEVELOPMENT_FRONTEND_PROPS, STABLE_FRONTEND_PROPS, CANDIDATE_FRONTEND_PROPS, CHANGES_FRONTEND_PROPS);
			}
		}
		catch (Exception err) {
			//deleteDir();
			propertiesMU.isChanged = false;
			println(err);
			sh "svn revert -R .";
			sh "svn up";
		}

	}

	if (propertiesMU.isChanged){
		stage ("Generating Change Log"){
			def file = new File(CHANGES_PATH, "CHANGELOG.md");
			changeLog.createFile(file);
			sh "svn add ${file}";
		}

		stage("Commit changes"){
			sh "svn commit -m \"commit properties\""
			sh "svn up";
		}
	}
	else{
		stage("Revert changes"){
			sh "svn revert -R .";
			sh "svn up";
		}
	}

}