node() {
	def mvnHome = tool 'MVN3';
	def urlSVN = "http://svn-mx-pfc.rb.echonet/svn/cckfr";
	def urlArtifact = "${urlSVN}/${artifact}/${urlOrigin}";
	def newUrlBranch = "${urlSVN}/${artifact}/branches/${artifact}-${branchName}";

	stage("Preparation"){
		deleteDir();
		print "Check the url of artifact ${urlArtifact}";
		sh("svn ls ${urlArtifact}");
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
		print "${message}";
		def out = sh("svn import -m '${message}' ./${artifact} ${newUrlBranch}");
		print "${out}";	
	}

	stage ('Build'){
		def result = build job: "cckfr-workspace/cckfr-build-branch", propagate: false, parameters: [[$class: "ListSubversionTagsParameterValue", name: "artifact", tag: "${artifact}", tagsDir: "http://10.164.84.173/svn/cckfr"], [$class: "GeneratorKeyValueParameterValue", name: "branch_name", value: "${artifact}-${branchName}"]]
		print "${result}";	
	}

}