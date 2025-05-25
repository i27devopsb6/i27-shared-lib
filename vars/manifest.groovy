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
        environment {
            // Kubernetes DEV cluster details
            DEV_CLUSTER_NAME = "i27-cluster"
            DEV_CLUSTER_ZONE = "us-central1-c"
            DEV_PROJECT_ID = "silver-tempo-455118-a5"

            // Kuberenetes TEST cluster details
            TST_CLUSTER_NAME = "i27-cluster"
            TST_CLUSTER_ZONE = "us-central1-c"
            TST_PROJECT_ID = "silver-tempo-455118-a5"
        }
        parameters {
            string (
                name: 'NAMESPACE_NAME',
                description: "Enter the name of the namespace, you want to create"
            )
        }
        stages {
            stage('Authentication') {
                steps {
                    script {
                        k8s.auth_login("${env.DEV_CLUSTER_NAME}", "${env.DEV_CLUSTER_ZONE}", "${env.DEV_PROJECT_ID}")
                    }
                }
            }
            stage ('Creaet k8s Namespace') {
                steps {
                    script {
                        k8s.namespace_creation("${params.NAMESPACE_NAME}")
                    }
                }
            }
        }
    }


}
