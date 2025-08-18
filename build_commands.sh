#!/usr/bin/bash

mvn install -Dmaven.test.skip=true

cd ~/repos/cloudgene3/target/cloudgene-2.8.7-assembly
chmod 750 cloudgene
rm -rf /local/cloudgene3/cloudgene_exec/webapp ; cp -r webapp /local/cloudgene3/
rm -rf /local/cloudgene3/cloudgene_exec/sample ; cp -r sample /local/cloudgene3/
rm -rf /local/cloudgene3/cloudgene_exec/lib ; cp -r lib /local/cloudgene3/
rm /local/cloudgene3/cloudgene_exec/cloudgene ; cp cloudgene /local/cloudgene3/
rm /local/cloudgene3/cloudgene_exec/cloudgene.jar ; cp cloudgene.jar /local/cloudgene3/
