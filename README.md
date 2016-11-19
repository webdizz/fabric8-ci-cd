# fabric8-ci-cd
Fabric8 CI/CD

# Docker Registry Proxy Cache

Docker Registry configuration file `docker-registry-config.yml`

Start Docker Registry
```
  cp docker-registry-config.yml /tmp/docker-registry/config.yml

  docker run -d --restart=always -p 192.168.50.10:5000:5000 --name v2-mirror \
    -v /tmp/docker-registry:/var/lib/registry registry:2 /var/lib/registry/config.yml
```
  Make sure `--registry-mirror=https://<my-docker-mirror-host>:<port-number>` added to docker daemon configuration file. Here are some additional details https://blog.docker.com/2015/10/registry-proxy-cache-docker-open-source/.

# Run OpenShift using Minishift

Create OpenShift cluster

```
  minishift start --cpus 8 --deploy-registry=true --deploy-router true --disk-size 40g  --openshift-version v1.3.1 --vm-driver virtualbox --memory 8144
```
# Deploy fabric8

```
 gofabric8 deploy -y --domain f8 --pv --version=2.4.3
```
# Default Gogs credentials

gogsadmin/RedHat$1

# Additional images to pull:
- fabric8/maven-builder
- iocanel/jenkins-jnlp-client:latest
