# enter a namespace , verify if namespace is there 
# if available skip it 
# if not create it 


#!/bin/bash
namespace_name = "boutique"
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