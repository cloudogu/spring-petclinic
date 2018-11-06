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
                        withZalenium { zaleniumIp ->
                            def petclinicHostIp = new Docker(this).findIp()
                            mvn "failsafe:integration-test failsafe:verify -Pe2e " +
                                    "-Dselenium.remote.url=http://${zaleniumIp}:4444/wd/hub " +
                                    "-Dselenium.petclinic.host=${petclinicHostIp}"
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

void withZalenium(config = [seleniumVersion : '3.14.0-p15',
                            zaleniumVersion : '3.14.0g',
                            zaleniumVideoDir: 'zalenium',
                            debugZalenium   : false], Closure closure) {
    sh "mkdir -p ${config.zaleniumVideoDir}"

    docker.image("elgalu/selenium:${config.seleniumVersion}").pull()
    docker.image("dosel/zalenium:${config.zaleniumVersion}")
            .withRun(
            // Zalenium starts headless browsers in docker containers, so it needs the socket
            '-v /var/run/docker.sock:/var/run/docker.sock ' +
            "-v ${WORKSPACE}/${config.zaleniumVideoDir}:/home/seluser/videos",
            'start ' +
            "${config.debugZalenium ? '--debugEnabled true' : ''}"
            ) { zaleniumContainer ->

        def zaleniumIp = new Docker(this).findIp(zaleniumContainer)

        waitForSeleniumToGetReady(zaleniumIp)
        // Delete videos from previous builds, if any
        // This also works around the bug that zalenium stores files as root
        // https://github.com/zalando/zalenium/issues/760
        // This workaround still leaves a couple of files owned by root in the zaleniumVideoDir
        resetZalenium(zaleniumIp)

        try {
            closure(zaleniumIp)
        } finally {
            // Wait for Selenium sessions to end (i.e. videos to be copied)
            // Leaving the withRun() closure leads to "docker rm -f" being called, cancelling copying
            waitForSeleniumSessionsToEnd(zaleniumIp)
            archiveArtifacts allowEmptyArchive: true, artifacts: "${config.zaleniumVideoDir}/*.mp4"

            // Stop container gracefully and wait
            sh "docker stop ${zaleniumContainer.id}"
            // Store log for debugging purposes
            sh "docker logs ${zaleniumContainer.id} > zalenium-docker.log 2>&1"
        }
    }
}

void waitForSeleniumToGetReady(String host) {
    timeout(time: 1, unit: 'MINUTES') {
        echo "Waiting for selenium to become ready at http://${host}"
        while (!isSeleniumReady(host)) {
            sleep(time: 1, unit: 'SECONDS')
        }
        echo "Selenium ready at http://${host}"
    }
}

boolean isSeleniumReady(String host) {
    sh(returnStdout: true,
            script: "curl -sSL http://${host}:4444/wd/hub/status || true") // Don't fail
            .contains('status\": 0')
}

void waitForSeleniumSessionsToEnd(String host) {
    timeout(time: 1, unit: 'MINUTES') {
        echo "Waiting for selenium sessions to end at http://${host}"
        while (isSeleniumSessionsActive(host)) {
            sleep(time: 1, unit: 'SECONDS')
        }
        echo "No more selenium sessions active at http://${host}"
    }
}

boolean isSeleniumSessionsActive(String host) {
    sh(returnStatus: true,
            script: "(curl -sSL http://${host}:4444/grid/api/sessions || true) | grep sessions") == 0
}

void resetZalenium(String host) {
    sh(returnStatus: true,
            script: "curl -sSL http://${host}:4444/dashboard/cleanup?action=doReset") == 0
}
