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
}