node() {
	def mvnHome = tool 'MVN3';
	def urlArtifact = "${repository}/${artifact}/trunk";
	def newUrlBranch = "${repository}/${artifact}/branches/${artifact}-${newBranch}";

	stage("Preparation"){
		deleteDir();
		if (branch != "trunk"){
			urlArtifact = "${repository}/${artifact}/${branch}/${branchName}";
		}

		print "Check the url of artifact ${urlArtifact}";
		def out = sh("svn ls ${urlArtifact}");
		print "${out}";
	}

	stage('Checkout code') {
		print "Export from ${urlArtifact}";
		sh("svn export ${urlArtifact} ${artifact}");	
	}

	stage ('Set new version'){
		print "Set ${newVersion}";
		def out = sh("${mvnHome}/bin/mvn -f ./${artifact}/pom.xml versions:set -DnewVersion=${newVersion}");
		print "${out}";
	}

	stage ('Commit version'){
		out = sh("${mvnHome}/bin/mvn -f ./${artifact}/pom.xml versions:commit");
		print "${out}";
	}

	stage ('Import Branch'){
		sh("rm -Rf ./target");
		def out = sh("svn import -m 'import branch' ./${artifact} ${newUrlBranch}");
		print "${out}";	
	}

	stage ('Build'){
		def result = build job: "${buildBranch}", propagate: false, parameters: [[$class: "ListSubversionTagsParameterValue", name: "artifact", tag: "${artifact}", tagsDir: "${repository}"], [$class: "GeneratorKeyValueParameterValue", name: "branch_name", value: "${artifact}-${newBranch}"]]
		print "${result}";	
	}

}