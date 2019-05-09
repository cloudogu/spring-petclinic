# Petclinic Demo im Cloudogu EcoSystem

Willkommen im Smeagol-Teil der Petclinic-Demo! Dieses Projekt basiert auf der berühmten Spring petclinic Beispielanwendung. Darüber hinaus haben wir aber noch mehr auf Lager: Diese Demo zeigt das Zusammenspiel innerhalb einer Cloudogu EcoSystem-Entwicklungspipeline im Kontext von Continuous Integration und Deployment (CI/CD) in ein Artefakt-Repository.

## Interaktion

Bevor Sie loslegen, sollten Sie betrachten, welche Dinge bereits für Sie in diesem Projekt konfiguriert wurden:

- SCM-Manager enthält dieses Git-Repository:
    - Demo-Code der spring-petclinic
    - die Smeagol-Wiki-Seiten, die Sie gerade lesen
    - Jenkins-Anbindung
    - Redmine-Anbindung
- Jenkins enthält
    - ein Pipeline-Projekt
    - Konfiguration des SonarQube-Tools
    - Konfiguration des Nexus-Tools
- Redmine enthält ein Projekt mit einem Entwicklungsticket
- Smeagol enthält diese Dokumentation

Nun kommt der Teil, um die Einfachheit zu demonstrieren, wie alle Teile in der Build-Pipeline zusammenarbeiten. Wir haben unser Handwerkszeug parat, und bei einer Code-Änderung wird Folgendes passieren:

1. Übernehmen Sie eine Änderung und pushen Sie diese in das Git-Repository
    - Z. B.: Bearbeiten Sie diese Wiki-Seite, indem Sie auf den Bearbeiten-Button ![Bearbeiten Button](images/SmeagolEditButton.png) klicken und speichern Sie sie
    - Z. B.: Ändern Sie eine Datei (etwa `readme-petclinic.md`) und pushen Sie die Änderung zurück in das Git-Repository
        - `git add readme-petclinic.md`
        - `git commit -m "#1 Behebt Schnabelbruch"`
        - `git push`
1. **SCM-Manager**
   - beherbergt dieses Repository inkl. Ihres eben getätigten Commits
1. **Redmine** vermerkt Ihren Commit in der User Story
1. **Jenkins** bemerkt die Änderung und startet eine vordefinierte CI/CD-Pipeline:
   - ein Build wird ausgelöst
   - Unit- und Integrationstests werden durchgeführt
1. **SonarQube** analysiert Ihr Projekt
1. Jenkins deployt ein JAR-Artefakt wird in ein `snapshot`-Repository im **Nexus**

![Übersicht über das Zusammenspiel der CI/CD-Pipeline in diesem Beispielprojekt](images/Example-CI-CD-Project.jpg)

Übrigens:

Smeagol unterstützt auch eingebettetes PlantUML mit unterschiedlichsten Digrammtypen:

@startuml

participant Smeagol
participant Kommandozeile
database SCMManager as "SCM-Manager"
participant Redmine
participant Jenkins
participant SonarQube
database Nexus 

Smeagol -> SCMManager : Wiki ändern
Kommandozeile -> SCMManager : Quellcode ändern
SCMManager -> Redmine : Ticket aktualisieren
SCMManager -> Jenkins : Build anstoßen
Jenkins -> Jenkins : Buildprozess starten
Jenkins -> Jenkins : Tests ausführen
Jenkins -> SonarQube : Quellcode analysieren
Jenkins -> Nexus : JAR ablegen

@enduml
