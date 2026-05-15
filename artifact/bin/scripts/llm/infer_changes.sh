#!/bin/bash

if [ "$#" -ne 3 ]; then
    echo "Usage: infer_changes.sh <input_dir> <output_dir> <model>"
    exit 1
fi


input_dir=$1
output_dir=$2
model=$3

mkdir -p $output_dir 2>/dev/null

for pkg_path in $input_dir/*; do
    pkg_name=$(basename $pkg_path)
    if [ -d $output_dir/$pkg_name ]; then
        if ls $output_dir/$pkg_name/ | grep -q "_summary.txt"; then
            echo "Skipping $pkg_name as output already exists"
            continue
        fi
    fi
    echo "Processing $pkg_name"

#    if [ $(cat $pkg_path/modified.prompts.token_count | head -n 1) -ge 10000 ]; then
#        echo  "Skipping $pkg_name as token count is greater than 10000"
#        continue
#    fi
    if [ ! -f $pkg_path/official_prompt.txt ]; then
        echo "Skipping $pkg_name as no official prompt found"
        continue
    fi


    python3 ../../llm/src/llm_chat.py --input-dir $pkg_path --output-dir  $output_dir/$pkg_name  --model $model
    sleep 3
done