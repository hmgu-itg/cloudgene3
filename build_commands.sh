#!/usr/bin/bash

# /mnt/storage/cloudgene3
dest_dir=$1

if [[ -z "$dest_dir" ]];then
    echo "INFO: no destination dir specified; using /mnt/storage/cloudgene3"
    dest_dir="/mnt/storage/cloudgene3"
fi

mvn -Dmaven.test.skip=true package

cd ~/repos/cloudgene3/target/cloudgene-3.1.3
chmod 750 cloudgene
rm -rf "${dest_dir}"/webapp ; cp -r webapp "${dest_dir}"
# rm -rf /local/cloudgene3/cloudgene_exec/sample ; cp -r sample /local/cloudgene3/
# rm -rf /local/cloudgene3/cloudgene_exec/lib ; cp -r lib /local/cloudgene3/
rm -f "${dest_dir}"/cloudgene ; cp cloudgene "${dest_dir}"
rm -f "${dest_dir}"/cloudgene.jar ; cp cloudgene.jar "${dest_dir}"
