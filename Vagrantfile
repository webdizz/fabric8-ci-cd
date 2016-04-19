# coding: utf-8
# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.require_version ">= 1.8.0"

# for running the CD Pipeline we recommend at least 8000 for memory!
$vmMemory = ENV['FABRIC8_VM_MEMORY'] || 8096
$vmMinionMemory = ENV['FABRIC8_VM_MINION_MEMORY'] || 512
$numMinions = (ENV['NUM_MINIONS'] || 1).to_i

$provisionScript = <<SCRIPT
# Check memory
VM_MEMORY="#{$vmMemory}"
echo "=========================================================================="
echo "Using VM Memory of ${VM_MEMORY} MB"
if [ ${VM_MEMORY} -lt 8192 ]; then
  echo "NOTE: We recommend at least 8192 MB for running the 'cd-pipeline'."
  echo "      You can specify this with an environment variable FABRIC8_VM_MEMORY"
  echo "      E.g. when creating the VM with : 'FABRIC8_VM_MEMORY=8192 vagrant up'"
fi
echo "=========================================================================="

yum -y update
yum -y install epel-release
yum -y install htop

echo "=========================================================================="
echo "Installation of helm"
# install our fork for helm with support for OpenShift Templates and Secrets
curl -s https://get.helm.sh | bash
mv helm /bin/helm

# lets setup helm with our repo
helm update
helm repo add fabric8 https://github.com/fabric8io/charts.git
echo "=========================================================================="

echo "=========================================================================="
echo "Installation of gofabric8"

# download gofabric8
mkdir /tmp/gofabric8
curl --retry 999 --retry-max-time 0  -sSL https://github.com/fabric8io/gofabric8/releases/download/v0.4.9/gofabric8-0.4.9-linux-amd64.tar.gz | tar xzv -C /tmp/gofabric8
chmod +x /tmp/gofabric8/gofabric8
mv /tmp/gofabric8/* /usr/bin/
echo "=========================================================================="

echo "=========================================================================="
echo "Installation of openshift"

# setup openshift
if [ -d '/var/lib/openshift' ]; then
  exit 0
fi

mkdir /tmp/openshift
echo "Downloading OpenShift binaries..."
curl --retry 999 --retry-max-time 0  -sSL https://github.com/openshift/origin/releases/download/v1.1.6/openshift-origin-server-v1.1.6-ef1caba-linux-64bit.tar.gz | tar xzv -C /tmp/openshift
rm -rf /tmp/openshift/openshift-origin-*/LICENSE
rm -rf /tmp/openshift/openshift-origin-*/README.md
mv /tmp/openshift/openshift-origin-*/* /usr/bin/

mkdir -p /opt/openshift/openshift.local.manifests /var/lib/openshift

pushd /var/lib/openshift

/usr/bin/openshift start --master=172.28.128.4 --cors-allowed-origins=.* --hostname=172.28.128.4 --write-config=/opt/openshift/openshift.local.config
cat <<EOF >> /opt/openshift/openshift.local.config/node-172.28.128.4/node-config.yaml
kubeletArguments:
  "read-only-port":
    - "10255"
EOF
sed -i 's|^podManifestConfig: null|podManifestConfig:\\n  path: /opt/openshift/openshift.local.manifests\\n  fileCheckIntervalSeconds: 10|' /opt/openshift/openshift.local.config/node-172.28.128.4/node-config.yaml
popd
restorecon -Rv /var/lib/openshift

cat <<EOF > /usr/lib/systemd/system/openshift.service
[Unit]
Description=OpenShift
Requires=docker.service network.service
After=network.service
[Service]
ExecStart=/usr/bin/openshift start --master-config=/opt/openshift/openshift.local.config/master/master-config.yaml --node-config=/opt/openshift/openshift.local.config/node-172.28.128.4/node-config.yaml
WorkingDirectory=/opt/openshift/
[Install]
WantedBy=multi-user.target
EOF

# lets configure the routing subdomain: https://docs.openshift.com/enterprise/3.0/install_config/install/deploy_router.html#customizing-the-default-routing-subdomain
sed -i "s|subdomain: router.default.svc.cluster.local|subdomain: vagrant.f8|" /opt/openshift/openshift.local.config/master/master-config.yaml

systemctl daemon-reload
systemctl enable openshift.service
systemctl start openshift.service

mkdir -p ~/.kube/
ln -s /opt/openshift/openshift.local.config/master/admin.kubeconfig ~/.kube/config

while true; do
  curl -k -s -f -o /dev/null --connect-timeout 1 https://localhost:8443/healthz/ready && break || sleep 1
done

oadm policy add-cluster-role-to-user cluster-admin admin

gofabric8 deploy -y
gofabric8 secrets -y
gofabric8 volume -y --host-path="/vagrant/fabric8-data"

cat <<EOT


The OpenShift console is at: https://172.28.128.4:8443/console

Now we need to wait for the 'fabric8' pod to startup.
This will take a few minutes as it downloads some docker images.

Please be patient!
--------------------------------------------------------------

Now might be a good time to setup your host machine to work with OpenShift

* Download a recent release of the binaries and add them to your PATH:

   https://github.com/openshift/origin/releases/

* Set the following environment variables (example is in Unix style, use 'set' for Windows):

   export KUBERNETES_DOMAIN=vagrant.f8
   export DOCKER_HOST=tcp://vagrant.f8:2375

Now login to OpenShift via this command:

   oc login https://172.28.128.4:8443

Then enter admin/admin for user/password.

Over time your token may expire and you will need to reauthenticate via:

   oc login

Now to see the status of the system:

   oc get pods

or you can watch from the command line via one of these commands:

   watch oc get pods
   oc get pods --watch

--------------------------------------------------------------
Now waiting for the fabric8 pod to download and start Running....
--------------------------------------------------------------

Downloading docker images...

EOT

until oc get pods -l project=console,provider=fabric8  | grep -m 1 "Running"; do sleep 1 ; done

echo "Fabric8 console is now running. Waiting for the Openshift Router to start..."

# create the router
oadm router --create --service-account=router --expose-metrics

until oc get pods -l deploymentconfig=router,router=router  | grep -m 1 "Running"; do sleep 1 ; done

oc annotate service router prometheus.io/port=9101
oc annotate service router prometheus.io/scheme=http
oc annotate service router prometheus.io/path=/metrics
oc annotate service router prometheus.io/scrape=true

cat <<EOT

--------------------------------------------------------------
Fabric8 pod is running! Who-hoo!
--------------------------------------------------------------

Now open the fabric8 console at:

    http://fabric8.vagrant.f8/

When you first open your browser Chrome will say:

   Your connection is not private

Don't panic! There is more help on using the console here and dealing with the self signed certificates:

    http://fabric8.io/guide/getStarted/browserCertificates.html

* Click on the small 'Advanced' link on the bottom left
* Now click on the link that says 'Proceed to fabric8.vagrant.f8 (unsafe)' bottom left
* Now the browser should redirect to the login page. Enter admin/admin
* You should now be in the main fabric8 console. That was easy eh! :)
* Make sure you start off in the 'default' namespace.

To install more applications click the Run... button on the Apps tab.


--------------------------------------------------------------
Downloading docker images
--------------------------------------------------------------

We have found it common in docker in vagrant for it to have trouble concurrently downloading docker images.

You can pre-load docker images in your VM via:

    vagrant ssh
    sudo bash
    gofabric8 pull cd-pipeline

Where the last parameter is the name of the template (app) you wish to be able to run. You'll then have all the docker images downloaded so things will startup really fast.


We love feedback: http://fabric8.io/community/
Have fun!

--------------------------------------------------------------
Now open the fabric8 console at:

    http://fabric8.vagrant.f8/

Help on using the console:

    http://fabric8.io/guide/getStarted/browserCertificates.html
--------------------------------------------------------------

EOT

# now lets create the registry
oadm registry --create --service-account=admin

gofabric8 pull cd-pipeline
gofabric8 pull kubeflix
gofabric8 pull management
gofabric8 pull letschat
gofabric8 pull chat-letschat
gofabric8 pull chaos-monkey
docker pull fabric8/maven-builder
docker pull fabric8/jenkins-jnlp-client
docker pull fabric8/java-jboss-openjdk8-jdk:1.0.10

# And install the node-local fluentd pod
cat <<'EOF' > /opt/openshift/openshift.local.manifests/fluentd.yaml
apiVersion: v1
kind: Pod
metadata:
  name: fluentd-elasticsearch
  labels:
    provider: fabric8
    component: logging
spec:
  containers:
  - name: fluentd-elasticsearch
    image: fabric8/fluentd-kubernetes:v1.9
    securityContext:
      privileged: true
    resources:
      limits:
        cpu: 100m
    volumeMounts:
    - name: varlog
      mountPath: /var/log
    - name: varlibdockercontainers
      mountPath: /var/lib/docker/containers
      readOnly: true
    env:
    - name: ELASTICSEARCH_HOST
      value: elasticsearch
    - name: ELASTICSEARCH_PORT
      value: "9200"
  volumes:
  - name: varlog
    hostPath:
      path: /var/log
  - name: varlibdockercontainers
    hostPath:
      path: /var/lib/docker/containers
EOF

SCRIPT

$windows = (/cygwin|mswin|mingw|bccwin|wince|emx/ =~ RUBY_PLATFORM) != nil

$pluginToCheck = $windows ? "vagrant-hostmanager" : "landrush"
unless Vagrant.has_plugin?($pluginToCheck)
  raise 'Please type this command then try again: vagrant plugin install ' + $pluginToCheck
end

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  # Top level domain
  $tld = "vagrant.f8"

  # Landrush is used together with wildcard dns entries to map all
  # routes to the proper services
  if $windows
    config.hostmanager.enabled = true
    config.hostmanager.manage_host = true
    config.hostmanager.ignore_private_ip = false
    config.hostmanager.include_offline = true

    config.hostmanager.aliases = %w(fabric8.vagrant.f8 jenkins.vagrant.f8 gogs.vagrant.f8 nexus.vagrant.f8 hubot-web-hook.vagrant.f8 letschat.vagrant.f8 kibana.vagrant.f8 taiga.vagrant.f8 fabric8-forge.vagrant.f8)
  else
    config.landrush.enabled = true
    config.landrush.tld = $tld
    config.landrush.guest_redirect_dns = false
  end

  config.vm.box = "jimmidyson/centos-7.1"
  config.vm.box_version = "= 1.3.1"

  config.vm.provider "virtualbox" do |v|
    v.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
    v.memory = $vmMemory
    v.cpus = 4
  end

  config.vm.define 'master' do |master|
    master_ip = '172.28.128.4'
    master.vm.network 'private_network', ip: master_ip
    unless $windows
      master.landrush.host_ip_address = master_ip
    end
    master.vm.provision "shell", inline: $provisionScript, keep_color: true
  end

end
