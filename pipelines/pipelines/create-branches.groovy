node() {
	def mvnHome = tool 'MVN3';
	def urlArtifact = "${repository}/${artifact}/master";
	def newUrlBranch = "${repository}/${artifact}/branches/${artifact}-${newBranch}";

	stage('Preparation'){
		deleteDir();
		if (branch != "master"){
			urlArtifact = "${repository}/${artifact}/${branch}/${branchName}";
		}

		print "Check the url of artifact ${urlArtifact}";
		def out = sh("git show HEAD:${urlArtifact}");
		print "${out}";
	}



}