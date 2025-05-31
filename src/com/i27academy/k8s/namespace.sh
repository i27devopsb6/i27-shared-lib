# enter a namespace , verify if namespace is there 
# if available skip it 
# if not create it 


#!/bin/bash
namespace_name = "boutique"
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