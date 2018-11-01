#!groovy
@Library('github.com/cloudogu/ces-build-lib@24c4f03')
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

                        def docker = new Docker(this)

                        String uid = sh (returnStdout: true, script: 'echo "$(id -u):$(id -g)"').trim()

                        docker.image("elgalu/selenium:$seleniumVersion").pull()
                        docker.image("dosel/zalenium:$zaleniumVersion")
                                .mountJenkinsUser()
                        //.withRun("-u $uid -v /var/run/docker.sock:/var/run/docker.sock -v $WORKSPACE/$zaleniumVideoDir:/home/seluser/videos", 'start') {
                                .withRun("-e HOST_UID=1002 -e HOST_GID=1003 -v /var/run/docker.sock:/var/run/docker.sock -v $WORKSPACE/$zaleniumVideoDir:/home/seluser/videos", 'start') {
                            zaleniumContainer ->

                                def zaleniumIp = docker.findIp(zaleniumContainer)

                                try {
                                    // Run petclinic inside a container, so the zalenium container can reach the maven container
                                    docker.image('openjdk:8u181')
                                            .withRun("-v $WORKSPACE/target:/target", 'java -jar /target/spring-petclinic.jar') {
                                        petClinicContainer ->
                                            def petClinicContainerIp = docker.findIp(petClinicContainer)

                                            new MavenInDocker(this, '3.5.4-jdk-8').mvn "failsafe:integration-test failsafe:verify -Pe2e " +
                                                    "-Dselenium.remote.url=http://$zaleniumIp:4444/wd/hub " +
                                                    "-Dselenium.petclinic.url=http://$petClinicContainerIp:8080/"
                                    }
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
