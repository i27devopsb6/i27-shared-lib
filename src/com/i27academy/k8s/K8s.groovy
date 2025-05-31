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