#!groovy
@Library('github.com/cloudogu/ces-build-lib@e4dc741')
import com.cloudogu.ces.cesbuildlib.*

properties([
        // Don't run concurrent builds, because the ITs use the same port causing random failures on concurrent builds.
        disableConcurrentBuilds()
])

node {

    String cesFqdn = findHostName()
    String cesUrl = "https://${cesFqdn}"
    String credentialsId = 'scmCredentials'

    Maven mvn = new MavenWrapper(this)
    // Workaround SUREFIRE-1588 on Debian/Ubuntu. Should be fixed in Surefire 3.0.0
    mvn.additionalArgs = '-DargLine="-Djdk.net.URLClassPath.disableClassPathURLCheck=true"'

    catchError {

        stage('Checkout') {
            checkout scm
        }

        stage('Build') {
            mvn 'clean package -DskipTests'

            archiveArtifacts artifacts: '**/target/*.jar'
        }

        String jacoco = "org.jacoco:jacoco-maven-plugin:0.8.1"
        parallel(
                test: {
                    stage('Test') {
                        mvn "${jacoco}:prepare-agent test ${jacoco}:report"
                    }
                },
                integrationTest: {
                    stage('Integration Test') {
                        mvn "${jacoco}:prepare-agent-integration failsafe:integration-test failsafe:verify ${jacoco}:report-integration"
                    }
                },
                uiTest: {
                    stage('UI Test') {
                        String seleniumVersion = '3.14.0-p15'
                        String zaleniumVersion = '3.14.0f'
                        String zaleniumVideoDir = 'zalenium'

                        sh "rm -rf $WORKSPACE/$zaleniumVideoDir && mkdir $WORKSPACE/$zaleniumVideoDir"

                        docker.image("elgalu/selenium:$seleniumVersion").pull()
                        docker.image("dosel/zalenium:$zaleniumVersion")
                                .withRun('-v /var/run/docker.sock:/var/run/docker.sock ' +
                                    "-v $WORKSPACE/$zaleniumVideoDir:/home/seluser/videos",
                                    'start') { zaleniumContainer ->

                            def docker = new Docker(this)
                            def zaleniumIp = docker.findIp(zaleniumContainer)
                            def petclinicHostIp = docker.findDockerHostIp()

                            try {
                                mvn "failsafe:integration-test failsafe:verify -Pe2e " +
                                        "-Dselenium.remote.url=http://${zaleniumIp}:4444/wd/hub " +
                                        "-Dselenium.petclinic.host=${petclinicHostIp}"
                            } finally {
                                archiveArtifacts allowEmptyArchive: true, artifacts: "$WORKSPACE/$zaleniumVideoDir/*.mp4"
                            }
                        }
                    }
                }
        )

        stage('Static Code Analysis') {

            def sonarQube = new SonarQube(this, [usernamePassword: credentialsId, sonarHostUrl: "${cesUrl}/sonar"])

            sonarQube.analyzeWith(mvn)
        }

        stage('Deploy') {
            mvn.useDeploymentRepository([id: cesFqdn, url:  "${cesUrl}/nexus", credentialsId: credentialsId, type: 'Nexus3'])

            mvn.deployToNexusRepository('-Dmaven.javadoc.failOnError=false')
        }
    }

    // Archive Unit and integration test results, if any
    junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/TEST-*.xml,**/target/surefire-reports/TEST-*.xml'
}
