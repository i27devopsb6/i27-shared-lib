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


    // Namespace Creation 
    def namespace_creation(namespace_name){
        jenkins.sh """#!/bin/bash
            echo "Namespace provided is $namespace_name"
            # validate if namespace exists 
            if kubectl get namespace "$namespace_name" >/dev/null 2>&1; then
                echo "Namespace '$namespace_name' already exists."
            else
                echo "Namespace '$namespace_name' does not exist. Creating it now..."
                kubectl create namespace "$namespace_name"
                if [ $? -eq 0 ]; then
                    echo "Namespace '$namespace_name' created successfully."
                else
                    echo "Failed to create namespace '$namespace_name'."
                    exit 1
                fi
            fi
        """
    }
}