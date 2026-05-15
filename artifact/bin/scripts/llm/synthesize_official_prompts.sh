#!/bin/bash

# This script synthesizes the official prompts ( 1 prompt for 1 app)
# Usage: bash synthesize_official_prompts.sh <prompts_dir>

input_dir=$1

for package_path in $input_dir/*; do
    package_name=$(basename $package_path)
    echo "Synthesizing prompts for $package_name"

    if [ -f $package_path/official_prompt.txt ]; then
        rm $package_path/official_prompt.txt
    fi

    if [ -f $package_path/added.removed.prompts.json ]; then
        if jq -e '(.ADDED_CLASSES == {} and .REMOVED_CLASSES == {})' $package_path/added.removed.prompts.json > /dev/null; then
            echo "No added or removed classes for $package_name"
        else
            echo "# Added/Removed Classes and methods" > $package_path/official_prompt.txt
            cat $package_path/added.removed.prompts.json >> $package_path/official_prompt.txt
        fi
    fi

    if [ -f $package_path/modified.prompts.txt ]; then
        if ! grep -q "Entry" $package_path/modified.prompts.txt; then
            echo "No modified call paths for $package_name, suggesting only third party classes were modified"
        else
            echo -e "\n# Modified call paths" >> $package_path/official_prompt.txt
            cat $package_path/modified.prompts.txt >> $package_path/official_prompt.txt
        fi
    fi

done