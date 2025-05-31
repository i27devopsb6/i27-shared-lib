import com.i27academy.builds.Calculator
import com.i27academy.k8s.K8s

def call(Map pipelineParams) {
    // instance for the
    K8s k8s = new K8s(this)
    // This pipeline is for Eureke deployment 
    pipeline {
        agent {
            label 'k8s-slave'
        }
        tools {
            maven 'Maven-3.8.8'
            jdk 'JDK-17'
        }
        environment {
            APPLICATION_NAME = "${pipelineParams.appName}"
            POM_VERSION = readMavenPom().getVersion()
            POM_PACKAGING = readMavenPom().getPackaging()
            //APPLICATION_NAME = "eureka"
            SONAR_URL = "http://34.45.105.80:9000"
            SONAR_TOKEN  = credentials('sonar_creds')
            DOCKER_HUB = "docker.io/i27devopsb6"
            DOCKER_CREDS = credentials('dockerhub_creds')

            // Kubernetes DEV cluster details
            DEV_CLUSTER_NAME = "i27-cluster"
            DEV_CLUSTER_ZONE = "us-central1-c"
            DEV_PROJECT_ID = "silver-tempo-455118-a5"

            // Kuberenetes TEST cluster details
            TST_CLUSTER_NAME = "i27-cluster"
            TST_CLUSTER_ZONE = "us-central1-c"
            TST_PROJECT_ID = "silver-tempo-455118-a5"

            //Namespace definition
            DEV_NAMESPACE = "cart-dev-ns" // eureka-dev, user-dev, product-dev, clothing-dev
            TST_NAMESPACE = "cart-tst-ns" // eureka-tst, user-tst, product-tst, clothing-tst
            STG_NAMESPACE = "cart-stg-ns"
            PRD_NAMESPACE = "cart-prd-ns"

            //File name for the deployment
            K8S_DEV_FILE = "k8s_dev.yaml"
            K8S_TST_FILE = "k8s_tst.yaml"
            K8S_STG_FILE = "k8s_stg.yaml"
            K8S_PRD_FILE = "k8s_prd.yaml"

            // Environmental Details
            DEV_ENV = "dev"
            TST_ENV = "test"
            STG_ENV = "stage"
            PRD_ENV = "prod"

            // Chart path details 
            HELM_CHART_PATH = "${workspace}/i27-shared-lib/chart"
            ///home/rama/jenkins/workspace/i27-eureka_master
        }
        parameters {
            choice(name: 'scanOnly',
                choices: 'no\nyes',
                description: 'This will scan the application'
            )
            choice(name: 'buildOnly',
                choices: 'no\nyes',
                description: 'This will only build the application'
            )
            choice(name: 'dockerPush',
                choices: 'no\nyes',
                description: 'This will trigger docker build and docker push'
            )
            choice(name: 'deployToDev',
                choices: 'no\nyes',
                description: 'This will Deploy the application to Dev env '
            )
            choice(name: 'deployToTest',
                choices: 'no\nyes',
                description: 'This will Deploy the application to Test env '
            )
            choice(name: 'deployToStage',
                choices: 'no\nyes',
                description: 'This will Deploy the application to Stage env '
            )
            choice(name: 'deployToProd',
                choices: 'no\nyes',
                description: 'This will Deploy the application to Prod env '
            )
            //string(name: 'CHANGE_TICKET', defaultValue: 'ENTER_CHANGE_TICKET', description: '')
        }
        stages {
            stage('CheckoutSharedLib') {
                steps {
                    script{
                        k8s.gitClone()
                    }
                }
            }
            stage('Build') {
                when {
                    anyOf {
                        expression{
                            params.buildOnly == 'yes'
                            params.dockerPush == 'yes'
                        }
                    }
                }
                steps {
                    script {
                        buildApp().call()
                    }

                }
            }
            stage('Sonar') {
                when {
                    anyOf {
                        expression { params.buildOnly == 'yes' }
                        expression { params.dockerPush == 'yes' }
                        expression { params.scanOnly == 'yes' }
                    }
                }
                steps {
                    withSonarQubeEnv('SonarQube'){
                        sh """
                            echo "Starting Sonar Scan"
                            mvn clean verify sonar:sonar \
                                -Dsonar.projectKey=i27-eureka \
                                -Dsonar.host.url=${env.SONAR_URL} \
                                -Dsonar.login=${env.SONAR_TOKEN}
                        """
                    }
                    timeout(time: 2, unit: "MINUTES") {
                        script {
                            waitForQualityGate  abortPipeline: true
                        }
                    }
                }
            }
            stage ('Docker Build and Push') {
                when {
                    anyOf {
                        expression{
                            params.dockerPush == 'yes'
                        }
                    }
                }
                steps {
                    script {
                        dockerBuildAndPush().call()
                    }
                }
            }
            stage('Deploy to Dev') {
                when {
                    anyOf {
                        expression{
                            params.deployToDev == 'yes'
                        }
                    }
                }
                steps {
                    script {

                        // this variable is used to get the docker image 
                        def docker_image  = "${env.DOCKER_HUB}/${env.APPLICATION_NAME}:$GIT_COMMIT"

                        // This is a login method to connect to GCP
                        k8s.auth_login("${env.DEV_CLUSTER_NAME}", "${env.DEV_CLUSTER_ZONE}", "${env.DEV_PROJECT_ID}")
                        
                        // This is a method to validate the docker image 
                        imageValidation().call()

                        // DEploy using Helm Charts
                        k8s.k8sHelmChartDeploy("${env.APPLICATION_NAME}", "${env.DEV_ENV}", "${HELM_CHART_PATH}", "${env.DEV_NAMESPACE}")                        
                    }
                }
            }
            stage('Deploy to Test') {
                when {
                    anyOf {
                        expression{
                            params.deployToTest == 'yes'
                        }
                    }
                }
                steps {
                    script {
                        imageValidation().call()
                        echo "Deploying to Test env"
                        dockerDeploy('tst','6761').call()
                    }
                }
            }
            stage('Deploy to Stage') {
                when {
                    allOf {
                        anyOf {
                            expression {
                                params.deployToStage == 'yes'
                            }
                        }
                        anyOf {
                                branch 'release/*'
                                tag pattern: "v\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}", comparator: "REGEXP"
                        }
                    }

                }
                steps {
                    script {
                        imageValidation().call()
                        echo "Deploying to stg env"
                        dockerDeploy('stg','7761').call()
                    }
                }
            }
            stage('Deploy to prod') {
                when {
                    anyOf {
                        expression{
                            params.deployToProd == 'yes'
                        }
                    }
                anyOf {
                            tag pattern: "v\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}", comparator: "REGEXP"
                    }
                }
                steps {
                    timeout(time: 300, unit: 'SECONDS') { // 300 seconds
                        input message: "Deploying ${env.APPLICATION_NAME} to production ?", ok: 'yes', submitter: 'ramsre,i27academy'
                    }
                    script {
                        echo "Deploying to prod env"
                        dockerDeploy('prd','8761').call()
                    }
                }
            }
            stage ('Cleanup') {
                steps {
                    script {
                        echo "Cleaning up the workspace"
                        cleanWs()
                    }
                }
            }   
        }
    }


}
    def buildApp() {
        return{
            echo "Building ${env.APPLICATION_NAME} Application"
            sh "mvn clean package -DskipTests=true" 
        }
    }

    def imageValidation() {
        return {
            println("******************** Attempt to pull the docker image *********************")
            try {
                sh "docker pull ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:$GIT_COMMIT"
                println("********************** Image is pulled Succesfully *****************************")
            }
            catch(Exception e) {
                println("***************** OOPS , the docker image is not available....... So creating the image")
                buildApp().call()
                dockerBuildAndPush().call()
            }
        }
    }


    def dockerBuildAndPush() {
        return {
            echo "**************************************** Building Docker Image ****************************************"
            sh "cp ${workspace}/target/i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} ./.cicd"
            sh "docker build --no-cache --build-arg JAR_SOURCE=i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} -t ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:$GIT_COMMIT ./.cicd"
            echo "**************************************** Docker Login ****************************************"
            sh "docker login -u ${DOCKER_CREDS_USR} -p ${DOCKER_CREDS_PSW}"
            echo "**************************************** Push Docker Image ****************************************"
            sh "docker push ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:$GIT_COMMIT"
        }
    }

    def dockerDeploy(envDeploy, port) {
        return {
            echo "Deploying to $envDeploy env"
            withCredentials([usernamePassword(credentialsId: 'john_docker_cm_creds', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
                script {
                    try {
                        // stop the container
                        sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$docker_vm_ip \"docker stop ${APPLICATION_NAME}-$envDeploy\""
                        // remove the container
                        sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$docker_vm_ip \"docker rm ${APPLICATION_NAME}-$envDeploy\""
                    }
                    catch(err) {
                        echo "Error caught: $err"
                    }
                    sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$docker_vm_ip \"docker run --restart always --name ${APPLICATION_NAME}-$envDeploy -p $port:8761 -d ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:$GIT_COMMIT\""
                }
            }

        }
    }