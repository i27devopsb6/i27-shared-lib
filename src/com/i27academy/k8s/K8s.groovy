package com.i27academy.k8s;
class K8s {
    def jenkins
        // Constructor tom intiliase the Calculator class with the jenkins instance 
    K8s(jenkins) {
        this.jenkins = jenkins
    }


    // write all the methods here 
    def auth_login(clusterName, zone, projectID ){
        jenkins.sh """
            echo "****************** Entering into kubernetes Authentication/login method ******************"
            gcloud compute instances list
            echo "****************** Create the config file for the deployments *******************"
            gcloud container clusters get-credentials $clusterName --zone $zone --project $projectID
            kubectl get nodes
        """
    }

    // Method to deploy applications 
    def k8sdeploy(fileName, docker_image , namespace){
        jenkins.sh"""
            echo "Deploying into k8s cluster"
            sed -i "s|DIT|${docker_image}|g" ./.cicd/${fileName}
            kubectl apply -f ./.cicd/${fileName} -n ${namespace} 
        """
    }

    // Clone the shared library
    def gitClone(){
        jenkins.sh """
            echo "****************** Entering into git clone method ******************"
            echo "****************** Cloning the shared library ******************"
            git clone -b main https://github.com/i27devopsb6/i27-shared-lib.git
            echo "****************** Cloning the shared library completed ******************"
            echo "****************** Listing the files in the current directory ******************"
            ls -la
            echo "****************** Listing the files in the shared Library ******************"
            ls -la i27-shared-lib
        """
    }

    // Helm Deployments
    def k8sHelmChartDeploy(appName, env, helmChartPath, namespace){
        jenkins.sh """
            echo "****************** Entering into Helm Chart Deployment method ******************"
            echo "******************* Deploying the Helm Chart ******************"
            # Verify if helm chart exists
            if helm list -n $namespace | grep -q ${appName}-${env}-chart ; then
                echo "This Chart Exists"
                echo "****************** Upgrading the Helm Chart ******************"
                helm upgrade **********************
            else 
                echo "This Chart does not exist, Creating a new one"
                echo "****************** Installing the Helm Chart ******************"
                helm install ${appName}-${env}-chart -f .cicd/helm_values/values_${env}.yaml ${helmChartPath} -n ${namespace}
            fi
        """
    }

    // Namespace Creation 
    def namespace_creation(namespace_name){
        jenkins.sh """#!/bin/bash
            echo "Namespace provided is $namespace_name"
            # validate if namespace exists 
            if kubectl get namespace "$namespace_name" >/dev/null ; then
                echo "Namespace '$namespace_name' already exists."
                exit 0 
            else
                echo "Namespace '$namespace_name' does not exist. Creating it now..."
                if kubectl create ns '$namespace_name' &> /dev/null; then 
                    echo "Namespace '$namespace_name' has created successfully."
                    exit 0
                else
                    echo "Some error, Failed to create namespace '$namespace_name'."
                    exit 1
                fi
            fi
        """
    }
}