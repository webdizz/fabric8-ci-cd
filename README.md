# fabric8-ci-cd
Fabric8 CI/CD


# Step-by-step instruction

```bash
  vagrant up # run Vagrant based box with OpenShift Master & Node

  export KUBERNETES_DOMAIN=vagrant.f8
  export DOCKER_HOST=tcp://vagrant.f8:2375

  oc login https://172.28.128.4:8443 # to login into OpenShift using OpenShift CLI

  oc project default # to switch into Demo project

  oc create -f fabric8-support/*.svc.acnt.yml # to create missing service accounts

  oc create -f fabric8-support/build-config.json # to create build config

  oc create -n user-secrets-source-admin -f fabric8-support/webdizz-github.secret.yml

  # run build

  # create fabric8 service account in more new projects
  oc create -n default-staging -f fabric8-support/fabric8.svc.acnt.yml
  oc create -n default-production -f fabric8-support/fabric8.svc.acnt.yml
```
