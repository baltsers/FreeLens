# Artifact structure

├── analyzer (Scripts to analyze and parse diff output)
├── bin
│   └── scripts
├── differ (Java source code that does the differencing)
│   └── src
├── doc
├── llm (Summarization-related scripts)
│   └── src
└── metadata

# Freelens usage

## Download APK files (Phase 1)

1. Download APK scraper from [Raccoon's official website](https://raccoon.onyxbits.de/) (both `dummydroid` and `raccoon` and follow its instructions to configure the scraper with your Google account

   ```
   > java -jar raccoon-4.24.0.jar -h
   usage: raccoon [-D <property=value>] [--gp-auth] [--gp-renew-gsfid]
          [--gpa-batchdetails <file>] [--gpa-bulkdetails <file>]
          [--gpa-details <package>] [--gpa-download <pn[,vc[,ot]]>]
          [--gpa-download-dir <directory>] [--gpa-search <query>]
          [--gpa-update] [-h] [-v]
   ```

   

2. Download the app with `package_name`

## Differencing Apps (Phase 2)

The usage of app differ

```
Usage: <main class> [--call-path] [--save-cg] [--max-depth=<maxDepth>]
                    --output-dir=<outputDir> --ref-apk=<refVersionApkPath>
                    [--ref-ser-cg=<refSerializedCg>]
                    [--tgt-apk=<tgtVersionApkPath>]
                    [--tgt-ser-cg=<tgtSerializedCg>]
      --call-path   Run call path analysis
      --max-depth=<maxDepth>
                    Maximum depth of the call path
      --output-dir=<outputDir>
                    Path to the output directory
      --ref-apk=<refVersionApkPath>
                    Path to the apk file of the reference version
      --ref-ser-cg=<refSerializedCg>
                    Path to the reference call graph
      --save-cg     Save the call graph
      --tgt-apk=<tgtVersionApkPath>
                    Path to the apk file of the target version
      --tgt-ser-cg=<tgtSerializedCg>
                    Path to the target call graph
```



1. Complie the differ

   ```
   cd differ/
   mvn clean compile assembly:single
   ```

2. Create and Save Call graphs for the app

   ```
       java -jar compiled-utilities/differ-1.0-SNAPSHOT-jar-with-dependencies.jar \
           --ref-apk $apk_path \
           --save-cg \
           --output-dir $cg_dir
   ```

   

3. Perform differencing on two apps

   ```
   java -jar differ-1.0-SNAPSHOT-jar-with-dependencies.jar \
       --ref-apk "$ref_apk_path" \
       --tgt-apk "$tgt_apk_path" \
       --output-dir "$output_dir" \
       --ref-ser-cg "$ref_custom_cg_path" \
       --tgt-ser-cg "$tgt_custom_cg_path"
   ```

## Summarizing Changes (Phase 3)

1. Generate the prompts from the differencing output

   ```
   bash bin/llm/gen_prompts <input_dir> <output_dir>
   bash bin/synthesize_official_prompts.sh <prompts_dir>
   ```

2. Summerize the differences from prompts generated in last step

   ```
   bash bin/llm/infer_changes.sh <input_dir> <output_dir> <model>
   ```

# Metadata

In directory `metadata`, documents the App's `package_name` and its `version_name` , `version_code` distributed in target countries