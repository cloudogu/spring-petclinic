# Spring PetClinic Sample Application (Cloudogu Ecosystem)

This is the well-known spring sample application petclinic that has been extended to be a showcase for the Clodugou 
EcoSystem. See [demo.cloudogu.net](https://cloudogu.com/en/#demo).

We extended it by  

* a [Smeagol Wiki](https://github.com/cloudogu/smeagol) (see [Home.md](docs/Home.md))
* an [integration test](src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerITCase.java) (run with failsafe plugin, see [pom.xml](pom.xml))
* an [end to end test](src/test/java/org/springframework/samples/petclinic/e2e/FindOwnersITCase.java) using selenium
* a [Jenkinsfile](Jenkinsfile) for building, testing and SonarQube analysis.

For more details on petclinic, see also [original spring petclinci readme.md](readme-petclinic.md).


## end 2 end tests

The [existing e2e](src/test/java/org/springframework/samples/petclinic/e2e/FindOwnersITCase.java) is just a proof of 
concept. It does ignore a lot of best practices, that should be taken into account in real projects 

* It does not implement a page object
* The selenium config (system properties) are included in the test, no separation of concerns.
* It hard codes the `RemoteWebDriver`. For efficient local development you shoud provide multiple options such as local
  (maybe headless) firefox or chrome, etc.
* ...

If you want to start them locally use a selenium grid such as [zalenium](https://github.com/zalando/zalenium)  
```bash
docker pull elgalu/selenium:3.14.0-p15
docker run --rm -ti --name zalenium -p 4444:4444 \
  -v /var/run/docker.sock:/var/run/docker.sock  -v /tmp/videos:/home/seluser/videos \
 --privileged --network="host"  \
 dosel/zalenium:3.14.0f start
```
* `--network="host"` is necessary so the grid can find the pet clinic that is started locally
* See also [zalando/zalenium](https://github.com/zalando/zalenium)

Then run: `mvn failsafe:integration-test failsafe:verify -Pe2e`
