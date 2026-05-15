#!/bin/bash

# Generate prompts in batch

set -euo pipefail

if [ $# -ne 2 ]; then
    echo "Usage: $0 <input_dir> <output_dir>"
    exit 1
fi

input_dir=$1
output_dir=$2


for package_path in "$input_dir"/*; do
    package_name=$(basename $package_path)
    added_removed_diff_file=$package_path/"${package_name}"_added_removed_methods.json
    modified_diff_file=$package_path/"${package_name}"_modified_callpaths.json
    # Skip if empty directory
    if [ ! -f $added_removed_diff_file ] && [ ! -f $modified_diff_file ]; then
        echo "Skipping $package_name as diff files not found"
        continue
    fi

    echo "Processing $package_name"
    if ! jq -e '(.addedMethods == [] and .removedMethods == [])' $added_removed_diff_file > /dev/null; then
         mkdir -p $output_dir/$package_name 2>/dev/null
         python3 ../../analyzer/parse_add_del_cp_diff.py --input-file $added_removed_diff_file  > $output_dir/$package_name/added.removed.prompts.json
    fi

    #  "modifiedActivities" : { }

    if ! jq -e '(.modifiedActivities == {})' $modified_diff_file > /dev/null; then
          mkdir -p $output_dir/$package_name 2>/dev/null
          python3 ../../analyzer/parse_modified_cp_diff.py --input-file $modified_diff_file > /tmp/${package_name}_modified.prompts.json
          python3 ../../analyzer/parsed_diff_analyzer.py --input-file /tmp/${package_name}_modified.prompts.json   --adjacency-list --print-tree > $output_dir/$package_name/modified.prompts.txt
    fi

done
