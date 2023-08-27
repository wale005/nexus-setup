pipeline {
	agent any
	tools {
	    maven "MAVEN3"
	    jdk "OracleJDK11"
	}

	stages {
	    stage('Fetch code') {
            steps {
               git branch: 'main', url: 'https://github.com/awsbeginnertutorial/my-project.git'
            }

	    }

	    stage('Build'){
	        steps{
	           sh 'mvn install -DskipTests'
	        }

	        post {
	           success {
	              echo 'Now Archiving it...'
	              archiveArtifacts artifacts: '**/target/*.war'
	           }
	        }
	    }

	    stage('UNIT TEST') {
            steps{
                sh 'mvn test'
            }
       }

       stage('Checkstyle Analysis'){
            steps {
                sh 'mvn checkstyle:checkstyle'
            }
       }

       stage('Sonar Analysis') {
            environment {
                scannerHome = tool 'sonar4.8'
            }
            steps {
               withSonarQubeEnv('mysonar') {
                   sh '''${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=my-project \
                   -Dsonar.projectName=my-project \
                   -Dsonar.projectVersion=1.0 \
                   -Dsonar.sources=src/ \
                   -Dsonar.java.binaries=target/test-classes/com/visualpathit/account/controllerTest/ \
                   -Dsonar.junit.reportsPath=target/surefire-reports/ \
                   -Dsonar.jacoco.reportsPath=target/jacoco.exec \
                   -Dsonar.java.checkstyle.reportPaths=target/checkstyle-result.xml'''
              }
           }
       }

      stage("UploadArtifact"){
            steps{
                nexusArtifactUploader(
                      nexusVersion: 'nexus3',
                      protocol: 'http',
                      nexusUrl: '172.31.89.40:8081',
                      groupId: 'QA',
                      version: "${env.BUILD_ID}-${env.BUILD_TIMESTAMP}",
                      repository: 'my-project-repo',
                      credentialsId: 'nexuslogin',
                      artifacts: [
                        [artifactId: 'my-project',
                         classifier: '',
                         file: 'target/vprofile-v2.war',
                         type: 'war']
                    ]
                )
            }
        }
	}
}